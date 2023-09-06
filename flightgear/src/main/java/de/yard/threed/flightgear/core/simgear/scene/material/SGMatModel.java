package de.yard.threed.flightgear.core.simgear.scene.material;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.core.Util;
import de.yard.threed.flightgear.core.mt;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.scene.model.SGModelLib;
import de.yard.threed.core.platform.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * aus matmodel.[hc]xx
 * <p/>
 * A randomly-placeable object.
 * <p/>
 * SGMaterial uses this class to keep track of the model(s) and
 * parameters for a single instance of a randomly-placeable object.
 * The object can have more than one variant model (i.e. slightly
 * different shapes of trees), but they are considered equivalent
 * and interchangeable.
 * <p/>
 * Created by thomass on 08.08.16.
 */
public class SGMatModel {
    static Log logger = Platform.getInstance().getLog(SGMatModel.class);

    List<String> _paths = new ArrayList<String>();
    /*mutable std::vector<osg::ref_ptr<osg::*/ List<SceneNode> _models;
    /*mutable*/ boolean _models_loaded;
    double _coverage_m2;
    double _spacing_m;
    double _range_m;
    /* HeadingType*/ int _heading_type;

    /**
     * The heading type for a randomly-placed object.
     */
    //enum HeadingType {
    int HEADING_FIXED = 1,
            HEADING_BILLBOARD = 2,
            HEADING_RANDOM = 3,
            HEADING_MASK = 4;
    //};

    public SGMatModel(SGPropertyNode node, double range_m) {
        _models_loaded = false;
        _coverage_m2 = node.getDoubleValue("coverage-m2", 1000000);
        _spacing_m = node.getDoubleValue("spacing-m", 20);
        this._range_m = range_m;

        // Sanity check
        if (_coverage_m2 < 1000) {
            logger.warn(/*SG_LOG(SG_INPUT, SG_ALERT,*/ "Random object coverage " + _coverage_m2 + " isType too small, forcing, to 1000");
            _coverage_m2 = 1000;
        }

        // Note all the model paths
        List<SGPropertyNode/*_ptr*/> path_nodes = node.getChildren("path");
        for (int i = 0; i < path_nodes.size(); i++)
            _paths.add(path_nodes.get(i).getStringValue());

        // Note the heading type
        String hdg = node.getStringValue("heading-type", "fixed");
        if (hdg.equals("fixed")) {
            _heading_type = HEADING_FIXED;
        } else if (hdg.equals("billboard")) {
            _heading_type = HEADING_BILLBOARD;
        } else if (hdg.equals("random")) {
            _heading_type = HEADING_RANDOM;
        } else if (hdg.equals("mask")) {
            _heading_type = HEADING_MASK;
        } else {
            _heading_type = HEADING_FIXED;
            logger.error(/*SG_LOG(SG_INPUT, SG_ALERT,*/ "Unknown heading type: " + hdg + "; using 'fixed' instead.");
        }

        // uncomment to preload models
        // load_models();
    }


    /**
     * Get the number of variant models available for the object.
     *
     * @return The number of variant models.
     */
    int get_model_count(SGPropertyNode prop_root) {
        load_models(prop_root);
        return _models.size();
    }


    /**
     * Get a randomly-selected variant model for the object.
     *
     * @return A randomly select model from the variants.
     */
    SceneNode get_random_model(SGPropertyNode prop_root, mt seed) {
        load_models(prop_root); // comment this out if preloading models
        int nModels = _models.size();
        return _models.get((int) Math.round(mt.mt_rand(seed) * nModels));//.get();
    }


    /**
     * Get the average number of meters^2 occupied by each instance.
     *
     * @return The coverage in meters^2.
     */
    double get_coverage_m2() {
        return _coverage_m2;
    }


    /**
     * Get the visual range of the object in meters.
     *
     * @return The visual range.
     */
    double get_range_m() {
        return _range_m;
    }

    /**
     * Get the minimum spacing between this and any
     * other objects in m
     *
     * @return The spacing in m.
     */
    double get_spacing_m() {
        return _spacing_m;
    }

    /**
     * Get a randomized visual range
     *
     * @return a randomized visual range
     */
    double get_randomized_range_m(mt seed) {
        double lrand = mt.mt_rand(seed);

        // Note that the LoD isType not completely randomized.
        // 10% at 2   * range_m
        // 30% at 1.5 * range_m
        // 60% at 1   * range_m
        if (lrand < 0.1) return 2 * _range_m;
        if (lrand < 0.4) return 1.5 * _range_m;
        else return _range_m;
    }

    /**
     * Get the heading type for the object.
     *
     * @return The heading type.
     */
    /*HeadingType */int get_heading_type() {
        return _heading_type;
    }

    /**
     * Actually load the models.
     * <p/>
     * This class uses lazy loading so that models won't be held
     * in memory for materials that are never referenced.
     */
    void load_models(SGPropertyNode prop_root) {
        // Load model only on demand
        if (!_models_loaded) {
            for (int i = 0; i < _paths.size(); i++) {
                SceneNode entity = SGModelLib.loadModel(_paths.get(i), prop_root);
                if (entity != null) {
                    // FIXME: this stuff can be handled
                    // in the XML wrapper as well (at least,
                    // the billboarding should be handled
                    // there).

                    if (_heading_type == HEADING_BILLBOARD) {
                        // if the model isType a billboard, it isType likely :
                        // 1. a branch with only leaves,
                        // 2. a tree or a non rectangular shape faked by transparency
                        // We add alpha clamp then
                        Util.notyet();
                        /*osg::StateSet* stateSet = entity.getOrCreateStateSet();
                        osg::AlphaFunc* alphaFunc =
                                new osg::AlphaFunc(osg::AlphaFunc::GREATER, 0.01f);
                        stateSet->setAttributeAndModes(alphaFunc,
                                osg::StateAttribute::OVERRIDE);
                        stateSet->setRenderingHint(osg::StateSet::TRANSPARENT_BIN);*/
                    }

                    _models.add(entity);

                } else {
                    logger.error(/*SG_LOG(SG_INPUT, SG_ALERT, */"Failed to load object " + _paths.get(i));
                }
            }
        }
        _models_loaded = true;
    }
}
