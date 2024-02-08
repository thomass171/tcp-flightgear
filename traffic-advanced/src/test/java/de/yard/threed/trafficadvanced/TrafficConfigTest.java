package de.yard.threed.trafficadvanced;

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
import de.yard.threed.traffic.config.ConfigHelper;
import de.yard.threed.traffic.config.PoiConfig;
import de.yard.threed.traffic.config.VehicleConfigDataProvider;
import de.yard.threed.traffic.config.VehicleDefinition;
import de.yard.threed.traffic.config.XmlVehicleDefinition;
import de.yard.threed.trafficcore.config.AirportDefinition;
import de.yard.threed.trafficcore.model.Vehicle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
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

        EngineTestFactory.loadBundleAndWait("traffic-advanced");

    }

    @Test
    public void test1() {

        TrafficConfig vehicleDefinitions = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("traffic-advanced"), BundleResource.buildFromFullString("vehicle-definitions.xml"));

        Assertions.assertNotNull(getVehicleConfig(vehicleDefinitions.getVehicleDefinitions(),"747 KLM"), "vehicle1.config");
        Assertions.assertEquals("https://ubuntu-server.udehlavj1efjeuqv.myfritz.net/publicweb/bundlepool/fgdatabasicmodel", getVehicleConfig(vehicleDefinitions.getVehicleDefinitions(),"747 KLM").getBundlename(), "vehicle1.bundlename");
        Assertions.assertEquals("AI/Aircraft/747/744-KLM.xml", getVehicleConfig(vehicleDefinitions.getVehicleDefinitions(),"747 KLM").getModelfile(), "vehicle1.model");
        Assertions.assertEquals(3, getVehicleConfig(vehicleDefinitions.getVehicleDefinitions(),"737-800 AB").getZoffset(), "vehicle1.zoffset");
        Assertions.assertEquals("738", getVehicleConfig(vehicleDefinitions.getVehicleDefinitions(),"737-800 AB").getModelType(), "vehicle1.zoffset");

        List<NativeNode> viewpoints ;

        TrafficConfig eddkFlat = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("traffic-advanced"), BundleResource.buildFromFullString("EDDK-flat.xml"));
        assertNotNull(eddkFlat);
        viewpoints = eddkFlat.getViewpoints();
        Assertions.assertEquals(2, viewpoints.size(), "viewpoints");
        Assertions.assertEquals("TopView00", ConfigHelper.buildViewpoint(viewpoints.get(0)).name, "viewpoint0.name");
        Assertions.assertEquals("TopView", ConfigHelper.buildViewpoint(viewpoints.get(1)).name, "viewpoint1.name");
        AirportDefinition ad = eddkFlat.findAirportDefinitionsByIcao("EDDK").get(0);
        Assertions.assertEquals(2, ad.getLocations().size(), "locations.EDDK.size");

        TrafficConfig eddfFlat = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("traffic-advanced"), BundleResource.buildFromFullString("EDDF-flat.xml"));
        assertNotNull(eddfFlat);
        viewpoints = eddfFlat.getViewpoints();
        Assertions.assertEquals(2, viewpoints.size(), "viewpoints");
        Assertions.assertEquals("TopView00", ConfigHelper.buildViewpoint(viewpoints.get(0)).name, "viewpoint0.name");
        Assertions.assertEquals("TopView", ConfigHelper.buildViewpoint(viewpoints.get(1)).name, "viewpoint1.name");

        AirportDefinition eddk = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("traffic-advanced"), BundleResource.buildFromFullString("EDDK.xml")).findAirportDefinitionsByIcao("EDDK").get(0);
        Assertions.assertNotNull(eddk, "eddk");
        Assertions.assertEquals(3, eddk.getVehicles().size(), "vehiclecnt");
        Assertions.assertEquals("737-800 AB", eddk.getVehicles().get(1).getName(), "vehicle1.name");
        Assertions.assertNotNull(getVehicleConfig(vehicleDefinitions.getVehicleDefinitions(), eddk.getVehicles().get(1).getName()), "vehicle1.config");
        Assertions.assertEquals("parkpos:B_8", eddk.getVehicles().get(1).getLocation().location, "vehicle1.location");
        Assertions.assertEquals("B_8", eddk.getVehicles().get(1).getLocation().getParkPos(), "vehicle1.parkpos");

        VehicleDefinition config = getVehicleConfig(vehicleDefinitions.getVehicleDefinitions(), "c172p");
        assertNotNull(config);
        TestUtils.assertVector3(new Vector3(0.36f, -0.14f, 0.236f), config.getViewpoints().get("Captain").position, "c172p.viewpoint.captain.location");

        List<Vehicle> vehiclelist = vehicleDefinitions.getVehicleListByName("VehiclesWithCockpitXX");
        Assertions.assertNull(vehiclelist);
        vehiclelist = vehicleDefinitions.getVehicleListByName("VehiclesWithCockpit");
        Assertions.assertNotNull(vehiclelist);
        Assertions.assertEquals(4/*no bluebird 5*/, vehiclelist.size(), "VehiclesWithCockpit.size");
        Assertions.assertEquals("Locomotive", vehiclelist.get(3).getName(), "VehiclesWithCockpit[3]");
        Assertions.assertEquals("c172p", vehiclelist.get(0).getName(), "vehicle0.name");
        Assertions.assertEquals("777", vehiclelist.get(1).getName(), "aircraft.name");
        config = getVehicleConfig(vehicleDefinitions.getVehicleDefinitions(), "777");
        Assertions.assertEquals("https://ubuntu-server.udehlavj1efjeuqv.myfritz.net/publicweb/bundlepool/777", config.getBundlename(), "aircraft.bundlename");

    }

    @Test
    public void testAircraftConfig() {
        /*GroundServiceAircraftConfig*/
        TrafficConfig vehicleDefinitions = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("traffic-advanced"), BundleResource.buildFromFullString("vehicle-definitions.xml"));

        VehicleDefinition cfg747400 = /*getAircraftConfiguration*/getVehicleConfigByType(vehicleDefinitions.getVehicleDefinitions(),"747-400");
        Assertions.assertNotNull(cfg747400, "aircraft.config");
        Assertions.assertEquals(64.4f, cfg747400.getWingspread(), "aircraft.getWingspread");
        // ynegiert wegen rechter Door
        TestUtils.assertVector3(new Vector3(-20.38f, 2.89f, -0.24f), cfg747400.getCateringDoorPosition(), "aircraft.getWingspread");

        /*GroundServiceAircraftConfig*/
       /*28.11.23:TODO not really a config test, but Groundservices. Needs to be moved VehicleDefinition cfg738default = null;//tw.getAircraftConfiguration("xxx");
        Assertions.assertNotNull(cfg738default, "aircraft.config");
        Assertions.assertEquals(35.79f, cfg738default.getWingspread(), "aircraft.getWingspread");

*/
    }

    @Test
    public void testAddedConfig() {
        //tw = new TrafficWorldConfig("data-old", "TrafficWorld.xml");
        //TrafficWorldConfig railing = new TrafficWorldConfig("railing", "config/Railing.xml");
        /*27.12.21 not any more tw.add(railing);
       TestUtil.assertEquals("vehicles", 15/*bluebird14* /, tw.getVehicleCount());*/
    }

    @Test
    public void testPoi() {
        TrafficConfig worldPois = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("traffic-advanced"), BundleResource.buildFromFullString("world-pois.xml"));

        PoiConfig poi = worldPois.getPoiByName("equator20000");
        Assertions.assertNotNull(poi, "poi");
        Assertions.assertEquals("equator20000", poi.getName(), "poi.name");
        Assertions.assertEquals(WorldGlobal.equator020000.getElevationM(), poi.elevation, "poi.name");
        poi = worldPois.getPoiByName("EDDK Overview");
        Assertions.assertNotNull(poi, "poi");
        Assertions.assertEquals("EDDK Overview", poi.getName(), "poi.name");
        Assertions.assertEquals(WorldGlobal.eddkoverview.location.coordinates.getElevationM(), poi.elevation, "poi.name");
        Assertions.assertEquals(WorldGlobal.eddkoverview.location.coordinates.getLonDeg().getDegree(), poi.longitude.getDegree(), 0.001, "poi.name");
        Assertions.assertEquals(WorldGlobal.eddkoverview.location.coordinates.getLatDeg().getDegree(), poi.latitude.getDegree(), 0.001, "poi.name");
        Assertions.assertEquals(WorldGlobal.eddkoverview.location.heading.getDegree(), poi.heading.getDegree(), "poi.name");
        Assertions.assertEquals(WorldGlobal.eddkoverview.location.pitch.getDegree(), poi.pitch.getDegree(), "poi.name");
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

