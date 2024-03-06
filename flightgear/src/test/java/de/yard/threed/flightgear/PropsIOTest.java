package de.yard.threed.flightgear;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.props.PropsIO;
import de.yard.threed.flightgear.core.simgear.structure.SGException;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 */
@Slf4j
public class PropsIOTest {
    Platform platform = FgTestFactory.initPlatformForTest(true, false);

    @Test
    public void test() {


        Bundle bundle = BundleRegistry.getBundle("test-resources");
        assertNotNull(bundle);

        PropsIO propsIO = new PropsIO();
        SGPropertyNode startnode = new SGPropertyNode();

        BundleResource br = new BundleResource(bundle, new ResourcePath("xmltestmodel"), "test-main.xml");
        try {
            propsIO.readProperties(br, startnode);
        } catch (SGException e) {
            throw new RuntimeException();
        }
        validateXmlTestModel(propsIO, startnode);
    }

    private void validateXmlTestModel(PropsIO propsIO, SGPropertyNode startnode) {
        // name,path,2xsubmodel,2x animations
        assertEquals(6, startnode.nChildren());
        log.debug("locations=" + propsIO.locations);
        assertEquals(39, propsIO.locations.size());
        // both repeat many times
        assertEquals("xmltestmodel/test-main.xml", propsIO.locations.get(0));
        assertEquals("xmltestmodel/test-animations.xml", propsIO.locations.get(1));
    }
}