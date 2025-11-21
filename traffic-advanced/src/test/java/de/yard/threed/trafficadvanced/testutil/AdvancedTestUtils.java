package de.yard.threed.trafficadvanced.testutil;

import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsHelper;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.flightgear.core.simgear.scene.model.Model;

import static de.yard.threed.javanative.JavaUtil.sleepMs;
import static org.junit.jupiter.api.Assertions.*;

public class AdvancedTestUtils {

    /**
     * Why not TravelSceneTestHelper.loadAndValidateVehicle? First HangasrScene needs TrafficSystem/Sphere nad load via request
     * TODO merge with TravelSceneTestHelper.loadAndValidateVehicle
     * @param sceneRunner
     * @throws Exception
     */
    @Deprecated
    public static void loadAndValidateNextVehicleSupposedToBeC172(SceneRunnerForTesting sceneRunner) throws Exception {
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
        //4.11.25 2024 c172 no longer has this 'garmin196' NativeSceneNode garmin196 = SceneNode.findByName("Aircraft/Instruments-3d/garmin196/garmin196.gltf").get(0);
        NativeSceneNode garmin196 = SceneNode.findByName("Aircraft/Instruments-3d/garmin196/garmin196_map_symbols.gltf").get(0);
        //16.8.24 TODO assertTrue(Texture.hasTexture("screens.png"), "garmin.texture");

        // ASI not visible? Effect failure? Might indicate missing aircraftprovider.
        assertFalse(Model.ghostedObjects.contains("Needle"));
        assertFalse(Model.ghostedObjects.contains("Face"));
    }
}
