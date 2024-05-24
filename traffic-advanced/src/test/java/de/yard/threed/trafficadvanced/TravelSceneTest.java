package de.yard.threed.trafficadvanced;


import de.yard.threed.core.Event;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.Payload;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.Texture;
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
import de.yard.threed.graph.GraphPath;
import de.yard.threed.traffic.GraphTerrainSystem;
import de.yard.threed.traffic.GraphVisualizationSystem;
import de.yard.threed.traffic.RequestRegistry;
import de.yard.threed.traffic.SphereProjections;
import de.yard.threed.traffic.SphereSystem;
import de.yard.threed.traffic.TrafficEventRegistry;
import de.yard.threed.traffic.TrafficHelper;
import de.yard.threed.traffic.TrafficSystem;
import de.yard.threed.traffic.VehicleComponent;
import de.yard.threed.traffic.config.VehicleDefinition;
import de.yard.threed.traffic.geodesy.SimpleMapProjection;
import de.yard.threed.trafficadvanced.apps.FlatAirportScene;
import de.yard.threed.trafficadvanced.apps.TravelScene;
import de.yard.threed.trafficcore.model.Vehicle;
import de.yard.threed.trafficfg.TravelHelper;
import de.yard.threed.trafficfg.TravelSceneTestHelper;
import de.yard.threed.trafficfg.apps.TravelSceneBluebird;
import de.yard.threed.trafficfg.flight.GroundNetMetadata;
import de.yard.threed.trafficfg.flight.GroundServiceComponent;
import de.yard.threed.trafficfg.flight.GroundServicesSystem;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static de.yard.threed.javanative.JavaUtil.sleepMs;
import static org.junit.jupiter.api.Assertions.*;


/**
 *
 */
public class TravelSceneTest {

    SceneNode world;
    SceneRunnerForTesting sceneRunner;
    static final int INITIAL_FRAMES = 10;
    Log log;

    /**
     *
     */
    @Test
    public void testWithDoormarkerAndNavigator() throws Exception {
        run(true, true);
    }

    @Test
    public void testWithoutDoormarker() throws Exception {
        run(false, false);
    }

    public void run(boolean enableDoormarker, boolean enableNavigator) throws Exception {

        setup(enableDoormarker, enableNavigator);
        Log log = Platform.getInstance().getLog(TravelSceneTest.class);

        assertEquals(INITIAL_FRAMES, sceneRunner.getFrameCount());
        SphereSystem sphereSystem = (SphereSystem) SystemManager.findSystem(SphereSystem.TAG);
        // world is set in SphereSystem.init()
        assertNotNull(sphereSystem.world);
        TravelSceneTestHelper.waitForSphereLoaded(sceneRunner);

        TravelSceneTestHelper.validateSphereProjections();

        String[] bundleNames = BundleRegistry.getBundleNames();
        // 7 appears correct, but without "Terrasync-model" its only 6.
        assertEquals(6, bundleNames.length);
        assertNotNull(BundleRegistry.getBundle("fgdatabasic"));

        sceneRunner.runLimitedFrames(50);
        EcsEntity userEntity = SystemManager.findEntities(e -> "Freds account name".equals(e.getName())).get(0);
        assertNotNull(userEntity, "userEntity");
        assertNotNull(userEntity.getName(), "name");

        // "GroundServices" vehicle list from TrafficWorld.xml
        List<Vehicle> vehiclelist = TrafficHelper.getVehicleListByDataprovider();
        assertEquals(8, vehiclelist.size(), "size of vehiclelist");

        VehicleDefinition/*Config*/ config = TrafficHelper.getVehicleConfigByDataprovider("VolvoFuel", null);
        assertNotNull(config);

        // vehicle need groundnet to be loaded.
        TestUtils.waitUntil(() -> {
            sceneRunner.runLimitedFrames(1);
            return GroundServicesSystem.groundnets.get("EDDK") != null;
        }, 60000);

        //11 passt: "Player",5 GS Vehicle (3 LSG?, 2 Goldhofert?, no delayed aircraft),  Vehicle from sceneconfig (747, 737, 738, Bravo), 3 Aircraft
        //Why 15? (earth,moon,sun no longer exist)? + 7+24(??) animated scenery objects
        int expectedNumberOfEntites = /*15*/(enableNavigator ? 12 : 11) + 7 + 18/*??*/;
        TestUtils.waitUntil(() -> {
            TestHelper.processAsync();
            sceneRunner.runLimitedFrames(1);
            List<EcsEntity> entities = SystemManager.findEntities((EntityFilter) null);
            log.debug("" + entities.size());
            return entities.size() == expectedNumberOfEntites;
        }, 60000);

        // 20.5.24 elevation 68.8 is the result of limited EDDK elevation provider (default elevation). But runway should have
        // correct elevation. Value differs slightly to TravelSceneBluebird!?
        TravelSceneTestHelper.validatePlatzrunde(((TravelScene)sceneRunner.ascene).platzrundeForVisualizationOnly, 70.60974991063463, true);

        TravelSceneTestHelper.validateGroundnet();

        validateStaticEDDK(enableDoormarker);
        if (enableNavigator) {
            // 2*3 for navigator, 2 for LSG, 1 for 738, position not tested.
            EcsTestHelper.assertTeleportComponent(userEntity, 3 + 3 + 2 + 1, 8, null);
            EcsEntity navigator = EcsHelper.findEntitiesByName("Navigator").get(0);
            assertNotNull(navigator);
            // 10 and 4 appears correct
            EcsTestHelper.assertTeleportComponent(navigator, 10, 4, null);
        } else {
            // without navigator we only have the eddk overview viewpoint
            EcsTestHelper.assertTeleportComponent(userEntity, 1 + 2 + 1, 3, null);
        }

        EcsEntity entity747 = EcsHelper.findEntitiesByName("747 KLM").get(0);
        assertNotNull(entity747);
        Vector3 pos747 = entity747.getSceneNode().getTransform().getPosition();
        // die Werte sind plausibel
        // TODO 3D ref values TestUtils.assertVector3(new Vector3(-1694.7482728026903, 1299.8451319338214, 0.0), pos747);

        // 'visualizeTrack' is enabled
        GraphVisualizationSystem graphVisualizationSystem = (GraphVisualizationSystem) SystemManager.findSystem(GraphVisualizationSystem.TAG);
        assertNotNull(graphVisualizationSystem);

        assertEquals(2, SceneNode.findByName("Scene Light").size());

        // start auto move. From now on its non deterministic

        // start service for 747
        GroundServicesSystem.requestService(entity747);
        sceneRunner.runLimitedFrames(10);

        // TODO validate graphVisualizationSystem

        // let vehicle move(?)
        SystemManager.putRequest(new Request(UserSystem.USER_REQUEST_AUTOMOVE, new Payload(new Object[]{null})));
        sceneRunner.runLimitedFrames(10);

        // load c172p
        Request request = RequestRegistry.buildLoadVehicle(UserSystem.getInitialUser().getId(), null, null, null);
        SystemManager.putRequest(request);
        TestUtils.waitUntil(() -> {
            sceneRunner.runLimitedFrames(10);
            sleepMs(100);
            return BundleRegistry.getBundle("c172p") != null;
        }, 30000);
        assertNotNull(BundleRegistry.getBundle("c172p"));

        // Optionals should not have been created. But testing that way is a false positive for unknwn reasons.
        assertEquals(0, SceneNode.findByName("LandingLightCone").size());

        EcsEntity c172p = EcsHelper.findEntitiesByName("c172p").get(0);
        //log.debug(c172p.getSceneNode().dump(" ", 0));

        // garmin has multiple components and names. just look for one
        NativeSceneNode garmin196 = SceneNode.findByName("Aircraft/Instruments-3d/garmin196/garmin196.gltf").get(0);
        assertTrue(Texture.hasTexture("screens.png"), "garmin.texture");
        GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(c172p);
        assertEquals("groundnet.EDDK", gmc.getGraph().getName());

        // start c172p and wait until it has a flight route
        TravelSceneTestHelper.assertDefaultTrip(sceneRunner, c172p, true);

    }

    /**
     * Used for both (Flat)TravelScene tests for testing before any vehicle movement but after loading.
     * Auf die Reigenfolge der Tests achten. Zuerst die Basisdinge fuer anderes testen. Sonst hilft es nicht bei der Fehlersuche.
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
    private void setup(boolean enableDoormarker, boolean enableNavigator) throws Exception {
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("scene", "de.yard.threed.trafficadvanced.apps.TravelScene");
        properties.put("visualizeTrack", "true");
        properties.put("enableDoormarker", "" + enableDoormarker);
        properties.put("enableNavigator", "" + enableNavigator);
        //9.12.23 sceneRunner = TrafficTestUtils.setupForScene(INITIAL_FRAMES, ConfigurationByEnv.buildDefaultConfigurationWithEnv(properties));
        FgTestFactory.initPlatformForTest(properties, false, true);

        sceneRunner = (SceneRunnerForTesting) SceneRunnerForTesting.getInstance();
        sceneRunner.runLimitedFrames(INITIAL_FRAMES);
        log = Platform.getInstance().getLog(TravelSceneTest.class);
    }
}
