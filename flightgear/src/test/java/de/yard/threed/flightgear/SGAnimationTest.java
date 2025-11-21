package de.yard.threed.flightgear;

import de.yard.threed.core.BuildResult;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.platform.common.ModelLoader;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.testutil.EngineTestUtils;
import de.yard.threed.engine.testutil.TestHelper;
import de.yard.threed.flightgear.core.flightgear.main.AircraftResourceProvider;
import de.yard.threed.flightgear.core.flightgear.main.FGGlobals;
import de.yard.threed.flightgear.core.osg.Group;
import de.yard.threed.flightgear.core.osg.Node;
import de.yard.threed.flightgear.core.osg.Switch;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.scene.material.Effect;
import de.yard.threed.flightgear.core.simgear.scene.material.MakeEffect;
import de.yard.threed.flightgear.core.simgear.scene.model.*;
import de.yard.threed.flightgear.core.simgear.scene.util.SGTransientModelData;
import de.yard.threed.flightgear.testutil.AnimationAssertions;
import de.yard.threed.flightgear.testutil.EffectCollector;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.flightgear.testutil.FgTestUtils;
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
 * Now a dedicated one beside tests of SGAnimation in EffectTest and VehicleEffectTest
 * <p>
 */
@Slf4j
public class SGAnimationTest {

    @BeforeEach
    void setup() {
        FgTestFactory.initPlatformForTest(true, false, false);
    }

    /**
     *
     */
    @Test
    public void testNestedAnimationGroups() {

        // Kruecke zur Entkopplung des Modelload von AC policy.
        ModelLoader.processPolicy = new ACProcessPolicy(null);

        List<SGAnimation> animationList = new ArrayList<SGAnimation>();

        Bundle bundlemodel = BundleRegistry.getBundle("test-resources");
        assertNotNull(bundlemodel);

        BuildResult result = SGReaderWriterXML.buildModelFromBundleXML(new BundleResource(bundlemodel, "Models/digital-clock/digital-clock.xml"), null, (bpath, destinationNode, alist) -> {
            if (alist != null) {
                animationList.addAll(alist);
            }
        });

        TestHelper.processAsync();
        TestHelper.processAsync();
        assertEquals(14, animationList.size(), "animations");

        SceneNode root = new SceneNode(result.getNode());
        SceneNode hr001 = EngineTestUtils.findSingleNodeByName(root, "HR.001");
        SceneNode knobLeft = EngineTestUtils.findSingleNodeByName(root, "KnobLeft");

        String hierarchy = EngineTestUtils.getHierarchy(hr001, 8, false);
        log.debug("{}", root.dump("", 0));
        log.debug("hr001 up-hierarchy={}", hierarchy);

        AnimationAssertions.assertAnimationGroupHierarchy(hr001, "Blender_exporter_v2.26__digital-clock.ac->MaterialAnimationGroup-DHHMMC->SelectAnimation-H->TextureTransformGroup-H->HR.001");
        AnimationAssertions.assertAnimationGroupHierarchy(knobLeft, "Blender_exporter_v2.26__digital-clock.ac->KnobLeft");
        // assertEquals("gltfroot->Blender_exporter_v2.26__c172-common.ac->fuselage_1->material animation group->selectAnimation->material animation group->wing_right", hierarchy);

        SGPropertyNode selectAnimationNode = new SGPropertyNode("animation");
        selectAnimationNode.addChild(new SGPropertyNode("type", "select"));
        selectAnimationNode.addChild(new SGPropertyNode("object-name", "HR.001"));
        selectAnimationNode.addChild(new SGPropertyNode("object-name", "KnobLeft"));

        Group node = new Group();
        root.getTransform().setParent(node.getTransform());
        //SGTransientModelData modelData = new SGTransientModelData(group, prop_root, options, bpath.name/*local8BitStr()*/);
        SGTransientModelData modelData = new SGTransientModelData(node, selectAnimationNode, null, "");
        modelData.LoadAnimationValuesForElement(selectAnimationNode, 0);

        SGSelectAnimation selectAnimation = (SGSelectAnimation) SGAnimation.animate(modelData, "label");
        // No an additonal layer should have injected right above "HR.001"
        AnimationAssertions.assertAnimationGroupHierarchy(hr001, "Blender_exporter_v2.26__digital-clock.ac->MaterialAnimationGroup-DHHMMC->SelectAnimation-H->TextureTransformGroup-H->SelectAnimation-HK->HR.001");

        // "KnobLeft" shouldn't be moved to group of "HR.001" but should have its own.
        AnimationAssertions.assertAnimationGroupHierarchy(knobLeft, "Blender_exporter_v2.26__digital-clock.ac->SelectAnimation-HK->KnobLeft");

    }

    /**
     *
     */
    @Test
    public void testSGSwitchUpdateCallback() {

        // Kruecke zur Entkopplung des Modelload von AC policy.
        ModelLoader.processPolicy = new ACProcessPolicy(null);

        List<SGAnimation> animationList = new ArrayList<SGAnimation>();

        Bundle bundlemodel = BundleRegistry.getBundle("test-resources");
        assertNotNull(bundlemodel);

        assertEquals(0, MakeEffect.effectMap.size());
        LoaderOptions opt = new LoaderOptions();
        opt.usegltf = true;

        BuildResult result = SGReaderWriterXML.buildModelFromBundleXML(new BundleResource(bundlemodel, "Models/SwitchAnimation.xml"), opt, (bpath, destinationNode, alist) -> {
            if (alist != null) {
                animationList.addAll(alist);
            }
        });
        TestHelper.processAsync();
        TestHelper.processAsync();

        SceneNode modelRoot = new SceneNode(result.getNode());
        log.debug("dump: " + modelRoot.dump("  ", 0));

        //cannot be cast Switch conditionSwitch = (Switch) EngineTestUtils.findSingleNodeByName(modelRoot, "SubmodelConditionSwitch");

        SGReaderWriterXML.SGSwitchUpdateCallback switchUpdateCallback = (SGReaderWriterXML.SGSwitchUpdateCallback)
                FgTestUtils.findNodeCallback("digital-clock/digital-clock.xml");//conditionSwitch.callback;
        assertNotNull(switchUpdateCallback);
        // Initial propery value is 0, so 'greater' condition is false
        assertFalse(switchUpdateCallback.mCondition.test());
        switchUpdateCallback.update();

        FGGlobals.getInstance().get_props().getNode("/sim/model/lightmap/dome/factor", false).setFloatValue(0.5f);
        assertTrue(switchUpdateCallback.mCondition.test());
    }

}
