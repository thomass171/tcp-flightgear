package de.yard.threed.flightgear.core.simgear.scene.model;

import de.yard.threed.core.Matrix3;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.TreeNodeVisitor;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.NativeCollision;
import de.yard.threed.core.platform.NativeUniform;
import de.yard.threed.core.platform.PlatformHelper;
import de.yard.threed.core.platform.Uniform;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.Transform;
import de.yard.threed.engine.platform.common.RequestHandler;
import de.yard.threed.flightgear.core.osg.Group;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.math.SGInterpTable;
import de.yard.threed.flightgear.core.simgear.math.SGLimitsd;
import de.yard.threed.flightgear.core.simgear.props.SGCondition;
import de.yard.threed.flightgear.core.simgear.scene.util.SGTransientModelData;
import de.yard.threed.flightgear.core.simgear.structure.PrimitiveValue;
import de.yard.threed.flightgear.core.simgear.structure.SGBiasExpression;
import de.yard.threed.flightgear.core.simgear.structure.SGClipExpression;
import de.yard.threed.flightgear.core.simgear.structure.SGConstExpression;
import de.yard.threed.flightgear.core.simgear.structure.SGExpression;
import de.yard.threed.flightgear.core.simgear.structure.SGInterpTableExpression;
import de.yard.threed.flightgear.core.simgear.structure.SGPropertyExpression;
import de.yard.threed.flightgear.core.simgear.structure.SGStepExpression;

import java.util.ArrayList;
import java.util.List;

import static de.yard.threed.flightgear.core.simgear.SGPropertyNode.setValue;

/**
 * FG uses ancient OpenGls TextureMatrix for fixed pipelines. Meanwhile this should be done in shader.
 * (see also README.md)
 */
public class SGTexTransformAnimation extends SGAnimation {

    public UpdateCallback updateCallback;
    AnimationGroup animationGroup;
    // materials to which the animation applies
    public List<Material> materials;
    boolean UniformTEXTUREMATRIXnotfoundLogged = false;
    boolean noAnimationGroupLogged = false;

    abstract class /*SGTexTransformAnimation::*/Transform /*: public SGReferenced*/ {

        double _value = 0;

        /*public:
        Transform() :
        _value(0)
        {}
        virtual ~Transform()
        { }*/
        void setValue(double value) {
            _value = value;
        }

        //virtual

        abstract Matrix3 transform(/*osg::Matrix&) =0*/Matrix3 matrix);

    }

    class Translation extends SGTexTransformAnimation.Transform {
        // FG-DIFF: FG uses Vec3
        Vector2 _axis;

        public Translation(/*const SGVec3d&*/Vector2 axis) {
            _axis = axis;
        }

        //void transform(osg::Matrix&matrix) {
        Matrix3 transform(/*osg::Matrix&) =0*/Matrix3 matrix) {
            Matrix3 tmp = new Matrix3();
            //set_translation(tmp, _value, _axis);
            SGAnimation.set_translation(tmp, _value, _axis);
            //matrix.preMult(tmp);
            return matrix.multiply(tmp);
        }
    }

    /*class SGTexTransformAnimation::Rotation :
    public SGTexTransformAnimation::Transform {
        public:
        Rotation(const SGVec3d& axis, const SGVec3d& center) :
        _axis(axis),
                _center(center)
        { }
        virtual void transform(osg::Matrix& matrix)
        {
            osg::Matrix tmp;
            SGRotateTransform::set_rotation(tmp, SGMiscd::deg2rad(_value), _center,
                _axis);
            matrix.preMult(tmp);
        }
        private:
        SGVec3d _axis;
        SGVec3d _center;
    };*/

    /*class SGTexTransformAnimation::Trapezoid :
    public SGTexTransformAnimation::Transform {
        public:

        enum Side { TOP, RIGHT, BOTTOM, LEFT };

        Trapezoid(Side side):
        _side(side)
        { }
        virtual void transform(osg::Matrix& matrix)
        {
            VGfloat sx0 = 0, sy0 = 0,
                    sx1 = 1, sy1 = 0,
                    sx2 = 0, sy2 = 1,
                    sx3 = 1, sy3 = 1;
            switch( _side )
            {
                case TOP:
                    sx0 -= _value;
                    sx1 += _value;
                    break;
                case RIGHT:
                    sy1 -= _value;
                    sy3 += _value;
                    break;
                case BOTTOM:
                    sx2 -= _value;
                    sx3 += _value;
                    break;
                case LEFT:
                    sy0 -= _value;
                    sy2 += _value;
                    break;
            }
            VGfloat mat[3][3];
            VGUErrorCode err = vguComputeWarpQuadToSquare( sx0, sy0,
                    sx1, sy1,
                    sx2, sy2,
                    sx3, sy3,
                    (VGfloat*)mat );
            if( err != VGU_NO_ERROR )
                return;

            matrix.preMult( osg::Matrix(
                mat[0][0], mat[0][1], 0, mat[0][2],
                mat[1][0], mat[1][1], 0, mat[1][2],
                0,         0, 1,         0,
                mat[2][0], mat[2][1], 0, mat[2][2]
    ));
        }

        protected:
        Side _side;
    };*/

    class Entry {
        /*SGSharedPtr<*/ Transform transform;
        /*SGSharedPtr<const SGExpressiond>*/ SGExpression value;

        public Entry(Transform transform, SGExpression value) {
            this.transform = transform;
            this.value = value;
        }
    }

    class UpdateCallback/* :    public osg::StateAttribute::Callback*/ {
        /*private:
        struct Entry {

        };
        typedef std::vector<Entry> TransformList;
        /*TransformList*/ public List<Entry> _transforms = new ArrayList<>();
        /*SGSharedPtr<const*/ SGCondition _condition;
        /*osg::*/ Matrix3 _matrix = new Matrix3();

        public UpdateCallback(SGCondition condition) {
            this._condition = condition;

            //setName("SGTexTransformAnimation::UpdateCallback");
        }

        //virtual. FG-DIFF: FG is void
        /*void*/
        public Matrix3 operator()/* (osg::StateAttribute*sa,osg::NodeVisitor*)*/ {
            //if (!_condition || _condition -> test()) {
            if (_condition == null || _condition.test()) {
                // Set latest value to add to matrix later?
                // TransformList::const_iterator i;
                //for (i = _transforms.begin(); i != _transforms.end(); ++i)
                for (Entry i : _transforms) {
                    i.transform.setValue(i.value.getValue(null).doubleVal);
                }
            }
            /* assert (dynamic_cast < osg::TexMat * > (sa));
            osg::TexMat * texMat = static_cast < osg::TexMat * > (sa);
            texMat -> getMatrix().makeIdentity();
            TransformList::const_iterator i;*/
            Matrix3 mat = new Matrix3();
            for (Entry i : _transforms) {
                mat = i.transform.transform(mat/*texMat -> getMatrix()*/);
            }
            return mat;
        }

        void appendTransform(Transform transform, /*SGExpressiond* */ SGExpression value) {
            Entry entry = new Entry(transform, value);
            // what is the purpose of this transform? A kind of init? "_matrix" is never used.
            transform.transform(_matrix);
            _transforms.add(entry);
        }
    }

    public SGTexTransformAnimation/*::SGTexTransformAnimation*/(SGTransientModelData modelData, String label) {
        super(modelData, label);
    }

    @Override
    public AnimationGroup createAnimationGroup(/*Group*/SceneNode parent) {
        /*osg::*/
        Group group = new Group();
        group.setName("texture transform group");
        /*osg::StateSet* stateSet = group->getOrCreateStateSet();
        stateSet->setDataVariance(osg::Object::STATIC/*osg::Object::DYNAMIC* /);
        osg::TexMat* texMat = new osg::TexMat;*/
        /*UpdateCallback*/
        updateCallback = new UpdateCallback(getCondition());
        // interpret the configs ...
        String type = getType();

        if (type.equals("textranslate")) {
            appendTexTranslate(getConfig(), updateCallback);
        } else if (type.equals("texrotate")) {
            logger.warn("not yet implemented");
            return null;
            //appendTexRotate(*getConfig(), updateCallback);
        } else if (type.equals("textrapezoid")) {
            logger.warn("not yet implemented");
            return null;
            //appendTexTrapezoid(*getConfig(), updateCallback);
        } else if (type.equals("texmultiple")) {
            logger.warn("not yet implemented");
            return null;
            /*std::vector<SGSharedPtr<SGPropertyNode> > transformConfigs;
            transformConfigs = getConfig()->getChildren("transform");
            for (unsigned i = 0; i < transformConfigs.size(); ++i) {
                std::string subtype = transformConfigs[i]->getStringValue("subtype", "");
                if (subtype == "textranslate")
                    appendTexTranslate(*transformConfigs[i], updateCallback);
      else if (subtype == "texrotate")
                    appendTexRotate(*transformConfigs[i], updateCallback);
      else if (subtype == "textrapezoid")
                    appendTexTrapezoid(*transformConfigs[i], updateCallback);
      else
                SG_LOG(SG_INPUT, SG_ALERT,
                        "Ignoring unknown texture transform subtype");
            }*/
        } else {
            logger.error("Ignoring unknown texture transform type " + type);
        }

        /*texMat -> setUpdateCallback(updateCallback);
        stateSet -> setTextureAttribute(0, texMat);*/
        //parent.addChild(group);
        //return group;
        animationGroup = new AnimationGroup(parent, group);
        return animationGroup;
    }

    SGExpression/*d /*
    SGTexTransformAnimation::*/readValue(SGPropertyNode cfg, String suffix) {
        String prop_name = cfg.getStringValue("property");
        /*SGSharedPtr<SGExpressiond>*/
        SGExpression value;
        if (StringUtils.empty(prop_name)) {
            value = new SGConstExpression/*<double>*/(new PrimitiveValue(0.0));
        } else {
            //value = new SGPropertyExpression/*<double>*/(getModelRoot().getNode(prop_name, true));
            value = resolvePropertyValueExpression(prop_name, getModelRoot());
        }
        SGInterpTable table = read_interpolation_table( /*&*/ cfg);
        //if( table )
        if (table != null) {
            value = new SGInterpTableExpression/*<double>*/(value, table);
            double biasValue = cfg.getDoubleValue("bias", 0);
            //if (biasValue) {
            if (biasValue != 0) {
                value = new SGBiasExpression/*<double>*/(value, biasValue);
            }
            value = new SGStepExpression/*<double>*/(value,
                    cfg.getDoubleValue("step", 0),
                    cfg.getDoubleValue("scroll", 0));
        } else {
            double biasValue = cfg.getDoubleValue("bias", 0);
            //if (biasValue) {
            if (biasValue != 0) {
                value = new SGBiasExpression/*<double>*/(value, biasValue);
            }
            value = new SGStepExpression/*<double>*/(value,
                    cfg.getDoubleValue("step", 0),
                    cfg.getDoubleValue("scroll", 0));
            value = read_offset_factor(/* & */cfg, value, "factor", "offset" + suffix);

            if (cfg.hasChild("min" + suffix)
                    || cfg.hasChild("max" + suffix)) {
                double minClip = cfg.getDoubleValue("min" + suffix, -SGLimitsd.max);
                double maxClip = cfg.getDoubleValue("max" + suffix, SGLimitsd.max);
                value = new SGClipExpression/*<double>*/(value, minClip, maxClip);
            }
        }

        return value./*release().*/simplify();
    }


    void appendTexTranslate(SGPropertyNode cfg, UpdateCallback updateCallback) {
        Vector3 readAxis = /*normalize(*/readVec3(cfg, "axis").normalize();
        Translation translation = new Translation(new Vector2(readAxis.getX(), readAxis.getY()));
        translation.setValue(cfg.getDoubleValue("starting-position", 0));
        // suffix "" is default parameter in FG
        updateCallback.appendTransform(translation, readValue(cfg, ""));
    }
/*
    void
    SGTexTransformAnimation::appendTexRotate( const SGPropertyNode& cfg,
                                              UpdateCallback* updateCallback )
    {
        Rotation* rotation = new Rotation( normalize(readVec3(cfg, "axis")),
                readVec3(cfg, "center") );
        rotation->setValue(cfg.getDoubleValue("starting-position-deg", 0));
        updateCallback->appendTransform(rotation, readValue(cfg, "-deg"));
    }

    void
    SGTexTransformAnimation::appendTexTrapezoid( const SGPropertyNode& cfg,
                                                 UpdateCallback* updateCallback )
    {
        Trapezoid::Side side = Trapezoid::TOP;
  const std::string side_str = cfg.getStringValue("side");
        if( side_str == "right" )
            side = Trapezoid::RIGHT;
        else if( side_str == "bottom" )
            side = Trapezoid::BOTTOM;
        else if( side_str == "left" )
            side = Trapezoid::LEFT;

        Trapezoid* trapezoid = new Trapezoid(side);
        trapezoid->setValue(cfg.getDoubleValue("starting-position", 0));
        updateCallback->appendTransform(trapezoid, readValue(cfg));
    }*/

    @Override
    public void process(List<NativeCollision> pickingrayintersections, RequestHandler requestHandler) {

        Matrix3 textureMatrix = getTransformMatrix();

        if (animationGroup == null || animationGroup.childtarget == null) {
            if (!noAnimationGroupLogged) {
                logger.warn("no animationGroup/childtarget");
                noAnimationGroupLogged = true;
            }
            return;
        }
        String s = animationGroup.childtarget.getTransform().getSceneNode().dump("  ", 0);
        //logger.debug("s=" + s);

        if (materials == null) {
            materials = new ArrayList<Material>();
            // One time lookup and adjustment of materials of the objects defined for animation.
            // Be careful with material modification because other animations like SGMaterialAnimation might also change material.
            PlatformHelper.traverse(animationGroup.childtarget.getTransform(), new TreeNodeVisitor<de.yard.threed.engine.Transform>() {
                @Override
                public void handleNode(de.yard.threed.engine.Transform node) {
                    SceneNode sceneNode = node.getSceneNode();
                    if (sceneNode.getMesh() != null) {
                        Material m = sceneNode.getMesh().getMaterial();
                        m.getName();
                        materials.add(m);
                    }
                }
            });
        }
        for (Material m : materials) {
            NativeUniform uniform = m.material.getUniform(Uniform.TEXTUREMATRIX);
            if (uniform != null) {
                uniform.setValue(textureMatrix);
            } else {
                if (!UniformTEXTUREMATRIXnotfoundLogged) {
                    // avoid log flooding
                    logger.warn("Uniform TEXTUREMATRIX not found");
                    UniformTEXTUREMATRIXnotfoundLogged = true;
                }
            }
        }

    }

    public Matrix3 getTransformMatrix() {
        if (updateCallback == null) {
            logger.warn("no updateCallback");
            return new Matrix3();
        }
        return updateCallback.operator();
    }
}
