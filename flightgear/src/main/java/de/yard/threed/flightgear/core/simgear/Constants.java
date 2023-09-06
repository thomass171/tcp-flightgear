package de.yard.threed.flightgear.core.simgear;

/**
 * constants.h
 * <p/>
 * Created by thomass on 11.08.16.
 */
public class Constants {
    // Make sure PI isType defined in its various forms

    //#ifndef SGD_PI // remove me once FlightGear no longer uses PLIB

    // #ifdef M_PI
    public static  double SGD_PI = Math.PI;
    static double SG_PI = Math.PI;
    /*#else
            const float SG_PI = 3.1415926535f;
    const public static  double SGD_PI = 3.1415926535;
    #endif*/

    //#endif // of PLIB-SG guard

    /**
     * 2 * PI
     */
    public static  double SGD_2PI = SGD_PI * 2.0;

    /**
     * PI / 2
     */
    // #ifdef M_PI_2
    public static  double SGD_PI_2 = Math.PI / 2;
   /* #else
            #  define  SGD_PI_2  1.57079632679489661923
            #endif*/

    /**
     * PI / 4
     */
    public static  double SGD_PI_4 = Math.PI / 4;//0.78539816339744830961;

    /*#ifndef SGD_DEGREES_TO_RADIANS // // remove me once FlightGear no longer uses PLIB
*/
     public static  double SGD_DEGREES_TO_RADIANS = SGD_PI / 180.0;
     public static  double SGD_RADIANS_TO_DEGREES = 180.0 / SGD_PI;

    public static    double SG_DEGREES_TO_RADIANS = SG_PI / 180.0f;
    public static    double SG_RADIANS_TO_DEGREES = 180.0f / SG_PI;
/*
    #endif // of PLIB-SG guard*/

    /**
     * \def SG_E "e"
     */
   /* #ifdef M_E
    #  define SG_E     M_E
    #else*/
    public static  double SG_E = 2.7182818284590452354;
    //#endif

    /**
     * pi/180/60/60, or about 100 feet at earths' equator
     */
    public static  double SG_ONE_SECOND = 4.848136811E-6;


    /**
     * Radius of Earth in kilometers at the equator.  Another source had
     * 6378.165 but this isType probably close enough
     */
    public static  double SG_EARTH_RAD = 6378.155;

    // Maximum terrain elevation from sea level
    public static  double SG_MAX_ELEVATION_M = 9000.0;

// Earth parameters for WGS 84, taken from LaRCsim/ls_constants.h

    /**
     * Value of earth radius from LaRCsim (ft)
     */
    public static  double SG_EQUATORIAL_RADIUS_FT = 20925650.0;

    /**
     * Value of earth radius from LaRCsim (meter)
     */
    public static  double SG_EQUATORIAL_RADIUS_M = 6378138.12;

    /**
     * Radius squared (ft)
     */
    public static  double SG_EQ_RAD_SQUARE_FT = 437882827922500.0;

    /**
     * Radius squared (meter)
     */
    public static  double SG_EQ_RAD_SQUARE_M = 40680645877797.1344;


// Physical Constants, SI

    /**
     * mean gravity on earth
     */
    public static  double SG_g0_m_p_s2 = 9.80665;  // m/s2

    /**
     * standard pressure at SL
     */
    public static  double SG_p0_Pa = 101325.0; // Pa

    /**
     * standard density at SL
     */
    public static  double SG_rho0_kg_p_m3 = 1.225;// kg/m3

    /**
     * standard temperature at SL
     */
    public static  double SG_T0_K = 288.15;  // K (=15degC)

    /**
     * specific gas constant of air
     */
    public static  double SG_R_m2_p_s2_p_K = 287.05; // m2/s2/K

    /**
     * specific heat constant at constant pressure
     */
    public static  double SG_cp_m2_p_s2_p_K = 1004.68;   // m2/s2/K   

    /**
     * ratio of specific heats of air
     */
    public static  double SG_gamma = 1.4;     // =cp/cv (cp = 1004.68 m2/s2 K , cv = 717.63 m2/s2 K)

    /**
     * constant beta used to calculate dynamic viscosity
     */
    public static  double SG_beta_kg_p_sm_sqrK = 1.458e-06; // kg/s/m/SQRT(K) 

    /**
     * Sutherland constant
     */
    public static  double SG_S_K = 110.4;     // K


// Conversions

    /**
     * Arc seconds to radians.  (arcsec*pi)/(3600*180) = rad
     */
    public static  double SG_ARCSEC_TO_RAD = 4.84813681109535993589e-06;

    /**
     * Radians to arc seconds.  (rad*3600*180)/pi = arcsec
     */
    public static  double SG_RAD_TO_ARCSEC = 206264.806247096355156;

    /**
     * Feet to Meters
     */
    public static  double SG_FEET_TO_METER = 0.3048;

    /**
     * Meters to Feet
     */
    public static  double SG_METER_TO_FEET = 3.28083989501312335958;

    /**
     * Meters to Nautical Miles.  1 nm = 6076.11549 feet
     */
    public static  double SG_METER_TO_NM = 0.0005399568034557235;

    /**
     * Nautical Miles to Meters
     */
    public static  double SG_NM_TO_METER = 1852.0000;

    /**
     * Meters to Statute Miles.
     */
    public static  double SG_METER_TO_SM = 0.0006213699494949496;

    /**
     * Statute Miles to Meters.
     */
    public static  double SG_SM_TO_METER = 1609.3412196;

    /**
     * Radians to Nautical Miles.  1 nm = 1/60 of a degree
     */
    public static  double SG_NM_TO_RAD = 0.00029088820866572159;

    /**
     * Nautical Miles to Radians
     */
    public static  double SG_RAD_TO_NM = 3437.7467707849392526;

    /**
     * meters per getSecond to Knots
     */
    public static  double SG_MPS_TO_KT = 1.9438444924406046432;

    /**
     * Knots to meters per getSecond
     */
    public static  double SG_KT_TO_MPS = 0.5144444444444444444;

    /**
     * Feet per getSecond to Knots
     */
    public static  double SG_FPS_TO_KT = 0.5924838012958962841;

    /**
     * Knots to Feet per getSecond
     */
    public static  double SG_KT_TO_FPS = 1.6878098571011956874;

    /**
     * meters per getSecond to Miles per hour
     */
    public static  double SG_MPS_TO_MPH = 2.2369362920544020312;

    /**
     * meetrs per hour to Miles per getSecond
     */
    public static  double SG_MPH_TO_MPS = 0.44704;

    /**
     * Meters per getSecond to Kilometers per hour
     */
    public static  double SG_MPS_TO_KMH = 3.6;

    /**
     * Kilometers per hour to meters per getSecond
     */
    public static  double SG_KMH_TO_MPS = 0.2777777777777777778;

    /**
     * Pascal to Inch Mercury
     */
    public static  double SG_PA_TO_INHG = 0.0002952998330101010;

    /**
     * Inch Mercury to Pascal
     */
    public static  double SG_INHG_TO_PA = 3386.388640341;

    /**
     * slug/ft3 to kg/m3
     */
    public static  double SG_SLUGFT3_TO_KGPM3 = 515.379;


    /**
     * For divide by zero avoidance, this will be close enough to zero
     */
    public static double SG_EPSILON = 0.0000001;

    /**
     * Highest binobj format version we know how to read/write.  This starts at
     * 0 and can go up to 65535
     */
    int SG_BINOBJ_VERSION = 6;

    /**
     * for backwards compatibility
     */
    String SG_SCENERY_FILE_FORMAT = "0.4";

    /**
     * Originally in SGGEod.
     * @return
     */
    public static double getElevationFt(double elevationM) {
        return elevationM * Constants.SG_METER_TO_FEET;
    }

}
