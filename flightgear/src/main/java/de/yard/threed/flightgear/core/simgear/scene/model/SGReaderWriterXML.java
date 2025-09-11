package de.yard.threed.flightgear.core.simgear.scene.model;

import de.yard.threed.core.CharsetException;
import de.yard.threed.core.Degree;
import de.yard.threed.core.Matrix4;
import de.yard.threed.core.ModelBuildDelegate;
import de.yard.threed.core.ModelPreparedDelegate;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.loader.PreparedModel;
import de.yard.threed.core.platform.AsyncHttpResponse;
import de.yard.threed.core.platform.AsyncJobDelegate;
import de.yard.threed.core.platform.NativeBundleResourceLoader;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.ResourceLoader;
import de.yard.threed.engine.loader.PortableModelBuilder;
import de.yard.threed.engine.platform.ResourceLoaderFromBundle;
import de.yard.threed.engine.platform.ResourceLoaderFromDelayedBundle;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.ModelLoader;
import de.yard.threed.flightgear.FgBundleHelper;
import de.yard.threed.flightgear.FgModelHelper;
import de.yard.threed.flightgear.LoaderOptions;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.PropertyList;
import de.yard.threed.flightgear.core.SGLoaderOptions;

import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.engine.*;
import de.yard.threed.flightgear.core.osg.Group;
import de.yard.threed.flightgear.core.osg.Node;
import de.yard.threed.flightgear.core.osgdb.ReadResult;

import de.yard.threed.flightgear.core.osgdb.osgDB;
import de.yard.threed.flightgear.core.simgear.misc.SGPath;
import de.yard.threed.flightgear.core.simgear.props.PropsIO;
import de.yard.threed.flightgear.core.simgear.scene.util.SGReaderWriterOptions;
import de.yard.threed.flightgear.core.simgear.scene.util.SGTransientModelData;
import de.yard.threed.flightgear.core.simgear.structure.SGException;

import de.yard.threed.core.platform.Log;

import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.core.BuildResult;
import de.yard.threed.core.platform.Config;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.yard.threed.engine.platform.EngineHelper.LOADER_APPLYACPOLICY;
import static de.yard.threed.flightgear.FgModelHelper.mapFilename;


/**
 * Reading a XML properylist file.
 * <p>
 * 28.12.16: Der Versuch, das ganze ECS maessig aufzudröseln ist zwecklos. Dafür sind die Model zu sehr ineinander verschachtelt (submodel ineinander und propertylist includes).
 * <p>
 * 24.1.17: Er braucht aber auch z.B. AircraftResourceProvider oder andere.
 * 10.4.17: "Saubere" Variante mit Bundle. ist kein lookup per osgDB.findDataFile() erforderlich. Die Klasse muss trotzdem auch simple Model (z.B. ac laden koennen, weil
 * sie rekursiv aufgerufen wird.
 * <p>
 * 16.09.17: Und jetzt auch async. Damit ist nach laden und liefern der group zum XML dieses nicht unbedingt schon vollstaendig.
 * <p>
 * 07.06.18: Diese Klasse ist ueberigens nicht fuer das "-set.xml" file. Das wird in fg_init geladen.
 * <p>
 * Created by thomass on 07.12.15.
 */
public class SGReaderWriterXML {
    static Log logger = Platform.getInstance().getLog(SGReaderWriterXML.class);
    private static boolean async = true;
    public static boolean fgmodelloaddebug = false;
    // few helper for tests
    public static int errorCnt = 0;
    public static List<String> loadedList = new ArrayList<String>();
    public static List<String> failedList = new ArrayList<String>();

    public SGReaderWriterXML() {
        //MA17 supportsExtension("xml", "");
    }

    /**
     * 10.4.17:Variante ueber Bundle. Cache braucht es nicht mehr, weil es preprocessed models gibt.
     * Liefert ReadResult, um auch Animationen zu liefern zu koennen, die werden sukzessive erweitert.
     * Die rootnode wird auch für die Animationen gebraucht.
     * 25.4.17: Die BundleResource sollte/muss auch das Bundle enthalten. Hier das passende Bundle zu resolven dürfte schwierig sein, vor allem bei sowa wie TerrainTiles.
     * Die Loader/reader/Processor entkoppelt, damit hier kein SGPropertyNode und opttexturepath rein muss.
     * 12.6.17: Nicht mehr fuer STG Files. Die sind doch einfach zu speziell. Aber immer noch für btg.
     * Hier werden auch XML model gelesen, dafuer sind die "options". Aber eigenlich ist xml doch genauso speziell wie stg und sollte auch eine eigene MEthode haben.
     * 15.9.17: War vorher die allgemeingueltige Methode in ModelFactory. Das ist jetzt DIE Methode zum XML Model lesen.
     * Und da kommt per options auch der Property Tree fuer Animationen rein.
     * 4.4.18: Der modeldelegate wird fuer jedes submodel aufgerufen, also evtl. sehr oft!
     * Die einzelnen Model werden async geladen und nach und nach eingehangen. Die Bundle muessen aber da sein.
     * <p>
     * 08.02.24: Not sure what of the above comments still fits. The pure XML load (includes inclusive) is done snyc from bundle,
     * the referenced model (ac/gltf/xml) are loaded async. Also referenced XML?
     * Independent from "modeldelegate" (which might be null), the loaded model is hooked async into the destination node.
     */
    public static BuildResult buildModelFromBundleXML(BundleResource modelfile, LoaderOptions options, XmlModelCompleteDelegate modeldelegate) {

        SGReaderWriterXML ldr = new SGReaderWriterXML();
        BuildResult result = ldr.sgLoad3DModel_internal(modelfile, options, modeldelegate);
        return result;
    }

    /**
     * 10.2.24: Also the XML is loaded async. Only returns a complete empty destination node.
     */
    public static BuildResult buildModelFromXML(ResourceLoader resourceLoader, LoaderOptions options, XmlModelCompleteDelegate modeldelegate) {

        SGReaderWriterXML ldr = new SGReaderWriterXML();
        Util.notyet();
        BuildResult result = null;//ldr.sgLoad3DModel_internal_async(modelfile, options, modeldelegate);
        if (result == null) {
            //already logged.
            result = new BuildResult("load failed");
        }
        return result;
    }

    /**
     * Create animations, which in fact are effects (10.2.24: really is the same?).
     * 5.10.17: Als Animation werden die aber auch nochmal angelegt. Irgendwie unklar.
     */
    private void makeEffectAnimations(PropertyList animation_nodes, PropertyList effect_nodes) {
        logger.debug("makeEffectAnimations");
        //for (PropertyList::iterator itr = animation_nodes.begin(); itr != animation_nodes.end();++itr) {
        for (int i = 0; i < animation_nodes.size(); ++i) {
            SGPropertyNode animProp = animation_nodes.get(i);//itr->ptr();
            SGPropertyNode typeProp = animProp.getChild("type");
            if (typeProp == null)
                continue;

            SGPropertyNode/*_ptr*/ effectProp = null;

            String typeString = typeProp.getStringValue();
            if (typeString.equals("material")) {
                // obs das wirklich braucht? mal weglassen. Als Animation wird das ja auch nocht angelegt.
                //effectProp = SGMaterialAnimation.makeEffectProperties (animProp);
            } else if (typeString.equals("shader")) {
                logger.error("skipping animation shader");
                //Util.notyet();
            /*SGPropertyNode* shaderProp = animProp->getChild("shader");
            if (!shaderProp || strcmp(shaderProp->getStringValue(), "chrome"))
                continue;
            *itr = 0;           // effect replaces animation
            SGPropertyNode* textureProp = animProp->getChild("texture");
            if (!textureProp)
                continue;
            effectProp = new SGPropertyNode();
            makeChild(effectProp.ptr(), "inherits-from")
            ->setValue("Effects/chrome");
            SGPropertyNode* paramsProp = makeChild(effectProp.get(), "parameters");
            makeChild(paramsProp, "chrome-texture")
            ->setValue(textureProp->getStringValue());*/
            }
            if (effectProp != null/*.valid()*/) {
                PropertyList objectNameNodes = animProp.getChildren("object-name");
                //for (PropertyList::iterator objItr = objectNameNodes.begin(), end = objectNameNodes.end(); objItr != end; ++objItr)
                for (SGPropertyNode objItr : objectNameNodes) {
                    effectProp.addChild(new SGPropertyNode("object-name")).setStringValue(objItr.getStringValue());
                    effect_nodes.add(effectProp);
                }
            }
            //TODO ??animation_nodes.erase(remove_if(animation_nodes.begin(), animation_nodes.end(),  !boost::bind(&SGPropertyNode_ptr::valid,
            //    _1)),  animation_nodes.end());
        }
    }

    /**
     * 26.12.18: Muss bei Fehler leeres BuildResult liefern.
     */
    private BuildResult sgLoad3DModel_internal(BundleResource bpath, LoaderOptions bdbOptions, XmlModelCompleteDelegate modeldelegate) {
        /* 14.2.24: just an idea:
         SceneNode destinationNode=new SceneNode();
        // only a temporary name
        destinationNode.setName("XML-destinationNode");
        BuildResult result = new BuildResult(destinationNode.nativescenenode);

        resourceLoader.loadResource(response->{
            String content;
            try {
            content=    response.getContentAsString();
            } catch (CharsetException e) {
                // TODO better error handling
                throw new RuntimeException(e);
            }
            BuildResult br1 = sgLoad3DModel_internal_async(content, resourceLoader.isXml(), bdbOptions, modeldelegate);
            if (br1==null){
                failedList.add(bpath.getFullQualifiedName());

            }
        });
         end of idea */

        BuildResult result = sgLoad3DModel_internal_async(bpath, bdbOptions, modeldelegate);
        if (result == null) {
            //already logged. 28.9.19 ob der Constructor so gut ist?
            result = new BuildResult("load failed");
        }
        return result;
    }

    /**
     * Sync load of a XML model file. As of 2017 no longer for gltf/ac. Those referenced from XML are delegated async to the platform.
     * Returns null in case of an error (no exception is thrown), which already was logged. Its up
     * to the caller to create an empty BuildResult for avoiding NPEs (its hard to do that here).
     * <p>
     * 27.12.16: Kann die gelesenen Properties jetzt auch uebergeben bekommen. Ginge zwar, durch die Rekursion in die submodel hilft das aber nur wenig.
     * 10.04.17: Wenn es ein Bundle gibt, wird der "bpath" relativ in dieses Bundle betrachtet.
     * 15.09.17: Jetzt async. Das ist ein kompletter Umbau des Ablaufs.
     * xx.xx.?? bpath is considered to be not null and "(SG)path" no longer is an option!
     * 12.02.24: exclusive for XML? (not yet)
     * 25.08.24: Even though we have bundleless ResourceLoader meanwhile, we stay with bundle for easier handling of nested XMLs.
     *
     * @return
     */
    private BuildResult sgLoad3DModel_internal_async(BundleResource bpath, LoaderOptions bdbOptions, final XmlModelCompleteDelegate modeldelegate) {
        de.yard.threed.flightgear.core.osgdb.Options dbOptions = null;
        SGPropertyNode overlay = null;

        logger.debug("sgLoad3DModel_internal loading " + bpath.getFullQualifiedName());

        /*osg::ref_ptr <*/
        // 4.1.18: das mit dem copy is doch Driss
        SGReaderWriterOptions options = SGReaderWriterOptions.copyOrCreate(dbOptions);
        SGLoaderOptions boptions = SGLoaderOptions.copyOrCreate(bdbOptions);

        BundleResource bmodelpath = bpath;
        ResourcePath btexturepath = null;
        SGPath modelpath = null;
        SGPath texturepath = null;
        SGPath modelDir = null;
        boolean isxml = bpath.getExtension().equals("xml");

        /*SGSharedPtr<*/
        // Ueber die Options kann DER PropertyTree reinkommen. Den brauchen z.B. die Animationen.
        // 7.6.18: Mittlerweile ist das aber ein z.B. Vehicle spezifischer Tree. Aber nicht fuer "-set.xml", der
        // nicht hier gelesen wird.
        SGPropertyNode/*>*/ prop_root = options.getPropertyNode();
        if (prop_root == null/*!prop_root.valid()*/) {
            prop_root = boptions.getPropertyNode();
            if (prop_root == null/*!prop_root.valid() */) {
                prop_root = new SGPropertyNode();
            }
        }
        SGPropertyNode final_prop_root = prop_root;

        // The model data appear to be only used in the topmost model
        /*osg::ref_ptr <*/
        SGModelData data = options.getModelData();
        options.setModelData(null);

        //osg::ref_ptr < osg::Node > model;
        SceneNode model = null;
        //osg::ref_ptr < osg::Group > group;

        // 7.6.18: Die Properties kommen erst in eine temp node? Weil vieles im Haupttree nicht relevant ist?
        // Das "-set.xml" wird nicht hierueber geladen (sondern in fg_init), nur das model xml.
        SGPropertyNode/*_ptr*/ props = new SGPropertyNode();
        boolean previewMode = false;//dbOptions.getPluginStringData("SimGear::PREVIEW").equals("ON");

        // Check for an XML wrapper

        if (isxml) {
            logger.debug("found nodelpath xml. modelpath=" + modelpath + ",modelDir=" + modelDir);

            PropsIO propsIO = new PropsIO();
            try {
                propsIO.readProperties(bpath, props);
            } catch (SGException t) {
                logger.error("Failed to load xml: " + t.getMessage());
                failedList.add(bpath.getFullQualifiedName());
                return null;
            }

            if (overlay != null) {
                PropsIO.copyProperties(overlay, props);
            }
            if (previewMode && props.hasChild("nopreview")) {
                logger.warn("no preview");
                return null;
            }

            if (props.hasValue("/path")) {

                String modelPathStr = props.getStringValue("/path");
                logger.debug("found path prop. value=" + modelPathStr);

                bmodelpath = FgBundleHelper.findPath(modelPathStr, bpath);
                if (bmodelpath == null) {
                    logger.error("Failed to resolve " + modelPathStr + " in " + bpath);
                    errorCnt++;
                    failedList.add(bpath.getFullQualifiedName());
                    return null;
                }

                if (props.hasValue("/texture-path")) {
                    String texturePathStr = props.getStringValue("/texture-path");
                    if (!StringUtils.empty(texturePathStr)) {
                        //02.10.19 bpath ist ja wohl ein XML und bmodelpath ein AC. Und die Texture dann beim AC suchen, nicht XML (z.B. bluebird yoke)
                        //Total heikel. Was soll denn "Aircraft/Instruments-3d/yoke" sein? Relativ vom AC? Wohl nicht. Hier muss wohl nochmal ein resolve gemacht
                        //werden. Aber wie, nur auf den Pfad? Kann ich nicht. Das ist so eine FG Sonderlocke. Erstmal so nachbilden.
                        //Das ist total krampfig. TODO dabrauchts auch Tests zu.
                        if (StringUtils.startsWith(texturePathStr, "Aircraft/")) {
                            btexturepath = new ResourcePath(texturePathStr);
                        } else {
                            //2.10.10:modelpath statt bpath führt zu AI Fliegern ohne Textur.
                            btexturepath = new ResourcePath(bpath/*bpath*/.getPath().getPath() + "/" + texturePathStr);
                        }
                    }
                }
            } else {
                model = new /*osg::*/Node();
            }

            SGPropertyNode mp = props.getNode("multiplay");
            if (mp != null && prop_root != null && prop_root.getParent() != null) {
                PropsIO.copyProperties(mp, prop_root);
            }
        } else {
            // No XML but just a model without XML wrapper. For submodels inside XMLs that do not reference
            // other XML but directly ac model files.
            // Nothing to do here.
            int h = 9;
            Util.nomore();
        }

        BundleResource pendingbmodelpath = null;

        if (model == null) {
            // pure ac model or XML with "path".
            // Assume that textures are in
            // the same location as the XML file.
            if (btexturepath != null) {
                logger.debug("loading textures(?) or model from modelpath " + bpath.getFullName() + ",texturepath=" + (btexturepath.getPath()));
                boptions.setDatabasePath(new ResourcePath(btexturepath.getPath()));
            }

            if (bmodelpath != null) {
                String extension = bmodelpath.getExtension();
                BuildResult modelResult;
                if (extension.equals("xml")) {
                    // current XML references another XML??
                    modelResult = buildModelFromBundleXML(bmodelpath,/*TODO 25.4.17*/boptions, null);
                    //modelResult = new ReadResult(osgDB.readNodeFile(bmodelpath, null, options/*.get()*/));
                    if (modelResult == null || modelResult.getNode() == null) {
                        logger.error("Failed to build 3D model:" + ((modelResult == null) ? "" : modelResult.message()) /*10.4.17 +                        modelpath.str()*/);
                        return null;
                    }
                    model = new SceneNode(modelResult.getNode());//copyModel(modelResult.getNode());
                } else {
                    // 15.9.17 async Weiche. Ich les das model hier einfach nicht, sondern ganz zum Schluss async.
                    // modelResult = ModelFactory.buildModelFromBundle(bmodelpath);
                    pendingbmodelpath = bmodelpath;
                    // Das async geladene model wird spater eingehangen.
                    model = null;
                }

            } else {
                ReadResult modelResult = new ReadResult(osgDB.readNodeFile(null, modelpath.str(), options/*.get()*/));
                if (!modelResult.validNode()) {
                    logger.error("Failed to load 3D model:" + modelResult.message() /*10.4.17 +                        modelpath.str()*/);
                    return null;
                }
                model = modelResult.getNode();//copyModel(modelResult.getNode());
            }
            // Add an extra reference to the model stored in the database.
            // That is to avoid expiring the object from the cache even if
            // it is still in use. Note that the object cache will think
            // that a model is unused if the reference count is 1. If we
            // clone all structural nodes here we need that extra
            // reference to the original object
            /*TODO SGDatabaseReference * databaseReference;
            databaseReference = new SGDatabaseReference(modelResult.getNode());
            model -> addObserver(databaseReference);*/

            // Update liveries
            /*TODO TextureUpdateVisitor liveryUpdate (options -> getDatabasePathList());
            model -> accept(liveryUpdate);*/

            // Copy the userdata fields, still sharing the boundingvolumes,
            // but introducing new data for velocities.
            /*TODO UserDataCopyVisitor userDataCopyVisitor;
            model.accept(userDataCopyVisitor);*/

            /*TODO SetNodeMaskVisitor setNodeMaskVisitor (0, simgear::MODELLIGHT_BIT);
            model.accept(setNodeMaskVisitor);*/
        }
        if (model != null) {
            // keep suffix like ".xml" in node name.
            model.setName(bpath.getFullName());
        }


        final Group group = new Group();
        boolean needTransform = false;
        // Set up the alignment node if needed
        SGPropertyNode offsets = props.getNode("offsets", false);
        if (offsets != null) {
            logger.debug("found offsets");

            needTransform = true;
            /*osg::MatrixTransform **/
            //Matrix4 alignmainmodel = new Matrix4();//osg::MatrixTransform;
            //SceneNode alignmainmodel = new SceneNode();
            // ? alignmainmodel -> setDataVariance(osg::Object::STATIC);
            /*osg::*/
            Matrix4 res_matrix;
            // TODO CHECK getFirst parameter y axis and getSecond x axis?
            //res_matrix/*.makeRotate*/ = Matrix4.buildRotationMatrix(

            // 15.8.17: Die Offsets müssen direkt in die group (und auch ohne Zwischengroup), damit sie auch für die submodels gelten,
            // die später evtl. auch noch in diese group kommen.
            //group = new Group();
            group.getTransform().setRotation(buildRotationFromOffsets(offsets));
            /*new Quaternion(new Degree(offsets.getFloatValue("roll-deg", 0.0f)),
                    new Degree(offsets.getFloatValue("pitch-deg", 0.0f))/* * SG_DEGREES_TO_RADIANS,
                    osg::Vec3 (0, 1, 0)* /,
                    //   new Degree(offsets.getFloatValue("roll-deg", 0.0f))/* * SG_DEGREES_TO_RADIANS,                    osg::Vec3 (1, 0, 0)* /,
                    new Degree(offsets.getFloatValue("heading-deg", 0.0f))/* * SG_DEGREES_TO_RADIANS,
                    osg::Vec3 (0, 0, 1)* /));*/

            /*osg::*/
            Matrix4 tmat;
            //tmat/*.makeTranslate*/=Matrix4.buildTranslationMatrix(
            group.getTransform().setPosition(buildVector3FromOffsets(offsets));
            //group.attach(alignmainmodel);
            //alignmainmodel = (res_matrix.multiply(/* * */tmat));
            // group = alignmainmodel;
            //15.9.17 spaeter group.attach(model);
        } else {
            /*if (group == null) {*/
            //group = new /*osg::*/Group();
            //15.9.17 spaeter group.attach/*addChild*/(model/*.get()*/);
        }


        // Load sub-models
        PropertyList /*Vector<SGPropertyNode_ptr>*/ model_nodes = props.getChildren("model");

        logger.debug("loading sub models. cnt=" + model_nodes.size());
        for (int i = 0; i < model_nodes.size(); i++) {
            SGPropertyNode/*_ptr*/ sub_props = model_nodes.get(i);

            SGPath submodelpath;
            /*osg::ref_ptr < osg::*/
            /*Node*/
            SceneNode submodel;

            String subPathStr = sub_props.getStringValue("path");
            logger.debug("loading sub model " + subPathStr + " at " + i);

            SGPath submodelPath = null;
            BundleResource bsubmodelpath = null;
            bsubmodelpath = FgBundleHelper.findPath(subPathStr, bpath);
            if (bsubmodelpath == null) {
                logger.error("Failed (sub model path is null) to load file: \"" + subPathStr + "\"");
                failedList.add(subPathStr);
                continue;
            }

            if (sub_props.hasChild("usage")) { /* We don't want load this file and its content now */
                boolean isInterior = sub_props.getStringValue("usage").equals("interior");
                boolean isAI = prop_root.getStringValue("type").equals("AI");
                if (isInterior && isAI) {
                    props.addChild(new SGPropertyNode("interior-path")).setStringValue(submodelPath.str());
                    continue;
                }
            }

            SceneNode submodelresultNode = null;
            // 12.2.24: No longer use same methode for XML and non XML. Decide here.
            if (bsubmodelpath.getExtension().equals("xml")) {
                BuildResult submodelresult = sgLoad3DModel_internal(bsubmodelpath,/*MA23 submodelPath, options/*.get()* /,
                    sub_props.getNode("overlay"),*/ boptions, modeldelegate);
                // 27.3.25 submodel load might fail, so check.
                if (submodelresult.getNode() != null) {
                    submodelresultNode = new SceneNode(submodelresult.getNode());
                }
            } else {
                BundleResource finalbsubmodelpath = bsubmodelpath;
                SceneNode destinationNode = new SceneNode();
                destinationNode.setName(finalbsubmodelpath.getFullName());
                submodelresultNode = destinationNode;
                FgModelHelper.buildNativeModel(new ResourceLoaderFromBundle(finalbsubmodelpath), btexturepath, (BuildResult result) -> {
                    if (result.getNode() != null) {
                        destinationNode.attach(new SceneNode(result.getNode()));
                        // there should be no need here to load animations. And no need to inform delegate, its just a sub model.
                    } else {
                        logger.error("model built failed for " + finalbsubmodelpath.getFullName());
                    }

                }, boptions.usegltf ? EngineHelper.LOADER_USEGLTF : 0);
            }

            submodel = submodelresultNode;

            if (submodel/*20.7.21 submodel*/ == null) {
                //error already logged
                //Nicht return sondern weitermachen. Es gibt ja noch weitere.
                // Die error messages kann man nicht sammeln, weil das ja async ist/sein kann und der error
                // erst auftritt, wenn das BuildResult schon geliefert wurde.
                continue;
            }

            /*osg::ref_ptr < osg::*/
            SceneNode submodel_final = submodel;
            SGPropertyNode offs = sub_props.getNode("offsets", false);
            if (offs != null) {
                /* osg::Matrix res_matrix;
                osg::ref_ptr < osg::MatrixTransform > align = new osg::MatrixTransform;
                align -> setDataVariance(osg::Object::STATIC);
                res_matrix.makeIdentity();
                res_matrix.makeRotate(
                        offs -> getDoubleValue("pitch-deg", 0.0) * SG_DEGREES_TO_RADIANS,
                        osg::Vec3 (0, 1, 0),
                offs -> getDoubleValue("roll-deg", 0.0) * SG_DEGREES_TO_RADIANS,
                        osg::Vec3 (1, 0, 0),
                offs -> getDoubleValue("heading-deg", 0.0) * SG_DEGREES_TO_RADIANS,
                        osg::Vec3 (0, 0, 1));

                osg::Matrix tmat;
                tmat.makeIdentity();
                tmat.makeTranslate(offs -> getDoubleValue("x-m", 0),
                        offs -> getDoubleValue("y-m", 0),
                        offs -> getDoubleValue("z-m", 0));
                align -> setMatrix(res_matrix * tmat);
                align -> addChild(submodel.get());
                submodel_final = align;*/
                Group align = new Group();
                align.getTransform().setRotation(buildRotationFromOffsets(offs));
                align.getTransform().setPosition(buildVector3FromOffsets(offs));
                //10.1.19: Not existing in FG
                align.getTransform().setScale(buildScaleFromOffsets(offs));

                align.attach/*addChild*/(submodel);
                submodel_final = align;
            }
            submodel_final.setName(sub_props.getStringValue("name", ""));

            SGPropertyNode cond = sub_props.getNode("condition", false);
            if (cond != null) {
               /*TODO osg::ref_ptr < osg::Switch > sw = new osg::Switch;
                sw -> setUpdateCallback(new SGSwitchUpdateCallback(sgReadCondition(prop_root, cond)));
                group.addChild(sw.get());
                sw -> addChild(submodel_final.get());
                sw -> setName("submodel condition switch");*/
            } else {
                group.attach/*addChild*/(submodel_final/*.get()*/);
            }
        } // end of submodel loading

        /*TODO
        /*osg::* /Node * ( * load_panel)(SGPropertyNode *) = options -> getLoadPanel();
        //logger.debug("load_panel=%d\n", load_panel);
        if (load_panel) {
            // Load panels
            PropertyList /*vector<SGPropertyNode_ptr>* / panel_nodes = props.getChildren("panel");
            for (int i = 0; i < panel_nodes.size(); i++) {
                //SG_LOG(SG_INPUT, SG_DEBUG, "Loading a panel");
                osg::ref_ptr < osg::Node > panel = load_panel(panel_nodes[i]);
                if (panel_nodes.get(i).hasValue("name"))
                panel.setName(panel_nodes[i].getStringValue("name"));
                group.addChild(panel.get());
            }
        }*/

       /*TODO  if (dbOptions -> getPluginStringData("SimGear::PARTICLESYSTEM") != "OFF") {
            std::vector < SGPropertyNode_ptr > particle_nodes;
            particle_nodes = props -> getChildren("particlesystem");
            for (unsigned i = 0; i < particle_nodes.size(); ++i) {
                osg::ref_ptr < SGReaderWriterOptions > options2;
                options2 = new SGReaderWriterOptions( * options);
                if (i == 0) {
                    if (!texturepath.extension().empty())
                        texturepath = texturepath.dir();

                    options2 -> setDatabasePath(texturepath.str());
                }
                group -> addChild(Particles::appendParticles (particle_nodes[i],
                        prop_root,
                        options2.get()));
            }
        }*/

        PropertyList /*std::vector < SGPropertyNode_ptr >*/ text_nodes;
        text_nodes = props.getChildren("text");
        //logger.debug("loading text nodes. cnt=%d\n", text_nodes.size());
        for (int i = 0; i < text_nodes.size(); ++i) {
           /*TODO group.addChild(SGText::appendText (text_nodes.get(i),
                    prop_root,
                    options.get()));*/
        }

        List<SGAnimation> animationList = new ArrayList<SGAnimation>();
        group.setName("XmlDestination");
        // 6.2.24: Be more specific with name setting

        group.setName(bpath.getFullName());

        BuildResult xmlresult = new BuildResult(group.nativescenenode/*, animationList*/);
        // 15.9.17: und jetzt das noch fehlende Model. Ob async oder nicht, erst jetzt einhaengen.
        if (pendingbmodelpath != null) {
            // Load ac/gltf. Not for XML. The needed data (gltf) might
            // 1) already be available in a loaded bundle
            // 2) to be loaded via HTTP in general
            // 3) in a delayed/dummy bundle (26.8.24).
            BundleResource finalpendingbmodelpath = pendingbmodelpath;
            BundleResource finalbpath = bpath;
            ResourcePath finalbtexturepath = btexturepath;

            ModelBuildDelegate modelBuildDelegate = (BuildResult result) -> {
                // result sollte es immer geben.
                if (result != null && result.getNode() != null) {

                    group.attach(new SceneNode(result.getNode()));
                    //die group aendert sich wohl, also brauch ich auch keine returngroup
                    // Group returngroup = group;
                    buildAnimations(group, props, previewMode, final_prop_root, options, null/*12.2.24 path*/, bpath, animationList, bpath.getName());
                    if (modeldelegate != null) {
                        // 8.3.24: Passed source should be the XML, not the 'ac'.
                        modeldelegate.modelComplete(finalbpath, new SceneNode(result.getNode()), animationList);
                    }
                } else {
                    logger.error("model built failed for " + finalpendingbmodelpath.getFullName());
                }
            };

            if (pendingbmodelpath.bundle.isDelayed()) {
                int options1 = boptions.usegltf ? EngineHelper.LOADER_USEGLTF : 0;
                if (pendingbmodelpath.getFullName().endsWith("ac")) {
                    options1 |= LOADER_APPLYACPOLICY;
                }
                final int options2 = options1;
                // probably "TerraSync-model".
                //FgModelHelper.buildNativeModel(new ResourceLoaderFromDelayedBundle(pendingbmodelpath, bundleResourceLoader)/*resourceLoader*/,
                // finalbtexturepath, modelBuildDelegate, boptions.usegltf ? EngineHelper.LOADER_USEGLTF : 0);
                FgModelHelper.buildSharedModel(pendingbmodelpath, finalbtexturepath, modelBuildDelegate, options2);

            } else {
                FgModelHelper.buildNativeModel(new ResourceLoaderFromBundle(pendingbmodelpath)/*resourceLoader*/, btexturepath, modelBuildDelegate, boptions.usegltf ? EngineHelper.LOADER_USEGLTF : 0);
            }
        } else {
            // dann muss es ja da sein
            group.attach(model);
            //die group aendert sich wohl, also brauch ich auch keine returngroup
            // Group returngroup = group;
            buildAnimations(group, props, previewMode, prop_root, options, null/*12.2.24  path*/, bpath, animationList,
                    bpath.getName());
        }
        
        

       /*TODO if (!needTransform && returngroup.getNumChildren() < 2) {
            model = returngroup.getChild(0);
            returngroup -> removeChild(model.get());
            if (data.valid())
                data -> modelLoaded(modelpath.str(), props, model.get());
            return model.release();
        }
        if (data.valid())
            data -> modelLoaded(modelpath.str(), props, returngroup.get());
        if (props.hasChild("debug-outfile")) {
            std::string outputfile = props.getStringValue("debug-outfile",
                    "debug-model.osg");
            osgDB::writeNodeFile ( * returngroup, outputfile);
        }*/

        logger.debug("sgLoad3DModel_internal completed for " + bpath.getFullQualifiedName());

        loadedList.add(bpath.getFullQualifiedName());
        return xmlresult;//group;//returngroup/*.release()*/;
    }

    /**
     * Erstellen der Animationen und Effects, was auch async passieren kann.
     * Only known/implemented animations are built and stored in the passed "animationList".
     */
    void buildAnimations(Group group, SGPropertyNode props, boolean previewMode, SGPropertyNode prop_root, SGReaderWriterOptions options, SGPath path, BundleResource bpath, List<SGAnimation> animationList, String label) {
        PropertyList effect_nodes = props.getChildren("effect");
        PropertyList animation_nodes = props.getChildren("animation");

        if (previewMode) {
            /*TODO  PropertyList::iterator it;
            it = std::remove_if (animation_nodes.begin(), animation_nodes.end(), ExcludeInPreview());
            animation_nodes.erase(it, animation_nodes.end());*/
            Util.notyet();
        }
        // Some material animations (eventually all) are actually effects.
        makeEffectAnimations(animation_nodes, effect_nodes);

        /*ref_ptr<*/
        // 29.10.24: We don't replace the node via visitor like FG but create ModelFactories instead
        // FG Node modelWithEffects = Model.instantiateEffects(group/*.get()*/, effect_nodes, options/*.get()*/);
        // FG group = /*static_cast < Group * > (*/(Group) modelWithEffects/*.get()*/;
        Model.instantiateEffects(group/*.get()*/, effect_nodes, options/*.get()*/, label, bpath);

        logger.debug("Building animations for " + bpath.name + ". nodecount=" + animation_nodes.size());

        SGTransientModelData modelData = new SGTransientModelData(group, prop_root, options, bpath.name/*local8BitStr()*/);
        for (int i = 0; i < animation_nodes.size(); ++i) {
            if (previewMode && animation_nodes.get(i).hasChild("nopreview")) {
                /*TODOPropertyList names (animation_nodes.get(i).getChildren("object-name"));
                for (unsigned int n = 0;
                n<names.size (); ++n){
                    removeNamedNode(group, names[n]->getStringValue());
                } // of object-names in the animation
                continue;*/
            }

            /// OSGFIXME: duh, why not only model?????
            // 25.1.17: Der Frage schliesse ich mich an. group ist ca. zwei/drei ebenen über der AC Node und der AC transfrom node. Wenn
            // die animierte Node an die group gehagen wird, geht die ACprocesspolicy verloren.
            // Dafuer koennte der traverse in SGAnimation sein. 
            //FG-DIFF: Explizit den parent fuer die AnimationGroup reingeben.

            if (path != null) {
                //4.10.19:gibts doch nicht mehr
                Util.nomore();
                /*SGAnimation anim = SGAnimation.animate(group, animation_nodes.get(i), prop_root, options, path.str(), i);
                if (anim != null) {
                    animationList.add(anim);
                }*/
            }
            if (bpath != null) {
                /*
                 * Setup the model data for the node currently being animated.
                 */
                modelData.LoadAnimationValuesForElement(animation_nodes.get(i), i);

                SGAnimation anim = SGAnimation.animate(modelData
                        /*group, animation_nodes.get(i), prop_root, options, null, i*/, bpath.getName()+"."+i);
                if (anim != null) {
                    animationList.add(anim);
                }
            }
        }
    }

    /**
     * die alte synchron Variante
     *
     * @param bpath
     * @param path
     * @param dbOptions
     * @param overlay
     * @param bdbOptions
     * @return
     */
    /*private Node sgLoad3DModel_internal_sync(BundleResource bpath, final SGPath path, final de.yard.threed.fg.osgdb.Options dbOptions, SGPropertyNode overlay,
                                             LoaderOptions bdbOptions/*, SGPropertyNode alreadyloadedproperties* /) {
        if (Config.modelloaddebuglog) {
            logger.debug("sgLoad3DModel_internal");
        }
        if (path != null) {
            if (!path.exists()) {
                logger.error("Failed (!path.exists) to load file: \"" + path + "\"");
                return null;
            }
            if (Config.modelloaddebuglog) {
                logger.debug("SGReaderWriterXML::sgLoad3DModel_internal. path=" + path.file_base());
            }
        }

        
        
        SGReaderWriterOptions options = SGReaderWriterOptions.copyOrCreate(dbOptions);
        SGLoaderOptions boptions = SGLoaderOptions.copyOrCreate(bdbOptions);

        BundleResource bmodelpath = bpath;
        ResourcePath btexturepath = null;
        SGPath modelpath = null;
        SGPath texturepath = null;
        SGPath modelDir = null;
        boolean isxml;
        if (bpath == null) {
            modelpath = new SGPath(path);
            texturepath = new SGPath(path);
            modelDir = new SGPath(modelpath.dir());
            isxml = modelpath.extension().equals("xml");
        } else {
            isxml = bpath.getExtension().equals("xml");
        }
        
        
        // Ueber die Options kann DER PropertyTree reinkommen. Den brauchen z.B. die Animationen.
        SGPropertyNode/*>* / prop_root = options.getPropertyNode();
        if (prop_root == null/*!prop_root.valid()* /) {
            prop_root = boptions.getPropertyNode();
            if (prop_root == null/*!prop_root.valid() * /) {
                prop_root = new SGPropertyNode();
            }
        }

        // The model data appear to be only used in the topmost model
         
        SGModelData data = options.getModelData();
        options.setModelData(null);

        //osg::ref_ptr < osg::Node > model;
        SceneNode model = null;
        //osg::ref_ptr < osg::Group > group;
        Group group = null;

        SGPropertyNode props = new SGPropertyNode();
        boolean previewMode = false;//dbOptions.getPluginStringData("SimGear::PREVIEW").equals("ON");

        // Check for an XML wrapper

        if (isxml) {
            if (Config.modelloaddebuglog) {
                logger.debug("found nodelpath xml. modelpath=" + modelpath + ",modelDir=" + modelDir);
            }
            if (bpath != null) {
                try {
                    PropsIO.readProperties(bpath, props);
                } catch (SGException t) {
                    logger.error("Failed to load xml: " + t.getMessage());
                    return null;
                }
            } else {
                try {
                    PropsIO.readProperties(modelpath.str(), props);
                } catch (SGException t) {
                    logger.error("Failed to load xml: " + t.getMessage());
                    return null;
                }

            }

            if (overlay != null) {
                PropsIO.copyProperties(overlay, props);
            }
            if (previewMode && props.hasChild("nopreview")) {
                return null;
            }

            if (props.hasValue("/path")) {

                String modelPathStr = props.getStringValue("/path");
                if (Config.modelloaddebuglog) {
                    logger.debug("found path prop. value=" + modelPathStr);
                }
                if (bpath == null) {
                    modelpath = new SGPath(SGModelLib.findDataFile(modelPathStr, null, modelDir));
                    if (modelpath.isNull()) {
                        logger.error("Model file not found: '" + modelPathStr + "'" + path);
                        return null;
                    }
                    if (StringUtils.empty(modelpath.str())) {
                        logger.warn("modelpath found is empty. Missung ResourceProvider?");
                    }
                } else {
                    bmodelpath = BundleRegistry.findPath(modelPathStr, bpath);
                    if (bmodelpath == null) {
                        logger.error("Failed to resolve " + modelPathStr + " in " + bpath);
                        return null;
                    }
                }
                if (props.hasValue("/texture-path")) {
                    String texturePathStr = props.getStringValue("/texture-path");
                    if (!StringUtils.empty(texturePathStr)) {
                        if (bpath == null) {
                            texturepath = new SGPath(SGModelLib.findDataFile(texturePathStr, null, modelDir));
                            if (texturepath.isNull()) {
                                logger.error("Texture file not found: '" + texturePathStr + "'" + path);
                                return null;
                            }
                        } else {
                            btexturepath = new ResourcePath(bpath.getPath().path + "/" + texturePathStr);
                        }
                    }
                }
            } else {
                model = new Node();
            }

            SGPropertyNode mp = props.getNode("multiplay");
            if (mp != null && prop_root != null && prop_root.getParent() != null) {
                PropsIO.copyProperties(mp, prop_root);
            }
        } else {
            // model without wrapper
            // 27.12.16: Was sind denn das für Cases?
            // 10.4.17: Das dueften z.B. reine ac Model sein.
        }

        if (model == null) {
            // "simples" model, z.B. ac
            // Assume that textures are in
            // the same location as the XML file.
            if (bpath == null) {
                if (texturepath != null) {
                    if (!StringUtils.empty(texturepath.extension()))
                        texturepath = texturepath.dir();

                    if (Config.modelloaddebuglog) {
                        logger.debug("loading textures(?) or model from modelpath " + ((bpath == null) ? modelpath.str() : bpath.getFullName()) + ",texturepath=" + texturepath.str());
                    }
                    options.setDatabasePath(texturepath.str());
                }
            } else {
                if (btexturepath != null) {
                    if (Config.modelloaddebuglog) {
                        logger.debug("loading textures(?) or model from modelpath " + bpath.getFullName() + ",texturepath=" + (btexturepath.path));
                    }
                    boptions.setDatabasePath(new ResourcePath(btexturepath.path));
                }
            }
           

            if (bmodelpath != null) {
                String extension = bmodelpath.getExtension();
                BuildResult modelResult;
                if (extension.equals("xml")) {
                    modelResult = buildModelFromBundleXML(bmodelpath,/*TODO 25.4.17* /boptions);
                } else {
                    modelResult = ModelFactory.buildModelFromBundle(bmodelpath);
                }
                //modelResult = new ReadResult(osgDB.readNodeFile(bmodelpath, null, options/*.get()* /));
                if (modelResult == null || modelResult.getNode() == null) {
                    logger.error("Failed to build 3D model:" + ((modelResult == null) ? "" : modelResult.message()) /*10.4.17 +                        modelpath.str()* /);
                    return null;
                }
                model = modelResult.getNode();//copyModel(modelResult.getNode());
            } else {
                ReadResult modelResult = new ReadResult(osgDB.readNodeFile(null, modelpath.str(), options/*.get()* /));
                if (!modelResult.validNode()) {
                    logger.error("Failed to load 3D model:" + modelResult.message() /*10.4.17 +                        modelpath.str()* /);
                    return null;
                }
                model = modelResult.getNode();//copyModel(modelResult.getNode());
            }
            // Add an extra reference to the model stored in the database.
            // That is to avoid expiring the object from the cache even if
            // it is still in use. Note that the object cache will think
            // that a model is unused if the reference count is 1. If we
            // clone all structural nodes here we need that extra
            // reference to the original object
            /*TODO SGDatabaseReference * databaseReference;
            databaseReference = new SGDatabaseReference(modelResult.getNode());
            model -> addObserver(databaseReference);*/

    // Update liveries
            /*TODO TextureUpdateVisitor liveryUpdate (options -> getDatabasePathList());
            model -> accept(liveryUpdate);*/

    // Copy the userdata fields, still sharing the boundingvolumes,
    // but introducing new data for velocities.
            /*TODO UserDataCopyVisitor userDataCopyVisitor;
            model.accept(userDataCopyVisitor);*/

            /*TODO SetNodeMaskVisitor setNodeMaskVisitor (0, simgear::MODELLIGHT_BIT);
            model.accept(setNodeMaskVisitor);* /
        }
        if (bpath != null) {
            model.setName(bpath.getFullName());
        } else {
            model.setName(modelpath.str());
        }

        boolean needTransform = false;
        // Set up the alignment node if needed
        SGPropertyNode offsets = props.getNode("offsets", false);
        if (offsets != null) {
            if (Config.modelloaddebuglog) {
                logger.debug("found offsets");
            }
            needTransform = true;
            /*osg::MatrixTransform **/
    //Matrix4 alignmainmodel = new Matrix4();//osg::MatrixTransform;
    //SceneNode alignmainmodel = new SceneNode();
    // ? alignmainmodel -> setDataVariance(osg::Object::STATIC);
            /*osg::* /
            Matrix4 res_matrix;
            // TODO CHECK getFirst parameter y axis and getSecond x axis?
            //res_matrix/*.makeRotate* / = Matrix4.buildRotationMatrix(

            // 15.8.17: Die Offsets müssen direkt in die group (und auch ohne Zwischengroup), damit sie auch für die submodels gelten,
            // die später evtl. auch noch in diese group kommen.
            group = new Group();
            group.getTransform().setRotation(buildRotationFromOffsets(offsets));
            /*new Quaternion(new Degree(offsets.getFloatValue("roll-deg", 0.0f)),
                    new Degree(offsets.getFloatValue("pitch-deg", 0.0f))/* * SG_DEGREES_TO_RADIANS,
                    osg::Vec3 (0, 1, 0)* /,
                    //   new Degree(offsets.getFloatValue("roll-deg", 0.0f))/* * SG_DEGREES_TO_RADIANS,                    osg::Vec3 (1, 0, 0)* /,
                    new Degree(offsets.getFloatValue("heading-deg", 0.0f))/* * SG_DEGREES_TO_RADIANS,
                    osg::Vec3 (0, 0, 1)* /));*/

            /*osg::* /
            Matrix4 tmat;
            //tmat/*.makeTranslate* /=Matrix4.buildTranslationMatrix(
            group.getTransform().setPosition(buildVector3FromOffsets(offsets));
            //group.attach(alignmainmodel);
            //alignmainmodel = (res_matrix.multiply(/* * * /tmat));
            // group = alignmainmodel;
            group.attach(model);
        } else {
        /*if (group == null) {* /
            group = new /*osg::* /Group();
            group.attach/*addChild* /(model/*.get()* /);
        }


        // Load sub-models
        PropertyList /*Vector<SGPropertyNode_ptr>* / model_nodes = props.getChildren("model");

        if (Config.modelloaddebuglog) {
            logger.debug("loading sub models. cnt=" + model_nodes.size());
        }
        for (int i = 0; i < model_nodes.size(); i++) {
            SGPropertyNode/*_ptr* / sub_props = model_nodes.get(i);

            SGPath submodelpath;
            /*osg::ref_ptr < osg::* /
            Node submodel;

            String subPathStr = sub_props.getStringValue("path");
            if (Config.modelloaddebuglog) {
                logger.debug("loading sub model " + subPathStr + " at " + i);
            }
            SGPath submodelPath = null;
            BundleResource bsubmodelpath = null;
            if (bpath == null) {
                submodelPath = new SGPath(SGModelLib.findDataFile(subPathStr, null, modelDir));
                if (submodelPath.isNull()) {
                    logger.error("Failed (sub model path is null) to load file: \"" + subPathStr + "\"");
                    continue;
                }
            } else {
                bsubmodelpath = BundleRegistry.findPath(subPathStr, bpath);
                if (bsubmodelpath == null) {
                    logger.error("Failed (sub model path is null) to load file: \"" + subPathStr + "\"");
                    continue;
                }
            }


            if (sub_props.hasChild("usage")) { /* We don't want load this file and its content now * /
                boolean isInterior = sub_props.getStringValue("usage").equals("interior");
                boolean isAI = prop_root.getStringValue("type").equals("AI");
                if (isInterior && isAI) {
                    props.addChild(new SGPropertyNode("interior-path")).setStringValue(submodelPath.str());
                    continue;
                }
            }

            submodel = sgLoad3DModel_internal_sync(bsubmodelpath, submodelPath, options/*.get()* /,
                    sub_props.getNode("overlay"), boptions);


            if (submodel == null) {
                //error already logged
                continue;
            }

            /*osg::ref_ptr < osg::* /
            Node submodel_final = submodel;
            SGPropertyNode offs = sub_props.getNode("offsets", false);
            if (offs != null) {
                /* osg::Matrix res_matrix;
                osg::ref_ptr < osg::MatrixTransform > align = new osg::MatrixTransform;
                align -> setDataVariance(osg::Object::STATIC);
                res_matrix.makeIdentity();
                res_matrix.makeRotate(
                        offs -> getDoubleValue("pitch-deg", 0.0) * SG_DEGREES_TO_RADIANS,
                        osg::Vec3 (0, 1, 0),
                offs -> getDoubleValue("roll-deg", 0.0) * SG_DEGREES_TO_RADIANS,
                        osg::Vec3 (1, 0, 0),
                offs -> getDoubleValue("heading-deg", 0.0) * SG_DEGREES_TO_RADIANS,
                        osg::Vec3 (0, 0, 1));

                osg::Matrix tmat;
                tmat.makeIdentity();
                tmat.makeTranslate(offs -> getDoubleValue("x-m", 0),
                        offs -> getDoubleValue("y-m", 0),
                        offs -> getDoubleValue("z-m", 0));
                align -> setMatrix(res_matrix * tmat);
                align -> addChild(submodel.get());
                submodel_final = align;* /
                Group align = new Group();
                align.getTransform().setRotation(buildRotationFromOffsets(offs));
                align.getTransform().setPosition(buildVector3FromOffsets(offs));
                align.attach/*addChild* /(submodel);
                submodel_final = align;
            }
            submodel_final.setName(sub_props.getStringValue("name", ""));

            SGPropertyNode cond = sub_props.getNode("condition", false);
            if (cond != null) {
               /*TODO osg::ref_ptr < osg::Switch > sw = new osg::Switch;
                sw -> setUpdateCallback(new SGSwitchUpdateCallback(sgReadCondition(prop_root, cond)));
                group.addChild(sw.get());
                sw -> addChild(submodel_final.get());
                sw -> setName("submodel condition switch");* /
            } else {
                group.attach/*addChild* /(submodel_final/*.get()* /);
            }
        } // end of submodel loading

        /*TODO
        /*osg::* /Node * ( * load_panel)(SGPropertyNode *) = options -> getLoadPanel();
        //logger.debug("load_panel=%d\n", load_panel);
        if (load_panel) {
            // Load panels
            PropertyList /*vector<SGPropertyNode_ptr>* / panel_nodes = props.getChildren("panel");
            for (int i = 0; i < panel_nodes.size(); i++) {
                //SG_LOG(SG_INPUT, SG_DEBUG, "Loading a panel");
                osg::ref_ptr < osg::Node > panel = load_panel(panel_nodes[i]);
                if (panel_nodes.get(i).hasValue("name"))
                panel.setName(panel_nodes[i].getStringValue("name"));
                group.addChild(panel.get());
            }
        }* /

       /*TODO  if (dbOptions -> getPluginStringData("SimGear::PARTICLESYSTEM") != "OFF") {
            std::vector < SGPropertyNode_ptr > particle_nodes;
            particle_nodes = props -> getChildren("particlesystem");
            for (unsigned i = 0; i < particle_nodes.size(); ++i) {
                osg::ref_ptr < SGReaderWriterOptions > options2;
                options2 = new SGReaderWriterOptions( * options);
                if (i == 0) {
                    if (!texturepath.extension().empty())
                        texturepath = texturepath.dir();

                    options2 -> setDatabasePath(texturepath.str());
                }
                group -> addChild(Particles::appendParticles (particle_nodes[i],
                        prop_root,
                        options2.get()));
            }
        }* /

        PropertyList /*std::vector < SGPropertyNode_ptr >* / text_nodes;
        text_nodes = props.getChildren("text");
        //logger.debug("loading text nodes. cnt=%d\n", text_nodes.size());
        for (int i = 0; i < text_nodes.size(); ++i) {
           /*TODO group.addChild(SGText::appendText (text_nodes.get(i),
                    prop_root,
                    options.get()));* /
        }

        PropertyList effect_nodes = props.getChildren("effect");
        PropertyList animation_nodes = props.getChildren("animation");

        if (previewMode) {
            /*TODO  PropertyList::iterator it;
            it = std::remove_if (animation_nodes.begin(), animation_nodes.end(), ExcludeInPreview());
            animation_nodes.erase(it, animation_nodes.end());* /
            Util.notyet();
        }

        // Some material animations (eventually all) are actually effects.
        makeEffectAnimations(animation_nodes, effect_nodes);
        {
            /*ref_ptr<* /
            Node modelWithEffects
                    = Model.instantiateEffects(group/*.get()* /, effect_nodes, options/*.get()* /);
            group = /*static_cast < Group * > (* /(Group) modelWithEffects/*.get()* /;
        }

        if (Config.modelloaddebuglog) {
            logger.debug("building animations. nodecount=" + animation_nodes.size());
        }
        for (int i = 0; i < animation_nodes.size(); ++i) {
            if (previewMode && animation_nodes.get(i).hasChild("nopreview")) {
                /*TODOPropertyList names (animation_nodes.get(i).getChildren("object-name"));
                for (unsigned int n = 0;
                n<names.size (); ++n){
                    removeNamedNode(group, names[n]->getStringValue());
                } // of object-names in the animation
                continue;* /
            }

            /// OSGFIXME: duh, why not only model?????
            // 25.1.17: Der Frage schliesse ich mich an. group ist ca. zwei/drei ebenen über der AC Node und der AC transfrom node. Wenn
            // die animierte Node an dir group gehagen wird, geht die ACprocesspolicy verloren.
            // Dafuer koennte der traverse in SGAnimation sein. 
            //FG-DIFF: Explizit den parent fuer die AnimationGroup reingeben.
            if (path != null) {
                SGAnimation anim = SGAnimation.animate(group, animation_nodes.get(i), prop_root, options, path.str(), i);
                if (anim != null) {
                    animationList.add(anim);
                }
            }
            if (bpath != null) {
                SGAnimation anim = SGAnimation.animate(group, animation_nodes.get(i), prop_root, options, null, i);
                if (anim != null) {
                    animationList.add(anim);
                }
            }
        }

       /*TODO if (!needTransform && group.getNumChildren() < 2) {
            model = group.getChild(0);
            group -> removeChild(model.get());
            if (data.valid())
                data -> modelLoaded(modelpath.str(), props, model.get());
            return model.release();
        }
        if (data.valid())
            data -> modelLoaded(modelpath.str(), props, group.get());
        if (props.hasChild("debug-outfile")) {
            std::string outputfile = props.getStringValue("debug-outfile",
                    "debug-model.osg");
            osgDB::writeNodeFile ( * group, outputfile);
        }* /

        if (Config.modelloaddebuglog) {
            logger.debug("sgLoad3DModel_internal completed");
        }
        return group/*.release()* /;
    }*/

    /**
     * OSG has different coordinate system
     *
     * @param offs
     * @return
     */
    public static Quaternion buildRotationFromOffsets(SGPropertyNode offs) {
        if (FlightGear.useosgcoordinatesystem) {
            //22.11.16: TODO: Ob das stimmt, vor allem die Reihenfolge, ist unklar. Im 777 Cockpit entstehen hinten rechts noch merkwürdige Artefakte.
            //im grossen und ganzen passt es aber.
            //12.7.17: LSG Truck Rotation bestätigt die Richtigkeit.
            Quaternion q1 = Quaternion.buildQuaternionFromAngleAxis(new Degree(offs.getFloatValue("pitch-deg", 0.0f)), new Vector3(0, 1, 0));
            Quaternion q2 = Quaternion.buildQuaternionFromAngleAxis(new Degree(offs.getFloatValue("roll-deg", 0.0f)), new Vector3(1, 0, 0));
            Quaternion q3 = Quaternion.buildQuaternionFromAngleAxis(new Degree(offs.getFloatValue("heading-deg", 0.0f)), new Vector3(0, 0, 1));
            Quaternion q = MathUtil2.multiply(q3, MathUtil2.multiply(q2, q1));
            return (q);
        }
        return Quaternion.buildFromAngles(
                new Degree(offs.getFloatValue("roll-deg", 0.0f)),
                new Degree(offs.getFloatValue("heading-deg", 0.0f)),
                //   new Degree(offsets.getFloatValue("roll-deg", 0.0f)),
                //shpuld be negated
                new Degree(offs.getFloatValue("pitch-deg", 0.0f)));
    }

    /**
     * OSG has different coordinate system
     * 17.11.16: Aber die Werte in den FG XML Dateien sind schon im OSG Format. Siehe Wiki.
     *
     * @param offs
     * @return
     */
    public static Vector3 buildVector3FromOffsets(SGPropertyNode offs) {
        if (FlightGear.useosgcoordinatesystem) {

            Vector3 v = new Vector3(offs.getFloatValue("x-m", 0.0f), offs.getFloatValue("y-m", 0.0f), offs.getFloatValue("z-m", 0.0f));
            return v;
        }
        return new Vector3(offs.getFloatValue("x-m", 0.0f),
                offs.getFloatValue("z-m", 0.0f),
                // 28.7.16:wenn y nicht negiert wird, sind manche Panel falsch lokalisiert (z.B. das linke Efis). Irgendwie sonderbar, dass es nicht einfach gespiegelt ist.
                -offs.getFloatValue("y-m", 0.0f));

    }

    /**
     * 10.1.19: Erweiterung zu FG.
     *
     * @param offs
     * @return
     */
    public static Vector3 buildScaleFromOffsets(SGPropertyNode offs) {

        Vector3 v = new Vector3(offs.getFloatValue("x-scale", 1), offs.getFloatValue("y-scale", 1), offs.getFloatValue("z-scale", 1));
        return v;

    }

    public static void clearStatistics() {
        errorCnt = 0;
        loadedList = new ArrayList<String>();
        failedList = new ArrayList<String>();
    }
}

