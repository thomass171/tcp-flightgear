package de.yard.threed.trafficfg.flight;

import de.yard.threed.core.StringUtils;
import de.yard.threed.graph.GraphComponent;
import de.yard.threed.graph.GraphNode;
import de.yard.threed.traffic.NodeCoord;
import de.yard.threed.core.GeoCoordinate;

/**
 * Created by thomass on 28.03.17.
 */
public class TaxiwayNode extends GraphComponent {
    
    public String holdPointType;
    GraphNode node;
    boolean isOnRunway;
    boolean isPushBackHoldpoint = false;
    public GeoCoordinate coor;

    public TaxiwayNode(GraphNode node, String holdPointType, boolean isOnRunway) {
        this.node=node;
        this.holdPointType=holdPointType;
        this.isOnRunway=isOnRunway;
        if (StringUtils.equalsIgnoreCase(holdPointType,"PushBack")){
            isPushBackHoldpoint=true;
        }
        coor=((NodeCoord)node.customdata).coor;
    }
}
