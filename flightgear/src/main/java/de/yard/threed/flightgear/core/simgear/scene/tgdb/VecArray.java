package de.yard.threed.flightgear.core.simgear.scene.tgdb;

import java.util.ArrayList;

public class VecArray<T> extends ArrayList<T> {
    public VecArray(int size) {
        while (size() < size) {
            add(null);
        }
    }
}
