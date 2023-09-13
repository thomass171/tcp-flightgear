package de.yard.threed.toolsfg;

import de.yard.threed.core.StringUtils;
import de.yard.threed.core.loader.LoadedObject;
import de.yard.threed.core.loader.LoaderGLTF;
import de.yard.threed.core.loader.PortableModelList;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.test.testutil.TestUtil;

import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.testutil.FgFullTestFactory;
import de.yard.threed.flightgear.testutil.ModelAssertions;
import de.yard.threed.javanative.FileReader;
import de.yard.threed.tools.GltfMemoryBundle;
import de.yard.threed.tools.GltfProcessor;
import de.yard.threed.toolsfg.testutil.BtgModelAssertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;


/**
 *
 */
public class LoaderBTGBuilderTest {
    static Platform platform = FgFullTestFactory.initPlatformForTest(new HashMap<String, String>());


    /**
     */
    @Test
    public void testRefbtgGltf() throws Exception {

        String[] argv = new String[]{
                "-gltf", "-o", "src/test/resources/tmp", "src/test/resources/terrain/3056410.btg",
                "-l", "de.yard.threed.toolsfg.LoaderBTGBuilder"
        };
        Log log = Platform.getInstance().getLog(this.getClass());
        log.debug("Running in "+System.getProperty("user.dir"));
        GltfProcessor.runMain(argv);
        try {
            String gltfFilename = StringUtils.substringAfterLast(FlightGear.refbtggltf,"/");

            BundleResource gltfbr = new BundleResource(GltfMemoryBundle.buildFromFilesystem("src/test/resources/tmp/"+gltfFilename,gltfFilename.replace(".gltf","")), gltfFilename);

            // eigentlich geht das Laden ueber die Platform. Nur wegen Test werden die dahinterliegenden Klassen hier direkt aufgerufen.
            LoaderGLTF lf1 = LoaderGLTF.buildLoader(gltfbr, null);
            PortableModelList ppfile = lf1.ppfile;
            ModelAssertions.assertRefbtg(ppfile, false);
        } catch (Exception e) {
            throw new RuntimeException("Error opening or reading gltf file", e);
        }
    }


}
