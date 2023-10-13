package de.yard.threed.platform.webglext;

import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformFactory;
import de.yard.threed.core.platform.PlatformInternals;
import de.yard.threed.engine.Scene;
import de.yard.threed.flightgear.TerraSyncBundleResolver;
import de.yard.threed.platform.webgl.PlatformWebGl;
import de.yard.threed.platform.webgl.WebGlBundleResolver;
import de.yard.threed.trafficfg.apps.SceneryScene;

public class WebglExtMain extends de.yard.threed.platform.webgl.Main {

    @Override
    public Scene buildSceneUpdater(String name) {
        Log logger = Platform.getInstance().getLog(WebglExtMain.class);

        if (name.equals("SceneryScene"))
            return new SceneryScene();

        logger.error("Scene " + name + " not found");
        return null;
    }

    @Override
    protected PlatformFactory getPlatformFactory(Configuration configuration) {
        return new PlatformFactory() {
            @Override
            public PlatformInternals createPlatform(Configuration conf) {
                PlatformInternals platformInternals = PlatformWebGl.init(conf);
                Platform.getInstance().addBundleResolver(new TerraSyncBundleResolver("/bundles"));
                return platformInternals;
            }
        };

    }
}
