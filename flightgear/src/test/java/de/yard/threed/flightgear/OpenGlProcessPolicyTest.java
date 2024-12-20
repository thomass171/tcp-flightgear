package de.yard.threed.flightgear;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.test.testutil.TestUtil;
import de.yard.threed.flightgear.core.simgear.scene.model.ACProcessPolicy;
import de.yard.threed.flightgear.core.simgear.scene.model.OpenGlProcessPolicy;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import org.junit.jupiter.api.Test;


/**
 *
 */
public class OpenGlProcessPolicyTest {
    Platform platform = FgTestFactory.initPlatformForTest();

    @Test
    public void testOpenGl2FG() {

        OpenGlProcessPolicy processPolicy = new OpenGlProcessPolicy(null);

        Vector3 opengl = new Vector3(2, 3, 4);
        Vector3 v = processPolicy.opengl2fg.transform(opengl);
        TestUtil.assertVector3(new Vector3(4, 2, 3), v);
    }

    @Test
    public void testFG2OpenGL() {

        OpenGlProcessPolicy processPolicy = new OpenGlProcessPolicy(null);

        // +x becomes +z
        Vector3 fg = new Vector3(-8, 4, 2);
        Vector3 v = processPolicy.fg2opengl().transform(fg);
        TestUtil.assertVector3(new Vector3(4, 2, -8), v);
    }
}
