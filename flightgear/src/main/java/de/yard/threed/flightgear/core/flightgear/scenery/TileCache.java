package de.yard.threed.flightgear.core.flightgear.scenery;

import de.yard.threed.core.Util;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.flightgear.core.simgear.bucket.SGBucket;
import de.yard.threed.core.platform.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * tilecache.[ch]xx
 * A class to store and manage a pile of tiles
 * <p/>
 * Created by thomass on 11.08.16.
 */
public class TileCache {
    static Log logger = Platform.getInstance().getLog(TileCache.class);

    //typedef map < long, TileEntry * > tile_map;
    //typedef tile_map::iterator tile_map_iterator;
    // typedef tile_map::const_iterator const_tile_map_iterator;
    // cache storage space
    //map offenbar ohne autocreate
    HashMap<Long, TileEntry> /*tile_map*/ tile_cache = new HashMap<Long, TileEntry>();

    // maximum cache size
    int max_cache_size;

    // pointers to allow an external linear traversal of cache entries
    //tile_map_iterator current;
    int current;
    // bloede Konstruktion
    private List<Long> keyset;

    double current_time;
    
    
   /*
    tile_map_iterator begin() {
        return tile_cache.begin();
    }

    tile_map_iterator end() {
        return tile_cache.end();
    }

    const_tile_map_iterator begin()

    const

    {
        return tile_cache.begin();
    }

    const_tile_map_iterator end()

    const

    {
        return tile_cache.end();
    }*/

    // Constructor
    public TileCache() {
        max_cache_size = 100;
        current_time = 0.0;

        tile_cache.clear();
    }

    // Initialize the tile cache subsystem
    public void init() {
        logger.info(/*SG_LOG(SG_TERRAIN, SG_INFO,*/ "Initializing the tile cache.");

        logger.info(/*SG_LOG(SG_TERRAIN, SG_INFO,*/"  max cache size = " + max_cache_size);
        logger.info(/*SG_LOG(SG_TERRAIN, SG_INFO,*/ "  current cache size = " + tile_cache.size());

        clear_cache();

        logger.info(/*SG_LOG(SG_TERRAIN, SG_INFO,*/ "  done with init()");
    }

    // Free a tile cache entry
    void entry_free(long tile_index) {
        //SG_LOG( SG_TERRAIN, SG_DEBUG, "FREEING CACHE ENTRY = " << tile_index );
        TileEntry tile = tile_cache.get(tile_index);
        tile.removeFromSceneGraph();
        tile_cache.remove(/*erase*/tile_index);
        //delete tile;
    }
    
    // Check if the specified "bucket" exists in the cache
    boolean exists(SGBucket b) {
        long tile_index = b.gen_index();
        /*const_tile_map_iterator it = tile_cache.find(tile_index);
        return (it != tile_cache.end());*/
        return tile_cache.get(tile_index) != null;
    }

    // Return the index of a tile to be dropped from the cache, return -1 if
    // nothing available to be removed.
    long get_drop_tile() {
        
        long min_index = -1;
        double min_time = java.lang.Double.MAX_VALUE;//DBL_MAX;
        float priority = java.lang.Float.MAX_VALUE;//FLT_MAX;

        //tile_map_iterator current = tile_cache.begin();
        //tile_map_iterator end = tile_cache.end();

        //for (; current != end; ++current) {
        for (long index:tile_cache.keySet()){
            //long index = current.getFirst;
            TileEntry e = tile_cache.get(index);//current.getSecond;
            if ((!e.is_current_view()) &&
                    (e.is_expired(current_time))) {
                if (e.is_expired(current_time - 1.0) &&
                        !e.is_loaded()) {
                /* Immediately drop "empty" tiles which are no longer used/requested, and were last requested > 1 getSecond ago...
                 * Allow a 1 getSecond timeout since an empty tiles may just be loaded...
                 */
                    logger.debug( "    dropping an unused and empty tile");
                    min_index = index;
                    break;
                }
                if ((e.get_time_expired() < min_time) ||
                        ((e.get_time_expired() == min_time) &&
                                (priority > e.get_priority()))) {
                    // drop oldest tile with lowest priority
                    min_time = e.get_time_expired();
                    priority = e.get_priority();
                    min_index = index;
                }
            }
        }

        logger.debug( "    index = " + min_index);
        logger.debug("    min_time = " + min_time);

        return min_index;
    }


    long get_first_expired_tile() {
        Util.notyet();
        return -1;
/*
        const_tile_map_iterator current = tile_cache.begin();
        const_tile_map_iterator end = tile_cache.end();

        for (; current != end; ++current) {
            TileEntry * e = current.getSecond;
            if (!e.is_current_view() && e.is_expired(current_time)) {
                return current.getFirst;
            }
        }

        return -1; // no expired tile found*/
    }


    // Clear all flags indicating tiles belonging to the current viewer
    void clear_current_view() {
        /*tile_map_iterator current = tile_cache.begin();
        tile_map_iterator end = tile_cache.end();
        for (; current != end; ++current) {*/
        for (long index : tile_cache.keySet()){
            TileEntry  e = tile_cache.get(index);//current.getSecond;
            if (e.is_current_view()) {
                // update expiry time for tiles belonging to most recent position
                e.update_time_expired(current_time);
                e.set_current_view(false);
            }
        }
    }


    // Clear a cache entry, note that the cache only holds pointers
    // and this does not free the object which isType pointed to.
    void clear_entry(long cache_entry) {
        Util.notyet();
        // tile_cache.erase(tile_index);
    }

    // Clear all completely loaded tiles (ignores partially loaded tiles)
    void clear_cache() {
        List<Long> indexList = new ArrayList<Long>();
        //tile_map_iterator current = tile_cache.begin();
        //tile_map_iterator end = tile_cache.end();

        //for (; current != end; ++current) {
        for (long index : tile_cache.keySet()){
            //long index = current.getFirst;
            TileEntry e = tile_cache.get(index);//getSecond;
            if (e.is_loaded()) {
                e.tile_bucket.make_bad();
                // entry_free modifies tile_cache, so store index and call entry_free() later;
                indexList.add(index);
            }
        }
        for ( int it = 0;        it<indexList.size (); it++){
            entry_free((long)indexList.get(it));
        }
    }


    // Return a pointer to the specified tile cache entry
    TileEntry get_tile(long tile_index) {
        /*const_tile_map_iterator it = tile_cache.find( tile_index );
        if ( it != tile_cache.end() ) {
            return it.getSecond;
        } else {
            return NULL;
        }*/
        return tile_cache.get(tile_index);
    }

    // Return a pointer to the specified tile cache entry
    TileEntry get_tile(SGBucket b) {
        return get_tile(b.gen_index());
    }

    // Return the cache size
    public int get_size() {
        return tile_cache.size();
    }

    // External linear traversal of cache
   public void reset_traversal() {
        current = 0;//tile_cache.begin();
       keyset = new ArrayList<Long>(tile_cache.keySet());
    }

    public boolean at_end() {
        return current >= tile_cache.size();//== tile_cache.end();
    }

    public TileEntry get_current() {
        // cout << "index = " << current.getFirst << endl;
        return tile_cache.get(keyset.get(current));//current.getSecond;
    }

    public void next() {
        ++current;
    }

    int get_max_cache_size() {
        return max_cache_size;
    }

    void set_max_cache_size(int m) {
        max_cache_size = m;
    }

    /**
     * Create a new tile and enqueue it for loading.
     *
     * @return success/failure
     */
    boolean insert_tile(TileEntry e) {
        // register tile in the cache
        long tile_index = e.get_tile_bucket().gen_index();
        tile_cache.put(tile_index,e);
        e.update_time_expired(current_time);

        return true;
    }


    void set_current_time(double val) {
        current_time = val;
    }

    double get_current_time() {
        return current_time;
    }

    // update tile's priority and expiry time according to current request
    void request_tile(TileEntry t, float priority, boolean current_view, double request_time) {
        if ((!current_view) && (request_time <= 0.0))
            return;

        // update priority when higher - or old request has expired
        if ((t.is_expired(current_time)) ||
                (priority > t.get_priority())) {
            t.set_priority(priority);
        }

        if (current_view) {
            t.update_time_expired(current_time);
            t.set_current_view(true);
        } else {
            t.update_time_expired(current_time + request_time);
        }
    }
}

