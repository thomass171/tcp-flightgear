package de.yard.threed.flightgear.core.simgear.structure;

/**
 * From SGExpression.hxx
 */
public class VariableBinding {
    /*std::*/ String name;
    /*expression::Type*/ ExpressionType type;
    public int location;

    public VariableBinding() {
        type = /*expression::*/new ExpressionType(ExpressionType.DOUBLE);
        location = -1;
    }

    public VariableBinding(String name_, /*expression::Type*/ExpressionType type_, int location_) {
        name = name_;
        type = type_;
        location = location_;
    }
}
