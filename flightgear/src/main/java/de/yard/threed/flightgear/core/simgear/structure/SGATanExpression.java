package de.yard.threed.flightgear.core.simgear.structure;

/**
 * Created by thomass on 24.01.17.
 */
//  template<typename T>
public class SGATanExpression extends SGUnaryExpression {
    public SGATanExpression(SGExpression expr) {
        super(expr);
    }

    @Override
    public PrimitiveValue eval(Binding b) {
        double value = Math.atan(getOperand().getDoubleValue(b));
        return new PrimitiveValue(value);
    }

    //using SGUnaryExpression<T>::getOperand;
         

}
