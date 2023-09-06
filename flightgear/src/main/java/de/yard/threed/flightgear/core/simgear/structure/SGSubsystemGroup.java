package de.yard.threed.flightgear.core.simgear.structure;

/**
 * Created by thomass on 07.06.16.
 */

import java.util.ArrayList;
import java.util.List;

/**
 * A group of FlightGear subsystems.
 */
public class SGSubsystemGroup {
    //typedef std::vector<Member *> MemberVec;
    //MemberVec _members;
    //HashMap<String,Member> _members;
    List<Member> _members = new ArrayList<Member>();

    double _fixedUpdateTime;
    double _updateTimeRemainder;

    /// index of the member we are currently init-ing
     int _initPosition;
    
  /*  SGSubsystemGroup::SGSubsystemGroup () :
    _fixedUpdateTime(-1.0),
    _updateTimeRemainder(0.0),
    _initPosition(0) 
    {
    }

    SGSubsystemGroup::~SGSubsystemGroup ()
    {
        // reverse order to prevent order dependency problems
        for( size_t i = _members.size(); i > 0; i-- )
        {
            delete _members[i-1];
        }
    }

    void
    SGSubsystemGroup::init ()
    {
        for( size_t i = 0; i < _members.size(); i++ )
            _members[i]->subsystem->init();
    }
*/
   // SGSubsystem::InitStatus
    public int incrementalInit()    {
        if (_initPosition >= _members.size())
            return SGSubsystemMgr.INIT_DONE;

        //tsch_log("subsystem_mgr::incrementalInit _initPosition=%d, subsystem=%s\n",_initPosition,typeid(*_members[_initPosition]->subsystem).name());

       // SGTimeStamp st;
       // st.stamp();
        /*InitStatus*/int memberStatus = _members.get(_initPosition).subsystem.incrementalInit();
       // _members[_initPosition]->initTime += st.elapsedMSec();

        if (memberStatus == SGSubsystemMgr.INIT_DONE)
            ++_initPosition;

        return SGSubsystemMgr.INIT_CONTINUE;
    }

  /*  void
    SGSubsystemGroup::postinit ()
    {
        for( size_t i = 0; i < _members.size(); i++ )
            _members[i]->subsystem->postinit();
    }

    void
    SGSubsystemGroup::reinit ()
    {
        for( size_t i = 0; i < _members.size(); i++ )
            _members[i]->subsystem->reinit();
    }

    void
    SGSubsystemGroup::shutdown ()
    {
        // reverse order to prevent order dependency problems
        for( size_t i = _members.size(); i > 0; i-- )
            _members[i-1]->subsystem->shutdown();
        _initPosition = 0;
    }
*/
    void
    bind ()
    {
        for( int i = 0; i < _members.size(); i++ )
            _members.get(i).subsystem.bind();
    }
/*
    void
    SGSubsystemGroup::unbind ()
    {
        // reverse order to prevent order dependency problems
        for( size_t i = _members.size(); i > 0; i-- )
            _members[i-1]->subsystem->unbind();
    }

    void
    SGSubsystemGroup::update (double delta_time_sec)
    {
        int loopCount = 1;
        // if dt == 0.0, we are paused, so we need to run one iteration
        // of our members; if we have a fixed update time, we compute a
        // loop count, and locally adjust dt
        if ((delta_time_sec > 0.0) && (_fixedUpdateTime > 0.0)) {
            double localDelta = delta_time_sec + _updateTimeRemainder;
            loopCount = SGMiscd::roundToInt(localDelta / _fixedUpdateTime);
            _updateTimeRemainder = delta_time_sec - (loopCount * _fixedUpdateTime);
            delta_time_sec = _fixedUpdateTime;
        }

        bool recordTime = (reportTimingCb != NULL);
        SGTimeStamp timeStamp;
        while (loopCount-- > 0) {
            for( size_t i = 0; i < _members.size(); i++ )
            {
                if (recordTime)
                    timeStamp = SGTimeStamp::now();

                _members[i]->update(delta_time_sec); // indirect call

                if ((recordTime)&&(reportTimingCb))
                {
                    timeStamp = SGTimeStamp::now() - timeStamp;
                    _members[i]->updateExecutionTime(timeStamp.toUSecs());
                }
            }
        } // of multiple update loop
    }

    void
    SGSubsystemGroup::reportTiming(void)
    {
        for( size_t i = _members.size(); i > 0; i-- )
        {
            _members[i-1]->reportTiming();
        }
    }

    void
    SGSubsystemGroup::suspend ()
    {
        for( size_t i = 0; i < _members.size(); i++ )
            _members[i]->subsystem->suspend();
    }

    void
    SGSubsystemGroup::resume ()
    {
        for( size_t i = 0; i < _members.size(); i++ )
            _members[i]->subsystem->resume();
    }

    string_list
    SGSubsystemGroup::member_names() const
    {
        string_list result;
        for( size_t i = 0; i < _members.size(); i++ )
            result.push_back( _members[i]->name );

        return result;
    }

    bool
    SGSubsystemGroup::is_suspended () const
    {
        return false;
    }
    */

    void set_subsystem (String name, SGSubsystem  subsystem,                                     double min_step_sec)    {
        Member member = get_member(name, true);
        if (member.subsystem != null) {
         //   delete member->subsystem;
        }
        member.name = name;
        member.subsystem = subsystem;
        member.min_step_sec = min_step_sec;
    }
/*
    SGSubsystem *
    SGSubsystemGroup::get_subsystem (const string &name)
    {
        Member * member = get_member(name);
        if (member != 0)
            return member->subsystem;
        else
            return 0;
    }

    void
    SGSubsystemGroup::remove_subsystem (const string &name)
    {
        MemberVec::iterator it = _members.begin();
        for (; it != _members.end(); ++it) {
            if (name == (*it)->name) {
                delete *it;
                _members.erase(it);
                return;
            }
        }

        SG_LOG(SG_GENERAL, SG_WARN, "remove_subsystem: missing:" << name);
    }

    //------------------------------------------------------------------------------
    void SGSubsystemGroup::clearSubsystems()
    {
        for( MemberVec::iterator it = _members.begin();
             it != _members.end();
        ++it )
        delete *it;
        _members.clear();
    }

    void
    SGSubsystemGroup::set_fixed_update_time(double dt)
    {
        _fixedUpdateTime = dt;
    }

    bool
    SGSubsystemGroup::has_subsystem (const string &name) const
    {
        return (((SGSubsystemGroup *)this)->get_member(name) != 0);
    }
*/
    Member get_member ( String name, boolean create)
    {
        for( int i = 0; i < _members.size(); i++ ) {
            if (_members.get(i).name.equals(name)) {
                return _members.get(i);
            }
        }
        if (create) {
            Member member;
             member = new Member(name);
            _members.add(member);
            return member;
        } else {
            return null;
        }
    }
/*

////////////////////////////////////////////////////////////////////////
// Implementation of SGSubsystemGroup::Member
////////////////////////////////////////////////////////////////////////


    SGSubsystemGroup::Member::Member ()
    : name(""),
    subsystem(0),
    min_step_sec(0),
    elapsed_sec(0),
    exceptionCount(0),
    initTime(0)
    {
    }

// This shouldn't be called due to subsystem pointer ownership issues.
    SGSubsystemGroup::Member::Member (const Member &)
    {
    }

    SGSubsystemGroup::Member::~Member ()
    {
    }

    void
    SGSubsystemGroup::Member::update (double delta_time_sec)
    {
        elapsed_sec += delta_time_sec;
        if (elapsed_sec < min_step_sec) {
            return;
        }

        if (subsystem->is_suspended()) {
            return;
        }

        try {
            subsystem->update(elapsed_sec);
            elapsed_sec = 0;
        } catch (sg_exception& e) {
        SG_LOG(SG_GENERAL, SG_ALERT, "caught exception processing subsystem:" << name
                << "\nmessage:" << e.getMessage());

        if (++exceptionCount > SG_MAX_SUBSYSTEM_EXCEPTIONS) {
            SG_LOG(SG_GENERAL, SG_ALERT, "(exceptionCount=" << exceptionCount <<
                    ", suspending)");
            subsystem->suspend();
        }
    }
    }
*/
}

class Member        {
    //SampleStatistic timeStat;
    public String name;
    public SGSubsystem subsystem;
    public double min_step_sec;
    public double elapsed_sec;
    public boolean collectTimeStats;
    public int exceptionCount;
    public int initTime;

    public Member(String name) {
        this.name = name;
    }
    /*
        Member (const Member &member);
public:
        Member ();
        virtual ~Member ();

        virtual void update (double delta_time_sec);

        void reportTiming(void) { if (reportTimingCb) reportTimingCb(reportTimingUserData, name, &timeStat); }
        void updateExecutionTime(double time) { timeStat += time;}

        
        };*/
}