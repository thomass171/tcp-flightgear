package de.yard.threed.flightgear.core;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.flightgear.PositionInit;
import de.yard.threed.flightgear.core.flightgear.main.FGGlobals;
import de.yard.threed.flightgear.core.flightgear.main.Main;
import de.yard.threed.core.platform.Log;
import de.yard.threed.traffic.flight.FlightLocation;

/**
 * 30.9.19: Ist das noch aktuell? Fuer Aircraft Laden? Dafuer nicht, aber z.B. fuer FGTileMgr, FGGlobals,matlib?.
 */
public class FlightGearModuleBasic extends FlightGearModule {
    static Log logger = Platform.getInstance().getLog(FlightGearModuleBasic.class);
    public static boolean inited;

    public static void init(FlightLocation loc, String aircraftdir) {
        //27.3.18: Fuer Mehrfachaufruf (Tests) cleanen
        FGGlobals.globals = null;
        
        if (aircraftdir == null) {
            aircraftdir = "777";
        }
        //LoaderRegistry.addLoader("xml",new LoaderFactoryXml());
        //8.6.17: Der SceneryTileManager braucht das. Jetzt aber nicht mehr. 
        //LoaderRegistry.addLoader("stg",new LoaderFactoryStg());

        // fgmaininit macht auch den initAircraft
        // 27.6.17: Mal weglassen. Nee, zumindest dir muss fuer den Provider gesetzt sein.
        String[] argv = new String[]{
                //"--aircraft=777-200",
            //MA23    "--aircraft-dir=" + aircraftdir
        };
        if (Main.fgMainInit(argv.length, argv, false) != 0) {
            logger.error("Main.fgMainInit failed. Returning");

            return;
        }
        ////////////////////////////////////////////////////////////////////
        // Initialize the material manager
        ////////////////////////////////////////////////////////////////////
        //25.3.18 jetzt in scenery FGGlobals.globals.set_matlib(new SGMaterialLib());
        // das ist natuerlich irgendwie nicht sehr elegant, einfach ein Event dafuer zu nutzen. Aber naja.
        //Platform.getInstance().getEventBus().publish(new Event(EventType.EVENT_MATLIBCREATED, null));

        // Scenery braucht eine aktuelle Position
        if (loc != null) {
            // initial weit ueber Aequator. 
            PositionInit.initPositionFromGeod(loc);
        }
        //Das FGAircraftModel wurde urspruenglich auch als Subsstem angelegt. Obs das braucht ist unklar.
        //28.3.18: eher nicht
        //new FGAircraftModel();

        inited = true;
    }
}
