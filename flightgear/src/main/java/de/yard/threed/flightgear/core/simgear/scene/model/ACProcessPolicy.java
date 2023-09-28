package de.yard.threed.flightgear.core.simgear.scene.model;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.flightgear.core.osg.Group;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.engine.platform.ProcessPolicy;

/**
 * Transformation from AC model coordinates (-x forward, y up, z right) to FG aircraft model coordinates (-x forward, y right, z up).
 * That is a switch of y and z axis. (Blender also does it this way).
 * The reason for negating y is unclear.
 *
 * Created by thomass on 14.09.16.
 */
public class ACProcessPolicy implements ProcessPolicy {
     Log logger = Platform.getInstance().getLog(ACProcessPolicy.class);

     public Matrix4 ac2fg = new Matrix4(1, 0, 0, 0,
             0, 0, -1, 0,
             0, 1, 0, 0,
             0, 0, 0, 1);

    public ACProcessPolicy(String extension) {

    }

    public SceneNode process(SceneNode node, String filename/*MA31, Options opt*/) {
        //logger.debug("process");
        Matrix4 m = new Matrix4(1, 0, 0, 0,
                0, 0, 1, 0,
                0, -1, 0, 0,
                0, 0, 0, 1);
        // XXX Does there need to be a Group node here to trick the
        // optimizer into optimizing the static transform?
        //TODO 3.1.18: Btauche ich wirklich die root node?
        Group root = new Group();
        Group transform = new Group();
        //MatrixTransform* transform = new MatrixTransform;
        root.attach(transform);

        //TODO transform->setDataVariance(Object::STATIC);
        //transform->setMatrix(m);
        // Ist das wirklich die gleiche Rotation wie oben? Der println hat das bestaetigt (OSG hat andere column order)
        // 17.11.16: Das kommt mir aber spanisch vor. -90 scheint eingängiger. Wenn man es aus der Matrix extrahiert (mit passender column order), zeigt Unity aber genau die 90 Grad an.
        // 90 passt auch alles in allem. 
        if (FlightGear.useosgcoordinatesystem){
            Quaternion rotation= Quaternion.buildFromAngles(new Degree(90),new Degree(0),new Degree(0));
            //rotation= new Quaternion(new Degree(-90),new Degree(0),new Degree(0));
            //in passender column order :

            rotation = (MathUtil2.extractQuaternion(ac2fg));
            transform.getTransform().setRotation(rotation);
        }
        transform.setName("ACProcessPolicy.transform node");
        //Matrix4 m = Matrix4.buildRotationMatrix(rotation);
        //System.out.println("m="+m.dump("\n"));
        transform.attach(node);
        root.setName("ACProcessPolicy.root node");
        return root;

    }
}
