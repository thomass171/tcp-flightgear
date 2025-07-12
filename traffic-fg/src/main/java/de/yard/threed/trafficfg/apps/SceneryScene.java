package de.yard.threed.trafficfg.apps;


import de.yard.threed.core.Color;
import de.yard.threed.core.Degree;
import de.yard.threed.core.Dimension;
import de.yard.threed.core.DimensionF;
import de.yard.threed.core.Event;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Payload;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.AmbientLight;
import de.yard.threed.engine.BaseEventRegistry;
import de.yard.threed.engine.DirectionalLight;
import de.yard.threed.engine.Input;
import de.yard.threed.engine.KeyCode;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Observer;
import de.yard.threed.engine.ObserverSystem;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneMode;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.Transform;
import de.yard.threed.engine.apps.ModelSamples;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.FirstPersonMovingComponent;
import de.yard.threed.engine.ecs.FirstPersonMovingSystem;
import de.yard.threed.engine.ecs.InputToRequestSystem;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.TeleporterSystem;
import de.yard.threed.engine.gui.ControlPanel;
import de.yard.threed.engine.gui.ControlPanelHelper;
import de.yard.threed.engine.gui.NumericSpinnerHandler;
import de.yard.threed.engine.gui.SpinnerControlPanel;
import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.ModelLoader;
import de.yard.threed.engine.platform.common.Settings;
import de.yard.threed.engine.vr.VrInstance;
import de.yard.threed.engine.vr.VrOffsetWrapper;
import de.yard.threed.flightgear.FgVehicleLoaderResult;
import de.yard.threed.flightgear.ecs.FgAnimationComponent;
import de.yard.threed.traffic.TerrainElevationProvider;
import de.yard.threed.traffic.TrafficHelper;
import de.yard.threed.trafficfg.fgadapter.FgTerrainBuilder;
import de.yard.threed.flightgear.FgVehicleLoader;
import de.yard.threed.flightgear.FlightGearMain;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.simgear.scene.model.ACProcessPolicy;
import de.yard.threed.flightgear.core.simgear.scene.model.OpenGlProcessPolicy;
import de.yard.threed.flightgear.ecs.FgAnimationUpdateSystem;
import de.yard.threed.traffic.AbstractSceneryBuilder;
import de.yard.threed.trafficcore.EllipsoidCalculations;
import de.yard.threed.traffic.EllipsoidConversionsProvider;
import de.yard.threed.traffic.ScenerySystem;
import de.yard.threed.traffic.TrafficConfig;
import de.yard.threed.traffic.VehicleLauncher;
import de.yard.threed.traffic.VehicleLoaderResult;
import de.yard.threed.traffic.WorldGlobal;
import de.yard.threed.traffic.config.VehicleDefinition;
import de.yard.threed.traffic.flight.FlightLocation;
import de.yard.threed.trafficcore.model.Vehicle;
import de.yard.threed.trafficfg.FgCalculations;
import de.yard.threed.trafficfg.fgadapter.FlightGearSystem;


import java.util.List;
import java.util.Map;

import static de.yard.threed.engine.ecs.TeleporterSystem.EVENT_POSITIONCHANGED;

/**
 * A scene for first person moving through a scenery/terrain. No traffic, no graph, no ground services.
 * Derived from TravelScene.
 * No login/avatar (so no user or AvatarSystem, user is manually registered as 'acting player'), just an entity with simple node that is FPS moved.
 * Also no teleport and no TrafficSystem.
 * <p>
 * <p>
 * The origin of this coordinate system is the center of the earth. The X axis runs along a line from the center of the earth
 * out to the equator at the zero meridian. The Z axis runs along a line between the north and south poles with north being positive.
 * The Y axis isType parallel to a line running through the center of the earth out through the equator somewhere in the Indian Ocean.
 * <p>
 * Keys:
 * w/a/s/d, cursor
 * <p>
 * 11.3.24: Animation added (windturbine and tower)
 */
public class SceneryScene extends Scene {
    static Log logger = Platform.getInstance().getLog(SceneryScene.class);

    private boolean waitsForInitialVehicle = false;
    private EcsEntity userEntity = null;
    EllipsoidCalculations ellipsoidCalculations;
    private EcsEntity vehicleEntity = null;
    private LocalTransform captainPosition;
    private SceneNode vehicleModelnode = null;
    VrInstance vrInstance;

    @Override
    public String[] getPreInitBundle() {

        //'TerraSync-model' is loaded in preinit to avoid async issues if done later. Needs required custom resolver be available in plaform setup
        // 21.7.24: TerraSync-model isn't used anyway currently due to flag 'ignoreshared'. So save the time and memory for loading it.
        // "fgdatabasic" and "traffic-fg" are needed for bluebird
        // 'TerraySync' is loaded at runtime by TerraSyncBundleResolver' that is set up by the platform(using HOSTDIRFG on desktop and "bundles" in webgl)
        return new String[]{"engine", FlightGear.getBucketBundleName("model")+"-delayed", "sgmaterial", "fgdatabasic", "traffic-fg"};
    }

    /**
     * @param sceneMode
     */
    @Override
    public void init(SceneMode sceneMode) {
        logger.debug("init SceneryScene");

        SceneNode world = new SceneNode();
        world.setName("Scenery World");

        String initialVehicle = Platform.getInstance().getConfiguration().getString("initialVehicle");

        // Observer can exist before login/join for showing eg. an overview.
        // After login/join it might be attched to an avatar.
        Observer observer = Observer.buildForDefaultCamera();

        vrInstance = VrInstance.buildFromArguments();

        FlightGearMain.initFG(new FlightLocation(WorldGlobal.equator020000, new Degree(0), new Degree(0)), null);
        // BundleResourceProvider are FG specific (not mix up with BundleResolver) and currently not yet needed(?)
        // 29.2.24 not used FgBundleHelper.addProvider(new SimpleBundleResourceProvider("fgdatabasicmodel"));
        //4.1.18:TerraSync-model. Ob das hier gut ist?
        // 29.2.24 not used  FgBundleHelper.addProvider(new SimpleBundleResourceProvider(FlightGear.getBucketBundleName("model")));
        // FG, Position is initialisiert.

        // A elevation provider is needed for calculating 3D coordinates from geo coordinates.
        TerrainElevationProvider tep = new TerrainElevationProvider(null);
        SystemManager.putDataProvider(SystemManager.DATAPROVIDERELEVATION, tep);


        // just a cube near EDDK. Is visible from the default EDDK overview position.
        SceneNode cube = ModelSamples.buildCube(1000, new Color(0xCC, 00, 00));
        if (cube != null) {
            cube.getTransform().setPosition(new Vector3(3987743.8f, 480804.66f, 4937917.5f));
            //cube.setPosition(new Vector3(0, 0, 0));
            world.attach(cube);
        }
        addToWorld(world);


        ellipsoidCalculations = new FgCalculations();
        // EllipsoidConversionsProvider needs manual registration without SphereSystem
        SystemManager.putDataProvider("ellipsoidconversionprovider", new EllipsoidConversionsProvider(ellipsoidCalculations));

        ScenerySystem ts = new ScenerySystem(world);
        AbstractSceneryBuilder terrainBuilder = new FgTerrainBuilder();
        terrainBuilder.init(world);
        ts.setTerrainBuilder(terrainBuilder);
        SystemManager.addSystem(ts);

        InputToRequestSystem inputToRequestSystem = new InputToRequestSystem();

        FirstPersonMovingSystem.addDefaultKeyBindingsforContinuousMovement(inputToRequestSystem);
        SystemManager.addSystem(inputToRequestSystem);

        FirstPersonMovingSystem firstPersonMovingSystem = FirstPersonMovingSystem.buildFromConfiguration();
        firstPersonMovingSystem.firstpersonmovingsystemdebuglog = true;
        SystemManager.addSystem(firstPersonMovingSystem);

        SystemManager.addSystem(new ObserverSystem());
        SystemManager.addSystem(new FgAnimationUpdateSystem());
        SystemManager.addSystem(new FlightGearSystem());

        if (vrInstance != null) {
            // Even in VR the observer will be attached to avatar later. Controller only with vehicle. Otherwise there is nothing to control.
            if (initialVehicle != null) {

                observer.attach(vrInstance.getController(0));
                observer.attach(vrInstance.getController(1));

                ControlPanel leftControllerPanel = buildVrControlPanel();
                // position and rotation of VR controlpanel is controlled by property ...
                inputToRequestSystem.addControlPanel(leftControllerPanel);
                vrInstance.attachControlPanelToController(vrInstance.getController(0), leftControllerPanel);
            }
        } else {
        }

        addLight();

        ModelLoader.processPolicy = new ACProcessPolicy(null);

        if (initialVehicle != null) {
            waitsForInitialVehicle = true;
            TrafficConfig tc = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("traffic-fg"), new BundleResource("flight/vehicle-definitions.xml"));
            List<VehicleDefinition> vehicleDefinitions = tc.findVehicleDefinitionsByName(initialVehicle);
            if (vehicleDefinitions.size() == 0) {
                logger.warn("vehicle not found: " + initialVehicle);
            } else {
                loadInitialVehicle(vehicleDefinitions.get(0));
            }
        }
    }

    @Override
    public void initSettings(Settings settings) {
        settings.aasamples = 4;
        settings.vrready = true;
        settings.minfilter = EngineHelper.TRILINEAR;
    }

    @Override
    public Dimension getPreferredDimension() {
        return new Dimension(1280, 900);
    }

    public void addLight() {
        // See EDDK-sphere.xml for reference.
        DirectionalLight light = new DirectionalLight(Color.WHITE, new Vector3(0, 30000000, 20000000));

        addLightToWorld(light);

        addLightToWorld(new AmbientLight(new Color(0.3f, 0.3f, 0.3f, 1.0f)));
    }

    public void add(SceneNode model, double x, double y, double z, double scale, Quaternion rotation) {
        addToWorld(model);
        model.getTransform().setPosition(new Vector3(x, y, z));
        model.getTransform().setScale(new Vector3(scale, scale, scale));
        if (rotation != null) {
            model.getTransform().setRotation(rotation);
        }
    }

    @Override
    public void update() {
        //double currentdelta = getDeltaTime();
        //commonUpdate();

        if (userEntity == null && !waitsForInitialVehicle) {
            // one time runtime initialization

            // just an invisible node
            SceneNode userNode = new SceneNode();
            userEntity = new EcsEntity(userNode);
            userEntity.setName("pilot");

            Transform transform;
            Quaternion rotation;
            if (vehicleEntity == null) {
                transform = userNode.getTransform();
            } else {
                userNode.getTransform().setParent(vehicleModelnode.getTransform());
                userNode.getTransform().setPosition(captainPosition.position);
                userNode.getTransform().setRotation(captainPosition.rotation);
                // decouple for FirstPersonTransformer
                transform = new SceneNode(vehicleEntity.getSceneNode()).getTransform();
            }
            FirstPersonMovingComponent fpmc = new FirstPersonMovingComponent(transform);
            fpmc.getFirstPersonTransformer().setMovementSpeed(150);
            userEntity.addComponent(fpmc);

            FlightLocation initialFlightLocation = TrafficHelper.getInitialFlightLocation();
            if (initialFlightLocation == null) {
                initialFlightLocation = WorldGlobal.eddkoverview.location;
            }
            LocalTransform loc = initialFlightLocation.toPosRot(ellipsoidCalculations);
            transform.setPosition(loc.position);
            // Base rotation to make user node a FG model ...
            rotation = new OpenGlProcessPolicy(null).opengl2fg.extractQuaternion();
            // .. that is suited for FG geo transformation and FirstPersonTransformers OpenGL coordinate system.
            // Unclear why it is this order of rotation multiply.
            rotation = loc.rotation.multiply(rotation);
            transform.setRotation(rotation);

            // Let ObserverSystem attach observer
            SystemManager.sendEvent(BaseEventRegistry.buildUserAssembledEvent(userEntity));

            // trigger terrain loading
            SystemManager.sendEvent(TeleporterSystem.buildPositionChanged(loc.position));
            //the SphereSystem way SystemManager.sendEvent(TrafficEventRegistry.buildLOCATIONCHANGED(initialPosition, null,                    (initialTile)));

            // No LoginSystem is used, so manually register user as 'acting player'.
            ((InputToRequestSystem) SystemManager.findSystem(InputToRequestSystem.TAG)).setUserEntityId(userEntity.getId());

        }

        // Platzrunde (und wakeup) erst anlegen, wenn das Terrain da ist und worldadjustment durch ist. Schwierig zu erkennen
        boolean terrainavailable = false;
        if (AbstractSceneRunner.getInstance().getFrameCount() > 50) {
            terrainavailable = true;
        }


        if (Input.getKeyDown(KeyCode.D)) {
            logger.debug("scenegraph:" + dumpSceneGraph());
        }

        Observer.getInstance().update();
    }


    protected void initSpheres() {
        logger.debug("initSpheres");
        //SolarSystem solarSystem = new SolarSystem();
        //SceneNode sun = solarSystem.build(2 * WorldGlobal.DISTANCEMOONEARTH, WorldGlobal.DISTANCEMOONEARTH/*km(18000)*/);
        //addToWorld(sun);
    }


    protected void runTests() {
        logger.info("Running tests");

        logger.info("Tests completed");
    }

    protected SceneNode getDestinationNode() {
        return getWorld();
    }

    private void loadInitialVehicle(VehicleDefinition vehicleDefinition) {
        logger.info("Loading initial vehicle " + vehicleDefinition.getName());

        new FgVehicleLoader().loadVehicle(new Vehicle(vehicleDefinition.getName()), vehicleDefinition, (SceneNode container, VehicleLoaderResult loaderResult, SceneNode lowresNode) -> {

            FgVehicleLoaderResult fgVehicleLoaderResult = (FgVehicleLoaderResult) loaderResult;
            vehicleModelnode = VehicleLauncher.getModelNodeFromVehicleNode(container);
            SceneNode currentaircraft = container;

            // world position is set later
            // rotate from FG to FirstPersonTransformers coordinate system
            currentaircraft.getTransform().setRotation(new OpenGlProcessPolicy(null).fg2opengl().extractQuaternion());

            Map<String, LocalTransform> viewpoints = vehicleDefinition.getViewpoints();
            for (String key : viewpoints.keySet()) {
                if (key.equals("Captain")) {
                    captainPosition = new LocalTransform(viewpoints.get(key).position, viewpoints.get(key).rotation);
                }
            }

            addToWorld(currentaircraft);

            // Make it an entity to be ready for FirstPersonMoving
            vehicleEntity = new EcsEntity(currentaircraft);
            vehicleEntity.setName(vehicleDefinition.getName());
            vehicleEntity.addComponent(new FirstPersonMovingComponent(currentaircraft.getTransform()));
            vehicleEntity.addComponent(new FgAnimationComponent(currentaircraft, fgVehicleLoaderResult.animationList, null));

            waitsForInitialVehicle = false;
        });
    }

    /**
     * A simple control panel permanently attached to the left controller. Consists of
     * <p>
     * top line: vr y offset spinner
     * medium:
     * bottom:
     */
    private ControlPanel buildVrControlPanel() {
        Color backGround = Color.LIGHTBLUE;
        Material mat = Material.buildBasicMaterial(backGround, null);

        double ControlPanelWidth = 0.6;
        double ControlPanelRowHeight = 0.1;
        double ControlPanelMargin = 0.005;

        int rows = 3;
        DimensionF rowsize = new DimensionF(ControlPanelWidth, ControlPanelRowHeight);

        ControlPanel cp = new ControlPanel(new DimensionF(ControlPanelWidth, rows * ControlPanelRowHeight), mat, 0.01);

        // top line: property control for yvroffset
        cp.add(new Vector2(0, ControlPanelHelper.calcYoffsetForRow(2, rows, ControlPanelRowHeight)),
                new SpinnerControlPanel(rowsize, ControlPanelMargin, mat, new NumericSpinnerHandler(0.1, new VrOffsetWrapper()), Color.BLUE));
        // mid line
        // ...
        // bottom line:
        // ...
        return cp;
    }

}


