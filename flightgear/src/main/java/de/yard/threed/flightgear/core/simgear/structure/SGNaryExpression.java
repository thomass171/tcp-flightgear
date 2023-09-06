package de.yard.threed.flightgear.core.simgear.structure;

import de.yard.threed.flightgear.core.simgear.SGPropertyNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by thomass on 28.12.16.
 */
//template<typename T>
public abstract class SGNaryExpression extends SGExpression {
    List<SGExpression> _expressions = new ArrayList<SGExpression>();

    SGNaryExpression(SGExpression expr0, SGExpression expr1) {
        addOperand(expr0);
        addOperand(expr1);
    }

    public SGNaryExpression() {
        
    }

    int addOperand(SGExpression expression) {
        if (expression == null)
            return -1;//~int(0);
        _expressions.add(expression);
        return _expressions.size() - 1;
    }

    
    /*@Override
    SGExpression    simplify() {
        _expression = _expression.simplify();
        return SGExpression<T>::simplify ();
    }*/


    public int getNumOperands() {
        return _expressions.size();
    }

    SGExpression getOperand(int i) {
        return _expressions.get(i);
    }


    //template<typename Iter>
    /*void addOperands(Iter begin, Iter end) {
        for (Iter iter = begin; iter != end; ++iter) {
            addOperand(static_cast <::SGExpression < T >*>( * iter));
        }
    }*/

    @Override
    public boolean isConst() {
        for (int i = 0; i < _expressions.size(); ++i)
            if (!_expressions.get(i).isConst())
                return false;
        return true;
    }

   /*@Override
   public   SGExpression simplify() {
        for (int i = 0; i < _expressions.size(); ++i)
            _expressions.set(i,_expressions.get(i).simplify());
        return SGExpression<T>::simplify ();
    }*/

    @Override
    public void collectDependentProperties(Set<SGPropertyNode> props) {
        for (int i = 0; i < _expressions.size(); ++i)
            _expressions.get(i).collectDependentProperties(props);
    }


}
