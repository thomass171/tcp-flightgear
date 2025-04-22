package de.yard.threed.trafficfg.flight;

import de.yard.threed.core.Degree;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.graph.GraphPosition;
import de.yard.threed.graph.GraphProjection;
import de.yard.threed.traffic.flight.FlightLocation;
import de.yard.threed.core.GeoCoordinate;
import de.yard.threed.trafficcore.geodesy.MapProjection;
import de.yard.threed.trafficfg.FgCalculations;

/**
 * Created on 10.01.19.
 */
public class GraphProjectionFlight3D implements GraphProjection {
    MapProjection projection;

    public GraphProjectionFlight3D(MapProjection projection) {
        this.projection = projection;
    }

    @Override
    public LocalTransform project(GraphPosition cp) {
        /**
         * //Die Projection ist eine in die z0 Ebene. 9.1.19: Nicht mehr analog FG GroundServices, sondern "richtiger".
         //Liefert immer die unprojected 3D Werte.
         //logging.debug("3Dposition="~positionXYZ.toString());
         //10.3.18: Ob der Umgang mit direction hier so richtig ist, ist zweifelhaft. 

         */
        Vector2 direction = Vector2.buildFromVector3(cp.getDirection());
        Vector3 position = cp.get3DPosition();
        double fromalt = cp.currentedge.from.getLocation().getZ();
        double toalt = cp.currentedge.to.getLocation().getZ();
        double relpos = cp.getAbsolutePosition() / cp.currentedge.getLength();
        double alt = fromalt + ((toalt - fromalt) * relpos);
        FlightLocation loc = unprojectToFlightLocation(projection,Vector2.buildFromVector3(position), alt, direction);
        //logger.debug("geopos="+loc.coordinates+" with altitude "+alt);
        //SGGeod coord = setPositionFromXY(Vector2.buildFromVector3(position),gmc.projection);
        //var heading = get3DRotation(coord,cp.edgeposition,cp.reverseorientation,cp.currentedge.getEffectiveDirection( cp.getAbsolutePosition()));
        //me.hdgN.setValue(heading);

        //#logging.debug("updating vehicle altitude to " ~ alt ~ ", fromalt="~fromalt~", toalt="~toalt~",edge="~cp.currentedge.toString());
        //if (validateAltitude(alt)) {
        //    logging.warn("updating vehicle to out of range altitude " ~ alt ~ ", fromalt="~fromalt~", toalt="~toalt~",edge="~cp.currentedge.toString());
        //}
        //me.altN.setDoubleValue(alt * M2FT);
        LocalTransform posrot = loc.toPosRot(new FgCalculations());
        return posrot;
    }

    /**
     * Moved here from MapProjection
     */
    public static FlightLocation unprojectToFlightLocation(MapProjection projection, Vector2 pos, double alt, Vector2 direction) {
        LatLon lcoord = projection.unproject(pos);
        GeoCoordinate coord = new GeoCoordinate(lcoord.getLatDeg(),lcoord.getLonDeg(),alt);
        //coord.setElevationM(alt);
        Degree heading = getTrueHeadingFromDirection(coord,direction/*Vector2.buildFromVector3(new Vector3(0,1,0).rotate(rotation)) */);
        return new FlightLocation(coord,heading,new Degree(0));
    }

    /**
     * Return heading in Degree.
     * Also. irgendwie ist das Kokelores.
     */
    public static Degree getTrueHeadingFromDirection(GeoCoordinate coord, Vector2 vXY) {
        //# deflection compensation. TODO optimize calculation?
        //TODO: das gilt doch nur fuer die Nordosthalbkugel, oder?
        // x corresponds to longitude
        GeoCoordinate destination =  new GeoCoordinate(coord.getLatDeg().add(new Degree(1*vXY.y)),coord.getLonDeg().add(new Degree(1*vXY.x)),0);
        Degree heading = new FgCalculations().courseTo(coord,destination);
        return heading;
    }
}
