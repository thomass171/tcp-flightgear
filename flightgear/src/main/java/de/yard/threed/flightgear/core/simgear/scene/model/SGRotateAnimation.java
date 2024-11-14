package de.yard.threed.flightgear.core.simgear.scene.model;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.platform.NativeCollision;
import de.yard.threed.engine.Scene;
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
    // used when condition is false. Not for spin rotations, public for testing
    public Degree lastAngle = new Degree(0);

    public SGRotateAnimation(SGPropertyNode configNode, SGPropertyNode modelRoot, String label) {
        super(configNode, modelRoot, label);

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

        if (!_isSpin) {
            lastAngle = new Degree((float) _initialValue);
        }
    }

    @Override
    public void process(List<NativeCollision> pickingrayintersections, RequestHandler requestHandler) {
        //TODO parts missing, like condition
        if (rotategroup != null) {
            double value = _animationValue.getValue(null).doubleVal;
            /*if (_animationValue instanceof SGInterpTableExpression /*&& speed > 0.0001f* /) {
                logger.debug("process: " + _animationValue + "=" + speed);
            }*/
            //rotategroup.getTransform().rotateOnAxis(_axis, new Degree(3));
            Quaternion rotation = new Quaternion();
            if (_isSpin) {
                // in FG done by 'cc = new SpinAnimCallback(_condition, _animationValue, _initialValue);' via cullCallback
                // TODO check condition
                // TODO read tag 'starting-pos-deg'
                // From SpinAnimCallback:

                if (_condition == null || _condition.test()) {
                    double t = Scene.getCurrent().getDeltaTime();// nv -> getFrameStamp()->getSimulationTime();
                    double rps = value / 60.0;
                    /*ref_ptr<ReferenceValues>
                    refval(static_cast < ReferenceValues * > (_referenceValues.get()));
                    if (!refval || refval -> _rotVelocity != rps) {
                        ref_ptr<ReferenceValues> newref;
                        if (!refval.valid()) {
                            // initialization
                            newref = new ReferenceValues(t, 0.0, rps);
                        } else {
                            double newRot = refval -> _rotation + (t - refval -> _time) * refval -> _rotVelocity;
                            newref = new ReferenceValues(t, newRot, rps);
                        }
                        // increment reference pointer, because it will be stored
                        // naked in _referenceValues.
                        newref -> ref();
                        if (_referenceValues.assign(newref, refval)) {
                            if (refval.valid()) {
                                DeletionManager::instance () -> addStaleObject(refval.get());
                                refval -> unref();
                            }
                        } else {
                            // Another thread installed new values before us
                            newref -> unref();
                        }
                        // Whatever happened, we can use the reference values just
                        // calculated.
                        refval = newref;
                    }*/
                    /*double rotation = refval -> _rotation + (t - refval -> _time) * rps;
                    double intPart;
                    double rot = modf(rotation, & intPart);
                    double angle = rot * 2.0 * osg::PI;*/
                    Degree angle = Degree.buildFromRadians(value * t * rps);
                    //logger.debug(label + ":Spinning by diff angle " + angle + ",value=" + value + ",t=" + t);
                    // FG seems to apply the diff of rotation somehow. Also add the angle to existing rotation.
                    rotation = rotategroup.getTransform().getRotation().multiply(Quaternion.buildQuaternionFromAngleAxis(angle, _axis));
       /* const SGVec3d& sgcenter = transform->getCenter();
        const SGVec3d& sgaxis = transform->getAxis();
                    Matrixd mat = Matrixd::translate(-sgcenter[0], -sgcenter[1], -sgcenter[2])
            * Matrixd::rotate(angle, sgaxis[0], sgaxis[1], sgaxis[2])
            * Matrixd::translate(sgcenter[0], sgcenter[1], sgcenter[2])
            * *cv->getModelViewMatrix();
                    ref_ptr<RefMatrix> refmat = new RefMatrix(mat);
                    cv->pushModelViewMatrix(refmat.get(), transform->getReferenceFrame());
                    traverse(transform, nv);
                    cv->popModelViewMatrix();*/
                }
            } else {
                Degree angle;
                if (_condition == null || _condition.test()) {
                    angle = new Degree((float) value);
                    lastAngle = angle;
                    //logger.debug(label + ":Rotating to angle " + angle + ",value=" + value);
                } else {
                    angle = lastAngle;
                }
                rotation = Quaternion.buildQuaternionFromAngleAxis(angle, _axis);
                //logger.debug("process rotation="+rotation+",speed="+speed);
            }
            rotategroup.getTransform().setRotation(rotation);

        }
    }

    @Override
    public AnimationGroup/*SceneNode*/ createAnimationGroup(/*Group*/SceneNode parent) {
        if (_isSpin) {
            //TODO really spin.
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

    /**
     * 4.11.24: Helpful for testing
     */
    public PrimitiveValue getAnimationValue() {
        return _animationValue.getValue(null);
    }

    public boolean isSpin() {
        return _isSpin;
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