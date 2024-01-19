package de.yard.threed.trafficfg.config;

import de.yard.threed.core.LatLon;
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
 * <p>
 * Created on 23.02.18.
 */
public class AirportConfig {
    private String icao;
    private GroundNetMetadata groundNetMetadata;
    // 20.11.23 NativeNode->LocatedVehicle
    List<LocatedVehicle> vehicles;
    //17.6.20 public weil er bloederweise ueberschrieben werden muss
    public Airport airport;

    /**
     * 9.6.20: Der Airport muss in GroundNetMetadata bekannt sein!
     * @param icao
     * @param vehicles
     */
    public AirportConfig(String icao, List<LocatedVehicle> vehicles) {
        //this.icao = icao;
        this.vehicles = vehicles;
        groundNetMetadata = GroundNetMetadata.getMap().get(icao);
        //9.6.20
        airport=groundNetMetadata.airport;
    }

    /**
     * AlternativConstructor fuer wenn es keine Config Daten gibt.
     * 23.5.2020
     */
    public AirportConfig(Airport airport) {
        this.airport = airport;
    }

    public String getHome() {
        if (groundNetMetadata==null){
            return null;
        }
        return groundNetMetadata.home;
    }

    public LatLon getCenter() {
        //if (airport!=null) {
            return airport.getCenter();
        //}
        //return groundNetMetadata.airport.
    }

    public List<String> getDestinationlist() {
        return groundNetMetadata.destinationlist;
    }

    public Runway[] getRunways() {
        return /*groundNetMetadata.*/airport.getRunways();
    }

    public int getVehicleCount() {
        return vehicles.size();
    }

    public /*20.11.23 SceneVehicle*/LocatedVehicle getVehicle(int index) {
        return /*20.11.23 new SceneVehicle*/(vehicles.get(index));

    }

    public double getElevation() {
        return groundNetMetadata.elevation;
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
    public static AirportConfig buildFromAirportConfig(String icao) {
        if (icao == null) {
            return null;
        }

        TrafficConfig defs = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("traffic-advanced"), BundleResource.buildFromFullString(icao+".xml"));
AirportDefinition airportDefinition = defs.findAirportDefinitionsByIcao(icao).get(0);
        /*for (int i = 0; i < airports.size(); i++) {
            NativeNode airportnode = airports.get(i);
            if (icao.equals(XmlHelper.getStringAttribute(airportnode, "icao", null))) {
                return new AirportConfig(icao, XmlHelper.getChildNodeList(airportnode, "vehicles", "vehicle"));
            }
        }*/
        return new AirportConfig(icao,airportDefinition.getVehicles());//null;
    }
}
