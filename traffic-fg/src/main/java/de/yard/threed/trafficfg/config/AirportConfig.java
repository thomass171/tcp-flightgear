package de.yard.threed.trafficfg.config;

import de.yard.threed.core.LatLon;
import de.yard.threed.core.Util;
import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.traffic.TrafficConfig;
import de.yard.threed.traffic.config.SceneVehicle;
import de.yard.threed.trafficcore.config.AirportDefinition;
import de.yard.threed.trafficcore.config.LocatedVehicle;
import de.yard.threed.trafficcore.model.Airport;
import de.yard.threed.trafficcore.model.Runway;
import de.yard.threed.trafficfg.flight.GroundNetMetadata;

import java.util.List;

/**
 * Voruebergehende Abbildung eines Airports, bis alles aus einem Airport/FG Bundle gelesen wird
 * und der Config (Vehicles/parkpos) gelesen wird. Muss dann evtl. nach config?
 * <p>
 * 12.5.20: Das ist auch schon wieder deprecated. Es gibt jetzt {@link Airport}
 * <p>
 * Wobei hier aber ja noch ganz andere Sachen stehen, z.B. vehicles, parkpos, viewpoints. Also doch nicht ganz deprecated.
 * 16.5.20: Vor allem wird ein trafficgraph für Runways gebraucht. Groundnet nicht unbedingt.
 * 9.6.20:Aber Airport hier mit unterzubringen ist doch fragwürdig.
 * 20.11.23: Now we have AirportDefinition from XML config which is a new option.
 * 15.3.24: Refactoring. Composition of AirportDefinition and Airport
 * <p>
 * Created on 23.02.18.
 */
public class AirportConfig {
    private Airport airport;
    private AirportDefinition airportDefinition;

    /**
     * 9.6.20: Der Airport muss in GroundNetMetadata bekannt sein!
     * 15.3.24: private because builder should be used
     */
    private AirportConfig(AirportDefinition airportDefinition) {
        this.airportDefinition = airportDefinition;
    }

    public String getHome() {
        return airportDefinition.getHome();
    }

    public LatLon getCenter() {
        return airport.getCenter();
    }

    public Runway[] getRunways() {
        return airport.getRunways();
    }

    public int getVehicleCount() {
        return airportDefinition.getVehicles().size();
    }

    public LocatedVehicle getVehicle(int index) {
        return airportDefinition.getVehicles().get(index);

    }

    /**
     * 15.3.24: Having one elevation for an airport is just a quick solution for tests. Otherwise its confusing.
     * So made private and fail for having this comment.
     * @return
     */
    private double getElevation() {
        Util.nomore();// return groundNetMetadata.elevation;
        return 0;
    }

    public Airport getAirport() {
        return airport;
    }

    /**
     * 4.12.23: Moved here from TrafficWorldConfig.
     * This really needs a merge with AirportDefinition.
     *
     * @return
     */
    public static AirportConfig buildFromAirportConfig(String bundlename, String fullname, String icao, String groundnetXml) {
        if (icao == null) {
            return null;
        }

        TrafficConfig defs = TrafficConfig.buildFromBundle(BundleRegistry.getBundle(bundlename), BundleResource.buildFromFullString(fullname));
        AirportDefinition airportDefinition = defs.findAirportDefinitionsByIcao(icao).get(0);
        /*for (int i = 0; i < airports.size(); i++) {
            NativeNode airportnode = airports.get(i);
            if (icao.equals(XmlHelper.getStringAttribute(airportnode, "icao", null))) {
                return new AirportConfig(icao, XmlHelper.getChildNodeList(airportnode, "vehicles", "vehicle"));
            }
        }*/
        AirportConfig airportConfig = new AirportConfig(airportDefinition);
        airportConfig.airport = GroundNetMetadata.getAirport(icao);
        airportConfig.airport.setGroundNetXml(groundnetXml);
        return airportConfig;
    }
}
