package de.yard.threed.toolsfg.testutil;

import de.yard.threed.core.PortableModelTest;
import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.core.loader.LoaderGLTF;
import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.loader.PortableModel;
import de.yard.threed.core.loader.PortableModelDefinition;

import static org.junit.jupiter.api.Assertions.*;

public class ModelAssertions {

    public static void assertTerminal1(PortableModel portableModel, boolean loadedFromGltf) {
        PortableModelDefinition root = portableModel.getRoot();

        if (loadedFromGltf) {
            root = PortableModelTest.validateSingleRoot(root, LoaderGLTF.GLTF_ROOT);
        }

        PortableModelDefinition miniTower = portableModel.findObject("vorfeldkontrolle");
        assertNotNull(miniTower);
        // ac object "vorfeldkontrolle" was split into two due to two different materials
        // Both materials don't use textures
        assertEquals(2, miniTower.kids.size());
        assertEquals("subnode0", miniTower.getChild(0).getName());
        assertEquals("unshaded21roads", miniTower.getChild(0).material);
        assertEquals("subnode1", miniTower.getChild(1).getName());
        assertEquals("unshaded28glass", miniTower.getChild(1).material);

        PortableMaterial unshadedglass = portableModel.findMaterial("unshaded28glass");
        assertNotNull(unshadedglass);
        assertEquals(0.2727, unshadedglass.getTransparency().floatValue(), 0.0001);
        assertFalse(unshadedglass.isShaded());
        assertNull(unshadedglass.getTexture());


        PortableModelDefinition terminal1Starwalk = portableModel.findObject("Terminal 1 Starwalk");
        assertNotNull(terminal1Starwalk);
        // there was no split
        assertEquals(0, terminal1Starwalk.kids.size());
        SimpleGeometry geo = terminal1Starwalk.geo;
        //36 vert, 22 sruf (8 face4, 10/9 Face3, 4 face4)
        //56 assertEquals(36,geo.getVertices().size());
        assertEquals(16 * 3 + 10 * 3 + 8 * 3, geo.getIndices().length);// 102 indices?

    }

    public static void assertYoke(PortableModel portableModel, boolean loadedFromGltf) {
        PortableModelDefinition root = portableModel.getRoot();

        if (loadedFromGltf) {
            root = PortableModelTest.validateSingleRoot(root, LoaderGLTF.GLTF_ROOT);
        }

        PortableModelTest.assertLevel(root, "ac-world", new String[]{"Yoke"});
        // 14.8.24 materials now only contains really used material
        assertEquals(3, portableModel.getMaterialCount());

        PortableModelDefinition yoke = portableModel.findObject("Yoke");
        assertNotNull(yoke);
        // ac object "Yoke" was split into two due to three different materials
        // Both materials don't use textures?? TODO check
        assertEquals(3, yoke.kids.size());
        assertEquals("subnode0", yoke.getChild(0).getName());
        assertEquals("shaded0NoName-yoke", yoke.getChild(0).material);
        PortableMaterial shadedNoName = portableModel.findMaterial(yoke.getChild(0).material);
        assertNotNull(shadedNoName);
        assertTrue(shadedNoName.isShaded());
        assertNotNull(shadedNoName.getTexture());
        // "yoke.rgb" was replaced by ??
        assertEquals("yoke.png", shadedNoName.getTexture());

        assertEquals("subnode1", yoke.getChild(1).getName());
        assertEquals("shaded2NoName-yoke", yoke.getChild(1).material);
        PortableMaterial shaded2NoNameyoke = portableModel.findMaterial("shaded2NoName-yoke");
        assertNotNull(shaded2NoNameyoke);
        assertTrue(shaded2NoNameyoke.isShaded());
        assertNotNull(shaded2NoNameyoke.getTexture());
        // "yoke.rgb" was replaced by ??
        assertEquals("yoke.png", shaded2NoNameyoke.getTexture());

        assertEquals("subnode2", yoke.getChild(2).getName());
        assertEquals("unshaded1ac3dmat3-yoke", yoke.getChild(2).material);
        PortableMaterial unshaded1ac3dmat3 = portableModel.findMaterial("unshaded1ac3dmat3-yoke");
        assertNotNull(unshaded1ac3dmat3);
        assertFalse(unshaded1ac3dmat3.isShaded());
        assertNotNull(unshaded1ac3dmat3.getTexture());
        // "yoke.rgb" was replaced by ??
        assertEquals("yoke.png", unshaded1ac3dmat3.getTexture());
    }

    /**
     * sketch/skizze 34
     */
    public static void assertEgkkMaint_1_1(PortableModel portableModel, boolean loadedFromGltf) {
        PortableModelDefinition root = portableModel.getRoot();

        if (loadedFromGltf) {
            root = PortableModelTest.validateSingleRoot(root, LoaderGLTF.GLTF_ROOT);
        }

        PortableModelDefinition maint_1_1 = portableModel.findObject("maint_1_1");
        assertNotNull(maint_1_1);
        assertEquals(0, maint_1_1.kids.size());

        SimpleGeometry geo = maint_1_1.geo;
        //8 vert->20?, 5 surf ()-> 10 face3 -> 30 vertices
        assertEquals(30,geo.getVertices().size());
        assertEquals(30,geo.getNormals().size());
        assertEquals(30, geo.getIndices().length);// 30 indices?

    }

    /**
     * sketch/skizze 35
     */
    public static void assertJetwayMovable(PortableModel portableModel, boolean loadedFromGltf) {
        PortableModelDefinition root = portableModel.getRoot();

        if (loadedFromGltf) {
            root = PortableModelTest.validateSingleRoot(root, LoaderGLTF.GLTF_ROOT);
        }

        PortableModelDefinition tunnel1Rotunda = portableModel.findObject("Tunnel1Rotunda");
        assertNotNull(tunnel1Rotunda);
        assertEquals(0, tunnel1Rotunda.kids.size());

        SimpleGeometry geo = tunnel1Rotunda.geo;
        assertEquals(4*6,geo.getVertices().size());
        assertEquals(4*6,geo.getNormals().size());
        // 36 fits to similar test in tcp22
        assertEquals(36, geo.getIndices().length);

    }
}
