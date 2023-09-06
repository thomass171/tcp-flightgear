package de.yard.threed.flightgear.core.osg;

/**
 * Created by thomass on 08.06.16.
 */
public class Switch extends Group {
    public void addChild(Node model) {
        super.attach(model);
    }

    public void setChildValue(Group group, boolean b) {
        //TODO
    }
}
