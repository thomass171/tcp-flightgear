package de.yard.threed.flightgear;

import de.yard.threed.core.loader.InvalidDataException;
import de.yard.threed.core.loader.LoaderGLTF;
import de.yard.threed.core.loader.PortableModelList;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.flightgear.core.FlightGearModuleScenery;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.Degree;
import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Quaternion;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.core.Vector3;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.flightgear.scenery.FGTileMgrScheduler;
import de.yard.threed.flightgear.core.flightgear.scenery.SceneryPager;
import de.yard.threed.flightgear.core.flightgear.scenery.TileCache;
import de.yard.threed.flightgear.core.osg.Group;
import de.yard.threed.flightgear.core.osg.Node;
import de.yard.threed.flightgear.core.osgdb.Options;
import de.yard.threed.flightgear.testutil.FgFullTestFactory;
import de.yard.threed.flightgear.testutil.ModelAssertions;

import de.yard.threed.flightgear.core.simgear.geodesy.FgMath;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.flightgear.core.simgear.bucket.SGBucket;

import de.yard.threed.flightgear.core.simgear.scene.tgdb.Obj;
import de.yard.threed.flightgear.core.simgear.scene.tgdb.ReaderWriterSTG;
import de.yard.threed.flightgear.core.simgear.scene.tgdb.SGReaderWriterBTG;
import de.yard.threed.flightgear.core.simgear.scene.util.SGReaderWriterOptions;



import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.core.buffer.ByteArrayInputStream;
import de.yard.threed.core.testutil.Assert;
import de.yard.threed.traffic.WorldGlobal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests oberhalb von material und SGTileGeometry, z.B. Obj, SGBucket, ReaderWriterSTG,FGTileMgrScheduler, SQQaut, OsgMath
 * <p>
 * FGTileMgr ist in FlightGeartest, weil er zu stark eingebunden ist.
 * <p>
 * Geht auch auf die echte Installation, nicht src/test/resources. "3056410" ist das Referenz BTG (Dahlem). Erfordert, dass FG dort mal gestartet wurde, damit es da ist.
 * 6.7.17: Wegene Entkopplung geht es jetzt - zumindest teilweise - doch auf src/test/resources.
 * <p>
 * Auch fuer Tests der Scenery Bundles
 * 30.9.19: TestFactory laedt schon FG Teile.
 *
 * <p>
 * Created by thomass on 09.08.16.
 */
public class SceneryTest {
    static Platform platform = FgFullTestFactory.initPlatformForTest(new HashMap<String, String>());

    /**
     */
    @Test
    public void testLoadBTG1() {
        //TestFactory.loadBundleSync(SGMaterialLib.BUNDLENAME);
        // bis 7, weil dort das Material geladen wird.
        //FlightGear.init(7, FlightGear.argv);
        //FlightGearModuleBasic.init(null, null);
        //FlightGearModuleScenery.init(false);
        //12.9.23: BTG should no longer considered a use case in Obj.SGLoadBTG()?
        LoaderOptions loaderoptions = new LoaderOptions(FlightGearModuleScenery.getInstance().get_matlib());
        loaderoptions.usegltf=false;
        Node node = Obj.SGLoadBTG(new BundleResource(BundleRegistry.getBundle("test-resources"), FlightGear.refbtg), null, loaderoptions);
        assertNotNull(node);
        ModelAssertions.assertRefbtgNode(node, "terrain/3056410.btg");

        //und jetzt GLTF. Suffix remains ".btg"
        loaderoptions.usegltf = true;
        node = Obj.SGLoadBTG(new BundleResource(BundleRegistry.getBundle("test-resources"), FlightGear.refbtg), null, loaderoptions);
        assertNotNull(node);
        ModelAssertions.assertRefbtgNode(node, "terrain/3056410.gltf");
    }

    @Test
    public void testLoadBTG2() {
        //TestFactory.loadBundleSync(SGMaterialLib.BUNDLENAME);
        //TestFactory.loadBundleSync(FlightGear.getBucketBundleName("3056410"));
        // bis 7, weil dort das Material geladen wird.
        //MA23FlightGear.init(7, FlightGear.argv);
        //FlightGearModuleBasic.init(null, null);
        //FlightGearModuleScenery.init(false);

        SGReaderWriterOptions options = new SGReaderWriterOptions();
        SceneNode result = SGReaderWriterBTG.loadBTG(new BundleResource(BundleRegistry.getBundle("test-resources"), "terrain/3056410.btg"), options, null);
        assertNotNull(result);
    }

    /**
     * wegen Materialmehrfachnutzung auch 3072816.btg testen
     */
    @Test
    public void testLoadBTG3072816() {
        //TestFactory.loadBundleSync(SGMaterialLib.BUNDLENAME);
        //FlightGear.init(7, FlightGear.argv);
       // FlightGearModuleBasic.init(null, null);
       // FlightGearModuleScenery.init(false);
        BundleResource br = new BundleResource(BundleRegistry.getBundle("test-resources"), "terrain/3072816.btg");
        BundleData ins = br.bundle.getResource(br);
        try {
            PortableModelList ppfile = new LoaderBTG(new ByteArrayInputStream(ins.b), null, new LoaderOptions(FlightGearModuleScenery.getInstance().get_matlib()), br.getFullName()).preProcess();
            ModelAssertions.assert3072816(ppfile, true);
        } catch (InvalidDataException e) {
            throw new RuntimeException(e);
        }
        SceneNode result = SGReaderWriterBTG.loadBTG(br, null, new LoaderOptions(FlightGearModuleScenery.getInstance().get_matlib()));
        assertNotNull(result);
    }

    @Test
    public void testSGBucket() {
        SGBucket bucket = new SGBucket(2958154);
        // Wert 2958154 ohne Prüfung einfach als Referenzgenommen.
        assertEquals( "2958154", bucket.gen_index_str(),"gen_index_str");
        assertEquals( "e000n50/e000n51", bucket.gen_base_path(),"gen_base_path");
        bucket = new SGBucket(SGGeod.fromGeoCoordinate(WorldGlobal.elsdorf0));
        assertEquals( "3056442", bucket.gen_index_str(),"gen_index_str");
        // Bucket für refbtg
        SGGeod pos = SGGeod.fromCart(FlightGear.refbtgcenter);
        bucket = new SGBucket(pos.getLongitudeDeg(), pos.getLatitudeDeg());
        assertEquals( "3056410", bucket.gen_index_str(),"gen_index_str");
        //Dahlem liegt auch knapp da drin
        SGBucket unterrefbtg = bucket.sibling(0, -1);
        assertEquals( "3056402", unterrefbtg.gen_index_str(),"gen_index_str");
        // Dahlem Viewpoint liegt auch suedlich ((6.531), 50.374))
        pos = SGGeod.fromGeoCoordinate(WorldGlobal.dahlem1300.location.coordinates);
        bucket = new SGBucket(pos.getLongitudeDeg(), pos.getLatitudeDeg());
        assertEquals( "3056402", bucket.gen_index_str(),"gen_index_str");
        // in unseren Breiten gibt wohl vier horizontale Tiles pro Degree. Dahlem ist in beide Richtungen das Zweite.
        assertEquals( (int) Math.round(Math.floor(0.531f / (1f / 4))), bucket.get_x(),"dahlem grid.x");
        assertEquals(13914.938f, (float) bucket.get_height_m(),"dahlem height");
        assertEquals( (int) Math.round(Math.floor(0.374f / (1f / 8))), bucket.get_y(),"dahlem grid.y");
        assertEquals( 50 + ((2f + 1f) / 8), (float) bucket.get_highest_lat(),"dahlem highest lat");
        assertEquals( 17748.787f, (float) bucket.get_width_m(),"dahlem width");
        assertEquals( "50.3125, 6.625, 0.0 m", bucket.get_center().toString(),"dahlem center");

    }

    /**
     * Auch zum Testen der TerraSync Bundle
     */
    @Test
    public void testReaderWriterSTG() {
        //MA17Registry.setReadFileCallback(ModelRegistry.getInstance());
        EngineTestFactory.loadBundleSync(FlightGear.getBucketBundleName("3072816"));
        EngineTestFactory.loadBundleSync(FlightGear.getBucketBundleName("model"));
        Bundle bundle3072816 = BundleRegistry.getBundle("Terrasync-3072816");
        assertNotNull( bundle3072816,"bundle3072816");
        Bundle bundlemodel = BundleRegistry.getBundle("Terrasync-model");
        assertNotNull( bundlemodel,"bundlemodel");
        assertNotNull( bundle3072816.getResource("Objects/e000n50/e007n50/moffett-hangar-n-211.xml"),"moffett-hangar-n-211");

        EngineTestFactory.loadBundleSync(FlightGear.getBucketBundleName("3072856"));
        Bundle bundle3072856 = BundleRegistry.getBundle("Terrasync-3072856");
        assertNotNull( bundle3072856,"bundle3072816");
        assertNotNull( bundle3072856.getResource("Objects/e000n50/e007n51/vfl_stadion.xml"),"vfl_stadion.xml");
        //der Suffix gz kommt nicht mit ins directory
        if (FlightGearSettings.customTerraSync) {
            assertTrue( bundle3072856.exists("Terrain/e000n50/e007n51/3072856.gltf"),"3072856.btg.gz");
        } else {
            assertTrue( bundle3072856.exists("Terrain/e000n50/e007n51/3072856.btg"),"3072856.btg.gz");
        }
        // Bundle 3072824 ist nicht geeignet zum Test, weil es evtl. durch EDDK Custom ueberschrieben wird. Darum 3072856. Ist aber schon oben. 
        //TestFactory.loadBundleSync(FlightGear.getBucketBundleName("3072856"));
        //Bundle bundle3072856 = BundleRegistry.getBundle("Terrasync-3072856");
        assertNotNull( bundle3072856.getResource("Objects/e000n50/e007n51/vfl_stadion.gltf"),"vfl_stadion.gltf");
        BundleResource br = new BundleResource(bundle3072856,"Objects/e000n50/e007n51/vfl_stadion.gltf");
        LoaderGLTF lf1 = null;
        try {
            lf1 = LoaderGLTF.buildLoader(br, br.path);
        } catch (InvalidDataException e) {
            Assert.fail(e.getMessage());
        }
        PortableModelList ppfile = lf1.preProcess();

        LoaderOptions opt = new LoaderOptions();
        opt.usegltf = true;
        Node node = Obj.SGLoadBTG(new BundleResource(bundle3072816, "Terrain/e000n50/e007n50/3072816.btg"), null, opt);
        assertNotNull( node);

        // Das 3072816.stg hat ein paar mehr Zeilen als bloss das btg. Gibt es einmal in Objects und einmal in Terrain. 
        // Beide muessen gelesen werden.
        Options options = new Options();

        Group rr = new ReaderWriterSTG().build("3072816.stg", options, opt);
        if (rr/*.getNode()*/ == null) {
            Assert.fail("node isType null. 3072816.stg failed");
        }
    }

    @Test
    public void testFGTileMgrScheduler() {
        String fghome = Platform.getInstance().getConfiguration().getString("FG_HOME");

        // Elsdorf muesste als Tile komplett vorliegen. Ein Tile hat etwa die Masse 13kmx17km
        SGGeod elsdorf = SGGeod.fromGeoCoordinate(WorldGlobal.elsdorf0);
        double _maxTileRangeM = WorldGlobal.km(15);
        TileCache tile_cache = new TileCache();
        //Group terrain_branch = new Group();
        FGTileMgrScheduler fgTileMgrScheduler = new FGTileMgrScheduler(tile_cache, _maxTileRangeM/*, terrain_branch*/);
        double duration = 1;
        fgTileMgrScheduler.schedule_scenery(elsdorf, _maxTileRangeM, duration);
        // Mit der dustance oben so austariert, dass 3x3 geladen werden.
        assertEquals( 9, tile_cache.get_size());

        SceneryPager sceneryPager = new SceneryPager();
        //MA17ModelRegistry.getInstance().registerCallbacks();
        //MA17Registry.setReadFileCallback(ModelRegistry.getInstance());
        Options options = new Options();
        options.getDatabasePathList().add(fghome + "/TerraSync");

        tile_cache.reset_traversal();
        /*15.9.17: TODO Bundle nutzen
        while (!tile_cache.at_end()) {
            TileEntry e = tile_cache.get_current();
            //TODO sceneryPager.queueRequest(e.tileFileName, terrain_branch, 0, options, "basepathquatsch");
            //TODO tile_cache.next();
        }*/

    }

    /**
     * Test fuer korrekte Rotation bei Positionierung von Objekten.
     */
    @Test
    public void testRotation() {
        SGGeod location = SGGeod.fromDegM(new Degree(6.600000), new Degree(50.425833), 566.950000);
        Quaternion rotation = /*matrix*/ FgMath.makeZUpFrame(location);
        //matrix.preMultRotate(osg::Quat (SGMiscd::deg2rad (i._hdg), osg::Vec3 (0, 0, 1)));
        //matrix.preMultRotate(osg::Quat (SGMiscd::deg2rad (i._pitch), osg::Vec3 (0, 1, 0)));
        //matrix.preMultRotate(osg::Quat (SGMiscd::deg2rad (i._roll), osg::Vec3 (1, 0, 0)));
        Quaternion q = Quaternion.buildQuaternionFromAngleAxis((float)new Degree( 180).toRad(), new Vector3(0, 0, 1));
        rotation = rotation.multiply((q));
        Matrix4 m = Matrix4.buildTransformationMatrix(location.toCart(), rotation);
        
        /*SGAnimation::animate type=spin
        ModelRegistry::readNode (56) completed
        fromLonLatRad: lon=0.115192, lat=0.880097, quaternion=(0.054165,-0.939397,0.019487,0.337964)
        DelayLoadReadFileCallback.readNode matrix for Models/Power/windturbine.xml: ,hdg=180.000000,pitch=0.000000,roll=0.000000
                -0.765692 -0.088594  0.637077  0.000000
        0.114937 -0.993373  0.000000  0.000000
        0.632854  0.073224  0.770801  0.000000
        4044842.454362 468004.239693 4893537.705680  1.000000
        ReaderWriterSTG.readNode calling readRefNodeFile for name Models/Power/windturbine.xml
        ModelRegistry::readNode(58) filename=Models/Power/windturbine.xml*/
        Matrix4 expected = new Matrix4(-0.765692, 0.114937, 0.632854, 4044842.479915995,
                -0.088594, -0.993373, 0.073224, 468004.239693,
                0.637077, 0.000000, 0.770801, 4893537.684418591,
                0.000000, 0, 0, 1.000000);
        TestUtils.assertMatrix4(expected, m);
    }

    /**
     * Das stg enthaelt zwei Airports, u.a. EDDK
     * 9.6.17
     */
    @Test
    public void testSTG3072816() {
       //30.9.19  FlightGear.setupRegistry();
        EngineTestFactory.loadBundleSync(BundleRegistry.TERRAYSYNCPREFIX + "3072816");
        EngineTestFactory.loadBundleSync(BundleRegistry.TERRAYSYNCPREFIX + "model");
        SceneryPager sceneryPager = new SceneryPager();
        //ModelRegistry.getInstance().registerCallbacks();
        //Registry.setReadFileCallback(ModelRegistry.getInstance());
        SGReaderWriterOptions options = new SGReaderWriterOptions();
        SceneNode node = new SceneNode();
        sceneryPager.queueRequest("3072816.stg", node, 0, options, "e000n50/e007n50");

    }


}
