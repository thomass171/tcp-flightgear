package de.yard.threed.flightgear.core.simgear.structure;

/**
 * Immer double statt PrimitiveValue
 */

import de.yard.threed.core.Util;
import de.yard.threed.flightgear.core.simgear.math.SGMisc;

//* template<typename T>
public class SGClipExpression extends SGUnaryExpression/*<T>*/ {
    double _clipMin, _clipMax;

    public SGClipExpression(SGExpression expr) {
        super/*<T>*/(expr);
        Util.notyet();
        //_clipMin= (SGMisc<T>::min (-SGLimits<T>::max (), SGLimits<T>::min ())),
        //_clipMax = (SGLimits<T>::max ())

    }

    public SGClipExpression(SGExpression expr, double clipMin, double clipMax) {
        super(expr);
        _clipMin = clipMin;
        _clipMax = clipMax;
    }

    void setClipMin(double clipMin) {
        _clipMin = clipMin;
    }

    double getClipMin() {
        return _clipMin;
    }

    void setClipMax(double clipMax) {
        _clipMax = clipMax;
    }

    double getClipMax() {
        return _clipMax;
    }

    @Override
    public PrimitiveValue/* void*/ eval(/*PrimitiveValue value,*/ Binding b) {
        return new PrimitiveValue(SGMisc.clipD(getOperand().getValue(b).doubleVal, _clipMin, _clipMax));
        //value = SGMisc<T>::clip (getOperand().getValue(b), _clipMin, _clipMax);
    }

    /*erstmal nicht public SGExpression simplify() {
        return (SGExpression) Util.notyet();
            /*if (_clipMin <= SGMisc<T>::min (-SGLimits<T>::max (), SGLimits<T>::min ())&&
            *_clipMax >= SGLimits<T>::max ())
            *return getOperand().simplify();
            *return SGUnaryExpression<T>::simplify ();
            *}* /
        //using SGUnaryExpression<T>::getOperand;
    }*/
}
