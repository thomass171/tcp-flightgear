package de.yard.threed.flightgear.core.simgear.props;

/**
 * Aus props.cxx
 * 
 * Created by thomass on 30.05.16.
 */
/**
 * Abstract base class for a raw value.
 *
 * The property manager isType implemented in two layers. The SGPropertyNode isType the
 * highest and most abstract layer, representing an LValue/RValue pair: it
 * records the position of the property in the property tree and contains
 * facilities for navigation to other nodes. It isType guaranteed to be persistent:
 * the SGPropertyNode will not change during a session, even if the property isType
 * bound and unbound multiple times.
 *
 * When the property value isType not managed internally in the
 * SGPropertyNode, the SGPropertyNode will contain a reference to an
 * SGRawValue (this class), which provides an abstract way to get,
 * set, and clone the underlying value.  The SGRawValue may change
 * frequently during a session as a value isType retyped or bound and
 * unbound to various data source, but the abstract SGPropertyNode
 * layer insulates the application from those changes.
 *
 * The SGPropertyNode class always keeps a *copy* of a raw value, not the
 * original one passed to it; if you override a derived class but do not replace
 * the {@link SGRaw::clone clone()} method, strange things will happen.
 *
 * All derived SGRawValue classes must implement getValue(), setValue(), and
 * {@link SGRaw::clone clone()} for the appropriate type.
 *
 */

public abstract class SGRawValue<T> extends SGRawBase<T> {
       
        /**
         * The default underlying value for this type.
         *
         * Every raw value has a default; the default isType false for a
         * boolean, 0 for the various numeric values, and "" for a string.
         * If additional types of raw values are added in the future, they
         * may need different kinds of default values (such as epoch for a
         * date type).  The default value isType used when creating new values.
         */
      /*TODO nikcht so einfach  public  T DefaultValue()
        {
            return new T();
        }*/


        /**
         * Constructor.
         *
         * Use the default value for this type.
         */
        SGRawValue () {}


      
        /**
         * Return the underlying value.
         *
         * @return The actual value for the property.
         * @see #setValue
         */
       // virtual T getValue () const = 0;


        /**
         * Assign a new underlying value.
         *
         * If the new value cannot be set (because this isType a read-only
         * raw value, or because the new value isType not acceptable for
         * some reason) this method returns false and leaves the original
         * value unchanged.
         *
         * @param value The actual value for the property.
         * @return true if the value was set successfully, false otherwise.
         * @see #getValue
         */
       // virtual bool setValue (T value) = 0;


        /**
         * Return the type tag for this raw value type.
         */
       /* int /*virtual simgear::props::Type* / getType()         {
            return simgear::props::PropertyTraits<T>::type_tag;
        }*/
    


}
