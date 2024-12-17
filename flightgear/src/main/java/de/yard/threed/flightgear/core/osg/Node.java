package de.yard.threed.flightgear.core.osg;

import de.yard.threed.engine.SceneNode;
import de.yard.threed.flightgear.core.simgear.scene.material.OsgTransform;

/**
 * http://trac.openscenegraph.org/documentation/OpenSceneGraphReferenceDocs/a00540.html
 * 
 * In Osg Node is a very general super class.
 * 
 * Erstmal nicht von Model ableiten, obwohl das ganz praktisch waere. Ist doch besser, z.B. wegen Group. Doch nicht, denn das Model bekomme ich aus der ModelFactory.
 * Daraus kann ich dann aber keine Node machen.
 * 26.1.17: Deprecated obselet
 * 18.11.24: No longer deprecated for implementing OSGs addChild() for more intuitive migration of animations.
 * Created by thomass on 07.12.15.
 */
public class Node extends SceneNode {
   // Model model;

    
    /**
     * deprecated because nonsense
     */
    //@Deprecated
   /* public Node(){
        this.model = new Model();
    }*/
    
    /*Node(Model model){
        this.model = model;
        
    }*/
    public void setName(String name) {
        super.setName(name);
    }

    /**
     * Abweichend zu OSG gibt es keine Instanzen von Node die invalid sind. Dann wird null verwendet.
     * 
     * @return
     */
    /*public boolean valid() {
        //TODO
        return true;
    }*/

    //
    // 18.11.24 Implementation of OSG typicals methods for better readability of migrated code.
    //
    /*needed?public void addChild(Node model) {
        model.getTransform().setParent(this.getTransform());
    }*/

    /**
     * 18.11.24 A helper for adding OSG::Transform
     */
    /*needed? public void addChild(OsgTransform osgTransform) {
        osgTransform.getTransform().setParent(this.getTransform());
    }*/
}
