package de.yard.threed.flightgear;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.Vector2;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.core.Pair;
import de.yard.threed.flightgear.core.flightgear.main.FGGlobals;
import de.yard.threed.flightgear.core.flightgear.main.FGProperties;
import de.yard.threed.flightgear.core.simgear.scene.material.FGEffect;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterial;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterialCache;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterialLib;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.core.StringUtils;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;


/**
 * For SGMaterial, SGMaterialLib, SGMaterialCache, Effect and other material related.
 * <p>
 * Created by thomass on 08.08.16.
 */
public class SGMaterialTest {

    @BeforeAll
    static void setup() {
        FgTestFactory.initPlatformForTest(false, true);
    }

    /**
     * Nur laden ohne Cache
     */
    @Test
    public void testLoad() {
        SGMaterialLib matlib = initSGMaterialLib();

        // 1.10.23 was 288 from Granada bundle, 283 now from project might be correct
        Assertions.assertEquals(/*FG 3.4 284*/283, matlib.matlib.size(),"matlib.size");
        List<SGMaterial> mixedForestlist = matlib.matlib.get("MixedForest");
        // 1.10.23 was 24, now 2(?)
        Assertions.assertEquals(/*FG 3.4 8*/2, mixedForestlist.size(),"matlib.MixedForest.size");
        SGGeod refcenter = SGGeod.fromCart(FlightGear.refbtgcenter);
        SGMaterial mixedForest = matlib.find("MixedForest", new Vector2(refcenter.getLongitudeDeg().getDegree(),  refcenter.getLatitudeDeg().getDegree()));
        Assertions.assertEquals( 2000f, (float) mixedForest.get_xsize());
    }

    /**
     * Test auch SGConditional.
     */
    @Test
    public void testCache() {
        SGMaterialLib matlib = initSGMaterialLib();
        SGMaterialCache matcache = matlib.generateMatCache(SGGeod.fromCart(FlightGear.refbtgcenter));

        SGMaterial matMixedForest = matcache.find("MixedForest");
        if (matMixedForest == null) {
            fail("SGMaterialLib::generateMatCache: MixedForest not found");
        }
        Assertions.assertEquals( 2000f, (float) matMixedForest.get_xsize(),"xsize");
        List<Pair<BundleResource, Integer>> tpath = matMixedForest.getTexturePaths(0);
        // Der Test wird im Winter wohl scheitern
        Assertions.assertEquals("mixedforest-hires-autumn.png", StringUtils.substringAfterLast(tpath.get(0).getFirst().getFullName(), "/"),"texturepath0");

    }

    /**
     * Wahrscheinlich aus Materials/regions/global-winter.xml
     * Effect "Effects/terrain-default" ist der Defaulteffect;
     */
    @Test
    public void testMixedForestEffect() {
        SGMaterialLib matlib = initSGMaterialLib();

        SGGeod refcenter = SGGeod.fromCart(FlightGear.refbtgcenter);
        SGMaterial mixedForest = matlib.find("MixedForest", new Vector2((float) refcenter.getLongitudeDeg().getDegree(), (float) refcenter.getLatitudeDeg().getDegree()));
        int textureindex = 0;
        FGEffect eff = mixedForest.get_one_effect(textureindex);
    }

    /**
     * Aus Materials/regions/global.xml
     */
    @Test
    public void testGrassRwyEffect() {
        SGMaterialLib matlib = initSGMaterialLib();
        List<SGMaterial> grassrwylist = matlib.matlib.get("grass_rwy");
        Assertions.assertEquals(1, grassrwylist.size(),"matlib.grass_rwy.size");
        SGGeod refcenter = SGGeod.fromCart(FlightGear.refbtgcenter);
        SGMaterial grassrwy = matlib.find("grass_rwy", new Vector2((float) refcenter.getLongitudeDeg().getDegree(), (float) refcenter.getLatitudeDeg().getDegree()));
        Assertions.assertNotNull( grassrwy,"matlib.grass_rwy");
        Assertions.assertEquals(75, (float) grassrwy.get_xsize(),"xsize");
    }

    public static SGMaterialLib initSGMaterialLib() {
        // braucht auch einen Init wegen PropertyTree
        //MA23 FlightGear.init(0, FlightGear.argv);
        FlightGearMain.initFG(null, null);


        //SGPath mpath = new SGPath(FGGlobals.globals.get_fg_root());
        //mpath.append(FGProperties.fgGetString("/sim/rendering/materials-file"));
        String mpath = FGProperties.fgGetString("/sim/rendering/materials-file");
        //28.6.17: preferences.xml wird nicht mehr gelesen
        mpath ="Materials/regions/materials.xml";
        FGProperties.fgSetString("/sim/startup/season","summer");
        SGMaterialLib matlib = new SGMaterialLib();
        EngineTestFactory.loadBundleSync(SGMaterialLib.BUNDLENAME);
        //"Materials/regions/materials.xml"
        if (!matlib.load(mpath, FGGlobals.globals.get_props())) {
            fail("matlib.load failed");
        }
        return matlib;
    }
}
