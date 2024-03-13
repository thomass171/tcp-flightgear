package de.yard.threed.flightgear.ecs;


import de.yard.threed.core.platform.NativeCollision;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Camera;
import de.yard.threed.engine.Input;
import de.yard.threed.core.Point;
import de.yard.threed.engine.Ray;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.ecs.DefaultEcsSystem;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsGroup;
import de.yard.threed.engine.vr.VrInstance;
import de.yard.threed.flightgear.core.simgear.scene.model.SGAnimation;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.RequestHandler;

import java.util.List;


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
     * Check for mouse or controller click.
     */
    @Override
    public void update(EcsEntity entity, EcsGroup group, double tpf) {
        Point mouselocation = Input.getMouseDown();

        Ray pickingray = null;
        if (mouselocation != null) {
            Camera camera = Scene.getCurrent().getDefaultCamera();
            pickingray = camera.buildPickingRay(camera.getCarrierTransform(), mouselocation);
        }
        if (Input.getControllerButtonDown(10)) {
            logger.debug(" found controller button down 10 (right)");
            pickingray = VrInstance.getInstance().getController(1).getRay();
        }

        // 13.3.24: No longer pass pickingray to any animation and do intersection check again and again. This is very inefficient.
        // Instead pass the objects hit.
        List<NativeCollision> intersections = null;
        if (pickingray != null) {
            intersections = pickingray.getIntersections();
        }
        FgAnimationComponent ac = (FgAnimationComponent) group.cl.get(0);
        for (int i = 0; i < ac.animationList.size(); i++) {
            SGAnimation a = ac.animationList.get((i));
            a.process(intersections, new AUSRequestHandler());
        }

    }

    @Override
    public String getTag() {
        return TAG;
    }
}

class AUSRequestHandler implements RequestHandler {
    @Override
    public boolean processRequest(Request request) {
        return false;
    }
}
