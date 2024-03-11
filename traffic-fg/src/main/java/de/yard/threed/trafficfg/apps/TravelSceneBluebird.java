package de.yard.threed.trafficfg.apps;


import de.yard.threed.core.Color;
import de.yard.threed.core.Degree;
import de.yard.threed.core.Dimension;
import de.yard.threed.core.DimensionF;
import de.yard.threed.core.Event;
import de.yard.threed.core.IntHolder;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.Payload;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.geometry.Primitives;
import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.testutil.RuntimeTestUtil;
import de.yard.threed.engine.Camera;
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
import de.yard.threed.engine.ecs.InputToRequestSystem;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.TeleportComponent;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.gui.ControlMenuBuilder;
import de.yard.threed.engine.gui.ControlPanel;
import de.yard.threed.engine.gui.ControlPanelArea;
import de.yard.threed.engine.gui.FovElement;
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
import de.yard.threed.engine.util.NearView;
import de.yard.threed.engine.vr.VrInstance;
import de.yard.threed.flightgear.FgBundleHelper;
import de.yard.threed.flightgear.FgTerrainBuilder;
import de.yard.threed.flightgear.FgVehicleLoader;
import de.yard.threed.flightgear.FlightGearMain;
import de.yard.threed.flightgear.SimpleBundleResourceProvider;
import de.yard.threed.flightgear.TerrainElevationProvider;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.flightgear.core.simgear.scene.model.ACProcessPolicy;
import de.yard.threed.flightgear.core.simgear.scene.model.OpenGlProcessPolicy;
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
import de.yard.threed.traffic.RequestRegistry;
import de.yard.threed.traffic.ScenerySystem;
import de.yard.threed.traffic.SolarSystem;
import de.yard.threed.traffic.TrafficConfig;
import de.yard.threed.traffic.TrafficGraph;
import de.yard.threed.traffic.TrafficHelper;
import de.yard.threed.traffic.TrafficSystem;
import de.yard.threed.traffic.VehicleBuiltDelegate;
import de.yard.threed.traffic.VehicleLauncher;
import de.yard.threed.traffic.WorldGlobal;
import de.yard.threed.traffic.apps.BasicTravelScene;
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
import de.yard.threed.trafficcore.config.AirportDefinition;
import de.yard.threed.trafficcore.model.Runway;
import de.yard.threed.trafficcore.model.Vehicle;
import de.yard.threed.trafficfg.AutomoveSystem;
import de.yard.threed.trafficfg.FgBackProjectionProvider;
import de.yard.threed.trafficfg.FgCalculations;
import de.yard.threed.trafficfg.SGGeodAltitudeProvider;
import de.yard.threed.trafficfg.StgCycler;
import de.yard.threed.trafficfg.TravelHelper;
import de.yard.threed.trafficfg.VehicleEntityBuilder;
import de.yard.threed.trafficfg.config.ConfigHelper;
import de.yard.threed.trafficfg.fgadapter.FlightGearSystem;
import de.yard.threed.trafficfg.flight.FlightSystem;
import de.yard.threed.trafficfg.flight.GroundNet;
import de.yard.threed.trafficfg.flight.GroundServicesSystem;
import de.yard.threed.trafficfg.flight.RouteBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Like the big TravelScene, but only bluebird (no external, so much simpler.
 * No Ground services (but groundnet), no aircraft marking.
 * <p>
 * Full sgmaterial is not available
 */
public class TravelSceneBluebird extends BasicTravelScene {
    static Log logger = Platform.getInstance().getLog(TravelSceneBluebird.class);
    Light light;
    private static final int WIDTH = 1024;
    private static final int HEIGHT = 768;

    // Wofuer ist die world Zwsichenebene. Zum adjusten?
    // 20.3.18: Ja, zum komplettverschieben von allem, um Artefakte wegen Rundungsproblemen zu
    // vermeiden. Das ist was anderes als die Scene.world.
    SceneNode world;
    static Vector3 camerapositioninmodel = new Vector3(0, 0.6f, 0);

    // freecam ist mal für Tests
    //22.10.21 boolean freecam = false;
    // erste Position. 6/17: 3=equator,4=Dahlem ,5=EDDK
    int startpos = 5;
    Graph orbit;
    private FlightRoute platzrunde/*, orbittour*/;
    VehicleDefinition configShuttle;
    private EcsEntity orbitingvehicle = null;
    private boolean avatarInited = false;
    protected AirportDefinition airportDefinition;
    TrafficConfig trafficConfig;
    // worldPois are contained in trafficConfig
    // TrafficConfig worldPois;
    TrafficConfig vdefs;

    @Override
    public String[] getPreInitBundle() {
        // "fgdatabasic" and "traffic-fg" are needed for bluebird
        // 'TerraySync' is loaded at runtime by TerraSyncBundleResolver' that is set up by the platform(using HOSTDIRFG on desktop and "bundles" in webgl)
        return new String[]{"engine", FlightGear.getBucketBundleName("model"), "sgmaterial", "fgdatabasic", "traffic-fg"};
    }

    @Override
    public void customInit() {
        logger.debug("init Flight");

        vehiclelistname = "VehiclesWithCockpit";
        world = new SceneNode();
        world.setName("FlightWorld");

        FlightGearMain.initFG(new FlightLocation(WorldGlobal.equator020000, new Degree(0), new Degree(0)), null);
        FgBundleHelper.addProvider(new SimpleBundleResourceProvider("fgdatabasicmodel"));

        trafficConfig = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("traffic-fg"), new BundleResource("flight/EDDK-bluebird.xml"));
        // not until we define some worldPois = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("traffic-advanced"), new BundleResource("world-pois.xml"));
        airportDefinition = trafficConfig.findAirportDefinitionsByIcao("EDDK").get(0);

        //solange es kein Terrain gibt, immer elevation 80; was aber reichlich fraglich ist. Der braucht keine adjustment world
        TerrainElevationProvider tep = TerrainElevationProvider.buildForStaticAltitude(80);
        SystemManager.putDataProvider(SystemManager.DATAPROVIDERELEVATION, tep);

        ScenerySystem ts = new ScenerySystem(world);
        AbstractTerrainBuilder terrainBuilder = new FgTerrainBuilder();
        terrainBuilder.init(world);
        ts.setTerrainBuilder(terrainBuilder);
        SystemManager.addSystem(ts, 0);
        // TerrainElevationProvider was created in FgTerrainBuilder. Needs help because EDDK groundnet exceeds EDDK tile, so define a default value 68.
        ((TerrainElevationProvider) SystemManager.getDataProvider(SystemManager.DATAPROVIDERELEVATION)).setDefaultAltitude(68.0);

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

        SceneNode cube = ModelSamples.buildCube(1000, new Color(0xCC, 00, 00));
        if (cube != null) {
            cube.getTransform().setPosition(new Vector3(3987743.8f, 480804.66f, 4937917.5f));
            //cube.setPosition(new Vector3(0, 0, 0));
            world.attach(cube);
        }
        addToWorld(world);

        //visualizeTrack soll auch im usermode verfuegbar sein.
        if (visualizeTrack) {
            SystemManager.addSystem(new GraphVisualizationSystem(new SimpleGraphVisualizer(world)));
        }
        EllipsoidCalculations rbcp = TrafficHelper.getEllipsoidConversionsProviderByDataprovider();

        SystemManager.addSystem(new FlightSystem());
        SystemManager.addSystem(new AnimationUpdateSystem());
        SystemManager.addSystem(new FlightGearSystem());

        ((GraphTerrainSystem) SystemManager.findSystem(GraphTerrainSystem.TAG)).disable();

        SystemManager.addSystem(new GroundServicesSystem());
        GroundServicesSystem.airportConfigBundle = "traffic-fg";
        GroundServicesSystem.airportConfigFullName = "flight/EDDK-bluebird.xml";

        SystemManager.registerService("vehicleentitybuilder", new VehicleEntityBuilder());
        vdefs = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("traffic-fg"), new BundleResource("flight/vehicle-definitions.xml"));

        TrafficSystem.knownVehicles.addAll(XmlVehicleDefinition.convertVehicleDefinitions(vdefs.getVehicleDefinitions()));

        // 29.8.23: Missing for orbit tour, but is it correct? Well, orbittour seems to work.
        ((GraphMovingSystem) SystemManager.findSystem(GraphMovingSystem.TAG)).graphAltitudeProvider = new SGGeodAltitudeProvider();

        //groundnet is set later
        trafficSystem.locationList = airportDefinition.getLocations();
        trafficSystem.destinationNode = getDestinationNode();
        trafficSystem.nearView = nearView;
        trafficSystem.setVehicleLoader(new FgVehicleLoader());

        if (VrInstance.getInstance() == null) {
            InputToRequestSystem inputToRequestSystem = (InputToRequestSystem) SystemManager.findSystem(InputToRequestSystem.TAG);
            inputToRequestSystem.setControlMenuBuilder(camera -> buildControlMenuForScene(camera));
            // cameraForMenu already set by super class
        }

    }

    @Override
    protected void customProcessArguments() {
    }

    @Override
    public EcsSystem[] getCustomTerrainSystems() {
        return new EcsSystem[]{};
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
                    TravelHelper.startFlight(Destination.buildRoundtrip(0), getAvatarVehicle());
                    InputToRequestSystem.sendRequestWithId(new Request(InputToRequestSystem.USER_REQUEST_MENU));
                }),
                new MenuItem(null, new Text("Low Orbit Tour", Color.RED, Color.LIGHTGRAY), () -> {
                    logger.debug("menu: low orbit track");
                    TravelHelper.startFlight(Destination.buildRoundtrip(1), getAvatarVehicle());
                    InputToRequestSystem.sendRequestWithId(new Request(InputToRequestSystem.USER_REQUEST_MENU));
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
        };
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

        SceneNode parent = world;
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

    @Override
    public LightDefinition[] getLight() {
        return new LightDefinition[]{
                new LightDefinition(Color.WHITE, new Vector3(0, 30000000, 20000000)),
                new LightDefinition(Color.WHITE, new Vector3(0, -30000000, -20000000)),
        };
    }

    /**
     * Override the default control panel.
     * A traffic 3x3 control panel permanently attached to the left controller. Consists of
     * <p>
     * 0) load - automove - finetune up
     * 1) aircraft selector
     * 2) service - trip - finetune down
     * teleport not needed because already on VR button.
     */
    @Override
    public ControlPanel buildVrControlPanel() {

        double ControlPanelWidth = 0.3;
        double ControlPanelRowHeight = 0.1;
        int ControlPanelRows = 3;
        double[] ControlPanelColWidth = new double[]{0.1, 0.1, 0.1};
        double ControlPanelMargin = 0.005;
        Color controlPanelBackground = Color.LIGHTGREEN;

        ControlPanel cp = new ControlPanel(new DimensionF(ControlPanelWidth, ControlPanelRows * ControlPanelRowHeight), Material.buildBasicMaterial(controlPanelBackground, false), 0.01);
        PanelGrid panelGrid = new PanelGrid(ControlPanelWidth, ControlPanelRowHeight, ControlPanelRows, ControlPanelColWidth);

        // top line:
        cp.addArea(panelGrid.getPosition(0, 2), new DimensionF(ControlPanelColWidth[2], ControlPanelRowHeight), () -> {
            logger.debug("load clicked");
            SystemManager.putRequest(RequestRegistry.buildLoadVehicle(UserSystem.getInitialUser().getId(), null, null));
        }).setIcon(Icon.IconCharacter(11));
        cp.addArea(panelGrid.getPosition(1, 2), new DimensionF(ControlPanelColWidth[2], ControlPanelRowHeight), () ->
                SystemManager.putRequest(new Request(UserSystem.USER_REQUEST_AUTOMOVE))).setIcon(Icon.IconCharacter(0));
        cp.addArea(panelGrid.getPosition(2, 2), new DimensionF(ControlPanelColWidth[2], ControlPanelRowHeight), buttonDelegates.get("up")).setIcon(Icon.ICON_UPARROW);

        // mid line
        //double iconsize = size.height - 2 * margin;
        double iconareasize = ControlPanelRowHeight;
        double textareawidth = ControlPanelWidth - 1 * iconareasize;

        // text has no margin yet.
        TextTexture textTexture = new TextTexture(Color.LIGHTGRAY);
        ControlPanelArea textArea = cp.addArea(new Vector2(-iconareasize / 2, 0), new DimensionF(textareawidth, ControlPanelRowHeight), null);
        // empty string fails due to length 0
        textArea.setTexture(textTexture.getTextureForText(" ", Color.RED));


        /*cp.addArea(panelGrid.getPosition(2, 1), new DimensionF(ControlPanelColWidth[2], ControlPanelRowHeight), () -> {
            // cycleAircraft();
            //updateHud();
        }).setIcon(Icon.ICON_PLUS);*/

        // bottom line:
        cp.addArea(panelGrid.getPosition(0, 0), new DimensionF(ControlPanelColWidth[2], ControlPanelRowHeight), () -> {
            // no marking for now
        }).setIcon(Icon.IconCharacter(18));
        cp.addArea(panelGrid.getPosition(1, 0), new DimensionF(ControlPanelColWidth[2], ControlPanelRowHeight), () -> {
            TravelHelper.startDefaultTrip(getAvatarVehicle());
        }).setIcon(Icon.IconCharacter(19));
        cp.addArea(panelGrid.getPosition(2, 0), new DimensionF(ControlPanelColWidth[2], ControlPanelRowHeight), buttonDelegates.get("down")).setIcon(Icon.ICON_DOWNARROW);
        return cp;
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

        // Platzrunde (und wakeup) erst anlegen, wenn das Terrain da ist und worldadjustment durch ist. Schwierig zu erkennen
        boolean terrainavailable = false;
        if (AbstractSceneRunner.getInstance().getFrameCount() > 50) {
            terrainavailable = true;
        }

        if (terrainavailable && GroundServicesSystem.groundnetEDDK != null && !populated) {
            //jetzt muesste ein Groundnet mit 2D Projection da sein.
            populated = true;
            trafficSystem.groundNet = GroundServicesSystem.groundnetEDDK.groundnetgraph;
        }

        if (Input.getKeyDown(KeyCode.D)) {
            logger.debug("scenegraph:" + dumpSceneGraph());
        }
        // 29.8.23: As long a menu isn't working start orbit tour via key.'R' probably not yet in use.
        if (Input.getKeyDown(KeyCode.R)) {
            logger.debug("orbit tour:" + dumpSceneGraph());
            TravelHelper.startFlight(Destination.buildRoundtrip(1), getAvatarVehicle());
        }

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
        //needed somehow?? FlatAirportScene.adjustASI();

        //26.10.18: Jetzt 'S' statt Alpha6. Aber nicht im FPC mode ohne Avatar.
        if (Input.getKeyDown(KeyCode.S) /*10.3.22&& AvatarSystem.getAvatar() != null*/) {
            TravelHelper.startDefaultTrip(getAvatarVehicle());
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

    /**
     * Non VR Control menu.
     * Camera is a deferred camera defined during init.
     * <p>
     */
    public GuiGrid buildControlMenuForScene(Camera camera) {

        GuiGrid controlmenu = GuiGrid.buildForCamera(camera, 2, 3, 1, Color.BLACK_FULLTRANSPARENT, true);

        controlmenu.addButton(0, 0, 1, Icon.ICON_POSITION, () -> {
            InputToRequestSystem.sendRequestWithId(new Request(UserSystem.USER_REQUEST_TELEPORT, new Payload(new Object[]{new IntHolder(0)})));
        });
        controlmenu.addButton(1, 0, 1, Icon.ICON_MENU, () -> {
            InputToRequestSystem.sendRequestWithId(new Request(InputToRequestSystem.USER_REQUEST_MENU));
        });
        controlmenu.addButton(2, 0, 1, Icon.ICON_CLOSE, () -> {
            InputToRequestSystem.sendRequestWithId(new Request(InputToRequestSystem.USER_REQUEST_CONTROLMENU));
        });
        return controlmenu;
    }
}


