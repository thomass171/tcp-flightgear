package de.yard.threed.trafficfg;

import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.DataProvider;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsHelper;
import de.yard.threed.engine.ecs.EcsTestHelper;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.flightgear.ecs.FgAnimationComponent;
import de.yard.threed.graph.Graph;
import de.yard.threed.graph.GraphMovingComponent;
import de.yard.threed.graph.GraphPath;
import de.yard.threed.graph.ProjectedGraph;
import de.yard.threed.traffic.*;
import de.yard.threed.trafficcore.EllipsoidCalculations;
import de.yard.threed.traffic.flight.FlightRouteGraph;
import de.yard.threed.core.GeoCoordinate;
import de.yard.threed.trafficfg.flight.GroundNet;
import de.yard.threed.trafficfg.flight.GroundServicesSystem;
import de.yard.threed.trafficfg.flight.Parking;

import static de.yard.threed.javanative.JavaUtil.sleepMs;
import static org.junit.jupiter.api.Assertions.*;

public class TravelSceneTestHelper {

    public static void waitForSphereLoaded(SceneRunnerForTesting sceneRunner) throws Exception {
        TestUtils.waitUntil(() -> {
            sceneRunner.runLimitedFrames(1);
            return EcsTestHelper.getEventsFromHistory(TrafficEventRegistry.TRAFFIC_EVENT_SPHERE_LOADED).size() > 0;
        }, 60000);
    }

    public static void validateSphereProjections() {
        SphereProjections sphereProjections = TrafficHelper.getProjectionByDataprovider(null);
        assertNotNull(sphereProjections);
        assertNull(sphereProjections.projection);
        assertNotNull(sphereProjections.backProjection);
        //??TestUtils.assertLatLon(GroundNetMetadata.getMap().get("EDDK").airport.getCenter(), projections.projection.getOrigin(), 0.01, "EDDK origin");*/
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

        assertEquals("z0", groundnetEDDK.groundnetgraph.getBaseGraph().getGraphOrientation().getName());
    }

    /**
     * Typically "c172p" or "bluebird". "name" might be null to just load the next.
     */
    public static EcsEntity loadAndValidateVehicle(SceneRunnerForTesting sceneRunner, String name, String expectedVehicleAndBundleName) throws Exception {
        Request request = RequestRegistry.buildLoadVehicle(UserSystem.getInitialUser().getId(), name, null, null, null);
        SystemManager.putRequest(request);
        TestUtils.waitUntil(() -> {
            sceneRunner.runLimitedFrames(10);
            sleepMs(100);
            return BundleRegistry.getBundle(expectedVehicleAndBundleName) != null;
        }, 30000);
        assertNotNull(BundleRegistry.getBundle(expectedVehicleAndBundleName));

        EcsEntity vehicleEntity = EcsHelper.findEntitiesByName(expectedVehicleAndBundleName).get(0);
        assertEquals(expectedVehicleAndBundleName, vehicleEntity.getName());
        //log.debug(c172p.getSceneNode().dump(" ", 0));

        if ("c172p".equals(expectedVehicleAndBundleName)) {
            // Optionals should not have been created. But testing that way is a false positive for unknwn reasons.
            assertEquals(0, SceneNode.findByName("LandingLightCone").size());

            TestUtils.waitUntil(() -> {
                sceneRunner.runLimitedFrames(10);
                return SceneNode.findByName("Aircraft/Instruments-3d/garmin196/garmin196.gltf").size() > 0;
            }, 30000);

            // garmin has multiple components and names. just look for one
            NativeSceneNode garmin196 = SceneNode.findByName("Aircraft/Instruments-3d/garmin196/garmin196.gltf").get(0);
            //16.8.24 TODO assertTrue(Texture.hasTexture("screens.png"), "garmin.texture");
        } else if ("bluebird".equals(expectedVehicleAndBundleName)) {
            // nothing yet to check
        } else {
            throw new RuntimeException("unexpected/unvalidated vehicle");
        }
        return vehicleEntity;
    }

    /**
     * Starts and validates default trip
     */
    public static void startAndValidateDefaultTrip(SceneRunnerForTesting sceneRunner, EcsEntity aircraft,
                                                   boolean expectGeoCartGraphCoordinates) throws Exception {
        // start aircraft (eg.c172p) and wait until it has a flight route
        GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(aircraft);
        assertNull(gmc.getPath());
        // 15.5.25: We have no generic 'start' via request yet
        TravelHelper.startDefaultTrip(aircraft);
        //SystemManager.putRequest(...;

        TestUtils.waitUntil(() -> {
            sceneRunner.runLimitedFrames(10);
            sleepMs(100);
            return "Platzrunde".equals(gmc.getGraph().getName());
        }, 30000);

        Graph graph = gmc.getGraph();
        assertFalse(graph instanceof ProjectedGraph);
        GraphPath graphPath = gmc.getPath();
        assertNotNull(graphPath);

        validateGraphCoordinates(graph, expectGeoCartGraphCoordinates);

    }

    public static void validatePlatzrunde(FlightRouteGraph platzrunde, double expectedElevation, double elevationTolerance, boolean expectGeoCartGraphCoordinates) {
        EllipsoidCalculations rbcp = TrafficHelper.getEllipsoidConversionsProviderByDataprovider();
        assertNotNull(rbcp);

        validateGraphCoordinates(platzrunde.getGraph(), expectGeoCartGraphCoordinates);

        Vector3 firstLocation = platzrunde.getGraph().getNode(0).getLocation();
        if (expectGeoCartGraphCoordinates) {
            GeoCoordinate coord = rbcp.fromCart(firstLocation);
            if (coord.getElevationM() < 70) {
                DataProvider dataProvider = SystemManager.getDataProvider("Elevation");
            }
            assertEquals(expectedElevation, coord.getElevationM(), elevationTolerance, "elevation first node");
        } else {
            // if we do not expect geo coordinates, than graph is projected, where elevation probably is 0.0
            assertEquals(expectedElevation, firstLocation.getZ(), 0.001, "elevation first node");
        }
    }

    private static void validateGraphCoordinates(Graph graph, boolean expectGeoCartGraphCoordinates) {
        for (int i = 0; i < graph.getNodeCount(); i++) {
            Vector3 location = graph.getNode(i).getLocation();
            if (expectGeoCartGraphCoordinates) {
                assertTrue(Math.abs(location.getZ()) > 1000.0, "z coordinate not 3D: " + location.getZ());
            } else {
                assertTrue(Math.abs(location.getZ()) < 1000.0, "z coordinate not 2D: " + location.getZ());
            }
        }
    }


    public static void validateFgProperties(EcsEntity vehicle, boolean shouldHaveSpeed) {
        FgAnimationComponent fgAnimationComponent = FgAnimationComponent.getFgAnimationComponent(vehicle);
        // speed might vary, so test isn't absolutely reliable
        double speed = fgAnimationComponent.getPropertyValue(FgAnimationComponent.C172P_SPD);
        //log.debug("speed=" + speed);
        if (shouldHaveSpeed) {
            assertTrue(speed > 0.1 && speed < 1000, "" + speed);
        } else {
            assertTrue(Math.abs(speed) < 0.00000001, "" + speed);
        }
    }

    /**
     * Not sure how useful this extraction is. Should at least assert position/rotation
     */
    public static EcsEntity assertBluebird() {
        EcsEntity bluebird = EcsHelper.findEntitiesByName("bluebird").get(0);
        assertNotNull(bluebird);
        VehicleComponent vehicleComponent = VehicleComponent.getVehicleComponent(bluebird);
        //TODO not yet added assertNotNull(vehicleComponent);
        Vector3 posbluebird = bluebird.getSceneNode().getTransform().getPosition();
        Quaternion rotationbluebird = bluebird.getSceneNode().getTransform().getRotation();
        //log.debug("posbluebird=" + posbluebird);
        //log.debug("rotationbluebird=" + rotationbluebird);
        GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(bluebird);
        assertNotNull(gmc);

        // No entity is created for vehicle sub models. The animations are contained in the vehicle entity
        FgAnimationComponent fgAnimationComponent = FgAnimationComponent.getFgAnimationComponent(bluebird);
        assertNotNull(fgAnimationComponent);
        // currently 587 animations(!)
        assertTrue(fgAnimationComponent.animationList.size() > 100, "" + fgAnimationComponent.animationList.size());
        return bluebird;
    }
}
