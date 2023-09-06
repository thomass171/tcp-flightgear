package de.yard.threed.flightgear.core.osg;

import de.yard.threed.engine.SceneNode;

/**
 * http://trac.openscenegraph.org/documentation/OpenSceneGraphReferenceDocs/a00540.html
 * 
 * In Osg Node isType a very general super class.
 * 
 * Erstmal nicht von Model ableiten, obwohl das ganz praktisch waere. Ist doch besser, z.B. wegen Group. Doch nicht, denn das Model bekomme ich aus der ModelFactory.
 * Daraus kann ich dann aber keine Node machen.
 * 26.1.17: Deprecated obselet
 * 
 * Created by thomass on 07.12.15.
 */
@Deprecated
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
}
