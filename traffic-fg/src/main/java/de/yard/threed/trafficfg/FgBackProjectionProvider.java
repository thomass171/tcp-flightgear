package de.yard.threed.trafficfg;

import de.yard.threed.graph.GraphProjection;
import de.yard.threed.traffic.GraphBackProjectionProvider;
import de.yard.threed.trafficcore.geodesy.MapProjection;
import de.yard.threed.trafficfg.flight.GraphProjectionFlight3D;
import de.yard.threed.trafficfg.flight.GroundServicesSystem;

public class FgBackProjectionProvider implements GraphBackProjectionProvider {

    //GroundNet groundnetEDDK;

    public FgBackProjectionProvider() {
        //this.groundnetEDDK = groundnetEDDK;
    }

    @Override
    public GraphProjection/*Flight3D*/ getGraphBackProjection(MapProjection forwardProjection) {
        //GraphProjectionFlight3D graphprojection = new GraphProjectionFlight3D(GroundServicesSystem.groundnets.get("EDDK")/*DefaultTrafficWorld.getInstance().getGroundNet("EDDK")*/.projection);
        GraphProjectionFlight3D graphprojection = new GraphProjectionFlight3D(forwardProjection);
        return graphprojection;
    }
}
