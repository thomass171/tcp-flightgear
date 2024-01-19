package de.yard.threed.trafficfg.flight;

import de.yard.threed.core.geometry.ProceduralModelCreator;
import de.yard.threed.core.loader.PortableModelList;
import de.yard.threed.engine.apps.ModelSamples;


/**
 * Used by reflection!
 * 
 * Created on 17.01.19.
 */
public class NavigatorCreator implements ProceduralModelCreator {
    @Override
    public PortableModelList createModel() {
        PortableModelList needle = ModelSamples.buildCompassNeedle(10,1);
        return needle;
    }
}
