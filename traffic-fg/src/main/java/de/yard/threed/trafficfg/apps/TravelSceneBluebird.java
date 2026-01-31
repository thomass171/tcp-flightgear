package de.yard.threed.trafficfg.apps;


import de.yard.threed.core.*;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.testutil.RuntimeTestUtil;
import de.yard.threed.engine.Camera;
import de.yard.threed.engine.Input;
import de.yard.threed.engine.KeyCode;
import de.yard.threed.engine.Light;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.apps.ModelSamples;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsSystem;
import de.yard.threed.engine.ecs.InputToRequestSystem;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.TeleportComponent;
import de.yard.threed.engine.ecs.TeleporterSystem;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.gui.ControlPanel;
import de.yard.threed.engine.gui.GuiGrid;
import de.yard.threed.engine.gui.Icon;
import de.yard.threed.engine.gui.MenuItem;
import de.yard.threed.engine.gui.Text;
import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.engine.platform.ProcessPolicy;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.Settings;
import de.yard.threed.engine.util.NearView;
import de.yard.threed.engine.vr.VrInstance;
import de.yard.threed.trafficfg.TrafficRuntimeTestUtil;
import de.yard.threed.flightgear.FgVehicleLoader;
import de.yard.threed.flightgear.FlightGearMain;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.flightgear.core.simgear.scene.model.ACProcessPolicy;
import de.yard.threed.flightgear.core.simgear.scene.model.OpenGlProcessPolicy;
import de.yard.threed.flightgear.ecs.FgAnimationUpdateSystem;
import de.yard.threed.graph.Graph;
import de.yard.threed.graph.GraphEdge;
import de.yard.threed.graph.GraphEventRegistry;
import de.yard.threed.graph.GraphMovingComponent;
import de.yard.threed.graph.GraphMovingSystem;
import de.yard.threed.graph.GraphPosition;
import de.yard.threed.graph.SimpleGraphVisualizer;
import de.yard.threed.traffic.Destination;
import de.yard.threed.trafficcore.EllipsoidCalculations;
import de.yard.threed.trafficcore.GeoRoute;
import de.yard.threed.traffic.GraphBackProjectionProvider;
import de.yard.threed.traffic.GraphTerrainSystem;
import de.yard.threed.traffic.GraphVisualizationSystem;
import de.yard.threed.traffic.LightDefinition;
import de.yard.threed.traffic.TrafficConfig;
import de.yard.threed.traffic.TrafficHelper;
import de.yard.threed.traffic.TrafficSystem;
import de.yard.threed.traffic.WorldGlobal;
import de.yard.threed.traffic.apps.BasicTravelScene;
import de.yard.threed.traffic.config.PoiConfig;
import de.yard.threed.traffic.config.VehicleDefinition;
import de.yard.threed.traffic.config.XmlVehicleDefinition;
import de.yard.threed.traffic.flight.FlightLocation;
import de.yard.threed.traffic.flight.FlightRouteGraph;
import de.yard.threed.traffic.osm.OsmRunway;
import de.yard.threed.trafficcore.config.AirportDefinition;
import de.yard.threed.trafficcore.model.Runway;
import de.yard.threed.trafficfg.AutomoveSystem;
import de.yard.threed.trafficfg.FgBackProjectionProvider;
import de.yard.threed.trafficfg.FgCalculations;
import de.yard.threed.trafficfg.SGGeodAltitudeProvider;
import de.yard.threed.trafficfg.TravelHelper;
import de.yard.threed.trafficfg.VehicleEntityBuilder;
import de.yard.threed.trafficfg.fgadapter.FlightGearSystem;
import de.yard.threed.trafficfg.flight.FlightSystem;
import de.yard.threed.trafficfg.flight.FlightVrControlPanel;
import de.yard.threed.trafficfg.flight.GroundServicesSystem;
import de.yard.threed.trafficfg.flight.RouteBuilder;
import de.yard.threed.trafficfg.flight.TravelSceneHelper;

import java.util.ArrayList;
import java.util.List;

import static de.yard.threed.trafficfg.flight.TravelSceneHelper.getSphereWorld;


/**
 * Cloned/derived from big TravelScene, but only bluebird (no external, so much simpler.
 * No Ground services (but groundnet), no aircraft marking.
 * Due to required groundnet and available scenery this is limited to EDDK.
 * <p>
 * Full sgmaterial is not available
 */
public class TravelSceneBluebird extends BasicTravelScene {
    static Log logger = Platform.getInstance().getLog(TravelSceneBluebird.class);
    Light light;
    private static final int WIDTH = 1024;
    private static final int HEIGHT = 768;

    static Vector3 camerapositioninmodel = new Vector3(0, 0.6f, 0);

    // freecam ist mal für Tests
    //22.10.21 boolean freecam = false;
    // erste Position. 6/17: 3=equator,4=Dahlem ,5=EDDK
    int startpos = 5;
    Graph orbit;
    // public for testing
    public FlightRouteGraph platzrundeForVisualizationOnly/*, orbittour*/;
    VehicleDefinition configShuttle;
    private EcsEntity orbitingvehicle = null;
    private boolean avatarInited = false;
    protected AirportDefinition airportDefinition;
    TrafficConfig trafficConfig;
    // worldPois are contained in trafficConfig
    // TrafficConfig worldPois;
    TrafficConfig vdefs;
    // 18.3.24 From former hard coded EDDK setup. Isn't this the overview position?
    // 21.1.26:Slightly differs from initial/projection position in AirportScene(?)
    public static GeoCoordinate formerInitialPositionEDDK = new GeoCoordinate(new Degree(50.843675), new Degree(7.109709), 1150);
    GeoRoute initialRoute = null;

    @Override
    public String[] getPreInitBundle() {
        // "fgdatabasic" and "traffic-fg" are needed for bluebird
        // 'TerraySync' is loaded at runtime by TerraSyncBundleResolver' that is set up by the platform(using HOSTDIRFG on desktop and "bundles" in webgl)
        // 21.7.24: TerraSync-model isn't used anyway currently due to flag 'ignoreshared'. So save the time and memory for loading it.
        // 06.3.25: "data" is needed for initial earth texture. Strange we apparently never had it.
        return new String[]{"engine", "data", FlightGear.getBucketBundleName("model"), "sgmaterial", "fgdatabasic", "traffic-fg"};
    }

    @Override
    public void customInit() {
        logger.debug("init Flight");

        vehiclelistname = "VehiclesWithCockpit";

        FlightGearMain.initFG(new FlightLocation(WorldGlobal.equator020000, new Degree(0), new Degree(0)), null);
        // 6.3.25 Why should we need "fgdatabasicmodel" here? It's an advanced bundle. Probably a relict only causing error loggings
        //FgBundleHelper.addProvider(new SimpleBundleResourceProvider("fgdatabasicmodel"));

        trafficConfig = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("traffic-fg"), new BundleResource("flight/EDDK-bluebird.xml"));
        // not until we define some worldPois = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("traffic-advanced"), new BundleResource("world-pois.xml"));
        airportDefinition = trafficConfig.findAirportDefinitionsByIcao("EDDK").get(0);

        //solange es kein Terrain gibt, immer elevation 80; was aber reichlich fraglich ist. Der braucht keine adjustment world
        /*8.5.25 not any more TerrainElevationProvider tep = new TerrainElevationProvider(null);
        SystemManager.putDataProvider(SystemManager.DATAPROVIDERELEVATION, tep);*/

        //14.5.24 ScenerySystem and TerrainBuilder(FgTerrainBuilder) decoupled to SphereSystem/config.
        // TerrainElevationProvider was created in FgTerrainBuilder. Needs help because EDDK groundnet exceeds EDDK tile, so define a default value 68.
        // 6.3.25: EDDK gap helper also decoupled or considered useless?? ((TerrainElevationProvider) SystemManager.getDataProvider(SystemManager.DATAPROVIDERELEVATION)).setDefaultAltitude(68.0);

        SystemManager.addSystem(new AutomoveSystem());

        initHud();
        if (hud != null && hud.element != null && hud.element.getMesh() != null) {
            hud.setText(0, " ");
        }

        orbit = RouteBuilder.buildEquatorOrbit();

        //nearView soll nur die Vehicle abdecken.
        if (enableNearView) {
            nearView = new NearView(getDefaultCamera(), 0.01, 20, this);
        }

        SceneNode cube = ModelSamples.buildCube(1000, new Color(0xCC, 00, 00));
        if (cube != null) {
            cube.getTransform().setPosition(new Vector3(3987743.8f, 480804.66f, 4937917.5f));
            //cube.setPosition(new Vector3(0, 0, 0));
            getSphereWorld().attach(cube);
        }
        //16.5.24: Moved to SphereSystem addToWorld(world);

        //visualizeTrack soll auch im usermode verfuegbar sein.
        if (visualizeTrack) {
            SystemManager.addSystem(new GraphVisualizationSystem(new SimpleGraphVisualizer(getSphereWorld())));
        }
        EllipsoidCalculations rbcp = TrafficHelper.getEllipsoidConversionsProviderByDataprovider();

        SystemManager.addSystem(new FlightSystem());
        SystemManager.addSystem(new FgAnimationUpdateSystem());
        SystemManager.addSystem(new FlightGearSystem());

        ((GraphTerrainSystem) SystemManager.findSystem(GraphTerrainSystem.TAG)).disable();

        SystemManager.addSystem(new GroundServicesSystem());
        GroundServicesSystem.airportConfigBundle = "traffic-fg";
        GroundServicesSystem.airportConfigFullName = "flight/EDDK-bluebird.xml";

        SystemManager.registerService("vehicleentitybuilder", new VehicleEntityBuilder());
        vdefs = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("traffic-fg"), new BundleResource("flight/vehicle-definitions.xml"));

        for (VehicleDefinition vd : XmlVehicleDefinition.convertVehicleDefinitions(vdefs.getVehicleDefinitions())) {
            trafficSystem.addKnownVehicle(vd);
        }

        // 29.8.23: Missing for orbit tour, but is it correct? Well, orbittour seems to work.
        ((GraphMovingSystem) SystemManager.findSystem(GraphMovingSystem.TAG)).graphAltitudeProvider = new SGGeodAltitudeProvider();

        //groundnet is set later
        trafficSystem.locationList = airportDefinition.getLocations();
        trafficSystem.nearView = nearView;
        trafficSystem.setVehicleLoader(new FgVehicleLoader());

        if (VrInstance.getInstance() == null) {
            InputToRequestSystem inputToRequestSystem = (InputToRequestSystem) SystemManager.findSystem(InputToRequestSystem.TAG);
            inputToRequestSystem.setControlMenuBuilder(camera -> buildControlMenuForScene(camera, false));
            // cameraForMenu already set by super class
        }

    }

    @Override
    protected void customProcessArguments() {
        // 'basename' needs to be set aditionally.
        String ir = Platform.getInstance().getConfiguration().getString("initialRoute");
        if (ir != null) {
            try {
                initialRoute = GeoRoute.parse(ir);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public EcsSystem[] getCustomTerrainSystems() {
        return new EcsSystem[]{};
    }

    @Override
    public ProcessPolicy getProcessPolicy() {
        return new ACProcessPolicy(null);
    }

    String[] trips = new String[]{"Default Trip", "Low Orbit Tour"};
    int tripindex = 0;

    @Override
    public MenuItem[] getMenuItems() {
        return new MenuItem[]{
                new MenuItem(null, new Text(trips[0], Color.RED, Color.LIGHTGRAY), () -> {
                    tripindex = 0;
                    startTrip();
                    InputToRequestSystem.sendRequestWithId(new Request(InputToRequestSystem.USER_REQUEST_MENU));
                }),
                new MenuItem(null, new Text(trips[1], Color.RED, Color.LIGHTGRAY), () -> {
                    tripindex = 1;
                    startTrip();
                    InputToRequestSystem.sendRequestWithId(new Request(InputToRequestSystem.USER_REQUEST_MENU));
                }),
                /*not yet defined trips new MenuItem(null, new Text("Enter Equator Orbit", Color.RED, Color.LIGHTGRAY), () -> {
                    logger.debug("menu: equator orbit");
                    //8.3.20 enterEquatorOrbit();
                    TravelHelper.startFlight(Destination.buildForOrbit(true), getAvatarVehicle());
                    //openclosemenu();
                }),
                new MenuItem(null, new Text("Enter Moon Orbit", Color.RED, Color.LIGHTGRAY), () -> {
                    logger.debug("menu: moon orbit");
                    //enterEquatorOrbit();
                    //openclosemenu();
                }),*/
        };
    }

    /**
     * uses current index
     */
    private void startTrip() {
        logger.debug("startTrip:" + trips[tripindex]);
        switch (tripindex) {
            case 0:
                // 13.7.24 Don't ignore initialRoute TravelHelper.startFlight(Destination.buildRoundtrip(0), getAvatarVehicle());
                TravelHelper.startDefaultTrip(TeleporterSystem.getTeleportEntity());
                break;
            case 1:
                TravelHelper.startFlight(Destination.buildRoundtrip(1), TeleporterSystem.getTeleportEntity());
                break;
            default:
                logger.error("invalid tripindex " + tripindex);
        }
    }

    /**
     * For future use
     *
     * @return
     */
    private void addPoisToBluebird(EcsEntity bluebird) {
        // needle muss als parent eingetragen werden. NeeNee, needle ist child von needleforefg.
        SceneNode needleforfg = bluebird.scenenode;
        TeleportComponent navigatorpc = new TeleportComponent(needleforfg);

        SceneNode parent = getSphereWorld();
        for (PoiConfig poi : getPoiList(trafficConfig/*worldPois*/)) {
            navigatorpc.addPosition(poi.getName(), parent.getTransform(), poi.getTransform(new FgCalculations()));
        }
        // in general start in EDDK
        navigatorpc.setIndex(0/*startpos - 1*/);

        bluebird.addComponent(navigatorpc);
        bluebird.addComponent(new GraphMovingComponent(bluebird.scenenode.getTransform()));
    }

    private static List<PoiConfig> getPoiList(TrafficConfig tw) {
        List<PoiConfig> poilist = new ArrayList<PoiConfig>();
        poilist.add(tw.getPoiByName("EDDK Overview"));
        /*poilist.add(tw.getPoiByName("EDDK Overview Far"));
        poilist.add(tw.getPoiByName("EDDK Overview Far High"));
        poilist.add(tw.getPoiByName("elsdorf2000"));
        poilist.add(tw.getPoiByName("greenwich500"));
        poilist.add(tw.getPoiByName("Nordpol"));*/
        return poilist;
    }

    @Override
    public void initSettings(Settings settings) {
        settings.aasamples = 4;
        settings.vrready = true;
        settings.minfilter = EngineHelper.TRILINEAR;
    }

    @Override
    public Dimension getPreferredDimension() {
        if (Platform.getInstance().isDevmode()) {
            return new Dimension(WIDTH, HEIGHT);
        }
        return null;
    }

    /*@Override
    public LightDefinition[] getLight() {
        return new LightDefinition[]{
                /*15.5.24   now in EDDK-sphere.xml      new LightDefinition(Color.WHITE, new Vector3(0, 30000000, 20000000)),
                        new LightDefinition(Color.WHITE, new Vector3(0, -30000000, -20000000)),*b /
        };
    }*/

    /**
     * Override the default control panel with the flight travel default menu.
     */
    @Override
    public ControlPanel buildVrControlPanel() {

        return FlightVrControlPanel.buildVrControlPanel(false,
                // trip handler. Only one trip available here for now
                new SpinnerHandler() {
                    @Override
                    public void up() {
                        if (++tripindex >= trips.length) {
                            tripindex = 0;
                        }
                    }

                    @Override
                    public String getDisplayValue() {
                        return trips[tripindex];
                    }

                    @Override
                    public void down() {
                        if (--tripindex < 0) {
                            tripindex = trips.length - 1;
                        }
                    }
                }, () -> {
                    startTrip();
                },
                // no service (vehicles) currently
                null, null);
    }

    public void add(SceneNode model, double x, double y, double z, double scale, Quaternion rotation) {
        addToWorld(model);
        model.getTransform().setPosition(new Vector3(x, y, z));
        model.getTransform().setScale(new Vector3(scale, scale, scale));
        if (rotation != null) {
            model.getTransform().setRotation(rotation);
        }
    }

    boolean populated = false;

    @Override
    public void customUpdate(double currentdelta) {

        if (!avatarInited) {

            TeleportComponent avatartc = TeleportComponent.getTeleportComponent(UserSystem.getInitialUser());

            if (avatartc == null) {
                logger.warn("no avatar (tc) yet");
            } else {
                // 30.1.24: 'navigator.gltf' not found for some time now. Leads to repeatedly 'not fond' logging.
                //  buildBluebird(avatartc);
                LocalTransform loc = WorldGlobal.eddkoverview.location.toPosRot(new FgCalculations());
                // Base rotation to make user node a FG model ...
                Quaternion rotation = new OpenGlProcessPolicy(null).opengl2fg.extractQuaternion();
                // .. that is suited for FG geo transformation and FirstPersonTransformers OpenGL coordinate system.
                // Unclear why it is this order of rotation multiply.
                rotation = loc.rotation.multiply(rotation);

                //for (PoiConfig poi : getPoiList(trafficConfig/*worldPois*/)) {
                //avatartc.addPosition(poi.getName(), null, poi.getTransform(new FgCalculations()));
                avatartc.addPosition("t1", null, new LocalTransform(loc.position, rotation));
                //}
                avatartc.stepTo(0);

                if (teleporterSystem != null) {
                    teleporterSystem.setActivetc(avatartc);
                }
                avatarInited = true;
            }
        }

        boolean terrainavailable = TravelSceneHelper.hasTerrain();

        if (terrainavailable && GroundServicesSystem.groundnets.get("EDDK") != null && !populated) {
            //jetzt muesste ein Groundnet mit 2D Projection da sein.
            populated = true;
            //20.3.24 now via TRAFFIC_EVENT_GRAPHLOADED trafficSystem.addTrafficGraph(GroundServicesSystem.groundnetEDDK.groundnetgraph);
        }

        if (Input.getKeyDown(KeyCode.D)) {
            logger.debug("scenegraph:" + dumpSceneGraph());
        }
        // 29.8.23: As long a menu isn't working start orbit tour via key.'R' probably not yet in use.
        if (Input.getKeyDown(KeyCode.R)) {
            logger.debug("orbit tour:" + dumpSceneGraph());
            TravelHelper.startFlight(Destination.buildRoundtrip(1), TeleporterSystem.getTeleportEntity());
        }

        // Platzrunde erst anlegen, wenn das Terrain da ist und worldadjustment durch ist.
        if (platzrundeForVisualizationOnly == null && terrainavailable) {
            logger.info("Building Platzrunde");
            // Zum Test direkt mal den Rundflug einblenden
            Runway runway14l = OsmRunway.eddk14L();
            platzrundeForVisualizationOnly = new RouteBuilder(TrafficHelper.getEllipsoidConversionsProviderByDataprovider()).buildFlightRouteGraph(runway14l, null, 0);
            SystemManager.sendEvent(new Event(GraphEventRegistry.GRAPH_EVENT_PATHCREATED, new Payload(platzrundeForVisualizationOnly.getGraph(), platzrundeForVisualizationOnly.getPath())));
        }
        /*18.10.19: Warum soll die denn schon vcorab angelegt werden?
        if (orbittour == null) {
           orbittour=buildOrbitTour();

        }*/

        if (Input.getKeyDown(KeyCode.S)) {
            TravelHelper.startDefaultTrip(TeleporterSystem.getTeleportEntity());
        }
    }

    /**
     * Eine Equator Orbit Tour ab einer Runway erstellen.
     *
     * @param equatorOrbit
     * @return
     */
    FlightRouteGraph buildOrbitTour(boolean equatorOrbit) {
         /*schwierig zu erkennen, s.o. if (!terrainavailable){
             logger.error("no terrain");
             return null;
         }*/

        logger.info("Building orbittour");
        Runway runway14l = OsmRunway.eddk14L();
        FlightRouteGraph orbittour = new RouteBuilder(TrafficHelper.getEllipsoidConversionsProviderByDataprovider()).buildFlightRouteGraph(runway14l, null, (equatorOrbit) ? 3 : 1);
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
     * Eine FlightRouteGraph mit aktuellem Vehicle starten. (ueber Graph).
     * TODO: Das Vehicle muesste sich erst noch zum Start der FlightRouteGraph bewegen.
     */
    void startFlightRouteGraph(FlightRouteGraph route) {

        //14.11.18: Eine GMC beisst sich zwar mit dem Teleporter, aber damit er sich mal im Orbit bewegen.
        //das ist etwas provisorisch
        EcsEntity currentvehicle = TeleporterSystem.getTeleportEntity();
        boolean mitnavigator = true;
        if (currentvehicle == null) {
            logger.error("avatars vehicle not found");
            return;
        }
        GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(currentvehicle/*(mitnavigator) ? vm[0] : GroundServicesScene.findc172p()*/);
        GraphEdge startedge = route.getPath().getSegment(0).edge;
        route.getPath().startposition = new GraphPosition(startedge);
        gmc.setGraph(route.getGraph(), route.getPath().startposition);

        gmc.setPath(route.getPath(), true);
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
        /* 1.3.24: has a dimension bug in GraphMovingComponent leading to large iterations and problems in webgl. Not yet needed that way.
        SolarSystem solarSystem = new SolarSystem();
        SceneNode sun = solarSystem.build(2 * WorldGlobal.DISTANCEMOONEARTH, WorldGlobal.DISTANCEMOONEARTH/*km(18000)* /);
        addToWorld(sun);*/
    }

    /*@Override
    public VehicleLoader getVehicleLoader() {
        return new FgVehicleLoader();
    }*/

    @Override
    public GeoCoordinate getCenter() {
        return WorldGlobal.EDDK_CENTER;
    }

    /**
     * Runtime tests are important because they use a 'real' platform, while unit tests use SimpleHeadlessPlatform
     */
    @Override
    protected void runTests() {
        logger.info("Running tests");
        FlightGearMain.runFlightgearTests(getSphereWorld().getTransform().getPosition());
        RuntimeTestUtil.assertNotNull("platzrunde", platzrundeForVisualizationOnly);
        //altitude probably has some rounding effects.
        RuntimeTestUtil.assertFloat("holding.altitude", 71, SGGeod.fromCart(platzrundeForVisualizationOnly.getPath().getSegment(0).getEnterNode().getLocation()).getElevationM(), 1);
        // 31.5.24: Some time in the past changed from 71 to 72.03, no idea why.
        RuntimeTestUtil.assertFloat("landing.altitude", 72.03, SGGeod.fromCart(platzrundeForVisualizationOnly.getPath().getSegment(platzrundeForVisualizationOnly.getPath().getSegmentCount() - 1).getEnterNode().getLocation()).getElevationM(), 1);

        TrafficRuntimeTestUtil.assertSceneryNodes(getSphereWorld());
        logger.info("Tests completed");
    }

    //@Override
    protected SceneNode getDestinationNode() {
        return getSphereWorld();
    }



    /**
     * 19.3.24: Now needed
     */
    public String getDefaultTilename() {
        // from former hardcoded
        // 14.5.24: now use config file
        //return formerInitialPositionEDDK.toString();
        TravelSceneHelper.registerFgTerrainBuilder();
        return "traffic-fg:flight/EDDK-sphere.xml";
    }
}


