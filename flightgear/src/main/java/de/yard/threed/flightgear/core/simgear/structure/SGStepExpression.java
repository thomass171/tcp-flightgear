package de.yard.threed.flightgear.core.simgear.structure;

import de.yard.threed.core.MathUtil2;
import de.yard.threed.flightgear.core.simgear.math.SGLimitsd;

/**
 * A generic in FG. Assume 'T' to be Double for now.
 */
public class SGStepExpression extends SGUnaryExpression/*<T>*/ {
    Double _step;
    Double _scroll;

    //  * <p>template<typename T>
    public SGStepExpression(SGExpression/* <T>* */ expr /*=0*/, double step, double scroll) {
        //*:SGUnaryExpression<T>(expr),
        super(expr);
        // _step(step),_scroll(scroll)
        _step = step;
        _scroll = scroll;
    }

    public SGStepExpression(SGExpression/* <T>* */ expr /*=0*/) {
        //*T&step=T(1),T&scroll =T(0))
        this(expr, 1.0, 0.0);
    }

    void setStep(Double step) {
        _step = step;
    }

    Double getStep() {
        return _step;
    }

    void setScroll(Double scroll) {
        _scroll = scroll;
    }

    Double getScroll() {
        return _scroll;
    }

    @Override
    public PrimitiveValue /*void*/ eval(/*Double value*/ /*simgear::expression::*/Binding b) {
        /*value =*/
        return apply_mods(getOperand().getValue(b));
    }

    //using SGUnaryExpression<T>::getOperand;

    PrimitiveValue/*T*/ apply_mods(PrimitiveValue property) {
        if (_step <= SGLimitsd.min) return property;

        // apply stepping of input value
        Double modprop = Math.floor(property.doubleVal / _step) * _step;

        // calculate scroll amount (for odometer like movement)
        Double remainder = property.doubleVal <= SGLimitsd.min
                ? -MathUtil2.fmod(property.doubleVal, _step) : (_step - MathUtil2.fmod(property.doubleVal, _step));
        if (remainder > SGLimitsd.min && remainder < _scroll)
            modprop += (_scroll - remainder) / _scroll * _step;
        return new PrimitiveValue(modprop);
    }

    @Override
    public String toString() {
        return "SGStepExpression step=" + _step + "("+ super.toString() + ")";
    }

}
