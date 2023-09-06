package de.yard.threed.flightgear.core.simgear.scene.model;


import de.yard.threed.core.resource.BundleResource;

import java.util.List;

/**
 * 15.09.2017: Listener fuer das Laden eines XML Models über die Platform(zumindest async, aber platform? Naja.).
 * Der Aufruf heisst aber nicht wirklich, dass das model komplett ist. Es kann sich auch auf submodel beziehen. 
 * Das ist vielleicht noch nicht ganz ausgegoren.
 */
@FunctionalInterface
public interface XmlModelCompleteDelegate {
     /**
      * 2.11.17: Evtl. sollte ein Parameter rein, obwohl der Delegate ja sehr oft aufgerufen werden kann.
      */
     void modelComplete(BundleResource source, List<SGAnimation> animationList/*,SGPropertyNode propertyNode*/);
     
}
