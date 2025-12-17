package de.yard.threed.flightgear.core.flightgear.sound;

import de.yard.threed.core.StringUtils;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.flightgear.FgBundleHelper;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.props.PropsIO;
import de.yard.threed.flightgear.core.simgear.sound.SGSampleGroup;
import de.yard.threed.flightgear.core.simgear.sound.SGXmlSound;
import de.yard.threed.flightgear.core.simgear.structure.SGException;

import java.util.ArrayList;
import java.util.List;

import static de.yard.threed.flightgear.core.flightgear.main.FGGlobals.globals;
import static de.yard.threed.flightgear.core.flightgear.main.FGProperties.*;

/**
 * From fg_fx.[ch]xx
 * <p>
 * Generator for FlightGear model sound effects.
 * <p>
 * This module uses a FGSampleGroup class to generate sound effects based
 * on current flight conditions. The sound manager must be initialized
 * before this object is.
 * <p>
 * This module will load and play a set of sound effects defined in an
 * xml file and tie them to various property states.
 */
public class FGFX extends SGSampleGroup {
    Log logger = Platform.getInstance().getLog(FGFX.class);

    boolean _active;
    boolean _is_aimodel;
    /*SGSharedPtr<*/ SGSampleGroup _avionics;
    /*SGSharedPtr*/ SGSampleGroup _atc;

    List<SGXmlSound> _sound = new ArrayList<>();

    SGPropertyNode _props;
    public SGPropertyNode _enabled;
    SGPropertyNode _volume;
    SGPropertyNode _avionics_enabled;
    SGPropertyNode _avionics_volume;
    SGPropertyNode _avionics_ext;
    SGPropertyNode _internal;
    SGPropertyNode _atc_enabled;
    SGPropertyNode _atc_volume;
    SGPropertyNode _atc_ext;
    SGPropertyNode _machwave_active;
    SGPropertyNode _machwave_volume;

    public FGFX(String refname, SGPropertyNode props /*= 0*/) {
        if (props == null) {
            _is_aimodel = false;
            _props = globals.get_props();
            _enabled = fgGetNode("/sim/sound/effects/enabled", true);
            _volume = fgGetNode("/sim/sound/effects/volume", true);
        } else {
            _is_aimodel = true;
            _enabled = _props.getNode("/sim/sound/aimodels/enabled", true);
            _enabled.setBoolValue(fgGetBool("/sim/sound/effects/enabled"));
            _volume = _props.getNode("/sim/sound/aimodels/volume", true);
            _volume.setFloatValue(fgGetFloat("/sim/sound/effects/volume", 0.0f));
        }

        _avionics_enabled = _props.getNode("sim/sound/avionics/enabled", true);
        _avionics_volume = _props.getNode("sim/sound/avionics/volume", true);
        _avionics_ext = _props.getNode("sim/sound/avionics/external-view", true);
        _internal = _props.getNode("sim/current-view/internal", true);

        _atc_enabled = _props.getNode("sim/sound/atc/enabled", true);
        _atc_volume = _props.getNode("sim/sound/atc/volume", true);
        _atc_ext = _props.getNode("sim/sound/atc/external-view", true);

        _machwave_active = _props.getNode("sim/sound/machwave/active", true);
        _machwave_volume = _props.getNode("sim/sound/machwave/offset-m", true);

        _smgr = globals.sgSoundMgr;//get_subsystem<FGSoundManager>();
        if (_smgr == null) {
            return;
        }
        _active = _smgr.is_active();

        _refname = refname;
        _smgr.add(this, refname);

        if (!_is_aimodel) // only for the main aircraft
        {
            _avionics = _smgr.find("avionics", true);
            _avionics.tie_to_listener();

            _atc = _smgr.find("atc", true);
            _atc.tie_to_listener();
        }

    }
    //virtual ~FGFX ();

    public void init() {
        if (_smgr == null) {
            return;
        }

        // In FG the property "sim/sound/path" has value 'c172-sound.xml' when c172 was loaded.
        SGPropertyNode node = _props.getNode("sim/sound", true);

        String path_str = node.getStringValue("path");
        if (StringUtils.empty(path_str)) {
            logger.error("No path in sim/sound/path");
            return;
        }

        //SGPath path = globals.resolve_aircraft_path(path_str);
        BundleResource path = FgBundleHelper.findPath(path_str, null);
        if (path == null/*.isNull()*/) {
            //simgear::reportFailure (simgear::LoadFailure::NotFound, simgear::ErrorCode::AudioFX,
            //        "Failed to find FX XML file:" + path_str, sg_location {                path_str            });
            logger.error("File not found: '" + path_str);
            return;
        }
        logger.info("Reading sound '" + node.getNameString() + "' from " + path);

        SGPropertyNode root = new SGPropertyNode();
        try {
            new PropsIO().readProperties(path, root);
        } catch (SGException e) {
            // simgear::reportFailure (simgear::LoadFailure::BadData, simgear::ErrorCode::AudioFX,
            logger.error("Failure loading FX XML:" + e.getMessage() /*, e.getLocation()*/);
            return;
        }

        node = root.getNode("fx");
        if (node != null) {
            for (int i = 0; i < node.nChildren(); ++i) {
                /*std::unique_ptr < */
                SGXmlSound soundfx = new SGXmlSound();

                // try {
                boolean ok = soundfx.init(_props, node.getChild(i), this, _avionics,
                        path/*TODO path? .dir()*/);
                if (ok) {
                    // take the pointer out of the unique ptr so it's not deleted
                    _sound.add(soundfx/*.release()*/);
                }
               /*not thrown } catch (SGException e ){
                    logger.error( e.getMessage()/*FormattedMessage()* /);
                    //simgear::reportFailure (simgear::LoadFailure::BadData, simgear::ErrorCode::AudioFX,
                     //       "Failure creating Audio FX:" + e.getFormattedMessage(), path);
                }*/
            }
        }
    }

    void reinit() {
        super./*SGSampleGroup::*/stop();
        /*TODO std::for_each (_sound.begin(), _sound.end(), [](const SGXmlSound * snd){
            delete snd;
        });*/
        _sound.clear();
        init();
        super./*SGSampleGroup::*/resume();
    }

    @Override
    public void update(double dt) {
        if (_smgr == null) {
            return;
        }

        if (!_active && _smgr.is_active()) {
            _active = true;
            for (int i = 0; i < _sound.size(); i++) {
                _sound.get(i).start();
            }
        }


        if (_enabled.getBoolValue()) {
            if (_avionics != null) {
                boolean e = _avionics_enabled.getBoolValue();
                if (e && (_avionics_ext.getBoolValue() || _internal.getBoolValue())) {
                    // avionics sound is enabled
                    _avionics.resume(); // no-op if already in resumed state
                    _avionics.set_volume(_avionics_volume.getFloatValue());
                } else
                    _avionics.suspend();
            }

            if (_atc != null) {
                boolean e = _atc_enabled.getBoolValue();
                if (e && (_atc_ext.getBoolValue() || _internal.getBoolValue())) {
                    // ATC sound is enabled
                    _atc.resume(); // no-op if already in resumed state
                    _atc.set_volume(_atc_volume.getFloatValue());
                } else
                    _atc.suspend();
            }

            _machwave_active.setBoolValue(_mInCone);
            _machwave_volume.setFloatValue(_mOffset_m);

            set_volume((float) _volume.getDoubleValue());
            resume();

            // update sound effects if not paused
            for (int i = 0; i < _sound.size(); i++) {
                _sound.get(i).update(dt);
            }

            super./*SGSampleGroup::*/update(dt);
        } else
            suspend();
    }

    void unbind() {
        if (_smgr != null) {
            _smgr.remove(_refname);
        }

        // because SGXmlSound has an owning ref back to us, we need to
        // clear these here, or we will never get destroyed
       /*TODO  std::for_each (_sound.begin(), _sound.end(), [](const SGXmlSound * snd){
            delete snd;
        });*/
        _sound.clear();
    }
}
