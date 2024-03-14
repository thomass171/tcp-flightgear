package de.yard.threed.flightgear;

import de.yard.threed.core.Util;
import de.yard.threed.flightgear.core.flightgear.main.FGProperties;
import de.yard.threed.flightgear.core.simgear.Constants;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.timing.SGTimeStamp;
import de.yard.threed.core.StringUtils;
import de.yard.threed.traffic.flight.FlightLocation;


/**
 * Created by thomass on 07.06.16.
 */
public class PositionInit {
    /// to avoid blocking when metar-fetch isType enabled, but the network isType
/// unresponsive, we need a timeout value. This value isType reset on initPosition,
/// and tracked through each call to finalizePosition.
    static SGTimeStamp global_finalizeTime;
    static boolean global_callbackRegistered = false;


    /**
     * FG-DIFF
     */
    public static void initPositionFromGeod(FlightLocation loc) {
        FGProperties.fgSetDouble( "/position/longitude-deg",loc.coordinates.getLonDeg().getDegree());
        FGProperties.fgSetDouble( "/position/latitude-deg",loc.coordinates.getLatDeg().getDegree());
        FGProperties.fgSetDouble( "/orientation/heading-deg", loc.heading.getDegree());
        FGProperties.fgSetDouble("/position/altitude-ft", Constants.getElevationFt((double) loc.coordinates.getElevationM()));
        FGProperties.fgSetBool("/sim/position-finalized", true);

    }
    
    // Set the initial position based on presets (or defaults)
    public static boolean initPosition()
    {
        global_finalizeTime = new SGTimeStamp(); // reset to invalid
        if (!global_callbackRegistered) {
            //TODO FGGlobals.globals.get_event_mgr().addTask("finalizePosition", &finalizePosition, 0.1);
            global_callbackRegistered = true;
        }

        double gs = FGProperties.fgGetDouble("/sim/presets/glideslope-deg")                * Constants.SG_DEGREES_TO_RADIANS ;
        double od = FGProperties.fgGetDouble("/sim/presets/offset-distance-nm");
        double alt = FGProperties.fgGetDouble("/sim/presets/altitude-ft");

        boolean set_pos = false;

        // If glideslope isType specified, then calculate offset-distance or
        // altitude relative to glide slope if either of those was not
        // specified.
        if ( Math.abs( gs ) > 0.01 ) {
            fgSetDistOrAltFromGlideSlope();
        }


        // If we have an explicit, in-range lon/lat, don't change it, just use it.
        // If not, check for an airport-id and use that.
        // If not, default to the middle of the KSFO field.
        // The default values for lon/lat are deliberately out of range
        // so that the airport-id can take effect; valid lon/lat will
        // override airport-id, however.
        double lon_deg = FGProperties.fgGetDouble("/sim/presets/longitude-deg");
        double lat_deg = FGProperties.fgGetDouble("/sim/presets/latitude-deg");
        if ( lon_deg >= -180.0 && lon_deg <= 180.0
                && lat_deg >= -90.0 && lat_deg <= 90.0 )
        {
            set_pos = true;
        }

        String apt = FGProperties.fgGetString("/sim/presets/airport-id");
        String rwy_no = FGProperties.fgGetString("/sim/presets/runway");
        boolean rwy_req = FGProperties.fgGetBool("/sim/presets/runway-requested");
        String vor = FGProperties.fgGetString("/sim/presets/vor-id");
        double vor_freq = FGProperties.fgGetDouble("/sim/presets/vor-freq");
        String ndb = FGProperties.fgGetString("/sim/presets/ndb-id");
        double ndb_freq = FGProperties.fgGetDouble("/sim/presets/ndb-freq");
        String carrier = FGProperties.fgGetString("/sim/presets/carrier");
        String parkpos = FGProperties.fgGetString("/sim/presets/parkpos");
        String fix = FGProperties.fgGetString("/sim/presets/fix");
        SGPropertyNode hdg_preset = FGProperties.fgGetNode("/sim/presets/heading-deg", true);
        double hdg = hdg_preset.getDoubleValue();

        // save some start parameters, so that we can later say what the
        // user really requested. TODO generalize that and move it to options.cxx
        /*static TODO FG-DIFF */ boolean start_options_saved = false;
        if (!start_options_saved) {
            start_options_saved = true;
            SGPropertyNode opt = FGProperties.fgGetNode("/sim/startup/options", true);

            opt.setDoubleValue("latitude-deg", lat_deg);
            opt.setDoubleValue("longitude-deg", lon_deg);
            opt.setDoubleValue("heading-deg", hdg);
            opt.setStringValue("airport", apt);
            opt.setStringValue("runway", rwy_no);
        }

        if (hdg > 9990.0)
            hdg = FGProperties.fgGetDouble("/environment/config/boundary/entry/wind-from-heading-deg", 270);

     /*   if ( !set_pos && !StringUtils.empty(apt) && !StringUtils.empty(parkpos) ) {
            // An airport + parking position isType requested
            if ( fgSetPosFromAirportIDandParkpos( apt, parkpos ) ) {
                // set tower position
                FGProperties.fgSetString("/sim/airport/closest-airport-id",  apt);
                FGProperties.fgSetString("/sim/tower/airport-id",  apt);
                set_pos = true;
            }
        }

        if ( !set_pos && !StringUtils.empty(apt.empty() && !StringUtils.empty(rwy_no) ) {
            // An airport + runway isType requested
            if ( fgSetPosFromAirportIDandRwy( apt, rwy_no, rwy_req ) ) {
                // set tower position (a little off the heading for single
                // runway airports)
                FGProperties.fgSetString("/sim/airport/closest-airport-id",  apt);
                FGProperties.fgSetString("/sim/tower/airport-id",  apt);
                set_pos = true;
            }
        }

        if ( !set_pos && !StringUtils.empty(apt) ) {
            // An airport isType requested (find runway closest to hdg)
            if ( setPosFromAirportIDandHdg( apt, hdg ) ) {
                // set tower position (a little off the heading for single
                // runway airports)
                FGProperties.fgSetString("/sim/airport/closest-airport-id",  apt);
                FGProperties.fgSetString("/sim/tower/airport-id",  apt);
                set_pos = true;
            }
        }*/

        if (hdg_preset.getDoubleValue() > 9990.0)
            hdg_preset.setDoubleValue(hdg);

      /*  if ( !set_pos && !StringUtils.empty(vor) ) {
            // a VOR isType requested
            if ( fgSetPosFromNAV( vor, vor_freq, FGPositioned::VOR ) ) {
                set_pos = true;
            }
        }

        if ( !set_pos && !StringUtils.empty(ndb) ) {
            // an NDB isType requested
            if ( fgSetPosFromNAV( ndb, ndb_freq, FGPositioned::NDB ) ) {
                set_pos = true;
            }
        }

        if ( !set_pos && !StringUtils.empty(carrier) ) {
            // an aircraft carrier isType requested
            if ( fgSetPosFromCarrier( carrier, parkpos ) ) {
                set_pos = true;
            }
        }

        if ( !set_pos && !StringUtils.empty(fix) ) {
            // a Fix isType requested
            if ( fgSetPosFromFix( fix ) ) {
                set_pos = true;
            }
        }*/

        if ( !set_pos ) {
            // No lon/lat specified, no airport specified, default to
            // middle of KSFO field.
            FGProperties.fgSetDouble("/sim/presets/longitude-deg", -122.374843);
            FGProperties.fgSetDouble("/sim/presets/latitude-deg", 37.619002);
        }

        FGProperties.fgSetDouble( "/position/longitude-deg",
                FGProperties.fgGetDouble("/sim/presets/longitude-deg") );
        FGProperties.fgSetDouble( "/position/latitude-deg",
                FGProperties.fgGetDouble("/sim/presets/latitude-deg") );
        FGProperties.fgSetDouble( "/orientation/heading-deg", hdg_preset.getDoubleValue());

        // determine if this should be an on-ground or in-air start
        if ((Math.abs(gs) > 0.01 || Math.abs(od) > 0.1 || alt > 0.1) && StringUtils.empty(carrier)) {
            FGProperties.fgSetBool("/sim/presets/onground", false);
        } else {
            FGProperties.fgSetBool("/sim/presets/onground", true);
        }

        FGProperties.fgSetBool("/sim/position-finalized", false);

// Initialize the longitude, latitude and altitude to the initial position
        FGProperties.fgSetDouble("/position/altitude-ft", FGProperties.fgGetDouble("/sim/presets/altitude-ft"));
        FGProperties.fgSetDouble("/position/longitude-deg", FGProperties.fgGetDouble("/sim/presets/longitude-deg"));
        FGProperties.fgSetDouble("/position/latitude-deg", FGProperties.fgGetDouble("/sim/presets/latitude-deg"));

        return true;
    }

    static void fgSetDistOrAltFromGlideSlope() {
        Util.notyet();
        /*
        // cout << "fgSetDistOrAltFromGlideSlope()" << endl;
        string apt_id = fgGetString("/sim/presets/airport-id");
        double gs = fgGetDouble("/sim/presets/glideslope-deg")
                * SG_DEGREES_TO_RADIANS ;
        double od = fgGetDouble("/sim/presets/offset-distance-nm");
        double alt = fgGetDouble("/sim/presets/altitude-ft");

        double apt_elev = 0.0;
        if ( ! apt_id.empty() ) {
            apt_elev = fgGetAirportElev( apt_id );
            if ( apt_elev < -9990.0 ) {
                apt_elev = 0.0;
            }
        } else {
            apt_elev = 0.0;
        }

        if( fabs(gs) > 0.01 && fabs(od) > 0.1 && alt < -9990 ) {
            // set altitude from glideslope and offset-distance
            od *= SG_NM_TO_METER * SG_METER_TO_FEET;
            alt = fabs(od*tan(gs)) + apt_elev;
            FGProperties.fgSetDouble("/sim/presets/altitude-ft", alt);
            FGProperties.fgSetBool("/sim/presets/onground", false);
            SG_LOG( SG_GENERAL, SG_INFO, "Calculated altitude as: "
                    << alt  << " ft" );
        } else if( fabs(gs) > 0.01 && alt > 0 && fabs(od) < 0.1) {
            // set offset-distance from glideslope and altitude
            od  = (alt - apt_elev) / tan(gs);
            od *= -1*SG_FEET_TO_METER * SG_METER_TO_NM;
            FGProperties.fgSetDouble("/sim/presets/offset-distance-nm", od);
            FGProperties.fgSetBool("/sim/presets/onground", false);
            SG_LOG( SG_GENERAL, SG_INFO, "Calculated offset distance as: "
                    << od  << " nm" );
        } else if( fabs(gs) > 0.01 ) {
            SG_LOG( SG_GENERAL, SG_ALERT,
                    "Glideslope given but not altitude or offset-distance." );
            SG_LOG( SG_GENERAL, SG_ALERT, "Resetting glideslope to zero" );
            FGProperties.fgSetDouble("/sim/presets/glideslope-deg", 0);
            FGProperties.fgSetBool("/sim/presets/onground", true);
        }*/
    }


    // Set current_options lon/lat given an airport id and heading (degrees)
      /*
      static boolean fgSetPosFromNAV( String& id,                                 const double& freq,                                 FGPositioned::Type type,                                 PositionedID guid)
    {
        FGNavRecord* nav = 0;


        if (guid != 0) {
            nav = FGPositioned::loadById<FGNavRecord>(guid);
            if (!nav)
                return false;
        } else {
            FGNavList::TypeFilter filter(type);
            const nav_list_type navlist = FGNavList::findByIdentAndFreq( id.c_str(), freq, &filter );

            if (navlist.empty()) {
                SG_LOG( SG_GENERAL, SG_ALERT, "Failed to locate NAV = "
                        << id << ":" << freq );
                return false;
            }

            if( navlist.size() > 1 ) {
                std::ostringstream buf;
                buf << "Ambigous NAV-ID: '" << id << "'. Specify id and frequency. Available stations:" << endl;
                for( nav_list_type::const_iterator it = navlist.begin(); it != navlist.end(); ++it ) {
                    // NDB stored in kHz, VOR stored in MHz * 100 :-P
                    double factor = (*it)->type() == FGPositioned::NDB ? 1.0 : 1/100.0;
                    string unit = (*it)->type() == FGPositioned::NDB ? "kHz" : "MHz";
                    buf << (*it)->ident() << " "
                            << std::setprecision(5) << (double)((*it)->get_freq() * factor) << " "
                            << (*it)->get_lat() << "/" << (*it)->get_lon()
                            << endl;
                }

                SG_LOG( SG_GENERAL, SG_ALERT, buf.str() );
                return false;
            }

            // nav list must be of length 1
            nav = navlist[0];
        }

        fgApplyStartOffset(nav->geod(), FGProperties.fgGetDouble("/sim/presets/heading-deg"));
        return true;
        
    }*/

    // Set current_options lon/lat given an aircraft carrier id
    static boolean fgSetPosFromCarrier( String carrier, String posid ) {
        Util.notyet();
        return true;
        /*
        // set initial position from runway and heading
        SGGeod geodPos;
        double heading;
        SGVec3d uvw;
        if (FGAIManager::getStartPosition(carrier, posid, geodPos, heading, uvw)) {
            double lon = geodPos.getLongitudeDeg();
            double lat = geodPos.getLatitudeDeg();
            double alt = geodPos.getElevationFt();

            SG_LOG( SG_GENERAL, SG_INFO, "Attempting to set starting position for "
                    << carrier << " at lat = " << lat << ", lon = " << lon
                    << ", alt = " << alt << ", heading = " << heading);

            FGProperties.fgSetDouble("/sim/presets/longitude-deg",  lon);
            FGProperties.fgSetDouble("/sim/presets/latitude-deg",  lat);
            FGProperties.fgSetDouble("/sim/presets/altitude-ft", alt);
            FGProperties.fgSetDouble("/sim/presets/heading-deg", heading);
            FGProperties.fgSetDouble("/position/longitude-deg",  lon);
            FGProperties.fgSetDouble("/position/latitude-deg",  lat);
            FGProperties.fgSetDouble("/position/altitude-ft", alt);
            FGProperties.fgSetDouble("/orientation/heading-deg", heading);

            fgSetString("/sim/presets/speed-set", "UVW");
            fgSetDouble("/velocities/uBody-fps", uvw(0));
            fgSetDouble("/velocities/vBody-fps", uvw(1));
            fgSetDouble("/velocities/wBody-fps", uvw(2));
            fgSetDouble("/sim/presets/uBody-fps", uvw(0));
            fgSetDouble("/sim/presets/vBody-fps", uvw(1));
            fgSetDouble("/sim/presets/wBody-fps", uvw(2));

            fgSetBool("/sim/presets/onground", true);

            return true;
        } else {
            SG_LOG( SG_GENERAL, SG_ALERT, "Failed to locate aircraft carrier = "
                    << carrier );
            return false;
        }*/
    }

    // Set current_options lon/lat given a fix ident and GUID
    static boolean fgSetPosFromFix( String id, String/*TODO PositionedID */guid )
    {
        Util.notyet();
        return true;
        /*
        FGPositioned* fix = NULL;
        if (guid != 0) {
            fix = FGPositioned::loadById<FGPositioned>(guid);
        } else {
            FGPositioned::TypeFilter fixFilter(FGPositioned::FIX);
            fix = FGPositioned::findFirstWithIdent(id, &fixFilter);
        }

        if (!fix) {
            SG_LOG( SG_GENERAL, SG_ALERT, "Failed to locate fix = " << id );
            return false;
        }

        fgApplyStartOffset(fix->geod(), FGProperties.FGProperties.fgGetDouble("/sim/presets/heading-deg"));
        return true;*/
    }

}
