package de.yard.threed.toolsfg.testutil;

import de.yard.threed.core.buffer.ByteArrayInputStream;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.flightgear.LoaderBTG;
import de.yard.threed.flightgear.LoaderOptions;
import de.yard.threed.flightgear.core.FlightGear;

/**
 * Splitted from same class in fg.
 */
public class BtgModelAssertions {

    /**
     * 10.3.21: Nur mal hier geparkt.
     * @param boptions
     * @return
     */
    public static LoaderBTG loadRefBtg(LoaderOptions boptions) {
        try {
            BundleResource br = new BundleResource(BundleRegistry.getBundle("test-resources"), FlightGear.refbtg);
            BundleData ins = br.bundle.getResource(new BundleResource(FlightGear.refbtg));
            LoaderBTG btg = new LoaderBTG(new ByteArrayInputStream(ins.b), null, boptions, br.getFullName());
            return btg;
        } catch (Exception e) {
            throw new RuntimeException("Error opening or reading btg file", e);
        }
    }

}
