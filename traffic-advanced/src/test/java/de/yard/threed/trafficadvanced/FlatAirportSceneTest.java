package de.yard.threed.trafficadvanced;


import de.yard.threed.core.Payload;
import de.yard.threed.core.Util;
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
import de.yard.threed.engine.ecs.EntityFilter;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.ecs.VelocityComponent;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.engine.testutil.TestHelper;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.graph.GraphMovingComponent;
import de.yard.threed.javacommon.ConfigurationByEnv;
import de.yard.threed.traffic.GraphTerrainSystem;
import de.yard.threed.traffic.GraphVisualizationSystem;
import de.yard.threed.traffic.RequestRegistry;
import de.yard.threed.traffic.SphereProjections;
import de.yard.threed.traffic.TrafficHelper;
import de.yard.threed.traffic.TrafficSystem;
import de.yard.threed.traffic.VehicleComponent;
import de.yard.threed.traffic.config.VehicleDefinition;
import de.yard.threed.traffic.testutils.TrafficTestUtils;
import de.yard.threed.trafficadvanced.apps.FlatAirportScene;
import de.yard.threed.trafficfg.flight.GroundNetMetadata;
import de.yard.threed.trafficfg.flight.GroundServiceComponent;
import de.yard.threed.trafficfg.flight.GroundServicesSystem;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static de.yard.threed.javanative.JavaUtil.sleepMs;
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

    /**
     *
     */
    @Test
    public void testEDDK() throws Exception {

        setup(FlatAirportScene.DEFAULT_TILENAME);

        /*DefaultTrafficWorld.instance = null;
        assertNull("", DefaultTrafficWorld.getInstance());

        TrafficWorldConfig tw = new TrafficWorldConfig("data-old", "TrafficWorld.xml");
        SceneConfig sceneConfig = tw.getScene("Flight");
        new TrafficWorld2D(tw, sceneConfig);*/

        //setup(null);

        assertEquals(INITIAL_FRAMES, sceneRunner.getFrameCount());

        String[] bundleNames = BundleRegistry.getBundleNames();
        // 4 without 'data', but 'data' is needed, so 5
        assertEquals(5, bundleNames.length);
        assertNotNull(BundleRegistry.getBundle("fgdatabasic"));

        // "Wayland" has two graph files that should have been loaded finally (via EVENT_LOCATIONCHANGED)
       /* List<Event> completeEvents = EcsTestHelper.getEventsFromHistory(TrafficEventRegistry.EVENT_LOCATIONCHANGED);
        assertEquals("EVENT_LOCATIONCHANGED.size", 1, completeEvents.size());

        completeEvents = EcsTestHelper.getEventsFromHistory(TrafficEventRegistry.TRAFFIC_EVENT_GRAPHLOADED);
        assertEquals("TRAFFIC_EVENT_GRAPHLOADED.size", 2, completeEvents.size());

        TrafficGraph railwayGraph = TrafficHelper.getTrafficGraphByDataprovider(TrafficGraph.RAILWAY);
        assertNotNull("railwayGraph", railwayGraph);*/

        SphereProjections projections = TrafficHelper.getProjectionByDataprovider();
        assertNotNull(projections);
        assertNotNull(projections.projection);
        assertTrue(projections.backProjection == null);
        TestUtils.assertLatLon(GroundNetMetadata.getMap().get("EDDK").airport.getCenter(), projections.projection.getOrigin(), 0.01, "EDDK origin");
        assertTrue(((GraphTerrainSystem) SystemManager.findSystem(GraphTerrainSystem.TAG)).enabled);

        sceneRunner.runLimitedFrames(50);

        // "GroundServices" vehicle list from TrafficWorld.xml
        assertEquals(8, TrafficSystem.vehiclelist.size(), "size of vehiclelist");

        VehicleDefinition/*Config*/ config = TrafficHelper.getVehicleConfigByDataprovider("VolvoFuel", null);
        assertNotNull(config);

        //11 passt: "Player",GS Vehicle (ohne delayed aircraft) Vehicle from sceneconfig, 3 Aircraft
        int expectedNumberOfEntites = 11;
        TestUtils.waitUntil(() -> {
            TestHelper.processAsync();
            List<EcsEntity> entities = SystemManager.findEntities((EntityFilter) null);
            return entities.size() == expectedNumberOfEntites;
        }, 40000);

        validateStaticEDDK();

        EcsEntity entity747 = EcsHelper.findEntitiesByName("747 KLM").get(0);
        assertNotNull(entity747);
        Vector3 pos747 = entity747.getSceneNode().getTransform().getPosition();
        // die Werte sind plausibel
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

        // load c172p
        Request request = RequestRegistry.buildLoadVehicle(UserSystem.getInitialUser().getId(), null, null);
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

    }

    /**
     * Used for both (Flat)TravelScene tests for testing before any vehicle movement but after loading.
     * Auf die Reigenfolge der Tests achten. Zuerst die Basisdinge fuer anderes testen. Sonst hilft es nicht bei der Fehlersuche.
     */
    public static void validateStaticEDDK() {


        EcsEntity entity747 = EcsHelper.findEntitiesByName("747 KLM").get(0);
        assertNotNull(entity747);
        EcsEntity entity738 = EcsHelper.findEntitiesByName("738").get(0);
        assertNotNull(entity738);
        EcsEntity entityLSG0 = EcsHelper.findEntitiesByName("LSG").get(0);
        assertNotNull(entityLSG0);

        List<NativeSceneNode> doormarkerList = SceneNode.findByName("localdoormarker");
        // Warum eigentlich nur einer??
        assertEquals(1, doormarkerList.size(), "number of doormarker");
//TODO vehicle/aircraft config,

        // hat VehicleEntityBuilder gegriffen? Der legt GroundServiceComponent an.
        GroundServiceComponent gsc = GroundServiceComponent.getGroundServiceComponent(entityLSG0);
        assertNotNull(gsc);

    }

    /**
     * Needs parameter, so no @Before
     */
    private void setup(String tileName) throws Exception {
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("scene", "de.yard.threed.trafficadvanced.apps.FlatAirportScene");
        properties.put("visualizeTrack", "true");
        if (tileName != null) {
            properties.put("argv.basename", tileName);
        }
        //9.12.23 sceneRunner = TrafficTestUtils.setupForScene(INITIAL_FRAMES, ConfigurationByEnv.buildDefaultConfigurationWithEnv(properties));
        FgTestFactory.initPlatformForTest(properties, false, true);

        sceneRunner = (SceneRunnerForTesting) SceneRunnerForTesting.getInstance();
        sceneRunner.runLimitedFrames(INITIAL_FRAMES);
        log = Platform.getInstance().getLog(FlatAirportSceneTest.class);
    }
}