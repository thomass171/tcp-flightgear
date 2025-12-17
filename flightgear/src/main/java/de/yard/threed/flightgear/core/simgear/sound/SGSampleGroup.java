package de.yard.threed.flightgear.core.simgear.sound;

import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.flightgear.core.simgear.geodesy.FgMath;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.yard.threed.flightgear.core.simgear.Constants.SGD_PI_2;
import static de.yard.threed.flightgear.core.simgear.Constants.SG_FEET_TO_METER;

/**
 * From sample_group.[hc]xx
 * <p>
 * Manage a group of samples relative to a base position
 * <p>
 * Sample groups contain all sounds related to one specific object and
 * have to be added to the sound manager, otherwise they won't get processed.
 * <p>
 * Written for the new SoundSystem by Erik Hofman, October 2009
 * <p>
 * Copyright (C) 2009-2019 Erik Hofman <erik@ehofman.com>
 */
//class SGSoundMgr;

public class SGSampleGroup /*: public SGReferenced*/ {

    public SGSoundMgr _smgr = null;
    public String _refname = "";
    boolean _active = false;

    // Mach induced Sonic boom
    public boolean _mInCone = false;
    public float _mOffset_m = 0.0f;

    boolean _changed = false;
    boolean _pause = false;
    float _volume = 1.0f;
    float _pressure = 101.325f;
    float _humidity = 0.5f;
    float _degC = 20.0f;

    boolean _tied_to_listener = false;

    Vector3 _velocity = new Vector3();//SGVec3d::zeros();
    Quaternion _orientation = new Quaternion();//SGQuatd::zeros();
    SGGeod _base_pos = new SGGeod();

    //   using sample_map = std::map< std::string, SGSharedPtr<SGSoundSample> >;
    /*sample_map*/ public Map<String, SGSoundSample> _samples = new HashMap<>();
    List<SGSoundSample> _removed_samples = new ArrayList<>();
    List<String> _refsToRemoveFromSamplesMap = new ArrayList<>();

    //bool testForMgrError(std::string s);
    //bool testForError(void *p, std::string s);

    //void update_pos_and_orientation();
    //void update_sample_config( SGSoundSample *sound );

    // Mach induced Sonic boom
   /*TODO  enum

    {
        UP = 0,
                RIGHT,
                BACK
    }*/

    float _mach = 0.0f;

    /**
     * Empty constructor for class encapsulation.
     * Note: The sample group should still be activated before use
     */
    public SGSampleGroup() {
        _samples.clear();

    }

    /**
     * Constructor
     * Register this sample group at the sound manager using refname
     * Note: The sample group should still be activated before use
     *
     * @param smgr    Pointer to a pre-initialized sound manager class
     * @param refname Name of this group for reference purposes.
     */
    public SGSampleGroup(SGSoundMgr smgr, String refname) {

        _smgr = smgr;
        _refname = refname;

        _smgr.add(this, refname);
        _samples.clear();

    }

    /**
     * Destructor
     */
    //virtual ~SGSampleGroup ();

    /**
     * Set the status of this sample group to active.
     */
    /*inline*/ void activate() {
        _active = true;
    }

    /**
     * Update function.
     * Call this function periodically to update the state of all
     * samples associated with this class. None op the configuration changes
     * take place without a call to this function.
     */
    /*virtual*/
    public void update(double dt) {
        if (!_active || _pause) return;

        testForMgrError("start of update!!\n");

        cleanup_removed_samples();

        // Update the position and orientation information for all samples.
        if (_changed || _smgr.has_changed()) {
            update_pos_and_orientation();
            _changed = false;
        }

        for (String current : _samples.keySet()) {
            SGSoundSample sample = _samples.get(current);//current.second;

            if (!sample.is_valid_source() && sample.is_playing() && !sample.test_out_of_range()) {
                start_playing_sample(sample);

            } else if (sample.is_valid_source()) {
                check_playing_sample(sample);
            }
            testForMgrError("update");
        }

        for (/*const auto &*/String refname : _refsToRemoveFromSamplesMap) {
            _samples.remove/*erase*/(refname);
        }
        _refsToRemoveFromSamplesMap.clear();
    }

    /**
     * Register an audio sample to this group.
     *
     * @param sound   Pointer to a pre-initialized audio sample
     * @param refname Name of this audio sample for reference purposes
     * @return return true if successful
     */
    boolean add( /*SGSharedPtr<*/SGSoundSample sound, String refname) {
        //auto sample_it = _samples.find( refname );
        SGSoundSample sample_it = _samples.get(refname);
        if (sample_it != null/*_samples.end() */) {
            // sample name already exists
            return false;
        }

        _samples.put(refname, sound);
        return true;

    }

    /**
     * Remove an audio sample from this group.
     *
     * @param refname        Reference name of the audio sample to remove
     * @param delayedRemoval If true, don't modify _samples before the main
     *                       loop in update() is finished
     * @return return true if successful
     */
    boolean remove(String refname, boolean delayedRemoval /*= false*/) {
        //auto sample_it = _samples.find( refname );
        SGSoundSample sample_it = _samples.get(refname);
        if (sample_it == null/*_samples.end() */) {
            // sample was not found
            return false;
        }

        if (sample_it/* . second*/.is_valid_buffer())
            _removed_samples.add(sample_it /*. second*/);

        // Do not erase within the loop in update()
        if (delayedRemoval) {
            _refsToRemoveFromSamplesMap.add(refname);
        } else {
            _samples.remove(refname);
        }

        return true;

    }

    /**
     * Test if a specified audio sample is registered at this sample group
     *
     * @param refname Reference name of the audio sample to test for
     * @return true if the specified audio sample exists
     */
    boolean exists(String refname) {
        //auto sample_it = _samples.find( refname );
        SGSoundSample sample_it = _samples.get(refname);
        if (sample_it == null/*_samples.end() */) {
            // sample was not found
            return false;
        }

        return true;

    }

    /**
     * Find a specified audio sample in this sample group
     *
     * @param refname Reference name of the audio sample to find
     * @return A pointer to the SGSoundSample
     */
    SGSoundSample find(String refname) {
        //auto sample_it = _samples.find( refname );
        SGSoundSample sample_it = _samples.get(refname);
        if (sample_it == null/*_samples.end() */) {
            // sample was not found
            return null;
        }
        return sample_it /*. second*/;
    }

    /**
     * Stop all playing samples and set the source id to invalid.
     */
    public void stop() {
        _pause = true;
        /*TODO for (auto current : _samples) {
            _smgr . sample_destroy(current.second);
        }*/

    }

    /**
     * Request to stop playing all audio samples until further notice.
     */
    public void suspend() {
        if (_active && _pause == false) {
            _pause = true;
//#ifdef ENABLE_SOUND
            for (SGSoundSample current : _samples.values()) {
                _smgr.sample_suspend(current/*.second*/);
            }
//#endif
            testForMgrError("suspend");
        }

    }

    /**
     * Request to resume playing all audio samples.
     */
    public void resume() {
        if (_active && _pause == true) {
//#ifdef ENABLE_SOUND
            for (SGSoundSample current : _samples.values()) {
                _smgr.sample_resume(current/*.second*/);
            }
            testForMgrError("resume");
//#endif
            _pause = false;
        }

    }

    /**
     * Request to start playing the referred audio sample.
     *
     * @param refname Reference name of the audio sample to start playing
     * @param looping Define if the sound should loop continuously
     * @return true if the audio sample exsists and is scheduled for playing
     */
    boolean play(String refname, boolean looping /*=false*/) {
        SGSoundSample sample = find(refname);

        if (sample == null) {
            return false;
        }

        sample.play(looping);
        return true;

    }

    /**
     * Request to start playing the referred audio sample looping.
     *
     * @param refname Reference name of the audio sample to start playing
     * @return true if the audio sample exsists and is scheduled for playing
     */
    boolean play_looped(String refname) {
        return play(refname, true);
    }

    /**
     * Request to start playing the referred audio sample once.
     *
     * @param refname Reference name of the audio sample to start playing
     * @return true if the audio sample exists and is scheduled for playing
     */
    /*inline*/ boolean play_once(String refname) {
        return play(refname, false);
    }

    /**
     * Test if a referenced sample is playing or not.
     *
     * @param refname Reference name of the audio sample to test for
     * @return True of the specified sound is currently playing
     */
    boolean is_playing(String refname) {
        SGSoundSample sample = find(refname);
        if (sample == null) {
            return false;
        }
        return (sample.is_playing()) ? true : false;
    }

    /**
     * Request to stop playing the referred audio sample.
     *
     * @param refname Reference name of the audio sample to stop
     * @return true if the audio sample exists and is scheduled to stop
     */
    boolean stop(String refname) {
        SGSoundSample sample = find(refname);

        if (sample == null) {
            return false;
        }

        sample.stop();
        return true;
    }

    /**
     * Set the master volume for this sample group.
     *
     * @param vol Volume (must be between 0.0 and 1.0)
     */
    public void set_volume(float vol) {
        if (vol > _volume * 1.01 || vol < _volume * 0.99) {
            _volume = vol;
            //TODO SG_CLAMP_RANGE(_volume, 0.0f, 1.0f);
            _changed = true;
        }
    }

    /**
     * Set the velocity vector of this sample group.
     * This is in the local frame coordinate system; x=north, y=east, z=down
     *
     * @param vel Velocity vector
     */
    void set_velocity(Vector3 vel) {
        _velocity = vel;
        _changed = true;
    }

    /**
     * Set the position of this sample group.
     * This is in the same coordinate system as OpenGL; y=up, z=back, x=right.
     *
     * @param pos Base position
     */
    void set_position_geod(SGGeod pos) {
        _base_pos = pos;
        _changed = true;
    }

    /**
     * Set the orientation of this sample group.
     *
     * @param ori Quaternation containing the orientation information
     */
    void set_orientation(Quaternion ori) {
        _orientation = ori;
        _changed = true;
    }

    /**
     * Set both the temperature and relative humidity at the current altitude.
     *
     * @param t Temperature in degrees Celsius
     * @param h Percent relative humidity (0.0 to 1.0)
     * @param p Pressure in kPa
     */
    void set_atmosphere(float t, float h, float p) {
        _degC = t;
        _humidity = h;
        _pressure = p;
        _changed = true;
    }

    /**
     * Tie this sample group to the listener position, orientation and velocity
     */
    public void tie_to_listener() {
        _tied_to_listener = true;
    }

    void cleanup_removed_samples() {
        // Delete any buffers that might still be in use.
        int size = _removed_samples.size();
        for (int i = 0; i < size; ) {
            SGSoundSample sample = _removed_samples.get(i);

            _smgr.sample_stop(sample);
            boolean stopped = _smgr.is_sample_stopped(sample);

            if (stopped) {
                sample.stop();
                if ((!sample.is_queue()) &&
                        (sample.is_valid_buffer())) {
                    _smgr.release_buffer(sample);
                }
                _removed_samples.remove(/*_removed_samples.begin() +*/ i);
                size--;
                continue;
            }
            i++;
        }
    }

    void start_playing_sample(SGSoundSample sample) {
        //FG-DIFF no need to init an audio (done by platform), but we keeping for keeping
        // the program flow (Setting a pseudo source)
        _smgr.sample_init(sample);
        update_sample_config(sample);
        _smgr.sample_play(sample);
    }

    /**
     *
     * @param sample
     */
    void check_playing_sample(SGSoundSample sample) {
        // check if the sound has stopped by itself
        if (_smgr.is_sample_stopped(sample)) {
            // sample is stopped because it wasn't looping
            sample.stop();
            sample.no_valid_source();
            /*FG-DIFF  _smgr.release_source(sample.get_source());
            _smgr.release_buffer(sample);*/
            // Delayed removal because this is called while iterating over _samples
            remove(sample.get_sample_name(), true);
        } else if (sample.has_changed()) {
            if (!sample.is_playing()) {
                // a request to stop playing the sound has been filed.
                sample.stop();
                sample.no_valid_source();
                /*FG-DIFF _smgr.release_source(sample.get_source());*/
            } else if (_smgr.has_changed()) {
                update_sample_config(sample);
            }
        }
    }

    // set the source position and orientation of all managed sounds
    void update_pos_and_orientation() {

        Vector3 base_position = /*SGVec3d::fromGeod*/ (_base_pos.toCart());
        Vector3 smgr_position = _smgr.get_position();
        Quaternion hlOr = FgMath.fromLonLat (_base_pos);
        Quaternion ec2body = hlOr.multiply( _orientation);

        Vector3 velocity = new Vector3();//SGVec3d::zeros ();
        if (_velocity.length() != 0.0/*_velocity[0] || _velocity[1] || _velocity[2]*/) {
            //TODO velocity = hlOr.backTransform(_velocity * SG_FEET_TO_METER);
        }

        float speed = 0.0f;
        double mAngle = SGD_PI_2;
        if (!_tied_to_listener) {
            float Rvapor = 461.52f; // Water vapor: individual gas constant
            float Rair = 287.5f;    // Air: individual gas constant
            float y = 1.402f;       // Air: Ratio of specific heat

            float T = 273.16f + _degC;    // Kelvin
            float hr = 0.01f * _humidity;
            float R = Rair + 0.04f * hr * Rvapor;
            float sound_speed = (float) Math.sqrt(y * R * T); // m/s

            speed = (float) velocity.length();
            _mach = speed / sound_speed;
            mAngle = Math.asin(1.0 / _mach);
        }

        for (SGSoundSample current : _samples.values()) {
            SGSoundSample sample = current/*.second*/;
            sample.set_master_volume(_volume);
            sample.set_orientation(_orientation);
            sample.set_rotation(ec2body);
            sample.set_position(base_position);
            sample.set_velocity(/*toVec3f*/(velocity));
            sample.set_atmosphere(_degC, _humidity, _pressure);

            if (!_tied_to_listener) {
                sample.update_pos_and_orientation();

                // Sample position relative to the listener, including the
                // sample offset relative to the base position.
                // Same coordinate system as OpenGL; y=up, x=right, z=back
                Vector3 position = sample.get_position().subtract( smgr_position);
                /*TODO if (_mach > 1.0) {
                    _mOffset_m = position[BACK];

                    // Skip the slant calculation for angles greater than 89 deg
                    // to avoid instability
                    if (mAngle < 1.553343) {
                        _mOffset_m -= position[UP] / Math.tan(mAngle);
                    }
                    _mInCone = (_mOffset_m > 0.01) ? true : false;
                }*/

                // Test if a sample is farther away than max distance, if so
                // stop the sound playback and free it's source.
                float max2 = sample.get_max_dist() * sample.get_max_dist();
                //float dist2 = position[0] * position[0] + position[1] * position[1] + position[2] * position[2];
                float dist2 = (float) (position.getX() * position.getX() + position.getY() * position.getY() + position.getZ() * position.getZ());
                if ((dist2 > max2) && !sample.test_out_of_range()) {
                    sample.set_out_of_range(true);
                } else if ((dist2 < max2) && sample.test_out_of_range()) {
                    sample.set_out_of_range(false);
                }
            }
        }
    }

    void update_sample_config(SGSoundSample sample) {
//#ifdef ENABLE_SOUND
        Vector3 orientation, velocity;
        Vector3 position;

        if (_tied_to_listener) {
            orientation = _smgr.get_direction();
            position = new Vector3();//SGVec3d::zeros ();
            velocity = _smgr.get_velocity();
        } else {
            sample.update_pos_and_orientation();
            orientation = sample.get_orientation();
            position = sample.get_position().subtract(_smgr.get_position());
            velocity = sample.get_velocity();
        }

/*#if 0
    if (length(position) > 20000)
        printf("%s source and listener distance greater than 20km!\n",
                _refname.c_str());
    if (isNaN(toVec3f(position).data())) printf("NaN in source position\n");
    if (isNaN(orientation.data())) printf("NaN in source orientation\n");
    if (isNaN(velocity.data())) printf("NaN in source velocity\n");
#endif*/

        _smgr.update_sample_config(sample, position, orientation, velocity);
//#endif
    }

   /* boolean testForError(void *p, String s) {
        if (p == nullptr) {
            SG_LOG(SG_SOUND, SG_ALERT, "Error (sample group): " << s);
            return true;
        }
        return false;
    }*/

    boolean testForMgrError(String s) {
        _smgr.testForError(s + " (sample group)", _refname);
        return false;
    }

}

