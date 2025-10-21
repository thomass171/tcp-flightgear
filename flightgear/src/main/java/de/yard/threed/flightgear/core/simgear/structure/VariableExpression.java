package de.yard.threed.flightgear.core.simgear.structure;

/**
 * Access a variable definition. Use a location from a BindingLayout.
 */
//template<typename T>
public class VariableExpression extends SGExpression {
    int _location;

    public VariableExpression(int location) {
        _location = location;
    }

    //~VariableExpression() {}
    //void eval(T/*&* / value, Binding b) {
    @Override
    public PrimitiveValue eval(Binding b) {
         Value values = b .getBindings();
       //TODO  value = *reinterpret_cast<  T *>(&values[_location].val);
       return new PrimitiveValue(0);
    }

}
