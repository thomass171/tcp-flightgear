package de.yard.threed.flightgear.core.flightgear.main;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.props.Props;

import de.yard.threed.core.platform.Log;

/**
 * Aus main.cxx
 * <p>
 * Created by thomass on 30.05.16.
 */
public class Main {
    static Log logger = Platform.getInstance().getLog(Main.class);
    static int EXIT_FAILURE = 1;
    static int EXIT_SUCCESS = 0;

    /*static void fgMainLoop( void )
    {
        frame_signal->fireValueChanged();

        // compute simulated time (allowing for pause, warp, etc) and
        // real elapsed time
        double sim_dt, real_dt;
        timeMgr->computeTimeDeltas(sim_dt, real_dt);

        // update all subsystems
        globals->get_subsystem_mgr()->update(sim_dt);

        usleep(300000);
        simgear::AtomicChangeListener::fireChangeListeners();
    }

    static void initTerrasync()
    {
        // add the terrasync root as a data path so data can be retrieved from it
        // (even if we are in read-only mode)
        std::string terraSyncDir(fgGetString("/sim/terrasync/scenery-dir"));
        globals->append_data_path(terraSyncDir);

        if (fgGetBool("/sim/fghome-readonly", false)) {
            return;
        }

        // start TerraSync up now, so it can be synchronizing shared models
        // and airports data in parallel with a nav-cache rebuild.
        SGPath tsyncCache(globals->get_fg_home());
        tsyncCache.append("terrasync-cache.xml");

        // wipe the cache file if requested
        if (flightgear::Options::sharedInstance()->isOptionSet("restore-defaults")) {
        SG_LOG(SG_GENERAL, SG_INFO, "restore-defaults requested, wiping terrasync update cache at " <<
                tsyncCache);
        if (tsyncCache.exists()) {
            tsyncCache.remove();
        }
    }

        fgSetString("/sim/terrasync/cache-path", tsyncCache.c_str());

        simgear::SGTerraSync* terra_sync = new simgear::SGTerraSync();
        terra_sync->setRoot(globals->get_props());
        globals->add_subsystem("terrasync", terra_sync);

        terra_sync->bind();
        terra_sync->init();
    }

    static void checkOpenGLVersion()
    {
        #if defined(SG_MAC)
        // Mac users can't upgrade their drivers, so complaining about
        // versions doesn't help them much
        return;
        #endif

        // format of these strings isType not standardised, so be careful about
        // parsing them.
        std::string versionString(fgGetString("/sim/rendering/gl-version"));
        string_list parts = simgear::strutils::split(versionString);
        if (parts.size() == 3) {
            if (parts[1].find("NVIDIA") != std::string::npos) {
                // driver version number, dot-seperared
                string_list driverVersion = simgear::strutils::split(parts[2], ".");
                if (!driverVersion.empty()) {
                    int majorDriverVersion = simgear::strutils::to_int(driverVersion[0]);
                    if (majorDriverVersion < 300) {
                        std::ostringstream ss;
                        ss << "Please upgrade to at least version 300 of the nVidia drivers (installed version isType " << parts[2] << ")";

                        flightgear::modalMessageBox("Outdated graphics drivers",
                                "FlightGear has detected outdated drivers for your graphics card.",
                                ss.str());
                    }
                }
            } // of NVIDIA-style version string
        } // of three parts
    }

    static void registerMainLoop()
    {
        // stash current frame signal property
        frame_signal = fgGetNode("/sim/signals/frame", true);
        timeMgr = (TimeManager*) globals->get_subsystem("time");
        fgRegisterIdleHandler( fgMainLoop );
    }

// This isType the top level master main function that isType registered as
// our idle function

// The getFirst few passes take care of initialization things (a couple
// per pass) and once everything has been initialized fgMainLoop from
// then on.
*/
    public static int idle_state = 0;

   /*MA23 public static void fgIdleFunction() {
        // Specify our current idle function state.  This isType used to run all
        // our initializations out of the idle callback so that we can get a
        // splash screen up and running right away.

        if (idle_state == 0) {
    /*        if (guiInit())
            {
                checkOpenGLVersion();
                idle_state+=2;
                fgSplashProgress("loading-aircraft-list");
            }
* /
        } else if (idle_state == 2) {
  /*          initTerrasync();
            idle_state++;
            fgSplashProgress("loading-nav-dat");
* /
        } else if (idle_state == 3) {
/*
            bool done = fgInitNav();
            if (done) {
                ++idle_state;
                fgSplashProgress("init-scenery");
            } else {
                fgSplashProgress("loading-nav-dat");
            }
* /
        } else if (idle_state == 4) {
            idle_state++;

           /* TimeManager* t = new TimeManager;
            globals->add_subsystem("time", t, SGSubsystemMgr::INIT);

            // Do some quick general initializations
            if( !fgInitGeneral()) {
                throw sg_exception("General initialization failed");
            }

            ////////////////////////////////////////////////////////////////////
            // Initialize the property-based built-in commands
            ////////////////////////////////////////////////////////////////////
            fgInitCommands();

            flightgear::registerSubsystemCommands(globals->get_commands());
* /
            ////////////////////////////////////////////////////////////////////
            // Initialize the material manager
            ////////////////////////////////////////////////////////////////////
            FGGlobals.globals.set_matlib(new SGMaterialLib());
            //TODO simgear::SGModelLib::setPanelFunc(FGPanelNode::load);

        } else if ((idle_state == 5) || (idle_state == 2005)) {
            idle_state += 2;
            PositionInit.initPosition();
            //flightgear::initTowerLocationListener();

            SGModelLib.init(FGGlobals.globals.get_fg_root(), FGGlobals.globals.get_props());

            //TimeManager* timeManager = (TimeManager*) globals->get_subsystem("time");
            //timeManager->init();

            ////////////////////////////////////////////////////////////////////
            // Initialize the TG scenery subsystem.
            ////////////////////////////////////////////////////////////////////

            FGGlobals.globals.set_scenery(new FGScenery());
            FGGlobals.globals.get_scenery().init();
            FGGlobals.globals.get_scenery().bind();
            boolean terrainonly = false;
            FGGlobals.globals.set_tile_mgr(new FGTileMgr(terrainonly));

            //fgSplashProgress("creating-subsystems");
        } else if ((idle_state == 7) || (idle_state == 2007)) {
            boolean isReset = (idle_state == 2007);
            idle_state = 8; // from the next state on, reset & startup are identical
            //SGTimeStamp st;
            //st.stamp();
            FgInit.fgCreateSubsystems(isReset);
            logger.info("Creating subsystems took:");// + st.elapsedMSec());
            //fgSplashProgress("binding-subsystems");

        } else if (idle_state == 8) {
            idle_state++;
            // SGTimeStamp st;
            //st.stamp();
            FGGlobals.globals.get_subsystem_mgr().bind();
            logger.info("Binding subsystems took:");// << st.elapsedMSec());

            //fgSplashProgress("init-subsystems");
        } else if (idle_state == 9) {
            /*InitStatus* /
            int status = FGGlobals.globals.get_subsystem_mgr().incrementalInit();
            if (status == SGSubsystemMgr.INIT_DONE) {
                ++idle_state;
                //fgSplashProgress("finishing-subsystems");
            } else {
                //fgSplashProgress("init-subsystems");
            }

        } else if (idle_state == 10) {
          /*  idle_state = 900;
            fgPostInitSubsystems();
            fgSplashProgress("finalize-position");* /
        } else if (idle_state == 900) {
            /*idle_state = 1000;

            // setup OpenGL viewer parameters
            globals->get_renderer()->setupView();

            globals->get_renderer()->resize( fgGetInt("/sim/startup/xsize"),
                    fgGetInt("/sim/startup/ysize") );
            WindowSystemAdapter::getWSA()->windows[0]->gc->add(
                    new simgear::canvas::VGInitOperation()
            );

            int session = fgGetInt("/sim/session",0);
            session++;
            fgSetInt("/sim/session",session);* /
        }

     /*   if ( idle_state == 1000 ) {
            // We've finished all our initialization steps, from now on we
            // run the main loop.
            fgSetBool("sim/sceneryloaded", false);
            registerMainLoop();
        }

        if ( idle_state == 2000 ) {
            fgStartNewReset();
            idle_state = 2005;
        }* /
    }*/
/*
    void fgResetIdleState()
    {
        idle_state = 2000;
        fgRegisterIdleHandler( &fgIdleFunction );
    }

*/

    /**
     * Wat 'n dat'n
     *
     * @param name
     */
    static void upper_case_property(String name) {
        //using namespace simgear;
        SGPropertyNode p = FGProperties.fgGetNode(name, false);
        if (p == null) {
            p = FGProperties.fgGetNode(name, true);
            p.setStringValue("");
        } else {
           /* props::Type*/
            int t = p.getType();
            if (t == Props.NONE || t == Props.UNSPECIFIED)
                p.setStringValue("");
            else {


                //TODO    assert (t == Props.STRING);
            }
        }
        /*TODO SGPropertyChangeListener muc = new FGMakeUpperCase();
        globals.addListenerToCleanup(muc);
        p.addChangeListener(muc);*/
    }
/*
    // see http://code.google.com/p/flightgear-bugs/issues/detail?id=385
// for the details of this.
    static void ATIScreenSizeHack()
    {
        osg::ref_ptr<osg::Camera> hackCam = new osg::Camera;
        hackCam->setRenderOrder(osg::Camera::PRE_RENDER);
        int prettyMuchAnyInt = 1;
        hackCam->setViewport(0, 0, prettyMuchAnyInt, prettyMuchAnyInt);
        globals->get_renderer()->addCamera(hackCam, false);
    }

// Propose NVIDIA Optimus to use high-end GPU
    #if defined(SG_WINDOWS)
    extern "C" {
        _declspec(dllexport) DWORD NvOptimusEnablement = 0x00000001;
    }
    #endif

    static void logToFile()
    {
        SGPath logPath = globals->get_fg_home();
        logPath.append("fgfs.log");
        if (logPath.exists()) {
            SGPath prevLogPath = globals->get_fg_home();
            prevLogPath.append("fgfs_0.log");
            logPath.rename(prevLogPath);
            // bit strange, we need to restore the correct value of logPath now
            logPath = globals->get_fg_home();
            logPath.append("fgfs.log");
        }
        sglog().logToFile(logPath, SG_ALL, SG_INFO);

        #if defined(HAVE_CRASHRPT)
        if (global_crashRptEnabled) {
            crAddFile2(logPath.c_str(), NULL, "FlightGear Log File", CR_AF_MAKE_FILE_COPY);
            SG_LOG( SG_GENERAL, SG_INFO, "CrashRpt enabled");
        } else {
            SG_LOG(SG_GENERAL, SG_WARN, "CrashRpt enabled at compile time but failed to install");
        }
        #endif
    }
    
    */

    // Main top level initialization
    public static int fgMainInit(int argc, String[] argv, boolean loadaircraft) {
        // das wird in FG anscheinend schon während des Class bootstrap gemacht. Und das mit dem register geschieht dort "hinten rum"
        //MA17Registry.setReadFileCallback(ModelRegistry.getInstance());
        //MA17ModelRegistry.getInstance().registerCallbacks();

        // set default log levels
        //sglog().setLogLevels( SG_ALL, SG_ALERT );

        //globals = new FGGlobals;
        FGGlobals.getInstance();

        if (!FgInit.fgInitHome(/*Platform.getInstance().getSystemProperty("FG_HOME")*/)) {
            return EXIT_FAILURE;
        }

        if (!FGProperties.fgGetBool("/sim/fghome-readonly", false)) {
            // now home isType initialised, we can log to a file inside it
            //logToFile();
        }

        /*std::string version;
        #ifdef FLIGHTGEAR_VERSION
        version = FLIGHTGEAR_VERSION;
        #else
        version = "unknown version";
        #endif
        SG_LOG( SG_GENERAL, SG_INFO, "FlightGear:  Version "
                << version );
        SG_LOG( SG_GENERAL, SG_INFO, "Built with " << SG_COMPILER_STR);
        SG_LOG( SG_GENERAL, SG_INFO, "Jenkins number/ID " << HUDSON_BUILD_NUMBER << ":"
                << HUDSON_BUILD_ID);

        tsch_log("fgMainInit: version %s\n", version.c_str());

        // seed the random number generator
        sg_srandom_time();

*/
        //string_list *col = new string_list;
        //globals->set_channel_options_list( col );

        // fgValidatePath("", false);  // initialize static variables
        upper_case_property("/sim/presets/airport-id");
        upper_case_property("/sim/presets/runway");
        upper_case_property("/sim/tower/airport-id");
        upper_case_property("/autopilot/route-manager/input");

        // Load the configuration parameters.  (Command line options
        // override config file options.  Config file options override
        // defaults.)
        int configResult = FgInit.fgInitConfig(argc, argv, false);
        if (configResult == Options.FG_OPTIONS_ERROR) {
            return EXIT_FAILURE;
        } else if (configResult == Options.FG_OPTIONS_EXIT) {
            return EXIT_SUCCESS;
        }

        // launcher needs to know the aircraft paths in use
        FgInit.fgInitAircraftPaths(false);

        /*#if defined(HAVE_QT)
        bool showLauncher = flightgear::Options::checkForArg(argc, argv, "launcher");
        // an Info.plist bundle can't define command line arguments, but it can set
        // environment variables. This avoids needed a wrapper shell-script on OS-X.
        showLauncher |= (::getenv("FG_LAUNCHER") != 0);

        if (showLauncher) {
            QApplication app(argc, argv);
            app.setOrganizationName("FlightGear");
            app.setApplicationName("FlightGear");
            app.setOrganizationDomain("flightgear.org");

            // avoid double Apple menu and other weirdness if both Qt and OSG
            // try to initialise various Cocoa structures.
            flightgear::WindowBuilder::setPoseAsStandaloneApp(false);

            if (!QtLauncher::runLauncherDialog()) {
                return EXIT_SUCCESS;
            }
        }
        #endif*/

        /*25.3.18 Aircraft werden mittlerweile als Vevicle geladen configResult = FgInit.fgInitAircraft(false,loadaircraft);
        if (configResult == Options.FG_OPTIONS_ERROR) {
            return EXIT_FAILURE;
        } else if (configResult == Options.FG_OPTIONS_EXIT) {
            return EXIT_SUCCESS;
        }

        configResult = /*flightgear::* /Options.sharedInstance().processOptions();
        if (configResult == Options.FG_OPTIONS_ERROR) {
            return EXIT_FAILURE;
        } else if (configResult == Options.FG_OPTIONS_EXIT) {
            return EXIT_SUCCESS;
        }*/

        // Initialize the Window/Graphics environment.
        //fgOSInit(&argc, argv);
        //_bootstrap_OSInit++;

        //30.9.19 FgOsCommon.fgRegisterIdleHandler( /*&fgIdleFunction*/);

        // Initialize sockets (WinSock needs this)
        //simgear::Socket::initSockets();

        // Clouds3D requires an alpha channel
        //fgOSOpenWindow(true /* request stencil buffer */);
        //fgOSResetProperties();

        // Initialize the splash screen right away
        //fntInit();
        //fgSplashInit();

       /* if (fgGetBool("/sim/ati-viewport-hack", true)) {
            SG_LOG(SG_GENERAL, SG_ALERT, "Enabling ATI viewport hack");
            ATIScreenSizeHack();
        }*/

        //fgOutputSettings();

        //try to disable the screensaver
        //fgOSDisableScreensaver();

        // pass control off to the master event handler
        //int result = fgOSMainLoop();
        //frame_signal.clear();
        //fgOSCloseWindow();

        //simgear::clearEffectCache();

        // clean up here; ensure we null globals to avoid
        // confusing the atexit() handler
        //delete globals;
        //globals = NULL;

        // delete the NavCache here. This will cause the destruction of many cached
        // objects (eg, airports, navaids, runways).
        //delete flightgear::NavDataCache::instance();

        //return result;
        return 0;

    }

}
