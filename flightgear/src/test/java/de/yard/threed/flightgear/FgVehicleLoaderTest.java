package de.yard.threed.flightgear;

import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.XmlException;
import de.yard.threed.core.platform.NativeDocument;
import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.Texture;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.test.testutil.TestUtil;
import de.yard.threed.engine.testutil.EngineTestUtils;
import de.yard.threed.engine.testutil.TestHelper;
import de.yard.threed.flightgear.core.simgear.scene.model.SGReaderWriterXML;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.traffic.VehicleLauncher;
import de.yard.threed.traffic.VehicleLoaderResult;
import de.yard.threed.traffic.config.VehicleDefinition;
import de.yard.threed.traffic.config.XmlVehicleDefinition;
import de.yard.threed.traffic.testutils.TrafficTestUtils;
import de.yard.threed.trafficcore.model.Vehicle;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


/**
 *
 */
@Slf4j
public class FgVehicleLoaderTest {
    static Platform platform = FgTestFactory.initPlatformForTest(true, true, true);

    @Test
    public void testBluebird() throws Exception {

        String configXML = "<vehicle name=\"bluebird\" type=\"aircraft\">\n" +
                "            <bundlename>bluebird</bundlename>\n" +
                "            <modelfile>Models/bluebird.xml</modelfile>\n" +
                "             <zoffset>1.2</zoffset>\n" +
                "            <aircraftdir>bluebird</aircraftdir>\n" +
                "        </vehicle>";
        VehicleDefinition config = new XmlVehicleDefinition(getNodeFromXML(configXML));
        // 6.3.25 No longer "fgrootcore", so only "fgdatabasic". But not sgmaterial which has no provider.
        assertEquals(1/*2*/, FgBundleHelper.getProvider().size(), "provider.size");

        List<SceneNode> loaded = new ArrayList<>();
        List<FgVehicleLoaderResult> loadedResults = new ArrayList<>();

        SGReaderWriterXML.clearStatistics();
        new FgVehicleLoader().loadVehicle(new Vehicle(config.getName()), config,
                (SceneNode container, VehicleLoaderResult loaderResult, SceneNode lowresNode) -> {
                    TrafficTestUtils.assertVehicleNodeHierarchy(container, 1.2, new Quaternion());
                    loaded.add(container);
                    // animation loading might not been complete. TODO should. delegte called to often?
                    loadedResults.add((FgVehicleLoaderResult) loaderResult);
                    //Wow, 609 animations counted. But only 393 effective. Hmm
                    //TODO fix assertEquals(393, loadedResults.get(0).animationList.size());
                });
        for (int i = 0; i < 30; i++) {
            TestHelper.processAsync();
        }
        SceneNode bluebirdNode = loaded.get(0);
        //log.debug(bluebirdNode.dump("  ",0));

        assertEquals(0, SGReaderWriterXML.errorCnt, "errorCnt ");
        //Wow, 609 animations counted. But only 393 effective. Hmm. now 587 due to garmin?. 20.11.24 now 762->838.2.2.25 ->839 8.9.25->857
        assertEquals(871/*857*/, loadedResults.get(0).animationList.size());

        // should load bluebird, yoke, pedals,display-screens, 6 spheres, 3 of garmin(?), compass+digital clock
        assertEquals(1 + 1 + 1 + 1 + 6 + 3 + 2, SGReaderWriterXML.loadedList.size(), "loadedList ");
        // original ac-file referenced 'yoke.rgb', but was mapped to 'png' in ACLoader.
        assertTrue(Texture.hasTexture("yoke.png"), "yoke.texture");

        // texture was found via texturepath
        assertTrue(Texture.hasTexture("bluebird-1.png"), "bluebird-1.texture");
        // TODO validate used texturepath assertEquals("Models/Textures", );

        List<NativeSceneNode> mainyoke = SceneNode.findByName("YOKE");
        assertEquals(1, mainyoke.size(), "mainyoke.size");
        // The (FG!) coordinates appear correct. Important is a negative x (head of bluebird), y=0 for center and moderate z for height (includes zoffset)
        TestUtils.assertVector3(new Vector3(-7.8899, 0, 2.2499), mainyoke.get(0).getTransform().getWorldModelMatrix().extractPosition());
        // The full XML name is 'Aircraft/bluebird/Instruments-3d/yoke/yoke.xml'. Its stripped?
        List<NativeSceneNode> yokexml = SceneNode.findByName("Instruments-3d/yoke/yoke.xml");
        assertEquals(1, yokexml.size(), "yokexml.size");
        List<NativeSceneNode> yokegltf = SceneNode.findByName("Aircraft/Instruments-3d/yoke/yoke.gltf");
        assertEquals(1, yokegltf.size(), "yoke.gltf.size");
        // 'Yoke' is a 'Spin Animation Group' with two entries
        // 16.8.24: In the past we found it twice. Now only once. The reson is unknown.
        List<NativeSceneNode> yokes = SceneNode.findByName("Yoke");
        //log.debug(bluebirdNode.dump("  ", 0));
        assertEquals(/*2*/1, yokes.size(), "yokes.size");

        List<NativeSceneNode> mainpedals = SceneNode.findByName("PEDALS");
        assertEquals(1, mainpedals.size(), "mainpedals.size");


        //AircraftProvider must have been removed. Only "fgdatabasic" stays. 4.11.25:Provider is removed later now, so wait
        //until everything is done. But that isn't reliable. So, until we have a better solution, remove i there.
        TestUtils.waitUntil(() -> {
            TestHelper.processAsync();
            return AbstractSceneRunner.getInstance().getPendingAsyncCount() == 0;
        }, 10000);
        FgBundleHelper.removeAircraftSpecific();

        assertEquals(1, FgBundleHelper.getProvider().size(), "provider.size");
        assertFalse(FgBundleHelper.getProvider().get(0).isAircraftSpecific(), "provider.isAircraftSpecific");
        assertTrue(FgBundleHelper.getProvider().get(0) instanceof SimpleBundleResourceProvider);
        assertEquals("fgdatabasic", ((SimpleBundleResourceProvider) FgBundleHelper.getProvider().get(0)).bundlename);

        log.debug(bluebirdNode.dump("  ", 0));
        // full hierarchy is too large, so only check some
        String hierarchy = EngineTestUtils.getHierarchy(bluebirdNode, 10, true);
        assertTrue(hierarchy.contains("ACProcessPolicy.root node->ACProcessPolicy.transform node->Models/bluebird.gltf->gltfroot->ac-world->[Layer_Last->[centerBackTranslate"), hierarchy);
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