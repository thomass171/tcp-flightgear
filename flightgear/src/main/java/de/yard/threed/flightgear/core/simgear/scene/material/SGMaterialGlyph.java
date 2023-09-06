package de.yard.threed.flightgear.core.simgear.scene.material;

import de.yard.threed.flightgear.core.simgear.SGPropertyNode;

/**
 * aus mat.[ch]xx
 * <p/>
 * Created by thomass on 08.08.16.
 */
public class SGMaterialGlyph {
    double _left;
    double _right;

    public SGMaterialGlyph(SGPropertyNode p) {
        _left = (p.getDoubleValue("left", 0.0));
        _right = (p.getDoubleValue("right", 1.0));
    }

    /**
     * Constructor for CppHashMap only
     */
    public SGMaterialGlyph() {
        
    }

    double get_left() {
        return _left;
    }

    double get_right() {
        return _right;
    }

    double get_width() {
        return _right - _left;
    }


}
