package de.yard.threed.flightgear.testutil;

import de.yard.threed.flightgear.EffectBuilderListener;
import de.yard.threed.flightgear.core.simgear.scene.material.Effect;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EffectCollector implements EffectBuilderListener {
    public Map<String, Map<String, List<Effect>>> effects = new HashMap<>();

    public EffectCollector() {

    }

    @Override
    public void effectBuilt(Effect effect, String xmlResource, String objectName) {
       // log.debug("effectBuilt");
        Map<String, List<Effect>> effectsPerXml = effects.get(xmlResource);
        if (effectsPerXml==null){
            effects.put(xmlResource, new HashMap<>());
            effectsPerXml = effects.get(xmlResource);
        }
        List<Effect> effectsPerObject = effectsPerXml.get(objectName);
        if (effectsPerObject==null){
            effectsPerXml.put(objectName, new ArrayList<>());
            effectsPerObject = effectsPerXml.get(objectName);
        }
        effectsPerObject.add(effect);
    }
}
