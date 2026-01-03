package de.yard.threed.trafficfg;

import de.yard.threed.trafficcore.GeoRoute;

import java.util.HashMap;

/**
 * Util for those use cases that are using a property based setup instead of a XML file.
 */
public class SceneSetup {

    public static String EHAM06 = "52.2878684, 4.73415315";
    // Currently intersects earth. Needs more altitude.
    public static String EDDK14LtoEHAM18L = "wp:50.8800381,7.1296996->takeoff:50.8764919,7.1348404->wp:50.8566037,7.1636556->wp:50.8480166,7.1594773->wp:50.8459351,7.1456370->wp:50.8524115,7.1357771->wp:52.3457417,4.8181967->wp:52.3522189,4.8080071->wp:52.3525042,4.7933074->wp:52.3464347,4.7824657->touchdown:52.3195264,4.7800279->wp:52.2908234,4.7774309";
    public static String EGPH06toEGPF05 = "wp:55.9442996,-3.3892534->takeoff:55.9467891,-3.3819122->wp:55.9607437,-3.3407320->wp:55.9696781,-3.3390629->wp:55.9758200,-3.3507778->wp:55.9743070,-3.3666025->wp:55.9204845,-3.9163328->wp:55.8329947,-4.4526218->wp:55.8314837,-4.4683891->wp:55.8371362,-4.4808215->wp:55.8461119,-4.4814731->touchdown:55.8647208,-4.4467436->wp:55.8799744,-4.4182359";

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
     * 6.5.25: EHAM, runway 06. No elevation, so terrain is needed. Outside 'advanced' terrain
     * will not be available, so a dummy tile is built (meanwhile wireframe water?).
     */
    public static void setupTravelSceneBluebirdForBluebirdFreeFlightFromEHAM(HashMap<String, String> properties) {

        properties.put("initialLocation", "geo:" + EHAM06);
        properties.put("initialHeading", "57.8");
        properties.put("initialVehicle", "bluebird");
        properties.put("scene", "de.yard.threed.trafficfg.apps.TravelSceneBluebird");
    }

    /**
     * 6.5.25: EHAM, runway 06. 'advanced', so terrain and c172p should be available.
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

    /**
     * The base scene for moving in scenery, not the historic top down viewer scene
     */
    public static void setupAdvancedScenerySceneForEHAM(HashMap<String, String> properties) {

        // above runway '06'
        properties.put("initialLocation", "geo:52.2878684, 4.73415315,300");
        properties.put("initialHeading", "60");
        properties.put("scene", "de.yard.threed.trafficadvanced.apps.AdvancedSceneryScene");

    }

    /**
     *
     */
    public static void setupBluebirdForRouteFromEDKB(HashMap<String, String> properties, Double maximumSpeed) {

        properties.put("initialRoute", GeoRoute.SAMPLE_EDKB_EDDK);
        if (maximumSpeed != null) {
            properties.put("vehicle.bluebird.maximumspeed", "" + maximumSpeed);
            properties.put("vehicle.bluebird.acceleration", "" + maximumSpeed * 0.05);
        }
        properties.put("initialVehicle", "bluebird");
        properties.put("scene", "de.yard.threed.trafficfg.apps.TravelSceneBluebird");
    }

    /**
     * For easy test of scenery load between EDDK and EHAM. Highspeed routed flight to EHAM (appx 3 minutes).
     * Currently intersects earth. Needs more altitude.
     */
    public static void setupBluebirdForRouteFromEDDKtoEHAM(HashMap<String, String> properties, Double maximumSpeed) {

        properties.put("initialRoute", EDDK14LtoEHAM18L);
        if (maximumSpeed != null) {
            properties.put("vehicle.bluebird.maximumspeed", "" + maximumSpeed);
            properties.put("vehicle.bluebird.acceleration", "" + maximumSpeed * 0.05);
        }
        properties.put("initialVehicle", "bluebird");
        properties.put("scene", "de.yard.threed.trafficadvanced.apps.TravelScene");
    }

    /**
     *
     */
    public static void setupTravelSceneForC172pFreeFlightFromEDDK14L(HashMap<String, String> properties) {

        properties.put("initialLocation", "geo:50.85850600, 007.13874200");
        properties.put("initialHeading", "320");
        properties.put("initialVehicle", "c172p");
        properties.put("scene", "de.yard.threed.trafficadvanced.apps.TravelScene");
    }

    /**
     *
     */
    public static void setupBluebirdForRouteFromEGPHtoEGPF(HashMap<String, String> properties, Double maximumSpeed) {

        properties.put("initialRoute", EGPH06toEGPF05);
        if (maximumSpeed != null) {
            properties.put("vehicle.bluebird.maximumspeed", "" + maximumSpeed);
            properties.put("vehicle.bluebird.acceleration", "" + maximumSpeed * 0.05);
        }
        properties.put("initialVehicle", "bluebird");
        properties.put("scene", "de.yard.threed.trafficadvanced.apps.TravelScene");
    }
}
