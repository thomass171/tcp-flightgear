package de.yard.threed.flightgear.core.simgear.scene.material;

import de.yard.threed.flightgear.EffectMaterialWrapper;

/**
 * From Pass.[ch]xx
 * <p>
 * Used to extend https://github.com/openscenegraph/OpenSceneGraph/blob/master/include/osg/StateSet.
 * For providing the OSG/OpenGL state where effects should apply? See README.md#Effects.
 * <p>
 * We use a meterial wrapper instead of osg::StateSet as super class.
 * Cannot be a super class because it is created long before Pass. So it's component.
 */
public class Pass /*extends EffectMaterialWrapper*/ { //: osg::StateSet

    EffectMaterialWrapper wrapper;

    public Pass(EffectMaterialWrapper wrapper) {
        //super(null);
        //super(
        this.wrapper = wrapper;
    }
       /* public:
        typedef std::list<std::pair<int,std::string> > BufferUnitList;
        typedef std::map<std::string,osg::Vec4> PositionedUniformMap;

        META_Object(simgear,Pass);
        Pass() {}
        Pass(const Pass& rhs,
         const osg::CopyOp& copyop = osg::CopyOp::SHALLOW_COPY);

        void setBufferUnit( int unit, std::string buffer ) { _bufferUnitList.push_back( std::make_pair(unit,buffer) ); }
    const BufferUnitList& getBufferUnitList() const { return _bufferUnitList; }

        void addPositionedUniform( const std::string& name, const osg::Vec4& offset ) { _positionedUniforms[name] = offset; }
    const PositionedUniformMap& getPositionedUniformMap() const { return _positionedUniforms; }

        private:
        BufferUnitList _bufferUnitList;
        PositionedUniformMap _positionedUniforms;
    };*/

    /**
     * Replaces
     * pass.setMode(GL_BLEND, (realProp -> getBoolValue() ? StateAttribute::ON : StateAttribute::OFF));
     */
    public void setBlending(boolean enabled) {
        if (wrapper != null) {
            wrapper.setBlending(enabled);
        }
    }
}
