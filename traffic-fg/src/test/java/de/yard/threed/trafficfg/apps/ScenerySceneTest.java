package de.yard.threed.trafficfg.apps;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.engine.ObserverComponent;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.SystemState;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.flightgear.core.FlightGearModuleScenery;
import de.yard.threed.flightgear.core.simgear.scene.tgdb.ReaderWriterSTG;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;


public class ScenerySceneTest {

    SceneRunnerForTesting sceneRunner;
    static final int INITIAL_FRAMES = 10;

    /**
     *
     */
    @Test
    public void testLaunch() {
        launch(null);
    }

    @Test
    public void testLaunchWithVehicle() {
        launch("bluebird");
    }

    private void launch(String initialVehicle) {
        HashMap<String, String> properties = new HashMap<String, String>();
        if (initialVehicle != null) {
            properties.put("initialVehicle", initialVehicle);
        }
        sceneRunner = buildSceneRunner("de.yard.threed.trafficfg.apps.SceneryScene", properties, INITIAL_FRAMES);
        Log logger = Platform.getInstance().getLog(ScenerySceneTest.class);

        EcsEntity userEntity = SystemManager.findEntities(e -> "pilot".equals(e.getName())).get(0);
        assertNotNull(userEntity, "userEntity");
        assertNotNull(userEntity.getName(), "name");

        Vector3 position = userEntity.getSceneNode().getTransform().getPosition();
        logger.debug("position=" + position);

        assertNull(ObserverComponent.getObserverComponent(userEntity));

        String[] bundleNames = BundleRegistry.getBundleNames();

        // 9 is typical and plausibel if all tiles are available. Why 10 with project bundles? TODO check
        assertEquals(10, FlightGearModuleScenery.getInstance().get_scenery().get_terrain_branch().getTransform().getChildCount(), "terraingroup.children");
        // Was 12 with full scenery. 5 seems convincing with projects tile set.  EDDK,...
        assertEquals(5, ReaderWriterSTG.btgLoaded.size(), "loaded btgs");

    }

    public static SceneRunnerForTesting buildSceneRunner(String scene, HashMap<String, String> additionalProperties, int initial_frames) {

        SystemState.state = 0;
        SystemManager.reset();

        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("scene", scene);
        properties.putAll(additionalProperties);
        // buildDefaultConfigurationWithEnv is needed for HOSTDIR
        FgTestFactory.initPlatformForTest(properties, false, true);
        // not sufficient SceneRunnerForTesting sceneRunner = SceneRunnerForTesting.setupForScene(initial_frames, ConfigurationByEnv.buildDefaultConfigurationWithEnv(properties), new String[]{"engine", SGMaterialLib.BUNDLENAME});
        SceneRunnerForTesting sceneRunner = (SceneRunnerForTesting) SceneRunnerForTesting.getInstance();
        sceneRunner.runLimitedFrames(initial_frames);
        return sceneRunner;
    }
}
