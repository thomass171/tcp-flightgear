package de.yard.threed.flightgear.core.flightgear.scenery;

import de.yard.threed.core.Util;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.FlightGearModuleScenery;
import de.yard.threed.flightgear.core.StringList;
import de.yard.threed.flightgear.core.flightgear.main.FGGlobals;
import de.yard.threed.flightgear.core.flightgear.main.FGProperties;
import de.yard.threed.flightgear.core.osg.Group;
import de.yard.threed.flightgear.core.osgdb.FilePathList;
import de.yard.threed.flightgear.core.osgdb.Registry;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.bucket.SGBucket;
import de.yard.threed.flightgear.core.simgear.scene.tsync.SGTerraSync;
import de.yard.threed.flightgear.core.simgear.scene.util.SGReaderWriterOptions;
import de.yard.threed.flightgear.core.simgear.structure.DefaultSGSubsystem;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.StringUtils;

/**
 * tilemgr.[ch]xx
 * <p>
 * Created by thomass on 07.06.16.
 */
public class FGTileMgr extends DefaultSGSubsystem {
    static Log logger = Platform.getInstance().getLog(FGTileMgr.class);

    // Tile loading state
    //enum load_state {
    static int Start = 0,
            Inited = 1,
            Running = 2;

    int/*load_state*/ state, last_state;

    SGBucket previous_bucket;
    SGBucket current_bucket;
    SGBucket pending;
    /* osg::ref_ptr<simgear::*/ SGReaderWriterOptions _options;

    double scheduled_visibility;

    /**
     * tile cache
     */
    TileCache tile_cache = new TileCache();
    SGTerraSync _terra_sync;

    //class TileManagerListener;
    //friend class TileManagerListener;
    //TileManagerListener* _listener;

    // update various queues internal queues
    //void update_queues(bool& isDownloadingScenery);

    SGPropertyNode _visibilityMeters;
    SGPropertyNode _maxTileRangeM, _disableNasalHooks;
    SGPropertyNode _scenery_loaded, _scenery_override;

    /*osg::ref_ptr<flightgear::*/ SceneryPager _pager;

    /// isType caching of expired tiles enabled or not?
    boolean _enableCache;
    boolean terrainonly = false;

    public FGTileMgr(boolean terrainonly) {
        state = Start;
        last_state = Running;
        scheduled_visibility = 100.0;
        _terra_sync = null;
        //_listener=null;
        _visibilityMeters = FGProperties.fgGetNode("/environment/visibility-m", true);
        _maxTileRangeM = FGProperties.fgGetNode("/sim/rendering/static-lod/bare", true);
        _disableNasalHooks = FGProperties.fgGetNode("/sim/temp/disable-scenery-nasal", true);
        _scenery_loaded = FGProperties.fgGetNode("/sim/sceneryloaded", true);
        _scenery_override = FGProperties.fgGetNode("/sim/sceneryloaded-override", true);
        // 2.6.24 only have one pager that is here.
        _pager = new SceneryPager();//FGScenery.getPagerSingleton();
        _enableCache = true;
        this.terrainonly = terrainonly;
    }

    // Initialize the Tile Manager
    @Override
    public void init() {
        reinit();
    }

    @Override
    public void reinit() {
        logger.info("Initializing Tile Manager subsystem.");
        _terra_sync = null;//TODO static_cast < simgear::SGTerraSync * > (FGGlobals.globals.get_subsystem("terrasync"));

        // drops the previous options reference
        _options = new SGReaderWriterOptions();
        //TODO _listener = new TileManagerListener(this);

        materialLibChanged();
        _options.setPropertyNode(FGGlobals.globals.get_props());

        FilePathList fp = _options.getDatabasePathList();
        StringList sc = FGGlobals.globals.get_fg_scenery();
        fp.clear();
        // std::copy (sc.begin(), sc.end(), back_inserter(fp));
        // Reiehenfolge?
        for (String s : sc) {
            fp.add(s);
        }
        //28.6.17: Tja, bundle? TODO ??_options.setPluginStringData("SimGear::FG_ROOT", FGGlobals.globals.get_fg_root());

        if (_terra_sync != null) {
            _options.setPluginStringData("SimGear::TERRASYNC_ROOT", FGProperties.fgGetString("/sim/terrasync/scenery-dir"));
        }
        if (terrainonly) {
            _options.pluginstringdata.put("SimGear::FG_ONLY_TERRAIN", "ON");
        }
        
        if (!_disableNasalHooks.getBoolValue()) {
            //TODO _options.setModelData(new FGNasalModelDataProxy);
        }

        if (state != Start) {
            // protect against multiple scenery reloads and properly reset flags,
            // otherwise aircraft fall through the ground while reloading scenery
            if (_scenery_loaded.getBoolValue() == false) {
                logger.info(/*SG_LOG( SG_TERRAIN, SG_INFO,*/ "/sim/sceneryloaded already false, avoiding duplicate re-init of tile manager");
                return;
            }
        }

        _scenery_loaded.setBoolValue(false);
        FGProperties.fgSetDouble("/sim/startup/splash-alpha", 1.0);

        materialLibChanged();

        // remove all old scenery nodes from scenegraph and clear cache
        Group group = FlightGearModuleScenery.getInstance().get_scenery().get_terrain_branch();
        group.removeChildren(0, group.getNumChildren());
        tile_cache.init();

        // clear OSG cache, except on initial start-up
        if (state != Start) {
            Registry.getInstance().clearObjectCache();
        }

        state = Inited;

        if (previous_bucket != null) {
            previous_bucket.make_bad();
        }
        if (current_bucket != null) {
            current_bucket.make_bad();
        }

        scheduled_visibility = 100.0;

        // force an update now
        update(0.0);

    }

    /**
     * Update the various queues maintained by the tilemgr (private
     * internal function, do not call directly.)
     * <p>
     * public for testen
     * 30.5.24: Is this really loading scnery (via SceneryPager)? apparently
     */
    public void update_queues(boolean isDownloadingScenery) {
        //TODO osg::FrameStamp * framestamp                = FGGlobals.globals.get_renderer().getViewer().getFrameStamp();
        double current_time = -1;//TODO framestamp.getReferenceTime();
        double vis = _visibilityMeters.getDoubleValue();
        TileEntry e;
        int loading = 0;
        int sz = 0;

        tile_cache.set_current_time(current_time);

        tile_cache.reset_traversal();
        while (!tile_cache.at_end()) {
            e = tile_cache.get_current();
            if (e != null) {
                // Prepare the ssg nodes corresponding to each tile.
                // Set the ssg transform and update it's range selector
                // based on current visibilty
                // 20.12.17: Das Laden des Bundle per queueRequest() geht letztendlich async. Darum neues Flag, um mehrfach queuen zu vermeiden.
                // wie FG das hinbekommt??
                e.prep_ssg_node((float) vis);

                if (!e.is_loaded() && !e.queued) {
                    boolean nonExpiredOrCurrent = !e.is_expired(current_time) || e.is_current_view();
                    boolean downloading = isTileDirSyncing(e.tileFileName);
                    isDownloadingScenery |= downloading;
                    if (!downloading && nonExpiredOrCurrent) {
                        // schedule tile for loading with osg pager
                        _pager.queueRequest(e.tileFileName, e.getNode(), e.get_priority(), /*framestamp, e.getDatabaseRequest(),*/ _options/*.get()*/, e.get_tile_bucket().gen_base_path());
                        e.queued=true;
                        loading++;
                    }
                } // of tile not loaded case
            } else {
                logger.error(/*SG_LOG(SG_TERRAIN, SG_ALERT,*/ "Warning: empty tile in cache!");
            }
            tile_cache.next();
            sz++;
        }

        int drop_count = sz - tile_cache.get_max_cache_size();
        boolean dropTiles = false;
        if (_enableCache) {
            dropTiles = (drop_count > 0) && ((loading == 0) || (drop_count > 10));
        } else {
            dropTiles = true;
            drop_count = sz; // no limit on tiles to drop
        }

        if (dropTiles) {
            long drop_index = _enableCache ? tile_cache.get_drop_tile() :
                    tile_cache.get_first_expired_tile();
            while (drop_index > -1) {
                // schedule tile for deletion with osg pager
                TileEntry old = tile_cache.get_tile(drop_index);
                logger.debug(/*) SG_LOG(SG_TERRAIN, SG_DEBUG,*/ "Dropping:" + old.get_tile_bucket());

                tile_cache.clear_entry(drop_index);

                Util.notyet();
                /*osg::ref_ptr < osg::Object > subgraph = old.getNode();
                old.removeFromSceneGraph();
                delete old;
                // zeros out subgraph ref_ptr, so subgraph isType owned by
                // the pager and will be deleted in the pager thread.
                _pager.queueDeleteRequest(subgraph);
*/

                if (!_enableCache)
                    drop_index = tile_cache.get_first_expired_tile();
                    // limit tiles dropped to drop_count
                else if (--drop_count > 0)
                    drop_index = tile_cache.get_drop_tile();
                else
                    drop_index = -1;
            }
        } // of dropping tiles loop
    }

    /**
     * given the current lon/lat (in degrees), fill in the array of local
     * chunks.  If the chunk isn't already in the cache, then read it from
     * disk.
     */
    @Override
    public void update(double ddddummy) {
        double vis = _visibilityMeters.getDoubleValue();
        schedule_tiles_at(FGGlobals.globals.get_view_position(), vis);

        boolean waitingOnTerrasync = false;
        update_queues(waitingOnTerrasync);

        // scenery loading check, triggers after each sim (tile manager) reinit
        if (!_scenery_loaded.getBoolValue()) {
            boolean fdmInited = FGProperties.fgGetBool("sim/fdm-initialized");
            boolean positionFinalized = FGProperties.fgGetBool("sim/position-finalized");
            boolean sceneryOverride = _scenery_override.getBoolValue();


            // we are done if final position isType set and the scenery & FDM are done.
            // scenery-override can ignore the last two, but not position finalization.
            if (positionFinalized && (sceneryOverride || (isSceneryLoaded() && fdmInited))) {
                _scenery_loaded.setBoolValue(true);
                FlightGear.fgSplashProgress("");
                logger.debug("FGTileMgr::update:scenery loaded\n");
            } else {
                if (!positionFinalized) {
                    FlightGear.fgSplashProgress("finalize-position");
                } else if (waitingOnTerrasync) {
                    FlightGear.fgSplashProgress("downloading-scenery");
                } else {
                    FlightGear.fgSplashProgress("loading-scenery");
                }

                // be nice to loader threads while waiting for initial scenery, reduce to 20fps
                //TODO SGTimeStamp::sleepForMSec (50);
                logger.debug("FGTileMgr::update:Still waiting for scenery\n");
            }
        }
    }


    SGBucket get_current_bucket() {
        return current_bucket;
    }


    // Returns true if tiles around current viewer position have been loaded
    boolean isSceneryLoaded() {
        double range_m = 100.0;
        if (scheduled_visibility < range_m)
            range_m = scheduled_visibility;

        return new FGTileMgrScheduler(tile_cache, _maxTileRangeM.getDoubleValue()/*, FGGlobals.globals.get_scenery().get_terrain_branch()*/).schedule_scenery(FGGlobals.globals.get_view_position(), range_m, 0.0);
    }

    // notify the tile manahger the material library was reloaded,
    // so it can pass this through to its options object
    void materialLibChanged() {
        _options.setMaterialLib(FlightGearModuleScenery.getInstance().get_matlib());
    }


    /**
     * schedule tiles for the viewer bucket
// (FDM/AI/groundcache/... should use "schedule_scenery" instead)
     * Runs async!
     */
    public void schedule_tiles_at(SGGeod location, double range_m) {
        
        logger.debug("schedule_tiles_at location=" + location + ",range_m=" + range_m);

        current_bucket = new SGBucket(location);

        // schedule more tiles when visibility increased considerably
        // TODO Calculate tile size - instead of using fixed value (5000m)
        // TODO warum braucht FG keinen check auf null?
        if (range_m - scheduled_visibility > 5000.0 && previous_bucket != null)
            previous_bucket.make_bad();

        // SG_LOG( SG_TERRAIN, SG_DEBUG, "Updating tile list for "
        //         << current_bucket );
        FGProperties.fgSetInt("/environment/current-tile-id", (int) current_bucket.gen_index());

        // do tile load scheduling.
        // Note that we need keep track of both viewer buckets and fdm buckets.
        if (state == Running) {
            if (last_state != state) {
                //SG_LOG( SG_TERRAIN, SG_DEBUG, "State == Running" );
            }
            if (current_bucket != previous_bucket) {
                // We've moved to a new bucket, we need to schedule any
                // needed tiles for loading.
                //SG_LOG( SG_TERRAIN, SG_INFO, "FGTileMgr: at " << location << ", scheduling needed for:" << current_bucket                        << ", visbility=" << range_m);
                scheduled_visibility = range_m;
                new FGTileMgrScheduler(tile_cache, _maxTileRangeM.getDoubleValue()/*, FGGlobals.globals.get_scenery().get_terrain_branch()*/).schedule_needed(current_bucket, range_m);
            }

            // save bucket
            previous_bucket = current_bucket;
        } else if (state == Start || state == Inited) {
            //SG_LOG( SG_TERRAIN, SG_DEBUG, "State == Start || Inited" );
            // do not update bucket yet (position not valid in initial loop)
            state = Running;
            // warum braucht FG den check denn nicht?
            if (previous_bucket != null) {
                previous_bucket.make_bad();
            }
        }
        last_state = state;
    }

    boolean isTileDirSyncing(String tileFileName) {
        if (_terra_sync == null) {
            return false;
        }

        String nameWithoutExtension = StringUtils.substring(tileFileName, 0, StringUtils.length(tileFileName) - 4);
        long bucketIndex = Long.parseLong(nameWithoutExtension);
        SGBucket bucket = new SGBucket(bucketIndex);

        return false;//TODO _terra_sync.isTileDirPending(bucket.gen_base_path());
    }

    public SceneryPager getPager() {
        return _pager;
    }
}


