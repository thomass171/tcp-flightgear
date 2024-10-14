package de.yard.threed.trafficfg.apps;

import de.yard.threed.core.GeneralParameterHandler;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.apps.GalleryScene;
import de.yard.threed.engine.ecs.FirstPersonMovingComponent;
import de.yard.threed.engine.platform.common.ModelLoader;
import de.yard.threed.flightgear.FgBundleHelper;
import de.yard.threed.flightgear.SimpleBundleResourceProvider;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.flightgear.main.AircraftResourceProvider;
import de.yard.threed.flightgear.core.simgear.scene.model.OpenGlProcessPolicy;
import de.yard.threed.flightgear.core.simgear.scene.model.SGAnimation;
import de.yard.threed.trafficfg.fgadapter.FlightGearProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * An extension of the generic gallery for viewing FG XML model.
 * Like super class using ECS for AR.
 * <p>
 * Not using trafficConfig. But "AircraftDir" is needed for FG aircraft. Doesn't know "optionals" and
 * "zoffset" and so on. But that is acceptable.
 * FG Animations are supported.
 * Keys are
 * <p>
 * Model with visual aimations:
 * - tower radar
 * - Windturbine
 * - beacon
 * <p>
 * Not only for XML but also GLTF.
 */
public class FgGalleryScene extends GalleryScene {
    public Log logger = Platform.getInstance().getLog(FgGalleryScene.class);
    List<SGAnimation> animationList = new ArrayList<SGAnimation>();
    AircraftResourceProvider arp;
    FlightGearProperties flightGearProperties = new FlightGearProperties();
    private boolean speedSet = false;

    /**
     * Only models included in project. Advanced model might be used by an extending scene in 'traffic-advanced'
     * See FgModelPreviewScene for syntax.
     */
    @Override
    public String[] getModelList() {
        return new String[]{
                //Antenna on top rotates.
                "T3072824:Objects/e000n50/e007n50/egkk_tower.xml;scale=0.002",
                //windturbine should have two rotations (currently only one)
                "H:Models/Power/windturbine.xml;scale=0.002",
                "H:Models/Power/generic_pylon_50m.xml;scale=0.002",
                "bluebird:Models/bluebird.xml;scale=0.01",
                "fgdatabasic:Aircraft/Instruments-3d/pedals/pedals.xml;scale=0.5",
                "H:Models/Airport/windsock.xml;scale=0.02",
                "H:Models/Airport/beacon.xml;scale=0.02",
                "H:Models/Airport/light-pole-gray-38m.xml;scale=0.01",
                "T3072824:Objects/e000n50/e007n50/EDDK-Station.gltf;scale=0.002",
                "T3072816:Objects/e000n50/e007n50/egkk_maint_1_1.gltf;scale=0.002",
                "H:Models/Airport/Jetway/jetway-movable.xml;scale=0.02",
                "fgdatabasic:Aircraft/Instruments-3d/garmin196/garmin196.xml;scale=1.0",
                "fgdatabasic:Aircraft/Instruments-3d/yoke/yoke.xml;scale=0.5",

        };
    }

    @Override
    public String[] getPreInitBundle() {
        return new String[]{"engine", "data", FlightGear.getBucketBundleName("model"), "sgmaterial", "fgdatabasic", "traffic-fg"};
    }

    @Override
    public void customInit() {

        FgModelPreviewScene.extendSmartModelLoaderForFG(arp, new GeneralParameterHandler<List<SGAnimation>>() {
            @Override
            public void handle(List<SGAnimation> animationsOfLoadedModel) {
                //just add?? animationList = new ArrayList<SGAnimation>();
                animationList.addAll(animationsOfLoadedModel);
            }
        });

        arp = initFG();
        // ACpolicy doesn't help here because coordinate system is OpenGL instead of FG.
        // Use a policy for XML as it is the most used here.
        //ModelLoader.processPolicy = new ACProcessPolicy(null);
        ModelLoader.processPolicy = new OpenGlProcessPolicy(true);
    }

    /**
     * Die FG Komponenten initen bzw. einhaengen, die fuer Model laden gebraucht werden. Mehr aber nicht.
     * Nachbildung des FlightGear.init(7);
     * Die Reihenfolge orientiert sich an FG fgIdleFunction()
     * System swerden hier nocht nicht angelegt.
     * 21.10.17: jetzt ohne initFG.
     */
    private static AircraftResourceProvider initFG() {
        // In fgdatabasic sind manche Aircraftteile, z.B. Instruments3D für CDU
        FgBundleHelper.addProvider(new SimpleBundleResourceProvider("fgdatabasic"));
        AircraftResourceProvider arp = new AircraftResourceProvider();
        FgBundleHelper.addProvider(arp);
        return arp;
    }

    @Override
    public void customUpdate() {
        if (!speedSet && avatar != null) {
            FirstPersonMovingComponent fpmc = FirstPersonMovingComponent.getFirstPersonMovingComponent(avatar);
            if (fpmc != null) {
                fpmc.getFirstPersonTransformer().setMovementSpeed(1.3);
                fpmc.getFirstPersonTransformer().setRotationSpeed(54.0);
                speedSet = true;
            }
        }

        // TODO update animations via ECS
        FgModelPreviewScene.updateAnimations(flightGearProperties, animationList, getDefaultCamera());

    }

}
