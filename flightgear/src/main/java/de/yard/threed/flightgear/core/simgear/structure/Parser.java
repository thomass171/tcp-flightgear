package de.yard.threed.flightgear.core.simgear.structure;

import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.props.Props;

import java.util.List;

/**
 * From SGExpression.hxx
 */
public abstract class Parser {
    // Apparently never inited in FG(??)
    protected BindingLayout _bindingLayout = new BindingLayout();

    //typedef Expression* (*exp_parser)(  SGPropertyNode exp,    Parser* parser);
    public void addParser(String name, exp_parser parser) {
        getParserMap().put(name, parser);///*insert(std::make_pair (name, parser));
    }

    // FG-DIFF use SGExpression instead of Expression
    public SGExpression read(SGPropertyNode exp) throws ParseError {
        ParserMap map = getParserMap();
        //ParserMap::iterator itr = map.find(exp .getName());
        exp_parser parser = map.get(exp.getName());
        //if (itr == map.end())
        if (parser == null)
            throw new ParseError("unknown expression '" + exp.getName()+"'");
        //exp_parser parser = itr .getSecond;
        return parser/*(*parser)*/.parse(exp, this);
    }

    // XXX vector of SGSharedPtr?
    // FG-DIFF use SGExpression instead of Expression
    boolean readChildren(SGPropertyNode exp, List<SGExpression> result) throws ParseError {
        for (int i = 0; i < exp.nChildren(); ++i)
            result.add(read(exp.getChild(i)));
        return true;
    }



    /**
     * Function that parses a property tree, producing an expression.
     */
    //typedef std::map<std::string,exp_parser>ParserMap;    ParserMap& getParserMap() =0;
    abstract ParserMap getParserMap();

    /**
     * After an expression isType parsed, the binding layout may contain
     * references that need to be bound during evaluation.
     */
    public BindingLayout getBindingLayout() {
        return _bindingLayout;
    }

}



