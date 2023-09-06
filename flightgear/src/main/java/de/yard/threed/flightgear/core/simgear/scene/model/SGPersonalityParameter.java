package de.yard.threed.flightgear.core.simgear.scene.model;

import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.math.SGRandom;

/**
 * aus persparam.[ch]xx
 * <p>
 * tricky wegen operator override. double instead of generic.
 * <p>
 * Created by thomass on 05.01.17.
 */
public class SGPersonalityParameter {
    double/*T*/ _var, _min, _max;

    SGPersonalityParameter(SGPropertyNode props, String name, double/*T*/ defval) {
        _var = defval;
        _min = (defval);
        _max = (defval);
        SGPropertyNode node = props.getNode(name);
        if (node != null) {
            SGPropertyNode rand_n = node.getNode("random");
            if (rand_n != null) {
                _min = getNodeValue(rand_n, "min", /*(T)*/0);
                _max = getNodeValue(rand_n, "max", /*(T)*/1);
                shuffle();
            } else {
                _var = _min = _max = getNodeValue(props, name, defval);
            }
        }
    }

    /* SGPersonalityParameter<T> &operator=( T v ) { _var = v; return *this; }
     SGPersonalityParameter<T> &operator+=( T v ) { _var += v; return *this; }
     SGPersonalityParameter<T> &operator-=( T v ) { _var -= v; return *this; }*/

    SGPersonalityParameter add(double v){
        _var += v;
        return this;
    }

    SGPersonalityParameter multiply(double v){
        _var *= v;
        return this;
    }

    /*T*/double shuffle() {
        return (_var = _min + SGRandom.sg_random() * (_max - _min));
    }

    /*T*/double value() {
        return _var;
    }

    /*T*/double getNodeValue(SGPropertyNode props, String name, /*T*/double defval) {
        return props.getDoubleValue(name, defval);
    }
    //operator T() const { return _var; }

}
