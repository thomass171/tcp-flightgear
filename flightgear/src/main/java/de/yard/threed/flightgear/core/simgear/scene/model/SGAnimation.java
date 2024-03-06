package de.yard.threed.flightgear.core.simgear.scene.model;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.*;
import de.yard.threed.core.Vector3;
import de.yard.threed.flightgear.core.osg.Group;
import de.yard.threed.flightgear.core.osg.Node;
import de.yard.threed.flightgear.core.osgdb.Options;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.math.SGInterpTable;
import de.yard.threed.flightgear.core.simgear.math.SGLimitsd;
import de.yard.threed.flightgear.core.simgear.props.SGCondition;
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
 */
public abstract class SGAnimation {
    static Log logger = Platform.getInstance().getLog(SGAnimation.class);

    boolean _found;
    String _name;
    SGPropertyNode _configNode;
    SGPropertyNode _modelRoot;
    protected List<String> _objectNames = new ArrayList<String>();

    List<Node> _installedAnimations;
    boolean _enableHOT;

    public SGAnimation(SGPropertyNode configNode, SGPropertyNode modelRoot) {
        // osg::NodeVisitor(osg::NodeVisitor::TRAVERSE_ALL_CHILDREN),
        _found = false;
        _configNode = configNode;
        _modelRoot = modelRoot;

        _name = configNode.getStringValue("name", "");
        _enableHOT = configNode.getBoolValue("enable-hot", true);
        List<SGPropertyNode> objectNames = configNode.getChildren("object-name");
        for (int i = 0; i < objectNames.size(); ++i) {
            String oname = objectNames.get(i).getStringValue();
            /*if (oname.equals("Needle")){
                oname=oname;
            }*/
            _objectNames.add(oname);
        }
    }
    //virtual ~SGAnimation();

    /**
     * Build animation from XML content?
     */
    static SGAnimation/*boolean*/ animate(Node node, SGPropertyNode configNode,
                                          SGPropertyNode modelRoot,
                                          Options options,
                                          String path, int i) {
        String type = configNode.getStringValue("type", "none");
        SGAnimation animation = null;
        if (type.equals("alpha-test")) {
            // SGAlphaTestAnimation animInst(configNode, modelRoot);
            // animInst.apply(node);
        } else if (type.equals("billboard")) {
            // SGBillboardAnimation animInst(configNode, modelRoot);
            // animInst.apply(node);
        } else if (type.equals("blend")) {
            //SGBlendAnimation animInst(configNode, modelRoot);
            //animInst.apply(node);
        } else if (type.equals("dist-scale")) {
            //SGDistScaleAnimation animInst(configNode, modelRoot);
            //animInst.apply(node);
        } else if (type.equals("flash")) {
            // SGFlashAnimation animInst(configNode, modelRoot);
            // animInst.apply(node);
        } else if (type.equals("interaction")) {
            //SGInteractionAnimation animInst(configNode, modelRoot);
            //animInst.apply(node);
        } else if (type.equals("material")) {
            //SGMaterialAnimation animInst(configNode, modelRoot, options, path);
            SGMaterialAnimation animInst = new SGMaterialAnimation(configNode, modelRoot);
            animInst.apply(node);
            animation = animInst;
        } else if (type.equals("noshadow")) {
            //SGShadowAnimation animInst(configNode, modelRoot);
            // animInst.apply(node);
        } else if (type.equals("pick")) {

             SGPickAnimation animInst = new SGPickAnimation(configNode, modelRoot);
            animInst.apply(node);
            animation = animInst;
        } else if (type.equals("knob")) {
            // SGKnobAnimation animInst(configNode, modelRoot);
            //  animInst.apply(node);
        } else if (type.equals("slider")) {
            //SGSliderAnimation animInst(configNode, modelRoot);
            // animInst.apply(node);
        } else if (type.equals("range")) {
            // SGRangeAnimation animInst(configNode, modelRoot);
            // animInst.apply(node);
        } else if (type.equals("rotate") || type.equals("spin")) {
            SGRotateAnimation animInst = new SGRotateAnimation(configNode, modelRoot);
            animInst.apply(node);
            animation = animInst;
        } else if (type.equals("scale")) {
            //SGScaleAnimation animInst(configNode, modelRoot);
            // animInst.apply(node);
        } else if (type.equals("select")) {
            // SGSelectAnimation animInst(configNode, modelRoot);
            // animInst.apply(node);
        } else if (type.equals("shader")) {
            //  SGShaderAnimation animInst(configNode, modelRoot, options);
            //  animInst.apply(node);
        } else if (type.equals("textranslate") || type.equals("texrotate") ||
                type.equals("textrapezoid") || type.equals("texmultiple")) {
            //  SGTexTransformAnimation animInst(configNode, modelRoot);
            //  animInst.apply(node);
        } else if (type.equals("timed")) {
            //SGTimedAnimation animInst(configNode, modelRoot);
            //animInst.apply(node);
        } else if (type.equals("locked-track")) {
            //SGTrackToAnimation animInst(node, configNode, modelRoot);
            //animInst.apply(node);
        } else if (type.equals("translate")) {
            //SGTranslateAnimation animInst(configNode, modelRoot);
            //animInst.apply(node);
        } else if (type.equals("light")) {
            //SGLightAnimation animInst(configNode, modelRoot, options, path, i);
            //animInst.apply(node);
        } else if (type.equals("null") || type.equals("none") || StringUtils.empty(type)) {
            //SGGroupAnimation animInst(configNode, modelRoot);
            //animInst.apply(node);
        } else {
            logger.warn("Animation not created: " + type);
            return null;
        }

        return animation;
    }

    /**
     * Statt SGTranslateTransform.
     * 4.10.19: Das ist aber nicht sehr generisch. Darum pack ich erstmal noch Parameter ein, wie es gebraucht wird. Alles zur für PickAnimation
     * <p>
     * FG DIFF
     */
    public abstract void process(Ray pickingray, RequestHandler requestHandler);

    /**
     * Ob es die pro Animation geben muesste und hier abstract ist unklar. Eigentlich ist apply der Callback des NodeVisitors.
     * FG-DIFF Nicht wie bei OSG in den Scenegraph mit Visitor. Hmmm, auf jeden Fall wohl ueberschreibbar durch z.B. BlendAnimation
     * 4.10.19: Das ist doch sehr spezifisch für Animationen, die neue Nodes (AnimationGroup) brauchen.
     * @param
     */
    protected void apply(Node group) {

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
            animationGroup = installInGroup(nameIt, (Group) group, animationGroup);
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
     * // Ok, that isType to build a subgraph from different other
     * // graph nodes. I guess that this stems from the time where modellers
     * // could not build hierarchical trees ...
     * <p>
     * Diese Methode wird von der ableitenden Klasse ueberschrieben.
     * 25.1.17: SceneNode statt Group, weil traverse fehlt
     * 04.10.19: Nicht jede Animation braucht das. darum doch nicht abstract, sondern Defaultimplementierung
     * @param parent
     * @return
     */
    public AnimationGroup createAnimationGroup(SceneNode parent){
        return  null;
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
    Vector3[]/*void*/ readRotationCenterAndAxis(/* SGVec3d center,                              Vector3 axis     */) {
        Vector3[] centerandaxis = new Vector3[2];// = new SGVec3d();//::zeros();
        centerandaxis[0] = new Vector3();

        if (_configNode.hasValue("axis/x1-m")) {
            Vector3 v1 = readVec3("axis", "1-m", new Vector3()), // axis/[xyz]1-m
                    v2 = readVec3("axis", "2-m", new Vector3()); // axis/[xyz]2-m
            //center = 0.5*(v1+v2);
            centerandaxis[0] = v1.add(v2).multiply(0.5f);
            centerandaxis[1] = v2.subtract(v1);
        } else {
            centerandaxis[1] = readVec3("axis", "", new Vector3());
        }
        //Keine Ahnnug. erstmal immer normalisieren if( 8 * SGLimitsd.min < norm(axis) ){
        centerandaxis[1] = centerandaxis[1].normalize();
        //}

        centerandaxis[0] = readVec3("center", "-m", centerandaxis[0]);

        //FG-DIFF Unklar, wie der AC Transform erhalten bleibt bzw. wieso die Animation mit FG Achsen beschrieben werden koennen.
        // Bei mir geht es nur mit vertauschten yz-Axen .
        centerandaxis[1] = new Vector3(centerandaxis[1].getX(), centerandaxis[1].getZ(), centerandaxis[1].getY());
        //center auch? fuer windturbine ja
        centerandaxis[0] = new Vector3(centerandaxis[0].getX(), centerandaxis[0].getZ(), centerandaxis[0].getY());

        return centerandaxis;
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
     * Ein Object in eine AnimationGroup einhaengen. Wenns noch keine AnimationGroup gibt, eine anlegen.
     *
     * Das mit der AnimationGroup koennte fuer mehrere Animations auf EINER Node sein. Dann kann
     * man das evtl. in einer abbilden. Scheint aber zu frickelig.
     * Ist dafuer auch _installedAnimations?
     * 4.10.19: Was passiert hier eigentlich? Es geht wohl darum, verschiedene SceneGraph Nodes zu bauen, über die dann
     * die Animation (z.B. Rotation) erfolgt. Aber das kann doch eigentlich nicht generisch sein.
     * <p>
     * Kann (bei Fehlern) auch null liefern.
     *
     * @param name
     * @param group
     */
    AnimationGroup installInGroup(String name, Group group, AnimationGroup animationGroup) {
        //int i = group.getNumChildren() - 1;
        //for (; 0 <= i; --i) {
        // rekursiv suchen, weil ich keinen traverse habe. Das ist sicherlich nicht 100% vergleichbar.
        // 7.10.17: Und es kann je nach Loder (zB. gltf) Nodedubletten geben. Wie sich das auswirkt? Wer weiss.
        SceneNode child = null;
        List<SceneNode> nlist = group.findNodeByName(name);//getChild(i);
        if (nlist.size()>0){
            child = nlist.get(0);
        }
        if (child == null) {
            // 10.10.18: debug statt warn um es disablen zu können. Kommt sehr oft.
            if (Config.animationdebuglog) {
                logger.debug("installInGroup:child not found: " + name);
            }
            return null;
        }
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
                animationGroup.childtarget.attach/*addChild*/(child);
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
            SGPropertyNode inputProperty;
            inputProperty = modelRoot.getNode(inputPropertyName, true);
            value = new SGPropertyExpression/*<double>*/(inputProperty);
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

    SGPropertyNode getConfig() {
        return _configNode;
    }

    SGPropertyNode getModelRoot() {
        return _modelRoot;
    }

    /**
     * helper zum testen
     */
    public boolean isOnObject(String objname){
        return _objectNames.contains(objname);
    }
}
