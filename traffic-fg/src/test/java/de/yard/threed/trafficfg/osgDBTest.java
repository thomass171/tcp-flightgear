package de.yard.threed.trafficfg;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.flightgear.core.osgdb.osgDB;
import de.yard.threed.javacommon.JavaBundleResolverFactory;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by thomass on 22.08.16.
 */
public class osgDBTest {
    static Platform platform = EngineTestFactory.initPlatformForTest( new String[] {"engine"}, new SimpleHeadlessPlatformFactory());

    @Test
    public void testUtils() {
        Assertions.assertEquals( "c.Ext", osgDB.getSimpleFileName("/a/b/c.Ext"));
        Assertions.assertEquals( "c.Ext", osgDB.getSimpleFileName("c.Ext"));
        Assertions.assertEquals( "c.Ext", osgDB.getSimpleFileName("./c.Ext"));
    }
    }
