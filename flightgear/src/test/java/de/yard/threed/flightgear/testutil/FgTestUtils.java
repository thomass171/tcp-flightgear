package de.yard.threed.flightgear.testutil;


import de.yard.threed.core.buffer.SimpleByteBuffer;
import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.testutil.EngineTestUtils;
import de.yard.threed.flightgear.core.osg.NodeCallback;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.flightgear.core.simgear.scene.model.SGAnimation;
import de.yard.threed.flightgear.ecs.FgAnimationUpdateSystem;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FgTestUtils {

    /**
     * Dummy Bundle bauen
     */
    public static Bundle buildDummyBundleModel777() {
        Bundle my777 = new Bundle("My-777", false, new String[]{"Models/777-200.ac"}, "??", null);
        my777.addResource("Models/777-200.ac", new BundleData(new SimpleByteBuffer(new byte[]{}), true), false);
        return my777;
    }

    public static Bundle buildDummyBundleModelbasic() {
        Bundle fgdatabasicmodel = new Bundle("fgdatabasicmodel", false, new String[]{"AI/Aircraft/737/Models/B737-300.ac", "AI/Aircraft/737/737-AirBerlin.xml"}, "??", null);
        fgdatabasicmodel.addResource("AI/Aircraft/737/Models/B737-300.ac", new BundleData(new SimpleByteBuffer(new byte[]{}), true), false);
        fgdatabasicmodel.addResource("AI/Aircraft/737/737-AirBerlin.xml", new BundleData(new SimpleByteBuffer(new byte[]{}), true), false);
        return fgdatabasicmodel;
    }

    public static void assertSGGeod(String label, SGGeod expected, SGGeod actual) {
        assertEquals((float) expected.getLatitudeDeg().getDegree(), (float) actual.getLatitudeDeg().getDegree(), 0.03, "LatitudeDeg");
        assertEquals((float) expected.getLongitudeDeg().getDegree(), (float) actual.getLongitudeDeg().getDegree(), 0.03, "LongitudeDeg");
    }

    public static SceneNode findAndAssertStgNode(int index) {
        List<NativeSceneNode> scenerynodes = Platform.getInstance().findSceneNodeByName("pagedObjectLOD" + index);
        assertEquals(1, scenerynodes.size());
        SceneNode stgNode = new SceneNode(scenerynodes.get(0));
        EngineTestUtils.assertSceneNodeLevel(stgNode, "pagedObjectLOD" + index, new String[]{"terrain", "STG-group-A"});
        return stgNode;
    }

    public static List<SGAnimation> findAnimationsByObjectName(List<SGAnimation> animationList, String objName) {
        return findAnimationsByObjectNameAndLabelAndId(animationList, objName, null, null);
    }

    public static List<SGAnimation> findAnimationsByObjectNameAndLabelAndId(List<SGAnimation> animationList, String objName, String animationId, String label) {
        List<SGAnimation> result = new ArrayList<>();
        for (SGAnimation animation : animationList) {
            if (animation.isOnObject(objName)) {
                boolean matches = true;
                if (animationId != null) {
                    if (!animation.genId().equals(animationId)) {
                        matches = false;
                    }
                }
                if (label != null) {
                    if (!animation.label.equals(label)) {
                        matches = false;
                    }
                }
                if (matches) {
                    result.add(animation);
                }
            }
        }
        return result;
    }

    public static NodeCallback findNodeCallback(String name) {
        for (NodeCallback nodeCallback : FgAnimationUpdateSystem.nodeCallbacks) {
            if (nodeCallback.getName().equals(name)) {
                return nodeCallback;
            }
        }
        return null;
    }
}
