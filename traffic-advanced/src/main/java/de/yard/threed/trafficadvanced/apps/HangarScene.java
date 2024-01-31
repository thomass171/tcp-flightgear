package de.yard.threed.trafficadvanced.apps;


import de.yard.threed.core.Color;
import de.yard.threed.core.Degree;
import de.yard.threed.core.Dimension;
import de.yard.threed.core.DimensionF;
import de.yard.threed.core.IntHolder;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Payload;
import de.yard.threed.core.Point;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.HttpBundleResolver;
import de.yard.threed.engine.Camera;
import de.yard.threed.engine.DirectionalLight;
import de.yard.threed.engine.FirstPersonController;
import de.yard.threed.engine.Input;
import de.yard.threed.engine.KeyCode;
import de.yard.threed.engine.Light;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Mesh;
import de.yard.threed.engine.Observer;
import de.yard.threed.engine.ObserverSystem;
import de.yard.threed.engine.Ray;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneMode;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.Texture;
import de.yard.threed.engine.Transform;
import de.yard.threed.engine.avatar.AvatarSystem;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.InputToRequestSystem;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.SystemState;
import de.yard.threed.engine.ecs.TeleportComponent;
import de.yard.threed.engine.ecs.TeleporterSystem;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.geometry.ShapeGeometry;
import de.yard.threed.engine.gui.DefaultMenuProvider;
import de.yard.threed.engine.gui.GuiGrid;
import de.yard.threed.engine.gui.Icon;
import de.yard.threed.engine.gui.Menu;
import de.yard.threed.engine.gui.MenuCycler;
import de.yard.threed.engine.gui.MenuProvider;
import de.yard.threed.engine.gui.Text;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.ModelLoader;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.Settings;
import de.yard.threed.engine.vr.VrInstance;
import de.yard.threed.flightgear.FgBundleHelper;
import de.yard.threed.flightgear.FgVehicleLoader;
import de.yard.threed.flightgear.FlightGearSettings;
import de.yard.threed.flightgear.SimpleBundleResourceProvider;
import de.yard.threed.flightgear.core.SGLoaderOptions;
import de.yard.threed.flightgear.core.simgear.scene.model.ACProcessPolicy;
import de.yard.threed.flightgear.ecs.AnimationComponent;
import de.yard.threed.flightgear.ecs.AnimationUpdateSystem;
import de.yard.threed.traffic.TrafficConfig;
import de.yard.threed.traffic.VehicleLauncher;
import de.yard.threed.traffic.VehicleLoaderResult;
import de.yard.threed.traffic.config.VehicleDefinition;
import de.yard.threed.trafficcore.model.Vehicle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 22.12.18: Vielleicht als VehicleViewer ausbauen? Aber es gibt ja noch ModelPreviewScene. Wie grenzt sich das dann ab?
 * Die CockpitScene setzt jedenfalls auf TrafficWorld.xml und ECS (z.B. TeleporterSystem). Aber nicht auf TrafficWorld(graph etc).
 * Was ist mit Animationen?
 * 02.10.19: Zur Abgrenzung zu ModelPreviewScene: Da kann man Objekte drehen/scalen hier nicht. Dafuer kann man hier ins Cockpit, da nicht.
 * Und bzgl. Animationen: Die kann man hier im Vehicle dann nutz/testbar machen.
 * Weil es gut passt und es hier noch kein Menu gibt, CDU als HUD menu verwenden.
 * Vielleicht kann man dies zu einer HangarScene mit allen Vehicles ausbauen, auch mit Nutzung von ShadowModellen und Teleport.
 * 18.10.19: x/y/z Kalibrierung hat falsche Orientierung.
 * Ich leg für hier mal wie Railing y=0 Ebene fest, einfach, damit es anders ist als FlatTravelScene.
 * <p>
 * Tasten:
 * t teleport, cycle position
 * l cycle aircraft (war mal 'n')
 * m cycle menu (cdu and 'normal')
 * 7.3.21: Dass 'Lok' hier mit drin ist, ist ja doch ein boesses Coupling. Die gehört nach 'railing' und damit fertig. Obwohl, der kann hier Bundle laden
 * wie er möchte.
 *
 * 4.12.23: Migration from TrafficWorldConfig to TrafficConfig (without sceneconfig).
 * 30.1.24: f.k.a. CockpitScene
 */
public class HangarScene extends Scene {
    Log logger = Platform.getInstance().getLog(HangarScene.class);
    private double backlightitensity = 0.5f;
    FirstPersonController fps = null;
    boolean usearp = true;
    int vehicleindex = 0;
    SceneNode currentaircraft = null;
    /*4.12.23 TrafficWorldConfig*/ TrafficConfig tw;
    SceneNode hud;
    List<String> vehiclelist = new ArrayList<String>();
    MenuCycler menuCycler = null;
    boolean enableLoweredAvatar = false;
    Double yoffsetVR;
    boolean avatarInited = false;
    boolean modelInited = false;

    @Override
    public void init(SceneMode forServer) {
        // Kruecke zur Entkopplung des Modelload von AC policy.
        ModelLoader.processPolicy = new ACProcessPolicy(null);

        processArguments();
        //Camera camera = getDefaultCamera();
        //5.12.23 tw = new TrafficWorldConfig("data-old", "TrafficWorld.xml");
        //5.12.23 TrafficWorldConfig railing = new TrafficWorldConfig("railing", "config/Railing.xml");
        tw = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("traffic-advanced"), BundleResource.buildFromFullString("Hangar.xml"));

        List<Vehicle> vlist = /*20.11.23 tw*/FlightTravelScene.getVehicleListByName("VehiclesWithCockpit");

        for (int i = 0; i < vlist.size(); i++) {
            vehiclelist.add(vlist.get(i).getName());
        }

        //30.6.21: Observer muss jetzt noch an den Avatar (ausser VR?). Das ist hier wahrscheinlich nicht der beste Platz dafür.
        //27.1.22 wird vielleicht schon irgendwo gemacht Observer.getInstance().getTransform().setParent(avatar.getSceneNode().getTransform());
        SystemManager.addSystem(new UserSystem());
        SystemManager.addSystem(new AvatarSystem());

        TeleporterSystem ts = new TeleporterSystem();
        //anim ist zu ruckelig/fehlerhaft
        ts.setAnimated(false);
        SystemManager.addSystem(ts, 0);


        ObserverSystem viewingsystem = new ObserverSystem(true);
        SystemManager.addSystem(viewingsystem, 0);

        //z.B. fuer Click in CDU menu
        AnimationUpdateSystem animationUpdateSystem = new AnimationUpdateSystem();
        SystemManager.addSystem(animationUpdateSystem, 0);

        addLight();

        AbstractSceneRunner.instance.loadBundle("fgdatabasic", (Bundle b1) -> {
            logger.debug("fgdatabasic loaded. Loading aircraft now");
            //27.1.22 jetzt spaeter newModel(vehicleindex);

        });
        //addToWorld(ModelSamples.buildAxisHelper(200, 1));

        if (usearp) {
            FgBundleHelper.addProvider(new SimpleBundleResourceProvider("fgdatabasic"));
            // arp = new AircraftResourceProvider();
            // BundleRegistry.addProvider(arp);
        }
        //menuCycler = new MenuCycler(new MenuProvider[]{new CockpitMenuBuilder(this), new CockpitMainMenuBuilder(this)});
        menuCycler = new MenuCycler(new MenuProvider[]{new CockpitMenuBuilder(this), new DefaultMenuProvider(this.getDefaultCamera(), () -> {
            //Versuchen, ca 3 m vor Avatar statt nearplane, damit es normal und in VR brauchbar ist.
            //ach, wie CDU 5m.
            double width = 5.5;

            GuiGrid menu = new GuiGrid(new DimensionF(width, width * 0.7), -5, -4.9, 1, 6, 3, GuiGrid.GREEN_SEMITRANSPARENT);
            // In der Mitte rechts ein Button mit Image
            menu.addButton(/*new Request(rs.REQUEST_CLOSE),*/ 4, 1, 1, Icon.ICON_CLOSE, () -> {
                menuCycler.close();
            });
            // und unten in der Mitte ein breiter Button mit Text
            menu.addButton(/*new Request(rs.REQUEST_CLOSE),*/ 2, 0, 2, new Text("Load", Color.BLUE, Color.LIGHTBLUE), () -> {
                //unpraktisch weil man vielleicht weiter möchte rs.menuCycler.close();
                cycleVehicle(1, vehiclelist.size());
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
        })});

        SystemManager.addSystem(new InputToRequestSystem(), 0);
        //1.4.21 Player.init(avatar);

        //Die Plane ist schon in y=0 und muss nicht rotiert werden.
        Material goundmat = Material.buildLambertMaterial(Color.GRAY);
        SceneNode ground = new SceneNode(new Mesh(ShapeGeometry.buildPlane(500, 500, 1, 1), goundmat, true, true));
        ground.getTransform().setPosition(new Vector3(0, 0, 0));
        ground.setName("Ground");
        addToWorld(ground);

        Observer.buildForDefaultCamera();

        // Avatar anlegen (via login)
        SystemManager.putRequest(UserSystem.buildLoginRequest("Freds account name", ""));

        // 24.1.22: State ready to join now needed for 'login'
        SystemState.state = SystemState.STATE_READY_TO_JOIN;

    }

    protected void processArguments() {
        Boolean b;
        if ((b = Platform.getInstance().getConfiguration().getBoolean("argv.enableLoweredAvatar")) != null) {
            enableLoweredAvatar = b;
        }
        yoffsetVR = Platform.getInstance().getConfiguration().getDouble("argv.yoffsetVR");
    }

    @Override
    public String[] getPreInitBundle() {
        // "fgdatabasic" in project is only a small subset. The external should have 'before' prio to load instead of subset.
        // "data" is needed for taxiway ground texture.
        // "railing" is a relict from using 'loc"
        Platform.getInstance().addBundleResolver(new HttpBundleResolver("fgdatabasic@https://ubuntu-server.udehlavj1efjeuqv.myfritz.net/publicweb/bundlepool"),true);
        return new String[]{"engine","traffic-advanced"/*5.12.23 "data-old"*/,
                "data", /*30.1.24"railing",*//*BundleRegistry.FGHOMECOREBUNDLE,*/
                FlightGearSettings.FGROOTCOREBUNDLE};
    }

    @Override
    public void initSettings(Settings settings) {
        settings.vrready = true;
    }

    /**
     * Licht in diesem Sinne gibt es mit dem model-combined shader nicht.
     * Aber es werden ja vielleicht, zumindest zum Test, auch mal
     * Standardshader verwendet. Und die brauchen dieses Licht.
     */
    private void addLight() {
        // Licht aus Höhe 60. 2.5.19: Auf einmal ist alles so dunkel. Komisch. Obwohl irgendwie plausibel. Darum noch eins von hinten.
        Light light = new DirectionalLight(Color.WHITE, new Vector3(0, 60, 0));
        addLightToWorld(light);
        light = new DirectionalLight(Color.WHITE, new Vector3(1, 0, 0));
        addLightToWorld(light);
    }

    @Override
    public void update() {
        double tpf = getDeltaTime();

        if (Input.getKeyDown(KeyCode.F)) {
            if (fps == null) {
                fps = new FirstPersonController(getDefaultCamera().getCarrierTransform(),true);
                //fps.setMovementSpeed(100);
            } else {
                fps = null;
            }
        }
       /* if (!useteleport) {
            if (fps != null) {
                fps.update(tpf);
            } else {
                controller.update(tpf);
            }
        }*/

        //10.3.22 Avatar avatar = null; AvatarSystem.getAvatar();
        EcsEntity user = UserSystem.getInitialUser();
        if (!avatarInited && user != null && TeleportComponent.getTeleportComponent(user) != null) {
            //if (avatar == null) {
            //    logger.warn("no avatar yet");
            // } else {
            // Outside von rechtem Wing. Bewusst auf Höhe 0, um vertikale Orientierung des Aircraft zu sehen.
            // Avater ist schon fuer FG orinetiert und muss sich nur nach links drehen.
            // 18.10.19: Ist jetzt y=0 Ebene. Dann muss der sich doch gar nicht mehr drehen.
            // Und nicht mehr auf 0, sondern Avatar like etwas höher.

            TeleportComponent.getTeleportComponent(user).addPosition("Wing", new LocalTransform(new Vector3(0, 1.0, 50), Quaternion.buildRotationZ(new Degree(0))));

            //4.10.19 verdirbt der Observer VR? Offenbar nicht
            /*31.10.23 now in ObserverSystem ObserverComponent oc = new ObserverComponent(getDefaultCamera().getCarrierTransform());
            oc.setRotationSpeed(40);
            UserSystem.getInitialUser().addComponent(oc);*/

            avatarInited = true;
            // }
        }

        if (avatarInited && !modelInited) {
            newModel(vehicleindex);
            modelInited = true;
        }

        //1.1.20 N->L
        if (Input.getKeyDown(KeyCode.L)) {
            if (Input.getKey(KeyCode.Shift)) {
                cycleVehicle(-1, vehiclelist.size());
            } else {
                cycleVehicle(1, vehiclelist.size());
            }
        }
        Point mouselocation = Input.getMouseDown();

        menuCycler.update(mouselocation);
        Observer.getInstance().update();
    }


    /**
     * Nicht zu gross machen wegen WebGL. Da muss man sonst scrollen.
     *
     * @return
     */
    @Override
    public Dimension getPreferredDimension() {
        if (((Platform) Platform.getInstance()).isDevmode()) {
            return new Dimension(1024, 768);
        }
        return null;
    }

    public void cycleVehicle(int inc, int cnt) {
        vehicleindex += inc;
        if (vehicleindex < 0) {
            vehicleindex = cnt - 1;
        }
        if (vehicleindex >= cnt) {
            vehicleindex = 0;
        }
        newModel(vehicleindex);
    }

    private void newModel(int index) {
        if (currentaircraft != null) {
            SceneNode.removeSceneNode(currentaircraft);
            currentaircraft = null;
        }
        //FlightGearAircraft aircraft = FlightGearAircraft.aircrafts[index];
        VehicleDefinition config;
        //if (index < vehiclelist.size()){
        //5.12.23 config = ConfigHelper.getVehicleConfig(tw.tw, vehiclelist.get(index));

        // 4.1.24: 'bluebird' is in other config file, so currently not available
        Bundle bnd = BundleRegistry.getBundle("traffic-advanced");
        TrafficConfig vehicleDefinitions = TrafficConfig.buildFromBundle(bnd, BundleResource.buildFromFullString("vehicle-definitions.xml"));
        config = vehicleDefinitions.findVehicleDefinitionsByName(vehiclelist.get(index)).get(0);



       /* }else{
            config=tw.getAircraftConfig(aircraftlist.get(index-vehiclelist.size()));
        }*/

        // 18.10.19: TrafficHelper geht hier nicht, weil das Vehicle nicht auf einen Graph kommt. Naja, da koennte man ja null übergeben, oder ähnlich.
        new FgVehicleLoader()/*FgVehicleLauncher*/.loadVehicle(new Vehicle(vehiclelist.get(index)), config, (SceneNode container, VehicleLoaderResult loaderResult/*List<SGAnimation> animationList, SGPropertyNode propertyNode,*/, SceneNode lowresNode) -> {
            //SceneNode basenode=container;
            SceneNode basenode = VehicleLauncher.getModelNodeFromVehicleNode(container);
            currentaircraft = container;

            LocalTransform vehiclebasetransform = /*5.12.23sceneConfig*/tw.getBaseTransformForVehicleOnGraph();
            if (vehiclebasetransform != null) {
                currentaircraft.getTransform().setPosition(vehiclebasetransform.position);
                currentaircraft.getTransform().setRotation(vehiclebasetransform.rotation);
            }

            TeleportComponent pc = TeleportComponent.getTeleportComponent(UserSystem.getInitialUser());
            //TODO besser erkennen , welche alten weg müssen
            pc.removePosition("Captain");
            pc.removePosition("Driver");
            pc.removePosition("BackSide");
            Map<String, LocalTransform> viewpoints = config.getViewpoints();
            for (String key : viewpoints.keySet()) {
                pc.addPosition(key, basenode.getTransform(), new LocalTransform(viewpoints.get(key).position, viewpoints.get(key).rotation));
            }

            addToWorld(currentaircraft);

            logger.debug("teleport positions:" + pc.getPointCount());
            for (int i = 0; i < pc.getPointCount(); i++) {
                logger.debug("teleport position:" + pc.getPointLabel(i));
            }
        });
    }
}

class CockpitMenuBuilder implements MenuProvider {
    HangarScene sc;

    CockpitMenuBuilder(HangarScene sc) {
        this.sc = sc;

    }

    @Override
    public Menu buildMenu() {
        return new CockpitCduMenu(sc.getDefaultCamera(), sc.logger);
    }

    @Override
    public Transform getAttachNode() {
        return Observer.getInstance().getTransform();//tem.getAvatar().getNode();//getFaceNode();
        //rs.getDefaultCamera().getCarrier();
    }

    /* @Override
     public Camera getCamera() {
         return null;//FovElement.getDeferredCamera(null);
     }*/
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
 * Ob der an deferred Cam kommen sollte? Wegen VR?
 * Weils in VR eigentlich kein HUD gibt, besser nicht.
 * Ich versuchs mal an die default Cam. Zwar in Cam Space, aber nicht deferred.
 */
class CockpitCduMenu implements Menu {
    Log logger;
    SceneNode cduCarrier;
    EcsEntity entity;

    CockpitCduMenu(Camera camera, Log logger) {
        this.logger = logger;
        // Einen Cube links oben

        cduCarrier = new SceneNode();//FovElement.buildSceneNodeForDeferredCamera(camera);

        AbstractSceneRunner.instance.loadBundle("fgdatabasic", (Bundle bundle) -> {

            SGLoaderOptions opt = new SGLoaderOptions();
            //opt.setPropertyNode(destinationProp);
            //lieber cdu2, dann kann das ganze Display eine einzige Textur (Canvas?) sein, statt einzelne Buchstaben.
            entity = de.yard.threed.flightgear.traffic.ModelFactory.buildModelFromBundleXmlAsEntity(new BundleResource(bundle, "Aircraft/Instruments-3d/cdu2/boeing.xml"), null);
            entity.setName("CDU-Menu");
            AnimationComponent.getAnimationComponent(entity).setCameraProvider(() -> {
                return camera/*FovElement.getDeferredCamera(null)*/;
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
        return cduCarrier;//getDefaultCamera().getCarrier();
    }

    @Override
    public boolean checkForClickedArea(Ray ray) {
        // das geht doch alles ueber PickAnimation. 6.11.19: Auch in VR?
        return false;
    }

    @Override
    public void checkForSelectionByKey(int position) {

    }

    @Override
    public void remove() {
        //TODO check/test das er weg ist
        SystemManager.removeEntity(entity);
        SceneNode.removeSceneNode(cduCarrier);
        entity = null;
        cduCarrier = null;
    }


}
