package de.yard.threed.flightgear.core.flightgear.scenery;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.flightgear.core.FlightGearModuleScenery;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.flightgear.core.simgear.bucket.SGBucket;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.MathUtil2;

/**
 * Extracted parts from FGTileMgr for decoupling.
 * Temporary instances are created by FGTileMgr for loading/queueing single tiles? Or populating a TileCache entry?
 * <p>
 * Created by thomass on 22.08.16.
 */
public class FGTileMgrScheduler {
    static Log logger = Platform.getInstance().getLog(FGTileMgrScheduler.class);
    //22.3.18 avoid multiple references private Group terrain_branch;
    TileCache tile_cache;
    double _maxTileRangeM;

    public FGTileMgrScheduler(TileCache tile_cache, double _maxTileRangeM/*, Group terrain_branch*/) {
        this.tile_cache = tile_cache;
        this._maxTileRangeM = _maxTileRangeM;
        //   this.terrain_branch = terrain_branch;
    }

    /**
     * Schedules scenery for given position. Load request remains valid for given duration
     * (duration=0.0 => nothing isType loaded).
     * Used for FDM/AI/groundcache/... requests. Viewer uses "schedule_tiles_at" instead.
     * Returns true when all tiles for the given position are already loaded, false otherwise.
     * Returns true if scenery isType available for the given lat, lon position
     * within a range of range_m.
     * lat and lon are expected to be in degrees.
     * 28.6.17: Wird anscheinend nur einmal aufgerufen und dann mit false beendet. Liegt wahrscheinlich an Art der update() Einbindung. Ist wohl nur eine Initialisierung.
     * Im update ist auch schedule_tiles_at() fuer staendige Prüfung.
     */
    public boolean schedule_scenery(SGGeod position, double range_m, double duration) {
        // sanity check (unfortunately needed!)
        /*TODOif (!position.isValid())
            return false;*/
        float priority = 0.0f;
        boolean available = true;

        SGBucket bucket = new SGBucket(position);
        available = sched_tile(bucket, priority, false, duration);

        //wird staendig aufgerufen tsch_log( "schedule_scenery: Scheduling tile at bucket:\n");

        if ((!available) && (duration == 0.0)) {
            logger.debug(/*SG_LOG(SG_TERRAIN, SG_DEBUG,*/ "schedule_scenery: Scheduling tile at bucket:" + bucket + " return false");
            return false;
        }
        logger.debug("schedule_scenery: Scheduling tile at bucket:" + bucket + " with range_m " + range_m);

        //SGVec3d cartPos = SGVec3d::fromGeod (position);
        Vector3 cartPos = position.toCart();

        // Traverse all tiles required to be there for the given visibility.
        double tile_width = bucket.get_width_m();
        double tile_height = bucket.get_height_m();
        double tile_r = 0.5 * Math.sqrt(tile_width * tile_width + tile_height * tile_height);
        double max_dist = tile_r + range_m;
        double max_dist2 = max_dist * max_dist;

        int xrange = (int) Math.abs(range_m / tile_width) + 1;
        int yrange = (int) Math.abs(range_m / tile_height) + 1;

        for (int x = -xrange; x <= xrange; ++x) {
            for (int y = -yrange; y <= yrange; ++y) {

                // We have already checked for the center tile.
                if (x != 0 || y != 0) {
                    SGBucket b = bucket.sibling(x, y);
                    if (!b.isValid()) {
                        continue;
                    }

                    Vector3 centerpos = b.get_center().toCart();
                    double distance2 = MathUtil2.distSqr(cartPos, centerpos);
                    //System.out.println("x="+x+",y="+y+",distance="+Math.sqrt(distance2)/1000+",centerpos="+SGGeod.fromCart(centerpos).toWGS84decimalString()+"bucket.center="+bucket.get_center().toWGS84decimalString());
                    // Do not ask if it isType just the next tile but way out of range.
                    if (distance2 <= max_dist2) {
                        //logger.debug("schedule_scenery: x=%d,y=%d\n", x, y);

                        available &= sched_tile(b, priority, false, duration);
                        if ((!available) && (duration == 0.0))
                            return false;
                    }
                }
            }
        }

        return available;
    }

    /**
     * schedule a tile for loading, keep request for given amount of time.
     * Returns true if tile is already loaded.
     * <p>
     * 28.6.17:Wird auch verwendet, um zu pruefen obs ein Tile gibt. Wenn nicht, wird ein neuer "empty" entry im cache angelegt.
     */
    boolean sched_tile(SGBucket b, double priority, boolean current_view, double duration) {
        // see if tile already exists in the cache
        TileEntry t = tile_cache.get_tile(b);
        // too often logger.debug("sched_tile " + b.gen_index() + ",current_view=" + current_view + ",t=" + t);
        if (t == null) {
            // create a new entry
            t = new TileEntry(b);
            // insert the tile into the cache, update will generate load request
            if (tile_cache.insert_tile(t)) {
                // Attach to scene graph
                t.addToSceneGraph(/*terrain_branch*/FlightGearModuleScenery.getInstance().get_scenery().get_terrain_branch());
            } else {
                // insert failed (cache full with no available entries to
                // delete.)  Try again later
                //delete t;
                logger.warn("sched_tile: insert failed. Cache full?");
                return false;
            }

            logger.debug("sched_tile: new TileEntry for bucket " + b + ", cache size= " + (int) tile_cache.get_size());
        } else {
            if (t.is_loaded()) {
                logger.warn("sched_tile for " + b.gen_index() + " which claims to be loaded already. Gap in terrain?");
            }
        }

        // update tile's properties
        tile_cache.request_tile(t, (float) priority, current_view, duration);

        return t.is_loaded();
    }

    /**
     * schedule needed buckets for the current viewer position for loading,
     * keep request for given amount of time
     */
    public void schedule_needed(SGBucket curr_bucket, double vis) {
        // sanity check (unfortunately needed!)
        if (!curr_bucket.isValid()) {
            logger.error("Attempting to schedule tiles for invalid bucket");
            return;
        }

        double tile_width = curr_bucket.get_width_m();
        double tile_height = curr_bucket.get_height_m();
        logger.info("scheduling needed tiles for " + curr_bucket + ", tile_cache.size=" + tile_cache.get_size());

        double tileRangeM = Math.min(vis, _maxTileRangeM/*.getDoubleValue()*/);
        int xrange = (int) (tileRangeM / tile_width) + 1;
        int yrange = (int) (tileRangeM / tile_height) + 1;
        if (xrange < 1) {
            xrange = 1;
        }
        if (yrange < 1) {
            yrange = 1;
        }

        // make the cache twice as large to avoid losing terrain when switching
        // between aircraft and tower views.
        // 25.6.25: FG-DIFF No longer set cache size but rely on default. As we have teleport avoid reducing the cache size too large . However needs a solution one day.
        // tile_cache.set_max_cache_size((2 * xrange + 2) * (2 * yrange + 2) * 2);

        // clear flags of all tiles belonging to the previous viewer set 
        tile_cache.clear_current_view();

        // update timestamps, so all tiles scheduled now are *newer* than any tile previously loaded
        /*TODO osg::FrameStamp * framestamp
                = FGGlobals.globals.get_renderer().getViewer().getFrameStamp();
        tile_cache.set_current_time(framestamp.getReferenceTime());*/

        //SGBucket b;

        int x, y;

        /* schedule all tiles, use distance-based loading priority,
         * so tiles are loaded in innermost-to-outermost sequence. */
        for (x = -xrange; x <= xrange; ++x) {
            for (y = -yrange; y <= yrange; ++y) {
                SGBucket b = curr_bucket.sibling(x, y);
                if (!b.isValid()) {
                    continue;
                }

                float priority = (-1.0f) * (x * x + y * y);
                sched_tile(b, priority, true, 0.0);

                /*TODOif (_terra_sync != null) {
                     _terra_sync.scheduleTile(b);
                }*/
            }
        }
    }

}
