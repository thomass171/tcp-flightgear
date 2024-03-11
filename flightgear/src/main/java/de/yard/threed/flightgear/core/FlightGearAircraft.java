package de.yard.threed.flightgear.core;


import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.core.Vector3;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.flightgear.FgBundleHelper;
import de.yard.threed.flightgear.core.flightgear.main.AircraftResourceProvider;
import de.yard.threed.flightgear.core.flightgear.main.FGGlobals;
import de.yard.threed.flightgear.core.simgear.scene.model.SGReaderWriterXML;
import de.yard.threed.core.resource.Bundle;

/**
 * 21.10.17: Ein FG Aircraft, das wegen des AircraftProviders etwas ganz spezeielles ist.
 * 29.12.18: Die Klasse ist doch in config.xml aufgegangen, oder? Mal deprecated.
 * <p>
 */
@Deprecated
public class FlightGearAircraft {
    private Vector3 pilotpos;
    //public SceneNode sn;
    public EcsEntity entity;
    //wo ist "vorne". 25.10.17: Das ist hoffentlich uebeall gleich
    public String[] optionals;
    // 30.10.17: Manche Model habe z0 nicht bei den Gears. Keine Ahnung, wie FG damit klar kommt.
    // Evtl. ist das aber auch die Folge meines async load?
    public float zoffset = 0;

    public static FlightGearAircraft[] aircrafts = {
           /*jetzt in xml new FlightGearAircraft("777","777", "Models/777-200.xml", new Vector3(-22.60f, -0.5f, 0.8f), "777", new String[]{
                    "RHLND.spot",
                    "LHLND.spot",
                    "LandingLights",
                    "LandingLightCone_L",
                    "LandingLightCone_C",
                    "LandingLightCone_R",
                    "Dome light",
                    "TaxiLightCone",
                    "LandingLightCone",
            },6),
            //
            new FlightGearAircraft("c172p","c172p", "Models/c172p.xml", new Vector3(0.16f, -0.14f, 0.236f), "c172p", new String[]{
                    "TaxiLightCone",
                    "LandingLightCone",
                    "nav-light-right",
                    "nav-light-left",
                    "nav-light-tail",
                    "BeaconOff",
                    "BeaconOffX",
                    "strobe1",
                    "strobe2",
                    "glas",
                    "glas_interior",
                    "glas_interior_sides",
                    "leftwindow",
                    "leftwindow_interior",
                    "rightwindow",
                    "rightwindow_interior",
            },1.2f),
            
            new FlightGearAircraft("followme","fgdatabasicmodel", "Models/Airport/Followmeausfgaddonkopiert/followme.xml", new Vector3(1.91f, -0.33f, 1.3f), "followme", new String[]{},0)*/
    };
    public String name,bundlename, modelname, aircraftdir;

    /**
     * pilotpos ist die Captain Kopfhöhe
     */
   /* public FlightGearAircraft(String name, String bundlename, String modelname, Vector3 pilotpos, String aircraftdir, String[] optionals,float zoffset) {
        this.name=name;
        this.bundlename = bundlename;
        this.modelname = modelname;
        this.pilotpos = pilotpos;
        this.aircraftdir = aircraftdir;
        this.optionals = optionals;
        this.zoffset = zoffset;
    }*/

    public Vector3 getPilotPosition() {
        return pilotpos;
    }

    public static FlightGearAircraft get(String name) {
        for (FlightGearAircraft a : aircrafts) {
            if (a.name.equals(name)) {
                return a;
            }
        }
        return null;
    }

    public static void removeCones(FlightGearAircraft ac) {
        for (String o : ac.optionals) {
            SceneNode.removeSceneNodeByName(o);
        }
       
    }

    /**
     * Laden eines FG Aircraft. Das Bundle muss schon da sein, das Model wird aber async geladen.
     * D.h., die gelieferte Node ist zunächst leer.
     * 
     * Der Avatar Teleporter wird um die Captain Position ergänzt. ist jetzt wegen coupling ausgelagert.
     * 2.3.18: Deprecated zugunsten Kopie loadVehicle in TrafficSystem.
     * @param aircraft     
     * @return
     */
    @Deprecated
    public static SceneNode loadAircraft(FlightGearAircraft aircraft/*,EcsEntity avatar*/) {
        //FGinit kann erst nach laden des Bundle gemacht werden.
        //ach, direkt oben anfangen wie FG
        //27.4.17: brauchts den fginit immer noch? Warum eigentlich? Zumindest mal fuer den AircraftPRovider und damit auch FgGlobals
        //4.8.17: FlightGear.init(5, FlightGear.argv);
        //21.10.17: jetzt ohne initFG.
       // if (usearp) {
        FgBundleHelper.removeAircraftSpecific();
            //arp.setAircraftDir(aircraft.aircraftdir);
        FgBundleHelper.addProvider(new AircraftResourceProvider(aircraft.aircraftdir));
        //} else {
          //  FlightScene.initFG(null, aircraft.aircraftdir/*fgname/*"My-777"*/);
            //Mit state 9 ist 777-200 schon geladen, aber wer weiss wo positioniert?.
            //Der read777200 liest auch nur das eine ac file;oder? Nee, er liest komplett. Aber zeigt nichts an.
        //}
        Bundle bundle = BundleRegistry.getBundle(aircraft.bundlename);
        BundleResource br = BundleResource.buildFromFullString(aircraft.modelname);
        br.bundle = bundle;
        // PropertyTree isType needed for animations
        SGLoaderOptions opt = new SGLoaderOptions();
        opt.setPropertyNode(FGGlobals.getInstance().get_props());
      SceneNode  currentaircraft = new SceneNode(SGReaderWriterXML.buildModelFromBundleXML(br, opt, (bpath,destinationNode,alist) -> {
            // das wird vermutlich zu oft aufgerufen.
            removeCones(aircraft);
         
        }).getNode());
        SceneNode node = currentaircraft;
        Vector3 p = node.getTransform().getPosition();
        p = new Vector3(p.getX(),p.getY(),p.getZ()+aircraft.zoffset);
        node.getTransform().setPosition(p);
        
        //String smodel = "/Users/thomass/Projekte/FlightGear/MyAircraft/My-777/Models/777-200.ac";
        //Nur das Flightdeck
        //String smodel = "/Users/thomass/Projekte/FlightGear/MyAircraft/My-777/Models/flightdeck.ac";
        //addToWorld(currentaircraft);
       /* TeleportComponent pc = TeleportComponent.getTeleportComponent(avatar);
        pc.removePosition("Captain");
        //pc.addPosition("Captain", new PosRot(new Vector3(-22.60f, -0.5f, 0.8f), new Quaternion(new Degree(90), new Degree(0), new Degree(90))));
        pc.addPosition("Captain", currentaircraft, new PosRot(aircraft.getPilotPosition(), aircraft.orientation));*/
        return currentaircraft;

    }
}