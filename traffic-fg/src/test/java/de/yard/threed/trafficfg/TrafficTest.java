package de.yard.threed.trafficfg;


import de.yard.threed.core.Degree;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.flightgear.TerrainElevationProvider;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.flightgear.testutil.FgTestUtils;
import de.yard.threed.graph.Graph;
import de.yard.threed.graph.GraphMovingComponent;
import de.yard.threed.graph.GraphMovingSystem;
import de.yard.threed.graph.GraphNode;
import de.yard.threed.graph.GraphPath;
import de.yard.threed.graph.GraphPathSegment;
import de.yard.threed.graph.GraphPosition;
import de.yard.threed.graph.GraphProjection;
import de.yard.threed.graph.GraphTestUtil;
import de.yard.threed.graph.ProjectedGraph;
import de.yard.threed.javacommon.JavaBundleResolverFactory;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import de.yard.threed.traffic.EllipsoidConversionsProvider;
import de.yard.threed.traffic.NodeCoord;
import de.yard.threed.traffic.flight.FlightRouteGraph;
import de.yard.threed.traffic.geodesy.GeoCoordinate;
import de.yard.threed.traffic.geodesy.MapProjection;
import de.yard.threed.traffic.geodesy.SimpleMapProjection;
import de.yard.threed.traffic.osm.OsmRunway;
import de.yard.threed.traffic.testutils.TrafficTestUtils;
import de.yard.threed.trafficcore.model.Runway;

import de.yard.threed.trafficfg.flight.GroundNet;
import de.yard.threed.trafficfg.flight.GroundNetTest;
import de.yard.threed.trafficfg.flight.RouteBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * Was TrafficSystemTest once.
 * Not really testing TrafficSystem, is it? (13.5.20: thats in FlightSystemTest)
 * Renamed to TrafficTest
 *
 * <p>
 * Created by thomass on 20.3.18.
 */
public class TrafficTest {
    Platform platform = FgTestFactory.initPlatformForTest(true, false, false);

    @Test
    public void testEddkPlatzrunde() throws Exception {
        //TrafficWorldConfig tw =  TrafficWorldConfig.readConfig("data-old", "TrafficWorld.xml"/*"GroundServices"*/);

        EngineTestFactory.loadBundleSync("traffic-fg");

        Bundle bundle = BundleRegistry.getBundle("traffic-fg");

        GroundNet groundnet = GroundNetTest.loadGroundNetForTesting(bundle, 0,"EDDK", false);

        // 23.11.23: wasn't needed before(??). Maybe a side effect.
        SystemManager.putDataProvider("ellipsoidconversionprovider", new EllipsoidConversionsProvider(new FgCalculations()));

        //erst danach Provider einrichten. Weil GroundnetTest den 87er verwendet?
        SystemManager.putDataProvider(SystemManager.DATAPROVIDERELEVATION, null);
        SystemManager.putDataProvider(SystemManager.DATAPROVIDERELEVATION, TerrainElevationProvider.buildForStaticAltitude(27));

        Runway runway14l = OsmRunway.eddk14L();
        groundnet.createRunwayEntry(runway14l);
        SimpleMapProjection projection = (SimpleMapProjection) groundnet.projection;
        // Erstmal testen mit 2D Projection. Make sure the correct elevation provider is used.
        TerrainElevationProvider tep = (TerrainElevationProvider) SystemManager.getDataProvider(SystemManager.DATAPROVIDERELEVATION);
        assertNotNull( tep,"elevation provider");
        Assertions.assertEquals( 27, tep.getAltitude(), 0.5,"altitude from elevation provider");
        FlightRouteGraph route = new RouteBuilder(new FgCalculations()).buildFlightRouteGraph(runway14l, projection, 0);
        GraphPath smoothedflightpath = route.getPath();
        Graph graph = route.getGraph();
        //sind durch smoothing viel mehr als 7
        //TestUtil.assertEquals("edges", 7, graph.getEdgeCount());
        //2D Projection legt elevation in Z ab. Toleranz muss relativ hoch sein.
        Assertions.assertEquals(27, graph.findNodeByName("holding").getLocation().getZ(), 0.5f,"holding.altitude");
        Assertions.assertEquals(RouteBuilder.platzrundealtitude, graph.findNodeByName("sid").getLocation().getZ(), 0.5f,"sid.altitude");

        System.out.println("platzrunde=" + smoothedflightpath);
        GraphNode holding = groundnet.getHolding(runway14l.getFromNumber()/*getName()*/);
        // Das erste Segment muss ab Holding ein smoothbegin sein.
        GraphPathSegment segment0 = smoothedflightpath.getSegment(0);
        //die erste Node hat nicht die runway im Namen
        Assertions.assertEquals("holding", segment0.getEnterNode().getName(),"enternode");
        Assertions.assertEquals( "smoothbegin.takeoff", segment0.edge.getName(),"enternode");
        GraphNode firstnodeonpath = segment0.edge.getOppositeNode(segment0.getLeaveNode());
        //TODO 21.3.24 TrafficTestUtils.assertGeoCoordinate( ((NodeCoord) holding.customdata).coor, GeoCoordinate.fromLatLon(projection.unproject(Vector2.buildFromVector3(firstnodeonpath.getLocation())),0),"platzrunde holding geod");

        // Und jetzt in 3D ohne Projection. Make sure the correct elevation provider is used.
        tep = (TerrainElevationProvider) SystemManager.getDataProvider(SystemManager.DATAPROVIDERELEVATION);
        assertNotNull( tep,"elevation provider");
        Assertions.assertEquals( 27, tep.getAltitude(), 0.5,"altitude from elevation provider");
        route = new RouteBuilder(new FgCalculations()).buildFlightRouteGraph(runway14l, null, 0);
        smoothedflightpath = route.getPath();
        graph = route.getGraph();
        System.out.println("platzrunde=" + smoothedflightpath);
        Assertions.assertEquals(27, (float) SGGeod.fromCart(graph.findNodeByName("holding").getLocation()).getElevationM(), 0.5f,"holding.altitude");

        // mal sehen, ob die Position auf dem Graph auch richtig ermittelt wird. 
        // "start" auf "holding" (erste Node im Graph) wird ueber path.startposition gesetzt. Die muss aber auf smoothed edge sein.
        GraphMovingComponent gmc = new GraphMovingComponent();
        gmc.setGraph(graph, null, null);
        gmc.setPath(smoothedflightpath, false);
        //Stimmt die Position im Graph?
        GraphPathSegment firstseg = smoothedflightpath.getSegment(0);
        GraphTestUtil.assertGraphPosition("am holding", new GraphPosition(firstseg.edge), smoothedflightpath.startposition);
        //grob geschaetzt
        float distancetosid = 3500;
        gmc.moveForward(distancetosid);
        LocalTransform posrot = GraphMovingSystem.getPosRot(gmc/*, null*/);
        SGGeod coor = SGGeod.fromCart(posrot.position);
        //die konkreten Werte uebernommen, aber plausibel sind sie
        FgTestUtils.assertSGGeod("sid.coor", new SGGeod(new Degree(7.162683f), new Degree(50.857273f), 100), coor);
    }


}
