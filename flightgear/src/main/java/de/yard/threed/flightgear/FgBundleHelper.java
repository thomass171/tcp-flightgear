package de.yard.threed.flightgear;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;

import java.util.ArrayList;
import java.util.List;

/**
 * 13.10.23: Extracted from BundleRegistry.
 */
public class FgBundleHelper {

    Log logger= Platform.getInstance().getLog(FgBundleHelper.class);

    //Locating relative resources across bundles.
    static private List<BundleResourceProvider> providerlist = new ArrayList<BundleResourceProvider>();

    /**
     *
     *
     * 1) Current context(bundle)
     * 2) search provider list
     * <p>
     * Returns null if resource not found.
     * context isType current "location" in a bundle.
     * Da muss aber der Pfad drin gesetzt sein. Da kann man auch direkt den Pfad uebergeben. TODO
     * <p>
     * // FG hat im CurrentAircraftDirProvider einen Nebeneffekt, der absolute Pfade beachtet. Ob das Absicht ist?
     * Es ist nicht erkennbar, wo FG absolute Pfade abdeckt. Darum hier extra eingebaut. Das gibts in FG nicht.
     * 12.6.17: Ist das nicht jetzt über die absolute Suche im Bundle geloest?
     * 4.1.18: Wenn in einem Bundle ein "ac" gesucht wird, muss der exists auch bei vorliegendem gltf true liefern.
     */
    public static BundleResource findPath(String resource, BundleResource current/*currentbundle*/) {
        if (current != null && current.bundle != null) {
            //TODO improved path append
            /*if (current.getPath()==null){
                throw new RuntimeException("no path");
            }*/
            BundleResource br = new BundleResource(current.getPath(), resource);
            if (BundleRegistry.bundleexists(current.bundle, br)) {
                br.bundle = current.bundle;
                return br;
            }
            //12.6.17: absolute Suche im Bundle
            br = new BundleResource(resource);
            if (BundleRegistry.bundleexists(current.bundle, br)) {
                br.bundle = current.bundle;
                return br;
            }
        }

        // TODO fileseparator
      /*  if (StringUtils.indexOf(aResource, "/") != -1) {
            SGPath r = new SGPath(aResource);
            if (r.exists()) {
                return r;
            }
        }*/

        for (BundleResourceProvider rp : providerlist) {
            BundleResource path = rp.resolve(resource/*, null*/);
            if (path != null) {
                return path;
            }
        }

        return null;
    }

    public static void addProvider(BundleResourceProvider provider) {
        providerlist.add(provider);
    }

    public static void removeAircraftSpecific() {
        for (int i = providerlist.size() - 1; i >= 0; i--) {
            if (providerlist.get(i).isAircraftSpecific()) {
                providerlist.remove(i);
            }
        }
    }

    /**
     * Only for testing
     * @return
     */
    public static List<BundleResourceProvider> getProvider() {
        return providerlist;
    }

    public static void clear() {
        //30.9.19: Auch provider
        providerlist.clear();
        // logger.debug("Bundles cleared");
    }

}
