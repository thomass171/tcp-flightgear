package de.yard.threed.flightgear;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
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


import de.yard.threed.flightgear.testutil.FgFullTestFactory;
import de.yard.threed.traffic.VehicleLauncher;
import de.yard.threed.traffic.VehicleLoaderResult;
import de.yard.threed.traffic.config.ConfigHelper;

import de.yard.threed.traffic.config.VehicleConfig;
import de.yard.threed.traffic.WorldGlobal;
import de.yard.threed.engine.testutil.TestHelper;
import de.yard.threed.trafficcore.model.Vehicle;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

/**
 * Tests in greater context with FG startup mechanismen (InitStates).
 * <p>
 * <p/>
 * Created by thomass on 31.05.16.
 */
public class FlightGearTest {
    static Platform platform = FgFullTestFactory.initPlatformForTest(new HashMap<String, String>());
    //String aircraftdir = "My-777";
    static Log logger = Platform.getInstance().getLog(FlightGearTest.class);
    
    /**
     * In Level 0 muss es schon das Material geben.
     */
    @Test
    public void testInitializationTo7() {
        EngineTestFactory.loadBundleSync("sgmaterial");

        FlightGearModuleBasic.init(null, null);
        FlightGearModuleScenery.init(false);
        TestUtil.assertEquals("matlib.size", /*FG 3.4 284*/288, FlightGearModuleScenery.getInstance().get_matlib().matlib.size());
    }
    

    @Test
    public void testFGTileMgr() {
        //PropertyTree und FGScenery is needed. Den FGTileMgr gibt es dann auch schon
        //27.3.18 FlightGear.init(5, FlightGear.argv);
        //25.9.23 EngineTestFactory.loadBundleSync("sgmaterial");
        FlightGearModuleBasic.init(null, null);
         FlightGearModuleScenery.init(false);
         
        //model bundle is needed for scenery objects.
        EngineTestFactory.loadBundleSync(FlightGear.getBucketBundleName("model"));

        FGTileMgr tilemgr = FlightGearModuleScenery.getInstance().get_tile_mgr();
        tilemgr.init();
        TestUtil.assertEquals("terraingroup.children", 0, FlightGearModuleScenery.getInstance().get_scenery().get_terrain_branch().getTransform().getChildCount());

        //center des tile von Greenwich und range, so wie es von FG gelogged wurde. Gelogged wurden 32000, das braucht mit echten
        // Reader zuviel Heap und dauert zu lang. Darum der dummyreader. TODO: wieder Dummy verwenden?
        //MA17 Registry.getInstance().replaceReaderWriter("stg", new DummyReaderBTG());
        //Muss 2-mal aufgerufen werden!
        double range_m = 32000;
        tilemgr.schedule_tiles_at(SGGeod.fromGeoCoordinate(WorldGlobal.greenwichtilecenter), range_m);
        tilemgr.schedule_tiles_at(SGGeod.fromGeoCoordinate(WorldGlobal.greenwichtilecenter), range_m);
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
        //TODO 20.7.21 kommt 2(??). 22.9.23: Still 2. Changed 1->2
        TestUtil.assertEquals("", /*1*/2, scenerynodes.size());
        SceneNode scenerynode = new SceneNode(scenerynodes.get(0));
        TestUtil.assertEquals("", "FGScenery", scenerynode.getName());
        Ray ray = FGScenery.getVerticalRay(SGGeod.fromGeoCoordinate(WorldGlobal.greenwichtilecenter),new Vector3());
        // Depends on intersection calculation and thus depends on the platform
        Double elevation = scenery.get_elevation_m(SGGeod.fromGeoCoordinate(WorldGlobal.greenwichtilecenter),new Vector3());
        TestUtil.assertNotNull("elevation", elevation);
        // There is no exact correct value, it depends on ellipsoid calculation. So use a quite large tolerance.
        // Just should be plausible.
        TestUtil.assertFloat("elevation", 45.99812f, elevation, 1f);
        //TODO check double loading.
    }


}
