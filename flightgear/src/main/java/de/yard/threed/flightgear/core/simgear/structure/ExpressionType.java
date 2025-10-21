package de.yard.threed.flightgear.core.simgear.structure;

import de.yard.threed.core.Util;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;

import java.util.List;

/**
 * From SGExpression.hxx
 *
 * Is 'expression::Type' in FG.
 */
public class ExpressionType {
    // The numeric order is important for deciding about conversion options, the more lower the more convertable,
    // BOOL can be converted to INT, INT to FLOAT a.s.o
    public static final int BOOL = 1,
            INT = 2,
            FLOAT = 3,
            DOUBLE = 4;
    int type;

    public ExpressionType(int type) {
        this.type = type;
    }

    public PrimitiveValue getValueFromProperty(SGPropertyNode prop) {
        switch (type) {
            case DOUBLE:
                return new PrimitiveValue(prop.getDoubleValue());
        }
        //TODO logger warn?
        return null;
    }

    public PrimitiveValue buildDefaultValue() {
        switch (type) {
            case DOUBLE:
                return new PrimitiveValue(0.0);
        }
        Util.notyet();
        return null;
    }
}