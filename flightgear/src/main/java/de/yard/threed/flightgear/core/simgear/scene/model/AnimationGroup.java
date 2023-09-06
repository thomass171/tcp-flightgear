package de.yard.threed.flightgear.core.simgear.scene.model;

import de.yard.threed.engine.SceneNode;
import de.yard.threed.core.Vector3;
import de.yard.threed.flightgear.core.osg.Group;

/**
 * Kapselung um das Bauen einer Group zur Animation?
 * Erstellung einer Node Hierarchie, in der dann die Animation stattfindet. "parent" ist die Node, in der das ganze eingehangen wird.
 * 5.10.17: Warum leitet das nicht von Group ab? Weil es eher ein Collector (fuer alle Objects einer Animation) oder sowas ist. Die Einbindung in den Graph erfolgt ueber den parent.
 * 4.10.19: Da müssen wir noch mal bei, z.B. mit Skizze TODO
 * Created by thomass on 27.01.17.
 */
public class AnimationGroup{
    SceneNode traget;
    Group rotategroup;
    public SceneNode childtarget;

    AnimationGroup(){
        
    }

    AnimationGroup(SceneNode parent,Group translategroupo){
        parent.attach/*addChild*/(translategroupo);
        childtarget = translategroupo;
    }
    
    /**
     * 5.10.17: Kruecke. Muss in SGRotationAnimation. TODO
     * Aufbau: parent->translategroup->rotategroup->childtarget
     * @param parent
     * @param c
     * @return
     */
    public static AnimationGroup buildAnimationGroupForRotation(SceneNode parent, Vector3 c){
        //TODO das gehoert doch in die SGRotation
        Group translategroupo = new Group();
        translategroupo.setName("center back translate");
        translategroupo.getTransform().setPosition(c);

        AnimationGroup ag = new AnimationGroup(parent,translategroupo);

        ag.childtarget = new SceneNode();
        ag.childtarget.setName("center 0 translate");
        ag.childtarget.getTransform().setPosition(c.negate());

        ag.rotategroup = new Group();
        ag.rotategroup.setName("Spin Animation Group");
        ag.rotategroup.attach/*addChild*/(ag.childtarget);
        translategroupo.attach/*addChild*/(ag.rotategroup);
       
        return ag;
    }
}
