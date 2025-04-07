package de.yard.threed.flightgear;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.RuntimeTestUtil;
import de.yard.threed.engine.Ray;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.flightgear.core.FlightGearModuleBasic;
import de.yard.threed.flightgear.core.FlightGearModuleScenery;

import de.yard.threed.traffic.flight.FlightLocation;

import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeCollision;
import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.core.GeoCoordinate;

import java.util.List;

import static de.yard.threed.traffic.WorldGlobal.eddkoverview;
import static de.yard.threed.traffic.WorldGlobal.fleddkoverview;

/**
 * Alternative/Successor/Extension to class FlightGear.
 *
 * 11.8.2020
 */
public class FlightGearMain {
    private static Log logger = Platform.getInstance().getLog(FlightGearMain.class);

     /**
     * Die FG Komponenten initen bzw. einhaengen.  Nachbildung des FlightGear.init(7);
     * Die Reihenfolge orientiert sich an FG fgIdleFunction()
     * System swerden hier nocht nicht angelegt.
     * 27.6.17: Die 777 ist eigentlich selbstgeladen, nicht über FG. FG ist "nur" fuer die Scenery. Wird die 777 hier auch geladen, doppelt? Nein, nicht mehr.
     * 21.10.17: Deperectaed, weil AircraftResourceProvider jetzt konfigurierbar ist und FG eh nicht 1:1 migriert wird. Aber hier wird es vorerst noch gebraucht.
     * 25.3.18: Trotz deprecated zum Decoupling aufgeteilt in ein BasicInit und SceneryInit. Praktischerweise auch in Tests verwendet, darum vorerst doch nicht deprecated.
     * Dies dient erstmal als Ersatz fuer die FG init state loop fgIdleFunction.
     * 11.08.2020: Moved from TravelScene to here
     */
    //@Deprecated
    public static void initFG(FlightLocation loc, String aircraftdir) {
        FlightGearModuleBasic.init(loc, aircraftdir);
        //jetzt in TerrainSystem FlightGearModuleScenery.init(loc, aircraftdir);
    }

    /**
     * Tests zur Laufzeit analog zu ReferenceScene. Auch nutzbar aus FlightScene, darum static. FlöightScene hat besser eigene.
     * Sollten vor allem FG relevante Tests ein. Einfache können ja nach wie vor nach ReferenceScene.
     *
     * 10.8.20: Die waren mal in SceneryViewerScene und werden auch von da verwendet (und TravelScene). Nach Lösung der Depenencies zurück
     * nach FG.
     */
    public static void runFlightgearTests(Vector3 worldadjustment) {
        logger.info("Running tests");
        //Ist die Scenery auch richtig im Tree eingehangen?
        List<NativeSceneNode> scenerynodes = Platform.getInstance().findSceneNodeByName("FGScenery");
        RuntimeTestUtil.assertEquals("", 1, scenerynodes.size());
        SceneNode scenerynode = new SceneNode(scenerynodes.get(0));
        RuntimeTestUtil.assertEquals("", "FGScenery", scenerynode.getName());
        scenerynodes = Platform.getInstance().findSceneNodeByName("pagedObjectLOD3072816");
        RuntimeTestUtil.assertEquals("pagedObjectLOD3072816", 1, scenerynodes.size());
        //TODO haengt das Tile richtig im Baum? Wobei es hier ja gar nicht im Scenery Baum haengt.

        // Einen Ray ins Center probieren.
        //Das ist mal auf den EDDK Tile ausgelegt. Da muss er einfach was finden.
        SGGeod geodStart = new SGGeod(/*WorldGlobal.*/fleddkoverview.coordinates.getLonRad(), /*WorldGlobal.*/fleddkoverview.coordinates.getLatRad(), 10000);
        Vector3 eddktoppos = geodStart.toCart();
        Ray ray = new Ray(eddktoppos.add(worldadjustment), eddktoppos.negate());
        for (NativeCollision intersection : ray.getIntersections()) {
            Vector3 p = (intersection.getPoint());
            p = p.subtract(worldadjustment);
            logger.debug("found intersection for custom ray from EDDK top pos:" + p + ", coor=" + SGGeod.fromCart(p)+", hit node:"+((intersection.getSceneNode()!=null)?new SceneNode(intersection.getSceneNode()).getPath():"null"));
        }
        // Terrain solle er einmal finden
        for (NativeCollision intersection : ray.getIntersections(FlightGearModuleScenery.getInstance().get_scenery().get_terrain_branch(),true)) {
            Vector3 p = (intersection.getPoint());
            logger.debug("found terrain intersection for custom ray:" + p + ", coor=" + SGGeod.fromCart(p));
        }

        // Fuer einen Referenzpunkt in EDDK die Elevation ermitteln. Das muesste dann ja auch gehen.
        double elevation = (double)FlightGearModuleScenery.getInstance().get_scenery().get_elevation_m(SGGeod.fromGeoCoordinate(/*WorldGlobal.*/fleddkoverview.coordinates),worldadjustment);
        logger.debug("Elevation of eddkoverview: " + elevation);
        //Since double 70.51 instead of 71.16? 31.5.24: Now back to 71.15, no idea why.4.6.24 back again, 20.6.24 back
        RuntimeTestUtil.assertFloat("Elevation EDDK overview", 71.15, elevation, 0.3);


        logger.info("Tests completed");
    }


}
