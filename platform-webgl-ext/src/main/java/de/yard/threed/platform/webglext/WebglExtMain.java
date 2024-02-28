package de.yard.threed.platform.webglext;

import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformFactory;
import de.yard.threed.core.platform.PlatformInternals;
import de.yard.threed.engine.Scene;
import de.yard.threed.flightgear.TerraSyncBundleResolver;
import de.yard.threed.platform.webgl.PlatformWebGl;
import de.yard.threed.trafficadvanced.apps.FlatAirportScene;
import de.yard.threed.trafficadvanced.apps.HangarScene;
import de.yard.threed.trafficadvanced.apps.TravelScene;
import de.yard.threed.trafficfg.apps.RailingScene;
import de.yard.threed.trafficfg.apps.SceneryScene;
import de.yard.threed.trafficfg.apps.TravelSceneBluebird;

public class WebglExtMain extends de.yard.threed.platform.webgl.Main {

    @Override
    public Scene buildSceneUpdater(String name) {
        Log logger = Platform.getInstance().getLog(WebglExtMain.class);

        if (name.equals("SceneryScene"))
            return new SceneryScene();
        if (name.equals("RailingScene"))
            return new RailingScene();
        if (name.equals("FlatAirportScene"))
            return new FlatAirportScene();
        if (name.equals("HangarScene"))
            return new HangarScene();
        if (name.equals("TravelScene"))
            return new TravelScene();
        if (name.equals("TravelSceneBluebird"))
            return new TravelSceneBluebird();
        logger.error("Scene " + name + " not found");
        return null;
    }

    @Override
    protected PlatformFactory getPlatformFactory(Configuration configuration) {
        return new PlatformFactory() {
            @Override
            public PlatformInternals createPlatform(Configuration conf) {
                PlatformInternals platformInternals = PlatformWebGl.init(conf);
                // "/TerraySync" is added inside resolver. TerraSyncBundleResolver needs to be before default resolver, which resolves everything.
                // And no leading "/". That is added later.
                // This resolver is only a basic default only returning a subset. For full TerrySync bundle a custom
                // TerraSyncBundleResolver needs to be added by the platform.
                Platform.getInstance().addBundleResolver(new TerraSyncBundleResolver("bundles"), true);
                return platformInternals;
            }
        };

    }
}
