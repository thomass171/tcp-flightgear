package de.yard.threed.flightgear.core.simgear.scene.model;


import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.SceneNode;

import java.util.List;

/**
 * 15.09.2017: Listener for loading a XML model (XML is sync and not via platform, GLTF is async via platform).
 * This delegate is also called for XML submodel.
 * TODO recheck if this is useful
 */
@FunctionalInterface
public interface XmlModelCompleteDelegate {
     /**
      * 2.11.17: Evtl. sollte ein Parameter rein, obwohl der Delegate ja sehr oft aufgerufen werden kann.
      */
     void modelComplete(BundleResource source, SceneNode destinationNode, List<SGAnimation> animationList/*,SGPropertyNode propertyNode*/);
     
}
