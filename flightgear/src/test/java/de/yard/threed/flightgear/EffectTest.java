package de.yard.threed.flightgear;

import de.yard.threed.core.buffer.SimpleByteBuffer;
import de.yard.threed.core.platform.NativeJsonValue;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.resource.URL;
import de.yard.threed.core.testutil.InMemoryBundle;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.flightgear.core.EffectFactory;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.SGLoaderOptions;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.scene.model.SGAnimation;
import de.yard.threed.core.BuildResult;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.flightgear.core.simgear.scene.model.SGReaderWriterXML;
import de.yard.threed.flightgear.core.simgear.scene.model.SGRotateAnimation;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeSceneNode;

import de.yard.threed.engine.testutil.TestHelper;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.javanative.FileReader;
import de.yard.threed.tools.GltfBuilderResult;
import de.yard.threed.tools.GltfProcessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Auch fuer SGAnimation. Und damit allgemein auch fuer SGReaderWriterXML.
 * Siehe auch ModelBuildTest.
 * <p>
 * Created by thomass on 27.10.15.
 */
public class EffectTest {
    Platform platform = FgTestFactory.initPlatformForTest(true, true);
    Log logger = Platform.getInstance().getLog(EffectTest.class);

    @Test
    //29.9.15: DEer Test geht nicht, weil hier keine AssetManager existiert.  16.10.18: Jetzt koennte er wieder.
    public void testLoad() {
        try {
            //InputStream ins = Platform.getInstance().getRessourceManager().loadResourceSync(new BundleResource("src/main/webapp/flusi/model-combined.eff")).inputStream;
            //byte[] bytebuf = ins.readFully();
            BundleResource br = new BundleResource(BundleRegistry.getBundle("test-resources"), "effects/model-combined.eff");

            EffectFactory.buildEffect(br.bundle.getResource(br).getContentAsString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        /*List<Way> finkenweg =*/
    }

    /**
     * async ein Model laden und pruefen, dass z.B. Animationen drin sind
     */
    @Test
    public void testAnimationsWithAsync() throws Exception {
        //PlatformHomeBrew op = (PlatformHomeBrew) platform;
        //evtl. Reste wegraeumen
        TestHelper.cleanupAsync();
        //als Gegenprobe sync
        //PlatformOpenGL.asyncmode=0;
        List<SGAnimation> animationList = new ArrayList<SGAnimation>();
        SGLoaderOptions opt = new SGLoaderOptions();
        opt.setPropertyNode(new SGPropertyNode("" + "-root")/*FGGlobals.getInstance().get_props()*/);

        EngineTestFactory.loadBundleSync(FlightGear.getBucketBundleName("model"));

        Bundle bundlemodel = BundleRegistry.getBundle("Terrasync-model");
        assertNotNull(bundlemodel);

        BuildResult result = SGReaderWriterXMLTest.loadModelAndWait(new BundleResource(bundlemodel, "Models/Power/windturbine.xml"),animationList,
                2, "Models/Power/windturbine.xml");
        validateWindturbine(new SceneNode(result.getNode()), animationList);

        animationList.clear();
        result = SGReaderWriterXML.buildModelFromBundleXML(new BundleResource(bundlemodel, "Models/Power/windturbine.xml"), null, (bpath, destinationNode, alist) -> {
            if (alist != null) {
                animationList.addAll(alist);//  xmlloaddelegate.modelComplete( animationList);
            }
        });
        assertEquals(0, animationList.size(), "animations");
        TestHelper.processAsync();
        TestHelper.processAsync();
        validateWindturbine(new SceneNode(result.getNode()), animationList);
    }

    @Test
    @Disabled
    public void testAnimationsBundleLess() {
        TestHelper.cleanupAsync();
        List<SGAnimation> animationList = new ArrayList<SGAnimation>();
        SGLoaderOptions opt = new SGLoaderOptions();
        opt.setPropertyNode(new SGPropertyNode("" + "-root")/*FGGlobals.getInstance().get_props()*/);

        EngineTestFactory.loadBundleSync(FlightGear.getBucketBundleName("model"));

        Bundle bundlemodel = BundleRegistry.getBundle("Terrasync-model");
        assertNotNull(bundlemodel);

        BuildResult result = SGReaderWriterXMLTest.loadModelBundleLess(new URL("", new ResourcePath("Models/Power"),"windturbine.xml"),animationList);
        validateWindturbine(new SceneNode(result.getNode()),animationList);
    }

    private void validateWindturbine(SceneNode node, List<SGAnimation> animationList){
        assertEquals(2, animationList.size(), "animations");
        assertNotNull(((SGRotateAnimation) animationList.get(0)).rotategroup, "rotationgroup");
        assertNotNull(((SGRotateAnimation) animationList.get(1)).rotategroup, "rotationgroup");
        assertEquals("Models/Power/windturbine.xml->ACProcessPolicy.root node->ACProcessPolicy.transform node->Models/Power/windturbine.gltf->[Tower,center back translate]", TestHelper.getHierarchy(node, 4));

    }

    /**
     *
     */
    @Test
    public void testMaterialAnimation() throws IOException {
        Platform op = platform;
        //evtl. Reste wegraeumen
        TestHelper.cleanupAsync();
        //PlatformOpenGL.asyncmode=1;
        List<SGAnimation> animationList = new ArrayList<SGAnimation>();
        SGLoaderOptions opt = new SGLoaderOptions();
        opt.setPropertyNode(new SGPropertyNode("" + "-root")/*FGGlobals.getInstance().get_props()*/);

        // needs in memory bundle due to ac->gltf
        String acfile = "flightgear/src/test/resources/models/CDU-777-boeing.ac";
        String xmlfile = "flightgear/src/test/resources/models/CDU-777-boeing.xml";
        GltfBuilderResult lf = new GltfProcessor().convertToGltf(TestUtils.locatedTestFile(acfile), Optional.empty());

        NativeJsonValue gltf = platform.parseJson(lf.gltfstring);
        Assertions.assertNotNull(gltf, "parsedgltf");
        InMemoryBundle bundle = new InMemoryBundle("models/CDU-777-boeing", lf.gltfstring, lf.bin);

        bundle.addAdditionalResource("models/CDU-777-boeing.xml", new BundleData(new SimpleByteBuffer(FileReader.readFully(new FileInputStream(new File(TestUtils.locatedTestFile(xmlfile))))), true));

        // "*.ac" is not really content, but 'gltf' is OK
        assertTrue(bundle.exists(new BundleResource("models/CDU-777-boeing.ac")));

        BuildResult result = SGReaderWriterXML.buildModelFromBundleXML(new BundleResource(bundle, "models/CDU-777-boeing.xml"), opt, (bpath, destinationNode, alist) -> {
            if (alist != null) {
                animationList.addAll(alist);//  xmlloaddelegate.modelComplete( animationList);
            }
        });
        //MT gibt es im Test nicht. Darum gibt es keine Animation.
        assertEquals(0, animationList.size(), "animations");
        TestHelper.processAsync();
        TestHelper.processAsync();
        //TestUtil.assertNotNull("rotationgroup",((SGRotateAnimation)result.animationList.get(0)).rotategroup);
        //TestUtil.assertNotNull("rotationgroup",((SGRotateAnimation)result.animationList.get(1)).rotategroup);
        SceneNode node = new SceneNode(result.getNode());
        Scene.getCurrent().addToWorld(node);
        //logger.debug(node.dump("  ",1));
        logger.debug(Scene.getCurrent().getWorld().dump("  ", 1));
        List<NativeSceneNode> lettering = op.findSceneNodeByName("Lettering_Cdu");
        assertEquals(1, lettering.size(), "Lettering_Cdu");
        NativeSceneNode mag = op.findSceneNodeByName("material animation group").get(0);
        // eigentlich muessen es laut XML 5 sein, Lettering_Btns gibt es aber nicht im AC. Duerfte ein Fehler in der XML sein.
        // 15.11.23: Value seems to vary. Sometimes its only 1 TODO check
        int ccnt = mag.getTransform().getChildren().size();
        assertTrue(ccnt == 1 || ccnt == 4, "material animation group.children " + ccnt);

    }

    /**
     * Sind nach async read die texturen per texture-path vorhanden.
     * 15.11.23: Needs other model.
     */
    @Test
    @Disabled
    public void testTexturePath() {
        Platform op = platform;
        //evtl. Reste wegraeumen
        TestHelper.cleanupAsync();
        //PlatformOpenGL.asyncmode=1;
        List<SGAnimation> animationList = new ArrayList<SGAnimation>();
        SGLoaderOptions opt = new SGLoaderOptions();
        opt.setPropertyNode(new SGPropertyNode("" + "-root")/*FGGlobals.getInstance().get_props()*/);
        BuildResult result = SGReaderWriterXML.buildModelFromBundleXML(new BundleResource(BundleRegistry.getBundle("fgdatabasicmodel"), "AI/Aircraft/747/744-KLM.xml"), opt, (bpath, destinationNode, alist) -> {
            if (alist != null) {
                animationList.addAll(alist);//  xmlloaddelegate.modelComplete( animationList);
            }
        });

        //MT gibt es im Test nicht. Darum gibt es keine Animation.
        assertEquals(0, animationList.size(), "animations");
        TestHelper.processAsync();
        //es muesste eine gear animation geben. 3.4.18: Ja? Wirklich?
        //TestUtil.assertEquals("animations",1,result.animationList.size());

        SceneNode node = new SceneNode(result.getNode());
        Scene.getCurrent().addToWorld(node);
        //logger.debug(node.dump("  ",1));
        logger.debug(Scene.getCurrent().getWorld().dump("  ", 1));
        //die letzte Textur muss eigentlich die der 747 sein
        //7.7.21 Use texturetool?
        //TestUtil.assertTrue("KLM Textur geladen", op.texturelist.size() > 0);
        //String texkey = op.texturelist.get(op.texturelist.size() - 1);
        //TestUtil.assertTrue("KLM im Pfad", texkey.contains("Textures/KLM"));

    }

    public static List<SGAnimation> getAnimationsOnObject(String objname, List<SGAnimation> animationList) {
        List<SGAnimation> l = new ArrayList<SGAnimation>();
        for (SGAnimation a : animationList) {
            if (a.isOnObject(objname)) {
                l.add(a);
            }
        }
        return l;
    }
}
