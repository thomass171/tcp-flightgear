package de.yard.threed.trafficfg.flight;


import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.Payload;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.BaseRequestRegistry;
import de.yard.threed.engine.ecs.DefaultEcsSystem;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsGroup;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.ecs.VelocityComponent;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.RequestType;
import de.yard.threed.graph.Graph;
import de.yard.threed.graph.GraphEventRegistry;
import de.yard.threed.graph.GraphMovingComponent;
import de.yard.threed.graph.GraphNode;
import de.yard.threed.graph.GraphPath;
import de.yard.threed.graph.GraphPosition;
import de.yard.threed.traffic.Destination;
import de.yard.threed.trafficcore.EllipsoidCalculations;
import de.yard.threed.traffic.FlightRouteBuilder;
import de.yard.threed.traffic.OrbitToOrbitRouteBuilder;
import de.yard.threed.traffic.RequestRegistry;
import de.yard.threed.traffic.SolarSystem;
import de.yard.threed.traffic.TrafficEventRegistry;
import de.yard.threed.traffic.TrafficHelper;
import de.yard.threed.traffic.Travelplan;
import de.yard.threed.traffic.VehicleComponent;
import de.yard.threed.traffic.flight.FlightRouteGraph;
import de.yard.threed.trafficcore.model.Runway;
import de.yard.threed.trafficfg.TrafficRequest;

/**
 * On request moves an aircraft from airport to airport by creating one or more moving graphs.
 * Moving itself is done by GraphMovingSystem as usual.
 * In 2D only a local pattern('Platzrunde').
 * <p>
 * Was intended as TravelSystem.
 * <p>
 * Created by thomass on 26.03.19.
 */
public class FlightSystem extends DefaultEcsSystem {
    private static Log logger = Platform.getInstance().getLog(FlightSystem.class);
    public static String TAG = "FlightSystem";

    /**
     *
     */
    public FlightSystem() {
        super(new String[]{VehicleComponent.TAG},
                new RequestType[]{RequestRegistry.TRAFFIC_REQUEST_AIRCRAFTDEPARTING},
                new EventType[]{GraphEventRegistry.GRAPH_EVENT_PATHCOMPLETED
                });
        this.name = "FlightSystem";
    }

    @Override
    public void init(EcsGroup group) {

    }

    @Override
    final public void update(EcsEntity entity, EcsGroup group, double tpf) {
        if (group == null) {
            return;
        }
        VehicleComponent vhc = VehicleComponent.getVehicleComponent(entity);
        GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(entity);

        // check for completed movment
        //13.5.20 Das mit dem FlightRouteBuilder ist doch nur eine Idee?
        GraphPath p;
        if (vhc.flightRouteBuilder != null) {
            if (vhc.flightRouteBuilder.apply(gmc, vhc)) {
                vhc.flightRouteBuilder = findNextFlightRouteBuilder();
            }
        }

        // 13.5.20: check for aircraft that reached takeoff point
        // Wenn es ein Aircraft ist, das departen will, den FlightGraph hinterlegen.
        // Das war mal im PATHCOMPleted. Dann schon einen neuen graph/path zu setzen, ist doch unsauber. Andere wollen das Event ja auch noch verarbeiten.
        // Ausserdem soll der Destination airport schon geladen sein.
        // Ob das hier aber immer zuverlässig greift? Andererseits, warum nicht, Vehicle hat keinen Graph und muss weiterkommen. Aber es könnte schon am Endziel sein.
        // Dann duerfte es doch keinen Travelplan mehr geben.
        if (entity.isLockedBy(this) && gmc.pathCompleted() && vhc.travelplan != null) {
            //dann bin ich zuständig
            Destination destination = vhc.travelplan.getCurrentDestination();
            logger.debug("found aircraft with completed path. destination=" + destination + ",entity=" + entity.getName());

            if (destination.type == Destination.TYPE_FOR_TAKEOFF/*vc.taxiingfortakeoff*/) {
                buildAircraftDepartAction(entity, destination.runway);
            }
            if (destination.type == Destination.TYPE_LOCAL_ORBIT_ENTRY) {
                enterLocalOrbit(entity, destination);
            }
        }
    }

    private FlightRouteBuilder findNextFlightRouteBuilder() {
        return null;
    }

    @Override
    public boolean processRequest(Request request) {
        logger.debug("got event " + request.getType());
        if (request.getType().equals(RequestRegistry.TRAFFIC_REQUEST_AIRCRAFTDEPARTING)) {
            // Assumes that the aircraft is somewhere on the groundnet and needs to move to runway first.
            //  Servicepoint cleanen? Evtl. ist ja noch Service aktiv. TODO
            TrafficRequest tr = (TrafficRequest) request.getPayloadByIndex(0);
            //Schedule schedule = new Schedule(null, tr.groundnet/*, trafficSystem*/);
            String failMsg = buildAircraftDepartAction(/*tr.groundnet,*/ tr.aircraft,/* tr.holding, tr.departing, */tr.flightdestination);
            if (failMsg != null /*not set :-( && request.getUserEntityId() != null*/) {
                // showing a GUI message for the requesting user is OK so far. Showing it in some cockpit instrument doesn't appear better.
                SystemManager.putRequest(BaseRequestRegistry.buildUserMessageRequest(UserSystem.getInitialUser().getId(), failMsg, 3000, request.getUserEntityId()));
            }
            //if needed load destination airport to have it available at takeoff.TODO Bravo hat flightdestination null
            if (tr.flightdestination != null && tr.flightdestination.isType(Destination.TYPE_ICAO_PARKPOS)) {
                String icao = tr.flightdestination.getIcao();
                Util.notyet();
                /*27.12.21 TODO if (!DefaultTrafficWorld.getInstance().hasAirport(icao)) {
                    SystemManager.putRequest(new Request(RequestRegistry.TRAFFIC_REQUEST_LOADGROUNDNET, new Payload(icao)));
                }*/
            }
            return true;
        }
        return false;
    }

    @Override
    public void process(Event evt) {
        logger.debug("got event " + evt.getType());
        if (evt.getType().equals(GraphEventRegistry.GRAPH_EVENT_PATHCOMPLETED)) {
            EcsEntity vehicle = (EcsEntity) evt.getPayloadByIndex(2);
            VehicleComponent vc = VehicleComponent.getVehicleComponent(vehicle);
        }
        if (evt.getType().equals(TrafficEventRegistry.TRAFFIC_EVENT_AIRPORT_LOADED)) {
        }
    }

    @Override
    public String getTag() {
        return TAG;
    }

    /**
     * Move Aircraft to some 'holding point' (not really correct as it is on the runway instead of before)
     * on the runway. From there a flight graph will continue moving.
     * Also creates a travelplan for the vehicle to reach 'flightdestination'.
     * Currently hardcoded for EDDK!
     * 13.5.25: Returns null on success and a fail message in case of failure.
     * <p>
     * Created by thomass on 13.02.2018.
     */
    private String buildAircraftDepartAction(EcsEntity aircraft, Destination flightdestination) {
        GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(aircraft);
        VehicleComponent vhc = VehicleComponent.getVehicleComponent(aircraft);
        GraphPosition start = gmc.getCurrentposition();
        //MA31 Graph ist kein TrafficGraph mehr. Auf die Art icao durchzureichen, ist wirklich Friemelei.
        // TrafficGraph trafficGraph = (TrafficGraph) gmc.getGraph();
        String icao = "EDDK";//trafficGraph.icao;

        logger.debug("buildAircraftDepartAction for taxiing vehicle " + aircraft.getName());

        GroundNet groundNet = null;
        Runway runway = null;
        //27.12.21 if (DefaultTrafficWorld.getInstance() != null) {
        //27.12.21 vorerst immer EDDK
        // groundNet = DefaultTrafficWorld.getInstance().getGroundNet(icao);
        // runway = DefaultTrafficWorld.getInstance().getAirport(icao).getRunways()[0];
        groundNet = GroundServicesSystem.groundnets.get("EDDK");
        runway = GroundServicesSystem.airport.getRunways()[0];
        //}
        if (groundNet == null) {
            logger.warn("no groundnet");
            return "no groundnet";
        }

        //Die arrived werden noch nicht ricvhtig mit MovenmentCompioentn angelegt. Darum einen c172p an der Runway sezten.
        //auch weils für Tests schneller geht.
        // Der Taxipath muss zum Holdingpoint gehen.
        GraphNode holding = groundNet.getHolding(runway.getFromNumber())/*getName())*/ /*enternodefromgroundnet*/;
        if (holding == null) {
            logger.error("No holding found on runway " + runway.getName());
            return "No holding found on runway " + runway.getName();
        }
        if (start == null) {
            // Might happen when pressing (s) before a vehicle is loaded?
            // 12.5.25 Or when a vehicle isn't bound to a graph at all.
            logger.warn("no graph start location");
            return "no graph start location";
        }

        GraphPath path = groundNet.groundnetgraph.createPathFromGraphPosition(start, holding, null, vhc.config);
        if (path == null) {
            logger.error("no path found to " + holding);
            return "no path found to " + holding;
        }
        path.setName("toTakeoff");
        SystemManager.sendEvent(new Event(GraphEventRegistry.GRAPH_EVENT_PATHCREATED, new Payload(groundNet.groundnetgraph, path)));
        logger.debug("set path:" + path);
        gmc.setPath(path, true);
        Destination takeoffDestination = Destination.buildForTakeoff(runway);
        //vhc.taxiingfortakeoff = true;
        //vhc.runway = runway;
        vhc.travelplan = new Travelplan(flightdestination);
        vhc.travelplan.addDestination(takeoffDestination);
        if (!aircraft.lockEntity(this)) {
            logger.error("Lock vehicle failed");
        }
        return null;
    }

    /**
     * Takeoff from Runway. Das Vehicle steht jetzt schon auf der Runway.
     * Ist fuer 2D und 3D, daher die Projection (null bei 3D).
     * Die Runway steht hier ja schon fest, darum als Parameter
     * <p>
     * 11.4.20: Es wird ein GraphMovement bis zu einer Stelle erstellt, an der sich das Aircraft aufhalten kann (Landing, Holding, Orbit)
     * 13.5.20: For now, this will only be done if the destination has already been loaded. Thats useful for creating the SID.
     * 16.5.20: No requests an airport load if airport hasn't been loaded yet.
     * Wird das nicht immer wieder gemacht, bis es den Airport gibt?
     * <p>
     * Created by thomass on 13.02.2018.
     */
    private void buildAircraftDepartAction(EcsEntity aircraft/*, GraphNode positiononrunway,*/, Runway departing) {
        VehicleComponent vhc = VehicleComponent.getVehicleComponent(aircraft);
        GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(aircraft);
        VelocityComponent vc = VelocityComponent.getVelocityComponent(aircraft);
        Graph trafficGraph = gmc.getGraph();

        logger.debug("buildAircraftDepartAction departing for " + aircraft.getName());

        GraphPosition position = gmc.getCurrentposition();
        //21.3.19: Ob die Node richtig ist?
        GraphNode positiononrunway = position.getNodeInDirectionOfOrientation();

        Destination travelDestination = vhc.travelplan.travelDestination;
        logger.debug("buildAircraftDepartAction departing for vehicle " + aircraft.getName() +
                " from runway "+departing+" with destination "+travelDestination);

        if (travelDestination.isType(Destination.TYPE_ICAO_PARKPOS)) {
            Util.notyet();
            /*27.12.21 TODO
            String icao = travelDestination.getIcao();
            if (!DefaultTrafficWorld.getInstance().hasAirport(icao)) {
                //load request should have been created already
                //SystemManager.putRequest(new Request(RequestRegistry.TRAFFIC_REQUEST_LOADGROUNDNET, new Payload(icao)));
                logger.debug("airport not known. Skipping depart action");
                return;
            }
            AirportConfig airport;

            if ((airport = DefaultTrafficWorld.getInstance().getAirport(icao)) == null) {
                //TODO return to current? or parkpos?
                logger.debug("airport not loaded. Skipping depart action");
            }

             */
        }

        //Platzrunde als Default
        int pattern = 0;
        if (travelDestination != null && travelDestination.hasPattern()) {
            pattern = travelDestination.getPattern();
        }
        // 28.9.18: Die projection ist wirklich nur fuer 2D? Klingt plausibel.
        FlightRouteGraph flightRoute = new RouteBuilder(TrafficHelper.getEllipsoidConversionsProviderByDataprovider()).buildFlightRouteGraph(departing,
                (/*27.12.21DefaultTrafficWorld.getInstance() == null*/false) ? null : TrafficHelper.getProjectionByDataprovider(null/*??*/).projection, pattern);
        GraphPath smoothedflightpath = flightRoute.getPath();
        Graph graph = flightRoute.getGraph();


        if (smoothedflightpath != null) {
            SystemManager.sendEvent(new Event(GraphEventRegistry.GRAPH_EVENT_PATHCREATED, new Payload(graph, smoothedflightpath)));
        } else {
            logger.error("no path found to " + positiononrunway);
            return;
        }
        logger.debug("set path:" + smoothedflightpath);
        //hier wechselt der Graph. Fuer Flight darf es keine Projection geben. Bei GroundService schon, oder?
        // Nee, ich glaub nicht. Der Graph ist dann ja schon projected

        gmc.setGraph(graph, null, null);

        gmc.setPath(smoothedflightpath, true);

        //travelDestination.

        vhc.travelplan.addDestination(flightRoute.nextDestination);
        if (pattern != 0) {
            vc.enableHyperSpeed(3000.0);
        }
    }

    /**
     * Eigentlich koennte er direkt ins global Orbit wechseln. Das ist aber etwas ruppig. Darum erstmal ins local.
     *
     * @param aircraft
     * @param localOrbitEntry
     */
    private void enterLocalOrbit(EcsEntity aircraft, Destination localOrbitEntry) {
        VehicleComponent vhc = VehicleComponent.getVehicleComponent(aircraft);
        GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(aircraft);
        Graph trafficGraph = gmc.getGraph();

        EllipsoidCalculations rbcp = TrafficHelper.getEllipsoidConversionsProviderByDataprovider();
        Vector3 starte1 = rbcp.toCart(localOrbitEntry.equatorentry, null, null);
        Graph graph = SolarSystem.buildLocalOrbitGraph(starte1);
        GraphPosition position = new GraphPosition(graph.getEdge(0));
        gmc.setGraph(graph, position, null);

        //if (v
        // und dann soll mal ein anderer das Orbit wechseln.
        vhc.flightRouteBuilder = new OrbitToOrbitRouteBuilder();
    }

}

