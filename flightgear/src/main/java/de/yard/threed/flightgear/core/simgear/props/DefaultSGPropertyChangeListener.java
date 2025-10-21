package de.yard.threed.flightgear.core.simgear.props;

import de.yard.threed.flightgear.core.simgear.SGPropertyNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper to relieve implementing classes to implement all methods.
 * Not needed in FG C++?
 *
 * Created by thomass on 30.05.16.
 */
public class DefaultSGPropertyChangeListener implements SGPropertyChangeListener {
    List<SGPropertyNode> _properties;
    
   // virtual ~SGPropertyChangeListener ();

    /// Called if value of \a node has changed.
    public void valueChanged(SGPropertyNode node){}

    /// Called if \a child has been added to the given \a parent.
    public void childAdded(SGPropertyNode parent, SGPropertyNode child){}

    /// Called if \a child has been removed from its \a parent.
  /*  virtual void childRemoved(SGPropertyNode * parent, SGPropertyNode * child);
*/
     public void register_property (SGPropertyNode  node){
         if (_properties == null){
             _properties=new ArrayList<SGPropertyNode>();
         }
         _properties.add(node);
     }
    public void unregister_property (SGPropertyNode  node){
        _properties.remove(node);
    }

}
