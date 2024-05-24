package de.yard.threed.trafficfg;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.ecs.DataProvider;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsTestHelper;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.graph.Graph;
import de.yard.threed.graph.GraphMovingComponent;
import de.yard.threed.graph.GraphNode;
import de.yard.threed.graph.GraphPath;
import de.yard.threed.graph.ProjectedGraph;
import de.yard.threed.traffic.EllipsoidCalculations;
import de.yard.threed.traffic.SphereProjections;
import de.yard.threed.traffic.TrafficEventRegistry;
import de.yard.threed.traffic.TrafficHelper;
import de.yard.threed.traffic.flight.FlightRouteGraph;
import de.yard.threed.traffic.geodesy.GeoCoordinate;
import de.yard.threed.trafficfg.flight.GroundNet;
import de.yard.threed.trafficfg.flight.GroundServicesSystem;
import de.yard.threed.trafficfg.flight.Parking;

import static de.yard.threed.javanative.JavaUtil.sleepMs;
import static org.junit.jupiter.api.Assertions.*;

public class TravelSceneTestHelper {

    /**
     * Starts and validates default trip
     */
    public static void assertDefaultTrip(SceneRunnerForTesting sceneRunner, EcsEntity aircraft,
                                         boolean expectGeoCartGraphCoordinates) throws Exception {
// start c172p and wait until it has a flight route
        GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(aircraft);
        assertNull(gmc.getPath());
        TravelHelper.startDefaultTrip(aircraft);
        TestUtils.waitUntil(() -> {
            sceneRunner.runLimitedFrames(10);
            sleepMs(100);
            return "Platzrunde".equals(gmc.getGraph().getName());
        }, 30000);

        Graph graph = gmc.getGraph();
        assertFalse(graph instanceof ProjectedGraph);
        GraphPath graphPath=gmc.getPath();
        assertNotNull(graphPath);

validateGraphCoordinates(graph,expectGeoCartGraphCoordinates);

    }

    public static void validateSphereProjections() {
        SphereProjections sphereProjections = TrafficHelper.getProjectionByDataprovider(null);
        assertNotNull(sphereProjections);
        assertNull(sphereProjections.projection);
        assertNotNull(sphereProjections.backProjection);
        //??TestUtils.assertLatLon(GroundNetMetadata.getMap().get("EDDK").airport.getCenter(), projections.projection.getOrigin(), 0.01, "EDDK origin");*/
    }

    public static void validatePlatzrunde(FlightRouteGraph platzrunde, double expectedElevation,  boolean expectGeoCartGraphCoordinates) {
        EllipsoidCalculations rbcp = TrafficHelper.getEllipsoidConversionsProviderByDataprovider();
        assertNotNull(rbcp);

        validateGraphCoordinates(platzrunde.getGraph(),expectGeoCartGraphCoordinates);

        Vector3 firstLocation = platzrunde.getGraph().getNode(0).getLocation();
        if (expectGeoCartGraphCoordinates) {
            GeoCoordinate coord = rbcp.fromCart(firstLocation);
            if (coord.getElevationM() < 70) {
                DataProvider dataProvider = SystemManager.getDataProvider("Elevation");
            }
            assertEquals(expectedElevation, coord.getElevationM(), 0.001, "elevation first node");
        } else {
            // if we do not expect geo coordinates, than graph is projected, where elevation probably is 0.0
            assertEquals(expectedElevation, firstLocation.getZ(), 0.001, "elevation first node");
        }
    }

    private static void validateGraphCoordinates(Graph graph,  boolean expectGeoCartGraphCoordinates){
        for (int i = 0; i < graph.getNodeCount(); i++) {
            Vector3 location = graph.getNode(i).getLocation();
            if (expectGeoCartGraphCoordinates) {
                assertTrue(Math.abs(location.getZ()) > 1000.0, "z coordinate not 3D: " + location.getZ());
            } else {
                assertTrue(Math.abs(location.getZ()) < 1000.0, "z coordinate not 2D: " + location.getZ());
            }
        }
    }

    public static void waitForSphereLoaded(SceneRunnerForTesting sceneRunner) throws Exception {
        TestUtils.waitUntil(() -> {
            sceneRunner.runLimitedFrames(1);
            return  EcsTestHelper.getEventsFromHistory(TrafficEventRegistry.TRAFFIC_EVENT_SPHERE_LOADED).size() > 0;
        }, 60000);
    }

    public static void validateGroundnet() {
        GroundNet groundnetEDDK = GroundServicesSystem.groundnets.get("EDDK");
        assertNotNull(groundnetEDDK);
        //assertNotNull(groundnetEDDK.projection);
        Parking home = groundnetEDDK.getVehicleHome();
        assertEquals("A20", home.name);
        // Even in 3D the groundnet graph is projected, which is seen by low coordinate values
        assertTrue(Math.abs(home.node.getLocation().getX()) < 3000, "x-coordinate of groundnet node < 3000");
        //LatLon backProjectedHome = groundnetEDDK.projection.unproject(Vector2.buildFromVector3(home.node.getLocation()));
        //TODO assertEquals(51.0, Math.round(backProjectedHome.getLatDeg().getDegree()));
    }
}
