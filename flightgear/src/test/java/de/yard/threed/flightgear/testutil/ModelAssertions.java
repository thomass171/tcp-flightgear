package de.yard.threed.flightgear.testutil;

import de.yard.threed.core.TreeNode;
import de.yard.threed.core.TreeNodeFilter;
import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.core.loader.LoaderGLTF;
import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.loader.PortableModelDefinition;
import de.yard.threed.core.loader.PortableModel;
import de.yard.threed.core.loader.PreparedModel;
import de.yard.threed.core.loader.PreparedObject;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;

import de.yard.threed.engine.Texture;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsHelper;
import de.yard.threed.flightgear.LoaderBTG;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.osg.Node;

import de.yard.threed.core.Vector2Array;
import de.yard.threed.core.Color;

import de.yard.threed.engine.test.testutil.TestUtil;
import de.yard.threed.flightgear.core.simgear.scene.model.SGModelLib;
import de.yard.threed.javacommon.SimpleHeadlessPlatform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * better in tools-fg? Only parts.
 */
public class ModelAssertions {

    public static Map<Integer, String[]> objectsPerTile = defineObjectsPerTile();

    public static void assertEgkkTower(PortableModel ppfile, int vertices, int indices) {
        //TestUtil.assertNotNull("root node", ppfile.root);
        TestUtil.assertEquals("number of objects", 6, ppfile.getRoot().getChild(0).kids.size());
        //TestUtil.assertEquals("number of kids", 6, ppfile.root.kids.size());
        TestUtil.assertEquals("number of materials", /*8*/5, ppfile.materials.size());
        PortableModelDefinition hazard = ppfile.findObject("hazard");
        TestUtil.assertEquals("materialname", "unshaded0DefaultWhite-egkk_tower", hazard.material);


       /*TODO don't assert material by index. List content might change
            TestUtil.assertEquals("materialname", "unshaded2hazard2Xmat-egkk_tower", ppfile.materials.get(2).getName());
       */
        TestUtil.assertEquals("texturebasepath", "flusi", ppfile.defaulttexturebasepath.getPath());
        //texture ist z.Z. nur bei den auch verwendeten gesetzt.
        //TODO don't assert material by index. List content might change TestUtil.assertEquals("texturename", "egkk_tower.png", ppfile.materials.get(3).getTexture());
        PortableModelDefinition obj0 = ppfile.getRoot().getChild(0).kids.get(0);
        TestUtil.assertEquals("obj0name", "hazard", obj0.name);
        SimpleGeometry obj0geo = obj0.geo;//list.get(0);
        TestUtil.assertEquals("number of vertices", vertices, obj0geo.getVertices().size());
        TestUtil.assertEquals("number of normals", vertices, obj0geo.getNormals().size());
        TestUtil.assertEquals("number of indices", indices, obj0geo.getIndices().length);
        TestUtil.assertEquals("number of uvs", vertices, obj0geo.getUvs().size());
        PortableModelDefinition tower = ppfile.getRoot().getChild(0).kids.get(5);
        PortableModelDefinition unshadedTowerPart = tower.kids.get(0);
        TestUtil.assertEquals("materialname", "unshaded3Material-egkk_tower", unshadedTowerPart.material);
        SimpleGeometry towergeo = unshadedTowerPart.geo;
        Vector2Array toweruvs = towergeo.getUvs();
        TestUtil.assertVector2("uv0", new Vector2(0.1163029f, 0.6413539f), toweruvs.getElement(0));
        TestUtil.assertVector2("uv1", new Vector2(0.0024108677f, 0.6413539f), toweruvs.getElement(1));

        PortableModelDefinition shadedTowerPart = tower.kids.get(1);
        TestUtil.assertEquals("materialname", "shaded3Material-egkk_tower", shadedTowerPart.material);

    }

    public static void assertWindturbine(PortableModel ppfile, int expectedmaterials, boolean loadedFromGltf) {
        PortableModelDefinition ppobj = ppfile.getRoot();
        if (loadedFromGltf) {
            assertEquals(LoaderGLTF.GLTF_ROOT, ppobj.getName());
            if (ppobj.kids.size() != 1) {
                fail("not  only one child");
            }
            ppobj = ppobj.getChild(0);
        }
        TestUtil.assertEquals("number of objects", 7, ppobj.kids.size());
        TestUtil.assertEquals("number of materials", expectedmaterials, ppfile.materials.size());
        TestUtil.assertEquals("materialname", (expectedmaterials == 2) ? "shaded0DefaultWhite" : "DefaultWhite", ppfile.materials.get(0).getName());
    }

    public static void assertFollowMe(PortableModel ppfile, int expectedmaterials) {
        TestUtil.assertEquals("number of objects", 31, ppfile.getRoot().getChild(0).kids.size());
        TestUtil.assertEquals("number of materials", expectedmaterials, ppfile.materials.size());
        // "DefaultWhite" isn't used at all
        TestUtil.assertEquals("materialname", "shaded1followmeoutside-followmeoutside", ppfile.materials.get(0).getName());
        PortableModelDefinition car = ppfile.getRoot().getChild(0).kids.get(0);
        TestUtil.assertEquals("number of car.objects", 13, car.kids.size());
        PortableModelDefinition mesh76 = car.kids.get(0);
        TestUtil.assertEquals("number of mesh76.objects", 3, mesh76.kids.size());
        PortableModelDefinition doorrl = mesh76.kids.get(1);
        TestUtil.assertEquals("number of doorrl.objects", 2, doorrl.kids.size());

        // 15.8.24: Object with mat17 ("indicater_left") seems to be shaded
        PortableMaterial shadedMaterial_4 = ppfile.findMaterial("shaded17Material_4");
        TestUtil.assertColor("unshadedMaterial_4.color", new Color(1.0f, 0.8f, 0.2f, 1.0f), shadedMaterial_4.getColor());
        TestUtil.assertTrue("unshadedMaterial_4.shaded", shadedMaterial_4.isShaded());
    }

    /**
     * Used for both btg- and gltf loading.
     * "ppfile" will be different with/without matlib.
     */
    public static void assertRefbtg(PortableModel portableModel, boolean hadmatlib, boolean loadedFromGltf) {

        PortableModelDefinition ppobj = getBtgRoot(portableModel, loadedFromGltf);

        // next node should be btg-root. 3056410 is refBTG
        assertBtgRootNode(ppobj, 3056410, 17);

        // preprocess liefert ein Object mit 17 children.

        TestUtil.assertNotNull("translation", ppobj.translation);
        TestUtil.assertVector3("gbs_center", FlightGear.refbtgcenter, ppobj.translation);
        TestUtil.assertEquals("geolists", 17, ppobj.kids.size());
        // dass geo0 die mit 43 ist, ist wohl Zufall.
        TestUtil.assertEquals("vertices", 43, ppobj.getChild(0).geo.getVertices().size());
        // normals sind jetzt genausoviele wie vertices. Wie auch immer, sinnig ist es.
        TestUtil.assertEquals("normals", 43, ppobj.getChild(0).geo.getNormals().size());
        //12.12.17: das mit den indices ist unklar. Passt aber zu 120 Triangles (Wiki)
        TestUtil.assertEquals("indices", 120, ppobj.getChild(0).geo.getIndices().length);
        //stichprobenartig ein paar Werte testen. Initial erstmal so uebernommen
        TestUtil.assertVector3("", new Vector3(4341.8657f, -6667.45f, -2179.5708f), ppobj.getChild(0).geo.getVertices().getElement(0));
        TestUtil.assertVector3("", new Vector3(-646.4524f, 2071.1538f, 1039.9973f), ppobj.getChild(3).geo.getVertices().getElement(4));
        //ppobj = ppfile.objects.get(1);
        //TestUtil.assertEquals("indices", 0, ppobj.getChild(0).getIndices().length);
        // die 17 materials sind aber alle null, wenn es keine Materiallib gibt. Ob das ideal ist, naja, so ist es jetzt aber.
        if (hadmatlib) {
            TestUtil.assertEquals("geolistmaterial0", "0", ppobj.getChild(0).material);//geolistmaterial.get(0));
            TestUtil.assertEquals("materials", 17, portableModel.materials.size());
            for (int i = 0; i < 17; i++) {
                TestUtil.assertEquals("materials0.name", "" + i, portableModel.materials.get(i).getName());
            }
            //die wraps muessen immer true sein.
            TestUtil.assertTrue("geolistmaterial0.wraps", portableModel.materials.get(0).getWraps());
            TestUtil.assertTrue("geolistmaterial0.wrapt", portableModel.materials.get(5).getWrapt());

        } else {
            // Without matlib the models do not contain any material. But the material names still exist as a kind
            // of attribute.
            TestUtil.assertEquals("materials", 0, portableModel.materials.size());
            // landclasses are material names
            TestUtil.assertEquals("materials0.name", "Grassland", ppobj.getChild(0).material);
            TestUtil.assertEquals("materials4.name", "OpenMining", ppobj.getChild(4).material);
            TestUtil.assertEquals("materials9.name", "Scrub", ppobj.getChild(9).material);
            TestUtil.assertEquals("materials14.name", "CropGrass", ppobj.getChild(14).material);
            TestUtil.assertEquals("materials16.name", "MixedForest", ppobj.getChild(16).material);
        }
    }

    public static void assertBTG3072816(PortableModel ppfile, boolean hadmatlib, boolean loadedFromGltf) {

        PortableModelDefinition ppobj = getBtgRoot(ppfile, loadedFromGltf);

        TestUtil.assertEquals("geolists", 24, ppobj.kids.size());

        if (hadmatlib) {
            TestUtil.assertEquals("materials", 24, ppfile.materials.size());
            for (int i = 0; i < 24; i++) {
                TestUtil.assertEquals("materials0.name", "" + i, ppfile.materials.get(i).getName());
            }
            //die wraps muessen immer true sein.
            TestUtil.assertTrue("geolistmaterial0.wraps", ppfile.materials.get(0).getWraps());
            TestUtil.assertTrue("geolistmaterial0.wrapt", ppfile.materials.get(5).getWrapt());

        } else {
            TestUtil.assertEquals("materials", 0, ppfile.materials.size());
        }
    }

    public static void assertRefbtgNode(Node node, String rootnodename) {
        TestUtil.assertNotNull("", node);
        TestUtil.assertEquals("rootnode.name", rootnodename, node.getName());
        TestUtil.assertEquals("number of kids", 1, node.getTransform().getChildren().size());
        SceneNode terraingroup = node.getTransform().getChildren().get(0).getSceneNode();
        TestUtil.assertEquals("terraingroup.name", "BTGTerrainGroup", terraingroup.getName());
        SceneNode sn = terraingroup.getTransform().getChildren().get(0).getSceneNode();
        TestUtil.assertEquals("sn.name", "surfaceGeometryGroup", sn.getName());
        TestUtil.assertEquals("number of kids/geos", 17, sn.getTransform().getChildren().size());
        SceneNode geo0 = sn.getTransform().getChildren().get(0).getSceneNode();
        TestUtil.assertNotNull("material.0", geo0.getMesh().getMaterial());
        //der material name ist wohl nie gefuellt 
        TestUtil.assertNull("materials0.name", geo0.getMesh().getMaterial().getName());

        assertEquals(16, Texture.texturePoolSize());
        // only check 1 texture
        assertTrue(Texture.hasTexture("mixedforest-hires-autumn.png"), "texture");
    }

    public static void assertCDU777(PortableModel ppfile, boolean fromgltf) {
        PortableModelDefinition root = ppfile.getRoot();
        if (fromgltf) {
            root = root.getChild(0);
        }
        TestUtil.assertEquals("", 84, root.kids.size());
        PortableModelDefinition ppknob = root.kids.get(0);
        SimpleGeometry geo = ppknob.geo;
        TestUtil.assertEquals("name", "Bright_UprDisp.knob", ppknob.name);
        TestUtil.assertVector3("loc", new Vector3(0.016473f, 0.004128f, -0.046148f), ppknob.translation);
        //material wurde dupliziert
        TestUtil.assertEquals("materials", /*14*/9, ppfile.materials.size());
        PortableMaterial unshaded0Defaultwhite = ppfile.findMaterial("unshaded0DefaultWhite-APpanel_1");
        assertNotNull(unshaded0Defaultwhite);
        TestUtil.assertEquals("material.name", /*(fromgltf)?"0":*/"unshaded0DefaultWhite-APpanel_1", unshaded0Defaultwhite.getName());
        //15.8.24:texture no longer with color
        assertNull(unshaded0Defaultwhite.getColor());
        TestUtil.assertEquals("shininess", 0.64f, unshaded0Defaultwhite.getShininess().value);
        TestUtil.assertFalse("shaded", unshaded0Defaultwhite.isShaded());
        /*TODO 5.10.24 don't assert material by index. List content might change PortableMaterial unshadeddefaultwhite = ppfile.materials.get(1);
        TestUtil.assertEquals("material.name", /*(fromgltf)?"0":* /"unshadedDefaultWhite", unshadeddefaultwhite.getName());

        PortableMaterial shadedinstr_lights = ppfile.materials.get(4);
        TestUtil.assertEquals("material.name", /*(fromgltf)?"4":* /"shadedinstr-lights", shadedinstr_lights.getName());
        TestUtil.assertColor("color", new Color(0.8f, 0.8f, 0.8f, 1f), shadedinstr_lights.getColor());
        TestUtil.assertColor("color", new Color(0.5f, 0.5f, 0.5f, 1f), shadedinstr_lights.getEmis());
        if (!fromgltf) {
            //?? how
            /*12.8.24 not used anywhere?? TestUtil.assertColor("color", new Color(0.8f, 0.8f, 0.8f, 1f), shadedinstr_lights.ambient);
            TestUtil.assertColor("color", new Color(0f, 0f, 0f, 1f), shadedinstr_lights.specular);* /
        }
        TestUtil.assertEquals("shininess", 0f, shadedinstr_lights.getShininess().value);
        TestUtil.assertTrue("shaded", shadedinstr_lights.isShaded());
        //22.1.18: Das muss bei AC wohl so sein.
        TestUtil.assertTrue("wraps", shadedinstr_lights.getWraps());
        TestUtil.assertTrue("wrapt", shadedinstr_lights.getWrapt());
*/
    }

    public static void assertOverhead777(PortableModel ppfile, boolean fromgltf) {
        TestUtil.assertEquals("", 861, ppfile.getRoot().kids.size());
        // das PANEL_B1 ist eine einfache Platte (Wuerfel), gut zum Testen
        PortableModelDefinition panel_b1 = ppfile.findObject("PANEL_B1");
        SimpleGeometry geo = panel_b1.getChild(0).geo;
        TestUtil.assertEquals("name", "PANEL_B1", panel_b1.name);
        TestUtil.assertVector3("loc", new Vector3(0, 0, -0.002101f), panel_b1.translation);

        PortableModelDefinition LBUS_Switch = ppfile.findObject("LBUS_Switch");
        String LBUS_Switchmatname = LBUS_Switch.material;//geolistmaterial.get(0);
        TestUtil.assertEquals("matname", "unshadedgreenglow43", LBUS_Switchmatname);
        PortableMaterial LBUS_Switchmat = ppfile.findMaterial(LBUS_Switchmatname);
        TestUtil.assertEquals("texture.name", "Buttons_03.png", LBUS_Switchmat.getTexture());

        PortableModelDefinition PANEL_A4 = ppfile.findObject("PANEL_A4");
        TestUtil.assertEquals("PANEL_A4.kids", 7, PANEL_A4.kids.size());
        TestUtil.assertVector3("loc", new Vector3(0, -0.251259f, -1.57064f), PANEL_A4.translation);

        PortableModelDefinition plate1_008 = PANEL_A4.kids.get(2);
        TestUtil.assertEquals("name", "PANEL_B1", panel_b1.name);
        TestUtil.assertVector3("loc", new Vector3(0.000148f, 0, -0.001075f), plate1_008.translation);


    }

    public static void assertTestData(PortableModel ppfile, int expectedmaterials) {
        TestUtil.assertEquals("number of objects", 5, ppfile.getRoot().kids.size());
        TestUtil.assertEquals("number of materials", expectedmaterials, ppfile.materials.size());
        TestUtil.assertEquals("materialname", "ROOF_DEFAULT", ppfile.materials.get(3).getName());
    }

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
        String[] objectsIn3072824 = objectsPerTile.get(3072824);
        assertEquals(objectsIn3072824.length, entities.size());
        for (int i = 0; i < objectsIn3072824.length; i++) {
            assertEquals(objectsIn3072824[i], entities.get(i).getName());
        }

        SceneNode stg3072824 = FgTestUtils.findAndAssertStgNode(3072824);
        SceneNode stgGroupA = stg3072824.getTransform().getChild(1).getSceneNode();
        assertEquals(12, stgGroupA.getTransform().getChildCount());
    }

    /**
     * btgroot should be beyond of a possible gltf root.
     *
     * @return
     */
    public static void assertBtgRootNode(PortableModelDefinition ppobj, int bucket, int expectedChildren) {
        assertEquals(LoaderBTG.BTG_ROOT + "-" + bucket, ppobj.getName());
        assertEquals(expectedChildren, ppobj.kids.size());
    }

    public static PortableModelDefinition getBtgRoot(PortableModel portableModel, boolean loadedFromGltf) {
        PortableModelDefinition ppobj = portableModel.getRoot();
        if (loadedFromGltf) {
            assertEquals(LoaderGLTF.GLTF_ROOT, ppobj.getName());
            if (ppobj.kids.size() != 1) {
                fail("not  only one child");
            }
            ppobj = ppobj.getChild(0);
        }
        return ppobj;
    }

    private static Map<Integer, String[]> defineObjectsPerTile() {
        Map<Integer, String[]> objectsPerTile = new HashMap<Integer, String[]>();
        objectsPerTile.put(3072824, new String[]{
                "Objects/e000n50/e007n50/EDDH_Radartower2.xml",
                "Objects/e000n50/e007n50/egkk_carpark_multi.xml",
                "Objects/e000n50/e007n50/egkk_carpark_multi.xml",
                "Objects/e000n50/e007n50/egkk_carpark_multi.xml",
                "Objects/e000n50/e007n50/egkk_carpark_multi.xml",
                "Objects/e000n50/e007n50/egkk_tower.xml",
                "Objects/e000n50/e007n50/windturbine.xml"
        });
        objectsPerTile.put(3072816, new String[]{
                // 1.6.24 what do we expect here?
        });
        objectsPerTile.put(3056435, new String[]{
                // 1.6.24 what do we expect here?
        });
        objectsPerTile.put(3056443, new String[]{
                // 1.6.24 what do we expect here?
        });
        return objectsPerTile;
    }

    /**
     * Should be a shared model, so no parameter
     */
    public static void assertWindsock() {
        PreparedModel preparedModel = SGModelLib.preparedModelCache.get("Models/Airport/windsock.gltf");
        assertNotNull(preparedModel);

        List<TreeNode<PreparedObject>> wf4s = preparedModel.getRoot().findNode(n -> "wf4".equals(n.getElement().getName()));
        assertEquals(1, wf4s.size());
        PreparedObject wf4 = wf4s.get(0).getElement();

        SimpleHeadlessPlatform.DummyMaterial material = (SimpleHeadlessPlatform.DummyMaterial) wf4.material;
        // no texture, but should be shaded TODO fix somewhere
        assertEquals(0, material.parameters.size());
    }
}
