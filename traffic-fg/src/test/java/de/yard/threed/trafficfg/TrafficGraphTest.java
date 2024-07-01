package de.yard.threed.trafficfg;


import de.yard.threed.core.Degree;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.flightgear.TerrainElevationProvider;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.graph.DefaultGraphPathConstraintProvider;
import de.yard.threed.graph.Graph;
import de.yard.threed.graph.GraphEdge;
import de.yard.threed.graph.GraphMovingComponent;
import de.yard.threed.graph.GraphNode;
import de.yard.threed.graph.GraphOrientation;
import de.yard.threed.graph.GraphPath;
import de.yard.threed.graph.GraphPathConstraintProvider;
import de.yard.threed.graph.GraphPosition;
import de.yard.threed.graph.GraphTestUtil;
import de.yard.threed.graph.GraphUtils;
import de.yard.threed.graph.TurnExtension;
import de.yard.threed.javacommon.JavaBundleResolverFactory;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import de.yard.threed.traffic.TrafficGraphFactory;

import de.yard.threed.trafficfg.flight.GeoUtil;
import de.yard.threed.trafficfg.flight.GroundNet;
import de.yard.threed.trafficfg.flight.GroundNetTest;
import de.yard.threed.trafficfg.flight.RouteBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Tests fuer Graphen in echten Welten, zB. Flight 3D.
 * <p>
 * <p>
 * Created by thomass on 16.3.2018
 */
public class TrafficGraphTest {
    Bundle bundleAirportsConfig;

    @BeforeEach
    void setup() {
        Platform platform = FgTestFactory.initPlatformForTest(false, false);

        //FgTestFactory.addBundleFromProjectDirectory("traffic-fg", "data/extended-config");
         bundleAirportsConfig = BundleRegistry.getBundle("traffic-fg");
    }

    /**
     * Simple Edge mit Nordrichtung
     */
    @Test
    public void test1() {
        // an der Stelle, an der die Identiy resultiert (Nordpol). Mit Edge Richtung -x. Wenn man keine Himmelsrichtung verwendet, ist das auch keine
        // Singularität.
        Graph graph = new Graph(GraphOrientation.buildForFG());

        SGGeod coor = new SGGeod(new Degree(0), new Degree(90), 0);
        GraphNode n1 = graph.addNode("n1", coor.toCart());
        //GraphNode n2 = graph.addNode("n2", coor.applyCourseDistance(new Degree(0), 50).toCart());
        GraphNode n2 = graph.addNode("n2", new Vector3(-1000, n1.getLocation().getY(), n1.getLocation().getZ()));
        GraphEdge edge = graph.connectNodes(n1, n2);
        GraphPosition position = new GraphPosition(edge);
        Quaternion rotation = graph.orientation.get3DRotation(false, edge.getEffectiveBeginDirection(), edge);
        //22.3.18: Ist das auch richtig? Laut dot product sind die beiden nicht gleich. Ist das Singularitaet? TODO klaeren. Bleibt erstmal Fehler.
        //29.3.18: jetzt mit upVector passt das wieder.
        TestUtils.assertQuaternion(new Quaternion(), rotation);
        //TestUtils.assertQuaternion("", new Quaternion(0,-1,0,0), rotation);
        //TestUtils.assertQuaternion("", new Quaternion(-0.5f,-0.5f,0.5f,0.5f), rotation);
        System.out.println("dot product: " + Quaternion.getDotProduct(new Quaternion(), new Quaternion(0, -1, 0, 0)));

        FgCalculations fgc = new FgCalculations();
        // die lokale Rotation in Nordrichtung ist ueberall Identity.
        graph = new Graph(GraphOrientation.buildForFG());
        coor = new SGGeod(new Degree(17), new Degree(55), 0);
        n1 = graph.addNode("n1", coor.toCart());
        n2 = graph.addNode("n2", fgc.toCart(GeoUtil.applyCourseDistance(coor.toGeoCoordinate(), new Degree(0), 50)));
        edge = graph.connectNodes(n1, n2);
        //10.5.18 rotation = graph.orientation.getLocal3DRotation(edge, edge.getEffectiveBeginDirection());
        //10.5.18 TestUtils.assertQuaternion("", new Quaternion(), rotation);

        // ueber Equator
        graph = new Graph(GraphOrientation.buildForFG());
        coor = new SGGeod(new Degree(0), new Degree(0), 0);
        n1 = graph.addNode("n1", coor.toCart());
        n2 = graph.addNode("n2", fgc.toCart(GeoUtil.applyCourseDistance(coor.toGeoCoordinate(), new Degree(0), 50)));
        edge = graph.connectNodes(n1, n2);
        rotation = graph.orientation.get3DRotation(false, edge.getEffectiveBeginDirection(), edge);
        //TestUtils.assertQuaternion("equator", new Quaternion(new Degree(180),new Degree(0),new Degree(-90)), rotation);
        //Die Referenzwerte ergeben sich aus rotieren der Identity vom Norpol, sonst lassen die sich aber nicht so recht erklaeren. Platzrunde bestaetigt es aber.
        TestUtils.assertQuaternion(Quaternion.buildFromAngles(new Degree(0), new Degree(90), new Degree(0)), rotation, "equator");

    }

    @Test
    public void test2() throws Exception {
        // Groundnet hat schoene Geods definiert, der Graph ist aber in z0 Ebene.
        //TrafficWorldConfig tw = TrafficWorldConfig.readConfig("data-old", "TrafficWorld.xml"/*"GroundServices"*/);

        GroundNet groundnet = GroundNetTest.loadGroundNetForTesting(bundleAirportsConfig, 0, "EDDK", false);
        SystemManager.putDataProvider(SystemManager.DATAPROVIDERELEVATION, null);
        SystemManager.putDataProvider(SystemManager.DATAPROVIDERELEVATION, TerrainElevationProvider.buildForStaticAltitude(80));
        Graph graph = new RouteBuilder(new FgCalculations()).buildSimpleTestRouteB8toC4(groundnet).getBaseGraph();
        GraphEdge edge = graph.getEdge(0);
        Quaternion rotation = graph.orientation.get3DRotation(false, edge.getEffectiveBeginDirection(), edge);
        //Die Werte einfach ueernommen, nach die c172 Orientierung in FlightScene richtig war.
        //Seit upVector geht das nicht mehr. 30.11.23: No it works again. Surprise surprise.
        TestUtils.assertQuaternion( new Quaternion(-0.06929136f,0.32748893f,-0.07918669f,0.9389777f), rotation);

    }

    /**
     * Zurücksetzen in OSM Sample. Skizze 3.
     */
    @Test
    public void testBackwardsOsm() {
        float radius = 8;
        Graph osm = TrafficGraphFactory.buildOsmSample().getBaseGraph();
        GraphEdge first = osm.findEdgeByName("0-1");
        GraphEdge succ = osm.findEdgeByName("1-2");
        TurnExtension turn = GraphUtils.createBack(osm, osm.getNode(0), first, succ, 1);
        TestUtils.assertVector3(osm.getNode(1).getLocation(), turn.edge.from.getLocation());
        TestUtils.assertVector3(new Vector3(-7.071068f, 10 - 7.071068f, 0), turn.edge.to.getLocation());
        //18.7.17:Werte übernommen, y scheint fraglich. Wieso 0? Optisch sieht das nicht nach 0 aus.
        TestUtils.assertVector3(new Vector3(-4.142136f, 0, 0), turn.arc.getCenter());

        int layer = 2;
        GraphPathConstraintProvider graphPathConstraintProvider = new DefaultGraphPathConstraintProvider(0, 4.5);
        GraphPath path = GraphUtils.createBackPathFromGraphPosition(osm, GraphUtils.createBack(osm, first.from, first, succ, layer), osm.getNode(3), null, graphPathConstraintProvider, layer, false, false, null);
        Assertions.assertEquals("[back on smootharc]ex:e->1-2(7)->2-3(10)", path.toString(), "path");
        GraphEdge backarc = path.startposition.currentedge;
        TestUtils.assertVector3(osm.getNode(0).getLocation(), backarc.from.getLocation(), "startposition.location");
        Assertions.assertTrue(path.backward, "path.backward");
        float exlen = 10;
        Assertions.assertEquals(exlen, path.getSegment(0).edge.getLength());
        GraphMovingComponent gmc = new GraphMovingComponent(null);
        gmc.setGraph(osm, null, null);
        gmc.setPath(path, true);
        gmc.moveForward(8);
        //position immer noch auf back arc. plausiblen Wert übernommen
        float backarclen = 9.759678f;
        Assertions.assertEquals(backarclen, backarc.getLength(), "backarc.len");
        Assertions.assertEquals(backarc.getLength() - 8, gmc.getCurrentposition().edgeposition, "currentposition.position");
        Assertions.assertEquals(backarc.getId(), gmc.getCurrentposition().currentedge.getId(), "currentposition.edge");
        Assertions.assertTrue(gmc.getCurrentposition().isReverseOrientation(), "currentposition.reverse");
        gmc.moveForward(5);
        //jetzt steht er auf dem ersten Pathsegment
        Assertions.assertEquals(path.getSegment(0).edge.getId(), gmc.getCurrentposition().currentedge.getId(), "currentposition.edge");
        Assertions.assertTrue(gmc.getCurrentposition().isReverseOrientation(), "currentposition.reverseorientation");
        Assertions.assertEquals(8 + 5 - backarclen, gmc.getCurrentposition().edgeposition, "currentposition.position");

    }

    /**
     * Path smoothing mit Relocation in OSM Sample. Skizze 3.
     * Normalerweise kann an n1 nicht gesmoothed werden, weil start auf der edge dorthin ist. Deswegen gibt es einen turnloop.
     * Mit Relocation geht es dann aber doch.
     */
    @Test
    public void testRelocationSmooth() {
        Graph osm = TrafficGraphFactory.buildOsmSample().getBaseGraph();
        GraphEdge first = osm.findEdgeByName("0-1");
        GraphEdge succ = osm.findEdgeByName("1-2");
        GraphPosition start = new GraphPosition(first);
        int layer = 2;
        //ohne Relocation mit turnloop
        GraphPathConstraintProvider graphPathConstraintProvider = new DefaultGraphPathConstraintProvider(0, 4.5);
        GraphPath path = GraphUtils.createPathFromGraphPosition(osm, start, succ.getTo(), null, graphPathConstraintProvider, layer, true, false, null);
        //wieso ist hier eigentlich der zweite smootharc? 19.11.18: Seit float->double ist der nicht mehr, was wohl auch richtig ist
        Assertions.assertEquals( /*5*/4, path.getSegmentCount(), "segments");
        //Assertions.assertEquals("path", "n1:e1->turnloop.smootharc(265)->smoothbegin.n1(20)->smootharc(0)->smoothend.n1(7)", path.toString());
        Assertions.assertEquals("n1:e1->turnloop.smootharc(265)->e2(20)->1-2(7)", path.toString(), "path");
        Assertions.assertNull(path.startposition, "startposition");

        //jetzt mit Relocation
        GraphMovingComponent gmc = new GraphMovingComponent(null);
        gmc.setGraph(osm, start, null);
        path = GraphUtils.createPathFromGraphPosition(osm, start, succ.getTo(), null, graphPathConstraintProvider, layer, true, true, null);
        Assertions.assertNotNull(path.startposition, "startposition");
        Assertions.assertEquals(3, path.getSegmentCount(), "segments");
        Assertions.assertEquals("n0:smoothbegin.n1->smootharc(4)->smoothend.n1(5)", path.toString(), "path");
        Assertions.assertEquals("smoothbegin.n1", path.startposition.currentedge.getName(), "startposition.edge.name");
        Assertions.assertEquals(0, path.startposition.edgeposition, "startposition.edgeposition");
        Assertions.assertFalse(path.startposition.isReverseOrientation(), "startposition.edge.name");
        GraphTestUtil.assertGraphPosition("startposition.edge.name", gmc.getCurrentposition(), start);
        gmc.setPath(path, true);
        GraphTestUtil.assertGraphPosition("startposition.edge.name", gmc.getCurrentposition(), path.startposition);
        gmc.moveForward(8);

        // zu weit vorne, als das ein inner arc noch geht.
        GraphPosition farstart = new GraphPosition(first, first.getLength() - 0.3f);
        path = GraphUtils.createPathFromGraphPosition(osm, farstart, succ.getTo(), null, graphPathConstraintProvider, layer, true, true, null);
        Assertions.assertEquals("n1:e1->turnloop.smootharc(265)->e2(20)", path.toString(), "path");
        //warum ist denn hier die startposition gesetzt?
        //Assertions.assertNull("startposition", path.startposition);

        //ein bischen weiter vorne
        GraphPosition shortstart = new GraphPosition(first, 0.5f);
        path = GraphUtils.createPathFromGraphPosition(osm, shortstart, succ.getTo(), null, graphPathConstraintProvider, layer, true, true, null);
        Assertions.assertEquals("n0:smoothbegin.n1->smootharc(4)->smoothend.n1(5)", path.toString(), "path");
        Assertions.assertEquals("smoothbegin.n1", path.startposition.currentedge.getName(), "startposition.edge.name");
        Assertions.assertEquals(0.5f, path.startposition.edgeposition, "startposition.edgeposition");
        Assertions.assertFalse(path.startposition.isReverseOrientation(), "startposition.edge.name");

        //Jetzt mal n2->n0
        start = new GraphPosition(succ, 0, true);
        graphPathConstraintProvider = new DefaultGraphPathConstraintProvider(0, 1.5);
        path = GraphUtils.createPathFromGraphPosition(osm, start, first.getFrom(), null, graphPathConstraintProvider, layer, true, true, null);
        Assertions.assertNotNull(path.startposition, "startposition");
        Assertions.assertEquals(3, path.getSegmentCount(), "segments");
        Assertions.assertEquals("n2:smoothbegin.n1->smootharc(1)->smoothend.n1(9)", path.toString(), "path");
        Assertions.assertEquals("smoothbegin.n1", path.startposition.currentedge.getName(), "startposition.edge.name");
        Assertions.assertEquals(0, path.startposition.edgeposition, "startposition.edgeposition");
        Assertions.assertFalse(path.startposition.isReverseOrientation(), "startposition.edge.isReverseOrientation");


    }

}
