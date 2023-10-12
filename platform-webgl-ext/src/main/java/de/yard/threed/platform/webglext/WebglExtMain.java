package de.yard.threed.platform.webglext;

import de.yard.threed.core.JsonHelper;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Scene;
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

}
