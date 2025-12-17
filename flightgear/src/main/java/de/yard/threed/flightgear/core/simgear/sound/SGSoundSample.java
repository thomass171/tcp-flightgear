package de.yard.threed.flightgear.core.simgear.sound;

import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.URL;
import de.yard.threed.engine.Audio;
import de.yard.threed.engine.AudioClip;
import de.yard.threed.flightgear.FgBundleHelper;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.misc.SGPath;

/**
 * From sample.[hc]xx -- Audio sample encapsulation class
 * <p>
 * Likely we only need parts of this because we have platform audio.
 */
/*TODO
enum {
SG_SAMPLE_MONO = 1,
SG_SAMPLE_STEREO = 2,

SG_SAMPLE_4BITS = 4,
SG_SAMPLE_8BITS = 8,
SG_SAMPLE_16BITS = 16,

SG_SAMPLE_UNCOMPRESSED = 0,
SG_SAMPLE_COMPRESSED = 256,

SG_SAMPLE_MONO8    = (SG_SAMPLE_MONO|SG_SAMPLE_8BITS),
SG_SAMPLE_MONO16   = (SG_SAMPLE_MONO|SG_SAMPLE_16BITS),
SG_SAMPLE_MULAW    = (SG_SAMPLE_MONO|SG_SAMPLE_8BITS|SG_SAMPLE_COMPRESSED),
SG_SAMPLE_ADPCM    = (SG_SAMPLE_MONO|SG_SAMPLE_4BITS|SG_SAMPLE_COMPRESSED),

SG_SAMPLE_STEREO8  = (SG_SAMPLE_STEREO|SG_SAMPLE_8BITS),
SG_SAMPLE_STEREO16 = (SG_SAMPLE_STEREO|SG_SAMPLE_16BITS)
        };*/


public class SGSoundSample extends SGSoundSampleInfo/*, public SGReferenced*/ {
    Log logger = Platform.getInstance().getLog(SGSoundSample.class);
    boolean _is_file = false;
    boolean _changed = true;

    // Sources are points emitting sound.
    // FG-DIFF no need to maintain an audio source(done by platform audioclip), but the full sound framework
    // relies on having a flag for sound is actually playing(?). So use it for that.
    boolean _valid_source = false;
    //int _source = SGSoundMgr.NO_SOURCE;

    //private:
    //std::unique_ptr<unsigned char, decltype(free)*> _data = { nullptr, free };

    // Buffers hold sound data. FG-DIFF no need to maintain an audio source(done by platform)
    //boolean _valid_buffer = false;
    //unsigned int _buffer = SGSoundMgr::NO_BUFFER;

    public AudioClip audioClip;
    public Audio audio;
    public int playedAudio = 0;

    /**
     * Empty constructor, can be used to read data to the systems
     * memory and not to the driver.
     */
    /*SGSoundSample() = default;
    virtual ~SGSoundSample () = default;*/

    /**
     * Constructor that uses the specified path as is
     *
     * @param file Path to sound file
     *             Buffer data is freed by the sample group
     */
    /* explicit*/
    public SGSoundSample(/*const SGPath&*/BundleResource file) {
        _is_file = true;
        _refname = file;//.utf8Str();
        if (file != null) {
            audioClip = new AudioClip(file.getUrl());
            audio = Audio.buildAudio(audioClip);
        } else {
            int h = 9;
        }
    }

    /**
     * Constructor that goes through ResourceManager::findPath()
     *
     * @param file File name of sound
     * @param dir  “Context” argument for ResourceManager::findPath()
     *             Buffer data is freed by the sample group
     */
    SGSoundSample(String file, /*const SGPath&*/ BundleResource dir) {
        //  simgear::ResourceManager::instance()->findPath(file, dir))
        this(FgBundleHelper.findPath(file, dir));
        if (_refname == null) {
            logger.warn("Failed to resolve file " + file);
        }
    }

    /**
     * Constructor.
     * @param data Pointer to a memory buffer containing this audio sample data
    The application may free the data by calling free_data(), otherwise it
    will be resident until the class is destroyed. This pointer will be
    set to nullptr after calling this function.
     * @param len Byte length of array
     * @param freq Frequency of the provided data (bytes per second)
     * @param format SimGear format id of the data
     */
  /*TODO?  SGSoundSample( std::unique_ptr<unsigned char, decltype(free)*> data,
    int len, int freq,
    int format = SG_SAMPLE_MONO8 );
*/

    /**
     * Test if this audio sample configuration has changed since the last call.
     * Calling this function will reset the flag so calling it a second
     * time in a row will return false.
     *
     * @return Return true is the configuration has changed in the mean time.
     */
    boolean has_changed() {
        boolean b = _changed;
        _changed = false;
        return b;
    }

    /**
     * Detect whether this audio sample holds the information of a sound file.
     *
     * @return Return true if this sample is to be constructed from a file.
     */
    /*inline*/ boolean is_file() {
        return _is_file;
    }

    //SGPath file_path() const;

    /**
     * Schedule this audio sample for playing. Actual playing will only start
     * at the next call op SoundGroup::update()
     *
     * @param loop Define whether this sound should be played in a loop.
     */
    public void play(boolean loop /*= false*/) {
        logger.debug("play " + _refname);
        if ("jet.wav".equals(this._refname.getName())) {
            int h = 9;
        }

        _playing = true;
        _loop = loop;
        _changed = true;
        _static_changed = true;
        if (audio != null) {
            audio.setLooping(_loop);
        }
    }

    /**
     * Check if this audio sample is set to be continuous looping.
     *
     * @return Return true if this audio sample is set to looping.
     */
    /*inline*/ boolean is_looping() {
        return _loop;
    }

    /**
     * Schedule this audio sample to stop playing.
     */
    /*virtual*/
    public void stop() {
        logger.debug("stop " + _refname);
        if ("fgdatabasic:Sounds/jet.wav".equals(this._refname.toString())) {
            int h = 9;
        }

        _playing = false;
        _changed = true;
    }

    /**
     * Schedule this audio sample to play once.
     *
     * @see #play
     */
    /*inline*/
    public void play_once() {
        play(false);
    }

    /**
     * Schedule this audio sample to play looped.
     *
     * @see #play
     */
    /*inline*/
    public void play_looped() {
        play(true);
    }

    /**
     * Test if a audio sample is scheduled for playing.
     *
     * @return true if this audio sample is playing, false otherwise.
     */
    /*inline*/
    public boolean is_playing() {
        return _playing;
    }


    /**
     * Set this sample to out-of-range (or not) and
     * Schedule this audio sample to stop (or start) playing.
     */
    /*inline*/ void set_out_of_range(boolean oor/* = true*/) {
        _out_of_range = oor;
        _changed = true;
    }

    /**
     * Test if this sample to out-of-range or not.
     */
    /*inline*/ boolean test_out_of_range() {
        return _out_of_range;
    }

    /**
     * Set the data associated with this audio sample
     * @param data Pointer to a memory block containg this audio sample data.
    This pointer will be set to nullptr after calling this function.
     */
    /*inline*/ /*TODO void set_data( std::unique_ptr<unsigned char, decltype(free)*> data ) {
        _data = std::move(data);
    }*/

    /**
     * Return the data associated with this audio sample.
     * @return A pointer to this sound data of this audio sample.
     */
    /*inline*/ /*TODO unsigned char* get_data() const { return _data.get(); }*/

    /**
     * Free the data associated with this audio sample
     */
    /*inline*/ /*TODO void free_data() { _data = null; }*/

    /**
     * Set the source id of this source
     * FG-DIFF no need to init an audio (done by platform)
     * @param sid source-id
     */
    /*virtual*/  void set_source( int sid) {
        //_source = sid;
        _valid_source = true;
        _changed = true;
    }

    /**
     * Get the source id of this source
     * FG-DIFF no need to maintain an audio source(done by platform)
     * @return source-id
     */
    /*virtual int get_source() {
        return _source;
    }*/

    /**
     * Test if the source-id of this audio sample is usable.
     * FG-DIFF no need to maintain an audio source(done by platform)
     * @return true if the source-id is valid
     */
    /*virtual */public boolean is_valid_source() {
        return _valid_source;
    }

    /**
     * Set the source-id of this audio sample to invalid.
     * FG-DIFF no need to maintain an audio source(done by platform), but needed for clearing flag after stop
     * Apperently it is also effectivly stopping the audio
     */
    /*virtual*/ void no_valid_source() {
        _valid_source = false;
        stopAudio();
    }

    /**
     * Set the buffer-id of this source
     * FG-DIFF no need to maintain an audio source(done by platform)
     * @param bid buffer-id
     */
    /*inline void set_buffer(unsigned int bid) {
        _buffer = bid; _valid_buffer = true; _changed = true;
    }*/

    /**
     * Get the buffer-id of this source
     * FG-DIFF no need to maintain an audio source(done by platform)
     * @return buffer-id
     */
    /*inline unsigned int get_buffer() { return _buffer; }*/

    /**
     * Test if the buffer-id of this audio sample is usable.
     * FG-DIFF no need to maintain an audio source(done by platform)
     *
     * @return true if the buffer-id is valid
     */
    /*inline*/ boolean is_valid_buffer() {
        return audio != null;// _valid_buffer; */
    }

    /**
     * Set the buffer-id of this audio sample to invalid.
     * FG-DIFF no need to maintain an audio source(done by platform)
     */
    /*inline void no_valid_buffer() {
        _valid_buffer = false;
    }*/

    /**
     * Set the playback pitch of this audio sample.
     * Should be between 0.0 and 2.0 for maximum compatibility.
     *
     * @param p Pitch
     */
    /*inline*/ void set_pitch(float p) {
        if (p > 2.0) p = 2.0f;
        else if (p < 0.01) p = 0.01f;
        _pitch = p;
        _changed = true;
    }

    /**
     * Set the master volume of this sample. Should be between 0.0 and 1.0.
     * The final volume is calculated by multiplying the master and audio sample
     * volume.
     *
     * @param v Volume
     */
    /*inline*/ void set_master_volume(float v) {
        if (v > 1.0) v = 1.0f;
        else if (v < 0.0) v = 0.0f;
        _master_volume = v;
        _changed = true;
    }

    /**
     * Set the volume of this audio sample. Should be between 0.0 and 1.0.
     * The final volume is calculated by multiplying the master and audio sample
     * volume.
     *
     * @param v Volume
     */
    /*inline*/ void set_volume(float v) {
        if (v > 1.0) v = 1.0f;
        else if (v < 0.0) v = 0.0f;
        _volume = v;
        _changed = true;
    }

    /**
     * Set the SimGear format of this audio sample.
     * @param format SimGear format-id
     */
    /*inline*/ /*TODO void set_format( int fmt ) {
        _tracks = fmt & 0x3; _bits = fmt & 0x1C; _compressed = fmt & 0x100;
    }*/

    /*TODO
    /*inline* / void set_bits_sample( unsigned int b ) { _bits = b; }
    /*inline* / void set_no_tracks( unsigned int t ) { _tracks = t; }
    /*inline* / void set_compressed( bool c ) { _compressed = c; }
*/

    /**
     * Set the block alignament for compressed audio.
     *
     * @param block the block alignment in bytes
     */
    /*inline*/ void set_block_align(int block) {
        _block_align = block;
    }

    /**
     * Set the frequency (in Herz) of this audio sample.
     *
     * @param freq Frequency
     */
    /*inline*/ void set_frequency(int freq) {
        _frequency = freq;
    }

    /**
     * Sets the size (in bytes) of this audio sample.
     *
     * @param size Data size
     */
    /*inline*/ void set_size(int size) {
        _samples = size * 8 / (_bits * _tracks);
    }

    /*inline*/ void set_no_samples(int samples) {
        _samples = samples;
    }

    /**
     * Set the position of this sound relative to the base position.
     * This is in the same coordinate system as OpenGL; y=up, z=back, x=right.
     *
     * @param pos Relative position of this sound
     */
    /*inline*/ void set_relative_position(Vector3 pos) {
        _relative_pos = /*toVec3d*/(pos);
        _changed = true;
    }

    /**
     * Set the base position in Cartesian coordinates
     *
     * @param pos position in Cartesian coordinates
     */
    /*inline*/ void set_position(Vector3 pos) {
        _base_pos = pos;
        _changed = true;
    }

    /*inline*/ void set_position_properties(SGPropertyNode[] pos) {
        _pos_prop[0] = pos[0];
        _pos_prop[1] = pos[1];
        _pos_prop[2] = pos[2];
        if (pos[0] != null || pos[1] != null || pos[2] != null) _use_pos_props = true;
        _changed = true;
    }

    /**
     * Set the orientation of this sound.
     *
     * @param ori Quaternation containing the orientation information
     */
    /*inline*/ void set_orientation(Quaternion ori) {
        _orientation = ori;
        _changed = true;
    }

    /*inline*/ void set_rotation(Quaternion ec2body) {
        _rotation = ec2body;
        _changed = true;
    }

    /**
     * Set direction of this sound relative to the orientation.
     * This is in the same coordinate system as OpenGL; y=up, z=back, x=right
     *
     * @param dir Sound emission direction
     */
    /*inline*/ void set_direction(Vector3 dir) {
        _direction =/* toVec3d*/(dir);
        _static_changed = true;
    }

    /**
     * Define the audio cone parameters for directional audio.
     * Note: setting it to 2 degree will result in 1 degree to both sides.
     *
     * @param inner Inner cone angle (0 - 360 degrees)
     * @param outer Outer cone angle (0 - 360 degrees)
     * @param gain  Remaining gain at the edge of the outer cone (0.0 - 1.0)
     */
    void set_audio_cone(float inner, float outer, float gain) {
        _inner_angle = inner;
        _outer_angle = outer;
        _outer_gain = gain;
        _static_changed = true;
    }

    /**
     * Set the velocity vector (in meters per second) of this sound.
     * This is in the local frame coordinate system; x=north, y=east, z=down
     *
     * @param vel vector
     */
    /*inline*/ void set_velocity(Vector3 vel) {
        _velocity = vel;
        _changed = true;
    }

    /**
     * Set both the temperature and relative humidity at the current altitude.
     *
     * @param t Temperature in degrees Celsius
     * @param h Percent relative humidity (0.0 to 1.0)
     * @param p Pressure in kPa;
     */
    /*inline*/ void set_atmosphere(float t, float h, float p) {
        if (Math.abs(_degC - t) > 1.0f || Math.abs(_humidity - h) > 0.1f ||
                Math.abs(_pressure - p) > 1.0f) {
            _degC = t;
            _humidity = h;
            _pressure = p;
            _static_changed = true;
        }
    }

    /**
     * Set reference distance (in meters) of this sound.
     * This is the distance where the gain will be half.
     *
     * @param dist Reference distance
     */
    /*inline*/ void set_reference_dist(float dist) {
        _reference_dist = dist;
        _static_changed = true;
    }

    /**
     * Set maximum distance (in meters) of this sound.
     * This is the distance where this sound is no longer audible.
     *
     * @param dist Maximum distance
     */
    /*inline*/ void set_max_dist(float dist) {
        _max_dist = dist;
        _static_changed = true;
    }

    /*inline*/ /*virtual*/ boolean is_queue() {
        return false;
    }

    public void update_pos_and_orientation() {


        /*TODO if (_use_pos_props) {
            if (_pos_prop[0]) _relative_pos[0] = -_pos_prop[0]->getDoubleValue();
            if (_pos_prop[1]) _relative_pos[1] = -_pos_prop[1]->getDoubleValue();
            if (_pos_prop[2]) _relative_pos[2] = -_pos_prop[2]->getDoubleValue();
        }
        _absolute_pos = _base_pos;
        if (_relative_pos[0] || _relative_pos[1] || _relative_pos[2] ) {
            _absolute_pos += _rotation.rotate( _relative_pos );
        }

        _orivec = SGVec3f::zeros();
        if ( _direction[0] || _direction[1] || _direction[2] ) {
            _orivec = toVec3f( _rotation.rotate( _direction ) );
        }*/
    }

    /**
     * Not in FG
     */
    public void pause() {
        logger.warn("not yet");
    }

    public void playAudio() {
        if ("fgdatabasic:Sounds/jet.wav".equals(_refname.toString())) {
            int h = 9;
        }
        if (audio == null) {
            logger.warn("no audio to play");
        } else {
            logger.debug("playing audio clip " + _refname);
            audio.play();
        }
        playedAudio++;
    }

    public void stopAudio() {
        if ("fgdatabasic:Sounds/jet.wav".equals(_refname.toString())) {
            int h = 9;
        }
        if (audio == null) {
            logger.warn("no audio to stop");
        } else {
            logger.debug("stoping audio clip " + _refname);
            audio.stop();
        }
    }

    /**
     * Not about flag "_playing" but effectivyly playing.
     * @return
     */
    public boolean isEffectivelyPlaying(){
     if (audio!=null){
         boolean b= audio.isPlaying();
         return b;
     }
     return false;
    }
}


