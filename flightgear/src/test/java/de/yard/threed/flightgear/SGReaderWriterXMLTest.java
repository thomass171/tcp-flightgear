package de.yard.threed.flightgear;

import de.yard.threed.core.BuildResult;
import de.yard.threed.core.Util;
import de.yard.threed.core.buffer.SimpleByteBuffer;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.resource.URL;
import de.yard.threed.core.testutil.TestBundle;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.ModelLoader;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.testutil.EngineTestUtils;
import de.yard.threed.engine.testutil.TestHelper;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.SGLoaderOptions;
import de.yard.threed.flightgear.core.flightgear.main.AircraftResourceProvider;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.scene.material.Effect;
import de.yard.threed.flightgear.core.simgear.scene.material.MakeEffect;
import de.yard.threed.flightgear.core.simgear.scene.model.ACProcessPolicy;
import de.yard.threed.flightgear.core.simgear.scene.model.SGAnimation;
import de.yard.threed.flightgear.core.simgear.scene.model.SGMaterialAnimation;
import de.yard.threed.flightgear.core.simgear.scene.model.SGReaderWriterXML;
import de.yard.threed.flightgear.core.simgear.scene.model.SGRotateAnimation;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static de.yard.threed.core.testutil.TestUtils.loadFileFromTestResources;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Also for SGAnimation.
 * 28.10.24: And effects in models
 */
@Slf4j
public class SGReaderWriterXMLTest {
    // 19.8.24: Now also has TerraSync bundle
    Platform platform = FgTestFactory.initPlatformForTest(true, true, false);

    @Test
    public void testXmlTestModel() {

        // Kruecke zur Entkopplung des Modelload von AC policy.
        ModelLoader.processPolicy = new ACProcessPolicy(null);

        List<SGAnimation> animationList = new ArrayList<SGAnimation>();

        Bundle bundlemodel = BundleRegistry.getBundle("test-resources");
        assertNotNull(bundlemodel);

        BuildResult result = SGReaderWriterXML.buildModelFromBundleXML(new BundleResource(bundlemodel, "xmltestmodel/test-main.xml"), null, (bpath, destinationNode, alist) -> {
            if (alist != null) {
                animationList.addAll(alist);
            }
        });
        SceneNode resultNode = new SceneNode(result.getNode());
        log.debug(resultNode.dump("  ", 0));
        // XML (and includes?) was loaded sync, gltf and animations will be loaded async
        assertEquals("xmltestmodel/test-main.xml->[" + "" +
                "submodel->xmltestmodel/test-submodel.xml," +
                "plainsubmodel->xmltestmodel/cube.ac" +
                "]", EngineTestUtils.getHierarchy(resultNode, 8));
        assertEquals(0, animationList.size(), "animations");

        TestHelper.processAsync();
        TestHelper.processAsync();
        validateXmlTestModel(resultNode, animationList);
    }

    private void validateXmlTestModel(SceneNode resultNode, List<SGAnimation> animationList) {

        log.debug(resultNode.dump("  ", 0));
        // Now has not only 'submodel' but also gltf.
        assertEquals("xmltestmodel/test-main.xml->" +
                        "[submodel->xmltestmodel/test-submodel.xml->ACProcessPolicy.root node->ACProcessPolicy.transform node->xmltestmodel/loc.gltf->gltfroot," +
                        "plainsubmodel->xmltestmodel/cube.ac->ACProcessPolicy.root node->ACProcessPolicy.transform node->xmltestmodel/cube.gltf->gltfroot," +
                        "ACProcessPolicy.root node->ACProcessPolicy.transform node->xmltestmodel/loc.gltf->gltfroot->center back translate->Spin Animation Group]",
                EngineTestUtils.getHierarchy(resultNode, 6));

        assertEquals(2, animationList.size(), "animations");
        assertNotNull(((SGMaterialAnimation) animationList.get(0)).group, "group");
        //??assertNotNull(((SGRotateAnimation) animationList.get(1)).rotategroup, "rotationgroup");

        SceneNode xmlNode = new SceneNode(SceneNode.findByName("xmltestmodel/test-main.xml").get(0));
        assertNotNull(xmlNode);

        // Not sure whether animation hierarchy (Needle below Face) is correct. So don't test for now.

    }

    @Test
    public void testAsi() {

        // Kruecke zur Entkopplung des Modelload von AC policy.
        ModelLoader.processPolicy = new ACProcessPolicy(null);

        List<SGAnimation> animationList = new ArrayList<SGAnimation>();

        EngineTestFactory.loadBundleAndWait("traffic-fg");
        Bundle bundlemodel = BundleRegistry.getBundle("traffic-fg");
        assertNotNull(bundlemodel);

        BuildResult result = SGReaderWriterXML.buildModelFromBundleXML(new BundleResource(bundlemodel, "railing/asi.xml"), null, (bpath, destinationNode, alist) -> {
            if (alist != null) {
                animationList.addAll(alist);
            }
        });
        SceneNode resultNode = new SceneNode(result.getNode());
        log.debug(resultNode.dump("  ", 0));
        // XML was loaded sync, gltf and animations will be loaded async
        assertEquals("railing/asi.xml", resultNode.getName());
        assertEquals(0, resultNode.getTransform().getChildCount());
        assertEquals(0, animationList.size(), "animations");

        TestHelper.processAsync();
        TestHelper.processAsync();
        validateAsi(resultNode, animationList);
    }

    private void validateAsi(SceneNode resultNode, List<SGAnimation> animationList) {

        log.debug(resultNode.dump("  ", 0));
        assertEquals("railing/asi.xml", resultNode.getName());
        assertEquals(1, resultNode.getTransform().getChildCount());

        assertEquals(2, animationList.size(), "animations");
        assertNotNull(((SGMaterialAnimation) animationList.get(0)).group, "group");
        assertNotNull(((SGRotateAnimation) animationList.get(1)).rotategroup, "rotationgroup");

        SceneNode xmlNode = new SceneNode(SceneNode.findByName("railing/asi.xml").get(0));
        assertNotNull(xmlNode);

        // Not sure whether animation hierarchy (Needle below Face) is correct. So don't test for now.

    }

    @Test
    public void test777200() throws Exception {

        // Kruecke zur Entkopplung des Modelload von AC policy.
        ModelLoader.processPolicy = new ACProcessPolicy(null);

//        TestBundle bundleTestResources = (TestBundle) BundleRegistry.getBundle("test-resources");

        // Needs a bundle with name "777" for resolving "Aircraft/777/Models/777-200.ac".
        TestBundle bundle777 = new TestBundle("777", new String[]{}, "");
        // different case of 'models'!
        bundle777.addAdditionalResource("Models/777-200.xml", new BundleData(new SimpleByteBuffer(loadFileFromTestResources("models/777-200.xml")), true));
        bundle777.addAdditionalResource("Models/777-200.gltf", new BundleData(new SimpleByteBuffer(loadFileFromTestResources("models/cube.gltf")), true));
        bundle777.addAdditionalResource("Models/777-200.bin", new BundleData(new SimpleByteBuffer(loadFileFromTestResources("models/cube.bin")), false));
        // Also mock an non XML submodel. Caused RTE once by trying recursive sync load instead of async.
        bundle777.addAdditionalResource("Models/Airport/Vehicle/hoskosh-ti-1500.gltf", new BundleData(new SimpleByteBuffer(loadFileFromTestResources("models/cube.gltf")), true));
        bundle777.addAdditionalResource("Models/Airport/Vehicle/hoskosh-ti-1500.bin", new BundleData(new SimpleByteBuffer(loadFileFromTestResources("models/cube.bin")), false));
        bundle777.complete();
        BundleRegistry.registerBundle(bundle777.name, bundle777);

        FgBundleHelper.addProvider(new AircraftResourceProvider("777"));

        BundleResource br = new BundleResource(bundle777, "Models/777-200.xml");

        List<SGAnimation> animationList = new ArrayList<SGAnimation>();
        SGReaderWriterXML.clearStatistics();
        BuildResult result = SGReaderWriterXML.buildModelFromBundleXML(br, null, (bpath, destinationNode, alist) -> {
            if (alist != null) {
                animationList.addAll(alist);
            }
        });
        SceneNode resultNode = new SceneNode(result.getNode());
        // XML was loaded sync, gltf and animations will be loaded async
        assertEquals("Models/777-200.xml", resultNode.getName());

        // wait for completion.
        TestHelper.processAsync();
        TestHelper.processAsync();

        log.debug(resultNode.dump("  ", 0));
        assertEquals("Models/777-200.xml->[" +
                        "Firetruck1->Models/Airport/Vehicle/hoskosh-ti-1500.ac->ACProcessPolicy.root node->ACProcessPolicy.transform node->Models/Airport/Vehicle/hoskosh-ti-1500.gltf->gltfroot," +
                        "Firetruck2->Models/Airport/Vehicle/hoskosh-ti-1500.ac->ACProcessPolicy.root node->ACProcessPolicy.transform node->Models/Airport/Vehicle/hoskosh-ti-1500.gltf->gltfroot," +
                        "ACProcessPolicy.root node->ACProcessPolicy.transform node->Models/777-200.gltf->gltfroot-><no name>]",
                EngineTestUtils.getHierarchy(resultNode, 6));

        List<String> modelsBuilt = AbstractSceneRunner.getInstance().systemTracker.getModelsBuilt();
        assertEquals(3, modelsBuilt.size());
        assertEquals("777:Models/777-200.gltf", modelsBuilt.get(2));
        assertEquals("777:Models/Airport/Vehicle/hoskosh-ti-1500.gltf", modelsBuilt.get(0));
        assertEquals("777:Models/Airport/Vehicle/hoskosh-ti-1500.gltf", modelsBuilt.get(1));

        assertEquals(9, SGReaderWriterXML.failedList.size());
    }

    @Test
    public void test77200ModelNotFound() throws Exception {

        // Kruecke zur Entkopplung des Modelload von AC policy.
        ModelLoader.processPolicy = new ACProcessPolicy(null);

        List<SGAnimation> animationList = new ArrayList<SGAnimation>();
        Bundle bundleTestResources = BundleRegistry.getBundle("test-resources");
        BundleResource br = new BundleResource(bundleTestResources, "models/777-200.xml");
        // Aircraft/777/Models/777-200.ac does not exist in bundle or cannot be resolved respectively

        BuildResult result = SGReaderWriterXML.buildModelFromBundleXML(br, null, (bpath, destinationNode, alist) -> {
            if (alist != null) {
                animationList.addAll(alist);
            }
        });
        // TODO wait for completion
        assertNull(result.getNode());
    }

    /**
     * async ein Model laden und pruefen, dass z.B. Animationen drin sind
     */
    @Test
    public void testWindturbineAnimationsWithAsync() throws Exception {
        //Test setup should cleanup TestHelper.cleanupAsync();

        // Kruecke zur Entkopplung des Modelload von AC policy.
        ModelLoader.processPolicy = new ACProcessPolicy(null);

        List<SGAnimation> animationList = new ArrayList<SGAnimation>();
        SGLoaderOptions opt = new SGLoaderOptions();
        opt.setPropertyNode(new SGPropertyNode("" + "-root")/*FGGlobals.getInstance().get_props()*/);

        EngineTestFactory.loadBundleSync(FlightGear.getBucketBundleName("model"));

        Bundle bundlemodel = BundleRegistry.getBundle("Terrasync-model");
        assertNotNull(bundlemodel);

        BuildResult result = SGReaderWriterXMLTest.loadModelAndWait(new BundleResource(bundlemodel, "Models/Power/windturbine.xml"), animationList,
                2, "Models/Power/windturbine.xml", null);
        SGReaderWriterXMLTest.validateWindturbineAnimations(new SceneNode(result.getNode()), animationList);

        animationList.clear();
        result = SGReaderWriterXML.buildModelFromBundleXML(new BundleResource(bundlemodel, "Models/Power/windturbine.xml"), null, (bpath, destinationNode, alist) -> {
            if (alist != null) {
                animationList.addAll(alist);//  xmlloaddelegate.modelComplete( animationList);
            }
        });
        assertEquals(0, animationList.size(), "animations");
        TestHelper.processAsync();
        TestHelper.processAsync();
        SGReaderWriterXMLTest.validateWindturbineAnimations(new SceneNode(result.getNode()), animationList);
    }

    /**
     * See also README.md
     */
    public static void validateWindturbineAnimations(SceneNode node, List<SGAnimation> animationList) {
        log.debug(node.dump("  ", 0));
        assertEquals(2, animationList.size(), "animations");
        assertNotNull(((SGRotateAnimation) animationList.get(0)).rotategroup, "rotationgroup");
        assertNotNull(((SGRotateAnimation) animationList.get(1)).rotategroup, "rotationgroup");

        SceneNode tower = node.findNodeByName("Tower").get(0);
        SceneNode acWorld = node.findNodeByName("ac-world").get(0);
        // skip intermediate node
        SceneNode firstSpinAnimationGroup = EngineTestUtils.getChild(acWorld, 1, 0);
        validateAnimationGroup(firstSpinAnimationGroup, "Spin Animation Group", new String[]{"Generator", "center back translate"});
        SceneNode secondSpinAnimationGroup = EngineTestUtils.getChild(firstSpinAnimationGroup, 0, 1, 0);
        validateAnimationGroup(secondSpinAnimationGroup, "Spin Animation Group", new String[]{"Shaft", "Hub", "Blade1", "Blade2", "Blade3"});

        assertEquals("Models/Power/windturbine.xml->ACProcessPolicy.root node->ACProcessPolicy.transform node->Models/Power/windturbine.gltf->gltfroot->ac-world->[Tower,center back translate]", EngineTestUtils.getHierarchy(node, 6));

    }

    /**
     *
     */
    @Test
    public void testBeacon() throws Exception {

        // Kruecke zur Entkopplung des Modelload von AC policy.
        ModelLoader.processPolicy = new ACProcessPolicy(null);

        List<SGAnimation> animationList = new ArrayList<SGAnimation>();
        SGLoaderOptions opt = new SGLoaderOptions();
        opt.setPropertyNode(new SGPropertyNode("" + "-root")/*FGGlobals.getInstance().get_props()*/);

        EngineTestFactory.loadBundleSync(FlightGear.getBucketBundleName("model"));

        Bundle bundlemodel = BundleRegistry.getBundle("Terrasync-model");
        assertNotNull(bundlemodel);

        // has 23 animations, but only 6 are parsed(?)
        BuildResult result = SGReaderWriterXMLTest.loadModelAndWait(new BundleResource(bundlemodel, "Models/Airport/beacon.xml"), animationList,
                6, "Models/Airport/beacon.xml", "Models/Airport/beacon.gltf");
        //SGReaderWriterXMLTest.validateBeaconAnimations(new SceneNode(result.getNode()), animationList);

        // the effect might have exist before the test, but anyway, it should exist now.
        Effect modelTransparent = MakeEffect.effectMap.get("Effects/model-transparent");
        assertNotNull(modelTransparent);
    }

    /**
     * Waits until animationlist is complete. This also means, the model (XML part) is loaded.
     * Optionally also wait until the object is in the scene graph.
     */
    public static BuildResult loadModelAndWait(BundleResource bundleResource, List<SGAnimation> animationList, int expectedAnimations,
                                               String expectedSource, String expectedObject) throws Exception {
        BuildResult result = SGReaderWriterXML.buildModelFromBundleXML(bundleResource, null, (source, destinationNode, alist) -> {
            assertEquals(expectedSource, source.getFullName());
            if (alist != null) {
                animationList.addAll(alist);
            }
        });
        TestUtils.waitUntil(() -> {
            TestHelper.processAsync();
            log.debug("animationList.size()=" + animationList.size() + ",expected=" + expectedAnimations);
            return animationList.size() == expectedAnimations;
        }, 5000);

        if (expectedObject != null) {
            TestUtils.waitUntil(() -> {
                TestHelper.processAsync();
                log.debug("animationList.size()=" + animationList.size() + ",expected=" + expectedAnimations);
                return SceneNode.findByName(expectedObject).size() > 0;
            }, 5000);
        }

        return result;
    }

    private static void validateAnimationGroup(SceneNode animationNode, String expectedName, String[] expectedGrandChildren) {
        assertEquals(expectedName, animationNode.getName());
        // skip intermediate node
        assertEquals(1, animationNode.getTransform().getChildCount());
        animationNode = animationNode.getTransform().getChild(0).getSceneNode();
        assertEquals(expectedGrandChildren.length, animationNode.getTransform().getChildCount());
        for (int i = 0; i < expectedGrandChildren.length; i++) {
            assertEquals(expectedGrandChildren[i], animationNode.getTransform().getChild(i).getSceneNode().getName());
        }
    }
}