package de.yard.threed.flightgear.core.simgear.scene.model;

import de.yard.threed.engine.SceneNode;
import de.yard.threed.core.Vector3;
import de.yard.threed.flightgear.core.osg.Group;

/**
 * Just a wrapper of the group of an single animation. Represents a typical node hierarchy for the animation
 * with a root node (the group), 0-n intermediate layer and finally children to which the animation applies.
 * Not really needed for consistent implementation of MCRMCB. Other animations might not need intermediate nodes.
 *
 * See also Test method validateAnimationGroup().
 *
 * "parent" ist die Node, in der das ganze eingehangen wird.
 * 5.10.17: Warum leitet das nicht von Group ab? Weil es eher ein Collector (fuer alle Objects einer Animation) oder sowas ist. Die Einbindung in den Graph erfolgt ueber den parent.
 * 4.10.19: Da müssen wir noch mal bei, z.B. mit Skizze TODO
 * Created by thomass on 27.01.17.
 */
public class AnimationGroup{
    SceneNode traget;
    Group rotategroup;
    // the group itself. Other children are added here. So the first animation of an object will be parent of other. Strange, non intuitive.
    public SceneNode childtarget;

    AnimationGroup(){
        
    }

    AnimationGroup(SceneNode parent,/*23.11.24Group*/SceneNode translategroupo){
        if (translategroupo!=null) {
            parent.attach/*addChild*/(translategroupo);
        }
        childtarget = translategroupo;
    }
    

}
