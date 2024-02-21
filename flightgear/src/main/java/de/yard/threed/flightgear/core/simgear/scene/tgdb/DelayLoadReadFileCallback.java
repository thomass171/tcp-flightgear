package de.yard.threed.flightgear.core.simgear.scene.tgdb;

import de.yard.threed.core.BuildResult;
import de.yard.threed.core.Degree;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.platform.ResourceLoaderFromBundle;
import de.yard.threed.flightgear.FgModelHelper;
import de.yard.threed.flightgear.LoaderOptions;
import de.yard.threed.engine.ModelFactory;
import de.yard.threed.core.Quaternion;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.core.Vector3;
import de.yard.threed.flightgear.core.osg.Group;
import de.yard.threed.flightgear.core.osgdb.Options;
import de.yard.threed.flightgear.core.osgdb.ReadResult;
import de.yard.threed.flightgear.core.osgdb.osgDB;
import de.yard.threed.flightgear.core.simgear.bucket.SGBucket;
import de.yard.threed.flightgear.core.simgear.misc.SGPath;
import de.yard.threed.flightgear.core.simgear.scene.model.SGReaderWriterXML;
import de.yard.threed.flightgear.core.simgear.scene.util.SGReaderWriterOptions;
import de.yard.threed.flightgear.core.simgear.geodesy.FgMath;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.core.platform.Log;

import de.yard.threed.engine.platform.EngineHelper;

import java.util.List;

/**
 * ReaderWriterSTG.[ch]xx
 * 
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

    public ReadResult readNode(String dummys, Options dummyopt) {
        /*osg::ref_ptr < osg::*/
        Group group = new Group();
        group.setName("STG-group-A");
        //TODO group.setDataVariance(osg::Object::STATIC);

        //for (std::list < _ObjectStatic >::iterator i = _objectStaticList.begin();        i != _objectStaticList.end();        ++i){
        for (_ObjectStatic i : _objectStaticList) {
            SceneNode node = null;
            // Unklar, welchen Zweck der Proxy hat. Erstmal nicht unterscheiden
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
                BuildResult result;
                if (extension.equals("xml")) {
                    // 4.1.18: GLTF Nutzung
                    LoaderOptions lo = new LoaderOptions();
                    lo.usegltf=true;
                    result = SGReaderWriterXML.buildModelFromBundleXML(i.resource, lo, null);
                    if (result == null||result.getNode()==null) {
                        logger.error( i._errorLocation + ": Failed to load " + i._token + " '" + i._name + "'");
                        continue;
                    }
                    node = new SceneNode(result.getNode());
                }else{
                    // Das Model async ueber die Platform laden. Die gelieferte node wird nie null sein.
                    // 4.1.18: using GLTF, 18.10.23: ac->gltf name mapping and policy setting now here.
                    node = FgModelHelper.mappedasyncModelLoad(new ResourceLoaderFromBundle(i.resource));
                }
                
                
            } else {
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
            Quaternion q = Quaternion.buildQuaternionFromAngleAxis((float)new Degree( i._hdg).toRad(), new Vector3(0, 0, 1));
            rotation = rotation.multiply( (q));
            q = Quaternion.buildQuaternionFromAngleAxis((float)new Degree( i._pitch).toRad(), new Vector3(0, 1, 0));
            rotation = rotation.multiply( (q));
            q = Quaternion.buildQuaternionFromAngleAxis((float)new Degree( i._roll).toRad(), new Vector3(1, 0, 0));
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
}
