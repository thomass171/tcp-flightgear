package de.yard.threed.flightgear.core.simgear.scene.model;

import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.structure.Binding;
import de.yard.threed.flightgear.core.simgear.structure.PrimitiveValue;
import de.yard.threed.flightgear.core.simgear.structure.SGExpression;
import de.yard.threed.flightgear.core.simgear.structure.SGUnaryExpression;

/**
 * aus animation.[hc]xx
 * <p>
 * Created by thomass on 05.01.17.
 */
public class SGPersonalityScaleOffsetExpression extends SGUnaryExpression {
    /*mutable*/ SGPersonalityParameter/*<double>*/ _scale;
    /*mutable*/ SGPersonalityParameter/*<double>*/ _offset;

    SGPersonalityScaleOffsetExpression(SGExpression/*<double>*/ expr, SGPropertyNode config, String scalename, String offsetname/*, double defScale/*= 1/*                                       double defOffset= 0*/) {
        super(/*<double>(*/expr);
        _scale = new SGPersonalityParameter(config, scalename/*.c_str()*/, 1/*defScale*/);
        _offset = new SGPersonalityParameter(config, offsetname/*.c_str()*/, 0/*defOffset*/);
    }

  /*??  void setScale(double scale) {
        _scale = scale;
    }

    void setOffset(double offset) {
        _offset = offset;
    }*/

    @Override
    public PrimitiveValue eval(/*double& value,*/Binding b) {
        _offset.shuffle();
        _scale.shuffle();
        // Woodoo FG-DIFF
        return new PrimitiveValue(_offset._var + (_scale._var*(getOperand().getValue(b).doubleVal)));
    }

    @Override
    public boolean isConst() {
        return false;
    }

}
