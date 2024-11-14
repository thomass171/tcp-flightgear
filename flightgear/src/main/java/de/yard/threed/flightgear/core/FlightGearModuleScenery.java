package de.yard.threed.flightgear.core;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.flightgear.core.flightgear.main.FGGlobals;
import de.yard.threed.flightgear.core.flightgear.main.FGProperties;
import de.yard.threed.flightgear.core.flightgear.scenery.FGScenery;
import de.yard.threed.flightgear.core.flightgear.scenery.FGTileMgr;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterialLib;
import de.yard.threed.flightgear.core.simgear.scene.model.SGModelLib;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;


/**
 * 8.8.20: Braucht der ein FlightGearModuleBasic? Scheinbar ja, ist eigentlich auch klar.
 */
public class FlightGearModuleScenery extends FlightGearModule {
    static Log logger = Platform.getInstance().getLog(FlightGearModuleScenery.class);
    public static boolean inited;
    // FlightGear scenery manager
        /*SGSharedPtr<*/ static FGScenery _scenery;

    // Tile manager
       /* SGSharedPtr<*/ static FGTileMgr _tile_mgr;

    // Material properties library
        /*SGSharedPtr<*/ static SGMaterialLib matlib;

    static FlightGearModuleScenery instance;

    // 6.2.23 Event von tcp-22 nach hier moved
    public static EventType EVENT_MATLIBCREATED = EventType.register(-22, "EVENT_MATLIBCREATED");

    public static FlightGearModuleScenery getInstance() {
        return instance;
    }

    public static void init(boolean terrainonly, boolean forBtgConversion) {
        instance= new FlightGearModuleScenery(terrainonly, forBtgConversion);
    }
    
    FlightGearModuleScenery(boolean terrainonly, boolean forBtgConversion) {
        logger.info("Creating instance");
        ////////////////////////////////////////////////////////////////////
        // Initialize the material manager
        ////////////////////////////////////////////////////////////////////
        /*FGGlobals.globals.*/
        set_matlib(new SGMaterialLib());
        // das ist natuerlich irgendwie nicht sehr elegant, einfach ein Event dafuer zu nutzen. Aber naja.
        // 17.9.23: Seems outdated. matlib should only be needed for btg to gltf conversion.
        Platform.getInstance().getEventBus().publish(new Event(EVENT_MATLIBCREATED, null));

        SGModelLib.init(FGGlobals.globals.get_fg_root(), FGGlobals.globals.get_props());

        //TimeManager* timeManager = (TimeManager*) globals->get_subsystem("time");
        //timeManager->init();

        ////////////////////////////////////////////////////////////////////
        // Initialize the TG scenery subsystem.
        ////////////////////////////////////////////////////////////////////

        /*FGGlobals.globals.*/
        set_scenery(new FGScenery());
         get_scenery().init();
        get_scenery().bind();
        set_tile_mgr(new FGTileMgr(terrainonly));

        //SGTimeStamp st;
        //st.stamp();
        //24.3.18 FgInit.fgCreateSubsystems(false/*isReset*/);
        String mpath = FGProperties.fgGetString("/sim/rendering/materials-file");

        mpath = "Materials/regions/materials.xml";
        FGProperties.fgSetString("/sim/startup/season", "summer");
        if (!/*FGGlobals.globals.*/get_matlib().load(/*FGGlobals.globals.get_fg_root(),*/ mpath, FGGlobals.globals.get_props(), forBtgConversion)) {
            throw new /*SGIO*/RuntimeException("Error loading materials file" + mpath);
        }
        logger.info("Creating subsystems took:");// + st.elapsedMSec());
        inited = true;
    }

    public FGScenery get_scenery() {
        return _scenery/*.get()*/;
    }

    public void set_scenery(FGScenery s) {
        _scenery = s;
    }

    public FGTileMgr get_tile_mgr() {
        return _tile_mgr/*.get()*/;
    }

    public void set_tile_mgr(FGTileMgr t) {
        _tile_mgr = t;
    }

    public SGMaterialLib get_matlib() {
        return matlib;
    }


    public void set_matlib(SGMaterialLib m) {
        matlib = m;
    }
}
