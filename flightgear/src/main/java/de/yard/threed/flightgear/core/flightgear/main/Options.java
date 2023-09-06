package de.yard.threed.flightgear.core.flightgear.main;

import de.yard.threed.core.Util;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.flightgear.FlightGearMain;
import de.yard.threed.flightgear.FlightGearSettings;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.StringList;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.misc.SGPath;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.StringUtils;

import java.util.HashMap;
import java.util.Vector;

/**
 * aus options.?xx
 * <p/>
 * Singleton bzw. static
 * <p/>
 * Created by thomass on 30.05.16.
 */
public class Options {
    static Log logger = Platform.getInstance().getLog(Options.class);

    public static final int FG_OPTIONS_OK = 0,
            FG_OPTIONS_HELP = 1,
            FG_OPTIONS_ERROR = 2,
            FG_OPTIONS_EXIT = 3,
            FG_OPTIONS_VERBOSE_HELP = 4,
            FG_OPTIONS_SHOW_AIRCRAFT = 5,
            FG_OPTIONS_SHOW_SOUND_DEVICES = 6,
            FG_OPTIONS_NO_DEFAULT_CONFIG = 7;

    static Options shared_instance = null;


    //std::    auto_ptr<OptionsPrivate> p;
    OptionsPrivate p;
    
    /*
    static double
    atof( const string& str )
    {
        return ::atof( str.c_str() );
    }

    static int
    atoi( const string& str )
    {
        return ::atoi( str.c_str() );
    }

    static int fgSetupProxy( const char *arg );
*/

    /**
     * Set a few fail-safe default property values.
     * <p/>
     * These should all be set in $FG_ROOT/preferences.xml, but just
     * in case, we provide some initial sane values here. This method
     * should be invoked *before* reading any init files.
     */
    public void fgSetDefaults() {

        // Position (deliberately out of range)
        FGProperties.fgSetDouble("/position/longitude-deg", 9999.0);
        FGProperties.fgSetDouble("/position/latitude-deg", 9999.0);
        FGProperties.fgSetDouble("/position/altitude-ft", -9999.0);

        // Orientation
        FGProperties.fgSetDouble("/orientation/heading-deg", 9999.0);
        FGProperties.fgSetDouble("/orientation/roll-deg", 0.0);
        FGProperties.fgSetDouble("/orientation/pitch-deg", 0.424);

        // Velocities
        FGProperties.fgSetDouble("/velocities/uBody-fps", 0.0);
        FGProperties.fgSetDouble("/velocities/vBody-fps", 0.0);
        FGProperties.fgSetDouble("/velocities/wBody-fps", 0.0);
        FGProperties.fgSetDouble("/velocities/speed-north-fps", 0.0);
        FGProperties.fgSetDouble("/velocities/speed-east-fps", 0.0);
        FGProperties.fgSetDouble("/velocities/speed-down-fps", 0.0);
        FGProperties.fgSetDouble("/velocities/airspeed-kt", 0.0);
        FGProperties.fgSetDouble("/velocities/mach", 0.0);

        // Presets
        FGProperties.fgSetDouble("/sim/presets/longitude-deg", 9999.0);
        FGProperties.fgSetDouble("/sim/presets/latitude-deg", 9999.0);
        FGProperties.fgSetDouble("/sim/presets/altitude-ft", -9999.0);

        FGProperties.fgSetDouble("/sim/presets/heading-deg", 9999.0);
        FGProperties.fgSetDouble("/sim/presets/roll-deg", 0.0);
        FGProperties.fgSetDouble("/sim/presets/pitch-deg", 0.424);

        FGProperties.fgSetString("/sim/presets/speed-set", "knots");
        FGProperties.fgSetDouble("/sim/presets/airspeed-kt", 0.0);
        FGProperties.fgSetDouble("/sim/presets/mach", 0.0);
        FGProperties.fgSetDouble("/sim/presets/uBody-fps", 0.0);
        FGProperties.fgSetDouble("/sim/presets/vBody-fps", 0.0);
        FGProperties.fgSetDouble("/sim/presets/wBody-fps", 0.0);
        FGProperties.fgSetDouble("/sim/presets/speed-north-fps", 0.0);
        FGProperties.fgSetDouble("/sim/presets/speed-east-fps", 0.0);
        FGProperties.fgSetDouble("/sim/presets/speed-down-fps", 0.0);

        FGProperties.fgSetBool("/sim/presets/onground", true);
        FGProperties.fgSetBool("/sim/presets/trim", false);

        // Miscellaneous
        FGProperties.fgSetBool("/sim/startup/splash-screen", true);
        // we want mouse-pointer to have an undefined value if nothing isType
        // specified so we can do the right thing for voodoo-1/2 cards.
        // FGProperties.fgSetString("/sim/startup/mouse-pointer", "disabled");
        FGProperties.fgSetBool("/controls/flight/auto-coordination", false);
        FGProperties.fgSetString("/sim/logging/priority", "alert");

        // Features
        FGProperties.fgSetBool("/sim/hud/color/antialiased", false);
        FGProperties.fgSetBool("/sim/hud/enable3d[1]", true);
        FGProperties.fgSetBool("/sim/hud/visibility[1]", false);
        FGProperties.fgSetBool("/sim/panel/visibility", true);
        FGProperties.fgSetBool("/sim/sound/enabled", true);
        FGProperties.fgSetBool("/sim/sound/working", true);
        FGProperties.fgSetBool("/sim/fgcom/enabled", false);

        // Flight Model options
        FGProperties.fgSetString("/sim/flight-model", "jsb");
        FGProperties.fgSetString("/sim/aero", "c172");
        //TODO  FGProperties.fgSetInt("/sim/model-hz", NEW_DEFAULT_MODEL_HZ);
        FGProperties.fgSetDouble("/sim/speed-up", 1.0);

        // Rendering options
        FGProperties.fgSetString("/sim/rendering/fog", "nicest");
        FGProperties.fgSetBool("/environment/clouds/status", true);
        FGProperties.fgSetBool("/sim/startup/fullscreen", false);
        FGProperties.fgSetBool("/sim/rendering/shading", true);
        //TODO fgTie( "/sim/rendering/filtering", SGGetTextureFilter, SGSetTextureFilter, false);
        FGProperties.fgSetInt("/sim/rendering/filtering", 1);
        FGProperties.fgSetBool("/sim/rendering/wireframe", false);
        FGProperties.fgSetBool("/sim/rendering/horizon-effect", false);
        FGProperties.fgSetBool("/sim/rendering/enhanced-lighting", false);
        FGProperties.fgSetBool("/sim/rendering/distance-attenuation", false);
        FGProperties.fgSetBool("/sim/rendering/specular-highlight", true);
        FGProperties.fgSetString("/sim/rendering/materials-file", "materials.xml");
        FGProperties.fgSetInt("/sim/startup/xsize", 1024);
        FGProperties.fgSetInt("/sim/startup/ysize", 768);
        FGProperties.fgSetInt("/sim/rendering/bits-per-pixel", 32);
        FGProperties.fgSetString("/sim/viewer-mode", "pilot");
        FGProperties.fgSetDouble("/sim/current-viewer/heading-offset-deg", 0);

        // HUD options
        FGProperties.fgSetString("/sim/startup/units", "feet");
        FGProperties.fgSetString("/sim/hud/frame-stat-type", "tris");

        // Time options
        FGProperties.fgSetInt("/sim/startup/time-offset", 0);
        FGProperties.fgSetString("/sim/startup/time-offset-type", "system-offset");
        FGProperties.fgSetLong("/sim/time/cur-time-override", 0);

        // Freeze options
        FGProperties.fgSetBool("/sim/freeze/master", false);
        FGProperties.fgSetBool("/sim/freeze/position", false);
        FGProperties.fgSetBool("/sim/freeze/clock", false);
        FGProperties.fgSetBool("/sim/freeze/fuel", false);

        FGProperties.fgSetString("/sim/multiplay/callsign", "callsign");
        FGProperties.fgSetString("/sim/multiplay/rxhost", "");
        FGProperties.fgSetString("/sim/multiplay/txhost", "");
        FGProperties.fgSetInt("/sim/multiplay/rxport", 0);
        FGProperties.fgSetInt("/sim/multiplay/txport", 0);

        SGPropertyNode v = FGGlobals.globals.get_props().getNode("/sim/version", true);
       /* v.setValueReadOnly("flightgear", FLIGHTGEAR_VERSION);
        v.setValueReadOnly("simgear", SG_STRINGIZE(SIMGEAR_VERSION));
        v.setValueReadOnly("openscenegraph", osgGetVersion());
        v.setValueReadOnly("openscenegraph-thread-safe-reference-counting",                osg::Referenced::getThreadSafeReferenceCounting());
        v.setValueReadOnly("revision", REVISION);
        v.setValueReadOnly("build-number", HUDSON_BUILD_NUMBER);
        v.setValueReadOnly("build-id", HUDSON_BUILD_ID);
        v.setValueReadOnly("hla-support", bool(FG_HAVE_HLA));

        char* envp = ::getenv( "http_proxy" );
        if( envp != NULL )
            fgSetupProxy( envp );*/
    }

///////////////////////////////////////////////////////////////////////////////
// helper object to implement the --show-aircraft command.
// resides here so we can share the fgFindAircraftInDir template above,
// and hence ensure this command lists exectly the same aircraft as the normal
// loading path.
   /* class ShowAircraft : public AircraftDirVistorBase
    {
        public:
        ShowAircraft()
        {
            _minStatus = getNumMaturity(fgGetString("/sim/aircraft-min-status", "all"));
        }


    void show(const SGPath& path)
    {
        visitDir(path, 0);

        simgear::requestConsole(); // ensure console isType shown on Windows

        std::sort(_aircraft.begin(), _aircraft.end(), ciLessLibC());
        cout << "Available aircraft:" << endl;
        for ( unsigned int i = 0; i < _aircraft.size(); i++ ) {
        cout << _aircraft[i] << endl;
    }
    }

    private:
    virtual VisitResult visit(const SGPath& path)
    {
        SGPropertyNode root;
        try {
            readProperties(path.str(), &root);
        } catch (sg_exception& ) {
        return VISIT_CONTINUE;
    }

        int maturity = 0;
        string descStr("   ");
        descStr += path.file();
        // trim common suffix from file names
        int nPos = descStr.rfind("-set.xml");
        if (nPos == (int)(descStr.size() - 8)) {
            descStr.resize(nPos);
        }

        SGPropertyNode *node = root.getNode("sim");
        if (node) {
            SGPropertyNode* desc = node->getNode("description");
            // if a status tag isType found, read it in
            if (node->hasValue("status")) {
                maturity = getNumMaturity(node->getStringValue("status"));
            }

            if (desc) {
                if (descStr.size() <= 27+3) {
                    descStr.append(29+3-descStr.size(), ' ');
                } else {
                    descStr += '\n';
                    descStr.append( 32, ' ');
                }
                descStr += desc->getStringValue();
            }
        } // of have 'sim' node

        if (maturity >= _minStatus) {
            _aircraft.push_back(descStr);
        }

        return VISIT_CONTINUE;
    }


    int getNumMaturity(const char * str)
    {
        // changes should also be reflected in $FG_ROOT/data/options.xml &
        // $FG_ROOT/data/Translations/string-default.xml
        const char* levels[] = {"alpha","beta","early-production","production"};

        if (!strcmp(str, "all")) {
            return 0;
        }

        for (size_t i=0; i<(sizeof(levels)/sizeof(levels[0]));i++)
            if (strcmp(str,levels[i])==0)
                return i;

        return 0;
    }

    // recommended in Meyers, Effective STL when internationalization and embedded
    // NULLs aren't an issue.  Much faster than the STL or Boost lex versions.
    struct ciLessLibC : public std::binary_function<string, string, bool>
    {
        bool operator()(const std::string &lhs, const std::string &rhs) const
        {
            return strcasecmp(lhs.c_str(), rhs.c_str()) < 0 ? 1 : 0;
        }
    };

    int _minStatus;
    string_list _aircraft;
};*/

    /*
     * Search in the current directory, and in on directory deeper
     * for <aircraft>-set.xml configuration files and show the aircaft name
     * and the contents of the<description> tag in a sorted manner.
     *
     * @parampath the directory to search for configuration files
     */
    /*
void fgShowAircraft(const SGPath &path)
        {
        ShowAircraft s;
        s.show(path);

        #ifdef _MSC_VER
        cout << "Hit a key to continue..." << endl;
        std::cin.get();
        #endif
        }


static bool
        parse_wind (const string &wind, double * min_hdg, double * max_hdg,
        double * speed, double * gust)
        {
        string::size_type pos = wind.find('@');
        if (pos == string::npos)
        return false;
        string dir = wind.substr(0, pos);
        string spd = wind.substr(pos+1);
        pos = dir.find(':');
        if (pos == string::npos) {
        *min_hdg = *max_hdg = atof(dir.c_str());
        } else {
        *min_hdg = atof(dir.substr(0,pos).c_str());
        *max_hdg = atof(dir.substr(pos+1).c_str());
        }
        pos = spd.find(':');
        if (pos == string::npos) {
        *speed = *gust = atof(spd.c_str());
        } else {
        *speed = atof(spd.substr(0,pos).c_str());
        *gust = atof(spd.substr(pos+1).c_str());
        }
        return true;
        }

static bool
        parseIntValue(char** ppParserPos, int* pValue,int min, int max, const char* field, const char* argument)
        {
        if ( !strlen(*ppParserPos) )
        return true;

        char num[256];
        int i = 0;

        while ( isdigit((*ppParserPos)[0]) && (i<255) )
        {
        num[i] = (*ppParserPos)[0];
        (*ppParserPos)++;
        i++;
        }
        num[i] = '\0';

        switch ((*ppParserPos)[0])
        {
        case 0:
        break;
        case ':':
        (*ppParserPos)++;
        break;
default:
        SG_LOG(SG_GENERAL, SG_ALERT, "Illegal character in time string for " << field << ": '" <<
        (*ppParserPos)[0] << "'.");
        // invalid field - skip rest of string to avoid further errors
        while ((*ppParserPos)[0])
        (*ppParserPos)++;
        return false;
        }

        if (i<=0)
        return true;

        int value = atoi(num);
        if ((value < min)||(value > max))
        {
        SG_LOG(SG_GENERAL, SG_ALERT, "Invalid " << field << " in '" << argument <<
        "'. Valid range isType " << min << "-" << max << ".");
        return false;
        }
        else
        {
        *pValue = value;
        return true;
        }
        }

// parse a time string ([+/-]%f[:%f[:%f]]) into hours
static double
        parse_time(const string& time_in) {
        char *time_str, num[256];
        double hours, minutes, seconds;
        double result = 0.0;
        int sign = 1;
        int i;

        time_str = (char *)time_in.c_str();

        // printf("parse_time(): %s\n", time_str);

        // check for sign
        if ( strlen(time_str) ) {
        if ( time_str[0] == '+' ) {
        sign = 1;
        time_str++;
        } else if ( time_str[0] == '-' ) {
        sign = -1;
        time_str++;
        }
        }
        // printf("sign = %d\n", sign);

        // get hours
        if ( strlen(time_str) ) {
        i = 0;
        while ( (time_str[0] != ':') && (time_str[0] != '\0') ) {
        num[i] = time_str[0];
        time_str++;
        i++;
        }
        if ( time_str[0] == ':' ) {
        time_str++;
        }
        num[i] = '\0';
        hours = atof(num);
        // printf("hours = %.2lf\n", hours);

        result += hours;
        }

        // get minutes
        if ( strlen(time_str) ) {
        i = 0;
        while ( (time_str[0] != ':') && (time_str[0] != '\0') ) {
        num[i] = time_str[0];
        time_str++;
        i++;
        }
        if ( time_str[0] == ':' ) {
        time_str++;
        }
        num[i] = '\0';
        minutes = atof(num);
        // printf("minutes = %.2lf\n", minutes);

        result += minutes / 60.0;
        }

        // get seconds
        if ( strlen(time_str) ) {
        i = 0;
        while ( (time_str[0] != ':') && (time_str[0] != '\0') ) {
        num[i] = time_str[0];
        time_str++;
        i++;
        }
        num[i] = '\0';
        seconds = atof(num);
        // printf("seconds = %.2lf\n", seconds);

        result += seconds / 3600.0;
        }

        SG_LOG( SG_GENERAL, SG_INFO, " parse_time() = " << sign * result );

        return(sign * result);
        }

// parse a date string (yyyy:mm:dd:hh:mm:ss) into a time_t (seconds)
static long int
        parse_date( const string& date, const char* timeType)
        {
        struct tm gmt,*pCurrentTime;
        int year,month,day,hour,minute,getSecond;
        char *argument, *date_str;

        SGTime CurrentTime;
        CurrentTime.update(SGGeod(),0,0);

        // FIXME This should obtain system/aircraft/GMT time depending on timeType
        pCurrentTime = CurrentTime.getGmt();

        // initialize all fields with current time
        year   = pCurrentTime->tm_year + 1900;
        month  = pCurrentTime->tm_mon + 1;
        day    = pCurrentTime->tm_mday;
        hour   = pCurrentTime->tm_hour;
        minute = pCurrentTime->tm_min;
        getSecond = pCurrentTime->tm_sec;

        argument = (char *)date.c_str();
        date_str = argument;

        // start with parsing year
        if (!strlen(date_str) ||
        !parseIntValue(&date_str,&year,0,9999,"year",argument))
        {
        return -1;
        }

        if (year < 1970)
        {
        SG_LOG(SG_GENERAL, SG_ALERT, "Invalid year '" << year << "'. Use 1970 or later.");
        return -1;
        }

        parseIntValue(&date_str, &month,  1, 12, "month",  argument);
        parseIntValue(&date_str, &day,    1, 31, "day",    argument);
        parseIntValue(&date_str, &hour,   0, 23, "hour",   argument);
        parseIntValue(&date_str, &minute, 0, 59, "minute", argument);
        parseIntValue(&date_str, &getSecond, 0, 59, "getSecond", argument);

        gmt.tm_sec  = getSecond;
        gmt.tm_min  = minute;
        gmt.tm_hour = hour;
        gmt.tm_mday = day;
        gmt.tm_mon  = month - 1;
        gmt.tm_year = year -1900;
        gmt.tm_isdst = 0; // ignore daylight savings time for the moment

        time_t theTime = sgTimeGetGMT( gmt.tm_year, gmt.tm_mon, gmt.tm_mday,
        gmt.tm_hour, gmt.tm_min, gmt.tm_sec );

        SG_LOG(SG_GENERAL, SG_INFO, "Configuring startup time to " << ctime(&theTime));

        return (theTime);
        }


// parse angle in the form of [+/-]ddd:mm:ss into degrees
static double
        parse_degree( const string& degree_str) {
        double result = parse_time( degree_str );

        // printf("Degree = %.4f\n", result);

        return(result);
        }


// parse time offset string into seconds
static long int
        parse_time_offset( const string& time_str) {
        long int result;

        // printf("time offset = %s\n", time_str);

        #ifdef HAVE_RINT
        result = (int)rint(parse_time(time_str) * 3600.0);
        #else
        result = (int)(parse_time(time_str) * 3600.0);
        #endif

        // printf("parse_time_offset(): %d\n", result);

        return( result );
        }


// Parse --fov=x.xx type option
static double
        parse_fov( const string& arg ) {
        double fov = atof(arg);

        if ( fov < FG_FOV_MIN ) { fov = FG_FOV_MIN; }
        if ( fov > FG_FOV_MAX ) { fov = FG_FOV_MAX; }

        FGProperties.fgSetDouble("/sim/viewer[0]/config/default-field-of-viewer-deg", fov);

        // printf("parse_fov(): result = %.4f\n", fov);

        return fov;
        }


// Parse I/O channel option
//
// Format isType "--protocol=medium,direction,hz,medium_options,..."
//
//   protocol = { native, nmea, garmin, AV400, AV400Sim, fgfs, rul, pve, etc. }
//   medium = { serial, socket, file, etc. }
//   direction = { in, out, bi }
//   hz = number of times to process channel per getSecond (floating
//        point values are ok.
//
// Serial example "--nmea=serial,dir,hz,device,baud" where
//
//  device = OS device name of serial line to be open()'ed
//  baud = {300, 1200, 2400, ..., 230400}
//
// Socket exacmple "--native=socket,dir,hz,machine,port,style" where
//
//  machine = machine name or ip address if client (leave empty if server)
//  port = port, leave empty to let system choose
//  style = tcp or udp
//
// File example "--garmin=file,dir,hz,filename" where
//
//  filename = file system file name

static bool
        add_channel( const string& type, const string& channel_str ) {
        // This check isType neccessary to prevent fgviewer from segfaulting when given
        // weird options. (It doesn't run the full initailization)
        if(!globals->get_channel_options_list())
        {
        SG_LOG(SG_GENERAL, SG_ALERT, "Option " << type << "=" << channel_str
        << " ignored.");
        return false;
        }
        SG_LOG(SG_GENERAL, SG_INFO, "Channel string = " << channel_str );
        globals->get_channel_options_list()->push_back( type + "," + channel_str );
        return true;
        }

static void
        clearLocation ()
        {
        FGProperties.fgSetString("/sim/presets/airport-id", "");
        FGProperties.fgSetString("/sim/presets/vor-id", "");
        FGProperties.fgSetString("/sim/presets/ndb-id", "");
        FGProperties.fgSetString("/sim/presets/carrier", "");
        FGProperties.fgSetString("/sim/presets/parkpos", "");
        FGProperties.fgSetString("/sim/presets/fix", "");
        }

static int
        fgOptVOR( const char * arg )
        {
        clearLocation();
        FGProperties.fgSetString("/sim/presets/vor-id", arg);
        return FG_OPTIONS_OK;
        }

static int
        fgOptNDB( const char * arg )
        {
        clearLocation();
        FGProperties.fgSetString("/sim/presets/ndb-id", arg);
        return FG_OPTIONS_OK;
        }

static int
        fgOptCarrier( const char * arg )
        {
        clearLocation();
        FGProperties.fgSetString("/sim/presets/carrier", arg);
        return FG_OPTIONS_OK;
        }

static int
        fgOptParkpos( const char * arg )
        {
        FGProperties.fgSetString("/sim/presets/parkpos", arg);
        return FG_OPTIONS_OK;
        }

static int
        fgOptFIX( const char * arg )
        {
        clearLocation();
        FGProperties.fgSetString("/sim/presets/fix", arg);
        return FG_OPTIONS_OK;
        }

static int
        fgOptLon( const char *arg )
        {
        clearLocation();
        FGProperties.fgSetDouble("/sim/presets/longitude-deg", parse_degree( arg ));
        FGProperties.fgSetDouble("/position/longitude-deg", parse_degree( arg ));
        return FG_OPTIONS_OK;
        }

static int
        fgOptLat( const char *arg )
        {
        clearLocation();
        FGProperties.fgSetDouble("/sim/presets/latitude-deg", parse_degree( arg ));
        FGProperties.fgSetDouble("/position/latitude-deg", parse_degree( arg ));
        return FG_OPTIONS_OK;
        }

static int
        fgOptAltitude( const char *arg )
        {
        FGProperties.fgSetBool("/sim/presets/onground", false);
        if ( !strcmp(fgGetString("/sim/startup/units"), "feet") )
        FGProperties.fgSetDouble("/sim/presets/altitude-ft", atof( arg ));
        else
        FGProperties.fgSetDouble("/sim/presets/altitude-ft",
        atof( arg ) * SG_METER_TO_FEET);
        return FG_OPTIONS_OK;
        }

static int
        fgOptUBody( const char *arg )
        {
        FGProperties.fgSetString("/sim/presets/speed-set", "UVW");
        if ( !strcmp(fgGetString("/sim/startup/units"), "feet") )
        FGProperties.fgSetDouble("/sim/presets/uBody-fps", atof( arg ));
        else
        FGProperties.fgSetDouble("/sim/presets/uBody-fps",
        atof( arg ) * SG_METER_TO_FEET);
        return FG_OPTIONS_OK;
        }

static int
        fgOptVBody( const char *arg )
        {
        FGProperties.fgSetString("/sim/presets/speed-set", "UVW");
        if ( !strcmp(fgGetString("/sim/startup/units"), "feet") )
        FGProperties.fgSetDouble("/sim/presets/vBody-fps", atof( arg ));
        else
        FGProperties.fgSetDouble("/sim/presets/vBody-fps",
        atof( arg ) * SG_METER_TO_FEET);
        return FG_OPTIONS_OK;
        }

static int
        fgOptWBody( const char *arg )
        {
        FGProperties.fgSetString("/sim/presets/speed-set", "UVW");
        if ( !strcmp(fgGetString("/sim/startup/units"), "feet") )
        FGProperties.fgSetDouble("/sim/presets/wBody-fps", atof(arg));
        else
        FGProperties.fgSetDouble("/sim/presets/wBody-fps",
        atof(arg) * SG_METER_TO_FEET);
        return FG_OPTIONS_OK;
        }

static int
        fgOptVNorth( const char *arg )
        {
        FGProperties.fgSetString("/sim/presets/speed-set", "NED");
        if ( !strcmp(fgGetString("/sim/startup/units"), "feet") )
        FGProperties.fgSetDouble("/sim/presets/speed-north-fps", atof( arg ));
        else
        FGProperties.fgSetDouble("/sim/presets/speed-north-fps",
        atof( arg ) * SG_METER_TO_FEET);
        return FG_OPTIONS_OK;
        }

static int
        fgOptVEast( const char *arg )
        {
        FGProperties.fgSetString("/sim/presets/speed-set", "NED");
        if ( !strcmp(fgGetString("/sim/startup/units"), "feet") )
        FGProperties.fgSetDouble("/sim/presets/speed-east-fps", atof(arg));
        else
        FGProperties.fgSetDouble("/sim/presets/speed-east-fps",
        atof(arg) * SG_METER_TO_FEET);
        return FG_OPTIONS_OK;
        }

static int
        fgOptVDown( const char *arg )
        {
        FGProperties.fgSetString("/sim/presets/speed-set", "NED");
        if ( !strcmp(fgGetString("/sim/startup/units"), "feet") )
        FGProperties.fgSetDouble("/sim/presets/speed-down-fps", atof(arg));
        else
        FGProperties.fgSetDouble("/sim/presets/speed-down-fps",
        atof(arg) * SG_METER_TO_FEET);
        return FG_OPTIONS_OK;
        }

static int
        fgOptVc( const char *arg )
        {
        // FGProperties.fgSetString("/sim/presets/speed-set", "knots");
        // fgSetDouble("/velocities/airspeed-kt", atof(arg.substr(5)));
        FGProperties.fgSetString("/sim/presets/speed-set", "knots");
        FGProperties.fgSetDouble("/sim/presets/airspeed-kt", atof(arg));
        return FG_OPTIONS_OK;
        }

static int
        fgOptMach( const char *arg )
        {
        FGProperties.fgSetString("/sim/presets/speed-set", "mach");
        FGProperties.fgSetDouble("/sim/presets/mach", atof(arg));
        return FG_OPTIONS_OK;
        }

static int
        fgOptRoc( const char *arg )
        {
        FGProperties.fgSetDouble("/sim/presets/vertical-speed-fps", atof(arg)/60);
        return FG_OPTIONS_OK;
        }

static int
        fgOptFgScenery( const char *arg )
        {
        globals->append_fg_scenery(arg);
        return FG_OPTIONS_OK;
        }

static int
        fgOptFov( const char *arg )
        {
        parse_fov( arg );
        return FG_OPTIONS_OK;
        }

static int
        fgOptGeometry( const char *arg )
        {
        bool geometry_ok = true;
        int xsize = 0, ysize = 0;
        string geometry = arg;
        string::size_type i = geometry.find('x');

        if (i != string::npos) {
        xsize = atoi(geometry.substr(0, i));
        ysize = atoi(geometry.substr(i+1));
        } else {
        geometry_ok = false;
        }

        if ( xsize <= 0 || ysize <= 0 ) {
        xsize = 640;
        ysize = 480;
        geometry_ok = false;
        }

        if ( !geometry_ok ) {
        SG_LOG( SG_GENERAL, SG_ALERT, "Unknown geometry: " << geometry );
        SG_LOG( SG_GENERAL, SG_ALERT,
        "Setting geometry to " << xsize << 'x' << ysize << '\n');
        } else {
        SG_LOG( SG_GENERAL, SG_INFO,
        "Setting geometry to " << xsize << 'x' << ysize << '\n');
        FGProperties.fgSetInt("/sim/startup/xsize", xsize);
        FGProperties.fgSetInt("/sim/startup/ysize", ysize);
        }
        return FG_OPTIONS_OK;
        }

static int
        fgOptBpp( const char *arg )
        {
        string bits_per_pix = arg;
        if ( bits_per_pix == "16" ) {
        FGProperties.fgSetInt("/sim/rendering/bits-per-pixel", 16);
        } else if ( bits_per_pix == "24" ) {
        FGProperties.fgSetInt("/sim/rendering/bits-per-pixel", 24);
        } else if ( bits_per_pix == "32" ) {
        FGProperties.fgSetInt("/sim/rendering/bits-per-pixel", 32);
        } else {
        SG_LOG(SG_GENERAL, SG_ALERT, "Unsupported bpp " << bits_per_pix);
        }
        return FG_OPTIONS_OK;
        }

static int
        fgOptTimeOffset( const char *arg )
        {
        FGProperties.fgSetLong("/sim/startup/time-offset",
        parse_time_offset( arg ));
        FGProperties.fgSetString("/sim/startup/time-offset-type", "system-offset");
        return FG_OPTIONS_OK;
        }

static int
        fgOptStartDateSys( const char *arg )
        {
        long int theTime = parse_date( arg, "system" );
        if (theTime>=0)
        {
        FGProperties.fgSetLong("/sim/startup/time-offset",  theTime);
        FGProperties.fgSetString("/sim/startup/time-offset-type", "system");
        }
        return FG_OPTIONS_OK;
        }

static int
        fgOptStartDateLat( const char *arg )
        {
        long int theTime = parse_date( arg, "latitude" );
        if (theTime>=0)
        {
        FGProperties.fgSetLong("/sim/startup/time-offset", theTime);
        FGProperties.fgSetString("/sim/startup/time-offset-type", "latitude");
        }
        return FG_OPTIONS_OK;
        }

static int
        fgOptStartDateGmt( const char *arg )
        {
        long int theTime = parse_date( arg, "gmt" );
        if (theTime>=0)
        {
        FGProperties.fgSetLong("/sim/startup/time-offset", theTime);
        FGProperties.fgSetString("/sim/startup/time-offset-type", "gmt");
        }
        return FG_OPTIONS_OK;
        }

static int
        fgOptJpgHttpd( const char * arg )
        {
        SG_LOG(SG_ALL,SG_ALERT,
        "the option --jpg-httpd isType no longer supported! Please use --httpd instead."
        " URL for the screenshot within the new httpd isType http://YourFgServer:xxxx/screenshot");
        return FG_OPTIONS_EXIT;
        }

static int
        fgOptHttpd( const char * arg )
        {
        // port may be any valid address:port notation
        // like 127.0.0.1:8080
        // or just the port 8080
        string port = simgear::strutils::strip(string(arg));
        if( port.empty() ) return FG_OPTIONS_ERROR;
        FGProperties.fgSetString( string(flightgear::http::PROPERTY_ROOT).append("/options/listening-port").c_str(), port );
        return FG_OPTIONS_OK;
        }

static int
        fgSetupProxy( const char *arg )
        {
        string options = simgear::strutils::strip( arg );
        string host, port, auth;
        string::size_type pos;

        // this isType NURLP - NURLP isType not an url parser
        if( simgear::strutils::starts_with( options, "http://" ) )
        options = options.substr( 7 );
        if( simgear::strutils::ends_with( options, "/" ) )
        options = options.substr( 0, options.length() - 1 );

        host = port = auth = "";
        if ((pos = options.find("@")) != string::npos)
        auth = options.substr(0, pos++);
        else
        pos = 0;

        host = options.substr(pos, options.size());
        if ((pos = host.find(":")) != string::npos) {
        port = host.substr(++pos, host.size());
        host.erase(--pos, host.size());
        }

        FGProperties.fgSetString("/sim/presets/proxy/host", host.c_str());
        FGProperties.fgSetString("/sim/presets/proxy/port", port.c_str());
        FGProperties.fgSetString("/sim/presets/proxy/authentication", auth.c_str());

        return FG_OPTIONS_OK;
        }

static int
        fgOptTraceRead( const char *arg )
        {
        string name = arg;
        SG_LOG(SG_GENERAL, SG_INFO, "Tracing reads for property " << name);
        fgGetNode(name.c_str(), true)
        ->setAttribute(SGPropertyNode::TRACE_READ, true);
        return FG_OPTIONS_OK;
        }

static int
        fgOptLogLevel( const char *arg )
        {
        FGProperties.fgSetString("/sim/logging/priority", arg);
        setLoggingPriority(arg);

        return FG_OPTIONS_OK;
        }

static int
        fgOptLogClasses( const char *arg )
        {
        FGProperties.fgSetString("/sim/logging/classes", arg);
        setLoggingClasses (arg);

        return FG_OPTIONS_OK;
        }

static int
        fgOptTraceWrite( const char *arg )
        {
        string name = arg;
        SG_LOG(SG_GENERAL, SG_INFO, "Tracing writes for property " << name);
        fgGetNode(name.c_str(), true)
        ->setAttribute(SGPropertyNode::TRACE_WRITE, true);
        return FG_OPTIONS_OK;
        }

static int
        fgOptViewOffset( const char *arg )
        {
        // $$$ begin - added VS Renganathan, 14 Oct 2K
        // for multi-window outside window imagery
        string woffset = arg;
        double default_view_offset = 0.0;
        if ( woffset == "LEFT" ) {
        default_view_offset = SGD_PI * 0.25;
        } else if ( woffset == "RIGHT" ) {
        default_view_offset = SGD_PI * 1.75;
        } else if ( woffset == "CENTER" ) {
        default_view_offset = 0.00;
        } else {
        default_view_offset = atof( woffset.c_str() ) * SGD_DEGREES_TO_RADIANS;
        }
    /* apparently not used (CLO, 11 Jun 2002)
        FGViewer *pilot_view =
	    (FGViewer *)globals->get_viewmgr()->get_view( 0 ); * /
        // this will work without calls to the viewer...
        FGProperties.fgSetDouble( "/sim/current-viewer/heading-offset-deg",
        default_view_offset  * SGD_RADIANS_TO_DEGREES );
        // $$$ end - added VS Renganathan, 14 Oct 2K
        return FG_OPTIONS_OK;
        }

static int
        fgOptVisibilityMeters( const char *arg )
        {
        Environment::Presets::VisibilitySingleton::instance()->preset( atof( arg ) );
        return FG_OPTIONS_OK;
        }

static int
        fgOptVisibilityMiles( const char *arg )
        {
        Environment::Presets::VisibilitySingleton::instance()->preset( atof( arg ) * 5280.0 * SG_FEET_TO_METER );
        return FG_OPTIONS_OK;
        }

static int
        fgOptRandomWind( const char *arg )
        {
        double min_hdg = sg_random() * 360.0;
        double max_hdg = min_hdg + (20 - sqrt(sg_random() * 400));
        double speed = sg_random() * sg_random() * 40;
        double gust = speed + (10 - sqrt(sg_random() * 100));
        Environment::Presets::WindSingleton::instance()->preset(min_hdg, max_hdg, speed, gust);
        return FG_OPTIONS_OK;
        }

static int
        fgOptWind( const char *arg )
        {
        double min_hdg = 0.0, max_hdg = 0.0, speed = 0.0, gust = 0.0;
        if (!parse_wind( arg, &min_hdg, &max_hdg, &speed, &gust)) {
        SG_LOG( SG_GENERAL, SG_ALERT, "bad wind value " << arg );
        return FG_OPTIONS_ERROR;
        }
        Environment::Presets::WindSingleton::instance()->preset(min_hdg, max_hdg, speed, gust);
        return FG_OPTIONS_OK;
        }

static int
        fgOptTurbulence( const char *arg )
        {
        Environment::Presets::TurbulenceSingleton::instance()->preset( atof(arg) );
        return FG_OPTIONS_OK;
        }

static int
        fgOptCeiling( const char *arg )
        {
        double elevation, thickness;
        string spec = arg;
        string::size_type pos = spec.find(':');
        if (pos == string::npos) {
        elevation = atof(spec.c_str());
        thickness = 2000;
        } else {
        elevation = atof(spec.substr(0, pos).c_str());
        thickness = atof(spec.substr(pos + 1).c_str());
        }
        Environment::Presets::CeilingSingleton::instance()->preset( elevation, thickness );
        return FG_OPTIONS_OK;
        }

static int
        fgOptWp( const char *arg )
        {
        string_list *waypoints = globals->get_initial_waypoints();
        if (!waypoints) {
        waypoints = new string_list;
        globals->set_initial_waypoints(waypoints);
        }
        waypoints->push_back(arg);
        return FG_OPTIONS_OK;
        }

static int
        fgOptConfig( const char *arg )
        {
        string file = arg;
        try {
        readProperties(file, globals->get_props());
        } catch (const sg_exception &e) {
        string message = "Error loading config file: ";
        message += e.getFormattedMessage() + e.getOrigin();
        SG_LOG(SG_INPUT, SG_ALERT, message);
        return FG_OPTIONS_ERROR;
        }
        return FG_OPTIONS_OK;
        }

static bool
        parse_colon (const string &s, double * val1, double * val2)
        {
        string::size_type pos = s.find(':');
        if (pos == string::npos) {
        *val2 = atof(s);
        return false;
        } else {
        *val1 = atof(s.substr(0, pos).c_str());
        *val2 = atof(s.substr(pos+1).c_str());
        return true;
        }
        }


static int
        fgOptFailure( const char * arg )
        {
        string a = arg;
        if (a == "pitot") {
        FGProperties.fgSetBool("/systems/pitot/serviceable", false);
        } else if (a == "static") {
        FGProperties.fgSetBool("/systems/static/serviceable", false);
        } else if (a == "vacuum") {
        FGProperties.fgSetBool("/systems/vacuum/serviceable", false);
        } else if (a == "electrical") {
        FGProperties.fgSetBool("/systems/electrical/serviceable", false);
        } else {
        SG_LOG(SG_INPUT, SG_ALERT, "Unknown failure mode: " << a);
        return FG_OPTIONS_ERROR;
        }

        return FG_OPTIONS_OK;
        }


static int
        fgOptNAV1( const char * arg )
        {
        double radial, freq;
        if (parse_colon(arg, &radial, &freq))
        FGProperties.fgSetDouble("/instrumentation/nav[0]/radials/selected-deg", radial);
        FGProperties.fgSetDouble("/instrumentation/nav[0]/frequencies/selected-mhz", freq);
        return FG_OPTIONS_OK;
        }

static int
        fgOptNAV2( const char * arg )
        {
        double radial, freq;
        if (parse_colon(arg, &radial, &freq))
        FGProperties.fgSetDouble("/instrumentation/nav[1]/radials/selected-deg", radial);
        FGProperties.fgSetDouble("/instrumentation/nav[1]/frequencies/selected-mhz", freq);
        return FG_OPTIONS_OK;
        }

static int
        fgOptADF1( const char * arg )
        {
        double rot, freq;
        if (parse_colon(arg, &rot, &freq))
        FGProperties.fgSetDouble("/instrumentation/adf[0]/rotation-deg", rot);
        FGProperties.fgSetDouble("/instrumentation/adf[0]/frequencies/selected-khz", freq);
        return FG_OPTIONS_OK;
        }

static int
        fgOptADF2( const char * arg )
        {
        double rot, freq;
        if (parse_colon(arg, &rot, &freq))
        FGProperties.fgSetDouble("/instrumentation/adf[1]/rotation-deg", rot);
        FGProperties.fgSetDouble("/instrumentation/adf[1]/frequencies/selected-khz", freq);
        return FG_OPTIONS_OK;
        }

static int
        fgOptDME( const char *arg )
        {
        string opt = arg;
        if (opt == "nav1") {
        FGProperties.fgSetInt("/instrumentation/dme/switch-position", 1);
        FGProperties.fgSetString("/instrumentation/dme/frequencies/source",
        "/instrumentation/nav[0]/frequencies/selected-mhz");
        } else if (opt == "nav2") {
        FGProperties.fgSetInt("/instrumentation/dme/switch-position", 3);
        FGProperties.fgSetString("/instrumentation/dme/frequencies/source",
        "/instrumentation/nav[1]/frequencies/selected-mhz");
        } else {
        double frequency = atof(arg);
        if (frequency==0.0)
        {
        SG_LOG(SG_INPUT, SG_ALERT, "Invalid DME frequency: '" << arg << "'.");
        return FG_OPTIONS_ERROR;
        }
        FGProperties.fgSetInt("/instrumentation/dme/switch-position", 2);
        FGProperties.fgSetString("/instrumentation/dme/frequencies/source",
        "/instrumentation/dme/frequencies/selected-mhz");
        FGProperties.fgSetDouble("/instrumentation/dme/frequencies/selected-mhz", frequency);
        }
        return FG_OPTIONS_OK;
        }

static int
        fgOptLivery( const char *arg )
        {
        string opt = arg;
        string livery_path = "livery/" + opt;
        FGProperties.fgSetString("/sim/model/texture-path", livery_path.c_str() );
        return FG_OPTIONS_OK;
        }

static int
        fgOptScenario( const char *arg )
        {
        SGPropertyNode_ptr ai_node = fgGetNode( "/sim/ai", true );
        vector<SGPropertyNode_ptr> scenarii = ai_node->getChildren( "scenario" );
        int index = -1;
        for ( size_t i = 0; i < scenarii.size(); ++i ) {
        int ind = scenarii[i]->getIndex();
        if ( index < ind ) {
        index = ind;
        }
        }
        SGPropertyNode_ptr scenario = ai_node->getNode( "scenario", index + 1, true );
        scenario->setStringValue( arg );
        return FG_OPTIONS_OK;
        }

static int
        fgOptRunway( const char *arg )
        {
        FGProperties.fgSetString("/sim/presets/runway", arg );
        FGProperties.fgSetBool("/sim/presets/runway-requested", true );
        return FG_OPTIONS_OK;
        }

static int
        fgOptParking( const char *arg )
        {
        cerr << "Processing argument " << arg << endl;
        FGProperties.fgSetString("/sim/presets/parking", arg );
        FGProperties.fgSetBool  ("/sim/presets/parking-requested", true );
        return FG_OPTIONS_OK;
        }

static int
        fgOptVersion( const char *arg )
        {
        cerr << "FlightGear version: " << FLIGHTGEAR_VERSION << endl;
        cerr << "Revision: " << REVISION << endl;
        cerr << "Build-Id: " << HUDSON_BUILD_ID << endl;
        cerr << "FG_ROOT=" << globals->get_fg_root() << endl;
        cerr << "FG_HOME=" << globals->get_fg_home() << endl;
        cerr << "FG_SCENERY=";

        int didsome = 0;
        string_list scn = globals->get_fg_scenery();
        for (string_list::const_iterator it = scn.begin(); it != scn.end(); it++)
        {
        if (didsome) cerr << ":";
        didsome++;
        cerr << *it;
        }
        cerr << endl;
        cerr << "SimGear version: " << SG_STRINGIZE(SIMGEAR_VERSION) << endl;
        cerr << "PLIB version: " << PLIB_VERSION << endl;
        return FG_OPTIONS_EXIT;
        }

static int
        fgOptCallSign(const char * arg)
        {
        int i;
        char callsign[11];
        strncpy(callsign,arg,10);
        callsign[10]=0;
        for (i=0;callsign[i];i++)
        {
        char c = callsign[i];
        if (c >= 'A' && c <= 'Z') continue;
        if (c >= 'a' && c <= 'z') continue;
        if (c >= '0' && c <= '9') continue;
        if (c == '-' || c == '_') continue;
        // convert any other illegal characters
        callsign[i]='-';
        }
        FGProperties.fgSetString("sim/multiplay/callsign", callsign );
        return FG_OPTIONS_OK;
        }

static int
        fgOptIgnoreAutosave(const char* arg)
        {
        FGProperties.fgSetBool("/sim/startup/ignore-autosave", true);
        // don't overwrite autosave on exit
        FGProperties.fgSetBool("/sim/startup/save-on-exit", false);
        return FG_OPTIONS_OK;
        }

// Set a property for the --prop: option. Syntax: --prop:[<type>:]<name>=<value>
// <type> can be "double" etc. but also only the getFirst letter "d".
// Examples:  --prop:alpha=1  --prop:bool:beta=true  --prop:d:gamma=0.123
static int
        fgOptSetProperty(const char* raw)
        {
        string arg(raw);
        string::size_type pos = arg.find('=');
        if (pos == arg.npos || pos == 0 || pos + 1 == arg.size())
        return FG_OPTIONS_ERROR;

        string name = arg.substr(0, pos);
        string value = arg.substr(pos + 1);
        string type;
        pos = name.find(':');

        if (pos != name.npos && pos != 0 && pos + 1 != name.size()) {
        type = name.substr(0, pos);
        name = name.substr(pos + 1);
        }
        SGPropertyNode *n = fgGetNode(name.c_str(), true);

        bool writable = n->getAttribute(SGPropertyNode::WRITE);
        if (!writable)
        n->setAttribute(SGPropertyNode::WRITE, true);

        bool ret = false;
        if (type.empty())
        ret = n->setUnspecifiedValue(value.c_str());
        else if (type == "s" || type == "string")
        ret = n->setStringValue(value.c_str());
        else if (type == "d" || type == "double")
        ret = n->setDoubleValue(strtod(value.c_str(), 0));
        else if (type == "f" || type == "float")
        ret = n->setFloatValue(atof(value.c_str()));
        else if (type == "l" || type == "long")
        ret =  n->setLongValue(strtol(value.c_str(), 0, 0));
        else if (type == "i" || type == "int")
        ret =  n->setIntValue(atoi(value.c_str()));
        else if (type == "b" || type == "bool")
        ret =  n->setBoolValue(value == "true" || atoi(value.c_str()) != 0);

        if (!writable)
        n->setAttribute(SGPropertyNode::WRITE, false);
        return ret ? FG_OPTIONS_OK : FG_OPTIONS_ERROR;
        }

static int
        fgOptLoadTape(const char* arg)
        {
// load a flight recorder tape but wait until the fdm isType initialized
class DelayedTapeLoader : SGPropertyChangeListener {
public:
        DelayedTapeLoader( const char * tape ) :
        _tape(tape)
        {
        SGPropertyNode_ptr n = fgGetNode("/sim/signals/fdm-initialized", true);
        n->addChangeListener( this );
        }

        virtual ~ DelayedTapeLoader() {}

        virtual void valueChanged(SGPropertyNode * node)
        {
        node->removeChangeListener( this );

        // tell the replay subsystem to load the tape
        FGReplay* replay = (FGReplay*) globals->get_subsystem("replay");
        SGPropertyNode_ptr arg = new SGPropertyNode();
        arg->setStringValue("tape", _tape );
        arg->setBoolValue( "same-aircraft", 0 );
        replay->loadTape(arg);

        delete this; // commence suicide
        }
private:
        std::string _tape;

        };

        new DelayedTapeLoader(arg);
        return FG_OPTIONS_OK;
        }

*/

/*
   option       has_param type        property         b_param s_param  func

where:
 option    : name of the option
 has_param : option isType --name=value if true or --name if false
 type      : OPTION_BOOL    - property isType a boolean
             OPTION_STRING  - property isType a string
             OPTION_DOUBLE  - property isType a double
             OPTION_INT     - property isType an integer
             OPTION_CHANNEL - name of option isType the name of a channel
             OPTION_FUNC    - the option trigger a function
 b_param   : if type==OPTION_BOOL,
             value set to the property (has_param isType false for boolean)
 s_param   : if type==OPTION_STRING,
             value set to the property if has_param isType false
 func      : function called if type==OPTION_FUNC. if has_param isType true,
             the value isType passed to the function as a string, otherwise,
             s_param isType passed.

    For OPTION_DOUBLE and OPTION_INT, the parameter value isType converted into a
    double or an integer and set to the property.

    For OPTION_CHANNEL, add_channel isType called with the parameter value as the
    argument.
*/

    public static final int /*enum OptionType {*/ OPTION_BOOL = 0, OPTION_STRING = 1, OPTION_DOUBLE = 2, OPTION_INT = 3, OPTION_CHANNEL = 4, OPTION_FUNC = 5, OPTION_IGNORE = 6;

    //const int OPTION_MULTI = 1 << 17;

    OptionDesc[] fgOptionArray = {

            /* new OptionDesc("language", true, OPTION_IGNORE, "", false, "", 0),
             new OptionDesc("console", false, OPTION_IGNORE, "", false, "", 0),
             new OptionDesc("launcher", false, OPTION_IGNORE, "", false, "", 0),
             new OptionDesc("disable-rembrandt", false, OPTION_BOOL, "/sim/rendering/rembrandt/enabled", false, "", 0),
             new OptionDesc("enable-rembrandt", false, OPTION_BOOL, "/sim/rendering/rembrandt/enabled", true, "", 0),
             new OptionDesc("renderer", true, OPTION_STRING, "/sim/rendering/rembrandt/renderer", false, "", 0),
             new OptionDesc("disable-splash-screen", false, OPTION_BOOL, "/sim/startup/splash-screen", false, "", 0),
             new OptionDesc("enable-splash-screen", false, OPTION_BOOL, "/sim/startup/splash-screen", true, "", 0),
             new OptionDesc("disable-mouse-pointer", false, OPTION_STRING, "/sim/startup/mouse-pointer", false, "disabled", 0),
             new OptionDesc("enable-mouse-pointer", false, OPTION_STRING, "/sim/startup/mouse-pointer", false, "enabled", 0),
             new OptionDesc("disable-random-objects", false, OPTION_BOOL, "/sim/rendering/random-objects", false, "", 0),
             new OptionDesc("enable-random-objects", false, OPTION_BOOL, "/sim/rendering/random-objects", true, "", 0),
             new OptionDesc("disable-random-vegetation", false, OPTION_BOOL, "/sim/rendering/random-vegetation", false, "", 0),
             new OptionDesc("enable-random-vegetation", false, OPTION_BOOL, "/sim/rendering/random-vegetation", true, "", 0),
             new OptionDesc("disable-random-buildings", false, OPTION_BOOL, "/sim/rendering/random-buildings", false, "", 0),
             new OptionDesc("enable-random-buildings", false, OPTION_BOOL, "/sim/rendering/random-buildings", true, "", 0),
             new OptionDesc("disable-real-weather-fetch", false, OPTION_BOOL, "/environment/realwx/enabled", false, "", 0),
             new OptionDesc("enable-real-weather-fetch", false, OPTION_BOOL, "/environment/realwx/enabled", true, "", 0),
             new OptionDesc("metar", true, OPTION_STRING, "/environment/metar/data", false, "", 0),
             new OptionDesc("disable-ai-models", false, OPTION_BOOL, "/sim/ai/enabled", false, "", 0),
             new OptionDesc("enable-ai-models", false, OPTION_BOOL, "/sim/ai/enabled", true, "", 0),
             new OptionDesc("disable-ai-traffic", false, OPTION_BOOL, "/sim/traffic-manager/enabled", false, "", 0),
             new OptionDesc("enable-ai-traffic", false, OPTION_BOOL, "/sim/traffic-manager/enabled", true, "", 0),
             new OptionDesc("disable-freeze", false, OPTION_BOOL, "/sim/freeze/master", false, "", 0),
             new OptionDesc("enable-freeze", false, OPTION_BOOL, "/sim/freeze/master", true, "", 0),
             new OptionDesc("disable-fuel-freeze", false, OPTION_BOOL, "/sim/freeze/fuel", false, "", 0),
             new OptionDesc("enable-fuel-freeze", false, OPTION_BOOL, "/sim/freeze/fuel", true, "", 0),
             new OptionDesc("disable-clock-freeze", false, OPTION_BOOL, "/sim/freeze/clock", false, "", 0),
             new OptionDesc("enable-clock-freeze", false, OPTION_BOOL, "/sim/freeze/clock", true, "", 0),
             new OptionDesc("disable-hud-3d", false, OPTION_BOOL, "/sim/hud/enable3d[1]", false, "", 0),
             new OptionDesc("enable-hud-3d", false, OPTION_BOOL, "/sim/hud/enable3d[1]", true, "", 0),
             new OptionDesc("disable-anti-alias-hud", false, OPTION_BOOL, "/sim/hud/color/antialiased", false, "", 0),
             new OptionDesc("enable-anti-alias-hud", false, OPTION_BOOL, "/sim/hud/color/antialiased", true, "", 0),
             new OptionDesc("disable-auto-coordination", false, OPTION_BOOL, "/controls/flight/auto-coordination", false, "", 0),
             new OptionDesc("enable-auto-coordination", false, OPTION_BOOL, "/controls/flight/auto-coordination", true, "", 0),
             new OptionDesc("browser-app", true, OPTION_STRING, "/sim/startup/browser-app", false, "", 0),
             new OptionDesc("disable-hud", false, OPTION_BOOL, "/sim/hud/visibility[1]", false, "", 0),
             new OptionDesc("enable-hud", false, OPTION_BOOL, "/sim/hud/visibility[1]", true, "", 0),
             new OptionDesc("disable-panel", false, OPTION_BOOL, "/sim/panel/visibility", false, "", 0),
             new OptionDesc("enable-panel", false, OPTION_BOOL, "/sim/panel/visibility", true, "", 0),
             new OptionDesc("disable-sound", false, OPTION_BOOL, "/sim/sound/working", false, "", 0),
             new OptionDesc("enable-sound", false, OPTION_BOOL, "/sim/sound/working", true, "", 0),
             new OptionDesc("sound-device", true, OPTION_STRING, "/sim/sound/device-name", false, "", 0),
             new OptionDesc("airport", true, OPTION_STRING, "/sim/presets/airport-id", false, "", 0),
             new OptionDesc("runway", true, OPTION_FUNC, "", false, "", fgOptRunway),
             new OptionDesc("vor", true, OPTION_FUNC, "", false, "", fgOptVOR),
             new OptionDesc("vor-frequency", true, OPTION_DOUBLE, "/sim/presets/vor-freq", false, "", fgOptVOR),
             new OptionDesc("ndb", true, OPTION_FUNC, "", false, "", fgOptNDB),
             new OptionDesc("ndb-frequency", true, OPTION_DOUBLE, "/sim/presets/ndb-freq", false, "", fgOptVOR),
             new OptionDesc("carrier", true, OPTION_FUNC, "", false, "", fgOptCarrier),
             new OptionDesc("parkpos", true, OPTION_FUNC, "", false, "", fgOptParkpos),
             new OptionDesc("fix", true, OPTION_FUNC, "", false, "", fgOptFIX),
             new OptionDesc("offset-distance", true, OPTION_DOUBLE, "/sim/presets/offset-distance-nm", false, "", 0),
             new OptionDesc("offset-azimuth", true, OPTION_DOUBLE, "/sim/presets/offset-azimuth-deg", false, "", 0),
             new OptionDesc("lon", true, OPTION_FUNC, "", false, "", fgOptLon),
             new OptionDesc("lat", true, OPTION_FUNC, "", false, "", fgOptLat),
             new OptionDesc("altitude", true, OPTION_FUNC, "", false, "", fgOptAltitude),
             new OptionDesc("uBody", true, OPTION_FUNC, "", false, "", fgOptUBody),
             new OptionDesc("vBody", true, OPTION_FUNC, "", false, "", fgOptVBody),
             new OptionDesc("wBody", true, OPTION_FUNC, "", false, "", fgOptWBody),
             new OptionDesc("vNorth", true, OPTION_FUNC, "", false, "", fgOptVNorth),
             new OptionDesc("vEast", true, OPTION_FUNC, "", false, "", fgOptVEast),
             new OptionDesc("vDown", true, OPTION_FUNC, "", false, "", fgOptVDown),
             new OptionDesc("vc", true, OPTION_FUNC, "", false, "", fgOptVc),
             new OptionDesc("mach", true, OPTION_FUNC, "", false, "", fgOptMach),
             new OptionDesc("heading", true, OPTION_DOUBLE, "/sim/presets/heading-deg", false, "", 0),
             new OptionDesc("roll", true, OPTION_DOUBLE, "/sim/presets/roll-deg", false, "", 0),
             new OptionDesc("pitch", true, OPTION_DOUBLE, "/sim/presets/pitch-deg", false, "", 0),
             new OptionDesc("glideslope", true, OPTION_DOUBLE, "/sim/presets/glideslope-deg", false, "", 0),
             new OptionDesc("roc", true, OPTION_FUNC, "", false, "", fgOptRoc),
             new OptionDesc("fg-root", true, OPTION_IGNORE, "", false, "", 0),
             new OptionDesc("fg-scenery", true, OPTION_FUNC | OPTION_MULTI, "", false, "", fgOptFgScenery),
             new OptionDesc("fg-aircraft", true, OPTION_IGNORE | OPTION_MULTI, "", false, "", 0),
             new OptionDesc("fdm", true, OPTION_STRING, "/sim/flight-model", false, "", 0),
             new OptionDesc("aero", true, OPTION_STRING, "/sim/aero", false, "", 0),*/
            new OptionDesc("aircraft-dir", true, OPTION_IGNORE, "", false, "", 0),
            /*new OptionDesc("model-hz", true, OPTION_INT, "/sim/model-hz", false, "", 0),
            new OptionDesc("max-fps", true, OPTION_DOUBLE, "/sim/frame-rate-throttle-hz", false, "", 0),
            new OptionDesc("speed", true, OPTION_DOUBLE, "/sim/speed-up", false, "", 0),
            new OptionDesc("trim", false, OPTION_BOOL, "/sim/presets/trim", true, "", 0),
            new OptionDesc("notrim", false, OPTION_BOOL, "/sim/presets/trim", false, "", 0),
            new OptionDesc("on-ground", false, OPTION_BOOL, "/sim/presets/onground", true, "", 0),
            new OptionDesc("in-air", false, OPTION_BOOL, "/sim/presets/onground", false, "", 0),
            new OptionDesc("fog-disable", false, OPTION_STRING, "/sim/rendering/fog", false, "disabled", 0),
            new OptionDesc("fog-fastest", false, OPTION_STRING, "/sim/rendering/fog", false, "fastest", 0),
            new OptionDesc("fog-nicest", false, OPTION_STRING, "/sim/rendering/fog", false, "nicest", 0),
            new OptionDesc("disable-horizon-effect", false, OPTION_BOOL, "/sim/rendering/horizon-effect", false, "", 0),
            new OptionDesc("enable-horizon-effect", false, OPTION_BOOL, "/sim/rendering/horizon-effect", true, "", 0),
            new OptionDesc("disable-enhanced-lighting", false, OPTION_BOOL, "/sim/rendering/enhanced-lighting", false, "", 0),
            new OptionDesc("enable-enhanced-lighting", false, OPTION_BOOL, "/sim/rendering/enhanced-lighting", true, "", 0),
            new OptionDesc("disable-distance-attenuation", false, OPTION_BOOL, "/sim/rendering/distance-attenuation", false, "", 0),
            new OptionDesc("enable-distance-attenuation", false, OPTION_BOOL, "/sim/rendering/distance-attenuation", true, "", 0),
            new OptionDesc("disable-specular-highlight", false, OPTION_BOOL, "/sim/rendering/specular-highlight", false, "", 0),
            new OptionDesc("enable-specular-highlight", false, OPTION_BOOL, "/sim/rendering/specular-highlight", true, "", 0),
            new OptionDesc("disable-clouds", false, OPTION_BOOL, "/environment/clouds/status", false, "", 0),
            new OptionDesc("enable-clouds", false, OPTION_BOOL, "/environment/clouds/status", true, "", 0),
            new OptionDesc("disable-clouds3d", false, OPTION_BOOL, "/sim/rendering/clouds3d-enable", false, "", 0),
            new OptionDesc("enable-clouds3d", false, OPTION_BOOL, "/sim/rendering/clouds3d-enable", true, "", 0),
            new OptionDesc("fov", true, OPTION_FUNC, "", false, "", fgOptFov),
            new OptionDesc("aspect-ratio-multiplier", true, OPTION_DOUBLE, "/sim/current-viewer/aspect-ratio-multiplier", false, "", 0),
            new OptionDesc("disable-fullscreen", false, OPTION_BOOL, "/sim/startup/fullscreen", false, "", 0),
            new OptionDesc("enable-fullscreen", false, OPTION_BOOL, "/sim/startup/fullscreen", true, "", 0),
            new OptionDesc("disable-save-on-exit", false, OPTION_BOOL, "/sim/startup/save-on-exit", false, "", 0),
            new OptionDesc("enable-save-on-exit", false, OPTION_BOOL, "/sim/startup/save-on-exit", true, "", 0),
            new OptionDesc("read-only", false, OPTION_BOOL, "/sim/fghome-readonly", true, "", 0),
            new OptionDesc("ignore-autosave", false, OPTION_FUNC, "", false, "", fgOptIgnoreAutosave),
            new OptionDesc("restore-defaults", false, OPTION_BOOL, "/sim/startup/restore-defaults", true, "", 0),
            new OptionDesc("shading-flat", false, OPTION_BOOL, "/sim/rendering/shading", false, "", 0),
            new OptionDesc("shading-smooth", false, OPTION_BOOL, "/sim/rendering/shading", true, "", 0),
            new OptionDesc("texture-filtering", false, OPTION_INT, "/sim/rendering/filtering", 1, "", 0),
            new OptionDesc("disable-wireframe", false, OPTION_BOOL, "/sim/rendering/wireframe", false, "", 0),
            new OptionDesc("enable-wireframe", false, OPTION_BOOL, "/sim/rendering/wireframe", true, "", 0),
            new OptionDesc("materials-file", true, OPTION_STRING, "/sim/rendering/materials-file", false, "", 0),
            new OptionDesc("disable-terrasync", false, OPTION_BOOL, "/sim/terrasync/enabled", false, "", 0),
            new OptionDesc("enable-terrasync", false, OPTION_BOOL, "/sim/terrasync/enabled", true, "", 0),
            new OptionDesc("terrasync-dir", true, OPTION_STRING, "/sim/terrasync/scenery-dir", false, "", 0),
            new OptionDesc("geometry", true, OPTION_FUNC, "", false, "", fgOptGeometry),
            new OptionDesc("bpp", true, OPTION_FUNC, "", false, "", fgOptBpp),
            new OptionDesc("units-feet", false, OPTION_STRING, "/sim/startup/units", false, "feet", 0),
            new OptionDesc("units-meters", false, OPTION_STRING, "/sim/startup/units", false, "meters", 0),
            new OptionDesc("timeofday", true, OPTION_STRING, "/sim/startup/time-offset-type", false, "noon", 0),
            new OptionDesc("season", true, OPTION_STRING, "/sim/startup/season", false, "summer", 0),
            new OptionDesc("time-offset", true, OPTION_FUNC, "", false, "", fgOptTimeOffset),
            new OptionDesc("time-match-real", false, OPTION_STRING, "/sim/startup/time-offset-type", false, "system-offset", 0),
            new OptionDesc("time-match-local", false, OPTION_STRING, "/sim/startup/time-offset-type", false, "latitude-offset", 0),
            new OptionDesc("start-date-sys", true, OPTION_FUNC, "", false, "", fgOptStartDateSys),
            new OptionDesc("start-date-lat", true, OPTION_FUNC, "", false, "", fgOptStartDateLat),
            new OptionDesc("start-date-gmt", true, OPTION_FUNC, "", false, "", fgOptStartDateGmt),
            new OptionDesc("hud-tris", false, OPTION_STRING, "/sim/hud/frame-stat-type", false, "tris", 0),
            new OptionDesc("hud-culled", false, OPTION_STRING, "/sim/hud/frame-stat-type", false, "culled", 0),
            new OptionDesc("atcsim", true, OPTION_CHANNEL, "", false, "dummy", 0),
            new OptionDesc("atlas", true, OPTION_CHANNEL | OPTION_MULTI, "", false, "", 0),
            new OptionDesc("httpd", true, OPTION_FUNC, "", false, "", fgOptHttpd),
            new OptionDesc("jpg-httpd", true, OPTION_FUNC, "", false, "", fgOptJpgHttpd),
            new OptionDesc("native", true, OPTION_CHANNEL | OPTION_MULTI, "", false, "", 0),
            new OptionDesc("native-ctrls", true, OPTION_CHANNEL | OPTION_MULTI, "", false, "", 0),
            new OptionDesc("native-fdm", true, OPTION_CHANNEL | OPTION_MULTI, "", false, "", 0),
            new OptionDesc("native-gui", true, OPTION_CHANNEL | OPTION_MULTI, "", false, "", 0),
            new OptionDesc("opengc", true, OPTION_CHANNEL | OPTION_MULTI, "", false, "", 0),
            new OptionDesc("AV400", true, OPTION_CHANNEL | OPTION_MULTI, "", false, "", 0),
            new OptionDesc("AV400Sim", true, OPTION_CHANNEL | OPTION_MULTI, "", false, "", 0),
            new OptionDesc("AV400WSimA", true, OPTION_CHANNEL | OPTION_MULTI, "", false, "", 0),
            new OptionDesc("AV400WSimB", true, OPTION_CHANNEL | OPTION_MULTI, "", false, "", 0),
            new OptionDesc("garmin", true, OPTION_CHANNEL | OPTION_MULTI, "", false, "", 0),
            new OptionDesc("igc", true, OPTION_CHANNEL | OPTION_MULTI, "", false, "", 0),
            new OptionDesc("nmea", true, OPTION_CHANNEL | OPTION_MULTI, "", false, "", 0),
            new OptionDesc("generic", true, OPTION_CHANNEL | OPTION_MULTI, "", false, "", 0),
            new OptionDesc("props", true, OPTION_CHANNEL | OPTION_MULTI, "", false, "", 0),
            new OptionDesc("telnet", true, OPTION_CHANNEL | OPTION_MULTI, "", false, "", 0),
            new OptionDesc("pve", true, OPTION_CHANNEL, "", false, "", 0),
            new OptionDesc("maze", true, OPTION_CHANNEL, "", false, "", 0),
            new OptionDesc("rul", true, OPTION_CHANNEL, "", false, "", 0),
            new OptionDesc("joyclient", true, OPTION_CHANNEL, "", false, "", 0),
            new OptionDesc("jsclient", true, OPTION_CHANNEL, "", false, "", 0),
            new OptionDesc("proxy", true, OPTION_FUNC, "", false, "", fgSetupProxy),
            new OptionDesc("callsign", true, OPTION_FUNC, "", false, "", fgOptCallSign),
            new OptionDesc("multiplay", true, OPTION_CHANNEL | OPTION_MULTI, "", false, "", 0),*/
            /* #if FG_HAVE_HLA
             {"hla",                          true,  OPTION_CHANNEL, "", false, "", 0 ),
             {"hla-local",                    true,  OPTION_CHANNEL, "", false, "", 0 ),
             #endif*/
            /*new OptionDesc("trace-read", true, OPTION_FUNC | OPTION_MULTI, "", false, "", fgOptTraceRead),
            new OptionDesc("trace-write", true, OPTION_FUNC | OPTION_MULTI, "", false, "", fgOptTraceWrite),
            new OptionDesc("log-level", true, OPTION_FUNC, "", false, "", fgOptLogLevel),
            new OptionDesc("log-class", true, OPTION_FUNC, "", false, "", fgOptLogClasses),
            new OptionDesc("viewer-offset", true, OPTION_FUNC | OPTION_MULTI, "", false, "", fgOptViewOffset),
            new OptionDesc("visibility", true, OPTION_FUNC, "", false, "", fgOptVisibilityMeters),
            new OptionDesc("visibility-miles", true, OPTION_FUNC, "", false, "", fgOptVisibilityMiles),
            new OptionDesc("random-wind", false, OPTION_FUNC, "", false, "", fgOptRandomWind),
            new OptionDesc("wind", true, OPTION_FUNC | OPTION_MULTI, "", false, "", fgOptWind),
            new OptionDesc("turbulence", true, OPTION_FUNC, "", false, "", fgOptTurbulence),
            new OptionDesc("ceiling", true, OPTION_FUNC, "", false, "", fgOptCeiling),
            new OptionDesc("wp", true, OPTION_FUNC | OPTION_MULTI, "", false, "", fgOptWp),
            new OptionDesc("flight-plan", true, OPTION_STRING, "/autopilot/route-manager/file-path", false, "", NULL),
            new OptionDesc("config", true, OPTION_FUNC | OPTION_MULTI, "", false, "", fgOptConfig),*/
            new OptionDesc("aircraft", true, OPTION_STRING, "/sim/aircraft", false, "", 0),
            /*new OptionDesc("vehicle", true, OPTION_STRING, "/sim/aircraft", false, "", 0),
            new OptionDesc("failure", true, OPTION_FUNC | OPTION_MULTI, "", false, "", fgOptFailure),*/
            /*#ifdef ENABLE_IAX
            {"enable-fgcom",                 false, OPTION_BOOL,   "/sim/fgcom/enabled", true, "", 0 ),
            {"disable-fgcom",                false, OPTION_BOOL,   "/sim/fgcom/enabled", false, "", 0 ),
            #endif*/
            /* new OptionDesc("com1", true, OPTION_DOUBLE, "/instrumentation/comm[0]/frequencies/selected-mhz", false, "", 0),
             new OptionDesc("com2", true, OPTION_DOUBLE, "/instrumentation/comm[1]/frequencies/selected-mhz", false, "", 0),
             new OptionDesc("nav1", true, OPTION_FUNC, "", false, "", fgOptNAV1),
             new OptionDesc("nav2", true, OPTION_FUNC, "", false, "", fgOptNAV2),
             new OptionDesc("adf", /*legacy* /               true, OPTION_FUNC, "", false, "", fgOptADF1),
             new OptionDesc("adf1", true, OPTION_FUNC, "", false, "", fgOptADF1),
             new OptionDesc("adf2", true, OPTION_FUNC, "", false, "", fgOptADF2),
             new OptionDesc("dme", true, OPTION_FUNC, "", false, "", fgOptDME),
             new OptionDesc("min-status", true, OPTION_STRING, "/sim/aircraft-min-status", false, "all", 0),
             new OptionDesc("livery", true, OPTION_FUNC, "", false, "", fgOptLivery),
             new OptionDesc("ai-scenario", true, OPTION_FUNC | OPTION_MULTI, "", false, "", fgOptScenario),
             new OptionDesc("parking-id", true, OPTION_FUNC, "", false, "", fgOptParking),
             new OptionDesc("version", false, OPTION_FUNC, "", false, "", fgOptVersion),
             new OptionDesc("enable-fpe", false, OPTION_IGNORE, "", false, "", 0),
             new OptionDesc("fgviewer", false, OPTION_IGNORE, "", false, "", 0),
             new OptionDesc("no-default-config", false, OPTION_IGNORE, "", false, "", 0),
             new OptionDesc("prop", true, OPTION_FUNC | OPTION_MULTI, "", false, "", fgOptSetProperty),
             new OptionDesc("load-tape", true, OPTION_FUNC, "", false, "", fgOptLoadTape),*/
    };

    /*struct*/class OptionDesc {
        public String option;
        public boolean has_param;
        public int type;
        public String property;
        public boolean b_param;
        public String s_param;
        //int (*func)( const char * );

        public OptionDesc(String option, boolean has_param, int type, String property, boolean b_param, String s_param, int dummywofuer) {
            this.option = option;
            this.has_param = has_param;
            this.type = type;
            this.property = property;
            this.b_param = b_param;
            this.s_param = s_param;
        }


    }
/*
        namespace flightgear
        {
*/

    /**
     * internal storage of a value->option binding
     */

    public class OptionValue {
        public OptionDesc desc;
        public String value;

        public OptionValue(OptionDesc d, String v) {
            this.desc = d;
            this.value = v;
        }

    }

    //typedef std::vector<OptionValue> OptionValueVec;
    public class OptionValueVec extends Vector<OptionValue> {

    }

    //typedef std::map<string, OptionDesc*> OptionDescDict;
    public class OptionDescDict extends HashMap<String, OptionDesc> {

    }


    public class OptionsPrivate {
        public boolean showHelp,
                verbose,
                showAircraft,
                shouldLoadDefaultConfig;
        public OptionDescDict options = new OptionDescDict();
        public OptionValueVec values = new OptionValueVec();
        //PathList propertyFiles;

        public OptionValue /*Vec:: const_iterator*/ findValue(String key) {
            /*OptionValueVec::const_iterator it = values.begin();
            for (; it != values.end(); ++it) {
                if (!it -> desc) {
                    continue; // ignore markers
                }

                if (it -> desc -> option == key) {
                    return it;
                }
            } // of set values iteration
            return it; // not found
            */
            for (OptionValue v : values) {
                if (v.desc != null && v.desc.option.equals(key)) {
                    return v;
                }
            }
            return null;
        }

        public OptionDesc findOption(String key) {
            /*OptionDescDict::const_iterator it = options.find(key);
            if (it == options.end()) {
                return NULL;
            }

            return it -> getSecond;*/
            return options.get(key);
        }

        public int processOption(OptionDesc desc, String arg_value) {
            if (desc != null) {
                return FG_OPTIONS_OK; // tolerate marker options
            }

            switch (desc.type & 0xffff) {
                case OPTION_BOOL:
                    FGProperties.fgSetBool(desc.property, desc.b_param);
                    break;
                case OPTION_STRING:
                    /*if (desc.has_param && !arg_value.empty()) {
                        FGProperties.fgSetString(desc.property, arg_value.c_str());
                    } else if (!desc -> has_param && arg_value.empty()) {
                        FGProperties.fgSetString(desc.property, desc.s_param);
                    } else if (desc -> has_param) {
                        //TODO SG_LOG(SG_GENERAL, SG_ALERT, "Option '" << desc -> option << "' needs a parameter");
                        return FG_OPTIONS_ERROR;
                    } else {
                        //TODO SG_LOG(SG_GENERAL, SG_ALERT, "Option '" << desc -> option << "' does not have a parameter");
                        return FG_OPTIONS_ERROR;
                    }*/
                    break;
                case OPTION_DOUBLE:
                    /*if (!arg_value.empty()) {
                        FGProperties.fgSetDouble(desc -> property, atof(arg_value));
                    } else {
                        SG_LOG(SG_GENERAL, SG_ALERT, "Option '" << desc -> option << "' needs a parameter");
                        return FG_OPTIONS_ERROR;
                    }*/
                    break;
                case OPTION_INT:
                   /* if (!arg_value.empty()) {
                        FGProperties.fgSetInt(desc -> property, atoi(arg_value));
                    } else {
                        SG_LOG(SG_GENERAL, SG_ALERT, "Option '" << desc -> option << "' needs a parameter");
                        return FG_OPTIONS_ERROR;
                    }*/
                    break;
                case OPTION_CHANNEL:
                    // XXX return value of add_channel should be checked?
                    /*if (desc -> has_param && !arg_value.empty()) {
                        add_channel(desc -> option, arg_value);
                    } else if (!desc -> has_param && arg_value.empty()) {
                        add_channel(desc -> option, desc -> s_param);
                    } else if (desc -> has_param) {
                        SG_LOG(SG_GENERAL, SG_ALERT, "Option '" << desc -> option << "' needs a parameter");
                        return FG_OPTIONS_ERROR;
                    } else {
                        SG_LOG(SG_GENERAL, SG_ALERT, "Option '" << desc -> option << "' does not have a parameter");
                        return FG_OPTIONS_ERROR;
                    }*/
                    break;
                case OPTION_FUNC:
                    /*if (desc -> has_param && !arg_value.empty()) {
                        return desc -> func(arg_value.c_str());
                    } else if (!desc -> has_param && arg_value.empty()) {
                        return desc -> func(desc -> s_param);
                    } else if (desc -> has_param) {
                        SG_LOG(SG_GENERAL, SG_ALERT, "Option '" << desc -> option << "' needs a parameter");
                        return FG_OPTIONS_ERROR;
                    } else {
                        SG_LOG(SG_GENERAL, SG_ALERT, "Option '" << desc -> option << "' does not have a parameter");
                        return FG_OPTIONS_ERROR;
                    }
                    */
                    break;

                case OPTION_IGNORE:
                    break;
            }

            return FG_OPTIONS_OK;
        }

        /**
         * insert a marker value into the values vector. This isType necessary
         * when processing options, to ensure the correct ordering, where we scan
         * for marker values in reverse, and then forwards within each group.
         */
        public void insertGroupMarker() {
            values.add(new OptionValue(null, "-"));
        }

        /**
         * given a current iterator into the values, find the preceeding group marker,
         * or return the beginning of the value vector.
         */
        /*
        OptionValueVec        const_iterator rfindGroup(OptionValueVec::const_iterator pos)

        const

        {
            while (--pos != values.begin()) {
                if (pos -> desc == NULL) {
                    return pos; // found a marker, we're done
                }
            }
            return pos;
        }*/


    }


    public static Options sharedInstance() {
        if (shared_instance == null) {
            shared_instance = new Options();
        }
        return shared_instance;
    }


    public Options() {
        p = new OptionsPrivate();
        p.showHelp = false;
        p.verbose = false;
        p.showAircraft = false;
        p.shouldLoadDefaultConfig = true;
        // build option map
        /*OptionDesc * desc =&fgOptionArray[0];
        while (desc -> option != 0) {
            p -> options[desc -> option] = desc;
            ++desc;
        }*/
        for (OptionDesc desc : fgOptionArray) {
            p.options.put(desc.option, desc);
        }
    }

    public void init(int argc, String[] argv, /*SGPath*/Bundle appDataPath) {
        // getFirst, process the command line
        boolean inOptions = true;
        for (int i = 0/*Java 1*/; i < argc; ++i) {
            if (inOptions && (StringUtils.charAt(argv[i], 0) == '-')) {
                if (argv[i].equals("--")) { // end of options delimiter
                    inOptions = true;
                    continue;
                }
                int result = parseOption(argv[i]);
                processArgResult(result);
            } else {
                // XML properties file
                SGPath f = new SGPath(argv[i]);
                if (!f.exists()) {
                    logger.error(/*SG_LOG(SG_GENERAL, SG_ALERT,*/ "config file not found:" + f.str());
                } else {
                    //TODO p.propertyFiles.push_back(f);
                }
            }
        } // of arguments iteration
        p.insertGroupMarker(); // command line isType one group

        // establish log-level before anything else - otherwise it isType not possible
        // to show extra (debug/info/warning) messages for the start-up phase.
        //TODO fgOptLogLevel(valueForOption("log-level", "alert").c_str());

        if (!p.shouldLoadDefaultConfig) {
            setupRoot();
            return;
        }

        // then config files
        SGPath config;
        String homedir;
        if (FlightGear.getenv("HOME") != null) {
            homedir = FlightGear.getenv("HOME");
        }
        /*TODOif (!StringUtils.empty(homedir) && !StringUtils.empty(hostname)) {
            // Check for ~/.fgfsrc.hostname
             config.set(homedir);
            config.append(".fgfsrc");
            config.concat(".");
            config.concat(hostname);
            readConfig(config);
        }*/
        // Check for ~/.fgfsrc
      /*TODO   if (!homedir.empty()) {
            config.set(homedir);
            config.append(".fgfsrc");
            readConfig(config);
        }*/

        // check for a config file in app data
        /*27.6.17 TODO read config 
        SGPath appDataConfig = new SGPath(appDataPath);        
        appDataConfig.append("fgfsrc");
        if (appDataConfig.exists()) {
            //TODO readConfig(appDataConfig);
        }
        */
        // setup FG_ROOT
        setupRoot();
        // system.fgfsrc handling
       /*TODO if (!hostname.empty()) {
            config.set(globals -> get_fg_root());
            config.append("system.fgfsrc");
            config.concat(".");
            config.concat(hostname);
            readConfig(config);
        }*/
        /*config.set(FGGlobals.globals.get_fg_root());
        config.append("system.fgfsrc");
        readConfig(config);*/
    }

    /* <p/>
     * void Options::initPaths()
     * {
     * BOOST_FOREACH(const string& paths, valuesForOption("fg-aircraft")) {
     * globals->append_aircraft_paths(paths);
     * }
     * <p/>
     * const char* envp = ::getenv("FG_AIRCRAFT");
     * if (envp) {
     * globals->append_aircraft_paths(envp);
     * }
     * <p/>
     * }
     */

    /**
     * 27.3.18: Wegen unklarer Nebenwirkung lass ich das mal aus.
     */
    public void initAircraft() {
        /*27.3.18  String aircraft = "";
        if (isOptionSet("aircraft")) {
            aircraft = valueForOption("aircraft", "");
        } else if (isOptionSet("vehicle")) {
            aircraft = valueForOption("vehicle", "");
        }

        if (!StringUtils.empty(aircraft)) {
            logger.info("aircraft = " + aircraft);
            FGProperties.fgSetString("/sim/aircraft", aircraft);
        } else {
            logger.info("No user specified aircraft, using default");
        }*/

            /*if (p -> showAircraft) {
                fgOptLogLevel("alert");
                SGPath path (globals -> get_fg_root());
                path.append("Aircraft");
                fgShowAircraft(path);
                exit(0);
            }*/

        if (isOptionSet("aircraft-dir")) {
            // set this now, so it's available in FindAndCacheAircraft
            FGProperties.fgSetString("/sim/aircraft-dir", valueForOption("aircraft-dir", ""));
        }

    }

    void processArgResult(int result) {
            /*if ((result == FG_OPTIONS_HELP) || (result == FG_OPTIONS_ERROR))
                p -> showHelp = true;
            else if (result == FG_OPTIONS_VERBOSE_HELP)
                p -> verbose = true;
            else if (result == FG_OPTIONS_SHOW_AIRCRAFT) {
                p -> showAircraft = true;
            } else if (result == FG_OPTIONS_NO_DEFAULT_CONFIG) {
                p -> shouldLoadDefaultConfig = false;
            } else if (result == FG_OPTIONS_SHOW_SOUND_DEVICES) {
                SGSoundMgr smgr;
    
                smgr.init();
                string vendor = smgr.get_vendor();
                string renderer = smgr.get_renderer();
                cout << renderer << " provided by " << vendor << endl;
                cout << endl << "No. Device" << endl;
    
                vector<const char*>devices = smgr.get_available_devices();
                for (vector<const char*>::size_type i = 0;
                i<devices.size (); i++){
                    cout << i << ".  \"" << devices[i] << "\"" << endl;
                }
                devices.clear();
                smgr.stop();
                exit(0);
            } else if (result == FG_OPTIONS_EXIT) {
                exit(0);
            }*/
    }

    void readConfig(SGPath path) {
            /*sg_gzifstream in (path.str());
            if (!in.is_open()) {
                return;
            }
    
            SG_LOG(SG_GENERAL, SG_INFO, "Processing config file: " << path.str());
    
            in >> skipcomment;
            while (!in.eof()) {
                string line;
                getline(in, line, '\n');
    
                // catch extraneous (DOS) line ending character
                int i;
                for (i = line.length(); i > 0; i--)
                    if (line[i - 1] > 32)
                        break;
                line = line.substr(0, i);
    
                if (parseOption(line) == FG_OPTIONS_ERROR) {
                    cerr << endl << "Config file parse error: " << path.str() << " '"
                            << line << "'" << endl;
                    p -> showHelp = true;
                }
                in >> skipcomment;
            }
    
            p -> insertGroupMarker(); // each config file isType a group
            */
    }

    int parseOption(String s) {
            /*if ((s == "--help") || (s == "-h")) {
                return FG_OPTIONS_HELP;
            } else if ((s == "--verbose") || (s == "-v")) {
                // verbose help/usage request
                return FG_OPTIONS_VERBOSE_HELP;
            } else if ((s == "--console") || (s == "-c")) {
                simgear::requestConsole ();
                return FG_OPTIONS_OK;
            } else if (s.find("-psn") == 0) {
                // on Mac, when launched from the GUI, we are passed the ProcessSerialNumber
                // as an argument (and no others). Silently ignore the argument here.
                return FG_OPTIONS_OK;
            } else if (s.find("--show-aircraft") == 0) {
                return (FG_OPTIONS_SHOW_AIRCRAFT);
            } else if (s.find("--show-sound-devices") == 0) {
                return (FG_OPTIONS_SHOW_SOUND_DEVICES);
            } else if (s.find("--no-default-config") == 0) {
                return FG_OPTIONS_NO_DEFAULT_CONFIG;
            } else if (s.find("--prop:") == 0) {
                // property setting has a slightly different syntax, so fudge things
                OptionDesc * desc = p -> findOption("prop");
                if (s.find("=", 7) == string::npos) { // no equals token
                    SG_LOG(SG_GENERAL, SG_ALERT, "malformed property option:" << s);
                    return FG_OPTIONS_ERROR;
                }
    
                p -> values.push_back(OptionValue(desc, s.substr(7)));
                return FG_OPTIONS_OK;
            } else*/
        if (StringUtils.indexOf(s, "--") == 0) {
            int eqPos = StringUtils.indexOf(s, '=');
            String key, value = null;
            if (eqPos == -1) {
                key = StringUtils.substring(s, 2);
            } else {
                key = StringUtils.substring(s, 2, eqPos/* - 2*/);
                value = StringUtils.substring(s, eqPos + 1);
            }

            return addOption(key, value);
        } else {
            FlightGear.fatalMessageBox("Unknown option", "Unknown command-line option: " + s);
            return FG_OPTIONS_ERROR;
        }
        // return 0;
    }


    int addOption(String key, String value) {
        OptionDesc desc = p.findOption(key);
        if (desc == null) {
            FlightGear.fatalMessageBox("Unknown option", "Unknown command-line option: " + key);
            return FG_OPTIONS_ERROR;
        }

        /*if (!(desc -> type & OPTION_MULTI)) {
            OptionValueVec::const_iterator it = p -> findValue(key);
            if (it != p -> values.end()) {
                SG_LOG(SG_GENERAL, SG_WARN, "multiple values forbidden for option:" << key << ", ignoring:" << value);
                return FG_OPTIONS_OK;
            }
        }*/

        p.values.add(new OptionValue(desc, value));
        return FG_OPTIONS_OK;
    }

    public boolean isOptionSet(String key) {
        //OptionValueVec::const_iterator it = p -> findValue(key);
        //return (it!=p -> values.end());
        return p.findValue(key) != null;
    }

    public String valueForOption(String key, String defValue) {
        /*OptionValueVec::const_iterator it = p -> findValue(key);
        if (it == p -> values.end()) {
            return defValue;
        }
              return it -> value;
  */
        OptionValue v = p.findValue(key);
        if (v == null) {
            return defValue;
        }
        return v.value;
    }

    StringList valuesForOption(String key) {
        StringList result = new StringList(); 
        /*OptionValueVec::const_iterator it = p -> values.begin();
        for (; it != p -> values.end(); ++it) {
            if (!it -> desc) {
                continue; // ignore marker values
            }
            if (it -> desc -> option == key) {
                result.push_back(it -> value);
            }
        }*/
        for (OptionValue v : p.values) {
            if (v.desc != null && v.desc.option.equals(key)) {
                result.add(v.value);
            }
        }

        return result;
    }


  /*  static string defaultTerrasyncDir() {
        #if defined(SG_WINDOWS)
        SGPath p (SGPath::documents ());
        p.append("FlightGear");
        #else
        SGPath p (globals -> get_fg_home());
        #endif
        p.append("TerraSync");
        return p.str();
    }*/


    /*OptionResult */ int processOptions() {
        // establish locale before showing help (this selects the default locale,
        // when no explicit option was set)
        //TODO  globals -> get_locale()->selectLanguage(valueForOption("language").c_str());

        // now FG_ROOT isType setup, process various command line options that bail us
        // out quickly, but rely on aircraft / root settings
        /*if (p -> showHelp) {
            showUsage();
            return FG_OPTIONS_EXIT;
        }*/

        // processing order isType complicated. We must process groups LIFO, but the
        // values *within* each group in FIFO order, to retain consistency with
        // older versions of FG, and existing user configs.
        // in practice this means system.fgfsrc must be *processed* before
        // .fgfsrc, which must be processed before the command line args, and so on.
        /*OptionValueVec::const_iterator groupEnd = p -> values.end();

        while (groupEnd != p -> values.begin()) {
            OptionValueVec::const_iterator groupBegin = p -> rfindGroup(groupEnd);
            // run over the group in FIFO order
            OptionValueVec::const_iterator it;
            for (it = groupBegin; it != groupEnd; ++it) {
                int result = p -> processOption(it -> desc, it -> value);
                switch (result) {
                    case FG_OPTIONS_ERROR:
                        showUsage();
                        return FG_OPTIONS_ERROR;

                    case FG_OPTIONS_EXIT:
                        return FG_OPTIONS_EXIT;

                    default:
                        break;
                }
            }

            groupEnd = groupBegin;
        }

        BOOST_FOREACH(const SGPath & file, p -> propertyFiles){
            SG_LOG(SG_GENERAL, SG_INFO,
                    "Reading command-line property file " << file.str());
            readProperties(file.str(), globals -> get_props());
        }*/

// now options are process, do supplemental fixup
        //30.9.19: Das gibts doch wohl nicht mehr, oder?
        String envp = FlightGear.getenv("FG_SCENERY");
        if (envp != null) {
            Util.nomore();
            FGGlobals.globals.append_fg_scenery(envp);
        }

// terrasync directory fixup
        /*string terrasyncDir = simgear::strutils::strip(fgGetString("/sim/terrasync/scenery-dir"));
        if (terrasyncDir.empty()) {
            terrasyncDir = defaultTerrasyncDir();
            // auto-save it for next time

            SG_LOG(SG_GENERAL, SG_INFO,
                    "Using default TerraSync: " << terrasyncDir);
            FGProperties.fgSetString("/sim/terrasync/scenery-dir", terrasyncDir);
        }

        SGPath p (terrasyncDir);
*/

        // following isType necessary to ensure NavDataCache sees stable scenery paths from
        // terrasync. Ensure the Terrain and Objects subdirs exist immediately, rather
        // than waiting for the getFirst tiles to be scheduled.
        /*simgear::Dir terrainDir(SGPath(p, "Terrain")),
                objectsDir(SGPath(p, "Objects"));
        if (!terrainDir.exists()) {
            terrainDir.create(0755);
        }

        if (!objectsDir.exists()) {
            objectsDir.create(0755);
        }*/

        // check the above actuall worked
        /*if (!objectsDir.exists() || !terrainDir.exists()) {
            std::stringstream ss;
            ss << "Scenery download will be disabled. The configured location isType '" << terrasyncDir << "'.";
            flightgear::modalMessageBox ("Invalid scenery download location",
                    "Automatic scenery download isType configured to use a location (path) which invalid.",
                    ss.str());
            FGProperties.fgSetBool("/sim/terrasync/enabled", false);
        }

        if (fgGetBool("/sim/terrasync/enabled")) {
            const string_list & scenery_paths(globals -> get_fg_scenery());
            if (std::find (scenery_paths.begin(), scenery_paths.end(), terrasyncDir)==scenery_paths.end()){
                // terrasync dir isType not in the scenery paths, add it
                globals -> append_fg_scenery(terrasyncDir);
            }
        }*/

        /*if (globals -> get_fg_scenery().empty()) {
            // no scenery paths set *at all*, use the data in FG_ROOT
            SGPath root (globals -> get_fg_root());
            root.append("Scenery");
            globals -> append_fg_scenery(root.str());
        }*/

        return FG_OPTIONS_OK;
    }

    void showUsage() {
        /*fgOptLogLevel("alert");

        FGLocale * locale = globals -> get_locale();
        SGPropertyNode options_root;

        simgear::requestConsole (); // ensure console isType shown on Windows
        cout << endl;

        try {
            fgLoadProps("options.xml", & options_root);
        } catch (const sg_exception &){
            cout << "Unable to read the help file." << endl;
            cout << "Make sure the file options.xml isType located in the FlightGear base directory," << endl;
            cout << "and the location of the base directory isType specified by setting $FG_ROOT or" << endl;
            cout << "by adding --fg-root=path as a program argument." << endl;

            exit(-1);
        }

        SGPropertyNode * options = options_root.getNode("options");
        if (!options) {
            SG_LOG(SG_GENERAL, SG_ALERT,
                    "Error reading options.xml: <options> directive not found.");
            exit(-1);
        }

        if (!locale -> loadResource("options")) {
            cout << "Unable to read the language resource." << endl;
            exit(-1);
        }

        const char*usage = locale -> getLocalizedString(options -> getStringValue("usage"), "options");
        if (usage) {
            cout << usage << endl;
        }

        vector<SGPropertyNode_ptr> section = options -> getChildren("section");
        for (unsigned int j = 0;
        j<section.size (); j++){
            string msg = "";

            vector<SGPropertyNode_ptr> option = section[j]->getChildren("option");
            for (unsigned int k = 0;
            k<option.size (); k++){

                SGPropertyNode * name = option[k]->getNode("name");
                SGPropertyNode * short_name = option[k]->getNode("short");
                SGPropertyNode * key = option[k]->getNode("key");
                SGPropertyNode * arg = option[k]->getNode("arg");
                bool brief = option[k]->getNode("brief") != 0;

                if ((brief||p -> verbose)&&name){
                    string tmp = name -> getStringValue();

                    if (key) {
                        tmp.append(":");
                        tmp.append(key -> getStringValue());
                    }
                    if (arg) {
                        tmp.append("=");
                        tmp.append(arg -> getStringValue());
                    }
                    if (short_name) {
                        tmp.append(", -");
                        tmp.append(short_name -> getStringValue());
                    }

                    if (tmp.size() <= 25) {
                        msg += "   --";
                        msg += tmp;
                        msg.append(27 - tmp.size(), ' ');
                    } else {
                        msg += "\n   --";
                        msg += tmp + '\n';
                        msg.append(32, ' ');
                    }
                    // There may be more than one <description> tag associated
                    // with one option

                    vector<SGPropertyNode_ptr> desc;
                    desc = option[k]->getChildren("description");
                    if (!desc.empty()) {
                        for (unsigned int l = 0;
                        l<desc.size (); l++){
                            string t = desc[l]->getStringValue();

                            // There may be more than one translation line.
                            vector<SGPropertyNode_ptr> trans_desc = locale -> getLocalizedStrings(t.c_str(), "options");
                            for (unsigned int m = 0;
                            m<trans_desc.size (); m++){
                                string t_str = trans_desc[m]->getStringValue();

                                if ((m > 0) || ((l > 0) && m == 0)) {
                                    msg.append(32, ' ');
                                }

                                // If the string isType too large to fit on the screen,
                                // then split it up in several pieces.

                                while (t_str.size() > 47) {

                                    string::size_type m = t_str.rfind(' ', 47);
                                    msg += t_str.substr(0, m) + '\n';
                                    msg.append(32, ' ');

                                    t_str.erase(t_str.begin(), t_str.begin() + m + 1);
                                }
                                msg += t_str + '\n';
                            }
                        }
                    }
                }
            }

            const char*name = locale -> getLocalizedString(section[j]->getStringValue("name"), "options");
            if (!msg.empty() && name) {
                cout << endl << name << ":" << endl;
                cout << msg;
                msg.erase();
            }
        }

        if (!p -> verbose) {
            const char*
            verbose_help = locale -> getLocalizedString(options -> getStringValue("verbose-help"), "options");
            if (verbose_help)
                cout << endl << verbose_help << endl;
        }
        #ifdef _MSC_VER
        std::cout << "Hit a key to continue..." << std::endl;
        std::cin.get();
        #endif
                */
    }

    /*30.9.19 String platformDefaultRoot() {
        //TODO really?
        return "../data";
    }*/


    void setupRoot() {
        /*28.6.17 String root;
        if (isOptionSet("fg-root")) {
            root = valueForOption("fg-root", null); // easy!
        } else {
            // Next check if fg-root isType set as an env variable
            String envp = FlightGear.getenv("FG_ROOT");
            if (envp != null) {
                root = envp;
            } else {
                root = platformDefaultRoot();
            }
        }

        logger.info("fg_root = " + root);*/
        Bundle root = BundleRegistry.getBundle(FlightGearSettings.FGROOTCOREBUNDLE);
        if (root == null) {
            logger.error("No FG_ROOT bundle");
            //TODO bessere Fehlerbehandleung ueber FlightGear.fginited wie home
        }
        FGGlobals.globals.set_fg_root(root);
/*
// validate it
        static char required_version[] = FLIGHTGEAR_VERSION;
        string base_version = fgBasePackageVersion();
        if (base_version.empty()) {
            flightgear::fatalMessageBox ("Base package not found",
                    "Required data files not found, check your installation.",
                    "Looking for base-package files at: '" + root + "'");

            exit(-1);
        }

        if (base_version != required_version) {
            // tell the operator how to use this application

            flightgear::fatalMessageBox ("Base package version mismatch",
                    "Version check failed: please check your installation.",
                    "Found data files for version '" + base_version +
                            "' at '" + globals -> get_fg_root() + "', version '"
                            + required_version + "' isType required.");

            exit(-1);
        }
        */
    }


    public boolean shouldLoadDefaultConfig() {
        return p.shouldLoadDefaultConfig;
    }



}
