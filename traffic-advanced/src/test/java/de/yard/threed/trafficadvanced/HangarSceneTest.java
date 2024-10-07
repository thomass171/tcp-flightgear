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
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.graph.GraphMovingComponent;
import de.yard.threed.traffic.VehicleComponent;
import de.yard.threed.trafficadvanced.apps.HangarScene;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Putting it all together and test interaction.
 */
public class HangarSceneTest {

    SceneRunnerForTesting sceneRunner;
    static final int INITIAL_FRAMES = 10;
    Log log;

    /**
     *
     */
    @Test
    public void testDefault() throws Exception {
        run(null);
    }

    @Test
    public void test777() throws Exception {
        run("777");
    }

    public void run(String initialVehicle) throws Exception {

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
        FgTestFactory.initPlatformForTest(properties, false, true, true);

        sceneRunner = (SceneRunnerForTesting) SceneRunnerForTesting.getInstance();
        sceneRunner.runLimitedFrames(INITIAL_FRAMES);
        log = Platform.getInstance().getLog(HangarSceneTest.class);
    }
}
