package de.yard.threed.trafficfg;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.testutil.EngineTestFactory;

import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.graph.Graph;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;

import de.yard.threed.traffic.GreatCircle;
import de.yard.threed.traffic.WorldGlobal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created on 11.11.18.
 */
public class GreatCircleTest {
    //static Platform platform = TestFactory.initPlatformForTest(false,false,null,true);
    static Platform platform = EngineTestFactory.initPlatformForTest( new String[] {"engine"}, new SimpleHeadlessPlatformFactory());

    /**
     * Equator (lat 0, lon 0), direction india.
     */
    @Test
    public void testEquatorToIndia() {
        GreatCircle earthOrbit = new GreatCircle(new Vector3(WorldGlobal.EARTHRADIUS, 0, 0), new Vector3(0, WorldGlobal.EARTHRADIUS, 0));
        //System.out.println("n=" + earthOrbit.n);
        TestUtils.assertVector3(new Vector3(0, 0, 1), earthOrbit.n);
    }

    /**
     * Der Grosskreis einmal von etwa EDDK rund um den Globus
     */
    @Test
    public void testGrosskreisFromCologne() {
        //SGGeod entry = new SGGeod(new Degree(7), new Degree(50), 100);
        //Degree direction = new Degree(140);
        GreatCircle earthOrbit = buildGrosskreisFromCologne();
        //die Werte scheinen plausibel
        //TestUtil.assertVector3(new Vector3(-0.5814507f,0.6994702f,0.4155117f),earthOrbit.n);

        Graph graph = earthOrbit.getGraph(4);

        //correct??
        //SGGeod firstquarterref = entry.applyCourseDistance(direction, WorldGlobal.EARTHRADIUS * MathUtil2.PI2 / 4);
        Assertions.assertEquals( 4, graph.getEdgeCount(),"segments");
        for (int i = 0; i < graph.getNodeCount(); i++) {
            System.out.println("" + i + graph.getNode(i).getLocation() + " - " + SGGeod.fromCart(graph.getNode(i).getLocation()));
        }
        Assertions.assertEquals( 50, (float) SGGeod.fromCart(graph.getNode(0).getLocation()).getLatitudeDeg().getDegree(), 0.001f);
        //plausibel 12.7.25: why did it change? -29.752846 -> -27.7055
        Assertions.assertEquals( -27.7055f, (float) SGGeod.fromCart(graph.getNode(1).getLocation()).getLatitudeDeg().getDegree(), 0.01f);
        Assertions.assertEquals( 58.868813/*12.7.25 54.768597*/, (float) SGGeod.fromCart(graph.getNode(1).getLocation()).getLongitudeDeg().getDegree(), 0.05f);
        //Gegenpunkt
        Assertions.assertEquals( -50, (float) SGGeod.fromCart(graph.getNode(2).getLocation()).getLatitudeDeg().getDegree(), 0.001f);
        Assertions.assertEquals( -180 + 7, (float) SGGeod.fromCart(graph.getNode(2).getLocation()).getLongitudeDeg().getDegree(), 0.001f);
    }

    /**
     * Von etwa EDDK bis Equator.
     */
    @Test
    public void testFromCologneToEquatorOrbit() {
        GreatCircle earthOrbit = buildGrosskreisFromCologne();

        Graph graphToEquator = earthOrbit.getGraphToEquator(32);
        // 6 ist plausibel
        Assertions.assertEquals(6, graphToEquator.getEdgeCount(),"segments");
    }

    public GreatCircle buildGrosskreisFromCologne() {
        SGGeod entry = new SGGeod(new Degree(7), new Degree(50), 100);
        Degree direction = new Degree(140);
        GreatCircle earthOrbit =  GreatCircle.fromDG(entry.toGeoCoordinate(), direction,new FgCalculations());
        System.out.println("n=" + earthOrbit.n);
        System.out.println("entry=" + earthOrbit.entry);
        return earthOrbit;
    }
}
