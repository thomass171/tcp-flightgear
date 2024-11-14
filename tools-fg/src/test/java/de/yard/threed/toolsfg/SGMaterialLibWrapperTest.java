package de.yard.threed.toolsfg;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SGMaterialLibWrapperTest {

    static Platform platform = FgTestFactory.initPlatformForTest();


    /**
     *
     */
    @Test
    public void test() throws Exception {
        BundleRegistry.unregister("fgdatabasic");
        SGMaterialLibWrapper.dropInstance();

        assertNull(BundleRegistry.getBundle("fgdatabasic"));
        SGMaterialLibWrapper instance;
        instance = SGMaterialLibWrapper.getInstance();
        assertNotNull(instance);
        instance = SGMaterialLibWrapper.getInstance();
        assertNotNull(instance);
        assertNotNull(BundleRegistry.getBundle("fgdatabasic"));
    }
}
