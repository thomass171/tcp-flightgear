package de.yard.threed.trafficadvanced;

import de.yard.threed.core.BuildResult;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.platform.common.ModelLoader;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.testutil.EngineTestUtils;
import de.yard.threed.engine.testutil.TestHelper;
import de.yard.threed.flightgear.FgBundleHelper;
import de.yard.threed.flightgear.LoaderOptions;
import de.yard.threed.flightgear.core.SGLoaderOptions;
import de.yard.threed.flightgear.core.flightgear.main.AircraftResourceProvider;
import de.yard.threed.flightgear.core.flightgear.main.FGGlobals;
import de.yard.threed.flightgear.core.osg.NodeCallback;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.props.Props;
import de.yard.threed.flightgear.core.simgear.scene.material.Effect;
import de.yard.threed.flightgear.core.simgear.scene.material.MakeEffect;
import de.yard.threed.flightgear.core.simgear.scene.material.Technique;
import de.yard.threed.flightgear.core.simgear.scene.model.*;
import de.yard.threed.flightgear.ecs.FgAnimationUpdateSystem;
import de.yard.threed.flightgear.testutil.EffectCollector;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.flightgear.testutil.FgTestUtils;
import de.yard.threed.javacommon.SimpleHeadlessPlatform;
import de.yard.threed.trafficadvanced.testutil.AdvancedBundleResolverSetup;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test external bundles. External dependency!
 * TerraSync/Terrain tests are in SceneryTest
 * <p>
 * Created by thomass on 29.09.2025
 */
@Slf4j
public class BundlepoolTest {

    String bundlePool = "https://ubuntu-server.udehlavj1efjeuqv.myfritz.net/publicweb/bundlepool";
    static final String bundleName172p2024 = "c172p.2024";
    // only saves appx 2100ms static Bundle cachedBundleName172p2024 = null;

    @BeforeAll
    static void setupOnce() {
        //cachedBundleName172p2024 = null;
    }

    @BeforeEach
    void setup() {
        // No terrain/sgmaterial needed in this test class
        FgTestFactory.initPlatformForTest(new HashMap<>(), false, false, false, false, new AdvancedBundleResolverSetup());
    }

    /**
     * ...tree is:
     * ...
     * - Models/c172-common.xml
     * -- Damage
     * ...
     * -- tyre-smoke-n
     * -- tyre-smoke-p
     * -- tyre-smoke-s
     */
    @Test
    public void testC172p2024Full() throws Exception {
        // Kruecke zur Entkopplung des Modelload von AC policy.
        ModelLoader.processPolicy = new ACProcessPolicy(null);

        List<SGAnimation> animationList = new ArrayList<SGAnimation>();

        Bundle bundle = loadBundle(bundleName172p2024);

        assertEquals(0, MakeEffect.effectMap.size());

        AircraftResourceProvider arp = new AircraftResourceProvider();
        arp.setAircraftDirAndBundle("c172p", bundleName172p2024);
        FgBundleHelper.addProvider(arp);

        BundleResource br = new BundleResource(bundle, "Models/c172-common.xml");
        // Meanwhile we only use a single property tree (also FgVehicleLoader)
        SGLoaderOptions opt = new SGLoaderOptions();
        SGPropertyNode destinationProp = FGGlobals.getInstance().get_props();
        opt.setPropertyNode(destinationProp);
        opt.effectBuilderListener = new EffectCollector();
        BuildResult result = SGReaderWriterXML.buildModelFromBundleXML(br, opt, (bpath, destinationNode, alist) -> {
            if (alist != null) {
                animationList.addAll(alist);
            }
        });

        TestHelper.processAsync();
        TestHelper.processAsync();
        assertEquals(0, MakeEffect.errorList.size());
        //changing assertEquals(1686, animationList.size());
        SceneNode modelRoot = new SceneNode(result.getNode());
        assertNotNull(modelRoot);
        log.debug("dump: {}", modelRoot.dump("  ", 0));
        List<SceneNode> foundNodes = modelRoot.findNodeByName("Models/c172-common.xml");
        assertEquals(1, foundNodes.size());
        SceneNode commonNode = foundNodes.get(0);
        // log says 66, but 'c172sp' is conditional false
        assertEquals(55/*65*/, commonNode.getTransform().getChildCount());
        //    assertEquals("Damage", commonNode.getTransform().getChild(0).getSceneNode().getName());
      /*  assertEquals("Skis", commonNode.getTransform().getChild(5).getSceneNode().getName());
        SceneNode skis = commonNode.getTransform().getChild(5).getSceneNode();
        //  Only child should be "Models/Effects/skis/skis.xml"
        assertEquals(1, skis.getTransform().getChildCount());
        // "Models/Effects/damage/nose_gear.xml" has no name
        assertEquals("", commonNode.getTransform().getChild(6).getSceneNode().getName());
        assertEquals("MooringHarness", commonNode.getTransform().getChild(18).getSceneNode().getName());*/
        // depending on 'glass'
/*positions no longer fit        assertEquals(/*"c172sp"* /"submodel condition switch", commonNode.getTransform().getChild(20).getSceneNode().getName());
        assertEquals(/*"c172sp?"* /"submodel condition switch", commonNode.getTransform().getChild(21).getSceneNode().getName());
        assertEquals("ControlLock", commonNode.getTransform().getChild(22).getSceneNode().getName());
        assertEquals("PilotPedals", commonNode.getTransform().getChild(23).getSceneNode().getName());
        assertEquals("CoPilotPedals", commonNode.getTransform().getChild(24).getSceneNode().getName());*/
        //assertEquals("OAT-object", commonNode.getTransform().getChild(43).getSceneNode().getName());

        foundNodes = modelRoot.findNodeByName("elevatorright");
        assertEquals(1, foundNodes.size());

        // don't care too much about effectMap assertEquals(117, MakeEffect.effectMap.size());

        validatePropertyTree(destinationProp);
        validateWingRight(modelRoot, animationList);
        validateAsi(modelRoot);
        validatePropeller(modelRoot);
        validateWindowframeleftint(modelRoot, animationList);
        validateMagCompass(modelRoot, animationList);
    }

    private void validatePropertyTree(SGPropertyNode root) {
        /*these are only tmp aliases SGPropertyNode node = root.getNode("/params/wing_right_damaged/property");
        assertNotNull(node);
        assertEquals(Props.INT, node.getType());
        node = root.getNode("/params/crash/property");
        assertNotNull(node);
        assertEquals(Props.BOOL, node.getType());*/
    }

    private void validateWingRight(SceneNode modelRoot, List<SGAnimation> animationList) {
        // "wing_right"
        // Effect Effects/exterior/dirt-wing
        // material animation 'Specularity fix'
        // select animation 'Right wing No damage'
        // material animation texture wing.png
        List<SceneNode> foundNodes = modelRoot.findNodeByName("wing_right");
        assertEquals(1, foundNodes.size());
        SceneNode wingRight = foundNodes.get(0);

        List<Effect> wingRightEffects = Model.appliedEffects.get("wing_right");
        // effect in map apperently overrwritten by damage effect with same name
        Effect effectFromCache = MakeEffect.effectMap.get("Aircraft/c172p/Models/Effects/exterior/dirt-wing");

        String hierarchy = EngineTestUtils.getHierarchy(wingRight, 6, false);
        log.debug("wing_right up-hierarchy={}", hierarchy);
        assertEquals("Blender_exporter_v2.26__c172-common.ac->wing->wing_1->MaterialAnimationGroup-LRLRbeeefffAffffhllrrrvwwwwwwwwSrrrrovafA->SelectAnimation-wwrR->MaterialAnimationGroup-wwwwwwfrlwwcwrwwlwwwwlwwrw->wing_right", hierarchy);
        List<SGAnimation> allAnimations = FgTestUtils.findAnimationsByObjectName(animationList, "wing_right");
        assertEquals(3, allAnimations.size());
        SGSelectAnimation selectAnimation = (SGSelectAnimation) allAnimations.get(1);
        // should be selected typically, otherwise "wing_right" won't be visible
        assertTrue(selectAnimation.isSelected());
        SGPropertyNode genProp = FGGlobals.getInstance().get_props().getNode("/sim/multiplay/generic/int[19]");
        // Check for type int and value, which indicates a proper node setup
        assertEquals(3, genProp.getType());
        assertEquals(0, genProp.getIntValue());

        // update shouldn't change it, as long as we have some default c172p fitting property tree
        selectAnimation.process(null, null);
        assertTrue(selectAnimation.isSelected());

        SimpleHeadlessPlatform.DummyMaterial caseMaterial = (SimpleHeadlessPlatform.DummyMaterial) wingRight.getMesh().getMaterial().material;
        //assertNotNull(material.textures);
        //??assertEquals("??", caseMaterial.uniformValue.get("u_texture"));
        assertEquals("true", caseMaterial.uniformValue.get("u_textured"));
        assertEquals("true", caseMaterial.uniformValue.get("u_shaded"));
    }

    private void validateAsi(SceneNode modelRoot) {
        // pure "asi" exists twice
        List<SceneNode> foundNodes = modelRoot.findNodeByName("Models/Interior/Panel/Instruments/asi/asi.gltf");
        assertEquals(1, foundNodes.size());
        SceneNode asi = foundNodes.get(0);

        String hierarchy = EngineTestUtils.getHierarchy(asi, 6, false);
        log.debug("asi up-hierarchy={}", hierarchy);

    }

    /**
     * Also Tests SelectAnimation.
     */
    private void validatePropeller(SceneNode modelRoot) {
        List<SceneNode> propellers = modelRoot.findNodeByName("Propeller");
        assertEquals(1, propellers.size());
        SceneNode propeller = propellers.get(0);
        String hierarchy = EngineTestUtils.getHierarchy(propeller, 6, false);
        log.debug("propeller up-hierarchy={}", hierarchy);

        propellers = modelRoot.findNodeByName("Propeller.Fast");
        assertEquals(1, propellers.size());
        SceneNode propellerFast = propellers.get(0);
        hierarchy = EngineTestUtils.getHierarchy(propellerFast, 6, false);
        log.debug("propeller.Fast up-hierarchy={}", hierarchy);

    }

    /**
     * view through left window shouldn't be blocked
     */
    private void validateWindowframeleftint(SceneNode modelRoot, List<SGAnimation> animationList) {
        SceneNode windowframeleftint = EngineTestUtils.findSingleNodeByName(modelRoot, "windowframeleftint");
        assertNotNull(windowframeleftint);

        List<SGAnimation> windowframeleftintAnimations = FgTestUtils.findAnimationsByObjectName(animationList, "windowframeleftint");
        // Material animation comes from .../Interior/Panel/c172p-panel/c172p.xml, a large but likely currently effectless one.
        // RotateAnimations are for opening left/right door?
        assertEquals(4, windowframeleftintAnimations.size());
        SGSelectAnimation mySelectAnimation = (SGSelectAnimation) windowframeleftintAnimations.get(3);
        // Be sure it has latest state
        mySelectAnimation.process(null, null);
        assertFalse(mySelectAnimation.isSelected());

    }

    /**
     * Only one of two ("MagCompassFloat"/"MagCompass") should exist depending on "property alias="/params/bushkit" ("sim/model/variant")
     */
    private void validateMagCompass(SceneNode modelRoot, List<SGAnimation> animationList) {
        SceneNode MagCompassFloat = EngineTestUtils.findSingleNodeByName(modelRoot, "MagCompassFloat");
        assertNotNull(MagCompassFloat);

        SceneNode MagCompass = EngineTestUtils.findSingleNodeByName(modelRoot, "MagCompass");
        assertNotNull(MagCompass);

        SGReaderWriterXML.SGSwitchUpdateCallback magCompassCallback = (SGReaderWriterXML.SGSwitchUpdateCallback) FgTestUtils.findNodeCallback("MagCompass");
        assertNotNull(magCompassCallback);
        assertTrue(magCompassCallback.mCondition.test());

        SGReaderWriterXML.SGSwitchUpdateCallback magCompassFloatCallback = (SGReaderWriterXML.SGSwitchUpdateCallback) FgTestUtils.findNodeCallback("MagCompassFloat");
        assertNotNull(magCompassFloatCallback);
        assertFalse(magCompassFloatCallback.mCondition.test());
    }

    @Test
    public void testC172p2024ProceduralLight() throws Exception {
        // Kruecke zur Entkopplung des Modelload von AC policy.
        ModelLoader.processPolicy = new ACProcessPolicy(null);

        List<SGAnimation> animationList = new ArrayList<SGAnimation>();

        Bundle bundle = loadBundle(bundleName172p2024);

        assertEquals(0, MakeEffect.effectMap.size());

        AircraftResourceProvider arp = new AircraftResourceProvider();
        arp.setAircraftDirAndBundle("c172p", bundleName172p2024);
        FgBundleHelper.addProvider(arp);

        Model.ghostedObjects.clear();

        BundleResource br = new BundleResource(bundle, "Models/Effects/lights/procedural_light_nav_right.xml");
        LoaderOptions options = new LoaderOptions();
        options.effectBuilderListener = new EffectCollector();
        BuildResult result = SGReaderWriterXML.buildModelFromBundleXML(br, options, (bpath, destinationNode, alist) -> {
            if (alist != null) {
                animationList.addAll(alist);
            }
        });

        TestHelper.processAsync();
        TestHelper.processAsync();
        assertEquals(0, MakeEffect.errorList.size());
        assertEquals(2, animationList.size());

        //<model>
        //<name>nav-light-right</name>
        //<path>Effects/lights/procedural_light_nav_right.xml</path>
        //<offsets>
        //<x-m>0.102</x-m>
        //<y-m>5.66</y-m>
        //<z-m>0.53459</z-m>
        //</offsets>
        //</model>

        // Effects/lights/procedural_light_nav_right.xml:
        // <path>procedural_light.ac</path>
        //<nopreview/>
        //<effect>
        //<inherits-from>procedural-light-nav-right</inherits-from>
        //<object-name>procedural_light</object-name>
        //</effect>

        //Effect:
        //<PropertyList>
        //<name>procedural-light-nav-right</name>
        //<inherits-from>Effects/procedural-light</inherits-from>
        //<parameters>
        //...

        Map<String, List<Effect>> proceduralLightNavRightEffects = ((EffectCollector) options.effectBuilderListener).effects.get("procedural_light_nav_right.xml.effect.procedural_light");
        assertNotNull(proceduralLightNavRightEffects);
        Effect proceduralLightNavRightEffect = proceduralLightNavRightEffects.get("procedural_light").get(0);
        // Single technique inherited from "fgdatabasic/Effects/procedural-light.eff"
        List<Technique> techniques = proceduralLightNavRightEffect.getTechniques();
        assertEquals(1, techniques.size());
        // pred expression in 'Effects/procedural-light.eff' should finally be false
        assertFalse(techniques.get(0).valid());
        assertEquals(1, Model.ghostedObjects.size());
        assertNull(proceduralLightNavRightEffect.getAppliedTechnique());

        assertEquals(2, animationList.size());
        SGScaleAnimation sgScaleAnimation = (SGScaleAnimation) animationList.get(0);

    }

    private Bundle loadBundle(String bundleName) {
        //if (cachedBundleName172p2024 == null) {
        EngineTestFactory.loadBundleAndWait(bundlePool + "/" + bundleName);
        Bundle bundle = BundleRegistry.getBundle(bundleName);
        //} else {
        //    BundleRegistry.registerBundle(bundleName172p2024, cachedBundleName172p2024);
        //}
        assertNotNull(bundle);
        return bundle;
    }
}

