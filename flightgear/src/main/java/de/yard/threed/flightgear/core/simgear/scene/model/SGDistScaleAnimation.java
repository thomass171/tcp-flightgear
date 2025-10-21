package de.yard.threed.flightgear.core.simgear.scene.model;

import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.NativeCollision;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.platform.common.RequestHandler;
import de.yard.threed.flightgear.core.osg.Group;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.math.SGInterpTable;
import de.yard.threed.flightgear.core.simgear.math.SGLimitsd;
import de.yard.threed.flightgear.core.simgear.props.SGCondition;
import de.yard.threed.flightgear.core.simgear.scene.util.SGTransientModelData;
import de.yard.threed.flightgear.core.simgear.structure.SGClipExpression;
import de.yard.threed.flightgear.core.simgear.structure.SGExpression;
import de.yard.threed.flightgear.core.simgear.structure.SGInterpTableExpression;

import java.util.List;

import static de.yard.threed.flightgear.core.simgear.scene.model.SGAnimation.read_interpolation_table;

/**
 * From animation.[hc]xx
 * Only a draft for now
 * <p>
 * Created by thomass on 20.10.25.
 */
public class SGDistScaleAnimation extends SGAnimation {

    /*SGSharedPtr<*/SGInterpTable _table;
    Vector3 _center;
    double _min_v;
    double _max_v;
    double _factor;
    double _offset;
    Group transformGroup;

    public SGDistScaleAnimation(SGTransientModelData modelData, String label) {
        super(modelData, label);

        //setName(configNode.getStringValue("name", "dist scale animation"));
        //setReferenceFrame(RELATIVE_RF);
        //setStateSet(getNormalizeStateSet());
        _factor = _configNode.getDoubleValue("factor", 1);
        _offset = _configNode.getDoubleValue("offset", 0);
        _min_v = _configNode.getDoubleValue("min", 0.0/*SGLimitsd::epsilon()*/);
        _max_v = _configNode.getDoubleValue("max", SGLimitsd.max);
        _table = read_interpolation_table(_configNode);
        _center = new Vector3(_configNode.getDoubleValue("center/x-m", 0),
         _configNode.getDoubleValue("center/y-m", 0),
         _configNode.getDoubleValue("center/z-m", 0));
    }

    @Override
    public void process(List<NativeCollision> pickingrayintersections, RequestHandler requestHandler) {
        //TODO

    }

    /**
     */
    @Override
    public AnimationGroup/*SceneNode*/ createAnimationGroup(/*Group*/SceneNode parent) {

        //Transform* transform = new Transform(getConfig());
        transformGroup = new Group();
        //parent.addChild(transform);
        transformGroup.getTransform().setParent(parent.getTransform());
        //return transform;

        transformGroup.setName("transformGroup");
        AnimationGroup ag = new AnimationGroup(parent, transformGroup);
        return ag;
    }

   /* private:
    double computeScaleFactor(osg::NodeVisitor* nv) const
    {
        if (!nv)
            return 1;

        double scale_factor = (toOsg(_center) - nv->getEyePoint()).length();
        if (_table == 0) {
            scale_factor = _factor * scale_factor + _offset;
        } else {
            scale_factor = _table->interpolate( scale_factor );
        }
        if (scale_factor < _min_v)
            scale_factor = _min_v;
        if (scale_factor > _max_v)
            scale_factor = _max_v;

        return scale_factor;
    }*/


}


////////////////////////////////////////////////////////////////////////
// Implementation of dist scale animation
////////////////////////////////////////////////////////////////////////

class SGDistScaleAnimationTransform {
    //SGDistScaleAnimation::Transform : public osg::Transform {
    //Transform() : _min_v(0.0), _max_v(0.0), _factor(0.0), _offset(0.0) {}
    /*Transform(const Transform& rhs,
            const osg::CopyOp& copyOp = osg::CopyOp::SHALLOW_COPY)
    : osg::Transform(rhs, copyOp), _table(rhs._table), _center(rhs._center),
            _min_v(rhs._min_v), _max_v(rhs._max_v), _factor(rhs._factor),
            _offset(rhs._offset)
    {
    }
    META_Node(simgear, SGDistScaleAnimation::Transform);*/
    SGDistScaleAnimationTransform(SGPropertyNode configNode) {

    }
}

    /*virtual bool computeLocalToWorldMatrix(osg::Matrix& matrix,
            osg::NodeVisitor* nv) const
    {
        osg::Matrix transform;
        double scale_factor = computeScaleFactor(nv);
        transform(0,0) = scale_factor;
        transform(1,1) = scale_factor;
        transform(2,2) = scale_factor;
        transform(3,0) = _center[0]*(1 - scale_factor);
        transform(3,1) = _center[1]*(1 - scale_factor);
        transform(3,2) = _center[2]*(1 - scale_factor);
        matrix.preMult(transform);
        return true;
    }

    virtual bool computeWorldToLocalMatrix(osg::Matrix& matrix,
            osg::NodeVisitor* nv) const
    {
        double scale_factor = computeScaleFactor(nv);
        if (fabs(scale_factor) <= SGLimits<double>::min())
        return false;
        osg::Matrix transform;
        double rScaleFactor = 1/scale_factor;
        transform(0,0) = rScaleFactor;
        transform(1,1) = rScaleFactor;
        transform(2,2) = rScaleFactor;
        transform(3,0) = _center[0]*(1 - rScaleFactor);
        transform(3,1) = _center[1]*(1 - rScaleFactor);
        transform(3,2) = _center[2]*(1 - rScaleFactor);
        matrix.postMult(transform);
        return true;
    }*/

    /*static bool writeLocalData(const osg::Object& obj, osgDB::Output& fw)
    {
    const Transform& trans = static_cast<const Transform&>(obj);
        fw.indent() << "center " << trans._center << "\n";
        fw.indent() << "min_v " << trans._min_v << "\n";
        fw.indent() << "max_v " << trans._max_v << "\n";
        fw.indent() << "factor " << trans._factor << "\n";
        fw.indent() << "offset " << trans._offset << "\n";
        return true;
    }*/



/*namespace
{
    osgDB::RegisterDotOsgWrapperProxy distScaleAnimationTransformProxy
        (
                new SGDistScaleAnimation::Transform,
                "SGDistScaleAnimation::Transform",
                "Object Node Transform SGDistScaleAnimation::Transform Group",
                0,
        &SGDistScaleAnimation::Transform::writeLocalData
   );
}*/



