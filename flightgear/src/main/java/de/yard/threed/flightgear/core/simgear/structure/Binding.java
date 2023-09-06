package de.yard.threed.flightgear.core.simgear.structure;

/**
 * Created by thomass on 05.01.17.
 */
// Support for binding variables around an expression.
public abstract class Binding {
    // ~Binding() {}
    abstract Value getBindings();//   =0;
    /*Value*    getBindings() =0;*/
}
