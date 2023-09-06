package de.yard.threed.flightgear.core.simgear.structure;

/**
 * Created by thomass on 24.01.17.
 */
//template<typename T>
class SGDivExpression extends SGBinaryExpression {
    public SGDivExpression(SGExpression expr0, SGExpression expr1) {
        super(expr0, expr1);
    }

    @Override
    public PrimitiveValue eval(Binding b) {
        // div auf double?
        long value = ((long)Math.round(getOperand(0).getValue(b).doubleVal)) / ((long)Math.round(getOperand(1).getValue(b).doubleVal));
        return new PrimitiveValue(value);
    }
//     using SGBinaryExpression<T>::getOperand;
}

