package de.yard.threed.flightgear.core.simgear.structure;

import de.yard.threed.flightgear.core.simgear.math.SGInterpTable;

/**
 * Created by thomass on 28.12.16.
 */
//template<typename T>
public class SGInterpTableExpression extends SGUnaryExpression/*<T>*/ {
    SGInterpTable _interpTable;

    public SGInterpTableExpression(SGExpression/*<T>*/ expr, SGInterpTable interpTable) {
        super(expr);
        _interpTable = interpTable;
    }

    /*void*/
    @Override
    public PrimitiveValue eval(/*T& value,*/   Binding b) {
        if (_interpTable != null) {
            return new PrimitiveValue(_interpTable.interpolate(getOperand().getValue(b).doubleVal));
        }
        return new PrimitiveValue(0);
        //using SGUnaryExpression<T>::getOperand;

    }

    @Override
    public String toString(){
        return "SGInterpTable "+super.toString();
    }

}
