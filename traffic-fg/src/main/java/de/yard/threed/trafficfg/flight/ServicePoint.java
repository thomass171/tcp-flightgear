package de.yard.threed.trafficfg.flight;


import de.yard.threed.core.Degree;
import de.yard.threed.core.Event;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.Payload;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeDocument;
import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.graph.DefaultGraphWeightProvider;
import de.yard.threed.graph.GraphEdge;
import de.yard.threed.graph.GraphEventRegistry;
import de.yard.threed.graph.GraphNode;
import de.yard.threed.graph.GraphPath;
import de.yard.threed.graph.GraphPosition;
import de.yard.threed.graph.TurnExtension;
import de.yard.threed.traffic.NoElevationException;
import de.yard.threed.traffic.TrafficConfig;
import de.yard.threed.traffic.TrafficGraph;
import de.yard.threed.traffic.config.ConfigHelper;
import de.yard.threed.traffic.config.VehicleDefinition;

import java.util.List;


/**
 * Created by thomass on 15.07.17.
 */
public class ServicePoint {
    private Log logger = Platform.getInstance().getLog(ServicePoint.class);

    public Vector3 prjdoorpos;
    public Vector3 prjwingpassingpoint, prjdoorturnpoint, prjdoorturncenter;
    public Vector3 prjleftwingapproachpoint;
    // point a few (5) meter behind the aircraft
    public Vector3 prjrearpoint;
    public /*20.11.23 GroundServiceAircraftConfig*/VehicleDefinition aircraft;
    Vector3 prjposition;
    Degree heading;
    Vector2 direction;
    // vertical edge to right front door. "from" isType at door.
    public GraphEdge doorEdge;
    // parallel edge behind left wind (for fuel truck)
    public GraphEdge wingedge, wingapproach, wingbranchedge, wingbestHitEdge;
    private GraphEdge door2wing;
    public GraphEdge wingreturn, wingreturn1, wingreturn2, doorbranchedge;
    //public GraphEdge doorReturnedge;
    TurnExtension backturn;
    GroundNet groundnet;
    long establishedtimestamp = Platform.getInstance().currentTimeMillis();
    //16.5.18 public EcsEntity/*ArrivedAircraft*/ aa;
    //public Schedule cateringschedule, fuelschedule;
    static int uniqueid = 1;
    int id;

    public ServicePoint(GroundNet groundnet, @Deprecated EcsEntity aa, Vector3 prjposition, @Deprecated Degree heading, Vector3 directioninsteadofheading, VehicleDefinition aircraft,
                        List<VehicleDefinition> vehicleDefinitions) throws NoElevationException {
        this.groundnet = groundnet;
        this.prjposition = prjposition;
        this.heading = heading;
        this.aircraft = aircraft;
        //this.aa = aa;
        if (directioninsteadofheading != null) {
            this.direction = new Vector2(directioninsteadofheading.getX(), directioninsteadofheading.getY());
            this.heading = MathUtil2.getHeadingFromDirection(this.direction);
            heading = this.heading;
        } else {
            this.direction = MathUtil2.getDirectionFromHeading(heading);
        }
        this.id = uniqueid++;
        logger.debug("Creating Servicepoint");
        // Create points/steps for reaching front right door
        Vector3 doorpos = aircraft.getCateringDoorPosition();
        prjdoorpos = groundnet.getProjectedAircraftLocation(prjposition, heading, doorpos);
        Vector3 wingpos = aircraft.getWingPassingPoint();
        prjwingpassingpoint = groundnet.getProjectedAircraftLocation(prjposition, heading, wingpos);
        prjdoorturncenter = groundnet.getProjectedAircraftLocation(prjposition, heading, doorpos.add(new Vector3(-TrafficGraph.SMOOTHINGRADIUS, 0, 0)));
        prjdoorturnpoint = groundnet.getProjectedAircraftLocation(prjposition, heading, doorpos.add(new Vector3(-TrafficGraph.SMOOTHINGRADIUS, TrafficGraph.SMOOTHINGRADIUS, 0)));

        // Create points/steps for reaching back area of left wing for fuel truck
        prjleftwingapproachpoint = groundnet.getProjectedAircraftLocation(prjposition, heading, aircraft.getLeftWingApproachPoint());
        prjrearpoint = groundnet.getProjectedAircraftLocation(prjposition, heading, aircraft.getRearPoint());
        // 27.12.21:Bischen nach hier verschoebn. Merkwurdige Logik??
        double approachoffset = 0;
        for (int i = 0; i < /*TrafficWorldConfig.getInstance().*/vehicleDefinitions.size(); i++) {
            VehicleDefinition v = /*TrafficWorldConfig.getInstance()*/vehicleDefinitions.get(i);
            if (GroundServiceComponent.VEHICLE_CATERING.equals(v.getType())) {
                approachoffset = v.getApproachoffset();
            }
        }
        buildHelperPaths(approachoffset);
    }

    /*public void setDoorEdge(GraphEdge doorEdge) {
        this.doorEdge = doorEdge;
    }*/

    /**
     * Zusätzliche edges des Servicepoint. Aber noch keine Paths.
     */
    private void buildHelperPaths(double approachoffset) throws NoElevationException {
        // path to door

        GraphEdge[] e = groundnet.createDoorApproach(prjdoorpos, direction, prjwingpassingpoint, prjrearpoint, (double) aircraft.getWingspread(), approachoffset);
        doorEdge = e[0];
        door2wing = e[1];
        doorbranchedge = e[2];
        backturn = groundnet.createBack(doorEdge.from, doorEdge, door2wing);
        SystemManager.sendEvent(new Event(GraphEventRegistry.GRAPH_EVENT_LAYERCREATED, new Payload(groundnet.groundnetgraph, new Integer(doorEdge.getLayer()))));
        SystemManager.sendEvent(new Event(GraphEventRegistry.GRAPH_EVENT_LAYERCREATED, new Payload(groundnet.groundnetgraph, new Integer(backturn.getLayer()))));

        // path to left wing. Used for fuel truck. Might be to small depending on aircraft.
        e = groundnet.createFuelingApproach(prjposition, direction, prjleftwingapproachpoint, prjrearpoint);
        wingedge = e[0];
        wingapproach = e[1];
        wingbranchedge = e[2];
        wingbestHitEdge = e[3];
        e = createWingReturn();
        wingreturn = e[0];
        wingreturn1 = e[1];
        wingreturn2 = e[2];
        SystemManager.sendEvent(new Event(GraphEventRegistry.GRAPH_EVENT_LAYERCREATED, new Payload(groundnet.groundnetgraph, new Integer(wingedge.getLayer()))));
        SystemManager.sendEvent(new Event(GraphEventRegistry.GRAPH_EVENT_LAYERCREATED, new Payload(groundnet.groundnetgraph, new Integer(wingreturn.getLayer()))));
    }

    /**
     * Find path to a servicepoint node.
     * Die smoothpath edge bekommen ein eigenes Layer, weil das Layer ja wirklich nur fuer diesen Pfad verwendet werden soll und kann. Sonst können
     * sie smoothing paths am Serviepoint wiederverwendet werden, mit kuriosen Effekten.
     * TODO use symbolic name like DOOR/WIONG instead of explicit destination node
     */
    public GraphPath getApproach(GraphPosition start, GraphNode destination, boolean withsmooth) {

        DefaultGraphWeightProvider graphWeightProvider = new DefaultGraphWeightProvider(groundnet.groundnetgraph.getBaseGraph(), 0);
        graphWeightProvider.validlayer.add(doorEdge.getLayer());
        graphWeightProvider.validlayer.add(wingedge.getLayer());
        voidEdgeUnderAircraft(graphWeightProvider);
        GraphPath approach = groundnet.groundnetgraph.createPathFromGraphPosition(start, destination, graphWeightProvider, withsmooth, null);
        if (approach != null) {
            SystemManager.sendEvent(new Event(GraphEventRegistry.GRAPH_EVENT_PATHCREATED, new Payload(groundnet.groundnetgraph, approach)));
        }

        return approach;
    }


    /**
     * Avoid edges under the aircraft. Nearest will probably find the edge to parking pos.
     * das kann echt noch optimiert werden.
     */
    private void voidEdgeUnderAircraft(DefaultGraphWeightProvider graphWeightProvider) {
        GraphNode nearest = groundnet.groundnetgraph.getBaseGraph().findNearestNode(prjposition, null);
        for (int i = 0; i < nearest.getEdgeCount(); i++) {
            GraphEdge e = nearest.getEdge(i);
            // don't accidenttally ignore temporary approach edges.
            if (e.getLayer() == 0) {
                graphWeightProvider.voidedges.add(e);
            }
        }
    }

    /**
     * Return home from door. mit zurücksetzen.
     * Nur um das klarzustellen: Alte Smotthings werden nicht wiederverwendet, denn die sind hoffentlich weg.
     * Smoothings muessen immer mit eigenem layer gemacht werden. Eine Ausnamhe ist der erste back arc, was aber eigentlich kein Smoothing ist.
     *
     * @return
     */
    public GraphPath getDoorReturnPath(boolean withsmooth) {
        DefaultGraphWeightProvider graphWeightProvider = getDoorReturnPathProvider();
        GraphPath returnpath = groundnet.groundnetgraph.createBackPathFromGraphPosition(doorEdge.from, doorEdge, backturn, groundnet.getVehicleHome().node, graphWeightProvider, withsmooth, null);
        if (returnpath != null) {
            SystemManager.sendEvent(new Event(GraphEventRegistry.GRAPH_EVENT_PATHCREATED, new Payload(groundnet.groundnetgraph, returnpath)));
        }
        return returnpath;
    }

    public DefaultGraphWeightProvider getDoorReturnPathProvider() {
        DefaultGraphWeightProvider graphWeightProvider = new DefaultGraphWeightProvider(groundnet.groundnetgraph.getBaseGraph(), 0);
        graphWeightProvider.validlayer.add(backturn.getLayer());
        graphWeightProvider.validlayer.add(doorEdge.getLayer());
        voidEdgeUnderAircraft(graphWeightProvider);
        return graphWeightProvider;
    }

    /**
     * ohne zurücksetzen.
     * Nur wingreturn layer zulassen, damit er nicht ueber den Approach geht.
     * <p>
     * Die smoothpath edge bekommen ein eigenes Layer, weil das Layer ja wirklich nur fuer diesen Pfad verwendet werden soll und kann. Sonst können
     * sie smoothing paths am Serviepoint wiederverwendet werden, mit kuriosen Effekten.
     *
     * @return
     */
    public GraphPath getWingReturnPath(boolean withsmooth) {
        DefaultGraphWeightProvider graphWeightProvider = new DefaultGraphWeightProvider(groundnet.groundnetgraph.getBaseGraph(), 0);
        graphWeightProvider.validlayer.add(wingreturn.getLayer());
        voidEdgeUnderAircraft(graphWeightProvider);
        GraphPath returnpath = groundnet.groundnetgraph.createPathFromGraphPosition(new GraphPosition(wingedge), groundnet.getVehicleHome().node, graphWeightProvider,/* wingedge.getLayer(),*/ withsmooth, null);
        if (returnpath != null) {
            SystemManager.sendEvent(new Event(GraphEventRegistry.GRAPH_EVENT_PATHCREATED, new Payload(groundnet.groundnetgraph, returnpath)));
        }
        return returnpath;
    }

    private int[]/*List<Integer>*/ getLayer() {
        //List<Integer> layer=new ArrayList<Integer>();
        return new int[]{backturn.getLayer(), doorEdge.getLayer(), wingedge.getLayer(), wingreturn.getLayer()};
    }

    private GraphEdge[] createWingReturn() {
        GraphEdge[] e = groundnet.createWingReturn(wingedge, wingbranchedge, direction);
        return e;
    }

    /**
     * Zu entfernen sind zum einen die edges des Servicepoint, aber auch die pathes, die
     * evtl. nicht gefahren wurden.
     * 15.8.17: Ach, erst anlegen wenn sie gebraucht werden.
     * Also, der SP gibt sich selber frei. Aber nicht aus der Liste aller SPs entfernen, das muss der Aufrufer machen. Doch, analog zu schedule.
     * 15.5.18: Sind Z.Z. immer 4 Layer.
     * 19.3.19: Analog zu FG.
     */
    public void delete() {
        logger.debug("deleting ");
        for (int layerid : getLayer()) {
            groundnet.groundnetgraph.getBaseGraph().removeLayer(layerid);
            SystemManager.sendEvent(new Event(GraphEventRegistry.GRAPH_EVENT_LAYERREMOVED, new Payload(groundnet.groundnetgraph, layerid)));
        }
        GroundServicesSystem.servicepoint.remove(id);
    }


    /**
     * Returns doorpos in world space.
     *
     * @param aircraft
     * @return
     */
    /*public Vector3 getAircraftDoorPosition(EcsEntity aircraft) {
        Vector3 p = aircraft.scenenode.getTransform().getPosition();
        Degree heading = getAircraftHeading(aircraft);
        
    }*/

    /*public Vector2 getAircraftWinghookPosition(EcsEntity aircraft) {
        Vector3 p = aircraft.scenenode.getTransform().getPosition();
        Degree heading = getAircraftHeading(aircraft);
        
    }*/


}
