package de.yard.threed.flightgear.core.simgear.misc;

//import de.yard.threed.flightgear.core.FileSystemResource;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.NativeResource;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 7.6.16: Doch Singleton. Nme SgResourceManager wegen duplicate Klasse.
 *
 * 7.7.21 BundleResource statt FileSystemResource
 *
 * <p/>
 * <p/>
 * Created by thomass on 11.12.15.
 */
public class SgResourceManager {
    private List<ResourceProvider> providerlist = new ArrayList<ResourceProvider>();
    static SgResourceManager instance = null;

    private SgResourceManager() {

    }

    public static SgResourceManager getInstance() {
        if (instance == null) {
            instance = new SgResourceManager();
        }
        return instance;
    }

    /**
     * Gibts so nicht in FG.
     * Sollten hier die Provider durchsucht werden? Das scheint mir widersinnig, wenn ich doch schon einen Resourcepath uebergebe. Darum nicht.
     * Tja, gibt es aber. In manchen Aircrafts findet sich:
     * <signals include="/Aircraft/Generic/flightrecorder/components/position.xml" />
     * was auf FG_ROOT verzweigt. Und genau das Gleiche gibt es auch ohne absoluten Pfad. Junge/Junge.
     * <p>
     * Liefert null bei nicht exist.
     * <p>
     * 27.12.16
     *
     * @param aResource
     * @param aContext
     * @return
     */
    public NativeResource findPath(String aResource, /*SGPath*/ResourcePath aContext) {
        // Nicht einfach die FileSystemResource so bauen, weil die resource ja auch einen Pafdanteil enthalten kann. Der muss als Pfadbestandteil erhalten bleiben.
        //FileSystemResource fr = new FileSystemResource(aContext, aResource);
        //Wenn die aResoucre absoluten Pfad hat, ignoriere ich den Context und, tja. Durch die Provider oder direkt FG ROot. Sagen wir mal Provider.
        // Kann aber auch echter absoluter Pfad sein??
        //Der absolute Pfad scheint keinerlei Bedeutung zu haben. Siehe position.xml. Mal mit und mal ohne. Also muss gesucht werden. 
        // Der "smarte" AircraftResourceProvider kann sowas auflösen.

        /*FileSystem*/BundleResource fr = /*FileSystem*/BundleResource.buildFromFullString(aContext.getPath() + "/" + aResource);
        if (fr.exists()) {
            return fr;
        }
        for (ResourceProvider rp : providerlist) {
            if (StringUtils.indexOf(aResource,"position.xml")!=-1){
                fr = null;
            }
            SGPath path = rp.resolve(aResource, new SGPath(aContext.getPath()));
            if (!path.isNull()) {
                return /*FileSystem*/BundleResource.buildFromFullString(path.str());
            }
        }
            /*Wo soll der denn hinzeigen? Absolut? SGPath r = new SGPath(aResource);
            if (r.exists()) {
                return r;
            }*/
        return null;
    }

    /**
     * Liefert leeres SGPath, wenn die Resource in dem Context nicht gefunden wird.
     *
     * @param aResource
     * @param aContext
     * @return
     */
    public SGPath findPath(String aResource, SGPath aContext) {
        if (aContext != null && !aContext.isNull()) {
            SGPath r = new SGPath(aContext, aResource);
            if (r.exists()) {
                return r;
            }
        }

        // FG hat im CurrentAircraftDirProvider einen Nebeneffekt, der absolute Pfade beachtet. Ob das Absicht ist?
        // Es ist nicht erkennbar, wo FG absolute Pfade abdeckt. Darum hier extra eingebaut. Das gibts in FG nicht.
        //27.12.16: Das ist doch eh falsch! Es muss doch auf Index 0 geprueft werden!
        // TODO fileseparator
        if (StringUtils.indexOf(aResource, "/") != -1) {
            SGPath r = new SGPath(aResource);
            if (r.exists()) {
                return r;
            }
        }

        //  Iterator<ResourceProvider> it = providerlist.iterator();
        //while (it.hasNext()) {
        for (ResourceProvider rp : providerlist) {
            SGPath path = rp.resolve(aResource, aContext);
            if (!path.isNull()) {
                return path;
            }
        }

        return new SGPath();
    }

    public void addBasePath(SGPath aPath/*, Priority aPriority*/) {
        addProvider(new BasePathProvider(aPath, 0/*aPriority*/));
    }

    public void addProvider(ResourceProvider aProvider) {
        providerlist.add(aProvider);
    }

    public static void clear() {
        instance = null;
    }
}

/**
 * trivial provider using a fixed base path
 */
class BasePathProvider implements ResourceProvider {
    SGPath _base;

    public BasePathProvider(SGPath aBase, int aPriority) {
        //TODO prio super(aPriority),
        _base = aBase;
    }

    @Override
    public SGPath resolve(String aResource, SGPath pathunused) {
        SGPath p = new SGPath(_base, aResource);
        // tsch_log("BasePathProvider.resolve: aResource=%s. \n",aResource.c_str());

        return p.exists() ? p : new SGPath();
    }

}
