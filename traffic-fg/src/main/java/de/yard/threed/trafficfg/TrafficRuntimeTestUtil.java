package de.yard.threed.trafficfg;

import de.yard.threed.core.StringUtils;
import de.yard.threed.core.testutil.RuntimeTestUtil;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.Transform;

/**
 * For tests/assertions used in unit tests and runtime.
 * Runtime tests are important for revealing platform effects because
 * they use a 'real' platform, while unit tests use SimpleHeadlessPlatform
 */
public class TrafficRuntimeTestUtil {

    public static void assertSceneryNodes(SceneNode sphereWorld) {
        // sphere world should be child of Scene.world
        Transform parent = sphereWorld.getTransform().getParent();
        RuntimeTestUtil.assertNotNull("sphere.world in scene.world", parent);
        RuntimeTestUtil.assertEquals("sphere.world in scene.world", "World", parent.getSceneNode().getName());
        RuntimeTestUtil.assertEquals("FGScenery in sphere.world", 1, SceneNode.findNode(n -> "FGScenery".equals(n.getName() == null ? "" : n.getName()), sphereWorld).size());
    }
}
