package de.yard.threed.flightgear;

import de.yard.threed.core.BooleanHolder;
import de.yard.threed.core.BuildResult;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.loader.InvalidDataException;
import de.yard.threed.core.loader.LoaderAC;
import de.yard.threed.core.loader.LoaderGLTF;
import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.loader.PortableModelDefinition;
import de.yard.threed.core.loader.PortableModelList;
import de.yard.threed.core.loader.StringReader;
import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleLoadDelegate;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.engine.platform.ResourceLoaderFromBundle;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.AsyncHelper;
import de.yard.threed.engine.test.testutil.TestUtil;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.testutil.TestHelper;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.osgdb.ReadResult;
import de.yard.threed.flightgear.core.simgear.scene.tgdb.DelayLoadReadFileCallback;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.flightgear.testutil.ModelAssertions;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Test for loading the model, not for testing gltf/ac reading/conversion, which is done in 'tools-fg'.
 * Also for testing FgModelHelper,...
 */
@Slf4j
public class ModelTest {
    // fullFG for having TerraSyncBundleResolver
    static Platform platform = FgTestFactory.initPlatformForTest(true, true);

    /**
     * Test isn't suited because 777 isn't bundled.
     */
    @Test
    @Disabled
    public void testAC_777_200() {
        String smodel = "/Users/thomas/Projekte/FlightGear/MyAircraft/My-777/Models";
        //SceneNode model = ModelFactory.buildModelFromBundle(new BundleResource(BundleRegistry.getBundle("My-777"),new ResourcePath("Models"),"777-200.ac")).getNode();
        Bundle bundle = BundleRegistry.getBundle("My-777");
        TestUtil.assertNotNull("bundle My-777", bundle);
        // 18.10.23: No more 'ac', so only gltf any more.
        Platform.getInstance().buildNativeModelPlain(new ResourceLoaderFromBundle(new BundleResource(bundle, new ResourcePath("Models"), "777-200.ac")), null, (BuildResult result) -> {
            TestUtil.assertNotNull("node", result.getNode());
        }, 0);
        //  TestUtil.assertEquals("world kids", 113, world.kids.size());
    }

    /**
     * Should find 'gltf' file even with 'ac' file specified.
     */
    @Test
    public void testWindturbineGltfByAc() throws Exception {

        AsyncHelper.cleanup();
        AbstractSceneRunner.getInstance().cleanup();

        EngineTestFactory.loadBundleSync(FlightGear.getBucketBundleName("model"));

        Bundle bundlemodel = BundleRegistry.getBundle("Terrasync-model");

        // This windturbine.gltf is from earlier bundle build. 'ac' is mapped to 'gltf'.
        BundleResource resource = new BundleResource(bundlemodel, "Models/Power/windturbine.ac");

        SceneNode node = FgModelHelper.mappedasyncModelLoad(new ResourceLoaderFromBundle(resource));

        assertEquals(0, node.findNodeByName("Blade3").size());
        // modelbuildvalues not available for checking

        TestHelper.processAsync();
        TestHelper.processAsync();
        TestHelper.processAsync();
        assertEquals(1, node.findNodeByName("Blade3").size());
    }

    /**
     * objectresource=Terrasync-3072824:(Objects/e000n50/e007n50)egkk_tower.xml
     */
    @Test
    public void testEgkk_towerGltf() throws Exception {

        AbstractSceneRunner.getInstance().cleanup();

        String bundleName = "Terrasync-3072824";
        if (BundleRegistry.getBundle(bundleName) != null) {
            BundleRegistry.unregister(bundleName);
        }

        BooleanHolder modelLaunched = new BooleanHolder();
        // list just as holder
        List<SceneNode> destinationNodes = new ArrayList<>();

        AbstractSceneRunner.getInstance().loadBundle(bundleName, new BundleLoadDelegate() {
            @Override
            public void bundleLoad(Bundle bundle) {
                // This gltf is from earlier bundle build.
                assertNotNull(BundleRegistry.getBundle(bundleName));
                // the bundle dir contains the file with suffix 'gltf', but 'ac' will be mapped.
                BundleResource resource = new BundleResource(BundleRegistry.getBundle(bundleName), "Objects/e000n50/e007n50/egkk_tower.ac");

                log.debug("Starting loader");

                // only test loading. Converting ac is tested in 'tools-fg'.
                SceneNode node = FgModelHelper.mappedasyncModelLoad(new ResourceLoaderFromBundle(resource));
                destinationNodes.add(node);
                modelLaunched.setValue(true);
            }
        });

        TestUtils.waitUntil(() -> {
            TestHelper.processAsync();
            TestHelper.processAsync();
            return modelLaunched.getValue();
        }, 10000);

        // modelbuildvalues not available for checking
        TestHelper.processAsync();
        TestHelper.processAsync();
        assertEquals(1, destinationNodes.size());
        //rootnodename is full name instead of just egkk_tower
        SceneNode egkkTowerNode = destinationNodes.get(0).findNodeByName("Objects/e000n50/e007n50/egkk_tower.gltf").get(0);
        assertNotNull(egkkTowerNode);

        TestUtil.assertEquals("number of kids", 6, egkkTowerNode.getTransform().getChildren().size());
        TestUtil.assertNotNull("kid(0) mesh", egkkTowerNode.getTransform().getChild(0).getSceneNode().getMesh());

    }

    /**
     * 19.10.23: Test needs to be fixed. Not sure if "delayed" is still needed/used.
     */
    @Test
    @Disabled
    public void testEgkk_towerGltfDelayed() {
        //Das egkk_tower.gltf liegt als Referenz schon in data und wurde nicht durch preprocess erstellt.
        //Bundle erst loeschen fuer klare Startbedinung
        String BUNDLENAMEDELAYED = "datadelayed";
        BundleRegistry.unregister(BUNDLENAMEDELAYED);
        EngineTestFactory.loadBundleSync("data-old"/*21.12.23 , BUNDLENAMEDELAYED, true*/);
        TestUtil.assertNull("", BundleRegistry.getBundle(BUNDLENAMEDELAYED).getResource("flusi/egkk_tower.gltf"));
        TestUtil.assertNull("", BundleRegistry.getBundle(BUNDLENAMEDELAYED).getResource("flusi/egkk_tower.bin"));
        TestUtil.assertTrue("", BundleRegistry.getBundle(BUNDLENAMEDELAYED).exists("flusi/egkk_tower.gltf"));
        TestUtil.assertTrue("", BundleRegistry.getBundle(BUNDLENAMEDELAYED).exists("flusi/egkk_tower.bin"));
        TestUtil.assertFalse("", BundleRegistry.getBundle(BUNDLENAMEDELAYED).contains("flusi/egkk_tower.gltf"));
        TestUtil.assertFalse("", BundleRegistry.getBundle(BUNDLENAMEDELAYED).contains("flusi/egkk_tower.bin"));

        final StringBuffer callbackdone = new StringBuffer("");
        BundleResource resource = new BundleResource(BundleRegistry.getBundle(BUNDLENAMEDELAYED), "flusi/egkk_tower.gltf");
        Platform.getInstance().buildNativeModelPlain(new ResourceLoaderFromBundle(resource), null, (BuildResult result) -> {
            TestUtil.assertNotNull("built node", result.getNode());
            //rootnodename ist jetzt full name statt nur egkk_tower
            TestUtil.assertEquals("rootnode.name", "flusi/egkk_tower.gltf", result.getNode().getName());
            TestUtil.assertEquals("number of kids", 6, result.getNode().getTransform().getChildren().size());
            TestUtil.assertNotNull("kid(0) mesh", result.getNode().getTransform().getChild(0).getSceneNode().getMesh());
            callbackdone.append("done");
        }, 0);
        TestHelper.processAsync();
        TestUtil.assertNotNull("", BundleRegistry.getBundle(BUNDLENAMEDELAYED).getResource("flusi/egkk_tower.gltf"));
        TestUtil.assertNull("", BundleRegistry.getBundle(BUNDLENAMEDELAYED).getResource("flusi/egkk_tower.bin"));
        TestUtil.assertEquals("", 1, AbstractSceneRunner.getInstance().futures.size());
        TestHelper.processAsync();
        TestHelper.processAsync();
        TestUtil.assertEquals("", 0, AbstractSceneRunner.getInstance().futures.size());
        //gltf/bin wurden nach dem Built wieder genullt.
        TestUtil.assertNull("", BundleRegistry.getBundle(BUNDLENAMEDELAYED).getResource("flusi/egkk_tower.gltf"));
        TestUtil.assertNull("", BundleRegistry.getBundle(BUNDLENAMEDELAYED).getResource("flusi/egkk_tower.bin"));
        TestUtil.assertFalse("", BundleRegistry.getBundle(BUNDLENAMEDELAYED).contains("flusi/egkk_tower.gltf"));
        TestUtil.assertFalse("", BundleRegistry.getBundle(BUNDLENAMEDELAYED).contains("flusi/egkk_tower.bin"));
        TestUtil.assertEquals("callbackdone", "done", callbackdone.toString());

    }

}