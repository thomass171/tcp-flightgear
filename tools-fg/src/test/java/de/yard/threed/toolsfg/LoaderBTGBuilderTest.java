package de.yard.threed.toolsfg;

import de.yard.threed.core.StringUtils;
import de.yard.threed.core.loader.*;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.testutil.TestUtils;

import de.yard.threed.flightgear.FgBundleHelper;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.flightgear.testutil.ModelAssertions;
import de.yard.threed.javacommon.FileReader;
import de.yard.threed.tools.GltfProcessor;
import de.yard.threed.tools.testutil.ToolsTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.*;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * We also have LoaderBTGTest
 */
public class LoaderBTGBuilderTest {

    @BeforeEach
    void setup() {
        // Only minimal resolver for realistic testing
        FgTestFactory.initPlatformForTest(true, false, false);
        // clear all resolver to be sure matlib creates one for "fgdatabasic"
        FgBundleHelper.clear();
    }

    /**
     */
    @ParameterizedTest
    @CsvSource(value = {
            "3056410.btg;true",
            "3056410.btg;false",
            "3072816.btg;true",
            "3072816.btg;false",
    }, delimiter = ';')
    public void testRefbtgGltfViaCmdLine(String btgfile, boolean strict) throws Exception {

        String[] argv = new String[]{
                "-gltf", "-o", "src/test/resources/tmp", "src/test/resources/terrain/"+btgfile,
                "-l", "de.yard.threed.toolsfg.LoaderBTGBuilder"+((strict)?"Strict":"")
        };
        Log log = Platform.getInstance().getLog(this.getClass());
        String workingDir=System.getProperty("user.dir");
        log.debug("Running in "+workingDir);
        assertTrue(workingDir.endsWith("tools-fg"), "wrong workingdir:"+workingDir);
        new GltfProcessor().runMain(argv);
        try {
            String gltfFilename = StringUtils.substringAfterLast(FlightGear.refbtggltf,"/");

            BundleResource gltfbr = new BundleResource(ToolsTestUtil.buildFromFilesystem("src/test/resources/tmp/"+gltfFilename,gltfFilename.replace(".gltf","")), gltfFilename);

            // eigentlich geht das Laden ueber die Platform. Nur wegen Test werden die dahinterliegenden Klassen hier direkt aufgerufen.
            LoaderGLTF lf1 = LoaderGLTF.buildLoader(gltfbr, null);
            PortableModel ppfile = lf1.doload();
            // LoaderBTGBuilder should provide matlib. TODO assert depending on paramter btgfile
            ModelAssertions.assertRefbtg(ppfile, true, true);
        } catch (Exception e) {
            throw new RuntimeException("Error opening or reading gltf file", e);
        }
    }

    /**
     * We also have LoaderBTGTest for eg. for EDDK
     */
    @ParameterizedTest
    @CsvSource(value = {
            "fg-raw-data/terrasync/Terrain/e000n50/e007n50/EDDK.btg.gz;true",
    }, delimiter = ';')
    public void testbtgGltf(String btgfile, boolean strict) throws Exception {

        InputStream ins = new GZIPInputStream(new FileInputStream(TestUtils.locatedTestFile(btgfile)));
        byte[] buf = FileReader.readFully(ins);
        LoaderBTGBuilder loaderBTGBuilder=new LoaderBTGBuilder();
        AbstractLoader loader = loaderBTGBuilder.buildAbstractLoader(buf, btgfile);
        PortableModel portableModel = loader.buildPortableModel();
    }


}
