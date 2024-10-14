package de.yard.threed.flightgear.core.simgear.scene.model;

import de.yard.threed.core.Degree;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.platform.ProcessPolicy;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.osg.Group;

/**
 * Transformation from OpenGL model coordinates (x right, y up, -z forward) to FG aircraft model coordinates (-x forward, y right, z up).
 * This is probably just a convention, especially whether to negate forward.
 * It depends on the use case viewing/camera or model.
 *
 * Created by thomass on 28.09.23.
 */
public class OpenGlProcessPolicy implements ProcessPolicy {
     Log logger = Platform.getInstance().getLog(OpenGlProcessPolicy.class);
private Matrix4 mat;

     public Matrix4 opengl2fg = new Matrix4(
             0, 0, 1, 0,
             1, 0, 0, 0,
             0, 1, 0, 0,
             0, 0, 0, 1);

    public OpenGlProcessPolicy(String extension) {
        mat = opengl2fg;
    }

    /**
     * Hack for FgGalleryScene. Apparently this is just the identity(??).
     */
    public OpenGlProcessPolicy(boolean xml2opengl) {
        mat = new Matrix4(
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1);
    }

    public Matrix4 fg2opengl(){
        return opengl2fg.getInverse();
    }

    public SceneNode process(SceneNode node, String filename/*MA31, Options opt*/) {
        return ACProcessPolicy.applyPolicy(node, mat);
    }
}
