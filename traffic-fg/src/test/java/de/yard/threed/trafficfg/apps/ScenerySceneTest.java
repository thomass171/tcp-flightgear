package de.yard.threed.trafficfg.apps;

import de.yard.threed.core.InitMethod;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.SimpleEventBusForTesting;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsTestHelper;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.SystemState;
import de.yard.threed.engine.test.testutil.TestUtil;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.flightgear.core.FlightGearModuleScenery;
import de.yard.threed.flightgear.core.flightgear.scenery.FGTileMgr;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterialLib;
import de.yard.threed.flightgear.core.simgear.scene.tgdb.ReaderWriterSTG;
import de.yard.threed.flightgear.testutil.FgFullTestFactory;
import de.yard.threed.javacommon.ConfigurationByEnv;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class ScenerySceneTest {

    SceneRunnerForTesting sceneRunner;
    static final int INITIAL_FRAMES = 10;

    /**
     *
     */
    @Test
    public void testLaunch() {
        sceneRunner = buildSceneRunner(new HashMap<>(), INITIAL_FRAMES);
        Log logger = Platform.getInstance().getLog(ScenerySceneTest.class);

        EcsEntity userEntity = SystemManager.findEntities(e -> "pilot".equals(e.getName())).get(0);
        assertNotNull(userEntity, "userEntity");
        assertNotNull(userEntity.getName(), "name");

        Vector3 position = userEntity.getSceneNode().getTransform().getPosition();
        logger.debug("position=" + position);

        // 9 is typical if all tiles are available.
        assertEquals( 9, FlightGearModuleScenery.getInstance().get_scenery().get_terrain_branch().getTransform().getChildCount(),"terraingroup.children");
        // why  12?
        assertEquals( 12, ReaderWriterSTG.btgLoaded.size(),"loaded btgs");

    }

    public static SceneRunnerForTesting buildSceneRunner(HashMap<String, String> additionalProperties, int initial_frames) {

        SystemState.state = 0;
        SystemManager.reset();

        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("scene", "de.yard.threed.trafficfg.apps.SceneryScene");
        properties.putAll(additionalProperties);
        // buildDefaultConfigurationWithEnv is needed for HOSTDIR
        FgFullTestFactory.initPlatformForTest(properties);
        // not sufficient SceneRunnerForTesting sceneRunner = SceneRunnerForTesting.setupForScene(initial_frames, ConfigurationByEnv.buildDefaultConfigurationWithEnv(properties), new String[]{"engine", SGMaterialLib.BUNDLENAME});
        SceneRunnerForTesting sceneRunner = (SceneRunnerForTesting)SceneRunnerForTesting.getInstance();
        sceneRunner.runLimitedFrames(initial_frames);
        return sceneRunner;
    }
}
