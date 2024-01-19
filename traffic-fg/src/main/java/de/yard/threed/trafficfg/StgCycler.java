package de.yard.threed.trafficfg;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Camera;
import de.yard.threed.engine.Input;
import de.yard.threed.engine.KeyCode;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.flightgear.core.osg.Group;
import de.yard.threed.flightgear.core.osg.PagedLOD;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;

import java.util.List;

/**
 * extrahiert aus SceneryViewerScene, um durch die Model bzw. Tiles des Terrain cyclen zu koennen. Einfacher Helper.
 */
public class StgCycler {
    private Log logger = Platform.getInstance().getLog(StgCycler.class);
    //-1, um beim 0ten zu beginnen
    public int currentobject = -1;
    public Group currentStgTile;
    Camera camera;

    public StgCycler(Camera camera) {
        this.camera = camera;
    }

    public void update() {
        if (Input.getKeyDown(KeyCode.N)) {
            int inc = 1;
            if (Input.getKey(KeyCode.Shift))
                inc = -1;
            cycleObject(inc);
        }
        if (Input.getKeyDown(KeyCode.T)) {
            int inc = 1;
            if (Input.getKey(KeyCode.Shift))
                inc = -1;
            cycleTile(inc);
        }
    }

    /**
     * Noch zu verbssern mit:
     * -gezielt suchen
     * -Leeds_Castle.ac gezielt anspringen, nur aus Interesse
     *
     * @param inc
     */
    private void cycleObject(int inc) {
        /*nordpolcheck();
        if (true)
            return;*/

        SceneNode stgGroupA;
        if (currentStgTile instanceof PagedLOD) {
            PagedLOD pagedLOD = (PagedLOD) currentStgTile;
            stgGroupA = pagedLOD.getTransform().getChild(1).getSceneNode();
        } else {
            // Dann einfach das erte gefundene
            List<NativeSceneNode> nodelist = Platform.getInstance().findSceneNodeByName("STG-group-A");
            logger.info("" + nodelist.size() + " STG-group-A nodes found. currentStgTile=" /*+ currentStgTile.getUniqueId()*/);
            if (nodelist.size() == 0) {
                return;
            }
            stgGroupA = new SceneNode(nodelist.get(0));
        }
        SceneNode sn;//= new SceneNode(nodelist.get(currentobject++).getChild(0));
        currentobject += inc;
        if (stgGroupA.getTransform().getChildCount() <= currentobject) {
            currentobject = 0;
        }
        if (currentobject < 0) {
            currentobject = stgGroupA.getTransform().getChildCount() - 1;
        }
        sn = stgGroupA.getTransform().getChild(currentobject).getSceneNode();

        Matrix4 worldmatrix = sn.getTransform().getWorldModelMatrix();
        SGGeod objposition = SGGeod.fromCart(worldmatrix.extractPosition());
        logger.info("current object: " + sn.getName() + "(" /*+ sn.getUniqueId() */ + ")" + ", pos=" + objposition);

        // Camera 200m hoeher, ca. 300m suedlicher und Blick Richtung Norden aufs Objekt. Geht so nicht auf Südkugel TODO
        objposition.setElevationM(objposition.getElevationM() + 200);
        objposition.setLatitudeRad(objposition.getLatitudeRad() - 0.000047124);
        //Camera camera = getDefaultCamera();
        camera.getCarrier().getTransform().setPosition(objposition.toCart());
        // detach nur sicherheitshalber, falls Camera attached war.
        //MA29 camera.detach();

        // Nach unter blicken, und dann 90 Grad nach oben aufrichten
        Quaternion rotation;
        rotation = new FgCalculations().buildRotation(objposition);
        rotation = rotation.multiply(Quaternion.buildFromAngles(new Degree(90), new Degree(0), new Degree(0)));
        
        /* Folgende Berechnugn ist irgendwie schief
        rotation = OsgMath.makeZUpFrame(objposition);
        float heading = 0;
        float pitch = 90;
        float roll = 0;
        rotation = rotation.multiply(new Quaternion(new Degree(heading), new Degree(pitch), new Degree(roll)));
        */

        camera.getCarrier().getTransform().setRotation(rotation);
    }

    /**
     * 21.12.17:Ich weiss gar nicht, ob das noch geht und wie es genau gedacht war. Warum loadBucket hier?
     *
     * @param inc
     */
    private void cycleTile(int inc) {
        /*tileindex += inc;
        if (tileindex >= tiles.length) {
            tileindex = 0;
        }
        if (tileindex < 0) {
            tileindex = tiles.length - 1;
        }
        SGGeod pos = tiles[tileindex];
        currentbucket = new SGBucket(pos.getLongitudeDeg(), pos.getLatitudeDeg());
        loadbucket();*/
    }

}
