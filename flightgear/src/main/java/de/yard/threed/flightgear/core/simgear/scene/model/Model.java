package de.yard.threed.flightgear.core.simgear.scene.model;

import de.yard.threed.core.platform.*;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Mesh;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.loader.MaterialFactory;
import de.yard.threed.flightgear.core.PropertyList;
import de.yard.threed.flightgear.core.osg.Node;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.scene.material.Effect;
import de.yard.threed.flightgear.core.simgear.scene.material.MakeEffect;
import de.yard.threed.flightgear.core.simgear.scene.util.SGReaderWriterOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Aus model.cxx
 * <p/>
 * Created by thomass on 21.07.16.
 */
public class Model {
    static Log logger = Platform.getInstance().getLog(Model.class);

/*
    osg::Texture2D*
    SGLoadTexture2D(bool staticTexture, const std::string& path,
                const osgDB::Options* options,
                    bool wrapu, bool wrapv, int)
    {
        osg::ref_ptr<osg::Image> image;
        if (options)
#if OSG_VERSION_LESS_THAN(3,4,0)
        image = osgDB::readImageFile(path, options);
#else
        image = osgDB::readRefImageFile(path, options);
#endif
  else
#if OSG_VERSION_LESS_THAN(3,4,0)
        image = osgDB::readImageFile(path);
#else
        image = osgDB::readRefImageFile(path);
#endif

        osg::ref_ptr<osg::Texture2D> texture = new osg::Texture2D;
        texture->setImage(image);
        texture->setMaxAnisotropy(SGSceneFeatures::instance()->getTextureFilter());

        if (staticTexture)
            texture->setDataVariance(osg::Object::STATIC);
        if (wrapu)
            texture->setWrap(osg::Texture::WRAP_S, osg::Texture::REPEAT);
  else
        texture->setWrap(osg::Texture::WRAP_S, osg::Texture::CLAMP);
        if (wrapv)
            texture->setWrap(osg::Texture::WRAP_T, osg::Texture::REPEAT);
  else
        texture->setWrap(osg::Texture::WRAP_T, osg::Texture::CLAMP);

        if (image) {
            int s = image->s();
            int t = image->t();

            if (s <= t && 32 <= s) {
                SGSceneFeatures::instance()->setTextureCompression(texture.get());
            } else if (t < s && 32 <= t) {
                SGSceneFeatures::instance()->setTextureCompression(texture.get());
            }
        }

        return texture.release();
    }

    namespace simgear
    {
        using namespace std;
        using namespace osg;
        using simgear::CopyOp;

        Node* copyModel(Node* model)
        {
    const CopyOp::CopyFlags flags = (CopyOp::DEEP_COPY_ALL
                & ~CopyOp::DEEP_COPY_TEXTURES
                & ~CopyOp::DEEP_COPY_IMAGES
                & ~CopyOp::DEEP_COPY_STATESETS
                & ~CopyOp::DEEP_COPY_STATEATTRIBUTES
                & ~CopyOp::DEEP_COPY_ARRAYS
                & ~CopyOp::DEEP_COPY_PRIMITIVES
                // This will preserve display lists ...
                & ~CopyOp::DEEP_COPY_DRAWABLES
                & ~CopyOp::DEEP_COPY_SHAPES);
            return (CopyOp(flags))(model);
        }

        TextureUpdateVisitor::TextureUpdateVisitor(const osgDB::FilePathList& pathList) :
        NodeAndDrawableVisitor(NodeVisitor::TRAVERSE_ALL_CHILDREN),
                _pathList(pathList)
        {
        }

        void TextureUpdateVisitor::apply(osg::Node& node)
        {
            StateSet* stateSet = cloneStateSet(node.getStateSet());
            if (stateSet)
                node.setStateSet(stateSet);
            traverse(node);
        }

        void TextureUpdateVisitor::apply(Drawable& drawable)
        {
            StateSet* stateSet = cloneStateSet(drawable.getStateSet());
            if (stateSet)
                drawable.setStateSet(stateSet);
        }

        Texture2D* TextureUpdateVisitor::textureReplace(int unit, const StateAttribute* attr)
        {
            using namespace osgDB;
    const Texture2D* texture = dynamic_cast<const Texture2D*>(attr);

            if (!texture)
                return 0;

    const Image* image = texture->getImage();
    const string* fullFilePath = 0;
            if (image) {
                // The currently loaded file name
                fullFilePath = &image->getFileName();
            } else {
                fullFilePath = &texture->getName();
            }

            // The short name
            string fileName = getSimpleFileName(*fullFilePath);
            if (fileName.empty())
                return 0;

            // The name that should be found with the current database path
            string fullLiveryFile = findFileInPath(fileName, _pathList);
            // If it is empty or they are identical then there is nothing to do
            if (fullLiveryFile.empty() || fullLiveryFile == *fullFilePath)
            return 0;

#if OSG_VERSION_LESS_THAN(3,4,0)
            Image* newImage = readImageFile(fullLiveryFile);
#else
            osg::ref_ptr<Image> newImage = readRefImageFile(fullLiveryFile);
#endif
            if (!newImage)
                return 0;

            CopyOp copyOp(CopyOp::DEEP_COPY_ALL & ~CopyOp::DEEP_COPY_IMAGES);
            Texture2D* newTexture = static_cast<Texture2D*>(copyOp(texture));
            if (!newTexture)
                return 0;

            newTexture->setImage(newImage);
#if OSG_VERSION_LESS_THAN(3,4,0)
            if (newImage->valid())
#else
            if (newImage.valid())
#endif
            {
                newTexture->setMaxAnisotropy(SGSceneFeatures::instance()->getTextureFilter());
            }

            return newTexture;
        }

        StateSet* TextureUpdateVisitor::cloneStateSet(const StateSet* stateSet)
        {
            typedef std::pair<int, Texture2D*> Tex2D;
            vector<Tex2D> newTextures;
            StateSet* result = 0;

            if (!stateSet)
                return 0;
            int numUnits = stateSet->getTextureAttributeList().size();
            if (numUnits > 0) {
                for (int i = 0; i < numUnits; ++i) {
            const StateAttribute* attr
                            = stateSet->getTextureAttribute(i, StateAttribute::TEXTURE);
                    Texture2D* newTexture = textureReplace(i, attr);
                    if (newTexture)
                        newTextures.push_back(Tex2D(i, newTexture));
                }
                if (!newTextures.empty()) {
                    result = static_cast<StateSet*>(stateSet->clone(CopyOp()));
                    for (vector<Tex2D>::iterator i = newTextures.begin();
                         i != newTextures.end();
                    ++i) {
                        result->setTextureAttribute(i->first, i->second);
                    }
                }
            }
            return result;
        }

        UserDataCopyVisitor::UserDataCopyVisitor() :
        NodeVisitor(NodeVisitor::NODE_VISITOR,
                NodeVisitor::TRAVERSE_ALL_CHILDREN)
        {
        }

        void UserDataCopyVisitor::apply(Node& node)
        {
            ref_ptr<SGSceneUserData> userData;
            userData = SGSceneUserData::getSceneUserData(&node);
            if (userData.valid()) {
                SGSceneUserData* newUserData  = new SGSceneUserData(*userData);
                newUserData->setVelocity(0);
                node.setUserData(newUserData);
            }
            node.traverse(*this);
        }

        namespace
        {
            class MakeEffectVisitor : public SplicingVisitor
            {
                public:
                typedef std::map<string, SGPropertyNode_ptr> EffectMap;
                using SplicingVisitor::apply;
                MakeEffectVisitor(const SGReaderWriterOptions* options = 0)
        : _options(options)
                {
                }
                virtual void apply(osg::Group& node);
                virtual void apply(osg::Geode& geode);
                EffectMap& getEffectMap() { return _effectMap; }
    const EffectMap& getEffectMap() const { return _effectMap; }
                void setDefaultEffect(SGPropertyNode* effect)
                {
                    _currentEffectParent = effect;
                }
                SGPropertyNode* getDefaultEffect() { return _currentEffectParent; }
                protected:
                EffectMap _effectMap;
                SGPropertyNode_ptr _currentEffectParent;
                osg::ref_ptr<const SGReaderWriterOptions> _options;
            };

            void MakeEffectVisitor::apply(osg::Group& node)
            {
                SGPropertyNode_ptr savedEffectRoot;
    const string& nodeName = node.getName();
                bool restoreEffect = false;
                if (!nodeName.empty()) {
                    EffectMap::iterator eitr = _effectMap.find(nodeName);
                    if (eitr != _effectMap.end()) {
                        savedEffectRoot = _currentEffectParent;
                        _currentEffectParent = eitr->second;
                        restoreEffect = true;
                    }
                }
                SplicingVisitor::apply(node);
                // If a new node was created, copy the user data too.
                ref_ptr<SGSceneUserData> userData = SGSceneUserData::getSceneUserData(&node);
                if (userData.valid() && _childStack.back().back().get() != &node)
                    _childStack.back().back()->setUserData(new SGSceneUserData(*userData));
                if (restoreEffect)
                    _currentEffectParent = savedEffectRoot;
            }

            void MakeEffectVisitor::apply(osg::Geode& geode)
            {
                if (pushNode(getNewNode(geode)))
                    return;
                osg::StateSet* ss = geode.getStateSet();
                if (!ss) {
                    pushNode(&geode);
                    return;
                }
                SGPropertyNode_ptr ssRoot = new SGPropertyNode;
                makeParametersFromStateSet(ssRoot, ss);
                SGPropertyNode_ptr effectRoot = new SGPropertyNode;
                effect::mergePropertyTrees(effectRoot, ssRoot, _currentEffectParent);
                Effect* effect = makeEffect(effectRoot, true, _options.get());
                EffectGeode* eg = dynamic_cast<EffectGeode*>(&geode);
                if (eg) {
                    eg->setEffect(effect);
                } else {
                    eg = new EffectGeode;
                    eg->setEffect(effect);
                    ref_ptr<SGSceneUserData> userData = SGSceneUserData::getSceneUserData(&geode);
                    if (userData.valid())
                        eg->setUserData(new SGSceneUserData(*userData));
                    for (unsigned i = 0; i < geode.getNumDrawables(); ++i) {
                        osg::Drawable *drawable = geode.getDrawable(i);
                        eg->addDrawable(drawable);

                        // Generate tangent vectors etc if needed
                        osg::Geometry *geom = dynamic_cast<osg::Geometry*>(drawable);
                        if(geom) eg->runGenerators(geom);
                    }
                }
                pushResultNode(&geode, eg);

            }

        }*/

    static class DefaultEffect {//: public simgear::Singleton<DefaultEffect>

        public DefaultEffect() {
            _effect = new SGPropertyNode();
            SGPropertyNode.makeChild(_effect/*.ptr()*/, "inherits-from").setStringValue("Effects/model-default");
        }

        //virtual ~DefaultEffect() {}
        SGPropertyNode getEffect() {
            return _effect/*.ptr()*/;
        }

        //protected:
        SGPropertyNode/*_ptr*/ _effect;
    }


    /**
     * FG uses a node visitor for building the effect, which replaces the node? Magic.
     * Anyway, this method is called *after* model building!
     * 28.10.2024: We create MaterialFactories not yet(maybe not needed).
     *
     * @param modelGroup
     * @param effectProps
     * @param options
     * @return
     */
    /*ref_ptr<*/
    public static /*Node*/ Map<String, MaterialFactory> instantiateEffects(/*osg::*/Node modelGroup, PropertyList effectProps, SGReaderWriterOptions options, String label) {
        SGPropertyNode/*_ptr*/ defaultEffectPropRoot = null;
        Map<String, MaterialFactory> factories = new HashMap<>();
        /*MakeEffectVisitor visitor(options);
        MakeEffectVisitor::EffectMap& emap = visitor.getEffectMap();*/
        //for (PropertyList::iterator itr = effectProps.begin(), end = effectProps.end(); itr != end; ++itr)
        int index=0;
        for (SGPropertyNode/*_ptr*/ configNode : effectProps) {
            logger.debug("instantiateEffects: effect (" + configNode.getStringValue() + ") with " + configNode.nChildren() + " children");
            //logger.debug("instantiateEffects: configNode=" + configNode.dump("\n"));

            //SGPropertyNode_ptr configNode = *itr;
            /*std::vector<SGPropertyNode_ptr>*/
            PropertyList objectNames = configNode.getChildren("object-name");
            SGPropertyNode defaultNode = configNode.getChild("default");
            if (defaultNode != null && defaultNode.getBool/*Value/*<bool>*/()) {
                defaultEffectPropRoot = configNode;
            }

            //MaterialFactory factoryForEffect = new FgMaterialFactory(effect);
            for (SGPropertyNode objNameNode : objectNames) {
                //if (SGReaderWriterXML.fgmodelloaddebug) {
                logger.debug("instantiateEffects: objNameNode=" + objNameNode.getStringValue());
                //}

                // FG uses MakeEffectVisitor (via emap) and finally MakeEffect.makeEffect(). For adding the objects to the effect??
                // Something magic happens with visitor for injecting effect building into AC loader? But the loader already is done here.

                //emap.insert(make_pair(objNameNode->getStringValue(), configNode));

                // FG-DIFF due to Effects. Just a quick workaround for transparency effect. Does not exist in FG.
                // 17.10.24: Appears to be nonsens. Set transparency *after* material building is questionable
                // and at this time here the objects were not yet built!??
                // 30.10.24: objects were already built!

                //factories.put(objNameNode.getStringValue(),factoryForEffect);
                /*29.10.24
                SGPropertyNode inherits_from = configNode.getChild("inherits-from");
                if (inherits_from != null && inherits_from.getStringValue().equals("Effects/model-transparent")) {*/
                if (true) {
                    String objname = objNameNode.getStringValue();
                    // 5.10.17: Nicht global suchen, denn durch async ist die Node noch nicht in den tree eingehangen.
                    // je nach Loader (zB. gltf kann es Nodesduibeltten geben. Darum alle druchgehen und die mit Mesh suchen.
                    List<SceneNode> nlist = modelGroup.findNodeByName(objname);
                    if (nlist.size() == 0) {
                        // Kommt wohl schon mal vor. "Lettering_Btns" z.B. wird in boeing.xml auf transparent gesetzt gibt es aber nicht
                        logger.warn("object not found: " + objname);
                    }
                    boolean meshfound = false;
                    for (SceneNode n : nlist) {
                        //logger.debug("setting transparency for node " + n.getName());
                        Mesh mesh = n.getMesh();
                        if (mesh != null) {
                            Material mat = mesh.getMaterial();
                            applyEffectToObject(configNode, mat, options, label+".effect."+index);
                            //mat.setTransparency(true);
                            //effect.apply();
                            meshfound = true;
                        }
                    }
                    if (!meshfound) {
                        logger.warn("no mesh for setting transparency.");// subtree:"+modelGroup.dump("",0));
                    }
                }
                // End FG-DIFF

            }
            // two lines not commented in FG
            //configNode.removeChild("default");
            //configNode.removeChildren("object-name");
            index++;
        }
        if (!(defaultEffectPropRoot != null)) {
            //defaultEffectPropRoot = new DefaultEffect::instance () -> getEffect();
        }
        /*visitor.setDefaultEffect(defaultEffectPropRoot.ptr());
        modelGroup -> accept(visitor);
        osg::NodeList & result = visitor.getResults();
        return ref_ptr < Node > (result[0].get());*/
        logger.debug(factories.size() + " material factories built");
        return factories;//modelGroup;
    }

    /**
     * Try to build effect here like MakeEffectVisitor does.
     */
    static private void applyEffectToObject(SGPropertyNode configNode, Material material,SGReaderWriterOptions options, String label){
        //
        SGPropertyNode ssRoot = new SGPropertyNode();
        Effect.makeParametersFromStateSet(ssRoot/*StateSet is from OSG, ss*/);
        SGPropertyNode effectRoot = new SGPropertyNode();
        // No idea if using configNode instead of _currentEffectParent is good here
        MakeEffect.mergePropertyTrees(effectRoot, ssRoot,configNode/* _currentEffectParent*/);
        Effect effect = MakeEffect.makeEffect(effectRoot, true, options,label, false);

    }
}

