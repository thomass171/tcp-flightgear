package de.yard.threed.flightgear.core.simgear.structure;

/**
 * Created by thomass on 28.12.16.
 */
/// Constant value expression
//template<typename T>
public class SGConstExpression extends SGExpression {
    /*T*/ PrimitiveValue _value;

    public SGConstExpression(PrimitiveValue value) {
        _value = value;
    }

    void setValue(  /*T&*/PrimitiveValue value) {
        _value = value;
    }

    public PrimitiveValue getValue(Binding binding /*= 0*/) {
        return _value;
    }

    @Override
    public PrimitiveValue eval(/*T& value,*/Binding b) {
        return /*value =*/ _value;
    }

    @Override
    public boolean isConst() {
        return true;
    }


    /*template<typename T>SGExpression<T>*        SGExpression<T>::*/
    public SGExpression simplify() {
        if (isConst())
            return new SGConstExpression(getValue(null));
        return this;
    }

}
