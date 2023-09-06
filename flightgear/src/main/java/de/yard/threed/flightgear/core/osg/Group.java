package de.yard.threed.flightgear.core.osg;

/**
 * 29.12.16: Child Nachbildung ueber Transform.
 * 
 * Created by thomass on 07.12.15.
 */
public class Group extends Node {
    
    /*public void addChild(Node model) {
        super.add(model);
    }*/

    /**
     * Wird mit Unity so nicht gehen. Brauchts die wirklich ? Irgendwie schon, darueber wird ermitelt,
     * ob ein Tile schon geladen wurde.
     * 29.12.16: Ueber transform nachbilden. Dann brauchts eigentlich auch die  Group nicht.
     * @return
     */
    @Deprecated
    public int getNumChildren	(		){
        return getTransform().getChildCount();
    }

    public void removeChildren(int i, int numChildren) {
        // TODO. braucht der TileMgr
        //Util.notyet();
    }
}
