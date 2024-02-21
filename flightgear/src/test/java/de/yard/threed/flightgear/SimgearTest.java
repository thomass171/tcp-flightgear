package de.yard.threed.flightgear;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.flightgear.core.PropertyList;
import de.yard.threed.flightgear.core.flightgear.main.AircraftResourceProvider;
import de.yard.threed.flightgear.core.flightgear.main.FGProperties;
import de.yard.threed.flightgear.core.osg.Node;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.misc.SGPath;
import de.yard.threed.flightgear.core.simgear.props.PropsIO;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterialLib;
import de.yard.threed.flightgear.core.simgear.structure.SGException;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.engine.testutil.EngineTestFactory;


import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.core.StringUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 * Test fuer Dieses und jenes, wo es kein eigenen Test für gibt.
 * Created by thomass on 27.11.15.
 */
public class SimgearTest {
    static Platform platform = FgTestFactory.initPlatformForTest(true,true);

    String modelfile = "models/efis-ctl1.xml";
    String testfile777200 = "models/777-200.xml";

    @Test
    public void testProperties() {
        SGPropertyNode props = new SGPropertyNode("root");
        try {
            new PropsIO().readProperties(new BundleResource(BundleRegistry.getBundle("test-resources"),modelfile), props);
        } catch (SGException e) {
            throw new RuntimeException(e);
        }
        Assertions.assertTrue( props.hasValue("/path"),"/path");
        Assertions.assertTrue( props.hasChild("animation"),"animation");
        Assertions.assertFalse( props.hasChild("animationxx"),"animationxx");
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
        Assertions.assertEquals( 11, model_nodes.size(),"modelnodecnt");

        for (int i = 0; i < model_nodes.size(); i++) {
            SGPropertyNode/*_ptr*/ sub_props = model_nodes.get(i);

            SGPath submodelpath;
            /*osg::ref_ptr < osg::*/
            Node submodel;

            String subPathStr = sub_props.getStringValue("path");
            Assertions.assertFalse(StringUtils.empty(subPathStr),"subPathStr");
        }
        Assertions.assertEquals("Aircraft/777/Models/flightdeck-200.xml", model_nodes.get(0).getStringValue("path"),"subPathStr");
        Assertions.assertEquals( "Aircraft/777/Models/Lights/light-coneLR.xml", model_nodes.get(1).getStringValue("path"),"subPathStr");
        Assertions.assertEquals( "Aircraft/777/Models/Lights/light-coneC.xml", model_nodes.get(2).getStringValue("path"),"subPathStr");

        SGPropertyNode pathnode = props.getChildren("path").get(0);
        Assertions.assertEquals( "Aircraft/777/Models/777-200.ac", pathnode.getStringValue(),"path value");

    }

    @Test
    public void testPropertyTree() {
        SGPropertyNode props = new SGPropertyNode("");


        //Muss mit ganzem Pfad angelegt werden
        props.setBoolValue("/sim/fghome-readonly", true);
        Assertions.assertTrue( props.getBoolValue("/sim/fghome-readonly", false),"/sim/fghome-readonly");

        Assertions.assertNull( props.getChild("/sim", 0, false),"falscher getchild");
        SGPropertyNode simnode = props.getChild("sim", 0, false);
        Assertions.assertEquals(1, simnode.getChildren("fghome-readonly").size(),"simnode.children");

        simnode = props.getNode("/sim", 0, false);
        Assertions.assertEquals( 1, simnode.getChildren("fghome-readonly").size(),"getNode");

        props.setBoolValue("/sim/fghome-readonly", false);
        Assertions.assertFalse( props.getBoolValue("/sim/fghome-readonly", true),"/sim/fghome-readonly");

        //props.setBoolValue("/sim/fgcom/enabled", true);
        System.out.println(props.dump("\n"));
    }

    @Test
    public void testAircraftDir() {
        // braucht auch einen Init wegen PropertyTree
        //MA23FlightGear.init(0, FlightGear.argv);
        EngineTestFactory.loadBundleSync(SGMaterialLib.BUNDLENAME);
        FlightGearMain.initFG(null, null);

        // Dummy Bundle bauen. Why? Isn't needed/used.
        //Bundle bundle = new Bundle("My-777","Models/777-200.ac\n",false);
        //bundle.addResource("Models/777-200.ac",new BundleData(""));

        Bundle bundleTestResources = BundleRegistry.getBundle("test-resources");
        //FGProperties.fgSetString("/sim/aircraft-dir", "My-777");
        FGProperties.fgSetString("/sim/aircraft-dir", bundleTestResources.name);
        //String path = "Aircraft/My-777/Models/777-200.ac";
        String path = "Aircraft/"+bundleTestResources.name+"/models/777-200.ac.gz";
        BundleResource result  = new AircraftResourceProvider().resolve(path);
        Assertions.assertEquals( "models", result.getPath().getPath());
        Assertions.assertEquals( "777-200.ac.gz", result.getName());
        Assertions.assertEquals( bundleTestResources.name, result.bundle.name);
    }
}
