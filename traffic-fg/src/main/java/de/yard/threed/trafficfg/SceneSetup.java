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
        // 32L from apt.dat. Not sure if heading fits exactly.
        // Elevation should be taken from scenery at runtime, but 78.05 could also be explicitly set.
        properties.put("initialLocation", "geo:50.85850600,  007.13874200 ,78.05");
        properties.put("initialHeading", "320");
        properties.put("initialVehicle", "bluebird");
        properties.put("scene", "de.yard.threed.trafficfg.apps.TravelSceneBluebird");
    }

    /**
     * 6.5.25: EHAM, runway 06. No elevation, so terrain is needed. Outside 'advanced' terrain will not be available, so a dummy tile is built (meanwhile wireframe water?).
     */
    public static void setupTravelSceneBluebirdForBluebirdFreeFlightFromEHAM(HashMap<String, String> properties) {

        properties.put("initialLocation", "geo:52.2878684, 4.73415315");
        properties.put("initialHeading", "57.8");
        properties.put("initialVehicle", "bluebird");
        properties.put("scene", "de.yard.threed.trafficfg.apps.TravelSceneBluebird");
    }

    /**
     * 6.5.25: EHAM, runway 06. No elevation, so terrain is needed. Outside 'advanced' terrain will not be available, so a dummy tile is built.
     */
    public static void setupTravelSceneForC172pFreeFlightFromEHAM(HashMap<String, String> properties) {

        properties.put("initialLocation", "geo:52.2878684, 4.73415315");
        properties.put("initialHeading", "57.8");
        properties.put("initialVehicle", "c172p");
        properties.put("scene", "de.yard.threed.trafficadvanced.apps.TravelScene");
    }

    /**
     * 16.6.25: EHAM
     */
    public static void setupSceneryViewSceneForEHAM(HashMap<String, String> properties) {

        properties.put("initialLocation", "geo:52.2878684, 4.73415315");
        properties.put("scene", "de.yard.threed.trafficadvanced.apps.AdvancedSceneryViewerScene");
    }
}
