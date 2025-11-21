package de.yard.threed.flightgear.core.osg;

import de.yard.threed.core.Util;
import de.yard.threed.core.Vector3;

/**
 * Created by thomass on 08.06.16.
 */
public class Switch extends Group {
    private String subPathStr;
    public NodeCallback callback;

    public void addChild(Node model) {
        super.attach(model);
    }

    public void setChildValue(Group group, boolean b) {

    }

    public void setAllChildrenOn() {
        callback.node.getTransform().setScale(new Vector3(1, 1, 1));
    }

    public void setAllChildrenOff() {
        callback.node.getTransform().setScale(new Vector3());
    }

    public void setUpdateCallback(NodeCallback callback) {
        this.callback = callback;
    }

    public void setInfo(String subPathStr) {
        this.subPathStr = subPathStr;
    }
}
