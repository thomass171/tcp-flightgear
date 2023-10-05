package de.yard.threed.toolsfg;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SGMaterialLibWrapperTest {

    static Platform platform = FgTestFactory.initPlatformForTest();


    /**
     *
     */
    @Test
    public void test() throws Exception {
        SGMaterialLibWrapper instance;
        instance = SGMaterialLibWrapper.getInstance();
        assertNotNull(instance);
        instance = SGMaterialLibWrapper.getInstance();
        assertNotNull(instance);
    }
}
