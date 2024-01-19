package de.yard.threed.trafficfg;

import de.yard.threed.core.Vector3;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.graph.GraphAltitudeProvider;

/**
 * Entkopplung speziell fuer "getHyperSpeedAltitude" und orbit travel.
 * <p>
 * MA31
 */
public class SGGeodAltitudeProvider implements GraphAltitudeProvider {
    public double getAltitude(Vector3 position) {
        SGGeod geopos = SGGeod.fromCart(position);
        double altitude = geopos.getElevationM();
        return altitude;
    }
}
