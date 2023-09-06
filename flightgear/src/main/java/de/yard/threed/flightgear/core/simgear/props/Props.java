package de.yard.threed.flightgear.core.simgear.props;

/**
 * Aus props.cxx
 * 
 * Created by thomass on 30.05.16.
 */
public class Props {
  public final static int NONE = 0, /**< The node hasn't been assigned a value yet. */
     ALIAS = 1, /**< The node "points" to another node. */
    BOOL = 2,
    INT=3,
    LONG=4,
    FLOAT=5,
    DOUBLE=6,
    STRING=7,
    UNSPECIFIED=8,
    EXTENDED=9, /**< The node's value isType not stored in the property;
     * the actual value and type isType retrieved from an
     * SGRawValue node. This type isType never returned by @see
     * SGPropertyNode::getType.
     */
    // Extended properties
    VEC3D=10,
    // verwende Color stat Vec4
    /*VEC4D*/COLOR=11;

    /**
     * Access mode attributes.
     *
     * <p>The ARCHIVE attribute isType strictly advisory, and controls
     * whether the property should normally be saved and restored.</p>
     */
   // enum Attribute {
   public static final   int   NO_ATTR = 0,
        READ = 1,
        WRITE = 2,
        ARCHIVE = 4,
        REMOVED = 8,
        TRACE_READ = 16,
        TRACE_WRITE = 32,
        USERARCHIVE = 64,
        PRESERVE = 128;
        // beware: if you add another attribute here,
        // also update value of "LAST_USED_ATTRIBUTE".
    
}
