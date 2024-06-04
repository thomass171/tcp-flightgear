package de.yard.threed.trafficfg;

import de.yard.threed.core.Degree;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.graph.GraphPosition;
import de.yard.threed.traffic.EllipsoidCalculations;
import de.yard.threed.traffic.TrafficContext;
import de.yard.threed.traffic.TrafficGraph;
import de.yard.threed.traffic.TrafficHelper;
import de.yard.threed.traffic.config.VehicleDefinition;
import de.yard.threed.traffic.geodesy.GeoCoordinate;
import de.yard.threed.trafficcore.config.LocatedVehicle;
import de.yard.threed.trafficcore.model.SmartLocation;
import de.yard.threed.trafficfg.config.AirportConfig;
import de.yard.threed.trafficfg.flight.GroundNet;
import de.yard.threed.trafficfg.flight.Parking;

import static de.yard.threed.traffic.TrafficHelper.isAircraft;

public class AirportTrafficContext implements TrafficContext {
    private Log logger = Platform.getInstance().getLog(AirportTrafficContext.class);

    GroundNet groundnet;
    AirportConfig airport;

    public AirportTrafficContext(GroundNet groundnet, AirportConfig airport) {
        this.groundnet = groundnet;
        this.airport = airport;
    }

    /**
     * Das Problem, dass graphposition hier evtl null bleibt, gab es immer schon.
     */
    @Override
    public GraphPosition getStartPosition(VehicleDefinition vconfig) {
        GraphPosition graphposition = null;
        // From TrafficHelper.launchVehicles
        Parking home = groundnet.getVehicleHome();
        if (home != null) {
            graphposition = groundnet.getParkingPosition(home);
        }
        //if (vconfig.getType().equals(VehicleComponent.VEHICLE_AIRCRAFT)) {
        if (isAircraft(vconfig)) {
            // position aircraft to the southwest most park position (just arbitrary).
            EllipsoidCalculations rbcp = TrafficHelper.getEllipsoidConversionsProviderByDataprovider();
            LatLon southwestL = rbcp.applyCourseDistance(airport.getCenter(), new Degree(225), 25000);
            GeoCoordinate southwest = GeoCoordinate.fromLatLon(southwestL, 0);
            //TODO das ist doch eine EDDK Kruecke
            southwest.setElevationM(75);
            Parking parking = groundnet.getParkPosNearCoordinates(rbcp.toCart(southwest, null, null));
            if (parking != null) {
                logger.debug("new aircraft at parking " + parking);
                GraphPosition newgraphposition = groundnet.getParkingPosition(parking);
                if (newgraphposition != null) {
                    graphposition = newgraphposition;
                }
            }
        }
        return graphposition;
    }

    @Override
    public int getVehicleCount() {
        return airport.getVehicleCount();
    }

    @Override
    public /*20.11.23 SceneVehicle*/LocatedVehicle getVehicle(int i) {
        return airport.getVehicle(i);
    }

    @Override
    public TrafficGraph getGraph() {
        return groundnet.groundnetgraph;
    }

    @Override
    public GraphPosition getStartPositionFromLocation(SmartLocation location) {
        return groundnet.getParkingPosition(groundnet.getParkPos(location.getParkPos()));
    }
}
