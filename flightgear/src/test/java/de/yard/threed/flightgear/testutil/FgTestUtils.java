package de.yard.threed.flightgear.testutil;


import de.yard.threed.core.buffer.SimpleByteBuffer;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import org.junit.jupiter.api.Assertions;

public class FgTestUtils {

    /**
     * Dummy Bundle bauen
     */
    public static Bundle buildDummyBundleModel777() {
        Bundle my777 = new Bundle("My-777", "Models/777-200.ac\n", false,"??");
        my777.addResource("Models/777-200.ac", new BundleData(new SimpleByteBuffer(new byte[]{}), true));
        return my777;
    }

    public static Bundle buildDummyBundleModelbasic() {
        Bundle fgdatabasicmodel = new Bundle("fgdatabasicmodel", "AI/Aircraft/737/Models/B737-300.ac\nAI/Aircraft/737/737-AirBerlin.xml\n", false,"??");
        fgdatabasicmodel.addResource("AI/Aircraft/737/Models/B737-300.ac", new BundleData(new SimpleByteBuffer(new byte[]{}), true));
        fgdatabasicmodel.addResource("AI/Aircraft/737/737-AirBerlin.xml", new BundleData(new SimpleByteBuffer(new byte[]{}), true));
        return fgdatabasicmodel;
    }

    public static void assertSGGeod(String label, SGGeod expected, SGGeod actual) {
        Assertions.assertEquals( (float) expected.getLatitudeDeg().getDegree(), (float) actual.getLatitudeDeg().getDegree(),0.03,"LatitudeDeg");
        Assertions.assertEquals( (float) expected.getLongitudeDeg().getDegree(), (float) actual.getLongitudeDeg().getDegree(),0.03, "LongitudeDeg");
    }
}
