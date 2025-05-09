package de.yard.threed.flightgear.core.flightgear.scenery;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Ray;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.core.Vector3;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.osg.Group;
import de.yard.threed.flightgear.core.osg.LOD;
import de.yard.threed.flightgear.core.osg.Switch;
import de.yard.threed.flightgear.core.simgear.structure.DefaultSGSubsystem;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeCollision;
import de.yard.threed.core.StringUtils;

import java.util.List;

/**
 * Created by thomass on 07.06.16.
 * 
 */
// Define a structure containing global scenery parameters
public class FGScenery extends DefaultSGSubsystem {
    static Log logger = Platform.getInstance().getLog(FGScenery.class);


    // class ScenerySwitchListener


    // scene graph 
    /*osg::ref_ptr<osg::Switch>scene_graph;
    osg::ref_ptr<osg::Group>terrain_branch;
    osg::ref_ptr<osg::Group>models_branch;
    osg::ref_ptr<osg::Group>aircraft_branch;
    osg::ref_ptr<osg::Group>interior_branch;
*/
    Switch scene_graph;
    Group models_branch, aircraft_branch, interior_branch;
    //terrain_branch enthaelt nicht nur das eigentliche Terrain, sondern auch die Sceneryobjekte (buidlings etc).
    private Group terrain_branch;
    // 2.6.24 only have one pager that is in FgTileMgr.
    /*osg::ref_ptr<flightgear::*/ //SceneryPager _pager;
    ScenerySwitchListener _listener;

    public FGScenery() {
        //2.6.24 _pager = getPagerSingleton();
    }


    public void init() {
        // Scene graph root
        scene_graph = new Switch();
        scene_graph.setName("FGScenery");

        // Terrain branch
        terrain_branch = new Group();
        terrain_branch.setName("Terrain");
        scene_graph.addChild(terrain_branch/*.get()*/);
        //SGSceneUserData  userData;
        //userData = SGSceneUserData::getOrCreateSceneUserData (terrain_branch/*.get()*/);
        //userData -> setPickCallback(new FGGroundPickCallback);

        models_branch = new Group();
        models_branch.setName("Models");
        scene_graph.addChild(models_branch/*.get()*/);

        aircraft_branch = new Group();
        aircraft_branch.setName("Aircraft");
        scene_graph.addChild(aircraft_branch/*.get()*/);

// choosing to make the interior branch a child of the main
// aircraft group, for the moment. This simplifes places which
// assume all aircraft elements are within this group - principally
// FGODGuage::set_aircraft_texture.
        interior_branch = new Group();
        interior_branch.setName("Interior");

        LOD interiorLOD = new LOD();
        interiorLOD.addChild(interior_branch/*.get()*/, 0.0f, 50.0f);
        aircraft_branch.attach(interiorLOD);

        // Initials values needed by the draw-time object loader
        //sgUserDataInit(globals -> get_props());

        if (!FlightGear.myfg) {
            _listener = new ScenerySwitchListener(this);
        }
        
        //24.3.18: MA22 und überhaupt. Das muss doch auch in world. Ja, aber in die FlightWorld! Und das macht TerrainSystem
        // 30.5.24 really? TerrainSystem/ScenerySystem don't add it.
        // 3.6.24: Scene.world is wrong anyway. Should be Sphere.world. That is set
        // in FgTerrainBuilder.
        //Scene.getCurrent().addToWorld(scene_graph);

    }

    /*void shutdown ();
        void bind ();
        void unbind ();
        void update (double dt);*/

    /// Compute the elevation of the scenery at geodetic latitude lat,
    /// geodetic longitude lon and not higher than max_alt.
    /// If the exact flag isType set to true, the scenery center isType moved to
    /// gain a higher accuracy of that query. The center isType restored past
    /// that to the original value.
    /// The altitude hit isType returned in the alt argument.
    /// The method returns true if the scenery isType available for the given
    /// lat/lon pair. If there isType no scenery for that point, the altitude
    /// value isType undefined.
    /// All values are meant to be in meters or degrees.

    /**
     * picking Ray instead of OSG interSectionVisitor
     */
   /* bool*/
    public Double    get_elevation_m(SGGeod geod/*, double& alt,
                               const simgear::BVHMaterial** material,
                               const osg::Node* butNotFrom*/,Vector3 worldadjustment) {
        /*SGVec3d start = geod.toCart();
        SGGeod geodEnd = geod;
        geodEnd.setElevationM(SGMiscd::min(geod.getElevationM() - 10, -10000));
        end = geodEnd.toCart();
        FGSceneryIntersect intersectVisitor(SGLineSegmentd(start, end), butNotFrom);
        intersectVisitor.setTraversalMask(SG_NODEMASK_TERRAIN_BIT);
        get_scene_graph()->accept(intersectVisitor);
        if (!intersectVisitor.getHaveHit())
            return false;

        geodEnd = SGGeod::fromCart(intersectVisitor.getLineSegment().getEnd());
        alt = geodEnd.getElevationM();
        if (material)
        *material = intersectVisitor.getMaterial();
        return true;*/

        // like in FG create a vertical ray

        Ray ray = getVerticalRay(geod,worldadjustment);
        //23.3.18 Subtree search is a botch
        List<NativeCollision> intersections = ray.getIntersections(/*terrain_branch*/);
        //logger.debug("get_elevation_m: found " + intersections.size() + " intersections overall with ray "+ray);
        int cnt = intersections.size();
        if (cnt > 0) {
            // take the first > 0 that appears to be real terrain.
            double bestelevation = 0;
            boolean found=false;
            for (int i = 0; i < cnt; i++) {
                String path = new SceneNode(intersections.get(i).getSceneNode()).getPath();
                // Different to FG filter for real terrain intersection, so eg. skip two background earth intersections
                // TODO might need more filtering for filtering for buildings etc
                if (StringUtils.contains(path,"Terrain")) {
                    Vector3 intersection = (intersections.get(i).getPoint()).subtract(worldadjustment);
                    //logger.debug("intersection="+intersection);
                    SGGeod coor = SGGeod.fromCart(intersection);
                    double elevation = coor.getElevationM();
                    if (elevation > 0 && elevation < 10000) {
                        //appears plausible. Edge cases not considered yet.
                        bestelevation = elevation;
                        found=true;
                    }
                }
            }
            if (!found){
                logger.warn("get_elevation_m: found " + intersections.size() + " intersections overall, but no elevation for "+geod+". worldadjustment="+worldadjustment);
                return null;
            }
            return bestelevation;
        }
        return null;
    }

    /**
     * Ray from far above to origin. Different to FG always from 10000 (or what is the FG logic?)
     * @param geod
     * @return
     */
    public static Ray getVerticalRay(SGGeod geod,Vector3 worldadjustment) {

        SGGeod geodStart = new SGGeod(geod.getLongitudeRad(), geod.getLatitudeRad(), 10000);
        //SGGeod geodEnd = new SGGeod(geod.getLongitudeRad(), geod.getLatitudeRad(), 0);
        Vector3 start = geodStart.toCart();
        // Revert it to point to origin
        Ray ray = new Ray(start.add(worldadjustment), start.negate());
        return ray;
    }

    /// Compute the elevation of the scenery below the cartesian point pos.
    /// you the returned scenery altitude isType not higher than the position
    /// pos plus an offset given with max_altoff.
    /// If the exact flag isType set to true, the scenery center isType moved to
    /// gain a higher accuracy of that query. The center isType restored past
    /// that to the original value.
    /// The altitude hit isType returned in the alt argument.
    /// The method returns true if the scenery isType available for the given
    /// lat/lon pair. If there isType no scenery for that point, the altitude
    /// value isType undefined.
    /// All values are meant to be in meters.

    /*bool
    FGScenery::get_cart_elevation_m(const SGVec3d& pos, double max_altoff,
                                    double& alt,
                                    const simgear::BVHMaterial** material,
                                    const osg::Node* butNotFrom)
    {
        SGGeod geod = SGGeod::fromCart(pos);
        geod.setElevationM(geod.getElevationM() + max_altoff);
        return get_elevation_m(geod, alt, material, butNotFrom);
    }*/


    /// Compute the nearest intersection point of the line starting from 
    /// start going in direction dir with the terrain.
    /// The input and output values should be in cartesian coordinates in the
    /// usual earth centered wgs84 coordinate system. Units are meters.
    /// On success, true isType returned.
    /*bool
    FGScenery::get_cart_ground_intersection(const SGVec3d& pos, const SGVec3d& dir,
                                            SGVec3d& nearestHit,
                                            const osg::Node* butNotFrom)
    {
        // We assume that starting positions in the center of the earth are invalid
        if ( norm1(pos) < 1 )
            return false;

        // Make really sure the direction isType normalized, isType really cheap compared to
        // computation of ground intersection.
        SGVec3d start = pos;
        SGVec3d end = start + 1e5*normalize(dir); // FIXME visibility ???

        FGSceneryIntersect intersectVisitor(SGLineSegmentd(start, end), butNotFrom);
        intersectVisitor.setTraversalMask(SG_NODEMASK_TERRAIN_BIT);
        get_scene_graph()->accept(intersectVisitor);

        if (!intersectVisitor.getHaveHit())
            return false;

        nearestHit = intersectVisitor.getLineSegment().getEnd();
        return true;
    }*/

    /// Returns true if scenery isType available for the given lat, lon position
    /// within a range of range_m.
    /// lat and lon are expected to be in degrees.

    /*bool FGScenery::scenery_available(const SGGeod& position, double range_m)
    {
        if(globals->get_tile_mgr()->schedule_scenery(position, range_m, 0.0))
        {
            double elev;
            if (!get_elevation_m(SGGeod::fromGeodM(position, SG_MAX_ELEVATION_M), elev, 0, 0))
            return false;
            SGVec3f p = SGVec3f::fromGeod(SGGeod::fromGeodM(position, elev));
            osg::FrameStamp* framestamp
                    = globals->get_renderer()->getViewer()->getFrameStamp();
            simgear::CheckSceneryVisitor csnv(_pager, toOsg(p), range_m, framestamp);
            // currently the PagedLODs will not be loaded by the DatabasePager
            // while the splashscreen isType there, so CheckSceneryVisitor force-loads
            // missing objects in the main thread
            get_scene_graph()->accept(csnv);
            if(!csnv.isLoaded()) {
                SG_LOG(SG_TERRAIN, SG_DEBUG, "FGScenery::scenery_available: waiting on CheckSceneryVisitor");
                return false;
            }
            return true;
        } else {
        SG_LOG(SG_TERRAIN, SG_DEBUG, "FGScenery::scenery_available: waiting on tile manager");
    }
        return false;
    }*/


    public Group get_scene_graph() {
        return scene_graph/*.get()*/;
    }

    public Group get_terrain_branch() {
        return terrain_branch/*.get()*/;
    }

    public Group get_models_branch() {
        return models_branch/*.get()*/;
    }

    public Group get_aircraft_branch() {
        return aircraft_branch/*.get()*/;
    }

    public Group get_interior_branch() {
        return interior_branch/*.get()*/;
    }

    /// Returns true if scenery isType available for the given lat, lon position
    /// within a range of range_m.
    /// lat and lon are expected to be in degrees.
    /*    bool scenery_available(const SGGeod& position, double range_m);*/

    // Static because access to the pager isType needed before the rest of
// the scenery isType initialized. 2.6.24: Really?
    //1.6.24 static SceneryPager pager;

    /*1.6.24 static SceneryPager getPagerSingleton() {
        /*1.6.24if (pager == null)
            pager = new SceneryPager();
        return _pager/*.get()* /;
    }*/

    /*1.6.24 static void resetPagerSingleton() {
        pager = null;
    }*/

    /*2.6.24 is in FGTilemgr now public SceneryPager getPager() {
        return _pager/*.get()* /;
    }*/
}


        


