package de.yard.threed.flightgear.core.simgear.sound;

import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;

/**
 * manages everything we need to know for an individual audio sample
 */

class SGSoundSampleInfo
{
   /* public:
    SGSoundSampleInfo();
    virtual ~SGSoundSampleInfo() {}
*/
    /**
     * Returns the format of this audio sample.
     * @return SimGear format-id
     */
    /*inline*/  int get_format() {
    return (_tracks | _bits /*TODO| _compressed*256*/);
}

    /**
     * Returns the block alignment of this audio sample.
     * @return block alignment in bytes
     */
    /*inline*/  int get_block_align() {
    return _block_align;
}

    /**
     * Get the reference name of this audio sample.
     * @return Sample name
     */
    /*inline*/ String get_sample_name()  { return _refname.getFullName(); }

    /**
     * Returns the frequency (in Herz) of this audio sample.
     * @return Frequency
     */
    /*inline*/  int get_frequency() { return _frequency; }

    /**
     * Get the current pitch value of this audio sample.
     * @return Pitch
     */
    /*inline*/ float get_pitch() { return _pitch; }

    /**
     * Get the final volume value of this audio sample.
     * @return Volume
     */
    /*inline*/ float get_volume() { return _volume * _master_volume; }

    /**
     * Returns the size (in bytes) of this audio sample.
     * @return Data size
     */
    /*inline*/ int get_size()  {
    return (_samples * _tracks * _bits)/8;
}
    /*inline*/ int get_no_samples() { return _samples; }
    /*inline*/ int get_no_tracks() { return _tracks; }


    /**
     * Get the absolute position of this sound.
     * This is in the same coordinate system as OpenGL; y=up, z=back, x=right.
     * @return Absolute position
     */
    /*inline*/ Vector3 get_position() { return _absolute_pos; }

    /**
     * Get the orientation vector of this sound.
     * This is in the same coordinate system as OpenGL; y=up, z=back, x=right
     * @return Orientaton vector
     */
    /*inline*/ Vector3 get_orientation() { return _orivec; }

    /**
     * Get the inner angle of the audio cone.
     * @return Inner angle in degrees
     */
    /*inline*/ float get_innerangle() { return _inner_angle; }

    /**
     * Get the outer angle of the audio cone.
     * @return Outer angle in degrees
     */
    /*inline*/ float get_outerangle() { return _outer_angle; }

    /**
     * Get the remaining gain at the edge of the outer cone.
     * @return Gain
     */
    /*inline*/ float get_outergain() { return _outer_gain; }

    /**
     * Get velocity vector (in meters per second) of this sound.
     * This is in the same coordinate system as OpenGL; y=up, z=back, x=right
     * @return Velocity vector
     */
    /*inline*/ Vector3 get_velocity() { return _velocity; }

    /**
     * Get reference distance ((in meters) of this sound.
     * This is the distance where the gain will be half.
     * @return Reference distance
     */
    /*inline*/ float get_reference_dist() { return _reference_dist; }

    /**
     * Get maximum distance (in meters) of this sound.
     * This is the distance where this sound is no longer audible.
     * @return Maximum distance
     */
    /*inline*/ float get_max_dist() { return _max_dist; }

    /**
     * Get the temperature (in degrees Celsius) at the current altitude.
     * @return temperature in degrees Celsius
     */
    /*inline*/ float get_temperature() { return _degC; }

    /**
     * Get the relative humidity at the current altitude.
     * @return Percent relative humidity (0.0 to 1.0)
     */
    /*inline*/ float get_humidity() { return _humidity; }

    /**
     * Get the pressure at the current altitude.
     * @return Pressure in kPa
     */
    /*inline*/ float get_pressure() { return _pressure; }

    /**
     * Test if static data of audio sample configuration has changed.
     * Calling this function will reset the flag so calling it a second
     * time in a row will return false.
     * @return Return true is the static data has changed in the mean time.
     */
    boolean has_static_data_changed() {
        boolean b = _static_changed;
        _static_changed = false;
        return b;
    }

    //protected:
    // static sound emitter info
    /*String*/ BundleResource _refname;
     int _bits = 16;
     int _tracks = 1;
     int _samples = 0;
     int _frequency = 22500;
     int _block_align = 2;
    boolean _compressed = false;
    boolean _loop = false;

    // dynamic sound emitter info (non 3d)
    boolean _static_changed = true;
    boolean _playing = false;

    float _pitch = 1.0f;
    float _volume = 1.0f;
    float _master_volume = 1.0f;

    // dynamic sound emitter info (3d)
    boolean _use_pos_props = false;
    boolean _out_of_range = false;

    float _inner_angle = 360.0f;
    float _outer_angle = 360.0f;
    float _outer_gain = 0.0f;

    float _reference_dist = 500.0f;
    float _max_dist = 3000.0f;
    float _pressure = 101.325f;
    float _humidity = 0.5f;
    float _degC = 20.0f;

    SGPropertyNode _pos_prop[] = new SGPropertyNode[3];
    Vector3 _absolute_pos = new Vector3();	// absolute position
    Vector3 _relative_pos;	// position relative to the base position
    Vector3 _direction;		// orientation offset
    Vector3 _velocity;		// Velocity of the source sound.

    // The position and orientation of this sound
    Quaternion _orientation;	// base orientation
    Vector3 _orivec;		// orientation vector
    Vector3 _base_pos;		// base position

    Quaternion _rotation;

    //String random_string();
}

