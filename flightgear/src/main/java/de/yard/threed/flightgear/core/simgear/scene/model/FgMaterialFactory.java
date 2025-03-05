package de.yard.threed.flightgear.core.simgear.scene.model;

import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.resource.ResourceLoader;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.AbstractMaterialFactory;
import de.yard.threed.engine.loader.DefaultMaterialFactory;
import de.yard.threed.flightgear.core.simgear.scene.material.Effect;

/**
 * Replaces (should replace) the MakeEffectVisitor way for having a material effect
 * Not yet used. Maybe not needed at all.
 * 06.02.2025: XML model loading have their own hard coded factory.
 */
public class FgMaterialFactory extends AbstractMaterialFactory {
    Effect effect;
    public FgMaterialFactory(Effect effect) {
        this.effect=effect;
    }

    @Override
    public Material buildMaterial(ResourceLoader resourceLoader, PortableMaterial mat, ResourcePath texturebasepath, boolean hasnormals) {
        DefaultMaterialFactory defaultMaterialFactory = new DefaultMaterialFactory();

        Material material = defaultMaterialFactory.buildMaterial(resourceLoader,mat,texturebasepath,hasnormals);
        return material;
    }
}
