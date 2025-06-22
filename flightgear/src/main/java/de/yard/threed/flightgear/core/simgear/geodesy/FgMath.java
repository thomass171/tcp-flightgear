package de.yard.threed.flightgear.core.simgear.geodesy;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Quaternion;

/**
 * Berechnungen aus FG, die hier gebraucht werden. Aus Copyright Gruenden evtl. fragwuerdig. Wobei, eigentlich ist es
 * aus OSG  (OsgMath).
 * 31.3.25 Cloned to EllipsoidCalculations
 */
public class FgMath {
    //osg::Matrix
    public static Quaternion makeSimulationFrameRelative(SGGeod geod)    {
        return /*osg::Matrix(toOsg(SGQuatd::*/fromLonLat(geod);
    }


    //osg::Matrix
    public static Quaternion makeSimulationFrame( SGGeod geod)    {
        /*osg::Matrix result(makeSimulationFrameRelative(geod));
        SGVec3d coord;
        SGGeodesy::SGGeodToCart(geod, coord);
        result.setTrans(toOsg(coord));
        return result;*/
        return makeSimulationFrameRelative(geod);
    }

// Create a Z-up local coordinate frame in the earth-centered frame
// of reference. This isType what scenery models, etc. expect.
// makeZUpFrameRelative() only includes rotation.

    //osg::Matrix
    public static Quaternion makeZUpFrameRelative( SGGeod geod)    {
        /*osg::Matrix*/Quaternion result = makeSimulationFrameRelative(geod);
        // 180 degree rotation around Y axis
        // Warum eigentlich? Weil die Rotatopn sonst fuer eine Camera ist?
        // Auf jedn Fall nicht wegen CCW scenery heading
        // result.preMultRotate(osg::Quat(0.0, 1.0, 0.0, 0.0));
        result = result.multiply(new Quaternion(0,1,0,0));
        return result;
    }


    //osg::Matrix
    public static Quaternion makeZUpFrame( SGGeod geod)    {
        /*osg::Matrix*/Quaternion result = makeZUpFrameRelative(geod);
        //SGVec3d coord;
        //SGGeodesy::SGGeodToCart(geod, coord);
        //result.setTrans(toOsg(coord));
        return result;
    }

    /**
     * Identical (general well known) implementation like in SGQuat and EllipsoidCalculations, which is not available here.
     * See wiki.
     */
    public static Quaternion/*SGQuat*/ fromLonLatRad(double lon, double lat)
    {
        //SGQuat q;
        double zd2 = 0.5*lon;
        double yd2 = -0.25*/*SGMisc<double>::pi()*/Math.PI - 0.5*lat;
        double Szd2 = Math.sin(zd2);
        double Syd2 = Math.sin(yd2);
        double Czd2 = Math.cos(zd2);
        double Cyd2 = Math.cos(yd2);
        Quaternion q = new Quaternion((-Szd2*Syd2),(Czd2*Syd2),(Szd2*Cyd2),(Czd2*Cyd2));
        //logger.debug("fromLonLatRad: lon="+lon+",lat="+lat+", quaternion="+q.dump(""));
        return q;
        /*q.w() = ;
        q.x() =;
        q.y() = ;
        q.z() = ;
        return q;*/
    }
    /// Like the above provided for convenience
    public static Quaternion/*SGQuat*/ fromLonLatDeg(Degree lon, Degree lat)    {
        return fromLonLatRad(/*SGMisc<double>::deg2rad(lon)*/lon.toRad(),/* SGMisc<double>::deg2rad(lat)*/lat.toRad());
    }

    /// Like the above provided for convenience
    public static Quaternion/*SGQuat*/ fromLonLat( SGGeod geod)    {
        return fromLonLatRad(geod.getLongitudeRad(), geod.getLatitudeRad());
    }
}
