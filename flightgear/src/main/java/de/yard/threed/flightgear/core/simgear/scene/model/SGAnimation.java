package de.yard.threed.flightgear.core.simgear.scene.model;

import de.yard.threed.core.Pair;
import de.yard.threed.core.Util;
import de.yard.threed.core.platform.NativeCollision;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.*;
import de.yard.threed.core.Vector3;
import de.yard.threed.flightgear.FlightGearProperties;
import de.yard.threed.flightgear.core.osg.Group;
import de.yard.threed.flightgear.core.osg.Node;
import de.yard.threed.flightgear.core.osgdb.Options;
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
import de.yard.threed.flightgear.core.simgear.structure.SGScaleExpression;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.core.platform.Config;
import de.yard.threed.core.StringUtils;
import de.yard.threed.engine.platform.common.RequestHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * animation.[ch]xx
 * <p>
 * Created by thomass on 28.12.16.
 * <p>
 * TODO implement "</enable-hot>" for optimization.
 */
public abstract class SGAnimation {
    static Log logger = Platform.getInstance().getLog(SGAnimation.class);

    boolean _found;
    String _name;
    SGPropertyNode _configNode;
    SGPropertyNode _modelRoot;
    // The objects listed in the animation definition
    protected List<String> _objectNames = new ArrayList<String>();

    List<Node> _installedAnimations;
    boolean _enableHOT;
    // a label is helpful for logging/debugging
    public String label;

    public SGAnimation(SGTransientModelData modelData, String label) {
        // osg::NodeVisitor(osg::NodeVisitor::TRAVERSE_ALL_CHILDREN),
        _found = false;
        _configNode = modelData.getConfigNode();
        _modelRoot = modelData.getModelRoot();
        this.label = label;

        logger.debug("Building animation from " + _configNode.getPath());

        _name = _configNode.getStringValue("name", "");
        _enableHOT = _configNode.getBoolValue("enable-hot", true);
        List<SGPropertyNode> objectNames = _configNode.getChildren("object-name");
        for (int i = 0; i < objectNames.size(); ++i) {
            String oname = objectNames.get(i).getStringValue();
            /*if (oname.equals("Needle")){
                oname=oname;
            }*/
            _objectNames.add(oname);
        }
    }
    //virtual ~SGAnimation();

    public SGPropertyNode getConfigNode() {
        return _configNode;
    }

    /**
     * Build animation from XML content?
     * FG meanwhile passes {@link SGTransientModelData}"&modelData" here, which contains most of the single parameter
     */
    static SGAnimation/*boolean*/ animate(SGTransientModelData modelData/*Node xmlNodeOfCurrentModel, SGPropertyNode configNode,
                                          SGPropertyNode modelRoot,
                                          Options options,
                                          String path, int i*/, String label) {
        String type = modelData.getConfigNode().getStringValue("type", "none");
        SGAnimation animation = null;
        long startTime = Platform.getInstance().currentTimeMillis();
        if (type.equals("alpha-test")) {
            // SGAlphaTestAnimation animInst(modelData);
            // animInst.apply(node);
        } else if (type.equals("billboard")) {
            // SGBillboardAnimation animInst(modelData);
            // animInst.apply(node);
        } else if (type.equals("blend")) {
            //SGBlendAnimation animInst(modelData);
            //animInst.apply(node);
        } else if (type.equals("dist-scale")) {
            //SGDistScaleAnimation animInst(modelData);
            //animInst.apply(node);
        } else if (type.equals("flash")) {
            // SGFlashAnimation animInst(modelData);
            // animInst.apply(node);
        } else if (type.equals("interaction")) {
            //SGInteractionAnimation animInst(modelData);
            //animInst.apply(node);
        } else if (type.equals("material")) {
            //SGMaterialAnimation animInst(modelData, options, path);
            SGMaterialAnimation animInst = new SGMaterialAnimation(modelData, label);
            animInst.apply(modelData.getXmlNode());
            animation = animInst;
        } else if (type.equals("noshadow")) {
            //SGShadowAnimation animInst(modelData);
            // animInst.apply(node);
        } else if (type.equals("pick")) {

            SGPickAnimation animInst = new SGPickAnimation(modelData, label);
            animInst.apply(modelData.getXmlNode());
            animation = animInst;
        } else if (type.equals("knob")) {
            // SGKnobAnimation animInst(modelData);
            //  animInst.apply(node);
        } else if (type.equals("slider")) {
            //SGSliderAnimation animInst(modelData);
            // animInst.apply(node);
        } else if (type.equals("range")) {
            // SGRangeAnimation animInst(modelData);
            // animInst.apply(node);
        } else if (type.equals("rotate") || type.equals("spin")) {
            SGRotateAnimation animInst = new SGRotateAnimation(modelData, label);
            animInst.apply(modelData.getXmlNode());
            animation = animInst;
        } else if (type.equals("scale")) {
            //SGScaleAnimation animInst(modelData);
            SGScaleAnimation animInst = new SGScaleAnimation(modelData, label);
            animInst.apply(modelData.getXmlNode());
            animation = animInst;
        } else if (type.equals("select")) {
            SGSelectAnimation animInst = new SGSelectAnimation(modelData, label);
            animInst.apply(modelData.getXmlNode());
            animation = animInst;
        } else if (type.equals("shader")) {
            //  SGShaderAnimation animInst(modelData, options);
            //  animInst.apply(node);
        } else if (type.equals("textranslate") || type.equals("texrotate") ||
                type.equals("textrapezoid") || type.equals("texmultiple")) {
            //  SGTexTransformAnimation animInst(modelData);
            //  animInst.apply(node);
        } else if (type.equals("timed")) {
            //SGTimedAnimation animInst(modelData);
            //animInst.apply(node);
        } else if (type.equals("locked-track")) {
            //SGTrackToAnimation animInst(node, modelData);
            //animInst.apply(node);
        } else if (type.equals("translate")) {
            //SGTranslateAnimation animInst(modelData);
            SGTranslateAnimation animInst = new SGTranslateAnimation(modelData, label);
            animInst.apply(modelData.getXmlNode());
            animation = animInst;
        } else if (type.equals("light")) {
            //SGLightAnimation animInst(modelData, options, path, i);
            //animInst.apply(node);
        } else if (type.equals("null") || type.equals("none") || StringUtils.empty(type)) {
            //SGGroupAnimation animInst(modelData);
            //animInst.apply(node);
        } else {
            logger.warn("Animation not created: " + type);
            return null;
        }
        if (animation != null) {
            long took = Platform.getInstance().currentTimeMillis() - startTime;
            if (took > 5) {
                // 5ms seems to be a good threshold currently
                logger.warn("Building " + animation.getClass().getName() + " took " + took + " ms");
            }
        }
        return animation;
    }

    /**
     * Statt SGTranslateTransform.
     * Replaces OSG NodeCallbacks that are used by FG(?) (see README.md).
     * 4.10.19: Das ist aber nicht sehr generisch. Darum pack ich erstmal noch Parameter ein, wie es gebraucht wird. Alles zur für PickAnimation
     * 13.3.24: No longer pass pickingray but the objects hit for better efficiency.
     * 18.11.24:
     * <p>
     * FG DIFF
     */
    public abstract void process(List<NativeCollision> pickingrayintersections, RequestHandler requestHandler);

    /**
     * Ob es die pro Animation geben muesste und hier abstract ist unklar. Eigentlich ist apply der Callback des NodeVisitors.
     * FG-DIFF Nicht wie bei OSG in den Scenegraph mit Visitor. Hmmm, auf jeden Fall wohl ueberschreibbar durch z.B. BlendAnimation
     * 4.10.19: Das ist doch sehr spezifisch für Animationen, die neue Nodes (AnimationGroup) brauchen.
     *
     * @param
     */
    protected void apply(Node xmlNodeOfCurrentModel) {

        // the trick isType to getFirst traverse the children and then
        // possibly splice in a new group node if required.
        // Else we end up in a recursive loop where we infinitly insert new
        // groups in between

        // Der reverse rueckt group wohl weiter nach unten im Tree, damit...warum auch immer. Auf jeden Fall koennte damit die
        // ACtransform in der Hierarchie bleiben.
        //TODO traverse(group);

        // Note that this algorithm preserves the order of the child objects
        // like they appear in the object-name tags.
        // The timed animations require this
        //osg::ref_ptr < osg::Group > animationGroup;
        AnimationGroup animationGroup = null;
        //std::list<std::string>::const_iterator nameIt;
        //for (nameIt = _objectNames.begin(); nameIt != _objectNames.end(); ++nameIt)
        for (String nameIt : _objectNames) {
            // ob der Cast so ideal ist?
            animationGroup = installInGroup(nameIt, (Group) xmlNodeOfCurrentModel, animationGroup);
        }
    }

    // 5.10.17: install (wegen Visitor) nicht erforderlich? Der wird doch fuer die Einrichtung sorgen?
    //  virtual void install(osg::Node& node);
    
  /*  Group createAnimationGroup(Group parent){
     
            SGTranslateTransform transform = new SGTranslateTransform;
            transform->setName("translate animation");
            if (_animationValue && !_animationValue->isConst()) {
                UpdateCallback* uc = new UpdateCallback(_condition, _animationValue);
                transform->setUpdateCallback(uc);
            }
            transform->setAxis(_axis);
            transform->setValue(_initialValue);
            parent.addChild(transform);
            return transform;
        }
    }*/

    /**
     * ////////////////////////////////////////////////////////////////////////
     * // Implementation of null animation
     * ////////////////////////////////////////////////////////////////////////
     * <p>
     * // Ok, that is to build a subgraph from different other
     * // graph nodes. I guess that this stems from the time where modellers
     * // could not build hierarchical trees ...
     * <p>
     * Create a new node level needed for animations, eg. a node where rotations apply.
     * Implemented by extending classes. In FG it typically builds the groups and hooks an update callback into OSG(?)
     * 25.1.17: SceneNode statt Group, weil traverse fehlt
     * 04.10.19: Not needed by all animations, so not abstract but default implementation
     * 18.11.24: Back to group from SceneNode for more intuitive migrated code? Not possible, because we have no group.
     * However we still have no traverse.
     *
     * @param parent The scene node to which the animation applies. Cannot be type Group because we have no such type.
     * @return
     */
    public AnimationGroup createAnimationGroup(SceneNode parent) {
        return null;
    }
   /*27.1.17 public SceneNode createAnimationGroup(SceneNode parent) {
        SceneNode group = new SceneNode();
        parent.attach(group);
        return group;
    }*/
    /*public Group createAnimationGroup(Group parent) {
        Group group = new Group();
        parent.addChild(group);
        return group;
    }*/

    //virtual void apply(osg::Group& group);


    /**
     * Read a 3d vector from the configuration property node.
     * <p>
     * Reads values from @a name/[xyz]@a prefix and defaults to the according
     * value of @a def for each value which isType not set.
     * <p>
     * FG-DIFF Wegen folgender Arithemtik Vector3 statt SGVec3d
     *
     * @param name   Name of the root node containing all coordinates
     * @param suffix Suffix appended to each child node (x,y,z)
     * @param def    Vector containing default values
     */
    Vector3 readVec3(SGPropertyNode cfg, String name, String suffix /*= ""*/, Vector3 def /*= SGVec3d::zeros()*/) {
        return new Vector3(
                (float) cfg.getDoubleValue(name + "/x" + suffix, def.getX()),
                (float) cfg.getDoubleValue(name + "/y" + suffix, def.getY()),
                (float) cfg.getDoubleValue(name + "/z" + suffix, def.getZ()));

    }

    Vector3 readVec3(String name, String suffix /*= ""*/, Vector3 def /*= SGVec3d::zeros()*/) {
        return readVec3(_configNode, name, suffix, def);
    }


    /**
     * factored out to share with SGKnobAnimation
     * <p>
     * FG-DIFF Wegen der Arithemtik Vector3 statt SGVec3d
     *
     * @return
     */
    void readRotationCenterAndAxis(Node _rootNode, Pair<Vector3, Vector3> centerAndAxis /* SGVec3d center, Vector3 axis*/, SGTransientModelData modelData) {
        //center = SGVec3d::zeros();
        centerAndAxis.setFirst(new Vector3());

        //if (setCenterAndAxisFromObject(_rootNode, center, axis, modelData))
        if (setCenterAndAxisFromObject(_rootNode, centerAndAxis, modelData)) {
            if (8 * SGLimitsd.min < /*norm(axis)*/centerAndAxis.getSecond().norm()) {
                //axis = normalize(axis);
                centerAndAxis.setSecond(centerAndAxis.getSecond().normalize());
            }
            return;
        }

        if (_configNode.hasValue("axis/x1-m")) {
            Vector3 v1 = readVec3("axis", "1-m", new Vector3()), // axis/[xyz]1-m
                    v2 = readVec3("axis", "2-m", new Vector3()); // axis/[xyz]2-m
            //center = 0.5*(v1+v2);
            //axis = v2 - v1;
            centerAndAxis.setFirst(v1.add(v2).multiply(0.5f));
            centerAndAxis.setSecond(v2.subtract(v1));
        } else {
            centerAndAxis.setSecond(readVec3("axis", "", new Vector3()));
        }
        if (8 * SGLimitsd.min < /*norm(axis)*/centerAndAxis.getSecond().norm()) {
            //axis = normalize(axis);
            centerAndAxis.setSecond(centerAndAxis.getSecond().normalize());
        }

        centerAndAxis.setFirst(readVec3("center", "-m", centerAndAxis.getFirst()));
        //FG-DIFF Unklar, wie der AC Transform erhalten bleibt bzw. wieso die Animation mit FG Achsen beschrieben werden koennen.
        // Bei mir geht es nur mit vertauschten yz-Axen .
        Vector3 axis = centerAndAxis.getSecond();
        axis = new Vector3(axis.getX(), axis.getZ(), axis.getY());
        //center auch? fuer windturbine ja
        Vector3 center = centerAndAxis.getFirst();
        center = new Vector3(center.getX(), center.getZ(), center.getY());
        centerAndAxis.setFirst(center);
        centerAndAxis.setSecond(axis);
    }

    /*
    SGExpressiond* readOffsetValue(const char* tag_name) const;

    void removeMode(osg::Node& node, osg::StateAttribute::GLMode mode);
    void removeAttribute(osg::Node& node, osg::StateAttribute::Type type);
    void removeTextureMode(osg::Node& node, unsigned unit,
                           osg::StateAttribute::GLMode mode);
    void removeTextureAttribute(osg::Node& node, unsigned unit,
                                osg::StateAttribute::Type type);
    void setRenderBinToInherit(osg::Node& node);
    void cloneDrawables(osg::Node& node);

    std::string getType() const
    { return std::string(_configNode->getStringValue("type", "")); }

    const SGPropertyNode* getConfig() const
    { return _configNode; }
    SGPropertyNode* getModelRoot() const
    { return _modelRoot; }
*/
    SGCondition getCondition() {
        SGPropertyNode conditionNode = _configNode.getChild("condition");
        if (conditionNode == null)
            return null;
        return SGCondition.sgReadCondition(_modelRoot, conditionNode);
    }

    /**
     * The purpose/idea/intention of this method is unclear. Apparently:
     * <p>
     * Move an animated object from the current location in the object tree to an AnimationGroup.
     * This might be required to apply subsequent animations like in windsock.
     * FG moves objects to the top XML group(?), but we cannot do this because we'll loose ACPolicy.
     * The AnimationGroup is created if it not yet exists.
     * <p>
     * Das mit der AnimationGroup koennte fuer mehrere Animations auf EINER Node sein. Dann kann
     * man das evtl. in einer abbilden. Scheint aber zu frickelig.
     * Ist dafuer auch _installedAnimations?
     * 4.10.19: Was passiert hier eigentlich? Es geht wohl darum, verschiedene SceneGraph Nodes zu bauen, über die dann
     * die Animation (z.B. Rotation) erfolgt. Aber das kann doch eigentlich nicht generisch sein.
     * <p>
     * Kann (bei Fehlern) auch null liefern.
     *
     * @param name
     * @param xmlNodeOfCurrentModel
     */
    AnimationGroup installInGroup(String name, Group xmlNodeOfCurrentModel, AnimationGroup animationGroup) {
        // 17.8.24: The original implementation used NodeVisitor/traverse for finding a child (children might be deeper inside tree?). Because we
        // don't have this, we use recursive findNodeByName(). Maybe its not really the same. And there might be duplicate names. Hmm.
        // But more severe, findNodeByName() is a performance killer (~40ms) at least in JME. Better use platform finder.
        //int i = group.getTransform().getChildCount() - 1;
        //for (; 0 <= i; --i) {
        long startTime = Platform.getInstance().currentTimeMillis();
        SceneNode child = null;
        //List<SceneNode> nlist = xmlNodeOfCurrentModel.findNodeByName(name);//getChild(i);
        List<NativeSceneNode> nlist = Platform.getInstance().findNodeByName(name, xmlNodeOfCurrentModel.nativescenenode);//getChild(i);
        if (nlist.size() > 0) {
            child = new SceneNode(nlist.get(0));
        }
        if (child == null) {
            // 10.10.18: debug statt warn um es disablen zu können. Kommt sehr oft.
            if (Config.animationdebuglog) {
                logger.debug("installInGroup:child not found: " + name);
            }
            return null;
        }
        //logger.debug("installInGroup took " + (Platform.getInstance().currentTimeMillis() - startTime) + " ms");

        // Check if this one isType already processed
            /*TODO if (std::find(_installedAnimations.begin(),
                    _installedAnimations.end(), child)
            != _installedAnimations.end())
            continue;*/

        if (/*name.empty() ||*/ child.getName().equals(name)) {
            // fire the installation of the animation
            // Manche Animationen haben noch einen install.
            //TODO install( * child);

            // Mit createAnimationGroup() wird eine Ebene fuer die Animation eingezogen.
            // Mir fehlt wohl der traverse, so dass die group zu weit oben ist. Darum gebe ich den Parent rein.
            // Die Implementierung hier ist total undurchsichtig. Evtl. ist die Rotation aber ohne AC gedacht??
            // Bei radar die z-Achse. Aber trotzdem, dan ist es an der falschen Stelle.
            // Die AnimationGroup kommt an den Parent des ersten Child. Damit bekommt man auch Cascades.
            // create a group node on demand
            //TODO ? if (!animationGroup.valid()) {
            /*Group*/
            if (animationGroup == null) {
                Transform parent = child.getTransform().getParent();
                if (parent == null) {
                    logger.error("no parent for " + child.getName());
                } else {
                    // we cannot attach to xmlNode like FG(?) because this bypasses ACPolicy. Also FG doesn't.
                    animationGroup = createAnimationGroup(/*group*/ parent.getSceneNode());

                }
            }

            //SceneNode animationGroup = createAnimationGroup(group);
            // Animation type that does not require a new group,
            // in this case we can stop and look for the next object
            //if (animationGroup.valid() && !_name.empty())
            //    animationGroup -> setName(_name);
            //}
            //if (animationGroup.valid()) {
            if (animationGroup != null) {
                //animationGroup->addChild(child);
                if (animationGroup.childtarget == null) {
                    logger.warn("Ignoring child due to missing childtarget");
                } else {
                    animationGroup.childtarget.attach/*addChild*/(child);
                }
            }
            //removed by above add/setParent group.removeChild(i);

            // store that we already have processed this child node
            // We can hit this one twice if an animation references some
            // part of a subtree twice
            //TODO ? _installedAnimations.add(child);
        }
        return animationGroup;
    }

/*
    class RemoveModeVisitor;
    class RemoveAttributeVisitor;
    class RemoveTextureModeVisitor;
    class RemoveTextureAttributeVisitor;
    class BinToInheritVisitor;
    class DrawableCloneVisitor;*/

    SGExpression/*d*/    read_value(SGPropertyNode configNode, SGPropertyNode modelRoot, String unit, double defMin, double defMax) {
        SGPropertyNode expression = configNode.getNode("expression");
        if (expression != null)
            return SGExpression.SGReadDoubleExpression(modelRoot, expression.getChild(0));

        SGExpression/*<double>*/ value = null;//0;

        String inputPropertyName = configNode.getStringValue("property", "");
        if (StringUtils.empty(inputPropertyName)) {
            String spos = unit_string("starting-position", unit);
            double initPos = configNode.getDoubleValue(spos, 0);
            value = new SGConstExpression(new PrimitiveValue(initPos));
        } else {
            // 2.12.24 Extracted logic to static method
            value = resolvePropertyValueExpression(inputPropertyName, modelRoot);
        }

        SGInterpTable interpTable = read_interpolation_table(configNode);
        if (interpTable != null) {
            return new SGInterpTableExpression/*<double>*/(value, interpTable);
        } else {
            String offset = unit_string("offset", unit);
            String min = unit_string("min", unit);
            String max = unit_string("max", unit);

            if (configNode.getBoolValue("use-personality", false)) {
                value = new SGPersonalityScaleOffsetExpression(value, configNode, "factor", offset);
            } else {
                value = read_factor_offset(configNode, value, "factor", offset);
            }

            double minClip = configNode.getDoubleValue(min, defMin);
            double maxClip = configNode.getDoubleValue(max, defMax);
            if (minClip > Math./*SGMiscd::*/min(SGLimitsd.min, -SGLimitsd.max) || maxClip < SGLimitsd.max) {
                value = new SGClipExpression/*<double>*/(value, minClip, maxClip);
            }

            return value;
        }

    }

    /**
     * Lookup a property node.
     * 5.11.24 Since we don't have one single property tree like FG, we need a kind of lookup.
     * But that is too much effort. See FlightGearProperties.
     * 7.11.24 modelroot points to a new node probably, so anything like "/environment" cannot be found.
     * So even with a single tree we need a kind of lookup
     * 2.12.24 Logic extracted to method for easier reuse.
     */
    public static SGExpression resolvePropertyValueExpression(String inputPropertyName, SGPropertyNode modelRoot) {
        SGPropertyNode inputProperty;
        //inputProperty = modelRoot.getNode(inputPropertyName, true);
        inputProperty = FlightGearProperties.resolve(inputPropertyName, modelRoot);
        SGPropertyExpression value = new SGPropertyExpression/*<double>*/(inputProperty);
        logger.debug("read_value: value for '" + inputPropertyName + "'=" + value.getValue(null).toString());
        return value;
    }

    static String unit_string(String value, String unit) {
        return value + unit;
    }

    /**
     * Read an interpolation table from properties.
     */
    static SGInterpTable read_interpolation_table(SGPropertyNode props) {
        SGPropertyNode table_node = props.getNode("interpolation");
        if (table_node == null)
            return null;
        return new SGInterpTable(table_node);
    }

    static SGExpression/*d*/    read_factor_offset(SGPropertyNode configNode, SGExpression/*d*/ expr, String factor, String offset) {
        double factorValue = configNode.getDoubleValue(factor, 1);
        if (factorValue != 1)
            expr = new SGScaleExpression/*<double>*/(expr, factorValue);
        double offsetValue = configNode.getDoubleValue(offset, 0);
        if (offsetValue != 0)
            expr = new SGBiasExpression/*<double>*/(expr, offsetValue);
        return expr;
    }

    /**
     * If an object is specified in the axis tag it is assumed to be a single line segment with two vertices.
     * This function will take action when axis has an object-name tag and the corresponding object
     * can be found within the hierarchy.
     * <p>
     * FG-DIFF Instead of filling center/axis a pair is used and filled. Not static because it uses configNode.
     */
    public boolean setCenterAndAxisFromObject(Node rootNode, Pair<Vector3, Vector3> centerAndAxis /*,SGVec3d& center, SGVec3d &axis, simgear::*/, SGTransientModelData modelData) {
        String axis_object_name = "";//std::string();
        boolean can_warn = true;

        if (_configNode.hasValue("axis/object-name")) {
            axis_object_name = _configNode.getStringValue("axis/object-name");
        } else if (_configNode.getNode("axis") == null) {
            axis_object_name = _configNode.getStringValue("object-name") + /*std::string*/("-axis");
            // for compatibility we will not warn if no axis object can be found when there was nothing
            // specified - as the axis could just be the default at the origin
            // so if there is a [objectname]-axis use it, otherwise fallback to the previous behaviour
            can_warn = false;
        }

        if (!StringUtils.empty(axis_object_name)) {
            /*
             * First search the currently loaded cache map to see if this axis object has already been located.
             * If we find it, we use it.
             */
            Util.notyet();
                /*
        const SGLineSegment<double> *axisSegment = modelData.getAxisDefinition(axis_object_name);
                if (!axisSegment)
                {
                    /*
                     * Find the object by name
                     * /
                    FindGroupVisitor axis_object_name_finder(axis_object_name);
                    rootNode->accept(axis_object_name_finder);
                    osg::Group *object_group = axis_object_name_finder.getGroup();

                    if (object_group)
                    {
                        /*
                         * we have found the object group (for the axis). This should be two vertices
                         * Now process this (with the line collector) to get the vertices.
                         * Once we have that we can then calculate the center and the affected axes.
                         * /
                        object_group->setNodeMask(0xffffffff);
                        LineCollector lineCollector;
                        object_group->accept(lineCollector);
                        std::vector<SGLineSegmentf> segs = lineCollector.getLineSegments();

                        if (!segs.empty())
                        {
                            /*
                             * Store the axis definition in the map; as once hidden it will not be possible
                             * to locate it again (and in any case it will be quicker to do it this way)
                             * This makes the axis/center static; there could be a use case for making this
                             * dynamic (and rebuilding the transforms), in which case this would need to
                             * do something different with the object; possibly storing a reference to the node
                             * so it can be extracted for dynamic processing.
                             * /
                            SGLineSegmentd segd(*(segs.begin()));
                            axisSegment = modelData.addAxisDefinition(axis_object_name, segd);
                            /*
                             * Hide the axis object. This also helps the modeller to know which axis animations are unassigned.
                             * /
                            object_group->setNodeMask(0);
                        }
                        else
                            SG_LOG(SG_INPUT, SG_ALERT, "Could not find a valid line segment for animation:  " << axis_object_name);
                    }
                    else if (can_warn)
                        SG_LOG(SG_INPUT, SG_ALERT, "Could not find at least one of the following objects for axis animation: " << axis_object_name);
                }
                if (axisSegment)
                {
                    center = 0.5*(axisSegment->getStart() + axisSegment->getEnd());
                    axis = axisSegment->getEnd() - axisSegment->getStart();
                    return true;
                }*/
        }
        return false;
    }

    Vector3/*SGVec3d*/ readTranslateAxis(SGPropertyNode configNode) {
        Vector3 axis;

        if (configNode.hasValue("axis/x1-m")) {
            Vector3 v1, v2;
            v1 = new Vector3(configNode.getDoubleValue("axis/x1-m", 0),
                    configNode.getDoubleValue("axis/y1-m", 0),
                    configNode.getDoubleValue("axis/z1-m", 0));
            v2 = new Vector3(configNode.getDoubleValue("axis/x2-m", 0),
                    configNode.getDoubleValue("axis/y2-m", 0),
                    configNode.getDoubleValue("axis/z2-m", 0));
            axis = v2.subtract(v1);//v2 - v1;
        } else {
            axis = new Vector3(configNode.getDoubleValue("axis/x", 0),
                    configNode.getDoubleValue("axis/y", 0),
                    configNode.getDoubleValue("axis/z", 0));
        }
        if (8 * SGLimitsd.min < axis.norm()) {
            axis = axis.normalize();//normalize(axis);
        }
        // FG animations think in FG coordinates.
        return ACProcessPolicy.switchYZ(axis);
    }

    SGPropertyNode getConfig() {
        return _configNode;
    }

    SGPropertyNode getModelRoot() {
        return _modelRoot;
    }

    /**
     * helper zum testen
     */
    public boolean isOnObject(String objname) {
        return _objectNames.contains(objname);
    }
}
