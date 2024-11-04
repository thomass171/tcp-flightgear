package de.yard.threed.flightgear.ecs;


import de.yard.threed.engine.Camera;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.AnimationComponent;
import de.yard.threed.engine.ecs.CameraProvider;
import de.yard.threed.engine.ecs.DefaultEcsComponent;
import de.yard.threed.engine.ecs.EcsComponent;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.flightgear.core.simgear.scene.model.SGAnimation;

import java.util.List;

/**
 * 8.3.24: Now extends AnimationComponent from tcp22
 * 4.11.24: Why it is extending? Until we know why, no longer.
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

    public FgAnimationComponent(SceneNode coreNode, List<SGAnimation> animationList) {
        //super(coreNode);
        this.animationList = animationList;
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
}


