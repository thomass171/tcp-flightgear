package de.yard.threed.flightgear.core.simgear.scene.model;

import de.yard.threed.core.Degree;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.NativeCollision;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.platform.common.RequestHandler;
import de.yard.threed.flightgear.core.osg.Group;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.math.SGInterpTable;
import de.yard.threed.flightgear.core.simgear.math.SGLimitsd;
import de.yard.threed.flightgear.core.simgear.props.SGCondition;
import de.yard.threed.flightgear.core.simgear.scene.util.SGTransientModelData;
import de.yard.threed.flightgear.core.simgear.structure.PrimitiveValue;
import de.yard.threed.flightgear.core.simgear.structure.SGClipExpression;
import de.yard.threed.flightgear.core.simgear.structure.SGConstExpression;
import de.yard.threed.flightgear.core.simgear.structure.SGExpression;
import de.yard.threed.flightgear.core.simgear.structure.SGInterpTableExpression;
import de.yard.threed.flightgear.core.simgear.structure.SGPropertyExpression;

import java.util.List;

/**
 * From animation.[hc]xx
 * <p>
 * Created by thomass on 02.12.24.
 */
public class SGScaleAnimation extends SGAnimation {
    /*SGSharedPtr<const SGCondition> _condition;
    SGSharedPtr<const SGExpressiond> _animationValue[3];
    SGVec3d _initialValue;
    SGVec3d _center;*/
    SGExpression[] _animationValue = new SGExpression[3];
    SGCondition _condition;
    Vector3 _center;
    Vector3 _initialValue;
    // The node where the scale finally applies
    public Group scaleGroup;

    public SGScaleAnimation(SGTransientModelData modelData, String label) {
        super(modelData, label);

        _condition = getCondition();

        // default offset/factor for all directions
        double offset = _configNode/*modelData.getConfigNode()*/.getDoubleValue("offset", 0);
        double factor = _configNode/*modelData.getConfigNode()*/.getDoubleValue("factor", 1);

        SGExpression inPropExpr;

        String inputPropertyName;
        inputPropertyName = _configNode.getStringValue("property", "");
        if (StringUtils.empty(inputPropertyName)) {
            inPropExpr = null;//new SGConstExpression<double>(0);
        } else {
            SGPropertyNode inputProperty;
            //inputProperty = modelData.getModelRoot()->getNode(inputPropertyName, true);
            //inPropExpr = new SGPropertyExpression/*<double>*/(inputProperty);
            inPropExpr = SGAnimation.resolvePropertyValueExpression(inputPropertyName, _modelRoot);
        }

        SGInterpTable interpTable = read_interpolation_table(_configNode);
        if (interpTable != null) {
            SGExpression value;
            value = new SGInterpTableExpression/*<double>*/(inPropExpr, interpTable);
            _animationValue[0] = value.simplify();
            _animationValue[1] = value.simplify();
            _animationValue[2] = value.simplify();
        } else if (_configNode.getBoolValue("use-personality", false)) {
            SGExpression value;
            value = new SGPersonalityScaleOffsetExpression(inPropExpr, _configNode,
                    "x-factor", "x-offset",
                    factor, offset);
            double minClip = _configNode.getDoubleValue("x-min", 0);
            double maxClip = _configNode.getDoubleValue("x-max", SGLimitsd.max);
            value = new SGClipExpression/*<double>*/(value, minClip, maxClip);
            _animationValue[0] = value.simplify();

            value = new SGPersonalityScaleOffsetExpression(inPropExpr, _configNode,
                    "y-factor", "y-offset",
                    factor, offset);
            minClip = _configNode.getDoubleValue("y-min", 0);
            maxClip = _configNode.getDoubleValue("y-max", SGLimitsd.max);
            value = new SGClipExpression/*<double>*/(value, minClip, maxClip);
            _animationValue[1] = value.simplify();

            value = new SGPersonalityScaleOffsetExpression(inPropExpr, _configNode,
                    "z-factor", "z-offset",
                    factor, offset);
            minClip = _configNode.getDoubleValue("z-min", 0);
            maxClip = _configNode.getDoubleValue("z-max", SGLimitsd.max);
            value = new SGClipExpression/*<double>*/(value, minClip, maxClip);
            _animationValue[2] = value.simplify();
        } else {
            SGExpression value;
            value = read_factor_offset(_configNode, inPropExpr, "x-factor", "x-offset");
            double minClip = _configNode.getDoubleValue("x-min", 0);
            double maxClip = _configNode.getDoubleValue("x-max", SGLimitsd.max);
            value = new SGClipExpression/*<double>*/(value, minClip, maxClip);
            _animationValue[0] = value.simplify();

            value = read_factor_offset(_configNode, inPropExpr, "y-factor", "y-offset");
            minClip = _configNode.getDoubleValue("y-min", 0);
            maxClip = _configNode.getDoubleValue("y-max", SGLimitsd.max);
            value = new SGClipExpression/*<double>*/(value, minClip, maxClip);
            _animationValue[1] = value.simplify();

            value = read_factor_offset(_configNode, inPropExpr, "z-factor", "z-offset");
            minClip = _configNode.getDoubleValue("z-min", 0);
            maxClip = _configNode.getDoubleValue("z-max", SGLimitsd.max);
            value = new SGClipExpression/*<double>*/(value, minClip, maxClip);
            _animationValue[2] = value.simplify();
        }
        _initialValue = new Vector3(_configNode.getDoubleValue("x-starting-scale", 1)
                * _configNode.getDoubleValue("x-factor", factor)
                + _configNode.getDoubleValue("x-offset", offset),
                _configNode.getDoubleValue("y-starting-scale", 1)
                        * _configNode.getDoubleValue("y-factor", factor)
                        + _configNode.getDoubleValue("y-offset", offset),
                _configNode.getDoubleValue("z-starting-scale", 1)
                        * _configNode.getDoubleValue("z-factor", factor)
                        + _configNode.getDoubleValue("z-offset", offset));
        _center = new Vector3(_configNode.getDoubleValue("center/x-m", 0),
                _configNode.getDoubleValue("center/y-m", 0),
                _configNode.getDoubleValue("center/z-m", 0));
    }

    @Override
    public void process(List<NativeCollision> pickingrayintersections, RequestHandler requestHandler) {

        if (scaleGroup == null) {
            logger.warn("No scaleGroup in " + getConfigNode().getPath());
            return;
        }
        if (_condition == null || _condition.test()) {

            Vector3 value = new Vector3(
                    _animationValue[0].getValue(null).doubleVal,
                    _animationValue[1].getValue(null).doubleVal,
                    _animationValue[2].getValue(null).doubleVal
            );

            //logger.debug("process: " + _animationValue + "=" + value);

            //TODO center
            scaleGroup.getTransform().setScale(value);
        }
    }

    /**
     * Should be possible to use only one node like in FG where
     * scale and center applies.
     * TODO check: do we need to renormalize normals like FG
     */
    @Override
    public AnimationGroup/*SceneNode*/ createAnimationGroup(/*Group*/SceneNode parent) {

        /*SGScaleTransform* transform = new SGScaleTransform;
        transform->setName("scale animation");
        transform->setCenter(_center);
        transform->setScaleFactor(_initialValue);
        UpdateCallback* uc = new UpdateCallback(_condition, _animationValue);
        transform->setUpdateCallback(uc);
        parent.addChild(transform);
        return transform;*/

        scaleGroup = new Group();
        scaleGroup.setName("scaleAnimation");
        //scaleGroup.getTransform().setPosition(c);

        AnimationGroup ag = new AnimationGroup(parent, scaleGroup);
        return ag;
    }

    public SGExpression getAnimationValueExpression(int index) {
        return _animationValue[index];
    }


}

