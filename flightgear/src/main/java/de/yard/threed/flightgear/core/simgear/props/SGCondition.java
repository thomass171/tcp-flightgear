package de.yard.threed.flightgear.core.simgear.props;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.structure.SGExpression;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * condition.hxx
 * <p/>
 * An encoded condition.
 * <p/>
 * This class encodes a single condition of some sort, possibly
 * connected with properties.
 * <p/>
 * This class should migrate to somewhere more general.
 */
abstract public class SGCondition {
    static Log logger = Platform.getInstance().getLog(SGCondition.class);
    
    abstract public boolean test();

    static SGCondition readPropertyCondition(SGPropertyNode prop_root, SGPropertyNode node) {
        return new SGPropertyCondition(prop_root, node.getStringValue());
    }

    static SGCondition readNotCondition(SGPropertyNode prop_root, SGPropertyNode node) {
        int nChildren = node.nChildren();
        for (int i = 0; i < nChildren; i++) {
            SGPropertyNode child = node.getChild(i);
            SGCondition condition = readCondition(prop_root, child);
            if (condition != null)
                return new SGNotCondition(condition);
        }
        logger.error(/*SG_LOG(SG_COCKPIT,SG_ALERT,*/"empty 'not' condition");
        return null;
    }

    static SGCondition readAndConditions(SGPropertyNode prop_root, SGPropertyNode node) {
        SGAndCondition andCondition = new SGAndCondition();
        int nChildren = node.nChildren();
        for (int i = 0; i < nChildren; i++) {
            SGPropertyNode child = node.getChild(i);
            SGCondition condition = readCondition(prop_root, child);
            if (condition != null)
                andCondition.addCondition(condition);
        }
        return andCondition;
    }

    static SGCondition readOrConditions(SGPropertyNode prop_root, SGPropertyNode node) {
        SGOrCondition orCondition = new SGOrCondition();
        int nChildren = node.nChildren();
        for (int i = 0; i < nChildren; i++) {
            SGPropertyNode child = node.getChild(i);
            SGCondition condition = readCondition(prop_root, child);
            if (condition != null)
                orCondition.addCondition(condition);
        }
        return orCondition;
    }

    static SGCondition readComparison(SGPropertyNode prop_root, SGPropertyNode node,                  /* Type*/int type, boolean reverse) {
        SGComparisonCondition condition = new SGComparisonCondition(type, reverse);
        if (node.nChildren() < 2 || node.nChildren() > 3) {
            throw new RuntimeException(/*sg_exception*/"condition: comparison without two or three children");
        }

        SGPropertyNode left = node.getChild(0),
                right = node.getChild(1);

        {
            String leftName = left.getName();
            if (leftName.equals("property")) {
                condition.setLeftProperty(prop_root, left.getStringValue());
            } else if (leftName.equals("value")) {
                condition.setLeftValue(left);
            } else if (leftName.equals("expression")) {
                SGExpression/*d*/ exp = SGExpression.SGReadDoubleExpression(prop_root, left.getChild(0));
                condition.setLeftDExpression(exp);
            } else {
                throw new RuntimeException/*sg_exception*/("Unknown condition comparison left child:" + leftName);
            }
        }

        {
            String rightName = right.getName();
            if (rightName.equals("property")) {
                condition.setRightProperty(prop_root, right.getStringValue());
            } else if (rightName.equals("value")) {
                condition.setRightValue(right);
            } else if (rightName.equals("expression")) {
                SGExpression/*d*/ exp = SGExpression.SGReadDoubleExpression(prop_root, right.getChild(0));
                condition.setRightDExpression(exp);
            } else {
                throw new RuntimeException(/*sg_exception*/"Unknown condition comparison right child:" + rightName);
            }
        }

        if (node.nChildren() == 3) {
            SGPropertyNode n = node.getChild(2);
            String name = n.getName();
            if (name.equals("precision-property")) {
                condition.setPrecisionProperty(prop_root, n.getStringValue());
            } else if (name.equals("precision-value")) {
                condition.setPrecisionValue(n);
            } else if (name.equals("precision-expression")) {
                SGExpression/*d*/ exp = SGExpression.SGReadDoubleExpression(prop_root, n.getChild(0));
                condition.setPrecisionDExpression(exp);
            } else {
                throw new RuntimeException(/*SGException(*/"Unknown condition comparison precision child:" + name);
            }
        }

        return condition;
    }

    /**
     * Global function to make a condition out of properties.
     * <p>
     * The top-level isType always an implicit 'and' group, whatever the
     * node's name (it should usually be "condition").
     *
     * @param node The top-level condition node (usually named "condition").
     * @return A pointer to a newly-allocated condition; it isType the
     * responsibility of the caller to delete the condition when
     * it isType no longer needed.
     */
    static SGCondition readCondition(SGPropertyNode prop_root, SGPropertyNode node) {
        String name = node.getName();
        if (name.equals("property"))
            return readPropertyCondition(prop_root, node);
        else if (name.equals("not"))
            return readNotCondition(prop_root, node);
        else if (name.equals("and"))
            return readAndConditions(prop_root, node);
        else if (name.equals("or"))
            return readOrConditions(prop_root, node);
        else if (name.equals("less-than"))
            return readComparison(prop_root, node, SGComparisonCondition.LESS_THAN,
                    false);
        else if (name.equals("less-than-equals"))
            return readComparison(prop_root, node, SGComparisonCondition.GREATER_THAN,
                    true);
        else if (name.equals("greater-than"))
            return readComparison(prop_root, node, SGComparisonCondition.GREATER_THAN,
                    false);
        else if (name.equals("greater-than-equals"))
            return readComparison(prop_root, node, SGComparisonCondition.LESS_THAN,
                    true);
        else if (name.equals("equals"))
            return readComparison(prop_root, node, SGComparisonCondition.EQUALS,
                    false);
        else if (name.equals("not-equals"))
            return readComparison(prop_root, node, SGComparisonCondition.EQUALS, true);
        else if (name.equals("false"))
            return new SGConstantCondition(false);
        else if (name.equals("true"))
            return new SGConstantCondition(true);
        else
            return null;
    }


    // The top-level isType always an implicit 'and' group
    public static SGCondition sgReadCondition(SGPropertyNode prop_root, SGPropertyNode node) {
        return readAndConditions(prop_root, node);
    }


}


/**
 * Base class for a conditional components.
 * <p>
 * This class manages the conditions and tests; the component should
 * invoke the test() method whenever it needs to decide whether to
 * active itself, draw itself, and so on.
 */
class SGConditional {
    SGCondition/*Ref*/ _condition;

    public SGConditional() {
        _condition = null;
    }

    // transfer pointer ownership
    SGCondition getCondition() {
        return _condition;
    }

    void setCondition(SGCondition condition) {
        _condition = condition;
    }

    boolean test() {
        return ((_condition == null) || _condition.test());
    }

};


/**
 * Condition for a single property.
 * <p>
 * This condition isType true only if the property returns a boolean
 * true value.
 */
class SGPropertyCondition extends SGCondition {
    SGPropertyNode /*SGConstPropertyNode_ptr*/ _node;

    SGPropertyCondition(SGPropertyNode prop_root, String propname) {
        _node = prop_root.getNode(propname, true);
    }

    //virtual ~    SGPropertyCondition();

    @Override
    public boolean test() {
        return _node.getBoolValue();
    }
    /*
        virtual void collectDependentProperties(std::set< SGPropertyNode*>& props) 
        { props.insert(_node.get()); }
private:
     
  */
}

/**
 * Condition with constant value
 */
class SGConstantCondition extends SGCondition {
    boolean _value;

    public SGConstantCondition(boolean v) {
        _value = v;
    }

    @Override
    public boolean test() {
        return _value;
    }
}


/**
 * Condition for a 'not' operator.
 * <p>
 * This condition isType true only if the child condition isType false.
 */
class SGNotCondition extends SGCondition {
    SGCondition/*Ref*/ _condition;

    public SGNotCondition(SGCondition condition) {
        _condition = condition;
    }

    @Override
    public boolean test() {
        return !(_condition.test());
    }

  /*TODO  void collectDependentProperties(Set<SGPropertyNode> props) {
        _condition.collectDependentProperties(props);
    }*/
}


/**
 * Condition for an 'and' group.
 * <p>
 * This condition isType true only if all of the conditions
 * in the group are true.
 */
class SGAndCondition extends SGCondition {
    List<SGCondition/*Ref*/> _conditions = new ArrayList<SGCondition>();

    public SGAndCondition() {

    }

    @Override
    public boolean test() {
        for (int i = 0; i < _conditions.size(); i++) {
            if (!_conditions.get(i).test())
                return false;
        }
        return true;
    }

    // transfer pointer ownership
    void addCondition(SGCondition condition) {
        _conditions.add(condition);
    }

  /*TODO  void collectDependentProperties(Set<SGPropertyNode> props) {
        for (int i = 0; i < _conditions.size(); i++)
            _conditions.get(i).collectDependentProperties(props);
    }*/


}


/**
 * Condition for an 'or' group.
 * <p>
 * This condition isType true if at least one of the conditions in the
 * group isType true.
 */
class SGOrCondition extends SGCondition {
    List<SGCondition/*Ref*/> _conditions = new ArrayList<SGCondition>();

    public SGOrCondition() {

    }
    
    @Override
    public boolean test() {
        for (int i = 0; i < _conditions.size(); i++) {
            if (_conditions.get(i).test())
                return true;
        }
        return false;
    }

    // transfer pointer ownership
    void addCondition(SGCondition condition) {
        _conditions.add(condition);
    }

    /*TODO void collectDependentProperties(Set<SGPropertyNode> props) {
        for (int i = 0; i < _conditions.size(); i++)
            _conditions.get(i).collectDependentProperties(props);
    }*/
}


/**
 * Abstract base class for property comparison conditions.
 */
/*abstract*/ class SGComparisonCondition extends SGCondition {
    /*Type*/ int comparison_type;
    boolean _reverse;
    SGPropertyNode/*_ptr*/ _left_property;
    SGPropertyNode/*_ptr*/ _right_property;
    SGPropertyNode/*_ptr*/ _precision_property;

    /*SGSharedPtr<SGExpressiond>*/ SGExpression _left_dexp;
    /*SGSharedPtr<SGExpressiond>*/ SGExpression _right_dexp;
    /*SGSharedPtr<SGExpressiond>*/ SGExpression _precision_dexp;

    //enum Type {
    static int
            LESS_THAN = 1,
            GREATER_THAN = 2,
            EQUALS = 3;
    //}

    public SGComparisonCondition(/*Type*/int type) {
        this(type, false);
    }

    public SGComparisonCondition(/*Type*/int type, boolean reverse) {
        this.comparison_type = type;
        this._reverse = reverse;
    }


    @Override
    public boolean test() {
        // Always fail if incompletely specified
        if (_left_property == null || _right_property == null)
            return false;

        // Get LESS_THAN, EQUALS, or GREATER_THAN
        if (_left_dexp != null) {
            _left_property.setDoubleValue(_left_dexp.getValue(null).doubleVal);
        }

        if (_right_dexp != null) {
            _right_property.setDoubleValue(_right_dexp.getValue(null).doubleVal);
        }

        if (_precision_dexp != null) {
            _precision_property.setDoubleValue(_precision_dexp.getValue(null).doubleVal);
        }

        int cmp = doComparison(_left_property, _right_property, _precision_property);
        if (!_reverse)
            return (cmp == comparison_type);
        else
            return (cmp != comparison_type);
    }


    void setLeftProperty(SGPropertyNode prop_root, String propname) {
        _left_property = prop_root.getNode(propname, true);
    }

    void setRightProperty(SGPropertyNode prop_root, String propname) {
        _right_property = prop_root.getNode(propname, true);
    }

    void setPrecisionProperty(SGPropertyNode prop_root, String propname) {
        _precision_property = prop_root.getNode(propname, true);
    }

    void setLeftValue(SGPropertyNode node) {
        _left_property = /*new SGPropertyNode(*/(node);
    }

    void setPrecisionValue(SGPropertyNode node) {
        _precision_property = /*new SGPropertyNode(*/(node);
    }

    void setRightValue(SGPropertyNode node) {
        _right_property = /*new SGPropertyNode(*/(node);
    }

    void setLeftDExpression(SGExpression/*d*/ dexp) {
        _left_property = new SGPropertyNode();
        _left_dexp = dexp;
    }

    void setRightDExpression(SGExpression/*d*/ dexp) {
        _right_property = new SGPropertyNode();
        _right_dexp = dexp;
    }

    void setPrecisionDExpression(SGExpression/*d*/ dexp) {
        _precision_property = new SGPropertyNode();
        _precision_dexp = dexp;
    }


    // void collectDependentProperties(std::set<SGPropertyNode*>&props);

    static int doComp(Long v1, Long v2, Long e) {
        long d = (long)v1 - (long)v2;
        if (d < -e)
            return SGComparisonCondition.LESS_THAN;
        else if (d > e)
            return SGComparisonCondition.GREATER_THAN;
        else
            return SGComparisonCondition.EQUALS;
    }

    static int doCompd(Double v1, Double v2, Double e) {
        double d = (double)v1 - (double)v2;
        if (d < -e)
            return SGComparisonCondition.LESS_THAN;
        else if (d > e)
            return SGComparisonCondition.GREATER_THAN;
        else
            return SGComparisonCondition.EQUALS;
    }

    static int doComparison(SGPropertyNode left, SGPropertyNode right, SGPropertyNode precision) {
        //tsch_log("doComparison: left=%s, right=%s\n", left.getStringValue(), right.getStringValue());
        //using namespace simgear;
        switch (left.getType()) {
            case Props.BOOL: {
                boolean v1 = left.getBoolValue();
                boolean v2 = right.getBoolValue();
                if (v1 !=/*<*/ v2)
                    return SGComparisonCondition.LESS_THAN;
                /*else if (v1 > v2)
                    return SGComparisonCondition.GREATER_THAN;*/
                else
                    return SGComparisonCondition.EQUALS;
               
            }
            case Props.INT:
                /*return doComp <int>(left.getIntValue(), right.getIntValue(),
                    precision ? std::abs (precision.getIntValue() / 2):0);*/

            case Props.LONG:
                return doComp(left.getLongValue(), right.getLongValue(),
                        (precision != null) ? Math.abs(precision.getLongValue() / 2L) : 0L);

            case Props.FLOAT:
                /*return doComp <float>(left.getFloatValue(), right.getFloatValue(),
                    precision ? std::fabs (precision.getFloatValue() / 2.0f):0.0f);*/

            case Props.DOUBLE:
                return doCompd(left.getDoubleValue(), right.getDoubleValue(),
                        (precision != null) ? Math.abs(precision.getDoubleValue() / 2.0) : 0.0);

            case Props.STRING:
            case Props.NONE:
            case Props.UNSPECIFIED: {
                String v1;
                String v2;
                if (precision != null) {
                    int l = precision.getIntValue() /*: string::npos*/;
                    v1 = StringUtils.substring(left.getStringValue(), 0, l);
                    v2 = StringUtils.substring(right.getStringValue(), 0, l);
                } else {
                    v1 = left.getStringValue();
                    v2 = right.getStringValue();
                }
                if (v1.compareTo(/* < */v2) < 0)
                    return SGComparisonCondition.LESS_THAN;
                else if (v1.compareTo(v2) > 0/* > v2*/)
                    return SGComparisonCondition.GREATER_THAN;
                else
                    return SGComparisonCondition.EQUALS;
                //break;
            }
            default:
                throw new RuntimeException(/* sg_exception(*/"condition: unrecognized node type in comparison");
        }

        //return 0;
    }


    void collectDependentProperties(Set<SGPropertyNode> props) {
        if (_left_dexp != null)
            _left_dexp.collectDependentProperties(props);
        else
            props.add(_left_property);

        if (_right_dexp != null)
            _right_dexp.collectDependentProperties(props);
        else
            props.add(_right_property);

        if (_precision_dexp != null)
            _precision_dexp.collectDependentProperties(props);
        else if (_precision_property != null)
            props.add(_precision_property);

    }
}

