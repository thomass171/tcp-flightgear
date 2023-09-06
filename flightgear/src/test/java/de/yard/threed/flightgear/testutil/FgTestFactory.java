package de.yard.threed.flightgear.testutil;

import de.yard.threed.core.InitMethod;
import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.flightgear.FlightGearMain;
import de.yard.threed.flightgear.FlightGearSettings;
import de.yard.threed.flightgear.core.FlightGearModuleBasic;
import de.yard.threed.flightgear.core.FlightGearModuleScenery;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterialLib;
import de.yard.threed.flightgear.core.simgear.scene.model.ACProcessPolicy;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.platform.common.ModelLoader;
import de.yard.threed.javacommon.ConfigurationByEnv;
import de.yard.threed.javacommon.DefaultResourceReader;
import de.yard.threed.outofbrowser.SyncBundleLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * 21.7.21 For FG Tests not needing a platform?
 * 6.9.23: Well, a platform is always needed. Tests need some FG specific bundles. Not nice??
 */
public class FgTestFactory {

    public static Platform initPlatformForTest() {

        List bundlelist = new ArrayList(Arrays.asList(new String[]{"engine", "fgdatabasic", FlightGearSettings.FGROOTCOREBUNDLE,
                "fgdatabasicmodel"}));
        bundlelist.add(SGMaterialLib.BUNDLENAME);

        //21.7.21: Headless reicht hier nicht, weil z.B. model geladen werden
        Platform platform = EngineTestFactory.initPlatformForTest(/*30.6.21 true,*/ (String[]) bundlelist.toArray(new String[0]), new PlatformFactoryHeadless(), (InitMethod) null,
                ConfigurationByEnv.buildDefaultConfigurationWithEnv(new HashMap<String,String>()));
        //EngineHelper platform = TestFactory.initPlatformForTest(true, true, null, true);


        //30.9.19: Aber irgendeine Art init brauchts doch (z.B. wegen TileMgr, proptree). Und die beiden Modules sind ja nun mal da.
        //wird teilweise in einzelnen Tests gemacht. Das ist aber inkonsistent.

        FlightGearModuleBasic.init(null, null);
        FlightGearModuleScenery.init(false);

        // Kruecke zur Entkopplung des Modelload von AC policy.
        ModelLoader.processPolicy=new ACProcessPolicy(null);

        if (BundleRegistry.getBundle("test-resources") == null) {
            ResourcePath bundlebasedir = new ResourcePath("src/test/resources");
            String e = SyncBundleLoader.loadBundleSyncInternal("test-resources", null,
                    false, new DefaultResourceReader(), bundlebasedir);
        }

        return platform;
    }
}
