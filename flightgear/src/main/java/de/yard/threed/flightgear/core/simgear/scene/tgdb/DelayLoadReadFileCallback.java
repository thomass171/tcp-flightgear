package de.yard.threed.flightgear.core.simgear.scene.tgdb;

import de.yard.threed.core.BuildResult;
import de.yard.threed.core.CharsetException;
import de.yard.threed.core.Degree;
import de.yard.threed.core.buffer.SimpleByteBuffer;
import de.yard.threed.core.platform.AsyncHttpResponse;
import de.yard.threed.core.platform.AsyncJobDelegate;
import de.yard.threed.core.platform.NativeBundleResourceLoader;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.resource.URL;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.platform.ResourceLoaderFromBundle;
import de.yard.threed.engine.platform.ResourceLoaderViaHttp;
import de.yard.threed.flightgear.FgModelHelper;
import de.yard.threed.flightgear.LoaderOptions;
import de.yard.threed.core.Quaternion;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.core.Vector3;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.flightgear.main.FGGlobals;
import de.yard.threed.flightgear.core.osg.Group;
import de.yard.threed.flightgear.core.osgdb.Options;
import de.yard.threed.flightgear.core.osgdb.ReadResult;
import de.yard.threed.flightgear.core.osgdb.osgDB;
import de.yard.threed.flightgear.core.simgear.bucket.SGBucket;
import de.yard.threed.flightgear.core.simgear.misc.SGPath;
import de.yard.threed.flightgear.core.simgear.scene.model.SGAnimation;
import de.yard.threed.flightgear.core.simgear.scene.model.SGReaderWriterXML;
import de.yard.threed.flightgear.core.simgear.scene.model.XmlModelCompleteDelegate;
import de.yard.threed.flightgear.core.simgear.scene.util.SGReaderWriterOptions;
import de.yard.threed.flightgear.core.simgear.geodesy.FgMath;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.core.platform.Log;

import de.yard.threed.flightgear.ecs.FgAnimationComponent;

import java.util.List;

/**
 * ReaderWriterSTG.[ch]xx
 * <p>
 * Liest ein Model file aus dem stg.
 * <p>
 * Created by thomass on 19.08.16.
 */
public class DelayLoadReadFileCallback /*extends OptionsReadFileCallback*/ {
    static Log logger = Platform.getInstance().getLog(DelayLoadReadFileCallback.class);
    List<_ObjectStatic> _objectStaticList;
    List<_Sign> _signList;

    /// The original options to use for this bunch of models
    /*osg::    ref_ptr<*/ SGReaderWriterOptions _options;
    SGBucket _bucket;

    /**
     * Finally some parts might be async here, so the returned node/group might be populated later.
     */
    public ReadResult readNode(String dummys, Options dummyopt) {
        /*osg::ref_ptr < osg::*/
        Group group = new Group();
        group.setName("STG-group-A");
        //TODO group.setDataVariance(osg::Object::STATIC);

        //for (std::list < _ObjectStatic >::iterator i = _objectStaticList.begin();        i != _objectStaticList.end();        ++i){
        for (_ObjectStatic i : _objectStaticList) {
            SceneNode node = null;
            // Unklar, welchen Zweck der Proxy hat. Erstmal nicht unterscheiden
            // 23.8.24: proxy is true for static objects and false for shared.
            /*if (i._proxy) {
                osg::ref_ptr < osg::ProxyNode > proxy = new osg::ProxyNode;
                proxy.setName("proxyNode");
                proxy.setLoadingExternalReferenceMode(osg::ProxyNode::DEFER_LOADING_TO_DATABASE_PAGER);
                proxy.setFileName(0, i._name);
                proxy.setDatabaseOptions(i._options.get());
                node = proxy;
            } else {*/
            if (i.resource != null) {
                String extension = i.resource.getExtension();

                // 25.8.24: Use dummy bundle for shared model(which are from "TerraSync-model"). "TerraSync-model" is just to large/inefficient
                // for loading as bundle. Load XML via HTTP from arbitrary location. And hope there are no nested XMLs.
                if (i.shared) {
                    //Platform.getInstance().buildResourceLoader();
                    //ResourceLoaderViaHttp resourceLoader = new ResourceLoaderViaHttp(new URL("http://localhost:" + wireMockServer.port() ,
                    //      new ResourcePath("/somepath"),"model.bin"));
                    if (extension.equals("xml")) {

                        Bundle bundle = i.resource.bundle;//BundleRegistry.TERRAYSYNCPREFIX + "model";//FlightGear.getBucketBundleName("model");
                        // maybe XML was already loaded
                        if (bundle.contains(i.resource)) {
                            node = loadXmlModel(i.resource);
                        } else {
                            //List<Bundle> loadedBundle = new ArrayList();
                            //String baseUrl = "https://ubuntu-server.udehlavj1efjeuqv.myfritz.net/publicweb/bundlepool";
                            //String fullName = i.resource.getFullName();//"";
                            // prepare a destination node
                            node = new SceneNode();
                            final SceneNode destinationNode = node;
                            NativeBundleResourceLoader resourceLoader = Platform.getInstance().buildResourceLoader(bundle.name, null);
                            resourceLoader.loadFile(i.resource.getFullName(), new AsyncJobDelegate<AsyncHttpResponse>() {
                                @Override
                                public void completed(AsyncHttpResponse response) {
                                    if (response.getStatus() == 200) {
                                        try {
                                            String xmlContent = response.getContentAsString();
                                            // Cannot use dummy name because we need/use/call bundle resolver later
                                            //Bundle dummyBundle =null;// new Bundle(BundleRegistry.TERRAYSYNCPREFIX + "model", new String[]{fullName},"");
                                            bundle.addResource(i.resource.getFullName(), new BundleData(response.getContent(), true));
                                            //BundleResource xmlResource = new BundleResource(dummyBundle,fullName);
                                            SceneNode newNode = loadXmlModel(i.resource);

                                            destinationNode.attach(newNode);
                                        } catch (CharsetException e) {
                                            logger.error("CharsetException");
                                        }

                                    }
                                }
                            });
                        }
                    } else {
                        // shared non XML
                        logger.warn("shared simple model not yet");
                        node = new SceneNode();
                    }
                } else {
                    if (extension.equals("xml")) {
                        if ((node = loadXmlModel(i.resource)) == null) {
                            // Might happen even though node is just a destination node.
                            continue;
                        }
                    } else {
                        // Das Model async ueber die Platform laden. Die gelieferte node wird nie null sein.
                        // 4.1.18: using GLTF, 18.10.23: ac->gltf name mapping and policy setting now here.
                        node = FgModelHelper.mappedasyncModelLoad(new ResourceLoaderFromBundle(i.resource));
                    }
                }

            } else {
                // 25.8.24: Is this a valid branch? Was it ever?
                node = osgDB.readRefNodeFile(null, i._name, i._options/*.get()*/);
                if (node == null) {//!node.valid()) {
                    logger.error(/*SG_LOG(SG_TERRAIN, SG_ALERT,*/ i._errorLocation + ": Failed to load " + i._token + " '" + i._name + "'");
                    continue;
                }
            }
            //}
            if (new SGPath(i._name).lower_extension().equals("ac")) {
                //TODO node.setNodeMask(~simgear::MODELLIGHT_BIT);
            }

            //osg::Matrix matrix;
            SGGeod location = SGGeod.fromDegM(i._lon, i._lat, i._elev);
            Quaternion rotation = /*matrix*/ FgMath.makeZUpFrame(location);
            //matrix.preMultRotate(osg::Quat (SGMiscd::deg2rad (i._hdg), osg::Vec3 (0, 0, 1)));
            //matrix.preMultRotate(osg::Quat (SGMiscd::deg2rad (i._pitch), osg::Vec3 (0, 1, 0)));
            //matrix.preMultRotate(osg::Quat (SGMiscd::deg2rad (i._roll), osg::Vec3 (1, 0, 0)));
            Quaternion q = Quaternion.buildQuaternionFromAngleAxis((float) new Degree(i._hdg).toRad(), new Vector3(0, 0, 1));
            rotation = rotation.multiply((q));
            q = Quaternion.buildQuaternionFromAngleAxis((float) new Degree(i._pitch).toRad(), new Vector3(0, 1, 0));
            rotation = rotation.multiply((q));
            q = Quaternion.buildQuaternionFromAngleAxis((float) new Degree(i._roll).toRad(), new Vector3(1, 0, 0));
            rotation = rotation.multiply((q));

            //rotation = rotation.multiply(new Quaternion(new Degree((float)i._hdg),new Degree((float)i._pitch),new Degree((float)i._roll)));
            /*osg::MatrixTransform **/
            SceneNode matrixTransform;
            matrixTransform = new SceneNode();//osg::MatrixTransform (matrix);
            Vector3 position = location.toCart();
            matrixTransform.getTransform().setPosition(position);
            matrixTransform.getTransform().setRotation(rotation);
            matrixTransform.setName("positionStaticObject");
            //TODO matrixTransform.setDataVariance(osg::Object::STATIC);
            matrixTransform.attach/*Child*/(node/*.get()*/);
            group.attach/*Child*/(matrixTransform);

            //logger.debug("DelayLoadReadFileCallback.readNode position/rotation for " + i._name + ": lon=" + i._lon + ",lat=" + i._lat + ",elev=" + i._elev + ",hdg=" + i._hdg +
            //        ",pitch=" + i._pitch + ",roll=" + i._roll + ",\n matrix=" + Matrix4.buildTransformationMatrix(position, rotation).dump("\n") + "\n");


        }

        /*
        simgear::AirportSignBuilder signBuilder(_options.getMaterialLib(), _bucket.get_center());
        for (std::list < _Sign >::iterator i = _signList.begin();
        i != _signList.end();
        ++i)
        signBuilder.addSign(SGGeod::fromDegM (i._lon, i._lat, i._elev),i._hdg, i._name, i._size);
        if (signBuilder.getSignsGroup())
            group.addChild(signBuilder.getSignsGroup());
*/

        return new ReadResult(group)/*.release()*/;
    }

    /**
     * Load an XML model.
     * Also creates an entity for animated models.
     * Used also for scenery objects, but not used for vehicle entities.
     */
    private SceneNode loadXmlModel(BundleResource resource) {
        BuildResult result;

        // 4.1.18: use GLTF which is default . 11.3.24: Also set root property for building animations.
        LoaderOptions lo = new LoaderOptions();
        lo.propertyNode = FGGlobals.getInstance().get_props();
        result = SGReaderWriterXML.buildModelFromBundleXML(resource, lo, new XmlModelCompleteDelegate() {
            @Override
            public void modelComplete(BundleResource source, SceneNode destinationNode, List<SGAnimation> animationList) {
                // Build entity for animated objects
                if (animationList.size() > 0) {
                    // 21.11.24: Also add scene node to entity
                    // 14.5.25: Is this a scenery model? We could pass the global property tree, but the tree parameter is intended a vehicle local one for syncing, so null
                    EcsEntity entity = new EcsEntity(destinationNode, new FgAnimationComponent(destinationNode, animationList, null));
                    logger.debug("Building entity with animations " + source.getFullName());
                    entity.setName(source.getFullName());
                }
            }
        });
        if (result == null || result.getNode() == null) {
            // Might happen even though node is just a destination node.
            //logger.error(i._errorLocation + ": Failed to load " + i._token + " '" + i._name + "'");
            logger.error(" Failed to load. No node. ");
            // return a node instead of null to make error handling easier.
            return new SceneNode();//null;
        }
        return new SceneNode(result.getNode());
    }
}
