package de.yard.threed.flightgear;

import de.yard.threed.core.BuildResult;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.NativeResource;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.platform.common.ModelLoader;
import de.yard.threed.engine.testutil.EngineTestUtils;
import de.yard.threed.engine.testutil.TestHelper;
import de.yard.threed.flightgear.core.flightgear.main.FGGlobals;
import de.yard.threed.flightgear.core.osg.Group;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.props.PropsIO;
import de.yard.threed.flightgear.core.simgear.props.SGCondition;
import de.yard.threed.flightgear.core.simgear.scene.material.MakeEffect;
import de.yard.threed.flightgear.core.simgear.scene.model.ACProcessPolicy;
import de.yard.threed.flightgear.core.simgear.scene.model.SGAnimation;
import de.yard.threed.flightgear.core.simgear.scene.model.SGReaderWriterXML;
import de.yard.threed.flightgear.core.simgear.scene.model.SGSelectAnimation;
import de.yard.threed.flightgear.core.simgear.scene.util.SGTransientModelData;
import de.yard.threed.flightgear.core.simgear.structure.SGException;
import de.yard.threed.flightgear.testutil.AnimationAssertions;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.flightgear.testutil.FgTestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Now a dedicated test beside tests as part of others
 * <p>
 */
@Slf4j
public class SGConditionTest {

    @BeforeEach
    void setup() {
        FgTestFactory.initPlatformForTest(true, false, false);
    }

    /**
     * Extracted from c172p MagCompass config in "Models/Interior/Panel/c172p-panel/c172p.xml"
     */
    @ParameterizedTest
    @CsvSource(value = {
            "false,true",
            "true,true",
            //The param section used for magcompass alias resides only in upper XML file and is not be found at all(?)
            "false,false",
            "true,false"
    })
    public void testMagCompassConditionWithAlias(boolean negated, boolean aliasExists) throws SGException {

        // Aliases are resolved while reading xml file
        String xmlbuf = "<?xml version=\"1.0\"?>"+"" +
                "<PropertyList>"+
                (aliasExists?"<params><bushkit>sim/model/variant</bushkit></params>\n":"") +
                "<model><name>MagCompassFloat</name>\n" +
                "<path>\n" +
                "Aircraft/c172p/Models/Interior/Panel/Instruments/mag-compass/mag-compass-float.xml\n" +
                "</path>\n" +
                "<condition>\n" + ((negated) ? "<not>" : "") +
                "<or>\n" +
                "<equals>\n" +
                "<property alias=\"/params/bushkit\"/>\n" +
                "<value>3</value>\n" +
                "</equals>\n" +
                "<equals>\n" +
                "<property alias=\"/params/bushkit\"/>\n" +
                "<value>4</value>\n" +
                "</equals>\n" +
                "</or>\n" + ((negated) ? "</not>" : "") +
                "</condition>\n" +
                "</model></PropertyList>";
        SGPropertyNode props = new SGPropertyNode();
        new PropsIO().readProperties(xmlbuf, new BundleResource("in-mem"),null, props, 0, false, false);

        SGPropertyNode conditionNode = props.getNode("/model[0]/condition", false);
        // yes, needs the global property tree. There can be only one.
        SGCondition condition = SGCondition.sgReadCondition(FGGlobals.getInstance().get_props(), conditionNode);

        if (negated) {
            assertTrue(condition.test());
        } else {
            assertFalse(condition.test());
        }
    }
}
