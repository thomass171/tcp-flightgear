package de.yard.threed.trafficfg.flight;

import de.yard.threed.core.ObjectBuilder;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.flightgear.TerrainElevationProvider;
import de.yard.threed.traffic.AbstractSceneryBuilder;
import de.yard.threed.traffic.BuilderRegistry;
import de.yard.threed.traffic.SphereSystem;
import de.yard.threed.trafficfg.apps.TravelSceneBluebird;
import de.yard.threed.trafficfg.fgadapter.FgTerrainBuilder;

/**
 * Commons from
 * - TravelScene
 * - TravelSceneBluebird
 */
public class TravelSceneHelper {
    static Log logger = Platform.getInstance().getLog(TravelSceneHelper.class);

    /**
     * 16.5.24:Needed after moving 'world' to SphereSystem
     */
    public static SceneNode getSphereWorld() {
        SceneNode world = ((SphereSystem) SystemManager.findSystem(SphereSystem.TAG)).world;
        if (world == null) {
            throw new RuntimeException("too early access to SphereSystem world");
        }
        return world;
    }

    public static void registerFgTerrainBuilder() {
        BuilderRegistry.add("FgTerrainBuilder", (ObjectBuilder<AbstractSceneryBuilder>) s -> {
            AbstractSceneryBuilder terrainBuilder = new FgTerrainBuilder();
            terrainBuilder.init(getSphereWorld());
            logger.debug("FgTerrainBuilder inited");

            // TerrainElevationProvider was created in FgTerrainBuilder. Needs help because EDDK groundnet exceeds EDDK tile, so define a default value 68.
            // 15.5.24: This can only be a temp workaround! TODO remove it early
            // 29.5.24: Is it really needed for groundnet. It leads to real malfunction with initialRoute. Try without
            //((TerrainElevationProvider) SystemManager.getDataProvider(SystemManager.DATAPROVIDERELEVATION)).setDefaultAltitude(68.8);
            return terrainBuilder;
        });
    }

    public static boolean hasTerrain() {
        // Platzrunde (und wakeup) erst anlegen, wenn das Terrain da ist und worldadjustment durch ist. Difficult to detect.
        // 21.5.24: Check existence of groundnet. Not perfect, but far better than number of frames.
        boolean terrainavailable = false;
        if (GroundServicesSystem.groundnets.get("EDDK") != null) {
            terrainavailable = true;
        }
        return terrainavailable;
    }
}
