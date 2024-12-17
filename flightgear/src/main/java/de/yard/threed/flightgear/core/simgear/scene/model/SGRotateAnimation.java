package de.yard.threed.flightgear.core.simgear.scene.model;

import de.yard.threed.core.Degree;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.Pair;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Util;
import de.yard.threed.core.platform.NativeCollision;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.core.Vector3;
import de.yard.threed.flightgear.core.osg.Group;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.math.SGLimitsd;
import de.yard.threed.flightgear.core.simgear.props.SGCondition;
import de.yard.threed.flightgear.core.simgear.scene.util.SGTransientModelData;
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
    // The node where the rotation finally applies
    public Group rotategroup;

    // used when condition is false. Not for spin rotations, public for testing
    public Degree lastAngle = new Degree(0);
    // for spinning like in FG
    ReferenceValues refval = null;

    public SGRotateAnimation(SGTransientModelData modelData, String label) {
        super(modelData, label);

        String type = _configNode.getStringValue("type", "");
        _isSpin = (type.equals("spin"));

        _condition = getCondition();
        SGExpression/*d*/ value;
        value = read_value(_configNode, _modelRoot, "-deg", -SGLimitsd.max, SGLimitsd.max);
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

        Pair<Vector3, Vector3> centerAndAxis = new Pair<>(null, null);
        readRotationCenterAndAxis(modelData.getXmlNode(), centerAndAxis, modelData);
        _center = centerAndAxis.getFirst();
        _axis = centerAndAxis.getSecond();

        if (!_isSpin) {
            lastAngle = new Degree((float) _initialValue);
        }
    }

    public Vector3 getAxis() {
        return _axis;
    }

    public Vector3 getCenter() {
        return _center;
    }

    @Override
    public void process(List<NativeCollision> pickingrayintersections, RequestHandler requestHandler) {

        if (rotategroup == null) {
            // TODO fix this (bluebird)
            logger.warn("No rotategroup in " + getConfigNode().getPath());
            return;
        }
        //23.11.24 if (rotategroup != null) {
        double value = _animationValue.getValue(null).doubleVal;
            /*if (_animationValue instanceof SGInterpTableExpression /*&& speed > 0.0001f* /) {
                logger.debug("process: " + _animationValue + "=" + speed);
            }*/
        //rotategroup.getTransform().rotateOnAxis(_axis, new Degree(3));
        Quaternion rotation = new Quaternion();
        if (_isSpin) {
            // in FG done by 'cc = new SpinAnimCallback(_condition, _animationValue, _initialValue);' via cullCallback
            // TODO read tag 'starting-pos-deg'
            // From SpinAnimCallback:
            // EffectCullVisitor* cv = dynamic_cast<EffectCullVisitor*>(nv);
            //  if (!cv)
            //        return;
            if (_condition == null || _condition.test()) {
                // From SpinAnimCallback.cxx
                // FG seems to use absolute time and later calculates delta in ReferenceValues().
                // So get delta immediately. "ReferenceValues" also contains last value?
                // 4.12.24: Prefer delta becasue we have it thats the typical use case for movement (also for smoothness?)
                double deltaTime = Scene.getCurrent().getDeltaTime();// nv -> getFrameStamp()->getSimulationTime();
                //double t = Platform.getInstance().currentTimeMillis();// nv -> getFrameStamp()->getSimulationTime();

                refval = calcSpinAngle(refval, value, deltaTime);
                // 5.12.24: Conversion to Degree might break fluent movement??
                //Degree angle = Degree.buildFromRadians(value * t * rps);
                //logger.debug(label + ":Spinning by diff angle " + refval.angle + ",value=" + value + ",t=" + deltaTime);
                // FG seems to apply the diff of rotation somehow (4.12.24 really? What is refval._rotation?). Also add the angle to existing rotation.
                // 5.12.24: But abs values seem to work better. And are better for testing.
                //rotation = rotategroup.getTransform().getRotation().multiply(Quaternion.buildQuaternionFromAngleAxis(angle, _axis));
                rotation = Quaternion.buildQuaternionFromAngleAxis(refval.angle, _axis);
                rotategroup.getTransform().setRotation(rotation);

//windturbine.xml.2:Spinning by diff
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
            //logger.debug("process rotation="+rotation+",speed="+speed);

            //logger.debug(rotategroup.dump("---",1));
            boolean analyze = false;
            if (analyze) {
                // analyzer code
                if (rotategroup.getTransform().getChild(0).getChild(0).getSceneNode().getName().equals("5kt")) {
                    if (angle.getDegree() != 30.0) {
                        logger.debug("Fixing 5kt angle " + angle + " to 30");
                        angle = new Degree(30);
                    }
                    logger.debug("process rotation,angle=" + angle);
                    rotation = Quaternion.buildQuaternionFromAngleAxis(angle, _axis);
                    rotategroup.getTransform().setRotation(rotation);
                /*staticdeg+=5;
                if (staticdeg > 0){
                    staticdeg=-90;
                }*/
                }
            } else {
                rotation = Quaternion.buildQuaternionFromAngleAxis(angle, _axis);
                rotategroup.getTransform().setRotation(rotation);
            }
        }
        //}
    }

    static double staticdeg = -90.0;

    /**
     * Extracted for better testing.
     * FG-DIFF: FG seems to update rotation, but we calculate abs angle. FGs logic isn't intuitive at all.
     */
    public static ReferenceValues calcSpinAngle(ReferenceValues refval, double value, double deltaTime) {
        double rps = value / 60.0;
           /*ref_ptr<ReferenceValues>
                    refval(static_cast < ReferenceValues * > (_referenceValues.get()));*/
        //if (!refval || refval -> _rotVelocity != rps) {
        if (refval == null || refval._rotVelocity != rps) {
            /*ref_ptr<*/
            ReferenceValues newref;
            if (refval == null/*!refval.valid()*/) {
                // initialization
                newref = new ReferenceValues(/*t,*/ 0.0, rps);
            } else {
                double newRot = refval._rotation + (deltaTime/*t - refval._time*/) * refval._rotVelocity;
                newref = new ReferenceValues(/*t,*/ newRot, rps);
            }
            // increment reference pointer, because it will be stored
            // naked in _referenceValues.
                        /*newref -> ref();
                        if (_referenceValues.assign(newref, refval)) {
                            if (?!?refval.valid()) {
                               DeletionManager::instance () -> addStaleObject(refval.get());
                                refval -> unref();
                                refval = newref;
                            }
                        } else {
                            // Another thread installed new values before us
                            newref -> unref();
                        }*/
            // Whatever happened, we can use the reference values just
            // calculated.
            refval = newref;
        }
        double rotation1 = refval._rotation + (deltaTime/*t - refval._time*/) * rps;
        double intPart;
        // modf() Decomposes given floating point value num into integral and fractional parts. Now idea why it is used in FG.
        double rot = rotation1;//modf(rotation1, & intPart);
        refval._rotation = rotation1;
        //double angle = rot * 2.0 * MathUtil2.PI;
        refval.angle = Degree.buildFromRadians(rot);
        return refval;
    }

    /**
     * Due to MCRMCB we cannot use the FG way with SGRotateTransform and SGRotAnimTransform.
     *
     * @param parent The scene node to which the animation applies. Cannot be type Group because we have no such type.
     * @return
     */
    @Override
    public AnimationGroup/*SceneNode*/ createAnimationGroup(/*Group*/SceneNode parent) {
        if (_isSpin) {
            AnimationGroup translategroupo = buildAnimationGroupForRotation(parent, _center, "spinRotateAnimation");
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
            // FG uses SGRotAnimTransform for translation and axis in one. We split it into two/three.
            /*FG-DIFF SGRotAnimTransform *transform = new SGRotAnimTransform();
            transform -> setName("setRotateStatus animation");
            transform -> _condition = _condition;
            transform -> _animationValue = _animationValue;
            transform -> _lastAngle = _initialValue;
            transform -> setCenter(_center);
            transform -> setAxis(_axis);
            parent.addChild(transform);
            return transform;*/
            // Ob in SGRotAnimTransform bzw. SGRotateTransform Achsen getauscht werden?? Spooky!
            AnimationGroup translategroupo = buildAnimationGroupForRotation(parent, _center, "rotateAnimation");
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

    public SGExpression getAnimationValueExpression() {
        return _animationValue;
    }

    public boolean isSpin() {
        return _isSpin;
    }

    /**
     * 20.11.24 Moved here from AnimationGroup
     * Build MCRMCB hierarchy. Avoid blanks in names for better log/debug readability.
     * Aufbau: parent->translategroup->rotategroup->childtarget
     *
     * @param parent
     * @param c
     * @return
     */
    private AnimationGroup buildAnimationGroupForRotation(SceneNode parent, Vector3 c, String rotateGroupNameLikeFG) {

        Group translategroupo = new Group();
        translategroupo.setName("centerBackTranslate");
        translategroupo.getTransform().setPosition(c);

        AnimationGroup ag = new AnimationGroup(parent, translategroupo);

        ag.childtarget = new SceneNode();
        ag.childtarget.setName("centerTranslate");
        // negate center like FG does(to move element to origin?)
        ag.childtarget.getTransform().setPosition(c.negate());

        ag.rotategroup = new Group();
        ag.rotategroup.setName(rotateGroupNameLikeFG);
        ag.rotategroup.attach/*addChild*/(ag.childtarget);
        translategroupo.attach/*addChild*/(ag.rotategroup);
        return ag;
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

    /**
     * From FG
     */
    public static class ReferenceValues {
        // 4.12.24: Prefer delta public double _time;
        // No idea what "_rotation" is. Is it just a start value? It is never updated.
        public double _rotation;
        // And what is _rotVelocity? Probably an previous rbs value.
        public double _rotVelocity;
        public Degree angle;

        public ReferenceValues(/*double t,*/ double rot, double vel) {
            // 4.12.24: Prefer delta _time = (t);
            _rotation = (rot);
            _rotVelocity = (vel);
            angle = new Degree(0);
        }
    }
}
