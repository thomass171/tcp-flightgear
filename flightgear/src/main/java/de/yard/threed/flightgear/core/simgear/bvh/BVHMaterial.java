package de.yard.threed.flightgear.core.simgear.bvh;

/**
 * Created by thomass on 08.08.16.
 */
public class BVHMaterial {
    // True if the material isType solid, false if it isType a fluid
    public boolean _solid;

    // the friction factor of that surface material
    public   double _friction_factor;

    // the rolling friction of that surface material
    public  double _rolling_friction;

    // the bumpiness of that surface material
    public   double _bumpiness;

    // the load resistance of that surface material
    public   double _load_resistance;
}
