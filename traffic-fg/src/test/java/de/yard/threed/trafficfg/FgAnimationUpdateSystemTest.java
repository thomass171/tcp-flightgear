package de.yard.threed.trafficfg;

import de.yard.threed.core.Quaternion;
import de.yard.threed.core.configuration.Properties;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformInternals;
import de.yard.threed.core.testutil.SimpleEventBusForTesting;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsGroup;
import de.yard.threed.engine.ecs.EcsHelper;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.platform.common.ModelLoader;
import de.yard.threed.engine.testutil.AdvancedHeadlessPlatform;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.flightgear.SceneryTest;
import de.yard.threed.flightgear.TerraSyncBundleResolver;
import de.yard.threed.flightgear.core.FlightGearModuleBasic;
import de.yard.threed.flightgear.core.FlightGearModuleScenery;
import de.yard.threed.flightgear.core.flightgear.main.FGGlobals;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterialLib;
import de.yard.threed.flightgear.core.simgear.scene.model.ACProcessPolicy;
import de.yard.threed.flightgear.core.simgear.scene.model.SGRotateAnimation;
import de.yard.threed.flightgear.ecs.FgAnimationComponent;
import de.yard.threed.flightgear.ecs.FgAnimationUpdateSystem;
import de.yard.threed.javacommon.ConfigurationByEnv;
import de.yard.threed.javacommon.DefaultResourceReader;
import de.yard.threed.outofbrowser.SimpleBundleResolver;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static de.yard.threed.core.testutil.TestUtils.assertQuaternion;
import static de.yard.threed.core.testutil.TestUtils.assertVector3;
import static org.junit.jupiter.api.Assertions.*;

/**
 * <p>
 */
@Slf4j
public class FgAnimationUpdateSystemTest {

    FgAnimationUpdateSystem animationUpdateSystem;

    /**
     *
     */
    @BeforeEach
    public void setup() {

        // use AdvancedHeadlessPlatform to have AsyncHelper and thus model loading
        EngineTestFactory.initPlatformForTest(new String[]{"data", "engine", "fgdatabasic"}, configuration1 -> {
            PlatformInternals platformInternals = AdvancedHeadlessPlatform.init(configuration1, new SimpleEventBusForTesting());
            Platform.getInstance().addBundleResolver(new TerraSyncBundleResolver(configuration1.getString("HOSTDIRFG") + "/bundles"));
            Platform.getInstance().addBundleResolver(new SimpleBundleResolver(configuration1.getString("HOSTDIRFG") + "/bundles", new DefaultResourceReader()));
            return platformInternals;
        }, () -> {
            animationUpdateSystem = new FgAnimationUpdateSystem();
            SystemManager.addSystem(animationUpdateSystem);
        }, ConfigurationByEnv.buildDefaultConfigurationWithEnv(new Properties().add("xx", "yy")));

        EngineTestFactory.loadBundleAndWait(SGMaterialLib.BUNDLENAME);
        FlightGearModuleBasic.init(null, null);
        FlightGearModuleScenery.init(false);
    }

    /**
     * Needs to test on STG level because entity/animation creation is done in STG reader.
     */
    @Test
    public void test3072824() throws Exception {

        // clean entities?

        // AC policy. Probably not needed in test.
        ModelLoader.processPolicy = new ACProcessPolicy(null);

        // 3072824.stg exists twice (in Objects and in Terrain). Both should be loaded.
        SceneNode destinationNode = SceneryTest.loadSTGFromBundleAndWait(3072824);
        assertNotNull(destinationNode);

        // egkk_tower has many animations. Cuurently only 2 are loaded(?) probably due to missing textures and submodel.
        EcsEntity egkkTower = EcsHelper.findEntitiesByName("Objects/e000n50/e007n50/egkk_tower.xml").get(0);
        String expectedSgPropertyName = "/sim/time/elapsed-sec";
        FgAnimationComponent animationComponent = FgAnimationComponent.getFgAnimationComponent(egkkTower);
        assertNotNull(animationComponent);
        assertEquals(2, animationComponent.animationList.size(), "animations");
        SGRotateAnimation rotateAnimation = (SGRotateAnimation) animationComponent.animationList.get(0);
        // rotation should still have default value
        assertQuaternion(new Quaternion(), rotateAnimation.rotategroup.getTransform().getRotation());
        assertNull(rotateAnimation.lastUsedAngle);

        double elapsedsec = 0.5;
        FGGlobals.getInstance().get_props().getNode("/sim/time/elapsed-sec", true).setDoubleValue(elapsedsec);
        EcsGroup group = new EcsGroup(new ArrayList<>(),egkkTower);
        group.add(animationComponent);
        // tpf is not considered by animations
        animationUpdateSystem.update(egkkTower, group, 0.4);
        // There is a "scale" of -90 degrees somewhere in RotateAnimation. So considering the elapsedtime the angle -45 appears correct.
        assertEquals(-45.0, rotateAnimation.lastUsedAngle.getDegree());
    }
}
