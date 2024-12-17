package de.yard.threed.flightgear.core.simgear.scene.model;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Util;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.flightgear.core.simgear.props.SGCondition;
import de.yard.threed.flightgear.core.simgear.structure.SGExpression;

/**
 * From animation.[ch]xx
 * Cannot be used due to MCRMCB (see README.md).
 * Only contained for documentation.
 */
public class SGRotAnimTransform extends SGRotateTransform {
    Log logger = Platform.getInstance().getLog(SGRotAnimTransform.class);

    // used when condition is false
    /*mutable double*/ Degree _lastAngle;
    public SGCondition _condition;
    //SGSharedPtr<SGExpressiond const> _animationValue;
    public SGExpression/*d*/ _animationValue;

    public SGRotAnimTransform() {
        _lastAngle = new Degree(0.0);
    }

    SGRotAnimTransform(SGRotAnimTransform existing/*,
                       const osg::CopyOp& copyop = osg::CopyOp::SHALLOW_COPY*/)

    //: SGRotateTransform(rhs, copyop), _condition(rhs._condition),
//_animationValue(rhs._animationValue), _lastAngle(rhs._lastAngle)
    {

        Util.notyet();
        //   META_Node(simgear, SGRotAnimTransform);
    }



        /*


        virtual bool computeLocalToWorldMatrix(osg::Matrix& matrix,
                                           osg::NodeVisitor* nv) const;
    virtual bool computeWorldToLocalMatrix(osg::Matrix& matrix,
                                           osg::NodeVisitor* nv) const;*/


    /**
     * Extend impl in SGRotateTranform
     * was in FG:
     */
    /*
   bool SGRotAnimTransform::computeLocalToWorldMatrix(osg::Matrix& matrix,
     * osg::NodeVisitor* nv) const
            /*double angle = 0.0;
                if (!_condition || _condition->test()) {
            angle = _animationValue->getValue();
            _lastAngle = angle;
                } else {
            angle = _lastAngle;
                }
            double angleRad = SGMiscd::deg2rad(angle);*/


            /* if (_referenceFrame == RELATIVE_RF) {
// FIXME optimize
osg::Matrix tmp;
set_rotation(tmp, angleRad, getCenter(), getAxis());
        matrix.preMult(tmp);
    } else {
osg::Matrix tmp;
SGRotateTransform::set_rotation(tmp, angleRad, getCenter(), getAxis());
matrix = tmp;
    }
            */



            /*
bool SGRotAnimTransform::computeWorldToLocalMatrix(osg::Matrix& matrix,
                                                   osg::NodeVisitor* nv) const
        {
double angle = 0.0;
    if (!_condition || _condition->test()) {
angle = _animationValue->getValue();
_lastAngle = angle;
    } else {
angle = _lastAngle;
    }
double angleRad = SGMiscd::deg2rad(angle);
        if (_referenceFrame == RELATIVE_RF) {
// FIXME optimize
osg::Matrix tmp;
set_rotation(tmp, -angleRad, getCenter(), getAxis());
        matrix.postMult(tmp);
    } else {
osg::Matrix tmp;
set_rotation(tmp, -angleRad, getCenter(), getAxis());
matrix = tmp;
    }
            return true;
            }*/
}
