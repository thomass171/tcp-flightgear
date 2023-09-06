package de.yard.threed.flightgear.core.osgdb;

import de.yard.threed.engine.SceneNode;
import de.yard.threed.flightgear.core.simgear.scene.model.SGAnimation;

import java.util.List;


/**
 * http://trac.openscenegraph.org/documentation/OpenSceneGraphReferenceDocs/a00681.html
 * 
 * 26.1.17: Ausgebaut fuer Animationen und auf Basis SceneNode.
 * 29.12.18: TODO: Was ist denn jetzt deprecated? ReadResult oder BuildResult? Wahrscheinlich BuildResult.
 * <p/>
 * Created by thomass on 07.12.15.
 */
public class ReadResult {
    public static ReadResult FILE_NOT_FOUND = new ReadResult("filenotfound");
    public static ReadResult FILE_NOT_HANDLED = new ReadResult("filenothandled");
    public static ReadResult ERROR_IN_READING_FILE = new ReadResult("errorinreadingfile");
    
    private SceneNode rootnode = null;
    private String msg;
    //FG DIFF
    public List<SGAnimation> animationList;
    
    public ReadResult(String msg) {
        this.msg = msg;
    }

    public ReadResult(SceneNode result) {
        rootnode = result;
        msg = "";
    }

    public ReadResult(SceneNode result, List<SGAnimation> animationList) {
        this(result);
        this.animationList=animationList;
    }

    public boolean validNode() {
        return rootnode != null;
    }

    public String message() {
        return msg;
    }


    public SceneNode getNode() {
        return rootnode;
    }
}
