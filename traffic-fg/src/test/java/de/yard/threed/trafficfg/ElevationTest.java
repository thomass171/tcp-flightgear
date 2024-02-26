package de.yard.threed.trafficfg;

import de.yard.threed.core.BooleanHolder;
import de.yard.threed.core.GeneralParameterHandler;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.testutil.TestHelper;
import de.yard.threed.flightgear.LoaderOptions;
import de.yard.threed.flightgear.TerrainElevationProvider;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.FlightGearModuleScenery;
import de.yard.threed.flightgear.core.simgear.scene.tgdb.SGReaderWriterBTG;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 */
@Slf4j
public class ElevationTest {
    Platform platform = FgTestFactory.initPlatformForTest(false, true);

    @Test
    public void testEDDKGroundnetElevation() throws Exception {
        EngineTestFactory.loadBundleSync(FlightGear.getBucketBundleName("3072816"));
        Bundle bundle3072816 = BundleRegistry.getBundle("Terrasync-3072816");

        // will map btg to gltf. GLTF is just result from btg conversion
        BundleResource br = new BundleResource(bundle3072816, "Terrain/e000n50/e007n50/EDDK.btg");
        final BooleanHolder validated = new BooleanHolder(false);
        SGReaderWriterBTG.loadBTG(br, null, new LoaderOptions(FlightGearModuleScenery.getInstance().get_matlib()), new GeneralParameterHandler<SceneNode>() {
            @Override
            public void handle(SceneNode result) {
                assertNotNull(result);
                // only reading the BTG doesn't add it to world, which is needed for elevation intersection. So do it here.
                Scene.getCurrent().addToWorld(result);
                validated.setValue(true);
            }
        });

        TestUtils.waitUntil(() -> {
            TestHelper.processAsync();
            return validated.getValue();
        }, 10000);

        log.debug(Scene.getCurrent().getWorld().dump("  ", 0));

        TerrainElevationProvider elevationProvider = new TerrainElevationProvider();
        /*elevations are missing TODO GroundNet groundNet = GroundNetTest.loadGroundNetForTesting( BundleRegistry.getBundle("traffic-fg"), 0, "EDDK",false,
                 elevationProvider);*/

    }

}
