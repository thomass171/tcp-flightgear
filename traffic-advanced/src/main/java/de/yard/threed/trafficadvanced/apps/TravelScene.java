package de.yard.threed.trafficadvanced.apps;


import de.yard.threed.core.Color;
import de.yard.threed.core.Degree;
import de.yard.threed.core.Dimension;
import de.yard.threed.core.Event;
import de.yard.threed.core.IntHolder;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.Payload;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.geometry.Primitives;
import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.HttpBundleResolver;
import de.yard.threed.core.testutil.RuntimeTestUtil;
import de.yard.threed.engine.FirstPersonController;
import de.yard.threed.engine.GenericGeometry;
import de.yard.threed.engine.Input;
import de.yard.threed.engine.KeyCode;
import de.yard.threed.engine.Light;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Mesh;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.apps.ModelSamples;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsHelper;
import de.yard.threed.engine.ecs.EcsSystem;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.TeleportComponent;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.gui.MenuItem;
import de.yard.threed.engine.gui.Text;
import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.engine.platform.ProcessPolicy;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.Settings;
import de.yard.threed.engine.util.NearView;
import de.yard.threed.flightgear.FgBundleHelper;
import de.yard.threed.flightgear.FgTerrainBuilder;
import de.yard.threed.flightgear.FgVehicleLoader;
import de.yard.threed.flightgear.FlightGearMain;
import de.yard.threed.flightgear.FlightGearSettings;
import de.yard.threed.flightgear.SimpleBundleResourceProvider;
import de.yard.threed.flightgear.TerraSyncBundleResolver;
import de.yard.threed.flightgear.TerrainElevationProvider;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.flightgear.core.simgear.scene.model.ACProcessPolicy;
import de.yard.threed.flightgear.ecs.AnimationUpdateSystem;
import de.yard.threed.graph.Graph;
import de.yard.threed.graph.GraphEdge;
import de.yard.threed.graph.GraphEventRegistry;
import de.yard.threed.graph.GraphMovingComponent;
import de.yard.threed.graph.GraphMovingSystem;
import de.yard.threed.graph.GraphPosition;
import de.yard.threed.graph.SimpleGraphVisualizer;
import de.yard.threed.traffic.AbstractTerrainBuilder;
import de.yard.threed.traffic.Destination;
import de.yard.threed.traffic.EllipsoidCalculations;
import de.yard.threed.traffic.GraphBackProjectionProvider;
import de.yard.threed.traffic.GraphTerrainSystem;
import de.yard.threed.traffic.GraphVisualizationSystem;
import de.yard.threed.traffic.LightDefinition;
import de.yard.threed.traffic.ScenerySystem;
import de.yard.threed.traffic.SolarSystem;
import de.yard.threed.traffic.TrafficConfig;
import de.yard.threed.traffic.TrafficGraph;
import de.yard.threed.traffic.TrafficHelper;
import de.yard.threed.traffic.TrafficSystem;
import de.yard.threed.traffic.VehicleBuiltDelegate;
import de.yard.threed.traffic.VehicleLauncher;
import de.yard.threed.traffic.WorldGlobal;
import de.yard.threed.traffic.config.PoiConfig;
import de.yard.threed.traffic.config.SceneConfig;
import de.yard.threed.traffic.config.VehicleConfigDataProvider;
import de.yard.threed.traffic.config.VehicleDefinition;
import de.yard.threed.traffic.config.XmlVehicleDefinition;
import de.yard.threed.traffic.flight.DoormarkerDelegate;
import de.yard.threed.traffic.flight.FlightLocation;
import de.yard.threed.traffic.flight.FlightRoute;
import de.yard.threed.traffic.geodesy.GeoCoordinate;
import de.yard.threed.traffic.osm.OsmRunway;
import de.yard.threed.trafficcore.model.Runway;
import de.yard.threed.trafficcore.model.Vehicle;
import de.yard.threed.trafficfg.AutomoveSystem;
import de.yard.threed.trafficfg.FgBackProjectionProvider;
import de.yard.threed.trafficfg.FgCalculations;
import de.yard.threed.trafficfg.SGGeodAltitudeProvider;
import de.yard.threed.trafficfg.StgCycler;
import de.yard.threed.trafficfg.TravelHelper;
import de.yard.threed.trafficfg.VehicleEntityBuilder;
import de.yard.threed.trafficfg.flight.FlightSystem;
import de.yard.threed.trafficfg.flight.GroundNet;
import de.yard.threed.trafficfg.flight.GroundServicesSystem;
import de.yard.threed.trafficfg.flight.RouteBuilder;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>
 * Views:
 * 1) Blick über Elsdorf auf Ufo/Pilot und Landschaft
 * 2) Blick aus Ufo (attached)
 * 3) ...
 * <p>
 * Der Pilot selber kann umgesetzt werden, er ist aber unsichtbar (zumindest soll er unsichtbar sein.
 * Er ist ein praktisches Hilfsmittel, um die Rotationen intuitiver zu gestalten.
 * <p>
 * 9.3.16: Jetzt auch mit einschaltbarem FPS Controller. Dann geht aber je nach Einstellung im FPS z.B. Pickingray nicht (wegen Mausmovehandling)
 * <p>
 * 31.8.16: Verwendet optional die FG Szenarien und die Tools dazu. Darum auch die Konventionen bzgl. der Erdkoordinaten:
 * The origin of this coordinate system isType the center of the earth. The X axis runs along a line from the center of the earth
 * out to the equator at the zero meridian. The Z axis runs along a line between the north and south poles with north being positive.
 * The Y axis isType parallel to a line running through the center of the earth out through the equator somewhere in the Indian Ocean.
 * <p>
 * Das Model/Pilot/Cycle Konzept geaendert:
 * Es gibt jetzt n Modelle (statische wie Tower, bewegliche wie Shuttle, Ufo, etc;NavigatorModel als Default). Der Pilot (CAmera) kann zwischen den Models
 * cyclen und dann innerhalb der Model verschiedene Views durchcyclen (Taste V analog FG). Er ist aber immer irgendwie attached. Zusätzlich
 * können sich die beweglichen Model an verschiedene Positionen bewegen (auch animiert). Die Modelle sind immer in der Scene
 * an einer ihrer Positionen, auch wenn der Viewer gerade woanders attached ist. Der Pilot hat weiter sein eigentlich unsichtbares
 * Model mit der Camera, der Intuition wegen.
 * <p>
 * Keys:
 * v cyclen der verschiedenen Views. Pilot bleibt nicht unbedingt an seinem Model, aber Model an seinem Platz.
 * p cyclen position des Models. Das aktuelle Model bewegt sich mitsamt Pilot an andere Position. (11.1.17: Navigator setzt ausser Start auch POI List)
 * t teleport zweistufig (mit CTRL). Nutzt auch die POI Liste.
 * <p>
 * Cyclen der Position ist Model abhängig. Nur Navigator hat POI Liste. Shuttle hat ORbit. 777-200 hat EDDK C4.
 * <p>
 * PGAGEUP/PAGEDOWN um Höhe zu ändern (23.1.17: nur navigator).
 * <p>
 * 01.03.2017:
 * 28.9.18: Eigentlich ist diese Scene fuer alles 3D maessige mit Traffic, ob nun Platzrunde oder Zieverich Rundweg.
 * 8.5.19:Analog GroundService... umbenannt: FlightScene->TravelScene
 * 26.10.21: Echte outside viewpoints wie Flat gibts hier doch gar nicht, oder?
 * 30.1.24: Still uses bundles from local directory (full Terrasync). But full sgmaterial is not available). See todo at bundlelist.
 */
public class TravelScene extends FlightTravelScene {
    static Log logger = Platform.getInstance().getLog(TravelScene.class);
    Light light;
    private static final int WIDTH = 1024;
    private static final int HEIGHT = 768;
    double y = 2;
    double shuttleheight = WorldGlobal.km(200);
    FirstPersonController fps;
    boolean shuttleinorbit;
    // Wofuer ist die world Zwsichenebene. Zum adjusten?
    // 20.3.18: Ja, zum komplettverschieben von allem, um Artefakte wegen Rundungsproblemen zu
    // vermeiden. Das ist was anderes als die Scene.world.
    SceneNode world;
    //EcsEntity[] vm;
    static Vector3 camerapositioninmodel = new Vector3(0, 0.6f, 0);

    // freecam ist mal für Tests
    //22.10.21 boolean freecam = false;
    // erste Position. 6/17: 3=equator,4=Dahlem ,5=EDDK
    int startpos = 5;
    // 17.1.18: Den StgCycler lass ich mal fuer ScneryViewer
    private boolean usestgcycler = false;
    private StgCycler stgcycler;
    //TrafficWorld3D gsw;
    Graph orbit;
    //22.10.21 boolean useteleport = true;
    private FlightRoute platzrunde/*, orbittour*/;
    VehicleDefinition configShuttle;
    private EcsEntity orbitingvehicle = null;
    private boolean avatarInited = false;


    @Override
    public String[] getPreInitBundle() {
        //4.10.21: den brauchts jetzt separat
        // "fgdatabasic" in project is only a small subset. The external should have 'before' prio to load instead of subset.
        // "data" is needed for taxiway ground texture.
        // "sgmaterial" in project also is only a small subset. But there is no way to add a file system resolver here.
        Platform.getInstance().addBundleResolver(new HttpBundleResolver("fgdatabasic@https://ubuntu-server.udehlavj1efjeuqv.myfritz.net/publicweb/bundlepool"),true);
        //30.1.24: TODO fix local directory dependency for full TerraSync
        Platform.getInstance().addBundleResolver(new TerraSyncBundleResolver("/Users/thomas/Projekte/Granada/bundles"), true);
        //29.3.18: Das 777 Bundle wird eh später geladen, allerdings nicht delayed. 11.10.18: kein data mehr
        return new String[]{"engine", "data-old", "data", "fgdatabasic", "fgdatabasicmodel", "sgmaterial",
                FlightGear.getBucketBundleName("model") ,  FlightGearSettings.FGROOTCOREBUNDLE,
                "traffic-advanced","traffic-fg"};
    }

    @Override
    public void customInit() {
        logger.debug("init Flight");

        world = new SceneNode();
        world.setName("FlightWorld");

        FlightGearMain.initFG(new FlightLocation(WorldGlobal.equator020000, new Degree(0), new Degree(0)), null);
        FgBundleHelper.addProvider(new SimpleBundleResourceProvider("fgdatabasicmodel"));
        //4.1.18:TerraSync-model. Ob das hier gut ist?
        FgBundleHelper.addProvider(new SimpleBundleResourceProvider(FlightGear.getBucketBundleName("model")));
        // FG, Position ist initialisiert. 

        //gsw = new DefaultTrafficWorldForFlightScene("EDDK");
        //30.11.23 tw = TrafficWorldConfig.readConfig("data-old", "TrafficWorld.xml");
        TrafficConfig trafficConfig = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("traffic-advanced"), new BundleResource("EDDK.xml"));
        worldPois = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("traffic-advanced"), new BundleResource("world-pois.xml"));
        //30.11.23 sceneConfig = tw.getScene("Flight");
        airportDefinition = trafficConfig.findAirportDefinitionsByIcao("EDDK").get(0);

        //solange es kein Terrain gibt, immer elevation 80; was aber reichlich fraglich ist. Der braucht keine adjustment world
        TerrainElevationProvider tep = TerrainElevationProvider.buildForStaticAltitude(80);
        SystemManager.putDataProvider(SystemManager.DATAPROVIDERELEVATION, tep);

        // 25.9.23: Replace TerrainSystem with ScenerySystem
        ScenerySystem ts = new ScenerySystem(world);
        AbstractTerrainBuilder terrainBuilder = new FgTerrainBuilder();
        terrainBuilder.init(world);
        ts.setTerrainBuilder(terrainBuilder);
        //TerrainSystem ts = new TerrainSystem(world, FlightSceneConfig.simpleearth, FlightSceneConfig.terrainonly);
        SystemManager.addSystem(ts, 0);

        SystemManager.addSystem(new AutomoveSystem());

        initHud();
        if (hud != null && hud.element != null && hud.element.getMesh() != null) {
            hud.setText(0, " ");
        }

        //4.12.23 configShuttle = ConfigHelper.getVehicleConfig(tw.tw, "simpleShuttle");
        orbit = RouteBuilder.buildEquatorOrbit();

        //nearView soll nur die Vehicle abdecken.
        if (enableNearView) {
            nearView = new NearView(getDefaultCamera(), 0.01, 20, this);
        }

        //24.10.21 verschoben nach update buildNavigator(avatartc);

        SceneNode cube = ModelSamples.buildCube(1000, new Color(0xCC, 00, 00));
        if (cube != null) {
            cube.getTransform().setPosition(new Vector3(3987743.8f, 480804.66f, 4937917.5f));
            //cube.setPosition(new Vector3(0, 0, 0));
            world.attach(cube);
        }
        //27.10.21 addLight();
        world.attach(buildSuedpolPillar());
        world.attach(buildNordpolPillar());
        world.attach(buildKoelnerDomPillar());
        world.attach(buildSriLankaPillar());
        //world.attach(ModelSamples.buildAxisHelper(WorldGlobal.EARTHRADIUS * 3, 20000));

        //addSceneUpdater(this);
        addToWorld(world);

        if (usestgcycler) {
            stgcycler = new StgCycler(getDefaultCamera());
        }

        //visualizeTrack soll auch im usermode verfuegbar sein.
        if (visualizeTrack) {
            SystemManager.addSystem(new GraphVisualizationSystem(new SimpleGraphVisualizer(world)));
        }
        EllipsoidCalculations rbcp = TrafficHelper.getEllipsoidConversionsProviderByDataprovider();
        SceneNode helpline = ModelSamples.buildLine(rbcp.toCart(WorldGlobal.eddkoverview.location.coordinates, null), rbcp.toCart(WorldGlobal.elsdorf2000.location.coordinates, null), Color.ORANGE);
        world.attach(helpline);

        SystemManager.addSystem(new FlightSystem());
        SystemManager.addSystem(new AnimationUpdateSystem());

        //16.6.20 request geht hier noch nicht wegen "not inited". Darum weiter vereinfacht initialposition. Das ist nur erstmal so ungefähr für Terrain
        //7.10.21 moved to Sphere initialPosition = WorldGlobal.eddkoverviewfar.location.coordinates;//SGGeod.fromLatLon(gsw.getAirport("EDDK").getCenter());
        //24.5.20 trafficWorld.nearestairport = gsw.airport;

        //10.12.21: Not needed currently in 3D
        ((GraphTerrainSystem) SystemManager.findSystem(GraphTerrainSystem.TAG)).disable();

        //4.1.24 AbstractSceneRunner.instance.httpClient = new AirportDataProviderMock();
        SystemManager.addSystem(new GroundServicesSystem());

        SystemManager.registerService("vehicleentitybuilder", new VehicleEntityBuilder());
        TrafficSystem.genericVehicleBuiltDelegate = new DoormarkerDelegate();
        //30.11.23 rely on generic provider, but add to TrafficSystem. SystemManager.putDataProvider("aircraftconfig", new AircraftConfigProvider(tw));
        TrafficConfig tw1 = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("traffic-advanced"), new BundleResource("vehicle-definitions.xml"));

        TrafficSystem.knownVehicles.addAll(XmlVehicleDefinition.convertVehicleDefinitions(tw1.getVehicleDefinitions()));

        // 29.8.23: Missing for orbit tour, but is it correct? Well, orbittour seems to work.
        ((GraphMovingSystem) SystemManager.findSystem(GraphMovingSystem.TAG)).graphAltitudeProvider = new SGGeodAltitudeProvider();

        //trafficSystem.addAdditionalData(getLocationList(), getGroundNet(),getDestinationNode(),nearView,getVehicleLoader());
        //groundnet is set later
        trafficSystem.locationList=getLocationList();
        trafficSystem.destinationNode=getDestinationNode();
        trafficSystem.nearView=nearView;
        trafficSystem.setVehicleLoader(new FgVehicleLoader());
    }

    @Override
    public EcsSystem[] getCustomTerrainSystems() {
        return new EcsSystem[]{};
    }

    @Override
    public List<Vehicle> getVehicleList() {
        return /*20.11.23 tw*/FlightTravelScene.getVehicleListByName(vehiclelistname);
    }

    @Override
    public ProcessPolicy getProcessPolicy() {
        return new ACProcessPolicy(null);
    }

    @Override
    public MenuItem[] getMenuItems() {
        return new MenuItem[]{
                new MenuItem(null, new Text("Default Trip", Color.RED, Color.LIGHTGRAY), () -> {
                    logger.debug("Default Trip");
                    //startDefaultTrip();
                    TravelHelper.startFlight(Destination.buildRoundtrip(0), getAvatarVehicle());
                    // openclosemenu();
                }),
                new MenuItem(null, new Text("Low Orbit Tour", Color.RED, Color.LIGHTGRAY), () -> {
                    logger.debug("menu: low orbit track");
                    //startOrbitTour();
                    TravelHelper.startFlight(Destination.buildRoundtrip(1), getAvatarVehicle());

                    // openclosemenu();
                }),
                new MenuItem(null, new Text("Enter Equator Orbit", Color.RED, Color.LIGHTGRAY), () -> {
                    logger.debug("menu: equator orbit");
                    //8.3.20 enterEquatorOrbit();
                    TravelHelper.startFlight(Destination.buildForOrbit(true), getAvatarVehicle());
                    //openclosemenu();
                }),
                new MenuItem(null, new Text("Enter Moon Orbit", Color.RED, Color.LIGHTGRAY), () -> {
                    logger.debug("menu: moon orbit");
                    //enterEquatorOrbit();
                    //openclosemenu();
                }),
                new MenuItem(null, new Text("Dahlem", Color.RED, Color.LIGHTGRAY), () -> {
                    //openclosemenu();
                }),
                /*3.1.20: Wegen Verzerrungen kaum nutzbar new MenuItem(null, new Text("Auto Start", Color.RED, Color.LIGHTGRAY), () -> {
                    logger.debug("menu: auto start");
                    SystemManager.putRequest(new Request(RequestRegistry.USER_REQUEST_AUTOMOVE));
                    menuCycler.close();
                }),
                new MenuItem(null, new Text("Teleport", Color.RED, Color.LIGHTGRAY), () -> {
                    logger.debug("menu: teleport");
                    SystemManager.putRequest(new Request(RequestRegistry.USER_REQUEST_TELEPORT, new Payload(new IntHolder(0))));
                }),
                new MenuItem(null, new Text("Load", Color.RED, Color.LIGHTGRAY), () -> {
                    logger.debug("menu: load");
                    Request request = new Request(RequestRegistry.TRAFFIC_REQUEST_LOADVEHICLE, null);
                    requestQueue.addRequest(request);
                    menuCycler.close();
                }),*/
        };
    }

    /**
     * Die Entity enthaelt das FG rotierte Model. Die Needle ist ein Submodel. Darueber muss der Viewport cyclen. Nee, der muss ueber den Pilot cyclen.
     * <p>
     * Das Defaultmodel als Kompassnadel ohne besondere Darstellung. Der Default.
     * Die Nadel verläuft auf der y-Achse mit dem roten Teil (Norden) im positivem y (oben)
     * 14.11.18: Eine GMC beisst sich zwar mit dem Teleporter, aber damit er sich mal im Orbit bewegen.
     * das ist etwas provisorisch
     * 24.1.19: Der Navigator ist eigentlich nur ein Hilfsmittel, um Avatar zu den POIs zu bewegen, er hat aber auch GMC (z.B. fuer Orbittour).
     * 19.10.19: Warum nur Hilfsmittel? Ist halt ein einfaches Vehicle, noch einfacher als Bluebird.
     *
     * @return
     */
    private void buildNavigator(TeleportComponent avatarpc) {
        //4.12.23 VehicleDefinition vehicleConfig = /*DefaultTrafficWorld.getInstance().getConfiguration()*/ConfigHelper.getVehicleConfig(tw.tw, "Navigator");
        // "Navigator" resides in traffic-fg
        TrafficConfig trafficConfig = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("traffic-fg"), new BundleResource("flight/vehicle-definitions.xml"));
        VehicleDefinition vehicleConfig = getVehicleConfig(trafficConfig.getVehicleDefinitions(), "Navigator");

        VehicleLauncher.launchVehicle(new Vehicle("Navigator"), vehicleConfig, null, null, avatarpc, getWorld(), null,
                /*4.12.23 sceneConfig.getBaseTransformForVehicleOnGraph()*/TrafficSystem.baseTransformForVehicleOnGraph, null/*nearView*/,new VehicleBuiltDelegate[]{ ((ecsEntity, config) -> {

                    // Weil der TeleporterSystem.init schon gelaufen ist, muss auch "needsupdate" gesetzt werden, darum stepTo().
                    avatarpc.stepTo(0);
                    addPoisToNavigator(ecsEntity);
                })}, new FgVehicleLoader());
    }

    private void addPoisToNavigator(EcsEntity navigator) {
        // needle muss als parent eingetragen werden. NeeNee, needle ist child von needleforefg.
        SceneNode needleforfg = navigator.scenenode;
        TeleportComponent navigatorpc = new TeleportComponent(needleforfg);

        SceneNode parent = world;
        for (PoiConfig poi : getPoiList(worldPois/*DefaultTrafficWorld.getInstance().getConfiguration()*/)) {
            navigatorpc.addPosition(poi.getName(), parent.getTransform(), poi.getTransform(new FgCalculations()));
        }
        // in general start in EDDK
        navigatorpc.setIndex(startpos - 1);

        navigator.addComponent(navigatorpc);
        navigator.addComponent(new GraphMovingComponent(navigator.scenenode.getTransform()));
    }

    private static List<PoiConfig> getPoiList(TrafficConfig tw) {
        List<PoiConfig> poilist = new ArrayList<PoiConfig>();
        poilist.add(tw.getPoiByName("equator20000"));
        poilist.add(tw.getPoiByName("equator5000indiocean"));
        poilist.add(tw.getPoiByName("equator5000eastafrica"));
        poilist.add(tw.getPoiByName("Dahlem 1300"));
        poilist.add(tw.getPoiByName("EDDK Overview"));
        poilist.add(tw.getPoiByName("EDDK Overview Far"));
        poilist.add(tw.getPoiByName("EDDK Overview Far High"));
        poilist.add(tw.getPoiByName("elsdorf2000"));
        poilist.add(tw.getPoiByName("greenwich500"));
        poilist.add(tw.getPoiByName("Nordpol"));
        return poilist;
    }

    /**
     * Kann sich z.Z. nur in einem einzigen Orbit bewegen.
     */
    private EcsEntity buildShuttle(TeleportComponent avatarpc) {
        EcsEntity shuttle = null;//15.6.21 TODO wrong package new EcsEntity(ModelSamples.buildShuttle());
        shuttle.setName("Shuttle");
        //ViewpointComponent vc = new ViewpointComponent(shuttle.scenenode/*pilot/*shuttle.scenenode*/);
        Vector3 ueberaquator = new Vector3(WorldGlobal.EARTHRADIUS + shuttleheight, 0, 0);

        //shuttle.scenenode.object3d.setPosition(ueberaquator);
        shuttle.helpernode = shuttle.scenenode;

        // attached hinten auf dem Shuttle (7m hoeher, 13 nach hinten, Blick nach vorn
        avatarpc.addPosition("", shuttle.scenenode.getTransform(), new LocalTransform(new Vector3(0, 257, 423), Quaternion.buildFromAngles(new Degree(0), new Degree(0), new Degree(0))));
        //shuttle.addComponent(vc);

        TeleportComponent pc = new TeleportComponent(shuttle.scenenode);
        SceneNode parent = world;
        pc.addPosition("", parent.getTransform(), new LocalTransform(ueberaquator, new Quaternion()));

        shuttle.addComponent(pc);

        return shuttle;
    }


    @Override
    public void initSettings(Settings settings) {
        //settings.targetframerate = 10;
        settings.aasamples = 4;
        //17.10.18: Jetzt kann man VR mal riskieren
        settings.vrready = true;
        settings.minfilter = EngineHelper.TRILINEAR;
        //9.2.20 Versuch weegen TOPOFTHEWORLD. Jetzt erst bei Spherewechsel.
        //settings.far   = 2000000000f;

    }

    @Override
    public Dimension getPreferredDimension() {
        if (Platform.getInstance().isDevmode()) {
            return new Dimension(WIDTH, HEIGHT);
        }
        return null;
    }

    @Override
    public LightDefinition[] getLight() {
        // create a point light
        //PointLight pointLight = new PointLight(Color.WHITE);
        //pointLight = new AmbientLight(Color.BLUE);
        //pointLight.setPosition(new Vector3(0, 2, 1.5f));

        /*7.12.21 DirectionalLight light = new DirectionalLight(Color.WHITE, new Vector3(0, 30000000, 20000000));

        addLightToWorld(light);
        light = new DirectionalLight(Color.WHITE, new Vector3(0, -30000000, -20000000));

        addLightToWorld(light);*/
        return new LightDefinition[]{
                new LightDefinition(Color.WHITE, new Vector3(0, 30000000, 20000000)),
                new LightDefinition(Color.WHITE, new Vector3(0, -30000000, -20000000)),
        };
    }

    public void add(SceneNode model, double x, double y, double z, double scale, Quaternion rotation) {
        addToWorld(model);
        model.getTransform().setPosition(new Vector3(x, y, z));
        model.getTransform().setScale(new Vector3(scale, scale, scale));
        if (rotation != null) {
            model.getTransform().setRotation(rotation);
        }
    }

    private SceneNode buildNodeMarker(Vector3 position) {
        SceneNode model = ModelSamples.buildCube(500000, new Color(0, 0, 0xFF));
        model.setName("MarkerNode");
        setModelLocation(model, new LocalTransform(position, new Quaternion()));
        return model;
    }

    private SceneNode buildSuedpolPillar() {
        SceneNode model = buildPillar(Color.BLUE);
        model.setName("Suedpolpyramide");
        FlightLocation suedpol = new FlightLocation(new GeoCoordinate(new Degree(-90), new Degree(0), 0), new Degree(0));
        setModelLocation(model, suedpol.toPosRot(new FgCalculations()));
        return model;
    }

    private SceneNode buildNordpolPillar() {
        SceneNode model = buildPillar(Color.RED);
        model.setName("Nordpolpyramide");
        FlightLocation nordpol = new FlightLocation(new GeoCoordinate(new Degree(90), new Degree(0), 0), new Degree(0));
        setModelLocation(model, nordpol.toPosRot(new FgCalculations()));
        return model;
    }

    private SceneNode buildSriLankaPillar() {
        SceneNode model = ModelSamples.buildCube(500000, Color.ORANGE);
        model.setName("Srilankapyramide");
        FlightLocation srilanka = new FlightLocation(new GeoCoordinate(new Degree(7), new Degree(80), 500), new Degree(0));
        setModelLocation(model, srilanka.toPosRot(new FgCalculations()));
        return model;
    }

    private SceneNode buildKoelnerDomPillar() {
        SceneNode model = buildPillar(Color.GREEN);
        model.setName("KoelnerDomPillar");
        FlightLocation kd = new FlightLocation(new GeoCoordinate(new Degree(50.9413272f), new Degree(6.95808001), 58.01f), new Degree(0/*92.1f*/));
        setModelLocation(model, kd.toPosRot(new FgCalculations()));
        return model;
    }

    private void setModelLocation(SceneNode model, LocalTransform posrot) {
        model.getTransform().setPosition(posrot.position);
        model.getTransform().setRotation(posrot.rotation);
    }

    private SceneNode buildPillar(Color color) {

        double size = WorldGlobal.km(2000);
        double xangle = -Math.PI / 2;
        double yangle = Math.PI / 2;
        double radiusTop = 0, radiusBottom = size / 2, height = size;
        int radialSegments = 16;
        SimpleGeometry pyramidgeo = Primitives.buildCylinderGeometry(radiusBottom, radiusTop, height, radialSegments, 0, MathUtil2.PI2);
        GenericGeometry geo = new GenericGeometry(pyramidgeo);
        Material pyramidmat = Material.buildPhongMaterial(color);
        Mesh pymesh = new Mesh(geo, pyramidmat, true, true);
        SceneNode model = new SceneNode(pymesh);
        return FlightLocation.rotateFromYupToFgWorld(model);
    }


    Degree angle = new Degree(1);

    boolean populated = false;

    @Override
    public void customUpdate(double currentdelta) {
        //double currentdelta = getDeltaTime();
        //commonUpdate();

        if (!avatarInited) {

            //25.10.21 aus init moved here
            TeleportComponent avatartc = TeleportComponent.getTeleportComponent(UserSystem.getInitialUser());

            if (avatartc == null) {
                logger.warn("no avatar (tc) yet");
            } else {
                // 30.1.24: 'navigator.gltf' not found for some time now. Leads to repeatedly 'not fond' logging.
                buildNavigator(avatartc);

                if (teleporterSystem != null) {
                    teleporterSystem.setActivetc(avatartc);
                }
                avatarInited = true;
            }
        }

        // 17.10.21 Plausipruefungen. Das geht aber erst nach Laden des groundnet. Hmm, vielleicht speter, denn so ist das noch nicht ausgegoren.
        /*if (TrafficHelper.getProjectionByDataprovider() == null) {
            throw new RuntimeException("no projection in 3D!");
        }
        if (TrafficHelper.getProjectionByDataprovider().backProjection == null) {
            throw new RuntimeException("no back projection in 3D!");
        }*/

        // Platzrunde (und wakeup) erst anlegen, wenn das Terrain da ist und worldadjustment durch ist. Schwierig zu erkennen
        boolean terrainavailable = false;
        if (AbstractSceneRunner.getInstance().getFrameCount() > 50) {
            terrainavailable = true;
        }

        if (terrainavailable && GroundServicesSystem.groundnetEDDK != null && !populated) {
            //jetzt muesste ein Groundnet mit 2D Projection da sein.

            //4.10.18: Shuttle ist zu gross. Braucht XML Wrapper und GLTF. Darum vorerst unbrauchbar. TODO ueber config
            //GroundServicesScene.launchVehicle(configShuttle, orbit, new GraphPosition(orbit.getEdge(0)), TeleportComponent.getTeleportComponent(avatar.avatarE), world, null);

            GroundNet groundnet = GroundServicesSystem.groundnetEDDK;
            // jetzt noch die konfigurierten Vehicles, die einfach so rumfahren.
            // Avatar Cockpit Pos lasse ich mal weg. 11.5.19 Warum? Um Teleport auf echte Cockpits zu beschraenken?
             /*29.10.21 Now Per Request in TrafficSystem
            TrafficHelper.launchVehicles(/*DefaultTrafficWorld.getInstance().* /TrafficSystem.vehiclelist, /*gsw.* /groundnet, DefaultTrafficWorld.getInstance().getGroundNetGraph("EDDK"), null/*TeleportComponent.getTeleportComponent(avatar.avatarE)* /, world, getBackProjection()/*gsw.groundnet.projection* /,
                    DefaultTrafficWorld.getInstance().getAirport("EDDK"), sceneConfig);
*/

            // Fuer GraphTest mal eine c172p setzen. Die muss dann auf B_8 stehen mit Heading C_4. Der Graph bekommt keine back projection.
            // TODO auslagenr in einen Test? Zwei c172p sind störend für z.B. Platzrunde
            boolean simplegraphtest = false;
            if (simplegraphtest) {
                //4.12.23 VehicleDefinition configc172p = /*27.12.21 DefaultTrafficWorld.getInstance()*/ConfigHelper.getVehicleConfig(tw.tw, "c172p");
                VehicleDefinition configc172p = TrafficHelper.getVehicleConfigByDataprovider( "c172p",null);
                GraphPosition c_4position = /*gsw.*/groundnet.getParkingPosition(/*gsw.*/groundnet.getParkPos("C_4"));
                TrafficGraph trafficgraph = new RouteBuilder(TrafficHelper.getEllipsoidConversionsProviderByDataprovider()).buildSimpleTestRouteB8toC4(/*gsw.*/groundnet);
                VehicleLauncher.launchVehicle(new Vehicle("c172p"), configc172p, trafficgraph, new GraphPosition(trafficgraph.getBaseGraph().getEdge(0)),
                        TeleportComponent.getTeleportComponent(UserSystem.getInitialUser()), world, null,
                        /*4.12.23 sceneConfig.getBaseTransformForVehicleOnGraph()*/TrafficSystem.baseTransformForVehicleOnGraph, null, new VehicleBuiltDelegate[]{}, new FgVehicleLoader());
            }

            //gsw.graphloaded = null;
            populated = true;
            trafficSystem.groundNet=GroundServicesSystem.groundnetEDDK.groundnetgraph;
        }

        if (Input.getKeyDown(KeyCode.D)) {
            logger.debug("scenegraph:" + dumpSceneGraph());
        }
        // 29.8.23: As long a menu isn't working start orbit tour via key.'R' probably not yet in use.
        if (Input.getKeyDown(KeyCode.R)) {
            logger.debug("orbit tour:" + dumpSceneGraph());
            TravelHelper.startFlight(Destination.buildRoundtrip(1), getAvatarVehicle());
        }

        if (stgcycler != null) {
            stgcycler.update();
        }

        if (fps != null) {
            fps.update(currentdelta);
        } else {
            if (Input.getKeyDown(KeyCode.S)) {
                /*TODO 9.1.17 if (Input.getKey(KeyCode.LeftShift)/* || Input.getKeyDown(KeyCode.RightShift)* /) {
                    if (getShuttle().shuttlespeed > 0) {
                        getShuttle().shuttlespeed -= 0.1f;
                    }
                } else {
                    getShuttle().shuttlespeed += 0.1f;

                }*/
            }

        }

        /*TODO 9.1.17 if (shuttleinorbit) {
            getShuttle().shuttlelocation -= getShuttle().shuttlespeed * currentdelta;
            getShuttle().adjustShuttleOrbitPosition();
        }*/
        //logger.debug("shuttle.worldmodelmatrix="+new Matrix4(shuttle.getWorldModelMatrix()).dump(" "));
        // tile.rotateX(angle);
        // tile.rotateY(angle);
        // tile.rotateZ(angle);
        adjustDimensions();

        if (Input.getKeyDown(KeyCode.Alpha7)) {
            //3.1.20: Programmatisch Navigator über Equator und Avatar an Navigator teleportieren. Ist Spherewechsel.
            String destination = "TopOfTheWorld";
            getDefaultCamera().setFar(2000000000);
            EcsEntity navigator = EcsHelper.findEntitiesByName("Navigator").get(0);
            TeleportComponent tc = TeleportComponent.getTeleportComponent(navigator);
            if (tc.findPoint(destination) == -1) {
                tc.addPosition(destination, new LocalTransform(new Vector3(0, 0, WorldGlobal.km(TOPOFTHEWORLD)),
                        Quaternion.buildRotationY(new Degree(-90))));
            }
            Request request = new Request(UserSystem.USER_REQUEST_TELEPORT, new Payload(new IntHolder(4), destination));
            SystemManager.putRequest(request);

        }
        if (Input.getKeyDown(KeyCode.Alpha8)) {
            //3.1.20: Programmatisch Navigator oder bluebird (mit Avatar drin) auf einen Obrbitgraph teleportieren. Ist Spherewechsel.
        }
        /*if (Input.getKeyDown(KeyCode.Alpha9)) {
            automoveSystem.setAutomoveEnabled(!automoveSystem.getAutomoveEnabled());
        }*/


        // Platzrunde erst anlegen, wenn das Terrain da ist und worldadjustment durch ist.
        if (platzrunde == null && terrainavailable) {
            logger.info("Building Platzrunde");
            // Zum Test direkt mal den Rundflug einblenden
            Runway runway14l = OsmRunway.eddk14L();
            platzrunde = new RouteBuilder(TrafficHelper.getEllipsoidConversionsProviderByDataprovider()).buildFlightRoute(runway14l, null, 0);
            SystemManager.sendEvent(new Event(GraphEventRegistry.GRAPH_EVENT_PATHCREATED, new Payload(platzrunde.getGraph(), platzrunde.getPath())));
        }
        /*18.10.19: Warum soll die denn schon vcorab angelegt werden?
        if (orbittour == null) {
           orbittour=buildOrbitTour();

        }*/
        FlatAirportScene.adjustASI();

        //26.10.18: Jetzt 'S' statt Alpha6. Aber nicht im FPC mode ohne Avatar.
        if (Input.getKeyDown(KeyCode.S) /*10.3.22&& AvatarSystem.getAvatar() != null*/) {
            TravelHelper.startDefaultTrip(getAvatarVehicle());
        }
        if (Input.getKeyDown(KeyCode.Alpha4)) {
            TravelHelper.startFlight(Destination.buildByIcao("EDDF"), getAvatarVehicle());
        }
    }

    /**
     * Eine Equator Orbit Tour ab einer Runway erstellen.
     *
     * @param equatorOrbit
     * @return
     */
    FlightRoute buildOrbitTour(boolean equatorOrbit) {
         /*schwierig zu erkennen, s.o. if (!terrainavailable){
             logger.error("no terrain");
             return null;
         }*/

        logger.info("Building orbittour");
        Runway runway14l = OsmRunway.eddk14L();
        FlightRoute orbittour = new RouteBuilder(TrafficHelper.getEllipsoidConversionsProviderByDataprovider()).buildFlightRoute(runway14l, null, (equatorOrbit) ? 3 : 1);
        SystemManager.sendEvent(new Event(GraphEventRegistry.GRAPH_EVENT_PATHCREATED, new Payload(orbittour.getGraph(), orbittour.getPath())));
            /*for (int i = 0; i < orbitgraph.getNodeCount(); i++) {
                Vector3 loc = orbitgraph.getNode(i).getLocation();
                logger.debug("Orbitvertex:" + i + ":" + SGGeod.fromCart(loc));
                world.attach(buildNodeMarker(loc));
            }*/
        return orbittour;
    }

    @Override
    public EllipsoidCalculations getRbcp() {
        return new FgCalculations();
    }

    //29.10.21 Moved to TrafficSystem. 17.12.21: No longer, needs FgMath
    @Override
    public GraphBackProjectionProvider/*Flight3D*/ getGraphBackProjectionProvider() {
        return new FgBackProjectionProvider();
    }

    /**
     * Eine FlightRoute mit aktuellem Vehicle starten. (ueber Graph).
     * TODO: Das Vehicle muesste sich erst noch zum Start der FlightRoute bewegen.
     */
    void startFlightRoute(FlightRoute route) {

        //14.11.18: Eine GMC beisst sich zwar mit dem Teleporter, aber damit er sich mal im Orbit bewegen.
        //das ist etwas provisorisch
        EcsEntity currentvehicle = getAvatarVehicle();
        boolean mitnavigator = true;
        if (currentvehicle == null) {
            logger.error("avatars vehicle not found");
            return;
        }
        GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(currentvehicle/*(mitnavigator) ? vm[0] : GroundServicesScene.findc172p()*/);
        GraphEdge startedge = route.getPath().getSegment(0).edge;
        route.getPath().startposition = new GraphPosition(startedge);
        gmc.setGraph(route.getGraph(), route.getPath().startposition, null);

        gmc.setPath(route.getPath());
        logger.debug("starting route ");
        orbitingvehicle = currentvehicle;
        //VelocityComponent.getVelocityComponent(orbitingvehicle).hasHyperSpeed=true;

    }


    /*15.11.23 help was removed@Override
    protected void help() {
        if (helphud == null) {
            helphud = Hud.buildForCameraAndAttach(getDefaultCamera(), 1);
            helphud.setText(0, "6 - Start Flight");
            helphud.setText(1, "8 - Load Vehicle");
            helphud.setText(2, "9 - Automove");
            helphud.setText(3, "p - Cycle Position");

            helphud.setText(4, "press h to close");
            //20.2.17? add(hud);
        } else {
            SceneNode.removeSceneNode(helphud);
            helphud = null;
        }
    }*/

    @Override
    protected Log getLog() {
        return logger;
    }

    @Override
    protected void initSpheres() {
        logger.debug("initSpheres");
        SolarSystem solarSystem = new SolarSystem();
        SceneNode sun = solarSystem.build(2 * WorldGlobal.DISTANCEMOONEARTH, WorldGlobal.DISTANCEMOONEARTH/*km(18000)*/);
        addToWorld(sun);
    }

    /*@Override
    public VehicleLoader getVehicleLoader() {
        return new FgVehicleLoader();
    }*/

    @Override
    public VehicleConfigDataProvider getVehicleConfigDataProvider() {
        return null;//4.12.23 new VehicleConfigDataProvider(tw.tw);
    }

    @Override
    public SceneConfig getSceneConfig() {
        //4.12.23 tw = TrafficWorldConfig.readConfig("data-old", "TrafficWorld.xml");
        sceneConfig = null;//tw.getScene("Flight");

        return sceneConfig;
    }

    @Override
    public GeoCoordinate getCenter() {
        return WorldGlobal.EDDK_CENTER;
    }

    /**
     * Die ganze Welt so translaten, dass die Camera bzw. das aktuelle Model im Bereich des Ursprungs liegt, um
     * Rundungseinfluesse zu vermindern. Das wirkt Wunder: Ohne diese Justierung ist in JME bei FPS Rundlauf um Needle
     * ein extremes Ruckeln zu sehen. Auch die Darstellung der Needle erscheint optisch falsch (Nord und Sue versetzt).
     * Bei Unity ist das ganz ähnlich.
     * 9.3.18: pilotpos wird gar nicht mehr gesetzt. Dann koennen wir auch gleich 0 nehmen.
     * Wobei, ich lass das erstmal um vm loszuwerden. Sollte dann spaeter analog auf currententity/position gehen.
     * Ist aber wichtig, ich komme sonst nicht ins 777 Cockpit.
     * 23.11.18: Ich versuch mal ohne auszukommen, weil es nicht richtig verstanden ist und GWT wohl kein Problem hat.
     * Das muss man vielleicht nochmal angehen. Ohne den Aufrf ist der Viewpoint in JME zu weit rechts. reichlich strange.
     * Evtl. ist das schon ein Rundungsproblem. GWT hat da wohl kein Problem.
     */

    static int TOPOFTHEWORLD = 1500000;

    private void adjustDimensions() {
        if (Platform.getInstance().getName().equals("WebGL")) {
            return;
        }
        Vector3 sp = null;//vm[0/*pilotpos*/].scenenode.getTransform().getPosition();
        //6.4.18: mal anders setzen. Das fiese Springen bei Groundmovements loest das aber nicht.
        sp = WorldGlobal.eddkc4.toPosRot(new FgCalculations()).position;
        //14.10.19: ohne springen die GS vehicles immer noch und Viewpoint zu weit rechts. Und C172 Cockpit im Fussraum
        //20.10.19: mit ist die "Dangast-TravelSphere" aber irgendwie an der falschen Stelle. Das passt alles nicht,
        //evtl. z-Buffer Probleme. Das braucht ne Multi-Pass Rendering Lösung, vor allem für JME.
        //world.getTransform().setPosition(new Vector3(-sp.getX(), -sp.getY(), -sp.getZ()));
        //9.2.20: mal ein Versuch für TOPOFTHEWORLD. Hilft aber auch nicht > 100000 (wegen far plane 100000000)
        //world.getTransform().setPosition(new Vector3(0, 0, -TOPOFTHEWORLD));
    }

   
    /*TODO to MovingSystem void adjustShuttleOrbitPosition() {
        double x =  Math.cos(shuttlelocation);
        double z =  Math.sin(shuttlelocation);
        if (model != null) {
            model.object3d.setPosition(new Vector3(x * (WorldGlobal.EARTHRADIUS + shuttleheight), 0, z * (WorldGlobal.EARTHRADIUS + shuttleheight)));

            //logger.debug("shuttlelocation="+shuttlelocation);
            Quaternion rotation = new Quaternion(new Degree(0), Degree.buildFromRadians(-shuttlelocation), new Degree(0));
            model.object3d.setRotation(rotation);
            //if (z >= 0) {
            //		rotation = Quaternion.Euler (90, -z * 90, 0);
            //	} else {

        }
    }*/

    @Override
    protected void runTests() {
        logger.info("Running tests");
        FlightGearMain.runFlightgearTests(world.getTransform().getPosition());
        RuntimeTestUtil.assertNotNull("platzrunde", platzrunde);
        //altitude kann eh nicht auf den Zentimmeter exakt sein.
        RuntimeTestUtil.assertFloat("holding.altitude", 71, SGGeod.fromCart(platzrunde.getPath().getSegment(0).getEnterNode().getLocation()).getElevationM(), 1);
        RuntimeTestUtil.assertFloat("landing.altitude", 71, SGGeod.fromCart(platzrunde.getPath().getSegment(platzrunde.getPath().getSegmentCount() - 1).getEnterNode().getLocation()).getElevationM(), 1);

        logger.info("Tests completed");
    }

    //@Override
    protected SceneNode getDestinationNode() {
        return world;
    }

    /*27.12.21@Override
    protected GraphWorld getGraphWorld() {
        return DefaultTrafficWorld.getInstance();
    }*/


}

class FlightSceneConfig {
    //777 wegen Unity mesh collider mal weglassen

    public static boolean simpleearth = false;
    public static boolean terrainonly = false;//true;
}

