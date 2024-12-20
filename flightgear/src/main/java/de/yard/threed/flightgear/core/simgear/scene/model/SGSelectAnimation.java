package de.yard.threed.flightgear.core.simgear.scene.model;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.NativeCollision;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.platform.common.RequestHandler;
import de.yard.threed.flightgear.core.osg.Group;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.props.SGCondition;
import de.yard.threed.flightgear.core.simgear.scene.util.SGTransientModelData;

import java.util.List;

/**
 * Implementation of a select animation
 */
public class SGSelectAnimation extends SGAnimation {
    SGCondition _condition;
    // The node where the scale finally applies
    public Group conditionNode;
    private boolean selected = false;

    //SGSelectAnimation::SGSelectAnimation(simgear::SGTransientModelData &modelData) :
    public SGSelectAnimation(SGTransientModelData modelData, String label) {
        super(modelData, label);

        _condition = getCondition();
    }

    @Override
    public AnimationGroup createAnimationGroup(SceneNode parent) {
        // if no condition given, this is a noop.
        // SGCondition condition = getCondition();
        // trick, gets deleted with all its 'animated' children
        // when the animation installer returns
        if (_condition == null) {
            return new AnimationGroup(parent, null);
        }
        /*ConditionNode cn = new simgear::ConditionNode;
        cn -> setName("select animation node");
        cn -> setCondition(condition.ptr());
        Group grp = new Group();
        cn.addChild(grp);
        parent.addChild(cn);
        return grp;
        */
        conditionNode = new Group();
        conditionNode.setName("selectAnimation");
        AnimationGroup ag = new AnimationGroup(parent, conditionNode);
        // unselect is default
        conditionNode.getTransform().setScale(new Vector3());
        return ag;
    }

    @Override
    public void process(List<NativeCollision> pickingrayintersections, RequestHandler requestHandler) {
        if (conditionNode == null) {
            //logger.warn("No conditionNode in " + getConfigNode().getPath());
            return;
        }
        if (_condition == null) {
            return;
        }
        // The FG ConditionNode skips a node during drawing?:
        // 'If the condition is true, traverse the first child; otherwise, traverse the second if it exists.'
        // Try to replace by scale (which also affects children!)
        if (_condition.test()) {
            if (!selected) {
                conditionNode.getTransform().setScale(new Vector3(1, 1, 1));
                selected = true;
            }
        } else {
            if (selected) {
                conditionNode.getTransform().setScale(new Vector3());
                selected = false;
            }
        }
    }

    public boolean isSelected() {
        return selected;
    }
}
