package de.yard.threed.trafficadvanced.apps;

import de.yard.threed.trafficadvanced.AdvancedConfiguration;
import de.yard.threed.trafficfg.apps.SceneryViewerScene;

/**
 * Scenery viewer for full scenery from bundlepool
 */
public class AdvancedSceneryViewerScene extends SceneryViewerScene {

    @Override
    public String[] getPreInitBundle() {
        // "fgdatabasic","sgmaterial" and "TerraySync" in project are only a small subset. The external should have 'before' prio to load instead of subset.
        AdvancedConfiguration.setAdvancedBundleResolver();

        return super.getPreInitBundle();
    }
}
