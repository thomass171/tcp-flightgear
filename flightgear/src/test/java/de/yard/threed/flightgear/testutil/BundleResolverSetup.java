package de.yard.threed.flightgear.testutil;

import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.flightgear.TerraSyncBundleResolver;
import de.yard.threed.javacommon.DefaultResourceReader;
import de.yard.threed.outofbrowser.SimpleBundleResolver;

public abstract class BundleResolverSetup {
    abstract public void setupResolver(Configuration configuration, boolean addTerraSyncResolver);

    public static class DefaultBundleResolverSetup extends BundleResolverSetup {

        @Override
        public void setupResolver(Configuration configuration, boolean addTerraSyncResolver) {
            if (addTerraSyncResolver) {
                Platform.getInstance().addBundleResolver(new TerraSyncBundleResolver(configuration.getString("HOSTDIRFG") + "/bundles"));
            }
            Platform.getInstance().addBundleResolver(new SimpleBundleResolver(configuration.getString("HOSTDIRFG") + "/bundles", new DefaultResourceReader()));
        }
    }
}


