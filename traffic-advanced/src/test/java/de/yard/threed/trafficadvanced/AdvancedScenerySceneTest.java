package de.yard.threed.trafficadvanced;

import de.yard.threed.core.Quaternion;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeLight;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.ObserverComponent;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.SystemState;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.engine.testutil.TestHelper;
import de.yard.threed.flightgear.core.FlightGearModuleScenery;
import de.yard.threed.flightgear.core.simgear.scene.tgdb.ReaderWriterSTG;
import de.yard.threed.flightgear.ecs.FgAnimationComponent;
import de.yard.threed.flightgear.testutil.BundleResolverSetup;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.trafficfg.apps.SceneryScene;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashMap;
import java.util.List;

import static de.yard.threed.core.testutil.TestUtils.assertQuaternion;
import static de.yard.threed.core.testutil.TestUtils.assertVector3;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class AdvancedScenerySceneTest {

    SceneRunnerForTesting sceneRunner;
    static final int INITIAL_FRAMES = 10;

    /**
     *
     */
    @ParameterizedTest
    @CsvSource(value = {
            ";;",
            "bluebird;;",
            // EHAM 06
            ";geo:52.2878684, 4.73415315,300;60"
    }, delimiter = ';')
    void launch(String initialVehicle, String initialLocation, String initialHeading) throws Exception {
        HashMap<String, String> properties = new HashMap<String, String>();
        if (!StringUtils.empty(initialVehicle )) {
            properties.put("initialVehicle", initialVehicle);
        }
        if (initialLocation != null) {
            properties.put("initialLocation", initialLocation);
        }
        if (initialHeading != null) {
            properties.put("initialHeading", initialHeading);
        }

        sceneRunner = buildSceneRunner("de.yard.threed.trafficadvanced.apps.AdvancedSceneryScene", properties, INITIAL_FRAMES);
        Log logger = Platform.getInstance().getLog(AdvancedScenerySceneTest.class);

        EcsEntity userEntity = SystemManager.findEntities(e -> "pilot".equals(e.getName())).get(0);
        assertNotNull(userEntity, "userEntity");
        assertNotNull(userEntity.getName(), "name");

        Vector3 position = userEntity.getSceneNode().getTransform().getPosition();
        logger.debug("position=" + position);

        assertNull(ObserverComponent.getObserverComponent(userEntity));

        sceneRunner.runLimitedFrames(5);

        String[] bundleNames = BundleRegistry.getBundleNames();

        /* scenery tiles difficult to check?
        // 9 is typical and plausibel if all tiles are available. Why 10 with project bundles? TODO check
        assertEquals(10, FlightGearModuleScenery.getInstance().get_scenery().get_terrain_branch().getTransform().getChildCount(), "terraingroup.children");
        // Was 12 with full scenery. 5 seems convincing with projects tile set.  EDDK,.... Now 6 with EDKB added
        assertEquals(6, ReaderWriterSTG.btgLoaded.size(), "loaded btgs");*/

        if (initialVehicle != null) {
            // wait for 'bluebird'
            TestUtils.waitUntil(() -> {
                TestHelper.processAsync();
                sceneRunner.runLimitedFrames(1);
                List<EcsEntity> entities = SystemManager.findEntities(e -> "bluebird".equals(e.getName()));
                log.debug("" + entities.size());
                return entities.size() > 0;
            }, 60000);

            EcsEntity bluebirdEntity = SystemManager.findEntities(e -> "bluebird".equals(e.getName())).get(0);
            assertNotNull(bluebirdEntity, "bluebirdEntity");

            // No entity is created for vehicle sub models. The animations are contained in the vehicle entity
            FgAnimationComponent fgAnimationComponent = FgAnimationComponent.getFgAnimationComponent(bluebirdEntity);
            assertNotNull(fgAnimationComponent);
            // currently 587 animations(!)
            assertTrue(fgAnimationComponent.animationList.size() > 100, "" + fgAnimationComponent.animationList.size());
        }

        if (initialLocation==null){
            // default EDDK
            assertVector3(new Vector3(4001608.0239371024,500476.7661612326,4925011.036712442),SceneryScene.mainTransform.getPosition());
            assertQuaternion(new Quaternion(-0.2780844410824227,-0.7101587766754089,-0.6192861254879614,0.18662328449813553),SceneryScene.mainTransform.getRotation());

        } else {
            // assume for now its always EHAM06
            assertVector3(new Vector3(3896514.672946898,322690.2884301445,5022697.52194817),SceneryScene.mainTransform.getPosition());
            assertQuaternion(new Quaternion(0.6886338756334864,0.42273753635977596,-0.023259879717384857,0.5886725224418107),SceneryScene.mainTransform.getRotation());
        }
    }

    public static SceneRunnerForTesting buildSceneRunner(String scene, HashMap<String, String> additionalProperties, int initial_frames) {

        SystemState.state = 0;
        SystemManager.reset();

        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("scene", scene);
        properties.putAll(additionalProperties);
        // buildDefaultConfigurationWithEnv is needed for HOSTDIR
        FgTestFactory.initPlatformForTest(properties, false, true, true, false, new BundleResolverSetup.DefaultBundleResolverSetup());
        // not sufficient SceneRunnerForTesting sceneRunner = SceneRunnerForTesting.setupForScene(initial_frames, ConfigurationByEnv.buildDefaultConfigurationWithEnv(properties), new String[]{"engine", SGMaterialLib.BUNDLENAME});
        SceneRunnerForTesting sceneRunner = (SceneRunnerForTesting) SceneRunnerForTesting.getInstance();
        sceneRunner.runLimitedFrames(initial_frames);
        return sceneRunner;
    }
}
