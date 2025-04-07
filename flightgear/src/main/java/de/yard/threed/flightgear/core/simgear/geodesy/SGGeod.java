package de.yard.threed.flightgear.core.simgear.geodesy;

import de.yard.threed.core.Degree;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.GeoCoordinate;

/**
 * FG has parameter order lon/lat always.
 * Auch keine Setter aus dem gleichen Grund wie bei Vector!
 * 17.11.21: Deprecated because it might/should be replaced by LatLon for simple (2D) travelling.
 *
 * See https://en.wikipedia.org/wiki/Earth-centered,_Earth-fixed_coordinate_system
 * <p>
 * Created by thomass on 18.02.16.
 */
public class SGGeod extends LatLon {
    static Log logger = Platform.getInstance().getLog(SGGeod.class);

    public static SGGeod suedpol = new SGGeod(new Degree(0), new Degree(-90), 0);
    public static SGGeod nordpol = new SGGeod(new Degree(0), new Degree(90), 0);
    //TODO: move definition to central FG class
    public static double ERAD = 6378138.12;

    //private double latitudeRad;
    //private double longitudeRad;
    private double elevationM;

    public SGGeod(double longitudeRad, double latitudeRad, double elevationM) {
        super(latitudeRad,longitudeRad);
        this.elevationM = elevationM;
    }

    public SGGeod(Degree longitude, Degree latitude, double elevationM) {
      super( latitude.toRad(), longitude.toRad());
        this.elevationM = elevationM;
    }

    public SGGeod() {
super(0,0);
    }

    public static SGGeod fromLatLon(LatLon latLon, double elevation) {
       return new SGGeod(latLon.getLonDeg(),latLon.getLatDeg(),elevation);
    }

    public static SGGeod fromGeoCoordinate(GeoCoordinate geoCoordinate) {
        return new SGGeod(geoCoordinate.getLonDeg(),geoCoordinate.getLatDeg(),geoCoordinate.getElevationM());
    }


    public void setElevationM(double elevationM) {
        this.elevationM = elevationM;
    }

    public double getLongitudeRad() {
        return getLonRad();
    }

    public double getLatitudeRad() {
        return getLatRad();
    }

    public Degree getLongitudeDeg() {
        return Degree.buildFromRadians(getLonRad());
    }

    public Degree getLatitudeDeg() {
        return Degree.buildFromRadians(getLatRad());
    }

    public double getElevationM() {
        return elevationM;
    }

    @Override
    public String toString() {
        //return latitudeRad+" "+longitudeRad;
        return toWGS84decimalString() + ", " + elevationM + " m";
    }




    public static SGGeod fromCart(Vector3 cart) {
        return SGGeodesy.SGCartToGeod(cart);
    }

    public Vector3 toCart() {
        return SGGeodesy.SGGeodToCart(this);
    }

    /**
     * Parameter in Degrees.
     *
     * @param lon
     * @param lat
     * @return
     */
    public static SGGeod fromDeg(double lon, double lat) {
        return new SGGeod(new Degree(lon), new Degree(lat), 0);
    }

    public static SGGeod fromDeg(Degree lon, Degree lat) {
        return new SGGeod(lon, lat, 0);
    }

    public static SGGeod fromLatLon(Degree lat, Degree lon) {
        return new SGGeod(lon, lat, 0);
    }

    public static SGGeod fromGeodM(SGGeod geod, double elevation) {
        return new SGGeod(geod.getLonRad(), geod.getLatRad(), elevation);
    }


    public static SGGeod fromDegM(Degree lon, Degree lat, double elevation) {
        return new SGGeod(lon, lat, elevation);
    }


    public static SGGeod fromLatLon(LatLon latLon){
        return new SGGeod(latLon.lonRad,latLon.latRad,0.0);
    }

    public GeoCoordinate toGeoCoordinate() {
        return new GeoCoordinate(getLatitudeDeg(),getLongitudeDeg(),getElevationM());
    }


}
