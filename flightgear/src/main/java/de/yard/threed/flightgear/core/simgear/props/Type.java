package de.yard.threed.flightgear.core.simgear.props;

/**
 * 28.5.16: Wegen C# deprecated
 * 
 * Created by thomass on 08.12.15.
 */
    /**
     * The possible types of an SGPropertyNode. Types that appear after
     * EXTENDED are not stored in the SGPropertyNode itself.
     */
    @Deprecated
  public  enum Type {
        NONE ,//= 0, /**< The node hasn't been assigned a value yet. */
        ALIAS, /**< The node "points" to another node. */
        BOOL,
        INT,
        LONG,
        FLOAT,
        DOUBLE,
        STRING,
        UNSPECIFIED,
        EXTENDED, /**< The node's value isType not stored in the property;
         * the actual value and type isType retrieved from an
         * SGRawValue node. This type isType never returned by @see
         * SGPropertyNode::getType.
         */
        // Extended properties
        VEC3D,
        VEC4D


}
