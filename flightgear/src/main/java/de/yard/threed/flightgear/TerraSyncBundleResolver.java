package de.yard.threed.flightgear;

import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Util;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleResolver;
import de.yard.threed.core.resource.ResourcePath;

import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.engine.platform.common.Settings;

public class TerraSyncBundleResolver extends BundleResolver {

    public static String TERRAYSYNCPREFIX = "Terrasync-";
    String basePath;

    /**
     * Can be used with http and file system the same way.
     */
    public TerraSyncBundleResolver(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public ResourcePath resolveBundle(String bundleName) {
        if (StringUtils.startsWith(bundleName, TERRAYSYNCPREFIX)) {
            ResourcePath bundlePath = new ResourcePath(basePath + "/TerraSync");
            Platform.getInstance().getLog(TerraSyncBundleResolver.class).debug("bundlePath=" + bundlePath.getPath());
            return bundlePath;
        }
        // Once (pre 2018) was checking FG_ROOT and FG_HOME also

        // Probably a standard bundle that is resolved by some other resolver. Forward to next resolver.
        return null;

    }
}
