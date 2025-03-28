package de.yard.threed.flightgear.core.simgear.scene.material;

import java.util.ArrayList;
import java.util.List;

/**
 * From Technique.[ch]xx
 */
public class Technique {//: public osg::Object

   /* public:
    META_Object(simgear,Technique);
    Technique(bool alwaysValid = false);
    Technique(const Technique& rhs,
              const osg::CopyOp& copyop = osg::CopyOp::SHALLOW_COPY);
    virtual ~Technique();*/
    enum Status
    {
        UNKNOWN,
        QUERY_IN_PROGRESS,
        INVALID,
        VALID
    };

    /** Returns the validity of a technique in a state. If we don't
     * know, a query will be launched.
     */
    //virtual Status valid(osg::RenderInfo* renderInfo);
    /** Returns the validity of the technique without launching a
     * query.
     */
    //Status getValidStatus(const osg::RenderInfo* renderInfo) const;
    /** Tests and sets the validity of the Technique. Must be run in a
     *graphics context.
     */
    /*virtual void validateInContext(osg::GraphicsContext* gc);

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
    boolean getAlwaysValid() { return _alwaysValid; }
    void setAlwaysValid(boolean val) { _alwaysValid = val; }
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
    boolean _alwaysValid;
    /*osg::ref_ptr<osg::StateSet> _shadowingStateSet;
    SGSharedPtr<SGExpressionb> _validExpression;
    int _contextIdLocation;
    std::string _scheme;*/
}

/*class TechniquePredParser : public expression::ExpressionParser
{
    public:
    void setTechnique(Technique* tniq) { _tniq = tniq; }
    Technique* getTechnique() { return _tniq.get(); }
//    void setEffect(Effect* effect) { _effect = effect; }
//    Effect* getEffect() { return _effect.get(); }
    protected:
    osg::ref_ptr<Technique> _tniq;
    // osg::ref_ptr<Effect> _effect;

}*/


