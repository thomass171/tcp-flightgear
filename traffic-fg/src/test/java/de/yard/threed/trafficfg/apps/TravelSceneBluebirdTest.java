package de.yard.threed.trafficfg.apps;


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
import de.yard.threed.engine.ecs.EntityFilter;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.engine.testutil.TestHelper;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.traffic.GraphVisualizationSystem;
import de.yard.threed.traffic.RequestRegistry;
import de.yard.threed.traffic.TrafficHelper;
import de.yard.threed.traffic.TrafficSystem;
import de.yard.threed.traffic.config.VehicleDefinition;
import de.yard.threed.trafficfg.flight.GroundServiceComponent;
import de.yard.threed.trafficfg.flight.GroundServicesSystem;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static de.yard.threed.javanative.JavaUtil.sleepMs;
import static org.junit.jupiter.api.Assertions.*;


/**
 *
 */
public class TravelSceneBluebirdTest {
    SceneRunnerForTesting sceneRunner;
    static final int INITIAL_FRAMES = 10;
    Log log;

    /**
     * disabled until we have elevation solution
     */
    @Test
    @Disabled
    public void test() throws Exception {
        run();
    }

    public void run() throws Exception {

        setup( );

        assertEquals(INITIAL_FRAMES, sceneRunner.getFrameCount());

        String[] bundleNames = BundleRegistry.getBundleNames();
        // 7 appears correct, but without "Terrasync-model" its only 6.
        assertEquals(6, bundleNames.length);
        assertNotNull(BundleRegistry.getBundle("fgdatabasic"));

        sceneRunner.runLimitedFrames(50);

        // "VehiclesWithCockpit" has only bluebird
        assertEquals(1, TrafficSystem.vehiclelist.size(), "size of vehiclelist");

        VehicleDefinition/*Config*/ config = TrafficHelper.getVehicleConfigByDataprovider("bluebird", null);
        assertNotNull(config);

        // vehicle need groundnet to be loaded.
        TestUtils.waitUntil(() -> {
            sceneRunner.runLimitedFrames(1);
            return GroundServicesSystem.groundnetEDDK != null;
        }, 60000);

        //11 passt: "Player",5 GS Vehicle (3 LSG?, 2 Goldhofert?, no delayed aircraft),  Vehicle from sceneconfig (747, 737, 738, Bravo), 3 Aircraft
        //Why 15?
        int expectedNumberOfEntites = 15;
        TestUtils.waitUntil(() -> {
            TestHelper.processAsync();
            sceneRunner.runLimitedFrames(1);
            List<EcsEntity> entities = SystemManager.findEntities((EntityFilter) null);
            log.debug(""+entities.size());
            return entities.size() == expectedNumberOfEntites;
        }, 60000);

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

        EcsEntity c172p = EcsHelper.findEntitiesByName("bluebird").get(0);
        //log.debug(c172p.getSceneNode().dump(" ", 0));

        // garmin has multiple components and names. just look for one
        NativeSceneNode garmin196 = SceneNode.findByName("Aircraft/Instruments-3d/garmin196/garmin196.gltf").get(0);
        assertTrue(Texture.hasTexture("screens.png"), "garmin.texture");

    }

    /**
     * Needs parameter, so no @Before
     */
    private void setup() throws Exception {
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("scene", "de.yard.threed.trafficfg.apps.TravelSceneBluebird");
        properties.put("visualizeTrack", "true");
        //properties.put("initialVehicle", "bluebird");
        FgTestFactory.initPlatformForTest(properties, false, true);

        sceneRunner = (SceneRunnerForTesting) SceneRunnerForTesting.getInstance();
        sceneRunner.runLimitedFrames(INITIAL_FRAMES);
        log = Platform.getInstance().getLog(TravelSceneBluebirdTest.class);
    }
}
