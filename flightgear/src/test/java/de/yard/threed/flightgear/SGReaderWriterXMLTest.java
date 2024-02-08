package de.yard.threed.flightgear;

import de.yard.threed.core.BuildResult;
import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.platform.common.ModelLoader;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.testutil.TestHelper;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.SGLoaderOptions;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.scene.model.ACProcessPolicy;
import de.yard.threed.flightgear.core.simgear.scene.model.SGAnimation;
import de.yard.threed.flightgear.core.simgear.scene.model.SGMaterialAnimation;
import de.yard.threed.flightgear.core.simgear.scene.model.SGReaderWriterXML;
import de.yard.threed.flightgear.core.simgear.scene.model.SGRotateAnimation;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.traffic.config.VehicleDefinition;
import de.yard.threed.traffic.config.XmlVehicleDefinition;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 */
@Slf4j
public class SGReaderWriterXMLTest {
    Platform platform = FgTestFactory.initPlatformForTest(true, false);

    @Test
    public void testAsi() {

        // Kruecke zur Entkopplung des Modelload von AC policy.
        ModelLoader.processPolicy = new ACProcessPolicy(null);

        List<SGAnimation> animationList = new ArrayList<SGAnimation>();

        EngineTestFactory.loadBundleAndWait("traffic-fg");
        Bundle bundlemodel = BundleRegistry.getBundle("traffic-fg");
        assertNotNull(bundlemodel);

        BuildResult result = SGReaderWriterXML.buildModelFromBundleXML(new BundleResource(bundlemodel, "railing/asi.xml"), null, (bpath, alist) -> {
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
}