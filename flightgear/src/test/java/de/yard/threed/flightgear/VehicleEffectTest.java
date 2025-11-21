package de.yard.threed.flightgear;

import de.yard.threed.core.BuildResult;
import de.yard.threed.core.Color;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.apps.ModelSamples;
import de.yard.threed.engine.platform.common.ModelLoader;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.testutil.EngineTestUtils;
import de.yard.threed.engine.testutil.TestHelper;
import de.yard.threed.flightgear.core.flightgear.main.AircraftResourceProvider;
import de.yard.threed.flightgear.core.simgear.scene.material.Effect;
import de.yard.threed.flightgear.core.simgear.scene.material.MakeEffect;
import de.yard.threed.flightgear.core.simgear.scene.model.ACProcessPolicy;
import de.yard.threed.flightgear.core.simgear.scene.model.Model;
import de.yard.threed.flightgear.core.simgear.scene.model.SGAnimation;
import de.yard.threed.flightgear.core.simgear.scene.model.SGReaderWriterXML;
import de.yard.threed.flightgear.testutil.EffectCollector;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.javacommon.SimpleHeadlessPlatform;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Effect tests only for vehicles, so without TerraSync and matlib.
 * <p>
 * Also for SGAnimation and thus also for SGReaderWriterXML.
 */
@Slf4j
public class VehicleEffectTest {

    @BeforeEach
    void setup() {
        FgTestFactory.initPlatformForTest(true, false, false);
    }

    /**
     * Up to now it is unclear what we can test here.
     */
    @Test
    public void testBird17() {

        // Kruecke zur Entkopplung des Modelload von AC policy.
        ModelLoader.processPolicy = new ACProcessPolicy(null);

        List<SGAnimation> animationList = new ArrayList<SGAnimation>();

        Bundle bundlemodel = BundleRegistry.getBundle("test-resources");
        assertNotNull(bundlemodel);

        assertEquals(0, MakeEffect.effectMap.size());

        BuildResult result = SGReaderWriterXML.buildModelFromBundleXML(new BundleResource(bundlemodel, "Models/bird17.xml"), null, (bpath, destinationNode, alist) -> {
            if (alist != null) {
                animationList.addAll(alist);
            }
        });
    }

    /***
     * Originally from c172p, but added to bluebird
     */
    @ParameterizedTest
    @CsvSource(value = {
            // for "Case" inherits from
            // -Aircraft:../../../../Effects/interior/lm-digitalclock.eff
            // -Aircraft:Models/Effects/interior/c172p-interior.eff
            // -fgdatabasic:Effects/model-interior.eff
            // -Effects/model-default.eff
            // for "DigitalClock" inherits from
            // -?
            // -?
            // TODO What about the other objects?
            "Models/Interior/Panel/Instruments/digital-clock/digital-clock.xml;true",
            "Models/Interior/Panel/Instruments/mag-compass/mag-compass.xml;false",
            "Models/bluebird.xml;false",
            "Models/Interior/Panel/Instruments/kx165/kx165-1.xml;false",
    }, delimiter = ';')
    public void testBluebirdAndComponents(String modelReference, boolean expectedDetailCheck) {

        // Kruecke zur Entkopplung des Modelload von AC policy.
        ModelLoader.processPolicy = new ACProcessPolicy(null);

        List<SGAnimation> animationList = new ArrayList<SGAnimation>();

        EngineTestFactory.loadBundleAndWait("bluebird");
        Bundle bluebirdmodel = BundleRegistry.getBundle("bluebird");
        assertNotNull(bluebirdmodel);

        assertEquals(0, MakeEffect.effectMap.size());

        /*should be no need SceneNode cube = ModelSamples.buildCube(1, Color.BLUE);
        cube.setName("Case");
        Scene.getCurrent().addToWorld(cube);*/

        AircraftResourceProvider arp = new AircraftResourceProvider();
        arp.setAircraftDir("bluebird");
        FgBundleHelper.addProvider(arp);

        BundleResource br = new BundleResource(bluebirdmodel, modelReference);
        BuildResult result = SGReaderWriterXML.buildModelFromBundleXML(br, null, (bpath, destinationNode, alist) -> {
            if (alist != null) {
                animationList.addAll(alist);
            }
        });

        TestHelper.processAsync();
        TestHelper.processAsync();
        assertEquals(0, MakeEffect.errorList.size());

        boolean detailCheckDone = false;
        if (modelReference.contains("digital-clock")) {
            // digital-clock.xml has 6 objects with effects defined
            assertEquals(6, Model.appliedEffects.size());
            assertEquals(1, Model.appliedEffects.get("Case").size());
            assertEquals(1, Model.appliedEffects.get("DigitalClock").size());
            assertEquals(1, Model.appliedEffects.get("KnobLeft").size());
            assertEquals(1, Model.appliedEffects.get("KnobCenter").size());
            assertEquals(1, Model.appliedEffects.get("KnobRight").size());
            assertEquals(1, Model.appliedEffects.get("glass_panel").size());

            // effectMap also contains standalone inherited. Effects are currently reused via effectMap!
            assertEquals(4 + 2, MakeEffect.effectMap.size());
            detailCheckDone = true;
        }
        assertEquals(expectedDetailCheck, detailCheckDone);
    }

    @Test
    public void testC172pBumpSpec() throws Exception {

        // Kruecke zur Entkopplung des Modelload von AC policy.
        ModelLoader.processPolicy = new ACProcessPolicy(null);

        //assertEquals(0, MakeEffect.effectMap.size());
        Model.ghostedObjects.clear();

        AircraftResourceProvider arp = new AircraftResourceProvider();
        arp.setAircraftDirAndBundle("c172p","test-resources");
        FgBundleHelper.addProvider(arp);

        BundleResource br = new BundleResource(BundleRegistry.getBundle("test-resources"), "Models/bumpspec-test.xml");
        LoaderOptions options = new LoaderOptions();
        options.effectBuilderListener = new EffectCollector();
        BuildResult result = SGReaderWriterXML.buildModelFromBundleXML(br, options, (bpath, destinationNode, alist) -> {
        });

        TestHelper.processAsync();
        TestHelper.processAsync();
        SceneNode modelRoot = new SceneNode(result.getNode());

        assertEquals(0, MakeEffect.errorList.size());

        Map<String, List<Effect>> caseEffects = ((EffectCollector) options.effectBuilderListener).effects.get("bumpspec-test.xml.effect.Case");
        assertNotNull(caseEffects);
        List<SceneNode> foundNodes = modelRoot.findNodeByName("Case");
        assertEquals(1, foundNodes.size());
        SceneNode caseNode = foundNodes.get(0);

        List<Effect> wingRightEffects = Model.appliedEffects.get("wing_right");
        // effect in map apperently overrwritten by damage effect with same name
        Effect effectFromCache = MakeEffect.effectMap.get("Aircraft/c172p/Models/Effects/exterior/dirt-wing");

        String hierarchy = EngineTestUtils.getHierarchy(caseNode, 6, false);
        log.debug("Case up-hierarchy={}", hierarchy);
        assertEquals("Models/bumpspec-test.xml->ACProcessPolicy.root node->ACProcessPolicy.transform node->Models/digital-clock/digital-clock.gltf->gltfroot->Blender_exporter_v2.26__digital-clock.ac->Case", hierarchy);
        SimpleHeadlessPlatform.DummyMaterial caseMaterial = (SimpleHeadlessPlatform.DummyMaterial) caseNode.getMesh().getMaterial().material;

        assertEquals("src/test/resources/Models/digital-clock/clock-tex.png,clock-tex.png", caseMaterial.uniformValue.get("u_texture"));
        assertEquals("true", caseMaterial.uniformValue.get("u_textured"));
        assertEquals("true", caseMaterial.uniformValue.get("u_shaded"));

    }

}
