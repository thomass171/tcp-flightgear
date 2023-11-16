package de.yard.threed.flightgear;

import de.yard.threed.core.resource.BundleResource;

/**
 * Locating relative resources across bundles.
 * Created by thomass on 12.04.17.
 */
public interface BundleResourceProvider {
    BundleResource resolve(String resource);
    boolean isAircraftSpecific();
}
