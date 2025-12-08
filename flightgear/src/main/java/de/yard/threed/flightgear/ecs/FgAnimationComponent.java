package de.yard.threed.flightgear.ecs;


import de.yard.threed.core.GeneralFunction;
import de.yard.threed.engine.Camera;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.AnimationComponent;
import de.yard.threed.engine.ecs.CameraProvider;
import de.yard.threed.engine.ecs.DefaultEcsComponent;
import de.yard.threed.engine.ecs.EcsComponent;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.scene.model.SGAnimation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 8.3.24: Now extends AnimationComponent from tcp22
 * 4.11.24: Why it is extending? Until we know why, no longer.
 * 14.5.25: This component is also used for updating tree properties of FG vehicle(aircraft) entities from outside FG. The PropertyTree passed in
 * is the entity/vehicle local tree, which differs from FG. Merged PropertyComponent.
 * Animation will/should use these properties.
 *
 *
 * <p>
 * Created by thomass on 29.12.16.
 */
public class FgAnimationComponent extends EcsComponent /*4.11.24 AnimationComponent*/ {
    public static String TAG = "FgAnimationComponent";
    // die animationlist wird sukzessive gefuellt pro submodel gefuellt.
    public List<SGAnimation> animationList;
    //PickAnimation z.B. needs a camera. Für VR ist das aber nicht hilfreich. Das duerfte verzichtbar sein. 26.11.19: Darum deprecated.
    //aber iregdnwo muss sie ja herkommen. Tja,...
    @Deprecated
    CameraProvider cameraProvider = null;
    // root node of entity/vehicle local tree
    public SGPropertyNode rootnode;
    private Map<String,GeneralFunction<Double, Void>> valueProviders = new HashMap();

    public static String C172P_SPD="fdm/jsbsim/velocities/vias-kts";
    public static String PROP_ENGINE0="/engines/engine[0]/rpm";
    //c172p also uses engine[2].rpm
    public static String PROP_ENGINE2="/engines/engine[2]/rpm";
    public static String PROP_ENGINE_RUNNING="/engines/engine/running";

    public FgAnimationComponent(SceneNode coreNode, List<SGAnimation> animationList, SGPropertyNode rootnode) {
        //super(coreNode);
        this.animationList = animationList;
        this.rootnode = rootnode;
    }

    //@Override
    public String getTag() {
        return TAG;
    }

    public Camera getCamera() {
        if (cameraProvider != null) {
            return cameraProvider.getCamera();
        }
        return null;
    }

    public void setCameraProvider(CameraProvider cameraProvider) {
        this.cameraProvider = cameraProvider;
    }

    public static FgAnimationComponent getFgAnimationComponent(EcsEntity e) {
        FgAnimationComponent gmc = (FgAnimationComponent) e.getComponent(FgAnimationComponent.TAG);
        return gmc;
    }

    /**
     * For example for railing (und c172p) asi needle.
     * @param property
     * @param valueProvider
     */
    public void addPropertySync(String property, GeneralFunction<Double, Void> valueProvider) {
        valueProviders.put(property, valueProvider);
    }

    /**
     * Transfer values from provider to property tree
     */
    public void syncProperties() {
        //this.speed = speed;
        for (String property : valueProviders.keySet()) {
            SGPropertyNode sn = rootnode.getNode(property, true);
            sn.setDoubleValue(valueProviders.get(property).handle(null));
        }
        /*if (speed > 0.001f){
            logger.debug("setSpeed "+speed+" in "+sn.getPath(true));
        }*/
    }

    public double getPropertyValue(String property) {
        SGPropertyNode sn = rootnode.getNode(property, false);
        return sn.getDoubleValue();
    }
}


