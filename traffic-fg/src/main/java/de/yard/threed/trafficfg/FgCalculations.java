package de.yard.threed.trafficfg;

import de.yard.threed.core.Degree;
import de.yard.threed.core.GeneralParameterHandler;
import de.yard.threed.core.GeoCoordinate;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.trafficcore.EllipsoidCalculations;
import de.yard.threed.trafficcore.ElevationProvider;
import de.yard.threed.flightgear.core.simgear.geodesy.FgMath;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeodesy;
import de.yard.threed.trafficcore.geodesy.GeoTools;

/**
 * The flightgear implementation of EllipsoidCalculations.
 */
public class FgCalculations extends EllipsoidCalculations {

    private static Log logger = Platform.getInstance().getLog(FgCalculations.class);

    //Taken from FG
    public static double ERAD = 6378138.12;

    /**
     * <p>
     * 11.1.17: Now FG OsgMath.makeZUpFrame()
     *
     * @param location
     * @return
     */
    public Quaternion buildRotation(SGGeod location) {
        return FgMath.makeZUpFrameRelative(location);
    }

    /**
     * 8.6.17: Die Methode ist wohl für die Platzierung vom Modeln. Für Camera muss das Ergebnis noch ...(gespiegelt?) werden.
     */
    @Override
    public  Quaternion buildZUpRotation(GeoCoordinate location, Degree heading, Degree pitch) {
        Quaternion rotation = buildRotation(SGGeod.fromGeoCoordinate(location));
        // Rotation ist eigentlich CCW, Heading soll aber CW sein (in FG und intuitiv), darum negieren.
        // Scenery Objekte haben aber trotzdem CCW Haeding! Die mussen dann negiertes Heading hier reinstecken.
        rotation = rotation.multiply(Quaternion.buildQuaternionFromAngleAxis(heading, new Vector3(0, 0, -1)));
        rotation = rotation.multiply(Quaternion.buildQuaternionFromAngleAxis(pitch, new Vector3(0, 1, 0)));
        return rotation;
    }

    /**
     * Den Vector, der an der Location parallel zur Erdoberfläche nach Norden zeigt.
     * @param location
     * @return
     */
    @Override
    public  Vector3 getNorthHeadingReference(GeoCoordinate location) {
        Quaternion rotation = buildRotation(SGGeod.fromGeoCoordinate(location)/*,new FGDegree(0),new FGDegree(0)*/);
        Vector3 v = new Vector3(-1, 0, 0);
        // v = v.rotate(Quaternion.buildRotationZ(new Degree((float) -location.getLongitudeDeg().degree)));
        v = v.rotate(rotation);
        return v;
        // SGGeod ln = new SGGeod(location.getLongitudeDeg(),new FGDegree(location.getLatitudeDeg().degree+0.01f),location.getElevationM());
        // return ln.toCart().subtract(location.toCart()).normalize();
    }

    @Override
    public  GeoCoordinate fromCart(Vector3 cart) {
        SGGeod sgGeod = SGGeodesy.SGCartToGeod(cart);
        return sgGeod.toGeoCoordinate();
    }

    /**
     * 5.5.20: Wenn die Klasse nach core geht, das hier vielleicht woanders hin.
     * 21.3.24: Decoupled from SGGeod
     * @param elevationprovider
     * @return
     */
    @Override
    public  Vector3 toCart(GeoCoordinate geoCoordinate, ElevationProvider elevationprovider, GeneralParameterHandler<GeoCoordinate> missingElevationHandler) {
        // 21.3.24: elevation in GeoCoordinate is optional meanwhile. So intermediate SGGeod will fail.
        //SGGeod sgGeod = SGGeod.fromGeoCoordinate(geoCoordinate);
        Double elevation = geoCoordinate.getElevationM();
        if (elevationprovider != null) {
            elevation = elevationprovider.getElevation(geoCoordinate.getLatDeg().getDegree(), geoCoordinate.getLonDeg().getDegree());
        }
        if (elevation == null) {
            // 29.5.24: No longer hide this problem but report to delegate. For avoiding NPE continue for now with workaround.
            missingElevationHandler.handle(geoCoordinate);
            // warn, because it might lead to too low positions, which is hard to analyze
            // using 180 instead of 0 helps for visual detection of the problem. Wrong it is in any case.
            logger.warn("no elevation. Using 180.0");
            elevation = 180.0;
        }
        return SGGeodesy.SGGeodToCart(geoCoordinate.getLonRad(), geoCoordinate.getLatRad(), (double)elevation);

    }

    @Override
    public  Vector3 toCart(GeoCoordinate geoCoordinate) {
        SGGeod sgGeod = SGGeod.fromGeoCoordinate(geoCoordinate);
        Double elevation = sgGeod.getElevationM();
        return SGGeodesy.SGGeodToCart(sgGeod.getLonRad(), sgGeod.getLatRad(), (double)elevation);

    }

    /**
     * like in FG, distance in meter.
     * Algorithm taken from geo.nas.
     * "spährisches Dreieck"
     * <p>
     * 22.3.18: Mit der Berechnung mit den MathUtil float MEthoden hatte ich weniger Rundungsfehler als mit double. Hmmmm. Suspekt.
     *
     * @return
     */
    @Override
    public LatLon applyCourseDistance(LatLon latLon, Degree coursedeg, double dist) {
        double course = coursedeg.toRad();
        //course *= D2R;
        dist /= (double) ERAD;

        if (dist < 0.0) {
            dist = Math.abs(dist);
            course = course - Math.PI;
        }

        double lon = 0;
        double lat = Math.asin(Math.sin(latLon.latRad) * Math.cos(dist)
                + Math.cos(latLon.latRad) * Math.sin(dist) * Math.cos(course));

        // Java has % module operator for float (different to C++, where Math.mod() needs to be used)
        if (Math.cos(latLon.latRad) > MathUtil2.FLT_EPSILON) {
            lon = Math.PI - ((Math.PI - latLon.lonRad
                    - Math.asin(Math.sin(course) * Math.sin(dist)
                    / Math.cos(latLon.latRad)) % (2 * Math.PI)));
        }
        return new LatLon(lat, lon);
    }

    @Override
    public Degree courseTo(LatLon latLon, LatLon dest) {
        // 22.4.25: We now have a FG like solution in tcp-22.
        return GeoTools.heading(latLon, dest);
    }

    /**
     * From https://de.wikipedia.org/wiki/Orthodrome and geo.nas
     */
    @Override
    public double distanceTo(LatLon latLon, LatLon dest) {
        if (latLon.latRad == dest.latRad && latLon.lonRad == dest.lonRad) {
            return 0;
        }

        double a = Math.sin((latLon.latRad - dest.latRad) * 0.5);
        double o = Math.sin((latLon.lonRad - dest.lonRad) * 0.5);
        return (2.0 * ERAD * Math.asin(Math.sqrt(a * a + Math.cos(latLon.latRad)
                * Math.cos(dest.latRad) * o * o)));
    }

}
