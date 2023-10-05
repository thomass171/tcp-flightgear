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

 */
/*public class FgFullTestFactory {

    public static Platform initPlatformForTest() {

        List bundlelist = new ArrayList(Arrays.asList(new String[]{"engine", SGMaterialLib.BUNDLENAME}));


        postInit();

        return platform;
    }


}*/
