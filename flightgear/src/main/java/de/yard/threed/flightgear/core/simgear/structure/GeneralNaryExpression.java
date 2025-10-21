package de.yard.threed.flightgear.core.simgear.structure;

import java.util.ArrayList;
import java.util.List;

/**
 * An n-ary expression where the types of the argument aren't the
 * same as the return type.
 * 'abstract' removed because isn't abstract in FG(?)
 */
//template<typename T, typename OpType>
public /*abstract*/ class GeneralNaryExpression<T, OpType> extends SGExpression {
    private List</*SGSharedPtr<*/SGExpression/*<OpType>*/> _expressions=new ArrayList<>();

    public GeneralNaryExpression() {
    }

    GeneralNaryExpression(SGExpression/*<OpType>*/ expr0, SGExpression/*<OpType>*/ expr1) {
        addOperand(expr0);
        addOperand(expr1);
    }

    //typedef OpType operand_type;
    int getNumOperands() {
        return _expressions.size();
    }

    SGExpression/*<OpType>*/ getOperand(int i) {
        return _expressions.get(i);
    }

    /*SGExpression<OpType> getOperand(int i) {
        return _expressions.get(i);
    }*/

    public int addOperand(SGExpression/*<OpType>*/ expression) {
        if (expression == null)
            return -1;//~int(0);
        _expressions.add(expression);
        return _expressions.size() - 1;
    }

    // template<typename Iter>
    void addOperands(List<SGExpression> exprs/*Iter begin, Iter end*/) {
        // for (Iter iter = begin; iter != end; ++iter)
        for (SGExpression e : exprs) {

            addOperand(e/*static_cast< ::SGExpression<OpType>*>(*iter)*/);
        }
    }

    @Override
    public boolean isConst() {
        for (int i = 0; i < _expressions.size(); ++i)
            if (!_expressions.get(i).isConst())
                return false;
        return true;
    }

   /* @Override
    SGExpression simplify() {
        for (int i = 0; i < _expressions.size(); ++i)
            _expressions.get(i) = _expressions.get(i).simplify();
        return SGExpression<T>::simplify ();
    }*/

        /*simgear::expression::Type getOperandType()
        {
        return simgear::expression::TypeTraits<OpType>::typeTag;
        }*/
}
