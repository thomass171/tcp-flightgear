package de.yard.threed.trafficfg.flight;

import de.yard.threed.core.CharsetException;
import de.yard.threed.core.Degree;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.XmlException;
import de.yard.threed.core.loader.StringReader;
import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.XmlDocument;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.util.XmlHelper;
import de.yard.threed.flightgear.TerrainElevationProvider;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeodesy;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.flightgear.testutil.FgTestUtils;
import de.yard.threed.graph.DefaultGraphPathConstraintProvider;
import de.yard.threed.graph.DefaultGraphWeightProvider;
import de.yard.threed.graph.Graph;
import de.yard.threed.graph.GraphArcParameter;
import de.yard.threed.graph.GraphEdge;
import de.yard.threed.graph.GraphMovingComponent;
import de.yard.threed.graph.GraphNode;
import de.yard.threed.graph.GraphPath;
import de.yard.threed.graph.GraphPathConstraintProvider;
import de.yard.threed.graph.GraphPathSegment;
import de.yard.threed.graph.GraphPosition;
import de.yard.threed.graph.GraphTransition;
import de.yard.threed.graph.GraphUtils;
import de.yard.threed.graph.TurnExtension;
import de.yard.threed.traffic.NoElevationException;
import de.yard.threed.traffic.PositionHeading;
import de.yard.threed.traffic.TrafficConfig;
import de.yard.threed.traffic.TrafficGraph;
import de.yard.threed.traffic.config.ConfigHelper;
import de.yard.threed.traffic.config.VehicleConfigDataProvider;
import de.yard.threed.traffic.config.VehicleDefinition;
import de.yard.threed.traffic.config.XmlVehicleDefinition;
import de.yard.threed.traffic.flight.FlightLocation;
import de.yard.threed.traffic.geodesy.SimpleMapProjection;
import de.yard.threed.trafficcore.config.AirportDefinition;
import de.yard.threed.trafficcore.config.LocatedVehicle;
import de.yard.threed.trafficfg.config.AirportConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static de.yard.threed.core.testutil.Assert.fail;

/**
 * Anhand der abgelegten EDDK Definition. Auch fuer Servicepoint.
 * <p>
 * Created by thomass on 27.03.17.
 */
public class GroundNetTest {
    //9.6.20 kacke static Platform platform = TestFactory.initPlatformForTest(false, new String[]{"data"}, false);
//9.6.20 geht dann auch nicht    Log logger = Platform.getInstance().getLog(GroundNetTest.class);
    GraphPathConstraintProvider defaultgraphPathConstraintProvider = new DefaultGraphPathConstraintProvider(TrafficGraph.MINIMUMPATHSEGMENTLEN, TrafficGraph.SMOOTHINGRADIUS);
    //private TrafficWorldConfig tw;
    private TrafficConfig airportDefinitions;
    private TrafficConfig vehicleDefinitions;
    String CONFIG_BUNDLE = "test-resources";
    Bundle bundleGroundnetConfig;
    //MapProjection projection;
    //GroundNet groundnet;
    //Graph g;

    @BeforeEach
    public void setup() {
        //EngineTestFactory.initPlatformForTest( new String[] {"engine","data","data-old","railing"},
         //       new SimpleHeadlessPlatformFactory(JavaBundleResolverFactory.bySimplePath(GranadaPlatform.GRANDA_BUNDLE_PATH)));
        Platform platform = FgTestFactory.initPlatformForTest(true, false);

        //FgTestFactory.addBundleFromProjectDirectory("extended-config","data/extended-config");
        EngineTestFactory.loadBundleSync("traffic-fg");

        bundleGroundnetConfig = BundleRegistry.getBundle("traffic-fg");
        Bundle testResources = BundleRegistry.getBundle(CONFIG_BUNDLE);

        airportDefinitions =  TrafficConfig.buildFromBundle(testResources, new BundleResource("airport-definitions-fortest.xml")/*"GroundServices"*/);
        vehicleDefinitions =  TrafficConfig.buildFromBundle(testResources, new BundleResource("vehicle-definitions-fortest.xml")/*"GroundServices"*/);

    }

    @Test
    public void testGroundNet() throws Exception {
        GroundNet groundnet = loadGroundNetForTest(0);
        GraphNode nodetosmooth = groundnet.groundnetgraph.getBaseGraph().findNodeByName("90");
        List<GraphEdge> arcs = GraphUtils.smoothNode(groundnet.groundnetgraph.getBaseGraph(), nodetosmooth, 10, 77);

        Assertions.assertEquals( 2, arcs.size(),"new arcs");
        Graph gr = groundnet.groundnetgraph.getBaseGraph();
        GraphPath path = gr.findPath(gr.findNodeByName("16"), gr.findNodeByName("2"), null);
        Assertions.assertEquals( 10, path.getSegmentCount(),"path.segments");
        GraphNode parkpos_b_2 = gr.findNodeByName("2");
        GraphNode parkpos_e20 = gr.findNodeByName("16");
        Parking b_2 = groundnet.getParkPos("B_2");
        Assertions.assertEquals( 341.59f, (double) b_2.heading.getDegree(),"heading B_2");
        GraphEdge b_2_approach = groundnet.groundnetgraph.getBaseGraph().findConnection(parkpos_b_2, gr.findNodeByName("101"));
        b_2_approach = groundnet.groundnetgraph.getBaseGraph().findConnection(parkpos_b_2, gr.findNodeByName("202"));
        Assertions.assertEquals( 341.59f, (double) GroundNet.getHeadingFromDirection(b_2_approach.getEffectiveInboundDirection(parkpos_b_2)).getDegree(),0.001,"heading B_2");
        b_2_approach = b_2.getApproach();
        Assertions.assertEquals( "2-202", b_2_approach.getName(),"heading B_2");

        b_2 = groundnet.getParkPos("B_2");
        TurnExtension teardrop = groundnet.addTearDropTurnAtParking(b_2, true);
        Assertions.assertNotNull( teardrop);

        path = gr.findPath(gr.findNodeByName("16"), gr.findNodeByName("16"), null);
        Assertions.assertEquals( 0, path.getSegmentCount(),"path.segments");

    }

    /**
     * Tests als Referenz für Tests in FG.
     */
    @Test
    public void testGroundNetEDDKLikeFG() throws Exception {

        GroundNet groundnet = loadGroundNetForTest(0);
        TestUtils.assertVector3( new Vector3(-1889.7698f, -295.14346f, /*TrafficWorld2D.getInstance().*/getAirport("EDDK").getElevation()), groundnet.groundnetgraph.getBaseGraph().getNode(0).getLocation(),"node0");

        /*GraphNode nodetosmooth = g.findNodeByName("90");
        List<GraphEdge> arcs = GraphUtils.smoothNode(g, nodetosmooth, 10, 77);
        Assert.assertEquals("new arcs", 2, arcs.size());
        TestUtil.assertVector3("arcs0.from", new Vector3(-1118.8313f, 1192.5712f, 0), arcs.get(0).from.getLocation());
        TestUtil.assertVector3("arcs0.to", new Vector3(-1118.2125f, 1209.4492f, 0), arcs.get(0).to.getLocation());
        loadGroundNetForTest(1);*/

        // Refwerte einfach eingetragen
        Assertions.assertEquals( 241, groundnet.groundnetgraph.getBaseGraph().getNodeCount(),"nodes");
        Assertions.assertEquals( 269, groundnet.groundnetgraph.getBaseGraph().getEdgeCount(),"edges");

        Parking parkpos_c_7 = groundnet.getParkPos("C_7");
        //logger.debug("parkpos C_7:" + parkpos_c_7.name + ",location.x=" + parkpos_c_7.node.getLocation());
        Vector3 c7loc = parkpos_c_7.node.getLocation();
        TestUtils.assertVector3( new Vector3(-1642, 1434, 0), new Vector3(Math.round(c7loc.getX()), Math.round(c7loc.getY()), 0),"C_7location");
        Graph gr = groundnet.groundnetgraph.getBaseGraph();
        //
        GraphPath path = gr.findPath(gr.findNodeByName("16"), gr.findNodeByName("2"), null);
        Assertions.assertNotNull( path,"findpath");

        //Path nach C_7 und da wieder raus.
        GraphNode n134 = gr.findNodeByName("134");
        Parking c_7 = groundnet.getParkPos("C_7");
        path = gr.findPath(n134, groundnet.getParkPos("C_7").node, null);
        Assertions.assertEquals("134:133-134->103-133(88)->207-103(24)->7-207(50)", path.toString(),"path");

        GraphPosition startposition = GraphPosition.buildPositionAtNode(gr.findEdgeByName("133-134"), n134, true);
        GraphPathConstraintProvider graphPathConstraintProvider = new DefaultGraphPathConstraintProvider(TrafficGraph.MINIMUMPATHSEGMENTLEN, TrafficGraph.SMOOTHINGRADIUS);

        path = GraphUtils.createPathFromGraphPosition(gr, startposition,
                c_7.node, null, graphPathConstraintProvider, 233, true, false, null);
        //ohnebypassTestUtil.assertEquals("path", "125:e1->turnloop.smootharc(210.303)->e2(19.999989)->smoothbegin.104(167.49985)->smootharc(1.1164284)->smoothend.104(2.3709562)", path.toString());
        Assertions.assertEquals("133:e1->turnloop.smootharc(131)->e2(20)->smoothbegin.103(87)->smootharc(2)->smoothbegin.207(21)->smootharc(3)->smoothend.207(48)", path.toString(),"path");
        Assertions.assertEquals("nodes:0:269;233:13;", gr.getStatistic(),"statistics");
        GraphMovingComponent gmc = new GraphMovingComponent();
        gmc.setGraph(null, startposition, null);
        gmc.setPath(path);
        gmc.moveForward(100000);
        GraphEdge edge7_207 = gr.findEdgeByName("7-207"/*"smoothend.207"*/);
        //3.8.17: Der bleibt jetzt am Pathende stehen statt auf Layer 0 zu gehen (Wiki).
        //15.8.17: Nicht mehr, es gibt jetzt die finalposition
        assertPosition(new GraphPosition(edge7_207, edge7_207.getLength(), true), gmc.getCurrentposition());
        gr.removeLayer(path.layer);
        Assertions.assertEquals( 269, gr.getEdgeCount(),"edges");

        // A20 nach C_4
        GraphNode c_4 = groundnet.getParkPos("C_4").node;
        Parking a20 = groundnet.getVehicleHome();
        // a20 parkpos hat gar nicht 1-201 heading 
        GraphPosition a20position = new GraphPosition(gr.findEdgeByName("1-201"), 50.180775f, true);
        //groundnet.getParkingPosition(a20);
        //15.5.18: kann nichr asserted werden weil 1-63 die korrekte ist.assertA20position(a20position);
        path = groundnet.groundnetgraph.createPathFromGraphPosition(a20position, c_4, null, null);
        Assertions.assertEquals( "1:e1->turnloop.smootharc(7)->e2(20)->smoothbegin.63(28)->smootharc(0)->smoothbegin.69(52)->smootharc(12)->smoothbegin.68(14)->smootharc(0)->smoothbegin.129(83)->smootharc(14)->smoothbegin.130(160)->smootharc(0)->smoothbegin.131(27)->smootharc(0)->smoothbegin.132(107)->smootharc(0)->smoothbegin.134(62)->smootharc(0)->smoothbegin.125(81)->smootharc(19)->smoothbegin.206(102)->smootharc(2)->smoothend.206(49)", path.toString(),"path");
        Assertions.assertEquals( "nodes:0:269;1:56;", gr.getStatistic(),"statistics");
        gmc = new GraphMovingComponent();
        gmc.setGraph(groundnet.groundnetgraph.getBaseGraph(), a20position, null);
        gmc.setPath(path);
        gmc.moveForward(100000);
        GraphEdge edge6_206 = gr.findEdgeByName("6-206");
        assertPosition(new GraphPosition(edge6_206, edge6_206.getLength(), true), gmc.getCurrentposition());
        gr.removeLayer(path.layer);
        Assertions.assertEquals( 269, gr.getEdgeCount(),"edges");

        // ausfahren aus C_4. Gibt es unten auch nochmal separat.
        GraphPosition c_4position = groundnet.getParkingPosition(groundnet.getParkPos("C_4"));
        GraphNode e20 = groundnet.groundnetgraph.getBaseGraph().findNodeByName("16");
        path = groundnet.groundnetgraph.createPathFromGraphPosition(c_4position, e20, null, null);
        Assertions.assertEquals("6:e1->turnloop.smootharc(4)->e2(20)->smoothbegin.104(56)->smootharc(0)->smoothbegin.124(97)->smootharc(18)->smoothbegin.134(67)->smootharc(18)->smoothbegin.89(80)->smootharc(4)->smoothbegin.90(322)->smootharc(20)->smoothbegin.46(78)->smootharc(0)->smoothend.46(21)", path.toString(),"path from C_4");
        //liegt der arc richtig? die 17 scheinen plausibel
        Vector3 center = path.getSegment(6).edge.getCenter();
        Assertions.assertNotNull( center);
        Assertions.assertEquals( 17.89897f, Vector3.getDistance(center, groundnet.groundnetgraph.getBaseGraph().findNodeByName("125").getLocation()),0.0001,"arccenter distance to 125");
        gmc = new GraphMovingComponent();
        gmc.setGraph(groundnet.groundnetgraph.getBaseGraph(), c_4position, null);
        gmc.setPath(path);
        gmc.moveForward(100000);
        gr.removeLayer(path.layer);
        Assertions.assertEquals( 269, gr.getEdgeCount(),"edges");
/*      

        GraphEdge e63_69 = gr.findEdgeByName("63-69");
        GraphPosition gpos = new GraphPosition(e63_69, 0);
        TestUtil.assertVector3("e63_69.dir", new Vector3(0.64909995f, 0.76070315f, 0), e63_69.getEffectiveBeginDirection());
        TestUtil.assertVector3("e63_69.dir", new Vector3(0.64909995f, 0.76070315f, 0), e63_69.getEffectiveEndDirection());
        GraphEdge e68_69 = gr.findEdgeByName("68-69");
        TestUtil.assertVector3("e68_69.dir", new Vector3(-0.94416976f, 0.32945943f, 0), e68_69.getEffectiveBeginDirection());

        // smoothing von 63->68
        GraphEdge e63smoothbegin = e63_69.getFrom().getEdge(3);
        TestUtil.assertEquals("", "smoothbegin", e63smoothbegin.getName());
        GraphEdge e63smootharc = e63smoothbegin.getTo().getEdge(1);
        TestUtil.assertEquals("", "smootharc", e63smootharc.getName());
        TestUtil.assertVector3("e63smootharc.dir", e63_69.getEffectiveEndDirection(), e63smootharc.getEffectiveBeginDirection());
        TestUtil.assertVector3("e63smootharc.dir", e68_69.getEffectiveEndDirection().negate(), e63smootharc.getEffectiveEndDirection());*/

    }

    /**
     * Einen Pfad von (0,0,0) nach Sueden (fuer Followme von dort). Skizze 32.
     */
    @Test
    public void testPathIntoGroundNet180() throws Exception {
        //tw =  TrafficWorldConfig.readConfig("data-old", "TrafficWorld.xml"/*"GroundServices"*/);

        GroundNet groundnet = loadGroundNetForTest(0);
        double airportelevation = /*TrafficWorld2D.getInstance().*/getAirport("EDDK").getElevation();

        PositionHeading poshdg = new PositionHeading(new Vector3(-1200, 1300, airportelevation), new Degree(180));
        GraphEdge edgeintograph = groundnet.createPathIntoGroundNet(poshdg);
        TestUtils.assertVector3( new Vector3(-1200, 1300, airportelevation), edgeintograph.getFrom().getLocation(),"edgeintograph.from");
        // von from muss es drei edges geben. Eine gerade auf die intersection und zwei kürzere gerade für jede Richtung zum Smoothing.
        // 19.5.17: Das muss eh nochmal gechecked werden.
        Assertions.assertEquals( 3, edgeintograph.getFrom().getEdgeCount(),"edgeintograph.from.edges");
        // Pfad dazu
        GraphPath path = groundnet.findFollowMePath(edgeintograph.from, groundnet.getParkPos("C_7"));
        GraphEdge nearestlineedge = path.getNearestLineEdge(null);
        TestUtils.assertVector3( poshdg.position, nearestlineedge.from.getLocation(),"nearestlineedge.origin");
        Assertions.assertEquals( 15.372437f, nearestlineedge.getLength(),0.001, "nearestlineedge.len");
        nearestlineedge = path.getNearestLineEdge(nearestlineedge);
        // das ist jetzt das "lange" Stück
        Assertions.assertEquals( "89", nearestlineedge.to.getName(),"nearestlineedge.origin");
        Assertions.assertEquals( 205.43611f, nearestlineedge.getLength(),0.0001,"nearestlineedge.len");
    }

    /**
     * Einen Pfad von (0,0,0) nach SuedWest (fuer Followme von dort). Skizze 32.
     */
    @Test
    public void testPathIntoGroundNet260() throws Exception {
       // tw =  TrafficWorldConfig.readConfig("data-old", "TrafficWorld.xml"/*"GroundServices"*/);

        GroundNet groundnet = loadGroundNetForTest(0);
        double airportelevation = /*TrafficWorld2D.getInstance().*/getAirport("EDDK").getElevation();

        PositionHeading poshdg = new PositionHeading(new Vector3(-1200, 1300, airportelevation), new Degree(260));
        GraphEdge edgeintograph = groundnet.createPathIntoGroundNet(poshdg);
        TestUtils.assertVector3( new Vector3(-1200, 1300, airportelevation), edgeintograph.getFrom().getLocation(),"edgeintograph.from");
        // von from muss es drei edges geben. Eine gerade auf die intersection und zwei kürzere gerade für jede Richtung zum Smoothing.
        Assertions.assertEquals( 3, edgeintograph.getFrom().getEdgeCount(),"edgeintograph.from.edges");
        // Pfad dazu
        GraphPath aircraftpath = groundnet.findFollowMePath(edgeintograph.from, groundnet.getParkPos("C_7"));
        GraphEdge nearestlineedge = aircraftpath.getNearestLineEdge(null);
        // nearestlineedge muss zu einer smoothing node fuehren. Ob begin oder end mag Zufall sein.
        // 19.5.17: Das muss eh nochmal gechecked werden.
        Assertions.assertEquals("smootharcfrom", nearestlineedge.to.getName(),"nearestlineedge.to");
        TestUtils.assertVector3( poshdg.position, nearestlineedge.from.getLocation(),"nearestlineedge.origin");
        Assertions.assertEquals( 36.344894f, nearestlineedge.getLength(),0.01, "nearestlineedge.len");

        TurnExtension teardropturn = groundnet.createFollowMeVehicleApproach(aircraftpath);
        Assertions.assertNotNull( teardropturn,"TearDropTurn");


        //  GraphPath followmepath = groundnet.groundnetgraph.findPath(getGraphMovingComponent(followme).currentposition, teardropturn.to, null);

    }

    /**
     * 24.5.17: Der Path wird gesmoothed, nicht das groundnet.
     * Skizze 33 und 29d
     */
    @Test
    public void testPathFromA20To69() throws Exception {
        GroundNet groundnet = loadGroundNetForTest(0);
        GraphNode n69 = groundnet.groundnetgraph.getBaseGraph().findNodeByName("69");
        Parking a20 = groundnet.getVehicleHome();
        GraphPosition a20position = groundnet.getParkingPosition(a20);
        assertA20position(a20position);
        GraphPath path = GraphUtils.createPathFromGraphPosition(groundnet.groundnetgraph.getBaseGraph(), a20position, n69, null, defaultgraphPathConstraintProvider, 233, true, false, null);
        //Assertions.assertEquals("path", "1:e1->turnloop.smootharc(7)->e2(20)->smoothbegin.63(28)->smootharc(0)->smoothend.63(59)", path.toString());
        Assertions.assertEquals( "1:teardrop.smootharc->smoothbegin.63(27)->smootharc(3)->smoothend.63(57)", path.toString(),"path");
        Assertions.assertEquals(n69.getName(), path.getSegment(3).getLeaveNode().getName(),"path.destinationnode");
    }

    /**
     * Der Path wird gesmoothed, nicht das groundnet.
     * Skizze 33 und 29d
     */
    @Test
    public void testPathFromA20To70() throws Exception {
        GroundNet groundnet = loadGroundNetForTest(0);
        GraphNode n70 = groundnet.groundnetgraph.getBaseGraph().findNodeByName("70");
        Parking a20 = groundnet.getVehicleHome();
        GraphPosition a20position = groundnet.getParkingPosition(a20);
        assertA20position(a20position);
        GraphPath path = GraphUtils.createPathFromGraphPosition(groundnet.groundnetgraph.getBaseGraph(), a20position, n70, null, defaultgraphPathConstraintProvider, 233, true, false, null);
        //Assertions.assertEquals("path", "1:e1->turnloop.smootharc(7)->e2(20)->smoothbegin.63(28)->smootharc(0)->smoothbegin.69(44)->smootharc(20)->smoothend.69(267)", path.toString());
        Assertions.assertEquals("1:teardrop.smootharc->smoothbegin.63(27)->smootharc(3)->smoothbegin.69(43)->smootharc(20)->smoothend.69(267)", path.toString(),"path");
    }

    /**
     * Von der Edge 1-63 ausgehen, so wie A20 schon mal angefahren wird. Dann brauchts einen TearDrop.
     * Nicht fertig
     */
    @Test
    public void testPathFromA20To70WithTearDrop() throws Exception {
        GroundNet groundnet = loadGroundNetForTest(0);
        GraphNode n70 = groundnet.groundnetgraph.getBaseGraph().findNodeByName("70");
        GraphEdge e1_63 = groundnet.groundnetgraph.getBaseGraph().findEdgeByName("1-63");
        GraphPosition a20position = new GraphPosition(e1_63, e1_63.getLength(), true);
        GraphPath path = GraphUtils.createPathFromGraphPosition(groundnet.groundnetgraph.getBaseGraph(), a20position, n70, null, defaultgraphPathConstraintProvider, 233, true, false, null);
        Assertions.assertEquals( "1:teardrop.smootharc->smoothbegin.63(27)->smootharc(3)->smoothbegin.69(43)->smootharc(20)->smoothend.69(267)", path.toString(),"path");
    }

    /**
     * Der Path wird gesmoothed, nicht das groundnet.
     * Skizze 33 und 29d
     * 27.5.17: Scheitert z.Z., weil das erste Segment gebypasst wird. Da gibts es keine universelle Lösung.
     * Das headingkonforme ausfahren wäre nur für aircraft, und die machen eh pushback, der anders läuft. Aber der
     * Pfad beginnt ja schon mit einem passenden Turn, und der würde mit bypass des ersten Segments unbrauchbar.
     * Quatsch, der turn wird doch nach dem bypass erstellt.
     * Also: das erste Segment kann doch ruhig bypassed werden. Dann beginnt der Pfad mit zwei Bypass.
     * Wegen des bypass am ersten beginnt es mit turnloop.
     * Auch oben in Flightgear
     */
    @Test
    public void testPathFromC_4() throws Exception {
        GroundNet groundnet = loadGroundNetForTest(0);
        GraphNode e20 = groundnet.groundnetgraph.getBaseGraph().findNodeByName("16");
        GraphPosition c_4position = groundnet.getParkingPosition(groundnet.getParkPos("C_4"));
        assertC_4position(c_4position);
        GraphPath path = groundnet.groundnetgraph.getBaseGraph().findPath(groundnet.getParkPos("C_4").node, e20, null);
        Assertions.assertEquals( "6:6-206->206-104(8)->104-125(111)->124-125(2)->134-124(93)->134-89(95)->89-90(339)->46-90(94)->16-46(21)", path.toString(),"path");
        Assertions.assertEquals(9, path.getSegmentCount(),"path.segments");
        Assertions.assertEquals( "6-206", path.getSegment(0).edge.getName(),"path.segments.name0");
        Assertions.assertEquals( "134-89", path.getSegment(5).edge.getName(),"path.segments.name5");

        path = GraphUtils.createPathFromGraphPosition(groundnet.groundnetgraph.getBaseGraph(), c_4position, e20, null, defaultgraphPathConstraintProvider, 233, true, false, null);
        Assertions.assertEquals( "6:e1->turnloop.smootharc(4)->e2(20)->smoothbegin.104(56)->smootharc(0)->smoothbegin.124(97)->smootharc(18)->smoothbegin.134(67)->smootharc(18)->smoothbegin.89(80)->smootharc(4)->smoothbegin.90(322)->smootharc(20)->smoothbegin.46(78)->smootharc(0)->smoothend.46(21)", path.toString(),"path");
        //liegt der arc richtig? die 17 scheinen plausibel
        Assertions.assertEquals( 17.89897f, Vector3.getDistance(path.getSegment(6).edge.getCenter(), groundnet.groundnetgraph.getBaseGraph().findNodeByName("125").getLocation()),0.00001,"arccenter distance to 125");
    }

    /**
     * Problemfall skippen des vorletzten Segments. Als vorletztes darf ich es nicht "nach vorne" bypassen, um das headingkonforme einfahren nicht zu gefährden.
     * Also muss es back gebypassed werden.
     * An 125 muss es einen turn loop geben.
     * Skizze 33 und 29d
     */
    @Test
    public void testPathFrom123toC_4() throws Exception {
        GroundNet groundnet = loadGroundNetForTest(0);
        GraphNode n123 = groundnet.groundnetgraph.getBaseGraph().findNodeByName("123");
        Parking c_4 = groundnet.getParkPos("C_4");
        GraphPath path = groundnet.groundnetgraph.getBaseGraph().findPath(n123, groundnet.getParkPos("C_4").node, null);
        Assertions.assertEquals("123:125-123->104-125(111)->206-104(8)->6-206(50)", path.toString(),"path");

        path = GraphUtils.createPathFromGraphPosition(groundnet.groundnetgraph.getBaseGraph(), GraphPosition.buildPositionAtNode(groundnet.groundnetgraph.getBaseGraph().findEdgeByName("125-123"), n123, true),
                c_4.node, null, defaultgraphPathConstraintProvider, 233, true, false, null);
        //ohnebypassAssertions.assertEquals("path", "125:e1->turnloop.smootharc(210.303)->e2(19.999989)->smoothbegin.104(167.49985)->smootharc(1.1164284)->smoothend.104(2.3709562)", path.toString());
        Assertions.assertEquals("125:e1->turnloop.smootharc(150)->e2(20)->smoothbegin.206(116)->smootharc(2)->smoothend.206(49)", path.toString(),"path");
        //liegt der arc richtig? die 35 scheinen plausibel
        Assertions.assertEquals( 35.612064f, Vector3.getDistance(path.getSegment(1).edge.getCenter(), groundnet.groundnetgraph.getBaseGraph().findNodeByName("125").getLocation()),0.0001,"arccenter distance to 125");
    }

    /**
     * Departure path fuer ein ueber Smoothing bei C_7 (node 7) eingefahrendes Vehicle.
     * Er darf nicht einen anderen Edge nehmen, sondern muss teardrop nehmen.
     * <p>
     * Skizze 33
     */
    @Test
    public void testPathFromC_7WithSmoothing() throws Exception {
        GroundNet groundnet = loadGroundNetForTest(0);
        Graph gr = groundnet.groundnetgraph.getBaseGraph();
        GraphEdge edge7_207 = gr.findEdgeByName("7-207");
        GraphNode n133 = gr.findNodeByName("131");
        Assertions.assertEquals( 269, gr.getEdgeCount(),"edges");

        // erstmal nach C_7 fahren
        GraphPosition start = new GraphPosition(gr.findEdgeByName("130-131"));
        GraphPath path = groundnet.groundnetgraph.createPathFromGraphPosition(start, groundnet.getParkPos("C_7").node, null, null);
        Assertions.assertEquals( "131:smoothbegin.132->smootharc(0)->smoothbegin.133(35)->smootharc(18)->smoothbegin.103(74)->smootharc(2)->smoothbegin.207(21)->smootharc(3)->smoothend.207(48)", path.toString(),"path");
        // path komplett abfahren.
        GraphMovingComponent gmc = new GraphMovingComponent();
        gmc.setGraph(null, start, null);
        gmc.setPath(path);
        GraphPath completed = gmc.moveForward(100000);
        Assertions.assertNotNull(completed,"completed.path");
        // path muss completed sein und die Position echt auf C_7, nicht mehr auf smmothed path
        GraphPosition c_7position = groundnet.getParkingPosition(groundnet.getParkPos("C_7"));
        assertPosition(new GraphPosition(edge7_207, edge7_207.getLength(), true), c_7position);
        assertPosition(c_7position, gmc.getCurrentposition());
        gr.removeLayer(path.layer);
        Assertions.assertEquals(269, gr.getEdgeCount(),"edges");
        //Jetzt steh ich in C_7. Jetzt der eigentliche Test.
        path = groundnet.groundnetgraph.createPathFromGraphPosition(c_7position, n133, null, null);
        Assertions.assertEquals( "7:teardrop.smootharc->smoothbegin.207(49)->smootharc(2)->smoothbegin.103(22)->smootharc(2)->smoothbegin.133(74)->smootharc(18)->smoothbegin.132(35)->smootharc(0)->smoothend.132(107)", path.toString(),"path");


    }

    /**
     * Departure path fuer ein ueber Smoothing bei C_4 (node 6) eingefahrendes Vehicle.
     * Gab visuell Artefakte weil es da bypass gibt un der Winkel des turnloop ziemlich spitz ist?
     * Nee, weil calcArc bei outer arc die falsche arc Richtung gesetzt hat.
     * <p>
     * Skizze 33
     */
    @Test
    public void testPathFromC_4WithSmoothing() throws Exception {
        GroundNet groundnet = loadGroundNetForTest(0);
        Graph gr = groundnet.groundnetgraph.getBaseGraph();
        GraphEdge edge6_206 = gr.findEdgeByName("6-206");
        GraphNode n133 = gr.findNodeByName("131");

        // erstmal nach C_4 fahren
        GraphPosition start = new GraphPosition(gr.findEdgeByName("130-131"));
        GraphPath path = groundnet.groundnetgraph.createPathFromGraphPosition(start, groundnet.getParkPos("C_4").node, null, null);
        Assertions.assertEquals( "131:smoothbegin.132->smootharc(0)->smoothbegin.134(62)->smootharc(0)->smoothbegin.125(81)->smootharc(19)->smoothbegin.206(102)->smootharc(2)->smoothend.206(49)", path.toString(),"path");
        // path komplett abfahren.
        GraphMovingComponent gmc = new GraphMovingComponent();
        gmc.setGraph(null, start, null);
        gmc.setPath(path);
        GraphPath completed = gmc.moveForward(100000);
        Assertions.assertNotNull( completed,"completed.path");
        // path muss completed sein und die Position echt auf C_4, nicht mehr auf smmothed path
        GraphPosition c_4position = groundnet.getParkingPosition(groundnet.getParkPos("C_4"));
        assertPosition(new GraphPosition(edge6_206, edge6_206.getLength(), true), c_4position);
        assertPosition(c_4position, gmc.getCurrentposition());
        gr.removeLayer(path.layer);
        Assertions.assertEquals( 269, gr.getEdgeCount(),"edges");
        //Jetzt steh ich in C_4. Jetzt der eigentliche Test.
        path = groundnet.groundnetgraph.createPathFromGraphPosition(c_4position, n133, null, null);
        Assertions.assertEquals("6:e1->turnloop.smootharc(4)->e2(20)->smoothbegin.104(56)->smootharc(0)->smoothbegin.124(97)->smootharc(18)->smoothbegin.133(93)->smootharc(0)->smoothbegin.132(48)->smootharc(0)->smoothend.132(107)", path.toString(),"path");

        // Rueck Projection von C_4. erstmal die inneren MEthoden
        Parking c_4cparking = ((Parking) groundnet.getParkPos("C_4").node.customdata);
        FlightLocation fl = GraphProjectionFlight3D.unprojectToFlightLocation(groundnet.projection,new Vector2(), 44, new Vector2(0, 1));
        Assertions.assertEquals( 0, fl.heading.getDegree(),"heading");

        // und jetzt "von aussen"
        gmc = new GraphMovingComponent();
        gmc.setGraph(null, c_4position, null);
        Vector3 upVector = new Vector3(0, 0, 1);
        /*TODO 17.12.21: GraphProjectionFlight3D needs FgMath.
        LocalTransform posrot = GraphMovingSystem.getPosRot(gmc, new GraphProjectionFlight3D(groundnet.projection));
        //logger.debug("posrot=" + posrot);
        SGGeod reC_4 = SGGeod.fromCart(posrot.position);
        //logger.debug("reC_4=" + reC_4);
        Assertions.assertEquals("reC_4.lat", (double) c_4cparking.coor.getLatitudeDeg().getDegree(), (double) reC_4.getLatitudeDeg().getDegree());
        Assertions.assertEquals("reC_4.lon", (double) c_4cparking.coor.getLongitudeDeg().getDegree(), (double) reC_4.getLongitudeDeg().getDegree());
        // ein zurueckrechnen von Quaternion auf heding/pitch scheint nicht moeglich. Darum Quaternion vergleichen
        // TODO 13.3.18: Ich weiss nicht was hier richtig ist.
        //TestUtils.assertQuaternion("reC_4.rotation", FlightLocation.buildRotation(reC_4,new FGDegree(c_4cparking.heading.getDegree()),new FGDegree(0)), posrot.rotation);
*/
    }

    @Test
    public void testApproachA20() throws Exception {
        GroundNet groundnet = loadGroundNetForTest(0);
        Parking a20 = groundnet.getVehicleHome();
        GraphPosition a20position = groundnet.getParkingPosition(a20);
        assertA20position(a20position);
        TestUtils.assertVector3( new Vector3(-0.6518262f, -0.7583684f, 0), a20position.currentedge.getEffectiveInboundDirection(a20.node),"a20.direction");
        Vector2 dir = MathUtil2.getDirectionFromHeading(new Degree(208.3f));
        //15.5.18: assert scheitert vorerst, solange getDirectionFromHeading nicht korrekt ist.
        //TestUtils.assertVector3("a20.direction", new Vector3(dir.x, dir.y, 0), a20position.currentedge.getEffectiveInboundDirection(a20.node));
        GraphNode nextnode = a20position.getNodeInDirectionOfOrientation();
        Assertions.assertEquals( "1", nextnode.getName(),"a20position");
    }

    @Test
    public void testTransisiton134() throws Exception {
        GroundNet groundnet = loadGroundNetForTest(0);
        GraphEdge e104_125 = groundnet.groundnetgraph.getBaseGraph().findEdgeByName("104-125");
        GraphEdge e134_124 = groundnet.groundnetgraph.getBaseGraph().findEdgeByName("134-124");
        GraphNode destinationnode = groundnet.groundnetgraph.getBaseGraph().findNodeByName("134");
        GraphTransition t = GraphUtils.createTransition(groundnet.groundnetgraph.getBaseGraph(), new GraphPosition(e104_125), e134_124, destinationnode, defaultgraphPathConstraintProvider, 244);
        //TODO assert arc
    }

    /**
     * Wegen des spitzen Winkels an 68 kann hier kein inner arc und damit keine Transition gebaut werden.
     */
    @Test
    public void testTransisiton68() throws Exception {
        GroundNet groundnet = loadGroundNetForTest(0, "EDDK", true);
        GraphEdge e64_68 = groundnet.groundnetgraph.getBaseGraph().findEdgeByName("64-68");
        GraphEdge e68_69 = groundnet.groundnetgraph.getBaseGraph().findEdgeByName("68-69");
        GraphNode destinationnode = groundnet.groundnetgraph.getBaseGraph().findNodeByName("69");
        GraphArcParameter arcpara = GraphUtils.calcArcParameterAtConnectedEdges(e64_68, e68_69,
                TrafficGraph.SMOOTHINGRADIUS, true, false);
        Assertions.assertEquals( 24.563856f, arcpara.distancefromintersection,0.0001);
        Assertions.assertEquals( -1741.1694f,  arcpara.arcbeginloc.getX(),0.0001);
        GraphPosition from = new GraphPosition(e64_68, 3, false);
        double relpos = GraphUtils.compareEdgePosition(from, arcpara.arcbeginloc);

        GraphTransition t = GraphUtils.createTransition(groundnet.groundnetgraph.getBaseGraph(), from, e68_69, destinationnode, defaultgraphPathConstraintProvider, 244);
        Assertions.assertNull( t);
    }

    @Test
    public void testEHAM() throws Exception {
        GroundNet groundnet = loadGroundNetForTest(0, "EHAM", false);
        Graph gr = groundnet.groundnetgraph.getBaseGraph();
        GraphPath path = gr.findPath(gr.findNodeByName("16"), gr.findNodeByName("2"), null);

    }

    /**
     * Aircraft wird an B_2 angenommen mit Parking Heading links hoch (ca. 330 Grad).
     */
    @Test
    public void testDoorApproachAndReturnB_2_747() throws Exception {
       // tw =  TrafficWorldConfig.readConfig("data-old", "TrafficWorld.xml"/*"GroundServices"*/);

        GroundNet groundnet = loadGroundNetForTest(0, "EDDK", false);
        Parking b_2 = groundnet.getParkPos("B_2");
        double airportelevation = /*TrafficWorld2D.getInstance().*/getAirport("EDDK").getElevation();
        ServicePoint sp = new ServicePoint(groundnet, null, b_2.node.getLocation(), b_2.heading, null, getAircraftConfiguration("747-400"),XmlVehicleDefinition.convertVehicleDefinitions(vehicleDefinitions.getVehicleDefinitions()));

        Vector3 doorpos = sp.aircraft.getCateringDoorPosition();
        Vector3 worlddoorpos = groundnet.getProjectedAircraftLocation(b_2.node.getLocation(), b_2.heading, doorpos);
        TestUtils.assertVector3(new Vector3(-1698.4425f, 1320.0948f, airportelevation), worlddoorpos,"worlddoorpos");
        GraphPosition b_2pos = groundnet.getParkingPosition(b_2);
        Vector3 dir = b_2pos.getDirection();
        Vector2 dir2 = new Vector2(dir.getX(), dir.getY());
        TestUtils.assertVector3( new Vector3(-0.31583f, 0.94882f, 0), dir,"b_2.dir");
        worlddoorpos = groundnet.getProjectedAircraftLocation(b_2.node.getLocation(), MathUtil2.getHeadingFromDirection(dir2), doorpos);
        TestUtils.assertVector3( new Vector3(-1698.4425f, 1320.0948f, airportelevation), worlddoorpos,"worlddoorpos");

        Vector3 wingpos = sp.aircraft.getWingPassingPoint();
        Vector3 worldwingpos = groundnet.getProjectedAircraftLocation(b_2.node.getLocation(), b_2.heading, wingpos);
        Vector3 worldrearpos = groundnet.getProjectedAircraftLocation(b_2.node.getLocation(), b_2.heading, sp.aircraft.getRearPoint());
        TestUtils.assertVector3(new Vector3(-1657.7429f, 1315.3242f, airportelevation), worldwingpos,"worldwingpos");
        GraphEdge dooredge = groundnet.createDoorApproach(worlddoorpos, dir2, worldwingpos, worldrearpos, sp.aircraft.getWingspread(), 0)[0];
        Assertions.assertNotNull( dooredge,"dooredge");

        GraphPosition start = new GraphPosition(groundnet.groundnetgraph.getBaseGraph().findEdgeByName("129-130"));
        // Layer 0 und das der door
        DefaultGraphWeightProvider graphWeightProvider = new DefaultGraphWeightProvider(groundnet.groundnetgraph.getBaseGraph(), 0);
        graphWeightProvider.validlayer.add(dooredge.getLayer());
        GraphPathConstraintProvider graphPathConstraintProvider = new DefaultGraphPathConstraintProvider(0/*MINIMUMPATHSEGMENTLEN*/, TrafficGraph.SMOOTHINGRADIUS);
        GraphPath doorapproach = GraphUtils.createPathFromGraphPosition(groundnet.groundnetgraph.getBaseGraph(), start, dooredge.from,
                graphWeightProvider, graphPathConstraintProvider, dooredge.getLayer(), true, false, null);
        Assertions.assertNotNull(doorapproach,"doorapproach");
        Assertions.assertEquals("130:smoothbegin.131->smootharc(19)->smoothbegin.101(79)->smootharc(13)->smoothbegin.wing1(7)->smootharc(3)->smoothbegin.wing0(10)->smootharc(9)->smoothbegin.door1(19)->smootharc(7)->smoothend.door1(12)", doorapproach.toString(),"path");
        validatePathAltitude(groundnet, doorapproach);
        // und jetzt return. 
        GraphPath doorreturnpath = sp.getDoorReturnPath(true);
        Assertions.assertNotNull(doorreturnpath,"doorreturnpath");
        //alllayerbypassAssertions.assertEquals("doorreturnpath", "[back on smootharc]ex:e->smoothbegin.wing0(20)->smootharc(9)->smoothbegin.101(20)->smootharc(12)->smoothbegin.131(80)->smootharc(19)->smoothbegin.130(14)->smootharc(0)->smoothbegin.129(160)->smootharc(14)->smoothbegin.68(83)->smootharc(0)->smoothbegin.69(14)->smootharc(12)->smoothbegin.63(52)->smootharc(0)->smoothend.63(28)", doorreturnpath.toString());
        //mit bypass nur in layer 0:
        //13.3.19: kleinere Längenanpassungen
        Assertions.assertEquals( "[back on smootharc]ex:e->smoothbegin.wing0(23)->smootharc(9)->smoothbegin.wing1(10)->smootharc(3)->smoothbegin.101(7)->smootharc(13)->smoothbegin.131(79)->smootharc(19)->smoothbegin.130(14)->smootharc(0)->smoothbegin.129(160)->smootharc(14)->smoothbegin.68(83)->smootharc(0)->smoothbegin.69(14)->smootharc(12)->smoothbegin.63(52)->smootharc(0)->smoothend.63(28)", doorreturnpath.toString(),"doorreturnpath");
        validatePathAltitude(groundnet, doorreturnpath);

    }



    private void validatePathAltitude(GroundNet groundnet, GraphPath doorapproach) {
        double airportelevation = /*TrafficWorld2D.getInstance().*/getAirport("EDDK").getElevation();

        for (int i = 0; i < doorapproach.getSegmentCount(); i++) {
            GraphPathSegment s = doorapproach.getSegment(i);
            Assertions.assertEquals( airportelevation, s.edge.from.getLocation().getZ(),"z");
            Assertions.assertEquals( airportelevation, s.edge.to.getLocation().getZ(),"z");
        }
    }

    /**
     * zusätzlich zu testDoorApproachAndReturnB_2_747
     * Auch als Referenz für Tests in FG.
     */
    @Test
    public void testServicePoint747_B2() throws Exception {
      //  tw =  TrafficWorldConfig.readConfig("data-old", "TrafficWorld.xml"/*"GroundServices"*/);

        GroundNet groundnet = loadGroundNetForTest(0, "EDDK", false);
        Parking b_2 = groundnet.getParkPos("B_2");
        double airportelevation = /*TrafficWorld2D.getInstance().*/getAirport("EDDK").getElevation();

        ServicePoint sp = new ServicePoint(groundnet, null, b_2.node.getLocation(), b_2.heading, null, /*TrafficWorld2D.getInstance().getConfiguration().*/getAircraftConfiguration("747-400"),XmlVehicleDefinition.convertVehicleDefinitions(vehicleDefinitions.getVehicleDefinitions()));
        TestUtils.assertVector3( new Vector3(-1698.4425f, 1320.0948f, airportelevation), sp.prjdoorpos,"prjdoorpos");
        Assertions.assertEquals( 4, sp.wingreturn.getLayer(),"wingreturnlayer");
        double wingapproachlen = sp.wingapproach.getLength();
        Assertions.assertEquals(30, wingapproachlen,"wingapproachlen");
        // ein fuel truck approach. Skizze 32.
        GraphPosition start = new GraphPosition(groundnet.groundnetgraph.getBaseGraph().findEdgeByName("129-130"));
        // erstmal ohne smoothing.
        GraphPath path = sp.getApproach(start, sp.wingedge.to, false);
        //bypasslayer0Assertions.assertEquals("path", "130:e1->turnloop.smootharc(69)->e2(20)->100-130(88)->bypass(44)->wingapproach(30)->wingedge(20)", path.toString());
        Assertions.assertEquals( "130:e1->turnloop.smootharc(69)->e2(20)->100-130(88)->202-100(27)->branchedge(18)->wingapproach(30)->wingedge(20)", path.toString(),"path");
        groundnet.groundnetgraph.getBaseGraph().removeLayer(path.layer);

        // der Rueckweg
        path = sp.getWingReturnPath(false);
        Assertions.assertEquals("outernode:return0->return1(20)->return12(19)->bypass(45)->65-64(53)->64-68(39)->68-69(21)->63-69(59)->1-63(28)", path.toString(),"path");
        groundnet.groundnetgraph.getBaseGraph().removeLayer(path.layer);

        path = sp.getWingReturnPath(true);
        // Der gesmoothte Returnpath hat noch ein Artefakt bei 69
        Assertions.assertEquals("outernode:smoothbegin.->smootharc(12)->smoothbegin.ex(8)->smootharc(10)->smoothbegin.3(1)->smootharc(18)->smoothbegin.65(33)->smootharc(0)->smoothbegin.64(49)->smootharc(9)->smoothbegin.68(10)->smootharc(24)->smoothend.68(0)->smoothbegin.63(59)->smootharc(0)->smoothend.63(28)", path.toString(),"path");
        groundnet.groundnetgraph.getBaseGraph().removeLayer(path.layer);
        // 13.3.19: das Gleiche nochmal mit multilane, da kam schon mal ein ungültiger Path raus.
        groundnet.groundnetgraph.multilaneenabled = true;
        path = sp.getWingReturnPath(true);
        String msg = path.validate();
        if (msg != null) {
            fail(msg);
        }
        groundnet.groundnetgraph.getBaseGraph().removeLayer(path.layer);
        groundnet.groundnetgraph.multilaneenabled = false;

        // und jetzt mal von A20 zur door. Da darf er bei catering nicht unter dem aircraft fahren.
        GraphEdge edge1_201 = groundnet.groundnetgraph.getBaseGraph().findEdgeByName("1-201");
        start = new GraphPosition(edge1_201);
        path = sp.getApproach(start, sp.doorEdge.from, false);
        //13.3.19: doortowing(25)->27
        Assertions.assertEquals("201:e1->turnloop.smootharc(9)->e2(20)->201-63(23)->63-69(59)->68-69(21)->129-68(91)->129-130(169)->130-131(27)->101-131(100)->branchedge(16)->wingedge(16)->door2wing(27)->dooredge(16)", path.toString(),"path");
        groundnet.groundnetgraph.getBaseGraph().removeLayer(path.layer);
        path = sp.getApproach(start, sp.wingedge.to, false);
        //TODO Der Fuel approach hat hier einfach Artefakte aufgrund der spzeillen B_2 Anordnung. Dass muss ich mit strict/advanced mal angehen.
        //Assertions.assertEquals("path", "", path.toString());
        groundnet.groundnetgraph.getBaseGraph().removeLayer(path.layer);

        // Der Returnpath nach home steht jetzt schon fest. Der Approach aber nicht, denn der hängt vom Vehicle ab.
        // 15.8.17: Ist aber inkonsisten, wenn der jetzt schon feststeht. Pfade entstehen sonst immer
        // erst unmittelbar vor benutzen. Sonst wird er nicht freigegeben.
        /*Schedule schedule = new Schedule(sp, groundnet);
        schedule.addAction(new VehicleOrderAction(schedule, GroundServiceComponent.VEHICLE_CATERING, sp.doorEdge.from));
        schedule.addAction(new VehicleServiceAction(schedule, 0));
        schedule.addAction(new VehicleReturnAction(schedule, true, sp, true));
        // weiterer Schedule fuer fule, damit der parallel läuft
        schedule = new Schedule(sp, groundnet);
        schedule.addAction(new VehicleOrderAction(schedule, GroundServiceComponent.VEHICLE_FUELTRUCK, sp.wingedge.to));
        schedule.addAction(new VehicleServiceAction(schedule, 0));
        schedule.addAction(new VehicleReturnAction(schedule, false, sp, false));*/

        testUturnA20ServicePoint747_B2(groundnet, sp);
        //groundnet ist multilane now. Path back from door.
        path = sp.getDoorReturnPath(false);
        //13.3.19: Ein paar Längenanpassungen
        Assertions.assertEquals( "[back on smootharc]ex:e->toOutline1(27)->toOutline2(16)->toOutline3(16)->toOutline4(91)->toOutline5(18)->toOutline6(161)->toOutline7(83)->toOutline8(28)->reenter(66)->last(28)", path.toString(),"path");
        groundnet.groundnetgraph.getBaseGraph().removeLayer(path.layer);
        // per multilane fromn A20 (1-201) to door. turnloop must fit to outline 
        start = new GraphPosition(edge1_201, edge1_201.getLength(), true);
        path = sp.getApproach(start, sp.doorEdge.from, false);
        //13.3.19: reenter 25->27
        Assertions.assertEquals( "1:e1->turnloop.smootharc(24)->e2(20)->toOutline1(31)->toOutline2(52)->toOutline3(15)->toOutline4(99)->toOutline5(176)->toOutline6(37)->toOutline7(110)->toOutline8(16)->toOutline9(16)->reenter(27)->last(16)", path.toString(),"path");
        //13.3.19:reenter 25->27
        Assertions.assertEquals("1:e1--ex-->turnloop.smootharc(24)--ex-->e2(20)--1-->toOutline1(31)--outline1@63-->toOutline2(52)--outline2@69-->toOutline3(15)--outline3@68-->toOutline4(99)--outline4@129-->toOutline5(176)--outline5@130-->toOutline6(37)--outline6@131-->toOutline7(110)--outline7@101-->toOutline8(16)--outline8@wing1-->toOutline9(16)--outline9@wing0-->reenter(27)--door1-->last(16)", path.getDetailedString(),"path");
        groundnet.groundnetgraph.getBaseGraph().removeLayer(path.layer);

        sp.delete();
        Assertions.assertEquals( 269, groundnet.groundnetgraph.getBaseGraph().getEdgeCount(),"edges");
    }

    @Test
    public void testUturnA20ServicePoint747_B2() throws Exception {
       // tw =  TrafficWorldConfig.readConfig("data-old", "TrafficWorld.xml"/*"GroundServices"*/);

        GroundNet groundnet = loadGroundNetForTest(0, "EDDK", false);
        Parking b_2 = groundnet.getParkPos("B_2");
        ServicePoint sp = new ServicePoint(groundnet, null, b_2.node.getLocation(), b_2.heading, null, /*TrafficWorld2D.getInstance().getConfiguration().*/getAircraftConfiguration("747-400"),XmlVehicleDefinition.convertVehicleDefinitions(vehicleDefinitions.getVehicleDefinitions()));
        testUturnA20ServicePoint747_B2(groundnet, sp);
    }

    /**
     * subtest from above
     * Per multilane/outline from A20 (1-63, not 1-201, for fitting U-Turn) to door. Must begin with UTurn. First without, then with smoothing.
     */
    private void testUturnA20ServicePoint747_B2(GroundNet groundnet, ServicePoint sp) {
        groundnet.groundnetgraph.multilaneenabled = true;
        GraphEdge edge1_63 = groundnet.groundnetgraph.getBaseGraph().findEdgeByName("1-63");
        GraphPosition start = new GraphPosition(edge1_63, edge1_63.getLength(), true);
        GraphPath path = sp.getApproach(start, sp.doorEdge.from, false);
        GraphNode node131 = groundnet.groundnetgraph.getBaseGraph().findNodeByName("131");
        System.out.println(path);
        GraphPathSegment segToOutline2at69 = path.getSegment(4);
        // die Werte sind plausibel zur 69
        TestUtils.assertVector3(new Vector3(-1746, 1111, 87), segToOutline2at69.getLeaveNode().getLocation(), 1,"outline an 69");
        GraphPathSegment segToOutlineat129 = path.getSegment(6);
        // die Werte sind plausibel zur 129.
        TestUtils.assertVector3(new Vector3(-1638, 1078, 87), segToOutlineat129.getLeaveNode().getLocation(), 1,"outline an 129");
        GraphPathSegment segToOutlineat131 = path.getSegment(8);
        // die Werte sind plausibel zur 131
        TestUtils.assertVector3( new Vector3(-1542, 1268, 87), segToOutlineat131.getLeaveNode().getLocation(), 1,"outline an 131");
        groundnet.groundnetgraph.getBaseGraph().removeLayer(path.layer);

        path = sp.getApproach(start, sp.doorEdge.from, true);
        //turnloop isType indicator for failed bypassshorties
        Assertions.assertFalse( path.toString().contains("turnloop"),"turnloop in path");
        for (int i = 0; i < path.getSegmentCount(); i++) {
            GraphPathSegment seg = path.getSegment(i);
            //System.out.println("enternode:" + seg.getEnterNode().getLocation());
            Assertions.assertEquals( GroundNetMetadata.DEFAULTELEVATION, seg.getEnterNode().getLocation().getZ(),"outline.z");
        }
        // despite outline the end must be at the door. Must be tested at edge because vehicle stops before doorpos.
        TestUtils.assertVector3( sp.doorEdge.getFrom().getLocation(), path.getSegment(path.getSegmentCount() - 1).getLeaveNode().getLocation(),"outline.end");
        groundnet.groundnetgraph.getBaseGraph().removeLayer(path.layer);
    }

    /**
     * Und noch einer fuer FG. C_4 war in FG mal entartet (wegen falscher config).
     */
    @Test
    public void testServicePoint737_C4() throws Exception {
        //tw =  TrafficWorldConfig.readConfig("data-old", "TrafficWorld.xml"/*"GroundServices"*/);

        GroundNet groundnet = loadGroundNetForTest(0, "EDDK", false);
        Parking c_4 = groundnet.getParkPos("C_4");
        double airportelevation = /*TrafficWorld2D.getInstance().*/getAirport("EDDK").getElevation();
        ServicePoint sp = new ServicePoint(groundnet, null, c_4.node.getLocation(), c_4.heading, null, /*TrafficWorld2D.getInstance().getConfiguration().*/getAircraftConfiguration("738"),XmlVehicleDefinition.convertVehicleDefinitions(vehicleDefinitions.getVehicleDefinitions()));
        TestUtils.assertVector3( new Vector3(-1605.632f, 1535.3408f, airportelevation), sp.prjdoorpos,"prjdoorpos");
        TestUtils.assertVector3( new Vector3(-1592.258f, 1519.9026f, airportelevation), sp.prjleftwingapproachpoint,"prjleftwingapproachpoint");
        Assertions.assertEquals(4, sp.wingreturn.getLayer(),"wingreturnlayer");
        TestUtils.assertVector3( new Vector3(-1592.258f, 1519.9026f, /*TrafficWorld2D.getInstance().*/getAirport("EDDK").getElevation()), sp.wingedge.from.getLocation(),"wingedge.from");
        TestUtils.assertVector3( new Vector3(-1591.56f, 1499.9138f, /*TrafficWorld2D.getInstance().*/getAirport("EDDK").getElevation()), sp.wingedge.to.getLocation(),"wingedge.to");
        Assertions.assertEquals(TrafficGraph.MINIMUMPATHSEGMENTLEN, sp.wingedge.getLength(),0.01,"wingedge.length");
        TestUtils.assertVector3( new Vector3(-1591.211f, 1489.919f, /*TrafficWorld2D.getInstance().*/getAirport("EDDK").getElevation()), sp.wingreturn1.from.getLocation(),"wingreturn1.from");
        TestUtils.assertVector3(new Vector3(-1572.19f, 1483.7386f, /*TrafficWorld2D.getInstance().*/getAirport("EDDK").getElevation()), sp.wingreturn1.to.getLocation(),"wingreturn1.to");

        double wingapproachlen = sp.wingapproach.getLength();
        Assertions.assertEquals( 30, wingapproachlen,0.000001,"wingapproachlen");
        TestUtils.assertVector3( new Vector3(-1570.4719f, 1522.2318f, /*TrafficWorld2D.getInstance().*/getAirport("EDDK").getElevation()), sp.prjrearpoint,"prjrearpoint");
        double doorbranchedgelen = sp.doorbranchedge.getLength();
        Assertions.assertEquals(152.25296f, doorbranchedgelen,0.0001, "doorbranchedgelen");
        double wingbranchedgelen = sp.wingbranchedge.getLength();
        Assertions.assertEquals( 129.43648f, wingbranchedgelen,0.0001, "wingbranchedgelen");
        Assertions.assertEquals( "134-124(134->124)", sp.wingbestHitEdge.toString(),"wingbestHitEdge");


        // ein fuel truck approach. Skizze 32.
        GraphPosition start = new GraphPosition(groundnet.groundnetgraph.getBaseGraph().findEdgeByName("129-130"));
        // erstmal ohne smoothing.
        GraphPath path = sp.getApproach(start, sp.wingedge.to, false);
        Assertions.assertEquals( "130:130-131->131-132(107)->bypass(62)->134-124(93)->branchedge(129)->wingapproach(30)->wingedge(20)", path.toString(),"path");
        groundnet.groundnetgraph.getBaseGraph().removeLayer(path.layer);

        // der Rueckweg
        path = sp.getWingReturnPath(false);
        Assertions.assertEquals( "outernode:return0->return1(20)->bypass(47)->bypass(110)->bypass(106)->132-133(48)->131-132(107)->130-131(27)->129-130(169)->129-68(91)->68-69(21)->63-69(59)->1-63(28)", path.toString(),"path");
        groundnet.groundnetgraph.getBaseGraph().removeLayer(path.layer);

        path = sp.getWingReturnPath(true);
        // 30.11.17: Ergebnis einfach so übernommen.
        Assertions.assertEquals("outernode:smoothbegin.->smootharc(12)->smoothbegin.ex(6)->smootharc(12)->smoothbegin.104(34)->smootharc(11)->smoothbegin.124(91)->smootharc(18)->smoothbegin.133(93)->smootharc(0)->smoothbegin.132(48)->smootharc(0)->smoothbegin.131(107)->smootharc(0)->smoothbegin.130(27)->smootharc(0)->smoothbegin.129(160)->smootharc(14)->smoothbegin.68(83)->smootharc(0)->smoothbegin.69(14)->smootharc(12)->smoothbegin.63(52)->smootharc(0)->smoothend.63(28)", path.toString(),"path");
        groundnet.groundnetgraph.getBaseGraph().removeLayer(path.layer);

        sp.delete();
        Assertions.assertEquals( 269, groundnet.groundnetgraph.getBaseGraph().getEdgeCount(),"edges");
    }

    /**
     * Und noch einer fuer FG. (0,0; unknown parkpos) war in FG mal entartet. Das liegt daran, dass von der wingedge keine vernuenftige hitedge gefunden wird (Wiki).
     * Auch ganz gut, um die Aircraftpositionen einzuschaetzen, weil es keine transform gibt.
     */
    @Test
    public void testServicePoint737_00() throws Exception {
        //tw =  TrafficWorldConfig.readConfig("data-old", "TrafficWorld.xml"/*"GroundServices"*/);

        GroundNet groundnet = loadGroundNetForTest(0, "EDDK", false);
        double airportelevation = /*TrafficWorld2D.getInstance().*/getAirport("EDDK").getElevation();

        ServicePoint sp = new ServicePoint(groundnet, null, new Vector3(), new Degree(0), null, /*TrafficWorld2D.getInstance().getConfiguration().*/
                getAircraftConfiguration("738"),XmlVehicleDefinition.convertVehicleDefinitions(vehicleDefinitions.getVehicleDefinitions()));
        TestUtils.assertVector3(new Vector3(1.6023995f, 9.489999f, airportelevation), sp.prjdoorpos,"prjdoorpos");
        /*TestUtils.assertVector3("prjleftwingapproachpoint", new Vector3(-1592.258f, 1519.9026f, groundnet.airportelevation), sp.prjleftwingapproachpoint);
        Assertions.assertEquals("wingreturnlayer", 4, sp.wingreturn.getLayer());
        TestUtils.assertVector3("wingedge.from", new Vector3(-1592.258f, 1519.9026f, groundnet.airportelevation), sp.wingedge.from.getLocation());
        TestUtils.assertVector3("wingedge.to", new Vector3(-1591.56f, 1499.9138f, groundnet.airportelevation), sp.wingedge.to.getLocation());
        TestUtil.assertEquals("wingedge.length", GroundNet.MINIMUMPATHSEGMENTLEN, sp.wingedge.getLength());
        TestUtil.assertVector3("wingreturn1.from", new Vector3(-1591.211f, 1489.919f, groundnet.airportelevation), sp.wingreturn1.from.getLocation());
        TestUtil.assertVector3("wingreturn1.to", new Vector3(-1572.19f, 1483.7386f, groundnet.airportelevation), sp.wingreturn1.to.getLocation());

        float wingapproachlen = sp.wingapproach.getLength();
        TestUtil.assertFloat("wingapproachlen", 30, wingapproachlen);
        TestUtil.assertVector3("prjrearpoint", new Vector3(-1570.4719f, 1522.2318f, groundnet.airportelevation), sp.prjrearpoint);
        float doorbranchedgelen = sp.doorbranchedge.getLength();
        TestUtil.assertFloat("doorbranchedgelen", 152.25296f, doorbranchedgelen);
        float wingbranchedgelen = sp.wingbranchedge.getLength();
        TestUtil.assertFloat("wingbranchedgelen", 129.43648f, wingbranchedgelen);
        TestUtil.assertEquals("wingbestHitEdge", "134-124(134->124)", sp.wingbestHitEdge.toString());*/

        GraphPosition start = new GraphPosition(groundnet.groundnetgraph.getBaseGraph().findEdgeByName("1-201"));
        GraphPath path = sp.getApproach(start, sp.doorEdge.from, false);
        //13.3.19: doortowing(15)->16
        Assertions.assertEquals( "201:e1->turnloop.smootharc(9)->e2(20)->201-63(23)->63-69(59)->68-69(21)->129-68(91)->128-129(305)->127-128(91)->126-127(34)->150-126(54)->150-152(148)->152-149(84)->149-148(50)->bypass(56)->139-140(139)->140-141(113)->141-142(147)->142-143(351)->41-143(123)->branchedge(1930)->wingedge(9)->door2wing(16)->dooredge(9)", path.toString(),"path");
        /*path = sp.getApproach(start, sp.doorEdge.from, true);
        TestUtil.assertEquals("path", "130:smoothbegin.131->smootharc(0)->smoothbegin.132(95)->smootharc(17)->smoothbegin.wing1(136)->smootharc(0)->smoothbegin.wing0(5)->smootharc(7)->smoothbegin.door1(4)->smootharc(8)->smoothend.door1(5)", path.toString());
        // der Rueckweg.  Referenz mal so uebernommen. Scheint plausibel
        path = sp.getDoorReturnPath(false);
        TestUtil.assertEquals("path", "[back on smootharc]ex:e->door2wing(12)->wingedge(9)->branchedge(148)->131-132(107)->130-131(27)->129-130(169)->129-68(91)->68-69(21)->63-69(59)->1-63(28)", path.toString());
        path = sp.getDoorReturnPath(true);
        TestUtil.assertEquals("path", "[back on smootharc]ex:e->smoothbegin.wing0(8)->smootharc(7)->smoothbegin.wing1(5)->smootharc(0)->smoothbegin.132(136)->smootharc(17)->smoothbegin.131(95)->smootharc(0)->smoothbegin.130(27)->smootharc(0)->smoothbegin.129(160)->smootharc(14)->smoothbegin.68(83)->smootharc(0)->smoothbegin.69(14)->smootharc(12)->smoothbegin.63(52)->smootharc(0)->smoothend.63(28)", path.toString());

        // ein fuel truck approach. Skizze 32.
        // erstmal ohne smoothing.
        path = sp.getApproach(start, sp.wingedge.to, false);
        TestUtil.assertEquals("path", "130:130-131->131-132(107)->branchedge(126)->wingapproach(30)->wingedge(20)", path.toString());
        path = sp.getApproach(start, sp.wingedge.to, true);
        //Auch mal so uebernommen.
        TestUtil.assertEquals("path", "130:smoothbegin.131->smootharc(0)->smoothbegin.132(92)->smootharc(19)->smoothbegin.enternode(110)->smootharc(2)->smoothbegin.innernode(15)->smootharc(19)->smoothend.innernode(6)", path.toString());
        // der Rueckweg
        path = sp.getWingReturnPath(false);
        TestUtil.assertEquals("path", "outernode:return0->return1(20)->bypass(44)->102-132(113)->131-132(107)->130-131(27)->129-130(169)->129-68(91)->68-69(21)->63-69(59)->1-63(28)", path.toString());
        path = sp.getWingReturnPath(true);
        TestUtil.assertEquals("path", "outernode:smoothbegin.->smootharc(12)->smoothbegin.ex(5)->smootharc(13)->smoothbegin.102(29)->smootharc(12)->smoothbegin.132(93)->smootharc(18)->smoothbegin.131(93)->smootharc(0)->smoothbegin.130(27)->smootharc(0)->smoothbegin.129(160)->smootharc(14)->smoothbegin.68(83)->smootharc(0)->smoothbegin.69(14)->smootharc(12)->smoothbegin.63(52)->smootharc(0)->smoothend.63(28)", path.toString());*/
    }

    @Test
    public void testServicePoint737_B8() throws Exception {
        //tw =  TrafficWorldConfig.readConfig("data-old", "TrafficWorld.xml"/*"GroundServices"*/);

        GroundNet groundnet = loadGroundNetForTest(0, "EDDK", false);
        Parking b_8 = groundnet.getParkPos("B_8");
        ServicePoint sp = new ServicePoint(groundnet, null, b_8.node.getLocation(), b_8.heading, null, /*TrafficWorld2D.getInstance().getConfiguration().*/getAircraftConfiguration("738"),XmlVehicleDefinition.convertVehicleDefinitions(vehicleDefinitions.getVehicleDefinitions()));
        double wingapproachlen = sp.wingapproach.getLength();
        Assertions.assertEquals( 30, wingapproachlen,0.0001, "wingapproachlen");
        GraphPosition start = new GraphPosition(groundnet.groundnetgraph.getBaseGraph().findEdgeByName("129-130"));

        //door
        GraphPath path = sp.getApproach(start, sp.doorEdge.from, false);
        //13.3.19: doortowing(15)->16
        Assertions.assertEquals("130:130-131->131-132(107)->branchedge(148)->wingedge(9)->door2wing(16)->dooredge(9)", path.toString(),"path");
        path = sp.getApproach(start, sp.doorEdge.from, true);
        //13.3.19: Ein paar edgelängen haben sich leicht verändert, warum auch immer(??)
        Assertions.assertEquals("130:smoothbegin.131->smootharc(0)->smoothbegin.132(95)->smootharc(17)->smoothbegin.wing1(136)->smootharc(0)->smoothbegin.wing0(6)->smootharc(6)->smoothbegin.door1(8)->smootharc(10)->smoothend.door1(3)", path.toString(),"path");
        // der Rueckweg.  Referenz mal so uebernommen. Scheint plausibel
        path = sp.getDoorReturnPath(false);
        //13.3.19: door2wing 15->16
        Assertions.assertEquals( "[back on smootharc]ex:e->door2wing(16)->wingedge(9)->branchedge(148)->131-132(107)->130-131(27)->129-130(169)->129-68(91)->68-69(21)->63-69(59)->1-63(28)", path.toString(),"path");
        path = sp.getDoorReturnPath(true);
        //13.3.19: Ein paar kleinere Längenanpassungen
        Assertions.assertEquals( "[back on smootharc]ex:e->smoothbegin.wing0(13)->smootharc(6)->smoothbegin.wing1(6)->smootharc(0)->smoothbegin.132(136)->smootharc(17)->smoothbegin.131(95)->smootharc(0)->smoothbegin.130(27)->smootharc(0)->smoothbegin.129(160)->smootharc(14)->smoothbegin.68(83)->smootharc(0)->smoothbegin.69(14)->smootharc(12)->smoothbegin.63(52)->smootharc(0)->smoothend.63(28)", path.toString(),"path");

        // ein fuel truck approach. Skizze 32.
        // erstmal ohne smoothing.
        path = sp.getApproach(start, sp.wingedge.to, false);
        Assertions.assertEquals( "130:130-131->131-132(107)->branchedge(126)->wingapproach(30)->wingedge(20)", path.toString(),"path");
        path = sp.getApproach(start, sp.wingedge.to, true);
        //Auch mal so uebernommen.
        Assertions.assertEquals( "130:smoothbegin.131->smootharc(0)->smoothbegin.132(92)->smootharc(19)->smoothbegin.enternode(110)->smootharc(2)->smoothbegin.innernode(15)->smootharc(19)->smoothend.innernode(6)", path.toString(),"path");
        // der Rueckweg
        path = sp.getWingReturnPath(false);
        Assertions.assertEquals("outernode:return0->return1(20)->bypass(44)->102-132(113)->131-132(107)->130-131(27)->129-130(169)->129-68(91)->68-69(21)->63-69(59)->1-63(28)", path.toString(),"path");
        path = sp.getWingReturnPath(true);
        Assertions.assertEquals("outernode:smoothbegin.->smootharc(12)->smoothbegin.ex(5)->smootharc(13)->smoothbegin.102(29)->smootharc(12)->smoothbegin.132(93)->smootharc(18)->smoothbegin.131(93)->smootharc(0)->smoothbegin.130(27)->smootharc(0)->smoothbegin.129(160)->smootharc(14)->smoothbegin.68(83)->smootharc(0)->smoothbegin.69(14)->smootharc(12)->smoothbegin.63(52)->smootharc(0)->smoothend.63(28)", path.toString(),"path");

        // von Sueden anfahren
        start = new GraphPosition(groundnet.groundnetgraph.getBaseGraph().findEdgeByName("68-69"));
        path = sp.getApproach(start, sp.wingedge.to, false);
        Assertions.assertEquals( "69:teardrop.smootharc->teardrop.branch(21)->129-68(91)->129-130(169)->130-131(27)->131-132(107)->branchedge(126)->wingapproach(30)->wingedge(20)", path.toString(),"path");

    }

    private VehicleDefinition getAircraftConfiguration(String modeltype) {
        //return TrafficWorldConfig.getAircraftConfiguration(airportDefinitions, s);
        //GroundServiceAircraftConfig c = aircrafts.get(type);
        // the first should be sufficient
        VehicleConfigDataProvider vcdp = new VehicleConfigDataProvider(
                XmlVehicleDefinition.convertVehicleDefinitions(vehicleDefinitions.getVehicleDefinitions()));

        return vcdp.findVehicleDefinitionsByModelType(modeltype).get(0);
    }

    private AirportConfig getAirport(String icao) {

        //return null;//27.12.21 TrafficWorld2D.getInstance().getConfiguration().getAircraftConfiguration("738");

        //20.11.23 refactored
        // return tw.getAirportConfig(icao);

        AirportDefinition definition = airportDefinitions.findAirportDefinitionsByIcao(icao).get(0);
        // constructor extracted from previous implementation only setting icao and vehicle list.
        // remainign parts are set somewhere else?
        return new AirportConfig(icao, definition.getVehicles());
    }


    /**
     * 15.8.17: Der path ist halt problemtisch. Kein TO DO mehr, weil es dafür smartere Wege, vleiicht sogar lanes geben muss.
     */
    @Test
    public void testPathToA20() throws Exception {
        GroundNet groundnet = loadGroundNetForTest(0, "EDDK", false);
        //List<GraphPath> paths = GroundServicesScene.buildPathCollection(groundnet);
        //nicht ueber collection, um dedizierter testen zu können
        GraphEdge e3_202 = groundnet.groundnetgraph.getBaseGraph().findEdgeByName("3-202");
        GraphPosition start = new GraphPosition(e3_202, 0, true);
        GraphNode a20 = groundnet.getParkPos("A20").node;
        // erstmal ohne smoothing. Die turnloop an 3 muss es aber z.Z. geben.
        GraphPathConstraintProvider graphPathConstraintProvider = new DefaultGraphPathConstraintProvider(TrafficGraph.MINIMUMPATHSEGMENTLEN, TrafficGraph.SMOOTHINGRADIUS);

        GraphPath path = GraphUtils.createPathFromGraphPosition(groundnet.groundnetgraph.getBaseGraph(), start, a20,
                null, graphPathConstraintProvider, 23, false, false, null);
        Assertions.assertEquals( "3:e1->turnloop.smootharc(325)->e2(20)->bypass(45)->65-64(53)->64-68(39)->68-69(21)->63-69(59)->1-63(28)", path.toString(),"unsmoothed path");

        // und jetzt mit smoothing
        path = groundnet.groundnetgraph.createPathFromGraphPosition(start, a20, null, null);

        //GraphPath path = paths.get(0);
        Assertions.assertNotNull( path,"path");
        String s = path.toString();
        // 203 muss gebypassed sein
        Assertions.assertFalse(s.contains("203"),"node 203");
        // path pruefen   TestUtil.assertEquals("path", "", s);
    }

    /**
     * Den Bogen zur Runway fahren. Der muss gesmoothed werden. (REQ1)
     */
    @Test
    public void testPathTo186() throws Exception {
        GroundNet groundnet = loadGroundNetForTest(0, "EDDK", false);
        GraphPosition start = new GraphPosition(groundnet.groundnetgraph.getBaseGraph().findEdgeByName("133-134"));
        GraphPath path = groundnet.groundnetgraph.createPathFromGraphPosition(start, groundnet.groundnetgraph.getBaseGraph().findNodeByName("186"), null, null);
        Assertions.assertEquals( "134:smoothbegin.125->smootharc(1)->smoothbegin.123(45)->smootharc(3)->smoothbegin.122(49)->smootharc(2)->smoothbegin.121(45)->smootharc(1)->smoothbegin.183(58)->smootharc(2)->smoothbegin.184(27)->smootharc(1)->smoothbegin.185(77)->smootharc(2)->smoothend.185(78)", path.toString(),"path");
    }

    @Test
    public void testfindHitEdge() throws Exception {
        GroundNet groundnet = loadGroundNetForTest(0, "EDDK", false);

        GraphNode n130 = groundnet.groundnetgraph.getBaseGraph().findNodeByName("130");
        Object[] o = groundnet.findHitEdge(new PositionHeading(n130.getLocation(), new Degree(90)));
        Assertions.assertNotNull( o);
        o = groundnet.findHitEdge(new PositionHeading(n130.getLocation(), new Degree(180)));
        Assertions.assertNotNull( o);
        GraphNode n1 = groundnet.groundnetgraph.getBaseGraph().findNodeByName("1");
        o = groundnet.findHitEdge(new PositionHeading(n1.getLocation(), new Degree(90)));
        Assertions.assertNotNull( o);
        // nach links von der 1 gibt es aber nichts. Er keonnte sich aber selber treffen,darum noch ein Stück nach links.
        o = groundnet.findHitEdge(new PositionHeading(n1.getLocation().add(new Vector3(-10, 0, 0)), new Degree(-90)));
        Assertions.assertNull( o);
    }

    @Test
    public void testEDDF() throws Exception {
        GroundNet groundnet = loadGroundNetForTest(0, "EDDF", false);
        Graph gr = groundnet.groundnetgraph.getBaseGraph();
        Parking v109 = groundnet.getParkPos("V109");
        LatLon v109g = groundnet.projection.unproject(Vector2.buildFromVector3(v109.node.getLocation()));
        //50.049999, 8.591414 mit Google und anderen Umrechnern ermittelt
        FgTestUtils.assertSGGeod("", SGGeod.fromLatLon(new Degree(50.049999f), new Degree(8.591414f)), SGGeod.fromLatLon(v109g));
        //50.050425 8.591264
        GraphNode n223 = groundnet.groundnetgraph.getBaseGraph().findNodeByName("223");
        LatLon n223g = groundnet.projection.unproject(Vector2.buildFromVector3(n223.getLocation()));
        FgTestUtils.assertSGGeod("", SGGeod.fromLatLon(new Degree(50.050425f), new Degree(8.591264f)), SGGeod.fromLatLon(n223g));
        //heading laut groundnet ="160.6", Der Onlinerechner kommt auf einen Peilwinkel von 347.26. Ich bin mal grosszügug, aber die FG/SGGeod Berechnung stimmt.->Dann stimmt die Angabe im groundnet nicht.
        Degree heading = SGGeodesy.courseDeg(v109g, n223g);
        Assertions.assertEquals( 347.225f, (double) heading.getDegree(),0.01,"heading v109");
        GraphEdge v109approach = v109.getApproach();
        //Assertions.assertEquals("v109approach",347.225f,(double)v109approach.degree);

    }

    private void assertA20position(GraphPosition a20position) {
        Assertions.assertNotNull(a20position,"a20position");
        //14.5.18: Mit "true" Heading Berechnung ist die Edge die 1-63, wie in FG.
        //Assertions.assertEquals("a20position", "1-201", a20position.currentedge.getName());
        // Assertions.assertEquals("a20position",/*23.5.17 0*/50.180775f, a20position.edgeposition);
        Assertions.assertEquals("1-63", a20position.currentedge.getName(),"a20position");
        Assertions.assertEquals( 28.146395f, a20position.edgeposition,0.0001,"a20position");
        Assertions.assertTrue( a20position.isReverseOrientation(),"a20position.reverse");
    }

    private void assertC_4position(GraphPosition c_4position) {
        Assertions.assertNotNull( c_4position,"c_4position");
        Assertions.assertEquals("6-206", c_4position.currentedge.getName(),"c_4position");
        Assertions.assertEquals( 50.182262f, c_4position.edgeposition,0.0001, "c_4position");
        Assertions.assertTrue(c_4position.isReverseOrientation(),"c_4position.reverse");
    }

    private void assertPosition(GraphPosition expected, GraphPosition actual) {
        Assertions.assertNotNull( actual,"position");
        Assertions.assertEquals(expected.currentedge.getName(), actual.currentedge.getName(),"position.edge");
        Assertions.assertEquals( expected.edgeposition, actual.edgeposition,"position.position");
        Assertions.assertTrue( expected.isReverseOrientation() == actual.isReverseOrientation(),"position.reverse");
    }

    private GroundNet loadGroundNetForTest(int smoothmode) throws NoElevationException {
        return loadGroundNetForTest(smoothmode, "EDDK", false);
    }

    private GroundNet loadGroundNetForTest(int smoothmode, String icao, boolean strict) throws NoElevationException {
        //TrafficWorldConfig tw =  TrafficWorldConfig.readConfig("data-old", "TrafficWorld.xml"/*"GroundServices"*/);
        //27.12.21 TrafficWorld2D gsw = new TrafficWorld2D(/*icao,*/ /*tw,*/ tw.getScene("GroundServices"));
        return loadGroundNetForTesting(bundleGroundnetConfig/*gsw,*/, smoothmode, icao, strict);
    }

    public static GroundNet loadGroundNetForTesting(Bundle bundle , int smoothmode, String icao, boolean strict) throws NoElevationException {
        try {

            AirportConfig airport = new AirportConfig(icao, new ArrayList<LocatedVehicle>());
            //27.12.21 gsw.addAirport(airport.airport);
            //evtl. noch vorhandene Provider loeschen
            SystemManager.putDataProvider(SystemManager.DATAPROVIDERELEVATION, null);
            SystemManager.putDataProvider(SystemManager.DATAPROVIDERELEVATION, new TerrainElevationProvider(airport.getElevation()));

            String gnet = "flight/" + icao + ".groundnet.xml";
            GroundNet.layoutmode = strict;
            //TODO 31.3.20: GroundServicesSystem.loadGroundnet() verwenden
            SimpleMapProjection projection = new SimpleMapProjection(/*TrafficWorld2D.*/SGGeod.fromLatLon(airport.getCenter()));
            //Bundle bundle = BundleRegistry.getBundle("test-resources");
            XmlDocument groundnetxml;
            try {
                groundnetxml = XmlDocument.buildXmlDocument(bundle.getResource(new BundleResource(gnet)).getContentAsString());
            } catch (CharsetException e) {
                // TODO improved eror handling
                throw new RuntimeException(e);
            }
            GroundNet groundnet = new GroundNet(projection, groundnetxml, airport.getHome()/*"A20"* /, airport*/);
            groundnet.groundnetgraph.icao = icao;
            //27.12.21 gsw.addGroundNet(icao, groundnet);
            return groundnet;
        } catch (XmlException e) {
            throw new RuntimeException(e);
        }
        // g = groundnet.groundnetgraph;

    }
}
