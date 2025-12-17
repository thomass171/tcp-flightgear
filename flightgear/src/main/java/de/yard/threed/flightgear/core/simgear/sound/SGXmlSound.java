package de.yard.threed.flightgear.core.simgear.sound;

import de.yard.threed.core.GeneralFunction;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.props.SGCondition;
import de.yard.threed.flightgear.core.simgear.structure.SGExpression;
import de.yard.threed.flightgear.ecs.FgAnimationUpdateSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.yard.threed.flightgear.core.simgear.math.SGRandom.sg_random;
import static de.yard.threed.flightgear.core.simgear.props.SGCondition.sgReadCondition;
import static de.yard.threed.flightgear.core.simgear.structure.SGExpression.SGReadDoubleExpression;

/**
 * From xmlsound.[hc]xx
 * Class for handling one sound event.
 * <p>
 * This class handles everything for a particular sound event, by
 * scanning an a pre-loaded property tree structure for sound
 * settings, setting up its internal states, and managing sound
 * playback whenever such an event happens.
 */
public class SGXmlSound {
    Log logger = Platform.getInstance().getLog(SGXmlSound.class);

    static int MAXPROP = 5;
    //enum { ONCE=0, LOOPED, IN_TRANSIT };
    static int MODE_ONCE = 0;
    static int MODE_LOOPED = 1;
    static int MODE_IN_TRANSIT = 2;
    //enum { LEVEL=0, INVERTED, FLIPFLOP };

    double MAX_TRANSIT_TIME = 0.1;     // 100 ms.

    //using _fn_t = std::function<double(double)>;
    //using _snd_prop = struct {
    class _snd_prop {
        /*SGSharedPtr<*/ SGExpression/*d*/ expr;    // sound system version 2.0
        /*std::shared_ptr<*/ Double intern;
        SGPropertyNode prop;
        /*_fn_t*/ GeneralFunction<Double, Double> fn;
        double factor;
        double offset;
        double min;
        double max;
        boolean subtract;

        public _snd_prop(SGExpression expr, Double intern, SGPropertyNode prop, GeneralFunction<Double, Double> fn, double factor, double offset, double min, double max, boolean subtract) {
            this.expr = expr;
            this.intern = intern;
            this.prop = prop;
            this.fn = fn;

            this.factor = factor;
            this.offset = offset;

            this.min = min;
            this.max = max;
            this.subtract = subtract;

        }
    }

    ;

    //using _sound_fn_t = std::map <std::string, _fn_t>;
    /*_sound_fn_t*/ Map<String, GeneralFunction<Double, Double>> _sound_fn = new HashMap<>();


    /*SGSharedPtr<*/ SGSampleGroup _sgrp;
    /*SGSharedPtr<*/ SGSoundSample _sample;

    /*SGSharedPtr<*/ SGCondition _condition;
    SGPropertyNode _property;

    boolean _active;
    float _version;
    String _name;
    int _mode;
    double _prev_value;
    double _dt_play;
    double _dt_stop;
    double _delay;        // time after which the sound should be started (default: 0)
    double _stopping;     // time after the sound should have stopped.
    // This is useful for lost packets in in-transit mode.

    // sound system version 1.0
    List<_snd_prop> _volume = new ArrayList<>();
    List<_snd_prop> _pitch = new ArrayList<>();

    /**
     * Initialize the sound event.
     * <p>
     * Prior to initialization of the sound event the program's property root
     * has to be defined, the sound configuration XML tree has to be loaded
     * and a sound manager class has to be defined.
     * <p>
     * A sound configuration file would look like this:
     *
     * @param root     The root node of the programs property tree.
     * @param node     A pointer to the location of the current event as
     *                 defined in the configuration file.
     * @param sgrp     A pointer to a pre-initialized sample group class.
     * @param avionics A pointer to the pre-initialized avionics sample group.
     * @param path     The path where the audio files remain.
     * @code{xml} <fx>
     * <event_name>
     * <name/> Define the name of the event. For reference only.
     * <mode/> Either:
     * looped: play this sound looped.
     * in-transit: play looped while the event is happening.
     * once: play this sound once.
     * <path/> The relative path to the audio file.
     * <property/> Take action if this property becomes true.
     * <condition/> Take action if this condition becomes true.
     * <delay-sec/> Time after which the sound should be played.
     * <volume> or <pitch> Define volume or pitch settings.
     * <property/> Take the value of this property as a reference for the
     * result.
     * <internal/> Either:
     * dt_start: the time elapsed since this sound is playing.
     * dt_stop: the time elapsed since this sound has stopped.
     * <offset/> Add this value to the result.
     * <factor/> Multiply the result by this factor.
     * <min/> Make sure the value is never less than this value.
     * <max/> Make sure the value is never larger than this value.
     * </volume> or </pitch>
     * </event_name>
     *
     * <event_name>
     * </event_name>
     * </fx>
     * @endcode
     */
    /*virtual*/
    public boolean init(SGPropertyNode root,
                        SGPropertyNode node,
                        SGSampleGroup sgrp,
                        SGSampleGroup avionics,
                        BundleResource/*const SGPath&*/ path) {

        //
        // set global sound properties
        //

        _name = node.getStringValue("name", "");
        logger.info("Loading sound information for: " + _name);

        String mode_str = node.getStringValue("mode", "");
        if (mode_str.equals("looped")) {
            _mode = MODE_LOOPED;

        } else if (mode_str.equals("in-transit")) {
            _mode = MODE_IN_TRANSIT;

        } else {
            _mode = MODE_ONCE;
        }

        boolean is_avionics = false;
        String type_str = node.getStringValue("type", "fx");
        if (type_str.equals("avionics"))
            is_avionics = true;

        String propval = node.getStringValue("property", "");
        if (!propval.equals("")) {
            _property = root.getNode(propval, true);
        }
        SGPropertyNode condition = node.getChild("condition");
        if (condition != null)
            _condition = sgReadCondition(root, condition);

        if (_property == null && _condition == null) {
            //TODO 
            Util.notyet();
            //simgear::reportFailure(simgear::LoadFailure::Misconfigured, simgear::ErrorCode::AudioFX,
            //      "SGXmlSound: node:" + _name, path);
        }

        _delay = node.getDoubleValue("delay-sec", 0.0);

        //
        // set volume properties
        //
        int i;
        float v = 0.0f;
        List<SGPropertyNode> kids = node.getChildren("volume");
        for (i = 0; (i < kids.size()) && (i < MAXPROP); i++) {
            _snd_prop volume = new _snd_prop(null, null, null, null, 1.0, 0.0, 0.0, 0.0, false);

            SGPropertyNode n = kids.get(i).getChild("expression");
            if (n != null) {
                volume.expr = SGReadDoubleExpression(root, n.getChild(0));
            }

            propval = kids.get(i).getStringValue("property", "");
            if (propval != "")
                volume.prop = root.getNode(propval, true);

            String intern_str = kids.get(i).getStringValue("internal", "");
            if (intern_str.equals("dt_play"))
                volume.intern = /*std::make_shared<double>*/(_dt_play);
            else if (intern_str.equals("dt_stop"))
                volume.intern = /*std::make_shared<double>*/(_dt_stop);

            if ((volume.factor = kids.get(i).getDoubleValue("factor", 1.0)) != 0.0)
                if (volume.factor < 0.0) {
                    volume.factor = -volume.factor;
                    volume.subtract = true;
                }

            /*String*/
            type_str = kids.get(i).getStringValue("type", "");
            if (!type_str.equals("") && !type_str.equals("lin")) {

                GeneralFunction<Double, Double> it = _sound_fn.get(type_str);
                if (it != null/*_sound_fn.end()*/) {
                    volume.fn = it/*.second*/;
                }

                if (volume.fn == null)
                    logger.warn("  Unknown volume type, default to 'lin'" + " in section: " + _name);
            }

            volume.offset = kids.get(i).getDoubleValue("offset", 0.0);

            if ((volume.min = kids.get(i).getDoubleValue("min", 0.0)) < 0.0)
                logger.warn(
                        "Volume minimum value below 0. Forced to 0" +
                                " in section: " + _name);

            volume.max = kids.get(i).getDoubleValue("max", 0.0);
            if (volume.max != 0.0 && (volume.max < volume.min))
                logger.warn("  Volume maximum below minimum. Neglected.");

            _volume.add(volume);
            v += volume.offset;

        }

        // rule of thumb: make reference distance a 100th of the maximum distance.
        double reference_dist = node.getDoubleValue("reference-dist", 60.0);
        double max_dist = node.getDoubleValue("max-dist", 6000.0);

        //
        // set pitch properties
        //
        double p = 0.0;
        kids = node.getChildren("pitch");
        for (i = 0; (i < kids.size()) && (i < MAXPROP); i++) {
            _snd_prop pitch = new _snd_prop(null, null, null, null, 1.0, 1.0, 0.0, 0.0, false);

            double randomness = kids.get(i).getDoubleValue("random", 0.0);
            randomness *= sg_random();

            SGPropertyNode n = kids.get(i).getChild("expression");
            if (n != null) {
                pitch.expr = SGReadDoubleExpression(root, n.getChild(0));
            }

            propval = kids.get(i).getStringValue("property", "");
            if (propval != "")
                pitch.prop = root.getNode(propval, true);

            String intern_str = kids.get(i).getStringValue("internal", "");
            if (intern_str.equals("dt_play"))
                pitch.intern = /*std::make_shared<double>*/(_dt_play);
            else if (intern_str.equals("dt_stop"))
                pitch.intern = /*std::make_shared<double>*/(_dt_stop);

            if ((pitch.factor = kids.get(i).getDoubleValue("factor", 1.0)) != 0.0)
                if (pitch.factor < 0.0) {
                    pitch.factor = -pitch.factor;
                    pitch.subtract = true;
                }

            /*String*/
            type_str = kids.get(i).getStringValue("type", "");
            if (type_str != "" && type_str != "lin") {

                GeneralFunction<Double, Double> it = _sound_fn.get(type_str);
                if (it != null/*_sound_fn.end()*/) {
                    pitch.fn = it/*.second*/;
                }

                if (pitch.fn == null)
                    logger.warn("  Unknown pitch type, default to 'lin'" + " in section: " + _name);
            }

            pitch.offset = kids.get(i).getDoubleValue("offset", 1.0);
            pitch.offset += randomness;

            if ((pitch.min = kids.get(i).getDoubleValue("min", 0.0)) < 0.0)
                logger.warn(
                        "  Pitch minimum value below 0. Forced to 0" +
                                " in section: " + _name);

            pitch.max = kids.get(i).getDoubleValue("max", 0.0);
            if (pitch.max != 0.0 && (pitch.max < pitch.min))
                logger.warn(
                        "  Pitch maximum below minimum. Neglected" +
                                " in section: " + _name);

            _pitch.add(pitch);
            p += pitch.offset;
        }

        //
        // Relative position
        //
        Vector3 offset_pos = new Vector3();//SGVec3f::zeros();
        SGPropertyNode prop = node.getChild("position");
        SGPropertyNode[] pos_prop = new SGPropertyNode[3];
        if (prop != null) {
            offset_pos/*[0]*/ = new Vector3(-prop.getDoubleValue("x", 0.0),
                    /*offset_pos[1] =*/ -prop.getDoubleValue("y", 0.0),
                    /*offset_pos[2] =*/ -prop.getDoubleValue("z", 0.0));

            pos_prop[0] = prop.getChild("x");
            if (pos_prop[0] != null) pos_prop[0] = pos_prop[0].getNode("property");
            if (pos_prop[0] != null) {
                pos_prop[0] = root.getNode(pos_prop[0].getStringValue(), true);
            }
            pos_prop[1] = prop.getChild("y");
            if (pos_prop[1] != null) pos_prop[1] = pos_prop[1].getNode("property");
            if (pos_prop[1] != null) {
                pos_prop[1] = root.getNode(pos_prop[1].getStringValue(), true);
            }
            pos_prop[2] = prop.getChild("z");
            if (pos_prop[2] != null) pos_prop[2] = pos_prop[2].getNode("property");
            if (pos_prop[2] != null) {
                pos_prop[2] = root.getNode(pos_prop[2].getStringValue(), true);
            }
        }

        //
        // Orientation
        //
        Vector3 dir = new Vector3();//SGVec3f::zeros();
        float inner = 360.0f;
        float outer = 360.0f;
        float outer_gain = 0.0f;
        prop = node.getChild("orientation");
        if (prop != null) {
            dir = new Vector3(-prop.getFloatValue("x", 0.0f),
                    -prop.getFloatValue("y", 0.0f),
                    -prop.getFloatValue("z", 0.0f));
            inner = prop.getFloatValue("inner-angle", 360.0f);
            outer = prop.getFloatValue("outer-angle", 360.0f);
            outer_gain = prop.getFloatValue("outer-gain", 0.0f);
        }

        //
        // Initialize the sample
        //
        if ((is_avionics) && (avionics != null)) {
            _sgrp = avionics;
        } else {
            _sgrp = sgrp;
        }
        String soundFileStr = node.getStringValue("path", "");
        _sample = new SGSoundSample(soundFileStr/*.c_str()*/, path);
        /*TODO if (!_sample.file_path().exists()) {
            //simgear::reportFailure(simgear::LoadFailure::NotFound, simgear::ErrorCode::AudioFX,
            logger.error("SGXmlSound: node:" + _name + "; can't find:" + soundFileStr + path);
            return false;
        }*/

        _sample.set_relative_position(offset_pos);
        _sample.set_position_properties(pos_prop);
        _sample.set_direction(dir);
        _sample.set_audio_cone(inner, outer, outer_gain);
        _sample.set_reference_dist((float) reference_dist);
        _sample.set_max_dist((float) max_dist);
        _sample.set_volume(v);
        _sample.set_pitch((float) p);
        _sgrp.add(_sample, _name);

        return true;

    }

    /**
     * Check whether an event has happened and if action has to be taken.
     */
    /*virtual*/
    public void update(double dt) {
        //
        // Sound trigger condition:
        // - if a condition is defined, test it.
        // - for mode IN_TRANSIT, check that the property changed
        // - otherwise just check the property
        //
        boolean condition = false;
        if (_condition != null)
            condition = _condition.test();
        else if (_property != null) {
            if (_mode == MODE_IN_TRANSIT) {
                double curr_value = _property.getDoubleValue();
                condition = (curr_value != _prev_value);
                _prev_value = curr_value;
            } else {
                if ("rumble".equals(_name)) {
                    int h = 9;
                }

                condition = _property.getBoolValue();
            }
        }

        if (!condition) {
            if ((_mode != MODE_IN_TRANSIT) || (_stopping > MAX_TRANSIT_TIME)) {
                if (_sample.is_playing()) {
                    logger.debug("Stopping audio after " + _dt_play + " sec: " + _name);
                    _sample.stop();
                }

                _active = false;
                _dt_stop += dt;
                _dt_play = 0.0;
            } else {
                _stopping += dt;
            }

            return;
        }

        if ("rumble".equals(_name)) {
            int h = 9;
        }
        if (_sample.test_out_of_range()) {
            // When out of range, sample is not played, but logic
            // (playing condition, _dt_*) is updated as if we were playing it.
            //
            // Exception: for mode ONCE, sample immediately finishes when going out of range
            // (even if the sample is long, you can not go out of range, then back in range to hear the end of it).

            if (_sample.is_playing()) {
                logger.debug("Stopping audio after " + _dt_play + " sec: " + _name + " (out of range)");
                _sample.stop();
            }
        }

        //
        // mode is ONCE and the sound is still playing?
        //
        if (_active && (_mode == MODE_ONCE)) {

            if (!_sample.is_playing()) {
                _dt_stop += dt;
                _dt_play = 0.0;
            } else {
                _dt_play += dt;
            }

        } else {

            //
            // Update the playing time, cache the current value and
            // clear the delay timer.
            //
            _dt_play += dt;
            _stopping = 0.0;
        }

        if (_dt_play < _delay)
            return;

        //
        // Do we need to start playing the sample?
        //
        if (!_active) {

            if (!_sample.test_out_of_range()) {
                if (_mode == MODE_ONCE)
                    _sample.play_once();
                else
                    _sample.play_looped();

                logger.debug("Playing audio after " + _dt_stop + " sec: " + _name);
                logger.debug("Playing " + ((_mode == MODE_ONCE) ? "once" : "looped"));
            }

            _active = true;
            _dt_stop = 0.0;
        }

        // Remark: at this point of the function, _active is always true
        // The sample might not be playing if
        // - it was played once and stopped, or
        // - it went out of range

        //
        // If a looped sample stopped playing but is still active,
        // it can only be because it went out of range. If needed, restart it.
        //
        if (!_sample.is_playing() && _mode != MODE_ONCE && !_sample.test_out_of_range()) {
            _sample.play_looped();

            logger.debug("Restarting sample (was out of range): " + _name);
        }

        //
        // Change sample state
        //
        if (_sample.is_playing()) {
            _sample.set_volume((float) volume());
            _sample.set_pitch((float) pitch());
        }
    }


    /**
     * Compute sample volume
     */
    double volume() {
        int max = _volume.size();
        double volume = 1.0;
        double volume_offset = 0.0;
        boolean expr = false;

        for (int i = 0; i < max; i++) {
            double v = 1.0;

            if (_volume.get(i).expr != null) {
                double expression_value = _volume.get(i).expr.getValue(null).doubleVal;
                if (expression_value >= 0)
                    volume *= expression_value;
                expr = true;
                continue;
            }

            if (_volume.get(i).prop != null) {
                // do not process if there was an expression defined
                if (expr) continue;

                v = _volume.get(i).prop.getDoubleValue();
            } else if (_volume.get(i).intern != null/*0.0*/) {
                // intern sections always get processed.
                //v = *_volume.get(i).intern;
                v *= _volume.get(i).intern;
            }

            if (_volume.get(i).fn != null)
                v = _volume.get(i).fn.handle(v);

            v *= _volume.get(i).factor;

            if (_volume.get(i).max != 0.0 && (v > _volume.get(i).max))
                v = _volume.get(i).max;
            else if (v < _volume.get(i).min)
                v = _volume.get(i).min;


            if (_volume.get(i).subtract) {                // Hack!
                v = v + _volume.get(i).offset;
                if (v >= 0)
                    volume = volume * v;
            } else {
                if (v >= 0) {
                    volume_offset += _volume.get(i).offset;
                    volume = volume * v;
                }
            }
        }

        double vol = volume_offset + volume;
        if (vol > 1.0) {
            logger.debug("Sound volume too large for '" + _name + "':  " + vol + "  .  clipping to 1.0");
            vol = 1.0;
        }

        return vol;

    }

    /**
     * Compute sample pitch
     */
    double pitch() {
        int max = _pitch.size();
        double pitch = 1.0;
        double pitch_offset = 0.0;
        boolean expr = false;

        for (int i = 0; i < max; i++) {
            double p = 1.0;

            if (_pitch.get(i).expr != null) {
                pitch *= _pitch.get(i).expr.getValue(null).doubleVal;
                expr = true;
                continue;
            }

            if (_pitch.get(i).prop != null) {
                // do not process if there was an expression defined
                if (expr) continue;

                p = _pitch.get(i).prop.getDoubleValue();
            } else if (_pitch.get(i).intern != null/*0.0*/) {
                // intern sections always get processed.
                //p = * _pitch.get(i).intern;
                p *= _pitch.get(i).intern;
            }

            if (_pitch.get(i).fn != null) {
                p = _pitch.get(i).fn.handle(p);
            }
            p *= _pitch.get(i).factor;

            if (_pitch.get(i).max != 0.0 && (p > _pitch.get(i).max))
                p = _pitch.get(i).max;

            else if (p < _pitch.get(i).min)
                p = _pitch.get(i).min;

            if (_pitch.get(i).subtract)                // Hack!
                pitch = _pitch.get(i).offset - p;

            else {
                pitch_offset += _pitch.get(i).offset;
                pitch *= p;
            }
        }

        return pitch_offset + pitch;
    }

    /**
     * Start taking action on the pre-defined events.
     */
    public void start() {
        if (_property != null) _prev_value = _property.getDoubleValue();
        _active = false;
    }

    /**
     * Stop taking action on the pre-defined events.
     */
    /*TODO?? void stop();*/

}
