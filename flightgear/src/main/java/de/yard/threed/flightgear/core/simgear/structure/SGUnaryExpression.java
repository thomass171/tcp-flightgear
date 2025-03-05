package de.yard.threed.flightgear.core.simgear.structure;

import de.yard.threed.flightgear.core.simgear.SGPropertyNode;

import java.util.Set;

/**
 * Created by thomass on 28.12.16.
 */


//template<typename T>
public abstract class SGUnaryExpression extends SGExpression {
    /*SGSharedPtr<SGExpression<T>>*/ SGExpression _expression;

    public SGUnaryExpression(SGExpression expression/*=0*/) {
        setOperand(expression);
    }

    public SGExpression getOperand() {
        return _expression;
    }

   /* SGExpression    getOperand() {
        return _expression;
    }*/

    void setOperand(SGExpression expression) {
        if (expression == null) {
            //expression = new SGConstExpression<T>(T());
            expression = new SGConstExpression(new PrimitiveValue(0.0));
        }
        _expression = expression;
    }

    @Override
    public boolean isConst() {
        return getOperand().isConst();
    }

    /*@Override
    SGExpression    simplify() {
        _expression = _expression.simplify();
        return SGExpression<T>::simplify ();
    }*/

    @Override
    public void collectDependentProperties(Set<SGPropertyNode> props) {
        _expression.collectDependentProperties(props);
    }

    @Override
    public String toString() {
        return "SGUnaryExpression expression=" + _expression + "("+ super.toString() + ")";
    }
}
