package de.yard.threed.flightgear.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Emulation of C++ std utils
 *
 */
public class CppStd  {
    public static  <T> T max_element(List<T> l, Comparator<T> comparator) {
        T max = l.get(0);
        for (int i = 1; i < l.size(); i++) {
            if (comparator.compare(l.get(i), max) > 0) {
                max = l.get(i);
            }
        }
        return max;
    }
 
}
