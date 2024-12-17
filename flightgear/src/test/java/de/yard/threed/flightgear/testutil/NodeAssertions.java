package de.yard.threed.flightgear.testutil;

import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsHelper;
import de.yard.threed.engine.test.testutil.TestUtil;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Assertions for nodes at runtime similar to ModelAssertions for model files.
 */
public class NodeAssertions {

    /**
     * STG3072816 (in objects) contains
     * - 37 regular objects (OBJECT_STATIC)
     * - 75 signs (OBJECT_SIGN)
     * - 608 shared objects (OBJECT_SHARED), 3 of which are windturbines, 2 windsock
     * (in Terrain) contains
     * - 0 regular objects (OBJECT_STATIC)
     * - 0 signs (OBJECT_SIGN)
     * - 9 shared objects (OBJECT_SHARED), 2 of which are beacons, 1 windsock
     */
    public static void assertSTG3072816(SceneNode destinationNode) {
        SceneNode stg3072816 = FgTestUtils.findAndAssertStgNode(3072816);
        SceneNode stgGroupA = stg3072816.getTransform().getChild(1).getSceneNode();
        assertEquals(37 + 608 + 9, stgGroupA.getTransform().getChildCount());

        List<SceneNode> beacons = stgGroupA.findNodeByName("Models/Airport/beacon.xml");
        assertEquals(2, beacons.size());
    }

    /**
     * STG3072824 (in objects) contains
     * - 12 regular objects (OBJECT_STATIC)
     * - 18 signs (OBJECT_SIGN)
     * - 303 shared objects (OBJECT_SHARED)
     */
    public static void assertSTG3072824(SceneNode destinationNode) {
        assertEquals(1, SceneNode.findByName("Objects/e000n50/e007n50/egkk_tower.xml").size());
        assertEquals(1, SceneNode.findByName("Objects/e000n50/e007n50/windturbine.xml").size());
        List<EcsEntity> entities = EcsHelper.findAllEntities();
        String[] objectsIn3072824 = ModelAssertions.objectsPerTile.get(3072824);
        assertEquals(objectsIn3072824.length, entities.size());
        for (int i = 0; i < objectsIn3072824.length; i++) {
            assertEquals(objectsIn3072824[i], entities.get(i).getName());
        }

        SceneNode stg3072824 = FgTestUtils.findAndAssertStgNode(3072824);
        SceneNode stgGroupA = stg3072824.getTransform().getChild(1).getSceneNode();
        assertEquals(12, stgGroupA.getTransform().getChildCount());
    }

    public static void assertEgkkTower(SceneNode egkkTowerNode) {
        TestUtil.assertEquals("number of kids gltfroot", 1, egkkTowerNode.getTransform().getChildren().size());
        TestUtil.assertEquals("number of kids ac-root", 1, egkkTowerNode.getTransform().getChild(0).getChildren().size());
        TestUtil.assertEquals("number of kids", 6, egkkTowerNode.getTransform().getChild(0).getChild(0).getChildren().size());
        TestUtil.assertNotNull("kid(0) mesh", egkkTowerNode.getTransform().getChild(0).getChild(0).getChild(0).getSceneNode().getMesh());

    }
}
