package de.yard.threed.trafficfg.fgadapter;

import de.yard.threed.core.StringUtils;
import de.yard.threed.engine.ecs.DefaultEcsSystem;
import de.yard.threed.flightgear.FlightGearProperties;
import de.yard.threed.flightgear.SGPropertyTreeResolver;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;

/**
 * No real ECS system because there is no FlightGearComponent. But an approach to embed FGs
 * initialization and globals into ECS more consistently.
 * <p>
 * 5.11.24: Merges "FGGlobals", ...
 7.11.24 not yet
 * <p>
 * Time dependent nodes of the global property tree are updated here.
 * t.b.c
 */
public class FlightGearSystem extends DefaultEcsSystem {

    FlightGearProperties flightGearProperties = new FlightGearProperties();

    @Override
    public void frameinit() {

        flightGearProperties.update();

    }
}
