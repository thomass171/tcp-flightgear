package de.yard.threed.flightgear.core;

import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterialGlyph;

/**
 * Created by thomass on 10.08.16.
 */
public class SGMaterialGlyphFactory extends de.yard.threed.flightgear.core.simgear.CppFactory<de.yard.threed.flightgear.core.simgear.scene.material.SGMaterialGlyph> {
    @Override
    public SGMaterialGlyph createInstance() {
        return new SGMaterialGlyph();
    }
}
