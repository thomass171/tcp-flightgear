package de.yard.threed.trafficfg;


import de.yard.threed.core.Payload;
import de.yard.threed.core.Util;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.graph.GraphMovingComponent;
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
     * Just send a request for a 'depart'.
     */
    public static void startFlight(EcsEntity aircraft, Destination flightdestination) {

        if (aircraft == null) {
            logger.warn("no aircraft");
            return;
        }
        Request evt6 = new Request(RequestRegistry.TRAFFIC_REQUEST_AIRCRAFTDEPARTING, new Payload(new Object[]{new TrafficRequest(aircraft, flightdestination)}));
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
     * "Use case Key 's'". Some roundtrip or similar.
     * It is not properly defined what the default trip is. Traditionally its a 'Platzrunde' started from a position on groundnet via TRAFFIC_REQUEST_AIRCRAFTDEPARTING.
     * 26.6.24: Now it can also be a 'initialRoute'.
     */
    public static void startDefaultTrip(EcsEntity avatarvehicle) {
        logger.info("Starting default trip");
        GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(avatarvehicle);
        if (gmc.getPath() != null) {
            // path already set from 'initialRoute'? Just use it.
            gmc.setAutomove(true);
        } else {
            // A roundtrip build by FlightSystem
            startFlight(Destination.buildRoundtrip(0), avatarvehicle);
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
