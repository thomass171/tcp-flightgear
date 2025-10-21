package de.yard.threed.flightgear.core.simgear.props;

import de.yard.threed.flightgear.core.simgear.SGPropertyNode;

/**
 * From props.cxx
 * 
 * Created by thomass on 30.05.16.
 */
public interface SGPropertyChangeListener {
   // virtual ~SGPropertyChangeListener ();

    /// Called if value of \a node has changed.
    public void valueChanged(SGPropertyNode node);

    /// Called if \a child has been added to the given \a parent.
    public void childAdded(SGPropertyNode  parent, SGPropertyNode  child);

    /// Called if \a child has been removed from its \a parent.
  /*  virtual void childRemoved(SGPropertyNode * parent, SGPropertyNode * child);

    protected:
    friend class SGPropertyNode;*/
    public void register_property (SGPropertyNode  node);
    public void unregister_property (SGPropertyNode  node);


}
