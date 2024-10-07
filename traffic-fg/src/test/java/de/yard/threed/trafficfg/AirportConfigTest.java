package de.yard.threed.trafficfg;

import de.yard.threed.core.Degree;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.traffic.TrafficConfig;
import de.yard.threed.traffic.WorldGlobal;
import de.yard.threed.traffic.config.ConfigHelper;
import de.yard.threed.traffic.config.PoiConfig;
import de.yard.threed.traffic.config.VehicleConfigDataProvider;
import de.yard.threed.traffic.config.VehicleDefinition;
import de.yard.threed.traffic.config.XmlVehicleDefinition;
import de.yard.threed.trafficcore.config.AirportDefinition;
import de.yard.threed.trafficfg.config.AirportConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.yard.threed.engine.testutil.EngineTestUtils.assertViewPoint;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 */
public class AirportConfigTest {

    @BeforeAll
    static void setup() {
        Platform platform = FgTestFactory.initPlatformForTest(false, false, false);
        EngineTestFactory.loadBundleAndWait("traffic-fg");
    }

    @Test
    public void testEDDK() {

        AirportConfig eddkConfig = AirportConfig.buildFromAirportConfig("traffic-fg","flight/EDDK-flat.xml","EDDK",null);
        assertEquals("A20", eddkConfig.getHome(), "home");

    }
}

