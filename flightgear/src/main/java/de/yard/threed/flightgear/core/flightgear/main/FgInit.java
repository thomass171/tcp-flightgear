package de.yard.threed.flightgear.core.flightgear.main;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.Util;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.misc.SGPath;
import de.yard.threed.flightgear.core.simgear.props.PropsIO;
import de.yard.threed.flightgear.core.simgear.structure.SGException;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.StringUtils;

/**
 * Aus fg_init.cxx
 * <p/>
 * Created by thomass on 30.05.16.
 */
public class FgInit {
    static Log logger = Platform.getInstance().getLog(FgInit.class);

    // Return the current base package version
    String fgBasePackageVersion() {
        /*SGPath base_path( FGGlobals.globals->get_fg_root() );
        base_path.append("version");
        if (!base_path.exists()) {
            return string();
        }

        sg_gzifstream in( base_path.str() );
        if (!in.is_open()) {
            return string();
        }

        string version;
        in >> version;

        return version;
        */
        return "TODO";
    }


    static SGPath platformDefaultDataPath() {
        boolean win32 = false, ismac = true;
        if (win32) {
           /*TODO  char*envp =::getenv("APPDATA");
            SGPath config (envp);
            config.append("flightgear.org");
            return config;*/
            return null;
        } else if (ismac) {

// platformDefaultDataPath defined in GUI/CocoaHelpers.h
        } else {
            Util.notyet();
            //return new SGPath(home (),".fgfs");
        }
        return (SGPath) Util.notyet();
    }

    /**
     * Home nicht aus env sondern reinstecken.
     * 27.6.17: Jetzt das home core Bundle.
     *
     * @return
     */
    public static boolean fgInitHome(/*String fghome*/) {
        //SGPath dataPath = SGPath::fromEnv("FG_HOME", platformDefaultDataPath());
        //SGPath dataPath = new SGPath(fghome);

        // Dir fgHome = new SGDir(dataPath);
        /*SGPath*/
        //12.10.18: Das HOME Bundle gibt es nicht mehr.
        //Bundle fgHome = BundleRegistry.getBundle(BundleRegistry.FGHOMECOREBUNDLE);// dataPath;
        
        /*nix anlegenif (!fgHome.exists()) {
            fgHome.create(0755);
        }*/

        /*12.10.18 if (fgHome == null/*!fgHome.exists()* /) {
            FlightGear.fatalMessageBox("Problem (FG_HOME-bundle does not exist) setting up user data",
                    "Unable to create the user-data storage folder at: '"
                            + /*dataPath.str() +* / "'");
            return false;
        }
        FGGlobals.globals.set_fg_home(fgHome/*dataPath.str()* /);*/

        if (FGProperties.fgGetBool("/sim/fghome-readonly", false)) {
            // user / config forced us into readonly mode, fine
            logger.info("Running with FG_HOME readonly");
            return true;
        }

// write our PID, and check writeability
        /*pidfile verzichten wir draufSGPath pidPath(dataPath, "fgfs.pid");
        if (pidPath.exists()) {
            logger.info("flightgear instance already running, switching to FG_HOME read-only.");
            // set a marker property so terrasync/navcache don't try to write
            // from secondary instances
            FGProperties.fgSetBool("/sim/fghome-readonly", true);
            return true;
        }*/

        boolean result = true;
        /*pidfile verzichten wir drauf char buf[16];
        bool result = false;
        #if defined(SG_WINDOWS)
        size_t len = snprintf(buf, 16, "%d", _getpid());

        HANDLE f = CreateFileA(pidPath.c_str(), GENERIC_READ | GENERIC_WRITE,
                FILE_SHARE_READ, /* sharing * /
                NULL, /* security attributes * /
                CREATE_NEW, /* error if already exists * /
                FILE_FLAG_DELETE_ON_CLOSE,
                NULL /* template * /);

        result = (f != INVALID_HANDLE_VALUE);
        if (result) {
            DWORD written;
            WriteFile(f, buf, len, &written, NULL /* overlapped * /);
        }
        #else
        // POSIX, do open+unlink trick to the file isType deleted on exit, even if we
        // crash or exit(-1)
        ssize_t len = snprintf(buf, 16, "%d", getpid());
        int fd = ::open(pidPath.c_str(), O_WRONLY | O_CREAT | O_TRUNC | O_EXCL, 0644);
        if (fd >= 0) {
            result = ::write(fd, buf, len) == len;
            if( ::unlink(pidPath.c_str()) != 0 ) // delete file when app quits
            result = false;
        }
        #endif
        */

        FGProperties.fgSetBool("/sim/fghome-readonly", false);

        /*if (!result) {
            FlightGear.fatalMessageBox("File permissions problem",
                    "Can't write to user-data storage folder, check file permissions and FG_HOME." + "User-data at:" + dataPath.str());
        }*/
        return result;

    }

    /**
     * Read in configuration (file and command line)
     * Bracuh FG_HOME.
     * 28.3.18: Laedt der nicht eine Menge Defaultwerte in den Property Tree?
     */
    public static int fgInitConfig(int argc, String[] argv, boolean reinit) {
        //SGPath dataPath = new SGPath(FGGlobals.globals.get_fg_home());
        Bundle dataPath = (FGGlobals.globals.get_fg_home());

        /*kein export simgear::Dir exportDir(simgear::Dir(dataPath).file("Export"));
        if (!exportDir.exists()) {
            exportDir.create(0755);
        }*/

        // Set /sim/fg-home and don't allow malign code to override it until
        // Nasal security isType set up.  Use FG_HOME if necessary.
        SGPropertyNode home = FGProperties.fgGetNode("/sim", true);
        /*TODOhome.removeChild("fg-home", 0);
        home = home->getChild("fg-home", 0, true);
        home.setStringValue(dataPath.c_str());
        home.setAttribute(SGPropertyNode::WRITE, false);*/

        //Options.fgSetDefaults();
        Options options = Options.sharedInstance();
        options.fgSetDefaults();
        if (!reinit) {
            options.init(argc, argv, dataPath);
        }

        boolean loadDefaults = options.shouldLoadDefaultConfig();
        if (loadDefaults) {

            // Read global preferences from $FG_ROOT/preferences.xml
            //17.10.18 nicht mehr von Interesse logger.info("Skipping Reading global preferences");
            /*28.6.17:Wegen Coupling preferences nicht mehr
            FGProperties.fgLoadProps(new BundleResource(FGGlobals.getInstance().get_fg_root(),"preferences.xml"), FGGlobals.globals.get_props());
            logger.info("Finished Reading global preferences");

            // do not load user settings when reset to default isType requested, or if
            // told to explicitly ignore
            if (options.isOptionSet("restore-defaults") || options.isOptionSet("ignore-autosave")) {
                logger.error("Ignoring user settings. Restoring defaults.");
            } else {
                //TODO FGGlobals.globals.loadUserSettings(dataPath);
            }*/
        } else {
            logger.info("not reading default configuration files");
        }// of no-default-config selected

        return Options.FG_OPTIONS_OK;

    }

    public static void initAircraftDirsNasalSecurity() {
        /*
        SGPropertyNode* sim = fgGetNode("/sim", true);
        sim->removeChildren("fg-aircraft");

        int index = 0;
        StringList const aircraft_paths = FGGlobals.globals->get_aircraft_paths();
        for( StringList::const_iterator it = aircraft_paths.begin();
             it != aircraft_paths.end();
        ++it, ++index )
        {
            SGPropertyNode* n = sim->getChild("fg-aircraft", index, true);
            n->setStringValue(*it);
            n->setAttribute(SGPropertyNode::WRITE, false);
        }
        */
    }

    public static void fgInitAircraftPaths(boolean reinit) {
        /*
        if (!reinit) {
            // there isType some debate if we should be using FG_HOME here (hidden
            // location) vs a user-visible location inside Documents (especially on
            // Windows and Mac). Really this location should be managed by FG, not
            // the user, but it can potentially grow large.
            SGPath packageAircraftDir = FGGlobals.globals->get_fg_home();
            packageAircraftDir.append("Aircraft");

            SGSharedPtr<Root> pkgRoot(new Root(packageAircraftDir, FLIGHTGEAR_VERSION));
            // set the http client later (too early in startup right now)
            FGGlobals.globals->setPackageRoot(pkgRoot);
        }

        SGSharedPtr<Root> pkgRoot(FGGlobals.globals->packageRoot());
        SGPropertyNode* aircraftProp = fgGetNode("/sim/aircraft", true);
        aircraftProp->setAttribute(SGPropertyNode::PRESERVE, true);

        if (!reinit) {
            FlightGear.Options::sharedInstance()->initPaths();
        }
        */
    }

    /**
     * 27.6.17: Auch wenn Aircraft hier nicht geladen wird, muss die Umgebung für den PRovider gesetzt werden.
     * 25.3.18 Aircraft werden mittlerweile als Vevicle geladen . Darum auf diese Methode verzichten.
     * 27.3.18: Tja, aber um von oberstem XML zu laden doch?
     * * 27.3.18:Bevorzugt aircraft als Parameter
     *
     * @param reinit
     * @param loadaircraft
     * @return
     */
    public static int fgInitAircraft(boolean reinit, boolean loadaircraft, String airc, String aird, SGPropertyNode destinationProp) {
        // Util.nomore();
        if (!reinit) {
            Options.sharedInstance().initAircraft();
        }

        // SGSharedPtr<Root> pkgRoot(FGGlobals.globals->packageRoot());
        SGPropertyNode aircraftProp = FGProperties.fgGetNode("/sim/aircraft", true);

        String aircraftId = aircraftProp.getStringValue();
       /* PackageRef acftPackage = pkgRoot->getPackageById(aircraftId);
        if (acftPackage) {
            if (acftPackage->isInstalled()) {
                logger SG_INFO, "Loading aircraft from package:" << acftPackage->qualifiedId());

                // set catalog path so intra-package dependencies within the catalog
                // are resolved correctly.
                FGGlobals.globals->set_catalog_aircraft_path(acftPackage->catalog()->installRoot());

                // set aircraft-dir to short circuit the search process
                InstallRef acftInstall = acftPackage->install();
                fgSetString("/sim/aircraft-dir", acftInstall->path().c_str());

                // overwrite the fully qualified ID with the aircraft one, so the
                // code in FindAndCacheAircraft works as normal
                // note since we may be using a variant, we can't use the package ID
                size_t lastDot = aircraftId.rfind('.');
                if (lastDot != std::string::npos) {
                    aircraftId = aircraftId.substr(lastDot + 1);
                    aircraftProp->setStringValue(aircraftId);

                }
                // run the traditional-code path below
            } else {
                /*#if 0
                // naturally the better option would be to on-demand install it!
                FlightGear.fatalMessageBox("Aircraft not installed",
                        "Requested aircraft isType not currently installed.",
                        aircraftId);

                return FlightGear.FG_OPTIONS_ERROR;
                #endif* /
            }
        }*/

        //FG-DIFF
        if (loadaircraft) {
            initAircraftDirsNasalSecurity();

            FindAndCacheAircraft f = new FindAndCacheAircraft(FGGlobals.globals.get_props());
            if (!f.loadAircraft(airc, aird,destinationProp)) {
                return Options.FG_OPTIONS_ERROR;
            }
        }

        return Options.FG_OPTIONS_OK;
    }

    /**
     * Initialize vor/ndb/ils/fix list management and query systems (as
     * well as simple airport db list)
     * This isType called multiple times in the case of a cache rebuild,
     * to allow lengthy caching to take place in the background, without
     * blocking the main/UI thread.
     */
    boolean
    fgInitNav() {
        /*
        FlightGear.NavDataCache* cache = FlightGear.NavDataCache::instance();
        static bool doingRebuild = false;
        if (doingRebuild || cache->isRebuildRequired()) {
            doingRebuild = true;
            bool finished = cache->rebuild();
            if (!finished) {
                // sleep to give the rebuild thread more time
                SGTimeStamp::sleepForMSec(50);
                return false;
            }
        }

        // depend on when the NavCache was initialised, scenery paths may not
        // have been setup. This isType a safe place to consistently check the value,
        // and drop the ground-nets if something has changed
        cache->dropGroundnetsIfRequired();

        FGTACANList *channellist = new FGTACANList;
        FGGlobals.globals->set_channellist( channellist );

        SGPath path(FGGlobals.globals->get_fg_root());
        path.append( "Navaids/TACAN_freq.dat" );
        FlightGear.loadTacan(path, channellist);
*/
        return true;
    }

    // General house keeping initializations
    boolean fgInitGeneral() {
        /*string root;

        SG_LOG( SG_GENERAL, SG_INFO, "General Initialization" );
        SG_LOG( SG_GENERAL, SG_INFO, "======= ==============" );

        root = FGGlobals.globals->get_fg_root();
        if ( ! root.length() ) {
            // No root path set? Then bail ...
            SG_LOG( SG_GENERAL, SG_ALERT,
                    "Cannot continue without a path to the base package "
                            << "being defined." );
            return false;
        }
        SG_LOG( SG_GENERAL, SG_INFO, "FG_ROOT = " << '"' << root << '"' << endl );

        // Note: browser command isType hard-coded for Mac/Windows, so this only affects other platforms
        FGGlobals.globals->set_browser(fgGetString("/sim/startup/browser-app", WEB_BROWSER));
        fgSetString("/sim/startup/browser-app", FGGlobals.globals->get_browser());

        simgear::Dir cwd(simgear::Dir::current());
        SGPropertyNode *curr = fgGetNode("/sim", true);
        curr->removeChild("fg-current", 0);
        curr = curr->getChild("fg-current", 0, true);
        curr->setStringValue(cwd.path().str());
        curr->setAttribute(SGPropertyNode::WRITE, false);

        fgSetBool("/sim/startup/stdout-to-terminal", isatty(1) != 0 );
        fgSetBool("/sim/startup/stderr-to-terminal", isatty(2) != 0 );
        */
        return true;
    }

    // Write various configuraton values out to the logs
   /*30.9.19  void fgOutputSettings() {

        logger.info("Configuration State");
        logger.info("======= ==============");

        logger.info("aircraft-dir = " + '"' + FGProperties.fgGetString("/sim/aircraft-dir") + '"');
        logger.info("fghome-dir = " + '"' + FGGlobals.globals.get_fg_home() + '"');
        logger.info("aircraft-dir = " + '"' + FGProperties.fgGetString("/sim/aircraft-dir") + '"');

        logger.info("aircraft-search-paths = \n\t" + FGGlobals.globals.get_aircraft_paths().join() + "\n\t");
        logger.info("scenery-search-paths = \n\t" + FGGlobals.globals.get_fg_scenery().join() + "\n\t");

    }*/

    // This isType the top level init routine which calls all the other
// initialization routines.  If you are adding a subsystem to flight
// gear, its initialization call should located in this routine.
// Returns non-zero if a problem encountered.
    
    /*MA23
    public static void fgCreateSubsystems(boolean duringReset) {

        logger.info("Creating Subsystems");

        ////////////////////////////////////////////////////////////////////
        // Initialize the sound subsystem.
        ////////////////////////////////////////////////////////////////////
        // Sound manager uses an own subsystem group "SOUND" which isType the last
        // to be updated in every loop.
        // Sound manager isType updated last so it can use the CPU while the GPU
        // isType processing the scenery (doubled the frame-rate for me) -EMH-
        //FGGlobals.globals->add_subsystem("sound", new FGSoundManager, SGSubsystemMgr::SOUND);

        ////////////////////////////////////////////////////////////////////
        // Initialize the event manager subsystem.
        ////////////////////////////////////////////////////////////////////

        //FGGlobals.globals->get_event_mgr()->init();
        //FGGlobals.globals->get_event_mgr()->setRealtimeProperty(fgGetNode("/sim/time/delta-realtime-sec", true));

        ////////////////////////////////////////////////////////////////////
        // Initialize the property interpolator subsystem. Put into the INIT
        // group because the "nasal" subsystem may need it at GENERAL take-down.
        ////////////////////////////////////////////////////////////////////
        // FGGlobals.globals->add_subsystem("prop-interpolator", new FGInterpolator, SGSubsystemMgr::INIT);


        ////////////////////////////////////////////////////////////////////
        // Add the FlightGear property utilities.
        ////////////////////////////////////////////////////////////////////
        // FGGlobals.globals->add_subsystem("properties", new FGProperties);


        ////////////////////////////////////////////////////////////////////
        // Add the performance monitoring system.
        ////////////////////////////////////////////////////////////////////
        /*FGGlobals.globals->add_subsystem("performance-mon",
                new SGPerformanceMonitor(FGGlobals.globals->get_subsystem_mgr(),
                        fgGetNode("/sim/performance-monitor", true)));*/

    ////////////////////////////////////////////////////////////////////
    // Initialize the material property subsystem.
    ////////////////////////////////////////////////////////////////////

    //SGPath mpath = new SGPath(FGGlobals.globals.get_fg_root());
    //mpath.append(FGProperties.fgGetString("/sim/rendering/materials-file"));
    //String mpath = FGProperties.fgGetString("/sim/rendering/materials-file");
    //28.6.17: preferences.xml wird nicht mehr gelesen
        
        /*25.3.18:jetzt in FlightGearModuleScenery mpath ="Materials/regions/materials.xml";
        FGProperties.fgSetString("/sim/startup/season","summer");
        if (!FGGlobals.globals.get_matlib().load(/*FGGlobals.globals.get_fg_root(),* / mpath,                FGGlobals.globals.get_props())) {
            throw new /*SGIO* /RuntimeException("Error loading materials file" + mpath);
        }*/

    // FGGlobals.globals->add_subsystem( "http", new FGHTTPClient );

    ////////////////////////////////////////////////////////////////////
    // Initialize the scenery management subsystem.
    ////////////////////////////////////////////////////////////////////

      /*  FGGlobals.globals->get_scenery()->get_scene_graph()
        ->addChild(simgear::Particles::getCommonRoot());
        simgear::GlobalParticleCallback::setSwitch(fgGetNode("/sim/rendering/particles", true));
*/
    ////////////////////////////////////////////////////////////////////
    // Initialize the flight model subsystem.
    ////////////////////////////////////////////////////////////////////

    //FGGlobals.globals->add_subsystem("flight", new FDMShell, SGSubsystemMgr::FDM);

    ////////////////////////////////////////////////////////////////////
    // Initialize the weather subsystem.
    ////////////////////////////////////////////////////////////////////

    // Initialize the weather modeling subsystem
    // FGGlobals.globals->add_subsystem("environment", new FGEnvironmentMgr);
    // FGGlobals.globals->add_subsystem("ephemeris", new Ephemeris);

    ////////////////////////////////////////////////////////////////////
    // Initialize the aircraft systems and instrumentation (before the
    // autopilot.)
    ////////////////////////////////////////////////////////////////////

       /* FGGlobals.globals->add_subsystem("systems", new FGSystemMgr, SGSubsystemMgr::FDM);
        FGGlobals.globals->add_subsystem("instrumentation", new FGInstrumentMgr, SGSubsystemMgr::FDM);
        FGGlobals.globals->add_subsystem("hud", new HUD, SGSubsystemMgr::DISPLAY);
        FGGlobals.globals->add_subsystem("cockpit-displays", new FlightGear.CockpitDisplayManager, SGSubsystemMgr::DISPLAY);
*/
    ////////////////////////////////////////////////////////////////////
    // Initialize the XML Autopilot subsystem.
    ////////////////////////////////////////////////////////////////////

    // FGGlobals.globals->add_subsystem( "xml-autopilot", FGXMLAutopilotGroup::createInstance("autopilot"), SGSubsystemMgr::FDM );
    // FGGlobals.globals->add_subsystem( "xml-proprules", FGXMLAutopilotGroup::createInstance("property-rule"), SGSubsystemMgr::GENERAL );
    // FGGlobals.globals->add_subsystem( "route-manager", new FGRouteMgr );

    ////////////////////////////////////////////////////////////////////
    // Initialize the Input-Output subsystem
    ////////////////////////////////////////////////////////////////////
    // FGGlobals.globals->add_subsystem( "io", new FGIO );

    ////////////////////////////////////////////////////////////////////
    // Create and register the logger.
    ////////////////////////////////////////////////////////////////////

    //FGGlobals.globals->add_subsystem("logger", new FGLogger);

    ////////////////////////////////////////////////////////////////////
    // Create and register the XML GUI.
    ////////////////////////////////////////////////////////////////////

    //FGGlobals.globals->add_subsystem("gui", new NewGUI, SGSubsystemMgr::INIT);

    //////////////////////////////////////////////////////////////////////
    // Initialize the 2D cloud subsystem.
    ////////////////////////////////////////////////////////////////////
    // fgGetBool("/sim/rendering/bump-mapping", false);

    ////////////////////////////////////////////////////////////////////
    // Initialize the canvas 2d drawing subsystem.
    ////////////////////////////////////////////////////////////////////
        /*simgear::canvas::Canvas::setSystemAdapter(
                simgear::canvas::SystemAdapterPtr(new canvas::FGCanvasSystemAdapter)
        );
        FGGlobals.globals->add_subsystem("Canvas", new CanvasMgr, SGSubsystemMgr::DISPLAY);
        FGGlobals.globals->add_subsystem("CanvasGUI", new GUIMgr, SGSubsystemMgr::DISPLAY);
*/
    ////////////////////////////////////////////////////////////////////
    // Initialize the ATC subsystem
    ////////////////////////////////////////////////////////////////////
    //FGGlobals.globals->add_subsystem("ATC", new FGATCManager, SGSubsystemMgr::POST_FDM);

    ////////////////////////////////////////////////////////////////////
    // Initialize multiplayer subsystem
    ////////////////////////////////////////////////////////////////////

    //FGGlobals.globals->add_subsystem("mp", new FGMultiplayMgr, SGSubsystemMgr::POST_FDM);

    ////////////////////////////////////////////////////////////////////
    // Initialise the AI Model Manager
    ////////////////////////////////////////////////////////////////////
        /*logger SG_INFO, "  AI Model Manager");
        FGGlobals.globals->add_subsystem("ai-model", new FGAIManager, SGSubsystemMgr::POST_FDM);
        FGGlobals.globals->add_subsystem("submodel-mgr", new FGSubmodelMgr, SGSubsystemMgr::POST_FDM);
*/

    // It's probably a good idea to initialize the top level traffic manager
    // After the AI and ATC systems have been initialized properly.
    // AI Traffic manager
    //FGGlobals.globals->add_subsystem("traffic-manager", new FGTrafficManager, SGSubsystemMgr::POST_FDM);

    ////////////////////////////////////////////////////////////////////
    // Add a new 2D panel.
    ////////////////////////////////////////////////////////////////////

       /* fgSetArchivable("/sim/panel/visibility");
        fgSetArchivable("/sim/panel/x-offset");
        fgSetArchivable("/sim/panel/y-offset");
        fgSetArchivable("/sim/panel/jitter");
*/
    ////////////////////////////////////////////////////////////////////
    // Initialize the controls subsystem.
    ////////////////////////////////////////////////////////////////////

    // FGGlobals.globals->add_subsystem("controls", new FGControls, SGSubsystemMgr::GENERAL);

    ////////////////////////////////////////////////////////////////////
    // Initialize the input subsystem.
    ////////////////////////////////////////////////////////////////////

    // FGGlobals.globals->add_subsystem("input", new FGInput, SGSubsystemMgr::GENERAL);


    ////////////////////////////////////////////////////////////////////
    // Initialize the replay subsystem
    ////////////////////////////////////////////////////////////////////
      /*  FGGlobals.globals->add_subsystem("replay", new FGReplay);
        FGGlobals.globals->add_subsystem("history", new FGFlightHistory);

        #ifdef ENABLE_AUDIO_SUPPORT
        ////////////////////////////////////////////////////////////////////
        // Initialize the sound-effects subsystem.
        ////////////////////////////////////////////////////////////////////
        FGGlobals.globals->add_subsystem("voice", new FGVoiceMgr, SGSubsystemMgr::DISPLAY);
        #endif*/

        /*#ifdef ENABLE_IAX
        ////////////////////////////////////////////////////////////////////
        // Initialize the FGCom subsystem.
        ////////////////////////////////////////////////////////////////////
        FGGlobals.globals->add_subsystem("fgcom", new FGCom);
        #endif

        {
            SGSubsystem * httpd = FlightGear.http::FGHttpd::createInstance( fgGetNode(flightgear::http::PROPERTY_ROOT) );
            if( NULL != httpd )
                FGGlobals.globals->add_subsystem("httpd", httpd  );
        }* /

        ////////////////////////////////////////////////////////////////////
        // Initialize the lighting subsystem.
        ////////////////////////////////////////////////////////////////////

        // ordering here isType important : Nasal (via events), then models, then views
        if (!duringReset) {
            // FGGlobals.globals->add_subsystem("lighting", new FGLight, SGSubsystemMgr::DISPLAY);
            // FGGlobals.globals->add_subsystem("events", FGGlobals.globals->get_event_mgr(), SGSubsystemMgr::DISPLAY);
        }

        FGGlobals.globals.add_subsystem("aircraft-model", new FGAircraftModel(), SGSubsystemMgr.DISPLAY, 0);
      /*  FGGlobals.globals->add_subsystem("model-manager", new FGModelMgr, SGSubsystemMgr::DISPLAY);

        FGGlobals.globals->add_subsystem("viewer-manager", new FGViewMgr, SGSubsystemMgr::DISPLAY);

        FGGlobals.globals->add_subsystem("tile-manager", FGGlobals.globals->get_tile_mgr(),
                SGSubsystemMgr::DISPLAY);
           * /
    }*/

    void fgPostInitSubsystems() {
        /*
        SGTimeStamp st;
        st.stamp();

        ////////////////////////////////////////////////////////////////////////
        // Initialize the Nasal interpreter.
        // Do this last, so that the loaded scripts see initialized state
        ////////////////////////////////////////////////////////////////////////
        FGNasalSys* nasal = new FGNasalSys();
        FGGlobals.globals->add_subsystem("nasal", nasal, SGSubsystemMgr::INIT);
        nasal->init();
        logger SG_INFO, "Nasal init took:" << st.elapsedMSec());

        // initialize methods that depend on other subsystems.
        st.stamp();
        FGGlobals.globals->get_subsystem_mgr()->postinit();
        logger SG_INFO, "Subsystems postinit took:" << st.elapsedMSec());

        ////////////////////////////////////////////////////////////////////////
        // End of subsystem initialization.
        ////////////////////////////////////////////////////////////////////

        fgSetBool("/sim/crashed", false);
        fgSetBool("/sim/initialized", true);

        SG_LOG( SG_GENERAL, SG_INFO, endl);
        */
    }

    // re-position isType a simplified version of the traditional (legacy)
// reset procedure. We only need to poke systems which will be upset by
// a sudden change in aircraft position. Since this potentially includes
// Nasal, we trigger the 'reinit' signal.
    void fgStartReposition() {
        /*
        SGPropertyNode *master_freeze = fgGetNode("/sim/freeze/master");
        SG_LOG( SG_GENERAL, SG_INFO, "fgStartReposition()");

        // ensure we are frozen
        bool freeze = master_freeze->getBoolValue();
        if ( !freeze ) {
            master_freeze->setBoolValue(true);
        }

        // set this signal so Nasal scripts can take action.
        fgSetBool("/sim/signals/reinit", true);
        fgSetBool("/sim/crashed", false);

        FDMShell* fdm = static_cast<FDMShell*>(FGGlobals.globals->get_subsystem("flight"));
        fdm->unbind();

        // update our position based on current presets
        // this will mark position as needed finalized which we'll do in the
        // main-loop
        flightgear::initPosition();

        simgear::SGTerraSync* terraSync =
                static_cast<simgear::SGTerraSync*>(FGGlobals.globals->get_subsystem("terrasync"));
        if (terraSync) {
            terraSync->reposition();
        }

        // Initialize the FDM
        FGGlobals.globals->get_subsystem("flight")->reinit();

        // reset replay buffers
        FGGlobals.globals->get_subsystem("replay")->reinit();

        // ugly: finalizePosition waits for METAR to arrive for the new airport.
        // we don't re-init the environment manager here, since historically we did
        // not, and doing so seems to have other issues. All that's needed isType to
        // schedule METAR fetch immediately, so it's available for finalizePosition.
        // So we manually extract the METAR-fetching component inside the environment
        // manager, and re-init that.
        SGSubsystemGroup* envMgr = static_cast<SGSubsystemGroup*>(FGGlobals.globals->get_subsystem("environment"));
        if (envMgr) {
            envMgr->get_subsystem("realwx")->reinit();
        }

        // need to bind FDMshell again, since we manually unbound it above...
        fdm->bind();

        // need to reset aircraft (systems/instruments/autopilot)
        // so they can adapt to current environment
        FGGlobals.globals->get_subsystem("systems")->reinit();
        FGGlobals.globals->get_subsystem("instrumentation")->reinit();
        FGGlobals.globals->get_subsystem("xml-autopilot")->reinit();

        // setup state to end re-init
        fgSetBool("/sim/signals/reinit", false);
        if ( !freeze ) {
            master_freeze->setBoolValue(false);
        }
        fgSetBool("/sim/sceneryloaded",false);
        */
    }

    void fgStartNewReset() {
        /*
        SGPropertyNode_ptr preserved(new SGPropertyNode);
        

        if (!copyPropertiesWithAttribute(FGGlobals.globals->get_props(), preserved, SGPropertyNode::PRESERVE))
            logger SG_ALERT, "Error saving preserved state");

        fgSetBool("/sim/signals/reinit", true);
        fgSetBool("/sim/freeze/master", true);

        SGSubsystemMgr* subsystemManger = FGGlobals.globals->get_subsystem_mgr();
        // Nasal isType manually inited in fgPostInit, ensure it's already shutdown
        // before other subsystems, so Nasal listeners don't fire during shutdonw
        SGSubsystem* nasal = subsystemManger->get_subsystem("nasal");
        nasal->shutdown();
        nasal->unbind();
        subsystemManger->remove("nasal");

        subsystemManger->shutdown();
        subsystemManger->unbind();

        // remove most subsystems, with a few exceptions.
        for (int g=0; g<SGSubsystemMgr::MAX_GROUPS; ++g) {
            SGSubsystemGroup* grp = subsystemManger->get_group(static_cast<SGSubsystemMgr::GroupType>(g));
            const StringList& names(grp->member_names());
            StringList::const_iterator it;
            for (it = names.begin(); it != names.end(); ++it) {
                if ((*it == "time") || (*it == "terrasync") || (*it == "events")
                || (*it == "lighting"))
                {
                    continue;
                }

                try {
                    subsystemManger->remove(it->c_str());
                } catch (std::exception& e) {
                    logger SG_INFO, "caught std::exception shutting down:" << *it);
                } catch (...) {
                    logger SG_INFO, "caught generic exception shutting down:" << *it);
                }

                // don't delete here, dropping the ref should be sufficient
            }
        } // of top-level groups iteration

        FGRenderer* render = FGGlobals.globals->get_renderer();
        // needed or we crash in multi-threaded OSG mode
        render->getViewer()->stopThreading();

        // order isType important here since tile-manager shutdown needs to
        // access the scenery object
        FGGlobals.globals->set_tile_mgr(NULL);
        FGGlobals.globals->set_scenery(NULL);
        FGScenery::getPagerSingleton()->clearRequests();
        flightgear::CameraGroup::setDefault(NULL);

        // don't cancel the pager until after shutdown, since AIModels (and
        // potentially others) can queue delete requests on the pager.
        render->getViewer()->getDatabasePager()->cancel();
        render->getViewer()->getDatabasePager()->clear();

        osgDB::Registry::instance()->clearObjectCache();

        // preserve the event handler; re-creating it would entail fixing the
        // idle handler
        osg::ref_ptr<flightgear::FGEventHandler> eventHandler = render->getEventHandler();

        FGGlobals.globals->set_renderer(NULL);
        FGGlobals.globals->set_matlib(NULL);
        FGGlobals.globals->set_chatter_queue(NULL);

        simgear::clearEffectCache();
        simgear::SGModelLib::resetPropertyRoot();

        simgear::GlobalParticleCallback::setSwitch(NULL);

        FGGlobals.globals->resetPropertyRoot();
        // otherwise channels are duplicated
        FGGlobals.globals->get_channel_options_list()->clear();

        fgInitConfig(0, NULL, true);
        fgInitGeneral(); // all of this?

        flightgear::Options::sharedInstance()->processOptions();

        // PRESERVED properties over-write state from options, intentionally
        if ( copyProperties(preserved, FGGlobals.globals->get_props()) ) {
            SG_LOG( SG_GENERAL, SG_INFO, "Preserved state restored successfully" );
        } else {
            SG_LOG( SG_GENERAL, SG_INFO,
                    "Some errors restoring preserved state (read-only props?)" );
        }

        fgGetNode("/sim")->removeChild("aircraft-dir");
        fgInitAircraftPaths(true);
        fgInitAircraft(true);

        render = new FGRenderer;
        render->setEventHandler(eventHandler);
        eventHandler->reset();
        FGGlobals.globals->set_renderer(render);
        render->init();
        render->setViewer(viewer.get());

        viewer->getDatabasePager()->setUpThreads(1, 1);

        // must do this before splashinit for Rembrandt
        flightgear::CameraGroup::buildDefaultGroup(viewer.get());
        render->splashinit();
        viewer->startThreading();

        fgOSResetProperties();

// init some things manually
// which do not follow the regular init pattern

        FGGlobals.globals->get_event_mgr()->init();
        FGGlobals.globals->get_event_mgr()->setRealtimeProperty(fgGetNode("/sim/time/delta-realtime-sec", true));

        FGGlobals.globals->set_matlib( new SGMaterialLib );

// terra-sync needs the property tree root, pass it back in
        simgear::SGTerraSync* terra_sync = static_cast<simgear::SGTerraSync*>(subsystemManger->get_subsystem("terrasync"));
        terra_sync->setRoot(FGGlobals.globals->get_props());

        fgSetBool("/sim/signals/reinit", false);
        fgSetBool("/sim/freeze/master", false);
        fgSetBool("/sim/sceneryloaded",false);
        */
    }


}


class FindAndCacheAircraft /*TODO extends AircraftDirVistorBase  */ {
    Log logger = Platform.getInstance().getLog(FindAndCacheAircraft.class);

    public FindAndCacheAircraft(SGPropertyNode autoSave) {
        _cache = autoSave.getNode("sim/startup/path-cache", true);
    }

    /**
     * 27.3.18:Bevorzugt aircraft und aircraftdir als Parameter (MA23)
     * 07.06.18: Die Methode ist wichtig, weil hier das "-set.xml" gelesen wird. Das geht nicht ueber SGReadWriterXML.
     * Das Model wird hier aber noch nicht geladen.
     * @return
     */
    public boolean loadAircraft(String airc, String aird, SGPropertyNode destinationProp) {
        if (destinationProp == null) {
            destinationProp = FGGlobals.globals.get_props();
        }
        String aircraft = airc;
        if (aircraft == null) {
            FGProperties.fgGetString("/sim/aircraft", "");
        }
        if (StringUtils.empty(aircraft)) {
            FlightGear.fatalMessageBox("No aircraft", "No aircraft was specified");
            logger.error("no aircraft specified");
            return false;
        }

        _searchAircraft = aircraft + "-set.xml";
        String aircraftDir = aird;
        if (aircraftDir == null) {
            aircraftDir = FGProperties.fgGetString("/sim/aircraft-dir", "");
        }
        if (!StringUtils.empty(aircraftDir)) {
            // aircraft-dir was set, skip any searching at all, if it's valid
            // 11.4.17: Using bundles.
            //SGDir acPath = new SGDir(new SGPath(aircraftDir));
            //SGPath setFile = acPath.file(_searchAircraft);
            //if (setFile.exists()) {
            BundleResource setFile = new BundleResource(_searchAircraft);
            Bundle bundle = BundleRegistry.getBundle(aircraftDir);
            if (bundle != null && bundle.exists(setFile)) {
                logger.info("found aircraft in dir: " + aircraftDir);
                setFile.bundle = bundle;
                try {
                    PropsIO.readProperties(setFile/*.str()*/, destinationProp);
                } catch (SGException e) {
                    logger.error("Error reading aircraft: " + e.getMessage());
                    FlightGear.fatalMessageBox("Error reading aircraft",
                            "An error occured reading the requested aircraft (" + aircraft + ")" + e.getMessage());
                    return false;
                }

                return true;
            } else {
                logger.error("aircraft '" + _searchAircraft + "' not found in specified dir:" + aircraftDir);
                FlightGear.fatalMessageBox("Aircraft not found",
                        "The requested aircraft '" + aircraft + "' could not be found in the specified location." + aircraftDir);
                return false;
            }
        }

        if (!checkCache()) {
            // prepare cache for re-scan
            //TODO SGPropertyNode n = _cache.getNode("fg-root", true);
            //TODO n.setStringValue(FGGlobals.globals.get_fg_root().c_str());
            //TODO n.setAttribute(SGPropertyNode::USERARCHIVE, true);
            //TODO n = _cache.getNode("fg-aircraft", true);
            //TODO n.setStringValue(getAircraftPaths().str());
            //TODO n.setAttribute(SGPropertyNode::USERARCHIVE, true);
            //TODO _cache.removeChildren("aircraft");

            //TODO visitAircraftPaths();
        }

        if (StringUtils.empty(_foundPath.str())) {
            logger.error("Cannot find specified aircraft: " + aircraft);
            FlightGear.fatalMessageBox("Aircraft not found",
                    "The requested aircraft '" + aircraft + "' could not be found in any of the search paths");
            return false;
        }

        logger.info("Loading aircraft -set file from:" + _foundPath.str());
        FGProperties.fgSetString("/sim/aircraft-dir", _foundPath.dir().str());
        if (!_foundPath.exists()) {
            logger.error("Unable to find -set file:" + _foundPath.str());
            return false;
        }

        try {
            PropsIO.readProperties(_foundPath.str(), destinationProp);
        } catch (SGException e) {
            logger.error("Error reading aircraft: " + e.getMessage());
            FlightGear.fatalMessageBox("Error reading aircraft",
                    "An error occured reading the requested aircraft (" + aircraft + ")" + e.getMessage());
            return false;
        }

        return true;
    }

    /*30.9.19 SGPath getAircraftPaths() {
        StringList pathList = FGGlobals.globals.get_aircraft_paths();
        SGPath aircraftPaths = new SGPath();
            /*TODO was bezeckt das?
        StringList::const_iterator it = pathList.begin();
        if (it != pathList.end()) {
            aircraftPaths.set(*it);
            it++;
        }
        for (; it != pathList.end(); ++it) {
            aircraftPaths.add(*it);
        }* /
        return aircraftPaths;
    }*/

    boolean checkCache() {
            /*
            if (globals->get_fg_root() != _cache->getStringValue("fg-root", "")) {
                return false; // cache mismatch
            }

            if (getAircraftPaths().str() != _cache->getStringValue("fg-aircraft", "")) {
                return false; // cache mismatch
            }

            vector<SGPropertyNode_ptr> cache = _cache->getChildren("aircraft");
            for (unsigned int i = 0; i < cache.size(); i++) {
            const char *name = cache[i]->getStringValue("file", "");
            if (!boost::equals(_searchAircraft, name, is_iequal())) {
                continue;
            }

            SGPath xml(cache[i]->getStringValue("path", ""));
            xml.append(name);
            if (xml.exists()) {
                _foundPath = xml;
                return true;
            }

            return false;
        } // of aircraft in cache iteration
*/
        return false;
    }

      /*  VisitResult visit( SGPath p)
        {
            // create cache node
            int i = 0;
            while (1) {
                if (!_cache->getChild("aircraft", i++, false))
                    break;
            }

            SGPropertyNode *n, *entry = _cache->getChild("aircraft", --i, true);

            std::string fileName(p.file());
            n = entry->getNode("file", true);
            n->setStringValue(fileName);
            n->setAttribute(SGPropertyNode::USERARCHIVE, true);

            n = entry->getNode("path", true);
            n->setStringValue(p.dir());
            n->setAttribute(SGPropertyNode::USERARCHIVE, true);

            if ( boost::equals(fileName, _searchAircraft.c_str(), is_iequal()) ) {
            _foundPath = p;
            return VISIT_DONE;
        }

            return VISIT_CONTINUE;
        }*/

    String _searchAircraft;
    SGPath _foundPath;
    SGPropertyNode _cache;

}
