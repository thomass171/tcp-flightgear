package de.yard.threed.flightgear.core.simgear.scene.model;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.flightgear.core.osgdb.Options;
import de.yard.threed.flightgear.core.osgdb.Registry;
import de.yard.threed.flightgear.core.osgdb.osgDB;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.misc.SgResourceManager;
import de.yard.threed.flightgear.core.simgear.misc.SGPath;
import de.yard.threed.flightgear.core.simgear.scene.util.SGReaderWriterOptions;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.StringUtils;

/**
 * Class for loading and managing models with XML wrappers.
 */
public class SGModelLib {
    static Log logger = Platform.getInstance().getLog(SGModelLib.class);
    static SGPropertyNode static_propRoot;


    // panel_func static_panelFunc;

    /*30.9.19public static String findDataFile(final String file) {
        return findDataFile(file, null, new SGPath());
    }*/
    /*28.6.17 nicht wegen Komponentiserungpublic static BundleResource findDataFileB(final String file) {
        return findDataFile(file, null, new SGPath());
    }*/

    /*30.9.19public static String findDataFile(final String file, final Options opts) {
        return findDataFile(file, opts, new SGPath());
    }*/
    /*28.6.17 nicht wegen Komponentiserungpublic static BundleResource findDataFileB(final String file, final Options opts) {
        return findDataFile(file, opts, new SGPath());
    }*/

    public static String findDataFile(final String file, final Options opts, SGPath currentDir) {
        if (StringUtils.empty(file))
            return file;
        SGPath p = SgResourceManager.getInstance().findPath(file, currentDir);
        if (p.exists()) {
            return p.str();
        }

        // finally hand on to standard OSG behaviour
        return osgDB.findDataFile(file, opts);
    }
    /*28.6.17 nicht wegen Komponentiserungpublic static BundleResource findDataFileB(final String file, final Options opts, SGPath currentDir) {
        if (StringUtils.empty(file))
            return file;
        SGPath p = SgResourceManager.getInstance().findPath(file, currentDir);
        if (p.exists()) {
            return p.str();
        }

        // finally hand on to standard OSG behaviour
        return osgDB.findDataFile(file, opts);
    }*/

    public static void init(Bundle/*String*/ root_dir, SGPropertyNode root) {
        //osgDB::Registry::instance()->getDataFilePathList().push_front(root_dir);
        static_propRoot = root;
    }

    static SceneNode loadFile(String path, SGReaderWriterOptions options) {
        logger.debug("loadFile: path=" + path);

        if (StringUtils.endsWith(path, ".ac")) {
            //TODO effects
            //options.setInstantiateEffects(true); 
        }

        // TSCH: readRefNodeFile ist aus OSG. Das scheint laut Logs aber iregndiwe per Callbacks vielleicht wieder simgear codee aufrzurufen.
        SceneNode model = osgDB.readRefNodeFile(null,path, options);
        if (model == null)
            return null;
        else
            return model/*.release()*/;
    }


   /* static void resetPropertyRoot();

    static void setPanelFunc(panel_func pf);*/

    // Load a 3D model (any format)
    // data->modelLoaded() will be called after the model isType loaded
    public static SceneNode loadModel(String path, SGPropertyNode prop_root) {
        return loadModel(path, prop_root, null, false);
    }

    public static SceneNode loadModel(String path, SGPropertyNode prop_root, SGModelData data, boolean load2DPanels) {

        logger.debug("loadModel: path=" + path);

        SGReaderWriterOptions opt = null;
            opt = SGReaderWriterOptions.copyOrCreate(Registry.getInstance().getOptions());
            opt.setPropertyNode((prop_root != null )? prop_root: static_propRoot/*.get()*/);
            opt.setModelData(data);

            /*if (load2DPanels) {
                opt.setLoadPanel(static_panelFunc);
            }*/

        SceneNode n = loadFile(path, opt/*.get()*/);
        if (n != null && StringUtils.empty(n.getName()))
            n.setName("Direct loaded model \"" + path + "\"");

        logger.debug("loadModel completed: name=" + n.getName());

        return n;

    }


    // Load a 3D model (any format) through the DatabasePager.
    // This function initially just returns a proxy node that refers to
    // the model file. Once the viewer steps onto that node the
    // model will be loaded.
    /*static osg::Node* loadDeferredModel(const std::string &path,
                                        SGPropertyNode *prop_root = NULL,
                                        SGModelData *data=0){}*/
    // Load a 3D model (any format) through the DatabasePager.
    // This function initially just returns a PagedLOD node that refers to
    // the model file. Once the viewer steps onto that node the
    // model will be loaded. When the viewer does no longer reference this
    // node for a long time the node isType unloaded again.
    /*static osg::PagedLOD* loadPagedModel(const std::string &path,
                                         SGPropertyNode *prop_root = NULL,
                                         SGModelData *data=0) {
    }*/


}
