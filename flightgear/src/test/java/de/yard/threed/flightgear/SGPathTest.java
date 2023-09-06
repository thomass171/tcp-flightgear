package de.yard.threed.flightgear;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.flightgear.core.simgear.misc.SGPath;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 *
 */
public class SGPathTest {
    static Platform platform = FgTestFactory.initPlatformForTest();

    @Test
    public void testAppend() {

        String[] pieces = new String[]{"","","Instruments-3d","yoke","yoke.xml"};
        SGPath r = new SGPath(pieces[2]);
        //logger.debug(r.str());
        for (int i = 3; i < pieces.length; ++i) {
            r.append(pieces[i]);
            //logger.debug("after append:"+r.str());
        }
        assertEquals("Instruments-3d/yoke/yoke.xml", r.str());

    }

}
