package de.yard.threed.flightgear.core;

/**
 * Eine Abstraktionsebene um das Zerlegen der FG Components und die Zurodnung zu ECS zu erleichern.
 * Auch um die Sybsystems und  init() sauber zuzuordnen.
 *
 * 30.9.19: Nach wie vor erforderlich, weil z.B. FGTileMgr FGGlobals verwendet.
 *
 * Created on 25.03.18.
 */
public abstract class FlightGearModule {
    //public abstract void init(FlightLocation loc, String aircraftdir);
}
