package de.yard.threed.flightgear;

import de.yard.threed.core.LatLon;
import de.yard.threed.core.Pair;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.flightgear.main.FGGlobals;
import de.yard.threed.flightgear.core.flightgear.main.FGProperties;
import de.yard.threed.flightgear.core.simgear.bucket.SGBucket;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.flightgear.core.simgear.scene.material.Effect;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterial;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterialCache;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterialLib;
import de.yard.threed.flightgear.core.simgear.scene.tgdb.SGOceanTile;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.yard.threed.core.testutil.TestUtils.*;
import static de.yard.threed.flightgear.SGMaterialTest.initSGMaterialLib;
import static org.junit.jupiter.api.Assertions.*;


/**
 * For SGOceanTile
 * <p>
 * Created by thomass on 05.06.25.
 */
public class SGOceanTileTest {

    @BeforeAll
    static void setup() {
        FgTestFactory.initPlatformForTest(false, true, true);
    }

    /**
     *
     */
    @Test
    public void testSimple() {
        SGMaterialLib matlib = initSGMaterialLib();

        // We don't have material "Ocean" here, so tile will be built wireless or just blue.
        LatLon somewhereInNorthSea = LatLon.fromDegrees(54.917587, 4.752215);
        SGBucket bucket = new SGBucket(somewhereInNorthSea.getLonDeg(), somewhereInNorthSea.getLatDeg());
        SGOceanTile sgOceanTile = SGOceanTile.SGOceanTile(bucket, matlib, 5, 5);
        assertNotNull(sgOceanTile);
        assertNotNull(sgOceanTile.node);
    }

    @Test
    public void testFgSample() {
        SGMaterialLib matlib = initSGMaterialLib();

        // We don't have material "Ocean" here, so tile will be built wireless or just blue.
        LatLon somewhere = LatLon.fromDegrees(53.8125, 7.375);
        SGBucket bucket = new SGBucket(somewhere.getLonDeg(), somewhere.getLatDeg());
        SGOceanTile sgOceanTile = SGOceanTile.SGOceanTile(bucket, matlib, 5, 5);
        assertNotNull(sgOceanTile);
        assertNotNull(sgOceanTile.node);
        // Ref values from FG
        assertQuaternion(new Quaternion(0.30993, 0.0199744, 0.948582, -0.0611342), sgOceanTile.oceanMesh.hlOr);

        assertVector3(new Vector3(-6949.05, 8246, -9.10498), sgOceanTile.oceanMesh.rel[0]);
        assertVector3(new Vector3(-6954.49, 4123, -5.1218), sgOceanTile.oceanMesh.rel[1]);
        assertVector3(new Vector3(-6956.31, -7.24185e-11, -3.79407), sgOceanTile.oceanMesh.rel[2]);
        assertVector3(new Vector3(-6954.49, -4123, -5.1218), sgOceanTile.oceanMesh.rel[3]);
        assertVector3(new Vector3(-6949.05, -8246, -9.10498), sgOceanTile.oceanMesh.rel[4]);
        assertVector3(new Vector3(-3470.91, 8239.88, -6.25549), sgOceanTile.oceanMesh.rel[5]);
        assertVector3(new Vector3(-3476.35, 4119.94, -2.27526), sgOceanTile.oceanMesh.rel[6]);
        assertVector3(new Vector3(-3478.16, -4.10409e-11, -0.948521), sgOceanTile.oceanMesh.rel[7]);
        assertVector3(new Vector3(-3476.35, -4119.94, -2.27526), sgOceanTile.oceanMesh.rel[8]);
        assertVector3(new Vector3(-3470.91, -8239.88, -6.25549), sgOceanTile.oceanMesh.rel[9]);
        assertVector3(new Vector3(7.24898, 8233.76, -5.30302), sgOceanTile.oceanMesh.rel[10]);
        assertVector3(new Vector3(1.81225, 4116.88, -1.32576), sgOceanTile.oceanMesh.rel[11]);
        assertVector3(new Vector3(0, 0, 0), sgOceanTile.oceanMesh.rel[12]);
        assertVector3(new Vector3(1.81225, -4116.88, -1.32576), sgOceanTile.oceanMesh.rel[13]);
        assertVector3(new Vector3(7.24898, -8233.76, -5.30302), sgOceanTile.oceanMesh.rel[14]);
        assertVector3(new Vector3(3485.42, 8227.63, -6.24761), sgOceanTile.oceanMesh.rel[15]);
        assertVector3(new Vector3(3479.99, 4113.82, -2.2733), sgOceanTile.oceanMesh.rel[16]);
        assertVector3(new Vector3(3478.18, -2.42721e-11, -0.948528), sgOceanTile.oceanMesh.rel[17]);
        assertVector3(new Vector3(3479.99, -4113.82, -2.2733), sgOceanTile.oceanMesh.rel[18]);
        assertVector3(new Vector3(3485.42, -8227.63, -6.24761), sgOceanTile.oceanMesh.rel[19]);
        assertVector3(new Vector3(6963.62, 8221.5, -9.08926), sgOceanTile.oceanMesh.rel[20]);
        assertVector3(new Vector3(6958.19, 4110.75, -5.11791), sgOceanTile.oceanMesh.rel[21]);
        assertVector3(new Vector3(6956.38, -4.3201e-12, -3.79412), sgOceanTile.oceanMesh.rel[22]);
        assertVector3(new Vector3(6958.19, -4110.75, -5.11791), sgOceanTile.oceanMesh.rel[23]);
        assertVector3(new Vector3(6963.62, -8221.5, -9.08926), sgOceanTile.oceanMesh.rel[24]);

        assertVector3(new Vector3(-0.0042959, 0.00129567, 0.99999), sgOceanTile.oceanMesh.normals[0]);

        assertVector3(new Vector3(-6988.4, 8245.86, -159.275), sgOceanTile.oceanMesh.vl.get(0));
        assertVector3(new Vector3(-6993.85, 4122.96, -155.292), sgOceanTile.oceanMesh.vl.get(1));
        assertVector3(new Vector3(-6995.66, 0.0175524, -153.965), sgOceanTile.oceanMesh.vl.get(2));
        assertVector3(new Vector3(-6993.85, -4122.92, -155.292), sgOceanTile.oceanMesh.vl.get(3));
        assertVector3(new Vector3(-6988.4, -8245.86, -159.275), sgOceanTile.oceanMesh.vl.get(4));
        assertVector3(new Vector3(-6948.33, 8285.81, -159.155), sgOceanTile.oceanMesh.vl.get(5), 0.01);
        assertVector3(new Vector3(-6949.05, 8246, -9.10498), sgOceanTile.oceanMesh.vl.get(6));
        assertVector3(new Vector3(-6954.49, 4123, -5.1218), sgOceanTile.oceanMesh.vl.get(7));
        assertVector3(new Vector3(-6956.31, -7.24185e-11, -3.79407), sgOceanTile.oceanMesh.vl.get(8));
        assertVector3(new Vector3(-6954.49, -4123, -5.1218), sgOceanTile.oceanMesh.vl.get(9));
        assertVector3(new Vector3(-6949.05, -8246, -9.10498), sgOceanTile.oceanMesh.vl.get(10));
        assertVector3(new Vector3(-6948.33, -8285.81, -159.155), sgOceanTile.oceanMesh.vl.get(11), 0.01);
        assertVector3(new Vector3(-3470.28, 8279.68, -156.306), sgOceanTile.oceanMesh.vl.get(12));
        assertVector3(new Vector3(-3470.91, 8239.88, -6.25549), sgOceanTile.oceanMesh.vl.get(13));
        assertVector3(new Vector3(-3476.35, 4119.94, -2.27526), sgOceanTile.oceanMesh.vl.get(14));
        assertVector3(new Vector3(-3478.16, -4.10409e-11, -0.948521), sgOceanTile.oceanMesh.vl.get(15));
        assertVector3(new Vector3(-3476.35, -4119.94, -2.27526), sgOceanTile.oceanMesh.vl.get(16));
        assertVector3(new Vector3(-3470.91, -8239.88, -6.25549), sgOceanTile.oceanMesh.vl.get(17));
        assertVector3(new Vector3(-3470.28, -8279.68, -156.306), sgOceanTile.oceanMesh.vl.get(18));
        assertVector3(new Vector3(7.79985, 8273.56, -155.354), sgOceanTile.oceanMesh.vl.get(19));
        assertVector3(new Vector3(7.24898, 8233.76, -5.30302), sgOceanTile.oceanMesh.vl.get(20));
        assertVector3(new Vector3(1.81225, 4116.88, -1.32576), sgOceanTile.oceanMesh.vl.get(21));
        assertVector3(new Vector3(0, 0, 0), sgOceanTile.oceanMesh.vl.get(22));
        assertVector3(new Vector3(1.81225, -4116.88, -1.32576), sgOceanTile.oceanMesh.vl.get(23));
        assertVector3(new Vector3(7.24898, -8233.76, -5.30302), sgOceanTile.oceanMesh.vl.get(24));
        assertVector3(new Vector3(7.79985, -8273.56, -155.354), sgOceanTile.oceanMesh.vl.get(25));
        assertVector3(new Vector3(3485.89, 8267.44, -156.298), sgOceanTile.oceanMesh.vl.get(26));
        assertVector3(new Vector3(3485.42, 8227.63, -6.24761), sgOceanTile.oceanMesh.vl.get(27));
        assertVector3(new Vector3(3479.99, 4113.82, -2.2733), sgOceanTile.oceanMesh.vl.get(28));
        assertVector3(new Vector3(3478.18, -2.42721e-11, -0.948528), sgOceanTile.oceanMesh.vl.get(29));
        assertVector3(new Vector3(3479.99, -4113.82, -2.2733), sgOceanTile.oceanMesh.vl.get(30));
        assertVector3(new Vector3(3485.42, -8227.63, -6.24761), sgOceanTile.oceanMesh.vl.get(31));
        assertVector3(new Vector3(3485.89, -8267.44, -156.298), sgOceanTile.oceanMesh.vl.get(32));
        assertVector3(new Vector3(6964, 8261.31, -159.14), sgOceanTile.oceanMesh.vl.get(33));
        assertVector3(new Vector3(6963.62, 8221.5, -9.08926), sgOceanTile.oceanMesh.vl.get(34));
        assertVector3(new Vector3(6958.19, 4110.75, -5.11791), sgOceanTile.oceanMesh.vl.get(35));
        assertVector3(new Vector3(6956.38, -4.3201e-12, -3.79412), sgOceanTile.oceanMesh.vl.get(36));
        assertVector3(new Vector3(6958.19, -4110.75, -5.11791), sgOceanTile.oceanMesh.vl.get(37));
        assertVector3(new Vector3(6963.62, -8221.5, -9.08926), sgOceanTile.oceanMesh.vl.get(38));
        assertVector3(new Vector3(6964, -8261.31, -159.14), sgOceanTile.oceanMesh.vl.get(39));
        assertVector3(new Vector3(7003.93, 8221.26, -159.004), sgOceanTile.oceanMesh.vl.get(40));
        assertVector3(new Vector3(6998.51, 4110.6, -155.033), sgOceanTile.oceanMesh.vl.get(41));
        assertVector3(new Vector3(6996.7, -0.0175809, -153.709), sgOceanTile.oceanMesh.vl.get(42));
        assertVector3(new Vector3(6998.51, -4110.64, -155.033), sgOceanTile.oceanMesh.vl.get(43));
        assertVector3(new Vector3(7003.93, -8221.26, -159.004), sgOceanTile.oceanMesh.vl.get(44));

        assertVector3(new Vector3(-0.0042959, 0.00129567, 0.99999), sgOceanTile.oceanMesh.nl.get(0));
        assertVector3(new Vector3(-0.00429675, 0.000647836, 0.999991), sgOceanTile.oceanMesh.nl.get(1));
        assertVector3(new Vector3(-0.00429704, -1.38778e-16, 0.999991), sgOceanTile.oceanMesh.nl.get(2));
        assertVector3(new Vector3(-0.00429675, -0.000647836, 0.999991), sgOceanTile.oceanMesh.nl.get(3));
        assertVector3(new Vector3(-0.0042959, -0.00129567, 0.99999), sgOceanTile.oceanMesh.nl.get(4));
        assertVector3(new Vector3(-0.0042959, 0.00129567, 0.99999), sgOceanTile.oceanMesh.nl.get(5));
        assertVector3(new Vector3(-0.0042959, 0.00129567, 0.99999), sgOceanTile.oceanMesh.nl.get(6));
        assertVector3(new Vector3(-0.00429675, 0.000647836, 0.999991), sgOceanTile.oceanMesh.nl.get(7));
        assertVector3(new Vector3(-0.00429704, -1.38778e-16, 0.999991), sgOceanTile.oceanMesh.nl.get(8));
        assertVector3(new Vector3(-0.00429675, -0.000647836, 0.999991), sgOceanTile.oceanMesh.nl.get(9));
        assertVector3(new Vector3(-0.0042959, -0.00129567, 0.99999), sgOceanTile.oceanMesh.nl.get(10));
        assertVector3(new Vector3(-0.0042959, -0.00129567, 0.99999), sgOceanTile.oceanMesh.nl.get(11));
        assertVector3(new Vector3(-0.00374939, 0.00129471, 0.999992), sgOceanTile.oceanMesh.nl.get(12));
        assertVector3(new Vector3(-0.00374939, 0.00129471, 0.999992), sgOceanTile.oceanMesh.nl.get(13));
        assertVector3(new Vector3(-0.00375025, 0.000647356, 0.999993), sgOceanTile.oceanMesh.nl.get(14));
        assertVector3(new Vector3(-0.00375053, -1.45717e-16, 0.999993), sgOceanTile.oceanMesh.nl.get(15));
        assertVector3(new Vector3(-0.00375025, -0.000647356, 0.999993), sgOceanTile.oceanMesh.nl.get(16));
        assertVector3(new Vector3(-0.00374939, -0.00129471, 0.999992), sgOceanTile.oceanMesh.nl.get(17));
        assertVector3(new Vector3(-0.00374939, -0.00129471, 0.999992), sgOceanTile.oceanMesh.nl.get(18));
        assertVector3(new Vector3(-0.00320288, 0.00129375, 0.999994), sgOceanTile.oceanMesh.nl.get(19));
        assertVector3(new Vector3(-0.00320288, 0.00129375, 0.999994), sgOceanTile.oceanMesh.nl.get(20));
        assertVector3(new Vector3(-0.00320374, 0.000646876, 0.999995), sgOceanTile.oceanMesh.nl.get(21));
        assertVector3(new Vector3(-0.00320402, -1.17961e-16, 0.999995), sgOceanTile.oceanMesh.nl.get(22));
        assertVector3(new Vector3(-0.00320374, -0.000646876, 0.999995), sgOceanTile.oceanMesh.nl.get(23));
        assertVector3(new Vector3(-0.00320288, -0.00129375, 0.999994), sgOceanTile.oceanMesh.nl.get(24));
        assertVector3(new Vector3(-0.00320288, -0.00129375, 0.999994), sgOceanTile.oceanMesh.nl.get(25));
        assertVector3(new Vector3(-0.00265637, 0.00129279, 0.999996), sgOceanTile.oceanMesh.nl.get(26));
        assertVector3(new Vector3(-0.00265637, 0.00129279, 0.999996), sgOceanTile.oceanMesh.nl.get(27));
        assertVector3(new Vector3(-0.00265723, 0.000646396, 0.999996), sgOceanTile.oceanMesh.nl.get(28));
        assertVector3(new Vector3(-0.00265751, -1.45717e-16, 0.999996), sgOceanTile.oceanMesh.nl.get(29));
        assertVector3(new Vector3(-0.00265723, -0.000646396, 0.999996), sgOceanTile.oceanMesh.nl.get(30));
        assertVector3(new Vector3(-0.00265637, -0.00129279, 0.999996), sgOceanTile.oceanMesh.nl.get(31));
        assertVector3(new Vector3(-0.00265637, -0.00129279, 0.999996), sgOceanTile.oceanMesh.nl.get(32));
        assertVector3(new Vector3(-0.00210985, 0.00129183, 0.999997), sgOceanTile.oceanMesh.nl.get(33));
        assertVector3(new Vector3(-0.00210985, 0.00129183, 0.999997), sgOceanTile.oceanMesh.nl.get(34));
        assertVector3(new Vector3(-0.00211071, 0.000645916, 0.999998), sgOceanTile.oceanMesh.nl.get(35));
        assertVector3(new Vector3(-0.00211099, -1.31839e-16, 0.999998), sgOceanTile.oceanMesh.nl.get(36));
        assertVector3(new Vector3(-0.00211071, -0.000645916, 0.999998), sgOceanTile.oceanMesh.nl.get(37));
        assertVector3(new Vector3(-0.00210985, -0.00129183, 0.999997), sgOceanTile.oceanMesh.nl.get(38));
        assertVector3(new Vector3(-0.00210985, -0.00129183, 0.999997), sgOceanTile.oceanMesh.nl.get(39));
        assertVector3(new Vector3(-0.00210985, 0.00129183, 0.999997), sgOceanTile.oceanMesh.nl.get(40));
        assertVector3(new Vector3(-0.00211071, 0.000645916, 0.999998), sgOceanTile.oceanMesh.nl.get(41));
        assertVector3(new Vector3(-0.00211099, -1.31839e-16, 0.999998), sgOceanTile.oceanMesh.nl.get(42));
        assertVector3(new Vector3(-0.00211071, -0.000645916, 0.999998), sgOceanTile.oceanMesh.nl.get(43));
        assertVector3(new Vector3(-0.00210985, -0.00129183, 0.999997), sgOceanTile.oceanMesh.nl.get(44));

        assertVector2(new Vector2(0.153809, 0.171466), sgOceanTile.oceanMesh.tl.get(0));
        assertVector2(new Vector2(10.5, 0.171466), sgOceanTile.oceanMesh.tl.get(1));
        assertVector2(new Vector2(20.8461, 0.171466), sgOceanTile.oceanMesh.tl.get(2));
        assertVector2(new Vector2(31.1923, 0.171466), sgOceanTile.oceanMesh.tl.get(3));
        assertVector2(new Vector2(41.5385, 0.171466), sgOceanTile.oceanMesh.tl.get(4));
        assertVector2(new Vector2(-0.234296, 0.55957), sgOceanTile.oceanMesh.tl.get(5));
        assertVector2(new Vector2(0.153809, 0.55957), sgOceanTile.oceanMesh.tl.get(6));
        assertVector2(new Vector2(10.5, 0.55957), sgOceanTile.oceanMesh.tl.get(7));
        assertVector2(new Vector2(20.8461, 0.55957), sgOceanTile.oceanMesh.tl.get(8));
        assertVector2(new Vector2(31.1923, 0.55957), sgOceanTile.oceanMesh.tl.get(9));
        assertVector2(new Vector2(41.5385, 0.55957), sgOceanTile.oceanMesh.tl.get(10));
        assertVector2(new Vector2(41.9266, 0.55957), sgOceanTile.oceanMesh.tl.get(11));
        assertVector2(new Vector2(-0.234296, 9.25586), sgOceanTile.oceanMesh.tl.get(12));
        assertVector2(new Vector2(0.153809, 9.25586), sgOceanTile.oceanMesh.tl.get(13));
        assertVector2(new Vector2(10.5, 9.25586), sgOceanTile.oceanMesh.tl.get(14));
        assertVector2(new Vector2(20.8461, 9.25586), sgOceanTile.oceanMesh.tl.get(15));
        assertVector2(new Vector2(31.1923, 9.25586), sgOceanTile.oceanMesh.tl.get(16));
        assertVector2(new Vector2(41.5385, 9.25586), sgOceanTile.oceanMesh.tl.get(17));
        assertVector2(new Vector2(41.9266, 9.25586), sgOceanTile.oceanMesh.tl.get(18));
        assertVector2(new Vector2(-0.234296, 17.9531), sgOceanTile.oceanMesh.tl.get(19));
        assertVector2(new Vector2(0.153809, 17.9531), sgOceanTile.oceanMesh.tl.get(20));
        assertVector2(new Vector2(10.5, 17.9531), sgOceanTile.oceanMesh.tl.get(21));
        assertVector2(new Vector2(20.8461, 17.9531), sgOceanTile.oceanMesh.tl.get(22));
        assertVector2(new Vector2(31.1923, 17.9531), sgOceanTile.oceanMesh.tl.get(23));
        assertVector2(new Vector2(41.5385, 17.9531), sgOceanTile.oceanMesh.tl.get(24));
        assertVector2(new Vector2(41.9266, 17.9531), sgOceanTile.oceanMesh.tl.get(25));
        assertVector2(new Vector2(-0.234296, 26.6494), sgOceanTile.oceanMesh.tl.get(26));
        assertVector2(new Vector2(0.153809, 26.6494), sgOceanTile.oceanMesh.tl.get(27));
        assertVector2(new Vector2(10.5, 26.6494), sgOceanTile.oceanMesh.tl.get(28));
        assertVector2(new Vector2(20.8461, 26.6494), sgOceanTile.oceanMesh.tl.get(29));
        assertVector2(new Vector2(31.1923, 26.6494), sgOceanTile.oceanMesh.tl.get(30));
        assertVector2(new Vector2(41.5385, 26.6494), sgOceanTile.oceanMesh.tl.get(31));
        assertVector2(new Vector2(41.9266, 26.6494), sgOceanTile.oceanMesh.tl.get(32));
        assertVector2(new Vector2(-0.234296, 35.3467), sgOceanTile.oceanMesh.tl.get(33));
        assertVector2(new Vector2(0.153809, 35.3467), sgOceanTile.oceanMesh.tl.get(34));
        assertVector2(new Vector2(10.5, 35.3467), sgOceanTile.oceanMesh.tl.get(35));
        assertVector2(new Vector2(20.8461, 35.3467), sgOceanTile.oceanMesh.tl.get(36));
        assertVector2(new Vector2(31.1923, 35.3467), sgOceanTile.oceanMesh.tl.get(37));
        assertVector2(new Vector2(41.5385, 35.3467), sgOceanTile.oceanMesh.tl.get(38));
        assertVector2(new Vector2(41.9266, 35.3467), sgOceanTile.oceanMesh.tl.get(39));
        assertVector2(new Vector2(0.153809, 35.7348), sgOceanTile.oceanMesh.tl.get(40));
        assertVector2(new Vector2(10.5, 35.7348), sgOceanTile.oceanMesh.tl.get(41));
        assertVector2(new Vector2(20.8461, 35.7348), sgOceanTile.oceanMesh.tl.get(42));
        assertVector2(new Vector2(31.1923, 35.7348), sgOceanTile.oceanMesh.tl.get(43));
        assertVector2(new Vector2(41.5385, 35.7348), sgOceanTile.oceanMesh.tl.get(44));

    }
}
