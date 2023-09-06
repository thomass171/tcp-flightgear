package de.yard.threed.flightgear.core;

import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterial;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by thomass on 10.08.16.
 */
public class ArrayListSGMaterialFactory extends de.yard.threed.flightgear.core.simgear.CppFactory<List<SGMaterial>> {
    
    @Override
    public List<SGMaterial> createInstance() {
        return new ArrayList<SGMaterial>();
    }
}
