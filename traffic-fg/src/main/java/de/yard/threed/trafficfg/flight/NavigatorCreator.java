package de.yard.threed.trafficfg.flight;

import de.yard.threed.core.geometry.ProceduralModelCreator;
import de.yard.threed.core.loader.PortableModel;
import de.yard.threed.engine.apps.ModelSamples;


/**
 * Used by reflection!
 * 
 * Created on 17.01.19.
 */
public class NavigatorCreator implements ProceduralModelCreator {
    @Override
    public PortableModel createModel() {
        PortableModel needle = ModelSamples.buildCompassNeedle(10,1);
        return needle;
    }
}
