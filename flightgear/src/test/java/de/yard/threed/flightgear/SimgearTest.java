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
import de.yard.threed.core.StringUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 * Tests for stuff that has no own test.
 * Created by thomass on 27.11.15.
 */
public class SimgearTest {
    static Platform platform = FgTestFactory.initPlatformForTest(true,true,false);


    @Test
    public void testAircraftDir() {

        EngineTestFactory.loadBundleSync(SGMaterialLib.BUNDLENAME);
        FlightGearMain.initFG(null, null);

        // Dummy Bundle bauen. Why? Isn't needed/used.
        //Bundle bundle = new Bundle("My-777","Models/777-200.ac\n",false);
        //bundle.addResource("Models/777-200.ac",new BundleData(""));

        Bundle bundleTestResources = BundleRegistry.getBundle("test-resources");
        //FGProperties.fgSetString("/sim/aircraft-dir", "My-777");
        FGProperties.fgSetString("/sim/aircraft-dir", bundleTestResources.name);
        //String path = "Aircraft/My-777/Models/777-200.ac";
        String path = "Aircraft/"+bundleTestResources.name+ "/Models/777-200.ac.gz";
        BundleResource result  = new AircraftResourceProvider().resolve(path);
        Assertions.assertEquals("Models", result.getPath().getPath());
        Assertions.assertEquals( "777-200.ac.gz", result.getName());
        Assertions.assertEquals( bundleTestResources.name, result.bundle.name);
    }
}
