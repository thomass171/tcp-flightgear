package de.yard.threed.trafficfg.flight;

import de.yard.threed.core.Color;
import de.yard.threed.core.Degree;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.geometry.Primitives;
import de.yard.threed.core.geometry.Rectangle;
import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Mesh;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.graph.Graph;
import de.yard.threed.graph.GraphEdge;
import de.yard.threed.graph.GraphNode;
import de.yard.threed.graph.GraphOrientation;
import de.yard.threed.graph.GraphVisualizer;
import de.yard.threed.traffic.AbstractSceneryBuilder;
import de.yard.threed.trafficcore.EllipsoidCalculations;
import de.yard.threed.traffic.GraphTerrainVisualizer;
import de.yard.threed.core.GeoCoordinate;
import de.yard.threed.trafficcore.geodesy.MapProjection;
import de.yard.threed.trafficcore.model.Runway;
import de.yard.threed.trafficfg.config.AirportConfig;

/**
 * Extrahiert aus GraphTerrainSystem
 * 27.12.21
 */
public class GroundNetTerrainBuilder implements AbstractSceneryBuilder {

    @Override
    public void init(SceneNode destinationNode) {

    }


    @Override
    public void updateForPosition(LatLon position) {

    }

    @Override
    public Rectangle getLastTileSize() {
        return null;
    }

    @Override
    public void buildTerrain(Object p0, Object p1, MapProjection projection){
        GroundNet gn = (GroundNet) p0;//evt.getPayloadByIndex(0);
        AirportConfig airport = (AirportConfig) p1;//evt.getPayloadByIndex(1);
        GraphVisualizer visualizer = new GraphTerrainVisualizer(/*gn.groundnetgraph, scene*/this);

        visualizer.visualize/*Graph*/(gn.groundnetgraph.getBaseGraph(), Scene.getCurrent().getWorld());
        for (Runway runway : /*TrafficWorld2D.*/airport.getRunways()) {
            //Runway Visualisierung. Also Q&D einfach aus einem schlichtem tmp Graph mit einer edge.
            //MapProjection projection = groundnet.projection;
            Vector2 from = projection.project(/*11.11.21SGGeod.fromLatLon*/(runway.getFrom()));
            Vector2 to = projection.project(GeoCoordinate.fromLatLon(runway.getTo(),0));

            Graph graph = new Graph(GraphOrientation.buildForZ0());
            //graph.iszEbene = true;
            // graph.upVector = new Vector3(0, 0, 1);

            GraphNode start = graph.addNode("start", new Vector3(from.getX(), from.getY(), 0));
            GraphNode end = graph.addNode("end", new Vector3(to.getX(), to.getY(), 0));

            GraphEdge edge = graph.connectNodes(start, end);
            //8.3.21: GraphTerrainVisualizer wird hier gebraucht. Da sind die Ableitungen im Visualizer nicht passend genug.
            if (visualizer instanceof GraphTerrainVisualizer) {
                SceneNode sn = ((GraphTerrainVisualizer) visualizer).buildEdge(edge, Color.GREEN, 10, 0, gn.groundnetgraph.getBaseGraph().getGraphOrientation());
                //verdeckt graph? Nö.
                Scene.getCurrent().addToWorld(sn);
            } else {
                Util.notyet();
            }
        }
    }

    @Override
    public SceneNode buildParkingNode(GraphNode n) {
        if (n.customdata != null && n.customdata instanceof Parking) {
            //10.11.2017: die virtualheight rausnehmen
            //15.2.18: TODO projection
            Vector3 loc = new Vector3(n.getLocation().getX(), n.getLocation().getY(), 0);
            SimpleGeometry geo = Primitives.buildPlaneGeometry(10, 10, 1, 1);
            Material mat;
            mat = Material.buildLambertMaterial(Color.ORANGE);
            SceneNode sn = new SceneNode(new Mesh(geo, mat));
            sn.getTransform().rotateX(new Degree(90));
            sn.getTransform().setPosition(loc);
            Parking parking = ((Parking) n.customdata);
            sn.setName("Parkpos " + parking.name + "(" + n.getName() + ")");

            return sn;
        }
        return null;
    }

    @Override
    public EllipsoidCalculations getEllipsoidCalculations() {
        Util.notyet();
        return null;
    }

    @Override
    public Double getElevation(LatLon position) {
        Util.notyet();
        return null;
    }
}
