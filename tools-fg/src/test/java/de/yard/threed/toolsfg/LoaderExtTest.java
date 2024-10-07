package de.yard.threed.toolsfg;

import de.yard.threed.core.BooleanHolder;
import de.yard.threed.core.BuildResult;
import de.yard.threed.core.GeneralParameterHandler;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.core.loader.InvalidDataException;
import de.yard.threed.core.loader.LoadedObject;
import de.yard.threed.core.loader.LoaderAC;
import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.loader.PortableModelDefinition;
import de.yard.threed.core.loader.PortableModel;
import de.yard.threed.core.loader.StringReader;
import de.yard.threed.core.platform.NativeJsonValue;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.testutil.InMemoryBundle;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.engine.platform.ResourceLoaderFromBundle;
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
    static Platform platform = FgTestFactory.initPlatformForTest(true, false, false);


    @Test
    public void testCDU() throws Exception {

        String acfile = "flightgear/src/test/resources/models/CDU-777-boeing.ac";

        LoaderAC ac = new LoaderAC(new StringReader(FileReader.readAsString(new File(TestUtils.locatedTestFile(acfile)))), false);

        System.out.println(ac.loadedfile.dumpMaterial("\n"));
        assertEquals(84, ac.loadedfile.object.kids.size());
        LoadedObject knob = ac.loadedfile.object.kids.get(0);
        //TestUtil.assertEquals("group kids", 0, cylinder.kids.size());
        assertEquals(1, knob.getFaceLists().size(), "facelists");
        TestUtil.assertVector3("loc", new Vector3(0.016473f, 0.004128f, -0.046148f), knob.location);
        //TestUtil.assertEquals("face4", 24, cylinder.getFaceLists().get(0).faces.size());
        PortableModel ppfile = ac.buildPortableModel();
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

        // 6.8.24: No longer ignore ac 'world'
        boolean ignoreacworld = false;
        LoaderAC loaderAC = new LoaderAC(new StringReader(rawac), ignoreacworld);
        PortableModel ac = loaderAC.buildPortableModel();
        if (ac.getName() == null) {
            ac.setName("777-200");
        }
        GltfBuilder gltfbuilder = new GltfBuilder();
        GltfBuilderResult lf = gltfbuilder.process(ac);

        NativeJsonValue gltf = platform.parseJson(lf.gltfstring);
        Assertions.assertNotNull(gltf, "parsedgltf");
        //System.out.println(lf.gltfstring);
        BundleResource gltfbr = new BundleResource(new InMemoryBundle("777-200", lf.gltfstring, lf.bin), "777-200.gltf");

        BooleanHolder loaded = new BooleanHolder(false);

        ModelLoader.readGltfModel(new ResourceLoaderFromBundle(gltfbr), new GeneralParameterHandler<PortableModel>() {
            @Override
            public void handle(PortableModel pml) {
                //18.10.23: set defaulttexturebasepath to make check happy. TODO check intention and fix
                pml.defaulttexturebasepath = new ResourcePath("flusi");
                check777200(pml, true, !ignoreacworld);
                loaded.setValue(true);
            }
        });

        TestUtils.waitUntil(() -> {
            TestHelper.processAsync();
            return loaded.getValue();
        }, 10000);

    }


}