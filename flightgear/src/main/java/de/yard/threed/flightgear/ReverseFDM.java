package de.yard.threed.flightgear;

import de.yard.threed.engine.ecs.VelocityComponent;
import de.yard.threed.flightgear.core.flightgear.main.FGGlobals;
import de.yard.threed.flightgear.core.flightgear.main.FGProperties;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.ecs.FgAnimationComponent;

/**
 * A simple "reverse" FlightDynamicModel (See README.md)
 * Might be used in FgVehicleLoaderResult
 */
public class ReverseFDM {

    /**
     * For general properties (eg. those from Options.java). Aircraft specific properties are synced in FgVehicleLoaderResult
     * Only useful until there is only one FG vehicle.
     */
    public static void syncGlobalPropertiesByVehicleSpeed(double speedms, double heading, double pitch, double yaw) {

        double rpm = speedms * 40;

        SGPropertyNode root = FGGlobals.globals.get_props();

        root.setDoubleValue("/orientation/heading-deg", 9999.0);
        root.setDoubleValue("/orientation/roll-deg", 0.0);
        root.setDoubleValue("/orientation/pitch-deg", 0.424);

        // Velocities
        /*FGProperties.fgSetDouble("/velocities/uBody-fps", 0.0);
        FGProperties.fgSetDouble("/velocities/vBody-fps", 0.0);
        FGProperties.fgSetDouble("/velocities/wBody-fps", 0.0);
        FGProperties.fgSetDouble("/velocities/speed-north-fps", 0.0);
        FGProperties.fgSetDouble("/velocities/speed-east-fps", 0.0);
        FGProperties.fgSetDouble("/velocities/speed-down-fps", 0.0);*/
        root.setDoubleValue("/velocities/airspeed-kt", speedms);
        root.setDoubleValue("/engines/active-engine/rpm", rpm);

    }
}
