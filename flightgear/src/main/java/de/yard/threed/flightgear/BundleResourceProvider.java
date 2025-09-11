package de.yard.threed.flightgear;

import de.yard.threed.core.resource.BundleResource;

/**
 * Locating relative resources across bundles.
 * 8.9.25: No need for a 'current' for resolving relative references like in findPath()? Not until now.
 * Created by thomass on 12.04.17.
 */
public interface BundleResourceProvider {
    BundleResource resolve(String resource/*, BundleResource current*/);
    boolean isAircraftSpecific();
}
