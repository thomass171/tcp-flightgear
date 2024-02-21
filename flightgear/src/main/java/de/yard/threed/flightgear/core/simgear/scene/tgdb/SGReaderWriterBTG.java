package de.yard.threed.flightgear.core.simgear.scene.tgdb;

import de.yard.threed.core.GeneralParameterHandler;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.flightgear.LoaderOptions;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.flightgear.core.osg.Node;
import de.yard.threed.flightgear.core.osgdb.Options;

import de.yard.threed.flightgear.core.simgear.scene.util.SGReaderWriterOptions;
import de.yard.threed.core.platform.Log;

/**
 * Created by thomass on 09.08.16.
 */
public class SGReaderWriterBTG /*15.9.17 extends ReaderWriter*/ {
    static Log logger = Platform.getInstance().getLog(SGReaderWriterBTG.class);
    public static boolean loaderbtgdebuglog = false;

   /*15.9.17 public SGReaderWriterBTG() {
        supportsExtension("btg", "");
    }

    @Override
    public boolean acceptsExtension(String extension) {
        // trick the osg extensions match algorithm to accept btg.gz files.
        if (StringUtils.toLowerCase(extension).equals("gz"))
            return true;
        return super.acceptsExtension(extension);
    }

    //virtual const char* className() const;

    @Override
    public void/*ReadResult* / readNode( BundleResource bpath, String/ * /NativeResource* / fileName, Options options,ReaderWriterListener readlistener) {
         loadBTG(bpath,options,null,readlistener);
    }*/

    /**
     * Runs sync. Bundle must have been loaded already.
     * 22.3.18: Simply returns SceneNode now instead of ReadResult. Wraps possible exceptions.
     * Errors are logged already.
     * 15.2.24: Now async
     */
    public static void /*SceneNode/*ReadResult*/ loadBTG(BundleResource bpath, Options options, LoaderOptions boptions, GeneralParameterHandler<SceneNode> delegate) {

        SGReaderWriterOptions sgOptions;
        sgOptions = (SGReaderWriterOptions) options;
        //Node result = null;

        Obj obj = new Obj();
        /*SceneNode result = */obj.SGLoadBTG(bpath,/*fileName*/ sgOptions, boptions, new GeneralParameterHandler<Node>() {
            @Override
            public void handle(Node node) {
                delegate.handle(node);
            }
        });
        //return result;
        //if (result == null)
        //    return ReadResult.FILE_NOT_HANDLED;
    }
}
