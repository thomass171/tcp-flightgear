package de.yard.threed.flightgear;

import de.yard.threed.core.CharsetException;
import de.yard.threed.core.Color;
import de.yard.threed.core.buffer.SimpleByteBuffer;
import de.yard.threed.core.platform.NativeJsonValue;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.core.testutil.InMemoryBundle;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Scene;
import de.yard.threed.flightgear.core.EffectFactory;
import de.yard.threed.flightgear.core.SGLoaderOptions;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.scene.material.Effect;
import de.yard.threed.flightgear.core.simgear.scene.material.MakeEffect;
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
import de.yard.threed.flightgear.core.simgear.scene.util.SGReaderWriterOptions;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.javanative.FileReader;
import de.yard.threed.tools.GltfBuilderResult;
import de.yard.threed.tools.GltfProcessor;
import lombok.extern.slf4j.Slf4j;
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
 * Also for SGAnimation and thus also for SGReaderWriterXML.
 * Siehe auch ModelBuildTest.
 * 19.8.24: parts of "windturbine" tests moved to SGReaderWriterXMLTest because it seems a better location because its no real "effect" in terms of FG. Maybe
 * all animation tests should move? Hmm, MaterialAnimations might be effects again. And only here materialLib is available!
 * Also for MakeEffect.
 * Effect tests are also in VehicleEffectTest
 * <p>
 * Created by thomass on 27.10.15.
 */
@Slf4j
public class EffectTest {
    Platform platform = FgTestFactory.initPlatformForTest(true, true, true);
    Log logger = Platform.getInstance().getLog(EffectTest.class);

    @Test
    //29.9.15: DEer Test geht nicht, weil hier keine AssetManager existiert.  16.10.18: Jetzt koennte er wieder.
    public void testLoad() throws Exception {
        //InputStream ins = Platform.getInstance().getRessourceManager().loadResourceSync(new BundleResource("src/main/webapp/flusi/model-combined.eff")).inputStream;
        //byte[] bytebuf = ins.readFully();
        BundleResource br = new BundleResource(BundleRegistry.getBundle("test-resources"), "effects/model-combined.eff");

        EffectFactory.buildEffect(br.bundle.getResource(br).getContentAsString());

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
        assertNotNull(gltf, "parsedgltf");
        InMemoryBundle bundle = new InMemoryBundle("Models/CDU-777-boeing", lf.gltfstring, lf.bin);

        bundle.addAdditionalResource("Models/CDU-777-boeing.xml", new BundleData(new SimpleByteBuffer(FileReader.readFully(new FileInputStream(new File(TestUtils.locatedTestFile(xmlfile))))), true));

        // "*.ac" is not really content, but 'gltf' is OK
        assertTrue(bundle.exists(new BundleResource("Models/CDU-777-boeing.ac")));

        BuildResult result = SGReaderWriterXML.buildModelFromBundleXML(new BundleResource(bundle, "Models/CDU-777-boeing.xml"), opt, (bpath, destinationNode, alist) -> {
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

    @Test
    public void testEffectModelTransparent() throws CharsetException {
        String name = "Effects/model-transparent";

        assertEquals(SceneryTest.INITIAL_EFFECTS, MakeEffect.effectMap.size());

        // 18.12.24 test effect with existing material
        Material material = Material.buildBasicMaterial(Color.BLUE);
        EffectMaterialWrapper wrapper = new EffectMaterialWrapper(material);
        int oldCounter=EffectMaterialWrapper.counter;
        Effect effect = MakeEffect.makeEffect(name, true, new SGReaderWriterOptions(), "test", false,wrapper ,null);
        assertNotNull(effect);
        assertEquals(SceneryTest.INITIAL_EFFECTS + 1, MakeEffect.effectMap.size());
        assertEquals(1, EffectMaterialWrapper.counter-oldCounter);
    }

    @Test
    public void testRotateAnimationSpinAngle() {
        // Not sure how correct the ref values are. TODO pick realistic from FG by checking windturbine.
        SGRotateAnimation.ReferenceValues refval = new SGRotateAnimation.ReferenceValues(0, 0);
        assertEquals(0.0, refval.angle.getDegree(), 0.0001);
        refval = SGRotateAnimation.calcSpinAngle(refval, 20.0, 0.1);
        assertEquals(1.9099, refval.angle.getDegree(), 0.001);
        refval = SGRotateAnimation.calcSpinAngle(refval, 20.0, 0.1);
        assertEquals(2*1.9099, refval.angle.getDegree(), 0.001);
        refval = SGRotateAnimation.calcSpinAngle(refval, 20.0, 0.1);
        assertEquals(3*1.9099, refval.angle.getDegree(), 0.001);
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
