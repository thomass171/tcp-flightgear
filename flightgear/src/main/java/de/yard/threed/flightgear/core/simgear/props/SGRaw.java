package de.yard.threed.flightgear.core.simgear.props;

/**
 * Aus props.hxx
 * 
 * Created by thomass on 30.05.16.
 */

import de.yard.threed.core.Util;

/**
 * Base class for SGRawValue classes that holds no type
 * information. This allows some generic manipulation of the
 * SGRawValue object.
 */
public abstract class SGRaw {
        /**
         * Get the type enumeration for the raw value.
         *
         * @return the type.
         */
     //  abstract int getType();
  
        /**
         * Create a new deep copy of this raw value.
         *
         * The copy will contain its own version of the underlying value
         * as well, and will be the same type.
         *
         * @return A deep copy of the current object.
         */
        //TODO virtual SGRaw* clone() const = 0;

    /**
     * Hier weil er in SGRawBase Probleme mit C# Generics macht.
     * @return
     */
        public int getType(){
            Util.notyet();
            return 0;
        }

}
