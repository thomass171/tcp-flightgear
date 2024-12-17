package de.yard.threed.flightgear;

import de.yard.threed.core.BooleanHolder;
import de.yard.threed.core.BuildResult;
import de.yard.threed.core.GeneralParameterHandler;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.loader.InvalidDataException;
import de.yard.threed.core.loader.LoaderGLTF;
import de.yard.threed.core.loader.PortableModel;
import de.yard.threed.core.loader.PreparedModel;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.Texture;
import de.yard.threed.engine.TexturePool;
import de.yard.threed.engine.platform.ResourceLoaderFromBundle;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.ModelLoader;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.testutil.TestHelper;
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
import de.yard.threed.flightgear.core.simgear.scene.material.Effect;
import de.yard.threed.flightgear.core.simgear.scene.material.MakeEffect;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterial;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterialCache;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterialLib;
import de.yard.threed.flightgear.core.simgear.scene.model.ACProcessPolicy;
import de.yard.threed.flightgear.core.simgear.scene.model.SGAnimation;
import de.yard.threed.flightgear.core.simgear.scene.model.SGModelLib;
import de.yard.threed.flightgear.testutil.FgTestFactory;
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
import de.yard.threed.flightgear.testutil.NodeAssertions;
import de.yard.threed.traffic.WorldGlobal;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * High Level Tests for material und SGTileGeometry, z.B. Obj, SGBucket, ReaderWriterSTG,FGTileMgrScheduler, SQQaut, OsgMath
 * <p>
 * FGTileMgr ist in FlightGeartest, weil er zu stark eingebunden ist.
 * <p>
 * Geht auch auf die echte Installation, nicht src/test/resources. "3056410" ist das Referenz BTG (Dahlem). Erfordert, dass FG dort mal gestartet wurde, damit es da ist.
 * 6.7.17: Wegene Entkopplung geht es jetzt - zumindest teilweise - doch auf src/test/resources.
 * <p>
 * Also for tests of Scenery Bundles
 * 30.9.19: TestFactory laedt schon FG Teile.
 * 22.7.24: LoaderBTG tests should move to tools?
 * <p>
 * Created by thomass on 09.08.16.
 */
@Slf4j
public class SceneryTest {

    @BeforeEach
    void setup() {
        FgTestFactory.initPlatformForTest(true, true, true);
    }

    /**
     *
     */
    @Test
    public void testLoadRefBTG() throws Exception {
        Obj obj = new Obj();

        Texture.resetTexturePool();
        //12.9.23: BTG should no longer considered a use case in Obj.SGLoadBTG()?
        LoaderOptions loaderoptions = new LoaderOptions(FlightGearModuleScenery.getInstance().get_matlib());
        final BooleanHolder validated = new BooleanHolder(false);
        /*12.11.24 indeed
        loaderoptions.usegltf = false;

        final BooleanHolder validated = new BooleanHolder(false);
        obj.SGLoadBTG(new BundleResource(BundleRegistry.getBundle("test-resources"), FlightGear.refbtg), null, loaderoptions, new GeneralParameterHandler<Node>() {
            @Override
            public void handle(Node node) {
                assertNotNull(node);
                ModelAssertions.assertRefbtgNode(node, "terrain/3056410.btg");
                validated.setValue(true);
            }
        });
        TestUtils.waitUntil(() -> {
            TestHelper.processAsync();
            return validated.getValue();
        }, 10000);*/

        Texture.resetTexturePool();
        // And now as GLTF. Suffix remains ".btg"
        loaderoptions.usegltf = true;
        validated.setValue(false);
        obj.SGLoadBTG(new BundleResource(BundleRegistry.getBundle("test-resources"), FlightGear.refbtg), null, loaderoptions, new GeneralParameterHandler<Node>() {
            @Override
            public void handle(Node node) {
                assertNotNull(node);
                ModelAssertions.assertRefbtgNode(node, "terrain/3056410.gltf");
                validated.setValue(true);
            }
        });
        TestUtils.waitUntil(() -> {
            TestHelper.processAsync();
            return validated.getValue();
        }, 10000);
    }

    @Test
    public void testLoadBTG3056410() throws Exception {

        SGReaderWriterOptions options = new SGReaderWriterOptions();
        LoaderOptions loaderoptions = new LoaderOptions(FlightGearModuleScenery.getInstance().get_matlib());
        // 12.1.24 Now longer pure BTG but only GLTF. Suffix remains ".btg"
        loaderoptions.usegltf = true;
        final BooleanHolder validated = new BooleanHolder(false);
        SGReaderWriterBTG.loadBTG(new BundleResource(BundleRegistry.getBundle("test-resources"), "terrain/3056410.btg"), options, loaderoptions, new GeneralParameterHandler<SceneNode>() {
            @Override
            public void handle(SceneNode result) {
                assertNotNull(result);
                validated.setValue(true);
            }
        });

        TestUtils.waitUntil(() -> {
            TestHelper.processAsync();
            return validated.getValue();
        }, 10000);
    }

    // all effects except "model-transparent"
    public static int INITIAL_EFFECTS = 16;

    /**
     * wegen Materialmehrfachnutzung auch 3072816.btg testen.
     * Effect "Effects/cropgrass" (mapped to texture "Textures/Terrain/dry_pasture4.png") is one of the effects used in 3072816.btg.
     * It is defined in global-summer.xml:
     * <pre>
     *     <material>
     *     <effect>Effects/cropgrass</effect>
     *     <name>Grassland</name>
     * 	    <texture-set>
     *          <texture>Terrain/dry_pasture4.png</texture>
     * 	        <texture n="12">Terrain/tundra-hawaii-green.png</texture>
     * 	   </texture-set>
     *     <xsize>2000</xsize>
     *     <ysize>2000</ysize>
     *     <light-coverage>2000000.0</light-coverage>
     * ...
     * </pre>
     */
    @Test
    public void testLoadBTG3072816() throws Exception {

        assertEquals(INITIAL_EFFECTS, MakeEffect.effectMap.size());
        assertSGMaterial("Grassland", false);

        // use suffix 'btg' even though GLTF is loaded
        BundleResource br = new BundleResource(BundleRegistry.getBundle("test-resources"), "terrain/3072816.btg");

        Texture.resetTexturePool();
        LoaderOptions loaderOptions = new LoaderOptions(FlightGearModuleScenery.getInstance().get_matlib());
        loaderOptions.usegltf = true;
        final BooleanHolder validated = new BooleanHolder(false);
        SGReaderWriterBTG.loadBTG(br, null, loaderOptions, new GeneralParameterHandler<SceneNode>() {
            @Override
            public void handle(SceneNode result) {
                assertNotNull(result);
                // does not exist yet?  ModelAssertions.assertBTG3072816(result);
                validated.setValue(true);
            }
        });

        TestUtils.waitUntil(() -> {
            TestHelper.processAsync();
            return validated.getValue();
        }, 10000);

        assertEquals(INITIAL_EFFECTS, MakeEffect.effectMap.size());
        assertEquals(16, Texture.texturePoolSize());
        // Why doesn't the pool contain the full name "Textures/Terrain/dry_pasture4.png"?
        assertTrue(Texture.hasTexture("dry_pasture4.png"), "texture");
        assertSGMaterial("Grassland", true);

        // only reading the BTG doesn't add it to world.
        log.debug(Scene.getCurrent().getWorld().dump("  ", 0));

    }

    @Test
    public void testSGBucket() {
        SGBucket bucket = new SGBucket(2958154);
        // Wert 2958154 ohne Prüfung einfach als Referenzgenommen.
        assertEquals("2958154", bucket.gen_index_str(), "gen_index_str");
        assertEquals("e000n50/e000n51", bucket.gen_base_path(), "gen_base_path");
        bucket = new SGBucket(SGGeod.fromGeoCoordinate(WorldGlobal.elsdorf0));
        assertEquals("3056442", bucket.gen_index_str(), "gen_index_str");
        // Bucket für refbtg
        SGGeod pos = SGGeod.fromCart(FlightGear.refbtgcenter);
        bucket = new SGBucket(pos.getLongitudeDeg(), pos.getLatitudeDeg());
        assertEquals("3056410", bucket.gen_index_str(), "gen_index_str");
        //Dahlem liegt auch knapp da drin
        SGBucket unterrefbtg = bucket.sibling(0, -1);
        assertEquals("3056402", unterrefbtg.gen_index_str(), "gen_index_str");
        // Dahlem Viewpoint liegt auch suedlich ((6.531), 50.374))
        pos = SGGeod.fromGeoCoordinate(WorldGlobal.dahlem1300.location.coordinates);
        bucket = new SGBucket(pos.getLongitudeDeg(), pos.getLatitudeDeg());
        assertEquals("3056402", bucket.gen_index_str(), "gen_index_str");
        // in unseren Breiten gibt wohl vier horizontale Tiles pro Degree. Dahlem ist in beide Richtungen das Zweite.
        assertEquals((int) Math.round(Math.floor(0.531f / (1f / 4))), bucket.get_x(), "dahlem grid.x");
        assertEquals(13914.938f, (float) bucket.get_height_m(), "dahlem height");
        assertEquals((int) Math.round(Math.floor(0.374f / (1f / 8))), bucket.get_y(), "dahlem grid.y");
        assertEquals(50 + ((2f + 1f) / 8), (float) bucket.get_highest_lat(), "dahlem highest lat");
        assertEquals(17748.787f, (float) bucket.get_width_m(), "dahlem width");
        assertEquals("50.3125, 6.625, 0.0 m", bucket.get_center().toString(), "dahlem center");

    }

    /**
     * 3072816.stg contains airports EDDK (only terrain, objects are in 3072824) and EDKB
     * Also for testing TerraSync Bundle
     */
    @Test
    public void testSTG3072816() throws Exception {
        //MA17Registry.setReadFileCallback(ModelRegistry.getInstance());
        EngineTestFactory.loadBundleAndWait(FlightGear.getBucketBundleName("3072816"));
        //25.8.24 "TerraSync-model" should be "delayed"!
        EngineTestFactory.loadBundleAndWait(FlightGear.getBucketBundleName("model"), true);
        Bundle bundle3072816 = BundleRegistry.getBundle("Terrasync-3072816");
        assertNotNull(bundle3072816, "bundle3072816");
        Bundle bundlemodel = BundleRegistry.getBundle("Terrasync-model");
        assertNotNull(bundlemodel, "bundlemodel");
        assertNotNull(bundle3072816.getResource("Objects/e000n50/e007n50/moffett-hangar-n-211.xml"), "moffett-hangar-n-211");

        String beaconGLTF = "Models/Airport/beacon.gltf";
        assertNull(bundlemodel.getResource(beaconGLTF));

        /* 8.10.23:Commented because tile 3072856 isn't content of project
        EngineTestFactory.loadBundleSync(FlightGear.getBucketBundleName("3072856"));
        Bundle bundle3072856 = BundleRegistry.getBundle("Terrasync-3072856");
        assertNotNull(bundle3072856, "bundle3072816");
        assertNotNull(bundle3072856.getResource("Objects/e000n50/e007n51/vfl_stadion.xml"), "vfl_stadion.xml");
        //der Suffix gz kommt nicht mit ins directory
        if (FlightGearSettings.customTerraSync) {
            assertTrue(bundle3072856.exists("Terrain/e000n50/e007n51/3072856.gltf"), "3072856.btg.gz");
        } else {
            assertTrue(bundle3072856.exists("Terrain/e000n50/e007n51/3072856.btg"), "3072856.btg.gz");
        }
        // Bundle 3072824 ist nicht geeignet zum Test, weil es evtl. durch EDDK Custom ueberschrieben wird. Darum 3072856. Ist aber schon oben. 
        //TestFactory.loadBundleSync(FlightGear.getBucketBundleName("3072856"));
        //Bundle bundle3072856 = BundleRegistry.getBundle("Terrasync-3072856");
        assertNotNull(bundle3072856.getResource("Objects/e000n50/e007n51/vfl_stadion.gltf"), "vfl_stadion.gltf");
        BundleResource br = new BundleResource(bundle3072856, "Objects/e000n50/e007n51/vfl_stadion.gltf");
        LoaderGLTF lf1 = null;
        try {
            lf1 = LoaderGLTF.buildLoader(br, br.path);
        } catch (InvalidDataException e) {
            Assert.fail(e.getMessage());
        }
        PortableModel ppfile = lf1.preProcess();

        LoaderOptions opt = new LoaderOptions();
        opt.usegltf = true;
        Obj obj = new Obj();
        Node node = obj.SGLoadBTG(new BundleResource(bundle3072816, "Terrain/e000n50/e007n50/3072816.btg"), null, opt);
        assertNotNull(node);
        end of tile3072856 test */

        // 3072816.stg has a few lines more than just btg. exists twice (in Objects and in Terrain).
        // Both should be loaded. TODO check
        Options options = new Options();
        LoaderOptions opt = new LoaderOptions();
        opt.usegltf = true;

        Group rr = new ReaderWriterSTG().build("3072816.stg", options, opt, false);
        if (rr/*.getNode()*/ == null) {
            Assert.fail("node isType null. 3072816.stg failed");
        }

        // Probably many many GLTFS are queued to be loaded
        TestUtils.waitUntil(() -> {
            TestHelper.processAsync();
            return AbstractSceneRunner.getInstance().futures.size() == 0;
        }, 31000);

        // only reading the STG doesn't add it to world. XMLs are loaded sync and available immediately, but for GLTFs we had to wait.
        log.debug(rr.dump("  ", 0));

        NodeAssertions.assertSTG3072816(rr);

        // used/needed parts of delayed bundle should now be loaded. Check only one
        assertNotNull(bundlemodel.getResource(beaconGLTF));

        // Cache should be used for windturbine, windsock, beacon. 25.9.24 and 'Models/Airport/light-pole-gray-38m.gltf'
        // 25.11.24: Now also "windsock_lit".
        assertEquals(5, SGModelLib.preparedModelCache.cache.size());
        PreparedModel preparedModelBeacon = SGModelLib.preparedModelCache.get("Models/Airport/beacon.gltf");
        assertNotNull(preparedModelBeacon);
        assertEquals(2, preparedModelBeacon.useCounter);
        assertEquals(3, SGModelLib.preparedModelCache.get("Models/Airport/windsock.gltf").useCounter);
        assertEquals(3, SGModelLib.preparedModelCache.get("Models/Power/windturbine.gltf").useCounter);
    }

    /**
     * Objects/3072824.stg contains airports EDDK (only objects, Terrain/3072824.stg only contains 3072824.btg,
     * but 3072816 contains main terrain part of EDDK)
     * Also for testing TerraSync Bundle
     */
    @Test
    public void testSTG3072824() throws Exception {

        // needs to clean entities
        setup();

        EngineTestFactory.loadBundleSync(FlightGear.getBucketBundleName("3072824"));
        //25.8.24 Bundle EngineTestFactory.loadBundleSync(FlightGear.getBucketBundleName("model"));
        Bundle bundle3072824 = BundleRegistry.getBundle("Terrasync-3072824");
        assertNotNull(bundle3072824, "bundle30728124");
        assertNotNull(bundle3072824.getResource("Objects/e000n50/e007n50/EDDK-Terminal1.xml"), "EDDK-Terminal1");
        assertNotNull(bundle3072824.getResource("Objects/e000n50/e007n50/egkk_tower.xml"), "egkk_tower");

        // 3072824.stg exists twice (in Objects and in Terrain). Both should be loaded. TODO check
        Options options = new Options();
        LoaderOptions opt = new LoaderOptions();
        opt.usegltf = true;

        ReaderWriterSTG readerWriterSTG = new ReaderWriterSTG();
        Group rr = readerWriterSTG.build("3072824.stg", options, opt, true);
        if (rr == null) {
            Assert.fail("node is null. 3072824.stg failed");
        }
        // only reading the STG doesn't add it to world. XMLs are loaded sync and available immediately.
        log.debug(rr.dump("  ", 0));
        // animations wait for model, so we have to wait

        long startTime = Platform.getInstance().currentTimeMillis();
        TestUtils.waitUntil(() -> {
            TestHelper.processAsync();
            return Platform.getInstance().currentTimeMillis() - startTime > 10000;
        }, 31000);

        NodeAssertions.assertSTG3072824(rr);
    }

    @Test
    public void testegkk_carpark_multi() throws Exception {

        EngineTestFactory.loadBundleSync(FlightGear.getBucketBundleName("3072824"));
        Bundle bundle3072824 = BundleRegistry.getBundle("Terrasync-3072824");
        // Kruecke zur Entkopplung des Modelload von AC policy.
        ModelLoader.processPolicy = new ACProcessPolicy(null);

        List<SGAnimation> animationList = new ArrayList<SGAnimation>();

        EngineTestFactory.loadBundleAndWait("traffic-fg");
        Bundle bundlemodel = BundleRegistry.getBundle("traffic-fg");
        assertNotNull(bundlemodel);

        // only one of the 2 animations is built currently.
        BuildResult result = SGReaderWriterXMLTest.loadModelAndWait(new BundleResource(bundle3072824, "Objects/e000n50/e007n50/egkk_carpark_multi.xml"),
                animationList, 1, "Objects/e000n50/e007n50/egkk_carpark_multi.xml", null);
        SceneNode resultNode = new SceneNode(result.getNode());
        log.debug(resultNode.dump("  ", 0));
        // XML was loaded sync, gltf and animations were loaded async
        assertEquals("Objects/e000n50/e007n50/egkk_carpark_multi.xml", resultNode.getName());
        assertEquals(1, resultNode.getTransform().getChildCount());
        assertEquals(1, animationList.size(), "animations");
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
        assertEquals(9, tile_cache.get_size());

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
        Quaternion q = Quaternion.buildQuaternionFromAngleAxis((float) new Degree(180).toRad(), new Vector3(0, 0, 1));
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
     * 3072816.stg contains airports EDDK and EDKB
     * 9.6.17
     */
    @Test
    public void testSTG3072816ByQueue() throws Exception {

        //26.2.24 pager wants to load it itself! So be sure remove it. EngineTestFactory.loadBundleSync(BundleRegistry.TERRAYSYNCPREFIX + "3072816");
        BundleRegistry.unregister(BundleRegistry.TERRAYSYNCPREFIX + "3072816");

        // 2.9.24 now used again, not used/needed anyway before
        EngineTestFactory.loadBundleSync(BundleRegistry.TERRAYSYNCPREFIX + "model");
        SceneryPager sceneryPager = new SceneryPager();
        //ModelRegistry.getInstance().registerCallbacks();
        //Registry.setReadFileCallback(ModelRegistry.getInstance());
        SGReaderWriterOptions options = new SGReaderWriterOptions();
        SceneNode destinationNode = new SceneNode();
        sceneryPager.queueRequest("3072816.stg", destinationNode, 0, options, "e000n50/e007n50");

        // waiting 30 secs should be sufficient. Not sure what better condition we have (futures?).
        long startTime = Platform.getInstance().currentTimeMillis();
        TestUtils.waitUntil(() -> {
            TestHelper.processAsync();
            return Platform.getInstance().currentTimeMillis() - startTime > 30000;
        }, 31000);

        // TODO make sure not the "duplicate stg loading??" branch in SceneryPager.queueRequest was used.

        // result will be in destinationNode
        log.debug(destinationNode.dump("  ", 0));

        NodeAssertions.assertSTG3072816(destinationNode);
        assertEquals(1, destinationNode.findNodeByName("Terrain/e000n50/e007n50/EDDK.gltf").size());
        assertEquals(1, SceneNode.findNode(n -> StringUtils.endsWith(n.getName() == null ? "" : n.getName(), "EDDK.gltf"), destinationNode).size());
    }

    /**
     * Testing the internal main steps done in "Obj.java".
     */
    @Test
    public void testLoadRefBtgAsGltf() throws Exception {

        BundleResource bpath = new BundleResource(BundleRegistry.getBundle("test-resources"), "terrain/3056410.btg");
        assertNotNull(bpath);

        String basename = StringUtils.substringBeforeLast(bpath.getFullName(), ".btg");
        bpath = new BundleResource(bpath.bundle, basename + ".gltf");

        //AbstractLoader tile = LoaderGLTF.buildLoader(bpath, null);
        BooleanHolder loaded = new BooleanHolder(false);
        LoaderGLTF.load(new ResourceLoaderFromBundle(bpath), new GeneralParameterHandler<PortableModel>() {
            @Override
            public void handle(PortableModel ppfile) {
                ModelAssertions.assertRefbtg(ppfile, true, true);

                SGMaterialLib matlib = SGMaterialTest.initSGMaterialLib();
                SGMaterialCache matcache = null;
                matcache = matlib.generateMatCache(SGGeod.fromCart(FlightGear.refbtgcenter));

                Obj obj = new Obj();
                //The GLTF itself does not contain material.
                Node node = obj.getSurfaceGeometryPart2(ppfile/*.gml*/, false, matcache);

                assertEquals(17, obj.foundMaterial.size());
                assertEquals(0, obj.notFoundMaterial.size());
                assertTrue(obj.foundMaterial.contains("DryCrop"), "DryCrop");
                loaded.setValue(true);
            }
        });
        TestUtils.waitUntil(() -> {
            TestHelper.processAsync();
            return loaded.getValue();
        }, 10000);
    }

    public static SceneNode loadSTGFromBundleAndWait(int tile) throws Exception {
        EngineTestFactory.loadBundleAndWait(FlightGear.getBucketBundleName("" + tile));
        Bundle bundle = BundleRegistry.getBundle("Terrasync-" + tile);
        assertNotNull(bundle);

        return loadSTGAndWait(tile);
    }

    /**
     * The loaded STG will not be attached to world (or anything else). That should be done by the caller.
     * However the STG is roughly validated.
     * Never returns null.
     */
    public static SceneNode loadSTGAndWait(int tile) throws Exception {

        EngineTestFactory.loadBundleSync(FlightGear.getBucketBundleName("" + tile));
        Bundle bundle = BundleRegistry.getBundle("Terrasync-" + tile);
        assertNotNull(bundle, "bundle" + tile);

        Options options = new Options();
        LoaderOptions opt = new LoaderOptions();
        opt.usegltf = true;
        // property node not needed for now

        Group rr = new ReaderWriterSTG().build(tile + ".stg", options, opt, true);
        if (rr/*.getNode()*/ == null) {
            fail("node is null. " + tile + ".stg failed");
        }
        // only reading the STG doesn't add it to world. XMLs are loaded sync and available immediately?
        TestUtils.waitUntil(() -> {
            TestHelper.processAsync();
            return isSTGLoaded(rr, ModelAssertions.objectsPerTile.get(tile));
        }, 30000);
        log.debug(rr.dump("  ", 0));
        assertNotNull(rr);
        // TODO validate its not in world yet
        return rr;
    }

    /**
     * difficult to decide when it is complete. Only some probes.
     */
    public static boolean isSTGLoaded(SceneNode destinationNode, String[] expectedObjects) {
        for (int i = 0; i < expectedObjects.length; i++) {
            if (SceneNode.findByName(expectedObjects[i]).size() == 0) {
                return false;
            }
        }
        if (AbstractSceneRunner.getInstance().futures.size() > 0) {
            return false;
        }
        return true;
    }

    private void assertSGMaterial(String name, boolean realized) {
        List<SGMaterial> materials = FlightGearModuleScenery.getInstance().get_matlib().get(name);
        assertEquals(1, materials.size());
        SGMaterial material = materials.get(0);
        assertEquals(name, material.getNames());
        assertEquals(1, material._status.size());
        Effect grasslandEffect = material._status.get(0).getEffect();
        assertEquals(realized, material._status.get(0).effect_realized);
    }
}
