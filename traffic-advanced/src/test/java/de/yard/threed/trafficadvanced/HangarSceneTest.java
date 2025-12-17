package de.yard.threed.trafficadvanced;


import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsHelper;
import de.yard.threed.engine.ecs.VelocityComponent;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.flightgear.core.flightgear.main.FGGlobals;
import de.yard.threed.flightgear.core.simgear.scene.model.Model;
import de.yard.threed.flightgear.ecs.FgAnimationUpdateSystem;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.flightgear.testutil.SoundAssertions;
import de.yard.threed.graph.GraphMovingComponent;
import de.yard.threed.traffic.VehicleComponent;
import de.yard.threed.trafficadvanced.apps.HangarScene;
import de.yard.threed.trafficadvanced.testutil.AdvancedBundleResolverSetup;
import de.yard.threed.trafficadvanced.testutil.AdvancedTestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Putting it all together and test interaction.
 */
@Slf4j
public class HangarSceneTest {

    SceneRunnerForTesting sceneRunner;
    static final int INITIAL_FRAMES = 10;

    @ParameterizedTest
    @CsvSource(value = {
            "null",
            "777",
            "c172p",
    }, delimiter = ';', nullValues={"null"})
    public void testHangar(String initialVehicle) throws Exception {

        setup(initialVehicle);

        assertEquals(INITIAL_FRAMES, sceneRunner.getFrameCount());

        List<String> expectedBundles = new ArrayList<>(List.of("engine", "data", "traffic-advanced", "traffic-fg", "fgdatabasic"));
        if (initialVehicle != null) {
            expectedBundles.add(initialVehicle);
        } else {
            expectedBundles.add("bluebird");
        }
        TestUtils.waitUntil(() -> {
            sceneRunner.runLimitedFrames(5);
            return TestUtils.listComplete(Arrays.asList(BundleRegistry.getBundleNames()), expectedBundles);
        }, 60000);

        List<String> expectedVehicles = List.of("bluebird", "c172p", "777");
        TestUtils.waitUntil(() -> {
            sceneRunner.runLimitedFrames(5);
            return TestUtils.listComplete(((HangarScene) sceneRunner.ascene).vehiclelist, expectedVehicles);
        }, 30000);

        TestUtils.waitUntil(() -> {
            sceneRunner.runLimitedFrames(5);
            return ((HangarScene) sceneRunner.ascene).modelInited;
        }, 30000);

        TestUtils.waitUntil(() -> {
            sceneRunner.runLimitedFrames(5);
            return EcsHelper.findEntitiesByName(initialVehicle == null ? "bluebird" : initialVehicle).size() > 0;
        }, 60000);

        EcsEntity initial = EcsHelper.findEntitiesByName(initialVehicle == null ? "bluebird" : initialVehicle).get(0);
        assertNotNull(initial);

        if (initialVehicle == null) {
            // initialVehicle was bluebird, load c172p now
            Model.ghostedObjects.clear();
            ((HangarScene) HangarScene.getCurrent()).addNextVehicle();
            AdvancedTestUtils.loadAndValidateNextVehicleSupposedToBeC172(sceneRunner);

        }

        if ("c172p".equals(initialVehicle)) {
            // Check engine sound is loaded but not yet playing.
            SoundAssertions.validateBasicSound("c172-sound.xml");
            SoundAssertions.validateC172pSound(false, 0);

            // Even with multiple updates.
            for (int i = 0; i < 5; i++) {
                FgAnimationUpdateSystem.updateSound(0.1);
            }
            SoundAssertions.validateC172pSound(false, 0);

            //log.debug(""+FGGlobals.getInstance().get_props().dump("\n"));

            // Start playing. Just low rpm should be sufficient for 'engine'
            FGGlobals.getInstance().get_props().getNode("/engines/active-engine/rpm", false).setDoubleValue(11);
            // update several times and be sure that audio play is triggered once only.
            for (int i = 0; i < 5; i++) {
                FgAnimationUpdateSystem.updateSound(0.1);
            }
            SoundAssertions.validateC172pSound(true, 1);

            // Stop again.
            FGGlobals.getInstance().get_props().getNode("/engines/active-engine/rpm", false).setDoubleValue(0.0);
            // update several times and be sure that audio play is triggered once only.
            for (int i = 0; i < 5; i++) {
                FgAnimationUpdateSystem.updateSound(0.1);
            }
            SoundAssertions.validateC172pSound(false, 1);

        }
    }

    /**
     * Needs parameter, so no @Before
     */
    private void setup(String initialVehicle) throws Exception {
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("scene", "de.yard.threed.trafficadvanced.apps.HangarScene");
        if (initialVehicle != null) {
            properties.put("initialVehicle", initialVehicle);
        }
        FgTestFactory.initPlatformForTest(properties, false, true, true, false, new AdvancedBundleResolverSetup());

        sceneRunner = (SceneRunnerForTesting) SceneRunnerForTesting.getInstance();
        sceneRunner.runLimitedFrames(INITIAL_FRAMES);
    }
}
