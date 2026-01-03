package de.yard.threed.trafficadvanced;

import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.testutil.Assert;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.platform.PlatformBundleLoader;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.test.testutil.TestUtil;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.testutil.TestHelper;
import de.yard.threed.flightgear.LoaderOptions;
import de.yard.threed.flightgear.TerraSyncBundleResolver;
import de.yard.threed.flightgear.core.FlightGearModuleScenery;
import de.yard.threed.flightgear.core.flightgear.scenery.FGScenery;
import de.yard.threed.flightgear.core.flightgear.scenery.SceneryPager;
import de.yard.threed.flightgear.core.osg.Group;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.flightgear.core.simgear.scene.tgdb.ReaderWriterSTG;
import de.yard.threed.flightgear.core.simgear.scene.util.SGReaderWriterOptions;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.flightgear.testutil.NodeAssertions;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import de.yard.threed.trafficadvanced.testutil.AdvancedBundleResolverSetup;
import de.yard.threed.trafficfg.SceneSetup;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static de.yard.threed.flightgear.TerraSyncBundleResolver.TERRAYSYNCPREFIX;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test external scenery loading. External dependency!
 * <p>
 * Created by thomass on 26.06.2025
 */
@Slf4j
public class SceneryTest {

    @BeforeEach
    void setup() {
        FgTestFactory.initPlatformForTest(new HashMap<>(), false, true, true, false, new AdvancedBundleResolverSetup());
    }

    /**
     * 3023763.stg contains airports EHAM and ...
     */
    @Test
    public void testSTG3023763() throws Exception {

        // redirect to ex
        AdvancedConfiguration.setAdvancedBundleResolver();

        // make bundle available to avoid ocean tile
        EngineTestFactory.loadBundleSync(BundleRegistry.TERRAYSYNCPREFIX + "3023763");
        EngineTestFactory.loadBundleAndWait(BundleRegistry.TERRAYSYNCPREFIX + "model", 120000);
        SGReaderWriterOptions options = new SGReaderWriterOptions();

        LoaderOptions opt = new LoaderOptions();
        opt.usegltf = true;
        SceneNode destinationNode = new ReaderWriterSTG().build("3023763.stg", options, opt, false);
        if (destinationNode == null) {
            Assert.fail("node is null. 3023763.stg failed");
        }

        // Probably many many GLTFS are queued to be loaded
        TestUtils.waitUntil(() -> {
            TestHelper.processAsync();
            return AbstractSceneRunner.getInstance().futures.size() == 0;
        }, 31000);

        // result will be in destinationNode. dump has large output.
        log.debug(destinationNode.dump("  ", 0));

        //NodeAssertions.assertSTG3072816(destinationNode);
        assertEquals(1, destinationNode.findNodeByName("Terrain/e000n50/e004n52/EHAM.gltf").size());
        assertEquals(1, SceneNode.findNode(n -> StringUtils.endsWith(n.getName() == null ? "" : n.getName(), "EHAM.gltf"), destinationNode).size());

        FGScenery scenery = FlightGearModuleScenery.getInstance().get_scenery();
        // FGScenery doesn't attach the scenery to world ...
        Scene.getCurrent().addToWorld(scenery.get_scene_graph());
        assertEquals(1, Platform.getInstance().findSceneNodeByName("Terrain/e000n50/e004n52/EHAM.gltf").size());
        // ... and ReaderWriterSTG doesn't attach to 'terrain_branch'
        FlightGearModuleScenery.getInstance().get_scenery().get_terrain_branch().attach(destinationNode);

        Double elevation = scenery.get_elevation_m(
                SGGeod.fromLatLon(Util.parseLatLon(SceneSetup.EHAM06)), new Vector3());
        TestUtil.assertNotNull("elevation", elevation);
        log.debug("EHAM06 elevation=", elevation);
    }
}

