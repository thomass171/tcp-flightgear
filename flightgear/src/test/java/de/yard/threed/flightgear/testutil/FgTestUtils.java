package de.yard.threed.flightgear.testutil;


import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleData;

public class FgTestUtils {

    /**
     * Dummy Bundle bauen
     */
    public static Bundle buildDummyBundleModel777() {
        Bundle my777 = new Bundle("My-777", "Models/777-200.ac\n", false);
        my777.addResource("Models/777-200.ac", new BundleData(""));
        return my777;
    }

    public static Bundle buildDummyBundleModelbasic() {
        Bundle fgdatabasicmodel = new Bundle("fgdatabasicmodel", "AI/Aircraft/737/Models/B737-300.ac\nAI/Aircraft/737/737-AirBerlin.xml\n", false);
        fgdatabasicmodel.addResource("AI/Aircraft/737/Models/B737-300.ac", new BundleData(""));
        fgdatabasicmodel.addResource("AI/Aircraft/737/737-AirBerlin.xml", new BundleData(""));
        return fgdatabasicmodel;
    }
}
