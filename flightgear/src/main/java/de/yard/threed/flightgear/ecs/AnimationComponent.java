package de.yard.threed.flightgear.ecs;


import de.yard.threed.engine.Camera;
import de.yard.threed.engine.ecs.CameraProvider;
import de.yard.threed.engine.ecs.DefaultEcsComponent;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.flightgear.core.simgear.scene.model.SGAnimation;

import java.util.List;

/**
 * Created by thomass on 29.12.16.
 */
public class AnimationComponent extends DefaultEcsComponent {
    public static String TAG = "AnimationComponent";
    // die animationlist wird sukzessive gefuellt pro submodel gefuellt.
    public List<SGAnimation> animationList;
    //PickAnimation z.B. braucht die Camera. Für VR ist das aber nicht hilfreich. Das duerfte verzichtbar sein. 26.11.19: Darum deprecated.
    //aber iregdnwo muss sie ja herkommen. Tja,...
    @Deprecated
    CameraProvider cameraProvider = null;

    public AnimationComponent(List<SGAnimation> animationList) {
        this.animationList = animationList;
    }

    @Override
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
        this.cameraProvider=cameraProvider;
    }



    public static AnimationComponent getAnimationComponent(EcsEntity e) {
        AnimationComponent gmc = (AnimationComponent) e.getComponent(AnimationComponent.TAG);
        return gmc;
    }
}


