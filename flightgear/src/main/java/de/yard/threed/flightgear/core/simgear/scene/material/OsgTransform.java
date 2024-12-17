package de.yard.threed.flightgear.core.simgear.scene.material;

import de.yard.threed.core.platform.NativeTransform;
import de.yard.threed.engine.SceneNode;

/**
 * Stub/Proxy for osg::Transform
 *
 * OSG::Transform appears to be a super class of Group, so could be add as child.
 *
 * Try to make it our Transform, or for now better SceneNode to be more consistent because our Transform is a component.
 *
 * 20.11.24: Really needed/useful??
 */
public abstract class OsgTransform extends SceneNode {

    // name comes from SceneNode. private String name;

    public OsgTransform() {

    }

    /**
     * correct here?
     */
    public void dirtyBound(){

    }


    /*name comes from SceneNode public void setName(String name) {
        this.name=name;
    }*/

    /**
     * Replacement of FGs/OSGs way to process animation changes by modifying the model matrix.
     * In principle just a marker for having consistent names.
     * 29.11.24 Not possible due to MCRMCB (see README.md).
     */
    //public abstract boolean updateTransformForAnimation();
}
