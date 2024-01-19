package de.yard.threed.trafficfg;


import de.yard.threed.core.Payload;
import de.yard.threed.core.Util;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.traffic.Destination;
import de.yard.threed.traffic.RequestRegistry;


/**
 * Container für statische MEthodesn, die sich so in den Systems rundruecken.
 * Aber nur wenn sie halbwegs Travel Allgemeingültig sind. Die trafficspezifischen kommen nach TrafficHelper.
 */
public class TravelHelper {
    private static Log logger = Platform.getInstance().getLog(TravelHelper.class);

    /**
     * Flug (z.B.Rundflug) für ein Aircraft starten. Wuerde wahrscheinlich mit jedem Vehicle gehen, aber naja, lassen wir
     * das mal. Hier erfolgt z.Z. keine Prüfung mehr, ob das aircraft wirklich verfuegbar ist. Ob das sinnvoll ist,
     * muss sich dann nochmal zeigen.
     * 9.5.19: Auf jeden Fall ist es wirklich für einen Flug (und damit wechsel des Graphen) nicht für innerhalb eines Graph.
     * <p>
     * Verschickt dann nur ein Event als Request. Gehört darum besser nicht in TrafficHelper? Naja, vielleicht doch. Vielleicht kann man es weiter eindampfen.
     */
    public static void startFlight(EcsEntity aircraft, Destination flightdestination) {

        if (aircraft == null) {
            logger.warn("no aircraft");
            return;
        }
        //9.10.19: Warum denn keycontrolled?Was soll damit gesteuert werden? Speed geht automatisch und Start/Stop über den Graphpath. Ich lass das mal.
        //Und wenn Speed mal gesteuert wird, lieber ueber menu/pick animation statt key.
        /*boolean keycontrolled = false;//true;
        if (keycontrolled) {
            //beim keycontrol scheitert manchmal der Graphwechsel. TODO
            GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(aircraft);
            //28.9.18: automoce lassen, damit es direkt losgeht.
            //gmc.setAutomoveEnabled(false);
            gmc.keycontrolled = true;
        }*/


        boolean use172p = true;
        if (use172p) {
                    /*GraphEdge ed = groundnet.groundnetgraph.findEdgeByName("128-129");
                    GraphPosition start = new GraphPosition(ed, ed.getLength() - 80, false);
                    loadFlightGearAircraft(0, start);*/
        } else {
            //ArrivedAircraft ac1 = arrivedaircraft.get(major);
        }
        Request evt6 = new Request(RequestRegistry.TRAFFIC_REQUEST_AIRCRAFTDEPARTING, new Payload(new Object[]{new TrafficRequest(aircraft, /*positiononrunway, runway, groundNet,*/ flightdestination)}));
        //mal ueber ECS versuchen requestQueue.addRequest(evt6);
        SystemManager.putRequest(evt6);

    }

    public static void startFlight(Destination travelDestination, EcsEntity currentvehicle) {
        //EcsEntity currentvehicle = getAvatarVehicle();
        if (currentvehicle == null) {
            logger.error("avatars vehicle not found");
            return;
        }
        TravelHelper.startFlight(currentvehicle, travelDestination);
    }

    /**
     * "Use case 6" bzw. Key 's'.
     * Platzrunde oder sonstige "Rundreise".
     *
     * 27.12.21:Von BasicTravelScene nach hier.
     */
    public static void startDefaultTrip(EcsEntity avatarvehicle) {
        logger.info("Starting default trip");
        if (true/*24.5.20 trafficWorld.currentairport != null*/) {
            // Mir airport geht ein Rundflug
            startFlight(Destination.buildRoundtrip(0), avatarvehicle);
        } else {
            // 8.5.19: Dann innerhalb des TrafficGraph eine (Rund)reise. Naja, erstmal irgendwo hin.
            //einfach erstmal c172p, weil die verfuegbar ist
            //muss auch per Request gehen, damit aus einem System das Vehicle gelockt wird.

            Util.nomore();
           /*27.12.21 EcsEntity vehicle = findVehicleByName("c172p");
            TrafficGraph trafficGraph = getGroundNet();
            GraphNode destination = trafficGraph.getBaseGraph().getNode(rand.nextInt() % trafficGraph.getBaseGraph().getNodeCount());
            Request evt6 = new Request(RequestRegistry.TRAFFIC_REQUEST_VEHICLE_MOVE, new Payload(vehicle, trafficGraph, destination));
            //mal ueber ECS versuchen
            SystemManager.putRequest(evt6);
*/
        }
    }

    /**
     * Ein ECS speziell fuer Travelling.
     * Naja, das ist aber doch nicht so geeignet
     * 21.3.20
     */
    /*9.11.21 public static void buildTravelEcs() {
        SystemManager.addSystem(new FlightSystem());
    }*/
}
