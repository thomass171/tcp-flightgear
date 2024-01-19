package de.yard.threed.trafficadvanced;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Event;
import de.yard.threed.core.InitMethod;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.Payload;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.testutil.SimpleEventBusForTesting;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ViewPoint;
import de.yard.threed.engine.ecs.EcsTestHelper;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.javacommon.ConfigurationByEnv;
import de.yard.threed.javacommon.JavaBundleResolverFactory;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import de.yard.threed.traffic.SphereProjections;
import de.yard.threed.traffic.SphereSystem;
import de.yard.threed.traffic.TrafficConfig;
import de.yard.threed.traffic.TrafficEventRegistry;
import de.yard.threed.traffic.TrafficHelper;
import de.yard.threed.traffic.config.SceneConfig;
import de.yard.threed.traffic.geodesy.GeoCoordinate;
import de.yard.threed.trafficadvanced.apps.FlatAirportScene;
import de.yard.threed.trafficfg.flight.GroundNetMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static de.yard.threed.traffic.SphereSystem.USER_REQUEST_SPHERE;
import static org.junit.jupiter.api.Assertions.*;


/**
 * <p>
 * Created by thomass on 7.10.21.
 */
public class SphereSystemExtTest {

    SceneNode world;

    @BeforeEach
    public void setup() {
        InitMethod initMethod = new InitMethod() {
            @Override
            public void init() {
                world = new SceneNode();

                //ohne Elevation wird kein groundnet geladen
                //??SystemManager.putDataProvider(SystemManager.DATAPROVIDERELEVATION, TerrainElevationProvider.buildForStaticAltitude(17));

            }
        };

        /*5.12.23 EngineTestFactory.initPlatformForTest(new String[]{"engine", "data-old", "osmscenery", "traffic"},
                new SimpleHeadlessPlatformFactory(new SimpleEventBusForTesting(),JavaBundleResolverFactory.bySimplePath(GranadaPlatform.GRANDA_BUNDLE_PATH)), initMethod,
                ConfigurationByEnv.buildDefaultConfigurationWithEnv(new HashMap<>()));*/
        Platform platform = FgTestFactory.initPlatformForTest(false, false);

        EngineTestFactory.loadBundleSync("traffic-advanced");

        //5.12.23 TODO ?? AbstractSceneRunner.instance.httpClient = new AirportDataProviderMock();
    }

    @Test
    public void testFlatEDDKWithConfigXml() throws Exception {
        /*DefaultTrafficWorld.instance = null;
        assertNull("", DefaultTrafficWorld.getInstance());

        TrafficWorldConfig tw = new TrafficWorldConfig("data-old", "TrafficWorld.xml");
        SceneConfig sceneConfig = tw.getScene("Flight");
        new TrafficWorld2D(/*tw,* / sceneConfig);*/

        startSimpleTest(FlatAirportScene.DEFAULT_TILENAME/*"dummy:EDDK"*/,"GroundServices");

        List<Event> completeEvents = EcsTestHelper.getEventsFromHistory(TrafficEventRegistry.EVENT_LOCATIONCHANGED);
        assertEquals(1, completeEvents.size(),"completeEvents.size");
        // 1 because of TRAFFIC_REQUEST_LOADGROUNDNET
        assertEquals(1, SystemManager.getRequestCount(),"requests ");
        Request request = SystemManager.getRequest(0);
        assertEquals("TRAFFIC_REQUEST_LOADGROUNDNET", request.getType().getLabel());
        //27.12.21 assertNotNull("", DefaultTrafficWorld.getInstance());
        SphereProjections projections = TrafficHelper.getProjectionByDataprovider();
        assertNotNull(projections);
        assertNotNull(projections.projection);
        assertTrue(projections.backProjection==null);
        TestUtils.assertLatLon( GroundNetMetadata.getMap().get("EDDK").airport.getCenter(),projections.projection.getOrigin(),0.01,"EDDK origin");


        List<ViewPoint> viewpoints = TrafficHelper.getViewpointsByDataprovider();
        // auf die 747/vehicle home und center 0,0. Aber nicht der fuer EDDF
        assertEquals(2, viewpoints.size(), "viewpoints");
        ViewPoint viewPoint = viewpoints.get(0);

        //Woher kommt denn die vehiclelsit? Das muessten doch ca.5 oder 7 sein. assertEquals("vehiclelist", 1, TrafficSystem.vehiclelist.size());
    }



    /*geht noch nicht wegen TrafficWorldSingleton @Test
    public void testWithAirportService() throws Exception {
        assertNull("",DefaultTrafficWorld.getInstance());

        runSimpleTest();

        assertNull("",DefaultTrafficWorld.getInstance());
    }*/


    private void runSimpleTest(String tilename) {
    }

    private void startSimpleTest(String tilename,String scene) {

        if (tilename == null || tilename.endsWith("EDDK")) {
            //DefaultTrafficWorld.instance = null;
            //assertNull("", DefaultTrafficWorld.getInstance());

            //4.12.23 TrafficWorldConfig tw =  TrafficWorldConfig.readConfig("data-old", "TrafficWorld.xml");
            TrafficConfig eddkFlat = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("traffic-advanced"), BundleResource.buildFromFullString("EDDK-flat.xml"));
            assertNotNull(eddkFlat);

            SceneConfig sceneConfig = null;//4.12.23 tw.getScene(scene);
            //new TrafficWorld2D(/*tw,*/ sceneConfig);

            SystemManager.addSystem(new SphereSystem(null, null, GeoCoordinate.fromLatLon(new LatLon(new Degree(50.86538f), new Degree(7.139103f)), 0), sceneConfig));
        } else {
            SystemManager.addSystem(new SphereSystem(null, null, null, null));

        }

        SystemManager.putRequest(new Request(USER_REQUEST_SPHERE, new Payload(tilename, new ArrayList())));
        //ein Request muss anliegen
        assertEquals(1, SystemManager.getRequestCount(), "requests ");
        //EcsTestHelper.processRequests();
        EcsTestHelper.processSeconds(2);
    }


}
