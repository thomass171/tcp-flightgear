package de.yard.threed.flightgear.core.simgear.structure;

/**
 * immer double
 * Created by thomass on 28.12.16.
 */
// template<typename T>
public class SGScaleExpression extends SGUnaryExpression/*<T>*/ {
    double _scale = 1;

    public SGScaleExpression(SGExpression expr, double scale) {
        super(expr);
        _scale = scale;
    }

    /**
     * SGScaleExpression()
     * :SGUnaryExpression<T> (expr), _scale(scale)
     * {
     * }
     */

    void setScale(double scale) {
        _scale = scale;
    }

    double getScale() {
        return _scale;
    }

    @Override
    public PrimitiveValue/*void*/ eval(/*T& value,   simgear::expression::*/Binding b) {
        // immer double?
        double value = _scale * getOperand() .getValue(b).doubleVal;
        return new PrimitiveValue(value);
    }

   /*erstnal nicht public SGExpression simplify() {
        return (SGExpression) Util.notyet();
             if (_scale == 1)
             return getOperand() .simplify();
             return SGUnaryExpression<T>::simplify();
             }
    }*/

}
