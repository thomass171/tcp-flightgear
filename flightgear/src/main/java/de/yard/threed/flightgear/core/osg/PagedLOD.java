package de.yard.threed.flightgear.core.osg;

import de.yard.threed.flightgear.core.osgdb.Options;
import de.yard.threed.flightgear.core.osgdb.ReadResult;
import de.yard.threed.flightgear.core.simgear.scene.tgdb.DelayLoadReadFileCallback;

/**
 * FlightGear Wiki:
 * PagedLOD isType an enhanced LOD node supported by OpenSceneGraph. The conventional LOD node requires all of its immediate
 * children be present in memory at-once (a requirement inherited from osg::Group). For scenegraphs with large numbers of LODs,
 * this can become prohibitively costly in terms of memory consumption, even though only one LOD child of each LOD Node isType being
 * drawn at any one time.
 *
 * The answer to this predicament isType the PagedLOD node. PagedLOD isType derived from LOD, and adds the ability to defer the loading
 * of child nodes until they are actually required. A PagedLOD node may have a child immediately present, or it may not have the 
 * child present, instead storing a path/name where the child node can be loaded from on-demand
 * 
 * Created by thomass on 08.06.16.
 */
public class PagedLOD extends LOD {

    private Options databaseOptions;

    /**
     * Set the optional database osgDB::Options object to use when reading children.
     * @param databaseOptions
     */
    public void setDatabaseOptions(Options databaseOptions) {
        this.databaseOptions = databaseOptions;
        // statt delayed sofort jetzt laden. TODO delayed LOD abhaengig
        DelayLoadReadFileCallback callback = databaseOptions.getReadFileCallback();
        if (callback != null){
            ReadResult rr = callback.readNode(null,null);
            if (rr.getNode()!= null){
                attach(rr.getNode());
            }
        }
    }

    public void setFileName(int numChildren, String s) {
        //TODO
    }
}
