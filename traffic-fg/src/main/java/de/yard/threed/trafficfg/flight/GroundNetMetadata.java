package de.yard.threed.trafficfg.flight;

import de.yard.threed.core.Degree;
import de.yard.threed.core.LatLon;
import de.yard.threed.traffic.osm.OsmRunway;
import de.yard.threed.trafficcore.model.Airport;
import de.yard.threed.trafficcore.model.Runway;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * 15.3.24: Reduced to only contain and provide hard coded airport layout data that should be
 * retrieved from OSM or apt.dat etc in the future. And deprecated.
 */
@Deprecated
public class GroundNetMetadata {
    //23.5.20 Thats what is "mocked" here.
    public Airport airport;

    private GroundNetMetadata(String icao, LatLon center) {
        this.airport = new Airport(icao, center.getLatDeg().getDegree(), center.getLonDeg().getDegree());
    }

    public static Airport getAirport(String icao) {
        return getMap().get(icao).airport;
    }

    private static Map<String, GroundNetMetadata> getMap() {
        Map<String, GroundNetMetadata> map = new HashMap<String, GroundNetMetadata>();

        // origin ist das was FG als EDDK center hat (laut logfile); zumindest die Koordinaten. Die höhe nehm ich einfach mal 50
        // der Origin ist für EDDK bzw. die Tests dazu ganz sensibel. Elevation veträgt er nicht.
        GroundNetMetadata eddk = new GroundNetMetadata("EDDK", new LatLon(new Degree(50.86538f), new Degree(7.139103f)));
        map.put("EDDK", eddk);
        eddk.airport.setRunways(new Runway[]{
                OsmRunway.eddk14L()
        });

        GroundNetMetadata eddf = new GroundNetMetadata("EDDF", new LatLon(new Degree(50.0321932322f), new Degree(8.5417224522f)/*, 108.204*/));
        map.put("EDDF", eddf);
        //TODO EHAM
        map.put("EHAM", new GroundNetMetadata("EHAM", new LatLon(new Degree(50), new Degree(5)/*, 108.204f*/)));

        //TODO EDKV elevation
        GroundNetMetadata edkv = new GroundNetMetadata("EDKV", new LatLon(new Degree(50.403389), new Degree(6.521667)/*, 300.0*/));
        map.put("EDKV", edkv);
        return map;
    }
}
