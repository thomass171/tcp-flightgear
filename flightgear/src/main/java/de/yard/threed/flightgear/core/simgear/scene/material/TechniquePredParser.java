package de.yard.threed.flightgear.core.simgear.scene.material;

import de.yard.threed.flightgear.core.simgear.structure.ExpressionParser;

/**
 * From Technique.[ch]xx
 */
public class TechniquePredParser  extends ExpressionParser
{
    /*protected    osg::ref_ptr<*/Technique _tniq;

    public void setTechnique(Technique tniq) {
        _tniq = tniq;
    }

    Technique getTechnique() { return _tniq/*.get()*/; }
//    void setEffect(Effect* effect) { _effect = effect; }
//    Effect* getEffect() { return _effect.get(); }
    // osg::ref_ptr<Effect> _effect;

}


