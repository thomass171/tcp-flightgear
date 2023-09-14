package de.yard.threed.flightgear;

import de.yard.threed.core.StringUtils;
import de.yard.threed.core.loader.LoadedObject;
import de.yard.threed.core.loader.LoaderAC;
import de.yard.threed.core.loader.PortableModelDefinition;
import de.yard.threed.core.loader.PortableModelList;
import de.yard.threed.core.loader.StringReader;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.flightgear.core.PropertyList;
import de.yard.threed.flightgear.core.flightgear.main.AircraftResourceProvider;
import de.yard.threed.flightgear.core.flightgear.main.FGProperties;
import de.yard.threed.flightgear.core.osg.Node;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.misc.SGPath;
import de.yard.threed.flightgear.core.simgear.props.PropsIO;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterialLib;
import de.yard.threed.flightgear.core.simgear.structure.SGException;
import de.yard.threed.flightgear.testutil.FgTestFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 * Test for AC loader with some FG models.
 */
public class LoaderACTest {
    static Platform platform = FgTestFactory.initPlatformForTest();


    @Test
    public void testFuelOilAmps() {
        try {
            BundleResource br = new BundleResource(BundleRegistry.getBundle("test-resources"), "models/FuelOilAmps.ac");
            LoaderAC ac = new LoaderAC(new StringReader(br.bundle.getResource(br).getContentAsString()), br);
            System.out.println(ac.loadedfile.dumpMaterial("\n"));
            Assertions.assertEquals( 1, ac.loadedfile.objects.size());
            LoadedObject world = ac.loadedfile.objects.get(0);
            Assertions.assertEquals( "FuelOilAmps", world.name,"world.name");
            System.out.println(ac.loadedfile.dumpObject("", world, "\n"));
            Assertions.assertEquals( 1, world.kids.size(),"world kids");
            LoadedObject group = world.kids.get(0);
            Assertions.assertEquals( 6, group.kids.size(),"group kids");
            Assertions.assertEquals( "FuelOilAmps.png", group.kids.get(0).texture,"texture");

            int[] flist = new int[]{5, 1, 1, 1, 1, 1};
            for (int i = 0; i < 6; i++) {
                LoadedObject kid = group.kids.get(i);
                Assertions.assertEquals( 1, kid.getFaceLists().size(),"facelists");
                Assertions.assertEquals( 1, kid.facelistmaterial.size());
                Assertions.assertEquals( flist[i], group.kids.get(i).getFaceLists().get(0).faces.size(),"face4");

                //TestUtil.assertFace4("face 0", new int[]{3, 2, 1, 0}, (Face4) ac.objects.get(0).kids.get(0).getFaceLists().get(0).get(0));
            }
            //22.12.17 auch PP testen
            PortableModelList ppfile = ac.preProcess();
            PortableModelDefinition ppworld = ppfile.getObject(0);
            Assertions.assertEquals("FuelOilAmps", ppworld.name,"world.name");
            //ist null TestUtil.assertEquals("source", "ss", ppfile.source.name);
            PortableModelDefinition ppgroup = ppworld.kids.get(0);
            //dein Material hat keine Textur
            Assertions.assertEquals( "FuelOilAmps.png", ppfile.materials.get(1).texture,"texture");


        } catch (Exception e) {
            throw new RuntimeException("Error opening or reading ac file", e);
        }
    }

    @Test
    public void testControlLight() {
        try {
            BundleResource br = new BundleResource(BundleRegistry.getBundle("test-resources"), "models/ControlLight.ac");
            LoaderAC ac = new LoaderAC(new StringReader(br.bundle.getResource(br).getContentAsString()), false);

            //LoaderAC ac = new LoaderAC(FileReader.getFileStream(new BundleResource("flusi/ControlLight.ac")), false);
            System.out.println(ac.loadedfile.dumpMaterial("\n"));
            Assertions.assertEquals( 1, ac.loadedfile.objects.size());
            LoadedObject world = ac.loadedfile.objects.get(0);
            System.out.println(ac.loadedfile.dumpObject("", world, "\n"));
            Assertions.assertEquals( 2, world.kids.size(),"world kids");
            LoadedObject cylinder = world.kids.get(0);
            Assertions.assertEquals( 0, cylinder.kids.size(),"group kids");
            Assertions.assertEquals( 1, cylinder.getFaceLists().size(),"facelists");
            Assertions.assertEquals( 24, cylinder.getFaceLists().get(0).faces.size(),"face4");
        } catch (Exception e) {
            throw new RuntimeException("Error opening or reading ac file", e);
        }
    }

}
