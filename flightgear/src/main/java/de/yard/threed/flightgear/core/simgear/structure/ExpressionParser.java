package de.yard.threed.flightgear.core.simgear.structure;

import de.yard.threed.core.GeneralFunction;
import de.yard.threed.core.Util;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.props.Props;
import de.yard.threed.flightgear.core.simgear.scene.material.TechniquePredParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static de.yard.threed.flightgear.core.simgear.structure.ExpressionType.BOOL;

/**
 * From SGExpression.hxx
 */
public class ExpressionParser extends Parser {

    /**
     * FG-DIFF _parserTable not in own class??
     */
    //class ParserMapSingleton /*extends simgear::Singleton<ParserMapSingleton>*/ {
    // Apparently never inited in FG(??)
    static ParserMap _parserTable = new ParserMap();
    //}

    @Override
    public ParserMap getParserMap() {
        return _parserTable;
        //return ParserMapSingleton/*::instance ()*/._parserTable;
    }

    /**
     * FG-DIFF made static
     *
     * @param token
     * @param parsefn
     */
    public static void addExpParser(String token, exp_parser parsefn) {
        /*ParserMapSingleton/*::instance().*/
        _parserTable.put(token, parsefn);//insert(std::make_pair(token, parsefn));
    }
    //protected:

    /**
     * 14.10.25: Missing parser parts from SGExpression.cxx
     */

    static class valueParser implements exp_parser {
        public SGExpression parse(SGPropertyNode exp, Parser parser) {
            switch (exp.getType()) {
                case Props.BOOL:
                    return new SGConstExpression(new ExpressionType(ExpressionType.BOOL).getValueFromProperty(exp));
                case Props.INT:
                    return new SGConstExpression(new ExpressionType(ExpressionType.INT).getValueFromProperty(exp));
                case Props.FLOAT:
                    //return new SGConstExpression<float>(new ExpressionType(ExpressionType.BOOL).getValueFromProperty(exp));
                case Props.DOUBLE:
                    return new SGConstExpression(new ExpressionType(ExpressionType.DOUBLE).getValueFromProperty(exp));
                default:
                    return null;
            }
        }
    }

    ExpParserRegistrar valueRegistrar = new ExpParserRegistrar("value", new valueParser());

    //template<template<typename OpType> class PredExp>
    static class predParser implements exp_parser {
        public SGExpression parse(SGPropertyNode exp, Parser parser) throws ParseError {
            List<SGExpression> children = new ArrayList<>();
            parser.readChildren(exp, children);
            ExpressionType operandType = Expression.promoteAndConvert(children);
            return Expression.makeTypedOperandExp /*< PredExp >*/(operandType, children);
        }
    }

    static ExpParserRegistrar equalRegistrar = new ExpParserRegistrar("equal", new predParser/*<EqualToExpression>*/());
    static ExpParserRegistrar lessRegistrar = new ExpParserRegistrar("less", new predParser/*<LessExpression>*/());
    static ExpParserRegistrar leRegistrar = new ExpParserRegistrar("less-equal", new predParser/*<LessEqualExpression>*/());

    //template<typename Logicop>
    static class logicopParser implements exp_parser {
        GeneralFunction<SGNaryExpression,Integer> Logiop;

        logicopParser(GeneralFunction<SGNaryExpression,Integer> Logiop){
            this.Logiop=Logiop;
        }
        public SGExpression parse(SGPropertyNode exp, Parser parser) throws ParseError {
            //using namespace boost;
            List<SGExpression> children = new ArrayList<>();
            parser.readChildren(exp, children);
             /*TODO vector < Expression * >::iterator notBool =
                    find_if(children.begin(), children.end(),
                            boost::bind ( & Expression::getType, _1) !=BOOL);
            if (notBool != children.end())
                throw ("non boolean operand to logical expression");*/

            // FG uses the template here
            //Logicop  expr = new Logicop;
            SGNaryExpression  expr = Logiop.handle(null);
            expr . addOperands(children/*.begin(), children.end()*/);
            return expr;
                   }
    }

    static ExpParserRegistrar andRegistrar = new ExpParserRegistrar("and", new logicopParser/*<AndExpression>*/(parameter -> new AndExpression()));

    static ExpParserRegistrar orRegistrar = new ExpParserRegistrar("or", new logicopParser/*<OrExpression>*/(parameter->new OrExpression()));
}

class ParserMap extends HashMap<String, exp_parser> {
}




