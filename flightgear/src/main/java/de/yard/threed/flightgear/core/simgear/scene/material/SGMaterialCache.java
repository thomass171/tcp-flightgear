package de.yard.threed.flightgear.core.simgear.scene.material;

import java.util.HashMap;

/**
 * Aus matlib.[hc]xx
 * <p/>
 * Created by thomass on 04.08.16.
 */
public class SGMaterialCache {
     public HashMap<String, SGMaterial> cache = new HashMap<String, SGMaterial>();

    public SGMaterialCache(){
        
    }
    // Insertion
    public  void insert(String name, SGMaterial material) {
        cache.put(name, material);
    }

    // Lookup
    public  SGMaterial find(String material) {
        return cache.get(material);
    }

}
