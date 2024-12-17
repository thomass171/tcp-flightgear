package de.yard.threed.flightgear.core.simgear.scene.model;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector3;
import de.yard.threed.flightgear.core.simgear.scene.material.OsgTransform;

/**
 * From SGRotateTransform.[hc]xx
 * Cannot be used due to MCRMCB (see README.md).
 * Only contained for documentation.
 * OSG::Transform appears to be a super class of Group
 */
public class SGRotateTransform extends OsgTransform {

    private Vector3 _center, _axis;
    private double _angleRad;

    public SGRotateTransform() {
        /*_center(0, 0, 0),
                _axis(0, 0, 0),
                _angleRad(0)*/

        //?? setReferenceFrame(RELATIVE_RF);

    }

    /**
     * Copy from existing?
     */
    public SGRotateTransform(/*const */ SGRotateTransform existing/*,
                    const osg::CopyOp& copyop = osg::CopyOp::SHALLOW_COPY*/) {
        Util.notyet();
        /*TODO osg::Transform(rot, copyop),
        _center(rot._center),
                _axis(rot._axis),
                _angleRad(rot._angleRad)*/

    }

    //META_Node(simgear, SGRotateTransform);


    void setCenter(Vector3 center) {
        _center = center;
        dirtyBound();
    }

    Vector3 getCenter() {
        return _center;
    }

    void setAxis(Vector3 axis) {
        _axis = axis;
        dirtyBound();
    }

    Vector3 getAxis() {
        return _axis;
    }

    void setAngleRad(double angle) {
        _angleRad = angle;
    }

    double getAngleRad() {
        return _angleRad;
    }

    void setAngleDeg(Degree angle) {
        _angleRad = getAngleRad();
    }

    /*double getAngleDeg() const
    { return SGMiscd::rad2deg(_angleRad); }*/

    /**
     * Replaces FGs computeLocalToWorldMatrix

    @Override
    public boolean updateTransformForAnimation() {
        set_rotation(/*tmp,* / _angleRad, _center, _axis);
        return true;
    }*/

    void set_rotation(/*osg::Matrix &matrix,*/ double position_rad, Vector3 center, Vector3 axis) {

        // WoW, what is happening here in FG? Probably MCRMCB? 29.11.24: Not possible to meet FG with our implementation. (see README.md).
        if (true) throw new RuntimeException("invalid usage");
        Degree angle = Degree.buildFromRadians(position_rad);
        Quaternion rotation = Quaternion.buildQuaternionFromAngleAxis(angle, axis);
        getTransform().setRotation(rotation);
        //moves blades somewhere, but we should set it because it is a property,
        getTransform().setPosition(center);

 /*
 double temp_angle = -position_rad;

  double s = sin(temp_angle);
  double c = cos(temp_angle);
  double t = 1 - c;

  // axis was normalized at load time
  // hint to the compiler to put these into FP registers
  double x = axis[0];
  double y = axis[1];
  double z = axis[2];

  matrix(0, 0) = t * x * x + c ;
  matrix(0, 1) = t * y * x - s * z ;
  matrix(0, 2) = t * z * x + s * y ;
  matrix(0, 3) = 0;

  matrix(1, 0) = t * x * y + s * z ;
  matrix(1, 1) = t * y * y + c ;
  matrix(1, 2) = t * z * y - s * x ;
  matrix(1, 3) = 0;

  matrix(2, 0) = t * x * z - s * y ;
  matrix(2, 1) = t * y * z + s * x ;
  matrix(2, 2) = t * z * z + c ;
  matrix(2, 3) = 0;

  // hint to the compiler to put these into FP registers
  x = center[0];
  y = center[1];
  z = center[2];

  matrix(3, 0) = x - x*matrix(0, 0) - y*matrix(1, 0) - z*matrix(2, 0);
  matrix(3, 1) = y - x*matrix(0, 1) - y*matrix(1, 1) - z*matrix(2, 1);
  matrix(3, 2) = z - x*matrix(0, 2) - y*matrix(1, 2) - z*matrix(2, 2);
  matrix(3, 3) = 1;*/
    }

    /*
    bool
SGRotateTransform::computeLocalToWorldMatrix(osg::Matrix& matrix,
                                             osg::NodeVisitor* nv) const
{
  // This is the fast path, optimize a bit
  if (_referenceFrame == RELATIVE_RF) {
    // FIXME optimize
    osg::Matrix tmp;
    set_rotation(tmp, _angleRad, _center, _axis);
    matrix.preMult(tmp);
  } else {
    osg::Matrix tmp;
    set_rotation(tmp, _angleRad, _center, _axis);
    matrix = tmp;
  }
  return true;
}

     */
    /*
    osg::BoundingSphere
    SGRotateTransform::computeBound() const
    {
        osg::BoundingSphere bs = osg::Group::computeBound();
        osg::BoundingSphere centerbs(toOsg(_center), bs.radius());
        centerbs.expandBy(bs);
        return centerbs;
    }*/

// Functions to read/write SGRotateTransform from/to a .osg file.

    /*namespace {

        bool RotateTransform_readLocalData(osg::Object& obj, osgDB::Input& fr)
        {
            SGRotateTransform& rot = static_cast<SGRotateTransform&>(obj);
            if (fr[0].matchWord("center")) {
                ++fr;
                osg::Vec3d center;
                if (fr.readSequence(center))
                    fr += 3;
                else
                    return false;
                rot.setCenter(toSG(center));
            }
            if (fr[0].matchWord("axis")) {
                ++fr;
                osg::Vec3d axis;
                if (fr.readSequence(axis))
                    fr += 3;
                else
                    return false;
                rot.setCenter(toSG(axis));
            }
            if (fr[0].matchWord("angle")) {
                ++fr;
                double angle;
                if (fr[0].getFloat(angle))
                    ++fr;
                else
                    return false;
                rot.setAngleRad(angle);
            }
            return true;
        }

        bool RotateTransform_writeLocalData(const osg::Object& obj, osgDB::Output& fw)
        {
    const SGRotateTransform& rot = static_cast<const SGRotateTransform&>(obj);
    const SGVec3d& center = rot.getCenter();
    const SGVec3d& axis = rot.getAxis();
    const double angle = rot.getAngleRad();
            int prec = fw.precision();
            fw.precision(15);
            fw.indent() << "center ";
            for (int i = 0; i < 3; i++) {
                fw << center(i) << " ";
            }
            fw << std::endl;
            fw.precision(prec);
            fw.indent() << "axis ";
            for (int i = 0; i < 3; i++) {
                fw << axis(i) << " ";
            }
            fw << std::endl;
            fw.indent() << "angle ";
            fw << angle << std::endl;
            return true;
        }
    }

    osgDB::RegisterDotOsgWrapperProxy g_SGRotateTransformProxy
            (
    new SGRotateTransform,
    "SGRotateTransform",
            "Object Node Transform SGRotateTransform Group",
            &RotateTransform_readLocalData,
    &RotateTransform_writeLocalData
    );*/
}
