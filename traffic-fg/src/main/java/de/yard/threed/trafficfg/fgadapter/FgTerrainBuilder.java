package de.yard.threed.trafficfg.fgadapter;

import de.yard.threed.core.Degree;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.NumericValue;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.geometry.Rectangle;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.apps.ModelSamples;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.flightgear.FgBundleHelper;
import de.yard.threed.flightgear.FlightGearMain;
import de.yard.threed.flightgear.PositionInit;
import de.yard.threed.flightgear.SimpleBundleResourceProvider;
import de.yard.threed.flightgear.TerrainElevationProvider;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.FlightGearModuleScenery;
import de.yard.threed.flightgear.core.flightgear.scenery.FGTileMgr;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.graph.GraphNode;
import de.yard.threed.traffic.AbstractSceneryBuilder;
import de.yard.threed.traffic.EllipsoidCalculations;
import de.yard.threed.traffic.WorldGlobal;
import de.yard.threed.traffic.flight.FlightLocation;
import de.yard.threed.traffic.geodesy.MapProjection;
import de.yard.threed.trafficfg.FgCalculations;

import java.util.List;

/**
 * Wrapper for low level FGTileMgrScheduler, FGTileMgr and SceneryPager
 * Extracted from TravelScene and TerrainSystem.
 */
public class FgTerrainBuilder implements AbstractSceneryBuilder {
    Log logger = Platform.getInstance().getLog(FgTerrainBuilder.class);
    //range, so wie es von FG gelogged wurde.
    double range_m = 32000;
    FGTileMgr tilemgr;
    boolean fgenabled=true;
    boolean terrainonly = false;
    SceneNode world, earth;
    // simpleearth ist nur eine einzige Textur. Ansonsten FG Scenery.
    // flag was only for disabling FG private boolean simpleearth = true;;

    @Override
    public void init(SceneNode destinationNode) {
        this.world = destinationNode;

        FlightGearMain.initFG(new FlightLocation(WorldGlobal.equator020000, new Degree(0), new Degree(0)), null);
        FgBundleHelper.addProvider(new SimpleBundleResourceProvider("fgdatabasicmodel"));
        //4.1.18:TerraSync-model. Ob das hier gut ist?
        FgBundleHelper.addProvider(new SimpleBundleResourceProvider(FlightGear.getBucketBundleName("model")));
        // FG, Position ist initialisiert.

        // Following from TerrainSystem.init:

        //als Radius die Basis von SGGeod Meeresspiegel +1 nehmen. Kommt aber irgendwie zu hoch
        //earth = buildEarth(/*SGGeod.ERAD-10000*/WorldGlobal.EARTHRADIUS);
        earth = buildEarth(SGGeod.ERAD - 100000);
        world.attach(earth);


        if (/*FlightGear.inited FlightGearModuleScenery.inited &&*/ fgenabled) {
            FlightGearModuleScenery.init(terrainonly);
            tilemgr = FlightGearModuleScenery.getInstance().get_tile_mgr();
            tilemgr.init();
        }
        TerrainElevationProvider tep = new TerrainElevationProvider();
        tep.setWorld(world);
        //TODO 25.11.21: Wo kommt denn der her, der hier erst gelöscht werden muss?
        SystemManager.putDataProvider(SystemManager.DATAPROVIDERELEVATION, null);
        SystemManager.putDataProvider(SystemManager.DATAPROVIDERELEVATION, tep);

        //31.5.24: attach to world move from updateForPosition to here.
        //29.3.18: Das Umhaengen muss/sollte vielleich gar nicht sein. Wohl aber der ganze FGScenery
        //world.attach(FlightGearModuleScenery.getInstance().get_scenery().get_terrain_branch());
        world.attach(FlightGearModuleScenery.getInstance().get_scenery().get_scene_graph());
    }

    @Override
    public void updateForPosition(Vector3 position, Vector3 direction) {

        // 23.9.23 dont' care about direction/rotation any more.
        //LocalTransform newpos = (LocalTransform) evt.getPayloadByIndex(0);
        LocalTransform newpos = new LocalTransform(position,new Quaternion());
        FlightLocation fl = FlightLocation.fromPosRot(newpos);
        PositionInit.initPositionFromGeod(fl);

        logger.debug("updateForPosition: fgenabled=" + fgenabled + ",fginited=" + FlightGearModuleScenery.inited);

        if (!fgenabled || !FlightGearModuleScenery.inited)
            return;

      /*  if (gwloaded)
            return;
        gwloaded=true;*/

        // Muss 2-mal aufgerufen werden!
        tilemgr.schedule_tiles_at(SGGeod.fromGeoCoordinate(fl.coordinates)/*WorldGlobal.greenwichtilecenter*/, range_m);
        tilemgr.schedule_tiles_at(SGGeod.fromGeoCoordinate(fl.coordinates)/*WorldGlobal.greenwichtilecenter*/, range_m);
        tilemgr.update_queues(false);
        //31.5.24: attach to world moved to init()
    }

    @Override
    public Rectangle getLastTileSize() {
        return null;
    }

    @Override
    public void buildTerrain(Object p0, Object p1, MapProjection projection) {

    }

    @Override
    public SceneNode buildParkingNode(GraphNode n) {
        return null;
    }

    @Override
    public EllipsoidCalculations getEllipsoidCalculations() {
        return new FgCalculations();
    }

    public List<String> getLoadedBundles(){
        return FlightGearModuleScenery.getInstance().get_tile_mgr().getPager().loadedBundle;
    }

    public List<String> getFailedBundles(){
        return FlightGearModuleScenery.getInstance().get_tile_mgr().getPager().failedBundle;
    }

    /**
     * simpleearth ist nur eine einzige Textur.
     *
     * @return
     */
    private SceneNode buildEarth(double radius) {
        SceneNode m = null, model = new SceneNode();
        //30.3.18: Erdkugel unabhaengig vom Flag immer bauen
        if (/*simpleearth*/true) {
            m = ModelSamples.buildEarth(128, NumericValue.SMOOTH);
            m.getTransform().setScale(new Vector3(radius, radius, radius));
            // m.setScale(new Vector3(600, 600, 600));
            // Der Pazifik ist rechts auf pos x. Darum einmal rumdrehen, damit dort der
            // Äquator ist. Die y-Achse läuft durch die Pole. Dafür nochmal um 90 Grad.
            //m.object3d.rotateX(new Degree(-90));
            //m.object3d.rotateZ(new Degree(180));
            m = FlightLocation.rotateFromYupToFgWorld(m);
        }
        model.attach(m);
        model.setName("earthcontainer");
        return model;
    }

}
