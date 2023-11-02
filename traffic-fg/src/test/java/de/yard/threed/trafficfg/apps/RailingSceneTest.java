package de.yard.threed.trafficfg.apps;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ObserverComponent;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
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

    }

}
