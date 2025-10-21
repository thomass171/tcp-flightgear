package de.yard.threed.flightgear.core.simgear.scene.material;

import de.yard.threed.core.Util;
import de.yard.threed.engine.Material;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.structure.*;

import java.util.ArrayList;
import java.util.List;

/**
 * From Technique.[ch]xx
 */
public class Technique {//: public osg::Object


   /* namespace simgear
    {
        using namespace osg;
        using namespace osgUtil;

        namespace
        {
*/
            /*struct ValidateOperation : GraphicsOperation
            {
                ValidateOperation(Technique* technique_)
        : GraphicsOperation(opName, false), technique(technique_)
                {
                }
                virtual void operator() (GraphicsContext* gc);
                osg::ref_ptr<Technique> technique;
                static const std::string opName;
            };

const std::string ValidateOperation::opName("ValidateOperation");


            void ValidateOperation::operator() (GraphicsContext* gc)
            {
                technique->validateInContext(gc);
            }
        }*/

       /* Technique::Technique(bool alwaysValid)
    : _alwaysValid(alwaysValid), _contextIdLocation(-1)
        {
        }*/

      /*  Technique::Technique(const Technique& rhs, const osg::CopyOp& copyop) :
        osg::Object(rhs,copyop),
        _contextMap(rhs._contextMap), _alwaysValid(rhs._alwaysValid),
                _shadowingStateSet(copyop(rhs._shadowingStateSet.get())),
                _validExpression(rhs._validExpression),
                _contextIdLocation(rhs._contextIdLocation)
        {
            for (std::vector<ref_ptr<Pass> >::const_iterator itr = rhs.passes.begin(),
                end = rhs.passes.end();
            itr != end;
            ++itr)
            passes.push_back(static_cast<Pass*>(copyop(itr->get())));
        }*/

        /*Technique::~Technique()
        {
        }*/

    /* public:
     META_Object(simgear,Technique);
     Technique(bool alwaysValid = false);
     Technique(const Technique& rhs,
               const osg::CopyOp& copyop = osg::CopyOp::SHALLOW_COPY);
     virtual ~Technique();*/
    enum Status {
        UNKNOWN,
        QUERY_IN_PROGRESS,
        INVALID,
        VALID
    }

    ;

    /** Returns the validity of a technique in a state. If we don't
     * know, a query will be launched.
     */
    //virtual Status valid(osg::RenderInfo* renderInfo);
    /**
     * Returns the validity of the technique without launching a
     * query.
     */
    //Status getValidStatus(const osg::RenderInfo* renderInfo) const;
   /*
    virtual EffectGeode::DrawablesIterator
    processDrawables(const EffectGeode::DrawablesIterator& begin,
                     const EffectGeode::DrawablesIterator& end,
        osgUtil::CullVisitor* cv,
        bool isCullingActive);
    std::vector<osg::ref_ptr<Pass> > passes;*/
    List<Pass> passes = new ArrayList<>();

    /*
    osg::StateSet* getShadowingStateSet() { return _shadowingStateSet.get(); }
    const osg::StateSet* getShadowingStateSet() const
    {
        return _shadowingStateSet.get();
    }
    void setShadowingStateSet(osg::StateSet* ss) { _shadowingStateSet = ss; }
    virtual void resizeGLObjectBuffers(unsigned int maxSize);
    virtual void releaseGLObjects(osg::State* state = 0) const;
    */
    boolean getAlwaysValid() {
        return _alwaysValid;
    }

    void setAlwaysValid(boolean val) {
        _alwaysValid = val;
    }

    /*void setValidExpression(SGExpressionb* exp,
                            const simgear::expression::BindingLayout&);
    void setGLExtensionsPred(float glVersion,
                             const std::vector<std::string>& extensions);
    void refreshValidity();
    const std::string &getScheme() const { return _scheme; }
    void setScheme(const std::string &scheme) { _scheme = scheme; }
    protected:
    // Validity of technique in a graphics context.
    struct ContextInfo : public osg::Referenced
    {
        ContextInfo() : valid(UNKNOWN) {}
        ContextInfo(const ContextInfo& rhs) : osg::Referenced(rhs), valid(rhs.valid()) {}
        ContextInfo& operator=(const ContextInfo& rhs)
        {
            valid = rhs.valid;
            return *this;
        }
        Swappable<Status> valid;
    };
    typedef osg::buffered_object<ContextInfo> ContextMap;
    mutable ContextMap _contextMap;*/
    private boolean _alwaysValid;
    //osg::ref_ptr<osg::StateSet> _shadowingStateSet;
    /*SGSharedPtr<SGExpressionb>*/private SGExpression _validExpression;
    int _contextIdLocation;
    //std::string _scheme;



/*
        Technique::Status Technique::valid(osg::RenderInfo* renderInfo)
        {
            if (_alwaysValid)
                return VALID;
            unsigned contextID = renderInfo->getContextID();
            ContextInfo& contextInfo = _contextMap[contextID];
            Status status = contextInfo.valid();
            if (status != UNKNOWN)
                return status;
            Status newStatus = QUERY_IN_PROGRESS;
            // lock and spawn validity check.
            if (!contextInfo.valid.compareAndSwap(status, newStatus)) {
                // Lost the race with another thread spawning a request
                return contextInfo.valid();
            }
            ref_ptr<ValidateOperation> validOp = new ValidateOperation(this);
            GraphicsContext* context = renderInfo->getState()->getGraphicsContext();
            GraphicsThread* thread = context->getGraphicsThread();
            if (thread)
                thread->add(validOp.get());
            else
                context->add(validOp.get());
            return newStatus;
        }

        Technique::Status Technique::getValidStatus(const RenderInfo* renderInfo) const
        {
            if (_alwaysValid)
                return VALID;
            ContextInfo& contextInfo = _contextMap[renderInfo->getContextID()];
            return contextInfo.valid();
        }*/

    /**
     * Tests and sets the validity of the Technique. Must be run in a
     * graphics context.
     * TS: Must be the 'gate' for rendering in FG/OSG; only here _validExpression is evaluated. And is only called by ValidateOperation
     * But we move the logic to valid()
     */
        /*private void Technique::validateInContext(GraphicsContext gc)
        {
            unsigned int contextId = gc->getState()->getContextID();
            ContextInfo& contextInfo = _contextMap[contextId];
            Status oldVal = contextInfo.valid();
            Status newVal = INVALID;
            expression::FixedLengthBinding<1> binding;
            binding.getBindings()[_contextIdLocation] = expression::Value((int) contextId);
            if (_validExpression.getValue(binding)) {
                newVal = VALID;
            }
            contextInfo.valid.compareAndSwap(oldVal, newVal);
        }*/

      /*  namespace
        {
            enum NumDrawables {NUM_DRAWABLES = 128};
        }

        EffectGeode::DrawablesIterator
        Technique::processDrawables(const EffectGeode::DrawablesIterator& begin,
                            const EffectGeode::DrawablesIterator& end,
            CullVisitor* cv,
            bool isCullingActive)
        {
            RefMatrix& matrix = *cv->getModelViewMatrix();
            float depth[NUM_DRAWABLES];
            EffectGeode::DrawablesIterator itr = begin;
            bool computeNearFar
                    = cv->getComputeNearFarMode() != CullVisitor::DO_NOT_COMPUTE_NEAR_FAR;
            for (int i = 0; i < NUM_DRAWABLES && itr != end; ++itr, ++i)
            {
                Drawable* drawable = itr->get();

#if OSG_VERSION_LESS_THAN(3,3,2)
      const BoundingBox& bb = drawable->getBound();
                osg::Drawable::CullCallback* cull = drawable->getCullCallback();
#else
      const BoundingBox& bb = drawable->getBoundingBox();
                osg::Drawable::CullCallback* cull =
                    dynamic_cast<osg::Drawable::CullCallback*>(drawable->getCullCallback());
#endif

                if(   (cull && cull->cull(cv, drawable, &cv->getRenderInfo()))
         || (isCullingActive && cv->isCulled(bb)) )
                {
                    depth[i] = FLT_MAX;
                    continue;
                }

                if( computeNearFar && bb.valid() )
                {
                    if( !cv->updateCalculatedNearFar(matrix, *drawable, false) )
                    {
                        depth[i] = FLT_MAX;
                        continue;
                    }
                }

                depth[i] = bb.valid()
                        ? cv->getDistanceFromEyePoint(bb.center(), false)
                        : 0.0f;
                if( isNaN(depth[i]) )
                    depth[i] = FLT_MAX;
            }
            EffectCullVisitor* ecv = dynamic_cast<EffectCullVisitor*>( cv );
            EffectGeode::DrawablesIterator drawablesEnd = itr;
            BOOST_FOREACH(ref_ptr<Pass>& pass, passes)
            {
                osg::ref_ptr<osg::StateSet> ss = pass;
                if (ecv && ( ! pass->getBufferUnitList().empty() || ! pass->getPositionedUniformMap().empty() ) ) {
                    ss = static_cast<osg::StateSet*>(
                            pass->clone( osg::CopyOp( ( ! pass->getBufferUnitList().empty() ?
                            osg::CopyOp::DEEP_COPY_TEXTURES :
                    osg::CopyOp::SHALLOW_COPY ) |
                    ( ! pass->getPositionedUniformMap().empty() ?
                            osg::CopyOp::DEEP_COPY_UNIFORMS :
                    osg::CopyOp::SHALLOW_COPY ) )
                )
            );
                    for (Pass::BufferUnitList::const_iterator ii = pass->getBufferUnitList().begin();
                    ii != pass->getBufferUnitList().end();
                    ++ii) {
                        osg::Texture2D* tex = ecv->getBuffer(ii->second);
                        if (tex != 0)
                            ss->setTextureAttributeAndModes( ii->first, tex );
                    }
                    for (Pass::PositionedUniformMap::const_iterator ii = pass->getPositionedUniformMap().begin();
                    ii != pass->getPositionedUniformMap().end();
                    ++ii) {
                        osg::RefMatrix* mv = cv->getModelViewMatrix();
                        osg::Vec4 v = ii->second * *mv;
                        ss->getUniform(ii->first)->set( v );
                    }
                }
                cv->pushStateSet(ss);
                int i = 0;
                for (itr = begin; itr != drawablesEnd; ++itr, ++i) {
                    if (depth[i] != FLT_MAX)
                        cv->addDrawableAndDepth(itr->get(), &matrix, depth[i]);
                }
                cv->popStateSet();
            }
            return drawablesEnd;
        }*/

        /*void Technique::resizeGLObjectBuffers(unsigned int maxSize)
        {
            if (_shadowingStateSet.valid())
                _shadowingStateSet->resizeGLObjectBuffers(maxSize);
            BOOST_FOREACH(ref_ptr<Pass>& pass, passes) {
            pass->resizeGLObjectBuffers(maxSize);
        }
            _contextMap.resize(maxSize);
        }*/

        /*void Technique::releaseGLObjects(osg::State* state) const
        {
            if (_shadowingStateSet.valid())
                _shadowingStateSet->releaseGLObjects(state);
            BOOST_FOREACH(const ref_ptr<Pass>& pass, passes)
            {
                pass->releaseGLObjects(state);
            }
            if (state == 0) {
                for (int i = 0; i < (int)_contextMap.size(); ++i) {
                    ContextInfo& info = _contextMap[i];
                    Status oldVal = info.valid();
                    info.valid.compareAndSwap(oldVal, UNKNOWN);
                }
            } else {
                ContextInfo& info = _contextMap[state->getContextID()];
                Status oldVal = info.valid();
                info.valid.compareAndSwap(oldVal, UNKNOWN);
            }
        }*/

    void setValidExpression(SGExpression/*b**/ exp,
                                   /*const simgear::expression
                                   ::*/BindingLayout layout) {
        //using namespace simgear::expression;
        _validExpression = exp;
        VariableBinding binding = layout.findBinding("__contextId");
        //if (layout.findBinding("__contextId", binding)) {
        if (binding != null) {
            _contextIdLocation = binding.location;
        }
    }

    static class GLVersionExpression extends SGExpression/*<float>*/ {
        @Override
        public PrimitiveValue eval(/*float& value, const expression::*/Binding binding) {
            //value = getGLVersionNumber();
            return new PrimitiveValue(0);
        }
    }

    static class glVersionParser implements exp_parser {
        public SGExpression parse(SGPropertyNode exp, Parser parser) {
            return new GLVersionExpression();
        }
    }

    static ExpParserRegistrar glVersionRegistrar = new ExpParserRegistrar("glversion", new glVersionParser());

    static class ExtensionSupportedExpression extends GeneralNaryExpression/*<bool, int>*/ {
        String _extString;

        public ExtensionSupportedExpression(String extString) {
            _extString = extString;
        }

        String getExtensionString() {
            return _extString;
        }

        void setExtensionString(String extString) {
            _extString = extString;
        }

        //void eval (bool & value, const expression::Binding * b) const
        @Override
        public PrimitiveValue eval(Binding b) {
            /*int contextId = getOperand(0)->getValue(b);
            value = isGLExtensionSupported((unsigned) contextId, _extString.c_str());*/
            return new PrimitiveValue(0);
        }
    }

    static class extensionSupportedParser implements exp_parser {
        public SGExpression parse(SGPropertyNode exp, Parser parser) {
            //TODO if (exp . getType() == props::STRING                    || exp . getType() == props::UNSPECIFIED) {
            ExtensionSupportedExpression esp = new ExtensionSupportedExpression(exp.getStringValue());
            int location = parser.getBindingLayout().addBinding("__contextId", new ExpressionType(ExpressionType.INT));
            VariableExpression contextExp = new VariableExpression/*<int>*/(location);
            esp.addOperand(contextExp);
            return esp;
            //}
            //TODO  throw new        ParseError("extension-supported expression has wrong type");
        }
    }

    static ExpParserRegistrar extensionSupportedRegistrar = new ExpParserRegistrar("extension-supported", new extensionSupportedParser());

    static class GLShaderLanguageExpression extends GeneralNaryExpression/*<float, int>*/ {

        //void eval ( float&value, const expression::Binding * b) const
        @Override
        public PrimitiveValue eval(Binding b) {
            double value = 0.0;
    /*        int contextId = getOperand(0)->getValue(b);
            GL2Extensions * extensions
                    = GL2Extensions::Get (static_cast < unsigned > (contextId), true);
            if (!extensions)
                return;
#if OSG_VERSION_LESS_THAN(3, 3, 4)
            if (!extensions -> isGlslSupported())
                return;
            value = extensions -> getLanguageVersion();
#else
            if (!extensions -> isGlslSupported)
                return;
            value = extensions -> glslLanguageVersion;
#endif*/
            return new PrimitiveValue(value);
        }
    }

    ;

    static class shaderLanguageParser implements exp_parser {
        public SGExpression parse(SGPropertyNode exp, Parser parser) {
            GLShaderLanguageExpression slexp = new GLShaderLanguageExpression();
            int location = parser.getBindingLayout().addBinding("__contextId", new ExpressionType(ExpressionType.INT));
            VariableExpression/*<int>**/contextExp = new VariableExpression/*<int>*/(location);
            slexp.addOperand(contextExp);
            return slexp;
        }
    }

    static ExpParserRegistrar shaderLanguageRegistrar = new ExpParserRegistrar("shader-language", new shaderLanguageParser());

    static class GLSLSupportedExpression extends GeneralNaryExpression/*<bool, int>*/ {
        //void eval (bool & value, const expression::Binding * b) const
        @Override
        public PrimitiveValue eval(Binding b) {
          /*  value = false;
            int contextId = getOperand(0)->getValue(b);
            GL2Extensions * extensions
                    = GL2Extensions::Get (static_cast < unsigned > (contextId), true);
            if (!extensions)
                return;
#if OSG_VERSION_LESS_THAN(3, 3, 4)
            value = extensions -> isGlslSupported();
#else
            value = extensions -> isGlslSupported;
#endif*/
            return new PrimitiveValue(0);
        }
    }

    static class glslSupportedParser implements exp_parser {
        public SGExpression parse(SGPropertyNode exp, Parser parser) {
            GLSLSupportedExpression sexp = new GLSLSupportedExpression();
            int location = parser.getBindingLayout().addBinding("__contextId", new ExpressionType(ExpressionType.INT));
            VariableExpression/*<int>* */ contextExp = new VariableExpression/*<int>*/(location);
            sexp.addOperand(contextExp);
            return sexp;
        }
    }

    static ExpParserRegistrar glslSupportedRegistrar = new ExpParserRegistrar("glsl-supported", new glslSupportedParser());

      /*  void Technique::setGLExtensionsPred(float glVersion,
                                    const std::vector<std::string>& extensions)
        {
            using namespace std;
            using namespace expression;
            BindingLayout layout;
            int contextLoc = layout.addBinding("__contextId", INT);
            VariableExpression<int>* contextExp
                = new VariableExpression<int>(contextLoc);
            SGExpression<bool>* versionTest
                = makePredicate<std::less_equal>(new SGConstExpression<float>(glVersion),
                new GLVersionExpression);
            AndExpression* extensionsExp = 0;
            for (vector<string>::const_iterator itr = extensions.begin(),
            e = extensions.end();
            itr != e;
            ++itr) {
            if (!extensionsExp)
                extensionsExp = new AndExpression;
            ExtensionSupportedExpression* supported
                    = new ExtensionSupportedExpression(*itr);
            supported->addOperand(contextExp);
            extensionsExp->addOperand(supported);
        }
            SGExpressionb* predicate = 0;
            if (extensionsExp) {
                OrExpression* orExp = new OrExpression;
                orExp->addOperand(versionTest);
                orExp->addOperand(extensionsExp);
                predicate = orExp;
            } else {
                predicate = versionTest;
            }
            setValidExpression(predicate, layout);
        }*/

    public void /*Technique::*/refreshValidity() {
        Util.notyet();
            /*TODO
            for (int i = 0; i < (int)_contextMap.size(); ++i) {
                ContextInfo& info = _contextMap[i];
                Status oldVal = info.valid();
                // What happens if we lose the race here?
                info.valid.compareAndSwap(oldVal, UNKNOWN);
            }*/
    }

     /*   bool Technique_writeLocalData(const Object& obj, osgDB::Output& fw)
        {
    const Technique& tniq = static_cast<const Technique&>(obj);
            fw.indent() << "alwaysValid "
                    << (tniq.getAlwaysValid() ? "TRUE\n" : "FALSE\n");
#if 0
            fw.indent() << "glVersion " << tniq.getGLVersion() << "\n";
#endif
            if (tniq.getShadowingStateSet()) {
                fw.indent() << "shadowingStateSet\n";
                fw.writeObject(*tniq.getShadowingStateSet());
            }
            fw.indent() << "num_passes " << tniq.passes.size() << "\n";
            BOOST_FOREACH(const ref_ptr<Pass>& pass, tniq.passes) {
            fw.writeObject(*pass);
        }
            return true;
        }*/

      /*  namespace
        {
            osgDB::RegisterDotOsgWrapperProxy TechniqueProxy
                (
                        new Technique,
                        "simgear::Technique",
                        "Object simgear::Technique",
                        0,
                &Technique_writeLocalData
    );*/

    /**
     * In FG this method comes from super class osg::Object
     * No idea how this works in FG/OSG. So we use our own idea.
     */
    public boolean valid() {
        if (_alwaysValid) {
            return true;
        }
        if (_validExpression != null) {
            PrimitiveValue e = _validExpression.getValue(null/*binding*/);
            if (e != null && e.doubleVal != 0.0) {
                return true;
            }
        }
        return false;
    }

}


