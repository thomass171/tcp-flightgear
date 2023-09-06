package de.yard.threed.flightgear.core;

import de.yard.threed.flightgear.core.simgear.CppFactory;
import de.yard.threed.flightgear.core.simgear.scene.tgdb.SGTexturedTriangleBin;

/**
 * Created by thomass on 10.08.16.
 */
public class SGTexturedTriangleBinFactory extends CppFactory<SGTexturedTriangleBin> {
    @Override
    public SGTexturedTriangleBin createInstance() {
        return new SGTexturedTriangleBin();
    }
}
