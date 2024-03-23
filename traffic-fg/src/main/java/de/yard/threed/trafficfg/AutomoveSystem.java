package de.yard.threed.trafficfg;


import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.DefaultEcsSystem;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsGroup;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.RequestType;
import de.yard.threed.engine.util.RandomIntProvider;
import de.yard.threed.graph.GraphEventRegistry;
import de.yard.threed.graph.GraphMovingComponent;
import de.yard.threed.graph.GraphNode;
import de.yard.threed.graph.GraphPath;
import de.yard.threed.traffic.TrafficEventRegistry;
import de.yard.threed.traffic.TrafficGraph;
import de.yard.threed.traffic.TrafficHelper;
import de.yard.threed.traffic.VehicleComponent;
import de.yard.threed.trafficfg.config.AirportConfig;
import de.yard.threed.trafficfg.flight.GroundNet;

import java.util.List;

/**
 * 14.3.19: Neues eigenes AutomoveSystem. Looks for a new random destination for a vehicle after some idle time. Not to be mixed up
 * with GraphMovingComponent.automoveenabled!
 *
 * 8.5.19: Jetzt abstrakter auf TrafficGraph statt Groundnet.
 * <p>
 */
public class AutomoveSystem extends DefaultEcsSystem {
    private static Log logger = Platform.getInstance().getLog(AutomoveSystem.class);
    private boolean automove = false;
    public int maxidletime = 3;
    public static List<String> destinationlist;
    private TrafficGraph trafficGraph;
    private RandomIntProvider rand = new RandomIntProvider();
    boolean automovetoggleenabled = true;
    boolean debuglog = true;
    AirportConfig airport;
    public static String TAG = "AutomoveSystem";

    /**
     *
     */
    public AutomoveSystem() {
        super(new String[]{VehicleComponent.TAG}, new RequestType[]{
                        UserSystem.USER_REQUEST_AUTOMOVE,
                },
                new EventType[]{
                        TrafficEventRegistry.GROUNDNET_EVENT_LOADED,
                        GraphEventRegistry.GRAPH_EVENT_PATHCOMPLETED,
                        TrafficEventRegistry.TRAFFIC_EVENT_GRAPHLOADED});
        this.name = "AutomoveSystem";

        Boolean b;
        if ((b = Platform.getInstance().getConfiguration().getBoolean("argv.enableAutomove")) != null) {
            automove = (boolean) b;
        }
    }

    @Override
    final public void update(EcsEntity entity, EcsGroup group, double tpf) {
        if (group == null) {
            return;
        }
        VehicleComponent vhc = VehicleComponent.getVehicleComponent(entity);
        GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(entity);
        //13.3.19:Aus GroundServicesSystem hierhin verschonben weil es doch nichts zu suchen hat.
        if (automove) {
            // spawn moving for idle vehicles to random destination. Durch unscheduledmoving
            // wird verhindert, ein Aircraft mit Cockpit zu nehmen. Das soll Avatar steuern.
            if (!entity.isLocked() && gmc.unscheduledmoving && expiredIdle(gmc, vhc, maxidletime)) {
                if (vhc.config.getType().equals(VehicleComponent.VEHICLE_AIRCRAFT)) {
                    // Eine Platzrunde einstellen (per FlightSystem). 7.5.19: nur wenn es ein Groundnet gibt
                    if (trafficGraph != null && airport != null) {
                        logger.debug("Starting flight");
                        TravelHelper.startFlight(/*airport.getRunways()[0],*/ entity, null);
                    }
                } else {
                    //GraphNode destination = groundnet.groundnetgraph.getNode(rand.nextInt() % groundnet.groundnetgraph.getNodeCount());
                    //erstmal immer pednel zwischen A20 und C_7,E26
                    vhc.lastdestination = getNextDestination(vhc.lastdestination);
            /*if (vhc.lastdestination.equals("A20")) {
                vhc.lastdestination = "C_4";
            } else {
                vhc.lastdestination = "A20";
            }*/
                    if (debuglog) {
                        logger.debug("Spawning move of " + entity + " to " + vhc.lastdestination.getName());
                    }
                    if (TrafficHelper.spawnMoving(entity, vhc.lastdestination/*groundnet.getParkPos(vhc.lastdestination).node*/, trafficGraph)) {
                        if (!entity.lockEntity(this)) {
                            logger.error("Lock vehicle failed");
                        }
                    }
                }
            }
        }
    }

    public boolean getAutomoveEnabled() {
        return automove;
    }

    public void setAutomoveEnabled(boolean automoveEnabled) {
        this.automove = automoveEnabled;
    }

    @Override
    public void process(Event evt) {
        if (debuglog) {
            logger.debug("got event " + evt.getType());
        }

        if (evt.getType().equals(TrafficEventRegistry.GROUNDNET_EVENT_LOADED)) {
            GroundNet gn = (GroundNet) evt.getPayloadByIndex(0);
            setGroundnetAndAirport(gn.groundnetgraph, (AirportConfig) evt.getPayloadByIndex(1));
            // hier die Vehicles startren ist z.Z. nicht moeglich wegen Dependencies
        }
        if (evt.getType().equals(TrafficEventRegistry.TRAFFIC_EVENT_GRAPHLOADED)) {
            trafficGraph = (TrafficGraph) evt.getPayloadByIndex(0);
            // hier die Vehicles startren ist z.Z. nicht moeglich wegen Dependencies
        }
        if (evt.getType().equals(GraphEventRegistry.GRAPH_EVENT_PATHCOMPLETED)) {
            GraphPath path = (GraphPath) evt.getPayloadByIndex(1);
            EcsEntity vehicle = (EcsEntity) evt.getPayloadByIndex(2);
            VehicleComponent vhc = VehicleComponent.getVehicleComponent(vehicle);
            GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(vehicle);
            //GroundServiceComponent gsc = GroundServiceComponent.getGroundServiceComponent(vehicle);
            if (vehicle.isLockedBy(this)) {
                vehicle.release(this);
            }
        }
    }

    @Override
    public boolean processRequest(Request request) {
        logger.debug("got request " + request.getType());
        if (request.getType().equals(UserSystem.USER_REQUEST_AUTOMOVE)) {
            setAutomoveEnabled(!getAutomoveEnabled());
            return true;
        }
        return false;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    private boolean expiredIdle(GraphMovingComponent gmc, VehicleComponent vhc, /*GroundServiceComponent gsc, */int maxidletimeinseconds) {

        /*if (gsc != null && !gsc.isIdle()) {
            return false;
        }*/
        if (gmc.getPath() != null) {
            return false;
        }
       /*26.3.19  if (vhc.statechangetimestamp + maxidletimeinseconds * 1000 > ((Platform) Platform.getInstance()).currentTimeMillis()) {
            return false;
        }*/
        if (gmc.statechangetimestamp + maxidletimeinseconds * 1000 > ((Platform) Platform.getInstance()).currentTimeMillis()) {
            if (debuglog) {
                logger.debug("idle not expired with maxidletimeinseconds " + maxidletimeinseconds);
            }
            return false;
        }
        return true;
    }

    private void setGroundnetAndAirport(TrafficGraph groundnet, AirportConfig airport) {
        this.trafficGraph = groundnet;
        //8.5.19: Lass ich mal weg. Verfolge ich sowas wie destinationlist noch weiter?  this.destinationlist = airport.getDestinationlist();
        this.airport = airport;
    }

    /**
     * might return lasttarget.
     * 13.3.19: jetzt ne ueble Kruecke. Warum?
     * <p>
     * 8.5.19: Durch Trennung Groundnet/TrafficGraph wird einfach mal keine destinationlist mehr verwendet.
     * Das muesste konzeptionell nochmal durchdacht werden, ob es solche destinationlist wirklich geben sollte.
     *
     * @return
     */
    private GraphNode getNextDestination(GraphNode lastdestination) {
        GraphNode destination;
        do {
            if (true || destinationlist == null || destinationlist.size() == 0) {
                // use randon destination
                destination = trafficGraph.getBaseGraph().getNode(rand.nextInt() % trafficGraph.getBaseGraph().getNodeCount());
            } else {
                int index = rand.nextInt() % (destinationlist.size() + 1);
                //logger.debug("using random destination index " + index);
                /*8.5.19 if (index == destinationlist.size()) {
                    destination = trafficGraph.getHome();
                } else {
                    destination = trafficGraph.getCustomNodeByName(destinationlist.get(index));
                }*/
            }
            if (lastdestination == null) {
                return destination;
            }
        }
        while (destination.equals(lastdestination) || isTemporaryNode(destination));
        return destination;
    }

    private boolean isTemporaryNode(GraphNode n) {
        return n.getEdgeCount() == 1 && n.getEdge(0).getLayer() > 0;
    }

}

