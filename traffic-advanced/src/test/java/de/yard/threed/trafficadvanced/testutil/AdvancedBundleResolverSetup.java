package de.yard.threed.trafficadvanced.testutil;

import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.flightgear.TerraSyncBundleResolver;
import de.yard.threed.flightgear.testutil.BundleResolverSetup;
import de.yard.threed.javacommon.DefaultResourceReader;
import de.yard.threed.outofbrowser.SimpleBundleResolver;
import de.yard.threed.trafficadvanced.AdvancedConfiguration;

public class AdvancedBundleResolverSetup extends BundleResolverSetup {

    @Override
    public void setupResolver(Configuration configuration, boolean addTerraSyncResolver) {
        // Some of the 'advanced' test cases still need 'traffic-fg'. So we keep the default
        // and extend it with higher prio resolver. But avoid resolving of limited TerraSync.
        new DefaultBundleResolverSetup().setupResolver(configuration, false);
        // just ignore flag addTerraSyncResolver
        AdvancedConfiguration.setAdvancedBundleResolver();

    }
}

