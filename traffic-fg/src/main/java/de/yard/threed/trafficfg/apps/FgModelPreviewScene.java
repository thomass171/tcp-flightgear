package de.yard.threed.trafficfg.apps;

import de.yard.threed.core.*;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeCollision;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.engine.*;
import de.yard.threed.engine.apps.ModelPreviewScene;
import de.yard.threed.engine.apps.SmartModelLoader;
import de.yard.threed.engine.gui.ControlPanel;
import de.yard.threed.engine.gui.ControlPanelMenu;
import de.yard.threed.engine.gui.DefaultMenuProvider;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.ModelLoader;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.RequestHandler;
import de.yard.threed.flightgear.FgBundleHelper;
import de.yard.threed.flightgear.SimpleBundleResourceProvider;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.FlightGearModuleBasic;
import de.yard.threed.flightgear.core.SGLoaderOptions;
import de.yard.threed.flightgear.core.flightgear.main.AircraftResourceProvider;
import de.yard.threed.flightgear.core.flightgear.main.FGGlobals;
import de.yard.threed.flightgear.core.simgear.scene.model.ACProcessPolicy;
import de.yard.threed.flightgear.core.simgear.scene.model.SGAnimation;
import de.yard.threed.flightgear.core.simgear.scene.model.SGReaderWriterXML;
import de.yard.threed.flightgear.FlightGearProperties;
import de.yard.threed.flightgear.ecs.FgAnimationUpdateSystem;
import de.yard.threed.trafficfg.AnimationControlPanel;

import java.util.ArrayList;
import java.util.List;

/**
 * An extension of the generic model viewer for viewing FG XML model.
 * Like super class not using ECS.
 * <p>
 * 22.12.18: Not using trafficConfig. But "AircraftDir" is needed for FG aircraft. Doesn't know "optionals" and
 * "zoffset" and so on. But that is acceptable.
 * FG Animations are supported manually (outside ECS).
 * Keys are
 * 1: browse forward
 * 2: browse backward
 * +: scale up
 * -: scale down
 * <p>
 * Model with visual aimations:
 * - Windturbine(index 1)
 * - tower radar(0)
 * - beacon
 * <p>
 * Not only for XML but also GLTF.
 */
public class FgModelPreviewScene extends ModelPreviewScene {
    public static Log logger = Platform.getInstance().getLog(FgModelPreviewScene.class);
    // Animations of current model incl. all submodel.
    List<SGAnimation> animationList = new ArrayList<>();
    private AircraftResourceProvider arp;
    FlightGearProperties flightGearProperties = new FlightGearProperties();
    public static SmartModelLoader terrasyncSmartModelLoader;

    /**
     * See SmartModelLoader for meaning of prefixes (typically it is a bundle name). 'fgdatabasic' is the limited one from project(?).
     * Prefixes
     * 'H': TerraSync-model
     * 'A': Aircraft model (full and single components, requiring AircraftResourceProvider)
     * 'Tindex': TerraSync model from tile
     * are registered additionally here.
     * <p>
     * 9.1.22: Separator now is ':' instead of '-'
     */
    @Override
    public String[] getModelList() {
        String bundlePool = "https://ubuntu-server.udehlavj1efjeuqv.myfritz.net/publicweb/bundlepool";
        return new String[]{
                //Antenna on top rotates.
                "T3072824:Objects/e000n50/e007n50/egkk_tower.xml",
                //windturbine should have two rotations (currently only one)
                "H:Models/Power/windturbine.xml",
                // optionals are not considered in 'A'!
                "A:Models/777-200.xml;bundleUrl=" + bundlePool + "/777",
                // 'garmin196' is contained in base project
                "fgdatabasic:Aircraft/Instruments-3d/garmin196/garmin196.xml",
                "",
                //5
                "fgdatabasicmodel:Models/Airport/Pushback/Douglas.xml;bundleUrl=" + bundlePool + "/fgdatabasicmodel",
                //has a rotating and blinking head light
                "fgdatabasicmodel:Models/Airport/Pushback/Goldhofert.xml;bundleUrl=" + bundlePool + "/fgdatabasicmodel",
                // 7:AI Aircrafts have 'correct' rotation
                "fgdatabasicmodel:AI/Aircraft/737/737-AirBerlin.xml;bundleUrl=" + bundlePool + "/fgdatabasicmodel",
                "fgdatabasicmodel:AI/Aircraft/747/744-KLM.xml;bundleUrl=" + bundlePool + "/fgdatabasicmodel",
                "fgdatabasicmodel-Followmeausfgaddonkopiert/followme.xml",
                "fgdatabasicmodel-Catering6620KopiertAusTerrasync/catruckmed-lsg1.xml",
                "fgdatabasicmodel-Models/Airport/Pushback/Forklift.xml",
                "fgdatabasicmodel-Models/Airport/Pushback/Military.xml",
                "fgdatabasicmodel-FuelTruck/Fuel_Truck_Short_VolvoFM.xml",
                //
                // the digital-clock model isn't origin based but somewhere at (-0.36431,0.07009,0.40234) in ac-space. Fix this via offset.
                // And contains 'Aircraft' references
                "A:Models/Interior/Panel/Instruments/digital-clock/digital-clock.xml;offset=0.38,0.39,-0.08;scale=656.0;bundleUrl=bluebird",
                //15: the mag-compass model isn't origin based but somewhere at (-0.4,0.25034,0.01142) in ac-space. Fix this via offset.
                "A:Models/Interior/Panel/Instruments/mag-compass/mag-compass.xml;offset=0.37,0.015,-0.23;scale=656.0;bundleUrl=bluebird",
                "",
                "A:Models/Interior/Panel/Instruments/kx165/kx165-1.xml;scale=500;bundleUrl=" + bundlePool + "/c172p.2024",
                // 18: Are c172 parts missing from FGROOT? There are gaps like missing wing and wheels
                // 1.9.25 And it doesn't look nice (any more?). 'glass' is not correct. Was anything lost
                // with new shader or JME? But in ThreeJS it is the same way strange. But lets focus on 2024 model.
                "A:Models/c172p.xml;bundleUrl=" + bundlePool + "/c172p.2018",
                // 19: Full c172p of 2024
                "A:Models/c172-common.xml;bundleUrl=" + bundlePool + "/c172p.2024",
                "A:Models/Interior/Panel/Instruments/asi/asi.xml;scale=500;bundleUrl=" + bundlePool + "/c172p.2024",
                "A:Models/Interior/Panel/c172p-panel/c172p-interior.gltf;scale=5;bundleUrl=" + bundlePool + "/c172p.2024;optTexturePath=/Models",
                "A:Models/Interior/Panel/c172p-panel/c172p.xml;scale=5;bundleUrl=" + bundlePool + "/c172p.2024;optTexturePath=/Models",

                //23: gltf geht wohl, hat aber keine ACPolicy. Darum xml. 29.12.18: EDDK-Terminal1 findet er nicht mehr. Das liegt nicht mehr in fgbasicmodel, sondern in ???
                "??EDDK-final/EDDK-Terminal1.xml",
                "??EDDK-EDDK-StarB.gltf",
                "??railing-loc.xml",
                // 26
                "bluebird:Models/bluebird.xml",
                "traffic-fg:flight/navigator.xml",
                "fgdatabasic:Aircraft/Instruments-3d/pedals/pedals.xml",
                "H:Models/Airport/windsock.xml",
                "H:Models/Airport/beacon.xml",
                // 31
                "T3072824:Objects/e000n50/e007n50/EDDK-Station.gltf",
                "T3072816:Objects/e000n50/e007n50/egkk_maint_1_1.gltf",
                "H:Models/Airport/Jetway/jetway-movable.xml",
        };
    }

    @Override
    public String[] getPreInitBundle() {
        // manche Aircraftteile (z.B. CDU) verwenden FG_ROOT/Aircraft(liegt in fgdatabasic). Braucht auch den Provider im init()
        // "fgdatabasicmodel"?
        return new String[]{"engine", "data",
                //21.11.25: Make these two optional (simply by comment) for speeding up.
                //FlightGear.getBucketBundleName("model"),
                //"sgmaterial",
                "fgdatabasic", "traffic-fg"};
    }

    @Override
    public void customInit() {

        // init FG before using 'arp' in extendSmartModelLoaderForFG()
        arp = initFG();

        extendSmartModelLoaderForFG(arp, new GeneralParameterHandler<List<SGAnimation>>() {
            @Override
            public void handle(List<SGAnimation> animationsOfLoadedModel) {
                // 25.11.25 no longer replace current list. This will override existing list submodel by submodel
                animationList.addAll(animationsOfLoadedModel);
            }
        }, animationList);

        major = 19;
        // Kruecke zur Entkopplung des Modelload von AC policy.
        ModelLoader.processPolicy = new ACProcessPolicy(null);

        menuCycler.add(new DefaultMenuProvider(getDefaultCamera(), (Camera camera) -> {
            ControlPanel m = AnimationControlPanel.buildAnimationControlPanel();
            m.getTransform().setPosition(new Vector3(-0, 0, -1.5));
            ControlPanelMenu menu = new ControlPanelMenu(m);
            return menu;
        }));
    }

    public static void extendSmartModelLoaderForFG(AircraftResourceProvider arp, GeneralParameterHandler<List<SGAnimation>> animationHandler, List<SGAnimation> currentAnimationList) {
        SmartModelLoader.register("H", new SmartModelLoader() {
            @Override
            public void loadModelBySource(String prefix, String modelname, String bundleUrl, ResourcePath optTexturePath, ModelBuildDelegate delegate) {
                //TerraSync Model
                currentAnimationList.clear();
                String bundlename = FlightGear.getBucketBundleName("model");
                SmartModelLoader.defaultSmartModelLoader.loadModelBySource(bundlename, modelname, bundleUrl, optTexturePath, delegate);
            }
        });

        // 9.10.24 'A' is still needed for setting aircraftresourceprovider with aircraft bundle name
        SmartModelLoader.register("A", new SmartModelLoader() {
            @Override
            public void loadModelBySource(String prefix, String modelname, String bundleUrl, ResourcePath optTexturePath, ModelBuildDelegate delegate) {
                currentAnimationList.clear();
                String bundleNameOfCurrentAircraft = bundleUrl;
                if (StringUtils.contains(bundleNameOfCurrentAircraft, "/")) {
                    bundleNameOfCurrentAircraft = StringUtils.substringAfterLast(bundleNameOfCurrentAircraft, "/");
                }
                // 'basename' is the name used in the XML files for referencing itself, eg."Aircraft/c172p/..."
                String basename = null;//"xx";
                if (StringUtils.contains(modelname, "777")) {
                    basename = "777";
                }
                if (StringUtils.contains(modelname, "asi") ||
                        StringUtils.contains(modelname, "c172")) {
                    basename = "c172p";
                }
                if (StringUtils.contains(modelname, "bluebird") ||
                        StringUtils.contains(modelname, "mag-compass") ||
                        StringUtils.contains(modelname, "digital-clock")) {
                    basename = "bluebird";
                }
                if (basename == null) {
                    throw new RuntimeException("unknown aircraft basename");
                }
                arp.setAircraftDirAndBundle(basename, bundleNameOfCurrentAircraft);
                SmartModelLoader.defaultSmartModelLoader.loadModelBySource(bundleNameOfCurrentAircraft, modelname, bundleUrl, optTexturePath, delegate);
            }
        });

        terrasyncSmartModelLoader = new SmartModelLoader() {
            @Override
            public void loadModelBySource(String prefix, String modelname, String bundleUrl, ResourcePath optTexturePath, ModelBuildDelegate delegate) {
                // a TerraSync tile/bucket
                currentAnimationList.clear();
                String tname = prefix;//StringUtils.substringBefore(modelname, ":");

                String no = StringUtils.substring(tname, 1);
                String bundlename = FlightGear.getBucketBundleName(no);
                //modelname = StringUtils.substringAfter(modelname, ":");
                Bundle bundle = BundleRegistry.getBundle(bundlename);
                SmartModelLoader.defaultSmartModelLoader.loadModelBySource(bundlename, modelname, bundleUrl, optTexturePath, delegate);
            }
        };

        SmartModelLoader.register("T3072824", terrasyncSmartModelLoader);
        SmartModelLoader.register("T3072816", terrasyncSmartModelLoader);
        // replace existing default loader with a XML ready one including bundle loading
        SmartModelLoader.defaultSmartModelLoader = new SmartModelLoader() {
            @Override
            public void loadModelBySource(String bundlename, String modelname, String bundleUrl, ResourcePath optTexturePath, ModelBuildDelegate delegate) {
                if (bundlename == null) {
                    logger.error("bundlename is null");
                }
                currentAnimationList.clear();
                Bundle bundle = BundleRegistry.getBundle(bundlename);
                BuildResult result;
                if (bundle == null) {
                    final SceneNode destination = new SceneNode();
                    result = new BuildResult(destination.nativescenenode);
                    AbstractSceneRunner.instance.loadBundle(bundleUrl != null ? bundleUrl : bundlename, (Bundle b_isnull) -> {
                        Bundle b = BundleRegistry.getBundle(bundlename);
                        addPossibleXmlModelFromBundle(b, modelname, bundleUrl, animationHandler, optTexturePath, delegate);
                        /*if (res.getNode() != null) {
                            destination.attach(new SceneNode(res.getNode()));
                        } else {
                            /*destination.attach(redCube);
                            redCube.getTransform().setPosition(new Vector3());* /
                        }*/
                    });
                } else {
                    addPossibleXmlModelFromBundle(bundle, modelname, bundleUrl, animationHandler, optTexturePath, delegate);
                }

            }
        };

    }

    /**
     * Init FG components that are needed for model load, but nothing more.
     * Similar to FlightGear.init(7);
     * Die Reihenfolge orientiert sich an FG fgIdleFunction()
     * System swerden hier nocht nicht angelegt.
     * 21.10.17: Now without initFG.
     */
    private static AircraftResourceProvider initFG() {
        // Some aircraft components like Instruments3D/CDU reside in 'fgdatabasic'
        FgBundleHelper.addProvider(new SimpleBundleResourceProvider("fgdatabasic"));
        AircraftResourceProvider arp = new AircraftResourceProvider();
        FgBundleHelper.addProvider(arp);
        // 24.11.24 also needed because it sets up the property tree
        FlightGearModuleBasic.init(null, null);
        return arp;
    }

    //@Override
    public static void addPossibleXmlModelFromBundle(Bundle bundle, String modelname, String bundleUrl, GeneralParameterHandler<List<SGAnimation>> animationHandler, ResourcePath optTexturePath, ModelBuildDelegate delegate) {
        BundleResource br = BundleResource.buildFromFullString(modelname);
        br.bundle = bundle;
        String extension = br.getExtension();
        BuildResult result = null;
        if (extension.equals("xml")) {
            SGLoaderOptions opt = new SGLoaderOptions();
            // Meanwhile we only use a single property tree (also FgVehicleLoader)
            opt.setPropertyNode(FGGlobals.getInstance().get_props());

            result = SGReaderWriterXML.buildModelFromBundleXML(br, opt, (bpath, destinationNode, alist) -> {
                if (alist != null) {
                    //animationList.addAll(alist);
                    animationHandler.handle(alist);
                }
                //logger.debug(Scene.getCurrent().getWorld().dump("  ", 1));
            });
            delegate.modelBuilt(result);
        } else {
            //9.1.22 result = new BuildResult(ModelFactory.asyncModelLoad(br, EngineHelper.LOADER_USEGLTF).nativescenenode);
            //result = super.addModelFromBundle(bundle, modelname);
            SmartModelLoader.simpleSmartModelLoader.loadModelBySource(bundle.name, modelname, bundleUrl, optTexturePath, delegate);
        }
    }

    @Override
    public void customUpdate(double tpf) {
        updateAnimations(flightGearProperties, animationList, getDefaultCamera());
        FgAnimationUpdateSystem.updateCallbacks();
        FgAnimationUpdateSystem.updateSound(tpf);
    }

    public static void updateAnimations(FlightGearProperties flightGearProperties, List<SGAnimation> animationList, Camera camera) {
        Point mouselocation = Input.getMouseDown();

        // Update FG property tree...
        flightGearProperties.update();

        // ... and then the animations. Picking ray is needed for click/pick animations?
        if (animationList != null) {
            List<NativeCollision> intersections = null;
            if (mouselocation != null) {
                Ray pickingray = camera.buildPickingRay(camera.getCarrier().getTransform(), mouselocation);
                // 13.3.24: No longer pass pickingray to any animation and do intersection check again and again. This is very inefficient.
                // Instead pass the objects hit.
                if (pickingray != null) {
                    intersections = pickingray.getIntersections();
                }
            }
            AURequestHandler auRequestHandler = new AURequestHandler();
            for (SGAnimation animation : animationList) {
                animation.process(intersections, auRequestHandler);
            }
        }

    }

    static class AURequestHandler implements RequestHandler {
        @Override
        public boolean processRequest(Request request) {
            return false;
        }
    }
}
