package de.yard.threed.flightgear.testutil;

import de.yard.threed.core.InitMethod;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformFactory;
import de.yard.threed.core.platform.PlatformInternals;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.flightgear.FlightGearSettings;
import de.yard.threed.flightgear.TerraSyncBundleResolver;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.FlightGearModuleBasic;
import de.yard.threed.flightgear.core.FlightGearModuleScenery;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterialLib;
import de.yard.threed.flightgear.core.simgear.scene.model.ACProcessPolicy;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.platform.common.ModelLoader;
import de.yard.threed.javacommon.ConfigurationByEnv;
import de.yard.threed.javacommon.DefaultResourceReader;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import de.yard.threed.outofbrowser.SyncBundleLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * 21.7.21 For FG Tests that need a platform with renderer (where headless isn't sufficient).
 * But why? For intersections?Darum in opengl? Maybe for btg terrain building
 *
 * 13.9.23: Currently SimpleHeadless seems sufficient.
 */
public class FgFullTestFactory {

    public static Platform initPlatformForTest(HashMap<String, String> properties) {

        // 29.12.21: Some bundles need to be loaded after init()
        // 12.9.23: "fgdatabasic", FlightGearSettings.FGROOTCOREBUNDLE might be needed in future for aircraft loading (apparently not needed for bluebird)
        // 12.9.23: "fgdatabasicmodel" might be needed in future. Or will be a separate module.
        // "sgmaterial" occupies 493 MB
        List bundlelist = new ArrayList(Arrays.asList(new String[]{"engine",
                /*,*/  "sgmaterial",
                }));
        //bundlelist.add(SGMaterialLib.BUNDLENAME);

        Platform platform = EngineTestFactory.initPlatformForTest((String[]) bundlelist.toArray(new String[0]),
                new SimpleHeadlessPlatformFactory(),
                null,
                ConfigurationByEnv.buildDefaultConfigurationWithEnv(properties));

        //14.9.21: Too late for some tests?
        platform.addBundleResolver(new TerraSyncBundleResolver());

        EngineTestFactory.loadBundleSync("Terrasync-model");

        if (BundleRegistry.getBundle("test-resources") == null) {
            ResourcePath bundlebasedir = new ResourcePath("src/test/resources");
            String e = SyncBundleLoader.loadBundleSyncInternal("test-resources", null,
                    false, new DefaultResourceReader(), bundlebasedir);
        }

        //30.9.19: Aber irgendeine Art init brauchts doch (z.B. wegen TileMgr, proptree). Und die beiden Modules sind ja nun mal da.
        //wird teilweise in einzelnen Tests gemacht. Das ist aber inkonsistent.

        FlightGearModuleBasic.init(null, null);
        FlightGearModuleScenery.init(false);

        // Kruecke zur Entkopplung des Modelload von AC policy.
        ModelLoader.processPolicy = new ACProcessPolicy(null);

        return platform;
    }
}
