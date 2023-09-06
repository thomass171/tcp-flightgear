package de.yard.threed.flightgear;


import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.core.Vector3;
import de.yard.threed.flightgear.core.FlightGearModuleScenery;
import de.yard.threed.traffic.geodesy.ElevationProvider;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.core.platform.Log;

/**
 * 9.3.21: MA31 Wohin damit?
 *
 * Created on 28.03.18.
 */
public class TerrainElevationProvider implements de.yard.threed.engine.ecs.DataProvider, ElevationProvider {
    Log logger = Platform.getInstance().getLog(TerrainElevationProvider.class);
    Double altitude = null;
    private SceneNode world;

    public TerrainElevationProvider() {
    }

    public TerrainElevationProvider(Double altitude) {
        if (altitude != null) {
            this.altitude = altitude;
        }
    }

    @Override
    public Object getData(Object[] parameter) {
        if (altitude != null) {
            return altitude;
        }
        SGGeod coor = (SGGeod) parameter[0];
        Double elevation = FlightGearModuleScenery.getInstance().get_scenery().get_elevation_m(coor,(world!=null)?world.getTransform().getPosition():new Vector3());
        logger.debug("elevation " + elevation + " found for " + coor+", world="+world);
        return elevation;
    }

    /**
     * Ein ElevationProvider, der immer eine feste Elevation liefert
     *
     * @return
     */
    public static TerrainElevationProvider buildForStaticAltitude(double altitude) {
        return new TerrainElevationProvider(altitude);
    }

    @Override
    public Double getElevation(double latitudedeg, double longitudedeg/*SGGeod coor*/) {
        Double elevation = (Double)getData(new Object[]{SGGeod.fromDeg(longitudedeg,latitudedeg)});
        if (elevation==null){
            // null zurueckzuliefern bringt ja nichts. 2.11.19: Doch, dann kann der Aufrufer needsUpdate setzen.
            return null;
        }
        double d = (double)elevation;
        return d;
    }

    public void setWorld(SceneNode world) {
        this.world = world;
    }

    public double getAltitude() {
        return (double)altitude;
    }
}
