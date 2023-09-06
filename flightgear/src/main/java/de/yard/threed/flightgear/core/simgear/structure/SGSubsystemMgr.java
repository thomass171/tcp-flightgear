package de.yard.threed.flightgear.core.simgear.structure;

/**
 * Created by thomass on 07.06.16.
 */

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Manage subsystems for FlightGear.
 * <p/>
 * This top-level subsystem will eventually manage all of the
 * subsystems in FlightGear: it broadcasts its life-cycle events
 * (init, bind, etc.) to all of the subsystems it manages.  Subsystems
 * are grouped to guarantee order of initialization and execution --
 * currently, the only two groups are INIT and GENERAL, but others
 * will appear in the future.
 * <p/>
 * All subsystems are named as well as grouped, and subsystems can be
 * looked up by name and cast to the appropriate subtype when another
 * subsystem needs to invoke specialized methods.
 * <p/>
 * The subsystem manager owns the pointers to all the subsystems in
 * it.
 */
public class SGSubsystemMgr /*: public SGSubsystem*/ {
    static Log logger = Platform.getInstance().getLog(SGSubsystemMgr.class);

    //SGSubsystemGroup* _groups[MAX_GROUPS];
    List<SGSubsystemGroup> _groups = new ArrayList<SGSubsystemGroup>();
    int _initPosition;

    // non-owning reference
    //typedef std::map<std::string, SGSubsystem*> SubsystemDict;
    //SubsystemDict _subsystem_map;
    HashMap<String, SGSubsystem> _subsystem_map = new HashMap<String, SGSubsystem>();

    /**
     * Types of subsystem groups.
     */
//enum GroupType {
    public static int INIT = 0,
            GENERAL = 1,
            FDM = 2,        ///< flight model, autopilot, instruments that run coupled
            POST_FDM = 3,   ///< certain subsystems depend on FDM data
            DISPLAY = 4,    ///< viewer, camera, rendering updates
            SOUND = 5;/*I want to be last!*/  ///< needs to run AFTER display, to allow concurrent GPU/sound processing
    int MAX_GROUPS = 20;

    public static int INIT_DONE = 1,      ///< subsystem isType fully initialised
            INIT_CONTINUE = 2;   ///< init should be called again

    public SGSubsystemMgr() {
        for (int i = 0; i < MAX_GROUPS; i++) {
            _groups.add(new SGSubsystemGroup());

        }
    }

    /*
    
            SGSubsystemMgr::~SGSubsystemMgr()
            {
            // ensure get_subsystem returns NULL from now onwards,
            // before the SGSubsystemGroup destructors are run
            _subsystem_map.clear();
    
            for(int i=0;i<MAX_GROUPS;i++){
            delete _groups[i];
            }
            }
    
            void
            SGSubsystemMgr::init()
            {
            for(int i=0;i<MAX_GROUPS;i++)
            _groups[i]->init();
            }
    */
    //SGSubsystem::InitStatus
    public int incrementalInit() {
        if (_initPosition >=/*MAX_GROUPS*/_groups.size()) {
            return INIT_DONE;
        }

        /*InitStatus*/
        int memberStatus = _groups.get(_initPosition).incrementalInit();
        if (memberStatus == INIT_DONE) {
            ++_initPosition;
        }

        return INIT_CONTINUE;
    }

    /*
            void
            SGSubsystemMgr::postinit()
            {
            for(int i=0;i<MAX_GROUPS;i++)
            _groups[i]->postinit();
            }
    
            void
            SGSubsystemMgr::reinit()
            {
            for(int i=0;i<MAX_GROUPS;i++)
            _groups[i]->reinit();
            }
    
            void
            SGSubsystemMgr::shutdown()
            {
            // reverse order to prevent order dependency problems
            for(int i=MAX_GROUPS-1;i>=0;i--)
            _groups[i]->shutdown();
    
            _initPosition=0;
            }
    */
    
            public void            bind()
            {
                for (SGSubsystemGroup grp : _groups)
                grp.bind();            
            }
    /*
            void
            SGSubsystemMgr::unbind()
            {
            // reverse order to prevent order dependency problems
            for(int i=MAX_GROUPS-1;i>=0;i--)
            _groups[i]->unbind();
            }
    
            void
            SGSubsystemMgr::update(double delta_time_sec)
            {
            for(int i=0;i<MAX_GROUPS;i++){
            _groups[i]->update(delta_time_sec);
            }
            }
    
            void
            SGSubsystemMgr::suspend()
            {
            for(int i=0;i<MAX_GROUPS;i++)
            _groups[i]->suspend();
            }
    
            void
            SGSubsystemMgr::resume()
            {
            for(int i=0;i<MAX_GROUPS;i++)
            _groups[i]->resume();
            }
    
            bool
            SGSubsystemMgr::is_suspended()const
            {
            return false;
            }
    */
    public void add(String name, SGSubsystem subsystem, int /*        GroupType*/ group, double min_time_sec) {
        logger.debug("Adding subsystem " + name);
        get_group(group).set_subsystem(name, subsystem, min_time_sec);

       /* if(_subsystem_map.find(name)!=_subsystem_map.end()){
        SG_LOG(SG_GENERAL,SG_ALERT,"Adding duplicate subsystem "<<name);
        throw sg_exception("duplicate subsystem:"+std::string(name));
        }*/
        _subsystem_map.put(name, subsystem);
    }
/*
        void
        SGSubsystemMgr::remove(const char*name)
        {
        SubsystemDict::iterator s=_subsystem_map.find(name);
        if(s==_subsystem_map.end()){
        return;
        }

        _subsystem_map.erase(s);

// tedious part - we don't know which group the subsystem belongs too
        for(int i=0;i<MAX_GROUPS;i++){
        if(_groups[i]->get_subsystem(name)!=NULL){
        _groups[i]->remove_subsystem(name);
        break;
        }
        } // of groups iteration
        }
*/

    SGSubsystemGroup get_group(/*GroupType*/int group) {
        return _groups.get(group);//[group];
    }
/*
        SGSubsystem*
        SGSubsystemMgr::get_subsystem(const string&name)const
        {
        SubsystemDict::const_iterator s=_subsystem_map.find(name);

        if(s==_subsystem_map.end())
        return 0;
        else
        return s->getSecond;
        }

/** Trigger the timing callback to report data for all subsystems. * /
        void
        SGSubsystemMgr::reportTiming()
        {
        for(int i=0;i<MAX_GROUPS;i++){
        _groups[i]->reportTiming();
        } // of groups iteration
        }*/
}


// end of subsystem_mgr.cxx
