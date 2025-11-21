package de.yard.threed.flightgear.core.osg;

import de.yard.threed.flightgear.ecs.FgAnimationUpdateSystem;

public abstract class NodeCallback {

    String name;
    public Node node;
    public NodeCallback(Node node, String name){
        this.node=node;
        this.name=name;
        FgAnimationUpdateSystem    .nodeCallbacks.add(this);
    }

    /**
     * FG comment about traverse():
     * > note, callback is responsible for scenegraph traversal so
     * > should always include call traverse(node,nv) to ensure
     * > that the rest of cullbacks and the scene graph are traversed.
     */
    /*we have update instead public void traverse(Node node, NodeVisitor nv){
    }*/

    public abstract void update();

    public String getName() {
        return name;
    }
}
