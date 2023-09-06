package de.yard.threed.flightgear.core.simgear.structure;

/**
 * Created by thomass on 24.01.17.
 */
// template<typename T>
public class SGSumExpression extends SGNaryExpression/*<T>*/ {
    public SGSumExpression(SGExpression expr0, SGExpression expr1) {
        super(expr0, expr1);
    }

    public SGSumExpression() {
        super();
    }

    @Override
    public PrimitiveValue eval(Binding b) {
        double value = (0);
        int sz = /*SGNaryExpression<T>::*/getNumOperands();
        for (int i = 0; i < sz; ++i) {
            value += getOperand(i).getValue(b).doubleVal;
        }
        return new PrimitiveValue(value);
        //* using SGNaryExpression<T>::getValue;
        //* using SGNaryExpression<T>::getOperand;
    }
}
