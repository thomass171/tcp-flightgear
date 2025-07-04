package de.yard.threed.flightgear.core.simgear.scene.material;

import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.GenericGeometry;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Mesh;
import de.yard.threed.engine.loader.DefaultMaterialFactory;
import de.yard.threed.engine.loader.PortableModelBuilder;
import de.yard.threed.engine.platform.ResourceLoaderFromBundle;
import de.yard.threed.flightgear.core.osg.Geode;


import de.yard.threed.core.resource.ResourcePath;


/**
 * Created by thomass on 05.08.16.
 * 14.12.17: Statt mit SGMaterial und Effect kann es jetzt auch mit Material arbeiten. Zumindest in den Bereichen,
 * die nicht auskommentiert sind.
 * 26.10.24: HmmHmm
 */
public class EffectGeode extends Geode {
    static Log log = Platform.getInstance().getLog(EffectGeode.class);
    /*osg::ref_ptr<*/ Effect _effect;
    //21.6.25 not is use for a long time now SGMaterial _material;
    //Alternative for using SGMaterial and Effect. 28.12.17: Jetzt LoadedMaterial statt Material
    //26.10.24 does this really belong here? Maybe. It's the effective material used finally.
    private /*Portable*/Material material;

        /*    #if OSG_VERSION_LESS_THAN(3,3,2)
    typedef DrawableList::iterator DrawablesIterator;
    #else*/

    // #endif

    public EffectGeode() {

    }
    
    /*EffectGeode(const EffectGeode& rhs,
                const osg::CopyOp& copyop = osg::CopyOp::SHALLOW_COPY);
    META_Node(simgear,EffectGeode);
    */

    Effect getEffect() {
        return _effect/*.get()*/;
    }

    public void setEffect(Effect effect) {
        _effect = effect;
        //if (!_effect)
        //    return;
        /*
        struct InitializeCallback : public UpdateOnceCallback
    {
        void doUpdate(osg::Node* node, osg::NodeVisitor* nv);
    };
         */
        // addUpdateCallback(new Effect::InitializeCallback);
    }

    /*21.6.25 SGMaterial getMaterial() {
        return _material;
    }

    public void setMaterial(SGMaterial mat) {
        _material = mat;
    }*/

    /**
     * 21.6.25 Replacing above
     */
    public void setMaterial(Material mat) {
        material = mat;
    }

    //virtual void resizeGLObjectBuffers(unsigned int maxSize);
    //virtual void releaseGLObjects(osg::State* = 0) const;

   /* #if OSG_VERSION_LESS_THAN(3,3,2)
    DrawablesIterator drawablesBegin() { return _drawables.begin(); }
    DrawablesIterator drawablesEnd() { return _drawables.end(); }
    #else*/
    //DrawablesIterator drawablesBegin() { return DrawablesIterator(_children.begin()); }
    //DrawablesIterator drawablesEnd() { return DrawablesIterator(_children.end()); }
    //#endif

    /**
     * Generates tangent space vectors or other data from geom, as defined by effect
     */
    public void runGenerators(/*osg::Geometry* /  geometry*/) {
         /*
        if(geometry && _effect.valid()) {
            // Generate tangent vectors for the geometry
            osg::ref_ptr<osgUtil::TangentSpaceGenerator> tsg = new osgUtil::TangentSpaceGenerator;

            // Generating only tangent vector should be enough
            // since the binormal isType a cross product of normal and tangent
            // This saves a bit of memory & memory bandwidth!
            int n = _effect->getGenerator(Effect::TANGENT);
            tsg->generate(geometry, 0);  // 0 isType normal_unit, but I have no idea what that isType!
            if (n != -1 && !geometry->getVertexAttribArray(n))
            #if OSG_MIN_VERSION_REQUIRED(3,1,8)
            geometry->setVertexAttribArray(n, tsg->getTangentArray(), osg::Array::BIND_PER_VERTEX);
            #else
            geometry->setVertexAttribData(n, osg::Geometry::ArrayData(tsg->getTangentArray(), osg::Geometry::BIND_PER_VERTEX,GL_FALSE));
            #endif

                    n = _effect->getGenerator(Effect::BINORMAL);
            if (n != -1 && !geometry->getVertexAttribArray(n))
            #if OSG_MIN_VERSION_REQUIRED(3,1,8)
            geometry->setVertexAttribArray(n, tsg->getBinormalArray(), osg::Array::BIND_PER_VERTEX);
            #else
            geometry->setVertexAttribData(n, osg::Geometry::ArrayData(tsg->getBinormalArray(), osg::Geometry::BIND_PER_VERTEX,GL_FALSE));
            #endif

                    n = _effect->getGenerator(Effect::NORMAL);
            if (n != -1 && !geometry->getVertexAttribArray(n))
            #if OSG_MIN_VERSION_REQUIRED(3,1,8)
            geometry->setVertexAttribArray(n, tsg->getNormalArray(), osg::Array::BIND_PER_VERTEX);
            #else
            geometry->setVertexAttribData(n, osg::Geometry::ArrayData(tsg->getNormalArray(), osg::Geometry::BIND_PER_VERTEX,GL_FALSE));
            #endif
        }*/

    }

    /**
     * Different to FG that creates via OSG Callback(?) (oder halb?) create the mesh here.
     * // FG-DIFF
     * build mesh from geometry and material.
     */
    public void buildMesh(SimpleGeometry geometry) {
        // Aus dem SGMaterial und dem Effect das Platform Material bauen. 14.12.17: Oder aus material, wenns schon vorliegt.
        Material mat;
        // 19.2.24: decoupled from bundle by using resourceLoader. path and name might be empty.
        // These are set later in PortableModelBuilder
        ResourceLoaderFromBundle resourceLoader = new ResourceLoaderFromBundle(
                new BundleResource(BundleRegistry.getBundle(SGMaterialLib.BUNDLENAME),new ResourcePath(""),""));
        // material should come from Effect.
        if (material != null) {
            // mat = material;
            //14.2.24 mat = PortableModelBuilder.buildMaterial(BundleRegistry.getBundle(SGMaterialLib.BUNDLENAME),
            //14.2.24         material, (material.texture != null) ? material.texture : null/*obj.texture*/, new ResourcePath(""/*texturebasepath*/),geometry.getNormals()!=null);
            //12.11.24 now again in Effect mat = new DefaultMaterialFactory().buildMaterial(resourceLoader, material, new ResourcePath(""), geometry.getNormals() != null);
            mat = material;
        } else {
            if (_effect == null) {
                // kann wohl vorkommen? TODO was tun? erstmal null und damit wireframe. Auch Absicht fuer Tests.
                mat = null;//Material.buildBasicMaterial(Color.YELLOW);
            } else {
                //11.11.24 never reached?
                //9.6.25 Eg. reached for scenery tiles with effects/materials which are defined but are not
                // available, eg. due to missing texture (eg. 'Ocean' without bundlepool).
                // Also stay with wireframe for now
                log.warn("No material for effect ");
                mat = null;

            }
        }
        setMesh(new Mesh(new GenericGeometry(geometry), mat));
    }
}

   /* class DrawablesIterator:
    public boost::iterator_adaptor<
            DrawablesIterator,
            osg::NodeList::iterator,
    osg::ref_ptr<osg::Drawable>,
    boost::use_default,
    osg::ref_ptr<osg::Drawable> // No reference as Reference type.
            // The child list does not contain Drawable
            // ref_ptr so we can not return any
            // references to them.
            >
    {
        public:

        DrawablesIterator()
        {}

        explicit DrawablesIterator(osg::NodeList::iterator const& node_it):
        DrawablesIterator::iterator_adaptor_(node_it)
            {}

        private:
        friend class boost::iterator_core_access;
        osg::ref_ptr<osg::Drawable> dereference() const
        {
            return base_reference()->get()->asDrawable();
        }
    };*/
