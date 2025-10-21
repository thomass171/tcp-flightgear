package de.yard.threed.flightgear.core.osg;

import de.yard.threed.core.Util;

/**
 * Created by thomass on 08.06.16.
 */
public class Switch extends Group {
    public void addChild(Node model) {
        super.attach(model);
    }

    public void setChildValue(Group group, boolean b) {

    }

    public void setAllChildrenOn() {
        Util.notyet();

    }

    public void setAllChildrenOff() {
        Util.notyet();
    }

    public void setUpdateCallback(NodeCallback callback) {

    }
}
