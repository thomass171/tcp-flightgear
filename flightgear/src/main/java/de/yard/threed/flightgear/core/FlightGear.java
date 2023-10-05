package de.yard.threed.flightgear.core;

import de.yard.threed.core.Pair;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.MaterialPool;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.core.Vector3;
import de.yard.threed.flightgear.core.flightgear.main.FGGlobals;
import de.yard.threed.flightgear.core.flightgear.main.FGProperties;
import de.yard.threed.flightgear.core.osg.Node;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterialLib;
import de.yard.threed.flightgear.core.simgear.scene.model.SGReaderWriterXML;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterial;


import de.yard.threed.flightgear.core.simgear.scene.tgdb.Obj;
import de.yard.threed.flightgear.core.simgear.structure.SGException;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeDocument;

import de.yard.threed.core.BuildResult;

/**
 * Hauptklasse zum Kapseln der FG Implementierung.
 * <p/>
 * Created by thomass on 20.02.16.
 */
public class FlightGear {
    static Log logger = Platform.getInstance().getLog(FlightGear.class);
    //jetzt In system prop final static String resources = "/Applications/FlightGear.app/Contents/Resources";
    //jetzt In system prop public final static String fgroot = resources + "/data";
    final static String terrasync = "/Users/thomas/flightgearhome/TerraSync";
    // Dasselbe Material kann unter verschiedenen Namen auftauchen (z.B. Road und Freeway)
    static MaterialPool materials = new MaterialPool();
    //static EngineHelper pf = ((Platform)Platform.getInstance());
    static boolean materialsloaded = false;
    //Praktisch zum Testen, weil es Zeit spart
    public static Material simplematerial = null;
    //24.4.17:Nicht mehr gz
    public static final String refbtg = "terrain/3056410.btg";
    public static final String refbtggltf = "flusi/terrain/3056410.gltf";
    // center einmal ausgelesen und dann hier hinterlegt. Aus FG Log, da ist es double: 4043285.357099 469612.002095 4893927.379193
    //wegen double public static Vector3 refbtgcenter = new Vector3(4043285.2, 469612.0, 4893927.5);
    public static Vector3 refbtgcenter = new Vector3(4043285.357099, 469612.002095, 4893927.379193);
    //Das OSG oder OpenGL Coordinate system nutzen
    //12.7.17: Deprecated, weil es jetzt klar sein dürfte, dass FG Komponenten immer das FG Coord System fuer Model nutzen muessen (http://wiki.flightgear.org/Howto:3D_Aircraft_Models)
    @Deprecated
    public static boolean useosgcoordinatesystem = true;
    // globals Codeweiche
    public static boolean myfg = true;
    // Default Optionen (frueher fest verdrahtet
    public static String[] argv = new String[]{
            "--aircraft=777-200",
            // "--aircraft-dir=" + Platform.getInstance().getSystemProperty("MY777HOME")
            //jetzt Bundle
            "--aircraft-dir=777"
    };
    // 26.6.17: Um erkenn zu koennen, dass FG nutzbar ist.
    //24.3.18 jetzt in modules public static boolean inited = false;

    public static SceneNode buildBTG(boolean roadsonly) {
        String smodel = refbtg;
        return buildBTG(/*new BundleResource(*/new BundleResource(BundleRegistry.getBundle("data"),refbtg), roadsonly);
    }

    public static SceneNode buildBTG(/*NativeResource*/BundleResource file, boolean roadsonly) {
        if (!materialsloaded && simplematerial == null) {
            loadMaterials();
        }
        Obj obj = new Obj();
        Node n = obj.SGLoadBTG( file, null, null);
        return n;
        /*LoadedFile loader = ModelFactory.loadModel(file);
        LoadedObject btg = loader.objects.get(0);
        SceneNode model = null;
        if (roadsonly) {
            SimpleGeometry roads = btg.getGeometryByMaterialName("Road");
            Material mat;
            if (simplematerial == null) {
                mat = materials.get("Road");
            } else {
                mat = simplematerial;
            }
            //mat.setWireframe(true);
            model = new SceneNode(new Mesh(new GenericGeometry(roads), mat));

        } else {
            if (simplematerial != null){
                materials = new MaterialPool();
                materials.put("simple",simplematerial);
            }
            model = loader.buildModel(btg, materials);
        }

        // Die Positionierung erfolgt real, nix verschieben auf (0,0,0). Das soll dann doch der Aufrufer machen.
        Vector3 v = btg.geolist.get(0).getVertices().getElement(0);
        //v = btg.geolist.get(0).getVertices().getElement(btg.geolist.get(0).getVertices().size() - 1);
        logger.info("btg loaded with center=" + SGGeodesy.SGCartToGeod(loader.gbs_center) + "(" + new Vector3(loader.gbs_center).dump("") + "), getFirst vertex="+new Vector3(v).dump(""));
        //model.setPosition(new Vector3(MathUtil2.subtract(new Vector3().vector3, v)));
        //Die Koordinaten/Vertices sind alle relativ zum Center. Das ist absolut. Darum dorthin positionieren.
        model.setPosition(new Vector3(loader.gbs_center));
        //Model m = new Model();
        //m.add(model);
        return model;*/
    }

    /*public static SceneNode buildTile() throws ResourceNotFoundException, InvalidDataException {
        SceneNode m = new SceneNode();
        m.add(loadTile("e000n50/e006n51"));
        m.add(loadTile("e000n50/e006n50"));
        return m;
    }*/

   /* public static SceneNode loadTile(String geocode) throws ResourceNotFoundException, InvalidDataException {
        String dir = terrasync + "/Terrain/" + geocode;
        NativeResource df = new FileSystemResource(dir);
        List<NativeResource> files = pf.listFiles(df);
        SceneNode m = new SceneNode();
        int cnt = 0;
        for (NativeResource f : files) {
            if (cnt > 3) {
                // break;
            }
            if (StringUtils.endsWith(f.getName(), ".stg")) {
                InputStream ins = pf.getRessourceManager().loadResourceSync(f).inputStream;
                LoaderSTG loader = new LoaderSTG(ins);
                for (String objectbase : loader.objectbase) {
                    SceneNode model = buildBTG(new FileSystemResource(dir + "/" + objectbase + ".gz"), false);
                    m.add(model);
                    cnt++;
                }
            }
        }
        return m;
    }*/

    public static void loadMaterials() {
        String fgroot = Platform.getInstance().getConfiguration().getString("FG_ROOT");
        String materialbasexml = fgroot + "/Materials/base/materials-base.xml";
        String materialeuropexml = fgroot + "/Materials/regions/europe.xml";
        String defaultsummer = fgroot + "/Materials/default/global-summer.xml";
        SGMaterial.loadMaterial(materialbasexml, materials);
        SGMaterial.loadMaterial(materialeuropexml, materials);
        SGMaterial.loadMaterial(defaultsummer, materials);
        materialsloaded = true;
    }

    /**
     * Von Anfang an so wie FG, aber ohne AircraftModel.
     * Liest das gesamte Model rekursiv mit allem.
     */
    public static SceneNode read777200(BundleResource modelfile) {
        return new SceneNode(SGReaderWriterXML.buildModelFromBundleXML(modelfile, null).getNode());

    }

    /**
     * kleines Flightdeck mit einigem auskommentiertem zum testen
     *
     * @return
     */
    /*15.9.17 public static SceneNode readFlightdeckSmall() {
        String modelfile = "/Users/thomass/Projekte/FlightGear/MyAircraft/My-777/Models/flightdeck-200-small.xml";
        ReadResult result = ModelRegistry.getInstance().readNode(null, modelfile, new de.yard.threed.fg.osgdb.Options());
        logger.debug("readresult=" + result.message());
        return result.getNode();
    }*/

    /**
     * zum Testen
     *
     * @return
     */
      /*15.9.17public static SceneNode readPedestal() {
        String modelfile = "/Users/thomass/Projekte/FlightGear/MyAircraft/My-777/Models/pedestal.xml";
        ReadResult result = ModelRegistry.getInstance().readNode(null, modelfile, new de.yard.threed.fg.osgdb.Options());
        logger.debug("readresult=" + result.message());
        return result.getNode();
    }*/

      /*15.9.17public static void readComponent() {
        // testweise immer erstmal EFIS
        String modelfile = "/Users/thomass/Projekte/FlightGear/MyAircraft/My-777/Models/Instruments/EFIS/efis-ctl1.xml";

        //SGPropertyNode destnode = new SGPropertyNode("root");
        try {
            // PropsIO.readProperties(modelfile,destnode);
            ReadResult result = ModelRegistry.getInstance().readNode(null, modelfile, new de.yard.threed.fg.osgdb.Options());
            logger.debug("readresult=" + result.message());
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }

        /*    PlatformFactory.getInstance().loadResource("/Users/thomass/Projekte/FlightGear/MyAircraft/My-777/Models/Instruments/EFIS/efis-ctl1.xml", new RessourceLoadingListener() {
            //PlatformFactory.getInstance().loadResource("/Users/thomass/Projekte/FlightGear/MyAircraft/My-777/Models/flighdeck-200.xml", new RessourceLoadingListener() {
                @Override
            public void onLoad(byte[] xmlbuf) {
                try {
                    NativeDocument doc = PlatformFactory.getInstance().parseXml(xmlbuf);
                    parseModel(doc);
                } catch (NativeException e) {
                    //TODO
                    e.printStackTrace();
                }
            }


            @Override
            public void onError(Exception e) {

            }
        });* /

    }*/

    /**
     * Der Aufbau ist immer aehnlich (evtl. auch gemischt):
     * <p/>
     * 1) Pfad zum AC
     * 2) offset
     * 3) effects
     * 4 ) animations
     * 5) models
     *
     * @param doc
     */
    private static void parseModel(NativeDocument doc) {
        SGPropertyNode startnode = new SGPropertyNode("", null);
        //TODO readProperties(doc, startnode, 0, false);
//NativeNodeList pathlist = doc.getElementsByTagName("PropertyList/path");
        //      NativeNode path = pathlist.getItem(0);
        //    logger.debug("path="+path.getNodeValue());
    }

    /**
     * Erstmal nicht als Messagebox sondern als Console (und loggen).
     *
     * @param s
     * @param s1
     */
    public static void fatalMessageBox(String s, String s1) {
        logger.error("Fatal: " + s + " " + s1);
        System.err.println("Fatal: " + s + " " + s1);
    }

    /**
     * Erstmal nicht als Messagebox sondern als Console(und loggen).
     *
     * @param s
     */
    public static void guiErrorMessage(String s, SGException e) {
        logger.error(s + " " + e.getMessage());
        System.err.println("Fatal: " + s + " " + e.getMessage());
    }

    /**
     * Die Hauptinitialisierung (quasi wie aus main)
     * Nur init bis zu einem bestimmten idle state, keine Hauptschleife (fgOSMainLoop) wie im echten.
     * 9.1.17: deprecated, weil FG nicht 1:1 portiert wird, sondern nur per SubSystem.
     */
    /*MA23 @Deprecated
    public static void init(int uptoidlestate, String[] argv) {
        clear();
        if (Main.fgMainInit(argv.length, argv,true) != 0) {
            logger.error("Main.fgMainInit failed. Returning");

            return;
        }
        // Bei 4 beginnen, weil die anderen nicht aktiv sind
        Main.idle_state = 4;
        while (Main.idle_state <= uptoidlestate) {
            fgOSMainLoop();
        }
    }*/

    /**
     * Singletons und andere statische Objekte leeren, soweit möglich. Wichtig fuer Tests, weil dann mehrfach ein init aufgerufen wird.
     */
    private static void clear() {
        //TODO geht irendwie nicht SgResourceManager.clear();
    }

    public static String getenv(String name) {
        return Platform.getInstance().getConfiguration().getString(name);
    }

    /**
     * Der "innere" Teil der fgOSMainLoop in fg_os_osgviewer.fgOSMainLoop()
     *
     * @return
     */
    /*MA23 private static int fgOSMainLoop() {
        /*viewer->setReleaseContextAtEndOfFrameHint(false);
        if (!viewer->isRealized())
            viewer->realize();
        while (!viewer->done()) * /
        {
            //fgIdleHandler idleFunc = globals.get_renderer().getEventHandler()->getIdleHandler();
            //if (idleFunc)
            //    (*idleFunc)();
            Main.fgIdleFunction();
            FGGlobals.globals.get_renderer().update();
            //viewer->frame( globals->get_sim_time_sec() );
        }

        //return status;
        return 0;
    }*/


    public static void fgSplashProgress(String s) {

    }

    /**
     * Wegen C# Typisierung kann die nicht nach Pair. Oder?
     *
     * @param t
     * @param i
     * @return
     */
    public static Pair<String, Integer> make_pair(String t, int i) {
        return new Pair<String, Integer>(t, i);
    }

    public static Pair<BundleResource, Integer> make_pair(BundleResource t, int i) {
        return new Pair<BundleResource, Integer>(t, i);
    }

    /*30.9.19 public static void setupRegistry() {
        // TODO eleganter gestalten
        //MA17ModelRegistry.getInstance().registerCallbacks();
        //MA17Registry.setReadFileCallback(ModelRegistry.getInstance());

        // 25.4.17: Jetzt eigene LoaderRegitryFactory
        //LoaderRegistry.addLoader("xml", new LoaderFactoryXml());


    }*/

    /**
     * ohen Suffix ".stg". Geht auch mit "model".
     * 
     * @param bucket
     * @return
     */
    public static String getBucketBundleName(String bucket) {
        String bundlename = BundleRegistry.TERRAYSYNCPREFIX + bucket;
        return bundlename;
    }

    /**
     * Helper fuer alle, die die FG engine mit init() nicht verwenden wollen, z.B. Tests oder GLTF Builder
     * Das Bundle SGMaterialLib.BUNDLENAME muss schon geladen sein.
     * Soll eigentlich ohne FGProperties und FGGlobals arbeiten, das geht aber wegen der Auswertung von Conditions aber nur schlecht bzw. gar nicht.
     * Also muss doch der init rein. Ein Cache kann hier aber noch nicht angelegt werden, denn dafuer brauchts ein Center.
     * Der ganze FG Kram soll ja eh in ECS enden, und dann relativiert sich das alles wieder.
     * 28.12.17
     */
    public static SGMaterialLib loadMatlib(){
        // brauch auch einen Init wegen PropertyTree
        //FlightGear.init(0, FlightGear.argv);
        FlightGearModuleBasic.init(null,null);
        // Material einlesen
        //SGPath mpath = new SGPath(FGGlobals.globals.get_fg_root());
        //mpath.append(FGProperties.fgGetString("/sim/rendering/materials-file"));
        String mpath;// = FGProperties.fgGetString("/sim/rendering/materials-file");
        //28.6.17: preferences.xml wird nicht mehr gelesen
        mpath ="Materials/regions/materials.xml";
        //28.12.17: season wird eigentlich gar nicht ausgewertet. Doch, ueber eine Condition.
        FGProperties.fgSetString("/sim/startup/season","summer");
        //muss Aufrufer machen TestFactory.loadBundleSync(SGMaterialLib.BUNDLENAME);
        SGMaterialLib matlib = new SGMaterialLib();
        //"Materials/regions/materials.xml"
        if (!matlib.load( mpath, FGGlobals.globals.get_props())) {
            logger.error("matlib.load failed");
            return null;
        }
        
        return matlib;
    }

    /**
     * Model laden ohne neu zu orientieren, Offsets aus XML beachten, zoffset dazu und kapseln.
     * 
     * Aus GroundServicesScene.
     * Fuer Nutzung im graph wird - wohl historisch gewachsen - anders rotiert. Sonst wird hier nicht rotiert.
     * Dann laufen die Modelle FG üblich entlang x mit z nach oben.
     * 1.3.18: Ein FG Model hat wegen AC Policy z nach oben. Das sollte sich hier nicht mehr aendern.Graph hin oder her.
     * Um es auszurichten, zB. Norden, kann die orientierung reingegeben werden. Das ist aber unuebersichtlich. Soll
     * der Aufrufer selber rotieren. Und deshalb wird hier auch der zoffset nicht mehr gesetzt. Das muss nach oder
     * mit der Rotation passieren. Nee, doch besser hier, denn die Model haben evtl ja auch noch eigene Offsets.
     * Und nochmal nee, das ist doch kein Grund. Den Offset kann man hier lassen ok, ist praktisch.
     * Aber rotieren mache ich von aussen. Dann bleibt das gelieferte model im bekannten FG space
     * und attachen bleibt uebersichtlich.
     * Also, zoffset wird hier in die bestehenden Offsets eingebaut, dann wird die node gekapselt und
     * geliefert, ohne zu rotieren.
     *
     * 2.3.18: Ist jetzt redundant zu TrafficSystem.loadVehicle().
     * @param br
     * @param zoffset

     * @return
     */
    public static SceneNode loadFgModel(BundleResource br, float zoffset/*, boolean forgraphuse,Quaternion orientierung */) {
        // Die Defaultorientierung im Graph ist hinten (-z). Und in die Richtung wird sie gedreht 90 Grad nach rechts um y.
        // Die FG Aircraft Model haben die Spitze Richtung -x und z nach oben.
        // Das FG Model hat die z-Ache nach oben. Das ist im Prinzip schon mal passend, weil wir in der z0 Ebene sind.
        // Der Graph wird aber rotieren, weil er von einem y-up Graphen bzw. Default -z ausgeht. Darum 
        // erstmal nach y-up rotieren (-90 an x). Und noch mal weil er falsch rum steht.
        // 2.5.17: Um y jetzt -90 statt 90, damit die Spitze richtig zeigt.
        //Quaternion yuprot = new Quaternion(new Degree(-90), new Degree(-90), new Degree(0));
        BuildResult result = SGReaderWriterXML.buildModelFromBundleXML(br, null);
        if (result != null && result.getNode() != null) {
            // Das model hat evtl. die offsets in seinem transform
            // 30.10.17: Die Modelle sind aber nicht alle mit z0 bei den Gears. MErkwuerdig.
            SceneNode node = new SceneNode(result.getNode());
            Vector3 p = node.getTransform().getPosition();
            p = new Vector3(p.getX(), p.getY(), p.getZ() + zoffset);
            node.getTransform().setPosition(p);
          /*  if (forgraphuse) {
                return rotateFgModelForGraph(node);
            } else {
                if (orientierung!= null){
                    node = new SceneNode(node);
                    node.getTransform().setRotation(orientierung);
                }else {*/
            // dann noch nicht rotieren
            //1.3.18. transform kapseln
            return new SceneNode(node);
            //}
            //}
            //1.3.18. transform kapseln
            //return new SceneNode(node);
        }
        return null;
    }

}