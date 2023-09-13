package de.yard.threed.toolsfg;

import de.yard.threed.core.loader.LoadedObject;
import de.yard.threed.core.loader.LoaderGLTF;
import de.yard.threed.core.loader.PortableModelList;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.test.testutil.TestUtil;
import de.yard.threed.flightgear.LoaderBTG;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.testutil.FgFullTestFactory;
import de.yard.threed.flightgear.testutil.ModelAssertions;
import de.yard.threed.toolsfg.testutil.BtgModelAssertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;


/**
 * MA31 splitted from LoaderTest.
 * From LoaderExtTest.
 * Created by thomass on 08.02.16.
 */
public class LoaderBTGTest {
    static Platform platform = FgFullTestFactory.initPlatformForTest(new HashMap<String, String>());

    /**
     * also see SGTileGeometryBinTest and Wiki
     */
    @Test
    public void testBTG() {
        // try {
        LoaderBTG btg = BtgModelAssertions.loadRefBtg(null);
        TestUtil.assertEquals("", 1, btg.loadedfile.objects.size());
        LoadedObject obj = btg.loadedfile.objects.get(0);
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
        TestUtil.assertVector3("gbs_center", FlightGear.refbtgcenter, btg.loadedfile.gbs_center);
        //das im Wiki stimmt nicht TestUtil.assertVector3("getFirst vertex",new Vector3(4776809.5f, 1466116.4f, 3950700.5f),obj.vertices.get(0));
        //List<Face> facelist = shutlayer.getFaceLists().get(0);
        //assertEquals("faces", 19408, facelist.size());
        PortableModelList ppfile = btg.preProcess();
        // die 17 materials sind aber alle null, weil es keine Materiallib gibt. Ob das ideal ist, naja, so ist es jetzt aber.
        ModelAssertions.assertRefbtg(ppfile, false);
    }
}
