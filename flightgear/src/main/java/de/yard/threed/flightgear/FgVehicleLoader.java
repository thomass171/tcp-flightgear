package de.yard.threed.flightgear;

import de.yard.threed.core.BuildResult;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.flightgear.core.FlightGearModuleBasic;
import de.yard.threed.flightgear.core.SGLoaderOptions;
import de.yard.threed.flightgear.core.flightgear.main.AircraftResourceProvider;
import de.yard.threed.flightgear.core.flightgear.main.FGGlobals;
import de.yard.threed.flightgear.core.flightgear.main.FgInit;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.scene.model.SGAnimation;
import de.yard.threed.flightgear.core.simgear.scene.model.SGReaderWriterXML;
import de.yard.threed.traffic.SimpleVehicleLoader;
import de.yard.threed.traffic.VehicleLauncher;
import de.yard.threed.traffic.VehicleLoadedDelegate;
import de.yard.threed.traffic.VehicleLoader;
import de.yard.threed.traffic.config.VehicleDefinition;
import de.yard.threed.trafficcore.model.Vehicle;

import java.util.ArrayList;
import java.util.List;

/**
 * 9.3.21: Sehr stark FG lastig (List<SGAnimation> animationList, SGPropertyNode rootpropertyNode), darum ins Module "flightgear".
 * Da kann er aber nicht hin, weil er auch in traffic gebraucht wird.
 * Darum entkoppelt mit VehicleLauncher. Der ist wirklich nur zum Laden von XML-Modellen. Der muss nach FG.
 * 10.11.21: Though it also can load pure GLTF?
 */
public class FgVehicleLoader implements VehicleLoader {

    private static Log logger = Platform.getInstance().getLog(FgVehicleLoader.class);

    /**
     * Loads a configured vehicle (eg. a FG Aircraft).
     * The bundle needed is loaded async if not yet available. Also model build and load is async, so use a delegate
     * instead of return value.
     * <p>
     * Model laden ohne neu zu orientieren, Offsets aus XML beachten, zoffset dazu und kapseln.
     * irgendwie redundant zu FlightGear.loadFgModel()
     * <p>
     * Adding the captain position to the avatar teleporter was moved to ?? for decoupling.
     * <p>
     * No ECS entity is built here, that is done by the caller.
     * 9.11.21 Moved here from static FgVehicleLauncher
     * 5.2.24: Aren't Vehicle and VehicleDefinition somehow redundant?
     */
    @Override
    public void loadVehicle(Vehicle vehicle, VehicleDefinition config, VehicleLoadedDelegate loaddelegate) {
        //FGinit kann erst nach laden des Bundle gemacht werden.
        //ach, direkt oben anfangen wie FG
        //27.4.17: brauchts den fginit immer noch? Warum eigentlich? Zumindest mal fuer den AircraftPRovider und damit auch FgGlobals
        //4.8.17: FlightGear.init(5, FlightGear.argv);
        //21.10.17: jetzt ohne initFG.
        // if (usearp) {
        logger.debug("Load vehicle " + config.getName());
        //animationlist will be populated later async
        List<SGAnimation> animationList = new ArrayList<SGAnimation>();

        AbstractSceneRunner.instance.loadBundle(config.getBundlename(), (Bundle bundle) -> {
            if (bundle == null) {
                logger.error("bundle not loaded. Not building vehicle " + config.getName());
            } else {
                //  13.11.25 For simplicity also vehicles will use the global tree (See also README.md)
                //SGPropertyNode destinationProp = new SGPropertyNode(config.getName() + "-root");
                SGPropertyNode destinationProp = FGGlobals.getInstance().get_props();
                //arp.setAircraftDir(aircraft.aircraftdir);
                FgBundleHelper.addProvider(new AircraftResourceProvider(config.getAircraftdir()));
                //} else {
                //  FlightScene.initFG(null, aircraft.aircraftdir/*fgname/*"My-777"*/);
                //Mit state 9 ist 777-200 schon geladen, aber wer weiss wo positioniert?.
                //Der read777200 liest auch nur das eine ac file;oder? Nee, er liest komplett. Aber zeigt nichts an.
                //}
                BundleResource br = BundleResource.buildFromFullString(config.getModelfile());
                br.bundle = bundle;
                if (config.getModelfile().equals("Models/777-200.xml") && FlightGearModuleBasic.inited) {
                    //27.3.18:Auch die Teile oberhalb des reinen Models laden. Das ist ein Provisorium, erstmal nur fuer Tests genutzt.
                    //Ob und wie das braucht, ist voellig unklar. Immerhin liegen da aber z.B. View und das FDM.
                    //30.9.19: Ich lass das mal weg. FDM nutz ich eh nicht, und die Views? Wohl auch nicht.
                    // Aber es geht hier generell um das "-set.xml". Wär vielleicht ganz huebsch. TODO pruefen.
                    //30.9.19. Ich lass das lieber noch mal drin.
                    FgInit.fgInitAircraft(false, true/*loadaircraft*/, "777-200", config.getAircraftdir(), destinationProp);
                }
                // PropertyTree is needed for animations. 3.4.18: Each vehicle will have its own tree.
                SGLoaderOptions opt = new SGLoaderOptions();
                opt.setPropertyNode(destinationProp);
                SceneNode currentaircraft;

                // 9.11.21: Can also load pure GLTF, not only XML. ModelFactory will not do because of 'optionals'.
                BuildResult buildresult = SGReaderWriterXML.buildModelFromBundleXML(br, opt, (bpath, destinationNode, alist) -> {
                    // 4.4.18: The delegate is called for each submodel, so very often!
                    for (String o : config.getOptionals()) {
                        SceneNode.removeSceneNodeByName(o);
                    }
                    if (alist != null) {
                        animationList.addAll(alist);//  xmlloaddelegate.modelComplete( animationList);
                    }
                    // Not really reliable. It might slip through for some reason, so stays after vehicle loading.
                    // But is also checked when new is added.
                    if (AbstractSceneRunner.getInstance().getPendingAsyncCount() == 0) {
                        FgBundleHelper.removeAircraftSpecific();
                    }
                });
                currentaircraft = new SceneNode(buildresult.getNode());

                SceneNode lowresNode = null;
                String lowresfile = config.getLowresFile();
                if (lowresfile != null) {
                    BundleResource lowresbr = BundleResource.buildFromFullString(lowresfile);
                    lowresbr.bundle = bundle;
                    SGPropertyNode lowresdestinationProp = new SGPropertyNode(config.getName() + "LowRes-root");
                    List<SGAnimation> lowresanimationList = new ArrayList<SGAnimation>();
                    lowresNode = de.yard.threed.flightgear.traffic.ModelFactory.buildModelFromBundleXml(lowresbr, lowresdestinationProp, animationList);
                }
                // In der base node KANN auch eine Rotation sein (manche AIs). Darum ist es doch unbrauchbar
                // und wird gekapselt.
                SceneNode nn = SimpleVehicleLoader.buildVehicleNode(currentaircraft, config.getZoffset());

                // Probably too early to inform delegates, because some asyncs still run. So the animationlist might still be empty here!
                // 4.4.18. The XML loader above also has a delegate that fires for each submodel

                loaddelegate.vehicleLoaded(nn, new FgVehicleLoaderResult(animationList, opt.getPropertyNode()), lowresNode);
                //4.11.25 too early, many async might still be running FgBundleHelper.removeAircraftSpecific();

                logger.debug("vehicle " + config.getName() + " load launched");
            }
        });
    }
}
