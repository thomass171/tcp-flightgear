package de.yard.threed.flightgear.core.simgear.structure;

import de.yard.threed.flightgear.core.simgear.SGPropertyNode;

import java.util.Set;

/**
 * Created by thomass on 28.12.16.
 */

// template<typename T>
public class SGPropertyExpression extends SGExpression {
    /*SGSharedPtr<*/ SGPropertyNode _prop;

    public SGPropertyExpression(ExpressionType type, SGPropertyNode prop) {
        _prop = prop;
        result_type = type;
    }

    public SGPropertyExpression(SGPropertyNode prop) {
        _prop = prop;
        // is double really a good idea?
        result_type = new ExpressionType(ExpressionType.DOUBLE);
    }

    void setPropertyNode(SGPropertyNode prop) {
        _prop = prop;
    }

    @Override
    public PrimitiveValue eval(/*T& Wert value,*/   Binding binding) {
        return doEval(/*value*/);
    }

    @Override
    public void collectDependentProperties(Set<SGPropertyNode> props) {
        props.add(_prop/*.get()*/);
    }

    private PrimitiveValue doEval(/*float& value*/) {
        return result_type.getValueFromProperty(_prop);
    }

/*private
        void doEval(float& value)  
        { if (_prop) value = _prop .getFloatValue(); }
        void doEval(double& value)  
        { if (_prop) value = _prop .getDoubleValue(); }
        void doEval(WertInt /*int&* / value)  
        { if (_prop!=null) value.intVal = _prop .getIntValue(); }
        void doEval(long& value)  
        { if (_prop) value = _prop .getLongValue(); }
        void doEval(bool& value)  
        { if (_prop) value = _prop .getBoolValue(); }*/

    @Override
    public String toString() {
        return "SGPropertyExpression " + _prop.getPath(true);
    }

}

