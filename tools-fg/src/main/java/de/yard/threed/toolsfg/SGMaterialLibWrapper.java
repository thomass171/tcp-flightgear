package de.yard.threed.toolsfg;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResolver;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.flightgear.FgBundleHelper;
import de.yard.threed.flightgear.SimpleBundleResourceProvider;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterialLib;
import de.yard.threed.javacommon.DefaultResourceReader;
import de.yard.threed.outofbrowser.NativeResourceReader;
import de.yard.threed.tools.SyncBundleLoader;

/**
 * Plugin for generic model (BTG) loader for building FG terrain material.
 *
 * Extracted from FlightGearModuleScenery for better dependency reduction
 * 22.9.25 Isn't it more confusing than a benefit to have this a singleton?
 */
public class SGMaterialLibWrapper {
    private static SGMaterialLibWrapper instance = null;
    private SGMaterialLib matlib;

    private SGMaterialLibWrapper() {


        ResourcePath bundlebasedir = BundleResolver.resolveBundle(SGMaterialLib.BUNDLENAME, Platform.getInstance().bundleResolver);

        SyncBundleLoader.loadBundleAndWait(SGMaterialLib.BUNDLENAME);
        // 12.11.24: Also needs "fgdatabasic" for effects
        SyncBundleLoader.loadBundleAndWait("fgdatabasic");
        // 02.09.25: Needs a bundle resolver for "fgdatabasic" since effects are no longer resolved
        // from hardcoded "fgdatabasic".
        FgBundleHelper.addProvider(new SimpleBundleResourceProvider("fgdatabasic"));

        matlib = new SGMaterialLib();
        String mpath;// = FGProperties.fgGetString("/sim/rendering/materials-file");

        SGPropertyNode tmpTree = new SGPropertyNode();
        mpath = "Materials/regions/materials.xml";
        tmpTree.setStringValue/*FGProperties.fgSetString*/("/sim/startup/season", "summer");

        if (!matlib.load(/*FGGlobals.globals.get_fg_root(),*/ mpath, tmpTree/*FGGlobals.globals.get_props()*/, true)) {
            throw new /*SGIO*/RuntimeException("Error loading materials file" + mpath);
        }

        //matlib.

    }

    public static SGMaterialLibWrapper getInstance() {
        if (instance == null) {
            instance = new SGMaterialLibWrapper();
        }
        return instance;
    }

    public static void dropInstance() {
        instance = null;
    }

    public SGMaterialLib getSGMaterialLib() {
        return matlib;
    }

    public void disableSGMaterialLib() {
        matlib = null;
    }
}
