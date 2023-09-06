package de.yard.threed.flightgear.core.simgear.structure;

/**
 * Created by thomass on 07.12.15.
 */
public class SGException extends java.lang.Exception {
    public SGException(String s) {
        super(s);
    }

    public SGException(String s, java.lang.Exception e) {
        super(s,e);
    }
}
