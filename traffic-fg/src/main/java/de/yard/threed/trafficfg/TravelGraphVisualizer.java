package de.yard.threed.trafficfg;


import de.yard.threed.core.Color;
import de.yard.threed.core.Degree;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.geometry.Primitives;
import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Mesh;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.graph.DefaultGraphVisualizer;
import de.yard.threed.graph.Graph;
import de.yard.threed.graph.GraphEdge;
import de.yard.threed.graph.GraphNode;
import de.yard.threed.graph.GraphPath;
import de.yard.threed.trafficcore.geodesy.MapProjection;
import de.yard.threed.traffic.osm.TerrainBuilder;
import de.yard.threed.trafficfg.flight.Parking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wird auch von osmScene verwendet, aber etwas andere Optionen.
 * Kann bestimmt zu einem TrafficGraphVisalizer o.ä. ausgebaut werden.
 * <p>
 * 25.2.18: Fuer die Grundedges GraphTerrainVisualizer extrahiert. Damit
 * ist das hier eigentlich nur noch ein GraphPathVisualizer, wobei offen ist,
 * wer andere Layer visualisiert. Die Grundvisualisierung des Graph nehme ich hier
 * auf jeden Fall erstmal raus. 9.12.21: Der buildEdgeArea() ist für ein schmales blaues Band drin.
 * 27.2.18: Hier ist auch die Projection, die aus einem 3D Path eine 2D Projektion macht.
 * 07.10.21:Eigentlich ist das doch ein groundnet/route graph visualizer, der den Graph und zusätzlich sowas wie Parkpos anzeigt? Irgendwie
 * recht vielseitig. Darum umbenannt GroundServiceVisualizer->TravelGraphVisualizer. Also, der kann wohl 2D/3D.
 * Obwohl, die line Darstellung macht der SimpleGraphVisualizer. Oder koennen das beide? 9.12.21: Eher nicht.
 *
 * 9.12.21: Warum wird denn die Projection hier nicht verwendet? Evtl. weil hier nur die blauen Bänder ("road") gezeigt werden, und es die nur in 2D gibt?
 * Flugrouten werden in 3D ueber SimpleGraphVisualizer und in 2D hierüber dargestellt?
 *
 *
 * <p>
 * Created by thomass on 04.05.17.
 */
public class TravelGraphVisualizer extends DefaultGraphVisualizer {
    private Map<Integer, List<SceneNode>> visuals = new HashMap<Integer, List<SceneNode>>();
    private Map<Integer, List<SceneNode>> visualPaths = new HashMap<Integer, List<SceneNode>>();
    private double taxiwaywidth = 20f;
    private Log logger = Platform.getInstance().getLog(TravelGraphVisualizer.class);
    public boolean showtaxiways = true;
    public MapProjection projection;
    //29.11.23 seems a design bug to have graph in instance as graph is passed for each visualization
    // public Graph graph;

    public TravelGraphVisualizer(Scene scene) {
        this( scene, 20f);
    }

    public TravelGraphVisualizer(Scene scene, double taxiwaywidth) {
        //super(scene);
        this.taxiwaywidth = taxiwaywidth;
       
    }

    /*@Override
    public void visualizeGraph(Graph graph) {
        //this.visualizer = visualizer;
        //if (visualizer!=null) {
        for (int i = 0; i < graph.getNodeCount(); i++) {
            GraphNode n = graph.getNode(i);
            SceneNode node = buildNode(n);
            if (node != null) {
                scene.addToWorld(node);
            }
        }
        for (int i = 0; i < graph.getEdgeCount(); i++) {
            GraphEdge n = graph.getEdge(i);
            SceneNode e = buildEdge(n);
            if (e != null) {
                scene.addToWorld(e);
            }
        }
    }*/

    /**
     * Nodes evtl., je nach dem, nicht darstellen.
     *
     * @param n
     * @return
     */
    @Override
    public SceneNode buildNode(Graph graph, GraphNode n) {
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

    public SceneNode buildEdge(Graph graph, GraphEdge edge, Color color, double width, double elevation) {
        // 9.12.21 Hier wird eine "Road" als blaues schmales Band dargestellt.
        SceneNode model = TerrainBuilder.buildEdgeArea(edge, width, color, elevation, graph.getGraphOrientation());
        model.setName(edge.getName());
        return model;
    }

    /**
     * 12.7.17: Ein simples Taxiwaysegment.
     *
     * @param edge
     * @return
     */
    @Override
    public SceneNode buildEdge(Graph graph, GraphEdge edge) {
        SceneNode segment = new SceneNode();
        double width = 0.1f;
        // fuer groundnet etwas groesser.
        width = 1;
        if (showtaxiways) {
            // Der z Wert für die Taxiwaymarkierung muss relativ hoch sein, warum auch immer.
            segment.attach(buildEdge(graph, edge, Color.YELLOW, width, 0.3f));
        }
        // Asphalt ueber Textur
        segment.attach(buildEdge(graph, edge, null, taxiwaywidth, 0));
        return segment;
    }

    @Override
    public Vector3 getPositionOffset() {
        return new Vector3();
    }

    /**
     * Ein GroundServie Graph ist schon 2D, ein Flight Path muss noch projected werden.
     * 1.3.18: Nein, wegen des erfoderlichen Smoothing ist der auch schon 2D, wenn erforderlich
     *
     * @param path
     */
    @Override
    public void visualizePath(Graph graph, GraphPath path, SceneNode destinationnode) {
        //logger.debug("visualizing graph "+path.id);
        if (visualPaths.get(path.id) != null) {
            logger.warn("already visual: path.id=" + path.id);
            return;
        }
        List<SceneNode> slist = new ArrayList<SceneNode>();
        for (int i = 0; i < path.getSegmentCount(); i++) {
            GraphEdge e = path.getSegment(i).edge;
            SceneNode n;
            /*1.3.18 if (path.is3D) {
                // dann nicht mehr als Road darstellen, sondern als echte line. Und natürlich projection verwenden.
                n = SimpleGraphVisualizer.buildEdge(e,Color.BLUE,projection);
                scene.addToWorld(n);

            } else {*/
            n = visualizeEdge(graph, e, destinationnode);
            //}
            //      if (n != null) {
            slist.add(n);

        }
        visualPaths.put(path.id, slist);

    }

    @Override
    public void removeVisualizedPath( Graph graph, GraphPath path) {
        List<SceneNode> nodes = visualPaths.get(path.id);
        if (nodes == null) {
            // not existing path
            return;
        }
        //logger.debug("removeVisualizedPath(" + nodes.size() + " nodes): " + path.id);
        for (SceneNode n : nodes) {
            int cnt = n.getTransform().getChildCount();
            for (int i = 0; i < cnt; i++) {
                SceneNode.removeSceneNode(n.getTransform().getChild(i).getSceneNode());
            }
            SceneNode.removeSceneNode(n);
        }
        visualPaths.remove(path.id);
    }

    @Override
    public SceneNode visualizeEdge( Graph graph, GraphEdge e, SceneNode destinationnode) {
        // 28.7.17: 0.1 höher als der Ground. Reicht aber nicht. muss schon 0.5 sein.
        SceneNode n = buildEdge(graph, e, Color.BLUE, 0.8f, 0.5f);
        n.setName("blueedge");
        //if (n != null) {
        //scene.addToWorld(n);
        destinationnode.attach(n);
        // }

        return n;
    }

    @Override
    public void addLayer(Graph g, int layer, SceneNode destinationnode) {
        visuals.put(layer, new ArrayList<SceneNode>());
        for (int i = 0; i < g.getEdgeCount(); i++) {
            GraphEdge e = g.getEdge(i);
            if (e.getLayer() == layer) {
                SceneNode en = buildEdge(g,e);
                if (en != null) {
                    //scene.addToWorld(en);
                    destinationnode.attach(en);
                }
                visuals.get(layer).add(en);//visualizeEdge(e));
            }
        }
    }

    @Override
    public void removeLayer(Graph g,int layer) {
        //logger.debug("removelayer "+layer);
        List<SceneNode> nodes = visuals.get(layer);
        if (nodes == null) {
            // not existing layer
            return;
        }
        for (SceneNode n : nodes) {
            SceneNode.removeSceneNode(n);
        }
        visuals.remove(layer);
    }

    /**
     * Kruecke um alle Extras zu entfernen. bis das ueber events richtig geht
     */
    /*public void clear() {
        while (visuals.size() > 0) {
            int i = new ArrayList<Integer>(visuals.keySet()).get(0);
            removeLayer(i);
        }
        visuals.clear();
        /*while (visualPaths.size() > 0) {
            Integer g = new ArrayList<Integer>(visualPaths.keySet()).get(0);
            removeVisualizedPath(g);
        }
        visualPaths.clear();* /
    }*/
}
