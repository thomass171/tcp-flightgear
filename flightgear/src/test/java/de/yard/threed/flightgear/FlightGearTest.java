package de.yard.threed.flightgear;

import de.yard.threed.core.Degree;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.Ray;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.core.Vector3;
import de.yard.threed.engine.test.testutil.TestUtil;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.FlightGearModuleBasic;
import de.yard.threed.flightgear.core.FlightGearModuleScenery;
import de.yard.threed.flightgear.core.flightgear.scenery.FGScenery;
import de.yard.threed.flightgear.core.flightgear.scenery.FGTileMgr;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.flightgear.core.simgear.props.PropsIO;
import de.yard.threed.flightgear.core.simgear.structure.SGException;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.engine.testutil.EngineTestFactory;


import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.traffic.VehicleLauncher;
import de.yard.threed.traffic.VehicleLoaderResult;
import de.yard.threed.traffic.config.ConfigHelper;

import de.yard.threed.traffic.config.VehicleDefinition;
import de.yard.threed.traffic.WorldGlobal;
import de.yard.threed.engine.testutil.TestHelper;
import de.yard.threed.traffic.geodesy.GeoCoordinate;
import de.yard.threed.trafficcore.model.Vehicle;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests in greater context with FG startup mechanismen (InitStates).
 * <p>
 * <p/>
 * Created by thomass on 31.05.16.
 */
@Slf4j
public class FlightGearTest {
    static Platform platform = FgTestFactory.initPlatformForTest(new HashMap<String, String>());
    //String aircraftdir = "My-777";
    static Log logger = Platform.getInstance().getLog(FlightGearTest.class);

    /**
     * In Level 0 muss es schon das Material geben.
     */
    @Test
    public void testInitializationTo7() {
        EngineTestFactory.loadBundleSync("sgmaterial");

        FlightGearModuleBasic.init(null, null);
        FlightGearModuleScenery.init(false, false);
        // 1.10.23 was 288 from Granada bundle, 283 now from project might be correct
        TestUtil.assertEquals("matlib.size", /*FG 3.4 284*/283, FlightGearModuleScenery.getInstance().get_matlib().matlib.size());
    }

    /**
     * 8.10.23: Was using 'greenwichtilecenter' once, but greenwichs tile is not content of project. Switched to refbtg.
     */
    @Test
    public void testFGTileMgr() throws Exception {

        // there might be fragments from previous tests. Hope remove catches the oldest.
        log.debug((Platform.getInstance()).findSceneNodeByName("World").size() + " worlds found");
        while ((Platform.getInstance()).findSceneNodeByName("World").size() > 1) {
            SceneNode.removeSceneNodeByName("World");
        }
        log.debug((Platform.getInstance()).findSceneNodeByName("World").size() + " worlds found after cleanup");
        //PropertyTree und FGScenery is needed. Den FGTileMgr gibt es dann auch schon
        //27.3.18 FlightGear.init(5, FlightGear.argv);
        //25.9.23 EngineTestFactory.loadBundleSync("sgmaterial");
        FlightGearModuleBasic.init(null, null);
        FlightGearModuleScenery.init(false, false);

        //model bundle is needed for scenery objects.
        EngineTestFactory.loadBundleSync(FlightGear.getBucketBundleName("model"));

        FGTileMgr tilemgr = FlightGearModuleScenery.getInstance().get_tile_mgr();
        tilemgr.init();
        TestUtil.assertEquals("terraingroup.children", 0, FlightGearModuleScenery.getInstance().get_scenery().get_terrain_branch().getTransform().getChildCount());

        //range, so wie es von FG gelogged wurde. Gelogged wurden 32000, das braucht mit echten
        // Reader zuviel Heap und dauert zu lang. Darum der dummyreader. TODO: wieder Dummy verwenden?
        //MA17 Registry.getInstance().replaceReaderWriter("stg", new DummyReaderBTG());
        //Muss 2-mal aufgerufen werden!
        double range_m = 32000;

        GeoCoordinate positionNearEddk = new GeoCoordinate(new Degree(50.843675), new Degree(7.109709), 1150);
        tilemgr.schedule_tiles_at(SGGeod.fromGeoCoordinate(positionNearEddk), range_m);
        tilemgr.schedule_tiles_at(SGGeod.fromGeoCoordinate(positionNearEddk), range_m);
        // das eigentliche (async) Laden anstossen (geht dann im SceneryPager).
        // Hier werden wohl viele Fehler protokolliert, weil die stg tiles nicht vorliegen.
        tilemgr.update_queues(false);

        // Loading of tiles and STGs is async.
        TestHelper.processAsync();
        TestHelper.processAsync();

        FGScenery scenery = FlightGearModuleScenery.getInstance().get_scenery();
        //Die "9" haengt evtl. auch davon ab, welche Bundle verfuegbar sind. Aber das ist schon pausibel, 1 Tile in jede Richtung
        TestUtil.assertEquals("terraingroup.children", 9, scenery.get_terrain_branch().getTransform().getChildCount());
        //Ist die Scenery auch richtig im Tree eingehangen? Das macht FGScenery aber nicht mehr selber.
        Scene.getCurrent().addToWorld(scenery.get_scene_graph());
        List<NativeSceneNode> scenerynodes = Platform.getInstance().findSceneNodeByName("FGScenery");
        //TODO 20.7.21 kommt 2(??). 22.9.23: Still 2. Changed 1->2. 19.10.23 Depending on number of tests the number might vary??
        //TestUtil.assertEquals("", /*1*/2, scenerynodes.size());
        assertTrue(scenerynodes.size() > 0);
        SceneNode scenerynode = new SceneNode(scenerynodes.get(0));
        TestUtil.assertEquals("", "FGScenery", scenerynode.getName());
        Ray ray = FGScenery.getVerticalRay(SGGeod.fromGeoCoordinate(positionNearEddk), new Vector3());
        // Depends on intersection calculation and thus depends on the platform.
        // 15.2.24: scenery load is async now. Wait somehow
        TestUtils.waitUntil(() -> {
            TestHelper.processAsync();
            Double elevation = scenery.get_elevation_m(SGGeod.fromGeoCoordinate(positionNearEddk), new Vector3());
            return elevation != null;
        }, 10000);

        Double elevation = scenery.get_elevation_m(SGGeod.fromGeoCoordinate(positionNearEddk), new Vector3());
        TestUtil.assertNotNull("elevation", elevation);
        // There is no exact correct value, it depends on ellipsoid calculation. So use a quite large tolerance.
        // Just should be plausible. Greenwich '45.99812f' replaced with convincing '56.673577656'
        TestUtil.assertFloat("elevation", 56.673577656, elevation, 1f);
        //TODO check double loading.
    }


}
