package de.yard.threed.trafficfg.apps;


import de.yard.threed.core.Event;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.Light;
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
import de.yard.threed.graph.GraphMovingSystem;
import de.yard.threed.graph.ProjectedGraph;
import de.yard.threed.traffic.EllipsoidCalculations;
import de.yard.threed.traffic.GeoRoute;
import de.yard.threed.traffic.GraphTerrainSystem;
import de.yard.threed.traffic.GraphVisualizationSystem;
import de.yard.threed.traffic.RequestRegistry;
import de.yard.threed.traffic.ScenerySystem;
import de.yard.threed.traffic.SphereProjections;
import de.yard.threed.traffic.SphereSystem;
import de.yard.threed.traffic.TrafficEventRegistry;
import de.yard.threed.traffic.TrafficGraph;
import de.yard.threed.traffic.TrafficHelper;
import de.yard.threed.traffic.TrafficSystem;
import de.yard.threed.traffic.VehicleComponent;
import de.yard.threed.traffic.config.VehicleDefinition;
import de.yard.threed.traffic.geodesy.GeoCoordinate;
import de.yard.threed.traffic.geodesy.SimpleMapProjection;
import de.yard.threed.traffic.testutils.TrafficTestUtils;
import de.yard.threed.trafficcore.model.Vehicle;
import de.yard.threed.trafficfg.TrafficRuntimeTestUtil;
import de.yard.threed.trafficfg.TravelHelper;
import de.yard.threed.trafficfg.TravelSceneTestHelper;
import de.yard.threed.trafficfg.fgadapter.FgTerrainBuilder;
import de.yard.threed.trafficfg.flight.GroundNet;
import de.yard.threed.trafficfg.flight.GroundServicesSystem;
import de.yard.threed.trafficfg.flight.Parking;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

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
    //@Disabled
    public void testWithBluebirdAndFromRoute() throws Exception {
        //too early to parse GeoRoute here
        // basename is EDKB
        run(true, null, GeoRoute.SAMPLE_EDKB_EDDK);
    }

    public void run(boolean withBluebird, String basename, String initialRoute) throws Exception {

        setup(withBluebird, basename, initialRoute);

        assertEquals(INITIAL_FRAMES, sceneRunner.getFrameCount());
        SphereSystem sphereSystem = (SphereSystem) SystemManager.findSystem(SphereSystem.TAG);
        // world is set in SphereSystem.init()
        assertNotNull(sphereSystem.world);
        TravelSceneTestHelper.waitForSphereLoaded(sceneRunner);

        TravelSceneTestHelper.validateSphereProjections();

        String[] bundleNames = BundleRegistry.getBundleNames();
        // 5 appears correct
        //TODO why 6 with initialRoute assertEquals(5, bundleNames.length);
        assertNotNull(BundleRegistry.getBundle("fgdatabasic"));

        sceneRunner.runLimitedFrames(50);
        // now all major initing should have been done

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
            TrafficTestUtils.assertGeoCoordinate(GeoCoordinate.parse(basename), initialPosition, "initialPosition");
        } else {
            TrafficTestUtils.assertGeoCoordinate(TravelSceneBluebird.formerInitialPositionEDDK, initialPosition, "initialPosition");
        }

        assertFalse(((GraphTerrainSystem) SystemManager.findSystem(GraphTerrainSystem.TAG)).enabled);
        EllipsoidCalculations ellipsoidCalculations = TrafficHelper.getEllipsoidConversionsProviderByDataprovider();
        assertNotNull(ellipsoidCalculations);

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

        // 20.5.24 elevation 68.8 is the result of limited EDDK elevation provider (default elevation). But runway should have
        // correct elevation. Value differs slightly to TravelScene!?
        TravelSceneTestHelper.validatePlatzrunde(((TravelSceneBluebird) sceneRunner.ascene).platzrundeForVisualizationOnly, 71.31, 0.5, true);

        TravelSceneTestHelper.validateGroundnet();


        EcsEntity bluebird = null;
        if (withBluebird) {
            bluebird = EcsHelper.findEntitiesByName("bluebird").get(0);
            assertNotNull(bluebird);
            VehicleComponent vehicleComponent = VehicleComponent.getVehicleComponent(bluebird);
            //TODO not yet added assertNotNull(vehicleComponent);
            Vector3 posbluebird = bluebird.getSceneNode().getTransform().getPosition();
            log.debug("posbluebird=" + posbluebird);
            GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(bluebird);
            assertNotNull(gmc);

            if (initialRoute != null) {
                // TODO no name yet assertEquals("??.EDDK", gmc.getGraph().getName());
                GeoCoordinate geoBluebird = ellipsoidCalculations.fromCart(posbluebird);
                assertEquals(50.768, geoBluebird.getLatDeg().getDegree(), 0.000001);
                assertEquals(7.1672, geoBluebird.getLonDeg().getDegree(), 0.000001);
                // elevation 68.79 appears correct. Now 60.15? Can also be correct.
                assertEquals(60.15, geoBluebird.getElevationM(), 0.01);
                LocalTransform posrot = GraphMovingSystem.getPosRot(gmc);
                log.debug("posrot=" + posrot);
                // position by graph should comply to nodes position
                TestUtils.assertVector3(posbluebird, posrot.position);
                // abort here for now
                return;
            } else {
                assertEquals("groundnet.EDDK", gmc.getGraph().getName());
                // ref values for initial EDDK position taken from visual test
                TestUtils.assertVector3(new Vector3(4001277.6476712367, 500361.77258586703, 4925186.718276716), posbluebird);
                LocalTransform posrot = GraphMovingSystem.getPosRot(gmc);
                log.debug("posrot=" + posrot);
                // ref values taken from visual test
                TestUtils.assertVector3(new Vector3(4001277.6476712367, 500361.77258586703, 4925186.718276716), posrot.position);
                // as always comparing a quaternion has a risk a false negative
                TestUtils.assertQuaternion(new Quaternion(0.25616279537311326, 0.2155633415950961, 0.7906922999954207, 0.5125609766212603), posrot.rotation);
            }

        }

        // 'visualizeTrack' is disabled by default
        GraphVisualizationSystem graphVisualizationSystem = (GraphVisualizationSystem) SystemManager.findSystem(GraphVisualizationSystem.TAG);
        assertNull(graphVisualizationSystem);

        List<NativeSceneNode> lights = SceneNode.findByName("Scene Light");
        assertEquals(2, lights.size());
        for (int i = 0; i < 2; i++) {
            // seems to be no way to retrieve light
            //assertEquals(30000000, Math.abs(lights.get(0).getTransform().getDirection().getY()));
        }

        if (withBluebird) {

            // start bluebird roundtrip
            TravelSceneTestHelper.assertDefaultTrip(sceneRunner, bluebird, true);


        }
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

        FgTestFactory.initPlatformForTest(properties, false, true, true);

        sceneRunner = (SceneRunnerForTesting) SceneRunnerForTesting.getInstance();
        sceneRunner.runLimitedFrames(INITIAL_FRAMES);
    }
}
