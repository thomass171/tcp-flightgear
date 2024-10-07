package de.yard.threed.trafficfg;

import de.yard.threed.core.BooleanHolder;
import de.yard.threed.core.Degree;
import de.yard.threed.core.GeneralParameterHandler;
import de.yard.threed.core.IntHolder;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.platform.common.ModelLoader;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.testutil.TestHelper;
import de.yard.threed.flightgear.LoaderOptions;
import de.yard.threed.flightgear.SceneryTest;
import de.yard.threed.flightgear.TerrainElevationProvider;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.FlightGearModuleScenery;
import de.yard.threed.flightgear.core.simgear.scene.model.ACProcessPolicy;
import de.yard.threed.flightgear.core.simgear.scene.tgdb.SGReaderWriterBTG;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.traffic.BasicRouteBuilder;
import de.yard.threed.traffic.EllipsoidConversionsProvider;
import de.yard.threed.traffic.GeoRoute;
import de.yard.threed.traffic.flight.FlightRouteGraph;
import de.yard.threed.traffic.geodesy.GeoCoordinate;
import de.yard.threed.trafficfg.fgadapter.FgTerrainBuilder;
import de.yard.threed.trafficfg.flight.GroundNet;
import de.yard.threed.trafficfg.flight.GroundNetTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 */
@Slf4j
public class ElevationTest {
    Platform platform = FgTestFactory.initPlatformForTest(false, true, true);

    @Test
    public void testEDDKGroundnetElevation() throws Exception {
        loadBtgOfEDDK();

        // EDDK groundnet exceeds EDDK tile, so define a default value 68. 31.5.24: Really?, using a default spoils test of intersection.
        TerrainElevationProvider elevationProvider = new TerrainElevationProvider();
        //29.5.24 no longer? keep for test
        elevationProvider.setDefaultAltitude(68.0);
        GroundNet groundNet = GroundNetTest.loadGroundNetForTesting(BundleRegistry.getBundle("traffic-fg"), 0, "EDDK", false,
                elevationProvider);
        assertNotNull(groundNet);
    }

    @Test
    public void testEDDKSimpleElevation() throws Exception {
        loadBtgOfEDDK();

        // EDDK groundnet exceeds EDDK tile, so define a default value 68. 31.5.24: Really?, using a default spoils test of intersection.
        TerrainElevationProvider elevationProvider = new TerrainElevationProvider();

        LatLon eddkCenter = new LatLon(new Degree(50.86538f), new Degree(7.139103f));
        Double elevation = elevationProvider.getElevation(eddkCenter.getLatDeg().getDegree(), eddkCenter.getLonDeg().getDegree());
        assertNotNull(elevation);
        // 76.10 appears correct
        assertEquals(76.1, elevation, 0.1);

        LatLon someOnRunway = new LatLon(new Degree(50.8662999), new Degree(7.1443999));
        elevation = elevationProvider.getElevation(someOnRunway.getLatDeg().getDegree(), someOnRunway.getLonDeg().getDegree());
        assertNotNull(elevation);
        // 78.66 appears correct
        assertEquals(78.66, elevation, 0.1);
    }

    /**
     * Test terrain loading for georoute EDKB_EDDK like it is done in TrafficSystem.
     * All tiles needed should be available. Tile has size 13kmx17km appx.
     */
    @ParameterizedTest
    @CsvSource(value = {"true", "false"}, delimiter = ';')
    public void testTerrainLoadForRoute(boolean withTerrainPreload) throws Exception {

        GeoRoute initialRoute = GeoRoute.parse(GeoRoute.SAMPLE_EDKB_EDDK);

        SystemManager.putDataProvider("ellipsoidconversionprovider", new EllipsoidConversionsProvider(new FgCalculations()));

        SceneNode localSphereWorld = new SceneNode();
        // only loading scenery doesn't add it to world, which is needed for elevation intersection. So do it here.
        Scene.getCurrent().addToWorld(localSphereWorld);

        FgTerrainBuilder fgTerrainBuilder = null;

        if (withTerrainPreload) {
            for (int tile : new int[]{3072824, 3072816,3056443,3056435}) {
                // 3072824.stg exists twice (in Objects and in Terrain). Both should be loaded.
                Scene.getCurrent().addToWorld(SceneryTest.loadSTGFromBundleAndWait(tile));
            }
            assertEquals(1, SceneNode.findNode(n -> "Terrain/e000n50/e007n50/EDDK.gltf".equals(n.getName() == null ? "" : n.getName()), Scene.getCurrent().getWorld()).size());
            assertEquals(1, SceneNode.findNode(n -> "Terrain/e000n50/e007n50/EDKB.gltf".equals(n.getName() == null ? "" : n.getName()), Scene.getCurrent().getWorld()).size());

            SystemManager.putDataProvider(SystemManager.DATAPROVIDERELEVATION, new TerrainElevationProvider());

        } else {
            // FgTerrainBuilder makes a full FG init
            fgTerrainBuilder = new FgTerrainBuilder();
            // init() might interfere with init in test setup
            fgTerrainBuilder.init(localSphereWorld);

            // elevation detection requires proper scenery properly in world, so make sure it is
            TrafficRuntimeTestUtil.assertSceneryNodes(localSphereWorld);
        }
        IntHolder attemptCounter = new IntHolder();
        elevateRoute(initialRoute, fgTerrainBuilder, attemptCounter);

        if (withTerrainPreload) {
            assertEquals(1, attemptCounter.getValue());
        } else {
            // 2 attempts appears correct
            assertEquals(2, attemptCounter.getValue());
            // Even though tiles are loaded async, they should exist now.
            // 4 appears correct (9 surrounding+EDDK?).  with default(?) range 15 km should have loaded 3x3.
            assertEquals(4, fgTerrainBuilder.getLoadedBundles().size());
        }
    }

    private void loadBtgOfEDDK() throws Exception {
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

    }

    /**
     * fgTerrainBuilder might be null if terrain should be available.
     * Counts number of attempts (or fails)
     */
    private FlightRouteGraph elevateRoute(GeoRoute initialRoute, FgTerrainBuilder fgTerrainBuilder, IntHolder attemptCounter) throws Exception {

        List<FlightRouteGraph> flightRoute = new ArrayList<FlightRouteGraph>();

        attemptCounter.setValue(0);
        TestUtils.waitUntil(() -> {
            attemptCounter.inc();
            BooleanHolder missingElevation = new BooleanHolder(false);

            flightRoute.add(new BasicRouteBuilder(new FgCalculations())
                    .fromGeoRoute(initialRoute, geoCoordinate -> {
                        log.debug("No elevation for " + geoCoordinate + " of initialRoute");
                        missingElevation.setValue(true);
                        Vector3 position = new FgCalculations().toCart(new GeoCoordinate(geoCoordinate.getLatDeg(), geoCoordinate.getLonDeg(), 0));
                        if (fgTerrainBuilder != null) {
                            fgTerrainBuilder.updateForPosition(position, new Vector3());
                            // Loading of bundles (STGs) is async.
                            TestHelper.processAsync();
                        }
                    }));
            return !missingElevation.getValue();
        }, 10000);

        return flightRoute.get(0);
    }
}
