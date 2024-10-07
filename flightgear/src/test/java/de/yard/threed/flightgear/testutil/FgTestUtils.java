package de.yard.threed.flightgear.testutil;


import de.yard.threed.core.buffer.SimpleByteBuffer;
import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.testutil.EngineTestUtils;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FgTestUtils {

    /**
     * Dummy Bundle bauen
     */
    public static Bundle buildDummyBundleModel777() {
        Bundle my777 = new Bundle("My-777", false, new String[]{"Models/777-200.ac"}, "??");
        my777.addResource("Models/777-200.ac", new BundleData(new SimpleByteBuffer(new byte[]{}), true));
        return my777;
    }

    public static Bundle buildDummyBundleModelbasic() {
        Bundle fgdatabasicmodel = new Bundle("fgdatabasicmodel", false, new String[]{"AI/Aircraft/737/Models/B737-300.ac", "AI/Aircraft/737/737-AirBerlin.xml"}, "??");
        fgdatabasicmodel.addResource("AI/Aircraft/737/Models/B737-300.ac", new BundleData(new SimpleByteBuffer(new byte[]{}), true));
        fgdatabasicmodel.addResource("AI/Aircraft/737/737-AirBerlin.xml", new BundleData(new SimpleByteBuffer(new byte[]{}), true));
        return fgdatabasicmodel;
    }

    public static void assertSGGeod(String label, SGGeod expected, SGGeod actual) {
        assertEquals((float) expected.getLatitudeDeg().getDegree(), (float) actual.getLatitudeDeg().getDegree(), 0.03, "LatitudeDeg");
        assertEquals((float) expected.getLongitudeDeg().getDegree(), (float) actual.getLongitudeDeg().getDegree(), 0.03, "LongitudeDeg");
    }

    public static SceneNode findAndAssertStgNode(int index) {
        List<NativeSceneNode> scenerynodes = Platform.getInstance().findSceneNodeByName("pagedObjectLOD" + index);
        assertEquals(1, scenerynodes.size());
        SceneNode stgNode =  new SceneNode(scenerynodes.get(0));
        EngineTestUtils.assertSceneNodeLevel(stgNode,"pagedObjectLOD" + index, new String[]{"terrain","STG-group-A"});
        return stgNode;
    }
}
