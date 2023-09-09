package de.yard.threed.flightgear.testutil;

import de.yard.threed.core.InitMethod;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformFactory;
import de.yard.threed.core.resource.BundleRegistry;
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
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * 21.7.21 For FG Tests that need a platform with renderer (where headless isn't sufficient).
 * But why? For intersections?Darum in opengl.
 * Aber was heisst das? Reicht ein SceneRunnerForTesting nicht aus?
 */
public class FgFullTestFactory {

    public static Platform initPlatformForTest(HashMap<String, String> properties) {

        // 29.12.21: Manche Bundle koennen erst nach dem Init geladen werden
        List bundlelist = new ArrayList(Arrays.asList(new String[]{"engine", "data-old","maze", "sandbox", "My-777", "data", "fgdatabasicmodel", "fgdatabasic", "sgmaterial","c172p",
                /*BundleRegistry.FGHOMECOREBUNDLE,*/ FlightGearSettings.FGROOTCOREBUNDLE,
                /*zu frueh FlightGear.getBucketBundleName("model")*/}));
        //bundlelist.add(SGMaterialLib.BUNDLENAME);

        //21.7.21: Headless reicht hier nicht, weil z.B. model geladen werden
        //PlatformHomeBrew without renderer?
        //new PlatformFactoryHomeBrewDummyRenderer()
                PlatformFactory platformFactory= new SimpleHeadlessPlatformFactory();
        Platform platform = EngineTestFactory.initPlatformForTest(/*30.6.21 true,*/ (String[]) bundlelist.toArray(new String[0]), platformFactory,
                null,
                ConfigurationByEnv.buildDefaultConfigurationWithEnv(properties));
        //EngineHelper platform = TestFactory.initPlatformForTest(true, true, null, true);

        //14.9.21: Das koennte fuer manche Tests wohl zu spaet sein.
        platform.addBundleResolver(new TerraSyncBundleResolver());

        EngineTestFactory.loadBundleSync("Terrasync-model");

        //30.9.19: Aber irgendeine Art init brauchts doch (z.B. wegen TileMgr, proptree). Und die beiden Modules sind ja nun mal da.
        //wird teilweise in einzelnen Tests gemacht. Das ist aber inkonsistent.

        FlightGearModuleBasic.init(null, null);
        FlightGearModuleScenery.init(false);

        // Kruecke zur Entkopplung des Modelload von AC policy.
        ModelLoader.processPolicy = new ACProcessPolicy(null);

        return platform;
    }
}
