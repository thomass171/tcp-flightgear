package de.yard.threed.trafficfg.apps;

import de.yard.threed.core.BuildResult;
import de.yard.threed.core.Color;
import de.yard.threed.core.Point;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.Geometry;
import de.yard.threed.engine.Input;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Mesh;
import de.yard.threed.engine.Ray;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.apps.ModelPreviewScene;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.ModelLoader;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.RequestHandler;
import de.yard.threed.flightgear.FgBundleHelper;
import de.yard.threed.flightgear.SimpleBundleResourceProvider;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.SGLoaderOptions;
import de.yard.threed.flightgear.core.flightgear.main.AircraftResourceProvider;
import de.yard.threed.flightgear.core.flightgear.main.FGGlobals;
import de.yard.threed.flightgear.core.simgear.scene.model.ACProcessPolicy;
import de.yard.threed.flightgear.core.simgear.scene.model.SGAnimation;
import de.yard.threed.flightgear.core.simgear.scene.model.SGReaderWriterXML;

import java.util.ArrayList;
import java.util.List;

/**
 * An extension of the generic model viewer for viewing FG XML model.
 * Like super class not using ECS.
 * Derived from Granada:ModelPreviewSceneExt.
 * <p>
 * 23.1.17: Extra, weil hier dann auch die OSG Registry verwendet werden muss.
 * 04.01.2018: Das loeschen beim Wechsel klappt nicht immer,z.B. bei A777. Und C172p war schon mal doppelt da?? 10.1.18: Koennte behoben sein.
 * 22.12.18: Not using trafficConfig. But "AircraftDir" is needed for FG aitcraft. Doesn't know "optionals" and
 * "zoffset" and so on. But that is acceptable.
 * FG Animations are supported manually (outside ECS).
 *
 * Model with visual aimations:
 * - Windturbine(index 1)
 * - tower radar(0)
 * <p>
 * Only for XML? Should due to super class.
 */
public class FgModelPreviewScene extends ModelPreviewScene {
    public Log logger = Platform.getInstance().getLog(FgModelPreviewScene.class);
    // scale 0.016 passend für Tower
    //Die Animationen des aktuellen Model inkl. aller Submodel.
    List<SGAnimation> animationList;
    AircraftResourceProvider arp;

    /**
     * 9.1.18: Vorne stehen die Bundlenamen (vor dem ersten ':') oder ein 'A' als Kenner fuer ein FGAircraft (mit Zusatzdefinitionen), H ist TerraSync-model
     * 9.1.22:Trenner ist jetzt ':' statt '-'
     */
    @Override
    public String[] getModelList() {
        return new String[]{
                //Antenna on top rotates.
                "T3072824:Objects/e000n50/e007n50/egkk_tower.xml",
                //windturbine should have two rotations (currently only one)
                "H:Models/Power/windturbine.xml",
                "A:Models/777-200.xml",
                "",
                "",
                //5
                "fgdatabasicmodel:Models/Airport/Pushback/Douglas.xml",
                //hat ein rotierendes und damit blinkendes Rundumlicht
                "fgdatabasicmodel-Models/Airport/Pushback/Goldhofert.xml",
                // 7:Die AI Aircrafts rotieren "richtig".
                "fgdatabasicmodel-AI/Aircraft/737/737-AirBerlin.xml",
                "fgdatabasicmodel-AI/Aircraft/747/744-KLM.xml",
                "fgdatabasicmodel-Followmeausfgaddonkopiert/followme.xml",
                "fgdatabasicmodel-Catering6620KopiertAusTerrasync/catruckmed-lsg1.xml",
                "fgdatabasicmodel-Models/Airport/Pushback/Forklift.xml",
                "fgdatabasicmodel-Models/Airport/Pushback/Military.xml",
                "fgdatabasicmodel-FuelTruck/Fuel_Truck_Short_VolvoFM.xml",
                //
                "",
                //15
                "",
                "",
                "",
                // 18: Fehlen c172 Teile aus FGROOT? Zumindest sieht die C172 so nicht sehr huebsch aus.
                "A-Models/c172p.xml",
                "fgdatabasic-Aircraft/Instruments-3d/garmin196/garmin196.xml",
                "777-Models/OHpanel.xml",
                //21:nochmal aus data (als Testreferenz)
                "data-flusi/Overhead-777/OHpanel.xml",
                "EDDK-eddk-latest.xml",
                //23: gltf geht wohl, hat aber keine ACPolicy. Darum xml. 29.12.18: EDDK-Terminal1 findet er nicht mehr. Das liegt nicht mehr in fgbasicmodel, sondern in ???
                "EDDK-final/EDDK-Terminal1.xml",
                "EDDK-EDDK-StarB.gltf",
                "railing-loc.xml",
                "bluebird:Models/bluebird.xml",
                "traffic-fg:flight/navigator.xml",
                "fgdatabasic:Aircraft/Instruments-3d/pedals/pedals.xml",

        };
    }

    @Override
    public String[] getPreInitBundle() {
        // manche Aircraftteile (z.B. CDU) verwenden FG_ROOT/Aircraft(liegt in fgdatabasic). Braucht auch den Provider im init()
        // "fgdatabasicmodel"?
        return new String[]{"engine", "data", FlightGear.getBucketBundleName("model"), "sgmaterial", "fgdatabasic", "traffic-fg"};
    }

    @Override
    public void customInit() {
        major = 1;
        arp = initFG();
        // Kruecke zur Entkopplung des Modelload von AC policy.
        ModelLoader.processPolicy = new ACProcessPolicy(null);
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

    boolean isloadingterrysynmodel = false;

    @Override
    public BuildResult loadModel(String modelname) {
        String dir = null;
        String bundlename = null;
        boolean resolved = false;
        // Erst die spezifischen versuchen, dann die aus Superklasse
        if (StringUtils.startsWith(modelname, "T")) {
            // a TerraSync tile/bucket
            String tname = StringUtils.substringBefore(modelname, ":");

            String no = StringUtils.substring(tname, 1);
            bundlename = FlightGear.getBucketBundleName(no);
            modelname = StringUtils.substringAfter(modelname, ":");
            resolved = true;
        } else {
            if (StringUtils.startsWith(modelname, "H")) {
                //TerraSync Model
                bundlename = FlightGear.getBucketBundleName("model");
                Bundle bundle = BundleRegistry.getBundle(bundlename);
                if (bundle == null) {
                    if (!isloadingterrysynmodel) {
                        AbstractSceneRunner.instance.loadBundle(bundlename, (r) -> {

                        });
                    }
                    isloadingterrysynmodel = true;
                    //9.1.22 ich glaube das geht nicht mehr richtig
                    logger.warn("bundle not yet loaded. Cycle back and come back here.");
                    return new BuildResult((NativeSceneNode) null);
                }
                //dir = Platform.getInstance().getSystemProperty("FG_HOME");
                modelname = StringUtils.substring(modelname, 2);
                resolved = true;
            } else {
                if (StringUtils.startsWith(modelname, "R")) {
                    dir = ((Platform) Platform.getInstance()).getConfiguration().getString("FG_ROOT");
                    modelname = StringUtils.substring(modelname, 2);
                    resolved = true;
                } else {
                    if (StringUtils.startsWith(modelname, "A")) {
                        int index = StringUtils.indexOf(modelname, "-");
                        modelname = StringUtils.substring(modelname, index + 1);
                        //29.12.18: FlightGearAircraft ist doch schon durch XML abgeloest
                        //FlightGearAircraft aircraft = FlightGearAircraft.get(StringUtils.substring(modelname, index + 1));
                        //aircraftdir, bundlename und modelname sind gluecklicherweise herleitbar.
                        String basename = "xx";
                        if (StringUtils.contains(modelname, "777")) {
                            basename = "777";
                        }
                        if (StringUtils.contains(modelname, "c172p")) {
                            basename = "c172p";
                        }
                        bundlename = basename;//aircraft.bundlename;
                        //modelname = aircraft.modelname;
                        arp.setAircraftDir(basename/*aircraft.aircraftdir*/);
                        resolved = true;
                    }
                }
            }
        }

        BuildResult result = null;

        if (resolved) {
            final String mname = modelname;
            final String bname = bundlename;
            if (bundlename != null) {
                Bundle bundle = BundleRegistry.getBundle(bundlename);
                if (bundle == null) {
                    final SceneNode destination = new SceneNode();
                    result = new BuildResult(destination.nativescenenode);
                    AbstractSceneRunner.instance.loadBundle(bundlename, (Bundle b_isnull) -> {
                        Bundle b = BundleRegistry.getBundle(bname);
                        BuildResult res = addModelFromBundle(b, mname);
                        if (res.getNode() != null) {
                            destination.attach(new SceneNode(res.getNode()));
                        } else {
                            destination.attach(redCube);
                            redCube.getTransform().setPosition(new Vector3());
                        }
                    });
                } else {
                    result = addModelFromBundle(bundle, modelname);
                }
            } else {
                result = new BuildResult(redCube.nativescenenode);
                redCube.getTransform().setPosition(new Vector3());
            }
        } else {
            result = super.loadModel(modelname);
        }
        return result;
    }

    @Override
    public BuildResult addModelFromBundle(Bundle bundle, String modelname) {
        BundleResource br = BundleResource.buildFromFullString(modelname);
        br.bundle = bundle;
        String extension = br.getExtension();
        BuildResult result = null;
        if (extension.equals("xml")) {
            SGLoaderOptions opt = new SGLoaderOptions();
            opt.setPropertyNode(FGGlobals.getInstance().get_props());

            // Das mit der animationlist duerfte durch das Ansammeln auch fuer geschachtelte Model gehen.
            animationList = new ArrayList<SGAnimation>();

            result = SGReaderWriterXML.buildModelFromBundleXML(br, opt, (bpath, destinationNode, alist) -> {
                if (alist != null) {
                    animationList.addAll(alist);

                }
            });
        } else {
            //9.1.22 result = new BuildResult(ModelFactory.asyncModelLoad(br, EngineHelper.LOADER_USEGLTF).nativescenenode);
            result = super.addModelFromBundle(bundle, modelname);
        }
        /*29.12.18 model = null;
        // Das model hat evtl. die offsets in seinem transform
        model = new SceneNode(result.getNode());
        //scale = 0.5f;
        // Der dump bringt hier nichts, weil der Load async ist und spaeter eingehangen wird.
        //logger.info("Building imported model. scale=" + scale+", tree:"+model.dump("",0));
        logger.info("Building imported model.");
        model.getTransform().setScale(new Vector3(scale, scale, scale));
        */

        //29.12.18 addToWorld(model);
        return result;
    }

    @Override
    public void customUpdate() {
        Point mouselocation = Input.getMouseDown();

        // for tower radar
        FGGlobals.getInstance().get_props().getNode("/sim/time/elapsed-sec", true).setDoubleValue(elapsedsec);
        // windturbine
        FGGlobals.getInstance().get_props().getNode("/environment/wind-from-heading-deg", true).setDoubleValue(elapsedsec * 20);
        // 5.10.2017: wind-speed mal besser konstant. Duerfte noch nicht implementierte spin sein.
        FGGlobals.getInstance().get_props().getNode("/environment/wind-speed-kt", true).setDoubleValue(40/*elapsedsec * 10 % 200*/);

        if (animationList != null) {
            Ray pickingray = null;
            if (mouselocation != null) {
                getDefaultCamera().buildPickingRay(getDefaultCamera().getCarrier().getTransform(), mouselocation);
            }
            AURequestHandler auRequestHandler = new AURequestHandler();
            for (SGAnimation animation : animationList) {
                animation.process(pickingray, auRequestHandler);
            }
        }

    }

    class AURequestHandler implements RequestHandler {
        @Override
        public boolean processRequest(Request request) {
            return false;
        }
    }
}
