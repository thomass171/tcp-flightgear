package de.yard.threed.trafficfg;


import de.yard.threed.core.Degree;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeodesy;
import de.yard.threed.flightgear.testutil.FgTestUtils;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import de.yard.threed.trafficcore.RunwayHelper;
import de.yard.threed.traffic.WorldGlobal;
import de.yard.threed.trafficcore.geodesy.SimpleMapProjection;
import de.yard.threed.traffic.osm.OsmRunway;
import de.yard.threed.trafficcore.model.Runway;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 * Auch fuer Projection und SGGeod.
 * <p>
 * Created by thomass on 07.02.16.
 */
public class GeodesyTest {
    //static Platform platform = TestFactory.initPlatformForTest(false,false,null,true);
    Platform platform = EngineTestFactory.initPlatformForTest( new String[] {"engine"}, new SimpleHeadlessPlatformFactory());

    SGGeod greenwich = new SGGeod(new Degree(0), new Degree(51.477524), 0);

    @Test
    public void test1() {


        SGGeod elsdorf = new SGGeod(new Degree(6.580982), new Degree(50.937770), 0.076);
        Vector3 v = (SGGeodesy.SGGeodToCart(elsdorf));
        System.out.println("elsdorf coord=" + v.dump(" "));
        // Gegenprobe
        SGGeod g = SGGeod.fromCart(v);
        Assertions.assertEquals( (float) elsdorf.getLatitudeRad(), (float) g.getLatitudeRad());
        Assertions.assertEquals( (float) elsdorf.getLongitudeRad(), (float) g.getLongitudeRad());

        // 0-Meridian/Äquator in Höhe 0. Muss in x den Erdradius ergeben.
        SGGeod meriaq = new SGGeod(0, 0, 0);
        v = (SGGeodesy.SGGeodToCart(meriaq));
        System.out.println("meriaq coord=" + v.dump(" "));
        Assertions.assertEquals( (float) SGGeodesy.EQURAD, v.getX());
        Assertions.assertEquals( 0, v.getY());
        Assertions.assertEquals( 0, v.getZ());

    }

    /**
     * WGS Coords von 3056410 center
     */
    @Test
    public void test2() {


        SGGeod center3056410 = SGGeodesy.SGCartToGeod(FlightGear.refbtgcenter);
        String wgs84 = center3056410.toWGS84decimalString();
        System.out.println("center3056410 coord=" + wgs84);
        // wahrscheinlich ist der Vergleich nicht immer zuverlässig, oder?
        //wegen double Assertions.assertEquals("center wgs84", "50.437501430681515, 6.625000144634338", wgs84);
        Assertions.assertEquals("50.43750000000188, 6.6250000000027915", wgs84,"center wgs84");

    }

    @Test
    public void testNordpol() {
        Vector3 coord = SGGeod.nordpol.toCart();
        TestUtils.assertVector3( new Vector3(0, 0, 6356752.314245179), coord,"nordpol coord");

        coord = SGGeod.suedpol.toCart();
        TestUtils.assertVector3(new Vector3(0, 0, -6356752.314245179), coord,"suedpol coord");
    }

    @Test
    public void testProjection() {
        SGGeod origin = SGGeod.fromGeoCoordinate(WorldGlobal.eddkoverview.location.coordinates);
        SimpleMapProjection projection = new SimpleMapProjection(origin);
        Vector2 projected = projection.project(origin);
        TestUtils.assertVector2( new Vector2(), projected,"origin");
        LatLon unprojected = projection.unproject(projected);
        Assertions.assertEquals( (float) origin.getLatitudeDeg().getDegree(), (float) unprojected.getLatDeg().getDegree(),"lat");
        Assertions.assertEquals((float) origin.getLongitudeDeg().getDegree(), (float) unprojected.getLonDeg().getDegree(),"lon");
        //TestUtil.assertEquals("elevation", (float) origin.getElevationM(), (float) unprojected.getElevationM());

        SGGeod n = new SGGeod(new Degree(7.028904), new Degree(51.876611), 170);
        projected = projection.project(n);
        TestUtils.assertVector2( new Vector2(-SimpleMapProjection.METERPERDEGREE / 10, SimpleMapProjection.METERPERDEGREE), projected,"origin");
        unprojected = projection.unproject(projected);
        Assertions.assertEquals( 51.876611f, (float) unprojected.getLatDeg().getDegree(),"lat");
        Assertions.assertEquals(7.028904f, (float) unprojected.getLonDeg().getDegree(),"lon");
        // belicbt 150 weil orogin 150 hat
        //TestUtil.assertEquals("elevation", 150, (float) unprojected.getElevationM());
    }

    @Test
    public void testEDDK14L() {

        Runway runway14l = OsmRunway.eddk14L();
        RunwayHelper runwayHelper = new RunwayHelper(runway14l,new FgCalculations());
        //der coor ist hinter dem Ende (Wiki).
        SGGeod coursed = SGGeod.fromLatLon(new FgCalculations().applyCourseDistance(runway14l.getTo(),runwayHelper.getHeading().reverse(), 2500f));
        System.out.println("coursed=" + coursed);
        // Referenz aus FG, geprüft in Google Maps
        FgTestUtils.assertSGGeod("ils14l2500", new SGGeod(new Degree(7.145059184988025f), new Degree(50.86944339170151f), 0/*runway14l.to.getElevationM())*/), coursed);
        //holding 50.880038164519554, 7.129699647939261 geprüft in Google Maps
        SGGeod holding = SGGeod.fromLatLon(runwayHelper.getHoldingPoint());
        FgTestUtils.assertSGGeod("holding", new SGGeod(new Degree(7.129699647939261), new Degree(50.880038164519554), 0), holding);
    }

    @Test
    public void testCourseAndDistance() {
        FgCalculations fgc = new FgCalculations();
        SGGeod start = SGGeod.nordpol;
        double distance = new FgCalculations().distanceTo(start,SGGeod.suedpol);
        Assertions.assertEquals( MathUtil2.PI * SGGeod.ERAD, (distance),"distance");
        start = new SGGeod(new Degree(7), new Degree(50), 0);
        SGGeod dest = SGGeod.fromLatLon(fgc.applyCourseDistance(start,new Degree(90), 1000),start.getElevationM());
        distance = new FgCalculations().distanceTo(start,dest);
        Assertions.assertEquals(1000, distance,0.0001,"distance");
        Degree course = new FgCalculations().courseTo(start,dest);
        Assertions.assertEquals(90f, (float) course.getDegree(), 0.01f,"course");

        dest = SGGeod.fromLatLon(fgc.applyCourseDistance(start,new Degree(-45), 1000),start.getElevationM());
        distance = new FgCalculations().distanceTo(start,dest);
        // 22.3.18: Mit der Berechnung mit den MathUtil float MEthoden hatte ich weniger Rundungsfehler und kam genau auf 1000 als mit double. Hmmmm. Suspekt.
        Assertions.assertEquals( 1000, distance, 0.1f,"distance");
        course = new FgCalculations().courseTo(start,dest);
        // Now with tcp-22 GeoTools value changed from -45 to 315.
        Assertions.assertEquals( 315f, (float) course.getDegree(), 0.01f,"course");

    }

    @Test
    public void testPrecision() {
        // Irgendeine Stelle im EDDK groundnet
        Vector3 v0 = new Vector3( 4001257.0f,500489.0f,4925190.0f);
        Vector3 v1 = new Vector3( 4001258.0f,500488.0f,4925191.0f);
        double distance = Vector3.getDistance(v0,v1);
        System.out.println("vector distance ="+distance);
        float f = 4925191.0f;
        for (int i=0;i<20;i++){
            System.out.println("f="+f);
            f+=0.3f;
        }
    }

}
