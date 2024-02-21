package de.yard.threed.flightgear;

import de.yard.threed.core.ModelBuildDelegate;
import de.yard.threed.core.Util;
import de.yard.threed.core.platform.Config;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.ResourceLoader;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.engine.ModelFactory;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.engine.platform.common.ModelLoader;

import static de.yard.threed.engine.platform.EngineHelper.LOADER_APPLYACPOLICY;

public class FgModelHelper {
    static Log logger = Platform.getInstance().getLog(FgModelHelper.class);

    /**
     * Load ac/gltf. Not for XML.
     * Extracted from ModelLoader.
     */
    public static void buildNativeModel(ResourceLoader resourceLoader, ResourcePath opttexturepath, ModelBuildDelegate modeldelegate, int options) {
        // die ACPolicy nutze ich wegen Einheitlichkeit immer, nicht nur in FG
        // 10.4.21: Brauchts die Option noch? Es gibt doch ein Plugin
        // 12.2.24: Its really needed. GLTFs converted from ac need a special rotation that regular gltf do not need. The plugin is triggered by this option.
        if (resourceLoader.nativeResource.getFullName().endsWith("ac")) {
            options |= LOADER_APPLYACPOLICY;
        }
        // Das Filemapping greift nur, wenn das acpp/gltf auch existiert.
        Platform.getInstance().buildNativeModelPlain(mapFilename(resourceLoader, true, options), opttexturepath, modeldelegate, options);
    }

    /**
     * 18.10.23: ac->gltf name mapping and policy setting now here.
     */
    public static SceneNode mappedasyncModelLoad(ResourceLoader resourceLoader) {
        int options = EngineHelper.LOADER_USEGLTF;
        if (resourceLoader.nativeResource.getExtension().equals("ac")) {
            options |= LOADER_APPLYACPOLICY;
        }
        return ModelFactory.asyncModelLoad(FgModelHelper.mapFilename(resourceLoader, true, options), options);
    }

    /**
     * Statt ac evtl. acpp/gltf verwenden. Aber auch nur, wenn das gltf mit bin im Bundle grundsaetzlich vorliegt.
     * 18.10.23: Moved here from ModelLoader.
     *
     * @param resourceLoader
     * @param preferpp
     * @param loaderoptions
     * @return
     */
    public static ResourceLoader mapFilename(ResourceLoader resourceLoader, boolean preferpp, int loaderoptions) {

        if (preferpp || ((loaderoptions & EngineHelper.LOADER_USEGLTF) > 0)) {
            // ac special handling. Not for explicitly loaded gltf files
            String extension = resourceLoader.nativeResource.getExtension();
            if (extension.equals("ac") || extension.equals("btg")) {
                boolean usegltf = (loaderoptions & EngineHelper.LOADER_USEGLTF) > 0;
                boolean usedgltf = false;

                // 17.2.24: Due to resourceloader no longer check whether gltf exists.
                resourceLoader = resourceLoader.fromReference(resourceLoader.nativeResource.getBasename() + ".gltf");

                /* 17.2.24: old code before resourceloader.
                if (usegltf) {

                    BundleResource ppfile = new BundleResource(file.bundle, file.path, file.getBasename() + ".gltf");
                    if (ppfile.bundle.exists(ppfile)) {
                        if (Config.modelloaddebuglog) {
                            logger.debug("using gltf instead of " + extension);
                        }
                        file = ppfile;
                        usedgltf = true;
                    } else {
                        logger.warn("not existing gltf " + ppfile.getFullName());
                    }
                }
                if (!usedgltf) {
                    // dann doch acpp versuchen
                    BundleResource ppfile = new BundleResource(file.bundle, file.path, file.name + "pp");
                    if (ppfile.bundle.exists(ppfile)) {
                        logger.debug("using acpp instead of " + extension);
                        //21.4.17geht nicht mehr. 22.12.17: Häh, geht doch.
                        file = ppfile;
                    }
                }*/
            }
        }
        return resourceLoader;
    }
}
