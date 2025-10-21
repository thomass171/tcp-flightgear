package de.yard.threed.flightgear.core.simgear.structure;

import de.yard.threed.core.Util;
import de.yard.threed.flightgear.core.CppStd;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * From SGExpression.[ch]xx
 *
 * Should be generic.
 * 'abstract' removed because isn't abstract in FG(?)
 * We prefer to use SGExpression to avoid mix in list mess.
 * 20.10.25
 */
public /*abstract*/ class Expression /*: public SGReferenced*/ {
    //public:
    //~Expression() {}
    //expression::Type getType() const = 0;
    //};

    /**
     * Should be abstract(?), but now for keeping it simple just DOUBLE
     * @return
     */
    public ExpressionType getType() {
        return new ExpressionType(ExpressionType.DOUBLE);
    }

    //template<typename T>
          /*Value<T> evalValue( Expression exp,Binding b)        {
            T val;
            static_cast<const SGExpression<T>*>(exp).eval(val, b);
            return expression::Value(val);
        }*/


        /*Value eval(  Expression exp,          Binding binding /*= 0* /) {
            //using namespace expression;
            switch (exp.getType()) {
                case ExpressionType.BOOL:
                    return evalValue < bool > (exp,b);
                case ExpressionType.INT:
                    return evalValue <int>(exp, b);
                case ExpressionType.FLOAT:
                    return evalValue <float>(exp, b);
                case ExpressionType.DOUBLE:
                    return evalValue <double>(exp, b);
                default:
                    throw "invalid type.";
            }

        }*/

    /**
     * FG-DIFF static
     */
    //template<typename T, typename OpType>
    /*inline*/static Expression makeConvert(Expression e)
    {
        //return new ConvertExpression<T, OpType>(static_cast<SGExpression<OpType>*>(e));
        //TODO
        return e;//new ConvertExpression<T, OpType>(static_cast<SGExpression<OpType>*>(e));
    }

    /**
     * FG-DIFF static
     * TODO needs to convert inside Expression itself(??)
     */
    static ExpressionType promoteAndConvert(List<SGExpression> exps, ExpressionType minType /* default done by separat method = BOOL*/)    {
        //vector<Expression*>::iterator maxElem = max_element(exps.begin(), exps.end());
        /*vector<Expression*>::iterator*/SGExpression maxElem = CppStd.max_element(exps, new Comparator<SGExpression>() {
            @Override
            public int compare(SGExpression o1, SGExpression o2) {
                return Integer.compare(o1.getType().type,o2.getType().type);
            }
        });
        ExpressionType maxType = maxElem.getType();
        ExpressionType resultType = minType.type < maxType.type ? maxType : minType;
        //for (vector<Expression*>::iterator itr = exps.begin(), end = exps.end(); itr != end; ++itr) {
        for (Expression itr :exps) {
            if (itr.getType().type != resultType.type) {
            /*TODO switch (itr.getType().type) {
                case ExpressionType.BOOL:
                    switch (resultType) {
                        case ExpressionType.INT:
                    *itr = makeConvert<int, bool>(*itr);
                            break;
                        case ExpressionType.FLOAT:
                    *itr = makeConvert<float, bool>(*itr);
                            break;
                        case ExpressionType.DOUBLE:
                    *itr = makeConvert<double, bool>(*itr);
                            break;
                        default:
                            break;
                    }
                    break;
                case ExpressionType.INT:
                    switch (resultType) {
                        case ExpressionType.FLOAT:
                    *itr = makeConvert<float, int>(*itr);
                            break;
                        case ExpressionType.DOUBLE:
                    *itr = makeConvert<double, int>(*itr);
                            break;
                        default:
                            break;
                    }
                    break;
                case ExpressionType.FLOAT:
                *itr = makeConvert<double, float>(*itr);
                    break;
                default:
                    break;
            }*/
            }
        }
        return resultType;
    }

    static ExpressionType promoteAndConvert(List<SGExpression> exps){
        return promoteAndConvert( exps, new ExpressionType(ExpressionType.BOOL));
        }

    /**
     * FG-DIFF Only GeneralNaryExpression has method addOperand(s). No idea how FG/C++ manages this with native "Expression".
     */
    //template<template<typename OpType> class Expr>
    static SGExpression makeTypedOperandExp(ExpressionType operandType, List<SGExpression> children)    {
       switch (operandType.type) {
            /*TODO case ExpressionType.BOOL:
            {
                Expr<bool> *expr = new Expr<bool>();
                expr->addOperands(children.begin(), children.end());
                return expr;
            }
            case ExpressionType.INT:
            {
                Expr<int> *expr = new Expr<int>();
                expr->addOperands(children.begin(), children.end());
                return expr;
            }
            case ExpressionType.FLOAT:
            {
                Expr<float> *expr = new Expr<float>();
                expr->addOperands(children.begin(), children.end());
                return expr;
            }*/
            case ExpressionType.DOUBLE:
            {
                GeneralNaryExpression/*<double>*/ expr = new GeneralNaryExpression/*Expression/*<double>*/();
                expr.addOperands(children/*.begin(), children.end()*/);
                return expr;
            }
            default:
                return null;
        }
    }
}
