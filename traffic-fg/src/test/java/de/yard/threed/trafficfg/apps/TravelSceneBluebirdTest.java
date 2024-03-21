package de.yard.threed.trafficfg.apps;


import de.yard.threed.core.Event;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsHelper;
import de.yard.threed.engine.ecs.EcsTestHelper;
import de.yard.threed.engine.ecs.EntityFilter;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.engine.testutil.TestHelper;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.graph.GraphMovingComponent;
import de.yard.threed.traffic.GeoRoute;
import de.yard.threed.traffic.GraphVisualizationSystem;
import de.yard.threed.traffic.TrafficEventRegistry;
import de.yard.threed.traffic.TrafficGraph;
import de.yard.threed.traffic.TrafficHelper;
import de.yard.threed.traffic.config.VehicleDefinition;
import de.yard.threed.traffic.geodesy.GeoCoordinate;
import de.yard.threed.traffic.testutils.TrafficTestUtils;
import de.yard.threed.trafficcore.model.Vehicle;
import de.yard.threed.trafficfg.flight.GroundServicesSystem;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class TravelSceneBluebirdTest {
    SceneRunnerForTesting sceneRunner;
    static final int INITIAL_FRAMES = 10;
    Log log;

    /**
     *
     */
    @Test
    public void testWithBluebird() throws Exception {
        run(true, null, null);
    }

    @Test
    public void testWithoutBluebird() throws Exception {
        run(false, null, null);
    }

    @Test
    @Disabled
    public void testWithBluebirdAndFromRoute() throws Exception {
        //too early GeoRoute route = GeoRoute.parse(GeoRoute.SAMPLE_EDKB_EDDK);
        // basename is EDKB
        run(true, "50.768,7.1672", GeoRoute.SAMPLE_EDKB_EDDK);
    }

    public void run(boolean withBluebird, String basename, String initialRoute) throws Exception {

        setup(withBluebird, basename, initialRoute);

        assertEquals(INITIAL_FRAMES, sceneRunner.getFrameCount());

        String[] bundleNames = BundleRegistry.getBundleNames();
        // 5 appears correct
        assertEquals(5, bundleNames.length);
        assertNotNull(BundleRegistry.getBundle("fgdatabasic"));

        sceneRunner.runLimitedFrames(50);

        List<Event> completeEvents = EcsTestHelper.getEventsFromHistory(TrafficEventRegistry.TRAFFIC_EVENT_SPHERE_LOADED);
        assertEquals(1, completeEvents.size(), "completeEvents.size");
        GeoCoordinate initialPosition = completeEvents.get(0).getPayload().get("initialPosition", s -> GeoCoordinate.parse(s));
        assertNotNull(initialPosition);
        if (basename != null) {
            TrafficTestUtils.assertGeoCoordinate(GeoCoordinate.parse(basename), initialPosition, "initialPosition");
        } else {
            TrafficTestUtils.assertGeoCoordinate(TravelSceneBluebird.formerInitialPositionEDDK, initialPosition, "initialPosition");
        }

        // 'bluebird' isn't in vehiclelist but set by property 'initialVehicle'
        List<Vehicle> vehiclelist = TrafficHelper.getVehicleListByDataprovider();
        assertEquals(0, vehiclelist.size(), "size of vehiclelist");

        VehicleDefinition config = TrafficHelper.getVehicleConfigByDataprovider("bluebird", null);
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
        int expectedNumberOfEntites = /*4*/1 + (withBluebird ? 1 : 0) + 7 + 24;
        TestUtils.waitUntil(() -> {
            TestHelper.processAsync();
            sceneRunner.runLimitedFrames(1);
            List<EcsEntity> entities = SystemManager.findEntities((EntityFilter) null);
            log.debug("" + entities.size());
            return entities.size() == expectedNumberOfEntites;
        }, 60000);

        if (withBluebird) {
            EcsEntity bluebird = EcsHelper.findEntitiesByName("bluebird").get(0);
            assertNotNull(bluebird);
            Vector3 posbluebird = bluebird.getSceneNode().getTransform().getPosition();
            // die Werte sind plausibel
            // TODO 3D ref values TestUtils.assertVector3(new Vector3(-1694.7482728026903, 1299.8451319338214, 0.0), pos747);

            GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(bluebird);
            assertNotNull(gmc);
            if (initialRoute != null) {
                assertEquals("??.EDDK", gmc.getGraph().getName());
            } else {
                assertEquals("groundnet.EDDK", gmc.getGraph().getName());
            }
        }

        // 'visualizeTrack' is disabled by default
        GraphVisualizationSystem graphVisualizationSystem = (GraphVisualizationSystem) SystemManager.findSystem(GraphVisualizationSystem.TAG);
        assertNull(graphVisualizationSystem);

        assertEquals(2, SceneNode.findByName("Scene Light").size());
    }

    /**
     * Needs parameter, so no @Before
     */
    private void setup(boolean withBluebird, String basename, String initialRoute) throws Exception {
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

        FgTestFactory.initPlatformForTest(properties, false, true);

        sceneRunner = (SceneRunnerForTesting) SceneRunnerForTesting.getInstance();
        sceneRunner.runLimitedFrames(INITIAL_FRAMES);
        log = Platform.getInstance().getLog(TravelSceneBluebirdTest.class);
    }
}
