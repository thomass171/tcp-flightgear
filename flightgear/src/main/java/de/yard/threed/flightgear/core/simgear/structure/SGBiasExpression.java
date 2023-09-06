package de.yard.threed.flightgear.core.simgear.structure;

/**
 * immer double statt PrimitiveType
 */

// template<typename T>
public class SGBiasExpression extends SGUnaryExpression {
    double _bias = 0;

    public SGBiasExpression(SGExpression expr, double bias)

    {
        super(expr);
        _bias = bias;
    }

    void setBias(double bias) {
        _bias = bias;
    }

    double getBias() {
        return _bias;
    }

    @Override
    public PrimitiveValue eval(/*T&value, simgear::expression::*/Binding b) {
        //value = _bias + getOperand().getValue(b);
        // immer double?
        return new PrimitiveValue(_bias + getOperand().getValue(b).doubleVal);
    }

/* erstmal nicht  public SGExpression simplify() {
        return (SGExpression) Util.notyet();
        /* if (_bias == 0)
        * return getOperand() .simplify();
        * return SGUnaryExpression<T>::simplify();
        * }* /
    }*/
}
