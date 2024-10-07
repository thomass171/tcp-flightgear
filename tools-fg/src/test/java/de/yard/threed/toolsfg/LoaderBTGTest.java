package de.yard.threed.toolsfg;

import de.yard.threed.core.buffer.ByteArrayInputStream;
import de.yard.threed.core.buffer.SimpleByteBuffer;
import de.yard.threed.core.loader.InvalidDataException;
import de.yard.threed.core.loader.LoadedObject;
import de.yard.threed.core.loader.LoaderGLTF;
import de.yard.threed.core.loader.PortableModel;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.test.testutil.TestUtil;
import de.yard.threed.flightgear.LoadedBtgFile;
import de.yard.threed.flightgear.LoaderBTG;
import de.yard.threed.flightgear.LoaderOptions;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.FlightGearModuleScenery;
import de.yard.threed.flightgear.core.simgear.scene.tgdb.SGReaderWriterBTG;
import de.yard.threed.flightgear.core.simgear.scene.tgdb.SGTileGeometryBin;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.flightgear.testutil.FgTestUtils;
import de.yard.threed.flightgear.testutil.ModelAssertions;
import de.yard.threed.javacommon.FileReader;
import de.yard.threed.tools.GltfBuilderResult;
import de.yard.threed.tools.GltfProcessor;
import de.yard.threed.toolsfg.testutil.BtgModelAssertions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * MA31 splitted from LoaderTest.
 * From LoaderExtTest.
 * Created by thomass on 08.02.16.
 */
@Slf4j
public class LoaderBTGTest {
    // 'fullFG' needed for sgmaterial via bundle
    static Platform platform = FgTestFactory.initPlatformForTest(true, true, true);

    /**
     * also see SGTileGeometryBinTest and Wiki
     */
    @Test
    public void testRefBTG() {
        // try {
        LoaderBTG btg = BtgModelAssertions.loadRefBtg(null);
        LoadedObject obj = btg.loadedfile.object;
        TestUtil.assertEquals("vertices", 17740, obj.vertices.size());
        TestUtil.assertEquals("normals", 17353, obj.normals.size());
        TestUtil.assertEquals("texcoords", 19008, btg.texcoords.size());
        TestUtil.assertEquals("tris_v", 35156, btg.tris_v.size());
        TestUtil.assertEquals("tris_n", 35156, btg.tris_n.size());
        TestUtil.assertEquals("tris_tcs", 35156, btg.tris_tcs.size());
        TestUtil.assertEquals("tris_tcs[0]", 4, btg.tris_tcs.get(0).size());
        TestUtil.assertEquals("tri_materials", 35156, btg.tri_materials.size());
        TestUtil.assertEquals("material[3]", "Airport", btg.tri_materials.get(3));

        // Faces werden bei BTG nicht gefuellt
        TestUtil.assertEquals("face lists", 0, obj.getFaceLists().size());
        TestUtil.assertEquals("material list", 0, obj.facelistmaterial.size());
        TestUtil.assertVector3("gbs_center", FlightGear.refbtgcenter, ((LoadedBtgFile)btg.loadedfile).gbs_center);
        //das im Wiki stimmt nicht TestUtil.assertVector3("getFirst vertex",new Vector3(4776809.5f, 1466116.4f, 3950700.5f),obj.vertices.get(0));
        //List<Face> facelist = shutlayer.getFaceLists().get(0);
        //assertEquals("faces", 19408, facelist.size());
        PortableModel portableModel = btg.buildPortableModel();
        // die 17 materials sind aber alle null, weil es keine Materiallib gibt. Ob das ideal ist, naja, so ist es jetzt aber.
        ModelAssertions.assertRefbtg(portableModel, false, false);
    }

    /**
     * 5.10.23: 4 materials are missing. We keep these missings for testing error handling.
     * 25.2.24: But the errors cause holes making groundnet unusable due to missing elevation. So
     * better avoid these errors in EDDK. TODO use non airport btg for error testing
     */
    @Test
    public void testEddkBtg() throws Exception {

        String btgfile = "fg-raw-data/terrasync/Terrain/e000n50/e007n50/EDDK.btg.gz";
        InputStream ins = new GZIPInputStream(new FileInputStream(TestUtils.locatedTestFile(btgfile)));
        byte[] buf = FileReader.readFully(ins);
        ByteArrayInputStream b = new ByteArrayInputStream(new SimpleByteBuffer(buf));

        LoaderBTG loaderBTG = new LoaderBTG(b, null, new LoaderOptions(FlightGearModuleScenery.getInstance().get_matlib()), "EDDK.btg");
        // 4070 just taken as reference
        assertEquals(4070, loaderBTG.tris_v.size());
        assertEquals(4070, loaderBTG.tris_n.size());
        assertEquals(4070, loaderBTG.tri_materials.size());

        PortableModel ppfile = loaderBTG.buildPortableModel();
        SGTileGeometryBin tileGeometryBin = loaderBTG.tileGeometryBin;
        // 34 just taken as reference
        assertEquals(34, tileGeometryBin.materialTriangleMap.size());
        // 4 materials are no longer missing: pc_taxiway, pa_stopway, pa_centerline, pc_stopway
        assertEquals(34, ppfile.materials.size());
        assertEquals(0, tileGeometryBin.materialNotFound.size());

    }

    /**
     * Test of external file (not in project). Loading these shows the effect of using a subset of sgmaterial.
     * Some material is not found, thus some land classes can't be resolved and holes remain in the terrain.
     */
    @Test
    public void testExternalGreenwichBTGs() throws Exception {

        String btgfile = "/Users/thomas/tmp/2941787.btg.gz";
        //String btgfile = "/Users/thomas/tmp/2958168.btg.gz";
        File file = new File(btgfile);
        if (file.exists()) {
            InputStream ins = new GZIPInputStream(new FileInputStream(file));
            byte[] buf = FileReader.readFully(ins);
            ByteArrayInputStream b = new ByteArrayInputStream(new SimpleByteBuffer(buf));

            LoaderBTG loaderBTG = new LoaderBTG(b, null, new LoaderOptions(FlightGearModuleScenery.getInstance().get_matlib()), btgfile);
            //assertEquals(4070, loaderBTG.tri_materials.size());

            PortableModel ppfile = loaderBTG.buildPortableModel();
            SGTileGeometryBin tileGeometryBin = loaderBTG.tileGeometryBin;
            // 16 just taken as reference
            assertEquals(16, tileGeometryBin.materialTriangleMap.size());
            // some materials are  missing:
            //assertEquals(34, ppfile.materials.size());
            assertEquals(4, tileGeometryBin.materialNotFound.size());
        } else {
            log.debug("External BTG file not found. Test skipped");
        }
    }
}
