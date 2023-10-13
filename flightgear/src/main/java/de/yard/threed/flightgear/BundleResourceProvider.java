package de.yard.threed.flightgear;

import de.yard.threed.core.resource.BundleResource;

/**
 * Created by thomass on 12.04.17.
 */
public interface BundleResourceProvider {
    BundleResource resolve(String resource/*, Bundle currrentbundle*/);
    boolean isAircraftSpecific();
}
