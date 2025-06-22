package de.yard.threed.trafficadvanced;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.HttpBundleResolver;
import de.yard.threed.flightgear.TerraSyncBundleResolver;

/**
 * Configuration for bundles in bundlepool.
 * See also README.md for 'bundlepool'.
 */
public class AdvancedConfiguration {

    public static String BUNDLEPOOL_URL = "https://ubuntu-server.udehlavj1efjeuqv.myfritz.net/publicweb/bundlepool";

    public static void setAdvancedBundleResolver() {
        // "fgdatabasic","sgmaterial" and "TerraySync" in project are only a small subset. The external should have 'before' prio to load instead of subset.
        //  There is no way to add a file system resolver here, so use the external also for desktop. This has the benefit
        // of revealing loading problems also during development.
        Platform.getInstance().addBundleResolver(new HttpBundleResolver("fgdatabasic@" + AdvancedConfiguration.BUNDLEPOOL_URL), true);
        Platform.getInstance().addBundleResolver(new HttpBundleResolver("sgmaterial@" + AdvancedConfiguration.BUNDLEPOOL_URL), true);
        //30.1.24: The default TerraSyncBundleResolver points to "bundles" in webgl.
        Platform.getInstance().addBundleResolver(new TerraSyncBundleResolver(AdvancedConfiguration.BUNDLEPOOL_URL), true);

    }
}
