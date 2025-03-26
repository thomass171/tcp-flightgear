package de.yard.threed.trafficfg;

import java.util.HashMap;

/**
 * Util for those use cases that are using a property based setup instead of a XML file.
 */
public class SceneSetup {

    /**
     * 6.3.25: Either choose a route in cockpit or free flight from EDDKs 32L
     */
    public static void setupForBluebirdFreeFlight(HashMap<String, String> properties) {
        // 32L from apt.dat. Not sure if heading fits exactly. Elevation should be taken from scenery at runtime.
        properties.put("initialLocation", "geo:50.85850600,  007.13874200 ,60.05");
        properties.put("initialHeading", "320");
        properties.put("initialVehicle", "bluebird");
        properties.put("scene", "de.yard.threed.trafficfg.apps.TravelSceneBluebird");
    }
}
