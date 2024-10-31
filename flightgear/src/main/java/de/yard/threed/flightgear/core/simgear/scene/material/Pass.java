package de.yard.threed.flightgear.core.simgear.scene.material;

/**
 * From Pass.[ch]xx
 *
 * Used to extend https://github.com/openscenegraph/OpenSceneGraph/blob/master/include/osg/StateSet
 * Seems it was just a kind of wrapper.
 */
public class Pass { //: osg::StateSet

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
     *
     */
    public void setBlending(boolean enabled){
       //TODO needs to know material/texture
    }
}
