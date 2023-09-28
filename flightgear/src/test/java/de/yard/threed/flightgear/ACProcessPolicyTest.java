package de.yard.threed.flightgear;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.test.testutil.TestUtil;
import de.yard.threed.flightgear.core.simgear.scene.model.ACProcessPolicy;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import org.junit.jupiter.api.Test;


/**
 *
 */
public class ACProcessPolicyTest {
    static Platform platform = FgTestFactory.initPlatformForTest();

    @Test
    public void testAC2FG() {

        ACProcessPolicy processPolicy = new ACProcessPolicy(null);

        Vector3 ac = new Vector3(2, 3, 4);
        Vector3 v = processPolicy.ac2fg.transform(ac);
        // y and z change. Negation is unclear
        TestUtil.assertVector3(new Vector3(2, -4, 3), v);
    }
}
