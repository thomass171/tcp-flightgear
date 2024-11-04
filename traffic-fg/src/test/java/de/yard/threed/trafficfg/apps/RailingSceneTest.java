package de.yard.threed.trafficfg.apps;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ObserverComponent;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsTestHelper;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.flightgear.ecs.FgAnimationComponent;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class RailingSceneTest {

    SceneRunnerForTesting sceneRunner;
    // 50 frames re currently wasted for terrain waiting
    static final int INITIAL_FRAMES = 100;

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
        // should start in vehicle. TODO: Check why 0,0,0 is correct position
        EcsTestHelper.assertTeleportComponent(userEntity, 1 + 3, 3, new Vector3());

        // No entity is created for 'ASI'. The animations are contained in the vehicle entity
        FgAnimationComponent fgAnimationComponent=FgAnimationComponent.getFgAnimationComponent(locEntity);
        assertNotNull(fgAnimationComponent);
        // one Material and one RotateAnimation (ASI needle)
        assertEquals(2, fgAnimationComponent.animationList.size());
    }

}
