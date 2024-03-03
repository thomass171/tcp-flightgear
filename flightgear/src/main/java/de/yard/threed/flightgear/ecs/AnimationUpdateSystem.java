package de.yard.threed.flightgear.ecs;


import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Input;
import de.yard.threed.core.Point;
import de.yard.threed.engine.Ray;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.avatar.Avatar;
import de.yard.threed.engine.avatar.AvatarSystem;
import de.yard.threed.engine.ecs.DefaultEcsSystem;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsGroup;
import de.yard.threed.engine.vr.VrInstance;
import de.yard.threed.flightgear.core.simgear.scene.model.SGAnimation;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.RequestHandler;


/**
 * Created by thomass on 28.12.16.
 */
public class AnimationUpdateSystem extends DefaultEcsSystem {
    public AnimationUpdateSystem() {
        super(new String[]{"AnimationComponent"});
    }
    Log logger = Platform.getInstance().getLog(AnimationUpdateSystem.class);
    public static String TAG = "AnimationUpdateSystem";

    @Override
    public void init(EcsGroup group) {
        int z = 6;
    }

    /**
     * Auf Mouse oder Controller Click pruefen.
     */
    @Override
    public void update(EcsEntity entity, EcsGroup group, double tpf) {
        Point mouselocation = Input.getMouseDown();

        //Avatar avatar = /*1.4.21 Player.getInstance()*/AvatarSystem.getAvatar();
        Ray pickingray = null;
        //Die Camera aus AnimationComponentn zu holen ist zwar doof, aber iregdnwo muss sie ja herkommen. Tja,...
        if (/*avatar != null && */mouselocation != null) {
            pickingray = Scene.getCurrent().getDefaultCamera().buildPickingRay(Scene.getCurrent().getDefaultCamera().getCarrierTransform(),mouselocation);
        }
        if (/*avatar != null &&*/ Input.getControllerButtonDown(10)) {
            logger.debug(" found controller button down 10 (right)");
            pickingray = VrInstance.getInstance().getController(1).getRay();
        }

        AnimationComponent ac = (AnimationComponent) group.cl.get(0);
        for (int i = 0; i < ac.animationList.size(); i++) {
            SGAnimation a = ac.animationList.get((i));
            /*Ray pickingray = null;
            if (ac.getCamera() != null && mouselocation != null) {
                pickingray = ac.getCamera().buildPickingRay(ac.getCamera().getCarrier().getTransform(),mouselocation);
            }*/
            a.process(pickingray, new AUSRequestHandler());
        }

    }

    @Override
    public String getTag() {
        return TAG;
    }
}

class AUSRequestHandler implements RequestHandler{
    @Override
    public boolean processRequest(Request request) {
        return false;
    }
}
