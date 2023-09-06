package de.yard.threed.flightgear.core.simgear.misc;

/**
 * Created by thomass on 30.05.16.
 */
public interface ResourceProvider {
    public SGPath resolve(String aResource, SGPath aContext);

}
