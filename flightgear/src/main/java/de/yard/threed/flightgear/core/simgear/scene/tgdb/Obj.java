package de.yard.threed.flightgear.core.simgear.scene.tgdb;

import de.yard.threed.core.GeneralParameterHandler;
import de.yard.threed.core.Util;
import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.core.loader.AbstractLoader;
import de.yard.threed.core.loader.InvalidDataException;
import de.yard.threed.core.loader.LoaderGLTF;
import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.loader.PortableModelDefinition;
import de.yard.threed.core.loader.PortableModel;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.platform.ResourceLoaderFromBundle;
import de.yard.threed.flightgear.LoaderOptions;
import de.yard.threed.core.Vector3;
import de.yard.threed.flightgear.core.osg.Group;
import de.yard.threed.flightgear.core.osg.Node;
import de.yard.threed.flightgear.core.simgear.scene.material.EffectGeode;
import de.yard.threed.flightgear.core.simgear.scene.material.Effect;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterial;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterialCache;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterialLib;
import de.yard.threed.flightgear.core.simgear.scene.util.SGReaderWriterOptions;

import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.flightgear.LoaderBTG;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.core.buffer.ByteArrayInputStream;
import de.yard.threed.core.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * From obj.cxx
 * 30.9.23: Methods no longer static.
 * <p/>
 * Created by thomass on 04.08.16.
 */
public class Obj {
    Log logger = Platform.getInstance().getLog(Obj.class);

    public List<String> foundMaterial = new ArrayList<String>();
    public List<String> notFoundMaterial = new ArrayList<String>();

    /**
     * Resource muss Bundle mit path der resource enthalten.
     * Bundle muss schon geladen sein. Suffix ".gz" darf nicht mit angegeben werden.
     * 14.12.17: Wegen preprocess zerlegt in alten SGLoadBTG und neuen SGbuildBTG.
     * 15.2.24: Now async
     *
     * @return
     */
    public void/*Node*/ SGLoadBTG(BundleResource bpath, SGReaderWriterOptions options, LoaderOptions boptions, GeneralParameterHandler<Node> delegate) {
        SGLoadBTG(bpath, options, boptions, 222, new GeneralParameterHandler<PortableModel>() {
            @Override
            public void handle(PortableModel ppfile) {
                if (ppfile == null) {
                    //already logged
                    return;
                }
                Node node = SGbuildBTG(ppfile, boptions != null ? boptions.materialLib : null);
                delegate.handle(node);
            }
        });

    }

    /**
     * Runs sync. Bundle must have been loaded already.
     * 15.2.14: No longer sync but asnyc like LoaderGLTF.
     */
    public void /*PortableModel/*Node*/ SGLoadBTG(BundleResource bpath, SGReaderWriterOptions options, LoaderOptions boptions, int dummy, GeneralParameterHandler<PortableModel> delegate) {
        //tsch_log("obj.cxx::SGLoadBTG(%d) path=%s \n",0,path.c_str());

        /*SGBinObject*/
        AbstractLoader tile = null;
        //if (!tile.read_bin(path))
        //    return NULL;
        if (bpath == null) {
            throw new RuntimeException("bpath is null");
            //tile = (LoaderBTG) ModelFactory.readModel(new FileSystemResource(path));
        } else {

            //TODO ueber platform. 21.12.17: NeeNee. Ich lad mal direkt das btg.
            // Auch mit GLTF ist das Laden ueber die Platform so einfach nicht möglich, weil eine Sonderbehandlung fuer das Material/Landclasses
            // erforderlich ist. Daher erstmal weiterhin hier das Model bauen.
            //LoadResult lr = ModelLoader.readModelFromBundle(bpath, false);
            if (bpath.bundle == null) {
                logger.warn("no bundle set");
                return /*null*/;
            }

            if (boptions != null && boptions.usegltf) {
                BundleData ins = null;
                String basename = StringUtils.substringBeforeLast(bpath.getFullName(), ".btg");
                bpath = new BundleResource(bpath.bundle, basename + ".gltf");
                ins = bpath.bundle.getResource(bpath);
                if (ins == null) {
                    logger.error(bpath.getName() + " not found in bundle " + bpath);
                    return /*null*/;
                }

                LoaderGLTF.load(new ResourceLoaderFromBundle(bpath), delegate);

            } else {
                // 22.7.24: This branch is still in use. So we cannot move LoaderBTG to tools yet.
                //load from BTG
                //12.11.24:Finally no valid use case not even for BTG conversion.
                Util.nomore();

                BundleData ins = null;
                // special handling of btg.gz files. Irgendwie Driss
                // 12.6.17: Andererseits werden pp files hier auch transparent geladen. Aber das macht eh schon der Bundleloader, damit der unzip in der platform ist.
                ins = bpath.bundle.getResource(bpath);
                if (ins == null) {
                    logger.error("loadModel " + bpath.getName() + " not found in bundle " + bpath);
                    return /*null*/;
                }

                try {
                    tile = new LoaderBTG(new ByteArrayInputStream(ins.b), options, boptions, bpath.getFullName());
                } catch (InvalidDataException e) {
                    throw new RuntimeException(e);
                }
                // Code moved to LoaderBTG.preProcess();
                PortableModel ppfile = tile.buildPortableModel();
                //ppfile.btgcenter = ((LoaderBTG)tile).center;
                delegate.handle(ppfile);
            }
            //21.12.17 tile = (LoaderBTG) lr.loader;
        }


        //return ppfile;
    }

    /**
     * 18.4.19: matlib is optional. Without it will be wireframe.
     *
     * @return
     */
    public Node SGbuildBTG(PortableModel ppfile, SGMaterialLib matlib) {
        boolean simplifyDistant = false;
        boolean simplifyNear = false;
        boolean useVBOs = false;

        PortableModelDefinition btgroot = getBtgDefinitionFromPortableModel(ppfile);
        Vector3 center = btgroot.translation;

        SGMaterialCache matcache = null;
        if (matlib != null) {
            matcache = matlib.generateMatCache(SGGeod.fromCart(center));
        }

        //18.4.19: Mesh isType build in getSurfaceGeometryPart2 with material from matcache.
        //The GLTF itself does not contain material.
        Node node = getSurfaceGeometryPart2(ppfile/*.gml*/, useVBOs, matcache);
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

    /**
     * Aus der allen Trianglemaps ein Mesh erstellen.
     * 12.12.17: Aufgeteilt um das erstellen der Geo im preprocess verwenden zu koennen.
     * 18.04.2019: ppfile contains no material. matcache is "optional". If null, wireframe will be created.
     * 30.9.23: Moved here from SGTileGeometryBin.
     * 13.11.2024: Meanwhile BTG converted GLTSs always contain a material list and land classes as material name.
     */
    public Node getSurfaceGeometryPart2(PortableModel ppfile, boolean useVBOs, SGMaterialCache matcache) {
        PortableModelDefinition btgroot = getBtgDefinitionFromPortableModel(ppfile);

        if (btgroot == null)
            return null;

        EffectGeode eg = null;
        //27.12.17:zur Vereinfachung immer group anlegen
        Group group = new Group();//(ppfile.objects.size() > 1 ? new Group() : null);
        if (group != null) {
            group.setName("surfaceGeometryGroup");
        }

        //osg::Geode* geode = new osg::Geode;
        //SGMaterialTriangleMap::const_iterator i;
        //for (i = materialTriangleMap.begin(); i != materialTriangleMap.end(); ++i) {
        //for (String ii : materialTriangleMap.keySet()) {
        for (int k = 0; k < btgroot.kids.size(); k++) {
            PortableModelDefinition po = btgroot.kids.get(k);
            //for (int index = 0; index < po.geolist.size(); index++) {
            SimpleGeometry p = po.geo;//list.get(index);

            /*in Part1 SGTexturedTriangleBin i = materialTriangleMap.get(ii);
             SimpleGeometry geometry = i/*->getSecond* /.buildGeometry(useVBOs);
            SGMaterial mat = null;
            if (matcache != null) {
                mat = matcache.find(ii/*->getFirst* /);
            }*/
            //11.11.24 back from PortableMaterial to core material
            Material mat = null;
            String matname = po.material;//geolistmaterial.get(index);
            // 11.11.24: BTG GLTF files contain a simple texture material when materiallib was used during conversion,
            // and as material name the landclass name.
            // matcache should be available, otherwise there will no material later very likely. (wireframe)
            mat = buildMaterialFromLandClass(matcache, matname);
            if (mat == null) {
                // no material found by land class
                logger.warn("material not found by land class '" + matname + "'. Falling back to simple GLTF material");
                PortableMaterial pm = ppfile.findMaterial(matname);
                if (pm != null) {
                    mat = Effect.buildMaterialWithResourceLoader(pm);
                } else {
                    logger.warn("Material not found in btg/gltf:" + matname);
                }
            }
            // FG-DIFF
            // In FG the material is created via Effect in EffectGeode (partly via Callback). And addDrawable/runGenerators?
            // We just create a mesh here (wireless if we don't have material; good for testing, but later?)
            //  Effect connects to the Shader and (might) contains textures in FG.
            eg = new EffectGeode();
            // FG-DIFF immer derselbe Name ist doch bloed. Es ist auch nicht erkennbar, dass FG da Logik dran hat. 12.12.17: Tja, jetzt hab ich aber keinen Namen.
            //eg.setName("EffectGeode");
            eg.setName(po.name);// + ii+"("+i.getNumTriangles()+" tris)");

            if (mat != null) {
                //12.12.17: im Effect ist das "endgültige" Material enthalten. Der EffectGeode treagt dazu nichts bei, zumindest nichts bekanntes.
                //Darum kommt jetzt auch nur noch "echtes" Material rein (wegen preprocess)
                eg.setMaterial(mat);
                //eg.setMaterial(p.mat);
                //Effect e = p.mat.get_one_effect(p.textureindex);///*->getSecond*/.getTextureIndex()));
                //eg.setEffect(e);
                foundMaterial.add(matname);
            } else {
                // 31.12.17 das log ich mal als warning, weils wirklich ein Grund zur Warnung ist
                // 18.04.19 das wird dann halt wireframe
                logger.warn("no material " + matname + " found");
                eg.setMaterial(null);
                notFoundMaterial.add(matname);
            }
            //eg.addDrawable(geometry);
            //eg.runGenerators(geometry);  // Generate extra data needed by effect
            if (group != null) {
                group.attach(eg);
            }
            eg.buildMesh(p/*.geo*/);
            //}
        }

        if (group != null) {
            return group;
        } else {
            return eg;
        }
    }

    /**
     * Up to 11/24 the material entry was the land class instead of an index to material.
     *
     * @param landclass
     * @return
     */
    private Material buildMaterialFromLandClass(SGMaterialCache matcache, String landclass) {
        Material mat = null;
        if (matcache == null) {
            logger.warn("no mat cache");
            return null;
        }
        SGMaterial sgmat = matcache.find(landclass);
        //31.12.17: TODO textureindx mal klaeren!
        //5.10.23: TODO use getEffectMaterialByTextureIndex
        if (sgmat == null) {
            logger.warn("No material for land class (material) '" + landclass + "'");
        } else {
            int textureindex = 0;
            //EffectMaterialWrapper wrapper = new EffectMaterialWrapper(null);
            Effect oneEffect = sgmat.get_one_effect(textureindex);
            if (oneEffect == null) {
                logger.warn("No effect available at " + textureindex + " for " + landclass);
            } else {
                mat = (sgmat != null) ? (oneEffect.material) : null;
            }
        }
        return mat;
    }

    /**
     * 31.7.24 When PortableModel is from GLTF, the btg is one level lower than from real BTG.
     * Real BTGs are used in tests.
     */
    public PortableModelDefinition getBtgDefinitionFromPortableModel(PortableModel portableModel) {
        if (portableModel.getRoot() != null && LoaderGLTF.GLTF_ROOT.equals(portableModel.getRoot().getName())) {
            return portableModel.getRoot().kids.get(0);
        }
        return portableModel.getRoot();
    }
}
