package de.yard.threed.flightgear.testutil;

import de.yard.threed.core.InitMethod;
import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformFactory;
import de.yard.threed.core.platform.PlatformInternals;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleLoadDelegate;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.testutil.SimpleEventBusForTesting;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.testutil.AdvancedHeadlessPlatform;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.flightgear.FgBundleHelper;
import de.yard.threed.flightgear.TerraSyncBundleResolver;
import de.yard.threed.flightgear.core.FlightGearModuleBasic;
import de.yard.threed.flightgear.core.FlightGearModuleScenery;
import de.yard.threed.flightgear.core.simgear.scene.material.Effect;
import de.yard.threed.flightgear.core.simgear.scene.material.MakeEffect;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterialLib;
import de.yard.threed.flightgear.core.simgear.scene.model.ACProcessPolicy;
import de.yard.threed.engine.platform.common.ModelLoader;
import de.yard.threed.flightgear.core.simgear.scene.model.SGModelLib;
import de.yard.threed.flightgear.core.simgear.scene.tgdb.ReaderWriterSTG;
import de.yard.threed.flightgear.core.simgear.scene.tgdb.SGOceanTile;
import de.yard.threed.javacommon.ConfigurationByEnv;
import de.yard.threed.javacommon.DefaultResourceReader;
import de.yard.threed.javacommon.SimpleHeadlessPlatform;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import de.yard.threed.outofbrowser.SimpleBundleResolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 21.7.21 For FG Tests not needing a platform with renderer.
 * * 21.7.21 For FG Tests that need a platform with additional bundles and inits.
 * *
 * * <p>
 * * 13.9.23: Currently SimpleHeadless seems sufficient.
 */
public class FgTestFactory {

    @Deprecated
    public static Platform initPlatformForTestWithFgResolver() {
        return initPlatformForTest(new HashMap<>(), true, true, true, false);
    }

    @Deprecated
    public static Platform initPlatformForTest() {
        return initPlatformForTest(new HashMap<>(), true, false, false, false);
    }

    @Deprecated
    public static Platform initPlatformForTest(HashMap<String, String> properties) {
        return initPlatformForTest(properties, false, true, true, false);

    }

    public static Platform initPlatformForTest(boolean addTestResourcesBundle, boolean addTerraSyncResolver, boolean addMaterialLib) {
        return initPlatformForTest(new HashMap<>(), addTestResourcesBundle, addTerraSyncResolver, addMaterialLib, false);
    }

    public static Platform initPlatformForTest(boolean addTestResourcesBundle, boolean addTerraSyncResolver, boolean addMaterialLib, boolean forBtgConversion) {
        return initPlatformForTest(new HashMap<>(), addTestResourcesBundle, addTerraSyncResolver, addMaterialLib, forBtgConversion);
    }

    /**
     * FG Resolver are probably not needed or trigger false positive results in tools.
     */
    public static Platform initPlatformForTest(HashMap<String, String> properties, boolean addTestResourcesBundle, boolean addTerraSyncResolver, boolean addMaterialLib, boolean forBtgConversion) {

        FgBundleHelper.clear();
        ReaderWriterSTG.btgLoaded.clear();
        SGModelLib.clear();
        MakeEffect.clearEffectCache();
        Effect.effectListForTesting = new ArrayList<>();
        SGOceanTile.created.clear();

        // 29.12.21: Some bundles need to be loaded after init()
        // 12.9.23: FlightGearSettings.FGROOTCOREBUNDLE might be needed in future for aircraft loading (apparently not needed for bluebird)
        // 12.9.23: "fgdatabasicmodel" might be needed in future. Or will be a separate module.
        // "sgmaterial" occupies 493 MB

        // "fgdatabasicmodel" and FlightGearSettings.FGROOTCOREBUNDLE currently not needed in tests
        // "fgdatabasic" is needed eg. for yoke and pedals in bluebird.
        // "data" is needed for textures for loc sometimes.
        String[] bundlelist = new String[]{"data", "engine", "fgdatabasic", "traffic-fg"};

        Configuration configuration = ConfigurationByEnv.buildDefaultConfigurationWithEnv(properties);

        boolean forScene = configuration.getString("scene") != null;
        if (forScene) {
            // bundles come from getPreInitBundle()
            bundlelist = null;
        }
        // Special platform factory for adding bundle resolver
        Platform platform = EngineTestFactory.initPlatformForTest(bundlelist,
                configuration1 -> {
                    // use AdvancedHeadlessPlatform to have AsyncHelper and thus model loading
                    PlatformInternals platformInternals = AdvancedHeadlessPlatform.init(configuration1, new SimpleEventBusForTesting());
                    if (addTerraSyncResolver) {
                        Platform.getInstance().addBundleResolver(new TerraSyncBundleResolver(configuration1.getString("HOSTDIRFG") + "/bundles"));
                    }
                    Platform.getInstance().addBundleResolver(new SimpleBundleResolver(configuration1.getString("HOSTDIRFG") + "/bundles", new DefaultResourceReader()));
                    return platformInternals;
                }, (InitMethod) null, configuration);

        if (!forScene) {
            //platform.addBundleResolver(new SimpleBundleResolver(configuration.getString("HOSTDIRFG") + "/bundles", new DefaultResourceReader()));

            // 7.11.24 moved out from "if" because also needed without materiallib because it sets up the property tree
            FlightGearModuleBasic.init(null, null);
            if (addMaterialLib) {
                // material is needed in following setup
                EngineTestFactory.loadBundleSync(SGMaterialLib.BUNDLENAME);


                // if (withPostInit) {
                postInit();
                //}
                //30.9.19: Aber irgendeine Art init brauchts doch (z.B. wegen TileMgr, proptree). Und die beiden Modules sind ja nun mal da.
                //wird teilweise in einzelnen Tests gemacht. Das ist aber inkonsistent.

                //moved up FlightGearModuleBasic.init(null, null);
                FlightGearModuleScenery.init(false, forBtgConversion);

                // Kruecke zur Entkopplung des Modelload von AC policy.
                ModelLoader.processPolicy = new ACProcessPolicy(null);
            }

            if (addTestResourcesBundle) {
                EngineTestFactory.addTestResourcesBundle();
            }
        }
        //not generic FgTestFactory.assertPlatform();

        return platform;
    }

    public static void postInit() {
        Platform platform = Platform.getInstance();
        //14.9.21: Too late for some tests?
        //platform.addBundleResolver(new TerraSyncBundleResolver());

        //EngineTestFactory.loadBundleSync("Terrasync-model");
    }

    public static void assertPlatform() {

        Platform platform = Platform.getInstance();
        assertEquals(3, platform.bundleResolver.size());

        String bundleDir = ((SimpleBundleResolver) platform.bundleResolver.get(0)).bundledir;
        assertTrue(bundleDir.contains("tcp-22/bundles"), bundleDir);
/*TODO might be terrasync at 1        bundleDir = ((SimpleBundleResolver) platform.bundleResolver.get(1)).bundledir;
        assertTrue(bundleDir.contains("tcp-flightgear/bundles"), bundleDir);*/

        Bundle sgmaterial = BundleRegistry.getBundle(SGMaterialLib.BUNDLENAME);
        assertNotNull(sgmaterial);
        assertEquals(91, sgmaterial.getSize());
    }
}
