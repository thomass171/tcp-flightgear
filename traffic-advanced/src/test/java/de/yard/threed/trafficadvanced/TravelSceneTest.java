package de.yard.threed.trafficadvanced;


import de.yard.threed.core.Payload;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsHelper;
import de.yard.threed.engine.ecs.EcsTestHelper;
import de.yard.threed.engine.ecs.EntityFilter;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.TeleportComponent;
import de.yard.threed.engine.ecs.TeleporterSystem;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.testutil.ExpectedEntity;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.engine.testutil.TestHelper;
import de.yard.threed.flightgear.core.FlightGearModuleScenery;
import de.yard.threed.flightgear.core.flightgear.scenery.FGTileMgr;
import de.yard.threed.flightgear.core.simgear.scene.tgdb.SGOceanTile;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.graph.GraphMovingComponent;
import de.yard.threed.traffic.*;
import de.yard.threed.traffic.config.VehicleDefinition;
import de.yard.threed.trafficadvanced.apps.TravelScene;
import de.yard.threed.trafficadvanced.testutil.AdvancedBundleResolverSetup;
import de.yard.threed.trafficadvanced.testutil.AdvancedTestUtils;
import de.yard.threed.trafficcore.model.Vehicle;
import de.yard.threed.trafficfg.TravelSceneTestHelper;
import de.yard.threed.trafficfg.flight.GroundServiceComponent;
import de.yard.threed.trafficfg.flight.GroundServicesSystem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashMap;
import java.util.List;

import static de.yard.threed.core.testutil.TestUtils.assertQuaternion;
import static de.yard.threed.javanative.JavaUtil.sleepMs;
import static org.junit.jupiter.api.Assertions.*;


/**
 *
 */
public class TravelSceneTest {

    SceneNode world;
    SceneRunnerForTesting sceneRunner;
    static final int INITIAL_FRAMES = 10;
    Log log;

    @ParameterizedTest
    @CsvSource(value = {
            "true;true;false;null;null",
            "false;false;false;null;null",
            "false;true;true;null;null",
            // 24.1.26 EHAM extracted to separate test
    }, delimiter = ';', nullValues = {"null"})
    public void testDefaultTravelSceneInEDDK(boolean enableDoormarker, boolean enableNavigator, boolean worldTeleport, String initialLocation, String initialHeading) throws Exception {

        if (worldTeleport && !enableNavigator) {
            fail("invalid");
        }

        setup(enableDoormarker, enableNavigator, initialLocation, initialHeading, null);
        Log log = Platform.getInstance().getLog(TravelSceneTest.class);

        assertEquals(INITIAL_FRAMES, sceneRunner.getFrameCount());
        SphereSystem sphereSystem = (SphereSystem) SystemManager.findSystem(SphereSystem.TAG);
        // world is set in SphereSystem.init()
        assertNotNull(sphereSystem.world);
        TravelSceneTestHelper.waitForSphereLoaded(sceneRunner);

        TravelSceneTestHelper.validateSphereProjections();

        String[] bundleNames = BundleRegistry.getBundleNames();
        // 7 appears correct, but without "Terrasync-model" its only 6.
        assertEquals(6, bundleNames.length);
        assertNotNull(BundleRegistry.getBundle("fgdatabasic"));

        sceneRunner.runLimitedFrames(50);

        EcsEntity userEntity = SystemManager.findEntities(e -> "Freds account name".equals(e.getName())).get(0);
        assertNotNull(userEntity, "userEntity");
        assertNotNull(userEntity.getName(), "name");

        validateVehicles();

        // vehicle need groundnet to be loaded.
        TestUtils.waitUntil(() -> {
            sceneRunner.runLimitedFrames(1);
            return GroundServicesSystem.groundnets.get("EDDK") != null;
        }, 60000);

        //11 passt: "Player",5 GS Vehicle (3 LSG?, 2 Goldhofert?, no delayed aircraft),  Vehicle from sceneconfig (747, 737, 738, Bravo), 3 Aircraft
        //Why 15? (earth,moon,sun no longer exist)? + 7+24(??) animated scenery objects
        //int expectedNumberOfEntites = /*15*/(enableNavigator ? 12 : 11) + 7 + 18/*??*/;
        // 20.7.24 with full scenery number of entities increased to more than 44. So test more specific.
        List<ExpectedEntity> expectedEntities = List.of(
                new ExpectedEntity("Freds account name", 1),
                new ExpectedEntity("Objects/e000n50/e006n50/colonius.xml", 1),
                new ExpectedEntity("Objects/e000n50/e007n50/EDDK-Tower.xml", 1),
                new ExpectedEntity("VolvoFuel", 2),
                new ExpectedEntity("LSG", 2),
                new ExpectedEntity("Goldhofert", 1),
                new ExpectedEntity("Douglas", 1),
                new ExpectedEntity("Bravo", 1),
                new ExpectedEntity("747 KLM", 1),
                new ExpectedEntity("737-800 AB", 1),
                new ExpectedEntity("738", 1)
        );


        TestUtils.waitUntil(() -> {
            TestHelper.processAsync();
            sceneRunner.runLimitedFrames(1);
            List<EcsEntity> entities = SystemManager.findEntities((EntityFilter) null);
            log.debug("" + entities.size());
            if (EcsHelper.filterList(entities, e -> e.getName() != null && e.getName().contains("Goldhofert")).size() > 0) {
                int h = 9;
            }
            for (ExpectedEntity expectedEntity : expectedEntities) {
                if (!ExpectedEntity.contains(entities, expectedEntity)) {
                    log.debug("entity still missing: " + expectedEntity.name);
                    return false;
                }
            }
            // if nothing is missing we are complete
            return true;
        }, 60000);

        // 20.5.24 elevation 68.8 is the result of limited EDDK elevation provider (default elevation). But runway should have
        // correct elevation. Value differs slightly to TravelSceneBluebird!?
        // 20.7.24 And due to scenery origin Granada/Full? Was 70.60974991063463, now 71.31074
        TravelSceneTestHelper.validateTrafficCircuit(((TravelScene) sceneRunner.ascene).trafficCircuitForVisualizationOnly, 71.31074, 1.0, true);

        TravelSceneTestHelper.validateGroundnet();

        validateStaticEDDK(enableDoormarker);

        FGTileMgr fgTileMgr = FlightGearModuleScenery.getInstance().get_tile_mgr();
        // Not sure why it is 16 or 10? TODO explain why
        assertEquals(enableNavigator ? 16 : 10, fgTileMgr.getTileCacheContent().size());
        //TODO explain why 1 and 7?
        assertEquals(enableNavigator ? 7 : 1, SGOceanTile.created.size());

        if (enableNavigator) {
            // 2*3 for navigator, 2 for LSG, 1 for 738, position not tested.
            EcsTestHelper.assertTeleportComponent(TeleportComponent.getTeleportComponent(userEntity), 3 + 3 + 2 + 1, 8, null, "738");
            EcsEntity navigator = EcsHelper.findEntitiesByName("Navigator").get(0);
            assertNotNull(navigator);
            // from 'world-pois.xml' 10 are listed. 4 appears correct which is current EDDK navigator overview position, but who set index 4??
            EcsTestHelper.assertTeleportComponent(TeleportComponent.getTeleportComponent(navigator), 10, 4, null, null);
            TeleportComponent navigatorTeleportComponent = TeleportComponent.getTeleportComponent(navigator);
            assertEquals("Dahlem 1300", navigatorTeleportComponent.getPointLabel(3));
            assertEquals("EDDK Overview", navigatorTeleportComponent.getPointLabel(4));
            assertEquals("greenwich500", navigatorTeleportComponent.getPointLabel(8));

            if (worldTeleport) {
                // 25.5.24: teleporting to greenwich difficult with current TeleporterSystem workflow

                ((TeleporterSystem) SystemManager.findSystem(TeleporterSystem.TAG)).setActivetc(navigatorTeleportComponent);
                SystemManager.putRequest(UserSystem.buildTeleportRequest(userEntity.getId(), 4, "greenwich500"));

                TestUtils.waitUntil(() -> {
                    sceneRunner.runLimitedFrames(10);
                    sleepMs(100);
                    return SceneNode.findByName("Objects/w010n50/w001n51/London-Bridge.xml").size() > 0;
                }, 30000);
                // terminate here. The rest is tested by other tests.
                return;
            }
        } else {
            // without navigator we only have the eddk overview viewpoint
            EcsTestHelper.assertTeleportComponent(TeleportComponent.getTeleportComponent(userEntity), 1 + 2 + 1, 3, null, "738");
        }

        EcsEntity entity747 = EcsHelper.findEntitiesByName("747 KLM").get(0);
        assertNotNull(entity747);
        Vector3 pos747 = entity747.getSceneNode().getTransform().getPosition();
        // die Werte sind plausibel
        // TODO 3D ref values TestUtils.assertVector3(new Vector3(-1694.7482728026903, 1299.8451319338214, 0.0), pos747);

        // 'visualizeTrack' is enabled
        GraphVisualizationSystem graphVisualizationSystem = (GraphVisualizationSystem) SystemManager.findSystem(GraphVisualizationSystem.TAG);
        assertNotNull(graphVisualizationSystem);

        assertEquals(2, SceneNode.findByName("Scene Light").size());

        // start auto move. From now on its non deterministic

        // start service for 747
        GroundServicesSystem.requestService(entity747);
        sceneRunner.runLimitedFrames(10);

        // TODO validate graphVisualizationSystem

        // let vehicle move(?)
        SystemManager.putRequest(new Request(UserSystem.USER_REQUEST_AUTOMOVE, new Payload(new Object[]{null})));
        sceneRunner.runLimitedFrames(10);

        // load c172p
        Request request = RequestRegistry.buildLoadVehicle(UserSystem.getInitialUser().getId(), null, null, null, null);
        SystemManager.putRequest(request);
        AdvancedTestUtils.loadAndValidateNextVehicleSupposedToBeC172(sceneRunner);

        EcsEntity c172p = EcsHelper.findEntitiesByName("c172p").get(0);
        //log.debug(c172p.getSceneNode().dump(" ", 0));

        GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(c172p);
        assertEquals("groundnet.EDDK", gmc.getGraph().getName());
        // has the graph attached where it is located (groundnet)
        assertNotNull(gmc.getGraph());
        assertFalse(gmc.hasAutomove());
        assertNull(gmc.getPath());
        assertQuaternion(FgVehicleSpace.getFgVehicleForwardRotation(), gmc.getModelRotation());

        // start c172p and wait until it has a flight route (first will be move to runway)
        TravelSceneTestHelper.startAndValidateDefaultTrip(sceneRunner, c172p, true);
        TestUtils.waitUntil(() -> {
            sceneRunner.runLimitedFrames(2);
            sleepMs(10);
            return gmc.getPath() != null;
        }, 30000);

        assertTrue(gmc.hasAutomove());
        TravelSceneTestHelper.validateFgProperties(c172p, true);

    }

    /**
     * EHAM without need to load EDDK
     * // 21.6.25: EHAM, runway 06. Should also load SGOceanTile.
     * "false;false;false;geo:52.2878684, 4.73415315;57.8",
     */
    @Test
    public void testTravelSceneEHAMbyBasename() throws Exception {

        setup(false, false, "geo:52.2878684, 4.73415315", "57.8", "traffic-advanced:Travel-sphere.xml");
        Log log = Platform.getInstance().getLog(TravelSceneTest.class);

        assertEquals(INITIAL_FRAMES, sceneRunner.getFrameCount());
        SphereSystem sphereSystem = (SphereSystem) SystemManager.findSystem(SphereSystem.TAG);
        // world is set in SphereSystem.init()
        assertNotNull(sphereSystem.world);
        TravelSceneTestHelper.waitForSphereLoaded(sceneRunner);

        TravelSceneTestHelper.validateSphereProjections();

        String[] bundleNames = BundleRegistry.getBundleNames();
        // 7 appears correct, but without "Terrasync-model" its only 6.
        assertEquals(6, bundleNames.length);
        assertNotNull(BundleRegistry.getBundle("fgdatabasic"));

        sceneRunner.runLimitedFrames(50);

        EcsEntity userEntity = SystemManager.findEntities(e -> "Freds account name".equals(e.getName())).get(0);
        assertNotNull(userEntity, "userEntity");
        assertNotNull(userEntity.getName(), "name");

        // Independent from the location we should have all (GS) vehicles
        validateVehicles();

        //wait for nothing pending
        TestUtils.waitUntil(() -> {
            sceneRunner.runLimitedFrames(10);
            sleepMs(100);
            return AbstractSceneRunner.getInstance().futures.size() == 0;
        }, 30000);

        FGTileMgr fgTileMgr = FlightGearModuleScenery.getInstance().get_tile_mgr();
        // Not sure why it is 10, maybe 3x3 + EHAM?
        assertEquals(10, fgTileMgr.getTileCacheContent().size());
        // EHAM: 1 ocean tile appears correct with 3x3 tiles
        // 24.1.26 now it is 2(??)
        assertEquals(2, SGOceanTile.created.size());

        //??    EcsTestHelper.assertTeleportComponent(TeleportComponent.getTeleportComponent(userEntity), 1 + 2 + 1, 3, null, "??");

        // start c172p and wait until it has a flight route (first will be move to runway)
        /*TODO TravelSceneTestHelper.startAndValidateDefaultTrip(sceneRunner, c172p, true);
        TestUtils.waitUntil(() -> {
            sceneRunner.runLimitedFrames(2);
            sleepMs(10);
            return gmc.getPath() != null;
        }, 30000);

        assertTrue(gmc.hasAutomove());
        TravelSceneTestHelper.validateFgProperties(c172p, true);
*/
    }

    private void validateVehicles() {
        TrafficSystem trafficSystem = ((TrafficSystem) SystemManager.findSystem(TrafficSystem.TAG));

        // "GroundServices" vehicle list from TrafficWorld.xml
        List<Vehicle> vehiclelist = TrafficHelper.getVehicleListByDataprovider();
        assertEquals(8, vehiclelist.size(), "size of vehiclelist");

        VehicleDefinition/*Config*/ config = trafficSystem.getVehicleConfig("VolvoFuel", null);
        assertNotNull(config);

    }

    /**
     * Used for both (Flat)TravelScene tests for testing before any vehicle movement but after loading.
     * Auf die Reigenfolge der Tests achten. Zuerst die Basisdinge fuer anderes testen. Sonst hilft es nicht bei der Fehlersuche.
     */
    public static void validateStaticEDDK(boolean enableDoormarker) {


        EcsEntity entity747 = EcsHelper.findEntitiesByName("747 KLM").get(0);
        assertNotNull(entity747);
        EcsEntity entity738 = EcsHelper.findEntitiesByName("738").get(0);
        assertNotNull(entity738);
        EcsEntity entityLSG0 = EcsHelper.findEntitiesByName("LSG").get(0);
        assertNotNull(entityLSG0);

        List<NativeSceneNode> doormarkerList = SceneNode.findByName("localdoormarker");
        if (enableDoormarker) {
            // Why one?? And which one is it? Its not the service marker.
            assertEquals(1, doormarkerList.size(), "number of doormarker");
        } else {
            assertEquals(0, doormarkerList.size(), "number of doormarker");
        }
//TODO vehicle/aircraft config,

        // hat VehicleEntityBuilder gegriffen? Der legt GroundServiceComponent an.
        GroundServiceComponent gsc = GroundServiceComponent.getGroundServiceComponent(entityLSG0);
        assertNotNull(gsc);

    }

    /**
     * Needs parameter, so no @Before
     */
    private void setup(boolean enableDoormarker, boolean enableNavigator, String initialLocation, String initialHeading, String basename) throws Exception {
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("scene", "de.yard.threed.trafficadvanced.apps.TravelScene");
        properties.put("visualizeTrack", "true");
        properties.put("enableDoormarker", "" + enableDoormarker);
        properties.put("enableNavigator", "" + enableNavigator);
        if (initialLocation != null) {
            properties.put("initialLocation", initialLocation);
        }
        if (initialHeading != null) {
            properties.put("initialHeading", initialHeading);
        }
        if (basename != null) {
            properties.put("basename", basename);
        }
        //9.12.23 sceneRunner = TrafficTestUtils.setupForScene(INITIAL_FRAMES, ConfigurationByEnv.buildDefaultConfigurationWithEnv(properties));
        FgTestFactory.initPlatformForTest(properties, false, true, true, false, new AdvancedBundleResolverSetup());

        sceneRunner = (SceneRunnerForTesting) SceneRunnerForTesting.getInstance();
        sceneRunner.runLimitedFrames(INITIAL_FRAMES);
        log = Platform.getInstance().getLog(TravelSceneTest.class);
    }
}
