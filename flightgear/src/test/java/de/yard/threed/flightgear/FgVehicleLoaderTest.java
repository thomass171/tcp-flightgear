package de.yard.threed.flightgear;

import de.yard.threed.core.XmlException;
import de.yard.threed.core.platform.NativeDocument;
import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.Texture;
import de.yard.threed.engine.test.testutil.TestUtil;
import de.yard.threed.engine.testutil.TestHelper;
import de.yard.threed.flightgear.core.simgear.scene.model.SGReaderWriterXML;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.traffic.VehicleLoaderResult;
import de.yard.threed.traffic.config.VehicleDefinition;
import de.yard.threed.traffic.config.XmlVehicleDefinition;
import de.yard.threed.trafficcore.model.Vehicle;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


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
        VehicleDefinition config = new XmlVehicleDefinition(getNodeFromXML(configXML));
        //"fgdatabasic" and "fgrootcore"(but not sgmaterial, dafuer gibts keinen Provider,oder?).
        assertEquals(2, FgBundleHelper.getProvider().size(), "provider.size");

        SGReaderWriterXML.clearStatistics();
        new FgVehicleLoader().loadVehicle(new Vehicle(config.getName()), config, (SceneNode container, VehicleLoaderResult loaderResult, SceneNode lowresNode) -> {


        });
        for (int i = 0; i < 30; i++) {
            TestHelper.processAsync();
        }

        assertEquals(0, SGReaderWriterXML.errorCnt, "errorCnt ");
        // should load bluebird, yoke, pedals,display-screens, 6 spheres
        assertEquals(1 + 1 + 1 + 1 + 6, SGReaderWriterXML.loadedList.size(), "loadedList ");
        // original ac-file referenced 'yoke.rgb', but was mapped to 'png' in ACLoader.
        assertTrue(Texture.hasTexture("yoke.png"), "yoke.texture");
        List<NativeSceneNode> yokes = SceneNode.findByName("Yoke");
        // why 2?
        assertEquals(2, yokes.size(), "yokes.size");

        // TODO pedals

        //AircraftProvider must have been removed. Only "fgdatabasic" and "fgrootcore" stay.
        assertEquals(2, FgBundleHelper.getProvider().size(), "provider.size");
        assertFalse(FgBundleHelper.getProvider().get(0).isAircraftSpecific(), "provider.isAircraftSpecific");
        assertTrue(FgBundleHelper.getProvider().get(0) instanceof SimpleBundleResourceProvider);
        assertEquals("fgdatabasic", ((SimpleBundleResourceProvider) FgBundleHelper.getProvider().get(0)).bundlename);
        assertFalse(FgBundleHelper.getProvider().get(1).isAircraftSpecific(), "provider.isAircraftSpecific");
        assertTrue(FgBundleHelper.getProvider().get(1) instanceof SimpleBundleResourceProvider);
        assertEquals("fgrootcore", ((SimpleBundleResourceProvider) FgBundleHelper.getProvider().get(1)).bundlename);

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
        VehicleDefinition config = new XmlVehicleDefinition(getNodeFromXML(configXML));
        new FgVehicleLoader()/*FgVehicleLauncher*/.loadVehicle(new Vehicle(config.getName()), config, (SceneNode container, VehicleLoaderResult loaderResult/*List<SGAnimation> animationList, SGPropertyNode propertyNode,*/, SceneNode lowresNode) -> {


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

        VehicleDefinition config = new XmlVehicleDefinition(getNodeFromXML(configXML));
        config.getBundlename();
        new FgVehicleLoader()/*FgVehicleLauncher*/.loadVehicle(new Vehicle(config.getName()), config, (SceneNode container, VehicleLoaderResult loaderResult/*List<SGAnimation> animationList, SGPropertyNode propertyNode,*/, SceneNode lowresNode) -> {
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