package de.yard.threed.trafficfg;

import de.yard.threed.core.Degree;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.traffic.TrafficConfig;
import de.yard.threed.traffic.WorldGlobal;
import de.yard.threed.traffic.config.ConfigAttributeFilter;
import de.yard.threed.traffic.config.ConfigHelper;
import de.yard.threed.traffic.config.PoiConfig;
import de.yard.threed.traffic.config.SceneConfig;
import de.yard.threed.traffic.config.VehicleConfigDataProvider;
import de.yard.threed.traffic.config.VehicleDefinition;
import de.yard.threed.traffic.config.ViewpointConfig;
import de.yard.threed.traffic.config.XmlVehicleDefinition;
import de.yard.threed.trafficcore.config.AirportDefinition;
import de.yard.threed.trafficcore.model.Vehicle;
import de.yard.threed.trafficfg.config.AirportConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.yard.threed.engine.testutil.TestUtils.assertViewPoint;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests migrated from TraffiwWorldConfigTest.
 * Also for SmartLocation.
 * <p>
 * <p>
 * Created by thomass on 20.2.2018
 */
public class TrafficConfigTest {

    @BeforeAll
    static void setup() {
        Platform platform = FgTestFactory.initPlatformForTest(false, false);

        //7.1.23 EngineTestFactory.addBundleFromProjectDirectory("extended-config", "data/extended-config");

    }


    @Test
    public void testRailing() {

        TrafficConfig railing = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("traffic-fg"), BundleResource.buildFromFullString("railing/Railing.xml"));
        // 'Railing.xml' doesn't use include.
        VehicleDefinition vc = getVehicleConfig(TrafficConfig.buildFromBundle(BundleRegistry.getBundle("traffic-fg"), BundleResource.buildFromFullString(("railing/locomotive.xml"))).getVehicleDefinitions(), "locomotive");
        Assertions.assertNotNull(vc, "VehicleDefinition");
        //SceneConfig sceneConfig = railing.getScene("Railing");
        List<NativeNode> viewpoints = railing.getViewpoints();
        assertEquals(3, viewpoints.size());
        assertViewPoint("view3", new LocalTransform(new Vector3(40, 10, -10),
                Quaternion.buildFromAngles(new Degree(-20), new Degree(0), new Degree(0))), ConfigHelper.buildViewpoint(viewpoints.get(2)));
    }

    private VehicleDefinition getVehicleConfig(List<NativeNode> vds, String name) {
        VehicleConfigDataProvider vcdp = new VehicleConfigDataProvider(
                XmlVehicleDefinition.convertVehicleDefinitions(vds));

        List<VehicleDefinition> vehicleDefinitions = vcdp.findVehicleDefinitionsByName(name);
        return vehicleDefinitions.get(0);
        //24.11.23 return ConfigHelper.getVehicleConfig(tw.tw, name);
    }

    private VehicleDefinition getVehicleConfigByType(List<NativeNode> vds, String type) {
        VehicleConfigDataProvider vcdp = new VehicleConfigDataProvider(
                XmlVehicleDefinition.convertVehicleDefinitions(vds));

        List<VehicleDefinition> vehicleDefinitions = vcdp.findVehicleDefinitionsByModelType(type);
        return vehicleDefinitions.get(0);
        //24.11.23 return ConfigHelper.getVehicleConfig(tw.tw, name);
    }
}

