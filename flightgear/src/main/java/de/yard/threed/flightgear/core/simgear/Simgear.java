package de.yard.threed.flightgear.core.simgear;

import de.yard.threed.flightgear.core.flightgear.main.FGGlobals;

/**
 * Location for member of FG namespace 'simgear' that cannot find any other fitting location.
 * Keeps the FG names and accessability via static for intuitive readability
 */
public class Simgear {

    /**
     * From userdata.cxx
     *
     */
    public static SGPropertyNode getPropertyRoot() {
        return FGGlobals.getInstance().get_props();
    }
}
