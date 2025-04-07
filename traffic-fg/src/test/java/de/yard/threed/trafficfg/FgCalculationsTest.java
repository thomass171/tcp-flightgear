package de.yard.threed.trafficfg;

import de.yard.threed.core.Degree;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.flightgear.core.simgear.geodesy.FgMath;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.traffic.EllipsoidCalculations;
import de.yard.threed.traffic.flight.FlightLocation;
import org.junit.jupiter.api.Test;

/**
 * auch SGQuat, OsgMath, FlightLocation.
 * <p>
 * 30.3.25: Renamed from FlightLocationTest to FgCalculationsTest
 * Created by thomass on 11.01.17.
 */
public class FgCalculationsTest {
    //static Platform platform = TestFactory.initPlatformForTest(false,false,null,true);
    static Platform platform = FgTestFactory.initPlatformForTest();
    static Log logger = Platform.getInstance().getLog(FgCalculationsTest.class);
    SGGeod suedpol = new SGGeod(new Degree(0), new Degree(-90), 0);
    SGGeod nordpol = new SGGeod(new Degree(0), new Degree(90), 0);
    SGGeod greenwich = new SGGeod(new Degree(0), new Degree(51.477524), 0);
    SGGeod nullequator = new SGGeod(new Degree(0), new Degree(0), 0);
    EllipsoidCalculations ec = new FgCalculations();

    /**
     * siehe Wiki
     */
    @Test
    public void testSGQuat() {
        Quaternion q = FgMath.fromLonLatRad(0, 0);
        Quaternion ref =  Quaternion.buildFromAngles(new Degree(0), new Degree(-90), new Degree(0));
        TestUtils.assertQuaternion( ref, q,"bei 0,0");

        Quaternion qsuedpol = FgMath.fromLonLatRad(0, -Math.PI / 2);
        Quaternion refsuedpol =  Quaternion.buildFromAngles(new Degree(0), new Degree(0), new Degree(0));
        TestUtils.assertQuaternion(refsuedpol, qsuedpol,"suedpol");

    }

    /**
     * siehe Wiki
     */
    @Test
    public void testOsgMath() {
        // der Nordpol ist quasi Default und hat keine Rotation. 
        Quaternion refsuedpol =  Quaternion.buildFromAngles(new Degree(0), new Degree(180), new Degree(0));
        Quaternion uprotationsuedpol = FgMath.makeZUpFrameRelative(suedpol);
        showAngles(uprotationsuedpol);
        TestUtils.assertQuaternion( refsuedpol, uprotationsuedpol,"suedpol");

        Quaternion refnordpol =  Quaternion.buildFromAngles(new Degree(0), new Degree(0), new Degree(0));
        Quaternion uprotationnordpol = FgMath.makeZUpFrameRelative(nordpol);
        showAngles(uprotationnordpol);
        TestUtils.assertQuaternion(refnordpol, uprotationnordpol,"nordpol");

        Quaternion refgreenwich =  Quaternion.buildFromAngles(new Degree(0), new Degree( (90-greenwich.getLatitudeDeg().getDegree())), new Degree(0));
        Quaternion uprotationgreenwich = FgMath.makeZUpFrameRelative(greenwich);
        showAngles(uprotationgreenwich);
        TestUtils.assertQuaternion(refgreenwich, uprotationgreenwich,"greenwich");

        Quaternion refnullequator =  Quaternion.buildFromAngles(new Degree(0), new Degree(90), new Degree(0));
        Quaternion uprotationnullequator = FgMath.makeZUpFrameRelative(nullequator);
        showAngles(uprotationnullequator);
        TestUtils.assertQuaternion( refnullequator, uprotationnullequator,"nullequator");


    }

    /**
     * DelayLoadReadFileCallback.readNode matrix for koelner-dom.ac: lon=6.958080,lat=50.941327,elev=58.010000,hdg=92.100000,pitch=30.000000,roll=0.000000
     * 12:58:23.792: -0.442040  0.817918 -0.368254  0.000000
     * 12:58:23.792: -0.765825 -0.130378  0.629693  0.000000
     * 12:58:23.792:  0.467024  0.560368  0.684015  0.000000
     * 12:58:23.792: 3997478.044654 487860.037102 4929479.359064  1.000000
     */
    @Test
    public void testGekippterKoelnerDom() {
        // Scenery Objekte nutzen CCW Heading, darum hier negieren.
        FlightLocation loc = new FlightLocation(new SGGeod(new Degree(6.958080), new Degree(50.941327f), 58.01f).toGeoCoordinate(), new Degree(-92.1f), new Degree(30));
        LocalTransform posrot = loc.toPosRot(new FgCalculations());
        // TODO Rundungsfehler bei position prüfen
        TestUtils.assertVector3(new Vector3(3997478.135514629/*044654f*/, 487860.04748317006, 4929479.28485282), posrot.position,"koelnerdom");
        Matrix4 refmat = new Matrix4(-0.442040, -0.765825, 0.467024, 0,
                0.817918, -0.130378, 0.560368, 0,
                -0.368254, 0.629693, 0.684015, 0,
                0, 0, 0, 0);
        Quaternion refrot = refmat.extractQuaternion();
        TestUtils.assertQuaternion( refrot, posrot.rotation,"koelnerdom");
    }

    @Test
    public void testNorthHeadingReference(){
        EllipsoidCalculations rbcp = new FgCalculations();
        Vector3 v = rbcp.getNorthHeadingReference(nullequator.toGeoCoordinate());
        TestUtils.assertVector3(new Vector3(0,0,1),v);
        v = rbcp.getNorthHeadingReference(new SGGeod(new Degree(20), new Degree(0), 0).toGeoCoordinate());
        TestUtils.assertVector3(new Vector3(0,0,1),v);
        v = rbcp.getNorthHeadingReference(new SGGeod(new Degree(-30), new Degree(0), 0).toGeoCoordinate());
        TestUtils.assertVector3(new Vector3(0,0,1),v);
        v = rbcp.getNorthHeadingReference(new SGGeod(new Degree(180), new Degree(0), 0).toGeoCoordinate());
        TestUtils.assertVector3(new Vector3(0,0,1),v);
        v = rbcp.getNorthHeadingReference(new SGGeod(new Degree(0), new Degree(45), 0).toGeoCoordinate());
        TestUtils.assertVector3(new Vector3(-0.7071068f,0,0.7071068f),v);
        v = rbcp.getNorthHeadingReference(new SGGeod(new Degree(90), new Degree(45), 0).toGeoCoordinate());
        TestUtils.assertVector3(new Vector3(0,-0.7071068f,0.7071068f),v);
    }
    
    private void showAngles(Quaternion q) {
        double[] a = new double[3];
        q.toAngles(a);
        System.out.println("x=" + Degree.buildFromRadians(a[0]));
        System.out.println("y=" + Degree.buildFromRadians(a[1]));
        System.out.println("z=" + Degree.buildFromRadians(a[2]));

    }
}
