package de.yard.threed.trafficfg;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Event;
import de.yard.threed.core.InitMethod;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.Payload;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ViewPoint;
import de.yard.threed.engine.ecs.EcsTestHelper;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.traffic.SphereProjections;
import de.yard.threed.traffic.SphereSystem;
import de.yard.threed.traffic.TrafficConfig;
import de.yard.threed.traffic.TrafficEventRegistry;
import de.yard.threed.traffic.TrafficHelper;
import de.yard.threed.traffic.geodesy.GeoCoordinate;
import de.yard.threed.trafficfg.flight.GroundNetMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static de.yard.threed.traffic.SphereSystem.USER_REQUEST_SPHERE;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Extracted from traffic-advanced:SphereSystemExtTest
 */
public class SphereSystemTest {

    SceneNode world;

    @BeforeEach
    public void setup() {
        InitMethod initMethod = new InitMethod() {
            @Override
            public void init() {
                world = new SceneNode();


            }
        };

        Platform platform = FgTestFactory.initPlatformForTest(false, false);

        EngineTestFactory.loadBundleAndWait("traffic-fg");
    }

    @Test
    @Disabled
    public void testEDDKWithConfigXml() throws Exception {
//TODO 16.3.24
        startSimpleTest("traffic-fg:flight/EDDK.xml");

        List<Event> completeEvents = EcsTestHelper.getEventsFromHistory(TrafficEventRegistry.TRAFFIC_EVENT_SPHERE_LOADED);
        assertEquals(1, completeEvents.size(), "completeEvents.size");
        // 1 because of TRAFFIC_REQUEST_LOADGROUNDNET
        assertEquals(1, SystemManager.getRequestCount(), "requests ");
        Request request = SystemManager.getRequest(0);
        assertEquals("TRAFFIC_REQUEST_LOADGROUNDNET", request.getType().getLabel());
        //27.12.21 assertNotNull("", DefaultTrafficWorld.getInstance());
        SphereProjections projections = TrafficHelper.getProjectionByDataprovider();
        assertNotNull(projections);
        assertNotNull(projections.projection);
        assertTrue(projections.backProjection == null);
        TestUtils.assertLatLon(GroundNetMetadata.getAirport("EDDK").getCenter(), projections.projection.getOrigin(), 0.01, "EDDK origin");


        List<ViewPoint> viewpoints = TrafficHelper.getViewpointsByDataprovider();
        // auf die 747/vehicle home und center 0,0. Aber nicht der fuer EDDF
        assertEquals(2, viewpoints.size(), "viewpoints");
        ViewPoint viewPoint = viewpoints.get(0);

        //Woher kommt denn die vehiclelsit? Das muessten doch ca.5 oder 7 sein. assertEquals("vehiclelist", 1, TrafficSystem.vehiclelist.size());
    }

    private void startSimpleTest(String tilename) {

        SystemManager.addSystem(new SphereSystem(null, null));

        SystemManager.putRequest(new Request(USER_REQUEST_SPHERE, new Payload(tilename, new ArrayList())));
        //ein Request muss anliegen
        assertEquals(1, SystemManager.getRequestCount(), "requests ");
        //EcsTestHelper.processRequests();
        EcsTestHelper.processSeconds(2);
    }


}
