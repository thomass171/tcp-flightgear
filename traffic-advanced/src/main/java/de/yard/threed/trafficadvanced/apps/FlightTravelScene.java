package de.yard.threed.trafficadvanced.apps;

import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.gui.Hud;
import de.yard.threed.traffic.TrafficConfig;
import de.yard.threed.traffic.apps.BasicTravelScene;
import de.yard.threed.traffic.config.VehicleDefinition;
import de.yard.threed.traffic.config.XmlVehicleDefinition;
import de.yard.threed.trafficcore.config.AirportDefinition;
import de.yard.threed.trafficcore.model.SmartLocation;
import de.yard.threed.trafficcore.model.Vehicle;

import java.util.List;


/**
 * just for sharing commons
 */
public class FlightTravelScene extends BasicTravelScene {
    //4.12.23 protected TrafficWorldConfig tw;
    protected AirportDefinition airportDefinition;
    TrafficConfig trafficConfig;
    protected TrafficConfig worldPois;

    public Hud helphud;

    /**
     * 27.12.21 Das war immer schon EDDK lastig.
     * @return
     */
    //@Override
    public List<SmartLocation> getLocationList() {
        // 30.11.23 locations are in airports now
        //return tw.getLocationListByName("EDDK");
        return airportDefinition.getLocations();
    }

    /*31.10.23 @Override
    protected TrafficGraph getGroundNet() {
        //27.12.21 return DefaultTrafficWorld.getInstance().getGroundNetGraph("EDDK");
        return GroundServicesSystem.groundnetEDDK.groundnetgraph;
    }*/

    /**
     * 20.11.23: Was in TrafficWorlConfig. Now here a temp wrapper. 6.12.23:No longer really needed.
     */
    /*6.12.23 public static  VehicleDefinition getAircraftConfiguration(String modeltype) {
        Util.notyet();
        return null;
    }*/

    /**
     * 20.11.23: Was in TrafficWorlConfig. Now here a temp wrapper.
     */
    public static  List<Vehicle> getVehicleListByName(String vehiclelistname) {

        TrafficConfig vdefs = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("traffic-advanced"), new BundleResource("vehicle-definitions.xml"));
        return vdefs.getVehicleListByName(vehiclelistname);

    }


}
