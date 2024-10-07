package de.yard.threed.flightgear.core.simgear.scene.model;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.platform.NativeCollision;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Ray;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.core.Vector3;
import de.yard.threed.flightgear.core.osg.Group;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.math.SGLimitsd;
import de.yard.threed.flightgear.core.simgear.props.SGCondition;
import de.yard.threed.flightgear.core.simgear.structure.PrimitiveValue;
import de.yard.threed.flightgear.core.simgear.structure.SGConstExpression;
import de.yard.threed.flightgear.core.simgear.structure.SGExpression;
import de.yard.threed.engine.platform.common.RequestHandler;

import java.util.List;

/**
 * animation.[hc]xx
 * <p>
 * Created by thomass on 28.12.16.
 */
public class SGRotateAnimation extends SGAnimation {
    SGCondition _condition;
    SGExpression/*d*/ _animationValue;
    Vector3 _axis;
    Vector3 _center;
    double _initialValue;
    boolean _isSpin;
    public Group rotategroup;
    // for testing
    public Degree lastUsedAngle = null;

    public SGRotateAnimation(SGPropertyNode configNode, SGPropertyNode modelRoot) {
        super(configNode, modelRoot);

        String type = configNode.getStringValue("type", "");
        _isSpin = (type.equals("spin"));

        _condition = getCondition();
        SGExpression/*d*/ value;
        value = read_value(configNode, modelRoot, "-deg", -SGLimitsd.max, SGLimitsd.max);
        if (value == null) {
            // eg. when there is no property tree
            logger.warn("cannot resolve expression. Using 0.");
            value = new SGConstExpression(new PrimitiveValue(0));
        }
        _animationValue = value.simplify();
        if (_animationValue != null)
            _initialValue = _animationValue.getValue(null).doubleVal;
        else
            _initialValue = 0;

        Vector3[] centerandaxis = readRotationCenterAndAxis();
        _center = centerandaxis[0];
        _axis = centerandaxis[1];
    }

    @Override
    public void process(List<NativeCollision> pickingrayintersections, RequestHandler requestHandler) {
        //TODO parts missing, like condition
        if (rotategroup != null) {
            double speed = _animationValue.getValue(null).doubleVal;
            /*if (_animationValue instanceof SGInterpTableExpression /*&& speed > 0.0001f* /) {
                logger.debug("process: " + _animationValue + "=" + speed);
            }*/
            //rotategroup.getTransform().rotateOnAxis(_axis, new Degree(3));
            lastUsedAngle = new Degree((float) speed);
            Quaternion rotation = Quaternion.buildQuaternionFromAngleAxis(lastUsedAngle, _axis);
            //logger.debug("process rotation="+rotation+",speed="+speed);
            rotategroup.getTransform().setRotation(rotation);
        }
    }

    @Override
    public AnimationGroup/*SceneNode*/ createAnimationGroup(/*Group*/SceneNode parent) {
        if (_isSpin) {
            //TODO really spin. 5.10.17:ist das sowas wie das Winrad? Das auch rotiert, wenn sich die property(wins-speed) nicht ändert.
            AnimationGroup translategroupo = AnimationGroup.buildAnimationGroupForRotation(parent, _center);
            //5.10.17 wegen Abstraktion 
            rotategroup = translategroupo.rotategroup;
           /* translategroupo.getTransform().setPosition(_center.negate());

            rotategroup = new Group();
            rotategroup.setName("Spin Animation Group");
            translategroupo.attach/*addChild* /(rotategroup);
            parent.attach(translategroupo);*/
            return translategroupo;
            /*SGRotateTransform* transform = new SGRotateTransform;
            transform->setName("spin setRotateStatus animation");
            SpinAnimCallback* cc;
            cc = new SpinAnimCallback(_condition, _animationValue, _initialValue);
            transform->setCullCallback(cc);
            transform->setCenter(_center);
            transform->setAxis(_axis);
            transform->setAngleDeg(_initialValue);
            parent.addChild(transform);
            return transform;*/
        } else {
            /*FG-DIFF SGRotAnimTransform * transform = new SGRotAnimTransform();
            transform -> setName("setRotateStatus animation");
            transform -> _condition = _condition;
            transform -> _animationValue = _animationValue;
            transform -> _lastAngle = _initialValue;
            transform -> setCenter(_center);
            transform -> setAxis(_axis);
            parent.addChild(transform);
            return transform;*/
            // Ob in SGRotAnimTransform bzw. SGRotateTransform Achsen getauscht werden?? Spooky!
            AnimationGroup translategroupo = AnimationGroup.buildAnimationGroupForRotation(parent, _center);
            rotategroup = translategroupo.rotategroup;

/*            rotategroup = new Group();
           // Group centergroup = new Group();
            rotategroup.getTransform().setPosition(_center.negate());
           // centergroup.attach/*addChild* /(rotategroup);
            rotategroup.setName("Rotate Animation Group");
            parent.attach/*addChild* /(rotategroup);*/
            return translategroupo;//rotategroup;
        }
        //return null;
    }
}


////////////////////////////////////////////////////////////////////////
// Implementation of setRotateStatus/spin animation
////////////////////////////////////////////////////////////////////////

/*class SGRotAnimTransform extends SGRotateTransform {
    public :

    SGRotAnimTransform();

    SGRotAnimTransform(const SGRotAnimTransform&,
                       const osg::CopyOp&copyop=osg::CopyOp::SHALLOW_COPY);

    META_Node(simgear, SGRotAnimTransform);

    virtual bool

    computeLocalToWorldMatrix(osg::Matrix&matrix,
                              osg::NodeVisitor*nv)

    const;
    virtual bool

    computeWorldToLocalMatrix(osg::Matrix&matrix,
                              osg::NodeVisitor*nv)

    const;
    SGSharedPtr<SGCondition const>_condition;
    SGSharedPtr<SGExpressiond const>_animationValue;
    // used when condition isType false
    mutable
    double _lastAngle;
};

SGRotAnimTransform::SGRotAnimTransform()
        :_lastAngle(0.0)
        {
        }

        SGRotAnimTransform::SGRotAnimTransform(const SGRotAnimTransform&rhs,
        const osg::CopyOp&copyop)
        :SGRotateTransform(rhs,copyop),_condition(rhs._condition),
        _animationValue(rhs._animationValue),_lastAngle(rhs._lastAngle)
        {
        }

        bool SGRotAnimTransform::computeLocalToWorldMatrix(osg::Matrix&matrix,
        osg::NodeVisitor*nv)const
        {
        double angle=0.0;
        if(!_condition||_condition->test()){
        angle=_animationValue->getValue();
        _lastAngle=angle;
        }else{
        angle=_lastAngle;
        }
        double angleRad=SGMiscd::deg2rad(angle);
        if(_referenceFrame==RELATIVE_RF){
        // FIXME optimize
        osg::Matrix tmp;
        set_rotation(tmp,angleRad,getCenter(),getAxis());
        matrix.preMult(tmp);
        }else{
        osg::Matrix tmp;
        SGRotateTransform::set_rotation(tmp,angleRad,getCenter(),getAxis());
        matrix=tmp;
        }
        return true;
        }

        bool SGRotAnimTransform::computeWorldToLocalMatrix(osg::Matrix&matrix,
        osg::NodeVisitor*nv)const
        {
        double angle=0.0;
        if(!_condition||_condition->test()){
        angle=_animationValue->getValue();
        _lastAngle=angle;
        }else{
        angle=_lastAngle;
        }
        double angleRad=SGMiscd::deg2rad(angle);
        if(_referenceFrame==RELATIVE_RF){
        // FIXME optimize
        osg::Matrix tmp;
        set_rotation(tmp,-angleRad,getCenter(),getAxis());
        matrix.postMult(tmp);
        }else{
        osg::Matrix tmp;
        set_rotation(tmp,-angleRad,getCenter(),getAxis());
        matrix=tmp;
        }
        return true;
        }
        }*/