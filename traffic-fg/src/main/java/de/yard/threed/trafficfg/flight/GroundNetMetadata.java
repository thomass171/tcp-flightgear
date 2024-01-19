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
 * Nur eine Hilfsklasse für z.B. Tests. Auch fuer Groundnetconfig.
 * 13.2.18: Besserer Name AirpotData?
 *
 * 12.5.20: Should be an extension to {@link Airport}
 * with non redundant data (except icao).
 */
public class GroundNetMetadata {
    //23.5.20 mal so versuchen. Wegen convenience.
    public Airport airport;
    //String icao;
    //25.5.20 public SGGeod center;
    public String home = "";
    public List<String> destinationlist;
    //public Runway[] runway;
    //Eine default Elevation ist zwar Quatsch, aber praktisch fuer Tests.
    public static double DEFAULTELEVATION=87;
    //TODO elevation
    public double elevation=DEFAULTELEVATION;


    public GroundNetMetadata(String icao, LatLon center) {
        //this.icao = icao;
        this.airport=new Airport(icao,center.getLatDeg().getDegree(),center.getLonDeg().getDegree());
        //this.center = center;
    }

    public static Map<String, GroundNetMetadata> getMap() {
        Map<String, GroundNetMetadata> map = new HashMap<String, GroundNetMetadata>();

        // origin ist das was FG als EDDK center hat (laut logfile); zumindest die Koordinaten. Die höhe nehm ich einfach mal 50
        // der Origin ist für EDDK bzw. die Tests dazu ganz sensibel. Elevation veträgt er nicht.
        GroundNetMetadata eddk = new GroundNetMetadata("EDDK", new LatLon(new Degree(50.86538f), new Degree(7.139103f)));
        map.put("EDDK", eddk);
        eddk.home = "A20";
        eddk.destinationlist = new ArrayList<String>();
        eddk.destinationlist.add("C_4");
        eddk.destinationlist.add("E20");
        eddk.destinationlist.add("C_7");
        eddk.airport.setRunways(new Runway[]{
                        OsmRunway.eddk14L()
                });

        GroundNetMetadata eddf = new GroundNetMetadata("EDDF",new LatLon(new Degree(50.0321932322f),new Degree(8.5417224522f)/*, 108.204*/));
        // 14.5.18: V164 einfach mal so.
        eddf.home = "V164";
        map.put("EDDF", eddf);
        //TODO EHAM
        map.put("EHAM", new GroundNetMetadata("EHAM",new LatLon(new Degree(50),new Degree(5)/*, 108.204f*/)));

        //TODO EDKV elevation
        GroundNetMetadata edkv = new GroundNetMetadata("EDKV",new LatLon(new Degree(50.403389),new Degree(6.521667)/*, 300.0*/));
        edkv.home = "?";
        map.put("EDKV", edkv);
        return map;
    }
}
