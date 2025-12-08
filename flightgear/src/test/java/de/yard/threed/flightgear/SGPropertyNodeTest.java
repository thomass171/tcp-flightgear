package de.yard.threed.flightgear;

import de.yard.threed.core.StringUtils;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.flightgear.core.PropertyList;
import de.yard.threed.flightgear.core.flightgear.main.FGGlobals;
import de.yard.threed.flightgear.core.osg.Node;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.misc.SGPath;
import de.yard.threed.flightgear.core.simgear.props.PropsIO;
import de.yard.threed.flightgear.core.simgear.props.SGCondition;
import de.yard.threed.flightgear.core.simgear.structure.SGException;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Now a dedicated test beside tests as part of others
 * <p>
 */
@Slf4j
public class SGPropertyNodeTest {

    String modelfile = "Models/efis-ctl1.xml";
    String testfile777200 = "Models/777-200.xml";

    @BeforeEach
    void setup() {
        FgTestFactory.initPlatformForTest(true, false, false);
    }

    @Test
    public void testProperties() {
        SGPropertyNode props = new SGPropertyNode("root");
        try {
            new PropsIO().readProperties(new BundleResource(BundleRegistry.getBundle("test-resources"),modelfile), props);
        } catch (SGException e) {
            throw new RuntimeException(e);
        }
        assertTrue( props.hasValue("/path"),"/path");
        assertTrue( props.hasChild("animation"),"animation");
        assertFalse( props.hasChild("animationxx"),"animationxx");
    }

    @Test
    public void testFindNode() {
        SGPropertyNode props = new SGPropertyNode("");
        try {
            new PropsIO().readProperties(new BundleResource(BundleRegistry.getBundle("test-resources"),testfile777200), props);
        } catch (SGException t) {
            throw new RuntimeException(t);
        }

        System.out.println(props.dump("\n"));
        PropertyList /*Vector<SGPropertyNode_ptr>*/ model_nodes = props.getChildren("model");
        assertEquals( 11, model_nodes.size(),"modelnodecnt");

        for (int i = 0; i < model_nodes.size(); i++) {
            SGPropertyNode/*_ptr*/ sub_props = model_nodes.get(i);

            SGPath submodelpath;
            /*osg::ref_ptr < osg::*/
            Node submodel;

            String subPathStr = sub_props.getStringValue("path");
            assertFalse(StringUtils.empty(subPathStr),"subPathStr");
        }
        assertEquals("Aircraft/777/Models/flightdeck-200.xml", model_nodes.get(0).getStringValue("path"),"subPathStr");
        assertEquals( "Aircraft/777/Models/Lights/light-coneLR.xml", model_nodes.get(1).getStringValue("path"),"subPathStr");
        assertEquals( "Aircraft/777/Models/Lights/light-coneC.xml", model_nodes.get(2).getStringValue("path"),"subPathStr");

        SGPropertyNode pathnode = props.getChildren("path").get(0);
        assertEquals( "Aircraft/777/Models/777-200.ac", pathnode.getStringValue(),"path value");

    }

    @Test
    public void testPropertyTree() {
        SGPropertyNode props = new SGPropertyNode("");

        //Needs full path
        props.setBoolValue("/sim/fghome-readonly", true);
        assertTrue( props.getBoolValue("/sim/fghome-readonly", false),"/sim/fghome-readonly");

        assertNull( props.getChild("/sim", 0, false),"falscher getchild");
        SGPropertyNode simnode = props.getChild("sim", 0, false);
        assertEquals(1, simnode.getChildren("fghome-readonly").size(),"simnode.children");

        simnode = props.getNode("/sim", 0, false);
        assertEquals( 1, simnode.getChildren("fghome-readonly").size(),"getNode");

        props.setBoolValue("/sim/fghome-readonly", false);
        assertFalse( props.getBoolValue("/sim/fghome-readonly", true),"/sim/fghome-readonly");

        //props.setBoolValue("/sim/fgcom/enabled", true);
        log.debug(props.dump("\n"));
    }

    /**
     * Remember: There are no real arrays. Indices are just a kind of suffix to the name
     */
    @Test
    public void testArrays() {
        SGPropertyNode props = new SGPropertyNode("");

        //Needs full path
        SGPropertyNode rpm2 = props.getNode("/engines/engine[2]/rpm", true);
        rpm2.setDoubleValue(44.55);

        assertEquals( 44.55, props.getDoubleValue("/engines/engine[2]/rpm", 0.0));

        // Even when created with index [2] there is only one child
        assertEquals( 1, props.getNode("/engines", false).nChildren());

        assertEquals("(value=)-engines(value=)-engines.engine[2](value=)-engines.engine[2].rpm(value=44.55)-", props.dump("-"));

    }

}
