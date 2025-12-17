package de.yard.threed.flightgear.core.simgear.sound;

import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.flightgear.core.simgear.geodesy.FgMath;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.flightgear.core.simgear.structure.DefaultSGSubsystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * From soundmgr.hxx and soundmgr_openal.cxx
 * <p>
 * Sound effect management class
 * <p>
 * Provides a sound manager class to keep track of multiple sounds and manage
 * playing them with different effects and timings.
 * <p>
 * Sound manager initially written by David Findlay
 * <david_j_findlay@yahoo.com.au> 2001
 */

/**
 * Manage a collection of SGSampleGroup instances
 */
public class SGSoundMgr extends DefaultSGSubsystem {
    Log logger = Platform.getInstance().getLog(SGSoundMgr.class);

    // Speed of sound in meters per second
    public static double SPEED_OF_SOUND = 340.3;

    //private:
    //class SoundManagerPrivate
    /// private implementation object
    /*std::unique_ptr<*/ public SoundManagerPrivate d = new SoundManagerPrivate();

    boolean _block_support;
    boolean _active = false;
    boolean _changed = true;
    float _volume = 0.0f;

    double _sound_velocity = SPEED_OF_SOUND;

    // Position of the listener.
    SGGeod _geod_pos = new SGGeod();

    // Velocity of the listener.
    Vector3 _velocity = new Vector3();//SGVec3d::zeros();

    String _renderer = "unknown";
    String _vendor = "unknown";
    String _device_name;

    // bool testForALCError(std::string s);
    // bool testForError(void *p, std::string s);
    // void update_sample_config( SGSampleGroup *sound );

    public SGSoundMgr() {

    }
    //virtual ~SGSoundMgr();

    // Subsystem API.
   /* void init() override;
    void reinit() override;
    void resume() override;
    void suspend() override;*/

    @Override
    public void update(double dt) {
//#ifdef ENABLE_SOUND
        if (_active) {
            // alcSuspendContext(d._context);

            if (_changed) {
                d.update_pos_and_orientation();
            }

            for (SGSampleGroup current : d._sample_groups.values()) {
                current./*second->*/update(dt);
            }

            if (_changed) {
/*#if 0
                if (isNaN(d->_at_up_vec)) printf("NaN in listener orientation\n");
                if (isNaN(toVec3f(d->_absolute_pos).data())) printf("NaN in listener position\n");
                if (isNaN(toVec3f(_velocity).data())) printf("NaN in listener velocity\n");
#endif*/
                //alListenerf(AL_GAIN, _volume);
                //alListenerfv(AL_ORIENTATION, d._at_up_vec);
                // alListenerfv( AL_POSITION, toVec3f(_absolute_pos).data() );

                Quaternion hlOr = FgMath.fromLonLat(_geod_pos);
                Vector3 velocity = new Vector3();//SGVec3d::zeros ();
                if (_velocity.length() > 0.0/*_velocity[0] || _velocity[1] || _velocity[2]*/) {
                    //TODO velocity = hlOr.backTransform(_velocity.multiply(/* * */SG_FEET_TO_METER));

                    /*TODO if (d._bad_doppler) {
                        float fact = 100.0f;
                        double mag = velocity.length();

                        if (mag > _sound_velocity) {
                            fact *= _sound_velocity / mag;
                        }
                        alDopplerFactor(fact);
                    }*/
                }

              /*  alListenerfv(AL_VELOCITY, toVec3f(velocity).data());
             alDopplerVelocity(_sound_velocity);
                testForError("update");*/
                _changed = false;
            }

            //alcProcessContext(d . _context);
        }
//#endif
    }


    // Subsystem identification.
    static String staticSubsystemClassId() {
        return "sound";
    }

    public void stop() {
        //#ifdef ENABLE_SOUND
        // first stop all sample groups
        for (SGSampleGroup current : d._sample_groups.values()) {
            current./*second->*/stop();
        }

        // clear all OpenAL sources
       /*for (ALuint source : d . _free_sources) {
            alDeleteSources(1, & source );
            testForError("SGSoundMgr::stop: delete sources");
        }
        d . _free_sources.clear();

        // clear any OpenAL buffers before shutting down
        for (auto current : d . _buffers) {
            refUint ref = current.second;
            ALuint buffer = ref.id;
            alDeleteBuffers(1, & buffer);
            testForError("SGSoundMgr::stop: delete buffers");
        }

        d . _buffers.clear();
        d . _sources_in_use.clear();

        if (is_working()) {
            _active = false;
            alcDestroyContext(d . _context);
            alcCloseDevice(d . _device);
            d . _context = nullptr;
            d . _device = nullptr;

            _renderer = "unknown";
            _vendor = "unknown";
        }*/
//#endif
    }


    /**
     * Select a specific sound device.
     * Requires a init/reinit call before sound is actually switched.
     */
    /*inline*/ void select_device(String devname) {
        _device_name = devname;
    }

    /**
     * Test is the sound manager is in a working condition.
     *
     * @return true is the sound manager is working
     */
    boolean is_working() {
        // We do not depend on any device like FG, so we can just go on.
        return true;//(d._device != null);
    }


    /**
     * Set the sound manager to a  working condition.
     */
    public void activate() {
        //#ifdef ENABLE_SOUND
        if (is_working()) {
            _active = true;

            for (SGSampleGroup current : d._sample_groups.values()) {
                current./*second->*/activate();
            }
        }
//#endif
    }

    /**
     * Test is the sound manager is in an active and working condition.
     *
     * @return true is the sound manager is active
     */
    /*inline*/
    public boolean is_active() {
        return _active;
    }

    /**
     * Register a sample group to the sound manager.
     *
     * @param sgrp    Pointer to a sample group to add
     * @param refname Reference name of the sample group
     * @return true if successful, false otherwise
     */
    public boolean add(SGSampleGroup sgrp, String refname) {
        //auto sample_grp_it = d -> _sample_groups.find(refname);
        SGSampleGroup sample_grp_it = d._sample_groups.get(refname);
        if (sample_grp_it != null/*d -> _sample_groups.end()*/) {
            // sample group already exists
            return false;
        }

        if (_active) sgrp.activate();
        d._sample_groups.put(refname, sgrp);
        return true;
    }


    /**
     * Remove a sample group from the sound manager.
     *
     * @param refname Reference name of the sample group to remove
     * @return true if successful, false otherwise
     */
    public boolean remove(String refname) {
        //auto sample_grp_it = d -> _sample_groups.find(refname);
        SGSampleGroup sample_grp_it = d._sample_groups.get(refname);
        if (sample_grp_it == null/*d -> _sample_groups.end()*/) {
            // sample group was not found.
            return false;
        }

        d._sample_groups.remove(refname);//erase(sample_grp_it);

        return true;
    }


    /**
     * Test if a specified sample group is registered at the sound manager
     *
     * @param refname Reference name of the sample group test for
     * @return true if the specified sample group exists
     */
    boolean exists(String refname) {
        //auto sample_grp_it = d -> _sample_groups.find(refname);
        SGSampleGroup sample_grp_it = d._sample_groups.get(refname);
        return (sample_grp_it != null/*d -> _sample_groups.end()*/);
    }


    /**
     * Find a specified sample group in the sound manager
     *
     * @param refname Reference name of the sample group to find
     * @param create  If the group should be create if it does not exist
     * @return A pointer to the SGSampleGroup
     */
    public SGSampleGroup find(String refname, boolean create/* =false*/) {
        //auto sample_grp_it = d -> _sample_groups.find(refname);
        SGSampleGroup sample_grp_it = d._sample_groups.get(refname);
        if (sample_grp_it == null/*d -> _sample_groups.end()*/) {
            // sample group was not found.
            if (create) {
                SGSampleGroup sgrp = new SGSampleGroup(this, refname);
                add(sgrp, refname);
                return sgrp;
            } else
                return null;
        }

        return sample_grp_it;// -> second;
    }


    /**
     * Set the Cartesian position of the sound manager.
     *
     * @param pos OpenAL listener position
     */
    void set_position(Vector3 pos, SGGeod pos_geod) {
        d._base_pos = pos;
        _geod_pos = pos_geod;
        _changed = true;

    }

    /**
     * Get the position of the sound manager.
     * This is in the same coordinate system as OpenGL; y=up, z=back, x=right
     *
     * @return OpenAL listener position
     */
    Vector3 get_position() {
        return d._absolute_pos;
    }

    /**
     * Set the velocity vector (in meters per second) of the sound manager
     * This is the horizontal local frame; x=north, y=east, z=down
     *
     * @param vel Velocity vector
     */
    void set_velocity(Vector3 vel) {
        _velocity = vel;
        _changed = true;
    }

    /**
     * Get the velocity vector of the sound manager
     * This is in the same coordinate system as OpenGL; y=up, z=back, x=right.
     *
     * @return Velocity vector of the OpenAL listener
     */
    /*inline SGVec3f*/Vector3 get_velocity() {
        return /*toVec3f*/(_velocity);
    }

    /**
     * Set the orientation of the sound manager
     *
     * @param ori Quaternation containing the orientation information
     */
    void set_orientation(Quaternion ori) {
        d._orientation = ori;
        _changed = true;

    }

    /**
     * Get the orientation of the sound manager
     *
     * @return Quaternation containing the orientation information
     */
    Quaternion get_orientation() {
        return d._orientation;
    }

    /**
     * Get the direction vector of the sound manager
     * This is in the same coordinate system as OpenGL; y=up, z=back, x=right.
     *
     * @return Look-at direction of the OpenAL listener
     */
    Vector3 get_direction() {
        return new Vector3(d._at_up_vec[0], d._at_up_vec[1], d._at_up_vec[2]);

    }

    // enum    {
    public static int NO_SOURCE = -1;// = (unsigned int)-1,
    public static int NO_BUFFER = -2;//(unsigned int)-1,
    public static int FAILED_BUFFER = -3;//= (unsigned int)-2
    //}

    ;

    /**
     * Set the master volume.
     *
     * @param v Volume (must be between 0.0 and 1.0)
     */
    void set_volume(float v) {
        _volume = v;
        //TODO SG_CLAMP_RANGE(_volume, 0.0f, 1.0f);
        _changed = true;

    }

    /**
     * Get the master volume.
     *
     * @return Volume (must be between 0.0 and 1.0)
     */
    /*inline*/    float get_volume() {
        return _volume;
    }

    /**
     * Set the speed of sound.
     *
     * @param vel Sound velocity
     */
    void set_sound_velocity(double vel) {
        _sound_velocity = vel;
    }

    /**
     * Get a free OpenAL source-id
     *
     * @return NO_SOURCE if no source is available
     */
    /*TODO int request_source() {

    }*/

    /**
     * Free an OpenAL source-id for future use
     *
     * @param source OpenAL source-id to free
     */
    void release_source(int source) {
//TODO?
    }

    /**
     * Get a free OpenAL buffer-id
     * The buffer-id will be assigned to the sample by calling this function.
     *
     * @param sample Pointer to an audio sample to assign the buffer-id to
     * @return NO_BUFFER if loading of the buffer failed.
     */

    /*TODO int request_buffer(SGSoundSample *sample) {
    }*/

    /**
     * Free an OpenAL buffer-id for this sample
     *
     * @param sample Pointer to an audio sample for which to free the buffer
     */
    void release_buffer(SGSoundSample sample) {
//TODO ?
    }

    /**
     * Initialize sample for playback.
     * FG-DIFF no need to init an audio (done by platform). But we keep it for keeping
     * the existing program flow.
     *
     * @param sample Pointer to an audio sample to initialize.
     */
    void sample_init(SGSoundSample sample) {
        // #ifdef ENABLE_SOUND
        //
        // a request to start playing a sound has been filed.
        //
        //ALuint source = request_source();
        //if (alIsSource(source) == AL_FALSE ) {
        //    return;
        //}
        sample.set_source(9999/*source */);
//#endif

    }

    /**
     * Stop and destroy a sample
     *
     * @param sample Pointer to an audio sample to destroy.
     */
    /*FG-DIFF void sample_destroy(SGSoundSample sample) {
        if (sample.is_valid_source()) {
//#ifdef ENABLE_SOUND
            //ALint source = sample.get_source();
            if (sample.is_playing()) {
                alSourceStop( source );
                sample.stop();
                //testForError("stop");
            }
            //release_source( source );
//#endif
            //sample.no_valid_source();
        }

        if (sample.is_valid_buffer()) {
            release_buffer(sample);
            sample.no_valid_buffer();
        }

    }*/

    /**
     * Start playback of a sample
     *
     * @param sample Pointer to an audio sample to start playing.
     */
    void sample_play(SGSoundSample sample) {
        //#ifdef ENABLE_SOUND
        /*AL*/
        boolean looping = sample.is_looping();// ? AL_TRUE : AL_FALSE;
        //ALint source = sample.get_source();

        if (!sample.is_queue()) {
            /*ALuint buffer = request_buffer(sample);
            if (buffer == SGSoundMgr::FAILED_BUFFER ||
                    buffer == SGSoundMgr::NO_BUFFER)
            {
                release_source(source);
                return;
            }*/

            // start playing the sample
            /*buffer = sample.get_buffer();
            if ( alIsBuffer(buffer) == AL_TRUE )
            {
                alSourcei( source, AL_BUFFER, buffer );
                testForError("assign buffer to source");
            } else
                SG_LOG( SG_SOUND, SG_ALERT, "No such buffer!");*/
        }

        //alSourcef( source, AL_ROLLOFF_FACTOR, 0.3 );
        //alSourcei( source, AL_LOOPING, looping );
        //alSourcei( source, AL_SOURCE_RELATIVE, AL_FALSE );
        //FG-DIFF alSourcePlay( source );
        sample.playAudio();
        //testForError("sample play");
//#endif

    }

    /**
     * Stop a sample
     *
     * @param sample Pointer to an audio sample to stop.
     */
    void sample_stop(SGSoundSample sample) {
        if (sample.is_valid_source()) {
            //int source = sample.get_source();

            boolean stopped = is_sample_stopped(sample);
            if (sample.is_looping() && !stopped) {
//#ifdef ENABLE_SOUND
                //FG-DIFF alSourceStop( source );
                sample.stop();
//#endif
                stopped = is_sample_stopped(sample);
            }

            if (stopped) {
                sample.no_valid_source();
                //FG-DIFF     release_source(source);
            }
        }

    }

    /**
     * Suspend playback of a sample
     *
     * @param sample Pointer to an audio sample to suspend.
     */
    void sample_suspend(SGSoundSample sample) {
        if (/*FG-DIFF sample.is_valid_source() &&*/ sample.is_playing()) {
            //FG-DIFF alSourcePause( sample->get_source() );
            sample.pause();
        }
    }

    /**
     * Resume playback of a sample
     *
     * @param sample Pointer to an audio sample to resume.
     */
    void sample_resume(SGSoundSample sample) {
        if (/*FG-DIFF sample.is_valid_source() &&*/ sample.is_playing()) {
            //FG-DIFF alSourcePlay( sample->get_source() );
            sample.play(false);
        }
    }

    /**
     * Check if a sample is stopped, or still playing
     *
     * This is about effectively playing??
     *
     * @param sample Pointer to an audio sample to test.
     * @return true if the sample is stopped.
     */
    boolean is_sample_stopped(SGSoundSample sample) {
//#ifdef ENABLE_SOUND
       /*TODO  if (sample . is_valid_source()) {
            ALint source = sample . get_source();
            ALint result;
            alGetSourcei(source, AL_SOURCE_STATE, & result );
            return (result == AL_STOPPED);
        }*/
//#endif
        // We don't really have such a flag. However it is important to avoid continuously restart
        return !sample.isEffectivelyPlaying();
    }


    /**
     * Update all status and 3d parameters of a sample.
     *
     * @param sample Pointer to an audio sample to update.
     */
    void update_sample_config(SGSoundSample sample, Vector3 position, Vector3 orientation, Vector3 velocity) {
        //TODO
    }

    /**
     * Test if the position of the sound manager has changed.
     * The value will be set to false upon the next call to update_late()
     *
     * @return true if the position has changed
     */
    /*inline*/
    boolean has_changed() {
        return _changed;
    }

    /**
     * Load a sample file and return it's configuration and data.
     *
     * @param samplepath Path to the file to load
     * @param data       Pointer to a variable that points to the allocated data
     * @param format     Pointer to a vairable that gets the OpenAL format
     * @param size       Pointer to a vairable that gets the sample size in bytes
     * @param freq       Pointer to a vairable that gets the sample frequency in Herz
     * @return true if succesful, false on error
     */
    /*virtual*/ /*TODO boolean load(String samplepath,
                             void**data,
                             int *format,
                             size_t *size,
                             int *freq,
                             int *block);*/

    /**
     * Get a list of available playback devices.
     */
    List<String> get_available_devices() {
      /*TODO   vector<std::string> devices;
#ifdef ENABLE_SOUND
    const ALCchar *s;

        if (alcIsExtensionPresent(nullptr, "ALC_enumerate_all_EXT") == AL_TRUE) {
            // REVIEW: Memory Leak - 4,136 bytes in 1 blocks are still reachable
            s = alcGetString(nullptr, ALC_ALL_DEVICES_SPECIFIER);
        } else {
            s = alcGetString(nullptr, ALC_DEVICE_SPECIFIER);
        }

        if (s) {
            ALCchar *nptr, *ptr = (ALCchar *)s;

            nptr = ptr;
            while (*(nptr += strlen(ptr)+1) != 0)
            {
                devices.push_back(ptr);
                ptr = nptr;
            }
            devices.push_back(ptr);
        }
#endif
        return devices;
*/
        return new ArrayList<>();
    }

    /**
     * Get the current OpenAL vendor or rendering backend.
     */
    String get_vendor() {
        return _vendor;
    }

    String get_renderer() {
        return _renderer;
    }

    boolean testForError(String s, String name /*= "sound manager"*/) {
        //      #ifdef ENABLE_SOUND
        /*TODO ALenum error = alGetError();
        if (error != AL_NO_ERROR)  {
            SG_LOG( SG_SOUND, SG_ALERT, "AL Error (" << name << "): "
                    << alGetString(error) << " at " << s);
            return true;
        }*/
//#endif
        return false;

    }

    public Map<String, SGSampleGroup> getSampleGroups() {
        return d._sample_groups;
    }
}

class SoundManagerPrivate {
    /*TODO ALCdevice *_device =nullptr;
    ALCcontext *_context =nullptr;

    std::
    vector<ALuint> _free_sources;
    std::
    vector<ALuint> _sources_in_use;*/

    Vector3 _absolute_pos = new Vector3();//SGVec3d::zeros();
    Vector3 _base_pos = new Vector3();//SGVec3d::zeros();
    Quaternion _orientation = new Quaternion();//SGQuatd::zeros();
    // Orientation of the listener.
    // first 3 elements are "at" vector, second 3 are "up" vector
    /*ALfloat*/ double _at_up_vec[] = new double[6];

    boolean _bad_doppler = false;
    boolean _source_radius = false;

    /*sample_group_map*/ Map<String, SGSampleGroup> _sample_groups = new HashMap<>();
    //buffer_map _buffers;

    /*public:
    SoundManagerPrivate() = default;
    ~SoundManagerPrivate() = default;
*/
    void init() {
        _at_up_vec[0] = 0.0;
        _at_up_vec[1] = 0.0;
        _at_up_vec[2] = -1.0;
        _at_up_vec[3] = 0.0;
        _at_up_vec[4] = 1.0;
        _at_up_vec[5] = 0.0;
        //_source_radius = alIsExtensionPresent((ALchar *)"AL_EXT_SOURCE_RADIUS");
    }

    void update_pos_and_orientation() {
        /**
         * Description: ORIENTATION is a pair of 3-tuples representing
         * 'at' direction vector and 'up' direction of the Object in
         * Cartesian space. AL expects two vectors that are orthogonal to
         * each other. These vectors are not expected to be normalized. If
         * one or more vectors have zero length, implementation behavior
         * is undefined. If the two vectors are linearly dependent,
         * behavior is undefined.
         *
         * This is in the same coordinate system as OpenGL; y=up, z=back, x=right.
         */
        /*TODO SGVec3d sgv_at = _orientation.backTransform(-SGVec3d::e3 ());
        SGVec3d sgv_up = _orientation.backTransform(SGVec3d::e2 ());
        _at_up_vec[0] = sgv_at[0];
        _at_up_vec[1] = sgv_at[1];
        _at_up_vec[2] = sgv_at[2];
        _at_up_vec[3] = sgv_up[0];
        _at_up_vec[4] = sgv_up[1];
        _at_up_vec[5] = sgv_up[2];

        _absolute_pos = _base_pos;*/
    }


}


