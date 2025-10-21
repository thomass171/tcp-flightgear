package de.yard.threed.trafficadvanced;

import de.yard.threed.core.BuildResult;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.platform.common.ModelLoader;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.testutil.TestHelper;
import de.yard.threed.flightgear.EffectBuilderListener;
import de.yard.threed.flightgear.FgBundleHelper;
import de.yard.threed.flightgear.LoaderOptions;
import de.yard.threed.flightgear.core.flightgear.main.AircraftResourceProvider;
import de.yard.threed.flightgear.core.simgear.scene.material.Effect;
import de.yard.threed.flightgear.core.simgear.scene.material.MakeEffect;
import de.yard.threed.flightgear.core.simgear.scene.material.Technique;
import de.yard.threed.flightgear.core.simgear.scene.model.*;
import de.yard.threed.flightgear.testutil.EffectCollector;
import de.yard.threed.flightgear.testutil.FgTestFactory;
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
        assertEquals(1686, animationList.size());
        SceneNode modelRoot = new SceneNode(result.getNode());
        assertNotNull(modelRoot);
        log.debug("dump: {}", modelRoot.dump("  ", 0));
        List<SceneNode> foundNodes = modelRoot.findNodeByName("Models/c172-common.xml");
        assertEquals(1, foundNodes.size());
        SceneNode commonNode = foundNodes.get(0);
        // log says 66, but 'c172sp' is conditional false
        assertEquals(65, commonNode.getTransform().getChildCount());
        assertEquals("Damage", commonNode.getTransform().getChild(0).getSceneNode().getName());
        assertEquals("Skis", commonNode.getTransform().getChild(5).getSceneNode().getName());
        SceneNode skis = commonNode.getTransform().getChild(5).getSceneNode();
        //  Only child should be "Models/Effects/skis/skis.xml"
        assertEquals(1, skis.getTransform().getChildCount());
        // "Models/Effects/damage/nose_gear.xml" has no name
        assertEquals("", commonNode.getTransform().getChild(6).getSceneNode().getName());
        assertEquals("MooringHarness", commonNode.getTransform().getChild(18).getSceneNode().getName());
        // depending on 'glass'
        assertEquals(/*"c172sp"*/"submodel condition switch", commonNode.getTransform().getChild(20).getSceneNode().getName());
        assertEquals(/*"c172sp?"*/"submodel condition switch", commonNode.getTransform().getChild(21).getSceneNode().getName());
        assertEquals("ControlLock", commonNode.getTransform().getChild(22).getSceneNode().getName());
        assertEquals("PilotPedals", commonNode.getTransform().getChild(23).getSceneNode().getName());
        assertEquals("CoPilotPedals", commonNode.getTransform().getChild(24).getSceneNode().getName());
        //assertEquals("OAT-object", commonNode.getTransform().getChild(43).getSceneNode().getName());

        foundNodes = modelRoot.findNodeByName("elevatorright");
        assertEquals(1, foundNodes.size());

        // don't care too much about effectMap assertEquals(117, MakeEffect.effectMap.size());

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

        Map<String,List<Effect>> proceduralLightNavRightEffects = ((EffectCollector)options.effectBuilderListener).effects.get("procedural_light_nav_right.xml.effect.procedural_light");
        assertNotNull(proceduralLightNavRightEffects);
        Effect proceduralLightNavRightEffect=proceduralLightNavRightEffects.get("procedural_light").get(0);
        // Single technique inherited from "fgdatabasic/Effects/procedural-light.eff"
        List<Technique> techniques = proceduralLightNavRightEffect.getTechniques();
        assertEquals(1,techniques.size());
        // pred expression in 'Effects/procedural-light.eff' should finally be false
        assertFalse(techniques.get(0).valid());
        assertEquals(1, Model.ghostedObjects.size());
        assertNull(proceduralLightNavRightEffect.getAppliedTechnique());

        assertEquals(2,animationList.size());
        SGScaleAnimation sgScaleAnimation= (SGScaleAnimation) animationList.get(0);

    }

    private Bundle loadBundle(String bundleName) {
        //if (cachedBundleName172p2024 == null) {
            EngineTestFactory.loadBundleAndWait(bundlePool + "/" + bundleName);
            Bundle bundle = BundleRegistry.getBundle(bundleName);
        //} else {
        //    BundleRegistry.registerBundle(bundleName172p2024, cachedBundleName172p2024);
        //}
        assertNotNull(bundle                   );
        return bundle;
    }
}

