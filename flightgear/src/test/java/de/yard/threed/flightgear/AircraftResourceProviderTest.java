package de.yard.threed.flightgear;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.testutil.TestBundle;
import de.yard.threed.flightgear.core.flightgear.main.AircraftResourceProvider;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 *
 */
@Slf4j
public class AircraftResourceProviderTest {
    Platform platform = FgTestFactory.initPlatformForTest(true, false);

    String resourceInXml = "Aircraft/777/models/777-200.ac";

    /**
     * Should resolve without ac listed.
     * Also needs a bundle "777" for resolving "Aircraft/777/Models/777-200.ac".
     */
    @Test
    public void testResolve() {
        TestBundle bundleTestResources = (TestBundle) BundleRegistry.getBundle("test-resources");
        BundleResource xmlResource = new BundleResource(bundleTestResources, "Models/777-200.xml");

        FgBundleHelper.addProvider(new AircraftResourceProvider("777"));

        // bundle "777" doesn't exist yet.
        BundleResource found = FgBundleHelper.findPath(resourceInXml, xmlResource);
        assertNull(found);

        // now add bundle. gltf should resolve as 'ac'.
        TestBundle bundle777 = new TestBundle("777", new String[]{}, "");
        bundle777.addAdditionalResource("models/777-200.gltf",bundleTestResources.getResource("models/cube.gltf"));
        bundle777.addAdditionalResource("models/777-200.bin",bundleTestResources.getResource("models/cube.bin"));
        bundle777.complete();
        BundleRegistry.registerBundle(bundle777.name, bundle777);

        found = FgBundleHelper.findPath(resourceInXml, xmlResource);
        assertNotNull(found);
    }
}