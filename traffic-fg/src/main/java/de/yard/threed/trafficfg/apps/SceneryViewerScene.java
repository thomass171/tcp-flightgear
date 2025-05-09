package de.yard.threed.trafficfg.apps;

import de.yard.threed.core.Color;
import de.yard.threed.core.Degree;
import de.yard.threed.core.Dimension;
import de.yard.threed.core.Event;
import de.yard.threed.core.GeneralParameterHandler;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Point;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.SmartArrayList;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.geometry.Face3;
import de.yard.threed.core.geometry.FaceList;
import de.yard.threed.core.geometry.GeometryHelper;
import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeCollision;
import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.testutil.Assert;
import de.yard.threed.core.testutil.RuntimeTestUtil;
import de.yard.threed.engine.Camera;
import de.yard.threed.engine.DirectionalLight;
import de.yard.threed.engine.FirstPersonController;
import de.yard.threed.engine.GenericGeometry;
import de.yard.threed.engine.Input;
import de.yard.threed.engine.KeyCode;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Mesh;
import de.yard.threed.engine.Ray;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneMode;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.StepController;
import de.yard.threed.engine.Texture;
import de.yard.threed.engine.ViewpointList;
import de.yard.threed.engine.geometry.ShapeGeometry;
import de.yard.threed.engine.gui.Hud;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.ModelLoader;
import de.yard.threed.engine.platform.common.Settings;
import de.yard.threed.flightgear.FlightGearMain;
import de.yard.threed.flightgear.FlightGearSettings;
import de.yard.threed.flightgear.LoaderOptions;
import de.yard.threed.flightgear.TerraSyncBundleResolver;
import de.yard.threed.flightgear.TerrainHelper;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.FlightGearModuleScenery;
import de.yard.threed.flightgear.core.flightgear.scenery.FGTileMgr;
import de.yard.threed.flightgear.core.flightgear.scenery.SceneryPager;
import de.yard.threed.flightgear.core.osg.Group;
import de.yard.threed.flightgear.core.osg.Node;
import de.yard.threed.flightgear.core.osgdb.ReadResult;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.bucket.SGBucket;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeodesy;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterialLib;
import de.yard.threed.flightgear.core.simgear.scene.model.ACProcessPolicy;
import de.yard.threed.flightgear.core.simgear.scene.model.SGAnimation;
import de.yard.threed.flightgear.core.simgear.scene.model.SGReaderWriterXML;
import de.yard.threed.flightgear.core.simgear.scene.model.XmlModelCompleteDelegate;
import de.yard.threed.flightgear.core.simgear.scene.tgdb.Obj;
import de.yard.threed.flightgear.core.simgear.scene.tgdb.ReaderWriterSTG;
import de.yard.threed.flightgear.core.simgear.scene.util.SGReaderWriterOptions;
import de.yard.threed.traffic.WorldGlobal;
import de.yard.threed.traffic.flight.FlightLocation;
import de.yard.threed.trafficfg.FgCalculations;
import de.yard.threed.trafficfg.StgCycler;

import java.util.ArrayList;
import java.util.List;

/**
 * Viewer for FG 3D scenery (STG,BTG) tiles optional with objects from STG.
 * Doesn't use ECS like TerrainSystem which adds much more complexity (also by FG).
 * <p>
 * Keys:
 * UP/DOWN change position (small steps)
 * (Shift)Cursor keys move from STG to STG.
 * PGUP/DOWN change viewpoint altitude.
 * n cycle through objects (stgcycler)
 * t cycle through tiles (stgcycler,no longer active), step in stepcontroller
 * v run tests
 * f toggle FPC
 * c Nordpolcheck
 * <p>
 * 8.6.17: No Poilist here, but view points via StepController.
 */
public class SceneryViewerScene extends Scene {
    private static Log logger = Platform.getInstance().getLog(SceneryViewerScene.class);
    private static final int WIDTH = 1024;
    private static final int HEIGHT = 768;
    double y = 2;
    public StepController controller;
    private SceneNode earth;
    private Vector3 initialcampos = SGGeodesy.SGGeodToCart(SGGeod.fromGeoCoordinate(WorldGlobal.elsdorf2000.location.coordinates))/*.subtract(new Vector3(-50, -50, -50)*/;
    private Vector3 initialcamlookat = SGGeodesy.SGGeodToCart(SGGeod.fromGeoCoordinate(WorldGlobal.elsdorf0));
    private Vector3 ueberaquator = new Vector3(WorldGlobal.EARTHRADIUS + WorldGlobal.km(200), 0, 0);
    private FirstPersonController fps;
    private SceneNode world;
    private Hud hud;
    // 0 = nur refbtg
    // 1=per stg liste
    // 2=Greenwich per FgTileMgr
    // 99=default, whats really intended. Beginnend auf reftbtg (oder anderem start STG) mit Navigation. Other modes are only for testing.
    // STG wird immer erst bei Navigation dorthin
    // geladen, damit man Grenzen besser erkenn kann.
    int mode = 99;
    // 0 = no (wireframe), 1= Colorpalette, 2=FG material
    int materialmode = 2;
    SceneNode centercube;
    SGBucket currentbucket = null;
    SGReaderWriterOptions options;
    LoaderOptions boptions;
    // als Default so, dass man ein BTG komplett sehen kann
    int elevation = 26000;
    // Default start position is RefBtg (ist 3056410)
    int tileindex = 0;
    SGGeod[] tiles =
            new SGGeod[]{SGGeod.fromCart(FlightGear.refbtgcenter),
                    //EDDK
                    new SGGeod(new Degree(7.142744f), new Degree(50.865917f), 150),
            };

    int startpos = 0;
    StgCycler stgcycler;

    @Override
    public String[] getPreInitBundle() {

        //'TerraSync-model' is not used currently
        // "fgdatabasic" and "traffic-fg" are needed for bluebird
        // 'TerraySync' is loaded at runtime by TerraSyncBundleResolver' that is set up by the platform(using HOSTDIRFG on desktop and "bundles" in webgl)
        return new String[]{"engine", "sgmaterial", "fgdatabasic", "traffic-fg"};
    }

    @Override
    public void init(SceneMode forServer) {
        logger.debug("init SceneryViewer");

        // Kruecke zur Entkopplung des Modelload von AC policy.
        ModelLoader.processPolicy = new ACProcessPolicy(null);

        // 4.1.17: in EDDK start
        tileindex = 1;

        // simplematerial weils schneller ist. 10.8.16: nicht mehr relevant
        //FlightGear.simplematerial = Material.buildLambertMaterial(new Texture(new BundleResource("images/Dangast.jpg")));
        String fghome = Platform.getInstance().getConfiguration().getString("FG_HOME");
        if (materialmode == 2) {
            FlightGearMain.initFG(null, null);
            FlightGearModuleScenery.init(false, false);
            // das ist natuerlich irgendwie nicht sehr elegant, einfach ein Event dafuer zu nutzen. Aber naja.
            // 26.2.24: Try without notification Platform.getInstance().getEventBus().publish(new Event(FlightGearModuleScenery.EVENT_MATLIBCREATED, null));
        } else {
            // Die ModelRegistry muss aber eingerichtet werden.
            //30.9.19 FlightGear.setupRegistry();
        }

        world = new SceneNode();
        world.setName("SceneryViewers World");
        Camera camera;
        camera = getDefaultCamera();
        camera.getCarrier().getTransform().setPosition(initialcampos);
        camera.lookAt(initialcamlookat);

        hud = Hud.buildForCameraAndAttach(getDefaultCamera(), 0);

        hud.setText(0, "major: " + 33);

        options = new SGReaderWriterOptions();
        //wegen Performance material evtl. nicht setzen. TODO colorpalette
        if (materialmode == 2) {
            options.setMaterialLib(FlightGearModuleScenery.getInstance().get_matlib());
        }
        options.setPropertyNode(new SGPropertyNode());
        options.getDatabasePathList().add(fghome + "/TerraSync");

        boptions = new LoaderOptions();
        //wegen Performance material evtl. nicht setzen. TODO colorpalette
        if (materialmode == 2) {
            boptions.setMaterialLib(FlightGearModuleScenery.getInstance().get_matlib());
        }
        boptions.usegltf = true;

        // boptions.setPropertyNode(new SGPropertyNode());
        // boptions.getDatabasePathList().add(fghome + "/TerraSync");

        switch (mode) {
            case 0:
                // Die Runway in Dahlem ist eine Luecke. War das immer schon? Wahrscheinlich. Ueber den tilemgr (mode99) ist die Luecke nicht.
                // Es gibt ja auch ein EDKV.btg.gz.
                //17.4.19: Bei einem anderen btg als ref steht die Camera nicht darüber!
                String btgname = FlightGear.refbtg;

                new Obj().SGLoadBTG(new BundleResource(BundleRegistry.getBundle("dataNoLongerContainsBtg"), btgname), options, boptions, new GeneralParameterHandler<Node>() {
                    @Override
                    public void handle(Node node) {
                        Vector3 center = node.getTransform().getPosition();
                        // Sicherheitshalber pruefen, ob das BTG nicht vielleicht tiefer hängt und center dann falsch ist.
                        if (center.length() < WorldGlobal.km(4000)) {
                            throw new RuntimeException("center appears to be not set");
                        }
                        world.attach(node);
                    }
                });
                startpos = 0;

                break;
            case 1:
                // Das TerraSync/Terrain/e000n50/e000n50/2958154.stg (somewhere in England?) hat ein paar mehr Zeilen als bloss das btg.
                // Gibt es einmal in Objects und einmal in Terrain. Beide muessen gelesen werden.
                // Das 3056410.stg hat aber auch ein bischen mehr
                // 12.12.17: scheint nicht mehr zu gehen
                de.yard.threed.flightgear.core.osgdb.Options options1 = new de.yard.threed.flightgear.core.osgdb.Options();
                options1.getDatabasePathList().add(fghome + "/TerraSync");
                String[] stglist = new String[]{"2958154.stg", "3056410.stg", "2958169.stg"};

                for (String stg : stglist) {
                    ReadResult rr = null;//TODO 8.6.17: anders wegen bundle new ReaderWriterSTG().readNode(null, stg, options1);
                    if (rr.getNode() == null) {
                        throw new RuntimeException("node isType null. stg failed:" + stg);
                    }
                    SceneNode node1 = rr.getNode();
                    world.attach(node1);
                }
                break;
            case 2:
                FGTileMgr tilemgr = FlightGearModuleScenery.getInstance().get_tile_mgr();
                //Der init ist im state 7 noch nicht gelaufen
                tilemgr.init();
                //range, so wie es von FG gelogged wurde. Muss 2-mal aufgerufen werden!
                //mit 32000 braucht er zuviel Speicher
                double range_m = 32000;
                range_m = 4000;
                tilemgr.schedule_tiles_at(SGGeod.fromGeoCoordinate(WorldGlobal.greenwichtilecenter), range_m);
                tilemgr.schedule_tiles_at(SGGeod.fromGeoCoordinate(WorldGlobal.greenwichtilecenter), range_m);
                tilemgr.update_queues(false);
                world.attach(FlightGearModuleScenery.getInstance().get_scenery().get_terrain_branch());
                startpos = 1;
                break;
            case 3:
                // why? wireframe earth?
                earth = buildEarth();
                world.attach(earth);
                break;
            case 99:
                SGGeod pos = tiles[tileindex];
                currentbucket = new SGBucket(pos.getLongitudeDeg(), pos.getLatitudeDeg());
                loadbucket();
                break;
            default:
                break;
        }

        // Elsdorf?
        addMarkerCube("rot somewhere", new Vector3(3987743.8, 480804.66, 4937917.5), Color.RED, WorldGlobal.km(1));
        centercube = addMarkerCube("blue 0,0,0", new Vector3(0, 0, 0), Color.BLUE, WorldGlobal.km(1000));
        addMarkerCube("rfgcenter", FlightGear.refbtgcenter, Color.GREEN, WorldGlobal.km(1));
        //addMarkerCube("nordpol", new Vector3(0,0,WorldGlobal.EARTHRADIUS), Color.ORANGE, WorldGlobal.km(1000));
        addMarkerCube("nordpol", new Vector3(0, 0, WorldGlobal.EARTHRADIUS), Color.ORANGE, WorldGlobal.km(1000));


        addLight();

        ViewpointList vpl = new ViewpointList();
        controller = new StepController(camera.getCarrierTransform(), vpl);
        //etwa center einer bouncing sphere und dann etwa 1000-5000m darüber. Blick auf Erde. Man darf Offset nicht addieren, das verzerrt!
        double offset = 4000;
        //controller.addStep(new Vector3(3987743.8f + offset, 480804.66f + offset, 4937917.5f + offset), new Vector3(-3, 2, 0));
        //offset = 8000;
        //Blankenheim
        vpl.addEntry(FlightGear.refbtgcenter.multiply(1.004f), buildLookDownRotation/*12.12.17FlightLocation.buildRotation*/(SGGeod.fromCart(FlightGear.refbtgcenter)));
        // Greenwich
        //TODO POI controller.addStep(new Vector3(SGGeodesy.SGGeodToCart(WorldGlobal.greenwich500)).multiply(1.004f), new Vector3(0, 0, 0));

        // Elsdorf Ufo Richtung Osten
        //TODO POI controller.addStep(new Vector3(SGGeodesy.SGGeodToCart(WorldGlobal.elsdorf2000)), new Vector3(3987743.8f + offset, 480804.66f + offset, 4937917.5f + offset));
        // von weit ueber Äquator nach unten
        vpl.addEntryForLookat(new Vector3(WorldGlobal.EARTHRADIUS + WorldGlobal.km(20000), 0, 0), new Vector3(0, 0, 0));
        // als letztes wieder zum Anfang
        vpl.addEntryForLookat(initialcampos, initialcamlookat);

        //nur Test von hinten
        vpl.addEntryForLookat(new Vector3(0, 0, 0), FlightGear.refbtgcenter);
        //EDDK Overview
        vpl.addEntry(SGGeodesy.SGGeodToCart(SGGeod.fromGeoCoordinate(WorldGlobal.eddkoverview.location.coordinates)).multiply(1.002f), buildLookDownRotation(SGGeod.fromGeoCoordinate(WorldGlobal.eddkoverview.location.coordinates)));
        controller.stepTo(startpos);

        updatePosition();
        addToWorld(world);

        stgcycler = new StgCycler(getDefaultCamera());
    }

    @Override
    public void initSettings(Settings settings) {
        settings.aasamples = 4;
        // In Unity kommen sonst diverse Laufzeitfehler. Und der blaue Würfel wird damit auch korrekt verdeckt.
        settings.near = Float.valueOf(40);
    }

    @Override
    public Dimension getPreferredDimension() {
        return new Dimension(WIDTH, HEIGHT);
    }


    private void addLight() {
        DirectionalLight light = new DirectionalLight(Color.WHITE, new Vector3(0, 30000000, 20000000));

        addLightToWorld(light);
        light = new DirectionalLight(Color.WHITE, new Vector3(0, -30000000, -20000000));

        addLightToWorld(light);
    }

    public void add(SceneNode model, double x, double y, double z, double scale, Quaternion rotation) {
        world.attach(model);
        model.getTransform().setPosition(new Vector3(x, y, z));
        model.getTransform().setScale(new Vector3(scale, scale, scale));
        if (rotation != null) {
            model.getTransform().setRotation(rotation);
        }
    }

    private SceneNode buildEarth() {
        SceneNode m = null, model = new SceneNode();
        model.attach(m);
        return model;
    }

    private SceneNode addMarkerCube(String name, Vector3 position, Color col, double size) {
        ShapeGeometry cubegeometry = ShapeGeometry.buildBox(size, size, size, null);
        Material mat = Material.buildLambertMaterial(col);
        SceneNode model = new SceneNode(new Mesh(cubegeometry, mat));
        model.getTransform().setPosition(position);
        model.setName(name);
        world.attach(model);
        return model;
    }

    @Override
    public void update() {
        double currentdelta = getDeltaTime();

        if (Input.getKey(KeyCode.Shift)) {
            if (Input.getKeyDown(KeyCode.UpArrow) && mode == 99) {
                currentbucket = currentbucket.sibling(0, 1);
                loadbucket();
            }
            if (Input.getKeyDown(KeyCode.DownArrow) && mode == 99) {
                currentbucket = currentbucket.sibling(0, -1);
                loadbucket();
            }
            if (Input.getKeyDown(KeyCode.LeftArrow) && mode == 99) {
                currentbucket = currentbucket.sibling(-1, 0);
                loadbucket();
            }
            if (Input.getKeyDown(KeyCode.RightArrow) && mode == 99) {
                currentbucket = currentbucket.sibling(1, 0);
                loadbucket();
            }
        } else {
            // value is a good compromise for high altitudes (quite slow) and low altitudes (quite quick)
            double inc = 0.000001;
            if (Input.getKey(KeyCode.UpArrow)) {
                SGGeod current = SGGeod.fromCart(getDefaultCamera().getCarrierPosition());
                current.setLatitudeRad(current.getLatitudeRad() + inc);
                getDefaultCamera().getCarrier().getTransform().setPosition(current.toCart());
            }
            if (Input.getKey(KeyCode.DownArrow)) {
                SGGeod current = SGGeod.fromCart(getDefaultCamera().getCarrierPosition());
                current.setLatitudeRad(current.getLatitudeRad() - inc);
                getDefaultCamera().getCarrier().getTransform().setPosition(current.toCart());
            }
            if (Input.getKey(KeyCode.LeftArrow)) {
                SGGeod current = SGGeod.fromCart(getDefaultCamera().getCarrierPosition());
                current.setLongitudeRad(current.getLongitudeRad() - inc);
                getDefaultCamera().getCarrier().getTransform().setPosition(current.toCart());
            }
            if (Input.getKey(KeyCode.RightArrow)) {
                SGGeod current = SGGeod.fromCart(getDefaultCamera().getCarrierPosition());
                current.setLongitudeRad(current.getLongitudeRad() + inc);
                getDefaultCamera().getCarrier().getTransform().setPosition(current.toCart());
            }

        }
        if (Input.getKey(KeyCode.PageUp) && fps == null) {
            //elevation += 1000;
            //updatePosition();
            getDefaultCamera().getCarrier().getTransform().setPosition(getDefaultCamera().getCarrierPosition().multiply(1.00001f));
        }
        if (Input.getKey(KeyCode.PageDown) && fps == null) {
            //elevation -= 1000;
            //updatePosition();
            getDefaultCamera().getCarrier().getTransform().setPosition(getDefaultCamera().getCarrierPosition().multiply(0.99999f));
        }
        if (Input.getKeyDown(KeyCode.F)) {
            if (fps == null) {
                fps = new FirstPersonController(getDefaultCamera().getCarrierTransform(), true);
                fps.transformer.setMovementSpeed(5000);
            } else {
                fps = null;
            }
        }
        // stgcycler uses KeyCode.N and KeyCode.T
        stgcycler.update();

        if (Input.getKeyDown(KeyCode.C)) {
            nordpolcheck();
        }
        if (Input.getKeyDown(KeyCode.V)) {
            FlightGearMain.runFlightgearTests(new Vector3());
        }
        if (fps != null) {
            fps.update(currentdelta);
        } else {
            controller.update(currentdelta);
        }
        //PickingRay only on mouse click, else is inefficent (Unity).
        Point mouseclick;
        if ((mouseclick = Input.getMouseDown()) != null) {
            logger.debug("Processing mouse click");
            checkForPickingRay(mouseclick);

        }
    }

    private void setLocation(FlightLocation loc) {
        Camera camera = getDefaultCamera();
        LocalTransform posrot = loc.toPosRot(new FgCalculations());
        camera.getCarrier().getTransform().setPosition(posrot.position);
        camera.getCarrier().getTransform().setRotation(posrot.rotation);
    }

    /**
     * Am Nordpol eine Turbine hinstellen und seitlich betrachten. Seitlich heisst etwas den Nullmeridian entlang (etwas erhöht). Dann blickt man
     * auf die linke Dangast Bildseite. (also von links auf das Bild).
     */
    private void nordpolcheck() {
        SGGeod nordpol = SGGeod.fromDegM(new Degree(0), new Degree(90), 0);
        buildNordPolTile(nordpol);
        Vector3 nordpolcart = nordpol.toCart();

        addModel(nordpol, new BundleResource(BundleRegistry.getBundle("data"), new ResourcePath("flusi"), "windturbine.xml"), new Degree(0));

        // 20m über Nordpoltile
        Camera camera = getDefaultCamera();
        Vector3 campos = nordpolcart.add(new Vector3(200, 0, 20));
        camera.getCarrier().getTransform().setPosition(campos);
        camera.lookAt(nordpol.toCart(), new Vector3(0, 0, 1));
    }

    /**
     * @param geo
     * @param model
     * @param heading
     */
    private void addModel(SGGeod geo, BundleResource model, Degree heading) {
        SceneNode sn = new SceneNode(SGReaderWriterXML.buildModelFromBundleXML(model, null, new XmlModelCompleteDelegate() {
            @Override
            public void modelComplete(BundleResource source, SceneNode destinationNode, List<SGAnimation> animationList) {

            }
        }).getNode());

        // TODO wait for above async

        //8.6.17: ac policy macht z.B. der buildModel
        SceneNode n = sn;//new SceneNode();
        //n.attach(sn);
        //n = new ACProcessPolicy("ac").process(n, null, null);


        Vector3 position = geo.toCart();
        n.getTransform().setPosition(position);
        /*Quaternion rotation = OsgMath.makeZUpFrame(geo);
        float pitch = 0;
        float roll = 0;
        //rotation = rotation.multiply(new Quaternion(new Degree(heading), new Degree(pitch), new Degree(roll)));
        Quaternion q = MathUtil2.buildQuaternionFromAngleAxis(new Degree(heading).toRad(), new Vector3(0, 0, 1).vector3);
        rotation = rotation.multiply(new Quaternion(q));*/
        n.getTransform().setRotation(new FgCalculations().buildZUpRotation(geo.toGeoCoordinate(), heading, new Degree(0)));

        world.attach(n);

    }

    /**
     * unterm Nordpol ein Viereck als Tileersatz ohne Rotation, um Orientierungen prüfen zu können
     * Dangast links oben ist der Punkt in der Westhemispäre und Richtung "vorne" (Afrika). (positive x und y)
     * <p>
     * Die Orientierung ist schwierig.
     */
    private void buildNordPolTile(SGGeod nordpol) {
        Vector3 position = nordpol.toCart();

        SceneNode needle = new SceneNode();
        needle.setName("NordpolTile");
        double size2 = 100;

        List</*7.2.18 Native*/Vector3> vertices = new ArrayList<Vector3>();
        FaceList faces = new FaceList(true);
        vertices.add(position.add(new Vector3(size2, -size2, 0)));
        vertices.add(position.add(new Vector3(size2, size2, 0)));
        vertices.add(position.add(new Vector3(-size2, size2, 0)));
        vertices.add(position.add(new Vector3(-size2, -size2, 0)));
        Vector2 uv0 = new Vector2(0, 0);
        Vector2 uv1 = new Vector2(0, 1);
        Vector2 uv2 = new Vector2(1, 1);
        Vector2 uv3 = new Vector2(1, 0);

        faces.faces.add(new Face3(0, 1, 2, uv1, uv0, uv3));
        faces.faces.add(new Face3(2, 3, 0, uv3, uv2, uv1));
        //faces.faces.add(new Face3(2, 1, 0, uv3, uv0, uv1));
        //faces.faces.add(new Face3(0, 3, 2, uv1, uv2, uv3));
        SimpleGeometry geo = GeometryHelper.prepareGeometry(vertices, new SmartArrayList<FaceList>(faces), null, false, new Degree(30)).get(0);
        Material mat = Material.buildBasicMaterial(Texture.buildBundleTexture("data", "images/river.jpg"));
        SceneNode tile = new SceneNode(new Mesh(new GenericGeometry(geo), mat));
        tile.setName("North Tile");
        world.attach(tile);
    }


    /**
     * TODO prevent double. Not yet robust, update() must check for running?
     * 12.6.17 Now async.
     */
    private void loadbucket() {
        String bucket = currentbucket.gen_index_str();
        String stgfile = bucket + ".stg";
        String bundlename = FlightGear.getBucketBundleName(bucket);
        AbstractSceneRunner.instance.loadBundle(bundlename, (Bundle b) -> {

            boolean terrainonly = false;//true;
            if (terrainonly) {
                options.pluginstringdata.put("SimGear::FG_ONLY_TERRAIN", "ON");
            }
            // 7.5.25: Now with shared
            SceneNode rr = SceneryPager.loadBucketByStg(stgfile, options, boptions);
            world.attach(rr);
            stgcycler.currentStgTile = rr;
            updatePosition();
            stgcycler.currentobject = 0;
        });
    }

    private void updatePosition() {
        Camera camera = getDefaultCamera();
        // bei mode 0 z.B. gibts kein currentbucket
        if (currentbucket != null) {
            SGGeod center = currentbucket.get_center();
            center.setElevationM(elevation);
            camera.getCarrier().getTransform().setPosition(center.toCart());
            camera.getCarrier().getTransform().setRotation(buildLookDownRotation(center));
        }
    }

    /**
     * Das duerfte zumindest für die Nordhalbkugel stimmen.
     *
     * @param location
     * @return
     */
    private Quaternion buildLookDownRotation(SGGeod location) {
        // Nach vorne kippen ist negativer Pitch. 8.6.17: Warum die Parameter so sind? Weil Camera anders als Model ist?
        return new FgCalculations().buildZUpRotation(location.toGeoCoordinate(), new Degree(-90), new Degree(0));
    }

    /**
     * only on mouse click, else is inefficent.
     */
    private void checkForPickingRay(Point mouseclick) {
        Ray pickingray = getMainCamera().buildPickingRay(getMainCamera().getCarrier().getTransform(), mouseclick);
        logger.debug("Mouse click at x=" + mouseclick.getX() + ", y=" + mouseclick.getY() + ", ray=" + pickingray);

        List<NativeCollision> intersects = pickingray.getIntersections();
        if (intersects.size() > 0) {
            NativeSceneNode intersect = intersects.get(0).getSceneNode();
            String names = "";
            for (int i = 0; i < intersects.size(); i++) {
                names += "," + intersects.get(i).getSceneNode().getName();
            }
            logger.debug("" + intersects.size() + " intersections of picking ray detected: " + names + ", getFirst = " + intersect.getName());
        }
    }

    private void checkForPickingRayBox() {
        Vector3 redboxpos = new Vector3(0, 0, 0);
        Vector3 campos = new Vector3(4000000, 5000000, 11000000);
        Ray raycasterredbox = new Ray(campos, redboxpos.subtract(campos));
        campos = new Vector3(4059458.2f, 471490.44f, 4913503.0f);
        Vector3 direction = redboxpos.subtract(campos);
        direction = direction.normalize();
        System.out.println("direction=" + direction);
        raycasterredbox = new Ray(campos, direction/*new Vector3(0.43643576f,-0.21821788f,-0.8728715f)*/);
        List<NativeCollision> intersects = raycasterredbox.getIntersections(centercube, true);
        //liefert 1 oder 2 
        if (intersects.size() == 0) {
            Assert.fail("no center box intersection found");
        }
        RuntimeTestUtil.assertEquals("name", "blue 0,0,0", intersects.get(0).getSceneNode().getName());
        logger.debug("redbox.intersect=" + (intersects.get(0).getPoint()).dump(" "));
        intersects = raycasterredbox.getIntersections();
        //liefert 1 oder 2 
        if (intersects.size() == 0) {
            Assert.fail("no red box intersection found");
        }
        RuntimeTestUtil.assertEquals("name", "blue 0,0,0", intersects.get(0).getSceneNode().getName());
    }


}
