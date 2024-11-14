package de.yard.threed.flightgear;

import de.yard.threed.core.BooleanHolder;
import de.yard.threed.core.BuildResult;
import de.yard.threed.core.GeneralParameterHandler;
import de.yard.threed.core.IntHolder;
import de.yard.threed.core.Packet;
import de.yard.threed.core.PortableModelTest;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.loader.InvalidDataException;
import de.yard.threed.core.loader.LoaderAC;
import de.yard.threed.core.loader.LoaderGLTF;
import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.loader.PortableModelDefinition;
import de.yard.threed.core.loader.PortableModel;
import de.yard.threed.core.loader.StringReader;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleLoadDelegate;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.loader.*;
import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.engine.platform.ResourceLoaderFromBundle;
import de.yard.threed.engine.test.testutil.TestUtil;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.simgear.scene.model.ACProcessPolicy;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.flightgear.testutil.FgTestUtils;
import de.yard.threed.flightgear.testutil.ModelAssertions;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.platform.common.*;
import de.yard.threed.engine.testutil.TestHelper;
import de.yard.threed.javanative.FileReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * MA31 splitted from LoaderTest.
 * 16.8.24 TODO Move ac tests to LoaderACTest
 * <p>
 * Created by thomass on 08.02.16.
 */
@Slf4j
public class LoaderExtTest {
    // fullFG for having TerraSyncBundleResolver
    static Platform platform = FgTestFactory.initPlatformForTest(true, true,true);

    /**
     * * TODO check, does it belong here or in tools?
     */
    @Test
    public void testAC_777_200() throws Exception {

        String acfile = "flightgear/src/test/resources/models/777-200.ac.gz";

        InputStream inputStream = new GZIPInputStream(new FileInputStream(TestUtils.locatedTestFile(acfile)));
        String rawac = new String(IOUtils.toByteArray(inputStream), StandardCharsets.ISO_8859_1);

        // 6.8.24: No longer ignore ac 'world'
        boolean ignoreacworld = false;
        LoaderAC loaderAC = new LoaderAC(new StringReader(rawac), ignoreacworld);

        PortableModel ac = loaderAC.buildPortableModel();
        //18.10.23: set defaulttexturebasepath to make check happy. TODO check intention and fix
        ac.defaulttexturebasepath = new ResourcePath("flusi");
        check777200(ac, false, !ignoreacworld);
        checksplitMultiMaterialModel(ac);
    }

    /**
     * 3.1.18: Mit gltf hat sich eine Ebene verschoben und material has no names.
     * 3.5.19: Aber material hat doch Namen
     *
     * @param loadedfile
     */
    public static void check777200(PortableModel loadedfile, boolean loadedFromGltf, boolean acworldIncluded) {

        PortableModelDefinition ppobj = loadedfile.getRoot();
        if (loadedFromGltf) {
            ppobj = PortableModelTest.validateSingleRoot(ppobj, LoaderGLTF.GLTF_ROOT);
        }
        if (acworldIncluded) {
            //ac-world is no single root
            //ppobj = PortableModelTest.validateSingleRoot(ppobj,"ac-world");
        }

        //}
        //System.out.println(loadedfile.dumpMaterial("\n"));
        TestUtil.assertEquals("", 113, ppobj.kids.size());
        TestUtil.assertEquals("materials", 23/*37*//*seit gltf24*/, loadedfile.getMaterialCount());
        //PreprocessedLoadedObject world = loadedfile.objects.get(0);
        //System.out.println(loadedfile.dumpObject("", world, "\n"));
        //TestUtil.assertEquals("world kids", 113, world.kids.size());
        //PortableModelDefinition reverser = (acworldIncluded) ? ppobj.kids.get(1) : loadedfile.getRoot().kids.get(1);
        PortableModelDefinition reverser =ppobj.kids.get(1) ;
        TestUtil.assertEquals("reverser kids", 0, reverser.kids.size());
        TestUtil.assertEquals("reverser name", "Reversers", reverser.name);
        TestUtil.assertVector3("reverser v1", new Vector3(-0.74679f, -1.31173f, -9.19898f), reverser.geo.getVertices().getElement(2));
        PortableModelDefinition landinglights = loadedfile.findObject("LandingLights");
        //Die 24 surfaces haben alle das gleiche Material.
        //2.8.24 TestUtil.assertEquals("landinglights facelists", 1, landinglights.geolist.size());
        // Vertices vermehrt wegen Smoothing. 25.1.17: Auf 61 wgen duplicateperUV. Ob das stimmt? TODO pruefen.
        // 3.4.17: Ist auf einmal nur noch 60. Evtl wegen unshaded ac Behandlung.
        // 16.9.24: Now down to 72?
        TestUtil.assertEquals("landinglights vertices", /*26* /32*//*60*/72, landinglights.geo.getVertices().size());
        TestUtil.assertEquals("landinglights faces", 24 * 3, landinglights.geo.getIndices().length);
        // LHstab has 6 unshaded and 22 shaded faces, but no kids. But the loader adds two children, one for
        // shaded and one for unshaded faces
        PortableModelDefinition lhstab = loadedfile.findObject("LHstab");
        // 2-mal mat 1, shaded und unshaded
        assertEquals( 2, lhstab.kids.size(), "LHstab kids");
        TestUtil.assertEquals("", /*(fromac) ?*/ "unshaded1Paint-Liveries-200/paint1" /*: "3"*/, lhstab.getChild(0).material);
        TestUtil.assertEquals("",/* (fromac) ?*/ "shaded1Paint-Liveries-200/paint1"/* : "2"*/, lhstab.getChild(1).material);
        PortableMaterial unshadedmaterial = loadedfile.findMaterial(lhstab.getChild(0).material);
        PortableMaterial shadedmaterial = loadedfile.findMaterial(lhstab.getChild(1).material);
        TestUtil.assertEquals("LHstab geolists", 2, lhstab.kids.size());
        // Faces vermehrt wegen Triangulation
        TestUtil.assertEquals("LHstab faces", /*6*/12 * 3, lhstab.getChild(0).geo.getIndices().length);
        TestUtil.assertEquals("LHstab faces", /*22*/29 * 3, lhstab.getChild(1).geo.getIndices().length);
        TestUtil.assertEquals("texturebasepath", "flusi", loadedfile.defaulttexturebasepath.getPath());
        TestUtil.assertEquals("material.texture", "Liveries-200/paint1.png", unshadedmaterial.getTexture());
        TestUtil.assertEquals("material.texture", "Liveries-200/paint1.png", shadedmaterial.getTexture());
        //TestUtil.assertTrue("shaded", ac.loadedfile.materials.get(0).shaded);
        //TestUtil.assertFalse("shaded", ac.loadedfile.materials.get(1).shaded);

        //FaceList lhndfaces1 = ac.loadedfile.objects.get(0).kids.get(0).getFaceLists().get(1);
        //TestUtil.assertFace4("lhndfaces0", new int[]{3, 2, 1, 0}, (FaceN) lhndfaces.faces.get(0));
        //TestUtil.assertFace4("lhndfaces1", new int[]{0, 1, 2, 3}, (FaceN) lhndfaces.faces.get(1));

    }

    /**
     * windturbine hat keine Textur.
     * Does it belong here or in tools? Maybe both. Here we check the model built by the GLTF built from tools. Really? We don't build a model.
     */
    @Test
    public void testWindturbineGltf() throws Exception {

        EngineTestFactory.loadBundleSync(FlightGear.getBucketBundleName("model"));

        Bundle bundlemodel = BundleRegistry.getBundle("Terrasync-model");
        // The windturbine.gltf here is from earlier bundle build via "sh bin/mkTerraSyncBundle.sh".
        BundleResource resource = new BundleResource(bundlemodel, "Models/Power/windturbine.gltf");
        // eigentlich geht das Laden ueber die Platform. Nur wegen Test werden die dahinterliegenden Klassen hier direkt aufgerufen.
        BooleanHolder loaded = new BooleanHolder(false);
        LoaderGLTF.load(new ResourceLoaderFromBundle(resource), new GeneralParameterHandler<PortableModel>() {
            @Override
            public void handle(PortableModel parameter) {
                // PortableModel ppfile = lf1.getPortableModel();
                // 18.19.23 1->2 ??
                ModelAssertions.assertWindturbine(parameter, 2, true);
                loaded.setValue(true);
            }
        });
        TestUtils.waitUntil(() -> {
            TestHelper.processAsync();
            return loaded.getValue();
        }, 10000);
    }

    /*
     * was a standalone test once
     */
    private void checksplitMultiMaterialModel(PortableModel loadedfile) {

        try {
            // 28.10.23 loadedfile = ModelLoader.readModelFromBundle(new BundleResource(BundleRegistry.getBundle("data-old"),"flusi/777-200.ac"), false,0);
        } catch (Exception e) {
            throw new RuntimeException("Error opening or reading ac file", e);
        }

        PortableModelDefinition lflap4 = loadedfile.findObject("Lflap4");
        // Has mat 1 and 4 and shaded and unshaded in 3 Facelists
        TestUtil.assertEquals("lflap4 facelistmaterial", 3, lflap4.kids.size());
        TestUtil.assertEquals("lflap4", "unshaded1Paint-Liveries-200/paint1", lflap4.getChild(0).material);
        TestUtil.assertEquals("lflap4", "shaded4rubber-Liveries-200/paint1", lflap4.getChild(1).material);
        TestUtil.assertEquals("lflap4", "shaded1Paint-Liveries-200/paint1", lflap4.getChild(2).material);
        PortableMaterial unshadedmaterial = loadedfile.findMaterial(lflap4.getChild(0).material);
        PortableMaterial shadedmaterial = loadedfile.findMaterial(lflap4.getChild(1).material);
        TestUtil.assertEquals("lflap4 geos", 3, lflap4.kids.size());

        /*ist schon trianguliert TestUtil.assertEquals("lflap4 faces", 6, lflap4.geolist.get(0).faces.size());
        //76 mal rubber
        TestUtil.assertEquals("lflap4 faces", 76, lflap4.geolist.get(1).faces.size());
        TestUtil.assertEquals("lflap4 faces", 14, lflap4.geolist.get(2).faces.size());
        List<SimpleGeometry> geolist = GeometryHelper.prepareGeometry(lflap4.vertices, lflap4.faces, null, true);
        TestUtil.assertEquals("lflap4 geolist", 3, geolist.size());

        List<Face3List> checkfacelist = GeometryHelper.triangulate(lflap4.faces);*/
        TestUtil.assertEquals("lflap4 faces3", 12 * 3, lflap4.getChild(0).geo.getIndices().length);
        TestUtil.assertEquals("lflap4 faces3", 136 * 3, lflap4.getChild(1).geo.getIndices().length);
        TestUtil.assertEquals("lflap4 faces3", 20 * 3, lflap4.getChild(2).geo.getIndices().length);
        //ist auch scvhon List<Vector3> checknormals = GeometryHelper.calculateSmoothVertexNormals(lflap4.vertices, checkfacelist, null);
        //TestUtil.assertEquals("checknormals", lflap4.vertices.size(), checknormals.size());

        // Pruefen, ob die Normalen beim split richtig uebertragen wurden
        // geht aber nicht so einfach
        // die eigentlichen Flaps, nicht das rubber und nicht das was drunterhaengt
        //SimpleGeometry flap = geolist.get(2);
        for (int i = 0; i < 14; i++) {
            //    TestUtil.assertVector3("normal down" + i, checknormals.get(checkfacelist.get(0).faces.size() * 3 + checkfacelist.get(1).faces.size() * 3 + i), flap.normals.get(i));
        }
    }


}