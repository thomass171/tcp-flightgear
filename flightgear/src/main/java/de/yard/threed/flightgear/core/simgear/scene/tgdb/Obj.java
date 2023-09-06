package de.yard.threed.flightgear.core.simgear.scene.tgdb;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.loader.AbstractLoader;
import de.yard.threed.engine.loader.InvalidDataException;
import de.yard.threed.engine.loader.LoaderGLTF;
import de.yard.threed.engine.loader.PortableModelList;
import de.yard.threed.flightgear.LoaderOptions;
import de.yard.threed.core.Vector3;
import de.yard.threed.flightgear.core.osg.Group;
import de.yard.threed.flightgear.core.osg.Node;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterialCache;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterialLib;
import de.yard.threed.flightgear.core.simgear.scene.util.SGReaderWriterOptions;

import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.flightgear.LoaderBTG;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.core.buffer.ByteArrayInputStream;
import de.yard.threed.core.StringUtils;

/**
 * aus obj.cxx
 * <p/>
 * Created by thomass on 04.08.16.
 */
public class Obj {
    static Log logger = Platform.getInstance().getLog(Obj.class);

    /**
     * Resource muss Bundle mit path der resource enthalten.
     * Bundle muss schon geladen sein. Suffix ".gz" darf nicht mit angegeben werden.
     * 14.12.17: Wegen preprocess zerlegt in alten SGLoadBTG und neuen SGbuildBTG.
     *
     * @return
     */
    public static Node SGLoadBTG(BundleResource bpath, SGReaderWriterOptions options, LoaderOptions boptions) {
        PortableModelList ppfile = SGLoadBTG(bpath, options, boptions, 222);
        if (ppfile==null){
            //already logged
            return null;
        }
        return SGbuildBTG(ppfile,boptions!=null?boptions.materialLib:null);
    }

    /**
     * Runs sync. Bundle must have been loaded already.
     */
    public static PortableModelList/*Node*/ SGLoadBTG(BundleResource bpath, SGReaderWriterOptions options, LoaderOptions boptions, int dummy) {
        //tsch_log("obj.cxx::SGLoadBTG(%d) path=%s \n",0,path.c_str());

        /*SGBinObject*/
        AbstractLoader tile;
        //if (!tile.read_bin(path))
        //    return NULL;
        if (bpath == null) {
            throw new RuntimeException("bpath isType null");
            //tile = (LoaderBTG) ModelFactory.readModel(new FileSystemResource(path));
        } else {
            
            //TODO ueber platform. 21.12.17: NeeNee. Ich lad mal direkt das btg.
            // Auch mit GLTF ist das Laden ueber die Platform so einfach nicht möglich, weil eine Sonderbehandlung fuer das Material/Landclasses
            // erforderlich ist. Daher erstmal weiterhin hier das Model bauen.
          //LoadResult lr = ModelLoader.readModelFromBundle(bpath, false);
            if (bpath.bundle == null) {
                logger.warn("no bundle set");
                return null;
            }

            if (boptions != null && boptions.usegltf){
                BundleData ins = null;
                String basename = StringUtils.substringBeforeLast(bpath.getFullName(),".btg");
                bpath = new BundleResource(bpath.bundle,basename+".gltf");
                ins = bpath.bundle.getResource(bpath);
                if (ins == null) {
                    logger.error(bpath.getName() + " not found in bundle " + bpath);
                    return null;
                }

                try {
                    tile = LoaderGLTF.buildLoader(bpath,null);
                } catch (InvalidDataException e) {
                    throw new RuntimeException(e);
                }
            }else {
                //load from BTG

                BundleData ins = null;
                // special handling of btg.gz files. Irgendwie Driss
                // 12.6.17: Andererseits werden pp files hier auch transparent geladen. Aber das macht eh schon der Bundleloader, damit der unzip in der platform ist.
                ins = bpath.bundle.getResource(bpath);
                if (ins == null) {
                    logger.error("loadModel " + bpath.getName() + " not found in bundle " + bpath);
                    return null;
                }

                try {
                    tile = new LoaderBTG(new ByteArrayInputStream(ins.b), options, boptions,bpath.getFullName());
                } catch (InvalidDataException e) {
                    throw new RuntimeException(e);
                }
            }
            //21.12.17 tile = (LoaderBTG) lr.loader;
        }

        // Code moved to LoaderBTG.preProcess();
        PortableModelList ppfile = tile.preProcess();
        //ppfile.btgcenter = ((LoaderBTG)tile).center;
        return ppfile;
    }

    /**
     * 18.4.19: matlib wird als optional" betrachtet. Ohne wirds wireframe.
     * @return
     */
    public static Node SGbuildBTG(PortableModelList ppfile, SGMaterialLib matlib) {
        boolean simplifyDistant = false;
        boolean simplifyNear = false;
        boolean useVBOs = false;
        SGTileGeometryBin tileGeometryBin = new SGTileGeometryBin();

        Vector3 center = (ppfile.getObject(0).translation);

        SGMaterialCache matcache=null;
        if (matlib != null) {
            matcache = matlib.generateMatCache(SGGeod.fromCart(center));
        }

        //18.4.19: Mesh isType build in getSurfaceGeometryPart2 with material from matcache.
        //The GLTF itself does not contain material.
        Node node = tileGeometryBin.getSurfaceGeometryPart2(ppfile/*.gml*/, useVBOs,matcache);
        if (node != null && simplifyDistant) {
            //osgUtil::Simplifier simplifier(ratio, maxError, maxLength);
            //node->accept(simplifier);
        }

        // The toplevel transform for that tile.
        /*osg::MatrixTransform* transform = new osg::MatrixTransform;
        transform->setName(path);
        transform->setMatrix(osg::Matrix::setRotateStatus(toOsg(hlOr))*
            osg::Matrix::translate(toOsg(center)));*/
        Group transform = new Group();
        //24.1.19: Der "name" wird in der Hierarchie dann doppelt sein, oder?
        transform.setName(ppfile.getName());
        //transform.getTransform().setPosition(ppfile.btgcenter);
        transform.getTransform().setPosition(center);

        if (node != null) {
            // tile points
            /*SGTileDetailsCallback* tileDetailsCallback = new SGTileDetailsCallback;
            tileDetailsCallback->insertPtGeometry( tile, matcache );

            // PagedLOD for the random objects so we don't need to generate
            // them all on tile loading.
            osg::PagedLOD* pagedLOD = new osg::PagedLOD;
            pagedLOD->setCenterMode(osg::PagedLOD::USE_BOUNDING_SPHERE_CENTER);
            pagedLOD->setName("pagedObjectLOD");*/

            if (simplifyNear == simplifyDistant) {
                // Same terrain type isType used for both near and far distances,
                // so add it to the main group.
                Group terrainGroup = new Group();
                terrainGroup.setName("BTGTerrainGroup");
                terrainGroup.attach(node);
                transform.attach(terrainGroup);
            } else if (simplifyDistant) {
                // Simplified terrain isType only used in the distance, the
                // call-back below will re-generate the closer version
                //TODO pagedLOD.addChild(node, object_range + SG_TILE_RADIUS, FLT_MAX);
            }

           /* osg::ref_ptr<SGReaderWriterOptions> opt;
            opt = SGReaderWriterOptions::copyOrCreate(options);

            // we just need to know about the read file callback that itself holds the data
            tileDetailsCallback->_options = opt;
            tileDetailsCallback->_path = std::string(path);
            tileDetailsCallback->_loadterrain = ! (simplifyNear == simplifyDistant);
            tileDetailsCallback->_gbs_center = center;
            tileDetailsCallback->_rootNode = node;
            tileDetailsCallback->_randomSurfaceLightsComputed = false;
            tileDetailsCallback->_tileRandomObjectsComputed = false;

            osg::ref_ptr<osgDB::Options> callbackOptions = new osgDB::Options;
            callbackOptions->setObjectCacheHint(osgDB::Options::CACHE_ALL);
            callbackOptions->setReadFileCallback(tileDetailsCallback);
            pagedLOD->setDatabaseOptions(callbackOptions.get());

            // Ensure that the random objects aren't expired too quickly
            pagedLOD->setMinimumExpiryTime(pagedLOD->getNumChildren(), tile_min_expiry);
            pagedLOD->setFileName(pagedLOD->getNumChildren(), "Dummy filename for random objects callback");
            pagedLOD->setRange(pagedLOD->getNumChildren(), 0, object_range + SG_TILE_RADIUS);
            transform->addChild(pagedLOD);*/
        }

        //transform->setNodeMask( ~simgear::MODELLIGHT_BIT );
        return transform;
    }

}
