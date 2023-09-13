package de.yard.threed.toolsfg;

import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.core.loader.PortableModelList;
import de.yard.threed.core.platform.Platform;

import de.yard.threed.flightgear.LoaderBTG;
import de.yard.threed.flightgear.LoaderOptions;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import de.yard.threed.flightgear.testutil.ModelAssertions;

import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterialCache;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterialLib;
import de.yard.threed.flightgear.core.simgear.scene.tgdb.SGTexturedTriangleBin;
import de.yard.threed.flightgear.core.simgear.scene.tgdb.SGTileGeometryBin;


import de.yard.threed.toolsfg.testutil.BtgModelAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;


/**
 * Created by thomass on 05.08.16.
 */
public class SGTileGeometryBinTest {
    static Platform platform = FgTestFactory.initPlatformForTest();

    @Test
    public void testSurfaceGeometryOhneMaterial() {
        LoaderBTG refbtg = BtgModelAssertions.loadRefBtg(null);

        SGTileGeometryBin tileGeometryBin = new SGTileGeometryBin();

        if (!tileGeometryBin.insertSurfaceGeometry(refbtg, null)) {
            fail("");
        }
        Assertions.assertEquals( 17, tileGeometryBin.materialTriangleMap.size(),"materialTriangleMap.size()");
        Assertions.assertEquals( 98, tileGeometryBin.materialTriangleMap.get("Airport")._triangleVector.size(),"materialTriangleMap[Airport].size");
        Assertions.assertEquals(1181, tileGeometryBin.materialTriangleMap.get("ComplexCrop")._triangleVector.size(),"materialTriangleMap[ComplexCrop].size");
        Assertions.assertEquals( 7798, tileGeometryBin.materialTriangleMap.get("CropGrass")._triangleVector.size(),"materialTriangleMap[CropGrass].size");
        Assertions.assertEquals(1648, tileGeometryBin.materialTriangleMap.get("DeciduousForest")._triangleVector.size(),"materialTriangleMap[DeciduousForest].size");
        Assertions.assertEquals(2995, tileGeometryBin.materialTriangleMap.get("DryCrop")._triangleVector.size(),"materialTriangleMap[DryCrop].size");
        Assertions.assertEquals( 5171, tileGeometryBin.materialTriangleMap.get("EvergreenForest")._triangleVector.size(),"materialTriangleMap[EvergreenForest].size");
        Assertions.assertEquals( 5010, tileGeometryBin.materialTriangleMap.get("Freeway")._triangleVector.size(),"materialTriangleMap[Freeway].size");
        Assertions.assertEquals( 40, tileGeometryBin.materialTriangleMap.get("Grassland")._triangleVector.size(),"materialTriangleMap[Grassland].size");
        Assertions.assertEquals(97, tileGeometryBin.materialTriangleMap.get("Industrial")._triangleVector.size(),"materialTriangleMap[Industrial].size");
        Assertions.assertEquals(2647, tileGeometryBin.materialTriangleMap.get("MixedForest")._triangleVector.size(),"materialTriangleMap[MixedForest].size");
        Assertions.assertEquals( 618, tileGeometryBin.materialTriangleMap.get("NaturalCrop")._triangleVector.size(),"materialTriangleMap[NaturalCrop].size");
        Assertions.assertEquals( 45, tileGeometryBin.materialTriangleMap.get("OpenMining")._triangleVector.size(),"materialTriangleMap[OpenMining].size");
        Assertions.assertEquals( 3055, tileGeometryBin.materialTriangleMap.get("Railroad")._triangleVector.size(),"materialTriangleMap[Railroad].size");
        Assertions.assertEquals( 1616, tileGeometryBin.materialTriangleMap.get("Road")._triangleVector.size(),"materialTriangleMap[Road].size");
        Assertions.assertEquals(1327, tileGeometryBin.materialTriangleMap.get("Scrub")._triangleVector.size(),"materialTriangleMap[Scrub].size");
        Assertions.assertEquals( 247, tileGeometryBin.materialTriangleMap.get("Stream")._triangleVector.size(),"materialTriangleMap[Stream].size");
        Assertions.assertEquals( 1563, tileGeometryBin.materialTriangleMap.get("Town")._triangleVector.size(),"materialTriangleMap[Town].size");

        SGTexturedTriangleBin airporttriangles = tileGeometryBin.materialTriangleMap.get("Airport");
        Assertions.assertEquals( 98, airporttriangles._triangleVector.size(),"airporttriangles.triangles.size");
        Assertions.assertEquals(78, airporttriangles._values.size(),"airporttriangles.vertnormtex.size");

        //preprocess Test ist in LoaderTest
    }

    /**
     * High level Test. SGMaterialTest sollte lauffähig sein.
     */
    @Test
    public void testSurfaceGeometryMitMaterial() {
       
        SGMaterialLib matlib = FlightGear.loadMatlib();
        if (matlib == null) {
            fail("matlib.load failed");
        }
        SGMaterialCache matcache = matlib.generateMatCache(SGGeod.fromCart(FlightGear.refbtgcenter));

        // BTG einlesen
        LoaderBTG refbtg = BtgModelAssertions.loadRefBtg(new LoaderOptions(matlib));
        SGTileGeometryBin tileGeometryBin = new SGTileGeometryBin();

        // und Nodes/Meshes bauen
        if (!tileGeometryBin.insertSurfaceGeometry(refbtg, matcache)) {
            fail("");
        }

        SimpleGeometry geometry = tileGeometryBin.materialTriangleMap.get("Airport").buildGeometry(tileGeometryBin.materialTriangleMap.get("Airport")._triangleVector, false);
        Assertions.assertEquals(78, geometry.getVertices().size(),"geometry.vertices.size()");
        geometry = tileGeometryBin.materialTriangleMap.get("Freeway").buildGeometry(tileGeometryBin.materialTriangleMap.get("Freeway")._triangleVector, false);
        Assertions.assertEquals( 4416, geometry.getVertices().size(),"geometry.vertices.size()");
        geometry = tileGeometryBin.materialTriangleMap.get("CropGrass").buildGeometry(tileGeometryBin.materialTriangleMap.get("CropGrass")._triangleVector, false);
        Assertions.assertEquals( 7518, geometry.getVertices().size(),"geometry.vertices.size()");
        //28.12.17: Grassland muss es geben, wenn die Conditions (z.B. season) richtig bewertet wurden.
        Assertions.assertNotNull(matcache.find("Grassland"),"Grassland");
        
        // FG loggt hier:
        // buildGeometry: 78 vertices for 98 triangles
        // buildGeometry: 1128 vertices for 1181 triangles
        // buildGeometry: 7518 vertices for 7798 triangles
        // buildGeometry: 1518 vertices for 1648 triangles
        // buildGeometry: 2954 vertices for 2995 triangles
        // buildGeometry: 4468 vertices for 5171 triangles
        // buildGeometry: 4416 vertices for 5010 triangles
        /*buildGeometry: 43 vertices for 40 triangles
        buildGeometry: 108 vertices for 97 triangles
        buildGeometry: 2259 vertices for 2647 triangles
        buildGeometry: 588 vertices for 618 triangles
        buildGeometry: 41 vertices for 45 triangles
        buildGeometry: 2173 vertices for 3055 triangles
        buildGeometry: 1612 vertices for 1616 triangles
        buildGeometry: 1273 vertices for 1327 triangles
        buildGeometry: 258 vertices for 247 triangles
        buildGeometry: 1595 vertices for 1563 triangles*/


        //preprocess Test ist auch in LoaderTest. Hier aber mit Materiallib
        PortableModelList ppfile = refbtg.preProcess();
        ModelAssertions.assertRefbtg(ppfile, true);
    }


}
