package de.yard.threed.trafficfg.fgadapter;

import de.yard.threed.engine.Scene;
import de.yard.threed.engine.ecs.DefaultEcsSystem;
import de.yard.threed.flightgear.core.flightgear.main.FGGlobals;

/**
 * Wrapper/Adapter for global property tree
 */
public class FlightGearProperties {

    public double elapsedsec = 0;

    public FlightGearProperties(){
        //TODO add root node?
    }

    public void update() {
        double tpf = Scene.getCurrent().getDeltaTime();
        FGGlobals.getInstance().get_props().getNode("/sim/time/elapsed-sec", true).setDoubleValue(elapsedsec);
        // windturbine tower should not rotate
        FGGlobals.getInstance().get_props().getNode("/environment/wind-from-heading-deg", true).setDoubleValue(20);
        // 5.10.2017: keep wind-speed constant doesn't work. Needs to change. Probably a kind of bug.
        FGGlobals.getInstance().get_props().getNode("/environment/wind-speed-kt", true).setDoubleValue(elapsedsec*40/*elapsedsec * 10 % 200*/);

        // not sure the calculation is correct. But we can just define 'elapsedsec' is just the sum of deltas.
        elapsedsec += tpf;
    }
}
