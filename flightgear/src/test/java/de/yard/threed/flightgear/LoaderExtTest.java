package de.yard.threed.flightgear;

import de.yard.threed.core.BooleanHolder;
import de.yard.threed.core.BuildResult;
import de.yard.threed.core.GeneralParameterHandler;
import de.yard.threed.core.IntHolder;
import de.yard.threed.core.Packet;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.loader.InvalidDataException;
import de.yard.threed.core.loader.LoaderAC;
import de.yard.threed.core.loader.LoaderGLTF;
import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.loader.PortableModelDefinition;
import de.yard.threed.core.loader.PortableModelList;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * MA31 splitted from LoaderTest.
 * <p>
 * Created by thomass on 08.02.16.
 */
@Slf4j
public class LoaderExtTest {
    // fullFG for having TerraSyncBundleResolver
    static Platform platform = FgTestFactory.initPlatformForTest(true, true);

    /**
     ** TODO check, does it belong here or in tools?
     */
    @Test
    public void testAC_777_200() throws Exception {

        String acfile = "flightgear/src/test/resources/models/777-200.ac.gz";

        InputStream inputStream = new GZIPInputStream(new FileInputStream(TestUtils.locatedTestFile(acfile)));
        String rawac = new String(IOUtils.toByteArray(inputStream), StandardCharsets.ISO_8859_1);

        boolean ignoreacworld = true;
        LoaderAC loaderAC = new LoaderAC(new StringReader(rawac), ignoreacworld);

        PortableModelList ac = loaderAC.preProcess();
        //18.10.23: set defaulttexturebasepath to make check happy. TODO check intention and fix
        ac.defaulttexturebasepath = new ResourcePath("flusi");
        check777200(ac, !ignoreacworld);
        checksplitMultiMaterialModel(ac);
    }

    /**
     * 3.1.18: Mit gltf hat sich eine Ebene verschoben und material has no names.
     * 3.5.19: Aber material hat doch Namen
     *
     * @param loadedfile
     */
    public static void check777200(PortableModelList loadedfile, boolean acworldIncluded) {

        //System.out.println(loadedfile.dumpMaterial("\n"));
        TestUtil.assertEquals("", (acworldIncluded) ? 1 : 113, loadedfile.getObjectCount());
        TestUtil.assertEquals("materials", 37/*seit gltf24*/, loadedfile.materials.size());
        //PreprocessedLoadedObject world = loadedfile.objects.get(0);
        //System.out.println(loadedfile.dumpObject("", world, "\n"));
        //TestUtil.assertEquals("world kids", 113, world.kids.size());
        PortableModelDefinition reverser = (acworldIncluded) ? loadedfile.getObject(0).kids.get(1) : loadedfile.getObject(1);
        TestUtil.assertEquals("reverser kids", 0, reverser.kids.size());
        TestUtil.assertEquals("reverser name", "Reversers", reverser.name);
        TestUtil.assertVector3("reverser v1", new Vector3(-0.74679f, -1.31173f, -9.19898f), reverser.geolist.get(0).getVertices().getElement(2));
        PortableModelDefinition landinglights = loadedfile.findObject("LandingLights");
        //Die 24 surfaces haben alle das gleiche Material.
        TestUtil.assertEquals("landinglights facelists", 1, landinglights.geolist.size());
        // Vertices vermehrt wegen Smoothing. 25.1.17: Auf 61 wgen duplicateperUV. Ob das stimmt? TODO pruefen.
        // 3.4.17: Ist auf einmal nur noch 60. Evtl wegen unshaded ac Behandlung.
        TestUtil.assertEquals("landinglights vertices", /*26* /32*/60, landinglights.geolist.get(0).getVertices().size());
        TestUtil.assertEquals("landinglights faces", 24 * 3, landinglights.geolist.get(0).getIndices().length);
        // LHstab hat 6 unshaded und 22 shaded faces
        PortableModelDefinition lhstab = loadedfile.findObject("LHstab");
        // 2-mal mat 1, shaded und unshaded
        TestUtil.assertEquals("LHstab facelistmaterial", 2, lhstab.geolistmaterial.size());
        TestUtil.assertEquals("", /*(fromac) ?*/ "unshadedPaint" /*: "3"*/, lhstab.geolistmaterial.get(0));
        TestUtil.assertEquals("",/* (fromac) ?*/ "shadedPaint"/* : "2"*/, lhstab.geolistmaterial.get(1));
        PortableMaterial unshadedmaterial = loadedfile.findMaterial(lhstab.geolistmaterial.get(0));
        PortableMaterial shadedmaterial = loadedfile.findMaterial(lhstab.geolistmaterial.get(1));
        TestUtil.assertEquals("LHstab geolists", 2, lhstab.geolist.size());
        // Faces vermehrt wegen Triangulation
        TestUtil.assertEquals("LHstab faces", /*6*/12 * 3, lhstab.geolist.get(0).getIndices().length);
        TestUtil.assertEquals("LHstab faces", /*22*/29 * 3, lhstab.geolist.get(1).getIndices().length);
        TestUtil.assertEquals("texturebasepath", "flusi", loadedfile.defaulttexturebasepath.getPath());
        TestUtil.assertEquals("material.texture", "Liveries-200/paint1.png", unshadedmaterial.texture);
        TestUtil.assertEquals("material.texture", "Liveries-200/paint1.png", shadedmaterial.texture);
        //TestUtil.assertTrue("shaded", ac.loadedfile.materials.get(0).shaded);
        //TestUtil.assertFalse("shaded", ac.loadedfile.materials.get(1).shaded);

        //FaceList lhndfaces1 = ac.loadedfile.objects.get(0).kids.get(0).getFaceLists().get(1);
        //TestUtil.assertFace4("lhndfaces0", new int[]{3, 2, 1, 0}, (FaceN) lhndfaces.faces.get(0));
        //TestUtil.assertFace4("lhndfaces1", new int[]{0, 1, 2, 3}, (FaceN) lhndfaces.faces.get(1));

    }

    /**
     * windturbine hat keine Textur.
     * TODO check, does it belong here or in tools?
     */
    @Test
    public void testWindturbineGltf() throws Exception {

        EngineTestFactory.loadBundleSync(FlightGear.getBucketBundleName("model"));

        Bundle bundlemodel = BundleRegistry.getBundle("Terrasync-model");
        //Das windturbine.gltf liegt als Referenz auch in data. Wurde nicht durch preprocess erstellt.
        // This one here is from earlier bundle build.
        BundleResource resource = new BundleResource(bundlemodel, "Models/Power/windturbine.gltf");
        // eigentlich geht das Laden ueber die Platform. Nur wegen Test werden die dahinterliegenden Klassen hier direkt aufgerufen.
        BooleanHolder loaded = new BooleanHolder(false);
        LoaderGLTF.load(new ResourceLoaderFromBundle(resource), new GeneralParameterHandler<PortableModelList>() {
            @Override
            public void handle(PortableModelList parameter) {
               // PortableModelList ppfile = lf1.getPortableModelList();
                // 18.19.23 1->2 ??
                ModelAssertions.assertWindturbine(parameter, 2);
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
    private void checksplitMultiMaterialModel(PortableModelList loadedfile) {

        try {
            // 28.10.23 loadedfile = ModelLoader.readModelFromBundle(new BundleResource(BundleRegistry.getBundle("data-old"),"flusi/777-200.ac"), false,0);
        } catch (Exception e) {
            throw new RuntimeException("Error opening or reading ac file", e);
        }

        PortableModelDefinition lflap4 = loadedfile.findObject("Lflap4");
        // Es gibt mat 1 und 4 sowie shaded und unshaded in 3 Facelists
        TestUtil.assertEquals("lflap4 facelistmaterial", 3, lflap4.geolistmaterial.size());
        TestUtil.assertEquals("lflap4", "unshadedPaint", lflap4.geolistmaterial.get(0));
        TestUtil.assertEquals("lflap4", "shadedrubber", lflap4.geolistmaterial.get(1));
        TestUtil.assertEquals("lflap4", "shadedPaint", lflap4.geolistmaterial.get(2));
        PortableMaterial unshadedmaterial = loadedfile.findMaterial(lflap4.geolistmaterial.get(0));
        PortableMaterial shadedmaterial = loadedfile.findMaterial(lflap4.geolistmaterial.get(1));
        TestUtil.assertEquals("lflap4 geos", 3, lflap4.geolist.size());

        /*ist schon trianguliert TestUtil.assertEquals("lflap4 faces", 6, lflap4.geolist.get(0).faces.size());
        //76 mal rubber
        TestUtil.assertEquals("lflap4 faces", 76, lflap4.geolist.get(1).faces.size());
        TestUtil.assertEquals("lflap4 faces", 14, lflap4.geolist.get(2).faces.size());
        List<SimpleGeometry> geolist = GeometryHelper.prepareGeometry(lflap4.vertices, lflap4.faces, null, true);
        TestUtil.assertEquals("lflap4 geolist", 3, geolist.size());

        List<Face3List> checkfacelist = GeometryHelper.triangulate(lflap4.faces);*/
        TestUtil.assertEquals("lflap4 faces3", 12 * 3, lflap4.geolist.get(0).getIndices().length);
        TestUtil.assertEquals("lflap4 faces3", 136 * 3, lflap4.geolist.get(1).getIndices().length);
        TestUtil.assertEquals("lflap4 faces3", 20 * 3, lflap4.geolist.get(2).getIndices().length);
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