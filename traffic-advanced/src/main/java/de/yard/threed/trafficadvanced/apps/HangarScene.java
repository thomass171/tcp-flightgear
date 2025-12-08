package de.yard.threed.trafficadvanced.apps;


import de.yard.threed.core.Color;
import de.yard.threed.core.Degree;
import de.yard.threed.core.DimensionF;
import de.yard.threed.core.IntHolder;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Payload;
import de.yard.threed.core.Point;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.HttpBundleResolver;
import de.yard.threed.engine.*;
import de.yard.threed.engine.avatar.AvatarSystem;
import de.yard.threed.engine.ecs.*;
import de.yard.threed.engine.geometry.ShapeGeometry;
import de.yard.threed.engine.gui.*;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.ModelLoader;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.Settings;
import de.yard.threed.engine.shading.ShaderDebugger;
import de.yard.threed.engine.vr.VrInstance;
import de.yard.threed.engine.vr.VrOffsetWrapper;
import de.yard.threed.flightgear.FgBundleHelper;
import de.yard.threed.flightgear.FgVehicleLoader;
import de.yard.threed.flightgear.FlightGearSettings;
import de.yard.threed.flightgear.SimpleBundleResourceProvider;
import de.yard.threed.flightgear.core.SGLoaderOptions;
import de.yard.threed.flightgear.core.flightgear.main.FGGlobals;
import de.yard.threed.flightgear.core.simgear.scene.model.ACProcessPolicy;
import de.yard.threed.flightgear.ecs.FgAnimationComponent;
import de.yard.threed.flightgear.ecs.FgAnimationUpdateSystem;
import de.yard.threed.traffic.*;
import de.yard.threed.traffic.config.VehicleDefinition;
import de.yard.threed.trafficadvanced.AdvancedConfiguration;
import de.yard.threed.trafficcore.model.Vehicle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Scene for viewing vehicles from outside and cockpit, but without vehicle movement.
 * Also in AR mode. 777 CDU is used for menu.
 * <p>
 * Different from FgModelPreviewScene which is more a model inspector and allows rotating and scaling.
 * <p>
 * Uses ECS (eg. TeleporterSystem) but no graphs.
 * <p>
 * While (Fg)ModelPreviewScene and (Fg)GalleryScene have the AnimationControlPanel for modifying properties in the property tree via menu,
 * we here have just the option to control a pseudo speed (the vehicle still will not move). That should adjust all related properties.
 * <p>
 * 18.10.19: x/y/z calibration has wrong orientation?
 * Uses y=0 plane different from FlatTravelScene?
 * <p>
 * Keys:
 * t teleport, cycle position
 * l cycle aircraft (was 'n' once)
 * m cycle menu (cdu and 'normal')
 * 7.3.21: 'Lok' doesn't really belong here but to 'railing'. Otherwise it's just a vehicle model loaded from a bundle
 * <p>
 * 4.12.23: Migration from TrafficWorldConfig to TrafficConfig (without sceneconfig).
 * 30.1.24: f.k.a. CockpitScene
 * 1.2.24: Prepared for AR and a new loaded vehicle no longer replaces the current but is located behind.
 * 03.12.25: (pseudo) speedup/down added for seeing related animations
 */
public class HangarScene extends Scene {
    Log logger = Platform.getInstance().getLog(HangarScene.class);
    private double backlightitensity = 0.5f;
    FirstPersonController fps = null;
    boolean usearp = true;
    int vehicleindex = 0;
    TrafficConfig tw;
    // loaded from VehiclesWithCockpit" in "vehicle-definitions.xml" (and hardcoded bluebird)
    public List<String> vehiclelist = new ArrayList<String>();
    MenuCycler menuCycler = null;
    boolean avatarInited = false;
    public boolean modelInited = false;
    String vrMode = null;
    VrInstance vrInstance;
    Map<String, ButtonDelegate> buttonDelegates = new HashMap<String, ButtonDelegate>();
    String initialVehicle = null;
    public MessageBox msgBox = null;
    ShaderDebugger shaderDebugger = null;
    SceneNode mainVehicleNode;

    @Override
    public void init(SceneMode forServer) {
        vrMode = Platform.getInstance().getConfiguration().getString("vrMode");

        processArguments();

        // Kruecke zur Entkopplung des Modelload von AC policy.
        ModelLoader.processPolicy = new ACProcessPolicy(null);

        tw = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("traffic-advanced"), BundleResource.buildFromFullString("Hangar.xml"));

        List<Vehicle> vlist = FlightTravelScene.getVehicleListByName("VehiclesWithCockpit");

        // 8.8.24: hardcoded bluebird added (because it isn't in the vehiclelist)
        vehiclelist.add("bluebird");
        for (int i = 0; i < vlist.size(); i++) {
            vehiclelist.add(vlist.get(i).getName());
        }

        buttonDelegates.put("up", () -> {
            logger.info("up");
            Observer.getInstance().fineTune(true);
        });
        buttonDelegates.put("down", () -> {
            logger.info("down");
            Observer.getInstance().fineTune(false);
        });

        Observer observer = Observer.buildForDefaultCamera();

        vrInstance = VrInstance.buildFromArguments();

        SystemManager.addSystem(new UserSystem());
        SystemManager.addSystem(new AvatarSystem());

        TeleporterSystem ts = new TeleporterSystem();
        //anim is stuttering
        ts.setAnimated(false);
        SystemManager.addSystem(ts, 0);

        ObserverSystem viewingsystem = new ObserverSystem(true);
        SystemManager.addSystem(viewingsystem, 0);

        // for click in CDU menu. 3.12.25 Now also for seeing speed dependent animations
        FgAnimationUpdateSystem animationUpdateSystem = new FgAnimationUpdateSystem();
        SystemManager.addSystem(animationUpdateSystem, 0);

        //addToWorld(ModelSamples.buildAxisHelper(200, 1));

        if (usearp) {
            FgBundleHelper.addProvider(new SimpleBundleResourceProvider("fgdatabasic"));
            // arp = new AircraftResourceProvider();
            // BundleRegistry.addProvider(arp);
        }

        PickingRayObjectSelector objectSelector = new PickingRayObjectSelector(getMainCamera(), "mainVehicleNode");
        shaderDebugger = new ShaderDebugger(objectSelector);
        msgBox = new MessageBox(Color.ORANGE);
        shaderDebugger.setMessageBox(msgBox);

        menuCycler = new MenuCycler(new MenuProvider[]{new CockpitMenuBuilder(this), new DefaultMenuProvider(this.getDefaultCamera(), (Camera camera) -> {
            // zpos -5 should also work good in VR
            double width = 5.5;

            GuiGrid menu = new GuiGrid(new DimensionF(width, width * 0.7), -5, -4.9, 1, 6, 3, GuiGrid.GREEN_SEMITRANSPARENT);
            // Mid right a button with image
            menu.addButton(4, 1, 1, Icon.ICON_CLOSE, () -> {
                menuCycler.close();
            });
            // Mid bottom a wide button with text
            menu.addButton(2, 0, 2, new Text("Load", Color.BLUE, Color.LIGHTBLUE), () -> {
                //No rs.menuCycler.close() here becasue we might do any more?
                addNextVehicle();
            });

            IntHolder option = new IntHolder(0);
            Request request = new Request(UserSystem.USER_REQUEST_TELEPORT, new Payload(new Object[]{option}));

            menu.addButton(request, 0, 2, 1, new Text("Teleport", Color.BLUE, Color.LIGHTBLUE), () -> {
                logger.debug("button delegate Teleport");
                SystemManager.putRequest(request);
            });

            menu.addButton(null, 3, 1, 1, Texture.buildBundleTexture("data-old", "images/Dangast.jpg"));
            menu.addButton(null, 2, 1, 1, Texture.buildBundleTexture("data-old", "images/Dangast.jpg"));
            return menu;
        }),
                // distance < 0.8 leads to distorsion?
                shaderDebugger.getMenuProvider(getDefaultCamera(), 1.2)
        });

        InputToRequestSystem inputToRequestSystem = new InputToRequestSystem();
        SystemManager.addSystem(inputToRequestSystem, 0);

        GrabbingSystem grabbingSystem = GrabbingSystem.buildFromConfiguration();
        GrabbingSystem.addDefaultKeyBindings(inputToRequestSystem);
        SystemManager.addSystem(grabbingSystem);

        // 4.11.25 Added for vehicle loading via request (but needs a sphere)
        SystemManager.addSystem(new TrafficSystem());

        if (!isAR()) {
            // Plane already is y=0 so needs no rotation
            Material goundmat = Material.buildLambertMaterial(Color.GRAY);
            SceneNode ground = new SceneNode(new Mesh(ShapeGeometry.buildPlane(500, 500, 1, 1), goundmat, true, true));
            ground.getTransform().setPosition(new Vector3(0, 0, 0));
            ground.setName("Ground");
            addToWorld(ground);
        }

        if (vrInstance != null) {
            // Even in VR the observer will be attached to avatar later
            observer.attach(vrInstance.getController(0));
            observer.attach(vrInstance.getController(1));

            ControlPanel leftControllerPanel = buildVrControlPanel(buttonDelegates);
            // position and rotation of VR controlpanel is controlled by property ...
            inputToRequestSystem.addControlPanel(leftControllerPanel);
            vrInstance.attachControlPanelToController(vrInstance.getController(0), leftControllerPanel);

        } else {
            // TODO 1.2.24: add menucycler to inputToRequestSystem
            inputToRequestSystem.setControlMenuBuilder(camera -> buildControlMenuForScene(camera));
        }

        addLight();

        mainVehicleNode = new SceneNode();
        mainVehicleNode.setName("mainVehicleNode");
        addToWorld(mainVehicleNode);

        // create avatar (via login)
        SystemManager.putRequest(UserSystem.buildLoginRequest("Freds account name", ""));

        // 24.1.22: State ready to join now needed for 'login'
        SystemState.state = SystemState.STATE_READY_TO_JOIN;
    }

    protected void processArguments() {
        initialVehicle = Platform.getInstance().getConfiguration().getString("initialVehicle");
    }

    @Override
    public String[] getPreInitBundle() {
        // "fgdatabasic" in project is only a small subset. The external should have 'before' prio to load instead of subset.
        // "data" is needed for taxiway ground texture.
        // 9.8.24: "traffic-fg" for bluebird
        // Also "fgdatabasic" preloaded, because otherwise its loaded 'on demand', which only works with long loading vehicles, but not quick like bluebird.
        Platform.getInstance().addBundleResolver(new HttpBundleResolver("fgdatabasic@" + AdvancedConfiguration.BUNDLEPOOL_URL), true);
        return new String[]{"engine", "traffic-advanced", "data", "traffic-fg", "fgdatabasic", /*6.3.25 FlightGearSettings.FGROOTCOREBUNDLE*/};
    }

    @Override
    public void initSettings(Settings settings) {
        settings.vrready = true;
    }

    /**
     *
     */
    private void addLight() {
        // Light from above
        Light light = new DirectionalLight(Color.WHITE, new Vector3(0, 60, 0));
        addLightToWorld(light);
        // 4.3.25 ambient light instead of second directional
        addLightToWorld(new AmbientLight(new Color(0.3f, 0.3f, 0.3f, 1.0f)));
    }

    @Override
    public void update() {
        double tpf = getDeltaTime();

        if (Input.getKeyDown(KeyCode.F)) {
            // TODO replace with system
            if (fps == null) {
                fps = new FirstPersonController(getDefaultCamera().getCarrierTransform(), true);
                //fps.setMovementSpeed(100);
            } else {
                fps = null;
            }
        }

        EcsEntity user = UserSystem.getInitialUser();
        if (!avatarInited && user != null && TeleportComponent.getTeleportComponent(user) != null) {
            // Outside von rechtem Wing. Bewusst auf Höhe 0, um vertikale Orientierung des Aircraft zu sehen.
            // Avater ist schon fuer FG orinetiert und muss sich nur nach links drehen.
            // 18.10.19: Ist jetzt y=0 Ebene. Dann muss der sich doch gar nicht mehr drehen.
            // Und nicht mehr auf 0, sondern Avatar like etwas höher.

            double distanceToModel = 50;
            if (isAR()) {
                distanceToModel = 0.5;
            }
            TeleportComponent.getTeleportComponent(user).addPosition("Wing", new LocalTransform(new Vector3(0, 1.0, distanceToModel), Quaternion.buildRotationZ(new Degree(0))));

            //31.10.23 ObserverComponent is now added in ObserverSystem

            avatarInited = true;
        }

        if (avatarInited && !modelInited) {
            if (initialVehicle != null) {
                while (!initialVehicle.equals(vehiclelist.get(vehicleindex))) {
                    vehicleindex++;
                }
            }
            addNextVehicle();
            modelInited = true;
        }

        //1.1.20 N->L
        if (Input.getKeyDown(KeyCode.L)) {
            addNextVehicle();
        }
        if (Input.getKeyDown(KeyCode.P)) {
            logger.debug("Property tree:\n" + FGGlobals.getInstance().get_props().dump("\n"));
        }
        Point mouselocation = Input.getMouseDown();

        menuCycler.update(mouselocation);
        Observer.getInstance().update();

        msgBox.hideIfExpired();
        shaderDebugger.update();
    }

    public void addNextVehicle() {
        vehicleindex += 1;
        if (vehicleindex >= vehiclelist.size()) {
            logger.info("no more vehicles");
            return;
        }
        newModel(vehicleindex - 1);
    }

    private void newModel(int index) {

        VehicleDefinition config;

        String newVehicle = vehiclelist.get(index);
        // 4.1.24: 'bluebird' is in other config file, so currently not available
        // 8.8.24: Now bluebird is hardcoded at beginning of list
        if (newVehicle.equals("bluebird")) {
            Bundle bnd = BundleRegistry.getBundle("traffic-fg");
            TrafficConfig vehicleDefinitions = TrafficConfig.buildFromBundle(bnd, BundleResource.buildFromFullString("flight/vehicle-definitions.xml"));
            config = vehicleDefinitions.findVehicleDefinitionsByName(newVehicle).get(0);
        } else {
            Bundle bnd = BundleRegistry.getBundle("traffic-advanced");
            TrafficConfig vehicleDefinitions = TrafficConfig.buildFromBundle(bnd, BundleResource.buildFromFullString("vehicle-definitions.xml"));
            config = vehicleDefinitions.findVehicleDefinitionsByName(newVehicle).get(0);
        }
        // 18.10.19: TrafficHelper (VehicleLauncher?) will not do, because vehicle will not be on a graph. Well, we could pass null in that case.
        // 4.11.25: Give ECS request a try, but first needs TrafficSystem and a sphere
        //Request request = RequestRegistry.buildLoadVehicle(UserSystem.getInitialUser().getId(), newVehicle, null, null, null);
        //SystemManager.putRequest(request);
        new FgVehicleLoader().loadVehicle(new Vehicle(newVehicle), config, (SceneNode container, VehicleLoaderResult loaderResult, SceneNode lowresNode) -> {
            //SceneNode basenode=container;
            SceneNode basenode = VehicleLauncher.getModelNodeFromVehicleNode(container);
            SceneNode currentaircraft = container;

            LocalTransform vehiclebasetransform = /*5.12.23sceneConfig*/tw.getBaseTransformForVehicleOnGraph();
            if (vehiclebasetransform != null) {
                currentaircraft.getTransform().setPosition(vehiclebasetransform.position);
                currentaircraft.getTransform().setRotation(vehiclebasetransform.rotation);
            }

            TeleportComponent pc = TeleportComponent.getTeleportComponent(UserSystem.getInitialUser());
            Map<String, LocalTransform> viewpoints = config.getViewpoints();
            for (String key : viewpoints.keySet()) {
                pc.addPosition(key, basenode.getTransform(), new LocalTransform(viewpoints.get(key).position, viewpoints.get(key).rotation));
            }

            // position will be (0,0,0)?
            Vector3 destination = new Vector3();
            if (isAR()) {
                double scale = 0.03;
                if (StringUtils.contains(newVehicle, "777")) {
                    scale = 0.006;
                }
                currentaircraft.getTransform().setScale(new Vector3(scale, scale, scale));
                // put it 'on the desk'. Stack all together. The user might grab and relocate.
                destination = new Vector3(0, 1, 0);
            } else {
                // locate one by the other
                destination = new Vector3(0, 0, vehicleindex * -30);
                // additional front view
                pc.addPosition("front", new LocalTransform(destination.add(new Vector3(-30, 4, 0)), Quaternion.buildRotationY(new Degree(-90))));
            }
            currentaircraft.getTransform().setPosition(destination);
            currentaircraft.getTransform().setParent(mainVehicleNode.getTransform());

            // 1.2.24: Make it an entity to be grabable
            EcsEntity modelEntity = new EcsEntity(currentaircraft);
            modelEntity.addComponent(new GrabbingComponent());
            modelEntity.setName(config.getName());

            logger.debug("teleport positions:" + pc.getPointCount());
            for (int i = 0; i < pc.getPointCount(); i++) {
                logger.debug("teleport position:" + pc.getPointLabel(i));
            }
            logger.debug("vehicle entity added for " + modelEntity.getName());

            // 2.3.25 Make it a vehicle and honor animations to use speed and related animations.
            // All this is typically done in VehicleLauncher in regular traffic scenes.
            VelocityComponent vc = new VelocityComponent();
            vc.setMaximumSpeed(config.getMaximumSpeed());
            vc.setAcceleration(config.getAcceleration());
            modelEntity.addComponent(vc);
            loaderResult.applyResultsToEntity(modelEntity);

        });
    }

    private boolean isAR() {
        return vrMode != null && vrMode.equals("AR");
    }

    /**
     * A simple control panel permanently attached to the left controller. Consists of
     * <p>
     * <p>
     * top line: vr y offset spinner
     * medium: spinner for teleport toggle
     * bottom: load button
     */
    private ControlPanel buildVrControlPanel(Map<String, ButtonDelegate> buttonDelegates) {
        Color backGround = Color.LIGHTBLUE;
        Material mat = Material.buildBasicMaterial(backGround, null);

        double ControlPanelWidth = 0.6;
        double ControlPanelRowHeight = 0.1;
        double ControlPanelMargin = 0.005;

        int rows = 3;
        DimensionF rowsize = new DimensionF(ControlPanelWidth, ControlPanelRowHeight);

        ControlPanel cp = new ControlPanel(new DimensionF(ControlPanelWidth, rows * ControlPanelRowHeight), mat, 0.01);

        // top line: property control for yvroffset
        cp.add(new Vector2(0, ControlPanelHelper.calcYoffsetForRow(2, rows, ControlPanelRowHeight)), new SpinnerControlPanel(rowsize, ControlPanelMargin, mat, new NumericSpinnerHandler(0.1, new VrOffsetWrapper()), Color.BLUE));
        // mid line
        cp.add(new Vector2(0, ControlPanelHelper.calcYoffsetForRow(1, rows, ControlPanelRowHeight)), new SpinnerControlPanel(rowsize, ControlPanelMargin, mat,
                new SelectSpinnerHandler(new String[]{"FPS", "Teleport"}, value -> {
                    logger.debug("Toggle teleport");
                    //TODO
                    return null;
                }), Color.BLUE));
        // bottom line:
        cp.addArea(new Vector2(0, ControlPanelHelper.calcYoffsetForRow(0, rows, ControlPanelRowHeight)),
                new DimensionF(ControlPanelWidth / 3, ControlPanelRowHeight), () -> {
                    logger.debug("load clicked");
                    addNextVehicle();
                    // 11='L'
                }).setIcon(Icon.IconCharacter(11));
        return cp;
    }

    /**
     * Non VR Control menu.
     * <p>
     */
    public GuiGrid buildControlMenuForScene(Camera camera) {

        GuiGrid controlmenu = GuiGrid.buildForCamera(camera, 2, 6, 1, Color.BLACK_FULLTRANSPARENT, true);

        controlmenu.addButton(0, 0, 1, Icon.ICON_POSITION, () -> {
            InputToRequestSystem.sendRequestWithId(new Request(UserSystem.USER_REQUEST_TELEPORT, new Payload(new Object[]{new IntHolder(0)})));
        });
        controlmenu.addButton(1, 0, 1, Icon.IconCharacter(11), () -> {
            // load next not yet loaded vehicle.
            //not working TODO but should SystemManager.putRequest(RequestRegistry.buildLoadVehicle(UserSystem.getInitialUser().getId(), null, null));
            addNextVehicle();
        });
        // control speed not via TrafficSystem because it is no real speed. Or is it possible via TrafficSystem?
        controlmenu.addButton(2, 0, 1, Icon.ICON_HORIZONTALLINE, () -> {
            adjustSpeed(-10);
        });
        controlmenu.addButton(3, 0, 1, Icon.ICON_PLUS, () -> {
            adjustSpeed(10);
        });

        controlmenu.addButton(4, 0, 1, Icon.ICON_MENU, () -> {
            // TODO link to menucycler
            InputToRequestSystem.sendRequestWithId(new Request(InputToRequestSystem.USER_REQUEST_MENU));
        });
        controlmenu.addButton(5, 0, 1, Icon.ICON_CLOSE, () -> {
            InputToRequestSystem.sendRequestWithId(new Request(InputToRequestSystem.USER_REQUEST_CONTROLMENU));
        });
        return controlmenu;
    }

    /**
     * A 'pseudo speed'.
     */
    private void adjustSpeed(int inc) {
        for (EcsEntity entity : SystemManager.findEntities(e ->
                FgAnimationComponent.getFgAnimationComponent(e) != null &&
                        VelocityComponent.getVelocityComponent(e) != null
        )) {
            logger.debug("adjustSpeed for vehicle " + entity.getName());
            VelocityComponent.getVelocityComponent(entity).incMovementSpeed(inc);
        }
    }
}

class CockpitMenuBuilder implements MenuProvider {
    HangarScene sc;

    CockpitMenuBuilder(HangarScene sc) {
        this.sc = sc;
    }

    @Override
    public Menu buildMenu(Camera camera) {
        return new CockpitCduMenu(camera, sc.logger);
    }

    @Override
    public Transform getAttachNode() {
        return Observer.getInstance().getTransform();
    }

    @Override
    public Ray getRayForUserClick(Point mouselocation) {
        if (mouselocation != null) {
            return sc.getDefaultCamera().buildPickingRay(sc.getDefaultCamera().getCarrierTransform(), mouselocation);
        }
        Ray ray = VrInstance.getInstance().getController(1).getRay();
        return ray;
    }

    @Override
    public void menuBuilt() {
    }
}

/**
 * Attach to deferred Cam? And in VR?
 * Try default camera. VR probably needs other solution.
 */
class CockpitCduMenu implements Menu {
    Log logger;
    SceneNode cduCarrier;
    EcsEntity entity;

    CockpitCduMenu(Camera camera, Log logger) {
        this.logger = logger;

        cduCarrier = new SceneNode();

        AbstractSceneRunner.instance.loadBundle("fgdatabasic", (Bundle bundle) -> {

            SGLoaderOptions opt = new SGLoaderOptions();
            //opt.setPropertyNode(destinationProp);
            //lieber cdu2, dann kann das ganze Display eine einzige Textur (Canvas?) sein, statt einzelne Buchstaben.
            entity = de.yard.threed.flightgear.traffic.ModelFactory.buildModelFromBundleXmlAsEntity(new BundleResource(bundle, "Aircraft/Instruments-3d/cdu2/boeing.xml"), null);
            entity.setName("CDU-Menu");
            FgAnimationComponent.getFgAnimationComponent(entity).setCameraProvider(() -> {
                return camera;
            });
            //BuildResult buildresult = SGReaderWriterXML.buildModelFromBundleXML(new BundleResource(bundle, "Aircraft/Instruments-3d/cdu/boeing.xml"), opt, (bpath, alist) -> {                 });
            cduCarrier.attach(entity.scenenode);
        });
        //die Werte sind bezogen auf die Camera. Lieber weiter weg und scalen wegen VR
        //cduCarrier.getTransform().setPosition(new Vector3(0, 0, -0.79f));
        cduCarrier.getTransform().setPosition(new Vector3(0, 0, -5f));
        double scale = 10;
        cduCarrier.getTransform().setScale(new Vector3(scale, scale, scale));
        cduCarrier.getTransform().setRotation(Quaternion.buildFromAngles(new Degree(-90), new Degree(-90), new Degree(0)));
    }

    @Override
    public SceneNode getNode() {
        return cduCarrier;
    }

    @Override
    public boolean checkForClickedArea(Ray ray) {
        // uses PickAnimation?. 6.11.19: Also in VR?
        return false;
    }

    @Override
    public void checkForSelectionByKey(int position) {
    }

    @Override
    public void remove() {
        //TODO check/test it is gone
        SystemManager.removeEntity(entity);
        SceneNode.removeSceneNode(cduCarrier);
        entity = null;
        cduCarrier = null;
    }
}
