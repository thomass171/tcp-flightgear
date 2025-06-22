package de.yard.threed.flightgear.core.flightgear.scenery;

import de.yard.threed.core.Util;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.flightgear.LoaderOptions;
import de.yard.threed.flightgear.TerrainHelper;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.SGLoaderOptions;
import de.yard.threed.flightgear.core.osgdb.Options;
import de.yard.threed.flightgear.core.osgdb.osgDB;
import de.yard.threed.flightgear.core.simgear.bucket.SGBucket;
import de.yard.threed.flightgear.core.simgear.scene.tgdb.ReaderWriterSTG;
import de.yard.threed.flightgear.core.simgear.scene.util.SGReaderWriterOptions;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.core.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thomass on 22.08.16.
 */
public class SceneryPager {//extends lic osgDB::DatabasePager
    static Log logger = Platform.getInstance().getLog(SceneryPager.class);
    public List<String> loadedBundle = new ArrayList<String>();
    public List<String> failedBundle = new ArrayList<String>();

    public SceneryPager() {
    }
        /*SceneryPager( SceneryPager  rhs){
                
            }*/

    // Unhide DatabasePager::requestNodeFile
    //using osgDB::DatabasePager::requestNodeFile;

    /**
     * FG-DIFF less parameter, no multithreading
     * Queue STG file for async loading.  The model hierarchy will end up in destinationnode.
     * <p>
     * filename is pathless, eg.:3056394.stg
     * 26.4.17:basepath added for bundle
     * 12.6.17: jetzt doch multithreaded per async loading.TODO: das Einhaengen in die destinationnode muss mal validiert werden, evtl ueber queue?
     */
    public void queueRequest(String fileName, SceneNode/*Group*/ destinationnode, float priority, /*osg::FrameStamp* frameStamp,  osg::ref_ptr<osg::Referenced>& databaseRequest,*/        Options options,
                             String basepath) {
        SceneNode n = null;
        if (basepath != null) {
            String bundlename = FlightGear.getBucketBundleName(StringUtils.substringBeforeLast(fileName, ".stg"));
            Bundle bundle = BundleRegistry.getBundle(bundlename);
            if (bundle == null) {
                AbstractSceneRunner.instance.loadBundle(bundlename, (Bundle b1) -> {
                    if (ReaderWriterSTG.terrainloaddebuglog) {
                        logger.debug("bundle loaded for " + fileName);
                    }
                    if (b1 == null) {
                        failedBundle.add(bundlename);
                    } else {
                        loadedBundle.add(bundlename);
                    }
                    SGLoaderOptions opt = new SGLoaderOptions();
                    opt.materialLib = ((SGReaderWriterOptions) options).getMaterialLib();
                    opt.setPropertyNode(((SGReaderWriterOptions) options).getPropertyNode());
                    SceneNode result = loadBucketByStg(fileName, options, opt);
                    destinationnode.attach(result);
                });
            } else {
                logger.warn("duplicate stg loading??");
            }

        } else {
            n = osgDB.readNodeFile(null, fileName, options);
            if (n != null) {
                destinationnode.attach(n);
            }
        }
    }

    /**
     * Extracted from above for reusability.
     * Always returns a node, in case of errors a dummy tile.
     */
    public static SceneNode loadBucketByStg(String fileName, Options options, LoaderOptions opt) {
        //8.6.17: STG is loaded sync (not via Registry/readnode etc.), while its content will be loaded async again.
        //4.1.18: per GLTF.
        opt.usegltf = true;
        /*BuildResult*/
        // 30.8.24: Now with shared
        boolean ignoreshared = false;
        SceneNode result = new ReaderWriterSTG().build(fileName, options, opt, ignoreshared);
        if (result != null /*&& result.getNode() != null*/) {
            return result;
        }
        // 5.6.25 ReaderWriterSTG might have decided to load water.
        // 7.5.25 FG apparently does nothing in this situation and will just have 'water'. Not sure how that is implemented.
        // Needs to be in a 'terrain' subtree for providing elevation. For now just create a green plane as dummy tile.
        // Retrieve location via bucket index.
        logger.warn("Tile " + fileName + " not found. Using dummy terrain");
        long bindex = Util.parseLong(StringUtils.substringBefore(fileName, ".stg"));
        SGBucket bucket = new SGBucket(bindex);
        SceneNode planeNode = TerrainHelper.buildDummyTile(bucket);
        return planeNode;
    }

    // This isType passed a ref_ptr so that it can "take ownership" of the
    // node to delete and decrement its refcount while holding the
    // lockEntity on the delete list.
    //void queueDeleteRequest(osg::ref_ptr<osg::Object>& objptr);
    //virtual void signalEndFrame();

    //void clearRequests();

    // Queue up file requests until the end of the frame
/*        struct PagerRequest
        {
        PagerRequest() : _priority(0.0f), _databaseRequest(0) {}
        PagerRequest(const PagerRequest& rhs) :
        _fileName(rhs._fileName), _group(rhs._group),
        _priority(rhs._priority), _frameStamp(rhs._frameStamp),
        _options(rhs._options), _databaseRequest(rhs._databaseRequest) {}

        PagerRequest(const std::string& fileName, osg::Group* group,
        float priority, osg::FrameStamp* frameStamp,
        osg::ref_ptr<Referenced>& databaseRequest,
        osgDB::ReaderWriter::Options* options):
        _fileName(fileName), _group(group), _priority(priority),
        _frameStamp(frameStamp), _options(options),
        _databaseRequest(&databaseRequest)
        {}

        void doRequest(SceneryPager* pager);
        std::string _fileName;
        osg::ref_ptr<osg::Group> _group;
        float _priority;
        osg::ref_ptr<osg::FrameStamp> _frameStamp;
        osg::ref_ptr<osgDB::ReaderWriter::Options> _options;
        osg::ref_ptr<osg::Referenced>* _databaseRequest;
        };
        typedef std::vector<PagerRequest> PagerRequestList;
        PagerRequestList _pagerRequests;
        typedef std::vector<osg::ref_ptr<osg::Object> > DeleteRequestList;
        DeleteRequestList _deleteRequests;
        virtual ~SceneryPager();
        };
        }
  */
}

