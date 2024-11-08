package de.yard.threed.flightgear;

import de.yard.threed.core.StringUtils;
import de.yard.threed.flightgear.core.flightgear.main.FGGlobals;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;

/**
 * 5.11.24 Since we don't have one single property tree like FG, we need
 * a kind of lookup.
 * 7.11.24:Even we still use a single tree, a resolver is needed for animations
 */
public interface SGPropertyTreeResolver {
    SGPropertyNode resolve(String inputPropertyName, SGPropertyNode defaultRoot);
}
