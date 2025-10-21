package de.yard.threed.flightgear.core.simgear.structure;

import de.yard.threed.core.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * From SGExpression.hxx
 */
public class BindingLayout {
    // Apparently never inited in FG(??)
    List<VariableBinding> bindings = new ArrayList<>();

    public int addBinding(String name, ExpressionType type) {
        //XXX error checkint
       /* vector<VariableBinding>::iterator itr
                = find_if(bindings.begin(), bindings.end(),
                boost::bind(&VariableBinding::name, _1) == name);*/
        VariableBinding itr = findBinding(name);
        //if (itr != bindings.end())
        if (itr != null) {
            return itr.location;
        }
        int result = bindings.size();
        bindings.add/*push_back*/(new VariableBinding(name, type, bindings.size()));
        return result;
    }

    /*boolean*/
    public VariableBinding findBinding(String name/*, VariableBinding result*/) {
        //using namespace std;
        // using namespace boost;
        // vector<VariableBinding>::const_iterator itr
        //       = find_if(bindings.begin(), bindings.end(),
        //     boost::bind(&VariableBinding::name, _1) == name);
        // boost::bind is a kind of lambda??
        for (VariableBinding b : bindings) {
            if (name.equals(b.name)) {
                return b;
            }
        }
        /*if (itr != bindings.end()) {
            result = *itr;
            return true;
        } else {
            return false;
        }*/
        return null;//false;
    }


}
