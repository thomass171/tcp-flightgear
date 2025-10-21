package de.yard.threed.flightgear;

import de.yard.threed.flightgear.core.simgear.scene.material.Effect;

/**
 * For testing purposes
 * 13.10.25
 */
public interface EffectBuilderListener {
    void effectBuilt(Effect effect, String xmlResource, String objectName);
}
