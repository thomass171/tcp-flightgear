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
 * Process FG animations.
 * 4.11.24: Renamed from just AnimationUpdateSystem to FgAnimationUpdateSystem to make clear it's for FG animations.
 * <p>
 * Created by thomass on 28.12.16.
 */
public class FgAnimationUpdateSystem extends DefaultEcsSystem {

    public FgAnimationUpdateSystem() {
        super(new String[]{"FgAnimationComponent"});
    }

    Log logger = Platform.getInstance().getLog(FgAnimationUpdateSystem.class);
    public static String TAG = "FgAnimationUpdateSystem";
    // intersections are cached per frame
    private List<NativeCollision> intersections = null;

    @Override
    public void init(EcsGroup group) {
        int z = 6;
    }

    @Override
    public void frameinit() {
        // Cache picking ray intersections for this frame instead of recalculating again and again when multiple entities are to be updated.

        //long startTime = Platform.getInstance().currentTimeMillis();
        Ray pickingray = null;

        Point mouselocation = Input.getMouseDown();
        /*if (mouselocation != null) {
            logger.debug("getMouseDown: total from start: " + (Platform.getInstance().currentTimeMillis() - startTime) + " ms");
        }*/

        if (mouselocation != null) {
            Camera camera = Scene.getCurrent().getDefaultCamera();
            pickingray = camera.buildPickingRay(camera.getCarrierTransform(), mouselocation);
            //logger.debug("buildPickingRay: total from start: " + (Platform.getInstance().currentTimeMillis() - startTime) + " ms");
        }
        if (Input.getControllerButtonDown(10)) {
            logger.debug(" found controller button down 10 (right)");
            pickingray = VrInstance.getInstance().getController(1).getRay();
        }
        // 13.3.24: No longer pass pickingray to any animation and do intersection check again and again. This is very inefficient.
        // Instead pass the objects hit.

        if (pickingray != null) {
            intersections = pickingray.getIntersections();
            // getIntersections already has its own performance warning
        }
    }

    /**
     * Check for mouse or controller click.
     */
    @Override
    public void update(EcsEntity entity, EcsGroup group, double tpf) {
        long startTime = Platform.getInstance().currentTimeMillis();

        FgAnimationComponent ac = (FgAnimationComponent) group.cl.get(0);
        for (int i = 0; i < ac.animationList.size(); i++) {
            SGAnimation a = ac.animationList.get((i));
            a.process(intersections, new AUSRequestHandler());
        }
        if (Platform.getInstance().currentTimeMillis() - startTime > 10) {
            logger.debug("update: total from start: " + (Platform.getInstance().currentTimeMillis() - startTime) + " ms");
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
