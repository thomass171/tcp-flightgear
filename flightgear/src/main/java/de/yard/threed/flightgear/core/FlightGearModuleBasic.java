package de.yard.threed.flightgear.core;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.flightgear.PositionInit;
import de.yard.threed.flightgear.core.flightgear.main.FGGlobals;
import de.yard.threed.flightgear.core.flightgear.main.Main;
import de.yard.threed.core.platform.Log;
import de.yard.threed.traffic.flight.FlightLocation;

/**
 * 30.09.19: Needed for FGTileMgr, FGGlobals,matlib?, but not for loading an (initial) aircraft
 * 10.12.25: Also for initing and starting SoundManager
 */
public class FlightGearModuleBasic extends FlightGearModule {
    static Log logger = Platform.getInstance().getLog(FlightGearModuleBasic.class);
    public static boolean inited;

    public static void init(FlightLocation loc, String aircraftdir) {
        //27.3.18: cleanup (important in tests)
        FGGlobals.globals = null;

        //8.6.17: No longer add XML or STG loader to LoaderRegistry

        // 27.6.17: fgmaininit also does init Aircraft, but we don't load an aircraft during init like FG. So no need for setting an aircraft dir.
        String[] argv = new String[]{
        };
        if (Main.fgMainInit(argv.length, argv, false) != 0) {
            logger.error("Main.fgMainInit failed. Returning");

            return;
        }
        //25.3.18 FG Initializes the material manager here. We once published EventType.EVENT_MATLIBCREATED, but meanwhile we do ...

        // Scenery needs current position
        if (loc != null) {
            // might be high above equator.
            PositionInit.initPositionFromGeod(loc.coordinates);
        }
        // 28.3.18: FGAircraftModel once was a subsystem. Apparently we don't need it (at least for now)
        //new FGAircraftModel();

        // 11.12.25: Not sure whether it is the best location here for activating
        //already in FGGlobals FGGlobals.getInstance().sgSoundMgr.activate();
        inited = true;
        logger.info("inited");
    }
}
