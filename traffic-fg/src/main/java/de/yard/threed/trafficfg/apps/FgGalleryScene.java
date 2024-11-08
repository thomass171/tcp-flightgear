package de.yard.threed.trafficfg.apps;

import de.yard.threed.core.Color;
import de.yard.threed.core.DimensionF;
import de.yard.threed.core.GeneralParameterHandler;
import de.yard.threed.core.IntHolder;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Camera;
import de.yard.threed.engine.KeyCode;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.apps.GalleryScene;
import de.yard.threed.engine.ecs.FirstPersonMovingComponent;
import de.yard.threed.engine.ecs.InputToRequestSystem;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.gui.ControlPanel;
import de.yard.threed.engine.gui.ControlPanelHelper;
import de.yard.threed.engine.gui.ControlPanelMenu;
import de.yard.threed.engine.gui.DefaultMenuProvider;
import de.yard.threed.engine.gui.LabeledSpinnerControlPanel;
import de.yard.threed.engine.gui.NumericDisplayFormatter;
import de.yard.threed.engine.gui.NumericSpinnerHandler;
import de.yard.threed.engine.gui.TimeDisplayFormatter;
import de.yard.threed.engine.platform.common.ModelLoader;
import de.yard.threed.flightgear.FgBundleHelper;
import de.yard.threed.flightgear.SimpleBundleResourceProvider;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.FlightGearModuleBasic;
import de.yard.threed.flightgear.core.flightgear.main.AircraftResourceProvider;
import de.yard.threed.flightgear.core.flightgear.main.FGGlobals;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.scene.model.OpenGlProcessPolicy;
import de.yard.threed.flightgear.core.simgear.scene.model.SGAnimation;
import de.yard.threed.flightgear.FlightGearProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * An extension of the generic gallery for viewing FG XML model.
 * Like super class using ECS for AR.
 * <p>
 * Not using trafficConfig. But "AircraftDir" is needed for FG aircraft. Doesn't know "optionals" and
 * "zoffset" and so on. But that is acceptable.
 * FG Animations are supported, but not via FgAnimationUpdateSystem, because the entities are built for
 * the models (by super class GalleryScene) without knowing about animations.
 * Keys are
 * <p>
 * Model with visual aimations:
 * - tower radar
 * - Windturbine
 * - beacon
 * <p>
 * Not only for XML but also pure GLTF.
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
                //Antenna on top rotates by "/sim/time/elapsed-sec".
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


        // super class probably does VR standards??
        InputToRequestSystem inputToRequestSystem = (InputToRequestSystem) SystemManager.findSystem(InputToRequestSystem.TAG);
        if (vrInstance != null) {

            ControlPanel leftControllerPanel = buildVrControlPanel(/*buttonDelegates*/);
            // position and rotation of VR controlpanel is controlled by property ...
            inputToRequestSystem.addControlPanel(leftControllerPanel);
            vrInstance.attachControlPanelToController(vrInstance.getController(0), leftControllerPanel);

        } else {
            inputToRequestSystem.addKeyMapping(KeyCode.M, InputToRequestSystem.USER_REQUEST_MENU);
            inputToRequestSystem.setMenuProvider(new DefaultMenuProvider(getDefaultCamera(), (Camera camera) -> {
                //ControlPanel m = ControlPanelHelper.buildSingleColumnFromMenuitems(new DimensionF(1.3, 0.7), -3, 0.01, menuitems, Color.LIGHTBLUE);
                ControlPanel m = buildVrControlPanel();
                m.getTransform().setPosition(new Vector3(-0, 0, -1.5));

                ControlPanelMenu menu = new ControlPanelMenu(m);
                return menu;
            }));
        }
        // 5.11.24: FlightGearSystem provides property trees
        //Yes, but we have our own properties update in this scene (see header)
        // SystemManager.addSystem(new FlightGearSystem());
    }

    /**
     * Die FG Komponenten initen bzw. einhaengen, die fuer Model laden gebraucht werden. Mehr aber nicht.
     * 21.10.17: now without initFG.
     * 8.11.24: But with init for property tree init
     */
    private static AircraftResourceProvider initFG() {
        // In fgdatabasic are some aircraft parts like Instruments3D for CDU
        FgBundleHelper.addProvider(new SimpleBundleResourceProvider("fgdatabasic"));
        AircraftResourceProvider arp = new AircraftResourceProvider();
        FgBundleHelper.addProvider(arp);

        // 8.11.24 also needed because it sets up the property tree
        FlightGearModuleBasic.init(null, null);

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

        // Cannot update animations via ECS because models are no entities in this scene (see header)
        FgModelPreviewScene.updateAnimations(flightGearProperties, animationList, getDefaultCamera());

    }

    /**
     * A control panel permanently attached to the left controller. Consists of
     * <p>
     * <p>
     * top line: vr y offset spinner
     * medium: spinner for teleport toggle
     */
    private ControlPanel buildVrControlPanel(/*Map<String, ButtonDelegate> buttonDelegates*/) {
        Color backGround = Color.RED;//controlPanelBackground;
        Material mat = Material.buildBasicMaterial(backGround, null);

        double ControlPanelWidth = 0.6;
        double ControlPanelRowHeight = 0.1;
        double ControlPanelMargin = 0.005;

        int rows = 2;
        DimensionF rowsize = new DimensionF(ControlPanelWidth, ControlPanelRowHeight);
        Color textColor = Color.RED;

        ControlPanel cp = new ControlPanel(new DimensionF(ControlPanelWidth, rows * ControlPanelRowHeight), mat, 0.01);

        SGPropertyNode windFromHeadingDeg = FGGlobals.getInstance().get_props().getNode("/environment/wind-from-heading-deg", false);
        cp.add(new Vector2(0,
                        ControlPanelHelper.calcYoffsetForRow(1, rows, ControlPanelRowHeight)),
                new LabeledSpinnerControlPanel("wnd hdg", rowsize, 0, mat,
                        new NumericSpinnerHandler(15, value -> {
                            if (value != null) {
                                windFromHeadingDeg.setDoubleValue(value);
                            }
                            return windFromHeadingDeg.getDoubleValue();
                        }, 360, new NumericDisplayFormatter(0)), textColor));

        SGPropertyNode windSpeedKt = FGGlobals.getInstance().get_props().getNode("/environment/wind-speed-kt", false);
        cp.add(new Vector2(0,
                        ControlPanelHelper.calcYoffsetForRow(0, rows, ControlPanelRowHeight)),
                new LabeledSpinnerControlPanel("wnd spd", rowsize, 0, mat,
                        new NumericSpinnerHandler(5, value -> {
                            if (value != null) {
                                windSpeedKt.setDoubleValue(value);
                            }
                            return windSpeedKt.getDoubleValue();
                        }, null, new NumericDisplayFormatter(0)), textColor));

        return cp;
    }
}
