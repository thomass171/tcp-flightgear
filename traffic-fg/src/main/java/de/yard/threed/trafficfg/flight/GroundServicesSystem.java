package de.yard.threed.trafficfg.flight;

import de.yard.threed.core.CharsetException;
import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.Payload;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.XmlException;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.XmlDocument;
import de.yard.threed.engine.ecs.DefaultEcsSystem;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsGroup;
import de.yard.threed.engine.ecs.EcsHelper;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.VelocityComponent;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.RequestType;
import de.yard.threed.graph.DefaultGraphWeightProvider;
import de.yard.threed.graph.GraphEdge;
import de.yard.threed.graph.GraphEventRegistry;
import de.yard.threed.graph.GraphMovingComponent;
import de.yard.threed.graph.GraphNode;
import de.yard.threed.graph.GraphPath;
import de.yard.threed.graph.GraphPosition;
import de.yard.threed.graph.GraphProjection;
import de.yard.threed.graph.TurnExtension;
import de.yard.threed.traffic.NoElevationException;
import de.yard.threed.traffic.RequestRegistry;
import de.yard.threed.traffic.ScenerySystem;
import de.yard.threed.traffic.SphereProjections;
import de.yard.threed.traffic.TrafficEventRegistry;
import de.yard.threed.traffic.TrafficGraph;
import de.yard.threed.traffic.TrafficHelper;
import de.yard.threed.traffic.TrafficSystem;
import de.yard.threed.traffic.VehicleComponent;
import de.yard.threed.traffic.config.VehicleDefinition;
import de.yard.threed.traffic.geodesy.MapProjection;
import de.yard.threed.traffic.geodesy.SimpleMapProjection;
import de.yard.threed.trafficcore.model.Airport;
import de.yard.threed.trafficcore.model.Runway;
import de.yard.threed.trafficfg.AirportTrafficContext;
import de.yard.threed.trafficfg.TrafficEvent;
import de.yard.threed.trafficfg.TrafficRequest;
import de.yard.threed.trafficfg.config.AirportConfig;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Erstellt GroundServices Aktionen fur vorhandene Vehicles in einem Groundnet per Schedules im TrafficSystem.
 * Delegates standard graph movement to GraphMovingSystem.
 * <p>
 * Logically handles all traffic. Movement of vehicles isType controlled by the GraphMovingSystem.
 * Singleton?. EcsSystem. Warum? Damit es jemanden gibt, der zum Abschluss von Movements neue auslösen kann.
 * 9.5.17: Einer muss ja die Trafficsteuerung übernehmen. Und der muss updaten() und auch auf Events reagieren (z.B. Vehicle hat Ziel erreicht).
 * Warum dann kein System? Es könnte auch GroundTraficSystem heissen, aber sagen wir erstmal ist ja auch Traffic.
 * Kann man immer noch aufdröseln.
 * <p>
 * Die Requests (z.B. Followme Anforderung) können hier als Event reinkommen.
 * Hier werden auch die Entities angelegt (über buildVehicle(), aber nicht die Model) und vorgehalten. Wobei vorgehalten werden muessen sie ja doch nicht wirklich.
 * Ob das so ganz sauber ist, muss sich noch zeigen.
 * <p>
 * Requests beginnen mit "request".
 * 15.7.17: Wenn dies ein ECS System per Vehicle ist, sollte es nicht alle Vehicles kennen. Dafür muesste es ein weiteres ECSSystem ohne ... geben. --> TrafficController
 * Der ist aber hier aufgegangen. Der SystemManager kennt alle Entities. Das duerfte reichen.
 * 12.02.2018: Sollte in 2D(GroundServices) wie in 3D(FlighScene) nutzbar sein. Das der immer ein groundnet kennt, ist eigentlich zu speziell. Hier kann es
 * auch mehrere Graphen geben; oder Rundfluege etc. Das sollte
 * zB. mit TrafficWorld entkoppelt werden. Oder ist das hier eigentlich ein GroundServiceSystem? Das könnte auch gut passen. Z.B. wegen der ganzen GroundService
 * spezifischen Use Cases. Dann kann er ein startendes Vehicle an ein anderes System übergeben oder übergeben bekommen?
 * 20.2.18: Und er könnte erkennen, wann ein Groundnet zu laden ist. Jetzt umbenannt: TrafficSystem->GroundServicesSystem.
 * 23.2.18: Nicht mehr Master der Projection. Die wird reingegeben (statt origin, das muesste hier irrelevant sein).
 * 27.2.18. Aber wenn das hier für GroundServices ist, gehoert Schedule und sowas nicht hier hin. Also brauchts doch zusaetzlich noch ein TrafficSystem, in dem
 * schedules liegen. Das hatte ich oben auch schon mal erwähnt. Ich starte mal eine Aufsplittung. Also gibts jetzt ein TrafficSystem und ein GroundServicesSystem.
 * schedules sind jetzt in TrafficSystem.
 * Und als System ist es ja optional, wenn es keinen Airport mit Groundnet gibt, gibt es auch dies System nicht.
 * 2.3.18: Der braucht doch keinen eigenen Visualizer. Er sollte Events schicken.
 * 6.3.18: Es kann aber ein Groundnet ohne GroundServicesSystem geben. Darum groundnet per setter, nee, Event.
 * Sonst ist System inaktiv (statt geloescht). Das gilt auch fuer airport.
 * Und Projection wieder in Scene.
 * 13.3.19: Arbeitet jetzt auf GroundServiceComponent statt VehicleComponent. automove verschoben nach TrafficSystem.
 * <p>
 * <p>
 * Created by thomass on 31.03.17.
 */
public class GroundServicesSystem extends DefaultEcsSystem {


    //private static TrafficSystem instance=null;
    private static Log logger = Platform.getInstance().getLog(GroundServicesSystem.class);
    // Der Visualizer kann auch null sein. Er ist hier bekannt, weil ja je nach Traffic etwas neues anzuzeigen sein kann.
    // 12.2.108: Dafuer gibt es aber doch das GraphVisualizationSystem, das über Event informiert wird. Dann sollte nur
    // der den Visualizer kennen. Wenn das Terrain stimmig ist, ist vielleicht ohnehin nur was zu Debugzwecken anzuzeigen.
    //2.3.18 GraphVisualizer visualizer;
    //13.3.19 wueste Kruecke.Warum? War mal public, deswegen? Wegen static wahrscheinlichh?
    /*31.3.20static*/
    //23.5.2020 das hat doch in einem System nicht zu suchen
    @Deprecated
    private GroundNet groundnet;
    public static boolean trafficsystemdebuglog = true;
    private FollowMe followme;
    //12.2.2018:Die Projection sollten nur die Darsteller kennen (GraphVisualizationSystem,TerrainSystem)
    //public MapProjection projection;
    //TODO queuen?
    TrafficRequest request = null;
    //21.3.24 String homename;
    //public static Map<Integer, Schedule> schedules = new HashMap<Integer, Schedule>();
    //List<ScheduledAction> actionsactive = new ArrayList<ScheduledAction>();
    public static Map<Integer, ServicePoint> servicepoint = new HashMap<Integer, ServicePoint>();
    //6.3.18:projection wandert wieder in die Scene.
    // MapProjection projection;
    //27.3.20 AirportConfig airport;


    //13.3.19: Für MA30
    public static boolean useSchedules = false;
    private SimpleMapProjection projection;

    //21.3.24: Now a map by icao(uppercase)
    public static Map<String, GroundNet> groundnets = new HashMap<String, GroundNet>();
    //27.12.21: Und Ersatz fuer den Airport und TrafficWorldConfig in DefaultTrafficWorld.
    // Needed eg. for runways, but not for groundnet. So it might appear the wrong location here. But ground services might
    // also need it for parking positions(?) and "followme" services.
    public static AirportConfig airport;
    // historical defaults for EDDK
    public static String airportConfigBundle = "traffic-fg";
    public static String airportConfigFullName = "flight/EDDK.xml";
    public static String TAG = "GroundServicesSystem";

    /**
     * weil er neue Pfade im Graph hinzufuegt, lauscht er auch auf GRAPH_EVENT_PATHCOMPLETED, um diese wieder aus dem
     * Graph zu entfernen. Und weil vielleicht ein Aircraft angekommen ist.
     * 13.3.19 MA30 und weil vielleicht ein Vehicle am ServicePoint angekommen ist.
     */
    public GroundServicesSystem(/*AirportConfig airport*/) {
        super(new String[]{/*VehicleComponent.TAG 13.3.19 */GroundServiceComponent.TAG}, new RequestType[]{
                        RequestRegistry.TRAFFIC_REQUEST_AIRCRAFTSERVICE,
                        RequestRegistry.TRAFFIC_REQUEST_LOADGROUNDNET},
                new EventType[]{
                        //20.3.19 EventRegistry.TRAFFIC_EVENT_PARKEDAIRCRAFTSERVICEREQUEST,
                        //20.3.19 EventRegistry.TRAFFIC_EVENT_AIRCRAFTDEPARTING,
                        TrafficEventRegistry.TRAFFIC_EVENT_VEHICLEMOVEREQUEST,
                        GraphEventRegistry.GRAPH_EVENT_PATHCOMPLETED,
                        TrafficEventRegistry.GROUNDNET_EVENT_LOADED,
                        TrafficEventRegistry.TRAFFIC_EVENT_AIRCRAFT_ARRIVE_REQUEST,
                        TrafficEventRegistry.TRAFFIC_EVENT_SPHERE_LOADED
                });
        /*this.airport = airport;
        //2.3.18 this.visualizer = visualizer;
        this.destinationlist = airport.getDestinationlist();
        this.homename = airport.getHome();*/
        //this.groundnet = groundnet;
        this.name = "GroundServicesSystem";
        // 22.5.24: Reset statics for more reliable tests
        servicepoint.clear();
        groundnets.clear();
        airport = null;
    }

    private void setGroundnetAndAirport(GroundNet groundnet, AirportConfig airport) {
        /*GroundServicesSystem.*/
        this.groundnet = groundnet;
        this.groundnets.put(airport.getAirport().getIcao(), groundnet);
        //this.airport = airport;
       //21.3.24  this.homename = airport.getHome();
    }

    @Override
    public void init(EcsGroup group) {

    }

    @Override
    final public void update(EcsEntity entity, EcsGroup group, double tpf) {
        // nicht zu haeufig versuchen. Evtl. eleganter? Per Event? Nee, Request statt GroundnetLOADED Event.
        //24.5.2020: needsupdate gibts nicht mehr, keine Elevation->kein groundnet
        /*if (groundnet.needsUpdate && AbstractSceneRunner.getInstance().getFrameCount() % 100 == 0) {
            groundnet.updateElevation();
        }*/

        if (group == null) {
            return;
        }
        //13.3.19 VehicleComponent vhc = VehicleComponent.getVehicleComponent(group.entity);
        GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(entity);
        GroundServiceComponent gsc = GroundServiceComponent.getGroundServiceComponent(entity);

        if (followme != null) {
            GraphMovingComponent gmc1 = GraphMovingComponent.getGraphMovingComponent(followme.followmecar);
            if (gmc1.pathCompleted()) {
                if (followme.isdeparting) {
                    // end of scenario
                    followme.completed = true;
                }
                if (followme.reachedaircraft && followme.reachedparkingpos == 0) {
                    followme.reachedparkingpos = (Platform.getInstance()).currentTimeMillis();
                }
                if (!followme.reachedaircraft) {
                    followme.reachedaircraft = true;
                    GraphMovingComponent.getGraphMovingComponent(followme.aircraft).setPath(followme.aircraftpath);
                    GraphEdge last = followme.aircraftpath.getLast().edge;
                    TurnExtension teardropturnatparking = groundnet.groundnetgraph.addTearDropTurn(last.to, last, true, followme.teardropturn.getLayer());
                    /*if (visualizer != null) {
                        visualizer.addLayer(groundnet.groundnetgraph, followme.teardropturn.getLayer());
                    }*/
                    SystemManager.sendEvent(new Event(GraphEventRegistry.GRAPH_EVENT_LAYERCREATED, new Payload(groundnet.groundnetgraph, new Integer(followme.teardropturn.getLayer()))));


                    GraphPath followmepath = groundnet.groundnetgraph.createPathFromGraphPosition(gmc1.getCurrentposition(), teardropturnatparking.arc.to, null, null);
                    gmc1.setPath(followmepath);
                }
            } else {
                if (followme.reachedaircraft) {

                } else {

                }
            }
            followme.checkDepart(groundnet);
            if (followme.completed) {
                /*if (visualizer != null) {
                    visualizer.removeLayer(/*groundnet.groundnetgraph,* / followme.teardropturn.getLayer());
                }*/
                SystemManager.sendEvent(new Event(GraphEventRegistry.GRAPH_EVENT_LAYERREMOVED, new Payload(groundnet.groundnetgraph, new Integer(followme.teardropturn.getLayer()))));

                followme = null;

            }
        }

        // 13.2.18: Einen Request hier im Entity update kann aber auch etwas unpassend sein.
        // 28.3.19: Ist das nicht überholt?
        if (request != null) {
            switch (request.type) {
                case 'f':
                    if (gsc.config.getType() == GroundServiceComponent.VEHICLE_FOLLOME) {
                        createFollowMe(request.aircraft, entity, request.from, request.destination);
                        request = null;
                    }
                    break;
                case 'm':
                    if (gsc.config.getType().equals(request.vehicletype)) {
                        //TODO lockEntity
                        TrafficHelper.spawnMoving(entity, request.destination.node, groundnet.groundnetgraph);
                        request = null;
                    }
                    break;
                /*ueber event case 'c':
                    if (vhc.type.equals(request.vehicletype)) {
                        spawnMoving(group.entity, request.destination.node);
                        request = null;
                    }
                    break;*/
                case 'd':
                    //depart
                    if (entity.equals(request.aircraft)) {
                        TrafficHelper.spawnMoving(entity, request.holding, groundnet.groundnetgraph);
                        //TODO lockEntity
                        request = null;
                    }
                    break;
            }
        }


        //13.3.19: MA30 Ansatz. ServiceVehicle durchgehen und Status evtl. anpassen. Polling ist aber doof. Besser Event.
        if (gsc.serviceCompleted()) {
            ServicePoint sp = gsc.sp;
            returnVehicle(entity, gsc);
            //SP kann weg weil jedes Vehicle einen eigenen hat
            sp.delete();
        }
    }

    @Override
    public void frameinit() {
       /*6.4.18 jetzt ueber use case if (automovetoggleenabled) {
            if (Input.getKeyDown(KeyCode.A)) {
                automove = !automove;
            }
        }*/
        //im update() wird das evtl. nicht aufgerufen. Und auch zu oft.
        if (AbstractSceneRunner.getInstance().getFrameCount() % 10/*0*/ == 0) {
            checkForPendingGroundnets();
        }
    }

    @Override
    public boolean processRequest(Request request) {


        if (trafficsystemdebuglog) {
            logger.debug("got request " + request.getType());
        }
        if (request.getType().equals(RequestRegistry.TRAFFIC_REQUEST_AIRCRAFTSERVICE)) {
            //13.3.19: MA30 Ansatz. Es gibt jeweils einen Request pro Service.
            EcsEntity aircraft = (EcsEntity) request.getPayloadByIndex(0);
            String servicetype = (String) request.getPayloadByIndex(1);
            int servicedurationinseconds = 5;
            if (servicetype.equals(GroundServiceComponent.VEHICLE_FUELTRUCK)) {
                servicedurationinseconds = 15;
            }
            if (servicetype.equals(GroundServiceComponent.VEHICLE_CATERING)) {
                servicedurationinseconds = 10;
            }
            spawnSingleService(aircraft, servicetype, servicedurationinseconds);
            return true;
        }
        //27.3.20: Groundnet jetzt ueber Request statt event
        //if (evt.getType().equals(EventRegistry.EVENT_LOCATIONCHANGED)) {
        if (request.getType().equals(RequestRegistry.TRAFFIC_REQUEST_LOADGROUNDNET)) {
            //7.5.19: Dann kann hier jetzt das Groundnet geladen werden.
            //TODO 27.3.20: Aber erst, wenn Terrain da ist. Sonst warten. Aber wie lange? Es ist ja gar nicht sicher,
            //dass es ueberhaupt jemals Terrain geben wird. Vorerst einfach mal per Counter
            //16.5.20: Groundnet sollte in der Lage sein zum Nachladen wie in FG.
            // 1.3.24: use temp solution for making sure we have elevation.
            // Otherwise elevation provider might fall back to a default (eg.68), especially in webgl.
            // causing bluebird (and other) be too low
            String icao = (String) request.getPayload().get("icao");
            if (/*24.5.20 request.declined > 10*/hasTerrainTempSolution(icao)) {
                // 16.5.20: Fuer EDDK bleibts erstmal mal beim alten.
                if (icao.equals("EDDK") && false) {
                    //24.5.20: EDDK einfach eintragen. Groundnet kommt spaeter als pending groundnet. Nee, kommt aus init wegen der config Daten,
                    // die es hier nicht so gibt. Oder doch? Nee zumindest zZ nicht. Aber groundnetXML hier lesen, damit pending geht.

                    /*27.12.21 steht doch oben auf false
                    String groundnetdefinition = GroundServicesSystem.getBundleForIcao("EDDK").getContentAsString();
                    DefaultTrafficWorld.getInstance().getAirport("EDDK").getAirport().setGroundNetXml(groundnetdefinition);*/

                    /* 24.5.20        projection = (SimpleMapProjection) request.getPayloadByIndex(1);
                    if (checkWakeup(icao, projection)) {
                        // Dafuer wird es sicherlich Interessenten geben.
                        SystemManager.sendEvent(new Event(EventRegistry.GROUNDNET_EVENT_LOADED, new Payload(groundnet, DefaultTrafficWorld.getInstance().getAirport(icao)/*27.3.20 nearestairport* /)));
                    }
                    if (groundnet != null) {
                        //Event reicht nicht fuer alle Zwecke. 23.5.2020 jetzt mal generischer setzen
                        //DefaultTrafficWorld.setLoadedGraph(groundnet, groundnet.groundnetgraph);
                        //airport ist schon drin DefaultTrafficWorld.getInstance().addAirport(airport);
                        DefaultTrafficWorld.getInstance().addGroundNet("EDDK", groundnet);
                    } else {
                        logger.debug("no groundnet loaded");
                    }*/
                } else {
                    /* 23.12.23 replaced with code from AirportDataProviderMock
                    AbstractSceneRunner.getInstance().getHttpClient().sendHttpRequest("http://localhost/airport/icao=" + icao, "POST", new String[]{}, (response) -> {
                        logger.debug("HTTP returned airport. status=" + response.getStatus() + ", response=" + response.getContentAsString());
                        if (response.getStatus() == 0) {
                            Airport airport = JsonUtil.toAirport(response.getContentAsString());
                            // das laden selber geht speater offenbar ueber checkForPendingGroundnets() airports
                            // 29.11.21: DefaultTrafficWorld ist in dieser Form doch deprecated. Darum Fehlen tolerieren.
                            //27.12.21 if (DefaultTrafficWorld.getInstance() != null) {
                            /*27.12.21DefaultTrafficWorld.getInstance().* /
                            addAirport(airport);
                            //}
                        }
                    });*/

                    // code from AirportDataProviderMock
                    logger.debug("Adding airport with groundnet (fka AirportDataProviderMock)");
                    // Wegen dependencies Daten aus Bundle
                    BundleData groundnetData = GroundServicesSystem.getGroundnetFromBundleForIcao(icao);
                    if (groundnetData == null) {
                        throw new RuntimeException("no bundle groundnetdefinition found for " + icao);
                    }
                    String groundnetdefinition;
                    try {
                        groundnetdefinition = groundnetData.getContentAsString();
                    } catch (CharsetException e) {
                        // TODO improved eror handling
                        throw new RuntimeException(e);
                    }
                    String groundnetXml = groundnetdefinition.replace("\n", "");
                    AirportConfig airportConfig = AirportConfig.buildFromAirportConfig(airportConfigBundle, airportConfigFullName, icao, groundnetXml);
                    if (airportConfig == null) {
                        //keine config daten
                        //15.3.24 TODO what? airport = new AirportConfig(ap);
                    }
                    GroundServicesSystem.airport = airportConfig;
                }
                //true because request was processed. No info about success.
                return true;

            }
            logger.debug("Aborting TRAFFIC_REQUEST_LOADGROUNDNET due to missing terrain for " + icao);
        }
        return false;
    }

    @Override
    public void process(Event evt) {
        if (trafficsystemdebuglog) {
            logger.debug("got event " + evt);
        }
       /* if (evt.getType().equals(EventRegistry.TRAFFIC_EVENT_PARKEDAIRCRAFTSERVICEREQUEST)) {
            //MA30: deprecated, wird nicht mehr verwendet (bzw. soll).
            logger.warn("Deprecated event TRAFFIC_EVENT_PARKEDAIRCRAFTSERVICEREQUEST");
            EcsEntity aircraft = ((TrafficRequest) evt.getPayload()).aircraft;
            VehicleComponent vc = VehicleComponent.getVehicleComponent(aircraft);
            GroundServiceAircraftConfig aircraftconfig = TrafficWorldConfig.getInstance().getAircraftConfiguration(vc.config.getModelType());
            if (aircraftconfig != null) {
                GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(aircraft);
                ServicePoint sp = new ServicePoint(groundnet, aircraft, aircraft.scenenode.getTransform().getPosition(), null, gmc.getCurrentposition().getDirection(), aircraftconfig);
                servicepoint.put(sp.id, sp);
                // Der Returnpath nach home steht jetzt schon fest. Der Approach aber nicht, denn der hängt vom Vehicle ab.
                // 15.8.17: Ist aber inkonsisten, wenn der jetzt schon feststeht. Pfade entstehen sonst immer
                // erst unmittelbar vor benutzen. Sonst wird er nicht freigegeben.
                Schedule schedule = new Schedule(sp, groundnet);
                schedule.addAction(new VehicleOrderAction(schedule, GroundServiceComponent.VEHICLE_CATERING, sp.doorEdge.from));
               // schedule.addAction(new VehicleServiceAction(schedule, GroundServiceComponent.cateringduration));
                schedule.addAction(new VehicleReturnAction(schedule, true, sp, true));
                sp.cateringschedule = schedule;
                TrafficSystem.getInstance().schedules.put(schedule.id, schedule);
                // weiterer Schedule fuer fule, damit der parallel läuft
                schedule = new Schedule(sp, groundnet);
                schedule.addAction(new VehicleOrderAction(schedule, GroundServiceComponent.VEHICLE_FUELTRUCK, sp.wingedge.to));
               // schedule.addAction(new VehicleServiceAction(schedule, GroundServiceComponent.fuelingduration));
                schedule.addAction(new VehicleReturnAction(schedule, false, sp , false));
                TrafficSystem.getInstance().schedules.put(schedule.id, schedule);
                sp.fuelschedule = schedule;

            } else {
                logger.error("No configuration found for aircraft type " + vc.config.getModelType());
            }
        }*/

        if (evt.getType().equals(TrafficEventRegistry.TRAFFIC_EVENT_AIRCRAFT_ARRIVE_REQUEST)) {
            // getFirst launch aircraft. Launch ist doof wegen async load. Ich setze einfach ein vorhandenes um.
            Parking destination = ((TrafficRequest) evt.getPayloadByIndex(0)).destination;
            GraphEdge launchedge = groundnet.groundnetgraph.getBaseGraph().findEdgeByName("89-90");
            //bischen vor der Node positionieren und dann Relocation erlauben.
            GraphPosition start = new GraphPosition(launchedge, launchedge.getLength() - 60, true);
            //SceneVehicle vconf = DefaultTrafficWorld.airport.getVehicle(i);
            //VehicleConfig config = TrafficWorldConfig.getInstance().getVehicleConfig("738");
            //GroundServicesScene.launchVehicle(config, groundnet.groundnetgraph, start, null/*TeleportComponent.getTeleportComponent(avatar.avatarE)*/, Scene.getCurrent().getWorld(), null);
            EcsEntity aircraft = findAvailableVehicle(VehicleComponent.VEHICLE_AIRCRAFT, null);
            spawnMovingAircraft(aircraft, destination, start);

        }

        if (evt.getType().equals(TrafficEventRegistry.TRAFFIC_EVENT_VEHICLEMOVEREQUEST)) {
            // Ein Vehicle eines bestimmten Typs an ein Ziel schicken.
            TrafficRequest request = (TrafficRequest) evt.getPayloadByIndex(0);
            //Schedule schedule = new Schedule(null, groundnet);
            //schedule.addAction(new VehicleOrderAction(schedule, request.vehicletype, request.destination.node));
            //TrafficSystem.getInstance().schedules.put(schedule.id, schedule);
            moveVehicle(request.vehicletype, request.destination.node);
        }
        if (evt.getType().equals(GraphEventRegistry.GRAPH_EVENT_PATHCOMPLETED)) {
            //Graph graph = (Graph) ((Object[]) evt.getPayload())[0];
            GraphPath path = (GraphPath) evt.getPayloadByIndex(1);
            EcsEntity vehicle = (EcsEntity) evt.getPayloadByIndex(2);
            // sollten hier nur die entfernt werden, die hier angelegt wurden?
            // 17.4.18:Laeuft bis jetzt aber ganz zufriedenstellend.
            // 8.5.19: Weil das auch allgemein ein TrafficGraph sein kann, macht derEvenetsender das jetzt.
            //groundnet.groundnetgraph.removeLayer(path.layer);
            VehicleComponent vhc = VehicleComponent.getVehicleComponent(vehicle);
            // wenn es ein Aircraft ist und es jetzt auf einer Parkpos steht (und nicht gerade starten will!), Service anfragen. (10m Toleranz)
            GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(vehicle);
            // TODO 14.4.20: im PATHCOMPleted kann ein anderes System schon einen neuen graph/path gesetzt haben. Das ist iregdnwie unsauber? Der completed path muss aus dem Event kommen.
            Vector3 position = gmc.getCurrentposition().get3DPosition();
            if (groundnet != null) {
                //Der PATHCOMPLETED kann aus irgendeinem TrafficGraph kommen
                Parking closestparking = groundnet.getParkPosNearCoordinates(position);
                logger.debug("currentpos=" + position + ",closestparking=" + closestparking);
                //21.11.18 vc isType expected to exist, even for navigator
                if (vhc.config.getType().equals(VehicleComponent.VEHICLE_AIRCRAFT) && Vector3.getDistance(position, closestparking.node.getLocation()) < 10) {
                    requestService(vehicle);
                }
                GroundServiceComponent gsc = GroundServiceComponent.getGroundServiceComponent(vehicle);
                // 13.3.19 MA30 und weil vielleicht ein Vehicle am ServicePoint angekommen ist.
                if (gsc != null) {
                    //gsc makes sure this isn't the "vehicle returned home" event.
                    if (gsc.isApproaching()) {
                        gsc.startService();
                    }
                    if (gsc.sp == null && vehicle.isLockedBy(this)) {
                        //vehicle appears to be returned
                        vehicle.release(this);
                    }
                }
            }
        }
        if (evt.getType().equals(TrafficEventRegistry.GROUNDNET_EVENT_LOADED)) {
            GroundNet gn = (GroundNet) evt.getPayloadByIndex(0);
            setGroundnetAndAirport(gn, (AirportConfig) evt.getPayloadByIndex(1));
            // hier die Vehicles startren ist z.Z. nicht moeglich wegen Dependencies
            // 29.10.21 The request for loading vehicles it sent by the sender of the GROUNDNET_EVENT_LOADED event.
        }


    }

    @Override
    public String getTag() {
        return TAG;
    }

    private void spawnSingleService(EcsEntity aircraft, String servicetype, int servicedurationinseconds) {
        VehicleComponent vc = VehicleComponent.getVehicleComponent(aircraft);

        /*20.11.23GroundServiceAircraftConfig*/
        VehicleDefinition aircraftconfig = /*20.11.23TrafficWorldConfig.getInstance().*/getAircraftConfigurationByType(vc.config.getModelType());
        if (aircraftconfig != null) {
            GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(aircraft);
            ServicePoint sp = null;
            try {
                //TrafficWorldConfig tw = TrafficWorldConfig.readDefault();
                //28.11.23: vehicledefinitions now come from TrafficSystem
                sp = new ServicePoint(groundnet, aircraft, aircraft.scenenode.getTransform().getPosition(), null, gmc.getCurrentposition().getDirection(), aircraftconfig, TrafficSystem.knownVehicles);
                launchServiceVehicle(servicetype, groundnet, sp, servicedurationinseconds);
                //schedule.addAction(new VehicleServiceAction(schedule, GroundServicesSystem.fuelingduration));
                //schedule.addAction(new VehicleReturnAction(schedule, false, sp /*sp.getWingReturnPath(true)*/, false));
            } catch (NoElevationException e) {
                e.printStackTrace();
            }

        } else {
            logger.error("No configuration found for aircraft type " + vc.config.getModelType());
        }
    }


    /**
     * Der Path wird so erstellt, das das aircraft Heading konform ins Parking faehrt.
     * Optional mit einer start Position. Das Aircraft wird dann direkt dorthin gesetzt.
     * 24.4.18: Mit relocation um turnloop zu vermeiden.
     * 16.5.18: Das mit der startposition kann tricky sein, weil dann die Edge der startposition die erste im (smoothed) path sein muss. Ich lass das mal.
     * Returns true when spawn was successful
     */
    private boolean spawnMovingAircraft(EcsEntity entity, Parking parking, GraphPosition startposition) {

        GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(entity);
       /* if (startposition != null) {
            gmc.setCurrentposition(startposition);
        }*/
        GraphEdge approachedge = parking.getApproach();
        List<GraphEdge> voidedges = parking.node.getEdgesExcept(approachedge);
        DefaultGraphWeightProvider graphWeightProvider = new DefaultGraphWeightProvider(groundnet.groundnetgraph.getBaseGraph(), voidedges);
        VehicleComponent vhc = VehicleComponent.getVehicleComponent(entity);

        GraphPath path = groundnet.groundnetgraph.createPathFromGraphPosition(gmc.getCurrentposition(), parking.node, graphWeightProvider, true, true, vhc.config);
        if (path == null) {
            return false;
        }
        //path.startposition = startposition;
        SystemManager.sendEvent(new Event(GraphEventRegistry.GRAPH_EVENT_PATHCREATED, new Payload(groundnet.groundnetgraph, path)));
        gmc.setPath(path);
        return true;
    }

    /**
     * Das aircraft muss sich schon mit verbindenden Edges im Graph befinden.
     * Skizze 32
     * Ein Request sollte eigentlich als Event reinkommen. Aber seis erstmal drum.
     */

    /*24.7.17 public void requestFollowMe(EcsEntity aircraft, GraphNode from, Parking destination) {
        // Das followmecar sucht er sich spaeter
        request = new TrafficRequest(aircraft, from, destination);

    }*/

    /**
     * einfach ein Standortwechsels eines Vehicles
     *
     * @param vehicletype
     * @param destination
     */
    public void requestMove(String vehicletype, Parking destination) {
        // Das vehicle sucht er sich spaeter
        request = new TrafficRequest(vehicletype, destination);

    }

    /**
     * Fuer ein Aircraft Service Schedule. Wenn es kein markiertes gibt, ein kurz vor einem Parking erstellen, da einfahren lassen und Service
     * beginnen. Danach muss es wieder verschwinden. Das ist analog zum FG UseCase "Service nearby"
     * <p>
     * Zwei Methoden, weils auch zwei Events sind und weil es sauberer ist.
     *
     * @param markedaircraft
     */
    public static void requestService(EcsEntity markedaircraft) {
        Request request;
        request = new Request(RequestRegistry.TRAFFIC_REQUEST_AIRCRAFTSERVICE, new Payload(markedaircraft, GroundServiceComponent.VEHICLE_FUELTRUCK));
        SystemManager.putRequest(request);
        request = new Request(RequestRegistry.TRAFFIC_REQUEST_AIRCRAFTSERVICE, new Payload(markedaircraft, GroundServiceComponent.VEHICLE_CATERING));
        SystemManager.putRequest(request);
    }

    /*24.7.17  public void requestCatering(EcsEntity aircraft, ServicePoint servicepoint) {
        // Das followmecar sucht er sich spaeter
        request = new TrafficRequest(aircraft, servicepoint);

    }*/

    /**
     * Request for departing a specific aircraft. 
     * Evtl. wird der Request delayed wegen aktivem Servicepoint.
     *
     * @param aircraft
     */
   /*per event und schedule  public void requestDepart(ArrivedAircraft aircraft, GraphNode positiononrunway) {
        request = new TrafficRequest(aircraft, positiononrunway);
    }*/

    /**
     * Das aircraft muss sich schon mit verbindenden Edges im Graph befinden.
     * Skizze 32
     */
    private void createFollowMe(EcsEntity aircraft, EcsEntity followmecar, GraphNode from, Parking destination) {

        // 1. Pfad fuer das Aircraft zum Gate bestimmen.
        GraphPath aircraftpath = groundnet.findFollowMePath(from, destination);
        /*if (visualizer != null) {
            visualizer.addLayer(groundnet.groundnetgraph, aircraftpath.layer);
        }*/
        SystemManager.sendEvent(new Event(GraphEventRegistry.GRAPH_EVENT_LAYERCREATED, new Payload(groundnet.groundnetgraph, new Integer(aircraftpath.layer))));

        // 2. Create Followme approach path to Aircraft 
        TurnExtension teardropturn = groundnet.createFollowMeVehicleApproach(aircraftpath);
        if (teardropturn == null) {
            logger.error("failed");
            return;
        }
        /*if (visualizer != null) {
            visualizer.addLayer(groundnet.groundnetgraph, teardropturn.getLayer());
        }*/
        SystemManager.sendEvent(new Event(GraphEventRegistry.GRAPH_EVENT_LAYERCREATED, new Payload(groundnet.groundnetgraph, new Integer(teardropturn.getLayer()))));


        GraphPath followmepath = groundnet.groundnetgraph.createPathFromGraphPosition(GraphMovingComponent.getGraphMovingComponent(followmecar).getCurrentposition(), teardropturn.arc.to, null, null);
        /*if (visualizer != null) {
            visualizer.addLayer(groundnet.groundnetgraph, followmepath.layer);
        }*/
        SystemManager.sendEvent(new Event(GraphEventRegistry.GRAPH_EVENT_LAYERCREATED, new Payload(groundnet.groundnetgraph, new Integer(followmepath.layer))));

        // 3. move followme vehicle to aircraft
        GraphMovingComponent.getGraphMovingComponent(followmecar).setPath(followmepath);

        // move aircraft
        //((GraphMovingComponent) aircraft.getComponent(GraphMovingComponent.TAG)).setPath(path);
        followme = new FollowMe(aircraft, aircraftpath, followmecar, followmepath, teardropturn);
    }


    /**
     * Ein AA kann irgendwo stehen und hat damit keine Graph Position. Dann bekommt es auch keine GraphComponents.
     * 13.2.18: Aber solche Aircrafts sollen sich auch mal wieder wegbewegen. Und auch wenn sie "irgendwo" stehen.
     * Von dort können sie per Helper edges ins groundnet geholt werden. Also doch eine GraphMovingComponent.
     * 3.3.18: AA fallen ja nicht vom Himmel, sondrn kommen per Graph auf eine Runway. Dann
     * kann man auch TrafficSystem.buildVehicleOnGraph() verwenden. Wobei es die Entity bei einem AA
     * schon geben muesste. Darum deprecated.
     * 15.11.17
     */
    @Deprecated
    public EcsEntity buildArrivedAircraft(SceneNode node, Parking parking) {

        EcsEntity e = new EcsEntity(node);
        // die Position ist evtl.  aber nicht definiert
        GraphPosition position = null;
        if (parking != null) {
            position = GraphPosition.buildPositionAtNode(parking.getApproach(), parking.node, true);
            if (position == null) {
                //TODO mal als error weil doof
                //logger.error("frickeling position");
                //position=new GraphPosition(parking.node.getEdge(0));
            }
        }
        //Das verhunzt die ORientation.
        //e.addComponent(new GraphMovingComponent(node.getTransform(),null,position));
        //Velocity braucht er dann auch fuer MovingSystem
        e.addComponent(new VelocityComponent());
        return e;
    }

    /**
     * Only return idle vehicles because moving ones cannot relocated (due to unknwon layer)
     * Optionally specific service vehicle (model_type)
     * 26.3.19: Verwendet jetzt algemeingültiger "lockEntity".
     *
     * @return
     */
    public static EcsEntity findAvailableVehicle(String vehicletype, String modeltype) {
        List<EcsEntity> vehicles = EcsHelper.findEntitiesByComponent(VehicleComponent.TAG);
        for (EcsEntity e : vehicles) {
            VehicleComponent vhc = VehicleComponent.getVehicleComponent(e);
            GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(e);
            GroundServiceComponent gsc = GroundServiceComponent.getGroundServiceComponent(e);
            if (vhc.config.getType().equals(vehicletype) && !e.isLocked()/*gsc.isIdle() && gmc.isMoving() == null*/) {
                if (modeltype == null || gsc.config.getModelType().equals(modeltype)) {
                    return e;
                }
            }
        }
        return null;
    }


    /*public MapProjection getProjection() {
        return projection;
    }*/

    /**
     * 3.12.18: Nicht core, sondern data.
     *
     * @param icao
     * @return
     */
    public static BundleData getGroundnetFromBundleForIcao(String icao) {
        //BundleData data = BundleRegistry.getBundle("data-old").getResource(new BundleResource("flusi/" + icao + ".groundnet.xml"));
        BundleData data = BundleRegistry.getBundle("traffic-fg").getResource(new BundleResource("flight/" + icao + ".groundnet.xml"));
        return data;
    }

    /**
     * A currently moving vehicle cannot be relocated for now because it most likely runs on a temporary unknown layer (-> "no path found");
     * If no vehicle isType available the launch isType aborted.
     * <p>
     * Analog FG.
     */
    private void launchServiceVehicle(String modeltype, GroundNet groundnet, ServicePoint servicepoint, int servicedurationinseconds) {
        EcsEntity vehicle = GroundServicesSystem.findAvailableVehicle(VehicleComponent.VEHICLE_CAR, modeltype);
        if (vehicle == null) {
            logger.warn("no " + modeltype + " vehicle available");
            servicepoint.delete();
            return;
        }

        VehicleComponent vhc = VehicleComponent.getVehicleComponent(vehicle);
        GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(vehicle);
        GroundServiceComponent gsc = GroundServiceComponent.getGroundServiceComponent(vehicle);

        GraphNode destination = gsc.setStateApproaching(servicepoint, modeltype, servicedurationinseconds);
        GraphPosition start = gmc.getCurrentposition();
        GraphPath approach;
        approach = servicepoint.getApproach(start, destination, true);

        if (approach != null) {
            SystemManager.sendEvent(new Event(GraphEventRegistry.GRAPH_EVENT_PATHCREATED, new Payload(gmc.getGraph(), approach)));
        } else {
            logger.error("no approach found to " + destination);
            gsc.reset();
            return;
        }
        logger.debug("set approachpath:" + approach);
        gmc.setPath(approach);
        if (!vehicle.lockEntity(this)) {
            logger.error("Lock vehicle failed");
        }
    }

    /**
     * Ein bestimmtes Vehicle eines Types zu einer Destination fahren.
     * Hat nichts mit GroundService zu tun.
     * <p>
     * 20.3.19
     *
     * @param vehicletype
     * @param destination
     */
    private void moveVehicle(String vehicletype, GraphNode destination) {
        EcsEntity vehicle = GroundServicesSystem.findAvailableVehicle(vehicletype, null);
        if (vehicle == null) {
            logger.warn("no vehicle available");
            return;
        }

        VehicleComponent vhc = VehicleComponent.getVehicleComponent(vehicle);
        GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(vehicle);

        GraphPosition start = gmc.getCurrentposition();
        GraphPath path = groundnet.groundnetgraph.createPathFromGraphPosition(start, destination, null, vhc.config);

        if (path != null) {
            SystemManager.sendEvent(new Event(GraphEventRegistry.GRAPH_EVENT_PATHCREATED, new Payload(gmc.getGraph(), path)));
        } else {
            return;
        }
        gmc.setPath(path);
    }

    /**
     * Analog FG.
     */
    public void returnVehicle(EcsEntity vehicle, GroundServiceComponent gsc) {
        logger.debug("return vehicle from service point. fordoor=" + gsc.fordoor);

        GraphPath returnpath;//= path;//schedule.sp.getReturnPath();
        if (gsc.fordoor) {
            returnpath = gsc.sp.getDoorReturnPath(true);
        } else {
            returnpath = gsc.sp.getWingReturnPath(true);
        }


        if (returnpath != null) {
            //schedule.trafficsystem.visualizer.visualizePath(returnpath);
        } else {
            logger.error("no returnpath found");
            //TODO state?
            return;
        }
        GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(vehicle);
        gmc.setPath(returnpath);
        gsc.setStateIdle();
    }

    /**
     * Analog FG.
     * Wenn ein Airport "nah" ist, das Groundnet dazu laden. Den Rest macht der/ein GROUNDNET_EVENT_LOADED Receiver.
     * Laden der Vehicle macht der Aufrufer im update() nach gelaufenenem Checkwakeup.
     * <p>
     * Laden von Terraintiles geschieht hier nicht.
     * <p>
     * Ob der Platz hier so gut ist muss sich noch zeigen.
     * 7.5.19 Da es speziell fuers Groundnet ist, passt es doch besser in GroundServicesSystem.
     *
     * <p>
     * In 3D Scenes ist projection null.
     * 4.1.19: Duplicate zu 3D. 7.5.19: Wohl nicht mehr duplicate.
     * 7.5.19: Heisst nur noch so wegen der Herkunft aus Analogie zu FG.
     * 24.3.20: Das muesste mal auf ein Request zum Laden eines Groundnet umgestellt werden. Bzw erweitert,
     * denn ein Groundnet bei Annäherung zu laden ist ja sinnvoll. Das passiert z.Z. auch beim init der TravelScene so.
     * Groundnet braucht aber Terrain! Ob nicht besser erst der Airport(mit Terrain) bei Annäherung(oder besser Request?) und dann Groundnet
     * geladen wird? (TRAFFIC_REQUEST_LOADAIRPORT).
     */
    private boolean checkWakeup(String icao/*27.3.20 AirportConfig airport*/, MapProjection projection) {
        //27.12.21 AirportConfig airport = DefaultTrafficWorld.getInstance().getAirport(icao);
        if (groundnet == null && airport != null/*getAirportInfo()*/) {
            // Kein groundnet vorhanden. Gibt es einen Airport in der Naehe?
            //AirportConfig nearestairport  = getNearestAirport();

            if (true/*!altinfo.needsupdate*/) {
                //logger.debug("Loading groundnet");

                groundnet = loadGroundNetForAirport(projection, airport);
                return true;
            }
            //logger.warn("ignoring airport. No elevation info");
        }
        return false;
    }

    /**
     * temporary helper?
     * 24.5.20
     *
     * @param projection
     * @param airport
     * @return
     */
    private static GroundNet loadGroundNetForAirport(MapProjection projection, AirportConfig airport) {
        if (projection == null) {
            // Auch in 3D Darstellung sollte Groundnet zumindest vorerst analog zu FG eine Projection verwenden.
            // 8.6.20: Elevation 0 ist hier schon fragwürdig.
            projection = new SimpleMapProjection(/*SGGeod.fromLatLon*/(airport.getCenter()));
        }
        try {
            GroundNet groundnet = loadGroundnet(airport, projection);
            //loadGroundnet(airport.icao, airport.getHome(), projection);

            // an den groundnet graph einen Weg zum Holding einrichten
            if (airport.getRunways() != null) {
                for (Runway runway : airport.getRunways()) {
                    groundnet.createRunwayEntry(runway/*.entrypoint, runway.holdingpoint, runway.name*/);
                }
            }
            return groundnet;
        } catch (NoElevationException e) {
            //warn weil es halt mal vorkommt
            logger.warn("loadGroundNetForAirport failed:" + e.getMessage());
        }
        return null;
    }

    /**
     * Auch in 3D Darstellung sollte Groundnet zumindest vorerst analog zu FG eine Projection verwenden.
     * 31.3.20: Jetzt static wegen Test.
     * 24.05.2020: Wenn Airport XML hat, dann daraus, sonst aus Bundle.
     */
    public static GroundNet loadGroundnet(AirportConfig airport,/*String icao, String homename,*/ MapProjection projection) throws NoElevationException {

        String groundnetdefinition;

        if (airport.getAirport() != null && airport.getAirport().getGroundNetXml() != null) {
            groundnetdefinition = airport.getAirport().getGroundNetXml();
        } else {
            try {
                groundnetdefinition = GroundServicesSystem.getGroundnetFromBundleForIcao(airport.getAirport().getIcao()).getContentAsString();
            } catch (CharsetException e) {
                // TODO improved eror handling
                throw new RuntimeException(e);
            }
        }

        XmlDocument groundnetxml;
        try {
            groundnetxml = XmlDocument.buildXmlDocument(groundnetdefinition);
        } catch (XmlException e) {
            throw new RuntimeException(e);
        }
        // projection is icao specific? thus cannot be a common?
        SphereProjections sphereProjections = TrafficHelper.getProjectionByDataprovider(projection);
        //sphereProjections.projection= (SimpleMapProjection) projection;
        //GraphProjection backProjection = new GraphProjectionFlight3D(projection);
        GroundNet groundnet = new GroundNet(projection, sphereProjections.backProjection, groundnetxml, airport.getHome()/*, airport*/);
        groundnet.groundnetgraph.icao = airport.getAirport().getIcao();
        // set a name for better logging/debugging just by a simple convention
        groundnet.groundnetgraph.getBaseGraph().setName("groundnet." + StringUtils.toUpperCase(groundnet.groundnetgraph.icao));
        return groundnet;
    }

    /**
     * Nachfolger von checkWakeup.
     * 21.10.21:Laedt letztendlich das Groundnet
     * 24.5.2020
     */
    private void checkForPendingGroundnets() {
        //27.12.21 DefaultTrafficWorld trafficWorld = DefaultTrafficWorld.getInstance();

        // 20.10.21 Erstmal nicht mehr, solange es ja keinen echten Flight gibt. Ob das dann aber so bleiben sollte? Mal sehn.
        // Nee, muss bleiben fuer das initiale Laden von EDDK
        if (true) {
            //return;
        }

        /*27.12.21 if (trafficWorld == null) {
            //das nervt bei Desdprf etc. logger.debug("Skipping checkForPendingGroundnets");
            return;
        }*/

        //for (AirportConfig ac : trafficWorld.getAirports()) {
        AirportConfig ac = GroundServicesSystem.airport;
        if (ac != null && ac.getAirport().getGroundNetXml() != null) {
            GroundNet groundNet = loadGroundNetForAirport(null, ac);
            if (groundNet != null) {
                // 27.12.21: Vorher TrafficContext setzen, sonst geht der vehicle launch nicht
                TrafficSystem.trafficContext = new AirportTrafficContext(groundNet, ac);
                SystemManager.sendEvent(new Event(TrafficEventRegistry.GROUNDNET_EVENT_LOADED, new Payload(groundNet, airport/*27.12.21DefaultTrafficWorld.getInstance().getAirport(ac.getAirport().getIcao())*//*27.3.20 nearestairport*/)));
                //mark as loaded
                ac.getAirport().setGroundNetXml(null);
                //27.12.21trafficWorld.addGroundNet(ac.getAirport().getIcao(), groundNet);
                GroundServicesSystem.groundnets.put(ac.getAirport().getIcao(), groundNet);
                // 20.3.24: inform other about loaded trafficgraph. TrafficSystem will then send a request TRAFFIC_REQUEST_LOADVEHICLES.
                String cluster = TrafficGraph.ROAD;
                SystemManager.sendEvent(new Event(TrafficEventRegistry.TRAFFIC_EVENT_GRAPHLOADED, new Payload(groundNet.groundnetgraph, cluster)));
                // 30.10.21: Vehicles cannot be loaded immediately, because they need to wait for example for elevation.
                //TrafficGraph trafficGraph = groundNet.groundnetgraph;
                //SystemManager.putRequest(RequestRegistry.buildLoadVehicles(trafficGraph));

            } else {
                logger.warn("pending groundnet not loaded");
            }
        }
        //}

    }

    /**
     * Ein Aircraft irgendwo erstellen, in einem Parking da einfahren lassen und Service
     * beginnen. Danach muss es wieder verschwinden. Das ist analog zum FG UseCase "Service nearby"
     */
    public static void requestArrivingAircraft(Parking parking) {
        TrafficEvent evt;
        evt = new TrafficEvent(TrafficEventRegistry.TRAFFIC_EVENT_AIRCRAFT_ARRIVE_REQUEST, new TrafficRequest(null, null, parking));
        SystemManager.sendEvent(evt);
    }

    /**
     * einfach ein Standortwechsels eines Vehicles. Ein Followme nach C_7 schicken.
     * Erzeugt auch nur das Event
     */
    public static void requestVehicleMove(/*TrafficWorld2D*/Object gsw) {
        TrafficEvent evt = new TrafficEvent(TrafficEventRegistry.TRAFFIC_EVENT_VEHICLEMOVEREQUEST, new TrafficRequest(GroundServiceComponent.VEHICLE_FOLLOME, GroundServicesSystem.groundnets.get("EDDK").getParkPos("C_7")));
        SystemManager.sendEvent(evt);
    }

    public static VehicleDefinition getAircraftConfigurationByType(String type) {
        // 24.11.23: Replaced former AircraftConfigProvider by the more generic VehicleConfigDataProvider
        VehicleDefinition vehicleDefinition = TrafficHelper.getVehicleConfigByDataprovider(null, type);

        if (vehicleDefinition == null) {
            // da genaue - vor allem Doorangaben - schwer zu bekommen sind, nehm ich die 738 als Default. Das passt werstmal fuer alle kleineren, solange kein Vehicle exakt anfährt.
            vehicleDefinition = TrafficHelper.getVehicleConfigByDataprovider(null, "738");
        }
        return vehicleDefinition;
    }

    /**
     * only temporary solution! TODO Probing complete groundnet will be more reliable.
     */
    private boolean hasTerrainTempSolution(String icao) {
        // Difficult to find a generic 2D/3D solution.
        // 16.5.24 Existing ScenerySystem can no longer be used to decide. It also exists in 2D now. Looking for terrainbuilder
        // is surely a better solution.
        //if (SystemManager.findSystem(ScenerySystem.TAG) == null) {
        if (!((ScenerySystem)SystemManager.findSystem(ScenerySystem.TAG)).hasTerrainBuilder()) {
            return true;
        }
        int eddks = SceneNode.findByName("Terrain/e000n50/e007n50/EDDK.gltf").size();
        logger.debug("No EDDK scene nodes, so no terrain yet");
        return eddks > 0;
    }

    /**
     * 21.3.24:just a quick hack after groundneteddk no longer is hardcoded.
     * @return
     */
    public static GroundNet groundnetEDDK(){
        return groundnets.get("EDDK");
    }
}

class FollowMe {
    EcsEntity aircraft, followmecar;
    GraphPath aircraftpath, followmepath;
    boolean reachedaircraft = false;
    TurnExtension teardropturn;
    public long reachedparkingpos = 0;
    boolean isdeparting = false;
    public boolean completed;

    FollowMe(EcsEntity aircraft, GraphPath aircraftpath, EcsEntity followmecar, GraphPath followmepath, TurnExtension teardropturn) {
        this.aircraft = aircraft;
        this.followmecar = followmecar;
        this.aircraftpath = aircraftpath;
        this.followmepath = followmepath;
        this.teardropturn = teardropturn;
    }

    public void checkDepart(GroundNet groundnet) {
        if (!isdeparting && reachedparkingpos != 0 && (Platform.getInstance()).currentTimeMillis() - reachedparkingpos > 3000) {
            GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(followmecar);
            GraphPath departpath = groundnet.groundnetgraph.createPathFromGraphPosition(gmc.getCurrentposition(), groundnet.getFollowmeHome(), null, null);
            gmc.setPath(departpath);
            isdeparting = true;
        }
    }
}


