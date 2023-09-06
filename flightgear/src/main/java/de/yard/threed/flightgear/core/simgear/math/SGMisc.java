package de.yard.threed.flightgear.core.simgear.math;

/**
 * 
 * Created by thomass on 11.08.16.
 */
public class SGMisc {
    // normalize the value to be in a range between [min, max[
    public static double normalizePeriodicD(double min, double max, double value) {
        double range = max - min;
        // stimmt das mit dem min? 4.7.18: Eher nicht. MIN_VALUE ist psitiov!
        // 18.7.18: Doch, ich denke das stimmt. Es wird ja range geprüft auf quasi min=max
        // Das deckt sich auch mit der C++ numeric_limits Definition fuer floats
        // 15.10.18:MA16: Float->Double
        if (range < java.lang.Double.MIN_VALUE/*SGLimits<T>::min()*/) {
            return min;
        }
        double normalized = value - range * Math.floor((value - min) / range);
        // two security checks that can only happen due to roundoff
        if (normalized <= min)
            return min;
        if (max <= normalized)
            return min;
        return normalized;
    }

    // clip the value of a to be in the range between and including _min and _max
    public static double clipD(double a, double _min, double _max) {
        return Math.max(_min, Math.min(_max, a));
    }


}
