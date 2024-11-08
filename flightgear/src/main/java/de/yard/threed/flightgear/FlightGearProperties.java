package de.yard.threed.flightgear;

import de.yard.threed.core.StringUtils;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.ecs.DefaultEcsSystem;
import de.yard.threed.flightgear.core.flightgear.main.FGGlobals;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;

/**
 * Wrapper/Adapter for global property tree.
 * Replaces FGGlobals:props and the global accessor "FGGlobals.getInstance().get_props()".
 * The original FG single tree is split into several.
 * Does not contain aircraft trees?
 * 7.11.24: Splitting the original single tree only sounds good. It requires many/many changes. Eg.  SGCondition might access all parts of all trees
 * for evaluation. So better stay with FGGlobals.prop_root.
 */
public class FlightGearProperties /*7.11.24 implements SGPropertyTreeResolver*/ {

    // The original FG single tree is split into several. There might be multiple "/environment" trees
    // per location in future.
    /*SGPropertyNode environmentProperties;
    SGPropertyNode simProperties;*/

    public double elapsedsec = 0;
    //SGPropertyNode root;
    public static double DEFAULT_WIND_SPEED_KT = 26.0;
    // wind from north west as default
    public static double DEFAULT_WIND_FROM_HEADING_DEG = 290.0;

    public static void setOurDefaults() {
        //root = new SGPropertyNode(rootName);
        /*/*7.11.24 split is too much effort
        environmentProperties = new SGPropertyNode("");
        SGPropertyNode n = environmentProperties.getNode("environment", true);
        add(n, "wind-from-heading-deg", DEFAULT_WIND_FROM_HEADING_DEG);
        add(n, "wind-speed-kt", DEFAULT_WIND_SPEED_KT);

        simProperties = new SGPropertyNode("/sim");
        add(simProperties, "time/elapsed-sec", 0.0);
        */
        FGGlobals.getInstance().get_props().getNode("/environment/wind-from-heading-deg", true).setDoubleValue(DEFAULT_WIND_FROM_HEADING_DEG);
        FGGlobals.getInstance().get_props().getNode("/environment/wind-speed-kt", true).setDoubleValue(DEFAULT_WIND_SPEED_KT);
        SGPropertyNode node = FGGlobals.getInstance().get_props();
    }

    /**
     * 7.11.24 static until we might have instances(??)
     */
    public void update() {
        double tpf = Scene.getCurrent().getDeltaTime();
        FGGlobals.getInstance().get_props().getNode("/sim/time/elapsed-sec", true).setDoubleValue(elapsedsec);
        /*5.11.24
        // windturbine tower should not rotate
        FGGlobals.getInstance().get_props().getNode("/environment/wind-from-heading-deg", true).setDoubleValue(20);
        // 5.10.2017: keep wind-speed constant doesn't work. Needs to change. Probably a kind of bug.
        FGGlobals.getInstance().get_props().getNode("/environment/wind-speed-kt", true).setDoubleValue(elapsedsec * 40/*elapsedsec * 10 % 200* /);
*/
        // not sure the calculation is correct. But we can just define 'elapsedsec' is just the sum of deltas.
        elapsedsec += tpf;

        //environmentProperties.update();
        //simProperties.update();

    }

    private void add(SGPropertyNode root, String name, double value) {
        SGPropertyNode n = root.getNode(name, true);
        n.setDoubleValue(value);
    }

   /*7.11.24 split is too much effort @Override.
   But we need a kind of lookup anyway.
   */
    public static SGPropertyNode resolve(String inputPropertyName, SGPropertyNode defaultRoot) {

        if (StringUtils.startsWith(inputPropertyName, "/environment")) {
            return FGGlobals.getInstance().get_props().getNode(inputPropertyName, true);
            //return environmentProperties.getNode(inputPropertyName, true);
        }
        if (StringUtils.startsWith(inputPropertyName, "/sim")) {
            return FGGlobals.getInstance().get_props().getNode(inputPropertyName, true);
            //return simProperties.getNode(inputPropertyName, true);
        }

        return defaultRoot.getNode(inputPropertyName, true);
    }
}
