package de.yard.threed.trafficfg.flight;

import de.yard.threed.core.configuration.Properties;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformInternals;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.testutil.SimpleEventBusForTesting;
import de.yard.threed.engine.BaseRequestRegistry;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsTestHelper;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.graph.GraphMovingComponent;
import de.yard.threed.javacommon.ConfigurationByEnv;
import de.yard.threed.javacommon.DefaultResourceReader;
import de.yard.threed.javacommon.SimpleHeadlessPlatform;
import de.yard.threed.outofbrowser.SimpleBundleResolver;
import de.yard.threed.traffic.Destination;
import de.yard.threed.traffic.EllipsoidConversionsProvider;
import de.yard.threed.traffic.RequestRegistry;
import de.yard.threed.trafficfg.FgCalculations;
import de.yard.threed.trafficfg.TravelHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * <p>
 */
@Slf4j
public class FlightSystemTest {

    FlightSystem flightSystem;

    /**
     *
     */
    @BeforeEach
    public void setup() {

        EngineTestFactory.initPlatformForTest(new String[]{"data", "engine", "fgdatabasic"}, configuration1 -> {
            PlatformInternals platformInternals = SimpleHeadlessPlatform.init(configuration1, new SimpleEventBusForTesting());
            Platform.getInstance().addBundleResolver(new SimpleBundleResolver(configuration1.getString("HOSTDIRFG") + "/bundles", new DefaultResourceReader()));
            return platformInternals;
        }, () -> {
            flightSystem = new FlightSystem();
            SystemManager.addSystem(flightSystem);
        }, ConfigurationByEnv.buildDefaultConfigurationWithEnv(new Properties().add("xx", "yy")));


    }

    /**
     * The traditional depart in EDDK.
     */
    @Test
    @Disabled
    public void testEDDKtraditional() throws Exception {

        EngineTestFactory.loadBundleAndWait("traffic-fg");

        Bundle bundle = BundleRegistry.getBundle("traffic-fg");

        GroundNet groundnet = GroundNetTest.loadGroundNetForTesting(bundle, 0, "EDDK", false);
        assertNotNull(groundnet);

        // 23.11.23: wasn't needed before(??). Maybe a side effect.
        SystemManager.putDataProvider("ellipsoidconversionprovider", new EllipsoidConversionsProvider(new FgCalculations()));

        GraphMovingComponent gmc = new GraphMovingComponent();
        EcsEntity aircraft = new EcsEntity(gmc);
        // will send TRAFFIC_REQUEST_AIRCRAFTDEPARTING, 0='Platzrunde'
        TravelHelper.startFlight(aircraft, Destination.buildRoundtrip(0));

        int seconds = 5;
        EcsTestHelper.processSeconds(seconds);
    }
}
