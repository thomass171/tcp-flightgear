package de.yard.threed.flightgear;

import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterialLib;
import de.yard.threed.core.resource.ResourcePath;

/**
 * 8.6.17: Alternative to FGs "SGReaderWriterOptions extends Options" to be used with bundle (databasepath is a Resourcepath here).
 * 11.3.24: Should replace SGReaderWriterOptions and Options.
 * <p>
 * Created by thomass on 25.04.17.
 */
public class LoaderOptions {
    public ResourcePath databasePath;
    public SGMaterialLib materialLib;
    //11.1.18: Jetzt true als default, weil auch acpp immer als default verwendet wurde.
    public boolean usegltf = true;
    // 11.3.24 root of the property tree to be used for building animations and effects. (not the config node tree!)
    public SGPropertyNode propertyNode;
    public EffectBuilderListener effectBuilderListener;

    public LoaderOptions() {

    }

    public LoaderOptions(SGMaterialLib materialLib) {
        this.materialLib = materialLib;
    }

    public void setDatabasePath(ResourcePath databasePath) {
        this.databasePath = databasePath;
    }

    public void setMaterialLib(SGMaterialLib materialLib) {
        this.materialLib = materialLib;
    }

    public SGPropertyNode getPropertyNode() {
        return propertyNode;
    }
}
