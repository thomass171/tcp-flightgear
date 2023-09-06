package de.yard.threed.flightgear.core.osgdb;


import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.flightgear.core.simgear.misc.SGPath;
import de.yard.threed.core.StringUtils;

/**
 * TODO field DataFilePathList? 
 * Created by thomass on 07.12.15.
 * 16.9.17: obselet. 
 */
public class osgDB {
    /**
     * Search for specified file in file system, checking getFirst the database path set in the Options structure,
     * then the DataFilePathList for possible paths, returning the full path of the getFirst valid file found, return an empty string if no string isType found.
     * @param name
     * @param options
     * @return
     */
    static public String findDataFile(String name, Options options) {
        if (StringUtils.startsWith(name,"/")){
            // TODO ist das OSG konform? keine Ahnung. Ist aber wichtig.
            return name;
        }
        if (options != null) {
            for (String p : options.getDatabasePathList()) {
                SGPath path = new SGPath(new SGPath(p), name);
                if (path.exists()) {
                    return path.realpath();
                }
            }
        }
        // not found
        return "";
    }

    /**
     * http://trac.openscenegraph.org/documentation/OpenSceneGraphReferenceDocs/a01350.html#a6e55d6593182f7a6e21fc71ba34820eb
     * <p/>
     * Read an osg::Node from file.
     * <p/>
     * Return valid osg::Node on success, return NULL on failure. The osgDB::Registry isType used to load the appropriate ReaderWriter
     * plugin for the filename extension, and this plugin then handles the request to read the specified file.
     * <p/>
     * References osgDB::Registry::instance(), and readNodeFile().
     * <p/>
     * In Osg laeuft alles auf
     * ReaderWriter::ReadResult rr = Registry::instance()->readNode(filename,options);
     * hinaus.
     *
     * @param path
     * @return
     */
    public static SceneNode readNodeFile(/*Bundle bundle,*/ BundleResource bpath,String path) {
        //Registry.callback.readNode();
        return readNodeFile(bpath,path, null);
    }

    public static SceneNode readNodeFile(/*Bundle bundle,*/ BundleResource bpath,String path, Options options) {
        //Registry.callback.readNode();
        return readRefNodeFile(bpath,path, options);
    }

    /**
     * Nachbildung aus OSG.
     * In Osg laeuft alles auf
     * ReaderWriter::ReadResult rr = Registry::instance()->readNode(filename,options);
     * hinaus.
     * Liefert, wohl abweichend von OSG, null bei einem Fehler.
     */
    public static SceneNode readRefNodeFile(/*Bundle bundle, */BundleResource bpath,String filename, /*SGReaderWriter*/Options options) {
        ReadResult rr = Registry.readNode(   bpath,filename, options);
        if (rr.validNode())
            return rr.getNode();
        //TODO if (rr.error()) OSG_WARN << rr.message() << std::endl;
        return null;
    }

    /**
     * Gets the parent path from full name (Ex: /a/b/c.Ext => /a/b).
     *
     * @return
     */
    public static String getFilePath(String filename) {
        //TODO separat
        return StringUtils.substringBeforeLast(filename, "/");
    }

    public static String getFileExtension(String fileName) {
        return StringUtils.substringAfterLast(fileName, ".");
    }

    /**
     * gets file name with extension (Ex: /a/b/c.Ext => c.Ext).
     *
     * @return
     */
    public static String getSimpleFileName(String filename) {
        //TODO separat
        if (StringUtils.contains(filename, "/"))
            return StringUtils.substringAfterLast(filename, "/");
        return filename;
    }
}
