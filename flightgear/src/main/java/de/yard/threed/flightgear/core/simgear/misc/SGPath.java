package de.yard.threed.flightgear.core.simgear.misc;

//4.7.21 import de.yard.threed.core.bundle.FileSystemResource;
import de.yard.threed.core.Util;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.StringUtils;


/**
 * Der Pfad kann relativ/absolut sein und auf eine Datei oder ein Verzeichnis.
 * 28.12.16: Als FileSystemResource abbilden.
 *
 * 7.7.21 BundleResource statt FileSystemResource
 *
 * <p>
 * Created by thomass on 07.12.15.
 */
public class SGPath {
    // nf ist null fuer einen nicht existierenden Pfad (etwas merkwurdig, aber analog zu FG Logik).
    //27.7.21: Das Ersetzen der FileSystemResource geht so nicht! Nochmal versuchen.
    //private FileSystemResource nf = null;
    private BundleResource nf = null;
    //27.5.16 private String path;

    public SGPath() {
        //4.7.21 nf = null;
        //path = null;
    }

    public SGPath(String path) {
        if (path != null) {
            this.nf = BundleResource.buildFromFullString/*new FileSystemResource*/(path);
        }
        //this.path = path;
    }

    public SGPath(BundleResource/*FileSystemResource*/ nf) {
        this.nf = nf;
    }

    public SGPath(SGPath path) {
        this(path.nf.getName());
    }

    public SGPath(SGPath path, String subpath) {
        this(path.nf.getName() + "/" + subpath);
    }

    public boolean exists() {
        //27.5.16: Das muss ueber die Platform gehen.
        // return nf != null && nf.exists();
        // 7.7.21 TODO
        //return nf != null && ((Platform)Platform.getInstance()).exists(nf);
        Util.nomore();
        return false;
    }

    /**
     * Liefert den Namen ohne Pfad und Suffix.
     *
     * @return
     */
    public String file_base() {
        String fb = nf.getName();
        int index;
        if ((index = StringUtils.lastIndexOf(fb, "/")) != -1) {
            fb = StringUtils.substring(fb, index + 1);
        }
        if ((index = StringUtils.lastIndexOf(fb, ".")) != -1) {
            fb = StringUtils.substring(fb, 0, index);
        }
        return fb;
    }

    /**
     * // get the directory part of the path.
     *
     * @return
     */
    public SGPath dir() {
        String fb = nf.getName();
        int index;
        //TODO file separator
        if ((index = StringUtils.lastIndexOf(fb, "/")) != -1) {
            return new SGPath(StringUtils.substring(fb, 0, index));
        }
        return new SGPath("");
    }

    public String extension() {
        int index = StringUtils.lastIndexOf(nf.getName(), ".");
        if (index == -1) {
            return "";
        }
        // Der "." koennte auch irgendwo im Pfad sein.
        int slindex;
        if ((slindex = StringUtils.lastIndexOf(nf.getName(), "/")) != -1) {
            if (slindex > index) {
                //Punkt ist im Pfad
                return "";
            }
        }
        return StringUtils.substring(nf.getName(), index + 1);
    }

    public String lower_extension() {
        return StringUtils.toLowerCase(extension());
    }

    public String str() {
        return nf.getFullName();
    }

    public boolean isNull() {
        return nf == null;//Util.isEmptyString(path);
    }

    @Override
    public String toString() {
        return nf.getName();
    }

    public void append(String p) {
        //nf = new FileSystemResource(nf.getName() + "/" + p);
        //27.7.21:wichtig ist full name, sonst geht der path verloren
        nf = BundleResource.buildFromFullString(nf.getFullName() + "/" + p);
    }

    /**
     * Returns a string with the absolute pathname that names the same file, whose
     * resolution does not involve '.', '..', or symbolic links.
     * <p>
     * TODO wegen platform und ueberhaupt konzeptionell unsauber
     */
    public String realpath() {
        return str();
    }

}
