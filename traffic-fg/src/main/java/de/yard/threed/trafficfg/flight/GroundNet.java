package de.yard.threed.trafficfg.flight;


import de.yard.threed.core.Degree;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeAttributeList;
import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.core.platform.NativeNodeList;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.XmlDocument;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.util.XmlHelper;
import de.yard.threed.graph.DefaultGraphWeightProvider;
import de.yard.threed.graph.GraphEdge;
import de.yard.threed.graph.GraphNode;
import de.yard.threed.graph.GraphNodeFilter;
import de.yard.threed.graph.GraphOrientation;
import de.yard.threed.graph.GraphPath;
import de.yard.threed.graph.GraphPosition;
import de.yard.threed.graph.GraphProjection;
import de.yard.threed.graph.GraphUtils;
import de.yard.threed.graph.GraphValidator;
import de.yard.threed.graph.TurnExtension;
import de.yard.threed.traffic.EllipsoidCalculations;
import de.yard.threed.traffic.NoElevationException;
import de.yard.threed.traffic.NodeCoord;
import de.yard.threed.traffic.PositionHeading;
import de.yard.threed.traffic.RunwayHelper;
import de.yard.threed.traffic.SphereProjections;
import de.yard.threed.traffic.TrafficGraph;
import de.yard.threed.traffic.TrafficHelper;
import de.yard.threed.traffic.geodesy.ElevationProvider;
import de.yard.threed.traffic.geodesy.GeoCoordinate;
import de.yard.threed.traffic.geodesy.MapProjection;
import de.yard.threed.trafficcore.model.Runway;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gedanklich in x(Ost/Westrichtung),y(NordSuedRichtung) in z=0 Ebene.?
 * Groessenangaben alle in Meter. Das macht die Intuition und Testdarstellung leichter. Erfordert Airports ohne Höhnunterschiede. Aber naja, bis es soweit ist.
 * Evtl. spaeter mal umstellen, dann evtl mit double wegen Rundungsfehler.
 * <p>
 * Als Name in Node/Edge wird "index" verwendet. Der duerfte immer gefüllt und eindeutig sein. Der Name von z.B. Parkpositionen liegt nur ausserhalb vor.
 * <p>
 * Layer: 0,1,2 smoothings.
 * 15.2.18: Optional nicht mehr in 2D projected.
 * <p>
 * Created by thomass on 27.03.17.
 */
public class GroundNet {
    Log logger = Platform.getInstance().getLog(GroundNet.class);
    private Map<String, GraphNode> parkposname2node = new HashMap<String, GraphNode>();
    public TrafficGraph groundnetgraph;
    // Ein Home muss es immer geben, damit Vehicle definiert von irgendwoher ins Groundnet fahren.
    private Parking home;
    // 0=no, 1=per node (deprecated), 2=per path
    //int smoothmode = 0;
    public static boolean layoutmode = false;
    // gehoert zwar nicht hier hin, ist aber gut wenns verfuaegbar ist (z.B. Tests?)
    // 15.2.18: Ist jetzt optional. Wenn null, wird nicht projected, zumindest nicht hier. Das ist allerdings
    // ein Spezialfall. Auch in 3D Darstellung sollte Groundnet zumindest vorerst analog zu FG eine Projection verwenden.
    // 9.1.19: Das macht aber vieles (z.B. die Ermittlung der effektiven Rotation) ziemlich undurchsichtig. Vielleicht doch nur bei 2D projecten?
    // 29.5.19: Auch wenn auch in 3D eine 2D Projection verwendet wird, darf projection null, sein. Z.B. fuer Tests oder Nutzung bei Scenery.
    // 21.3.24: IIRC projection is specific for this special groundnet, based on airport center.
    //21.3.24 Could be moved to ProjectedGraph
    public MapProjection projection;
    //damit es wie in FG eine altitude gibt. Erstmal nicht, weils beim rendern der edges bloed ist. Fuer Tests aber schon
    //5.4.18: Umbenennen von virtualaltitude nach airportelevation?
    //4.1.19: Ist das wirklich noch notwendig, wo es doch den ElevationProvider gibt? Eher nicht. Es gibt ja auch noch die AirportConfig.
    //public float airportelevation = 0;

    //4.11.19 AirportConfig airport;
    //24.5.20 public boolean needsUpdate = false;

    /**
     * Die XML Angaben in einen Graph abbilden. Das center ist fuer die Abblidung in xy Koordinatensystem
     * Auch in 3D ist das groundnet projected.
     *
     * @param groundnetxml
     */
    /*public GroundNet(MapProjection projection, XmlDocument groundnetxml,/* int smoothmode,* / String homename) {
        this(projection, groundnetxml, homename, null);
    }*/

    /**
     * 28.9.18: TODO Statt airport nur ein Elevationprobider reeingeben (decoupling)
     * 29.5.19: projection, homename,airport duefren null sein, zB. zur Nutzung bei Scenery.
     * 24.5.2020: No with exception when elevation not available.
     *
     * @param groundnetxml
     * @param homename
     */
    public GroundNet(MapProjection projection, GraphProjection backProjection, XmlDocument groundnetxml,/* int smoothmode,*/ String homename/*4.11.19, AirportConfig airport*/) throws NoElevationException {
        //4.11.19 this.airport = airport;
        //this.airportelevation = (airport == null) ? 0 : airport.getElevation();
        // Vector3 centerc = center.toCart();
        // 26.7.17: Validator ist ganz praktisch in der Testphase un Anomalien leichter zu finden.
        this.projection = projection;
        GraphValidator validator = null;
        if (layoutmode) {
            validator = new GroundNetGraphValidator();
        }
        GraphUtils.strict = layoutmode;
        //if (projection != null) {
            groundnetgraph = new TrafficGraph(validator, GraphOrientation.buildForZ0(), backProjection);
        /*} else {
            groundnetgraph = new TrafficGraph(validator, GraphOrientation.buildForZ0());
        }*/
        TrafficGraph.MINIMUMPATHSEGMENTLEN = 2 * TrafficGraph.SMOOTHINGRADIUS;
        if (layoutmode) {
            TrafficGraph.MINIMUMPATHSEGMENTLEN = 8;
        }

        //groundnetgraph.iszEbene = true;
        //groundnetgraph.upVector = new Vector3(0, 0, 1);
        // this.smoothmode = smoothmode;
        // "node" und "Parking" sind beides Graphnodes
        NativeNodeList nodelist = groundnetxml.nativedocument.getElementsByTagName("node");
        for (int i = 0; i < nodelist.getLength(); i++) {
            NativeNode node = nodelist.getItem(i);
            GraphNode n = addNode(projection, node);
            n.customdata = new TaxiwayNode(n, XmlHelper.getStringAttribute(node, "holdPointType"), XmlHelper.getBooleanAttribute(node, "isOnRunway", false));
        }
        nodelist = groundnetxml.nativedocument.getElementsByTagName("Parking");
        for (int i = 0; i < nodelist.getLength(); i++) {
            NativeNode node = nodelist.getItem(i);
            GraphNode n = addNode(projection, node);
            String parkingname = getAttrName(node);
            n.customdata = new Parking(n, parkingname, new Degree(XmlHelper.getFloatAttribute(node, "heading", 0)),
                    XmlHelper.getIntAttribute(node, "pushBackRoute", -1), XmlHelper.getFloatAttribute(node, "radius", 0));
            if (homename != null && homename.equals(parkingname)) {
                logger.debug("setting home " + homename);
                home = (Parking) n.customdata;
            }
        }

        // Die Groundnets haben wohl alle Wege doppelt (in beide Richtungen) eingetragen. Darauf verzichten.
        nodelist = groundnetxml.nativedocument.getElementsByTagName("arc");
        for (int i = 0; i < nodelist.getLength(); i++) {
            NativeNode node = nodelist.getItem(i);
            NativeAttributeList attrs = node.getAttributes();
            String begin = XmlHelper.getStringAttribute(node, "begin");
            String end = XmlHelper.getStringAttribute(node, "end");
            String name = getAttrName(node);
            GraphNode bn = groundnetgraph.getBaseGraph().findNodeByName(begin);
            GraphNode en = groundnetgraph.getBaseGraph().findNodeByName(end);
            if (bn == null) {
                logger.warn("begin node not found: " + begin);
            } else {
                if (en == null) {
                    logger.warn("end node not found: " + end);
                } else {
                    GraphEdge c = groundnetgraph.getBaseGraph().findConnection(bn, en);
                    if (c == null) {
                        // Connection gibt es noch nicht. Als name etwas leicht parseable nehmen.
                        String longname = "" + begin + "->" + end + "(" + name + ")";
                        String shortname = "" + begin + "-" + end;
                        c = groundnetgraph.getBaseGraph().connectNodes(bn, en, shortname);
                        //8.5.18: wird nicht verwendet c.customdata = new TaxiwaySegment(c, name, XmlHelper.getBooleanAttribute(node, "isPushBackRoute", false));
                    }
                }
            }
        }

        if (home == null) {
            for (int i = 0; i < groundnetgraph.getBaseGraph().getNodeCount(); i++) {
                GraphNode n = groundnetgraph.getBaseGraph().getNode(i);
                if (isParking(n)) {
                    if (((Parking) n.customdata).getApproach() != null) {
                        home = (Parking) n.customdata;
                        break;
                    }
                }
            }
        }
        if (home == null) {
            // dann lege ich einfach ein neues Parking an. An einer Node, die kein Parking ist. TODO
            throw new RuntimeException("no home");
        }
        groundnetgraph.setHome(home.node);
           
        /*29.5.17: das lass ich mal if (smoothmode == 1) {
            // and smooth all edges.

            for (int i = 0; i < groundnetgraph.getNodeCount(); i++) {
                GraphNode n = groundnetgraph.getNode(i);
                GraphUtils.smoothNode(groundnetgraph, n, SMOOTHINGRADIUS, 2);
            }

            //smooth per edge
                /*for (int i = 0; i < groundnetgraph.getEdgeCount(); i++) {
                    GraphEdge e = groundnetgraph.getEdge(i);
                    List<GraphEdge> nearby = groundnetgraph.findNearbyEdges(e.getFrom());
                    for (GraphEdge ne : nearby) {
                        smoothEdges(e, ne);
                    }
                    for (int j = 0; j < e.getTo().getEdgeCount(); j++) {
                        smoothEdges(e, e.getTo().getEdge(j));
                    }
                }* /


        }*/
        logger.info("groundnet loaded");
    }

    /**
     * Both edges might be directly connected, but this isn't required.
     *
     * @param e
     * @param destinationedge
     */

    private void smoothEdges(GraphEdge e, GraphEdge destinationedge) {
        // what isType best decision length?
        if (e.getLength() < TrafficGraph.SMOOTHINGRADIUS || destinationedge.getLength() < TrafficGraph.SMOOTHINGRADIUS) {
            return;
        }
    }

    private GraphNode addNode(MapProjection projection, NativeNode node) throws NoElevationException {
        NativeAttributeList attrs = node.getAttributes();
        // name muss der index sein, denn darueber wird gesucht.
        String name = attrs.getNamedItem("index").getValue();
        GeoCoordinate coor = new GeoCoordinate(Degree.parseDegree(attrs.getNamedItem("lat").getValue()), Degree.parseDegree(attrs.getNamedItem("lon").getValue()), 0);
        return addNodeByProjecting(name, coor);
    }

    private GraphNode addNodeByProjecting(String name, GeoCoordinate coor) throws NoElevationException {
        Vector3 point;
        double/*Elevation*/ elevation = 0;
        if (projection == null) {
            EllipsoidCalculations rbcp = TrafficHelper.getEllipsoidConversionsProviderByDataprovider();
            point = rbcp.toCart(coor, null, null);
        } else {
            Vector2 projected = projection.project(coor);
            //elevation = TrafficWorld2D.getElevationForLocation(airport, coor);
            //point = new Vector3(projected.getX(), projected.getY(), elevation.elevation);
            point = buildVector3FromVector2(projected, coor);
        }
        GraphNode n = groundnetgraph.getBaseGraph().addNode(name, point);
        n.customdata = new NodeCoord(coor, elevation);
        return n;
    }

    /**
     * 4.1.19: Warum hier fix airportelevation drin ist statt wie oben? Mach ich jetzt einheitlich.
     *
     * @param xy
     * @return
     */
    private Vector3 buildVector3FromVector2(Vector2 xy, LatLon coor) throws NoElevationException {
        if (coor == null) {
            //an Servicepoints fehlen coors
            coor = projection.unproject(xy);
        }
        double/*Elevation*/ elevation = getElevationForLocation(/*airport,*/ coor);
        /*if (elevation.needsupdate) {
            needsUpdate = true;
        }*/
        return new Vector3(xy.getX(), xy.getY(), /*airportelevation*/elevation);
    }

    /**
     * z will be overwritten with corresponding altitude. Ist hier aber immer gleich.
     * 4.1.19: Warum hier fix airportelevation drin ist statt wie oben? Mach ich jetzt einheitlich.
     *
     * @param name
     * @param loc
     * @return
     */
    private GraphNode addNodeToGraph(String name, Vector3 loc) throws NoElevationException {
        //loc = new Vector3(loc.getX(), loc.getY(), airportelevation);
        loc = buildVector3FromVector2(new Vector2(loc.getX(), loc.getY()), null);
        GraphNode node = groundnetgraph.getBaseGraph().addNode(name, loc);
        return node;
    }


    private String getAttrName(NativeNode node) {
        String s = XmlHelper.getStringAttribute(node, "name");
        return (s != null) ? s : "";
    }

    /**
     * get park pos by logical groundnet name.
     *
     * @param name
     * @return
     */
    public Parking getParkPos(String name) {
        GraphNode n = parkposname2node.get(name);
        if (n != null) {
            return (Parking) n.customdata;
        }
        for (int i = 0; i < groundnetgraph.getBaseGraph().getNodeCount(); i++) {
            n = groundnetgraph.getBaseGraph().getNode(i);
            if (n.customdata instanceof Parking) {
                if (((Parking) n.customdata).name.equals(name)) {
                    parkposname2node.put(name, n);
                    return (Parking) n.customdata;
                }
            }
        }
        //not found
        return null;
    }

    /**
     * get park pos closest to coordinates. Returns Parking customdata.
     * 25.5.20: isType position projected? Und warum geht das ueberhaupt über VectorCoordinates und nicht LatLon?
     */
    public Parking getParkPosNearCoordinates(Vector3 position) {
        double shortestdistance = Double.MAX_VALUE;
        GraphNode best = null;
        for (int i = 0; i < groundnetgraph.getBaseGraph().getNodeCount(); i++) {
            GraphNode n = groundnetgraph.getBaseGraph().getNode(i);
            if (isParking(n)) {
                //float distance = ((Parking)n.customdata).coor.distanceTo(coord);
                double distance = Vector3.getDistance(n.getLocation(), position);
                if (distance < shortestdistance) {
                    shortestdistance = distance;
                    best = n;
                }
            }
        }
        if (best == null) {
            logger.warn("no closest parking found");
        }
        return (Parking) best.customdata;
    }

    boolean isParking(GraphNode n) {
        return n.customdata != null && n.customdata instanceof Parking;
    }

    /**
     * 15.5.18: Deprecated weil das schlicht falsch ist. Zumindest ist es nicht immer richtig. Siehe SimpleMapProjection.
     *
     * @param direction
     * @return
     */
    @Deprecated
    public static Degree getHeadingFromDirection(Vector3 direction) {
        return MathUtil2.getHeadingFromDirection(new Vector2(direction.getX(), direction.getY()));
    }


    public TurnExtension addTearDropTurnAtParking(Parking parking, boolean left) {
        GraphEdge approach = parking.getApproach();
        if (approach == null) {
            // not possible
            return null;
        }
        return groundnetgraph.addTearDropTurn(parking.node, approach, left);
    }

    /**
     * name isType "create" instead of "find" because a teardrop isType created.
     * layerid isType created internally.
     *
     * @return
     */
    public GraphPath createPathForPushbackVehicle(Parking parkpos, GraphNode currentlocation) {
        //TODO avoid duplicat teardrop creating
        TurnExtension teardrop = addTearDropTurnAtParking(parkpos, true);
        // mark other edges as teardrop as void.
        List<GraphEdge> voidedges = new ArrayList<GraphEdge>();
        for (int i = 0; i < parkpos.node.getEdgeCount(); i++) {
            GraphEdge e = parkpos.node.getEdge(i);
            if (e != teardrop.edge) {
                voidedges.add(e);
            }
        }
        GraphPath path = groundnetgraph.getBaseGraph().findPath(currentlocation, parkpos.node, new DefaultGraphWeightProvider(groundnetgraph.getBaseGraph(), voidedges));
        return path;
    }

    /**
     * Von einer Stelle im "nirgendwo" einen "guten" Pfad in das Net finden bzw. den Graph um diesen Pfad ergänzen.
     * Das kann durchaus tricky sein, denn es kann vielfältige "gute" Pfade geben.
     * <p>
     * Skizze 32
     * <p>
     * Bleibt erstmal eingeschraenkt auf die z=0 Ebene.
     * <p>
     * Aspekte sind:
     * - welcher ist der nearest?
     * - wohin will ich? Da kann ein weniger nearer besser sein.
     * - wie verlaufen die nahen? Unnötige starke Bögen sollen vermieden werden. Einer quer hinter
     * mir scheint zunächst mal ungeeignet. Wenn er allerdings direkt zu meinem Ziel führt, auch wieder
     * nicht.
     * - wie kann sich ein FolloMe vor das aircraft setzen. Evtl. muss Followme hinter den ersten Bogen.
     * <p>
     * Das ist ein richtiges Optimierungs-, wenn nicht sogar ein KI Problem.
     * <p>
     * Es gibt zunächst mal zwei Hauptansätze:
     * a) ich verlängere mein Heading bis ich auf einen Taxiway stosse. Da smoothe ich dann.
     * b) ich verlängere mein Heading nur ca 10 Meter und zweige dann senkrecht auf einen Taxiway ab. Da
     * wird dann zweimal gesmoothed.
     * <p>
     * Erstmal nur a)
     * <p>
     */
    public GraphEdge createPathIntoGroundNet(PositionHeading poshead) throws NoElevationException {
        /*Vector2 headinglinestart = new Vector2(poshead.position.getX(), poshead.position.getY());
        Vector2 headinglineend = headinglinestart.add(MathUtil2.getDirectionFromHeading(poshead.heading).multiply(8000000));
        List<GraphEdge> edgelist = new ArrayList<GraphEdge>();
        for (int i = 0; i < groundnetgraph.getEdgeCount(); i++) {
            edgelist.add(groundnetgraph.getEdge(i));
        }*/
        // "best" isType the nearest
        Object[] o = findHitEdge(poshead);

        GraphEdge best = null;
        Vector2 bestintersection = null;
        if (o != null) {
            best = (GraphEdge) o[0];
            bestintersection = (Vector2) o[1];
        }
        /*Vector2 bestintersection = null;
        float bestdistance = Double.MAX_VALUE;
        for (GraphEdge e : edgelist) {
            Vector2 linestart = new Vector2(e.getFrom().getLocation().getX(), e.getFrom().getLocation().getY());
            Vector2 lineend = new Vector2(e.getTo().getLocation().getX(), e.getTo().getLocation().getY());
            Vector2 intersection = MathUtil2.getLineIntersection(headinglinestart, headinglineend, linestart, lineend);
            if (intersection != null && MathUtil2.isPointOnLine(linestart, lineend, intersection)) {
                float distance = Vector3.getDistance(poshead.position, new Vector3(intersection.getX(), intersection.getY(), 0));
                logger.debug("intersection=" + intersection + " with " + e.getName() + ", distance=" + distance);
                // generierte vermeiden
                if (!StringUtils.empty(e.getName()) && distance < bestdistance) {
                    best = e;
                    bestintersection = intersection;
                    bestdistance = distance;
                }
            }
        }*/
        GraphEdge result = null;
        if (best != null) {
            // path found. create it.
            int layer = groundnetgraph.newLayer();
            GraphNode origin = groundnetgraph.getBaseGraph().addNode("origin", poshead.position);
            GraphNode intersectionnode = groundnetgraph.getBaseGraph().addNode("destination", buildVector3FromVector2(bestintersection, null));
            result = groundnetgraph.getBaseGraph().connectNodes(origin, intersectionnode, "pathtointersection", layer);
            groundnetgraph.getBaseGraph().connectNodes(intersectionnode, best.from, "", layer);
            groundnetgraph.getBaseGraph().connectNodes(intersectionnode, best.to, "", layer);
            // smoothing only required for intersection node.
            GraphUtils.smoothNode(groundnetgraph.getBaseGraph(), intersectionnode, TrafficGraph.SMOOTHINGRADIUS, layer);

        }
        return result;
    }

    /**
     * Liefert die naheliegenste Edge (index0) und die intersection(index1), die mit Verlängerung von PosHeading getroffen wird.
     * Ansonsten null.
     * Nur für z=0, bzw identische z.
     * 3.8.17: Nur in LAyer 0, sonst kann und wird das durcheinander geben.
     *
     * @return
     */
    public Object[] findHitEdge(PositionHeading poshead) throws NoElevationException {
        Vector2 headinglinestart = new Vector2(poshead.position.getX(), poshead.position.getY());
        Vector2 headinglineend = headinglinestart.add(MathUtil2.getDirectionFromHeading(poshead.heading).multiply(8000000));
        List<GraphEdge> edgelist = new ArrayList<GraphEdge>();
        for (int i = 0; i < groundnetgraph.getBaseGraph().getEdgeCount(); i++) {
            edgelist.add(groundnetgraph.getBaseGraph().getEdge(i));
        }
        // "best" isType the nearest
        GraphEdge best = null;
        Vector2 bestintersection = null;
        double bestdistance = Double.MAX_VALUE;
        for (GraphEdge e : edgelist) {
            if (e.getLayer() == 0) {
                Vector2 linestart = new Vector2(e.getFrom().getLocation().getX(), e.getFrom().getLocation().getY());
                Vector2 lineend = new Vector2(e.getTo().getLocation().getX(), e.getTo().getLocation().getY());
                Vector2 intersection = MathUtil2.getLineIntersection(headinglinestart, headinglineend, linestart, lineend);
                if (intersection != null && MathUtil2.isPointOnLine(linestart, lineend, intersection)) {
                    double distance = Vector3.getDistance(poshead.position, buildVector3FromVector2(intersection, null));
                    //logger.debug("intersection=" + intersection + " with " + e.getName() + ", distance=" + distance);
                    //Pruefen, dass die intersection auch in Richtung heading ist, und nicht dahinter.
                    double angle = Vector3.getAngleBetween(buildVector3FromVector2(intersection, null).subtract(poshead.position),
                            Vector3.buildFromVector2(MathUtil2.getDirectionFromHeading(poshead.heading)));
                    if (angle < MathUtil2.PI_2) {
                        // generierte vermeiden. 1.8.17: Das geht so aber nicht!
                        if (!StringUtils.empty(e.getName()) && distance < bestdistance) {
                            best = e;
                            bestintersection = intersection;
                            bestdistance = distance;
                        }
                    }
                }
            }
        }
        if (best == null) {
            return null;
        }
        return new Object[]{best, bestintersection};
    }

    /**
     * only aircraft path, no teardrop return for car yet.
     *
     * @return
     */
    public GraphPath findFollowMePath(GraphNode from, Parking destination) {
        GraphPath path = groundnetgraph.getBaseGraph().findPath(from, destination.node, null);
        return path;
    }

    /**
     * Create Followme approach path for car to Aircraft (to last line edge near aircraft) if there isType enough space. Otherwise on "some" line edge before.
     * This depends on the aircrafts path to the desired destination.
     * Skizze 32.
     * <p>
     * layerid isType created internally and the same for all created segements.
     *
     * @return
     */
    public TurnExtension createFollowMeVehicleApproach(GraphPath aircraftpath) {
        int layer = groundnetgraph.newLayer();
        GraphEdge nearestlineedge = aircraftpath.getNearestLineEdge(null);
        GraphEdge teardropedge = null;
        GraphNode teardrophook = null;
        if (nearestlineedge.getLength() < 20) {
            // not enough space in front of aircraft. Try next nearest.
            nearestlineedge = aircraftpath.getNearestLineEdge(nearestlineedge);
        } else {
            // edge isType at aircraft. Additional node isType required as teardrop hook.
            teardrophook = groundnetgraph.getBaseGraph().addNode("teardrophook", nearestlineedge.to.getLocation().add(
                    nearestlineedge.getEffectiveOutboundDirection(nearestlineedge.to).multiply(nearestlineedge.getLength() / 2)));
            teardropedge = groundnetgraph.getBaseGraph().connectNodes(nearestlineedge.to, teardrophook, "", layer);
        }
        // 3. Add teardrop for car turning. 
        TurnExtension turn = GraphUtils.addTearDropTurn(groundnetgraph.getBaseGraph(), teardrophook, teardropedge, true, TrafficGraph.SMOOTHINGRADIUS, layer, false);

        return turn;

    }


    public GraphNode getFollowmeHome() {
        //TODO
        return groundnetgraph.getBaseGraph().findNodeByName("3");
    }

    public Parking getVehicleHome() {
        return home;
    }

    /**
     * Das Heading der parkpos kann entgegengesetzt zur direction des approach sein.
     * <p>
     * Might return null in inconsistent groundnets;
     *
     * @param parking
     * @return
     */
    public GraphPosition getParkingPosition(Parking parking) {
        GraphEdge approach = parking.getApproach();
        Vector2 dir;
        if (approach == null) {
            if (parking.node.getEdgeCount() == 0) {
                return null;
            }
            approach = parking.node.getEdge(0);
            dir = Vector2.buildFromVector3(approach.getEffectiveInboundDirection(parking.node));
        } else {
            dir = MathUtil2.getDirectionFromHeading(parking.heading);
        }
        GraphPosition position;
        if (approach.from.equals(parking.node)) {
            if (Vector3.getAngleBetween(new Vector3(dir.getX(), dir.getY(), 0), approach.getEffectiveOutboundDirection(parking.node)) < MathUtil2.PI_2) {
                position = new GraphPosition(approach);
            } else {
                //same position but reverse
                position = new GraphPosition(approach, /*24.5.17 0*/approach.getLength(), true);
            }
        } else {
            if (Vector3.getAngleBetween(new Vector3(dir.getX(), dir.getY(), 0), approach.getEffectiveInboundDirection(parking.node)) < MathUtil2.PI_2) {
                position = new GraphPosition(approach, approach.getLength());
            } else {
                //same position but reverse
                position = new GraphPosition(approach, approach.getLength(), true);
            }
        }
        //position.setUpVector(graphupvector);
        return position;
    }


    /**
     * Create approach to aircraft door.
     * <p>
     * layerid isType created internally and the same for all created segements.
     * <p>
     * Benutzt bewusst keine Graphposition fürs Aircraft, weil das Aircraft vielleicht gar nicht exakt steht.
     * Die Laenge der dooredge muss von der Spannweite (Viertel) abhängen, fixe 20m sind bei kleineren (z.B. 737) zu lang.
     * Returns [edge at door,returnsuccessor?,branchedge].
     * Skizze 32
     *
     * @return
     */
    public GraphEdge[] createDoorApproach(Vector3 prjdoorpos, Vector2 aircraftdirection, Vector3 worldwingpassingpoint, Vector3 worldrearpoint, double wingspread, double approachoffset) throws NoElevationException {
        int layer = groundnetgraph.newLayer();
        //heading away from aircraft
        Vector2 approachdirection = aircraftdirection.rotate(new Degree(-90));
        double approachlen = TrafficGraph.MINIMUMPATHSEGMENTLEN + 0.001;
        approachlen = wingspread / 4;
        // Avoid vehicle to move into aircraft by moving destination node more outside.
        prjdoorpos = prjdoorpos.add(Vector3.buildFromVector2(approachdirection.normalize().multiply(approachoffset)));
        GraphNode door0 = addNodeToGraph("door0", prjdoorpos);
        GraphNode door1 = addNodeToGraph("door1", prjdoorpos.add(Vector3.buildFromVector2(approachdirection.normalize().multiply(approachlen))));

        GraphEdge dooredge = groundnetgraph.getBaseGraph().connectNodes(door0, door1, "dooredge", layer);

        GraphNode wing0 = addNodeToGraph("wing0", worldwingpassingpoint);
        GraphEdge door2wing = groundnetgraph.getBaseGraph().connectNodes(door1, wing0, "door2wing", layer);

        // direction at wing edge
        Vector2 wingdirection = approachdirection.rotate(new Degree(-90));
        GraphNode wing1 = addNodeToGraph("wing1", wing0.getLocation().add(Vector3.buildFromVector2(wingdirection.normalize().multiply(approachlen))));
        GraphEdge c1 = groundnetgraph.getBaseGraph().connectNodes(wing0, wing1, "wingedge", layer);

        //default branch/merge node isType getFirst node if no specific node can be found.
        GraphNode branchnode = groundnetgraph.getBaseGraph().getNode(0);
        Object[] o = findHitEdge(new PositionHeading(wing1.getLocation(), MathUtil2.getHeadingFromDirection(wingdirection)));
        GraphEdge best = null;
        if (o != null) {
            best = (GraphEdge) o[0];
            //den Punkt am nächsten zum rearpoint nehmen.
            double distanceoffrom = Vector3.getDistance(best.from.getLocation(), (worldrearpoint));
            double distanceofto = Vector3.getDistance(best.to.getLocation(), (worldrearpoint));
            if (distanceoffrom < distanceofto) {
                branchnode = best.from;
            } else {
                branchnode = best.to;
            }
        }
        GraphEdge branchedge = groundnetgraph.getBaseGraph().connectNodes(wing1, branchnode, "branchedge", layer);
        return new GraphEdge[]{dooredge, door2wing, branchedge};
    }

    /**
     * Create approach to aircraft wing for refueling.
     * <p>
     * layerid isType created internally and the same for all created segements.
     * <p>
     * Benutzt bewusst keine Graphposition fürs Aircraft, weil das Aircraft vielleicht gar nicht exakt steht.
     * Returns [edge at door],[returnsuccessor].
     * <p>
     * Der Wingapproach geht avon aus, das die fuelpipe auf der rechten Seite des fuel truck ist. Und der Truck aoll ohne zurücksetzen wegfahren können,
     * damit auch mal Auflieger verwendbar sind. Ein bischen eng ist das aber schon.
     * Skizze 32
     *
     * @return
     */
    public GraphEdge[] createFuelingApproach(Vector3 aircraftposition, Vector2 aircraftdirection, Vector3 prjleftwingapproachpoint, Vector3 worldrearpoint) throws NoElevationException {
        int layer = groundnetgraph.newLayer();
        //heading appx parallel to wing of aircraft
        Vector2 approachdirection = aircraftdirection.rotate(new Degree(110));
        double approachlen = TrafficGraph.MINIMUMPATHSEGMENTLEN + 0.001f;//10;
        // wegen kleiner besser kürzer. bypass wird eh nicht gemacht. Das hat aber keinen Zweck. Da brächte ich einen kleineren Radius.
        //approachlen=GroundNet.MINIMUMPATHSEGMENTLEN/2;
        Vector3 innerpos = prjleftwingapproachpoint;
        GraphNode innernode = addNodeToGraph("innernode", innerpos);
        GraphNode outernode = addNodeToGraph("outernode", innerpos.add(buildVector3FromVector2(approachdirection.normalize().multiply(approachlen), null)));

        GraphEdge wingedge = groundnetgraph.getBaseGraph().connectNodes(innernode, outernode, "wingedge", layer);

        GraphNode approachbegin = addNodeToGraph("enternode", innerpos.subtract(Vector3.buildFromVector2(
                // statt fix 30m evtl. halbe Spannweite
                aircraftdirection.normalize().multiply(30))));
        GraphEdge approach = groundnetgraph.getBaseGraph().connectNodes(approachbegin, innernode, "wingapproach", layer);


        //default merge/branch node isType getFirst node if no specific node can be found.
        GraphNode branchnode = groundnetgraph.getBaseGraph().getNode(0);
        Object[] o = findHitEdge(new PositionHeading(approachbegin.getLocation(), MathUtil2.getHeadingFromDirection(aircraftdirection.negate())));
        GraphEdge bestHitEdge = null;
        if (o != null) {
            bestHitEdge = (GraphEdge) o[0];
            //den Punkt am nächsten zum rearpoint nehmen.
            double distanceoffrom = Vector3.getDistance(bestHitEdge.from.getLocation(), (worldrearpoint));
            double distanceofto = Vector3.getDistance(bestHitEdge.to.getLocation(), (worldrearpoint));
            if (distanceoffrom < distanceofto) {
                branchnode = bestHitEdge.from;
            } else {
                branchnode = bestHitEdge.to;
            }
        }

        GraphEdge branchedge = groundnetgraph.getBaseGraph().connectNodes(branchnode, approachbegin, "branchedge", layer);
        return new GraphEdge[]{wingedge, approach, branchedge, bestHitEdge};
    }

    /**
     * Returns XYZ projected coordinates for aircraft local coordinates.
     * a possible z offset (eg. door height) in the local coordinates isType lost here. This isType intended here,
     * because the resulting location isType used as vehicle destination. And the vehicle moves on the ground.
     */
    public Vector3 getProjectedAircraftLocation(Vector3 aircraftposition, Degree heading, Vector3 aircraftlocal) throws NoElevationException {
        Vector3 prjpos = aircraftposition.add(aircraftlocal.rotate(getRotationZfromHeading(heading)));
        return buildVector3FromVector2(new Vector2(prjpos.getX(), prjpos.getY()), null);
    }

    public static Quaternion getRotationZfromHeading(Degree heading) {
        return Quaternion.buildRotationZ(MathUtil2.getDegreeFromHeading(heading));
    }

    /**
     * name isType "create" instead of "find" because a temporary arc isType added.
     *
     * @return
     */
    public TurnExtension createBack(GraphNode startnode, GraphEdge dooredge, GraphEdge successor) {
        int layer = groundnetgraph.newLayer();
        TurnExtension turn = GraphUtils.createBack(groundnetgraph.getBaseGraph(), startnode, dooredge, successor, layer);
        return turn;
    }

    /**
     * Soll ohne zurücksetzen gehen wegen evtl. Auflieger.
     * Einfach etwas verlängern und dann zurück zur mergnode oder approachbegin. Oder besser zur nächsliegenden.
     * Den Rest macht smoothing.
     * Da muss aber noch eine Edge dazwischen, sonst kann der Winkel zu spitz werden.
     * Eigenes Layer, damit nicht der Approach fuer den Ruckweg genommen wird.
     *
     * @return
     */
    public GraphEdge[] createWingReturn(GraphEdge wingedge, GraphEdge wingbranchedge, Vector2 aircraftdirection) {
        int layer = groundnetgraph.newLayer();
        // den Radius nach vorne und dann im rechten Winkel. 
        GraphEdge return0 = GraphUtils.extendWithEdge(groundnetgraph.getBaseGraph(), wingedge, TrafficGraph.SMOOTHINGRADIUS + 0.001f, layer);
        return0.setName("return0");
        GraphEdge return1 = GraphUtils.extend(groundnetgraph.getBaseGraph(), return0.to, Vector3.buildFromVector2(aircraftdirection.negate()), TrafficGraph.MINIMUMPATHSEGMENTLEN, layer);
        return1.setName("return1");
        GraphNode nearest = groundnetgraph.getBaseGraph().findNearestNode(return1.to.getLocation(), new NodeToLayer0Filter());

        GraphEdge return2 = groundnetgraph.getBaseGraph().connectNodes(return1.to, /*wingbranchedge.from*/nearest, "return12", layer);
        return new GraphEdge[]{return0, return1, return2};
    }

    public void createRunwayEntry(Runway runway) throws NoElevationException {
        RunwayHelper runwayHelper = new RunwayHelper(runway, TrafficHelper.getEllipsoidConversionsProviderByDataprovider());
        GraphNode positiononrunway = groundnetgraph.getBaseGraph().findNodeByName(runway.enternodefromgroundnet);
        if (positiononrunway == null) {
            logger.warn("positiononrunway not found: " + runway.enternodefromgroundnet + " on " + runway.getName());
            return;
        }
        GeoCoordinate entrypoint = GeoCoordinate.fromLatLon(runwayHelper.getEntrypoint(), 0);
        GeoCoordinate holdingpoint = GeoCoordinate.fromLatLon(runwayHelper.getHoldingPoint(), 0);
        //, String runwayname
        GraphNode entry = addNodeByProjecting("entry", entrypoint);
        // 12.5.20: Das mit dem Holding ist ja so eone Sache. Jetzt mal unterschieden für beide Richtungen, zugeschnitten auf EDDK 14L.
        GraphNode holding = addNodeByProjecting("holding-" + runway.getFromNumber(), holdingpoint);
        groundnetgraph.getBaseGraph().connectNodes(positiononrunway, entry);
        groundnetgraph.getBaseGraph().connectNodes(entry, holding);
    }

    /**
     * Das Holding ist nicht Bestandteil des Groundnet, sondern muss seaparat ueber createRunwayEntry angelegt werden.
     * Der Begriff "holding" ist unpassend, denn der "Taxi Holding Point" ist VOR der Runway, um Starts und
     * Landungen anderer zuzulassen.
     * 12.5.20: name isr nocht from oder "to".
     *
     * @param runwayname
     * @return
     */
    public GraphNode getHolding(String runwayname) {
        return groundnetgraph.getBaseGraph().findNodeByName("holding-" + runwayname);
    }

    /**
     * returns altitude in meter
     * the airport elevation might not fit to scenery (eg. EDDKs 92m isType too high)
     * geodinfo might fail when scenery isn't yet loaded. For refreshing altitude, flag needsupdate isType used.
     * <p>
     * TODO 4.1.19: Das muss entkoppelt werden. 4.11.19:Was genau? Vielleicht der Zugriff auf SystemManager? Aber mein Gott. Dann ist er zumindest immer aktuell und es wird
     * keine Referenz lange vorgehalten.
     * 24.5.2020: Simplified: Exception when elevation isType not available. Then loading must be relaunched.
     */
    private double/*Elevation*/ getElevationForLocation(/*4.11.19 AirportConfig airport,*/ LatLon/*SGGeod*/ coor) throws NoElevationException {
        /*if (unittesting) {
        # non zero altitude for detecting rotation issues
        # tests in general have no scenery and no altitude
            return { alt: virtualtestingaltitude, needsupdate : 0 };
        }*/
        //if (position == nil) {
        //    pos = geo.aircraft_position();
        //}
        double alt;//4.11.19  = airport.getElevation();//default
        boolean needsupdate = false;
        //var info = geodinfo(pos.lat(), pos.lon());
        /*MA31 Terrain*/
        ElevationProvider tep = (/*MA31Terrain*/ElevationProvider) SystemManager.getDataProvider(SystemManager.DATAPROVIDERELEVATION);
        Double ele = null;
        if (tep != null) {
            ele = tep.getElevation(coor.getLatDeg().getDegree(), coor.getLonDeg().getDegree());//num(info[0]);

            //logging.debug("altitude="~alt~" "~info[0]);
        }
        if (ele == null) {
            //logger.warn("no altitude from geodinfo(" + coor + "). Using airportelevation " + alt + " m");
            logger.warn("no altitude from geodinfo(" + coor + ").");
            throw new NoElevationException("no altitude from geodinfo(" + coor + ")");
            /*24.5.20needsupdate = true;
            //Tests erwarten airport elevation. Die setzen jetzt aber einen Provider.
            alt = 0;*/
        } else {
            alt = (double) ele;
        }
        //TODO +5?
        return  /*new Elevation(alt, needsupdate)*/alt;
    }

    /**
     * NodeCoord.elevation ist wohl leer!
     * Das geht mit setLocation() nur, weil das Groundnet projected ist. Reichlich doof das ganze.
     * Das kann man bestimmt optimieren. Irgendwie ist das ganze Murks. TODO
     */
    /*24.5.20 public void updateElevation() {
        needsUpdate = false;
        for (int i = 0; i < groundnetgraph.getNodeCount(); i++) {
            GraphNode graphNode = groundnetgraph.getNode(i);
            SGGeod coor = null;
            if (graphNode.customdata instanceof NodeCoord) {
                coor = ((NodeCoord) graphNode.customdata).coor;
            }
            if (graphNode.customdata instanceof TaxiwayNode) {
                coor = ((TaxiwayNode) graphNode.customdata).coor;
            }
            if (graphNode.customdata instanceof Parking) {
                coor = ((Parking) graphNode.customdata).coor;
            }
            Vector2 projected = projection.project(coor);
            Vector3 point = buildVector3FromVector2(projected, coor);
            graphNode.setLocationOnlyForSpecialPurposes(point);
        }
        logger.debug("needsUpdate=" + needsUpdate);
    }*/
}

class NodeToLayer0Filter implements GraphNodeFilter {

    @Override
    public boolean acceptNode(GraphNode n) {
        for (int i = 0; i < n.getEdgeCount(); i++) {
            if (n.getEdge(i).getLayer() == 0) {
                return true;
            }
        }
        return false;
    }
}

class GroundNetGraphValidator implements GraphValidator {
    GroundNetGraphValidator() {

    }

    @Override
    public boolean nodesValidForEdge(GraphNode n1, GraphNode n2) {
        // Wert einfach mal willkürlich
        return Vector3.getDistance(n1.getLocation(), n2.getLocation()) > 0.0001f;
    }
}