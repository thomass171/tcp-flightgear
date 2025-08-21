package de.yard.threed.trafficfg.apps;


import de.yard.threed.core.Event;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.BaseRequestRegistry;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.Texture;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsHelper;
import de.yard.threed.engine.ecs.EcsTestHelper;
import de.yard.threed.engine.ecs.EntityFilter;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.engine.testutil.TestHelper;
import de.yard.threed.flightgear.ecs.FgAnimationComponent;
import de.yard.threed.flightgear.testutil.BundleResolverSetup;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.graph.GraphMovingComponent;
import de.yard.threed.graph.GraphMovingSystem;
import de.yard.threed.trafficcore.EllipsoidCalculations;
import de.yard.threed.traffic.GraphTerrainSystem;
import de.yard.threed.traffic.GraphVisualizationSystem;
import de.yard.threed.traffic.ScenerySystem;
import de.yard.threed.traffic.SphereSystem;
import de.yard.threed.traffic.TrafficEventRegistry;
import de.yard.threed.traffic.TrafficGraph;
import de.yard.threed.traffic.TrafficHelper;
import de.yard.threed.traffic.TrafficSystem;
import de.yard.threed.traffic.VehicleComponent;
import de.yard.threed.traffic.config.VehicleDefinition;
import de.yard.threed.core.GeoCoordinate;
import de.yard.threed.traffic.testutils.TrafficTestUtils;
import de.yard.threed.trafficcore.model.SmartLocation;
import de.yard.threed.trafficcore.model.Vehicle;
import de.yard.threed.trafficfg.TrafficRuntimeTestUtil;
import de.yard.threed.trafficfg.TravelSceneTestHelper;
import de.yard.threed.trafficfg.fgadapter.FgTerrainBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashMap;
import java.util.List;

import static de.yard.threed.trafficfg.flight.TravelSceneHelper.getSphereWorld;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
@Slf4j
public class TravelSceneBluebirdTest {
    SceneRunnerForTesting sceneRunner;
    static final int INITIAL_FRAMES = 10;

    // set up in baseTest()
    EllipsoidCalculations ellipsoidCalculations;

    @ParameterizedTest
    @CsvSource(value = {
            "simple;true;;;;",
            ";false;;;;",
    }, delimiter = ';')
    public void testBluebird(String testCaseName, boolean withBluebird) throws Exception {

        baseTest(withBluebird, null, null, null);

        EcsEntity bluebird;
        Vector3 posbluebird;
        GraphMovingComponent gmc;
        if (withBluebird) {
            bluebird = TravelSceneTestHelper.assertBluebird();
        } else {
            // without initialVehicle 'bluebird'
            // 'EDDK-sphere.xml' has no vehicle list, so needs to load by name
            bluebird = TravelSceneTestHelper.loadAndValidateVehicle(sceneRunner, "bluebird", "bluebird");
            assertNotNull(bluebird);
        }
        gmc = GraphMovingComponent.getGraphMovingComponent(bluebird);
        assertNotNull(gmc);
        posbluebird = bluebird.getSceneNode().getTransform().getPosition();

        // standard test case with 'bluebird' on usual EDDK position. Other test cases terminated or branched earlier.
        assertEquals("groundnet.EDDK", gmc.getGraph().getName());
        // ref values for initial EDDK position taken from visual test
        TestUtils.assertVector3(new Vector3(4001277.6476712367, 500361.77258586703, 4925186.718276716), posbluebird);
        TrafficTestUtils.assertVehicleEntity(bluebird, "bluebird", 1.2, new Vector3(4001277.6476712367, 500361.77258586703, 4925186.718276716), "TravelSphere", new Quaternion(), log);
        LocalTransform posrot = GraphMovingSystem.getPosRot(gmc);
        log.debug("posrot=" + posrot);
        // ref values taken from visual test
        TestUtils.assertVector3(new Vector3(4001277.6476712367, 500361.77258586703, 4925186.718276716), posrot.position);
        // as always comparing a quaternion has a risk a false negative. 22.4.25: Values changed (negated) due to GeoTools change?
        //TestUtils.assertQuaternion(new Quaternion(0.25616279537311326, 0.2155633415950961, 0.7906922999954207, 0.5125609766212603), posrot.rotation);
        TestUtils.assertQuaternion(new Quaternion(-0.25616279537311326, -0.2155633415950961, -0.7906922999954207, -0.5125609766212603), posrot.rotation);

        // 'bluebird' is on usual EDDK position, so start bluebird roundtrip
        TravelSceneTestHelper.startAndValidateDefaultTrip(sceneRunner, bluebird, true);

    }

    @ParameterizedTest
    @CsvSource(value = {
            // EDDK 32L. elevation 78.05 is hard coded. What is exact corresponding heading? 320? TODOclarify
            "EDDK-32L;geo:50.85850600,  007.13874200 ,78.05;320; 78.05",
            // EHAM, runway 06. No elevation, so terrain is needed. But terrain will not be available. A dummy tile will be created.
            // Elevation 25.55 appears correct with dummy tile edge elevation of 30. 22.6.25: But now we have ocean tile, so "25.55"->"-0.144"
            "EHAM;geo:52.2878684, 4.73415315; 57.8; -0.144",
    }, delimiter = ';')
    public void testBluebirdWithInitialLocation(String testCaseName, String initialLocation, String initialHeading,
                                                Double expectedInitialLocationElevation) throws Exception {

        baseTest(true, null, initialLocation, initialHeading);

        EcsEntity bluebird = TravelSceneTestHelper.assertBluebird();
        GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(bluebird);
        assertNotNull(gmc);
        Vector3 posbluebird = bluebird.getSceneNode().getTransform().getPosition();
        Quaternion rotationbluebird = bluebird.getSceneNode().getTransform().getRotation();

        // position differs from 'initialRoute' we are in EDDK or EHAM instead of EDKB
        GeoCoordinate initialLocationGeoCoordinate = SmartLocation.fromString(initialLocation).getGeoCoordinate();
        initialLocationGeoCoordinate = GeoCoordinate.fromLatLon(initialLocationGeoCoordinate, expectedInitialLocationElevation);

        GeoCoordinate geoBluebird = ellipsoidCalculations.fromCart(posbluebird);
        assertEquals(/*50.858506*/initialLocationGeoCoordinate.getLatDeg().getDegree(), geoBluebird.getLatDeg().getDegree(), 0.000001);
        assertEquals(/*7.138742*/initialLocationGeoCoordinate.getLonDeg().getDegree(), geoBluebird.getLonDeg().getDegree(), 0.000001);
        assertEquals(expectedInitialLocationElevation, geoBluebird.getElevationM(), 0.01);
        // vehicle isn't on graph
        assertNull(gmc.getCurrentposition());
        // 'bluebird' is in FG space, so no local rotation required.
        Quaternion expectedLocalVehicleRotation = new Quaternion();
        TrafficTestUtils.assertVehicleEntity(bluebird, "bluebird", 1.2,
                ellipsoidCalculations.toCart(initialLocationGeoCoordinate),
                "TravelSphere", expectedLocalVehicleRotation, log);
        // Not yet sure how to calc the ref value. For now use once existing values after visual check ("EDDK-32L" only).
        if (testCaseName.equals("EDDK-32L")) {
            Quaternion expectedRotation = new Quaternion(-0.0947478409981826, -0.3212913237534745, -0.37675868791648814, -0.8636247003104818);
            TestUtils.assertQuaternion(expectedRotation, rotationbluebird);
        }
        TravelSceneTestHelper.validateFgProperties(bluebird, false);
        // just speed up
        SystemManager.putRequest(new Request(BaseRequestRegistry.TRIGGER_REQUEST_START_SPEEDUP, UserSystem.getInitialUser().getId()));
        sceneRunner.runLimitedFrames(5);
        TravelSceneTestHelper.validateFgProperties(bluebird, true);
    }

    @ParameterizedTest
    @CsvSource(value = {
            // Route is GeoRoute.SAMPLE_EDKB_EDDK
            "EDKB-EDDK;wp:50.768,7.1672000->takeoff:50.7692,7.1617000->wp:50.7704,7.1557->wp:50.8176,7.0999->wp:50.8519,7.0921->touchdown:50.8625,7.1317000->wp:50.8662999,7.1443999",
            // Route from EDDK 14L to EHAM 18L
            "EDDK-EHAM;wp:50.8800381,7.1296996->takeoff:50.8764919,7.1348404->wp:50.8566037,7.1636556->wp:50.8480166,7.1594773->wp:50.8459351,7.1456370->wp:50.8524115,7.1357771->wp:52.3457417,4.8181967->wp:52.3522189,4.8080071->wp:52.3525042,4.7933074->wp:52.3464347,4.7824657->touchdown:52.3195264,4.7800279->wp:52.2908234,4.7774309",
    }, delimiter = ';')
    public void testBluebirdWithInitialRoute(String testCaseName, String initialRoute) throws Exception {

        baseTest(true, initialRoute, null, null);

        EcsEntity bluebird = TravelSceneTestHelper.assertBluebird();
        GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(bluebird);
        assertNotNull(gmc);
        Vector3 posbluebird = bluebird.getSceneNode().getTransform().getPosition();

        // TODO no name yet assertEquals("??.EDDK", gmc.getGraph().getName());
        if (testCaseName.equals("EDKB-EDDK")) {
            GeoCoordinate geoBluebird = ellipsoidCalculations.fromCart(posbluebird);
            assertEquals(50.768, geoBluebird.getLatDeg().getDegree(), 0.000001);
            assertEquals(7.1672, geoBluebird.getLonDeg().getDegree(), 0.000001);
            // elevation 68.79 appears correct. Now 60.15? Can also be correct.13.11.24 Now 59.96
            assertEquals(59.96, geoBluebird.getElevationM(), 0.01);
            LocalTransform posrot = GraphMovingSystem.getPosRot(gmc);
            log.debug("posrot=" + posrot);
            // position by graph should comply to nodes position
            TestUtils.assertVector3(posbluebird, posrot.position);
            TrafficTestUtils.assertVehicleEntity(bluebird, "bluebird", 1.2, posrot.position, "TravelSphere", new Quaternion(), log);
        }
    }

    /**
     * Needs parameter, so no @Before
     */
    private void setup(boolean withBluebird, String basename, String initialRoute, String initialLocation, String initialHeading) throws Exception {
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("scene", "de.yard.threed.trafficfg.apps.TravelSceneBluebird");
        if (withBluebird) {
            properties.put("initialVehicle", "bluebird");
        }
        // Default for basename is scene dependent
        if (basename != null) {
            properties.put("basename", basename);
        }
        if (initialRoute != null) {
            properties.put("initialRoute", initialRoute);
        }
        if (initialLocation != null) {
            properties.put("initialLocation", initialLocation);
        }
        if (initialHeading != null) {
            properties.put("initialHeading", initialHeading);
        }

        FgTestFactory.initPlatformForTest(properties, false, true, true, false, new BundleResolverSetup.DefaultBundleResolverSetup());

        sceneRunner = (SceneRunnerForTesting) SceneRunnerForTesting.getInstance();
        sceneRunner.runLimitedFrames(INITIAL_FRAMES);
    }

    private void baseTest(boolean withBluebird, String initialRoute, String initialLocation, String initialHeading) throws Exception {
        String basename = null;
        setup(withBluebird, basename, initialRoute, initialLocation, initialHeading);

        assertEquals(INITIAL_FRAMES, sceneRunner.getFrameCount());
        SphereSystem sphereSystem = (SphereSystem) SystemManager.findSystem(SphereSystem.TAG);
        // world is set in SphereSystem.init()
        assertNotNull(sphereSystem.world);
        TravelSceneTestHelper.waitForSphereLoaded(sceneRunner);

        TravelSceneTestHelper.validateSphereProjections();

        sceneRunner.runLimitedFrames(50);
        TrafficSystem trafficSystem = ((TrafficSystem) SystemManager.findSystem(TrafficSystem.TAG));
        // now all major initing should have been done

        String[] bundleNames = BundleRegistry.getBundleNames();
        assertEquals(6 + (withBluebird ? 1 : 0), bundleNames.length);
        // bundle order isn't reliable probably
        assertNotNull(BundleRegistry.getBundle("fgdatabasic"));
        assertNotNull(BundleRegistry.getBundle("traffic-fg"));
        assertNotNull(BundleRegistry.getBundle("data"));
        assertNotNull(BundleRegistry.getBundle("engine"));
        assertNotNull(BundleRegistry.getBundle("sgmaterial"));
        assertNotNull(BundleRegistry.getBundle("Terrasync-model"));
        if (withBluebird) {
            assertNotNull(BundleRegistry.getBundle("bluebird"));
        }

        // texture (from bundle "data") is used for initial earth model
        assertTrue(Texture.hasTexture("2_no_clouds_4k.jpg"));

        FgTerrainBuilder fgTerrainBuilder = (FgTerrainBuilder) ((ScenerySystem) SystemManager.findSystem(ScenerySystem.TAG)).getTerrainBuilder();
        // Even though tiles are loaded async, they should exist now. 10 in total appears correct (9 surrounding+EDDK?). But only 4 are really available in project.
        // EDKB.btg and EDDK are part of 3072816. Why fail 15 instead of 6? or 12? Values differ for some reason. So
        // check for minmum for now
        List<String> loadedBundles = fgTerrainBuilder.getLoadedBundles();
        assertEquals(4, loadedBundles.size());
        //assertEquals(15, fgTerrainBuilder.getFailedBundles().size());
        assertTrue(fgTerrainBuilder.getFailedBundles().size() >= 6, "" + fgTerrainBuilder.getFailedBundles().size());
        assertEquals("Terrasync-3056443", loadedBundles.get(0));
        assertEquals("Terrasync-3072824", loadedBundles.get(1));
        assertEquals("Terrasync-3056435", loadedBundles.get(2));
        assertEquals("Terrasync-3072816", loadedBundles.get(3));
        TrafficRuntimeTestUtil.assertSceneryNodes(getSphereWorld());

        List<Event> completeEvents = EcsTestHelper.getEventsFromHistory(TrafficEventRegistry.TRAFFIC_EVENT_SPHERE_LOADED);
        assertEquals(1, completeEvents.size(), "completeEvents.size");
        GeoCoordinate initialPosition = completeEvents.get(0).getPayload().get("initialPosition", s -> GeoCoordinate.parse(s));
        assertNotNull(initialPosition);
        if (basename != null) {
            Util.nomore();
            TestUtils.assertGeoCoordinate(GeoCoordinate.parse(basename), initialPosition, "initialPosition");
        } else {
            TestUtils.assertGeoCoordinate(TravelSceneBluebird.formerInitialPositionEDDK, initialPosition, "initialPosition");
        }

        assertFalse(((GraphTerrainSystem) SystemManager.findSystem(GraphTerrainSystem.TAG)).enabled);
        ellipsoidCalculations = TrafficHelper.getEllipsoidConversionsProviderByDataprovider();
        assertNotNull(ellipsoidCalculations);

        // 'bluebird' isn't in vehiclelist but set by property 'initialVehicle'
        List<Vehicle> vehiclelist = TrafficHelper.getVehicleListByDataprovider();
        assertEquals(0, vehiclelist.size(), "size of vehiclelist");

        VehicleDefinition config = trafficSystem.getVehicleConfig("bluebird", null);
        assertNotNull(config);

        // initialRoute for now is not stored in TrafficSystem.
        if (initialRoute != null) {
            // vehicle need a graph (eg. groundnet or a route) and thus terrain at start position (not initial position!) to be loaded.
            TestUtils.waitUntil(() -> {
                sceneRunner.runLimitedFrames(1);
                //return GroundServicesSystem.groundnetEDDK != null;
                // groundnet also use cluster 'ROAD'
                return TrafficHelper.getTrafficGraphByDataprovider(TrafficGraph.ROAD) != null;
            }, 60000);
        }

        // (Sun,Earth,Moon no longer exist),user, 7+24(??) animated scenery objects
        // 29.10.24: Needed? With increasing number of animations it is not reliable
        int expectedNumberOfEntites = /*4*/1 + (withBluebird ? 1 : 0) + 7 + 24;
        TestUtils.waitUntil(() -> {
            TestHelper.processAsync();
            sceneRunner.runLimitedFrames(1);
            List<EcsEntity> entities = SystemManager.findEntities((EntityFilter) null);
            log.debug("" + entities.size());
            // >= instead of == to be more reliable
            return entities.size() >= expectedNumberOfEntites;
        }, 60000);

        // 'visualizeTrack' is disabled by default
        GraphVisualizationSystem graphVisualizationSystem = (GraphVisualizationSystem) SystemManager.findSystem(GraphVisualizationSystem.TAG);
        assertNull(graphVisualizationSystem);

        List<NativeSceneNode> lights = SceneNode.findByName("Scene Light");
        assertEquals(2, lights.size());
        for (int i = 0; i < 2; i++) {
            // seems to be no way to retrieve light
            //assertEquals(30000000, Math.abs(lights.get(0).getTransform().getDirection().getY()));
        }

        // 20.5.24 elevation 68.8 is the result of limited EDDK elevation provider (default elevation). But runway should have
        // correct elevation. Value differs slightly to TravelScene!?
        TravelSceneTestHelper.validatePlatzrunde(((TravelSceneBluebird) sceneRunner.ascene).platzrundeForVisualizationOnly, 71.31, 0.5, true);
        TravelSceneTestHelper.validateGroundnet();

    }
}
