package de.yard.threed.flightgear.core.simgear;

/**
 * Speziell fuer CppHashMap
 * Created by thomass on 10.08.16.
 */
public abstract class CppFactory<T> {
    public abstract T createInstance();
}
