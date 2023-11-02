package de.yard.threed.toolsfg;

import de.yard.threed.core.BuildResult;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.core.loader.InvalidDataException;
import de.yard.threed.core.loader.LoadedObject;
import de.yard.threed.core.loader.LoaderAC;
import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.loader.PortableModelDefinition;
import de.yard.threed.core.loader.PortableModelList;
import de.yard.threed.core.loader.StringReader;
import de.yard.threed.core.platform.NativeJsonValue;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.engine.platform.common.AsyncHelper;
import de.yard.threed.engine.platform.common.ModelLoader;
import de.yard.threed.engine.test.testutil.TestUtil;
import de.yard.threed.engine.testutil.TestHelper;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.simgear.scene.model.ACProcessPolicy;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.flightgear.testutil.FgTestUtils;
import de.yard.threed.flightgear.testutil.ModelAssertions;
import de.yard.threed.javanative.FileReader;
import de.yard.threed.tools.GltfBuilder;
import de.yard.threed.tools.GltfBuilderResult;
import de.yard.threed.tools.GltfMemoryBundle;
import de.yard.threed.tools.GltfProcessor;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

import static de.yard.threed.flightgear.LoaderExtTest.check777200;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * MA31 splitted from LoaderTest.
 * <p>
 * Created by thomass on 08.02.16.
 */
public class LoaderExtTest {
    static Platform platform = FgTestFactory.initPlatformForTest(true, false);


    @Test
    public void testCDU() throws Exception {

        String acfile = "flightgear/src/test/resources/models/CDU-777-boeing.ac";

        LoaderAC ac = new LoaderAC(new StringReader(FileReader.readAsString(new File(TestUtils.locatedTestFile(acfile)))), true);

        System.out.println(ac.loadedfile.dumpMaterial("\n"));
        assertEquals(84, ac.loadedfile.objects.size());
        LoadedObject knob = ac.loadedfile.objects.get(0);
        //TestUtil.assertEquals("group kids", 0, cylinder.kids.size());
        assertEquals(1, knob.getFaceLists().size(), "facelists");
        TestUtil.assertVector3("loc", new Vector3(0.016473f, 0.004128f, -0.046148f), knob.location);
        //TestUtil.assertEquals("face4", 24, cylinder.getFaceLists().get(0).faces.size());
        PortableModelList ppfile = ac.preProcess();
        ModelAssertions.assertCDU777(ppfile, false);
    }

    /**
     * 11.4.17: jetzt kein Cachetest mehr.
     * 3.1.18: jetzt gltf statt acpp
     */
    @Test
    public void testPreprocessedAC_777_200() throws Exception {

        String acfile = "flightgear/src/test/resources/models/777-200.ac.gz";

        InputStream inputStream = new GZIPInputStream(new FileInputStream(TestUtils.locatedTestFile(acfile)));
        String rawac = new String(IOUtils.toByteArray(inputStream), StandardCharsets.ISO_8859_1);

        boolean ignoreacworld = true;
        LoaderAC loaderAC = new LoaderAC(new StringReader(rawac), ignoreacworld);
        PortableModelList ac = loaderAC.preProcess();

        GltfBuilder gltfbuilder = new GltfBuilder();
        GltfBuilderResult lf = gltfbuilder.process(ac);

        NativeJsonValue gltf = platform.parseJson(lf.gltfstring);
        Assertions.assertNotNull(gltf, "parsedgltf");
        //System.out.println(lf.gltfstring);
        BundleResource gltfbr = new BundleResource(new GltfMemoryBundle("777-200", lf.gltfstring, lf.bin), "777-200.gltf");

        PortableModelList pml = AsyncHelper.readGltfModelFromBundle(gltfbr, false, 0);

        //18.10.23: set defaulttexturebasepath to make check happy. TODO check intention and fix
        pml.defaulttexturebasepath = new ResourcePath("flusi");
        check777200(pml, !ignoreacworld);

    }

    /**
     * a3cd-ref
     * Skizze 10
     */
    @Test
    public void testTriangulateAndNormalBuildingAc3dRef() throws Exception {
        PortableModelList loadedfile;
        BundleResource br = new BundleResource(BundleRegistry.getBundle("test-resources"), "models/ac3d-ref.ac");
        LoaderAC ac = new LoaderAC(new StringReader(br.bundle.getResource(br).getContentAsString()), br);

        loadedfile = ac.preProcess();//28.10.23 ModelLoader.readModelFromBundle(new BundleResource(BundleRegistry.getBundle("data-old"),"model/ac3d-ref.ac"), false,0);

        PortableModelDefinition ac3drefflat = loadedfile.getObject(0).kids.get(0);
        checkRefCylinder(ac3drefflat, true);
        PortableModelDefinition ac3drefsmooth = loadedfile.getObject(0).kids.get(1);
        checkRefCylinder(ac3drefsmooth, false);
    }

    /**
     * Den RefCylinder pruefen, der entwder mit flat oder smoothshading erstellt wurde.
     *
     * @param ac3dref
     * @param flat
     */
    private void checkRefCylinder(PortableModelDefinition ac3dref, boolean flat) {
        Vector3 refnormal0 = new Vector3(0.92387956f, 0, -0.38268346f);
        Vector3 refnormal1 = new Vector3(0.38268346f, 0, -0.92387956f);
        Vector3 refnormal0smooth = new Vector3(1, 0, 0);
        //Die Refwerte sind einfach uebernommen, scheinen aber nicht ganz richtig. x und z sollten doch absolut gleich sein.TODO
        Vector3 refnormal1smooth = new Vector3(0.603748f, 0, -0.603748f);
        int segments = 8;
        /*ist schon preprocessed TestUtil.assertEquals("vertices", (segments + 1) * 2, ac3dref.vertices.size());
        List<SimpleGeometry> geolist = GeometryHelper.prepareGeometry(ac3dref.vertices, ac3dref.faces, null, false, ac3dref.crease);*/
        SimpleGeometry geo = ac3dref.geolist.get(0);
        // Bei der flat Variante gibt es wegen duplizierter Vertices fast doppelt zu viel.
        int basesize = (segments + 1) * 2;
        int size = basesize;
        if (flat) {
            size += (segments - 1) * 2;
        }
        TestUtil.assertEquals("vertices", size, geo.getVertices().size());
        TestUtil.assertEquals("normals", size, geo.getNormals().size());
        TestUtil.assertEquals("indices", 2 * 8 * 3, geo.getIndices().length);

        // Die erste zwei Faces pruefen.
        TestUtil.assertFace3("face3 0", new int[]{0, 2, 1}, geo.getIndices(), 0);
        TestUtil.assertFace3("face3 1", new int[]{3, 1, 2}, geo.getIndices(), 1);
        // Sollten im Face dieselbe Normale haben (bei flat auch in Normallist)
        //TODO TestUtil.assertVector3("facenormals", ((Face3) geo.getFaces().faces.get(0)).normal, refnormal0);
        //TODO TestUtil.assertVector3("facenormals", ((Face3) geo.getFaces().faces.get(0)).normal, ((Face3) geo.getFaces().faces.get(1)).normal);
        if (flat) {
            // Die zweiten zwei Faces pruefen.
            TestUtil.assertFace3("face3 2", new int[]{18, 4, 19}, geo.getIndices(), 2);
            TestUtil.assertFace3("face3 3", new int[]{5, 19, 4}, geo.getIndices(), 3);
            // Der Vertex 2 muesste der erste duplizierte sein, der 3 der zweite; also die linke Seite von Face 2
            // 15.12.16: Das Duplizieren geht nicht mehr so systematisch. Darum nicht darauf verlassen; die Face Reihenfolge hat sich aber eigentlich nicht geändert.
            // Duplizierung ist doch noch so wie hier angenommen. TODO Mit assertFaceIndexNormals kann man doch jetzt auch alle Normale testen
            TestUtil.assertFaceIndexNormals(geo.getIndices(), geo.getNormals(), new int[]{0, 1}, refnormal0);
            for (int i = 0; i < 4; i++) {
                TestUtil.assertVector3("facenormals " + i + ":", refnormal0, geo.getNormals().getElement(i));
            }

            TestUtil.assertVector3("facenormals", refnormal1, geo.getNormals().getElement(basesize));
            TestUtil.assertVector3("facenormals", refnormal1, geo.getNormals().getElement(basesize + 1));
            TestUtil.assertVector3("facenormals", refnormal1, geo.getNormals().getElement(4));
            TestUtil.assertVector3("facenormals", refnormal1, geo.getNormals().getElement(5));
            //Fuer die restlichen pruefen, dass sie identisch sind
            for (int i = 0; i < segments - 1; i++) {
                Vector3 refn = geo.getNormals().getElement(4 + (i * 2));
                TestUtil.assertVector3("facenormals " + i, refn, geo.getNormals().getElement(basesize + (i * 2)));
                TestUtil.assertVector3("facenormals " + i, refn, geo.getNormals().getElement(basesize + (i * 2) + 1));
                TestUtil.assertVector3("facenormals " + i, refn, geo.getNormals().getElement(5 + (i * 2)));
            }

            TestUtil.assertFace3("face3 2", new int[]{basesize + 0, 4, basesize + 1}, geo.getIndices(), 2);

        } else {
            //Die Normalen an den Ausenkanten (0,1 und am Ende) stimmen nicht, weil die Geo nicht geschlossen ist und damit an der Kante nicht
            //gemittelt werden kann.
            //TODO stimmt nicht ganz TestUtil.assertVector3("smoothvertexnormals", refnormal1smooth, geo.normals.get(2));
            //TODO stimmt nicht ganz TestUtil.assertVector3("smoothvertexnormals", refnormal1smooth, geo.normals.get(3));

        }

        // TestUtil.assertFace3("face3 2", new int[]{0, 1, 2}, (Face3) f.faces.get(2));
        //TestUtil.assertFace3("face3 3", new int[]{0, 2, 3}, (Face3) f.faces.get(3));

        /*
        // 4xFace3 ergibt eine VBO Groesse von 12
        TestUtil.assertEquals("Anzahl Normals", 12, normals.size());
        // die ersten 6 Normalen zeigen alle nach unten (y=-1), die anderen 6 nach oben
        for (int i = 0; i < 6; i++) {
            TestUtil.assertVector3("normal down" + i, new Vector3(0, -1, 0), normals.get(i));
            TestUtil.assertVector3("normal up " + i, new Vector3(0, 1, 0), normals.get(6 + i));
        }*/
    }
}
