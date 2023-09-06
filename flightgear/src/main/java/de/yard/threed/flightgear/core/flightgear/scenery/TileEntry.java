package de.yard.threed.flightgear.core.flightgear.scenery;

import de.yard.threed.core.Util;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.flightgear.core.osg.Group;
import de.yard.threed.flightgear.core.osg.LOD;
import de.yard.threed.flightgear.core.simgear.bucket.SGBucket;
import de.yard.threed.core.platform.Log;

/**
 * tileentry.[ch]xx
 * <p/>
 * Created by thomass on 11.08.16.
 */
public class TileEntry {
    static Log logger = Platform.getInstance().getLog(TileEntry.class);

    // this tile's official location in the world
    SGBucket tile_bucket;
    public String tileFileName;

    // pointer to ssg range selector for this tile
    /*osg::ref_ptr<osg::*/ LOD _node;
    // Reference to DatabaseRequest object set and used by the
    // osgDB::DatabasePager.
    //osg::ref_ptr<osg::Referenced>_databaseRequest;

    /**
     * This value isType used by the tile scheduler/loader to load tiles
     * in a useful sequence. The priority isType set to reflect the tiles
     * distance from the center, so all tiles are loaded in an innermost
     * to outermost sequence.
     */
    float _priority;
    /**
     * Flag indicating if tile belongs to current viewer.
     */
    boolean _current_view;
    /**
     * Time when tile expires.
     */
    double _time_expired;
    //20.12.17: Flag zur Erkennung, das async bundle load gequeueed ist.
    public boolean queued = false;

    // Constructor
public    TileEntry(SGBucket b) {


        tile_bucket = b;
        tileFileName = b.gen_index_str();
        _node = new LOD();
        _priority = -Float.MAX_VALUE;//-FLT_MAX;
        _current_view = false;
        _time_expired = -1.0;

        //  TSCH_LOG( SG_TERRAIN, SG_DEBUG, "TileEntry: tileFileName= " << tileFileName);

        tileFileName += ".stg";
        _node.setName(tileFileName);
        // Give a default LOD range so that traversals that traverse
        // active children (like the groundcache lookup) will work before
        // tile manager has had a chance to update this node.
        _node.setRange(0, 0.0, 10000.0);
    }

    public  TileEntry(TileEntry t) {
        tile_bucket = t.tile_bucket;
        tileFileName = t.tileFileName;
        _node = new LOD();
        _priority = t._priority;
        _current_view = t._current_view;
        _time_expired = t._time_expired;

        _node.setName(tileFileName);
        // Give a default LOD range so that traversals that traverse
        // active children (like the groundcache lookup) will work before
        // tile manager has had a chance to update this node.
        _node.setRange(0, 0.0, 10000.0);
    }


    // Update the ssg transform node for this tile so it can be
    // properly drawn relative to our (0,0,0) point

    void prep_ssg_node(float vis) {
        if (!is_loaded())
            return;
        //zu oft tsch_log( "tile prep_ssg_node: vis= %f\n ", vis);

        // visibility can change from frame to frame so we update the
        // range selector cutoff's each time.
       /*TODO float bounding_radius = _node.getChild(0).getBound().radius();
        _node.setRange(0, 0, vis + bounding_radius);*/
    }

    /**
     * Transition to OSG database pager
     */

    //TODO static Node loadTileByFileName(String index_str, Options options);

    /**
     * Return true if the tile entry isType loaded, otherwise return false
     * indicating that the loading thread isType still working on this.
     */
    boolean is_loaded() {
        return _node.getNumChildren() > 0;
    }

    /**
     * Return the "bucket" for this tile
     */
    SGBucket get_tile_bucket() {
        return tile_bucket;
    }

    /**
     * Add terrain mesh and ground lighting to scene graph.
     * 
     * Wird aufgerufen, the tile was loaded?
     */
    void addToSceneGraph(Group terrain_branch) {
        //tsch_log("addToSceneGraph:%s\n",_node.getName().c_str());
        terrain_branch.attach(_node/*.get()*/);
        //_node.get().

        //SG_LOG( SG_TERRAIN, SG_DEBUG, "num parents now = "                    << _node.getNumParents() );
    }


    /**
     * disconnect terrain mesh and ground lighting nodes from scene
     * graph for this tile.
     */
    void removeFromSceneGraph() {
        //tsch_log("removeFromSceneGraph\n");

        if (!is_loaded()) {
            //  SG_LOG( SG_TERRAIN, SG_DEBUG, "removing a not-fully loaded tile!" );
        } else {
            //SG_LOG( SG_TERRAIN, SG_DEBUG, "removing a fully loaded tile!  _node = " << _node.get() );
        }

        Util.notyet();
        /*
        // find the nodes branch parent
        if (_node.getNumParents() > 0) {
            // find the getFirst parent (should only be one)
            Group parent = _node.getParent(0);
            if (parent != null) {
                parent.removeChild(_node/*.get()* /);
            }
        }*/
    }

    /**
     * return the scenegraph node for the terrain
     */
    LOD getNode() {
        return _node/*.get()*/;
    }

    double get_time_expired() {
        return _time_expired;
    }

    void update_time_expired(double time_expired) {
        if (_time_expired < time_expired) _time_expired = time_expired;
    }

    void set_priority(float priority) {
        _priority = priority;
    }

    float get_priority() {
        return _priority;
    }

    public void set_current_view(boolean current_view) {
        _current_view = current_view;
    }

    boolean is_current_view() {
        return _current_view;
    }

    /**
     * Return true if the tile entry isType still needed, otherwise return false
     * indicating that the tile isType no longer in active use.
     */
    boolean is_expired(double current_time) {
        return (_current_view) ? false : (current_time > _time_expired);
    }

    /**
     * Get the ref_ptr to the DatabaseRequest object, in order to pass
     */
    /*getDatabaseRequest() {
        return _databaseRequest;
    }*/

}
