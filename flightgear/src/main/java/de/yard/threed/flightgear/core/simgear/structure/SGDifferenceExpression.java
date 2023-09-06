package de.yard.threed.flightgear.core.simgear.structure;

/**
 * Created by thomass on 24.01.17.
 */
//template<typename T>
class SGDifferenceExpression extends SGNaryExpression {
    public SGDifferenceExpression(SGExpression expr0, SGExpression expr1) {

        super(expr0, expr1);
    }

    public SGDifferenceExpression() {
        
    }

    @Override
    public PrimitiveValue eval(Binding b) {
        double value = getOperand(0).getValue(b).doubleVal;
        int sz = getNumOperands ();
        for (int i = 1; i < sz; ++i) {
            value -= getOperand(i).getValue(b).doubleVal;
        }
        return new PrimitiveValue(value);
    }
    // using SGNaryExpression<T>::getValue;
    // using SGNaryExpression<T>::getOperand;
}

