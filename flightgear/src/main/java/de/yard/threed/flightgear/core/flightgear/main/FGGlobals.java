package de.yard.threed.flightgear.core.flightgear.main;

import de.yard.threed.core.Util;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.Degree;
import de.yard.threed.flightgear.FgBundleHelper;
import de.yard.threed.flightgear.FlightGearMain;
import de.yard.threed.flightgear.FlightGearSettings;
import de.yard.threed.flightgear.SimpleBundleResourceProvider;
import de.yard.threed.flightgear.core.StringList;
import de.yard.threed.flightgear.core.flightgear.sound.FGFX;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.misc.SGDir;
import de.yard.threed.flightgear.core.simgear.misc.SGPath;
import de.yard.threed.flightgear.core.simgear.props.PropertyObjectBase;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.flightgear.core.simgear.sound.SGSoundMgr;

/**
 * Aus globals.cxx
 * <p/>
 * Ich denke, das ist ein Singleton.
 *
 * 30.9.19: Das wird (nur noch?) aus FlightGearModuleBasic verwendet. Aber auch in SGModellib/matlib und FGTileMgr.
 * Und die beiden FG Bundle werden hier hinterlegt. Trotzdem natuerlich eine totale Kruecke.
 *
 * <p/>
 * Created by thomass on 30.05.16.
 */
public class FGGlobals {
    Log logger = Platform.getInstance().getLog(FGGlobals.class);

    // properties, destroy last
    // the root of the global property tree.
    // 5.11.24 Intended to move to FlightGearSystem/FlightGearProperties, but too much effort. See FlightGearProperties.
    SGPropertyNode props;

    // localization
/*    FGLocale* locale;
*/
   // FGRenderer renderer;
    //SGSubsystemMgr subsystem_mgr;
    /*SGEventMgr *event_mgr;

    // Number of milliseconds elapsed since the start of the program.
    double sim_time_sec;
*/
    
    /**
     * locations to search for (non-scenery) data.
     * /
     * PathList additional_data_paths;
     */
    // Users home directory for data
    //14.6.17 gibts nicht mehr wegen Bundle. Jetzt Bundle statt string.
    Bundle /*String*/ fg_home;
    // Root of FlightGear data tree
    Bundle/*String*/ fg_root;

    // Roots of FlightGear scenery tree
    StringList fg_scenery = new StringList();
    /*
        std::string browser;
    
        // Time structure
        SGTime *time_params;
    
        // Sky structures
        SGEphemeris *ephem;*/

   
    // Global autopilot "route"
        /*FGRouteMgr *route_mgr;
    
        // control input state
        FGControls *controls;
    
        // viewer manager
        FGViewMgr *viewmgr;
    
        SGCommandMgr *commands;
    
        // list of serial port-like configurations
        string_list *channel_options_list;
    
        // A list of initial waypoints that are read from the command line
        // and or flight-plan file during initialization
        string_list *initial_waypoints;
    */
    /*
        FGFontCache *fontcache;
    
        // Navigational Aids
        FGTACANList *channellist;
    */
    /// roots of Aircraft trees
    StringList fg_aircraft_dirs = new StringList();

    SGPath catalog_aircraft_dir = new SGPath();

    boolean haveUserSettings;

    SGPropertyNode positionLon, positionLat, positionAlt;
    SGPropertyNode viewLon, viewLat, viewAlt;
    SGPropertyNode orientHeading, orientPitch, orientRoll;

    /**
     * helper to initialise standard properties on a new property tree
     */
    //void initProperties();

    /*SGSharedPtr<FGSampleQueue> _chatter_queue;

    void cleanupListeners();

    typedef std::vector<SGPropertyChangeListener*> SGPropertyChangeListenerVec;
    SGPropertyChangeListenerVec _listeners_to_cleanup;

    SGSharedPtr<simgear::pkg::Root>_packageRoot;
*/
    // 10.12.25: Will there be a better location? And to we need both? On same level? Both have update().
   public SGSoundMgr sgSoundMgr;
    public FGFX fgfx;

    // global global :-)
    // Singleton als Ersatz fuer global Variable   
    public static FGGlobals globals = null;
/*

    {*/

    private FGGlobals() {
// Constructor
        //FGGlobals::FGGlobals() :
       // renderer = new FGRenderer();
        //subsystem_mgr = new SGSubsystemMgr();
        /*        event_mgr( new SGEventMgr ),
                sim_time_sec( 0.0 ),
                fg_root( "" ),
                fg_home( "" ),
                time_params( NULL ),
                ephem( NULL ),
                route_mgr( NULL ),
                controls( NULL ),
                viewmgr( NULL ),
                commands( SGCommandMgr::instance() ),
        channel_options_list( NULL ),
                initial_waypoints( NULL ),
                fontcache ( new FGFontCache ),
                channellist( NULL ),
                haveUserSettings(false),
                _chatter_queue(NULL)*/

        props = new SGPropertyNode(""/*TODO leerstring?*/);
        //props = SGPropertyNode_ptr(root);
        // locale = new FGLocale(props);

        //30.9.19SgResourceManager.getInstance().addProvider(new AircraftResourceProvider());
        //30.9.19SgResourceManager.getInstance().addProvider(new CurrentAircraftDirProvider());
        //28.9.19: Ist das noch zeitgemeass? Erzeugt aus resolve eine self modification. Die AircraftResourceProvider nutzen
        //doch erst mit geladenem Bundle.
        //30.9.19 es gibt doch noch kein Aircraft BundleRegistry.addProvider(new AircraftResourceProvider());
        //30.9.19BundleRegistry.addProvider(new CurrentAircraftDirProvider());
        initProperties();

       sgSoundMgr= new SGSoundMgr();
       sgSoundMgr.activate();
    }

    /**
     * The main global property tree root.
     */
    public static FGGlobals getInstance() {
        if (globals == null) {
            globals = new FGGlobals();
        }
        return globals;
    }

    private void initProperties() {
        PropertyObjectBase.setDefaultRoot(props);

        positionLon = props.getNode("position/longitude-deg", true);
        positionLat = props.getNode("position/latitude-deg", true);
        positionAlt = props.getNode("position/altitude-ft", true);

        viewLon = props.getNode("sim/current-viewer/viewer-lon-deg", true);
        viewLat = props.getNode("sim/current-viewer/viewer-lat-deg", true);
        viewAlt = props.getNode("sim/current-viewer/viewer-elev-ft", true);

        orientPitch = props.getNode("orientation/pitch-deg", true);
        orientHeading = props.getNode("orientation/heading-deg", true);
        orientRoll = props.getNode("orientation/roll-deg", true);

    }
/*
    
// Destructor
    FGGlobals::~FGGlobals()
    {
        // save user settings (unless already saved)
        saveUserSettings();

        // The AIModels manager performs a number of actions upon
        // Shutdown that implicitly assume that other subsystems
        // are still operational (Due to the dynamic allocation and
        // deallocation of AIModel objects. To ensure we can safely
        // shut down all subsystems, make sure we take down the 
        // AIModels system getFirst.
        SGSubsystemRef ai = subsystem_mgr->get_subsystem("ai-model");
        if (ai) {
            subsystem_mgr->remove("ai-model");
            ai->unbind();
            ai.clear(); // ensure AI isType deleted now, not at end of this method
        }

        subsystem_mgr->shutdown();
        subsystem_mgr->unbind();

        subsystem_mgr->remove("aircraft-model");
        subsystem_mgr->remove("tile-manager");
        subsystem_mgr->remove("model-manager");
        _tile_mgr.clear();

        osg::ref_ptr<osgViewer::Viewer> vw(renderer->getViewer());
        if (vw) {
            // https://code.google.com/p/flightgear-bugs/issues/detail?id=1291
            // explicitly stop trheading before we delete the renderer or
            // viewMgr (which ultimately holds refs to the CameraGroup, and
            // GraphicsContext)
            vw->stopThreading();
        }

        // don't cancel the pager until after shutdown, since AIModels (and
        // potentially others) can queue delete requests on the pager.
        if (vw && vw->getDatabasePager()) {
            vw->getDatabasePager()->cancel();
            vw->getDatabasePager()->clear();
        }

        osgDB::Registry::instance()->clearObjectCache();

        // renderer touches subsystems during its destruction
        set_renderer(NULL);
        _scenery.clear();
        _chatter_queue.clear();

        delete subsystem_mgr;
        subsystem_mgr = NULL; // important so ::get_subsystem returns NULL
        vw = 0; // don't delete the viewer until now

        delete time_params;
        set_matlib(NULL);
        delete route_mgr;
        delete channel_options_list;
        delete initial_waypoints;
        delete fontcache;
        delete channellist;

        simgear::PropertyObjectBase::setDefaultRoot(NULL);
        simgear::SGModelLib::resetPropertyRoot();

        delete locale;
        locale = NULL;

        cleanupListeners();

        props.clear();

        delete commands;
    }*/

    // set the fg_root path
    public void set_fg_root(Bundle root) {
        //SGPath tmp = new SGPath(root);
        fg_root = root;//tmp.realpath();

        // append /data to root if it exists
        /*29.6.17 tmp.append("data");
        tmp.append("version");
        if (tmp.exists()) {
            FGProperties.fgGetNode("BAD_FG_ROOT", true).setStringValue(fg_root);
            fg_root += "/data";
            FGProperties.fgGetNode("GOOD_FG_ROOT", true).setStringValue(fg_root);
            logger.warn(/*SG_LOG(SG_GENERAL, SG_ALERT,* / "***\n***\n*** Warning: changing bad FG_ROOT/--fg-root to '" + fg_root + "'\n***\n***");
        }*/

        // remove /sim/fg-root before writing to prevent hijacking
        SGPropertyNode n = FGProperties.fgGetNode("/sim", true);
        //TODO n.removeChild("fg-root", 0);
        n = n.getChild("fg-root", 0, true);
        //29.6.17: wegen Bundle mal nicht mehr n.setStringValue(fg_root);
        //TODO n.setAttribute(Props.WRITE, false);

        //29.6.17: wegen Bundle mal nicht mehr als basepath. 
        //SgResourceManager.getInstance().addBasePath(new SGPath(fg_root)/*, ResourceManager::PRIORITY_DEFAULT*/);
        FgBundleHelper.addProvider(new SimpleBundleResourceProvider("fgdatabasic"));
         //6.3.25 FgBundleHelper.addProvider(new SimpleBundleResourceProvider(FlightGearSettings.FGROOTCOREBUNDLE));
    }

    public Bundle get_fg_root() {
        return fg_root;
    }

    // set the fg_home path
    /*30.9.19 public void set_fg_home(Bundle/*String* / home) {
        //SGPath tmp = new SGPath(home);
        fg_home = home;//tmp.realpath();
    }*/

    /*
        PathList FGGlobals::get_data_paths() const
        {
            PathList r(additional_data_paths);
            r.push_back(SGPath(fg_root));
            return r;
        }
    
        PathList FGGlobals::get_data_paths(const std::string& suffix) const
        {
            PathList r;
            BOOST_FOREACH(SGPath p, get_data_paths()) {
            p.append(suffix);
            if (p.exists()) {
                r.push_back(p);
            }
        }
    
            return r;
        }
    
        void FGGlobals::append_data_path(const SGPath& path)
        {
            if (!path.exists()) {
                SG_LOG(SG_GENERAL, SG_WARN, "adding non-existant data path:" << path);
            }
    
            additional_data_paths.push_back(path);
        }
    
        SGPath FGGlobals::find_data_dir(const std::string& pathSuffix) const
        {
            BOOST_FOREACH(SGPath p, additional_data_paths) {
            p.append(pathSuffix);
            if (p.exists()) {
                return p;
            }
        }
    
            SGPath rootPath(fg_root);
            rootPath.append(pathSuffix);
            if (rootPath.exists()) {
                return rootPath;
            }
    
            SG_LOG(SG_GENERAL, SG_WARN, "dir not found in any data path:" << pathSuffix);
            return SGPath();
        }*/

    public void append_fg_scenery(String paths) {
        //30.9.19: Das brauchts doch nicht mehr, oder?
        Util.nomore();
        SGPropertyNode sim = FGProperties.fgGetNode("/sim", true);

        // find getFirst unused fg-scenery property in /sim
        int propIndex = 0;
        while (sim.getChild("fg-scenery", propIndex) != null) {
            ++propIndex;
        }

        //BOOST_FOREACH(const SGPath& path, sgPathSplit( paths )) {
        for (String path : StringList.sgPathSplit(paths)) {
            SGPath abspath = new SGPath(new SGPath(path).realpath());
            if (!abspath.exists()) {
                logger.warn(/*SG_LOG(SG_GENERAL, SG_WARN,*/ "scenery path not found:" + abspath.str());
                continue;
            }

            // check for duplicates
                /*TODO
            string_list::const_iterator ex = std::find(fg_scenery.begin(), fg_scenery.end(), abspath.str());
            if (ex != fg_scenery.end()) {
                SG_LOG(SG_GENERAL, SG_INFO, "skipping duplicate add of scenery path:" << abspath.str());
                continue;
            }*/

            SGDir dir = new SGDir(abspath);
            SGPath terrainDir = dir.file("Terrain");
            SGPath objectsDir = dir.file("Objects");

            // this code used to add *either* the base dir, OR add the 
            // Terrain and Objects subdirs, but the conditional logic was commented
            // out, such that all three dirs are added. Unfortunately there's
            // no information as to why the change was made.
            fg_scenery.add(abspath.str());

            if (terrainDir.exists()) {
                fg_scenery.push_back(terrainDir.str());
            }

            if (objectsDir.exists()) {
                fg_scenery.push_back(objectsDir.str());
            }

            // insert a marker for FGTileEntry::load(), so that
            // FG_SCENERY=A:B becomes list ["A/Terrain", "A/Objects", "",
            // "B/Terrain", "B/Objects", ""]
            fg_scenery.push_back("");

            // make scenery dirs available to Nasal
            SGPropertyNode  n = sim.getChild("fg-scenery", propIndex++, true);
            n.setStringValue(abspath.str());
            //TODO n.setAttribute(SGPropertyNode.WRITE, false);

            // temporary fix so these values survive reset
            //TODO n -> setAttribute(SGPropertyNode::PRESERVE, true);
        } // of path list iteration
    }

    public void    clear_fg_scenery() {
        fg_scenery.clear();
    }

  /*  void FGGlobals
    ::

    set_catalog_aircraft_path(const SGPath&path) {
        catalog_aircraft_dir = path;
    }

    */

    /**
     * //28.9.19: Ist das noch zeitgemeass?  Die AircraftResourceProvider nutzen
     * doch Bundle statt aircraft path. Mal uebergehen.
     *
     * @return
     */
   /*30.9.19  public StringList get_aircraft_paths() {
        StringList r = new StringList();
        if (!catalog_aircraft_dir.isNull()) {
            r.push_back(catalog_aircraft_dir.str());
        }

        //r.insert(r.end(), fg_aircraft_dirs.begin(), fg_aircraft_dirs.end());
        r.addAll(fg_aircraft_dirs);
        return r;
    }*/

    /*30.9.19 void append_aircraft_path(String path) {
        SGPath dirPath = new SGPath(path);
        if (!dirPath.exists()) {
            logger.error("aircraft path not found:" + path);
            return;
        }

        SGPath acSubdir = new SGPath(dirPath);
        acSubdir.append("Aircraft");
        if (acSubdir.exists()) {
            logger.warn("Specified an aircraft-dir with an 'Aircraft' subdirectory:" + dirPath + ", will instead use child directory:" + acSubdir);
            dirPath = acSubdir;
        }

        String abspath = dirPath.realpath();
        fg_aircraft_dirs.push_back(abspath);
    }*/

    /*30.9.19 void append_aircraft_paths(String path) {
        StringList paths = StringList.sgPathSplit(path);
        for (int p = 0; p < paths.size(); ++p) {
            append_aircraft_path(paths.get(p));
        }
    }*/

    /*30.9.19 public SGPath resolve_aircraft_path(String branch) {
        return SgResourceManager.getInstance().findPath(branch, (SGPath)null);
    }*/

    /*
        SGPath FGGlobals::resolve_maybe_aircraft_path(const std::string& branch) const
        {
            return simgear::ResourceManager::instance()->findPath(branch);
        }
    
        SGPath FGGlobals::resolve_resource_path(const std::string& branch) const
        {
            return simgear::ResourceManager::instance()
            ->findPath(branch, SGPath(fgGetString("/sim/aircraft-dir")));
        }
    */
   /*30.9.19  public FGRenderer get_renderer() {
        return renderer;
    }*/

    /*
        void FGGlobals::set_renderer(FGRenderer *render)
        {
            if (render == renderer) {
                return;
            }
    
            delete renderer;
            renderer = render;
        }
    */
   /*30.9.19  public SGSubsystemMgr get_subsystem_mgr() {
        return subsystem_mgr;
    }*/

    /*
      
        SGSubsystem *
        FGGlobals::get_subsystem (const char * name)
        {
            if (!subsystem_mgr) {
                return NULL;
            }
    
            return subsystem_mgr->get_subsystem(name);
        }
    */
   /*30.9.19  public void add_subsystem(String name, SGSubsystem subsystem,
                              /*SGSubsystemMgr::GroupType* /int type,
                              double min_time_sec) {
        subsystem_mgr.add(name, subsystem, type, min_time_sec);
    }*/

    /*
        SGSoundMgr *
        FGGlobals::get_soundmgr () const
        {
            if (subsystem_mgr)
                return (SGSoundMgr*) subsystem_mgr->get_subsystem("sound");
    
            return NULL;
        }
    
        SGEventMgr *
        FGGlobals::get_event_mgr () const
        {
            return event_mgr;
        }
    
        SGGeod
        FGGlobals::get_aircraft_position() const
        {
            return SGGeod::fromDegFt(positionLon->getDoubleValue(),
                positionLat->getDoubleValue(),
                positionAlt->getDoubleValue());
        }
    
        SGVec3d
        FGGlobals::get_aircraft_position_cart() const
        {
            return SGVec3d::fromGeod(get_aircraft_position());
        }
    
        void FGGlobals::get_aircraft_orientation(double& heading, double& pitch, double& roll)
        {
            heading = orientHeading->getDoubleValue();
            pitch = orientPitch->getDoubleValue();
            roll = orientRoll->getDoubleValue();
        }*/

    public SGGeod get_view_position() {
        return new SGGeod(new Degree(viewLon.getDoubleValue()), new Degree(viewLat.getDoubleValue()), viewAlt.getDoubleValue());
    }

  /*30.9.19   public Vector3 get_view_position_cart() {
        return get_view_position().toCart();
    }*/

    /*static void treeDumpRefCounts(int depth, SGPropertyNode* nd)
    {
        for (int i=0; i<nd->nChildren(); ++i) {
            SGPropertyNode* cp = nd->getChild(i);
            if (SGReferenced::count(cp) > 1) {
                SG_LOG(SG_GENERAL, SG_INFO, "\t" << cp->getPath() << " refcount:" << SGReferenced::count(cp));
            }

            treeDumpRefCounts(depth + 1, cp);
        }
    }

    static void treeClearAliases(SGPropertyNode* nd)
    {
        if (nd->isAlias()) {
            nd->unalias();
        }

        for (int i=0; i<nd->nChildren(); ++i) {
            SGPropertyNode* cp = nd->getChild(i);
            treeClearAliases(cp);
        }
    }

    void
    FGGlobals::resetPropertyRoot()
    {
        delete locale;

        cleanupListeners();

        // we don't strictly need to clear these (they will be reset when we
        // initProperties again), but trying to reduce false-positives when dumping
        // ref-counts.
        positionLon.clear();
        positionLat.clear();
        positionAlt.clear();
        viewLon.clear();
        viewLat.clear();
        viewAlt.clear();
        orientPitch.clear();
        orientHeading.clear();
        orientRoll.clear();

        // clear aliases so ref-counts are accurate when dumped
        treeClearAliases(props);

        SG_LOG(SG_GENERAL, SG_INFO, "root props refcount:" << props.getNumRefs());
        treeDumpRefCounts(0, props);

        //BaseStackSnapshot::dumpAll(std::cout);

        props = new SGPropertyNode;
        initProperties();
        locale = new FGLocale(props);

        // remove /sim/fg-root before writing to prevent hijacking
        SGPropertyNode *n = props->getNode("/sim", true);
        n->removeChild("fg-root", 0);
        n = n->getChild("fg-root", 0, true);
        n->setStringValue(fg_root.c_str());
        n->setAttribute(SGPropertyNode::WRITE, false);
    }

    static std::string autosaveName()
    {
        std::ostringstream os;
        string_list versionParts = simgear::strutils::split(VERSION, ".");
        if (versionParts.size() < 2) {
            return "autosave.xml";
        }

        os << "autosave_" << versionParts[0] << "_" << versionParts[1] << ".xml";
        return os.str();
    }

// Load user settings from autosave.xml
    void
    FGGlobals::loadUserSettings(const SGPath& dataPath)
    {
        // remember that we have (tried) to load any existing autsave.xml
        haveUserSettings = true;

        SGPath autosaveFile = simgear::Dir(dataPath).file(autosaveName());
        SGPropertyNode autosave;
        if (autosaveFile.exists()) {
            SG_LOG(SG_INPUT, SG_INFO, "Reading user settings from " << autosaveFile.str());
            try {
                readProperties(autosaveFile.str(), &autosave, SGPropertyNode::USERARCHIVE);
            } catch (sg_exception& e) {
                SG_LOG(SG_INPUT, SG_WARN, "failed to read user settings:" << e.getMessage()
                        << "(from " << e.getOrigin() << ")");
            }
        }
        copyProperties(&autosave, globals->get_props());
    }

// Save user settings in autosave.xml
    void
    FGGlobals::saveUserSettings()
    {
        // only save settings when we have (tried) to load the previous
        // settings (otherwise user data was lost)
        if (!haveUserSettings)
            return;

        if (fgGetBool("/sim/startup/save-on-exit")) {
            // don't save settings more than once on shutdown
            haveUserSettings = false;

            SGPath autosaveFile(globals->get_fg_home());
            autosaveFile.append(autosaveName());
            autosaveFile.create_dir( 0700 );
            SG_LOG(SG_IO, SG_INFO, "Saving user settings to " << autosaveFile.str());
            try {
                writeProperties(autosaveFile.str(), globals->get_props(), false, SGPropertyNode::USERARCHIVE);
            } catch (const sg_exception &e) {
                guiErrorMessage("Error writing autosave:", e);
            }
            SG_LOG(SG_INPUT, SG_DEBUG, "Finished Saving user settings");
        }
    }

    FGViewer *
    FGGlobals::get_current_view () const
    {
        return viewmgr->get_current_view();
    }

    long int FGGlobals::get_warp() const
    {
        return fgGetInt("/sim/time/warp");
    }

    void FGGlobals::set_warp( long int w )
    {
        fgSetInt("/sim/time/warp", w);
    }

    long int FGGlobals::get_warp_delta() const
    {
        return fgGetInt("/sim/time/warp-delta");
    }

    void FGGlobals::set_warp_delta( long int d )
    {
        fgSetInt("/sim/time/warp-delta", d);
    }
*/
    
/*
    FGSampleQueue* FGGlobals::get_chatter_queue() const
    {
        return _chatter_queue;
    }

    void FGGlobals::set_chatter_queue(FGSampleQueue* queue)
    {
        _chatter_queue = queue;
    }

    void FGGlobals::addListenerToCleanup(SGPropertyChangeListener* l)
    {
        _listeners_to_cleanup.push_back(l);
    }

    void FGGlobals::cleanupListeners()
    {
        SGPropertyChangeListenerVec::iterator i = _listeners_to_cleanup.begin();
        for (; i != _listeners_to_cleanup.end(); ++i) {
            delete *i;
        }
        _listeners_to_cleanup.clear();
    }*/

   /* keine Packages vorerst simgear::pkg::Root*FGGlobals::

    packageRoot() {
        return _packageRoot.get();
    }

    void FGGlobals
    ::

    setPackageRoot(const SGSharedPtr<simgear::pkg::Root>&p) {
        _packageRoot = p;
    }*/

// end of globals.cxx

    /**
     * Returns the global property tree.
     */
    public SGPropertyNode get_props() {
        return props;
    }

    public Bundle get_fg_home() {
        return fg_home;
    }

    public StringList get_fg_scenery() {
        return fg_scenery;
    }
}
