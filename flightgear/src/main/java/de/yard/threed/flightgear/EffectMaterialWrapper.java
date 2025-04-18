package de.yard.threed.flightgear;

import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.engine.Material;

/**
 * Substitute of osg::StateSet as super class. See README.md#Effects.
 * A wrapper to a material where an effect can apply.
 */
public class EffectMaterialWrapper {

    // for testing
    public static int counter=0;

    Material material;
    public PortableMaterial materialdefinition = null;

    public EffectMaterialWrapper(Material material) {
        this.material = material;
    }

    /**
     * Corresponding to OpenGL glEnable(GL_BLEND)
     */
    public void setBlending(boolean enabled) {
        // quick hack as workaround
        if (material == null) {
            //log.warn();
            return;
        }
        counter++;
        material.setTransparency(enabled);
    }
}
