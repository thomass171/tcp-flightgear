package de.yard.threed.trafficfg.config;

import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.traffic.config.VehicleConfigDataProvider;
import de.yard.threed.traffic.config.VehicleDefinition;
import de.yard.threed.traffic.config.XmlVehicleDefinition;

import java.util.List;

public class ConfigHelper {

    /**
     * 4.12.23:Additional helper
     */
    public static VehicleDefinition getVehicleConfig(List<NativeNode> vds, String name) {
        VehicleConfigDataProvider vcdp = new VehicleConfigDataProvider(
                XmlVehicleDefinition.convertVehicleDefinitions(vds));

        List<VehicleDefinition> vehicleDefinitions = vcdp.findVehicleDefinitionsByName(name);
        return vehicleDefinitions.get(0);
        //24.11.23 return ConfigHelper.getVehicleConfig(tw.tw, name);
    }
}
