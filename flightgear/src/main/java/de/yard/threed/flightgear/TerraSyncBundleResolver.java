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
            // bucket or model bundle
            String effname = StringUtils.substringAfter(bundleName, "-");
            /*14.9.21 if (perhttp) {
                if (customTerraSync) {
                    return "bundles/TerraSync";
                }else{
                    return "TerraSync";
                }
            }*/
            if (FlightGearSettings.customTerraSync) {
                //return Platform.getInstance().bundledir + "/TerraSync";
                ResourcePath bundlePath = new ResourcePath(basePath + "/TerraSync");
                Platform.getInstance().getLog(TerraSyncBundleResolver.class).debug("bundlePath=" + bundlePath.getPath());
                return bundlePath;
            } else {
                // 25.7.21: Dieser Zweig soll wohl seit 2018 gar nicht mehr genutzt werden.
                String fghome = Platform.getInstance().getConfiguration().getString("FG_HOME");
                if (fghome == null) {
                    //26.7.21:Besser aussteigen als nachher ewig die URsache zu suchen
                    throw new RuntimeException("fghome is null");
                }
                Util.nomore();
                return null;//14.9.21 fghome + "/TerraSync";
            }

        }
        /*30.1.18: normales Bundle  if (bundlename.equals(SGMaterialLib.BUNDLENAME) || StringUtils.startsWith(bundlename, BundleRegistry.FGROOTCOREBUNDLE)) {
            if (perhttp) {
                return "fg_root";
            }
            String fgroot = Platform.getInstance().getSystemProperty("FG_ROOT");
            if (fgroot == null){
                //loggen geht hier nicht.
                //System.out.println("FG_ROOT not set as system property");
            }
            return fgroot;
        }
       if (StringUtils.startsWith(bundlename, BundleRegistry.FGHOMECOREBUNDLE)) {
            if (perhttp) {
                return "fg_home";
            }
            String fghome = Platform.getInstance().getSystemProperty("FG_HOME");
            if (fghome == null){
                //loggen geht hier nicht.
                //System.out.println("FG_HOME not set as system property");
            }
            return fghome;
        }*/
        // Dann ist es wohl ein Standardbundle oder sonstwas fuer andere Resolver.
        return null;

    }
}
