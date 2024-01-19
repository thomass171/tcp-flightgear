package de.yard.threed.trafficfg.flight;

import de.yard.threed.core.Degree;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.DataProvider;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.flightgear.TerrainElevationProvider;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.graph.Graph;
import de.yard.threed.graph.GraphNode;
import de.yard.threed.graph.GraphOrientation;
import de.yard.threed.traffic.Destination;
import de.yard.threed.traffic.EllipsoidCalculations;
import de.yard.threed.traffic.GreatCircle;
import de.yard.threed.traffic.RunwayHelper;
import de.yard.threed.traffic.TrafficGraph;
import de.yard.threed.traffic.WorldGlobal;
import de.yard.threed.traffic.flight.FlightRoute;
import de.yard.threed.traffic.geodesy.ElevationProvider;
import de.yard.threed.traffic.geodesy.GeoCoordinate;
import de.yard.threed.traffic.geodesy.MapProjection;
import de.yard.threed.trafficcore.model.Runway;
import de.yard.threed.trafficfg.FgCalculations;


/**
 * 15.2.18
 */
public class RouteBuilder {
    static Log logger = Platform.getInstance().getLog(RouteBuilder.class);
    public static double platzrundealtitude = 300;
    public static double cruisingaltitude = 900;
    EllipsoidCalculations rbcp;

    public RouteBuilder(EllipsoidCalculations rbcp) {
        this.rbcp = rbcp;
    }

    /**
     * Die Projection wird für 2D gebraucht, weil die Route immer in 3D erstellt wird.
     */
    public FlightRoute buildFlightRoute(Runway runway, MapProjection projection, int pattern) {
        logger.debug("buildFlightRoute with projection " + projection);
        FlightRoute graph = buildFlightGraphForAircraftTrafficPattern(runway/*, projection*/, (TerrainElevationProvider) SystemManager.getDataProvider(SystemManager.DATAPROVIDERELEVATION), pattern);
        // GraphPath flightpath = GraphPath.buildFromNode(flightgraph.getNode(0),22);
        //graph ist in 3D und nicht gesmoothed. Wenn die Scene eine Projection verwendet
        //muss der Graph vor dem Smoothen umprojected werden.
        if (projection != null) {
            graph.projectGraph(projection, rbcp);
        }
        graph.smooth();
        return (graph);
    }

    /**
     * Ein "Aircraft Traffic Pattern" (Platzrunde oder per Orbit) von einer Runway.
     * (https://www.ivao.aero/training/documentation/books/PP_ADC_Traffic_Pattern_Description.pdf)
     * <p>
     * Der FlightGraph ist in world 3D Koordinaten.
     * Will be projected accordingly in GroundServicesScsene.
     * 27.2.18: Hier soll es keine Projection geben. Dann kann hier aber auch kein Smoothing gemacht werden, denn dafuer
     * muss erst projected sein. Darum jetzt Smoothing ausgelagert.
     * <p>
     * Skizze 16
     * <p>
     * 18.10.19: Lieber nicht immer schon die komplette Route berechnen (inkl. Landung), denn das ist ja vielleicht noch nicht bekannt.
     * 11.4.20: Es wird ein GraphMovement bis zu einer Stelle erstellt, an der sich das Aircraft aufhalten kann (Landing, Holding, Orbit)
     *
     * @return
     */
    public FlightRoute buildFlightGraphForAircraftTrafficPattern(Runway departing, TerrainElevationProvider elevationprovider, int pattern) {
        Graph graph = buildTakeOffGraph(departing, platzrundealtitude, elevationprovider);
        GraphNode sidnode = graph.getNode(graph.getNodeCount() - 1);
        FlightRoute flightRoute = null;
        Vector3 loc;
        GraphNode n;
        RunwayHelper departingRunwayHelper = new RunwayHelper(departing, new FgCalculations());

        logger.debug("buildFlightGraphForAircraftTrafficPattern");

        switch (pattern) {
            case 0:
                //Platzrunde
                SGGeod turnpoint = GeoUtil.applyCourseDistance(SGGeod.fromCart(sidnode.getLocation()),
                        departingRunwayHelper.getHeading().add(new Degree(90)), 600);
                turnpoint.setElevationM(platzrundealtitude);
                GraphNode turn0 = graph.addNode("turn0", turnpoint.toCart());
                graph.connectNodes(sidnode, turn0, "querabflug");
                turnpoint = GeoUtil.applyCourseDistance(turnpoint,
                        departingRunwayHelper.getHeading().add(new Degree(180)), 6000);
                turnpoint.setElevationM(platzrundealtitude);
                GraphNode turn1 = graph.addNode("turn1", turnpoint.toCart());
                graph.connectNodes(turn0, turn1, "gegenanflug");
                addLandingGraph(departing, graph, turn1, platzrundealtitude, elevationprovider);
                graph.setName("Platzrunde");
                flightRoute = new FlightRoute(graph, graph.getEdge(1), graph.getEdge(graph.getEdgeCount() - 2));
                flightRoute.nextDestination = Destination.buildForLanding(departing);
                break;
            case 1:
                //Local Orbit "Platzrunde" per Grosskreis
                double orbitaltitude = 90000;
                //climb 45 Degree
                GeoCoordinate orbitentry = GeoUtil.applyCourseDistance(SGGeod.fromCart(sidnode.getLocation()),departingRunwayHelper.getHeading(), 90000).toGeoCoordinate();
                orbitentry.setElevationM(orbitaltitude);
                GreatCircle earthOrbit = GreatCircle.fromDG(orbitentry, departingRunwayHelper.getHeading(), rbcp);
                int segments = 256;
                Graph orbit = earthOrbit.getGraph(segments);
                GraphNode lastnode = sidnode;
                //leave orbit early enough
                //TODO extendGraph verwenden
                for (int i = 0; i < segments - 1; i++) {
                    loc = orbit.getNode(i).getLocation();
                    SGGeod g = SGGeod.fromCart(loc);
                    //logger.debug("geod=" + g);
                    n = graph.addNode("", loc);
                    graph.connectNodes(lastnode, n);
                    lastnode = n;
                }
                addLandingGraph(departing, graph, lastnode, platzrundealtitude, elevationprovider);
                for (int i = 0; i < graph.getNodeCount(); i++) {
                    loc = graph.getNode(i).getLocation();
                    SGGeod g = SGGeod.fromCart(loc);
                    //logger.debug("geod=" + g);
                }
                flightRoute = new FlightRoute(graph, graph.getEdge(1), graph.getEdge(graph.getEdgeCount() - 2));
                flightRoute.nextDestination = Destination.buildForLanding(departing);
                break;
            case 2:
                //High Orbit Enter. Von der Sid node noch eine Edge weiter ins high ORbit.
                //29.2.2020: Das koennte deprecated sein. Ersetzt durch (3).
                if (true) throw new RuntimeException("deprecated");
                double highorbitaltitude = 190000;
                //climb 45 Degree
                SGGeod highorbitentry = GeoUtil.applyCourseDistance(SGGeod.fromCart(sidnode.getLocation()),departingRunwayHelper.getHeading(), 90000);
                highorbitentry.setElevationM(highorbitaltitude);
                GraphNode highlastnode = sidnode;
                loc = highorbitentry.toCart();
                n = graph.addNode("", loc);
                graph.connectNodes(highlastnode, n);

                flightRoute = new FlightRoute(graph, graph.getEdge(1), graph.getEdge(graph.getEdgeCount() - 2));
                break;
            case 3:
                //Equator Transit Orbit Enter. Von der Sid node noch im local Orbit Grosskreis weiter und am Equator Richtung India "abbiegen". Dort graph complete.
                //29.2.2020: Ersetzt wohl (2).
                double eorbitaltitude = 90000;
                //climb 45 Degree
                GeoCoordinate eorbitentry = GeoUtil.applyCourseDistance(SGGeod.fromCart(sidnode.getLocation()),departingRunwayHelper.getHeading(), 90000).toGeoCoordinate();
                eorbitentry.setElevationM(eorbitaltitude);
                GreatCircle eearthOrbit = GreatCircle.fromDG(eorbitentry, departingRunwayHelper.getHeading(), rbcp);
                Graph graphToEquator = eearthOrbit.getGraphToEquator(256);
                GraphNode elastnode = extendGraph(graph, graphToEquator.getNodeCount(), graphToEquator);

                //im rechten Winkel bis Equator
                SGGeod equatorentry = SGGeod.fromCart(elastnode.getLocation());
                equatorentry.setLatitudeRad(0);
                // pruefen, ob der Startpoint zum Circle passt? 14.4.20 Nicht mehr relevant. Weil der Path bis zum equatorentry gesmoothed wird, das Orbit aber
                // nicht, ist es doof den Graph dort einfach fortzusetzen. Dann doch lieber den Path dort enden lassen und dann weitermachen. Ist vielleicht auch
                // besser um den passenden Startpoint zu finden? Naja, wer weiß.
                //Vector3 starte1=equatorentry.toCart();
                //SolarSystem.continueLocalOrbitGraph(graph, WorldGlobal.EARTHRADIUS+90000, starte1);
                /*EarthOrbit equatorOrbit = new EarthOrbit(equatorentry, new Degree(90));
                eorbit = equatorOrbit.getGraph(256);
                //nur halb, dann Spherewechsel
                GraphNode trEnterNode = extendGraph(graph,eorbit.getNodeCount()/2,eorbit);*/
                // TODO pruefen, ob das Smoothing des graph richtig endet.
                flightRoute = new FlightRoute(graph, graph.getEdge(1), graph.getEdge(graph.getEdgeCount() - 2));
                flightRoute.nextDestination = new Destination(equatorentry.toGeoCoordinate());
                break;
            case 4:
                // SID depart to a direction. Avoid spitzen Winkel durch additional node TODO
                Degree heading = new Degree(270);
                SGGeod lastpoint = GeoUtil.applyCourseDistance(SGGeod.fromCart(sidnode.getLocation()),heading.add(new Degree(90)), 600);
                lastpoint.setElevationM(2000);
                GraphNode last0 = graph.addNode("turn0", lastpoint.toCart());
                graph.connectNodes(sidnode, last0, "abflug");
                graph.setName("SID Abflug");

                flightRoute = new FlightRoute(graph, graph.getEdge(1), null);
                //TODO bis approachenter. Das geht aber nur, wenn Airportinfos verfuegbar sind.
                flightRoute.nextDestination = Destination.buildForApproachEnter();

                break;
        }
        return flightRoute;
    }

    private static GraphNode extendGraph(Graph graph, int cnt, Graph toAdd) {
        GraphNode lastNode = graph.getNode(graph.getNodeCount() - 1);

        for (int i = 0; i < cnt; i++) {
            Vector3 loc = toAdd.getNode(i).getLocation();
            SGGeod g = SGGeod.fromCart(loc);
            logger.debug("geod=" + g);
            GraphNode n = graph.addNode("", loc);
            graph.connectNodes(lastNode, n);
            lastNode = n;
        }
        return lastNode;
    }

    /**
     * @return
     */
    public FlightRoute buildFlightGraph(Runway departing, TerrainElevationProvider elevationprovider, String flightdestination) {
        Graph graph = buildTakeOffGraph(departing, cruisingaltitude, elevationprovider);
        GraphNode sidnode = graph.getNode(graph.getNodeCount() - 1);
        FlightRoute flightRoute = null;

        //TODO: 2.4.19: Es fehlt noch ein allgemeingültiges Handhaben von Runways. Solange gehts hier nicht weiter.
        return flightRoute;
    }

    /**
     * Orbit über dem Äquator.
     * Das wird ein reiner 3D Graph, ohne SGGeod.
     * Der Graph kommt in eine Äquatorialebene, in der auch zum Mond geflogen wird.
     * 19.10.19: Das duerfte mittlerweile deprecated sein.
     *
     * @return
     */
    @Deprecated
    public static Graph buildEquatorOrbit() {
        Graph graph = new Graph();
        //graph.iszEbene = true;
        //graph.upVector = new Vector3(0, 0, 1);
        double radius = WorldGlobal.EARTHRADIUS + WorldGlobal.km(300);
        GraphNode lastnode = null;
        int segments = 64;
        //wie einen Kreis aufbauen.
        Util.buildRotation(segments, 0, MathUtil2.PI2, (index, x, y) -> {
            if (index == segments) {
                //close with begin.
                graph.connectNodes(graph.getNode(graph.getNodeCount() - 1), graph.getNode(0));
            } else {
                //TODO stimmen x und y?
                GraphNode node = graph.addNode("node" + index, new Vector3(x * radius, y * radius, 0));
                if (graph.getNodeCount() > 1) {
                    graph.connectNodes(graph.getNode(graph.getNodeCount() - 2), node);
                }
            }

        });
        return graph;

    }

    /**
     * Simpler Graph, der in FlightScene sofort von EDDK overview sichtbar ist. (B_8 -> C_4)
     * Eigentlich nur fuer Tests.
     *
     * @return
     */
    public TrafficGraph buildSimpleTestRouteB8toC4(GroundNet groundnet) {
        // Groundnet hat schoene Geods definiert, der Graph ist aber in z0 Ebene. 28.3.18?
        Parking c4 = groundnet.getParkPos("C_4");
        Parking b8 = groundnet.getParkPos("B_8");
        DataProvider dv = SystemManager.getDataProvider("Elevation");
        double elevation = (double) dv.getData(new Object[]{c4.coor});
        c4.coor.setElevationM(elevation);
        b8.coor.setElevationM(elevation);
        // Einen Graph in FG Earth Orientierung
        TrafficGraph graph = new TrafficGraph(GraphOrientation.buildForFG());

        GraphNode nc4 = graph.getBaseGraph().addNode("C4", rbcp.toCart(c4.coor, null));
        GraphNode nb8 = graph.getBaseGraph().addNode("B8", rbcp.toCart(b8.coor, null));
        graph.getBaseGraph().connectNodes(nb8, nc4);
        logger.debug("b8 geod=" + SGGeod.fromCart(nb8.getLocation()) + ",coor=" + nb8.getLocation());
        return graph;
    }

    /**
     * Einen Graph vom Holding bis SID entry erstellen. Braucht kein Groundnet Graph mehr. Die altitude ist nur fuer den sid entry point.
     * <p>
     * Der FlightGraph ist in world 3D Koordinaten.
     * Das Smoothing erfolgt hier noch nicht.
     * Das Einfahren in die erste Edge am "Holding" muss schon passend erfolgen, weil sie schon gesmoothed ist (bzw. dann
     * sein wird?).
     * Eine v1 und entry node brauchts dann auch nicht.
     * 27.2.18:Hier keine Projection.
     * <p>
     * Skizze 16
     *
     * @param altitude
     * @return
     */
    public Graph buildTakeOffGraph(Runway runway, double altitude, ElevationProvider elevationprovider) {
        RunwayHelper runwayHelper = new RunwayHelper(runway, new FgCalculations());
        //20.318: z0 war doch eh falsch.
        Graph graph = new Graph(GraphOrientation.buildForFG());
        //graph.iszEbene = true;
        //graph.upVector = new Vector3(0, 0, 1);
        // entry sollte schon projected sein.
        //GraphNode entry = graph.addNode("entry", positiononrunway.getLocation());

        GraphNode holding = graph.addNode("holding", rbcp.toCart(GeoCoordinate.fromLatLon(runwayHelper.getHoldingPoint(),0), elevationprovider));
        //graph.connectNodes(entry,holding);
        //v1 ist fuer das smooting
        //GraphNode v1 = graph.addNode("v1", projection.projectWithAltitude(v1point,0));
        //graph.connectNodes(holding,v1);
        GraphNode takeoff = graph.addNode("takeoff", rbcp.toCart(GeoCoordinate.fromLatLon(runwayHelper.getTakeoffPoint(),0), elevationprovider));
        graph.connectNodes(holding, takeoff, "starting");
        SGGeod sidpoint = GeoUtil.applyCourseDistance(SGGeod.fromLatLon(runwayHelper.getTakeoffPoint()),runwayHelper.getHeading(), 3000);
        sidpoint.setElevationM(altitude);
        GraphNode sid = graph.addNode("sid", sidpoint.toCart());
        graph.connectNodes(takeoff, sid, "takeoff");
        return graph;
    }

    /**
     * Approach from some node through "star".
     * <p>
     * Der FlightGraph ist in world 3D Koordinaten.
     * * 27.2.18:Hier keine Projection.
     * <p>
     * Skizze 16
     *
     * @return
     */
    public void addLandingGraph(Runway runway, Graph graph, GraphNode lastnode, double altitude, TerrainElevationProvider elevationprovider) {
        RunwayHelper runwayHelper = new RunwayHelper(runway, new FgCalculations());
        SGGeod starpoint = GeoUtil.applyCourseDistance(SGGeod.fromLatLon(runwayHelper.getTakeoffPoint()),runwayHelper.getHeading().reverse(), 3000);
        starpoint.setElevationM(altitude);
        GraphNode star = graph.addNode("star", starpoint.toCart());
        graph.connectNodes(lastnode, star);

        GraphNode touchdown = graph.addNode("touchdown", rbcp.toCart(GeoCoordinate.fromLatLon(runwayHelper.getTouchdownpoint(),0), elevationprovider));
        graph.connectNodes(star, touchdown, "endanflug");
        SGGeod endpoint = GeoUtil.applyCourseDistance(SGGeod.fromLatLon(runwayHelper.getTouchdownpoint()),runwayHelper.getHeading(), 1000);
        GraphNode end = graph.addNode("endpoint", rbcp.toCart(endpoint.toGeoCoordinate(), elevationprovider));
        graph.connectNodes(touchdown, end);


    }
}
