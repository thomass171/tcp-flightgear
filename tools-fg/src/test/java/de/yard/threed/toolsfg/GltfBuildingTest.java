package de.yard.threed.toolsfg;

import de.yard.threed.core.loader.InvalidDataException;
import de.yard.threed.core.loader.LoaderGLTF;
import de.yard.threed.core.platform.NativeJsonValue;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.testutil.Assert;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.FlightGearModuleScenery;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.flightgear.testutil.FgTestUtils;
import de.yard.threed.flightgear.testutil.ModelAssertions;
import de.yard.threed.tools.GltfBuilder;
import de.yard.threed.tools.GltfBuilderResult;
import de.yard.threed.tools.GltfMemoryBundle;
import de.yard.threed.tools.GltfProcessor;
import de.yard.threed.tools.ToolsPlatform;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;


/**
 * Reading/building gltf is already tested in tcp-22 in general. These tests are specific to some FG model.
 * <p>
 * Created by thomass on 06.12.17.
 */
public class GltfBuildingTest {
    // 'fullFG' needed for sgmaterial via bundle.
    static Platform platform = FgTestFactory.initPlatformForTest(true,true);

    /**
     *
     */
    @Test
    public void testCreateWindturbineGltf() throws IOException {

        String acfile = "fg-raw-data/terrasync/Models/Power/windturbine.ac";
        GltfBuilderResult lf = new GltfProcessor().convertToGltf(TestUtils.locatedTestFile(acfile), Optional.empty());
        NativeJsonValue gltf = platform.parseJson(lf.gltfstring);
        Assertions.assertNotNull(gltf, "parsedgltf");
        BundleResource gltfbr = new BundleResource(new GltfMemoryBundle("windturbine", lf.gltfstring, lf.bin), "windturbine.gltf");
        try {
            LoaderGLTF lf1 = LoaderGLTF.buildLoader(gltfbr, null);
            ModelAssertions.assertWindturbine(lf1.ploadedfile, 2);
        } catch (InvalidDataException e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     *
     */
    @Test
    public void testCreateEgkkTowerGltf() throws IOException {

        String acfile = "fg-raw-data/terrasync/Objects/e000n50/e007n50/egkk_tower.ac";
        GltfBuilderResult lf = new GltfProcessor().convertToGltf(TestUtils.locatedTestFile(acfile), Optional.empty());
        BundleResource gltfbr = new BundleResource(new GltfMemoryBundle("egkk_tower", lf.gltfstring, lf.bin), "egkk_tower.gltf");
        try {
            // Den texturebasepath einfach mal so setzen
            LoaderGLTF lf1 = LoaderGLTF.buildLoader(gltfbr, new ResourcePath("flusi"));
            //8 vertices und 30 indices?
            ModelAssertions.assertEgkkTower(lf1.ploadedfile, 8, 30, true, false);
        } catch (InvalidDataException e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     *
     */
    @Test
    public void testCreateCDU777() throws IOException {

        String acfile = "flightgear/src/test/resources/models/CDU-777-boeing.ac";
        GltfBuilderResult lf = new GltfProcessor().convertToGltf(TestUtils.locatedTestFile(acfile), Optional.empty());

        BundleResource gltfbr = new BundleResource(new GltfMemoryBundle("boeing", lf.gltfstring, lf.bin), "boeing.gltf");
        try {
            // Den texturebasepath einfach mal so setzen
            LoaderGLTF lf1 = LoaderGLTF.buildLoader(gltfbr, new ResourcePath("flusi"));
            ModelAssertions.assertCDU777(lf1.ploadedfile, true);
        } catch (InvalidDataException e) {
            Assert.fail(e.getMessage());
        }
    }

    //11.9.21 needs BTG-Loader plugin in GltfProcessor
    @Test
    public void testCreateGltfForRefBtgWithmat() {
        // Prepare matlib?

        try {
            String btgfile = "tools-fg/src/test/resources/" + FlightGear.refbtg;
            GltfBuilderResult lf = new GltfProcessor().convertToGltf(TestUtils.locatedTestFile(btgfile), Optional.of("de.yard.threed.toolsfg.LoaderBTGBuilder"));
            BundleResource gltfbr = new BundleResource(new GltfMemoryBundle("3056410", lf.gltfstring, lf.bin), "3056410.gltf");
            try {
                // Den texturebasepath einfach mal so setzen
                LoaderGLTF lf1 = LoaderGLTF.buildLoader(gltfbr, new ResourcePath("flusi/terrain/3056410-gltf"));
                // Das GLTF wurde mit matlib erstellt
                ModelAssertions.assertRefbtg(lf1.ploadedfile, true);
            } catch (InvalidDataException e) {
                Assert.fail(e.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error opening or reading btg file", e);
        }
    }

    /**
     * The preferred BTG way without matlib.
     * 11.9.21 needs BTG-Loader plugin in GltfProcessor
     */
    @Test
    public void testCreateGltfForRefBtgWithoutMat() throws IOException {

        String btgfile = "tools-fg/src/test/resources/" + FlightGear.refbtg;
        // remove matlib
        SGMaterialLibWrapper.getInstance().disableSGMaterialLib();
        GltfBuilderResult lf = new GltfProcessor().convertToGltf(TestUtils.locatedTestFile(btgfile), Optional.of("de.yard.threed.toolsfg.LoaderBTGBuilder"));
        BundleResource gltfbr = new BundleResource(new GltfMemoryBundle("3056410", lf.gltfstring, lf.bin), "3056410.gltf");
        try {
            // Den texturebasepath einfach mal so setzen
            LoaderGLTF lf1 = LoaderGLTF.buildLoader(gltfbr, new ResourcePath("flusi/terrain/3056410-gltf"));
            // Das GLTF wurde ohne matlib erstellt
            ModelAssertions.assertRefbtg(lf1.ploadedfile, false);
        } catch (InvalidDataException e) {
            Assert.fail(e.getMessage());
        }
        // enable recreate of matlib
        SGMaterialLibWrapper.dropInstance();
    }

    /**
     * followme.ac has many nested kids.
     */
    @Test
    public void testCreateFollowmeGltf() throws IOException {

        String acfile = "flightgear/src/test/resources/models/followme.ac";
        GltfBuilderResult lf = new GltfProcessor().convertToGltf(TestUtils.locatedTestFile(acfile), Optional.empty());

        NativeJsonValue gltf = platform.parseJson(lf.gltfstring);
        Assertions.assertNotNull(gltf, "parsedgltf");
        //System.out.println(lf.gltfstring);
        BundleResource gltfbr = new BundleResource(new GltfMemoryBundle("followme", lf.gltfstring, lf.bin), "followme.gltf");
        try {
            LoaderGLTF lf1 = LoaderGLTF.buildLoader(gltfbr, null);
            ModelAssertions.assertFollowMe(lf1.ploadedfile, 38);
        } catch (InvalidDataException e) {
            Assert.fail(e.getMessage());
        }
    }
}
