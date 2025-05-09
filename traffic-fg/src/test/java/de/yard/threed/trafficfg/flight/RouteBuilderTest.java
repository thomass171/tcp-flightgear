package de.yard.threed.trafficfg.flight;


import de.yard.threed.core.GeneralParameterHandler;
import de.yard.threed.core.Util;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.graph.Graph;
import de.yard.threed.javacommon.JavaBundleResolverFactory;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import de.yard.threed.traffic.StaticElevationProvider;
import de.yard.threed.traffic.flight.FlightRouteGraph;
import de.yard.threed.core.GeoCoordinate;
import de.yard.threed.trafficcore.geodesy.SimpleMapProjection;
import de.yard.threed.traffic.osm.OsmRunway;
import de.yard.threed.trafficcore.model.Runway;
import de.yard.threed.trafficfg.FgCalculations;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 * <p>
 * Created by thomass on 15.2.18.
 */
public class RouteBuilderTest {

    //static Platform platform = EngineTestFactory.initPlatformForTest( new String[] {"engine","data","data-old","osmscenery"},
      //      new SimpleHeadlessPlatformFactory(JavaBundleResolverFactory.bySimplePath(GranadaPlatform.GRANDA_BUNDLE_PATH)));
    Platform platform = FgTestFactory.initPlatformForTest(true, false, false);

    @Test
    public void testEddkPlatzrunde() throws Exception{
        //TrafficWorldConfig tw =  TrafficWorldConfig.readConfig("data-old", "TrafficWorld.xml"/*"GroundServices"*/);

        Bundle bundle = BundleRegistry.getBundle("traffic-fg");

        GroundNet groundnet = GroundNetTest.loadGroundNetForTesting(bundle,0,"EDDK", false);
        Runway runway14l = OsmRunway.eddk14L();
        //Geht ECS ueberhaupt im Test? Hmmm.Lassen wir mal.
        //EcsEntity aircraft = new EcsEntity(null, new GraphMovingComponent(null, null, null));
        //21.3.24 SimpleMapProjection projection = (SimpleMapProjection) groundnet.projection;
        FlightRouteGraph flightpath = new RouteBuilder(new FgCalculations()).buildFlightGraphForAircraftTrafficPattern(runway14l/*,projection*/, StaticElevationProvider.buildForStaticAltitude(80), 0, new GeneralParameterHandler<GeoCoordinate>() {
            @Override
            public void handle(GeoCoordinate parameter) {
                // ignore for now
            }
        });
        System.out.println("flightpath=" + flightpath);
    }

    @Test
    public void testEquatorOrbit() {
        Graph orbit = RouteBuilder.buildEquatorOrbit();
        Assertions.assertEquals( 64, orbit.getEdgeCount(),"segments");
        Assertions.assertEquals( 64, orbit.getNodeCount(),"nodes");
    }

    @Test
    public void testEDDKOrbit() {
        Runway runway14l = OsmRunway.eddk14L();
        FlightRouteGraph orbit = new RouteBuilder(new FgCalculations()).buildFlightGraphForAircraftTrafficPattern(runway14l, StaticElevationProvider.buildForStaticAltitude(80),1, new GeneralParameterHandler<GeoCoordinate>() {
            @Override
            public void handle(GeoCoordinate parameter) {
                // ignore for now
            }
        });
        //20.3.24 Assertions.assertEquals("takeoff", orbit.takeoffedge.getName(),"takeoff name");
        //23.11.18: ist noch nicht smooth 
        Assertions.assertEquals( 260, orbit.getGraph().getEdgeCount(),"segments");
        Assertions.assertEquals( 261, orbit.getGraph().getNodeCount(),"nodes");
    }

    @Test
    public void testEDDKtoEDDF() {
        Runway runway14l = OsmRunway.eddk14L();
        FlightRouteGraph route = new RouteBuilder(new FgCalculations()).buildFlightGraph(runway14l, StaticElevationProvider.buildForStaticAltitude(80),"EDDF", new GeneralParameterHandler<GeoCoordinate>() {
            @Override
            public void handle(GeoCoordinate parameter) {
                // ignore for now
            }
        });
        //Assertions.assertEquals("takeoff name", "takeoff", route.takeoffedge.getName());
        //ist noch nicht smooth
        //Assertions.assertFalse("smoothed",route.isSmoothed());
        //Assertions.assertEquals("segments", 260, orbit.getGraph().getEdgeCount());
        //Assertions.assertEquals("nodes", 261, orbit.getGraph().getNodeCount());
    }

    /**
     * Von etwa EDDK bis Equator und von da Richtung India rund um den Globus. Und weiter?
     */
    @Test
    public void testFromCologneToEquatorOrbit() {
        Runway runway14l = OsmRunway.eddk14L();
        FlightRouteGraph orbit = new RouteBuilder(new FgCalculations()).buildFlightGraphForAircraftTrafficPattern(runway14l,StaticElevationProvider.buildForStaticAltitude(80),3, new GeneralParameterHandler<GeoCoordinate>() {
            @Override
            public void handle(GeoCoordinate parameter) {
                // ignore for now
            }
        });

        // 300 ist plausibel. 22.3.20 nur noch 172?? TODO
        //TODO TestUtil.assertEquals("segments", 44+256, orbit.getGraph().getEdgeCount());
    }
}
