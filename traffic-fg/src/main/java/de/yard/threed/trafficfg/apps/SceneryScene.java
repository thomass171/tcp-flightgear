package de.yard.threed.trafficfg.apps;


import de.yard.threed.core.Color;
import de.yard.threed.core.Degree;
import de.yard.threed.core.Dimension;
import de.yard.threed.core.Event;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Payload;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.engine.BaseEventRegistry;
import de.yard.threed.engine.BaseRequestRegistry;
import de.yard.threed.engine.DirectionalLight;
import de.yard.threed.engine.Input;
import de.yard.threed.engine.KeyCode;
import de.yard.threed.engine.Light;
import de.yard.threed.engine.Observer;
import de.yard.threed.engine.ObserverSystem;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneMode;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.apps.ModelSamples;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.FirstPersonMovingComponent;
import de.yard.threed.engine.ecs.FirstPersonMovingSystem;
import de.yard.threed.engine.ecs.InputToRequestSystem;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.ModelLoader;
import de.yard.threed.engine.platform.common.Settings;
import de.yard.threed.engine.platform.common.SimpleBundleResourceProvider;
import de.yard.threed.flightgear.FgTerrainBuilder;
import de.yard.threed.flightgear.FlightGearMain;
import de.yard.threed.flightgear.FlightGearSettings;
import de.yard.threed.flightgear.TerraSyncBundleResolver;
import de.yard.threed.flightgear.TerrainElevationProvider;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.simgear.scene.model.ACProcessPolicy;
import de.yard.threed.flightgear.core.simgear.scene.model.OpenGlProcessPolicy;
import de.yard.threed.traffic.AbstractTerrainBuilder;
import de.yard.threed.traffic.EllipsoidCalculations;
import de.yard.threed.traffic.EllipsoidConversionsProvider;
import de.yard.threed.traffic.ScenerySystem;
import de.yard.threed.traffic.WorldGlobal;
import de.yard.threed.traffic.flight.FlightLocation;
import de.yard.threed.trafficfg.FgCalculations;


import static de.yard.threed.engine.ecs.TeleporterSystem.EVENT_POSITIONCHANGED;

/**
 * A scene for first person moving through a scenery/terrain. No traffic.
 * Derived from TravelScene.
 * No login/avatar (so no user or AvatarSystem), just an entity with simple node that is FPS moved.
 * <p>
 * Views:
 * 1) Blick über Elsdorf auf Ufo/Pilot und Landschaft
 * 2) Blick aus Ufo (attached)
 * 3) ...
 * <p>
 * The origin of this coordinate system is the center of the earth. The X axis runs along a line from the center of the earth
 * out to the equator at the zero meridian. The Z axis runs along a line between the north and south poles with north being positive.
 * The Y axis isType parallel to a line running through the center of the earth out through the equator somewhere in the Indian Ocean.
 * <p>
 * Keys:
 * v cyclen der verschiedenen Views. Pilot bleibt nicht unbedingt an seinem Model, aber Model an seinem Platz.
 * p cyclen position des Models. Das aktuelle Model bewegt sich mitsamt Pilot an andere Position. (11.1.17: Navigator setzt ausser Start auch POI List)
 * t teleport zweistufig (mit CTRL). Nutzt auch die POI Liste.
 * <p>
 */
public class SceneryScene extends Scene {
    static Log logger = Platform.getInstance().getLog(SceneryScene.class);
    Light light;
    private static final int WIDTH = 1024;
    private static final int HEIGHT = 768;

    // freecam ist mal für Tests
    //22.10.21 boolean freecam = false;
    // erste Position. 6/17: 3=equator,4=Dahlem ,5=EDDK
    int startpos = 5;
    // 17.1.18: Den StgCycler lass ich mal fuer ScneryViewer
    private boolean usestgcycler = false;
    //private StgCycler stgcycler;
    private EcsEntity userEntity = null;
    EllipsoidCalculations ellipsoidCalculations;

    @Override
    public String[] getPreInitBundle() {

        //TerraSync-model cannot be loaded in preinit because of required custom resolver?
        return new String[]{"engine", FlightGear.getBucketBundleName("model"), /*2.10.23 "data-old", "data", "fgdatabasic", "fgdatabasicmodel",FlightGear.getBucketBundleName("model"),FlightGearSettings.FGROOTCOREBUNDLE*/ "sgmaterial"
                /*BundleRegistry.FGHOMECOREBUNDLE,*/};
    }

    /**
     * @param sceneMode
     */
    @Override
    public void init(SceneMode sceneMode) {
        logger.debug("init SceneryScene");

        SceneNode world = new SceneNode();
        world.setName("Scenery World");

        // Observer can exist before login/join for showing eg. an overview.
        // After login/join it might be attched to an avatar.
        Observer observer = Observer.buildForDefaultCamera();

        FlightGearMain.initFG(new FlightLocation(WorldGlobal.equator020000, new Degree(0), new Degree(0)), null);
        BundleRegistry.addProvider(new SimpleBundleResourceProvider("fgdatabasicmodel"));
        //4.1.18:TerraSync-model. Ob das hier gut ist?
        BundleRegistry.addProvider(new SimpleBundleResourceProvider(FlightGear.getBucketBundleName("model")));
        // FG, Position ist initialisiert. 

        // A elevation provider is needed for calculating 3D coordinates from geo coordinates. To keep is simple, use a fix one for now.
        TerrainElevationProvider tep = TerrainElevationProvider.buildForStaticAltitude(80);
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
        AbstractTerrainBuilder terrainBuilder = new FgTerrainBuilder();
        terrainBuilder.init(world);
        ts.setTerrainBuilder(terrainBuilder);
        SystemManager.addSystem(ts);

        InputToRequestSystem inputToRequestSystem = new InputToRequestSystem();
        // use continuous movement
        inputToRequestSystem.addKeyMapping(KeyCode.W, BaseRequestRegistry.TRIGGER_REQUEST_START_FORWARD);
        inputToRequestSystem.addKeyReleaseMapping(KeyCode.W, BaseRequestRegistry.TRIGGER_REQUEST_STOP_FORWARD);

        inputToRequestSystem.addKeyMapping(KeyCode.S, BaseRequestRegistry.TRIGGER_REQUEST_START_BACK);
        inputToRequestSystem.addKeyReleaseMapping(KeyCode.S, BaseRequestRegistry.TRIGGER_REQUEST_STOP_BACK);

        inputToRequestSystem.addKeyMapping(KeyCode.LeftArrow, BaseRequestRegistry.TRIGGER_REQUEST_START_TURNLEFT);
        inputToRequestSystem.addKeyReleaseMapping(KeyCode.LeftArrow, BaseRequestRegistry.TRIGGER_REQUEST_STOP_TURNLEFT);
        inputToRequestSystem.addKeyMapping(KeyCode.RightArrow, BaseRequestRegistry.TRIGGER_REQUEST_START_TURNRIGHT);
        inputToRequestSystem.addKeyReleaseMapping(KeyCode.RightArrow, BaseRequestRegistry.TRIGGER_REQUEST_STOP_TURNRIGHT);

        inputToRequestSystem.addKeyMapping(KeyCode.UpArrow, BaseRequestRegistry.TRIGGER_REQUEST_START_TURNUP);
        inputToRequestSystem.addKeyReleaseMapping(KeyCode.UpArrow, BaseRequestRegistry.TRIGGER_REQUEST_STOP_TURNUP);
        inputToRequestSystem.addKeyMapping(KeyCode.DownArrow, BaseRequestRegistry.TRIGGER_REQUEST_START_TURNDOWN);
        inputToRequestSystem.addKeyReleaseMapping(KeyCode.DownArrow, BaseRequestRegistry.TRIGGER_REQUEST_STOP_TURNDOWN);

        inputToRequestSystem.addKeyMapping(KeyCode.R, BaseRequestRegistry.TRIGGER_REQUEST_START_ROLLLEFT);
        inputToRequestSystem.addKeyReleaseMapping(KeyCode.R, BaseRequestRegistry.TRIGGER_REQUEST_STOP_ROLLLEFT);
        inputToRequestSystem.addShiftKeyMapping(KeyCode.R, BaseRequestRegistry.TRIGGER_REQUEST_START_ROLLRIGHT);
        inputToRequestSystem.addShiftKeyReleaseMapping(KeyCode.R, BaseRequestRegistry.TRIGGER_REQUEST_STOP_ROLLRIGHT);
        SystemManager.addSystem(inputToRequestSystem);

        FirstPersonMovingSystem firstPersonMovingSystem = FirstPersonMovingSystem.buildFromConfiguration();
        firstPersonMovingSystem.firstpersonmovingsystemdebuglog = true;
        SystemManager.addSystem(firstPersonMovingSystem);

        SystemManager.addSystem(new ObserverSystem());

        addLight();

        ModelLoader.processPolicy = new ACProcessPolicy(null);
    }

    @Override
    public void initSettings(Settings settings) {
        settings.aasamples = 4;
        settings.vrready = true;
        settings.minfilter = EngineHelper.TRILINEAR;
    }

    @Override
    public Dimension getPreferredDimension() {
    /*    if (Platform.getInstance().isDevmode()) {
            return new Dimension(WIDTH, HEIGHT);
        }*/
        return null;
    }


    public void addLight() {

        DirectionalLight light = new DirectionalLight(Color.WHITE, new Vector3(0, 30000000, 20000000));

        addLightToWorld(light);
        light = new DirectionalLight(Color.WHITE, new Vector3(0, -30000000, -20000000));

        addLightToWorld(light);

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

        if (userEntity == null) {
            // just an invisible node
            SceneNode node = new SceneNode();

            FirstPersonMovingComponent fpmc = new FirstPersonMovingComponent(node.getTransform());
            fpmc.getFirstPersonTransformer().setMovementSpeed(150);
            userEntity = new EcsEntity(node, fpmc);
            userEntity.setName("pilot");

            LocalTransform loc = WorldGlobal.eddkoverview.location.toPosRot(ellipsoidCalculations);
            node.getTransform().setPosition(loc.position);
            // Base rotation to make user node a FG model ...
            Quaternion rotation = Quaternion.buildFromAngles(new Degree(-90), new Degree(180), new Degree(90));
            rotation = new OpenGlProcessPolicy(null).opengl2fg.extractQuaternion();
            // .. that is suited for FG geo transformation. Unclear why it is this order of rotation multiply.
            rotation = loc.rotation.multiply(rotation);
            node.getTransform().setRotation(rotation);
            //node.getTransform().setRotation(loc.rotation.multiply(processPolicy.ac2ogl.getInverse().extractQuaternion()));

            // Let ObserverSystem attach observer
            SystemManager.sendEvent(BaseEventRegistry.buildUserAssembledEvent(userEntity));

            // trigger terrain loading
            SystemManager.sendEvent(new Event(EVENT_POSITIONCHANGED, new Payload(new Object[]{loc})));
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
        return world;
    }


}


