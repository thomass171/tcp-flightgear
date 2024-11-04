package de.yard.threed.trafficfg.apps;


import de.yard.threed.core.Color;
import de.yard.threed.core.Degree;
import de.yard.threed.core.Dimension;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.RuntimeTestUtil;
import de.yard.threed.engine.Input;
import de.yard.threed.engine.KeyCode;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.apps.ModelSamples;
import de.yard.threed.engine.ecs.EcsSystem;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.TeleportComponent;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.gui.MenuItem;
import de.yard.threed.engine.gui.Text;
import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.engine.platform.ProcessPolicy;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.Settings;
import de.yard.threed.engine.util.NearView;
import de.yard.threed.flightgear.FgBundleHelper;
import de.yard.threed.trafficfg.fgadapter.FgTerrainBuilder;
import de.yard.threed.flightgear.FgVehicleLoader;
import de.yard.threed.flightgear.FlightGearMain;
import de.yard.threed.flightgear.SimpleBundleResourceProvider;
import de.yard.threed.flightgear.TerrainElevationProvider;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.flightgear.core.simgear.scene.model.ACProcessPolicy;
import de.yard.threed.flightgear.ecs.FgAnimationUpdateSystem;
import de.yard.threed.traffic.AbstractSceneryBuilder;
import de.yard.threed.traffic.EllipsoidCalculations;
import de.yard.threed.traffic.GraphBackProjectionProvider;
import de.yard.threed.traffic.GraphTerrainSystem;
import de.yard.threed.traffic.LightDefinition;
import de.yard.threed.traffic.ScenerySystem;
import de.yard.threed.traffic.TrafficHelper;
import de.yard.threed.traffic.WorldGlobal;
import de.yard.threed.traffic.apps.BasicTravelScene;
import de.yard.threed.traffic.config.VehicleConfigDataProvider;
import de.yard.threed.traffic.flight.FlightLocation;
import de.yard.threed.traffic.flight.FlightRouteGraph;
import de.yard.threed.traffic.geodesy.GeoCoordinate;
import de.yard.threed.trafficcore.model.Vehicle;
import de.yard.threed.trafficfg.FgCalculations;

import java.util.List;

/**
 * Like BasicTravelScene the base class to run a traffic scene from config files.
 * Derived from TravelScene with the steps:
 * - old stuff removed
 * - unwanted stuff removed (hud, navigator, cubes,pillars and other marker, stgcycler, shuttle, Alpha7 use case, orbit).
 * - currently unavailable stuff commented (like GroundServices). Marked with "NOT-YET"
 * <p>
 * Currently this class is just an indicator for what is missing.
 * <p>
 * Idea is: Should be sufiicent to override some methods and rely on the super class for everything else. But that will be a long way to go.
 * <p>
 * Views according to config.
 * 20.1.24 just a design idea for now.
 * <p>
 */
public class TrafficScene extends BasicTravelScene {
    static Log logger = Platform.getInstance().getLog(TrafficScene.class);
    private static final int WIDTH = 1024;
    private static final int HEIGHT = 768;
    SceneNode world;

    private FlightRouteGraph platzrunde;
    private boolean avatarInited = false;

    @Override
    public String[] getPreInitBundle() {

        //'TerraSync-model' is loaded in preinit to avoid async issues if done later. Needs required custom resolver be available in plaform setup
        return new String[]{"engine", FlightGear.getBucketBundleName("model"), /*2.10.23 "data-old", "data", "fgdatabasic", "fgdatabasicmodel",FlightGear.getBucketBundleName("model"),FlightGearSettings.FGROOTCOREBUNDLE*/ "sgmaterial"
                /*BundleRegistry.FGHOMECOREBUNDLE,*/};
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

        //20.11.23 "NOT-YET"tw = TrafficWorldConfig.readConfig("data-old", "TrafficWorld.xml");
        //20.11.23 "NOT-YET"sceneConfig = tw.getScene("Flight");

        //solange es kein Terrain gibt, immer elevation 80; was aber reichlich fraglich ist. Der braucht keine adjustment world
        TerrainElevationProvider tep = TerrainElevationProvider.buildForStaticAltitude(80);
        SystemManager.putDataProvider(SystemManager.DATAPROVIDERELEVATION, tep);

        // 25.9.23: Replace TerrainSystem with ScenerySystem
        ScenerySystem ts = new ScenerySystem(world);
        AbstractSceneryBuilder terrainBuilder = new FgTerrainBuilder();
        terrainBuilder.init(world);
        ts.setTerrainBuilder(terrainBuilder);
        SystemManager.addSystem(ts, 0);

        //20.11.23 "NOT-YET"SystemManager.addSystem(new AutomoveSystem());

        initHud();
        if (hud != null && hud.element != null && hud.element.getMesh() != null) {
            hud.setText(0, " ");
        }

        //nearView soll nur die Vehicle abdecken.
        if (enableNearView) {
            nearView = new NearView(getDefaultCamera(), 0.01, 20, this);
        }

        addToWorld(world);

        //visualizeTrack soll auch im usermode verfuegbar sein.
        if (visualizeTrack) {
            //20.11.23 "NOT-YET"SystemManager.addSystem(new GraphVisualizationSystem(new SimpleGraphVisualizer(world)));
        }
        EllipsoidCalculations rbcp = TrafficHelper.getEllipsoidConversionsProviderByDataprovider();
        SceneNode helpline = ModelSamples.buildLine(rbcp.toCart(WorldGlobal.eddkoverview.location.coordinates, null, null), rbcp.toCart(WorldGlobal.elsdorf2000.location.coordinates, null, null), Color.ORANGE);
        world.attach(helpline);

        //20.11.23 "NOT-YET"SystemManager.addSystem(new FlightSystem());
        SystemManager.addSystem(new FgAnimationUpdateSystem());

        //16.6.20 request geht hier noch nicht wegen "not inited". Darum weiter vereinfacht initialposition. Das ist nur erstmal so ungefähr für Terrain
        //7.10.21 moved to Sphere initialPosition = WorldGlobal.eddkoverviewfar.location.coordinates;//SGGeod.fromLatLon(gsw.getAirport("EDDK").getCenter());
        //24.5.20 trafficWorld.nearestairport = gsw.airport;

        //10.12.21: Not needed currently in 3D
        ((GraphTerrainSystem) SystemManager.findSystem(GraphTerrainSystem.TAG)).disable();

        //20.11.23 "NOT-YET"AbstractSceneRunner.instance.httpClient = new AirportDataProviderMock();
        //20.11.23 "NOT-YET"SystemManager.addSystem(new GroundServicesSystem());

        //20.11.23 "NOT-YET"SystemManager.registerService("vehicleentitybuilder", new VehicleEntityBuilder());
        //20.11.23 "NOT-YET"TrafficSystem.genericVehicleBuiltDelegate = new DoormarkerDelegate();
        //20.11.23 "NOT-YET"SystemManager.putDataProvider("aircraftconfig", new AircraftConfigProvider(tw));

        //groundnet is set later
        //20.11.23 "NOT-YET"trafficSystem.locationList=getLocationList();
        trafficSystem.destinationNode = getDestinationNode();
        trafficSystem.nearView = nearView;
        trafficSystem.setVehicleLoader(new FgVehicleLoader());
    }

    @Override
    public EcsSystem[] getCustomTerrainSystems() {
        return new EcsSystem[]{};
    }

    @Override
    public List<Vehicle> getVehicleList() {
        return null;//20.11.23 "NOT-YET"tw.getVehicleListByName(vehiclelistname);
    }

    @Override
    public ProcessPolicy getProcessPolicy() {
        return new ACProcessPolicy(null);
    }

    @Override
    public MenuItem[] getMenuItems() {
        return new MenuItem[]{
                /*20.11.23 "NOT-YET"new MenuItem(null, new Text("Default Trip", Color.RED, Color.LIGHTGRAY), () -> {
                    logger.debug("Default Trip");
                    TravelHelper.startFlight(Destination.buildRoundtrip(0), getAvatarVehicle());
                }),
                new MenuItem(null, new Text("Low Orbit Tour", Color.RED, Color.LIGHTGRAY), () -> {
                    logger.debug("menu: low orbit track");
                    TravelHelper.startFlight(Destination.buildRoundtrip(1), getAvatarVehicle());
                }),
                new MenuItem(null, new Text("Enter Equator Orbit", Color.RED, Color.LIGHTGRAY), () -> {
                    logger.debug("menu: equator orbit");
                    TravelHelper.startFlight(Destination.buildForOrbit(true), getAvatarVehicle());
                }),*/
                new MenuItem(null, new Text("Enter Moon Orbit", Color.RED, Color.LIGHTGRAY), () -> {
                    logger.debug("menu: moon orbit");
                }),
                new MenuItem(null, new Text("Dahlem", Color.RED, Color.LIGHTGRAY), () -> {
                }),
        };
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

    private void setModelLocation(SceneNode model, LocalTransform posrot) {
        model.getTransform().setPosition(posrot.position);
        model.getTransform().setRotation(posrot.rotation);
    }

    boolean populated = false;

    @Override
    public void customUpdate(double currentdelta) {

        if (!avatarInited) {
            //25.10.21 aus init moved here
            TeleportComponent avatartc = TeleportComponent.getTeleportComponent(UserSystem.getInitialUser());

            if (avatartc == null) {
                logger.warn("no avatar (tc) yet");
            } else {
                //20.11.23 "NOT-YET": navigator is needed for global teleports
                // buildNavigator(avatartc);

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

        /*20.11.23 "NOT-YET"if (terrainavailable && GroundServicesSystem.groundnetEDDK != null && !populated) {
            //jetzt muesste ein Groundnet mit 2D Projection da sein.

            GroundNet groundnet = GroundServicesSystem.groundnetEDDK;

            // Fuer GraphTest mal eine c172p setzen. Die muss dann auf B_8 stehen mit Heading C_4. Der Graph bekommt keine back projection.
            // TODO auslagenr in einen Test? Zwei c172p sind störend für z.B. Platzrunde
            boolean simplegraphtest = false;
            if (simplegraphtest) {
                VehicleConfig configc172p = ConfigHelper.getVehicleConfig(tw.tw, "c172p");
                GraphPosition c_4position = groundnet.getParkingPosition(groundnet.getParkPos("C_4"));
                TrafficGraph trafficgraph = new RouteBuilder(TrafficHelper.getEllipsoidConversionsProviderByDataprovider()).buildSimpleTestRouteB8toC4(groundnet);
                VehicleLauncher.launchVehicle(new Vehicle("c172p"), configc172p, trafficgraph, new GraphPosition(trafficgraph.getBaseGraph().getEdge(0)),
                        TeleportComponent.getTeleportComponent(UserSystem.getInitialUser()), world, null, sceneConfig.getBaseTransformForVehicleOnGraph(), null, new VehicleBuiltDelegate[]{}, new FgVehicleLoader());
            }
            populated = true;
            trafficSystem.groundNet=GroundServicesSystem.groundnetEDDK.groundnetgraph;
        }*/

        if (Input.getKeyDown(KeyCode.D)) {
            logger.debug("scenegraph:" + dumpSceneGraph());
        }
        // 29.8.23: As long a menu isn't working start orbit tour via key.'R' probably not yet in use.
        /*20.11.23 "NOT-YET"if (Input.getKeyDown(KeyCode.R)) {
            logger.debug("orbit tour:" + dumpSceneGraph());
            TravelHelper.startFlight(Destination.buildRoundtrip(1), getAvatarVehicle());
        }*/

        // Platzrunde erst anlegen, wenn das Terrain da ist und worldadjustment durch ist.
        /*20.11.23 "NOT-YET"if (platzrunde == null && terrainavailable) {
            logger.info("Building Platzrunde");
            // Zum Test direkt mal den Rundflug einblenden
            Runway runway14l = OsmRunway.eddk14L();
            platzrunde = new RouteBuilder(TrafficHelper.getEllipsoidConversionsProviderByDataprovider()).buildFlightRouteGraph(runway14l, null, 0);
            SystemManager.sendEvent(new Event(GraphEventRegistry.GRAPH_EVENT_PATHCREATED, new Payload(platzrunde.getGraph(), platzrunde.getPath())));
        }*/

        //20.11.23 "NOT-YET" FlatTravelScene.adjustASI();

        //26.10.18: Jetzt 'S' statt Alpha6. Aber nicht im FPC mode ohne Avatar.
        /*20.11.23 "NOT-YET"if (Input.getKeyDown(KeyCode.S)) {
            TravelHelper.startDefaultTrip(getAvatarVehicle());
        }
        if (Input.getKeyDown(KeyCode.Alpha4)) {
            TravelHelper.startFlight(Destination.buildByIcao("EDDF"), getAvatarVehicle());
        }*/
    }

    @Override
    public EllipsoidCalculations getRbcp() {
        return new FgCalculations();
    }

    //29.10.21 Moved to TrafficSystem. 17.12.21: No longer, needs FgMath
    @Override
    public GraphBackProjectionProvider/*Flight3D*/ getGraphBackProjectionProvider() {
        return null;//20.11.23 "NOT-YET"new FgBackProjectionProvider();
    }

    @Override
    protected Log getLog() {
        return logger;
    }

    @Override
    protected void initSpheres() {
        logger.debug("initSpheres");
        //20.11.23 "NOT-YET"SolarSystem solarSystem = new SolarSystem();
        SceneNode sun = null;//20.11.23 "NOT-YET"solarSystem.build(2 * WorldGlobal.DISTANCEMOONEARTH, WorldGlobal.DISTANCEMOONEARTH/*km(18000)*/);
        addToWorld(sun);
    }

    @Override
    public VehicleConfigDataProvider getVehicleConfigDataProvider() {
        return null;//20.11.23 "NOT-YET"new VehicleConfigDataProvider(tw.tw);
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
}

