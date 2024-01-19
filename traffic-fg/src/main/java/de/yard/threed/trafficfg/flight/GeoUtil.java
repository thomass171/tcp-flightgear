package de.yard.threed.trafficfg.flight;

import de.yard.threed.core.Degree;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.traffic.geodesy.GeoCoordinate;
import de.yard.threed.trafficfg.FgCalculations;

public class GeoUtil {
    public static GeoCoordinate applyCourseDistance(GeoCoordinate coor, Degree coursedeg, double dist){
        FgCalculations fgc=new FgCalculations();

        return GeoCoordinate.fromLatLon(fgc.applyCourseDistance(coor,coursedeg, dist),coor.getElevationM());
    }

    public static SGGeod applyCourseDistance(SGGeod coor, Degree coursedeg, double dist) {
        return SGGeod.fromGeoCoordinate(GeoUtil.applyCourseDistance(coor.toGeoCoordinate(),
                coursedeg, dist));
    }
    }
