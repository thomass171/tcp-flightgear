package de.yard.threed.flightgear;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.XmlException;
import de.yard.threed.core.loader.LoaderAC;
import de.yard.threed.core.loader.LoaderGLTF;
import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.loader.PortableModelDefinition;
import de.yard.threed.core.loader.PortableModelList;
import de.yard.threed.core.loader.StringReader;
import de.yard.threed.core.platform.NativeDocument;
import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.test.testutil.TestUtil;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.testutil.TestHelper;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.flightgear.testutil.ModelAssertions;
import de.yard.threed.traffic.VehicleLoaderResult;
import de.yard.threed.traffic.config.VehicleConfig;
import de.yard.threed.traffic.config.XmlVehicleConfig;
import de.yard.threed.trafficcore.model.Vehicle;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;


/**
 *
 */
@Slf4j
public class FgVehicleLoaderTest {
    static Platform platform = FgTestFactory.initPlatformForTest(true, true);

    @Test
    public void testBluebird() {

        String configXML = "<vehicle name=\"bluebird\" type=\"aircraft\">\n" +
                "            <bundlename>bluebird</bundlename>\n" +
                "            <modelfile>Models/bluebird.xml</modelfile>\n" +
                "            <aircraftdir>bluebird</aircraftdir>\n" +
                "        </vehicle>";
        VehicleConfig config = new XmlVehicleConfig(getNodeFromXML(configXML));
        //Das muesste genau der eine fuer FGData sein. Ja, aber auch fgroot. (Aber nicht sgmaterial, dafuer gibts keinen Provider,oder?).
        assertEquals( 2, FgBundleHelper.getProviderCount(),"provider.size");
        new FgVehicleLoader()/*FgVehicleLauncher*/.loadVehicle(new Vehicle(config.getName()), config, (SceneNode container, VehicleLoaderResult loaderResult/*List<SGAnimation> animationList, SGPropertyNode propertyNode,*/, SceneNode lowresNode) -> {


        });
        for (int i = 0; i < 30; i++) {
            TestHelper.processAsync();
        }
        //Die Texture muss aus FGDATA gelesen werden.
        //7.7.21 TestUtil.assertTrue("yoke.texture",platform.hasTexture("yoke.png"));
        List<NativeSceneNode> yokes = SceneNode.findByName("Yoke");
        //2.10.19:TODO Warum finder der den denn nicht? Wegen openGL?
        //TestUtil.assertEquals("yokes.size", 1, yokes.size());
        //AircraftProvider muss wieder weg sein.
        TestUtil.assertEquals("provider.size", 2, FgBundleHelper.getProviderCount());

    }

    /**
     * 15.11.23: Bundle 'corruptedVehicleModel' is missing currently.
     */
    @Test
    public void testBrokenVehicle() {

        String configXML = "<vehicle name=\"corruptedVehicleModel\" type=\"aircraft\">\n" +
                "            <bundlename>corruptedVehicleModel</bundlename>\n" +
                "            <modelfile>Models/bluebird.xml</modelfile>\n" +
                "            <aircraftdir>corruptedVehicleModel</aircraftdir>\n" +
                "        </vehicle>";
        VehicleConfig config = new XmlVehicleConfig(getNodeFromXML(configXML));
        new FgVehicleLoader()/*FgVehicleLauncher*/.loadVehicle(new Vehicle(config.getName()),config, (SceneNode container, VehicleLoaderResult loaderResult/*List<SGAnimation> animationList, SGPropertyNode propertyNode,*/, SceneNode lowresNode) -> {


        });
        //TODO missing bundle cause RTE. TestHelper.processAsync();
    }

    /**
     * Test must use <aircraftdir>bluebird</aircraftdir> to have aircraftdir be piece 1 in resource path.
     * 15.11.23: Bundle 'corruptedVehicleModel' is missing currently.
     */
    @Test
    @Disabled
    public void testBrokenAircraftdir() {
        String configXML = "<vehicle name=\"corruptedVehicleModel\" type=\"aircraft\">\n" +
                "            <bundlename>corruptedVehicleModel</bundlename>\n" +
                "            <modelfile>Models/bluebird.xml</modelfile>\n" +
                "            <aircraftdir>bluebird</aircraftdir>\n" +
                "        </vehicle>";

        VehicleConfig config = new XmlVehicleConfig(getNodeFromXML(configXML));
        config.getBundlename();
        new FgVehicleLoader()/*FgVehicleLauncher*/.loadVehicle(new Vehicle(config.getName()),config, (SceneNode container, VehicleLoaderResult loaderResult/*List<SGAnimation> animationList, SGPropertyNode propertyNode,*/, SceneNode lowresNode) -> {
        });
        TestHelper.processAsync();
    }

    private NativeNode getNodeFromXML(String configXML) {
        NativeDocument xmlNode = null;
        try {
            xmlNode = platform.parseXml(configXML);
        } catch (XmlException e) {
            fail(e.getMessage());
        }
        return xmlNode;
    }

}