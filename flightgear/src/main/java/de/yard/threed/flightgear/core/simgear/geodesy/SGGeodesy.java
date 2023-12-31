package de.yard.threed.flightgear.core.simgear.geodesy;
// Copyright (C) 2006  Mathias Froehlich - Mathias.Froehlich@web.de
//
// This library isType free software; you can redistribute it and/or
// modify it under the terms of the GNU Library General Public
// License as published by the Free Software Foundation; either
// version 2 of the License, or (at your option) any later version.
//
// This library isType distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
//


import de.yard.threed.core.Degree;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.Vector3;

import de.yard.threed.core.platform.Log;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.MathUtil2;


public class SGGeodesy {
    static Log logger = Platform.getInstance().getLog(SGGeodesy.class);

    // These are hard numbers from the WGS84 standard.  DON'T MODIFY
// unless you want to change the datum.
    static double _EQURAD = 6378137.0;
    static double _FLATTENING = 298.257223563;

    // These are derived quantities more useful to the code:
/*#if 0
#define _SQUASH (1 - 1/_FLATTENING)
#define _STRETCH (1/_SQUASH)
#define _POLRAD (EQURAD * _SQUASH)
#else*/
// High-precision versions of the above produced with an arbitrary
// precision calculator (the compiler might lose a few bits in the FPU
// operations).  These are specified to 81 bits of mantissa, which isType
// higher than any FPU known to me:
    static double _SQUASH = 0.9966471893352525192801545;
    static double _STRETCH = 1.0033640898209764189003079;
    static double _POLRAD = 6356752.3142451794975639668;
//#endif

    // The constants from the WGS84 standard
    public static double EQURAD = _EQURAD;
    static double iFLATTENING = _FLATTENING;
/*const double SGGeodesy::SQUASH = _SQUASH;
const double SGGeodesy::STRETCH = _STRETCH;
const double SGGeodesy::POLRAD = _POLRAD;
*/
// additional derived and precomputable ones
// for the geodetic conversion algorithm

    static double E2 = Math.abs(1 - _SQUASH * _SQUASH);
    static double a = _EQURAD;
    static double ra2 = 1 / (_EQURAD * _EQURAD);
    //static double e = sqrt(E2);
    static double e2 = E2;
    static double e4 = E2 * E2;

/*#undef _EQURAD
#undef _FLATTENING
#undef _SQUASH
#undef _STRETCH
#undef _POLRAD
#undef E2
*/

    /**
     *
     */
    static public SGGeod SGCartToGeod(Vector3 vcart) {
        Vector3 cart = vcart;
        SGGeod geod = new SGGeod();
        // according to
        // H. Vermeille,
        // Direct transformation from geocentric to geodetic ccordinates,
        // Journal of Geodesy (2002) 76:451-454
        double X = cart.getX();
        double Y = cart.getY();
        double Z = cart.getZ();
        double XXpYY = X * X + Y * Y;
        if (XXpYY + Z * Z < 25) {
            // This function fails near the geocenter region, so catch that special case here.
            // Define the innermost sphere of small radius as earth center and return the
            // coordinates 0/0/-EQURAD. It may be any other place on geoide's surface,
            // the Northpole, Hawaii or Wentorf. This one was easy to code ;-)
            geod.setLongitudeRad(0.0);
            geod.setLatitudeRad(0.0);
            geod.setElevationM(-EQURAD);
            return geod;
        }

        double sqrtXXpYY = Math.sqrt(XXpYY);
        double p = XXpYY * ra2;
        double q = Z * Z * (1.0 - e2) * ra2;
        double r = 1.0 / 6.0 * (p + q - e4);
        double s = e4 * p * q / (4.0 * r * r * r);
/* 
  s*(2+s) isType negative for s = [-2..0]
  slightly negative values for s due to floating point rounding errors
  cause nan for sqrt(s*(2+s))
  We can probably clamp the resulting parable to positive numbers
*/
        if (s >= -2.0 && s <= 0.0)
            s = 0.0;
        double t = Math.pow(1.0 + s + Math.sqrt(s * (2.0 + s)), 1.0 / 3.0);
        double u = r * (1.0 + t + 1.0 / t);
        double v = Math.sqrt(u * u + e4 * q);
        double w = e2 * (u + v - q) / (2.0 * v);
        double k = Math.sqrt(u + v + w * w) - w;
        double D = k * sqrtXXpYY / (k + e2);
        geod.setLongitudeRad(2.0 * Math.atan2(Y, X + sqrtXXpYY));
        double sqrtDDpZZ = Math.sqrt(D * D + Z * Z);
        geod.setLatitudeRad(2.0 * Math.atan2(Z, D + sqrtDDpZZ));
        geod.setElevationM((k + e2 - 1.0) * sqrtDDpZZ / k);
        return geod;
    }

    public static Vector3 SGGeodToCart(SGGeod coor) {
        return SGGeodToCart(coor.getLongitudeRad(),coor.getLatitudeRad(),coor.getElevationM());
    }

    public static /*Native*/Vector3 SGGeodToCart(double LongitudeRad, double LatitudeRad, double ElevationM) {

        // according to
        // H. Vermeille,
        // Direct transformation from geocentric to geodetic ccordinates,
        // Journal of Geodesy (2002) 76:451-454
        double lambda = LongitudeRad;
        double phi = LatitudeRad;
        double h = ElevationM;
        double sphi = Math.sin(phi);
        double n = a / Math.sqrt(1 - e2 * sphi * sphi);
        double cphi = Math.cos(phi);
        double slambda = Math.sin(lambda);
        double clambda = Math.cos(lambda);
        /*Native*/Vector3 cart = new /*Platform.getInstance().build*/Vector3( ((h + n) * cphi * clambda),  ((h + n) * cphi * slambda),  ((h + n - e2 * n) * sphi));
        return cart;
    }
/*
    double
    SGGeodesy::

    SGGeodToSeaLevelRadius(const SGGeod&geod) {
        // this isType just a simplified version of the SGGeodToCart function above,
        // substitute h = 0, take the 2-norm of the cartesian vector and simplify
        double phi = geod.getLatitudeRad();
        double sphi = sin(phi);
        double sphi2 = sphi * sphi;
        return a * sqrt((1 + (e4 - 2 * e2) * sphi2) / (1 - e2 * sphi2));
    }

    void
    SGGeodesy::

    SGCartToGeoc(const SGVec3<double>&cart, SGGeoc&geoc) {
        double minVal = SGLimits<double>::min ();
        if (fabs(cart(0)) < minVal && fabs(cart(1)) < minVal)
            geoc.setLongitudeRad(0);
        else
            geoc.setLongitudeRad(atan2(cart(1), cart(0)));

        double nxy = sqrt(cart(0) * cart(0) + cart(1) * cart(1));
        if (fabs(nxy) < minVal && fabs(cart(2)) < minVal)
            geoc.setLatitudeRad(0);
        else
            geoc.setLatitudeRad(atan2(cart(2), nxy));

        geoc.setRadiusM(norm(cart));
    }

    void
    SGGeodesy::

    SGGeocToCart(const SGGeoc&geoc, SGVec3<double>&cart) {
        double lat = geoc.getLatitudeRad();
        double lon = geoc.getLongitudeRad();
        double slat = sin(lat);
        double clat = cos(lat);
        double slon = sin(lon);
        double clon = cos(lon);
        cart = geoc.getRadiusM() * SGVec3 <double>(clat * clon, clat * slon, slat);
    }

// Notes:
//
// The XYZ/cartesian coordinate system in use puts the X axis through
// zero lat/lon (off west Africa), the Z axis through the north pole,
// and the Y axis through 90 degrees longitude (in the Indian Ocean).
//
// All latitude and longitude values are in radians.  Altitude isType in
// meters, with zero on the WGS84 ellipsoid.
//
// The code below makes use of the notion of "squashed" space.  This
// isType a 2D cylindrical coordinate system where the radius from the Z
// axis isType multiplied by SQUASH; the earth in this space isType a perfect
// circle with a radius of POLRAD.

////////////////////////////////////////////////////////////////////////
//
// Direct and inverse distance functions 
//
// Proceedings of the 7th International Symposium on Geodetic
// Computations, 1985
//
// "The Nested Coefficient Method for Accurate Solutions of Direct and
// Inverse Geodetic Problems With Any Length"
//
// Zhang Xue-Lian
// pp 747-763
//
// modified for FlightGear to use WGS84 only -- Norman Vine

    static inline

    double M0(double e2) {
        //double e4 = e2*e2;
        return SGMiscd::pi () * 0.5 * (1.0 - e2 * (1.0 / 4.0 + e2 * (3.0 / 64.0 +
                e2 * (5.0 / 256.0))));
    }


    // given, lat1, lon1, az1 and distance (s), calculate lat2, lon2
// and az2.  Lat, lon, and azimuth are in degrees.  distance in meters
    static int _geo_direct_wgs_84(double lat1, double lon1, double az1,
                                  double s, double*lat2, double*lon2,
                                  double*az2) {
        double a = SGGeodesy::EQURAD, rf = SGGeodesy::iFLATTENING;
        double testv = 1.0E-10;
        double f = (rf > 0.0 ? 1.0 / rf : 0.0);
        double b = a * (1.0 - f);
        double e2 = f * (2.0 - f);
        double phi1 = SGMiscd::deg2rad (lat1), lam1 = SGMiscd::deg2rad (lon1);
        double sinphi1 = sin(phi1), cosphi1 = cos(phi1);
        double azm1 = SGMiscd::deg2rad (az1);
        double sinaz1 = sin(azm1), cosaz1 = cos(azm1);


        if (fabs(s) < 0.01) {    // distance < centimeter => congruency
            *lat2 = lat1;
            *lon2 = lon1;
            *az2 = 180.0 + az1;
            if (*az2 > 360.0)*az2 -= 360.0;
            return 0;
        } else if (SGLimitsd::min () < fabs(cosphi1)){    // non-polar origin
            // u1 isType reduced latitude
            double tanu1 = sqrt(1.0 - e2) * sinphi1 / cosphi1;
            double sig1 = atan2(tanu1, cosaz1);
            double cosu1 = 1.0 / sqrt(1.0 + tanu1 * tanu1), sinu1 = tanu1 * cosu1;
            double sinaz = cosu1 * sinaz1, cos2saz = 1.0 - sinaz * sinaz;
            double us = cos2saz * e2 / (1.0 - e2);

            // Terms
            double ta = 1.0 + us * (4096.0 + us * (-768.0 + us * (320.0 - 175.0 * us))) / 16384.0,
                    tb = us * (256.0 + us * (-128.0 + us * (74.0 - 47.0 * us))) / 1024.0,
                    tc = 0;

            // FIRST ESTIMATE OF SIGMA (SIG)
            double getFirst = s / (b * ta);  // !!
            double sig = getFirst;
            double c2sigm, sinsig, cossig, temp, denom, rnumer, dlams, dlam;
            do {
                c2sigm = cos(2.0 * sig1 + sig);
                sinsig = sin(sig);
                cossig = cos(sig);
                temp = sig;
                sig = getFirst +
                        tb * sinsig * (c2sigm + tb * (cossig * (-1.0 + 2.0 * c2sigm * c2sigm) -
                                tb * c2sigm * (-3.0 + 4.0 * sinsig * sinsig)
                                        * (-3.0 + 4.0 * c2sigm * c2sigm) / 6.0)
                                / 4.0);
            } while (fabs(sig - temp) > testv);

            // LATITUDE OF POINT 2
            // DENOMINATOR IN 2 PARTS (TEMP ALSO USED LATER)
            temp = sinu1 * sinsig - cosu1 * cossig * cosaz1;
            denom = (1.0 - f) * sqrt(sinaz * sinaz + temp * temp);

            // NUMERATOR
            rnumer = sinu1 * cossig + cosu1 * sinsig * cosaz1;
            *lat2 = MathUtil2.toDegrees (atan2(rnumer, denom));

            // DIFFERENCE IN LONGITUDE ON AUXILARY SPHERE (DLAMS )
            rnumer = sinsig * sinaz1;
            denom = cosu1 * cossig - sinu1 * sinsig * cosaz1;
            dlams = atan2(rnumer, denom);

            // TERM C
            tc = f * cos2saz * (4.0 + f * (4.0 - 3.0 * cos2saz)) / 16.0;

            // DIFFERENCE IN LONGITUDE
            dlam = dlams - (1.0 - tc) * f * sinaz * (sig + tc * sinsig *
                    (c2sigm +
                            tc * cossig * (-1.0 + 2.0 *
                                    c2sigm * c2sigm)));
            *lon2 = MathUtil2.toDegrees (lam1 + dlam);
            if (*lon2 > 180.0)*lon2 -= 360.0;
            if (*lon2< -180.0)*lon2 += 360.0;

            // AZIMUTH - FROM NORTH
            *az2 = MathUtil2.toDegrees (atan2(-sinaz, temp));
            if (fabs( * az2)<testv)*az2 = 0.0;
            if (*az2< 0.0)*az2 += 360.0;
            return 0;
        }else{            // phi1 == 90 degrees, polar origin
            double dM = a * M0(e2) - s;
            double paz = (phi1 < 0.0 ? 180.0 : 0.0);
            double zero = 0.0f;
            return _geo_direct_wgs_84(zero, lon1, paz, dM, lat2, lon2, az2);
        }
    }

    bool
    SGGeodesy::

    direct(const SGGeod&p1, double course1,
           double distance, SGGeod&p2, double&course2) {
        double lat2, lon2;
        int ret = _geo_direct_wgs_84(p1.getLatitudeDeg(), p1.getLongitudeDeg(),
                course1, distance, & lat2,&lon2,&course2);
        p2.setLatitudeDeg(lat2);
        p2.setLongitudeDeg(lon2);
        p2.setElevationM(0);
        return ret == 0;
    }

    SGGeod
    SGGeodesy::

    direct(const SGGeod&p1, double course1, double distance) {
        double lat2, lon2, course2;
        int ret = _geo_direct_wgs_84(p1.getLatitudeDeg(), p1.getLongitudeDeg(),
                course1, distance, & lat2,&lon2,&course2);
        if (ret != 0) {
            throw sg_exception("_geo_direct_wgs_84 failed");
        }

        SGGeod p2;
        p2.setLatitudeDeg(lat2);
        p2.setLongitudeDeg(lon2);
        p2.setElevationM(0);
        return p2;
    }*/


    // given lat1, lon1, lat2, lon2, calculate starting and ending
// az1, az2 and distance (s).  Lat, lon, and azimuth are in degrees.
// distance in meters
    static int _geo_inverse_wgs_84(Degree lat1, Degree lon1, Degree lat2,
                                   Degree lon2, GIW84Result res/*double*az1, double*az2,   double*s*/) {
        double a = SGGeodesy.EQURAD, rf = SGGeodesy.iFLATTENING;
        int iter = 0;
        double testv = 1.0E-10;
        double f = (rf > 0.0 ? 1.0 / rf : 0.0);
        double b = a * (1.0 - f);
        // double e2 = f*(2.0-f); // unused in this routine
        double phi1 = MathUtil2.toRadians(lat1.getDegree()), lam1 = MathUtil2.toRadians(lon1.getDegree());
        double sinphi1 = Math.sin(phi1), cosphi1 = Math.cos(phi1);
        double phi2 = MathUtil2.toRadians(lat2.getDegree()), lam2 = MathUtil2.toRadians(lon2.getDegree());
        double sinphi2 = Math.sin(phi2), cosphi2 = Math.cos(phi2);

        if ((Math.abs(lat1.getDegree() - lat2.getDegree()) < testv &&
                (Math.abs(lon1.getDegree() - lon2.getDegree()) < testv)) || (Math.abs(lat1.getDegree() - 90.0) < testv)) {
            // TWO STATIONS ARE IDENTICAL : SET DISTANCE & AZIMUTHS TO ZERO

            res.az1 = 0.0;
            res.az2 = 0.0;
            res.s = 0.0;
            return 0;
        } else if (Math.abs(cosphi1) < testv) {
            // initial point isType polar
            int k = _geo_inverse_wgs_84(lat2, lon2, lat1, lon1, res/*az1, az2, s*/);
            //SG_UNUSED(k);

            b = res.az1;
            res.az1 = res.az2;
            res.az2 = b;
            return 0;
        } else if (Math.abs(cosphi2) < testv) {
            // terminal point isType polar
            double _lon1 = lon1.getDegree() + 180.0f;
            int k = _geo_inverse_wgs_84(lat1, lon1, lat1, new Degree(_lon1), res/* az1, az2, s*/);
            //SG_UNUSED(k);

            res.s /= 2.0;
            res.az2 = res.az1 + 180.0;
            if (res.az2 > 360.0) res.az2 -= 360.0;
            return 0;
        } else if ((Math.abs(Math.abs(lon1.getDegree() - lon2.getDegree()) - 180) < testv) &&
                (Math.abs(lat1.getDegree() + lat2.getDegree()) < testv)) {
            // Geodesic passes through the pole (antipodal)
            double s1, s2;
            GIW84Result res1 = new GIW84Result();
            res1.az1 = res.az1;
            res1.az2 = res.az2;
            _geo_inverse_wgs_84(lat1, lon1, lat1, lon2, res1/*az1, az2, & s1*/);
            s1 = res1.s;
            GIW84Result res2 = new GIW84Result();
            res2.az1 = res.az1;
            res2.az2 = res.az2;
            _geo_inverse_wgs_84(lat2, lon2, lat1, lon2, res2/*az1, az2, & s2*/);
            s2 = res2.s;
            res.az2 = res.az1;
            res.s = s1 + s2;
            return 0;
        } else {
            // antipodal and polar points don't get here
            double dlam = lam2 - lam1, dlams = dlam;
            double sdlams, cdlams, sig, sinsig, cossig, sinaz,
                    cos2saz, c2sigm;
            double tc, temp, us, rnumer, denom, ta, tb;
            double cosu1, sinu1, sinu2, cosu2;

            // Reduced latitudes
            temp = (1.0 - f) * sinphi1 / cosphi1;
            cosu1 = 1.0 / Math.sqrt(1.0 + temp * temp);
            sinu1 = temp * cosu1;
            temp = (1.0 - f) * sinphi2 / cosphi2;
            cosu2 = 1.0 / Math.sqrt(1.0 + temp * temp);
            sinu2 = temp * cosu2;

            do {
                sdlams = Math.sin(dlams);
                cdlams = Math.cos(dlams);
                sinsig = Math.sqrt(cosu2 * cosu2 * sdlams * sdlams +
                        (cosu1 * sinu2 - sinu1 * cosu2 * cdlams) *
                                (cosu1 * sinu2 - sinu1 * cosu2 * cdlams));
                cossig = sinu1 * sinu2 + cosu1 * cosu2 * cdlams;

                sig = Math.atan2(sinsig, cossig);
                sinaz = cosu1 * cosu2 * sdlams / sinsig;
                cos2saz = 1.0 - sinaz * sinaz;
                c2sigm = (sinu1 == 0.0 || sinu2 == 0.0 ? cossig :
                        cossig - 2.0 * sinu1 * sinu2 / cos2saz);
                tc = f * cos2saz * (4.0 + f * (4.0 - 3.0 * cos2saz)) / 16.0;
                temp = dlams;
                dlams = dlam + (1.0 - tc) * f * sinaz *
                        (sig + tc * sinsig *
                                (c2sigm + tc * cossig * (-1.0 + 2.0 * c2sigm * c2sigm)));
                if (Math.abs(dlams) > MathUtil2.PI/*SGMiscd::pi ()*/ && iter++ > 50) {
                    return iter;
                }
            } while (Math.abs(temp - dlams) > testv);

            us = cos2saz * (a * a - b * b) / (b * b); // !!
            // BACK AZIMUTH FROM NORTH
            rnumer = -(cosu1 * sdlams);
            denom = sinu1 * cosu2 - cosu1 * sinu2 * cdlams;
            res.az2 = MathUtil2.toDegrees(Math.atan2(rnumer, denom));
            if (Math.abs(res.az2) < testv) {
                res.az2 = 0.0f;
            }
            if (res.az2 < 0.0) {
                res.az2 += 360.0f;
            }

            // FORWARD AZIMUTH FROM NORTH
            rnumer = cosu2 * sdlams;
            denom = cosu1 * sinu2 - sinu1 * cosu2 * cdlams;
            res.az1 = MathUtil2.toDegrees(Math.atan2(rnumer, denom));
            if (Math.abs(res.az1) < testv) {
                res.az1 = 0.0;
            }
            if (res.az1 < 0.0) {
                res.az1 += 360.0;
            }

            // Terms a & b
            ta = 1.0 + us * (4096.0 + us * (-768.0 + us * (320.0 - 175.0 * us))) /
                    16384.0;
            tb = us * (256.0 + us * (-128.0 + us * (74.0 - 47.0 * us))) / 1024.0;

            // GEODETIC DISTANCE
            res.s = b * ta * (sig - tb * sinsig *
                    (c2sigm + tb * (cossig * (-1.0 + 2.0 * c2sigm * c2sigm) - tb *
                            c2sigm * (-3.0 + 4.0 * sinsig * sinsig) *
                            (-3.0 + 4.0 * c2sigm * c2sigm) / 6.0) /
                            4.0));
            return 0;
        }
    }

    /*bool
    SGGeodesy::

    inverse(const SGGeod&p1, const SGGeod&p2, double&course1,
            double&course2, double&distance) {
        int ret = _geo_inverse_wgs_84(p1.getLatitudeDeg(), p1.getLongitudeDeg(),
                p2.getLatitudeDeg(), p2.getLongitudeDeg(),
                & course1,&course2,&distance);
        return ret == 0;
    }*/

    public static Degree courseDeg(LatLon p1, LatLon p2) {
        double course1, course2, distance;
        GIW84Result res = new GIW84Result();

        int r = _geo_inverse_wgs_84(p1.getLatDeg(), p1.getLonDeg(),
                p2.getLatDeg(), p2.getLonDeg(), res/*
                & course1,&course2,&distance*/);
        if (r != 0) {
            logger.warn("SGGeodesy::courseDeg, unable to compute course");
            return new Degree(0);
        }
        course1 = res.az1;

        return new Degree(course1);
    }

    
    /*double
    SGGeodesy::

    distanceM(const SGGeod&p1, const SGGeod&p2) {
        double course1, course2, distance;
        int r = _geo_inverse_wgs_84(p1.getLatitudeDeg(), p1.getLongitudeDeg(),
                p2.getLatitudeDeg(), p2.getLongitudeDeg(),
                & course1,&course2,&distance);
        if (r != 0) {
            throw sg_exception("SGGeodesy::distanceM, unable to compute distance");
        }

        return distance;
    }

    double
    SGGeodesy::

    distanceNm(const SGGeod&from, const SGGeod&to) {
        return distanceM(from, to) * SG_METER_TO_NM;
    }

/// Geocentric routines

    void
    SGGeodesy::

    advanceRadM(const SGGeoc&geoc, double course, double distance,
                SGGeoc&result) {
        result.setRadiusM(geoc.getRadiusM());

        // lat=asin(sin(lat1)*cos(d)+cos(lat1)*sin(d)*cos(tc))
        // IF (cos(lat)=0)
        //   lon=lon1      // endpoint a pole
        // ELSE
        //   lon=mod(lon1-asin(sin(tc)*sin(d)/cos(lat))+pi,2*pi)-pi
        // ENDIF

        distance *= SG_METER_TO_NM * SG_NM_TO_RAD;

        double sinDistance = sin(distance);
        double cosDistance = cos(distance);

        double sinLat = sin(geoc.getLatitudeRad()) * cosDistance +
                cos(geoc.getLatitudeRad()) * sinDistance * cos(course);
        sinLat = SGMiscd::clip (sinLat, -1, 1);
        result.setLatitudeRad(asin(sinLat));
        double cosLat = cos(result.getLatitudeRad());


        if (cosLat <= SGLimitsd::min ()){
            // endpoint a pole
            result.setLongitudeRad(geoc.getLongitudeRad());
        }else{
            double tmp = SGMiscd::clip (sin(course) * sinDistance / cosLat, -1, 1);
            double lon = SGMiscd::normalizeAngle (-geoc.getLongitudeRad() - asin(tmp));
            result.setLongitudeRad(-lon);
        }
    }

    double
    SGGeodesy::

    courseRad(const SGGeoc&from, const SGGeoc&to) {
        //double diffLon = to.getLongitudeRad() - from.getLongitudeRad();
        double diffLon = from.getLongitudeRad() - to.getLongitudeRad();

        double sinLatFrom = sin(from.getLatitudeRad());
        double cosLatFrom = cos(from.getLatitudeRad());

        double sinLatTo = sin(to.getLatitudeRad());
        double cosLatTo = cos(to.getLatitudeRad());

        double x = cosLatTo * sin(diffLon);
        double y = cosLatFrom * sinLatTo - sinLatFrom * cosLatTo * cos(diffLon);

        // guard atan2 returning NaN's
        if (fabs(x) <= SGLimitsd::min () && fabs(y) <= SGLimitsd::min ())
        return 0;

        double c = atan2(x, y);
        if (c >= 0)
            return SGMiscd::twopi () - c;
        else
        return -c;
    }

    double
    SGGeodesy::

    distanceRad(const SGGeoc&from, const SGGeoc&to) {
        // d = 2*asin(sqrt((sin((lat1-lat2)/2))^2 +
        //            cos(lat1)*cos(lat2)*(sin((lon1-lon2)/2))^2))
        double cosLatFrom = cos(from.getLatitudeRad());
        double cosLatTo = cos(to.getLatitudeRad());
        double tmp1 = sin(0.5 * (from.getLatitudeRad() - to.getLatitudeRad()));
        double tmp2 = sin(0.5 * (from.getLongitudeRad() - to.getLongitudeRad()));
        double square = tmp1 * tmp1 + cosLatFrom * cosLatTo * tmp2 * tmp2;
        double s = SGMiscd::min (sqrt(SGMiscd::max (square, 0)),1);
        return 2 * asin(s);
    }


    double
    SGGeodesy::

    distanceM(const SGGeoc&from, const SGGeoc&to) {
        return distanceRad(from, to) * SG_RAD_TO_NM * SG_NM_TO_METER;
    }

    bool
    SGGeodesy::

    radialIntersection(const SGGeoc&a, double r1,
                       const SGGeoc&b, double r2, SGGeoc&result) {
        // implementation of
        // http://williams.best.vwh.net/avform.htm#Intersection

        double crs13 = r1 * SG_DEGREES_TO_RADIANS;
        double crs23 = r2 * SG_DEGREES_TO_RADIANS;
        double dst12 = SGGeodesy::distanceRad (a, b);

        //IF sin(lon2-lon1)<0
        // crs12=acos((sin(lat2)-sin(lat1)*cos(dst12))/(sin(dst12)*cos(lat1)))
        // crs21=2.*pi-acos((sin(lat1)-sin(lat2)*cos(dst12))/(sin(dst12)*cos(lat2)))
        // ELSE
        // crs12=2.*pi-acos((sin(lat2)-sin(lat1)*cos(dst12))/(sin(dst12)*cos(lat1)))
        // crs21=acos((sin(lat1)-sin(lat2)*cos(dst12))/(sin(dst12)*cos(lat2)))
        // ENDIF
        double crs12 = SGGeodesy::courseRad (a, b),
        crs21 = SGGeodesy::courseRad (b, a);

        double sinLat1 = sin(a.getLatitudeRad());
        double cosLat1 = cos(a.getLatitudeRad());
        double sinDst12 = sin(dst12);
        double cosDst12 = cos(dst12);

        double ang1 = SGMiscd::normalizeAngle2 (crs13 - crs12);
        double ang2 = SGMiscd::normalizeAngle2 (crs21 - crs23);

        if ((sin(ang1) == 0.0) && (sin(ang2) == 0.0)) {
            SG_LOG(SG_GENERAL, SG_WARN, "SGGeodesy::radialIntersection: infinity of intersections");
            return false;
        }

        if ((sin(ang1) * sin(ang2)) < 0.0) {
            SG_LOG(SG_GENERAL, SG_WARN, "SGGeodesy::radialIntersection: intersection ambiguous");
            return false;
        }

        ang1 = fabs(ang1);
        ang2 = fabs(ang2);

        //ang3=acos(-cos(ang1)*cos(ang2)+sin(ang1)*sin(ang2)*cos(dst12))
        //dst13=atan2(sin(dst12)*sin(ang1)*sin(ang2),cos(ang2)+cos(ang1)*cos(ang3))
        //lat3=asin(sin(lat1)*cos(dst13)+cos(lat1)*sin(dst13)*cos(crs13))
        //lon3=mod(lon1-dlon+pi,2*pi)-pi

        double ang3 = acos(-cos(ang1) * cos(ang2) + sin(ang1) * sin(ang2) * cosDst12);
        double dst13 = atan2(sinDst12 * sin(ang1) * sin(ang2), cos(ang2) + cos(ang1) * cos(ang3));
        double lat3 = asin(sinLat1 * cos(dst13) + cosLat1 * sin(dst13) * cos(crs13));
        //dlon=atan2(sin(crs13)*sin(dst13)*cos(lat1),cos(dst13)-sin(lat1)*sin(lat3))
        double dlon = atan2(sin(crs13) * sin(dst13) * cosLat1, cos(dst13) - (sinLat1 * sin(lat3)));
        double lon3 = SGMiscd::normalizeAngle (-a.getLongitudeRad() - dlon);

        result = SGGeoc::fromRadM (-lon3, lat3, a.getRadiusM());
        return true;
    }

    bool
    SGGeodesy::

    radialIntersection(const SGGeod&a, double aRadial,
                       const SGGeod&b, double bRadial, SGGeod&result) {
        SGGeoc r;
        bool ok = radialIntersection(SGGeoc::fromGeod (a), aRadial,
                SGGeoc::fromGeod (b), bRadial, r);
        if (!ok) {
            return false;
        }

        result = SGGeod::fromGeoc (r);
        return true;
    }
*/
}

class GIW84Result {
    double az1, az2, s;
}