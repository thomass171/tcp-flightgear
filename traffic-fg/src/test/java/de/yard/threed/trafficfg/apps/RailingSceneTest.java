package de.yard.threed.trafficfg.apps;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ObserverComponent;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsTestHelper;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.TeleportComponent;
import de.yard.threed.engine.ecs.VelocityComponent;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.flightgear.core.simgear.scene.model.SGMaterialAnimation;
import de.yard.threed.flightgear.core.simgear.scene.model.SGRotateAnimation;
import de.yard.threed.flightgear.ecs.FgAnimationComponent;
import de.yard.threed.flightgear.testutil.AnimationAssertions;
import de.yard.threed.graph.GraphMovingComponent;
import de.yard.threed.graph.GraphMovingSystem;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class RailingSceneTest {

    SceneRunnerForTesting sceneRunner;
    // 50 frames re currently wasted for terrain waiting
    static final int INITIAL_FRAMES = 100;

    // set in FgVehicleLoader by: config.getName() + "-root"
    String vehiclePropertyRootNodeName = "locomotive-root";

    /**
     *
     */
    @Test
    public void testLaunch() {
        sceneRunner = ScenerySceneTest.buildSceneRunner("de.yard.threed.trafficfg.apps.RailingScene", new HashMap<>(), INITIAL_FRAMES);
        Log logger = Platform.getInstance().getLog(RailingSceneTest.class);

        List<EcsEntity> entities = SystemManager.findEntities(null);
        EcsEntity userEntity = SystemManager.findEntities(e -> "driver".equals(e.getName())).get(0);
        assertNotNull(userEntity, "userEntity");
        assertNotNull(userEntity.getName(), "name");

        Vector3 position = userEntity.getSceneNode().getTransform().getPosition();
        logger.debug("position=" + position);

        EcsEntity locEntity = SystemManager.findEntities(e -> "locomotive".equals(e.getName())).get(0);
        assertNotNull(locEntity, "locEntity");
        assertNotNull(locEntity.getName(), "name");

        assertNotNull(ObserverComponent.getObserverComponent(userEntity));
        // TODO validate brachselector

        sceneRunner.runLimitedFrames(50);
        // loc should still be not moving
        // TODO check autostart mode
        TeleportComponent tc = TeleportComponent.getTeleportComponent(userEntity);
        // should start in vehicle. TODO: Check why 0,0,0 is correct position
        EcsTestHelper.assertTeleportComponent(tc, 1 + 3, 3, new Vector3(),"locomotive");
        GraphMovingSystem graphMovingSystem = (GraphMovingSystem) SystemManager.findSystem(GraphMovingSystem.TAG);
        GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(locEntity);
        assertNotNull(gmc);
        VelocityComponent vc = VelocityComponent.getVelocityComponent(locEntity);
        assertNotNull(vc);
        // No entity is created for 'ASI'. The animations are contained in the vehicle entity
        // loc uses an asi.xml without center!
        FgAnimationComponent fgAnimationComponent = FgAnimationComponent.getFgAnimationComponent(locEntity);
        assertNotNull(fgAnimationComponent);
        AnimationAssertions.assertAsiAnimations(locEntity.getSceneNode(), fgAnimationComponent.animationList, 0.0,
                new Vector3());

        // start moving
        gmc.setAutomove(true);
        vc.setMovementSpeed(15.0);
        sceneRunner.runLimitedFrames(5);

        AnimationAssertions.assertAsiAnimations(locEntity.getSceneNode(), fgAnimationComponent.animationList, 15.0,
                new Vector3());

    }

}
