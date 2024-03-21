package de.yard.threed.trafficadvanced.apps;

import de.yard.threed.core.Color;
import de.yard.threed.core.Degree;
import de.yard.threed.core.Dimension;
import de.yard.threed.core.DimensionF;
import de.yard.threed.core.Event;
import de.yard.threed.core.IntHolder;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.Payload;
import de.yard.threed.core.Point;
import de.yard.threed.core.SpinnerHandler;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeCollision;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.HttpBundleResolver;
import de.yard.threed.core.testutil.RuntimeTestUtil;
import de.yard.threed.engine.Camera;
import de.yard.threed.engine.FirstPersonController;
import de.yard.threed.engine.Input;
import de.yard.threed.engine.KeyCode;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Mesh;
import de.yard.threed.engine.ObserverComponent;
import de.yard.threed.engine.Ray;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ViewPoint;
import de.yard.threed.engine.apps.ModelSamples;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsSystem;
import de.yard.threed.engine.ecs.EntityFilter;
import de.yard.threed.engine.ecs.InputToRequestSystem;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.TeleportComponent;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.ecs.VelocityComponent;
import de.yard.threed.engine.geometry.ShapeGeometry;
import de.yard.threed.engine.gui.ControlMenuBuilder;
import de.yard.threed.engine.gui.ControlPanel;
import de.yard.threed.engine.gui.ControlPanelArea;
import de.yard.threed.engine.gui.GuiGrid;
import de.yard.threed.engine.gui.Icon;
import de.yard.threed.engine.gui.MenuItem;
import de.yard.threed.engine.gui.PanelGrid;
import de.yard.threed.engine.gui.Text;
import de.yard.threed.engine.gui.TextTexture;
import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.engine.platform.ProcessPolicy;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.Settings;
import de.yard.threed.engine.vr.VrInstance;
import de.yard.threed.flightgear.FgVehicleLoader;
import de.yard.threed.flightgear.FlightGearSettings;
import de.yard.threed.flightgear.TerrainElevationProvider;
import de.yard.threed.flightgear.core.FlightGearModuleBasic;
import de.yard.threed.flightgear.core.simgear.scene.model.ACProcessPolicy;
import de.yard.threed.flightgear.ecs.AnimationUpdateSystem;
import de.yard.threed.flightgear.ecs.PropertyComponent;
import de.yard.threed.graph.GraphEventRegistry;
import de.yard.threed.graph.GraphMovingComponent;
import de.yard.threed.graph.GraphNode;
import de.yard.threed.graph.GraphPath;
import de.yard.threed.graph.GraphProjection;
import de.yard.threed.graph.GraphVisualizer;
import de.yard.threed.graph.SimpleGraphVisualizer;
import de.yard.threed.traffic.AbstractTerrainBuilder;
import de.yard.threed.traffic.Destination;
import de.yard.threed.traffic.EllipsoidCalculations;
import de.yard.threed.traffic.GraphVisualizationSystem;
import de.yard.threed.traffic.PositionHeading;
import de.yard.threed.traffic.RequestRegistry;
import de.yard.threed.traffic.SphereProjections;
import de.yard.threed.traffic.TrafficConfig;
import de.yard.threed.traffic.TrafficHelper;
import de.yard.threed.traffic.TrafficSystem;
import de.yard.threed.traffic.TravelSphere;
import de.yard.threed.traffic.VehicleComponent;
import de.yard.threed.traffic.VehicleLauncher;
import de.yard.threed.traffic.config.VehicleConfigDataProvider;
import de.yard.threed.traffic.config.XmlVehicleDefinition;
import de.yard.threed.traffic.flight.DoormarkerDelegate;
import de.yard.threed.traffic.flight.FlightRouteGraph;
import de.yard.threed.traffic.geodesy.GeoCoordinate;
import de.yard.threed.traffic.osm.OsmRunway;
import de.yard.threed.trafficcore.model.Runway;
import de.yard.threed.trafficcore.model.Vehicle;
import de.yard.threed.trafficfg.AutomoveSystem;
import de.yard.threed.trafficfg.FgCalculations;
import de.yard.threed.trafficfg.TravelGraphVisualizer;
import de.yard.threed.trafficfg.TravelHelper;
import de.yard.threed.trafficfg.VehicleEntityBuilder;
import de.yard.threed.trafficfg.flight.FlightSystem;
import de.yard.threed.trafficfg.flight.FlightVrControlPanel;
import de.yard.threed.trafficfg.flight.GroundNetTerrainBuilder;
import de.yard.threed.trafficfg.flight.GroundServicesSystem;
import de.yard.threed.trafficfg.flight.Parking;
import de.yard.threed.trafficfg.flight.RouteBuilder;

import java.util.ArrayList;
import java.util.List;

import static de.yard.threed.traffic.WorldGlobal.EDDK_CENTER;


/**
 * Eine Darstellung in der z=0 Ebene! Alles in 2D. Nur fuer GroundService Entwicklung. Nutzt die Komponenten, die nach Nasal konvertiert werden
 * so, wie sie auch in FG verwendet würden. Verhält sich also was die Nutzung der GS angeht ähnlich wie FG. Alternativ kann ein Test mit der FG GWT remote console
 * erfolgen.
 * <p>
 * Darum auch ohne Animationen und ohne Bodentextur. Für den SchnickSchnack gibt es Flight(Traffic)Scene.
 * 12.7.17: Die gab737 vehicles sind total deplaziert und machen letztlich auch nicht so viel her. Nehm ich nicht mehr.
 * 21.7.17: Naja, Fueltruck findet sich sonst keiner. Doch mal probieren.
 * 27.10.17: Das kann mit CockpitScene und dann doch Animationen (Effekte) doch zu einer Airportscene zusammenlegen.
 * Vielleicht sogar mit EDDK. Auf jeden Fall aber ohne BTG weil es eine Erdscheibe/flaeche statt Erdkugel verwendet.
 * Um immer noch als Referenz fuer FlightGear Addon.
 * <p>
 * 12.02.2018:Jetzt nur noch mit Teleport.
 * 7.5.19: OsmSceneryScene aufnehemn, weils in sowas wie Tile(Traffic)Scene uebergehen soll.
 * 8.5.19: Weil OsmSceneryScene aufgenommen ist umbenannt: GroundServicesScene->FlatTravelScene(Flat wegen Erdscheibe, Elevation gibts es trotzdem)
 * Dann kann FlightScene TravelScene heissen.
 * 25.9.2020: Generel eine flat traffic Darstellung. Damit kann auch mal Railing hierhin kommen.
 * 07.10.21: Ich nutze das mal lieber nur noch fuer die 2D GroundServices wegen FG. Tiles/Railing sollen ueber BasicTravelScene gehen.
 * 9.10.23: Renamed from FlatTravelScene to FlatAirportScene.
 * 3.2.24: doormarker only optional
 * <p>
 * Created by thomass on 14.09.16.
 */
public class FlatAirportScene extends FlightTravelScene {
    private Log logger = Platform.getInstance().getLog(FlatAirportScene.class);
    // die Aircraft lasse ich mal hier, weil TrafficSystem die nicht so kennt, zumindest noch nicht und nicht fur FG Groundservice.
    //List<ArrivedAircraft> arrivedaircraft = new ArrayList<ArrivedAircraft>();

    //Es gibt zwei verschiedene upVector, einen fuer die Camera und einen für den Graph
    //Vector3 camupvector = new Vector3(0, 1, 0);
    //Vector3 graphupvector = new Vector3(0, 0, 1);
    //Graph osm = null;
    //GroundServicesSystem groundservicessystem = null;
    Parking b_2;
    private SceneNode axishelper = null;
    // Nur Aircraft koennen als Servicerequester markiert werden.
    private AircraftMarker aircraftmarker;
    //private GraphVisualizer visualizer;
    //4.3.18 GroundServiceConfig config = GroundServiceConfig.getConfig();
    //25.9.20 TrafficWorld2D gsw;
    //Erst im Visualizer auf 2D projezieren, nicht schon im groundnet Graph. Geht mit TerrainBuilder aber nicht so einfach.
    //Vielleicht ist das auch nicht der richtige Weg. GroundServicesScsnen soll ja 2D sein.
    //Und groundnet wird vorerst weiter wie in FG intern in 2D arbeiten.
    //public static boolean delayedprojection = false;
    GraphPath platzrunde;
    //10.12.21 GraphTerrainSystem graphTerrainsystem;
    // Visualizer nur fuer Graphpath, nicht Terrain.
    private TravelGraphVisualizer travelGraphVisualizer;

    //7.10.21 static bis es wegkommt
    public static FirstPersonController fpc = null;
    //
    //public static Quaternion cockpitcameraorientation = Quaternion.buildFromAngles(new Degree(90), new Degree(0), new Degree(90));
    EcsEntity markedaircraft = null;
    private boolean enableDoormarker = false;

    @Override
    public String[] getPreInitBundle() {
        //12.10.18: Ohne ROOT kommt eine FM. Und fgdatabasic braucht er spaeter fuer C172P etc.
        //13.12.23 "fgdatabasicmodel" is loaded later when needed during vehicle loading.
        //30.12.23 FlightGearSettings.FGROOTCOREBUNDLE,"fgdatabasic" now have provider for bundlepool, no longer GRANADADIR
        // "fgdatabasic" in project is only a small subset. The external should have 'before' prio to load instead of subset.
        // "data" is needed for taxiway ground texture.
        Platform.getInstance().addBundleResolver(new HttpBundleResolver("fgdatabasic@https://ubuntu-server.udehlavj1efjeuqv.myfritz.net/publicweb/bundlepool"), true);
        return new String[]{"engine", "data", FlightGearSettings.FGROOTCOREBUNDLE, "fgdatabasic", /*9.12.23 "data-old", "fgdatabasic", FlightGearSettings.FGROOTCOREBUNDLE, "osmscenery",*/
                "traffic-advanced", "traffic-fg"};
    }

    /**
     * Runs after super.init()
     */
    @Override
    public void customInit() {
        logger.debug("customInit FlatAirportScene");

        //29.1.23 AbstractSceneRunner.instance.httpClient = new AirportDataProviderMock();
        SystemManager.addSystem(new GroundServicesSystem());
        SystemManager.addSystem(new AutomoveSystem());

        if (VrInstance.getInstance() == null) {
            ((InputToRequestSystem) SystemManager.findSystem(InputToRequestSystem.TAG)).setControlMenuBuilder(new ControlMenuBuilder() {
                @Override
                public GuiGrid buildControlMenu(Camera camera) {
                    return buildControlMenuForScene(camera);
                }
            });
        }
        //31.10.21commoninit();
        //27.3.18 initFG();
        //3.4.18: FGGlobals wird aber fuer Aircraft laden gebraucht
        FlightGearModuleBasic.init(null, null);

        // 27.12.21 jetzt in getSceneConfig
        //initTrafficWorld("GroundServices", vehiclelistname);
        //sceneConfig = /*27.12.21 DefaultTrafficWorld.getInstance().getConfiguration()*/tw.getScene("GroundServices");

        //icao = "EHAM";
        //icao = "EDDF";
        String icao = null;
        //7.10.21 erstmal hardcoded if (tilelist[major].file.equals("EDDK")) {
        icao = "EDDK";
        //}

        //7.5.19: projection erst nach location setzen
        //gsw.projection = new SimpleMapProjection(gsw.airport.getCenter());



        /*24.10.21 jetzt per AvatarSystem
        if (enableFPC) {
            //9.5.19 dann kein Teleport, Observer und auch keine Viewpoints. Avatar kommt an die im Tile hinterlegte Position.
            //Ob die Nutzung des Avatar praktikabel ist, muss sich noch zeigen. Und cameraposition und lookat. Naja, das auch.
            //Avatar ist schon mal nicht gut, weil er eine eigbeaute Rotation hat. Und weil er für Teleport und Cockpit gedacht ist, lass ich ihn
            //bei FPC mal weg.
            Vector3 upVector = new Vector3(0, 0, 1);
            fpc = new FirstPersonController(getDefaultCamera().getCarrierTransform(), upVector);
            getDefaultCamera().getCarrier().getTransform().setPosition(new Vector3(0, 0, 100));
            getDefaultCamera().lookAt(new Vector3(0, 100, 100), new Vector3(0, 0, 1));
            // lieber nicht zu schnell. Evtl. mit Shift kombinieren
            fpc.setMovementSpeed(60/*100* /);
            fpc.setRotationSpeed(60/*100* /);
        } else {
            //23.2.18: Welce (Default)Orientierung ist denn wohl die passendste? Lassen wir erstmal default.
            avatar = Avatar.buildDefault(getDefaultCamera());
            addToWorld(avatar.getSceneNode());
            //aus sphere?
            addOutsidePositions(avatar.pc, sceneConfig.getViewpoints(new ConfigAttributeFilter("icao", icao, true)));
            /*22.10.21teleporterSystem = new TeleporterSystem();
            //anim ist zu ruckelig/fehlerhaft
            teleporterSystem.setAnimated(false);
            SystemManager.addSystem(teleporterSystem, 0);* /
            ObserverComponent oc = new ObserverComponent(getDefaultCamera().getCarrierTransform());
            oc.setRotationSpeed(40);
            avatar.avatarE.addComponent(oc);
            //24.10.21viewingsystem = new ObserverSystem();
            //24.10.21SystemManager.addSystem(viewingsystem, 0);
        }*/

        travelGraphVisualizer = new TravelGraphVisualizer(this);
        //groundservicessystem = new GroundServicesSystem();
        //10.12.21 graphTerrainsystem = new GraphTerrainSystem(this, Scene.getWorld());
        // 7.5.19: Der initiale Tile (2D spezifisch) kommt aus den Arguments (basename), nicht der Config XML.
        //ICAOs laufen (noch?) nicht ueber Tiles
        /*7.10.21 if (!tilelist[major].file.equals("EDDK")) {
            // terrainsystem.setBasename(tilelist[major].file);
            //7.10.21
            TrafficWorld2D.basename = tilelist[major].file;
            logger.info("Using tile " + tilelist[major].file/*TrafficWorld2D.basename* /);
        }*/

        //9.12.21 projection wird ja gar nicht verwendet??
        //10.12.21 travelGraphVisualizer.projection = graphTerrainsystem.projection;

        //trafficSystem = new TrafficSystem(/*visualizer*/);
        //SystemManager.addSystem(new AutomoveSystem());

        // 15.11.17: Eine 738 an 0,0 (FG unknown parkpos). FG hat da Artefakte.
            /*arrivedaircraft.add(buildArrivedAircraft(new Vector3(), new Degree(0), new BundleResource(BundleRegistry.getBundle("fgdatabasicmodel"), "AI/Aircraft/738/738-AirBerlin.xml"),
                    "738", "738-00", 0, null));*/

            /*das Building wird voellig falsch orientiert sein
            BundleResource br = new BundleResource(BundleRegistry.getBundle("fgdatabasicmodel"), "EDDK-fg-CustomScenery/Objects/e000n50/e007n50/eddk-latest.xml");
            SceneNode b = new SceneNode(loadFgModel(br,0));
            addToWorld(b);*/

        /*29.12.23 now in BasicTravelScene if (visualizeTrack) {
            SystemManager.addSystem(new GraphVisualizationSystem(travelGraphVisualizer));
        }*/
        //27.10.21 addLight();
        //SystemManager.addSystem(new TrafficSystem(/*visualizer*/), 0);
        //10.12.21 SystemManager.addSystem(graphTerrainsystem, 0);
        SystemManager.addSystem(new AnimationUpdateSystem());
        SystemManager.addSystem(new FlightSystem());

        //addSceneUpdater(this);

        initHud();
        updateHud();

        SystemManager.putDataProvider(SystemManager.DATAPROVIDERELEVATION, TerrainElevationProvider.buildForStaticAltitude(0));

        //13.12.18 buildControlMenu();
        //7.10.21 activateTile(tilelist[major]);
        /*if (gsw.airport != null) {
            initialTile.center = gsw.airport.getCenter();
        } else {
            //default initialPosition = WorldGlobal.elsdorf0;
        }
        initialTile.nearestairport = gsw.getNearestAirport();*/

        SystemManager.registerService("vehicleentitybuilder", new VehicleEntityBuilder());
        if (enableDoormarker) {
            ((TrafficSystem) SystemManager.findSystem(TrafficSystem.TAG)).addVehicleBuiltDelegate(new DoormarkerDelegate());
        }
        // 24.11.23: AircraftConfigProvider replaced by the more generic VehicleConfigDataProvider
        // but for now with same name and maybe duplicate.
        //SystemManager.putDataProvider("aircraftconfig", new AircraftConfigProvider(tw));
        //30.11.23 rely on generic provider, but add to TrafficSystem. SystemManager.putDataProvider("aircraftconfig", new VehicleConfigDataProvider(tw));
        TrafficConfig tw1 = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("traffic-advanced"), new BundleResource("vehicle-definitions.xml"));
        TrafficSystem.knownVehicles.addAll(XmlVehicleDefinition.convertVehicleDefinitions(tw1.getVehicleDefinitions()));

        //trafficSystem.addAdditionalData(getLocationList(), getGroundNet(),getDestinationNode(),nearView,getVehicleLoader());
        //groundnet is set later

        // 4.12.23 Preload config parallel to later SphereSystem for location list
        TrafficConfig trafficConfig = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("traffic-fg"), new BundleResource("flight/EDDK.xml"));
        airportDefinition = trafficConfig.findAirportDefinitionsByIcao("EDDK").get(0);

        trafficSystem.locationList = getLocationList();
        trafficSystem.destinationNode = getDestinationNode();
        trafficSystem.nearView = nearView;
        trafficSystem.setVehicleLoader(new FgVehicleLoader());
    }

    @Override
    protected void customProcessArguments() {
        if (EngineHelper.isEnabled("enableDoormarker")) {
            enableDoormarker = true;
        }
    }

    /**
     * 29.12.23
     */
    @Override
    public GraphVisualizer getGraphVisualizer() {
        if (travelGraphVisualizer == null) {
            travelGraphVisualizer = new TravelGraphVisualizer(this);
        }
        return travelGraphVisualizer;
    }

    @Override
    public EcsSystem[] getCustomTerrainSystems() {
        return new EcsSystem[]{};
    }

    @Override
    public List<Vehicle> getVehicleList() {
        return getVehicleListByName(vehiclelistname);
    }

    @Override
    public AbstractTerrainBuilder getTerrainBuilder() {
        return new GroundNetTerrainBuilder();
    }

    public static String DEFAULT_TILENAME = "traffic-fg:flight/EDDK-flat.xml";

    @Override
    public String getDefaultTilename() {
        //leads to 2D. 30.11.21:Now needs a bundle name.
        String defaultbasename = "dummy:EDDK";//"B55-B477";
        // 5.12.23: No longer dummy but real config file.
        defaultbasename = DEFAULT_TILENAME;
        return defaultbasename;
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
                    // close menu
                    InputToRequestSystem.sendRequestWithId(new Request(InputToRequestSystem.USER_REQUEST_MENU));
                    TravelHelper.startDefaultTrip(getAvatarVehicle());
                }),
                new MenuItem(null, new Text("Auto Start", Color.RED, Color.LIGHTGRAY), () -> {
                    SystemManager.putRequest(new Request(UserSystem.USER_REQUEST_AUTOMOVE));
                    // close menu
                    InputToRequestSystem.sendRequestWithId(new Request(InputToRequestSystem.USER_REQUEST_MENU));
                }),
                new MenuItem(null, new Text("Teleport", Color.RED, Color.LIGHTGRAY), () -> {
                    SystemManager.putRequest(new Request(UserSystem.USER_REQUEST_TELEPORT, new Payload(new Object[]{new IntHolder(0)})));
                }),
                new MenuItem(null, new Text("Load", Color.RED, Color.LIGHTGRAY), () -> {
                    SystemManager.putRequest(RequestRegistry.buildLoadVehicle(UserSystem.getInitialUser().getId(), null, null, null));
                    // close menu
                    InputToRequestSystem.sendRequestWithId(new Request(InputToRequestSystem.USER_REQUEST_MENU));
                }),
                new MenuItem(null, new Text("Service", Color.RED, Color.LIGHTGRAY), () -> {
                    if (markedaircraft != null) {
                        markDoor(markedaircraft);
                        GroundServicesSystem.requestService(markedaircraft);
                    }
                    // close menu
                    InputToRequestSystem.sendRequestWithId(new Request(InputToRequestSystem.USER_REQUEST_MENU));
                }),
        };
    }


    /**
     * Das markermodel kommt in die world, nicht ans aircraft. Das Vehicle ist ja auch nicht im Aircraft space.
     * Wiese so kompliziert? Weil das aircraft unter der ac policy node hängt. Ist so zumindest
     * verwirrend. Darum in woirld.
     * 6.3.18: Also, die doorposition in der Config ist im Aircraft local space (FG). Da ist es doch am einfachsten, den
     * MArker einfach an das Model zu attachen.
     *
     * @param aircraft
     */

    private void markDoor(EcsEntity/*ArrivedAircraft*/ aircraft) {
        boolean makesimple = true;
        VehicleComponent vc = VehicleComponent.getVehicleComponent(aircraft);
        if (vc == null) {
            throw new RuntimeException("no VehicleComponent in entity " + aircraft.getName());
        }
        String modeltype = vc.config.getModelType();
        if (makesimple) {
            SceneNode marker = ModelSamples.buildAxisHelper(200, 1);
            //6.12.23 marker.getTransform().setPosition(/*27.12.21 DefaultTrafficWorld.getInstance().getConfiguration()*/getAircraftConfiguration(modeltype).getCateringDoorPosition());
            marker.getTransform().setPosition(TrafficHelper.getVehicleConfigByDataprovider(null, modeltype).getCateringDoorPosition());
            SceneNode basenode = VehicleLauncher.getModelNodeFromVehicleNode(aircraft.getSceneNode());
            /*aircraft.*/
            basenode.attach(marker);
        } /*6.12.23 unused for a very long time else {
            Vector3 p = aircraft.scenenode.getTransform().getPosition();
            Degree heading = null;//8.3.18 aircraft.heading;//getAircraftHeading(aircraft.entity);
            SceneNode marker = ModelSamples.buildAxisHelper(200, 1);
            //warum wohl der marker auch 90plus braucht??
            marker.getTransform().setRotation(Quaternion.buildRotationZ(new Degree(-(90 + heading.getDegree()))));
            Vector3 dp = /*27.12.21 DefaultTrafficWorld.getInstance().getConfiguration()* /getAircraftConfiguration(modeltype).getCateringDoorPosition();
            Vector3 worldcateringdoorpos = null;
            try {
                worldcateringdoorpos = GroundServicesSystem.groundnetEDDK.getProjectedAircraftLocation(p, heading, /*27.12.21 DefaultTrafficWorld.getInstance().getConfiguration()* /getAircraftConfiguration(modeltype).getCateringDoorPosition());
                marker.getTransform().setPosition((worldcateringdoorpos));
                aircraft.scenenode.attach(marker);
                addToWorld(marker);
            } catch (NoElevationException e) {
                e.printStackTrace();
            }
        }*/
        //aircraft.scenenode.attach(new ACProcessPolicy("").process(marker,null,null));
        //aircraft.scenenode.attach(marker);
    }


    public static Vector2 getAircraftDirection(EcsEntity aircraft) {
        GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(aircraft);

        Vector3 dir = gmc.getCurrentposition().getDirection();
        return new Vector2(dir.getX(), dir.getY());
    }

    @Override
    public void initSettings(Settings settings) {
        //27.1.18 für VR Tests
        if (Platform.getInstance().isDevmode()) {
            settings.targetframerate = 5;//20;//60;
        }
        settings.aasamples = 4;
        settings.vrready = true;
        settings.minfilter = EngineHelper.TRILINEAR;
    }

    @Override
    public Dimension getPreferredDimension() {
        if (Platform.getInstance().isDevmode()) {
            return new Dimension(1200, 900);
        }
        return null;
    }


    boolean populated = false;

    /**
     * die seitliche Schrittgroesse abhaenig von der Hohe.
     *
     * @return
     */
    @Override
    public void customUpdate(double tpf) {
        //double tpf = getDeltaTime();
        //commonUpdate();

        //if (gsw.graphloaded != null) {
        if (GroundServicesSystem.groundnetEDDK != null && !populated) {
            // 7.5.19: Das kann das groundnet sein, aber auch irgendein anderer Graph, auf den die Vehicles gesetzt werden können.

            //TODO: doof den graph so zu setzen
            //29.12.23 no longer needed
            // travelGraphVisualizer.graph = /*DefaultTrafficWorld.getInstance().getGroundNetGraph("EDDK")*/GroundServicesSystem.groundnetEDDK.groundnetgraph.getBaseGraph();//gsw.groundnet.groundnetgraph;


            // Die konfigurierten Vehicles, die einfach so rumfahren.
            // Evtl. wurde groundnet aber gar nicht wirklich geladen

            /*29.10.21 Now Per Request in TrafficSystem
            TrafficHelper.launchVehicles(TrafficSystem/*DefaultTrafficWorld.getInstance()* /.vehiclelist, DefaultTrafficWorld.getInstance().getGroundNet("EDDK"), DefaultTrafficWorld.getInstance().getGroundNetGraph("EDDK"), (AvatarSystem.getAvatar() == null) ? null : TeleportComponent.getTeleportComponent(AvatarSystem.getAvatar().avatarE), getWorld(), null,
                    DefaultTrafficWorld.getInstance().getAirport("EDDK"), sceneConfig);
*/

            // Evtl. wurde groundnet aber gar nicht wirklich geladen
            if (GroundServicesSystem.groundnetEDDK != null) {
                if (platzrunde == null) {
                    // Zum Test direkt mal den Rundflug einblenden
                    Runway runway14l = OsmRunway.eddk14L(/*(TerrainElevationProvider) SystemManager.getDataProvider(SystemManager.DATAPROVIDERELEVATION)*/);
                    FlightRouteGraph tour = new RouteBuilder(TrafficHelper.getEllipsoidConversionsProviderByDataprovider()).buildFlightRouteGraph(runway14l, GroundServicesSystem.groundnetEDDK.projection, 0);
                    platzrunde = tour.getPath();
                    SystemManager.sendEvent(new Event(GraphEventRegistry.GRAPH_EVENT_PATHCREATED, new Payload(tour.getGraph(), platzrunde)));
                }
                GroundServicesSystem.groundnetEDDK.groundnetgraph.multilaneenabled = true;
            }
            //gsw.graphloaded = null;
            populated = true;
            //20.3.24 now via TRAFFIC_EVENT_GRAPHLOADED trafficSystem.groundNet = GroundServicesSystem.groundnetEDDK.groundnetgraph;
        }

        // P ist key fuer Teleport
        if (Input.getKeyDown(KeyCode.B)) {
            SystemManager.pause();
        }
        /*9.5.19: 1/2 zum blättern. Wer ist usecase1?if (Input.getKeyDown(KeyCode.Alpha1)) {
            executeUsecase(1);
        }
        if (Input.getKeyDown(KeyCode.Alpha2)) {
            TrafficSystem.requestVehicleMove(gsw);
        }*/
        /*7.10.21 TODO per InputToRequest oder SphereSystem if (Input.getKeyDown(KeyCode.Alpha1)) {
            cycleMajor(1, tilelist.length);
        }
        if (Input.getKeyDown(KeyCode.Alpha2)) {
            cycleMajor(-1, tilelist.length);
        }*/
        if (Input.getKeyDown(KeyCode.Alpha3)) {
            // Followme für neues gelandetes Aircraft. 24.4.18: Das ist erstmal nur der Use Case fuer ein arriving aircraft.
            // Followme kann man dann spaeter noch ergaenzen.
            Degree angle = new Degree(180);
            angle = new Degree(260);
            PositionHeading target = new PositionHeading(new Vector3(-1200, 1300, 0), angle);
                /*24.7.17 GraphEdge graphintonet = groundnet.createPathIntoGroundNet(target);
                trafficsystem.requestFollowMe(second737, /*followme,* / graphintonet.from, groundnet.getParkPos("C_7"));*/
            Util.nomore();
            //27.12.21 TODO GroundServicesSystem.requestArrivingAircraft(DefaultTrafficWorld.getInstance().getGroundNet("EDDK").getParkPos("C_7"));
        }

        if (Input.getKeyDown(KeyCode.Alpha5)) {
            // Service for the marked aircraft.
            if (markedaircraft != null) {
                if (enableDoormarker) {
                    // TODO check: redundant to vehileloaddelegate solution?
                    markDoor(markedaircraft);
                }
                GroundServicesSystem.requestService(markedaircraft);
            } else {
                //Ein Aircraft "ranholen" und einfahren lassen
                //27.12.21 TODO GroundServicesSystem.requestArrivingAircraft(DefaultTrafficWorld.getInstance().getGroundNet("EDDK").getParkPos("C_7"));
            }
        }
        //Alpha6 ist nach Ersatz durch 'S' wieder frei.
        if (Input.getKeyDown(KeyCode.Alpha7)) {
            // einfach ein Standortwechsels eines Followme
            // TODO 14.10.21 muss jetzt per Request gehen
            Util.nomore();
            //groundservicessystem.requestMove(GroundServiceComponent.VEHICLE_FOLLOME, DefaultTrafficWorld.getInstance().getGroundNet("EDDK").getParkPos("C_7"));
        }
        /*in common if (Input.getKeyDown(KeyCode.Alpha8)) {
            //lauch c172p (or 777)
            //TrafficSystem.loadVehicles(tw, avataraircraftindex);
            lauchVehicleByIndex(gsw.groundnet, tw, avataraircraftindex, TeleportComponent.getTeleportComponent(avatar.avatarE), getWorld(), null);
            avataraircraftindex++;
        }*/
        /*7.10.21 jetzt per inputtorequest if (Input.getKeyDown(KeyCode.Alpha9)) {
            automoveSystem.setAutomoveEnabled(!automoveSystem.getAutomoveEnabled());
        }*/
        if (Input.getKeyDown(KeyCode.Space)) {
            cycleAircraft(1);
            updateHud();
        }

        if (fpc != null) {
            fpc.update(tpf);
            if (Input.getKey(KeyCode.KEY_PAGEUP) || Input.getKey(KeyCode.KEY_PAGEDOWN)) {
                Vector3 vpos = getDefaultCamera().getCarrierTransform().getPosition();
                double offset = 5;//10
                vpos = vpos.add(new Vector3(0, 0, (Input.getKey(KeyCode.KEY_PAGEUP)) ? offset : -offset));
                getDefaultCamera().getCarrierTransform().setPosition(vpos);
            }
        }
       /* if (Input.getKeyDown(KeyCode.N)) {
            // Nur bis das mit dene vents richtig geht
            ((GroundServiceVisualizer)visualizer).clear();
        }*/
        checkForPickingRay();
        //TODO nicht so haeufug. 5.10.18 HUD ist jetzt optional.
        updateHud();

        debugupdate();

        adjustASI();

        //26.10.18: Jetzt 'S' statt Alpha6. Aber nicht im FPC mode ohne Avatar.
        if (Input.getKeyDown(KeyCode.S) /*&& /*24.10.21avatar*//*10.3.22AvatarSystem.getAvatar() != null*/) {
            TravelHelper.startDefaultTrip(getAvatarVehicle());
        }
        if (Input.getKeyDown(KeyCode.Alpha4)) {
            TravelHelper.startFlight(Destination.buildByIcao("EDDF"), getAvatarVehicle());
        }
    }

    public static void adjustASI() {
        // fuer Speed Indicator für c172 asi. 6.4.21 Was in GraphMovingSystem before.
        // TODO generisch (auch in RailingScene)
        EcsEntity vehicle = TrafficHelper.findVehicleByName("c172p");
        if (vehicle != null) {
            PropertyComponent pc = PropertyComponent.getPropertyComponent(vehicle);
            if (pc != null) {
                pc.setSpeed(VelocityComponent.getVelocityComponent(vehicle).getMovementSpeed());
            }
        }
    }

    /**
     * Update für interne Zwecke, Helper, debug, report, etc.
     */
    private void debugupdate() {
        // KEy a ist schon automove. Und X ist position adjust. Mal 'E' versuchen. 20.6.20: Ist jetzt tests. Darum mal 'K'
        if (Input.getKeyDown(KeyCode.K)) {
            if (axishelper == null) {
                axishelper = ModelSamples.buildAxisHelper(2000);
                addToWorld(axishelper);
            } else {
                SceneNode.removeSceneNode(axishelper);
                axishelper = null;
            }
        }
        Vector3 cp = getDefaultCamera().getCarrierTransform().getPosition();
        double cameraheight = cp.getZ();
        double skipheight = cameraheight / 10;
        if (fpc == null) {
            //9.5.19: Dieser hanze Behelf hier kann sich vielleich in ein FPC Konzept einbinden? Mal sehn.
            TeleportComponent pc = TeleportComponent.getTeleportComponent(UserSystem.getInitialUser());
            if (pc != null && pc.getPosRot() != null) {
                cp = pc.getPosRot().position;
                skipheight = cp.getZ() / 10;

                // Die Cursortasten verwendet der Obeserver. Darum die TopView Position nur mit shift schieben.
                //if (!modelview) {
                if (Input.getKey(KeyCode.PageUp)) {
                    Vector3 inc = new Vector3(cp.getX(), cp.getY(), cp.getZ() * 1.1f);
                    //getDefaultCamera().getTransform().setPosition((inc));
                    pc.setPosition(0, inc);
                    //logger.debug("new cp:"+cp);
                }
                if (Input.getKey(KeyCode.PageDown)) {
                    Vector3 dec = new Vector3(cp.getX(), cp.getY(), cp.getZ() * 0.9f);
                    //getDefaultCamera().getTransform().setPosition((dec));
                    pc.setPosition(0, dec);
                }
                //Bewegung mit Shift-Cursor,  weil der Observer auch auf Cursortasten reagiert
                if (Input.getKey(KeyCode.Shift)) {
                    if (Input.getKey(KeyCode.UpArrow)) {
                        //getDefaultCamera().getTransform().setPosition(cp.add(new Vector3(0, skipheight, 0)));
                        //pc.point.get(0).position = cp.add(new Vector3(0, skipheight, 0));
                        pc.setPosition(0, cp.add(new Vector3(0, skipheight, 0)));
                    }
                    if (Input.getKey(KeyCode.DownArrow)) {
                        //getDefaultCamera().getTransform().setPosition(cp.add(new Vector3(0, -skipheight, 0)));
                        pc.setPosition(0, cp.add(new Vector3(0, -skipheight, 0)));
                    }
                    if (Input.getKey(KeyCode.LeftArrow)) {
                        //getDefaultCamera().getTransform().setPosition(cp.add(new Vector3(-skipheight, 0, 0)));
                        pc.setPosition(0, cp.add(new Vector3(-skipheight, 0, 0)));
                    }
                    if (Input.getKey(KeyCode.RightArrow)) {
                        //getDefaultCamera().getTransform().setPosition(cp.add(new Vector3(skipheight, 0, 0)));
                        pc.setPosition(0, cp.add(new Vector3(skipheight, 0, 0)));
                    }
                }
            }
        }
    }

    private void updateHud() {
        // During tests the mesh in Hud might not erist
        if (hud != null && hud.element != null && hud.element.getMesh() != null) {
            hud.clear();
            hud.setText(0, "current: " + ((markedaircraft != null) ? markedaircraft.getName() : ""));
            hud.setText(1, "");
            hud.setText(2, "edges: " + ((GroundServicesSystem.groundnetEDDK == null) ? 0 : GroundServicesSystem.groundnetEDDK.groundnetgraph.getBaseGraph().getEdgeCount()));
            //evtl. not yet found VelocityComponent vc = VelocityComponent.getVelocityComponent(GroundServicesSystem.findVehicle(VehicleComponent.VEHICLE_CATERING));
            //hud.setText(3, "speed: " + vc.getMovementSpeed());
        }
    }

    /*15.11.23 help was removed @Override
    protected void help() {
        if (helphud == null) {
            helphud = Hud.buildForCameraAndAttach(getDefaultCamera(), 1);
            helphud.setText(0, "9 - automove");
            helphud.setText(1, "space - cycle aircraft");
            helphud.setText(2, "6 - Start Flight");
            helphud.setText(3, "p - pause");
            helphud.setText(4, "r - report");
            helphud.setText(5, "x - axishelper");
            helphud.setText(6, "8 - Load Vehicle");

            helphud.setText(7, "press h to close");
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
        //Tja, das muss sich noch zeigen.
        TravelSphere.add(new TravelSphere("EDDK"));


    }

    /*@Override
    public VehicleLoader getVehicleLoader() {
        return new FgVehicleLoader();
    }*/

    @Override
    public EllipsoidCalculations getRbcp() {
        return new FgCalculations();
    }

    @Override
    public GeoCoordinate getCenter() {
        return EDDK_CENTER;
    }

    /**
     * Override the default control panel with the flight travel default menu.
     */
    @Override
    public ControlPanel buildVrControlPanel() {

        return FlightVrControlPanel.buildVrControlPanel(true,
                // trip handler. Only one trip available here for now
                new SpinnerHandler() {
                    @Override
                    public void up() {
                    }

                    @Override
                    public String getValue() {
                        return "Default Trip";
                    }

                    @Override
                    public void down() {
                    }
                }, () -> {
                    TravelHelper.startDefaultTrip(getAvatarVehicle());
                },
                // service handler
                new SpinnerHandler() {
                    @Override
                    public void up() {
                        cycleAircraft(1);
                    }

                    @Override
                    public String getValue() {
                        // empty string fails due to length 0
                        return markedaircraft == null ? " " : markedaircraft.getName();
                    }

                    @Override
                    public void down() {
                        cycleAircraft(-1);
                    }
                }, () -> {
                    if (markedaircraft != null) {
                        markDoor(markedaircraft);
                        GroundServicesSystem.requestService(markedaircraft);
                    }
                });
    }
    /*@Override jetzt in config
    protected Quaternion getBaseRotationForVehicleOnGraph() {
        Quaternion rotationforgraph = Quaternion.buildFromAngles(new Degree(-90), new Degree(-90), new Degree(0));
        return rotationforgraph;
    }*/

    private void checkForPickingRay() {
        Point mouselocation;
        if ((mouselocation = Input.getMouseMove()) != null) {
            // Die Maus hat sich bewegt.
            Ray pickingray = getMainCamera().buildPickingRay(getMainCamera().getCarrierTransform(), mouselocation);

            List<NativeCollision> intersects = pickingray.getIntersections();
            String names = "";
            for (int i = 0; i < intersects.size(); i++) {
                names += "," + intersects.get(i).getSceneNode().getName();

            }
            if (intersects.size() > 0) {
                logger.debug("" + intersects.size() + " intersections detected: " + names);
                SceneNode intersect = new SceneNode(intersects.get(0).getSceneNode());
            }
        }
    }

    private void executeUsecase(int usecase) {
        GraphPath path;
        switch (usecase) {
            case 1:
                // move initial Pushback Truck to parking pos (B_2) for pushback of 747
                GraphNode hp = GroundServicesSystem.groundnetEDDK.groundnetgraph.getBaseGraph().findNodeByName("16");
                logger.debug("use case 1");
                path = GroundServicesSystem.groundnetEDDK.createPathForPushbackVehicle(b_2, hp);
                //trafficsystem.addSchedule(new Schedule(path));
                //GraphMovingComponent.getGraphMovingComponent(pushback1).setPath(path);
                travelGraphVisualizer.addLayer(GroundServicesSystem.groundnetEDDK.groundnetgraph.getBaseGraph(), path.layer, getWorld());
                break;


            case 4:
                //Follome mit My777. Ist etwas langsam wegen Model laden.
                /*24.7.17if (my777 == null) {
                    logger.warn("my777 not ready. Start loading.");
                    Resource.loadBundle("fgdatabasic", new DefaultBundleLoadCallback("fgdatabasic", (String bundlename) -> {
                        Resource.loadBundle("My-777", new DefaultBundleLoadCallback("My-777", (String bname) -> {
                            initFG();
                            Bundle bundle = BundleRegistry.getBundle(bname);
                            SceneNode m777;
                            BundleResource br = BundleResource.buildFromFullString("Models/777-200.xml");
                            br.bundle = bundle;
                            SGLoaderOptions opt = new SGLoaderOptions();
                            //unrotated ist wichtigfuer die Captain Viewposition 
                            SceneNode unrotetdnode = loadFgModel(br);
                            m777 = new SceneNode(unrotetdnode);
                            addToWorld(m777);
                            My777Scene.removeCones();

                            Vector3 target4 = new Vector3(0, 0, 0);
                            GraphNode my777node = osm.addNode("My777", target4);
                            GraphEdge nearest = osm.findNearestEdge(target4);
                            GraphEdge con = osm.connectNodes(nearest.getFrom(), my777node);
                            visualizer.visualizeEdge(con);
                            GraphPosition my777pos = new GraphPosition(my777node.edges.get(0), my777node.edges.get(0).getLength(), false);
                            //my777pos.setUpVector(graphupvector);
                            my777 = trafficsystem.buildVehicle(m777, my777pos, null, VehicleComponent.VEHICLE_AIRCRAFT, 10);

                            // Captain. TODO zentralisieren mit FlightScene und My777 Scene bzw Configdatei. Der hier sitzt aber höher
                            controller.addStep(unrotetdnode, new Vector3(-22.60f, -0.5f, 0.8f), new Quaternion(new Degree(90), new Degree(0), new Degree(90)));

                            GraphEdge graphintonet4 = groundnet.createPathIntoGroundNet(new PositionHeading(target4, new Degree(180)));
                            visualizer.visualizeEdge(graphintonet4);
                        }));
                    }));
                } else {

                }*/
                break;


            default:
                throw new RuntimeException("invalid use case");
        }
    }


    /**
     * @return
     */
    /*6.3.18 private void buildArrivedAircraft(VehicleConfig vehicleconfig, Vector3 position, Degree heading, Parking parking) {
        // Ohne orientierung zeigt er nach links. Ich brauch hier aber das base model für den View Point.
        //3.3.18 Quaternion orientierung = new Quaternion(new Degree(0), new Degree(0), new Degree(-90));
        //3.3.18 orientierung = Quaternion.buildRotationZ(new Degree(MathUtil2.getDegreeFromHeading(heading).getDegree() + 0));


        //SceneNode basemodel = new SceneNode(FlightGear.loadFgModel(br, 0/*zoffset*//*, false, null* /));
        TrafficSystem.loadVehicle(vehicleconfig, (SceneNode container, SceneNode basenode) -> {
            SceneNode currentaircraft = container;
            SceneNode basemodel = basenode;
            //SceneNode model = rotateFgModel1(basemodel);
            SceneNode model = container;//new SceneNode(basemodel);
            addToWorld(model);
            //model.getTransform().setPosition(new Vector3(position.getX(), position.getY(), position.getZ() + zoffset));
            model.getTransform().setPosition(new Vector3(position.getX(), position.getY(), position.getZ()));
            // 1.3.18: Ich muss ihn nicht erst nach Nordern drehn, getDegreeFromHeading beruecksichtigt schon -x als Defaultrichtung
            model.getTransform().setRotation(Quaternion.buildRotationZ(MathUtil2.getDegreeFromHeading(heading)));
            EcsEntity ve = groundservicessystem.buildArrivedAircraft(model, parking);
            ve.setName(vehicleconfig.getName());
            TeleportComponent pc = TeleportComponent.getTeleportComponent(avatar.avatarE);
            pc.addPosition("BackSide", basemodel, new PosRot(new Vector3(90, 15, 20), new Quaternion(new Degree(0), new Degree(90), new Degree(90))));
            // ein kleiner doormarker im local space.
            SceneNode marker = ModelSamples.buildAxisHelper(8, 0.3f);
            marker.setName("localdoormarker");
            Vector3 dp = tw.getAircraftConfiguration(vehicleconfig.getModelType()).getCateringDoorPosition();
            marker.getTransform().setPosition(dp);
            basemodel.attach(marker);
            arrivedaircraft.add(new ArrivedAircraft(ve, vehicleconfig.getModelType(), heading,basenode));
        });
    }*/

   
  

    /*3.18 private EcsEntity buildVehicleEntity(GraphPosition position, SceneNode model, String type, float maximumspeed, String name, float acceleration, SceneNode basemodel) {
        EcsEntity ve = TrafficSystem.buildVehicleOnGraph(model, position, null, type, maximumspeed, acceleration);
        ve.setName(name);
        // Vehicle ist schon in den Scene Space (oder graph space?) rotiert. Etwas ueberraschend: x=seitlich,y=hoehe,z vor zurueck. 28.10.17: Wirklich?
        TeleportComponent pc = TeleportComponent.getTeleportComponent(avatar.avatarE);
        if (basemodel != null) {
            // ist noch in FG space. Dann muss ich aber rotieren. Aber warum so?
            pc.addPosition("BackSide", basemodel, new PosRot(new Vector3(90, 20, 15), new Quaternion(new Degree(0), new Degree(90), new Degree(90))));
        } else {
            // 28.10.17: Warum wohl keine Rotation?
            pc.addPosition("BackSide", model, new PosRot(new Vector3(15, 20, 90), new Quaternion(new Degree(0), new Degree(0), new Degree(0))));
        }
        //controller.addStep(model, new Vector3(15, 20, 90), new Quaternion(new Degree(0), new Degree(0), new Degree(0)));
        return ve;
    }*/

    /*17.4.18 private void/*ArrivedAircraft* / buildArrivedAircraft(VehicleConfig config, Parking parking) {
        //GraphPosition position = groundnet.getParkingPosition(parking);
        /*ArrivedAircraft aa = buildArrivedAircraft(config, parking.node.getLocation(), parking.heading,  parking);* /
        launchVehicle(config, gsw.groundnet.groundnetgraph, gsw.groundnet.getParkingPosition(parking), TeleportComponent.getTeleportComponent(avatar.avatarE), getWorld(), null);
        // return aa;
    }*/
    private void addGroundMarker(double x, double y) {
        Color col = new Color(0, 0, 256);
        double size = 5;
        ShapeGeometry g = ShapeGeometry.buildStandardSphere();
        Material mat = Material.buildLambertMaterial(col);
        SceneNode model = new SceneNode(new Mesh(g, mat));
        model.getTransform().setPosition(new Vector3(x, y, 0));
        model.getTransform().setScale(new Vector3(size, size, size));
        model.setName("");
        addToWorld(model);
    }

    /*private ArrivedAircraft findArrivedAircraft() {
        for (ArrivedAircraft aa : arrivedaircraft) {
            if (!aa.used) {
                aa.used = true;
                return aa;
            }
        }
        return null;
    }*/


    private void cycleAircraft(int inc) {
        List<EcsEntity> aircrafts = SystemManager.findEntities(new AircraftFilter());
        if (aircrafts.size() == 0) {
            markedaircraft = null;
            return;
        }
        int i;
        for (i = 0; i < aircrafts.size(); i++) {
            if (aircrafts.get(i).equals(markedaircraft)) {
                i += inc;
                break;
            }
        }
        if (i >= aircrafts.size()) {
            i = 0;
        }
        if (i < 0) {
            i = aircrafts.size() - 1;
        }
        markedaircraft = aircrafts.get(i);
    }


    /**
     * 28.9.18: Warum ist das hier null? Auf jeden Fall in das Vehicle sonmst nicht an der richtigen Stelle.
     * 10.1.19: Weil GroundServices 2D ist und die 2D Koorinaten auch zur Darstelleung nutzt.
     *
     * @return
     */
    /*29.10.21 @Override*/
    public GraphProjection/*Flight3D*/ getGraphBackProjection() {
        return null;
    }

    //@Override
    protected SceneNode getDestinationNode() {
        return getWorld();
    }

    /*27.12.21@Override
    protected GraphWorld getGraphWorld() {
        return DefaultTrafficWorld.getInstance();
    }*/

    @Override
    public VehicleConfigDataProvider getVehicleConfigDataProvider() {
        // 30.11.23: Rely on generic provider
        Util.nomore();
        return null;
        //return new VehicleConfigDataProvider(tw.tw);
    }

    /**
     * Eine Sammlung von special paths with Besonderheiten bei der Pfadbildung.
     * Auch fuer Tests.
     */
    /*26.4.18 public static List<GraphPath> buildPathCollection(GroundNet groundnet) {
        List<GraphPath> paths = new ArrayList<GraphPath>();
        //Bei dem Path gibts Artefakte an Node 69.
        GraphEdge e3_202 = groundnet.groundnetgraph.findEdgeByName("3-202");
        GraphPosition start = new GraphPosition(e3_202, 0, true);
        GraphPath path = groundnet.createPathFromGraphPosition(start, groundnet.getParkPos("A20").node,null);
        path.startposition = start;
        paths.add(path);
        return paths;
    }*/
    private void unloadCurrentTile() {
        //TODO damit ist es ja nicht getan.
        //10.12.21 graphTerrainsystem.removeAll();
    }


    @Override
    protected void runTests() {
        logger.info("Running tests");
        logger.info("Testing world projection by 747");
        EcsEntity v747 = TrafficHelper.findVehicleByName("747 KLM");
        RuntimeTestUtil.assertNotNull("747-400", v747);
        //DefaultTrafficWorld trafficWorld = TrafficWorld2D.getInstance();
        //RuntimeTestUtil.assertNotNull("trafficWorld", trafficWorld);
        SphereProjections projection = TrafficHelper.getProjectionByDataprovider();//trafficWorld.getProjection();
        RuntimeTestUtil.assertNotNull("projection", projection);
        Vector3 pos747 = v747.getSceneNode().getTransform().getWorldModelMatrix().extractPosition();
        LatLon latlon747 = projection.projection.unproject(Vector2.buildFromVector3(pos747));
        RuntimeTestUtil.assertLatLon("latlon747", new LatLon(new Degree(51), new Degree(7)), latlon747, 1.5);

        List<ViewPoint> viewpoints = TrafficHelper.getViewpointsByDataprovider();
        // auf die 747/vehicle home und center 0,0
        RuntimeTestUtil.assertEquals("viewpoints", 2, viewpoints.size());

        //Avatar avatar = AvatarSystem.getAvatar();
        //RuntimeTestUtil.assertNotNull("avatar", avatar);
        EcsEntity user = UserSystem.getInitialUser();
        ObserverComponent observerComponent = ObserverComponent.getObserverComponent(user);
        RuntimeTestUtil.assertNotNull("observerComponent", observerComponent);

        TeleportComponent tc = TeleportComponent.getTeleportComponent(user);
        RuntimeTestUtil.assertNotNull("tc", tc);
        // 2 overview, 6 vehicle (stimmt das? ist aber plausibel)
        RuntimeTestUtil.assertEquals("tc viewpoints", 2 + 6, tc.getPointCount());

        logger.info("Tests completed");
    }

    /**
     * Was in TrafficWorld2D before 28.10.21
     */
    private /*TrafficWorld2D*/void initTrafficWorld(String sceneName, String vehiclelistname) {
        //4.12.23 tw = TrafficWorldConfig.readConfig("data-old", "TrafficWorld.xml"/*"GroundServices"*/);


        //27.12.21 TrafficWorld2D gsw = new TrafficWorld2D(/*9.6.20icao, *//*tw,*/ tw.getScene(sceneName));
        //trafficWorld = gsw;
        //27.10.21 gsw.vehiclelist = tw.getVehicleListByName(vehiclelistname);
        //28.10.21 TrafficSystem.vehiclelist = tw.getVehicleListByName(vehiclelistname);
        //return null;
    }

    /**
     * Non VR Control menu.
     * <p>
     */
    public GuiGrid buildControlMenuForScene(Camera camera) {

        GuiGrid controlmenu = GuiGrid.buildForCamera(camera, 2, 4, 1, Color.BLACK_FULLTRANSPARENT, true);

        controlmenu.addButton(0, 0, 1, Icon.ICON_POSITION, () -> {
            InputToRequestSystem.sendRequestWithId(new Request(UserSystem.USER_REQUEST_TELEPORT, new Payload(new Object[]{new IntHolder(0)})));
        });
        controlmenu.addButton(1, 0, 1, Icon.ICON_MENU, () -> {
            InputToRequestSystem.sendRequestWithId(new Request(InputToRequestSystem.USER_REQUEST_MENU));
        });
        controlmenu.addButton(2, 0, 1, Icon.ICON_PLUS, () -> {
            cycleAircraft(1);
            updateHud();
        });
        controlmenu.addButton(3, 0, 1, Icon.ICON_CLOSE, () -> {
            InputToRequestSystem.sendRequestWithId(new Request(InputToRequestSystem.USER_REQUEST_CONTROLMENU));
        });
        return controlmenu;
    }
}

class AircraftMarker {
    private EcsEntity current;
    SceneNode marker;

    public AircraftMarker(EcsEntity aircraft) {
        String name = "marker cube";
        // immer 20 Meter ueber dem Objekt
        Vector3 position = new Vector3(0, 20, 0);
        Color col = new Color(256, 0, 128);
        double size = 5;
        ShapeGeometry cubegeometry = ShapeGeometry.buildBox(size, size, size, null);
        Material mat = Material.buildLambertMaterial(col);
        SceneNode model = new SceneNode(new Mesh(cubegeometry, mat));
        model.getTransform().setPosition(position);
        model.setName(name);
        marker = model;
        model.getTransform().setParent(aircraft.scenenode.getTransform());
        current = aircraft;
    }

    public void shift(ArrayList<EcsEntity> aircraft) {
        for (int i = 0; i < aircraft.size(); i++) {
            if (aircraft.get(i).equals(current)) {
                if (i == aircraft.size() - 1) {
                    i = 0;
                } else {
                    i++;
                }
                EcsEntity a = aircraft.get(i);
                marker.getTransform().setParent(a.scenenode.getTransform());
                current = a;
                return;
            }
        }
    }
}

class AircraftFilter implements EntityFilter {

    @Override
    public boolean matches(EcsEntity e) {
        VehicleComponent vc = VehicleComponent.getVehicleComponent(e);
        if (vc == null) {
            return false;
        }
        return vc.config.getType().equals(VehicleComponent.VEHICLE_AIRCRAFT);

    }
}
