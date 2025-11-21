package de.yard.threed.flightgear.core.simgear.scene.model;

import de.yard.threed.core.Pair;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.NativeCollision;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.platform.common.RequestHandler;
import de.yard.threed.flightgear.core.osg.Group;
import de.yard.threed.flightgear.core.osg.Node;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.math.SGLimitsd;
import de.yard.threed.flightgear.core.simgear.props.SGCondition;
import de.yard.threed.flightgear.core.simgear.scene.util.SGTransientModelData;
import de.yard.threed.flightgear.core.simgear.structure.SGExpression;

import java.util.List;

/**
 * From animation.[hc]xx
 * <p>
 * Created by thomass on 02.12.24.
 */
public class SGTranslateAnimation extends SGAnimation {
    /*SGSharedPtr<const SGCondition> _condition;
  SGSharedPtr<const SGExpressiond> _animationValue;
  SGVec3d _axis;
  double _initialValue;*/
    SGExpression _animationValue;
    SGCondition _condition;
    Vector3 _axis;
    double _initialValue;
    // The node where the translate finally applies. TODO might be multiple like in SelectAnimation
    public Group translateGroup;
    boolean noTranslateGroupLogged = false;

    public SGTranslateAnimation(SGTransientModelData modelData, String label) {
        super(modelData, label);

        _condition = getCondition();

        SGExpression value;
        value = read_value(_configNode, _modelRoot, "-m",
                -SGLimitsd.max, SGLimitsd.max);
        _animationValue = value.simplify();
        if (_animationValue != null)
            _initialValue = _animationValue.getValue(null).doubleVal;
        else
            _initialValue = 0;

        /*
        SGVec3d _center;
        if (modelData.getNode() && !setCenterAndAxisFromObject(modelData.getNode(), _center, _axis, modelData))
            _axis = readTranslateAxis(configNode);*/
        //SGVec3d _center;
        Pair<Vector3, Vector3> centerAndAxis = new Pair<>(null, null);
        if (modelData.getXmlNode() != null) {
            if (!setCenterAndAxisFromObject(modelData.getXmlNode(), centerAndAxis/*_center, _axis*/, modelData)) {
                _axis = readTranslateAxis(getConfigNode());
            } else {
                _axis = centerAndAxis.getSecond();
            }
        }
    }

    /*@Override
    protected void apply(Node xmlNodeOfCurrentModel) {
        super.apply(xmlNodeOfCurrentModel);
    }*/

    @Override
    public void process(List<NativeCollision> pickingrayintersections, RequestHandler requestHandler) {

        if (translateGroup == null) {
            if (!noTranslateGroupLogged) {
                logger.warn("No translateGroup in " + getConfigNode().getPath());
                noTranslateGroupLogged = true;
            }
            return;
        }
        if (_condition == null || _condition.test()) {
            double value = _animationValue.getValue(null).doubleVal;

            //logger.debug("process: translation(" + _animationValue + ")=" + value);

            translateGroup.getTransform().setPosition(_axis.multiply(value));
        }
    }

    /**
     * Should be possible to use only one node like in FG where
     * scale and center applies.
     * TODO check: do we need to renormalize normals like FG
     */
    @Override
    public AnimationGroup/*SceneNode*/ createAnimationGroup(/*Group*/SceneNode parent) {

        /* SGTranslateTransform* transform = new SGTranslateTransform;
  transform->setName("translate animation");
  if (_animationValue && !_animationValue->isConst()) {
    UpdateCallback* uc = new UpdateCallback(_condition, _animationValue);
    transform->setUpdateCallback(uc);
  }
  transform->setAxis(_axis);
  transform->setValue(_initialValue);
  parent.addChild(transform);
  return transform;*/

        translateGroup = new Group();
        translateGroup.setName("translateAnimation");
        /*TODO check constant if (_animationValue && !_animationValue->isConst()) {
            UpdateCallback* uc = new UpdateCallback(_condition, _animationValue);
            transform->setUpdateCallback(uc);
        }*/

        AnimationGroup ag = new AnimationGroup(parent, translateGroup);
        return ag;
    }

    public SGExpression getAnimationValueExpression() {
        return _animationValue;
    }

    public Vector3 getAxis() {
        return _axis;
    }
}

