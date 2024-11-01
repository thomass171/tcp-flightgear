package de.yard.threed.toolsfg;

import de.yard.threed.core.StringUtils;
import de.yard.threed.core.loader.LoadedObject;
import de.yard.threed.core.loader.LoaderGLTF;
import de.yard.threed.core.loader.PortableModel;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.test.testutil.TestUtil;

import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.flightgear.testutil.ModelAssertions;
import de.yard.threed.javanative.FileReader;
import de.yard.threed.tools.GltfProcessor;
import de.yard.threed.tools.testutil.ToolsTestUtil;
import de.yard.threed.toolsfg.testutil.BtgModelAssertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 *
 */
public class LoaderBTGBuilderTest {
    static Platform platform = FgTestFactory.initPlatformForTest(new HashMap<String, String>());


    /**
     */
    @Test
    public void testRefbtgGltf() throws Exception {

        String[] argv = new String[]{
                "-gltf", "-o", "src/test/resources/tmp", "src/test/resources/terrain/3056410.btg",
                "-l", "de.yard.threed.toolsfg.LoaderBTGBuilder"
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
            // LoaderBTGBuilder should provide matlib
            ModelAssertions.assertRefbtg(ppfile, true, true);
        } catch (Exception e) {
            throw new RuntimeException("Error opening or reading gltf file", e);
        }
    }


}
