package de.yard.threed.trafficadvanced;


import de.yard.threed.core.Event;
import de.yard.threed.core.Payload;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsHelper;
import de.yard.threed.engine.ecs.EcsTestHelper;
import de.yard.threed.engine.ecs.EntityFilter;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.ecs.VelocityComponent;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.engine.testutil.TestHelper;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.graph.GraphMovingComponent;
import de.yard.threed.traffic.GraphTerrainSystem;
import de.yard.threed.traffic.GraphVisualizationSystem;
import de.yard.threed.traffic.SphereProjections;
import de.yard.threed.traffic.TrafficEventRegistry;
import de.yard.threed.traffic.TrafficHelper;
import de.yard.threed.traffic.TrafficSystem;
import de.yard.threed.traffic.VehicleComponent;
import de.yard.threed.traffic.config.VehicleDefinition;
import de.yard.threed.trafficadvanced.apps.FlatAirportScene;
import de.yard.threed.trafficadvanced.testutil.AdvancedBundleResolverSetup;
import de.yard.threed.trafficcore.model.Vehicle;
import de.yard.threed.trafficfg.TravelSceneTestHelper;
import de.yard.threed.trafficfg.flight.GroundNetMetadata;
import de.yard.threed.trafficfg.flight.GroundServiceComponent;
import de.yard.threed.trafficfg.flight.GroundServicesSystem;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashMap;
import java.util.List;

import static de.yard.threed.traffic.SphereSystem.USER_REQUEST_SPHERE;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Integration test  Traditional EDDK groundServices Scene.
 * Putting it all together and test interaction.
 * <p>
 * <p>
 * Created by thomass on 29.11.21.
 */
public class FlatAirportSceneTest {

    SceneNode world;
    String icao = "EDDK";
    EcsEntity aircraft;
    GraphMovingComponent gmc;
    VehicleComponent vhc;
    VelocityComponent vc;
    SceneRunnerForTesting sceneRunner;
    static final int INITIAL_FRAMES = 10;
    Log log;

    @ParameterizedTest
    @CsvSource(value = {
            "true;",
            "false;",
            "false;c172p",
    }, delimiter = ';')
    public void runEDDK(boolean enableDoormarker, String initialVehicle) throws Exception {

        setup(FlatAirportScene.DEFAULT_TILENAME, enableDoormarker, initialVehicle);

        /*DefaultTrafficWorld.instance = null;
        assertNull("", DefaultTrafficWorld.getInstance());

        TrafficWorldConfig tw = new TrafficWorldConfig("data-old", "TrafficWorld.xml");
        SceneConfig sceneConfig = tw.getScene("Flight");
        new TrafficWorld2D(tw, sceneConfig);*/

        //setup(null);

        assertEquals(INITIAL_FRAMES, sceneRunner.getFrameCount());
        List<Request> requests = EcsTestHelper.getRequestsFromSystemTracker(USER_REQUEST_SPHERE);
        assertEquals(1, requests.size());

        String[] bundleNames = BundleRegistry.getBundleNames();
        // 4 without 'data', but 'data' is needed, so 5
        assertEquals(5, bundleNames.length);
        assertNotNull(BundleRegistry.getBundle("fgdatabasic"));

        List<Event> completeEvents = EcsTestHelper.getEventsFromHistory(TrafficEventRegistry.TRAFFIC_EVENT_SPHERE_LOADED);
        assertEquals(1, completeEvents.size());

        /*do we have these events here?
        completeEvents = EcsTestHelper.getEventsFromHistory(TrafficEventRegistry.TRAFFIC_EVENT_GRAPHLOADED);
        assertEquals( 2, completeEvents.size());

        TrafficGraph railwayGraph = TrafficHelper.getTrafficGraphByDataprovider(TrafficGraph.RAILWAY);
        assertNotNull( railwayGraph);*/

        SphereProjections projections = TrafficHelper.getProjectionByDataprovider(null/*??*/);
        assertNotNull(projections);
        assertNotNull(projections.projection);
        assertTrue(projections.backProjection == null);
        TestUtils.assertLatLon(GroundNetMetadata.getAirport("EDDK").getCenter(), projections.projection.getOrigin(), 0.01, "EDDK origin");
        assertTrue(((GraphTerrainSystem) SystemManager.findSystem(GraphTerrainSystem.TAG)).enabled);

        sceneRunner.runLimitedFrames(50);
        TrafficSystem trafficSystem = ((TrafficSystem) SystemManager.findSystem(TrafficSystem.TAG));

        List<Vehicle> vehiclelist = TrafficHelper.getVehicleListByDataprovider();
        assertEquals(8, vehiclelist.size(), "size of vehiclelist");

        VehicleDefinition/*Config*/ config = trafficSystem.getVehicleConfig("VolvoFuel", null);
        assertNotNull(config);

        //11 passt: "Player",GS Vehicle (ohne delayed aircraft) Vehicle from sceneconfig, 3 Aircraft
        int expectedNumberOfEntites = initialVehicle == null ? 11 : 12;
        TestUtils.waitUntil(() -> {
            TestHelper.processAsync();
            List<EcsEntity> entities = SystemManager.findEntities((EntityFilter) null);
            return entities.size() == expectedNumberOfEntites;
        }, 40000);

        // 23.5.24: Is 0.0 really correct elevation. Routebuilder should have converted original 3D->2D.
        TravelSceneTestHelper.validateTrafficCircuit(((FlatAirportScene) sceneRunner.ascene).platzrundeForVisualizationOnly, 0.0, 0.0, false);

        TravelSceneTestHelper.validateGroundnet();

        validateStaticEDDK(enableDoormarker);

        EcsEntity entity747 = EcsHelper.findEntitiesByName("747 KLM").get(0);
        assertNotNull(entity747);
        Vector3 pos747 = entity747.getSceneNode().getTransform().getPosition();
        // values appear correct
        TestUtils.assertVector3(new Vector3(-1694.7482728026903, 1299.8451319338214, 0.0), pos747);
        // start auto move. From now on its non deterministic

        assertEquals(2, SceneNode.findByName("Scene Light").size());

        // 'visualizeTrack' is enabled
        GraphVisualizationSystem graphVisualizationSystem = (GraphVisualizationSystem) SystemManager.findSystem(GraphVisualizationSystem.TAG);
        assertNotNull(graphVisualizationSystem);

        // start service for 747
        GroundServicesSystem.requestService(entity747);
        sceneRunner.runLimitedFrames(10);

        // TODO validate graphVisualizationSystem

        // let vehicle move(?)
        SystemManager.putRequest(new Request(UserSystem.USER_REQUEST_AUTOMOVE, new Payload(new Object[]{null})));
        sceneRunner.runLimitedFrames(10);

        // load next vehicle (by not passing a name), which typically is c172p. Second name parameter is the expected value!
        EcsEntity c172p = TravelSceneTestHelper.loadAndValidateVehicle(sceneRunner, null, "c172p");

        // start c172p default trip and wait until it has a flight route
        TravelSceneTestHelper.startAndValidateDefaultTrip(sceneRunner, c172p, false);

    }

    /**
     * Used for both (Flat)TravelScene tests for testing before any vehicle movement but after loading.
     * Care for test order. First test basic issues.
     */
    public static void validateStaticEDDK(boolean enableDoormarker) {


        EcsEntity entity747 = EcsHelper.findEntitiesByName("747 KLM").get(0);
        assertNotNull(entity747);
        EcsEntity entity738 = EcsHelper.findEntitiesByName("738").get(0);
        assertNotNull(entity738);
        EcsEntity entityLSG0 = EcsHelper.findEntitiesByName("LSG").get(0);
        assertNotNull(entityLSG0);

        List<NativeSceneNode> doormarkerList = SceneNode.findByName("localdoormarker");
        if (enableDoormarker) {
            // Why one?? And which one is it? Its not the service marker.
            assertEquals(1, doormarkerList.size(), "number of doormarker");
        } else {
            assertEquals(0, doormarkerList.size(), "number of doormarker");
        }
//TODO vehicle/aircraft config,

        // hat VehicleEntityBuilder gegriffen? Der legt GroundServiceComponent an.
        GroundServiceComponent gsc = GroundServiceComponent.getGroundServiceComponent(entityLSG0);
        assertNotNull(gsc);

    }

    /**
     * Needs parameter, so no @Before
     */
    private void setup(String tileName, boolean enableDoormarker, String initialVehicle) throws Exception {
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("scene", "de.yard.threed.trafficadvanced.apps.FlatAirportScene");
        properties.put("visualizeTrack", "true");
        properties.put("enableDoormarker", "" + enableDoormarker);
        if (tileName != null) {
            properties.put("argv.basename", tileName);
        }
        if (!StringUtils.empty(initialVehicle)) {
            properties.put("initialVehicle", initialVehicle);
        }

        //9.12.23 sceneRunner = TrafficTestUtils.setupForScene(INITIAL_FRAMES, ConfigurationByEnv.buildDefaultConfigurationWithEnv(properties));
        FgTestFactory.initPlatformForTest(properties, false, true, true, false, new AdvancedBundleResolverSetup());

        sceneRunner = (SceneRunnerForTesting) SceneRunnerForTesting.getInstance();
        sceneRunner.runLimitedFrames(INITIAL_FRAMES);
        log = Platform.getInstance().getLog(FlatAirportSceneTest.class);
    }
}
