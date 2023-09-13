package de.yard.threed.flightgear.testutil;

import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.loader.PortableModelDefinition;
import de.yard.threed.core.loader.PortableModelList;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;

import de.yard.threed.flightgear.LoaderBTG;
import de.yard.threed.flightgear.LoaderOptions;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.osg.Node;

import de.yard.threed.core.Vector2Array;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.core.Color;

import de.yard.threed.engine.test.testutil.TestUtil;
import de.yard.threed.core.buffer.ByteArrayInputStream;

/**
 * wird auch aus desktop/tools verwendet.
 * 9.3.21: Da es sehr FG lastig ist, nach traffic-ext.
 */
public class ModelAssertions {
    public static void assertEgkkTower(PortableModelList ppfile, int vertices, int indices, boolean shadedmaterial, boolean builtbyblender) {
        //TestUtil.assertNotNull("root node", ppfile.root);
        TestUtil.assertEquals("number of objects", 6, ppfile.getObjectCount());
        //TestUtil.assertEquals("number of kids", 6, ppfile.root.kids.size());
        TestUtil.assertEquals("number of materials", (shadedmaterial) ? 8 : 4, ppfile.materials.size());
        if (shadedmaterial) {
            TestUtil.assertEquals("materialname", "shadedDefaultWhite", ppfile.materials.get(0).name);
            TestUtil.assertEquals("materialname", "shadedhazard2Xmat", ppfile.materials.get(2).name);
        } else {
            TestUtil.assertEquals("materialname", "DefaultWhite", ppfile.materials.get(0).name);
            TestUtil.assertEquals("materialname", "hazard2Xmat", ppfile.materials.get(1).name);
        }
        TestUtil.assertEquals("texturebasepath", "flusi", ppfile.defaulttexturebasepath.path);
        //texture ist z.Z. nur bei den auch verwendeten gesetzt.
        TestUtil.assertEquals("texturename", "egkk_tower.png", ppfile.materials.get(3).texture);
        PortableModelDefinition obj0 = ppfile.getObject(0);
        TestUtil.assertEquals("obj0name", "hazard", obj0.name);
        SimpleGeometry obj0geo = obj0.geolist.get(0);
        TestUtil.assertEquals("number of vertices", vertices, obj0geo.getVertices().size());
        TestUtil.assertEquals("number of normals", vertices, obj0geo.getNormals().size());
        TestUtil.assertEquals("number of indices", indices, obj0geo.getIndices().length);
        TestUtil.assertEquals("number of uvs", vertices, obj0geo.getUvs().size());
        PortableModelDefinition tower = ppfile.getObject(5);
        SimpleGeometry towergeo = tower.geolist.get(0);
        Vector2Array toweruvs = towergeo.getUvs();
        //28.3.18: der v Wert aus dem bin Buffer muss evtl. geflippt werden. der Blener Export hat andere Reihenfolge
        if (builtbyblender) {
            TestUtil.assertVector2("uv0", new Vector2(0.0024108677f, 0.6413539f), toweruvs.getElement(0));
            //den x-Wert gibt es zweimal
            //TestUtil.assertVector2("uv1", new Vector2(0.1163029f, 0.6413539f), toweruvs.getElement(1));
            TestUtil.assertVector2("uv1", new Vector2(0.1163029f, 0.52769506f), toweruvs.getElement(1));
            
        } else {
            TestUtil.assertVector2("uv0", new Vector2(0.1163029f, 0.6413539f), toweruvs.getElement(0));
            TestUtil.assertVector2("uv1", new Vector2(0.0024108677f, 0.6413539f), toweruvs.getElement(1));
        }
    }

    public static void assertWindturbine(PortableModelList ppfile, int expectedmaterials) {
        TestUtil.assertEquals("number of objects", 7, ppfile.getObjectCount());
        TestUtil.assertEquals("number of materials", expectedmaterials, ppfile.materials.size());
        TestUtil.assertEquals("materialname", (expectedmaterials == 2) ? "shadedDefaultWhite" : "DefaultWhite", ppfile.materials.get(0).name);
    }

    public static void assertFollowMe(PortableModelList ppfile, int expectedmaterials) {
        TestUtil.assertEquals("number of objects", 31, ppfile.getObjectCount());
        TestUtil.assertEquals("number of materials", expectedmaterials, ppfile.materials.size());
        TestUtil.assertEquals("materialname", "shadedDefaultWhite" , ppfile.materials.get(0).name);
        PortableModelDefinition car = ppfile.getObject(0);
        TestUtil.assertEquals("number of car.objects", 13, car.kids.size());
        PortableModelDefinition mesh76 = car.kids.get(0);
        TestUtil.assertEquals("number of mesh76.objects", 3, mesh76.kids.size());
        PortableModelDefinition doorrl = mesh76.kids.get(1);
        TestUtil.assertEquals("number of doorrl.objects", 2, doorrl.kids.size());

        PortableMaterial unshadedMaterial_4 = ppfile.findMaterial("unshadedMaterial_4");
        TestUtil.assertColor("unshadedMaterial_4.color",new Color(1.0f,0.8f,0.2f,1.0f),unshadedMaterial_4.color);
        TestUtil.assertFalse("unshadedMaterial_4.shaded",unshadedMaterial_4.shaded);
    }

    public static void assertRefbtg(PortableModelList ppfile, boolean hadmatlib) {
        // preprocess liefert ein Object mit 17 geos.
        TestUtil.assertEquals("objects", 1, ppfile.getObjectCount());
        PortableModelDefinition ppobj = ppfile.getObject(0);
        TestUtil.assertNotNull("translation", ppobj.translation);
        TestUtil.assertVector3("gbs_center", FlightGear.refbtgcenter, ppobj.translation);
        TestUtil.assertEquals("geolists", 17, ppobj.geolist.size());
        // dass geo0 die mit 43 ist, ist wohl Zufall.
        TestUtil.assertEquals("vertices", 43, ppobj.geolist.get(0).getVertices().size());
        // normals sind jetzt genausoviele wie vertices. Wie auch immer, sinnig ist es.
        TestUtil.assertEquals("normals", 43, ppobj.geolist.get(0).getNormals().size());
        //12.12.17: das mit den indices ist unklar. Passt aber zu 120 Triangles (Wiki)
        TestUtil.assertEquals("indices", 120, ppobj.geolist.get(0).getIndices().length);
        //stichprobenartig ein paar Werte testen. Initial erstmal so uebernommen
        TestUtil.assertVector3("", new Vector3(4341.8657f, -6667.45f, -2179.5708f), ppobj.geolist.get(0).getVertices().getElement(0));
        TestUtil.assertVector3("", new Vector3(-646.4524f, 2071.1538f, 1039.9973f), ppobj.geolist.get(3).getVertices().getElement(4));
        //ppobj = ppfile.objects.get(1);
        //TestUtil.assertEquals("indices", 0, ppobj.geolist.get(0).getIndices().length);
        TestUtil.assertEquals("geolistmaterial", 17, ppobj.geolistmaterial.size());
        // die 17 materials sind aber alle null, wenn es keine Materiallib gibt. Ob das ideal ist, naja, so ist es jetzt aber.
        if (hadmatlib) {
            TestUtil.assertEquals("geolistmaterial0", "0", ppobj.geolistmaterial.get(0));
            TestUtil.assertEquals("materials", 17, ppfile.materials.size());
            for (int i = 0; i < 17; i++) {
                TestUtil.assertEquals("materials0.name", "" + i, ppfile.materials.get(i).name);
            }
            //die wraps muessen immer true sein.
            TestUtil.assertTrue("geolistmaterial0.wraps", ppfile.materials.get(0).wraps);
            TestUtil.assertTrue("geolistmaterial0.wrapt", ppfile.materials.get(5).wrapt);

        } else {
            TestUtil.assertEquals("materials", 0, ppfile.materials.size());
            // landclasses are material names
            TestUtil.assertEquals("materials0.name", "Grassland", ppobj.geolistmaterial.get(0));
            TestUtil.assertEquals("materials4.name", "OpenMining", ppobj.geolistmaterial.get(4));
            TestUtil.assertEquals("materials9.name", "Scrub", ppobj.geolistmaterial.get(9));
        }
    }

    public static void assert3072816(PortableModelList ppfile, boolean hadmatlib) {
        // preprocess liefert ein Object mit 17 geos.
        TestUtil.assertEquals("objects", 1, ppfile.getObjectCount());
        PortableModelDefinition ppobj = ppfile.getObject(0);
        TestUtil.assertEquals("geolists", 24, ppobj.geolist.size());

        if (hadmatlib) {
            TestUtil.assertEquals("materials", 24, ppfile.materials.size());
            for (int i = 0; i < 24; i++) {
                TestUtil.assertEquals("materials0.name", "" + i, ppfile.materials.get(i).name);
            }
            //die wraps muessen immer true sein.
            TestUtil.assertTrue("geolistmaterial0.wraps", ppfile.materials.get(0).wraps);
            TestUtil.assertTrue("geolistmaterial0.wrapt", ppfile.materials.get(5).wrapt);

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

    }

    public static void assertCDU777(PortableModelList ppfile, boolean fromgltf) {
        TestUtil.assertEquals("", 84, ppfile.getObjectCount());
        PortableModelDefinition ppknob = ppfile.getObject(0);
        SimpleGeometry geo = ppknob.geolist.get(0);
        TestUtil.assertEquals("name", "Bright_UprDisp.knob", ppknob.name);
        TestUtil.assertVector3("loc", new Vector3(0.016473f, 0.004128f, -0.046148f), ppknob.translation);
        //material wurde dupliziert
        TestUtil.assertEquals("materials", 14, ppfile.materials.size());
        PortableMaterial defaultwhite = ppfile.materials.get(0);
        TestUtil.assertEquals("material.name", /*(fromgltf)?"0":*/"shadedDefaultWhite", defaultwhite.name);
        TestUtil.assertColor("color", new Color(255, 255, 255, 255), defaultwhite.color);
        TestUtil.assertEquals("shininess", 0.64f, defaultwhite.shininess.value);
        TestUtil.assertTrue("shaded", defaultwhite.shaded);
        PortableMaterial unshadeddefaultwhite = ppfile.materials.get(1);
        TestUtil.assertEquals("material.name", /*(fromgltf)?"0":*/"unshadedDefaultWhite", unshadeddefaultwhite.name);
        TestUtil.assertColor("color", new Color(255, 255, 255, 255), unshadeddefaultwhite.color);
        TestUtil.assertEquals("shininess", 0.64f, unshadeddefaultwhite.shininess.value);
        TestUtil.assertFalse("shaded", unshadeddefaultwhite.shaded);

        PortableMaterial shadedinstr_lights = ppfile.materials.get(4);
        TestUtil.assertEquals("material.name", /*(fromgltf)?"4":*/"shadedinstr-lights", shadedinstr_lights.name);
        TestUtil.assertColor("color", new Color(0.8f, 0.8f, 0.8f, 1f), shadedinstr_lights.color);
        TestUtil.assertColor("color", new Color(0.5f, 0.5f, 0.5f, 1f), shadedinstr_lights.emis);
        if (!fromgltf) {
            //?? how
            TestUtil.assertColor("color", new Color(0.8f, 0.8f, 0.8f, 1f), shadedinstr_lights.ambient);
            TestUtil.assertColor("color", new Color(0f, 0f, 0f, 1f), shadedinstr_lights.specular);
        }
        TestUtil.assertEquals("shininess", 0f, shadedinstr_lights.shininess.value);
        TestUtil.assertTrue("shaded", shadedinstr_lights.shaded);
        //22.1.18: Das muss bei AC wohl so sein.
        TestUtil.assertTrue("wraps", shadedinstr_lights.wraps);
        TestUtil.assertTrue("wrapt", shadedinstr_lights.wrapt);

    }

    public static void assertOverhead777(PortableModelList ppfile, boolean fromgltf) {
        TestUtil.assertEquals("", 861, ppfile.getObjectCount());
        // das PANEL_B1 ist eine einfache Platte (Wuerfel), gut zum Testen
        PortableModelDefinition panel_b1 = ppfile.findObject("PANEL_B1");
        SimpleGeometry geo = panel_b1.geolist.get(0);
        TestUtil.assertEquals("name", "PANEL_B1", panel_b1.name);
        TestUtil.assertVector3("loc", new Vector3(0, 0, -0.002101f), panel_b1.translation);

        PortableModelDefinition LBUS_Switch = ppfile.findObject("LBUS_Switch");
        String LBUS_Switchmatname = LBUS_Switch.geolistmaterial.get(0);
        TestUtil.assertEquals("matname", "unshadedgreenglow43", LBUS_Switchmatname);
        PortableMaterial LBUS_Switchmat = ppfile.findMaterial(LBUS_Switchmatname);
        TestUtil.assertEquals("texture.name", "Buttons_03.png", LBUS_Switchmat.texture);

        PortableModelDefinition PANEL_A4 = ppfile.findObject("PANEL_A4");
        TestUtil.assertEquals("PANEL_A4.kids", 7, PANEL_A4.kids.size());
        TestUtil.assertVector3("loc", new Vector3(0, -0.251259f, -1.57064f), PANEL_A4.translation);

        PortableModelDefinition plate1_008 = PANEL_A4.kids.get(2);
        TestUtil.assertEquals("name", "PANEL_B1", panel_b1.name);
        TestUtil.assertVector3("loc", new Vector3(0.000148f, 0, -0.001075f), plate1_008.translation);


    }

    public static void assertTestData(PortableModelList ppfile, int expectedmaterials) {
        TestUtil.assertEquals("number of objects", 5, ppfile.getObjectCount());
        TestUtil.assertEquals("number of materials", expectedmaterials, ppfile.materials.size());
        TestUtil.assertEquals("materialname",  "ROOF_DEFAULT" , ppfile.materials.get(3).name);
    }



}
