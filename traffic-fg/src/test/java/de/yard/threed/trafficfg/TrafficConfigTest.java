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

        EngineTestFactory.loadBundleAndWait("traffic-fg");

    }


    @Test
    public void testRailing() {

        TrafficConfig railing = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("traffic-fg"), BundleResource.buildFromFullString("railing/Railing.xml"));
        // 'Railing.xml' doesn't use include.
        VehicleDefinition vc = getVehicleConfig(TrafficConfig.buildFromBundle(BundleRegistry.getBundle("traffic-fg"), BundleResource.buildFromFullString(("railing/locomotive.xml"))).getVehicleDefinitions(), "locomotive");
        assertNotNull(vc, "VehicleDefinition");
        //SceneConfig sceneConfig = railing.getScene("Railing");
        List<NativeNode> viewpoints = railing.getViewpoints();
        assertEquals(3, viewpoints.size());
        assertViewPoint("view3", new LocalTransform(new Vector3(40, 10, -10),
                Quaternion.buildFromAngles(new Degree(-20), new Degree(0), new Degree(0))), ConfigHelper.buildViewpoint(viewpoints.get(2)));
    }

    @Test
    public void testAirports(){
        List<NativeNode> viewpoints ;

        TrafficConfig eddkFlat = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("traffic-fg"), BundleResource.buildFromFullString("flight/EDDK-flat.xml"));
        assertNotNull(eddkFlat);
        viewpoints = eddkFlat.getViewpoints();
        assertEquals(2, viewpoints.size(), "viewpoints");
        assertEquals("TopView00", ConfigHelper.buildViewpoint(viewpoints.get(0)).name, "viewpoint0.name");
        assertEquals("TopView", ConfigHelper.buildViewpoint(viewpoints.get(1)).name, "viewpoint1.name");
        AirportDefinition ad = eddkFlat.findAirportDefinitionsByIcao("EDDK").get(0);
        assertEquals(2, ad.getLocations().size(), "locations.EDDK.size");
        assertEquals("A20", ad.getHome(), "home");

        TrafficConfig eddfFlat = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("traffic-fg"), BundleResource.buildFromFullString("flight/EDDF-flat.xml"));
        assertNotNull(eddfFlat);
        viewpoints = eddfFlat.getViewpoints();
        assertEquals(2, viewpoints.size(), "viewpoints");
        assertEquals("TopView00", ConfigHelper.buildViewpoint(viewpoints.get(0)).name, "viewpoint0.name");
        assertEquals("TopView", ConfigHelper.buildViewpoint(viewpoints.get(1)).name, "viewpoint1.name");
        ad = eddfFlat.findAirportDefinitionsByIcao("EDDF").get(0);
        assertEquals(0, ad.getLocations().size(), "locations.EDDF.size");
        assertEquals("V164", ad.getHome(), "home");

        AirportDefinition eddk = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("traffic-fg"), BundleResource.buildFromFullString("flight/EDDK.xml")).findAirportDefinitionsByIcao("EDDK").get(0);
        assertNotNull(eddk, "eddk");
        assertEquals(3, eddk.getVehicles().size(), "vehiclecnt");
        assertEquals("737-800 AB", eddk.getVehicles().get(1).getName(), "vehicle1.name");
        assertEquals("parkpos:B_8", eddk.getVehicles().get(1).getLocation().location, "vehicle1.location");
        assertEquals("B_8", eddk.getVehicles().get(1).getLocation().getParkPos(), "vehicle1.parkpos");
    }

    @Test
    public void testPoi() {
        TrafficConfig worldPois = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("traffic-fg"), BundleResource.buildFromFullString("flight/world-pois.xml"));
        assertNotNull(worldPois);
        PoiConfig poi = worldPois.getPoiByName("equator20000");
        assertNotNull(poi, "poi");
        assertEquals("equator20000", poi.getName(), "poi.name");
        assertEquals(WorldGlobal.equator020000.getElevationM(), poi.elevation, "poi.name");
        poi = worldPois.getPoiByName("EDDK Overview");
        assertNotNull(poi, "poi");
        assertEquals("EDDK Overview", poi.getName(), "poi.name");
        assertEquals(WorldGlobal.eddkoverview.location.coordinates.getElevationM(), poi.elevation, "poi.name");
        assertEquals(WorldGlobal.eddkoverview.location.coordinates.getLonDeg().getDegree(), poi.longitude.getDegree(), 0.001, "poi.name");
        assertEquals(WorldGlobal.eddkoverview.location.coordinates.getLatDeg().getDegree(), poi.latitude.getDegree(), 0.001, "poi.name");
        assertEquals(WorldGlobal.eddkoverview.location.heading.getDegree(), poi.heading.getDegree(), "poi.name");
        assertEquals(WorldGlobal.eddkoverview.location.pitch.getDegree(), poi.pitch.getDegree(), "poi.name");
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

