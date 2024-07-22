package de.yard.threed.trafficadvanced.apps;

import de.yard.threed.trafficadvanced.AdvancedConfiguration;
import de.yard.threed.trafficfg.apps.SceneryScene;

public class AdvancedSceneryScene extends SceneryScene {

    @Override
    public String[] getPreInitBundle() {
        // "fgdatabasic","sgmaterial" and "TerraySync" in project are only a small subset. The external should have 'before' prio to load instead of subset.
        AdvancedConfiguration.setAdvancedBundleResolver();

        return super.getPreInitBundle();
    }
}
