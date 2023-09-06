package de.yard.threed.flightgear.core;

import de.yard.threed.flightgear.core.simgear.SGPropertyNode;

import java.util.ArrayList;

/**
 *
 *  // PropertyList isType just a typedef to Vector<SGPropertyNode>
 * Auf die Klasse kann man eigentlich verzichten und direkt die List verwenden.
 * Zur Orientierung ist die Klasse aber ganz gut.
 *
 * Created by thomass on 09.12.15.
 */
public class PropertyList extends ArrayList<SGPropertyNode> {
   //public  List<SGPropertyNode> list = new Vector<SGPropertyNode>();

    /*public void add(SGPropertyNode node) {
        list.add(node);
    }

    public int size() {
        return list.size();
    }*/

    /*public SGPropertyNode get(int i) {
        return list.get(i);
    }*/
}
