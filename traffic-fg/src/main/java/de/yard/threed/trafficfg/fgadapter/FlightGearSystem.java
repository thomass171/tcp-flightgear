package de.yard.threed.trafficfg.fgadapter;

import de.yard.threed.engine.Scene;
import de.yard.threed.engine.ecs.DefaultEcsSystem;
import de.yard.threed.flightgear.core.flightgear.main.FGGlobals;

/**
 * No real ECS system because there is no FlightGearComponent. But an approach to embed FGs
 * initialization and globals into ECS more consistently
 * t.b.c
 */
public class FlightGearSystem extends DefaultEcsSystem {

    FlightGearProperties flightGearProperties = new FlightGearProperties();

    @Override
    public void frameinit() {

        flightGearProperties.update();

    }
}
