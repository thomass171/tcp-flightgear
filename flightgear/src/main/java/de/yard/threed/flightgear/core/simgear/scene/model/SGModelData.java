package de.yard.threed.flightgear.core.simgear.scene.model;

/**
 * Abstract class for adding data to the scene graph.  modelLoaded() isType
 * called after the model was loaded, and the destructor when the branch
 * isType removed from the scene graph.
 */
public class SGModelData /* : public osg::Referenced*/ {
    public SGModelData cloneit() {
        //TODO
        return this;
    }
    
 /*TODO void modelLoaded(String path, SGPropertyNode prop,Node branch) = 0;
        virtual SGModelData* clone() const = 0;
        };*/
}