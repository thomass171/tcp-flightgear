package de.yard.threed.trafficfg;




import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.graph.GraphNode;
import de.yard.threed.traffic.Destination;
import de.yard.threed.traffic.osm.OsmRunway;
import de.yard.threed.trafficfg.flight.GroundNet;
import de.yard.threed.trafficfg.flight.Parking;
import de.yard.threed.trafficfg.flight.ServicePoint;

/**
 * 13.3.19: Der ist als Universalparameter doch viel zu gross.
 * 20.3.19: ECS kennt jetzt Requersts.
 * 26.3.19: Diese Klasse, zumindest in irgendeiner Form, ist aber wohl als Payload erforderlich,
 * damit die System an Daten wie z.B. Groundnet kommen.
 *
 */
public class TrafficRequest {
    public Destination flightdestination;
    public EcsEntity aircraft;
    public GraphNode from;
    public GraphNode holding;
    public Parking destination;
    public char type;
    public String vehicletype;
    ServicePoint servicepoint;
    OsmRunway departing;
    GroundNet groundnet;


    /**
     * foloowme?
     * @param aircraft
     * @param from
     * @param destination
     */
    public TrafficRequest(EcsEntity aircraft, GraphNode from, Parking destination) {
        this.aircraft = aircraft;
        this.from = from;
        this.destination = destination;
        this.type = 'f';
    }

    /**
     * simple move
     */
    public TrafficRequest(String vehicletype, Parking destination) {
        this.vehicletype = vehicletype;
        this.destination = destination;
        this.type = 'm';
    }

    /**
     * depart: Taxiing bis zum "Holding". Geht nur mit groundnet, was ja eigentlich logisch ist.
     * destination might be
     * null for Platzrunde
     * icao for other airport
     */
    public TrafficRequest(EcsEntity/*ArrivedAircraft*/ aircraft/*, GraphNode holding,Runway departing,GroundNet groundnet,*/, Destination flightdestination) {
        this.aircraft = aircraft;
        //this.holding = holding;
        //this.departing=departing;
       // this.groundnet=groundnet;
        this.type = 'd';
        this.flightdestination=flightdestination;
    }

    public TrafficRequest(EcsEntity aircraft, ServicePoint servicepoint) {
        this.aircraft = aircraft;
        this.servicepoint = servicepoint;
        this.type = 'c';
    }
}