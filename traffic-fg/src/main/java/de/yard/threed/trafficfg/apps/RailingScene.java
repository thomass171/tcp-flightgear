package de.yard.threed.trafficfg.apps;

import de.yard.threed.core.Color;
import de.yard.threed.core.Dimension;
import de.yard.threed.core.DimensionF;
import de.yard.threed.core.IntHolder;
import de.yard.threed.core.Payload;
import de.yard.threed.core.Point;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformHelper;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.testutil.RuntimeTestUtil;
import de.yard.threed.engine.AmbientLight;
import de.yard.threed.engine.BaseRequestRegistry;
import de.yard.threed.engine.Camera;
import de.yard.threed.engine.DirectionalLight;
import de.yard.threed.engine.Input;
import de.yard.threed.engine.KeyCode;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Mesh;
import de.yard.threed.engine.Observer;
import de.yard.threed.engine.ObserverSystem;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneMode;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.avatar.AvatarSystem;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsHelper;
import de.yard.threed.engine.ecs.InputToRequestSystem;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.SystemState;
import de.yard.threed.engine.ecs.TeleporterSystem;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.ecs.VelocityComponent;
import de.yard.threed.engine.geometry.ShapeGeometry;
import de.yard.threed.engine.gui.DefaultMenuProvider;
import de.yard.threed.engine.gui.FovElement;
import de.yard.threed.engine.gui.GuiGrid;
import de.yard.threed.engine.gui.GuiGridMenu;
import de.yard.threed.engine.gui.Icon;
import de.yard.threed.engine.gui.Label;
import de.yard.threed.engine.gui.MenuItem;
import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.engine.platform.common.ModelLoader;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.Settings;
import de.yard.threed.engine.util.NearView;
import de.yard.threed.engine.vr.VrInstance;
import de.yard.threed.flightgear.FgVehicleLoader;
import de.yard.threed.flightgear.core.simgear.scene.model.ACProcessPolicy;
import de.yard.threed.flightgear.ecs.FgAnimationUpdateSystem;
import de.yard.threed.flightgear.ecs.PropertyComponent;
import de.yard.threed.graph.Graph;
import de.yard.threed.graph.GraphMovingComponent;
import de.yard.threed.graph.GraphMovingSystem;
import de.yard.threed.graph.RailingFactory;
import de.yard.threed.traffic.GraphTerrainSystem;
import de.yard.threed.traffic.SphereSystem;
import de.yard.threed.traffic.TrafficConfig;
import de.yard.threed.traffic.TrafficGraph;
import de.yard.threed.traffic.TrafficSystem;
import de.yard.threed.traffic.config.XmlVehicleDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * No longer like OSM in x/y layer but in y=0. Saves many rotations and eases graph using(?).
 * <p>
 * Intentionally not extending BasicTravelScene to be config independent and demonstrate several options.
 * No scale down to 'H0', just trackwidth 1.
 * <p>
 * Keys are:
 * 'a' start move (via GraphMovingSystem)
 * '+' inc speed (via GraphMovingSystem)
 * '-' reduce speed (via GraphMovingSystem)
 * </p>
 * Uses FgVehicleLoader and FG compliant locomotive model from "railing/locomotive.xml".
 * <p>
 * 29.10.2017: Statt Viewpointsystem Teleport mit Avatar verwenden (z.B. fuer VR)
 * 11.05.2021: Gibt jetzt Observer. Avatar wird hier nicht mehr gebraucht. Stoert aber auch nicht.
 * <p>
 * <p>
 * 29.10.23 Migrating to tcp-flightgear
 * Created by thomass on 14.09.16.
 */
public class RailingScene extends Scene {
    public Log logger = Platform.getInstance().getLog(RailingScene.class);

    MenuItem[] menuitems;
    //21.10.19 optional
    NearView nearView = null;
    boolean enableNearView = false;
    private VrInstance vrInstance;
    //29.10.23 VehicleConfig vc;
    // in non VR for menu and control menu
    protected Camera cameraForMenu = null;

    @Override
    public String[] getPreInitBundle() {
        // 'data' is needed for textures
        return new String[]{"engine", "data", "traffic-fg"};
    }

    @Override
    public void initSettings(Settings settings) {
        settings.vrready = true;
        settings.aasamples = 4;
    }

    @Override
    public void init(SceneMode forServer) {
        logger.debug("init RailingScene");

        // Kruecke zur Entkopplung des Modelload von AC policy.
        ModelLoader.processPolicy = new ACProcessPolicy(null);

        processArguments();
        vrInstance = VrInstance.buildFromArguments();


        SphereSystem sphereSystem = new SphereSystem(null);
        SystemManager.addSystem(sphereSystem);

        //29.10.23: TrafficWorldConfig and TrafficWorldSystem replaced by SphereSystem more below
        //TrafficWorldConfig railingWorld = TrafficWorldConfig.readConfig("railing", "config/Railing.xml");
        //sceneConfig = railingWorld.getScene("Railing");

        //nearView soll nur die Lok abdecken.
        if (enableNearView) {
            nearView = new NearView(getDefaultCamera(), 0.01, 20, this);
            //damit man erkennt, ob alles an home attached ist weg von (0,0,0) und etwas höher
            nearView.setPosition(new Vector3(-30, 10, -10));
        }
        //27.12.21 TODO: fehlt ein Terrainbuilder als Parameter?
        GraphTerrainSystem graphTerrainSystem = new GraphTerrainSystem(/*10.12.21this, getWorld()*/null);
        SystemManager.addSystem(graphTerrainSystem);

        //29.10.23 GraphTerrainSystem should visualize
        //29.10.23GraphPosition startposition = trafficWorldSystem.getStartPosition();
        //29.10.23 RailingVisualizer visualizer = new RailingVisualizer();

        //20.11.20: Jetzt ueber System. Noch zu frueh. 13.12.21: Geht doch gar nicht ueber System.
        //graphTerrainSystem.visualizer=visualizer;
        //29.10.23 visualizer.visualize(TrafficWorldSystem.world.getGraph().getBaseGraph(), Scene.getWorld());

        addLight();

        //26.11.20 buildAvatar();

        //jetzt in TrafficWorldSystem TrafficScene.addOutsidePositions(avatar.pc, sceneConfig.getViewpoints());

        TeleporterSystem ts = new TeleporterSystem();
        //anim is stuttering
        ts.setAnimated(false);
        SystemManager.addSystem(ts, 0);

        SystemManager.addSystem(new ObserverSystem(true), 0);

        SystemManager.addSystem(new GraphMovingSystem(), 0);

        // We use 'ASI' from FG, so need the system for updating animations
        SystemManager.addSystem(new FgAnimationUpdateSystem());

        //23.10.19: plane below rails
        Material goundmat = Material.buildLambertMaterial(Color.GREEN);
        double planewidth = 160;
        double planeheight = 640;
        SceneNode ground = new SceneNode(new Mesh(ShapeGeometry.buildPlane(planewidth, planeheight, 1, 1), goundmat, true, true));
        ground.getTransform().setPosition(new Vector3(planewidth / 2, 0, -planeheight / 2));
        ground.setName("Ground");
        addToWorld(ground);

        menuitems = new MenuItem[]{
                new MenuItem(null, Label.LABEL_RESET, () -> {
                    logger.debug("reset");

                }),
                new MenuItem(null, Label.LABEL_START, () -> {
                    logger.debug("start");
                    EcsEntity engine = EcsHelper.findEntitiesByName("locomotive"/*"Locomotive"*/).get(0);
                    GraphMovingComponent.getGraphMovingComponent(engine).setAutomove(true);
                    InputToRequestSystem.sendRequestWithId(new Request(InputToRequestSystem.USER_REQUEST_MENU));
                }),
        };

        InputToRequestSystem inputToRequestSystem = new InputToRequestSystem();
        // 'M' is also used by VR controller
        inputToRequestSystem.addKeyMapping(KeyCode.M, InputToRequestSystem.USER_REQUEST_MENU);
        SystemManager.addSystem(inputToRequestSystem);

        if (vrInstance != null) {
            // Even in VR the observer will be attached to avatar later
            // Observer was inited before
            Observer observer = Observer.getInstance();
            observer.initFineTune(vrInstance.getOffsetVR());
            observer.attach(vrInstance.getController(0));
            observer.attach(vrInstance.getController(1));
        } else {
            // controlpanel for non VR
            inputToRequestSystem.setControlMenuBuilder(camera -> buildControlMenuForScene(camera));
            // use dedicated camera for menu to avoid picking ray issues due to large/small dimensions conflicts
            cameraForMenu = FovElement.getDeferredCamera(getDefaultCamera());
            // might be null if not desired
            inputToRequestSystem.setCameraForMenu(cameraForMenu);
            // menu only in non VR
            inputToRequestSystem.setMenuProvider(new DefaultMenuProvider(cameraForMenu, (Camera camera) -> {
                /**
                 * avoid menu coverage by loc parts by deferred camera.
                 */
                double width = 3.5;
                double zpos = -camera.getNear() - 0.001;
                double buttonzpos = zpos - 0.0001;
                GuiGrid m = GuiGrid.buildSingleColumnFromMenuitems(new DimensionF(width, width * 0.7), zpos, buttonzpos, menuitems);
                GuiGridMenu menu = new GuiGridMenu(m);
                return menu;
            }));
        }

        SystemManager.addSystem(new UserSystem());

        TrafficSystem trafficSystem = new TrafficSystem();
        trafficSystem.setVehicleLoader(new FgVehicleLoader());
        trafficSystem.addGraphFactory("RailSample1", () -> {
            Graph graph = RailingFactory.buildRailSample1(0);
            graph.getEdge(0).setName("firstEdge");
            return new TrafficGraph(graph);
        });
        SystemManager.addSystem(trafficSystem);

        //XmlDocument xmlRailingConfig = XmlDocument.buildFromBundle("traffic-fg", "railing/Railing.xml");
        TrafficConfig xmlRailingConfig = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("traffic-fg"),
                new BundleResource("railing/Railing.xml"));
        //XmlDocument xmlVehicleConfig = XmlDocument.buildFromBundle("traffic-fg", "railing/locomotive.xml");
        TrafficConfig xmlVehicleConfig = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("traffic-fg"),
                new BundleResource("railing/locomotive.xml"));
        //Provided by TrafficSystem now
        /*SystemManager.putDataProvider("vehicleconfig", new VehicleConfigDataProvider(
                XmlVehicleDefinition.convertVehicleDefinitions(xmlVehicleConfig.getVehicleDefinitions())));*/
        // TODO add vehicle via event.
        trafficSystem.addKnownVehicle(XmlVehicleDefinition.convertVehicleDefinitions(xmlVehicleConfig.getVehicleDefinitions()).get(0));

        SystemManager.addSystem(new AvatarSystem(false));

        //??SystemManager.addSystem(new FlatTerrainSystem());
        Observer.buildForDefaultCamera();

        List<String> vehicleList = new ArrayList<>();
        vehicleList.add("loc");
        SystemManager.putRequest(new Request(SphereSystem.USER_REQUEST_SPHERE, new Payload("traffic-fg:railing/Railing.xml", vehicleList)));

    }

    /**
     * 15.2.21: siehe buildfromarguments
     */
    @Deprecated
    protected void processArguments() {
        if (EngineHelper.isEnabled("argv.enableNearView")) {
            enableNearView = true;
        }
    }

    @Override
    public Dimension getPreferredDimension() {
        return new Dimension(800, 600);
    }

    protected void addLight() {
        DirectionalLight light = new DirectionalLight(Color.WHITE, new Vector3(0, 30000000, 20000000));
        addLightToWorld(light);
        // 4.3.25 ambient light instead of second directional
        //light = new DirectionalLight(Color.WHITE, new Vector3(0, -30000000, -20000000));
        //addLightToWorld(light);
        addLightToWorld(new AmbientLight(new Color(0.3f, 0.3f, 0.3f, 1.0f)));
    }

    int localState = 0;

    @Override
    public void update() {

        // 20.11.20: Eigentlich sollte die Reihenfolge keine Rolle spielen, aber erstmal doch? Zumindest soll das Vehicle vorerst NACH Avatar kommen, damit AvatarSystem
        // den Teleport beim Avatar eintraegt.
        // Der Join request bleibt liegen bis es "world" gibt. 26.11.20: Wirklich? Warum? "world" gibt es doch schon durch den init() der Systems. Nur vielleicht
        // noch kein Vehicle.
        // Avatar(System) wartet doch auf Vehicle zum attachen. Eigentlich auch umgekehrt.


        switch (localState) {
            case 0:
                //set ready to join to make login/join possible before vehicle
                SystemState.state = SystemState.STATE_READY_TO_JOIN;
                SystemManager.putRequest(UserSystem.buildLoginRequest("driver", ""));
                localState++;
                break;
            case 1:
                // 29.10.23: TRAFFIC_REQUEST_LOADVEHICLE2 replaced by ... TODO
                //SystemManager.putRequest(new Request(RequestRegistry.TRAFFIC_REQUEST_LOADVEHICLE2, new Payload(vc, nearView)));
                localState++;
                break;
        }

        Point mouselocation = Input.getMouseDown();

        //(V)alidate statt (T)est
        if (Input.getKeyDown(KeyCode.V)) {
            RailingTests.testInteriorView(this);
            logger.info("tests completed");
        }

        // fuer Speed Indicator.
        // TODO generic
        List<EcsEntity> engines = EcsHelper.findEntitiesByName("locomotive"/*"Locomotive"*/);
        if (engines.size() > 0) {
            PropertyComponent pc = PropertyComponent.getPropertyComponent(engines.get(0));
            if (pc != null) {
                pc.setSpeed(VelocityComponent.getVelocityComponent(engines.get(0)).getMovementSpeed());
            }
        }

        Observer.getInstance().update();
    }

    /**
     * The non VR Control menu.
     * Camera is a deferred camera defined during init.
     */
    public GuiGrid buildControlMenuForScene(Camera camera) {

        GuiGrid controlmenu = GuiGrid.buildForCamera(camera, 2, 5, 1, Color.BLACK_FULLTRANSPARENT, true);

        controlmenu.addButton(0, 0, 1, Icon.ICON_POSITION, () -> {
            InputToRequestSystem.sendRequestWithId(new Request(UserSystem.USER_REQUEST_TELEPORT, new Payload(new Object[]{new IntHolder(0)})));
        });
        controlmenu.addButton(1, 0, 1, Icon.ICON_MENU, () -> {
            InputToRequestSystem.sendRequestWithId(new Request(InputToRequestSystem.USER_REQUEST_MENU));
        });
        controlmenu.addButton(2, 0, 1, Icon.ICON_HORIZONTALLINE, () -> {
            InputToRequestSystem.sendRequestWithId(new Request(BaseRequestRegistry.TRIGGER_REQUEST_START_SPEEDDOWN));
        });
        controlmenu.addButton(3, 0, 1, Icon.ICON_PLUS, () -> {
            InputToRequestSystem.sendRequestWithId(new Request(BaseRequestRegistry.TRIGGER_REQUEST_START_SPEEDUP));
        });
        controlmenu.addButton(4, 0, 1, Icon.ICON_CLOSE, () -> {
            InputToRequestSystem.sendRequestWithId(new Request(InputToRequestSystem.USER_REQUEST_CONTROLMENU));
        });
        return controlmenu;
    }
}


class RailingTests {

    public static void testInteriorView(RailingScene railingScene) {
        // 4.6.24 somehow broekn in past RuntimeTestUtil.assertEquals("cameras", 1 + ((railingScene.nearView != null) ? 1 : 0), railingScene.getCameraCount());
        RuntimeTestUtil.assertEquals("camera.layer", 0, railingScene.getCamera(0).getLayer());
        NativeSceneNode chimney = SceneNode.findByName("Chimney").get(0);
        RuntimeTestUtil.assertNotNull("chimney", chimney);
        if (railingScene.nearView != null) {
            Camera nearViewCam = railingScene.getCamera(1);
            RuntimeTestUtil.assertEquals("camera.layer", railingScene.nearView.getLayer(), nearViewCam.getLayer());
            SceneNode parentNode = nearViewCam.getCarrier().getTransform().getParent().getSceneNode();
            RuntimeTestUtil.assertEquals("camera.parent.name", "basenode", parentNode.getName());
            RuntimeTestUtil.assertEquals("chimney.layer.", railingScene.nearView.getLayer(), chimney.getTransform().getLayer());

            PlatformHelper.traverseTransform(parentNode.getTransform().transform, child -> {
                RuntimeTestUtil.assertEquals("child.layer." + child.getSceneNode().getName(), railingScene.nearView.getLayer(), child.getSceneNode().getTransform().getLayer());
            });
        }

        //Avatar avatar = AvatarSystem.getAvatar();
    }
}