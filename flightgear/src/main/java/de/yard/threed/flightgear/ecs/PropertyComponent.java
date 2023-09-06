package de.yard.threed.flightgear.ecs;


import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.DefaultEcsComponent;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.core.platform.Log;

/**
 * Ansatz fuer Entiteis mit eigenem PropertyTree
 *
 * z.B. railing asi needle.
 *
 * MA31: Ist in GraphMovingSystem auskommentiert/entfernt.
 *
 * Created by thomass on 03.04.18.
 */
public class PropertyComponent extends DefaultEcsComponent {
    private  Log logger = Platform.getInstance().getLog(PropertyComponent.class);

    public static String TAG = "PropertyComponent";
    public SGPropertyNode rootnode;

    public PropertyComponent(SGPropertyNode rootnode) {
        this.rootnode = /*new SGPropertyNode();/*/rootnode;
        
    }

    @Override
    public String getTag() {
        return TAG;
    }

    
    public static PropertyComponent getPropertyComponent(EcsEntity e) {
        PropertyComponent pc = (PropertyComponent) e.getComponent(PropertyComponent.TAG);
        return pc;
    }

    /**
     * For railing (und c172p) asi needle.
     *
     * @param speed
     */
    public void setSpeed(double speed) {
        //this.speed = speed;
        SGPropertyNode sn = rootnode.getNode("fdm/jsbsim/velocities/vias-kts", true);
        
        sn.setDoubleValue(speed);
        /*if (speed > 0.001f){
            logger.debug("setSpeed "+speed+" in "+sn.getPath(true));
        }*/
    }
}


