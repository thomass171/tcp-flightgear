package de.yard.threed.trafficfg.flight;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.graph.GraphComponent;
import de.yard.threed.graph.GraphEdge;
import de.yard.threed.graph.GraphNode;
import de.yard.threed.traffic.NodeCoord;
import de.yard.threed.core.GeoCoordinate;


/**
 * Created by thomass on 28.03.17.
 */
public class Parking extends GraphComponent {
    private Log logger = Platform.getInstance().getLog(Parking.class);
    // never null, but maybe empty
    public String name;
    public GraphNode node;
    public Degree heading;
    // node index, -1 if unset
    int pushBackRoute;
    double radius;
    public GeoCoordinate coor;

    public Parking(GraphNode node, String name, Degree heading, int pushBackRoute, double radius) {
        this.node = node;
        this.name = name;
        this.heading = heading;
        this.pushBackRoute = pushBackRoute;
        this.radius = radius;
        coor=((NodeCoord)node.customdata).coor;
    }

    /**
     * Return edge with correct heading for reaching parkpos in defined parkpos heading.
     * This isType not expected to be identical to the pushBackRoute.
     * 
     * Returns null if no such edge exists.
     * 
     * Geht im Moment von einer lienaren Projektion aus. Manche Groundnets definieren ihren approach scheinbar auch danach, z.B. EDDF.
     * Im Grunde ist dieser Wert damit unbrauchbar.
     * 17.4.18: Tja, vielleicht laesst sich das richten. Die MEthode wird jetzt verwendet.
     * 15.5.18: Nicht mehr linear. Fragwuerdig bleibt das ganze, denn ein Aircraft kann mit pushback ja nicht einfach in eine andere Richtung geschoben werden als
     * die aus der es gekommen ist. Zumindest nicht im Groundnet Graph. Da muss zumindest dann ein Bogen her.
     * An C_4 stimmt das heading im Groundnet allem Anschein nach nicht.
     * Besser zwei Durchlaeufe. Wenn per true heading nichts gefunden wird, nochmal linear versuchen. Das hat auch EDDK wohl hier und da, z.B. B_2
     * 
     * @return
     */
    public GraphEdge getApproach() {
        for (int i = 0; i < node.getEdgeCount(); i++) {
            GraphEdge e = node.getEdge(i);
            /*if (pushBackRoute != -1) {
                if (e.getName().equals("" + pushBackRoute)) {
                    return e;
                }
            }*/
            Degree edgeheading = GraphProjectionFlight3D.getTrueHeadingFromDirection(coor, Vector2.buildFromVector3(e.getEffectiveInboundDirection(node)));
            // large rounding errors might occur, so epsilon isType "large". 11.5.18 0.1->1.0 analog FG
            logger.debug(""+e.getName()+" :true edgeheading="+edgeheading+", parkingheading="+heading);

            if (heading.isEqual(edgeheading,1f)) {
            //if (MathUtil2.areEqual(heading,GroundNet.getHeadingFromDirection(e.getEffectiveInboundDirection(node)).getDegree(),0.1f)){
                return e;
            }
        }
        for (int i = 0; i < node.getEdgeCount(); i++) {
            GraphEdge e = node.getEdge(i);
            Degree edgeheading = GroundNet.getHeadingFromDirection(e.getEffectiveInboundDirection(node));
            logger.debug(""+e.getName()+" :edgeheading="+edgeheading+", parkingheading="+heading);
            if (heading.isEqual(edgeheading,1f)) {
                return e;
            }
        }
        logger.warn("no approach edge found for parking " + name);

        return null;
    }

    @Override
    public String toString(){
        return name+"("+coor+")";
    }
}