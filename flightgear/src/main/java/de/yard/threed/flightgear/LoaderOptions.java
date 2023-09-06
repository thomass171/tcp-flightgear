package de.yard.threed.flightgear;

import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterialLib;
import de.yard.threed.core.resource.ResourcePath;

/**
 * 8.6.17: Das ist von mir und nicht FG? Ich weiss nicht mehr warum, wegen Bundle? Ja, databasepath ist hier ein Resourcepath!
 * <p>
 * Created by thomass on 25.04.17.
 */
public class LoaderOptions {
    public ResourcePath databasePath;
    public SGMaterialLib materialLib;
    //11.1.18: Jetzt true als default, weil auch acpp immer als default verwendet wurde.
    public boolean usegltf=true;

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
}
