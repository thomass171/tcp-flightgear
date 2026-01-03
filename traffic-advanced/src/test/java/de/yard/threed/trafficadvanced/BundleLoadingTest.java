package de.yard.threed.trafficadvanced;

import de.yard.threed.core.platform.NativeResourceLoader;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.platform.PlatformBundleLoader;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.testutil.TestHelper;
import de.yard.threed.flightgear.TerraSyncBundleResolver;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static de.yard.threed.flightgear.TerraSyncBundleResolver.TERRAYSYNCPREFIX;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test external bundle loading. External dependency!
 * <p>
 * <p>
 * Created by thomass on 07.02.2024
 */
@Slf4j
public class BundleLoadingTest {

    @BeforeAll
    static void setup() {
        // No FgTestFactory needed
        Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine"}, new SimpleHeadlessPlatformFactory());
    }

    @Test
    public void testExternalSgMaterial() throws Exception {

        PlatformBundleLoader bundleLoader = new PlatformBundleLoader();

        String bundleName = "sgmaterial";
        List<Bundle> loadedBundle = new ArrayList();
        String baseUrl = "https://ubuntu-server.udehlavj1efjeuqv.myfritz.net/publicweb/bundlepool";
        NativeResourceLoader resourceLoader = Platform.getInstance().buildResourceLoader(bundleName, baseUrl);

        bundleLoader.loadBundle(bundleName, false, bundle -> {
            log.debug("got it");
            loadedBundle.add(bundle);
        }, resourceLoader);

        TestUtils.waitUntil(() -> {
            TestHelper.processAsync();
            return loadedBundle.size() > 0;
        }, 30000);

        assertEquals(1, loadedBundle.size());
        // 806 seems correct, it corresponds to the number of lines in directory.txt (sgmaterial subset only contains appc 91 files)
        assertEquals(861/*860*//*806*/, loadedBundle.get(0).getSize());
    }

    /**
     * 942018 is quite small.
     */
    @Test
    public void testExternalTerraSync() throws Exception {

        String bundleName = TERRAYSYNCPREFIX + "942018";
        Platform platform = Platform.getInstance();

        NativeResourceLoader resourceLoader = platform.buildResourceLoader(bundleName, null);
        assertNull(resourceLoader);

        String baseUrl = "https://ubuntu-server.udehlavj1efjeuqv.myfritz.net/publicweb/bundlepool";
        platform.addBundleResolver(new TerraSyncBundleResolver(baseUrl));
        resourceLoader = platform.buildResourceLoader(bundleName, null);
        assertNotNull(resourceLoader);

        PlatformBundleLoader bundleLoader = new PlatformBundleLoader();

        List<Bundle> loadedBundle = new ArrayList();

        bundleLoader.loadBundle(bundleName, false, bundle -> {
            log.debug("got it");
            loadedBundle.add(bundle);
        }, resourceLoader);

        TestUtils.waitUntil(() -> {
            TestHelper.processAsync();
            return loadedBundle.size() > 0;
        }, 30000);

        // 1.1.26 why 6 instead of 5?
        assertEquals(6, loadedBundle.get(0).getSize());
    }
}

