package de.yard.threed.flightgear.core.simgear.scene.material;

import de.yard.threed.core.Color;
import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.flightgear.core.PropertyList;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.scene.util.SGReaderWriterOptions;


import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.platform.common.EffectShader;

import java.util.ArrayList;
import java.util.List;

import static de.yard.threed.engine.util.XmlHelper.getBooleanValue;
import static de.yard.threed.flightgear.core.simgear.scene.material.MakeEffect.mergePropertyTrees;

/**
 * From Effect.cxx
 * <p>
 * 9.3.21: MA31 Was once joined with Effect in engine.
 * 17.10.24: Now decoupled from Effect in tcp-22. Might be a component some time. Renamed back from FGEffect to Effect.
 * An Effect seems to be just an effect (property tree) definition initially. Shader, textures, etc
 * are created later when needed (by realizeTechniques()?).
 * <p>
 * Created by thomass on 30.10.15.
 */
public class Effect /*extends Effect osg::Object */ {
    Log logger = Platform.getInstance().getLog(Effect.class);
    // 27.12.17: Jetzt eine Abnstraktionsstufe hoeher wegen preProcess 
    private PortableMaterial materialdefinition = null;
    //Material material = null;

    // made private with constructor.
    private String name;
    // label as debugging/logging helper
    public String label;
    public SGPropertyNode root, parametersProp;
    boolean _isRealized;

    //20.7.16 public boolean transparent = false;
    //11.3.16: Der Shader ist optional. Wenn er nicht angegeben ist, muss die Platform sehen, wie sie den Effekt hinbekommt.
    // Wenn ein Shader angegebn ist, wird er in allen Platformen verwendet.
    public EffectShader shader = null;

    //std::vector<osg::ref_ptr<Technique> > techniques;
    List<Technique> techniques = new ArrayList<>();

    /**
     * 21.10.24: root and parametersProp made private with constructor. "parent" is optional.
     */
    public Effect(String name, SGPropertyNode prop, Effect parent, String label) {
        this.name = name;
        this.label = label;
        if (parent == null) {
            root = prop;
            parametersProp = root.getChild("parameters");
        } else {

            root = new SGPropertyNode();
            mergePropertyTrees(root, prop, parent.root);
            parametersProp = root.getChild("parameters");
        }
    }

    /**
     * Erstmal zum Einstieg
     */
    private Effect(String name) {
        this.name = name;
        shader = new EffectShader();

    }

    void buildPass(Effect effect, Technique tniq, SGPropertyNode prop,
                   SGReaderWriterOptions options) {
        Pass pass = new Pass();
        tniq.passes.add(pass);
        // Loop through "lighting", "depth", "material", "blend", a.s.o.
        for (int i = 0; i < prop.nChildren(); ++i) {
            SGPropertyNode attrProp = prop.getChild(i);
            EffectBuilder.PassAttributeBuilder builder = EffectBuilder.PassAttributeBuilder.find(attrProp.getNameString());
            if (builder != null)
                builder.buildAttribute(effect, pass, attrProp, options);
            else
                logger.warn("skipping unknown pass attribute " + attrProp.getName());
        }
    }

    /*
        osg::Vec4f getColor(const SGPropertyNode* prop)
        {
            if (prop->nChildren() == 0) {
                if (prop->getType() == props::VEC4D) {
                    return osg::Vec4f(toOsg(prop->getValue<SGVec4d>()));
                } else if (prop->getType() == props::VEC3D) {
                    return osg::Vec4f(toOsg(prop->getValue<SGVec3d>()), 1.0f);
                } else {
                    SG_LOG(SG_INPUT, SG_ALERT,
                            "invalid color property " << prop->getName() << " "
                                    << prop->getStringValue());
                    return osg::Vec4f(0.0f, 0.0f, 0.0f, 1.0f);
                }
            } else {
                osg::Vec4f result;
                static const char* colors[] = {"r", "g", "b"};
                for (int i = 0; i < 3; ++i) {
                const SGPropertyNode* componentProp = prop->getChild(colors[i]);
                    result[i] = componentProp ? componentProp->getValue<float>() : 0.0f;
                }
            const SGPropertyNode* alphaProp = prop->getChild("a");
                result[3] = alphaProp ? alphaProp->getValue<float>() : 1.0f;
                return result;
            }
        }

        struct LightingBuilder : public PassAttributeBuilder
        {
            void buildAttribute(Effect* effect, Pass* pass, const SGPropertyNode* prop,
                            const SGReaderWriterOptions* options);
        };

        void LightingBuilder::buildAttribute(Effect* effect, Pass* pass,
                                         const SGPropertyNode* prop,
                                         const SGReaderWriterOptions* options)
        {
        const SGPropertyNode* realProp = getEffectPropertyNode(effect, prop);
            if (!realProp)
                return;
            pass->setMode(GL_LIGHTING, (realProp->getValue<bool>() ? StateAttribute::ON
                    : StateAttribute::OFF));
        }

        InstallAttributeBuilder<LightingBuilder> installLighting("lighting");

        struct ShadeModelBuilder : public PassAttributeBuilder
        {
            void buildAttribute(Effect* effect, Pass* pass, const SGPropertyNode* prop,
                            const SGReaderWriterOptions* options)
            {
            const SGPropertyNode* realProp = getEffectPropertyNode(effect, prop);
                if (!realProp)
                    return;
                StateAttributeFactory *attrFact = StateAttributeFactory::instance();
                string propVal = realProp->getStringValue();
                if (propVal == "flat")
                    pass->setAttribute(attrFact->getFlatShadeModel());
                else if (propVal == "smooth")
                    pass->setAttribute(attrFact->getSmoothShadeModel());
                else
                    SG_LOG(SG_INPUT, SG_ALERT,
                            "invalid shade model property " << propVal);
            }
        };

        InstallAttributeBuilder<ShadeModelBuilder> installShadeModel("shade-model");

        struct CullFaceBuilder : PassAttributeBuilder
        {
            void buildAttribute(Effect* effect, Pass* pass, const SGPropertyNode* prop,
                            const SGReaderWriterOptions* options)
            {
            const SGPropertyNode* realProp = getEffectPropertyNode(effect, prop);
                if (!realProp) {
                    pass->setMode(GL_CULL_FACE, StateAttribute::OFF);
                    return;
                }
                StateAttributeFactory *attrFact = StateAttributeFactory::instance();
                string propVal = realProp->getStringValue();
                if (propVal == "front")
                    pass->setAttributeAndModes(attrFact->getCullFaceFront());
                else if (propVal == "back")
                    pass->setAttributeAndModes(attrFact->getCullFaceBack());
                else if (propVal == "front-back")
                    pass->setAttributeAndModes(new CullFace(CullFace::FRONT_AND_BACK));
                else if (propVal == "off")
                    pass->setMode(GL_CULL_FACE, StateAttribute::OFF);
                else
                    SG_LOG(SG_INPUT, SG_ALERT,
                            "invalid cull face property " << propVal);
            }
        };

        InstallAttributeBuilder<CullFaceBuilder> installCullFace("cull-face");

        struct ColorMaskBuilder : PassAttributeBuilder
        {
            void buildAttribute(Effect* effect, Pass* pass, const SGPropertyNode* prop,
                            const SGReaderWriterOptions* options)
            {
            const SGPropertyNode* realProp = getEffectPropertyNode(effect, prop);
                if (!realProp)
                    return;

                ColorMask *mask = new ColorMask;
                Vec4 m = getColor(realProp);
                mask->setMask(m.r() > 0.0, m.g() > 0.0, m.b() > 0.0, m.a() > 0.0);
                pass->setAttributeAndModes(mask);
            }
        };

        InstallAttributeBuilder<ColorMaskBuilder> installColorMask("color-mask");

        EffectNameValue<StateSet::RenderingHint> renderingHintInit[] =
        {
            { "default", StateSet::DEFAULT_BIN },
            { "opaque", StateSet::OPAQUE_BIN },
            { "transparent", StateSet::TRANSPARENT_BIN }
        };

        EffectPropertyMap<StateSet::RenderingHint> renderingHints(renderingHintInit);

        struct HintBuilder : public PassAttributeBuilder
        {
            void buildAttribute(Effect* effect, Pass* pass, const SGPropertyNode* prop,
                            const SGReaderWriterOptions* options)
            {
            const SGPropertyNode* realProp = getEffectPropertyNode(effect, prop);
                if (!realProp)
                    return;
                StateSet::RenderingHint renderingHint = StateSet::DEFAULT_BIN;
                findAttr(renderingHints, realProp, renderingHint);
                pass->setRenderingHint(renderingHint);
            }
        };

        InstallAttributeBuilder<HintBuilder> installHint("rendering-hint");

        struct RenderBinBuilder : public PassAttributeBuilder
        {
            void buildAttribute(Effect* effect, Pass* pass, const SGPropertyNode* prop,
                            const SGReaderWriterOptions* options)
            {
                if (!isAttributeActive(effect, prop))
                    return;
            const SGPropertyNode* binProp = prop->getChild("bin-number");
                binProp = getEffectPropertyNode(effect, binProp);
            const SGPropertyNode* nameProp = prop->getChild("bin-name");
                nameProp = getEffectPropertyNode(effect, nameProp);
                if (binProp && nameProp) {
                    pass->setRenderBinDetails(binProp->getIntValue(),
                            nameProp->getStringValue());
                } else {
                    if (!binProp)
                        SG_LOG(SG_INPUT, SG_ALERT,
                                "No render bin number specified in render bin section");
                    if (!nameProp)
                        SG_LOG(SG_INPUT, SG_ALERT,
                                "No render bin name specified in render bin section");
                }
            }
        };

        InstallAttributeBuilder<RenderBinBuilder> installRenderBin("render-bin");

        struct MaterialBuilder : public PassAttributeBuilder
        {
            void buildAttribute(Effect* effect, Pass* pass, const SGPropertyNode* prop,
                            const SGReaderWriterOptions* options);
        };

        EffectNameValue<Material::ColorMode> colorModeInit[] =
        {
            { "ambient", Material::AMBIENT },
            { "ambient-and-diffuse", Material::AMBIENT_AND_DIFFUSE },
            { "diffuse", Material::DIFFUSE },
            { "emissive", Material::EMISSION },
            { "specular", Material::SPECULAR },
            { "off", Material::OFF }
        };
        EffectPropertyMap<Material::ColorMode> colorModes(colorModeInit);

        void MaterialBuilder::buildAttribute(Effect* effect, Pass* pass,
                                         const SGPropertyNode* prop,
                                         const SGReaderWriterOptions* options)
        {
            if (!isAttributeActive(effect, prop))
                return;
            Material* mat = new Material;
        const SGPropertyNode* color = 0;
            if ((color = getEffectPropertyChild(effect, prop, "ambient")))
                mat->setAmbient(Material::FRONT_AND_BACK, getColor(color));
            if ((color = getEffectPropertyChild(effect, prop, "ambient-front")))
                mat->setAmbient(Material::FRONT, getColor(color));
            if ((color = getEffectPropertyChild(effect, prop, "ambient-back")))
                mat->setAmbient(Material::BACK, getColor(color));
            if ((color = getEffectPropertyChild(effect, prop, "diffuse")))
                mat->setDiffuse(Material::FRONT_AND_BACK, getColor(color));
            if ((color = getEffectPropertyChild(effect, prop, "diffuse-front")))
                mat->setDiffuse(Material::FRONT, getColor(color));
            if ((color = getEffectPropertyChild(effect, prop, "diffuse-back")))
                mat->setDiffuse(Material::BACK, getColor(color));
            if ((color = getEffectPropertyChild(effect, prop, "specular")))
                mat->setSpecular(Material::FRONT_AND_BACK, getColor(color));
            if ((color = getEffectPropertyChild(effect, prop, "specular-front")))
                mat->setSpecular(Material::FRONT, getColor(color));
            if ((color = getEffectPropertyChild(effect, prop, "specular-back")))
                mat->setSpecular(Material::BACK, getColor(color));
            if ((color = getEffectPropertyChild(effect, prop, "emissive")))
                mat->setEmission(Material::FRONT_AND_BACK, getColor(color));
            if ((color = getEffectPropertyChild(effect, prop, "emissive-front")))
                mat->setEmission(Material::FRONT, getColor(color));
            if ((color = getEffectPropertyChild(effect, prop, "emissive-back")))
                mat->setEmission(Material::BACK, getColor(color));
        const SGPropertyNode* shininess = 0;
            mat->setShininess(Material::FRONT_AND_BACK, 0.0f);
            if ((shininess = getEffectPropertyChild(effect, prop, "shininess")))
                mat->setShininess(Material::FRONT_AND_BACK, shininess->getFloatValue());
            if ((shininess = getEffectPropertyChild(effect, prop, "shininess-front")))
                mat->setShininess(Material::FRONT, shininess->getFloatValue());
            if ((shininess = getEffectPropertyChild(effect, prop, "shininess-back")))
                mat->setShininess(Material::BACK, shininess->getFloatValue());
            Material::ColorMode colorMode = Material::OFF;
            findAttr(colorModes, getEffectPropertyChild(effect, prop, "color-mode"),
                    colorMode);
            mat->setColorMode(colorMode);
            pass->setAttribute(mat);
        }

        InstallAttributeBuilder<MaterialBuilder> installMaterial("material");

        EffectNameValue<BlendFunc::BlendFuncMode> blendFuncModesInit[] =
        {
            {"dst-alpha", BlendFunc::DST_ALPHA},
            {"dst-color", BlendFunc::DST_COLOR},
            {"one", BlendFunc::ONE},
            {"one-minus-dst-alpha", BlendFunc::ONE_MINUS_DST_ALPHA},
            {"one-minus-dst-color", BlendFunc::ONE_MINUS_DST_COLOR},
            {"one-minus-src-alpha", BlendFunc::ONE_MINUS_SRC_ALPHA},
            {"one-minus-src-color", BlendFunc::ONE_MINUS_SRC_COLOR},
            {"src-alpha", BlendFunc::SRC_ALPHA},
            {"src-alpha-saturate", BlendFunc::SRC_ALPHA_SATURATE},
            {"src-color", BlendFunc::SRC_COLOR},
            {"constant-color", BlendFunc::CONSTANT_COLOR},
            {"one-minus-constant-color", BlendFunc::ONE_MINUS_CONSTANT_COLOR},
            {"constant-alpha", BlendFunc::CONSTANT_ALPHA},
            {"one-minus-constant-alpha", BlendFunc::ONE_MINUS_CONSTANT_ALPHA},
            {"zero", BlendFunc::ZERO}
        };
        EffectPropertyMap<BlendFunc::BlendFuncMode> blendFuncModes(blendFuncModesInit);
    */

    /**
     * Until we have full blend functions we consider this to be the main use case for blending, semi transparency.
     *
     * See https://www.khronos.org/opengl/wiki/blending for blending.
     */
    class BlendBuilder extends EffectBuilder.PassAttributeBuilder {

        @Override
        void buildAttribute(Effect effect, Pass pass, SGPropertyNode prop,
                            SGReaderWriterOptions options) {
            if (!EffectBuilder.isAttributeActive(effect, prop))
                return;
            // XXX Compatibility with early <blend> syntax; should go away
            // before a release
            SGPropertyNode realProp = EffectBuilder.getEffectPropertyNode(effect, prop);
            if (realProp == null)
                return;
            if (realProp.nChildren() == 0) {
                // enable/disable blending
                // pass.setMode(GL_BLEND, (realProp -> getBoolValue() ? StateAttribute::ON : StateAttribute::OFF));
                pass.setBlending(realProp.getBoolValue());
                return;
            }

            SGPropertyNode pmode = EffectBuilder.getEffectPropertyChild(effect, prop, "mode");
            // XXX When dynamic parameters are supported, this code should
            // create the blend function even if the mode is off.
            if (pmode != null && !pmode.getBoolValue()) {
                // pass.setMode(GL_BLEND, StateAttribute::OFF);
                pass.setBlending(false);
                return;
            }

            parseBlendFunc.parseBlendFunc(
                    pass,
                    EffectBuilder.getEffectPropertyChild(effect, prop, "source"),
                    EffectBuilder.getEffectPropertyChild(effect, prop, "destination"),
                    EffectBuilder.getEffectPropertyChild(effect, prop, "source-rgb"),
                    EffectBuilder.getEffectPropertyChild(effect, prop, "destination-rgb"),
                    EffectBuilder.getEffectPropertyChild(effect, prop, "source-alpha"),
                    EffectBuilder.getEffectPropertyChild(effect, prop, "destination-alpha")
            );
        }
    }

    //InstallAttributeBuilder<BlendBuilder> installBlend("blend");
    EffectBuilder.PassAttributeBuilder installBlend = EffectBuilder.PassAttributeBuilder.passAttrMap.put("blend", new BlendBuilder());

/*
    EffectNameValue<Stencil::Function> stencilFunctionInit[] =
    {
        {"never", Stencil::NEVER },
        {"less", Stencil::LESS},
        {"equal", Stencil::EQUAL},
        {"less-or-equal", Stencil::LEQUAL},
        {"greater", Stencil::GREATER},
        {"not-equal", Stencil::NOTEQUAL},
        {"greater-or-equal", Stencil::GEQUAL},
        {"always", Stencil::ALWAYS}
    };

    EffectPropertyMap<Stencil::Function> stencilFunction(stencilFunctionInit);

    EffectNameValue<Stencil::Operation> stencilOperationInit[] =
    {
        {"keep", Stencil::KEEP},
        {"zero", Stencil::ZERO},
        {"replace", Stencil::REPLACE},
        {"increase", Stencil::INCR},
        {"decrease", Stencil::DECR},
        {"invert", Stencil::INVERT},
        {"increase-wrap", Stencil::INCR_WRAP},
        {"decrease-wrap", Stencil::DECR_WRAP}
    };

    EffectPropertyMap<Stencil::Operation> stencilOperation(stencilOperationInit);

    struct StencilBuilder : public PassAttributeBuilder
    {
        void buildAttribute(Effect* effect, Pass* pass, const SGPropertyNode* prop,
                        const SGReaderWriterOptions* options)
        {
            if (!isAttributeActive(effect, prop))
                return;

        const SGPropertyNode* pmode = getEffectPropertyChild(effect, prop,
                "mode");
            if (pmode && !pmode->getValue<bool>()) {
                pass->setMode(GL_STENCIL, StateAttribute::OFF);
                return;
            }
        const SGPropertyNode* pfunction
                = getEffectPropertyChild(effect, prop, "function");
        const SGPropertyNode* pvalue
                = getEffectPropertyChild(effect, prop, "value");
        const SGPropertyNode* pmask
                = getEffectPropertyChild(effect, prop, "mask");
        const SGPropertyNode* psfail
                = getEffectPropertyChild(effect, prop, "stencil-fail");
        const SGPropertyNode* pzfail
                = getEffectPropertyChild(effect, prop, "z-fail");
        const SGPropertyNode* ppass
                = getEffectPropertyChild(effect, prop, "pass");

            Stencil::Function func = Stencil::ALWAYS;  // Always pass
            int ref = 0;
            unsigned int mask = ~0u;  // All bits on
            Stencil::Operation sfailop = Stencil::KEEP;  // Keep the old values as default
            Stencil::Operation zfailop = Stencil::KEEP;
            Stencil::Operation passop = Stencil::KEEP;

            ref_ptr<Stencil> stencilFunc = new Stencil;

            if (pfunction)
                findAttr(stencilFunction, pfunction, func);
            if (pvalue)
                ref = pvalue->getIntValue();
            if (pmask)
                mask = pmask->getIntValue();

            if (psfail)
                findAttr(stencilOperation, psfail, sfailop);
            if (pzfail)
                findAttr(stencilOperation, pzfail, zfailop);
            if (ppass)
                findAttr(stencilOperation, ppass, passop);

            // Set the stencil operation
            stencilFunc->setFunction(func, ref, mask);

            // Set the operation, s-fail, s-pass/z-fail, s-pass/z-pass
            stencilFunc->setOperation(sfailop, zfailop, passop);

            // Add the operation to pass
            pass->setAttributeAndModes(stencilFunc.get());
        }
    };

    InstallAttributeBuilder<StencilBuilder> installStencil("stencil");

    struct AlphaToCoverageBuilder : public PassAttributeBuilder
    {
        void buildAttribute(Effect* effect, Pass* pass, const SGPropertyNode* prop,
                        const SGReaderWriterOptions* options);
    };

#ifndef GL_SAMPLE_ALPHA_TO_COVERAGE_ARB
#define GL_SAMPLE_ALPHA_TO_COVERAGE_ARB 0x809E
            #endif

    void AlphaToCoverageBuilder::buildAttribute(Effect* effect, Pass* pass,
                                     const SGPropertyNode* prop,
                                     const SGReaderWriterOptions* options)
    {
    const SGPropertyNode* realProp = getEffectPropertyNode(effect, prop);
        if (!realProp)
            return;
        pass->setMode(GL_SAMPLE_ALPHA_TO_COVERAGE_ARB, (realProp->getValue<bool>() ?
                StateAttribute::ON : StateAttribute::OFF));
    }

    InstallAttributeBuilder<AlphaToCoverageBuilder> installAlphaToCoverage("alpha-to-coverage");

    EffectNameValue<AlphaFunc::ComparisonFunction> alphaComparisonInit[] =
    {
        {"never", AlphaFunc::NEVER},
        {"less", AlphaFunc::LESS},
        {"equal", AlphaFunc::EQUAL},
        {"lequal", AlphaFunc::LEQUAL},
        {"greater", AlphaFunc::GREATER},
        {"notequal", AlphaFunc::NOTEQUAL},
        {"gequal", AlphaFunc::GEQUAL},
        {"always", AlphaFunc::ALWAYS}
    };
    EffectPropertyMap<AlphaFunc::ComparisonFunction>
    alphaComparison(alphaComparisonInit);

    struct AlphaTestBuilder : public PassAttributeBuilder
    {
        void buildAttribute(Effect* effect, Pass* pass, const SGPropertyNode* prop,
                        const SGReaderWriterOptions* options)
        {
            if (!isAttributeActive(effect, prop))
                return;
            // XXX Compatibility with early <alpha-test> syntax; should go away
            // before a release
        const SGPropertyNode* realProp = getEffectPropertyNode(effect, prop);
            if (!realProp)
                return;
            if (realProp->nChildren() == 0) {
                pass->setMode(GL_ALPHA_TEST, (realProp->getBoolValue()
                        ? StateAttribute::ON
                        : StateAttribute::OFF));
                return;
            }

        const SGPropertyNode* pmode = getEffectPropertyChild(effect, prop,
                "mode");
            // XXX When dynamic parameters are supported, this code should
            // create the blend function even if the mode is off.
            if (pmode && !pmode->getValue<bool>()) {
                pass->setMode(GL_ALPHA_TEST, StateAttribute::OFF);
                return;
            }
        const SGPropertyNode* pComp = getEffectPropertyChild(effect, prop,
                "comparison");
        const SGPropertyNode* pRef = getEffectPropertyChild(effect, prop,
                "reference");

            AlphaFunc::ComparisonFunction func = AlphaFunc::ALWAYS;
            float refValue = 1.0f;
            if (pComp)
                findAttr(alphaComparison, pComp, func);
            if (pRef)
                refValue = pRef->getValue<float>();
            if (func == AlphaFunc::GREATER && osg::equivalent(refValue, 1.0f)) {
            pass->setAttributeAndModes(StateAttributeFactory::instance()
                    ->getStandardAlphaFunc());
        } else {
            AlphaFunc* alphaFunc = new AlphaFunc;
            alphaFunc->setFunction(func);
            alphaFunc->setReferenceValue(refValue);
            pass->setAttributeAndModes(alphaFunc);
        }
        }
    };

    InstallAttributeBuilder<AlphaTestBuilder> installAlphaTest("alpha-test");

    InstallAttributeBuilder<TextureUnitBuilder> textureUnitBuilder("texture-unit");

    // Shader key, used both for shaders with relative and absolute names
    typedef pair<string, int> ShaderKey;

    inline ShaderKey makeShaderKey(SGPropertyNode_ptr& ptr, int shaderType)
    {
        return ShaderKey(ptr->getStringValue(), shaderType);
    }

    struct ProgramKey
    {
        typedef pair<string, int> AttribKey;
        osgDB::FilePathList paths;
        vector<ShaderKey> shaders;
        vector<AttribKey> attributes;
        struct EqualTo
        {
            bool operator()(const ProgramKey& lhs, const ProgramKey& rhs) const
            {
                return (lhs.paths.size() == rhs.paths.size()
                        && equal(lhs.paths.begin(), lhs.paths.end(),
                        rhs.paths.begin())
                        && lhs.shaders.size() == rhs.shaders.size()
                        && equal (lhs.shaders.begin(), lhs.shaders.end(),
                        rhs.shaders.begin())
                        && lhs.attributes.size() == rhs.attributes.size()
                        && equal(lhs.attributes.begin(), lhs.attributes.end(),
                        rhs.attributes.begin()));
            }
        };
    };

    size_t hash_value(const ProgramKey& key)
    {
        size_t seed = 0;
        boost::hash_range(seed, key.paths.begin(), key.paths.end());
        boost::hash_range(seed, key.shaders.begin(), key.shaders.end());
        boost::hash_range(seed, key.attributes.begin(), key.attributes.end());
        return seed;
    }

// XXX Should these be protected by a mutex? Probably

    typedef std::unordered_map<ProgramKey, ref_ptr<Program>,
            boost::hash<ProgramKey>, ProgramKey::EqualTo>
    ProgramMap;
    ProgramMap programMap;
    ProgramMap resolvedProgramMap;  // map with resolved shader file names

    typedef std::unordered_map<ShaderKey, ref_ptr<Shader>, boost::hash<ShaderKey> >
    ShaderMap;
    ShaderMap shaderMap;

    void reload_shaders()
    {
        for(ShaderMap::iterator sitr = shaderMap.begin(); sitr != shaderMap.end(); ++sitr)
        {
            Shader *shader = sitr->second.get();
            string fileName = SGModelLib::findDataFile(sitr->first.first);
            if (!fileName.empty()) {
                shader->loadShaderSourceFromFile(fileName);
            }
            else
                SG_LOG(SG_INPUT, SG_ALERT, "Could not locate shader: " << fileName);

        }
    }

    struct ShaderProgramBuilder : PassAttributeBuilder
    {
        void buildAttribute(Effect* effect, Pass* pass, const SGPropertyNode* prop,
                        const SGReaderWriterOptions* options);
    };


    EffectNameValue<GLint> geometryInputTypeInit[] =
            {
                    {"points", GL_POINTS},
                    {"lines", GL_LINES},
                    {"lines-adjacency", GL_LINES_ADJACENCY_EXT},
                    {"triangles", GL_TRIANGLES},
                    {"triangles-adjacency", GL_TRIANGLES_ADJACENCY_EXT},
            };
    EffectPropertyMap<GLint>
    geometryInputType(geometryInputTypeInit);


    EffectNameValue<GLint> geometryOutputTypeInit[] =
            {
                    {"points", GL_POINTS},
                    {"line-strip", GL_LINE_STRIP},
                    {"triangle-strip", GL_TRIANGLE_STRIP}
            };
    EffectPropertyMap<GLint>
    geometryOutputType(geometryOutputTypeInit);

    void ShaderProgramBuilder::buildAttribute(Effect* effect, Pass* pass,
                                          const SGPropertyNode* prop,
                                          const SGReaderWriterOptions*
                                                      options)
    {
        using namespace boost;
        if (!isAttributeActive(effect, prop))
            return;
        PropertyList pVertShaders = prop->getChildren("vertex-shader");
        PropertyList pGeomShaders = prop->getChildren("geometry-shader");
        PropertyList pFragShaders = prop->getChildren("fragment-shader");
        PropertyList pAttributes = prop->getChildren("attribute");
        ProgramKey prgKey;
        std::back_insert_iterator<vector<ShaderKey> > inserter(prgKey.shaders);
        transform(pVertShaders.begin(), pVertShaders.end(), inserter,
                boost::bind(makeShaderKey, _1, Shader::VERTEX));
        transform(pGeomShaders.begin(), pGeomShaders.end(), inserter,
                boost::bind(makeShaderKey, _1, Shader::GEOMETRY));
        transform(pFragShaders.begin(), pFragShaders.end(), inserter,
                boost::bind(makeShaderKey, _1, Shader::FRAGMENT));
        for (PropertyList::iterator itr = pAttributes.begin(),
        e = pAttributes.end();
        itr != e;
        ++itr) {
        const SGPropertyNode* pName = getEffectPropertyChild(effect, *itr,
                "name");
        const SGPropertyNode* pIndex = getEffectPropertyChild(effect, *itr,
                "index");
        if (!pName || ! pIndex)
            throw BuilderException("malformed attribute property");
        prgKey.attributes
                .push_back(ProgramKey::AttribKey(pName->getStringValue(),
                pIndex->getValue<int>()));
    }
        if (options)
            prgKey.paths = options->getDatabasePathList();
        Program* program = 0;
        ProgramMap::iterator pitr = programMap.find(prgKey);
        if (pitr != programMap.end()) {
            program = pitr->second.get();
            pass->setAttributeAndModes(program);
            return;
        }
        // The program wasn't in the map using the load path passed in with
        // the options, but it might have already been loaded using a
        // different load path i.e., its shaders were found in the fg data
        // directory. So, resolve the shaders' file names and look in the
        // resolvedProgramMap for a program using those shaders.
        ProgramKey resolvedKey;
        resolvedKey.attributes = prgKey.attributes;
        BOOST_FOREACH(const ShaderKey& shaderKey, prgKey.shaders)
        {
        const string& shaderName = shaderKey.first;
            Shader::Type stype = (Shader::Type)shaderKey.second;
            string fileName = SGModelLib::findDataFile(shaderName, options);
            if (fileName.empty())
            {
                SG_LOG(SG_INPUT, SG_ALERT, "Could not locate shader" << shaderName);


                throw BuilderException(string("couldn't find shader ") +
                        shaderName);
            }
            resolvedKey.shaders.push_back(ShaderKey(fileName, stype));
        }
        ProgramMap::iterator resitr = resolvedProgramMap.find(resolvedKey);
        if (resitr != resolvedProgramMap.end()) {
            program = resitr->second.get();
            programMap.insert(ProgramMap::value_type(prgKey, program));
            pass->setAttributeAndModes(program);
            return;
        }
        program = new Program;
        BOOST_FOREACH(const ShaderKey& skey, resolvedKey.shaders)
        {
        const string& fileName = skey.first;
            Shader::Type stype = (Shader::Type)skey.second;
            ShaderMap::iterator sitr = shaderMap.find(skey);
            if (sitr != shaderMap.end()) {
                program->addShader(sitr->second.get());
            } else {
                ref_ptr<Shader> shader = new Shader(stype);
                shader->setName(fileName);
                if (shader->loadShaderSourceFromFile(fileName)) {
                    program->addShader(shader.get());
                    shaderMap.insert(ShaderMap::value_type(skey, shader));
                }
            }
        }
        BOOST_FOREACH(const ProgramKey::AttribKey& key, prgKey.attributes) {
        program->addBindAttribLocation(key.first, key.second);
    }
    const SGPropertyNode* pGeometryVerticesOut
            = getEffectPropertyChild(effect, prop, "geometry-vertices-out");
        if (pGeometryVerticesOut)
            program->setParameter(GL_GEOMETRY_VERTICES_OUT_EXT,
                    pGeometryVerticesOut->getIntValue());
    const SGPropertyNode* pGeometryInputType
            = getEffectPropertyChild(effect, prop, "geometry-input-type");
        if (pGeometryInputType) {
            GLint type;
            findAttr(geometryInputType, pGeometryInputType->getStringValue(), type);
            program->setParameter(GL_GEOMETRY_INPUT_TYPE_EXT, type);
        }
    const SGPropertyNode* pGeometryOutputType
            = getEffectPropertyChild(effect, prop, "geometry-output-type");
        if (pGeometryOutputType) {
            GLint type;
            findAttr(geometryOutputType, pGeometryOutputType->getStringValue(),
                    type);
            program->setParameter(GL_GEOMETRY_OUTPUT_TYPE_EXT, type);
        }
        PropertyList pUniformBlockBindings
                = prop->getChildren("uniform-block-binding");
        for (const auto &pUniformBlockBinding : pUniformBlockBindings) {
        program->addBindUniformBlock(
                pUniformBlockBinding->getStringValue("name"),
                pUniformBlockBinding->getIntValue("index"));
    }
        programMap.insert(ProgramMap::value_type(prgKey, program));
        resolvedProgramMap.insert(ProgramMap::value_type(resolvedKey, program));
        pass->setAttributeAndModes(program);
    }

    InstallAttributeBuilder<ShaderProgramBuilder> installShaderProgram("program");

    EffectNameValue<Uniform::Type> uniformTypesInit[] =
    {
        {"bool", Uniform::BOOL},
        {"int", Uniform::INT},
        {"float", Uniform::FLOAT},
        {"float-vec3", Uniform::FLOAT_VEC3},
        {"float-vec4", Uniform::FLOAT_VEC4},
        {"sampler-1d", Uniform::SAMPLER_1D},
        {"sampler-1d-shadow", Uniform::SAMPLER_1D_SHADOW},
        {"sampler-2d", Uniform::SAMPLER_2D},
        {"sampler-2d-shadow", Uniform::SAMPLER_2D_SHADOW},
        {"sampler-3d", Uniform::SAMPLER_3D},
        {"sampler-cube", Uniform::SAMPLER_CUBE}
    };
    EffectPropertyMap<Uniform::Type> uniformTypes(uniformTypesInit);

// Optimization hack for common uniforms.
// XXX protect these with a mutex?

    ref_ptr<Uniform> texture0;
    ref_ptr<Uniform> colorMode[3];

    struct UniformBuilder :public PassAttributeBuilder
    {
        void buildAttribute(Effect* effect, Pass* pass, const SGPropertyNode* prop,
                        const SGReaderWriterOptions* options)
        {
            if (!texture0.valid()) {
                texture0 = new Uniform(Uniform::SAMPLER_2D, "texture");
                texture0->set(0);
                texture0->setDataVariance(Object::STATIC);
                for (int i = 0; i < 3; ++i) {
                    colorMode[i] = new Uniform(Uniform::INT, "colorMode");
                    colorMode[i]->set(i);
                    colorMode[i]->setDataVariance(Object::STATIC);
                }
            }
            if (!isAttributeActive(effect, prop))
                return;
            SGConstPropertyNode_ptr nameProp = prop->getChild("name");
            SGConstPropertyNode_ptr typeProp = prop->getChild("type");
            SGConstPropertyNode_ptr positionedProp = prop->getChild("positioned");
            SGConstPropertyNode_ptr valProp = prop->getChild("value");
            string name;
            Uniform::Type uniformType = Uniform::FLOAT;
            if (nameProp) {
                name = nameProp->getStringValue();
            } else {
                SG_LOG(SG_INPUT, SG_ALERT, "No name for uniform property ");
                return;
            }
            if (!valProp) {
                SG_LOG(SG_INPUT, SG_ALERT, "No value for uniform property "
                        << name);
                return;
            }
            if (!typeProp) {
                props::Type propType = valProp->getType();
                switch (propType) {
                    case props::BOOL:
                        uniformType = Uniform::BOOL;
                        break;
                    case props::INT:
                        uniformType = Uniform::INT;
                        break;
                    case props::FLOAT:
                    case props::DOUBLE:
                        break;          // default float type;
                    case props::VEC3D:
                        uniformType = Uniform::FLOAT_VEC3;
                        break;
                    case props::VEC4D:
                        uniformType = Uniform::FLOAT_VEC4;
                        break;
                    default:
                        SG_LOG(SG_INPUT, SG_ALERT, "Can't deduce type of uniform "
                                << name);
                        return;
                }
            } else {
                findAttr(uniformTypes, typeProp, uniformType);
            }
            ref_ptr<Uniform> uniform = UniformFactory::instance()->
                getUniform( effect, name, uniformType, valProp, options );

            // optimize common uniforms
            if (uniformType == Uniform::SAMPLER_2D || uniformType == Uniform::INT)
            {
                int val = 0;
                uniform->get(val); // 'val' remains unchanged in case of error (Uniform is a non-scalar)
                if (uniformType == Uniform::SAMPLER_2D && val == 0
                        && name == "texture") {
                    uniform = texture0;
                } else if (uniformType == Uniform::INT && val >= 0 && val < 3
                        && name == "colorMode") {
                    uniform = colorMode[val];
                }
            }
            pass->addUniform(uniform.get());
            if (positionedProp && positionedProp->getBoolValue() && uniformType == Uniform::FLOAT_VEC4) {
                osg::Vec4 offset;
                uniform->get(offset);
                pass->addPositionedUniform( name, offset );
            }
        }
    };

    InstallAttributeBuilder<UniformBuilder> installUniform("uniform");

// Not sure what to do with "name". At one point I wanted to use it to
// order the passes, but I do support render bin and stuff too...
// Clément de l'Hamaide 10/2014: "name" is now used in the UniformCacheKey

    struct NameBuilder : public PassAttributeBuilder
    {
        void buildAttribute(Effect* effect, Pass* pass, const SGPropertyNode* prop,
                        const SGReaderWriterOptions* options)
        {
            // name can't use <use>
            string name = prop->getStringValue();
            if (!name.empty())
                pass->setName(name);
        }
    };

    InstallAttributeBuilder<NameBuilder> installName("name");

    EffectNameValue<PolygonMode::Mode> polygonModeModesInit[] =
    {
        {"fill", PolygonMode::FILL},
        {"line", PolygonMode::LINE},
        {"point", PolygonMode::POINT}
    };
    EffectPropertyMap<PolygonMode::Mode> polygonModeModes(polygonModeModesInit);

    struct PolygonModeBuilder : public PassAttributeBuilder
    {
        void buildAttribute(Effect* effect, Pass* pass, const SGPropertyNode* prop,
                        const SGReaderWriterOptions* options)
        {
            if (!isAttributeActive(effect, prop))
                return;
        const SGPropertyNode* frontProp
                = getEffectPropertyChild(effect, prop, "front");
        const SGPropertyNode* backProp
                = getEffectPropertyChild(effect, prop, "back");
            ref_ptr<PolygonMode> pmode = new PolygonMode;
            PolygonMode::Mode frontMode = PolygonMode::FILL;
            PolygonMode::Mode backMode = PolygonMode::FILL;
            if (frontProp) {
                findAttr(polygonModeModes, frontProp, frontMode);
                pmode->setMode(PolygonMode::FRONT, frontMode);
            }
            if (backProp) {
                findAttr(polygonModeModes, backProp, backMode);
                pmode->setMode(PolygonMode::BACK, backMode);
            }
            pass->setAttribute(pmode.get());
        }
    };

    InstallAttributeBuilder<PolygonModeBuilder> installPolygonMode("polygon-mode");

    struct PolygonOffsetBuilder : public PassAttributeBuilder
    {
        void buildAttribute(Effect* effect, Pass* pass, const SGPropertyNode* prop,
                        const SGReaderWriterOptions* options)
        {
            if (!isAttributeActive(effect, prop))
                return;

        const SGPropertyNode* factor
                = getEffectPropertyChild(effect, prop, "factor");
        const SGPropertyNode* units
                = getEffectPropertyChild(effect, prop, "units");

            ref_ptr<PolygonOffset> polyoffset = new PolygonOffset;

            polyoffset->setFactor(factor->getFloatValue());
            polyoffset->setUnits(units->getFloatValue());

            SG_LOG(SG_INPUT, SG_BULK,
                    "Set PolygonOffset to " << polyoffset->getFactor() << polyoffset->getUnits() );

            pass->setAttributeAndModes(polyoffset.get(),
                    StateAttribute::OVERRIDE|StateAttribute::ON);
        }
    };

    InstallAttributeBuilder<PolygonOffsetBuilder> installPolygonOffset("polygon-offset");

    struct VertexProgramTwoSideBuilder : public PassAttributeBuilder
    {
        void buildAttribute(Effect* effect, Pass* pass, const SGPropertyNode* prop,
                        const SGReaderWriterOptions* options)
        {
        const SGPropertyNode* realProp = getEffectPropertyNode(effect, prop);
            if (!realProp)
                return;
            pass->setMode(GL_VERTEX_PROGRAM_TWO_SIDE,
                    (realProp->getValue<bool>()
                            ? StateAttribute::ON : StateAttribute::OFF));
        }
    };

    InstallAttributeBuilder<VertexProgramTwoSideBuilder>
    installTwoSide("vertex-program-two-side");

    struct VertexProgramPointSizeBuilder : public PassAttributeBuilder
    {
        void buildAttribute(Effect* effect, Pass* pass, const SGPropertyNode* prop,
                        const SGReaderWriterOptions* options)
        {
        const SGPropertyNode* realProp = getEffectPropertyNode(effect, prop);
            if (!realProp)
                return;
            pass->setMode(GL_VERTEX_PROGRAM_POINT_SIZE,
                    (realProp->getValue<bool>()
                            ? StateAttribute::ON : StateAttribute::OFF));
        }
    };

    InstallAttributeBuilder<VertexProgramPointSizeBuilder>
    installPointSize("vertex-program-point-size");

    struct PointBuilder : public PassAttributeBuilder
    {
        void buildAttribute(Effect* effect, Pass* pass, const SGPropertyNode* prop,
                        const SGReaderWriterOptions* options)
        {
            float minsize = 1.0;
            float maxsize = 1.0;
            float size    = 1.0;
            osg::Vec3f attenuation = osg::Vec3f(1.0, 1.0, 1.0);

        const SGPropertyNode* realProp = getEffectPropertyNode(effect, prop);
            if (!realProp)
                return;

        const SGPropertyNode* pminsize
                = getEffectPropertyChild(effect, prop, "min-size");
        const SGPropertyNode* pmaxsize
                = getEffectPropertyChild(effect, prop, "max-size");
        const SGPropertyNode* psize
                = getEffectPropertyChild(effect, prop, "size");
        const SGPropertyNode* pattenuation
                = getEffectPropertyChild(effect, prop, "attenuation");

            if (pminsize)
                minsize = pminsize->getFloatValue();
            if (pmaxsize)
                maxsize = pmaxsize->getFloatValue();
            if (psize)
                size = psize->getFloatValue();
            if (pattenuation)
                attenuation = osg::Vec3f(pattenuation->getChild("x")->getFloatValue(),
                pattenuation->getChild("y")->getFloatValue(),
                pattenuation->getChild("z")->getFloatValue());

            osg::Point* point = new osg::Point;
            point->setMinSize(minsize);
            point->setMaxSize(maxsize);
            point->setSize(size);
            point->setDistanceAttenuation(attenuation);
            pass->setAttributeAndModes(point);
        }
    };

    InstallAttributeBuilder<PointBuilder>
    installPoint("point");

    EffectNameValue<Depth::Function> depthFunctionInit[] =
    {
        {"never", Depth::NEVER},
        {"less", Depth::LESS},
        {"equal", Depth::EQUAL},
        {"lequal", Depth::LEQUAL},
        {"greater", Depth::GREATER},
        {"notequal", Depth::NOTEQUAL},
        {"gequal", Depth::GEQUAL},
        {"always", Depth::ALWAYS}
    };
    EffectPropertyMap<Depth::Function> depthFunction(depthFunctionInit);

    struct DepthBuilder : public PassAttributeBuilder
    {
        void buildAttribute(Effect* effect, Pass* pass, const SGPropertyNode* prop,
                        const SGReaderWriterOptions* options)
        {
            if (!isAttributeActive(effect, prop))
                return;
            ref_ptr<Depth> depth = new Depth;
        const SGPropertyNode* pfunc
                = getEffectPropertyChild(effect, prop, "function");
            if (pfunc) {
                Depth::Function func = Depth::LESS;
                findAttr(depthFunction, pfunc, func);
                depth->setFunction(func);
            }
        const SGPropertyNode* pnear
                = getEffectPropertyChild(effect, prop, "near");
            if (pnear)
                depth->setZNear(pnear->getValue<double>());
        const SGPropertyNode* pfar
                = getEffectPropertyChild(effect, prop, "far");
            if (pfar)
                depth->setZFar(pfar->getValue<double>());
        const SGPropertyNode* pmask
                = getEffectPropertyChild(effect, prop, "write-mask");
            if (pmask)
                depth->setWriteMask(pmask->getValue<bool>());
        const SGPropertyNode* penabled
                = getEffectPropertyChild(effect, prop, "enabled");
            bool enabled = ( penabled == 0 || penabled->getBoolValue() );
            pass->setAttributeAndModes(depth.get(), enabled ? osg::StateAttribute::ON : osg::StateAttribute::OFF);
        }
    };*/

    // InstallAttributeBuilder<DepthBuilder> installDepth("depth");

    void buildTechnique(Effect effect, SGPropertyNode prop, SGReaderWriterOptions options) {
        Technique tniq = new Technique();
        effect.techniques.add(tniq);
        // we ignore schemes for now
        //tniq->setScheme(prop->getStringValue("scheme"));
        SGPropertyNode predProp = prop.getChild("predicate");
        if (predProp == null) {
            tniq.setAlwaysValid(true);
        } else {
            /*TODO try {
                TechniquePredParser parser;
                parser.setTechnique(tniq);
                expression::BindingLayout& layout = parser.getBindingLayout();
                layout.addBinding("__contextId", expression::INT);
                SGExpressionb* validExp
                        = dynamic_cast<SGExpressionb*>(parser.read(predProp
                        ->getChild(0)));
                if (validExp)
                    tniq->setValidExpression(validExp, layout);
                else
                    throw expression::ParseError("technique predicate is not a boolean expression");
            }
            catch (expression::ParseError& except)
            {
                SG_LOG(SG_INPUT, SG_ALERT,
                        "parsing technique predicate " << except.getMessage());
                tniq->setAlwaysValid(false);
            }*/
        }
        PropertyList passProps = prop.getChildren("pass");
        /*for (PropertyList::iterator itr = passProps.begin(), e = passProps.end();
        itr != e;
        ++itr) {*/
        for (SGPropertyNode/*_ptr*/ passProp : passProps) {
            buildPass(effect, tniq, passProp, options);
        }
    }

    /**
     * Specifically for .ac files...
     * StateSet is from OSG. Contains a 'current context'.
     * Retrieves 'current' material properties.
     */
    public static boolean makeParametersFromStateSet(SGPropertyNode effectRoot/*, StateSet ss*/) {
        SGPropertyNode paramRoot = SGPropertyNode.makeChild(effectRoot, "parameters");
        SGPropertyNode matNode = paramRoot.getChild("material", 0, true);
        /*Vec4f*/
        Color ambVal, difVal, specVal, emisVal;
        float shininess = 0.0f;
        // Material is from OSG
     /*Material mat = getStateAttribute<Material>(ss);
        if (mat) {
            ambVal = mat.getAmbient(Material::FRONT_AND_BACK);
            difVal = mat.getDiffuse(Material::FRONT_AND_BACK);
            specVal = mat.getSpecular(Material::FRONT_AND_BACK);
            emisVal = mat.getEmission(Material::FRONT_AND_BACK);
            shininess = mat.getShininess(Material::FRONT_AND_BACK);
            SGPropertyNode.makeChild(matNode, "active").setValue(true);
            SGPropertyNode.makeChild(matNode, "ambient").setValue(toVec4d(toSG(ambVal)));
            SGPropertyNode.makeChild(matNode, "diffuse").setValue(toVec4d(toSG(difVal)));
            SGPropertyNode.makeChild(matNode, "specular").setValue(toVec4d(toSG(specVal)));
            SGPropertyNode.makeChild(matNode, "emissive").setValue(toVec4d(toSG(emisVal)));
            SGPropertyNode.makeChild(matNode, "shininess").setValue(shininess);
            matNode->getChild("color-mode", 0, true).setStringValue("diffuse");
        } else {
            makeChild(matNode, "active")->setValue(false);
        }
    const ShadeModel* sm = getStateAttribute<ShadeModel>(ss);
        String shadeModelString("smooth");
        if (sm) {
            ShadeModel::Mode smMode = sm->getMode();
            if (smMode == ShadeModel::FLAT)
                shadeModelString = "flat";
        }
        makeChild(paramRoot, "shade-model")->setStringValue(shadeModelString);
        string cullFaceString("off");
    const CullFace* cullFace = getStateAttribute<CullFace>(ss);
        if (cullFace) {
            switch (cullFace->getMode()) {
                case CullFace::FRONT:
                    cullFaceString = "front";
                    break;
                case CullFace::BACK:
                    cullFaceString = "back";
                    break;
                case CullFace::FRONT_AND_BACK:
                    cullFaceString = "front-back";
                    break;
                default:
                    break;
            }
        }
        makeChild(paramRoot, "cull-face")->setStringValue(cullFaceString);
        // Macintosh ATI workaround
        bool vertexTwoSide = cullFaceString == "off";
        makeChild(paramRoot, "vertex-program-two-side")->setValue(vertexTwoSide);
    const BlendFunc* blendFunc = getStateAttribute<BlendFunc>(ss);
        SGPropertyNode* blendNode = makeChild(paramRoot, "blend");
        if (blendFunc) {
            string sourceMode = findName(blendFuncModes, blendFunc->getSource());
            string destMode = findName(blendFuncModes, blendFunc->getDestination());
            makeChild(blendNode, "active")->setValue(true);
            makeChild(blendNode, "source")->setStringValue(sourceMode);
            makeChild(blendNode, "destination")->setStringValue(destMode);
            makeChild(blendNode, "mode")->setValue(true);
        } else {
            makeChild(blendNode, "active")->setValue(false);
        }
        string renderingHint = findName(renderingHints, ss->getRenderingHint());
        makeChild(paramRoot, "rendering-hint")->setStringValue(renderingHint);
        makeTextureParameters(paramRoot, ss);

      */
        return true;
    }

   /* SGPropertyNode_ptr schemeList;

    void mergeSchemesFallbacks(Effect *effect, const SGReaderWriterOptions *options)
    {
        if (!schemeList) {
            schemeList = new SGPropertyNode;
        const string schemes_file("Effects/schemes.xml");
            string absFileName
                    = SGModelLib::findDataFile(schemes_file, options);
            if (absFileName.empty()) {
                SG_LOG(SG_INPUT, SG_ALERT, "Could not find '" << schemes_file << "'");
                return;
            }
            try {
                readProperties(absFileName, schemeList, 0, true);
            } catch (sg_io_exception& e) {
                SG_LOG(SG_INPUT, SG_ALERT, "Error reading '" << schemes_file <<
                        "': " << e.getFormattedMessage());
                return;
            }
        }

        PropertyList p_schemes = schemeList->getChildren("scheme");
        for (const auto &p_scheme : p_schemes) {
        string scheme_name   = p_scheme->getStringValue("name");
        string fallback_name = p_scheme->getStringValue("fallback");
        if (scheme_name.empty() || fallback_name.empty())
            continue;
        vector<SGPropertyNode_ptr> techniques = effect->root->getChildren("technique");
        auto it = std::find_if(techniques.begin(), techniques.end(),
                               [&scheme_name](const SGPropertyNode_ptr &tniq) {
            return tniq->getStringValue("scheme") == scheme_name;
        });
        // Only merge the fallback effect if we haven't found a technique
        // implementing the scheme
        if (it == techniques.end()) {
            ref_ptr<Effect> fallback = makeEffect(fallback_name, false, options);
            if (fallback) {
                SGPropertyNode *new_root = new SGPropertyNode;
                mergePropertyTrees(new_root, effect->root, fallback->root);
                effect->root = new_root;
                effect->parametersProp = effect->root->getChild("parameters");
            }
        }
    }*/

    /**
     * In FG probably inherited from osg::Object
     */
    public boolean valid() {
        return true;//TODO Util.notyet();
    }

    /**
     * Walk the techniques property tree, building techniques and passes.
     * <p/>
     * FG-DIFF Implementierung
     * Hier wird mal das Material gebaut. SGMaterial.buildEffectProperties() hat die Materialwerte vorher in die PropertyNode geschrieben.
     * Das ist aber auch reichlich Woodoo. Nicht erkennbar, welche Textur wofuer genutzt wird.
     * Um nicht über die PropNode gehen zu muessen, hier direkt das SGMaterial reingeben. Geht aber nicht so einfgach.
     * 5.10.17: Wird
     *
     * @param options
     */
    public boolean realizeTechniques(SGReaderWriterOptions options/*, SGMaterial mat*/) {
        //material
        if (SGMaterialLib.materiallibdebuglog) {
            logger.debug("Effect:realizeTechniques " + getName());
        }
        //mergeSchemesFallbacks(this, options);
        if (_isRealized)
            return true;
        PropertyList tniqList = root.getChildren("technique");
        //for (PropertyList::iterator itr = tniqList.begin(), e = tniqList.end();        itr != e;        ++itr)
        for (SGPropertyNode/*_ptr*/ tniq : tniqList) {
            buildTechnique(this, tniq, options);
        }
        _isRealized = true;

        // 21.10.24
        // der genaue Aufbau ist unklar, auch warum es auf einmal mehrere Texturen gibt. Evtl. normal maps und co
        //logger.debug("effect.root=" + root.dump("\n"));
        PropertyList parameters = root.getChildren("parameters");
        PropertyList texturel = parameters.get(0).getChildren("texture");
        int uniqueid = parameters.get(0).getIntValue("uniqueid", -55);
        int index = 0;
        SGPropertyNode tex = texturel.get(index);
        String image = tex.getStringValue("image");
        //TODO wrap aus tree, bundlename
        //image enthaelt den kompletten absoluten Path. 12.6.17: jetzt nur noch den Pafd relativ im Bundle
        logger.debug("realizeTechniques with texture " + image);
       // if (StringUtils.startsWith(image,F))
        //27.12.17: Nicht mehr Textur laden und Material anlegen, sondern ein LoadedMaterial anlegen.
        //Texture texture = Texture.buildBundleTexture(SGMaterialLib.BUNDLENAME,image, true, true);
       
        /*if (StringUtils.endsWith(image, "drycrop4.png") ||
                StringUtils.endsWith(image, "naturalcrop1.png")) {
            material = Material.buildBasicMaterial(Color.YELLOW);
        } else {*/
        materialdefinition = new PortableMaterial(null, image, true, true);//Material.buildLambertMaterial(texture);
        //material.setName("SGMaterial id= "+uniqueid);

        //}

        return true;
    }


    /*public Material getMaterialD() {

        return material;
    }*/

    public PortableMaterial getMaterialDefinition() {

        return materialdefinition;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isRealized() {
        return _isRealized;
    }
}

