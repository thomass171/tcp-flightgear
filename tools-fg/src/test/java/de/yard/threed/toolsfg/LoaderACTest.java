package de.yard.threed.toolsfg;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.loader.LoadedObject;
import de.yard.threed.core.loader.LoaderAC;
import de.yard.threed.core.loader.PortableModel;
import de.yard.threed.core.loader.StringReader;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.test.testutil.TestUtil;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.javanative.FileReader;
import de.yard.threed.toolsfg.testutil.ModelAssertions;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoaderACTest {
    static Platform platform = FgTestFactory.initPlatformForTest(true, false, false);

    @Test
    public void testTerminal1() throws Exception {

        String acfile = "fg-raw-data/terrasync/Objects/e000n50/e007n50/EDDK-Terminal1.ac";

        LoaderAC ac = new LoaderAC(new StringReader(FileReader.readAsString(new File(TestUtils.locatedTestFile(acfile)))), false);

        PortableModel ppfile = ac.buildPortableModel();
        ModelAssertions.assertTerminal1(ppfile, false);
    }

    @Test
    public void testYoke() throws Exception {

        String acfile = "fg-raw-data/fgdatabasic/Aircraft/Instruments-3d/yoke/yoke.ac";

        LoaderAC ac = new LoaderAC(new StringReader(FileReader.readAsString(new File(TestUtils.locatedTestFile(acfile)))), false);

        PortableModel ppfile = ac.buildPortableModel();
        ModelAssertions.assertYoke(ppfile, false);
    }

    @Test
    public void testEgkkMaint_1_1() throws Exception {

        String acfile = "fg-raw-data/terrasync/Objects/e000n50/e007n50/egkk_maint_1_1.ac";

        LoaderAC ac = new LoaderAC(new StringReader(FileReader.readAsString(new File(TestUtils.locatedTestFile(acfile)))), false);

        PortableModel ppfile = ac.buildPortableModel();
        ModelAssertions.assertEgkkMaint_1_1(ppfile, false);
    }

    @Test
    public void testJetwayMovable() throws Exception {

        String acfile = "fg-raw-data/terrasync/Models/Airport/Jetway/jetway-movable.ac";

        LoaderAC ac = new LoaderAC(new StringReader(FileReader.readAsString(new File(TestUtils.locatedTestFile(acfile)))), false);

        PortableModel ppfile = ac.buildPortableModel();
        ModelAssertions.assertJetwayMovable(ppfile, false);
    }
}
