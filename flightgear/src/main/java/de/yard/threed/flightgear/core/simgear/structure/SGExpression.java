package de.yard.threed.flightgear.core.simgear.structure;

import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.Util;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.flightgear.core.CppStd;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.math.SGInterpTable;
import de.yard.threed.flightgear.core.simgear.props.Props;
import de.yard.threed.core.platform.Log;
import de.yard.threed.flightgear.core.simgear.scene.model.SGAnimation;

import java.util.*;

import static de.yard.threed.flightgear.core.simgear.structure.ExpressionType.BOOL;


/**
 * From SGExpression.hxx
 * <p>
 * wird anscheinend nicht fuer Nasal verwendet.
 * <p>
 * Trotz der Typisierung scheint es immer als double verwendet zu werden. Da sie keinen Nuzen beim Anlegen
 * der Instanz hat, erstmal weglassen.
 * <p>
 * <p>
 * Created by thomass on 01.09.16.
 */
abstract public class SGExpression/*<T>*/ extends Expression {
    static Log logger = Platform.getInstance().getLog(SGExpression.class);

    //public:
    //~    SGExpression() {    }

    //typedef T    result_type;
    //typedef T    operand_type;
    //FG-DIFF FG has no default, but that might be set by 'T'. Until we have it generic, set double to avoid NPE.
    //Many subclasses do not set it explicitly
    ExpressionType result_type = new ExpressionType(ExpressionType.DOUBLE);

    /**
     * 17.10.25:Now we have this as base implementation to avoid abstract SGExpression sub classes. Does "return null" comply to C++ "=0"?
     */
    public PrimitiveValue/*void*/ eval(/*T&,*/ Binding b) {//   =0;
        return null;
    }

    public PrimitiveValue/*T*/ getValue(Binding binding/*=0*/) {
        //T value;
        return eval(/*value,*/ binding);
        //return value;
    }

    @Override
    public ExpressionType getType() {
        return result_type;
    }

    double getDoubleValue(Binding binding/*=0*/) {
        //T value;
        return eval(/*value,*/ binding).doubleVal;
        //return value;
    }

    public boolean isConst() {
        return false;
    }

    /**
     * FG-DIFF? Defaultimplementierung
     *
     * @return
     */
    public SGExpression simplify() {
        return this;
    }

    //simgear::expression::

    /*ExpressionType getType() {
        return /*simgear::expression::TypeTraits<T>::typeTag* /;
    }*/

    /*simgear::expression::Type*/int getOperandType() {
        return -1;//simgear::expression::TypeTraits<T>::typeTag;
    }

    public void collectDependentProperties(/*std::set*/Set<SGPropertyNode> props) {
    }

    /**
     * Was passiert denn in FG, wenn es ein Doiuble werden soll, er aber einen String liest?
     * Wird das implizit konvertiert? Koennte sein, dass der SGReadValueFromString das macht.
     *
     * @param node
     * @param type
     * @return
     */
    //template<typename T>
    static PrimitiveValue/*boolean*/    SGReadValueFromContent(SGPropertyNode node, ExpressionType type/*T& value*/) {
        if (node == null)
            return null;// false;
        //return SGReadValueFromString(node.getStringValue(), value);
        return type.getValueFromProperty(node);
    }

    /**
     * FG-DIFF additional parameter type instead of generic, typically "new ExpressionType(ExpressionType.DOUBLE)"
     */
    //template<typename T>
    static SGExpression SGReadExpression(ExpressionType type, SGPropertyNode inputRoot, SGPropertyNode expression) {
        if (expression == null)
            return null;

        String name = expression.getName();

        if (name.equals("value")) {
            //T value;
            PrimitiveValue value = SGReadValueFromContent(expression, type);
            if (value == null) {
                logger.error(/*SG_LOG(SG_IO, SG_ALERT,*/ "Cannot read \"value\" expression.");
                return null;
            }
            return new SGConstExpression(/*type,*/value);
        }

        if (name.equals("property")) {
            if (inputRoot == null) {
                logger.error(/*SG_IO, SG_ALERT,*/ "Cannot read \"" + name + "\" expression.\n" + "No inputRoot argument given!");
                return null;
            }
            if (expression.getStringValue() == null) {
                logger.error(/*SG_IO, SG_ALERT,*/ "Cannot read \"" + name + "\" expression.");
                return null;
            }
            //SGPropertyNode inputNode;
            //inputNode = inputRoot.getNode(expression.getStringValue(), true);
            //return new SGPropertyExpression/*<T>*/(type, inputNode);
            return SGAnimation.resolvePropertyValueExpression(expression.getStringValue(), inputRoot);

        }

        if (name.equals("abs") || name.equals("fabs")) {
            Util.notyet();
            /*
            if (expression->nChildren() != 1) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            SGSharedPtr<SGExpression<T> > inputExpression;
            inputExpression = SGReadExpression<T>(inputRoot, expression->getChild(0));
            if (!inputExpression) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            return new SGAbsExpression<T>(inputExpression);*/
        }

        if (name.equals("sqr")) {
            Util.notyet();
            /* if (expression->nChildren() != 1) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            SGSharedPtr<SGExpression<T> > inputExpression;
            inputExpression = SGReadExpression<T>(inputRoot, expression->getChild(0));
            if (!inputExpression) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            return new SGSqrExpression<T>(inputExpression);*/
        }

        if (name.equals("clip")) {
            Util.notyet();
            /*if (expression->nChildren() != 3) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
        const SGPropertyNode* minProperty = expression->getChild("clipMin");
            T clipMin;
            if (!SGReadValueFromContent(minProperty, clipMin))
                clipMin = SGMisc<T>::min(SGLimits<T>::min(), -SGLimits<T>::max());

        const SGPropertyNode* maxProperty = expression->getChild("clipMax");
            T clipMax;
            if (!SGReadValueFromContent(maxProperty, clipMax))
                clipMin = SGLimits<T>::max();

            SGSharedPtr<SGExpression<T> > inputExpression;
            for (int i = 0; !inputExpression && i < expression->nChildren(); ++i)
                inputExpression = SGReadExpression<T>(inputRoot, expression->getChild(i));
            if (!inputExpression) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            return new SGClipExpression<T>(inputExpression, clipMin, clipMax);*/
        }

        if (name.equals("div")) {
            if (expression.nChildren() != 2) {
                logger.error("Cannot read \"" + name + "\" expression.");
                return null;
            }
            /*SGSharedPtr<SGExpression<T> >*/
            SGExpression inputExpressions[] = new SGExpression[]{
                    SGReadExpression(new ExpressionType(ExpressionType.DOUBLE), inputRoot, expression.getChild(0)),
                    SGReadExpression(new ExpressionType(ExpressionType.DOUBLE), inputRoot, expression.getChild(1))
            };
            if (inputExpressions[0] == null || inputExpressions[1] == null) {
                logger.error("Cannot read \"" + name + "\" expression.");
                return null;
            }
            return new SGDivExpression(inputExpressions[0], inputExpressions[1]);
        }
        if (name.equals("mod")) {
            if (expression.nChildren() != 2) {
                logger.error("Cannot read \"" + name + "\" expression.");
                return null;
            }
            /*SGSharedPtr<*/
            SGExpression/*<T>*/ inputExpressions[] = new SGExpression[]{
                    SGReadExpression/*<T>*/(new ExpressionType(ExpressionType.DOUBLE), inputRoot, expression.getChild(0)),
                    SGReadExpression/*<T>*/(new ExpressionType(ExpressionType.DOUBLE), inputRoot, expression.getChild(1))
            };
            if (inputExpressions[0] == null || inputExpressions[1] == null) {
                logger.error("Cannot read \"" + name + "\" expression.");
                return null;
            }
            return new SGModExpression/*<T>*/(inputExpressions[0], inputExpressions[1]);
        }

        if (name.equals("sum")) {
            if (expression.nChildren() < 1) {
                logger.error("Cannot read \"" + name + "\" expression.");
                return null;
            }
            SGSumExpression output = new SGSumExpression/*<T>*/();
            if (!SGReadNaryOperands(output, inputRoot, expression)) {
                //delete output;
                logger.error("Cannot read \"" + name + "\" expression.");
                return null;
            }
            return output;
        }

        if (name.equals("difference") || name.equals("dif")) {
            if (expression.nChildren() < 1) {
                logger.error(/*SG_IO, SG_ALERT,*/ "Cannot read \"" + name + "\" expression.");
                return null;
            }
            SGDifferenceExpression output = new SGDifferenceExpression();
            if (!SGReadNaryOperands(output, inputRoot, expression)) {
                //delete output;
                logger.error(/*SG_IO, SG_ALERT,*/ "Cannot read \"" + name + "\" expression.");
                return null;
            }
            return output;
        }

        if (name.equals("prod") || name.equals("product")) {
            if (expression.nChildren() < 1) {
                logger.error(/*SG_IO, SG_ALERT,*/ "Cannot read \"" + name + "\" expression.");
                return null;
            }
            SGProductExpression output = new SGProductExpression();
            if (!SGReadNaryOperands(output, inputRoot, expression)) {
                //delete output;
                logger.error(/*SG_IO, SG_ALERT, */"Cannot read \"" + name + "\" expression.");
                return null;
            }
            return output;
        }
        if (name.equals("min")) {
            Util.notyet();
            /*if (expression->nChildren() < 1) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            SGMinExpression<T>* output = new SGMinExpression<T>;
            if (!SGReadNaryOperands(output, inputRoot, expression)) {
                delete output;
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            return output;*/
        }
        if (name.equals("max")) {
            Util.notyet();
            /*if (expression->nChildren() < 1) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            SGMaxExpression<T>* output = new SGMaxExpression<T>;
            if (!SGReadNaryOperands(output, inputRoot, expression)) {
                delete output;
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            return output;*/
        }

        if (name.equals("table")) {
            SGInterpTable tab = new SGInterpTable(expression);
            if (tab == null) {
                logger.error("Cannot read \"" + name + "\" expression: malformed table");
                return null;
            }

            // find input expression - i.e a child not named 'entry'
            SGPropertyNode inputNode = null;
            for (int i = 0; (i < expression.nChildren()) && inputNode == null; ++i) {
                if (/*strcmp*/(expression.getChild(i).getName().equals("entry")/* == 0*/)) {
                    continue;
                }

                inputNode = expression.getChild(i);
            }

            if (inputNode == null) {
                logger.error("Cannot read \"" + name + "\" expression: no input found");
                return null;
            }

            /*SGSharedPtr<*/
            SGExpression/*<T>*/ inputExpression;
            inputExpression = SGReadExpression /*< T >*/(new ExpressionType(ExpressionType.DOUBLE), inputRoot, inputNode);
            if (inputExpression == null) {
                logger.error(/*SG_IO, SG_ALERT,*/ "Cannot read \"" + name + "\" expression.");
                return null;
            }

            return new SGInterpTableExpression/*<T>*/(inputExpression, tab);

        }

        if (name.equals("acos")) {
            Util.notyet();
            /*if (expression->nChildren() != 1) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            SGSharedPtr<SGExpression<T> > inputExpression;
            inputExpression = SGReadExpression<T>(inputRoot, expression->getChild(0));
            if (!inputExpression) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            return new SGACosExpression<T>(inputExpression);*/
        }

        if (name.equals("asin")) {
            Util.notyet();
            /*if (expression->nChildren() != 1) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            SGSharedPtr<SGExpression<T> > inputExpression;
            inputExpression = SGReadExpression<T>(inputRoot, expression->getChild(0));
            if (!inputExpression) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            return new SGASinExpression<T>(inputExpression);*/
        }

        if (name.equals("atan")) {
            if (expression.nChildren() != 1) {
                logger.error(/*SG_IO, SG_ALERT,*/ "Cannot read \"" + name + "\" expression.");
                return null;
            }
            SGExpression inputExpression;
            inputExpression = SGReadExpression(new ExpressionType(ExpressionType.DOUBLE), inputRoot, expression.getChild(0));
            if (inputExpression == null) {
                logger.error(/*SG_IO, SG_ALERT,*/ "Cannot read \"" + name + "\" expression.");
                return null;
            }
            return new SGATanExpression(inputExpression);
        }

        if (name.equals("ceil")) {
            Util.notyet();
            /*if (expression->nChildren() != 1) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            SGSharedPtr<SGExpression<T> > inputExpression;
            inputExpression = SGReadExpression<T>(inputRoot, expression->getChild(0));
            if (!inputExpression) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            return new SGCeilExpression<T>(inputExpression);*/
        }

        if (name.equals("cos")) {
            Util.notyet();
            /*if (expression->nChildren() != 1) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            SGSharedPtr<SGExpression<T> > inputExpression;
            inputExpression = SGReadExpression<T>(inputRoot, expression->getChild(0));
            if (!inputExpression) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            return new SGCosExpression<T>(inputExpression);*/
        }

        if (name.equals("cosh")) {
            Util.notyet();
            /*if (expression->nChildren() != 1) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            SGSharedPtr<SGExpression<T> > inputExpression;
            inputExpression = SGReadExpression<T>(inputRoot, expression->getChild(0));
            if (!inputExpression) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            return new SGCoshExpression<T>(inputExpression);*/
        }

        if (name.equals("deg2rad")) {
            Util.notyet();
            /*if (expression->nChildren() != 1) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            SGSharedPtr<SGExpression<T> > inputExpression;
            inputExpression = SGReadExpression<T>(inputRoot, expression->getChild(0));
            if (!inputExpression) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            return new SGScaleExpression<T>(inputExpression, SGMisc<T>::pi()/180);*/
        }

        if (name.equals("exp")) {
            Util.notyet();
            /*if (expression->nChildren() != 1) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            SGSharedPtr<SGExpression<T> > inputExpression;
            inputExpression = SGReadExpression<T>(inputRoot, expression->getChild(0));
            if (!inputExpression) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            return new SGExpExpression<T>(inputExpression);*/
        }

        if (name.equals("floor")) {
            Util.notyet();
            /*if (expression->nChildren() != 1) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            SGSharedPtr<SGExpression<T> > inputExpression;
            inputExpression = SGReadExpression<T>(inputRoot, expression->getChild(0));
            if (!inputExpression) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            return new SGFloorExpression<T>(inputExpression);*/
        }

        if (name.equals("log")) {
            Util.notyet();
            /*if (expression->nChildren() != 1) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            SGSharedPtr<SGExpression<T> > inputExpression;
            inputExpression = SGReadExpression<T>(inputRoot, expression->getChild(0));
            if (!inputExpression) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            return new SGLogExpression<T>(inputExpression);*/
        }

        if (name.equals("log10")) {
            Util.notyet();
            /*if (expression->nChildren() != 1) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            SGSharedPtr<SGExpression<T> > inputExpression;
            inputExpression = SGReadExpression<T>(inputRoot, expression->getChild(0));
            if (!inputExpression) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            return new SGLog10Expression<T>(inputExpression);*/
        }

        if (name.equals("rad2deg")) {
            if (expression.nChildren() != 1) {
                logger.error(/*SG_IO, SG_ALERT,*/ "Cannot read \"" + name + "\" expression.");
                return null;
            }
            SGExpression inputExpression;
            inputExpression = SGReadExpression(new ExpressionType(ExpressionType.DOUBLE), inputRoot, expression.getChild(0));
            if (inputExpression == null) {
                logger.error(/*SG_IO, SG_ALERT,*/ "Cannot read \"" + name + "\" expression.");
                return null;
            }
            return new SGScaleExpression(inputExpression, 180 / Math.PI/*  SGMisc.<T>::pi()*/);
        }

        if (name.equals("sin")) {
            Util.notyet();
            /*    if (expression->nChildren() != 1) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            SGSharedPtr<SGExpression<T> > inputExpression;
            inputExpression = SGReadExpression<T>(inputRoot, expression->getChild(0));
            if (!inputExpression) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            return new SGSinExpression<T>(inputExpression);*/
        }

        if (name.equals("sinh")) {
            Util.notyet();
            /*if (expression->nChildren() != 1) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            SGSharedPtr<SGExpression<T> > inputExpression;
            inputExpression = SGReadExpression<T>(inputRoot, expression->getChild(0));
            if (!inputExpression) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            return new SGSinhExpression<T>(inputExpression);*/
        }

        if (name.equals("sqrt")) {
            Util.notyet();
            /*if (expression->nChildren() != 1) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            SGSharedPtr<SGExpression<T> > inputExpression;
            inputExpression = SGReadExpression<T>(inputRoot, expression->getChild(0));
            if (!inputExpression) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            return new SGSqrtExpression<T>(inputExpression);*/
        }

        if (name.equals("tan")) {
            Util.notyet();
            /*if (expression->nChildren() != 1) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            SGSharedPtr<SGExpression<T> > inputExpression;
            inputExpression = SGReadExpression<T>(inputRoot, expression->getChild(0));
            if (!inputExpression) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            return new SGTanExpression<T>(inputExpression);*/
        }

        if (name.equals("tanh")) {
            Util.notyet();
            /*if (expression->nChildren() != 1) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            SGSharedPtr<SGExpression<T> > inputExpression;
            inputExpression = SGReadExpression<T>(inputRoot, expression->getChild(0));
            if (!inputExpression) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            return new SGTanhExpression<T>(inputExpression);*/
        }

// if (name.equals("step") {
// }
// if (name.equals("condition") {
// }

        if (name.equals("atan2")) {
            Util.notyet();
            /*if (expression->nChildren() != 2) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            SGSharedPtr<SGExpression<T> > inputExpressions[2] = {
                SGReadExpression<T>(inputRoot, expression->getChild(0)),
                SGReadExpression<T>(inputRoot, expression->getChild(1))
            };
            if (!inputExpressions[0] || !inputExpressions[1]) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            return new SGAtan2Expression<T>(inputExpressions[0], inputExpressions[1]);*/
        }
        if (name.equals("div")) {
            if (expression.nChildren() != 2) {
                logger.error(/*SG_IO, SG_ALERT,*/ "Cannot read \"" + name + "\" expression.");
                return null;
            }
            SGExpression[] inputExpressions = new SGExpression[]{
                    SGReadExpression(new ExpressionType(ExpressionType.DOUBLE), inputRoot, expression.getChild(0)),
                    SGReadExpression(new ExpressionType(ExpressionType.DOUBLE), inputRoot, expression.getChild(1))
            };
            if (inputExpressions[0] == null || inputExpressions[1] == null) {
                logger.error(/*SG_IO, SG_ALERT,*/ "Cannot read \"" + name + "\" expression.");
                return null;
            }
            return new SGDivExpression(inputExpressions[0], inputExpressions[1]);
        }
        if (name.equals("mod")) {
            Util.notyet();
            /*if (expression->nChildren() != 2) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            SGSharedPtr<SGExpression<T> > inputExpressions[2] = {
                SGReadExpression<T>(inputRoot, expression->getChild(0)),
                SGReadExpression<T>(inputRoot, expression->getChild(1))
            };
            if (!inputExpressions[0] || !inputExpressions[1]) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            return new SGModExpression<T>(inputExpressions[0], inputExpressions[1]);*/
        }
        if (name.equals("pow")) {
            Util.notyet();
            /*if (expression->nChildren() != 2) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            SGSharedPtr<SGExpression<T> > inputExpressions[2] = {
                SGReadExpression<T>(inputRoot, expression->getChild(0)),
                SGReadExpression<T>(inputRoot, expression->getChild(1))
            };
            if (!inputExpressions[0] || !inputExpressions[1]) {
                logger.error(/*SG_IO, SG_ALERT, "Cannot read \"" << name << "\" expression.");
                return 0;
            }
            return new SGPowExpression<T>(inputExpressions[0], inputExpressions[1]);*/
        }
        logger.error("notyet:" + name);
        Util.notyet();
        return null;
    }

    //template<typename T>
    static boolean SGReadNaryOperands(SGNaryExpression nary, SGPropertyNode inputRoot, SGPropertyNode expression) {
        for (int i = 0; i < expression.nChildren(); ++i) {
            SGExpression inputExpression;
            inputExpression = SGReadExpression(new ExpressionType(ExpressionType.DOUBLE), inputRoot, expression.getChild(i));
            if (inputExpression == null)
                return false;
            nary.addOperand(inputExpression);
        }
        return true;
    }

    //template<typename T>
    /*static Wert/*boolean * /   SGReadValueFromString(String str/*, T& value* /)    {
        if (str==null) {
            logger.error(/*SG_LOG(SG_IO, SG_ALERT,* / "Cannot read string content.");
            return null;//false;
        }
        return new Wert(str);
        /*std::stringstream s;
        s.str(std::string(str));
        s >> value;
        if (s.fail()) {
            logger.error(/*SG_LOG(SG_IO, SG_ALERT,* / "Cannot read string content.");
            return false;
        }
        return true;* /
    }*/
    
  /*  SGExpression</*int* /WertInt>    SGReadIntExpression(SGPropertyNode inputRoot,                     SGPropertyNode configNode) {
        return SGReadExpression (inputRoot, configNode);
    }

    SGExpression<float>*

    SGReadFloatExpression(SGPropertyNode *inputRoot,                      const SGPropertyNode *configNode) {
        return SGReadExpression <float>(inputRoot, configNode);
    }
*/

    public static SGExpression/*<double>**/    SGReadDoubleExpression(SGPropertyNode inputRoot, SGPropertyNode configNode) {
        return SGReadExpression/* <double>*/(new ExpressionType(ExpressionType.DOUBLE), inputRoot, configNode);
    }

    /**
     * 3.4.18:Ist hier nur als Indicator implementiert, dass die Subclasses das machen mussen.
     *
     * @return
     */
    @Override
    public String toString() {
        return "Expression";
    }
}


//template<typename T>
abstract class SGBinaryExpression extends SGExpression {
    SGExpression[] _expressions = new SGExpression[2];

    SGBinaryExpression(SGExpression expr0, SGExpression expr1) {
        setOperand(0, expr0);
        setOperand(1, expr1);
    }

    SGExpression getOperand(int i) {
        return _expressions[i];
    }

    /*SGExpression<T>*    getOperand(int i) {
        return _expressions[i];
    }*/

    void setOperand(int i, SGExpression expression) {
        if (expression == null)
            expression = new SGConstExpression(expression.getType().buildDefaultValue());
        if (2 <= i)
            i = 0;
        _expressions[i] = expression;
    }

    @Override
    public boolean isConst() {
        return getOperand(0).isConst() && getOperand(1).isConst();
    }

    /*@Override
    SGExpression    simplify() {
        _expressions[0] = _expressions[0].simplify();
        _expressions[1] = _expressions[1].simplify();
        return SGExpression<T>::simplify ();
    }*/

    @Override
    public void collectDependentProperties(Set<SGPropertyNode> props) {
        _expressions[0].collectDependentProperties(props);
        _expressions[1].collectDependentProperties(props);
    }
}


// template<typename T>

/**
 * class SGAbsExpression extends SGUnaryExpression<T> {
 * public:
 * SGAbsExpression(SGExpression<T>* expr = 0)
 * : SGUnaryExpression<T>(expr)
 * { }
 * <p>
 * void eval(T& value,   simgear::expression::Binding* b)
 * { value = getOperand() .getValue(b); if (value <= 0) value = -value; }
 * <p>
 * using SGUnaryExpression<T>::getOperand;
 * };
 * <p>
 * template<typename T>
 * class SGACosExpression extends SGUnaryExpression<T> {
 * public:
 * SGACosExpression(SGExpression<T>* expr = 0)
 * : SGUnaryExpression<T>(expr)
 * { }
 * <p>
 * void eval(T& value,   simgear::expression::Binding* b)
 * { value = acos((double)SGMisc<T>::clip(getOperand() .getValue(b), -1, 1)); }
 * <p>
 * using SGUnaryExpression<T>::getOperand;
 * };
 * <p>
 * template<typename T>
 * class SGASinExpression extends SGUnaryExpression<T> {
 * public:
 * SGASinExpression(SGExpression<T>* expr = 0)
 * : SGUnaryExpression<T>(expr)
 * { }
 * <p>
 * void eval(T& value,   simgear::expression::Binding* b)
 * { value = asin((double)SGMisc<T>::clip(getOperand() .getValue(b), -1, 1)); }
 * <p>
 * using SGUnaryExpression<T>::getOperand;
 * };
 * <p>
 * <p>
 * template<typename T>
 * class SGCeilExpression extends SGUnaryExpression<T> {
 * public:
 * SGCeilExpression(SGExpression<T>* expr = 0)
 * : SGUnaryExpression<T>(expr)
 * { }
 * <p>
 * void eval(T& value,   simgear::expression::Binding* b)
 * { value = ceil(getOperand() .getDoubleValue(b)); }
 * <p>
 * using SGUnaryExpression<T>::getOperand;
 * };
 * <p>
 * template<typename T>
 * class SGCosExpression extends SGUnaryExpression<T> {
 * public:
 * SGCosExpression(SGExpression<T>* expr = 0)
 * : SGUnaryExpression<T>(expr)
 * { }
 * <p>
 * void eval(T& value,   simgear::expression::Binding* b)
 * { value = cos(getOperand() .getDoubleValue(b)); }
 * <p>
 * using SGUnaryExpression<T>::getOperand;
 * };
 * <p>
 * template<typename T>
 * class SGCoshExpression extends SGUnaryExpression<T> {
 * public:
 * SGCoshExpression(SGExpression<T>* expr = 0)
 * : SGUnaryExpression<T>(expr)
 * { }
 * <p>
 * void eval(T& value,   simgear::expression::Binding* b)
 * { value = cosh(getOperand() .getDoubleValue(b)); }
 * <p>
 * using SGUnaryExpression<T>::getOperand;
 * };
 * <p>
 * template<typename T>
 * class SGExpExpression extends SGUnaryExpression<T> {
 * public:
 * SGExpExpression(SGExpression<T>* expr = 0)
 * : SGUnaryExpression<T>(expr)
 * { }
 * <p>
 * void eval(T& value,   simgear::expression::Binding* b)
 * { value = exp(getOperand() .getDoubleValue(b)); }
 * <p>
 * using SGUnaryExpression<T>::getOperand;
 * };
 * <p>
 * template<typename T>
 * class SGFloorExpression extends SGUnaryExpression<T> {
 * public:
 * SGFloorExpression(SGExpression<T>* expr = 0)
 * : SGUnaryExpression<T>(expr)
 * { }
 * <p>
 * void eval(T& value,   simgear::expression::Binding* b)
 * { value = floor(getOperand() .getDoubleValue(b)); }
 * <p>
 * using SGUnaryExpression<T>::getOperand;
 * };
 * <p>
 * template<typename T>
 * class SGLogExpression extends SGUnaryExpression<T> {
 * public:
 * SGLogExpression(SGExpression<T>* expr = 0)
 * : SGUnaryExpression<T>(expr)
 * { }
 * <p>
 * void eval(T& value,   simgear::expression::Binding* b)
 * { value = log(getOperand() .getDoubleValue(b)); }
 * <p>
 * using SGUnaryExpression<T>::getOperand;
 * };
 * <p>
 * template<typename T>
 * class SGLog10Expression extends SGUnaryExpression<T> {
 * public:
 * SGLog10Expression(SGExpression<T>* expr = 0)
 * : SGUnaryExpression<T>(expr)
 * { }
 * <p>
 * void eval(T& value,   simgear::expression::Binding* b)
 * { value = log10(getOperand() .getDoubleValue(b)); }
 * <p>
 * using SGUnaryExpression<T>::getOperand;
 * };
 * <p>
 * template<typename T>
 * class SGSinExpression extends SGUnaryExpression<T> {
 * public:
 * SGSinExpression(SGExpression<T>* expr = 0)
 * : SGUnaryExpression<T>(expr)
 * { }
 * <p>
 * void eval(T& value,   simgear::expression::Binding* b)
 * { value = sin(getOperand() .getDoubleValue(b)); }
 * <p>
 * using SGUnaryExpression<T>::getOperand;
 * };
 * <p>
 * template<typename T>
 * class SGSinhExpression extends SGUnaryExpression<T> {
 * public:
 * SGSinhExpression(SGExpression<T>* expr = 0)
 * : SGUnaryExpression<T>(expr)
 * { }
 * <p>
 * void eval(T& value,   simgear::expression::Binding* b)
 * { value = sinh(getOperand() .getDoubleValue(b)); }
 * <p>
 * using SGUnaryExpression<T>::getOperand;
 * };
 * <p>
 * template<typename T>
 * class SGSqrExpression extends SGUnaryExpression<T> {
 * public:
 * SGSqrExpression(SGExpression<T>* expr = 0)
 * : SGUnaryExpression<T>(expr)
 * { }
 * <p>
 * void eval(T& value,   simgear::expression::Binding* b)
 * { value = getOperand() .getValue(b); value = value*value; }
 * <p>
 * using SGUnaryExpression<T>::getOperand;
 * };
 * <p>
 * template<typename T>
 * class SGSqrtExpression extends SGUnaryExpression<T> {
 * public:
 * SGSqrtExpression(SGExpression<T>* expr = 0)
 * : SGUnaryExpression<T>(expr)
 * { }
 * <p>
 * void eval(T& value,   simgear::expression::Binding* b)
 * { value = sqrt(getOperand() .getDoubleValue(b)); }
 * <p>
 * using SGUnaryExpression<T>::getOperand;
 * };
 * <p>
 * template<typename T>
 * class SGTanExpression extends SGUnaryExpression<T> {
 * public:
 * SGTanExpression(SGExpression<T>* expr = 0)
 * : SGUnaryExpression<T>(expr)
 * { }
 * <p>
 * void eval(T& value,   simgear::expression::Binding* b)
 * { value = tan(getOperand() .getDoubleValue(b)); }
 * <p>
 * using SGUnaryExpression<T>::getOperand;
 * };
 * <p>
 * template<typename T>
 * class SGTanhExpression extends SGUnaryExpression<T> {
 * public:
 * SGTanhExpression(SGExpression<T>* expr = 0)
 * : SGUnaryExpression<T>(expr)
 * { }
 * <p>
 * void eval(T& value,   simgear::expression::Binding* b)
 * { value = tanh(getOperand() .getDoubleValue(b)); }
 * <p>
 * using SGUnaryExpression<T>::getOperand;
 * };
 * <p>
 *
 *
 *
 *
 * <p>
 * template<typename T>
 * class SGEnableExpression extends SGUnaryExpression<T> {
 * public:
 * SGEnableExpression(SGExpression<T>* expr = 0,
 * SGCondition* enable = 0,
 * T& disabledValue = T(0))
 * : SGUnaryExpression<T>(expr),
 * _enable(enable),
 * _disabledValue(disabledValue)
 * { }
 * <p>
 * T& getDisabledValue()
 * { return _disabledValue; }
 * void setDisabledValue(  T& disabledValue)
 * { _disabledValue = disabledValue; }
 * <p>
 * void eval(T& value,   simgear::expression::Binding* b)
 * {
 * if (_enable .test())
 * value = getOperand() .getValue(b);
 * else
 * value = _disabledValue;
 * }
 * <p>
 * SGExpression<T>* simplify()
 * {
 * if (!_enable)
 * return getOperand() .simplify();
 * return SGUnaryExpression<T>::simplify();
 * }
 * <p>
 * void collectDependentProperties(std::set<  SGPropertyNode*>& props)
 * {
 * SGUnaryExpression<T>::collectDependentProperties(props);
 * _enable .collectDependentProperties(props);
 * }
 * <p>
 * using SGUnaryExpression<T>::getOperand;
 * private:
 * SGSharedPtr<SGCondition> _enable;
 * T _disabledValue;
 * };
 * <p>
 * template<typename T>
 * class SGAtan2Expression extends SGBinaryExpression<T> {
 * public:
 * SGAtan2Expression(SGExpression<T>* expr0, SGExpression<T>* expr1)
 * : SGBinaryExpression<T>(expr0, expr1)
 * { }
 * void eval(T& value,   simgear::expression::Binding* b)
 * { value = atan2(getOperand(0) .getDoubleValue(b), getOperand(1) .getDoubleValue(b)); }
 * using SGBinaryExpression<T>::getOperand;
 * };
 * <p>
 * <p>
 * template<typename T>
 */

/**
 * TODO needs int overload
 */
class SGModExpression extends SGBinaryExpression/*<T>*/ {
    public SGModExpression(SGExpression expr0, SGExpression expr1)  //: SGBinaryExpression<T>(expr0, expr1)
    {
        super(expr0, expr1);
    }

    /*void*/
    public PrimitiveValue eval(/*T& value,   /*simgear::expression::*/Binding b) {
        double value = mod(getOperand(0).getValue(b).doubleVal, getOperand(1).getValue(b).doubleVal);
        return new PrimitiveValue(value);
    }

    /*using SGBinaryExpression<T>::getOperand;
    private:
    int mod(  int& v0,   int& v1)
    { return v0 % v1; }
    float mod(  float& v0,   float& v1)
    { return fmod(v0, v1); }*/
    double mod(double v0, double v1) {
        return MathUtil2.fmod(v0, v1);
    }
};
/* <p>
 * template<typename T>
 * class SGPowExpression extends SGBinaryExpression<T> {
 * public:
 * SGPowExpression(SGExpression<T>* expr0, SGExpression<T>* expr1)
 * : SGBinaryExpression<T>(expr0, expr1)
 * { }
 * void eval(T& value,   simgear::expression::Binding* b)
 * { value = pow(getOperand(0) .getDoubleValue(b), getOperand(1) .getDoubleValue(b)); }
 * using SGBinaryExpression<T>::getOperand;
 * };
 * <p>
 *
 * <p>
 * <p>
 * template<typename T>
 * class SGProductExpression extends SGNaryExpression<T> {
 * public:
 * SGProductExpression()
 * { }
 * SGProductExpression(SGExpression<T>* expr0, SGExpression<T>* expr1)
 * : SGNaryExpression<T>(expr0, expr1)
 * { }
 * void eval(T& value,   simgear::expression::Binding* b)
 * {
 * value = T(1);
 * int sz = SGNaryExpression<T>::getNumOperands();
 * for (int i = 0; i < sz; ++i)
 * value *= getOperand(i) .getValue(b);
 * }
 * using SGNaryExpression<T>::getValue;
 * using SGNaryExpression<T>::getOperand;
 * };
 * <p>
 * template<typename T>
 * class SGMinExpression extends SGNaryExpression<T> {
 * public:
 * SGMinExpression()
 * { }
 * SGMinExpression(SGExpression<T>* expr0, SGExpression<T>* expr1)
 * : SGNaryExpression<T>(expr0, expr1)
 * { }
 * void eval(T& value,   simgear::expression::Binding* b)
 * {
 * int sz = SGNaryExpression<T>::getNumOperands();
 * if (sz < 1)
 * return;
 * <p>
 * value = getOperand(0) .getValue(b);
 * for (int i = 1; i < sz; ++i)
 * value = SGMisc<T>::min(value, getOperand(i) .getValue(b));
 * }
 * using SGNaryExpression<T>::getOperand;
 * };
 * <p>
 * template<typename T>
 * class SGMaxExpression extends SGNaryExpression<T> {
 * public:
 * SGMaxExpression()
 * { }
 * SGMaxExpression(SGExpression<T>* expr0, SGExpression<T>* expr1)
 * : SGNaryExpression<T>(expr0, expr1)
 * { }
 * void eval(T& value,   simgear::expression::Binding* b)
 * {
 * int sz = SGNaryExpression<T>::getNumOperands();
 * if (sz < 1)
 * return;
 * <p>
 * value = getOperand(0) .getValue(b);
 * for (int i = 1; i < sz; ++i)
 * value = SGMisc<T>::max(value, getOperand(i) .getValue(b));
 * }
 * using SGNaryExpression<T>::getOperand;
 * };
 * <p>
 * typedef SGExpression<int> SGExpressioni;
 * typedef SGExpression<float> SGExpressionf;
 * typedef SGExpression<double> SGExpressiond;
 * typedef SGExpression<bool> SGExpressionb;
 * <p>
 * typedef SGSharedPtr<SGExpressioni> SGExpressioni_ref;
 * typedef SGSharedPtr<SGExpressionf> SGExpressionf_ref;
 * typedef SGSharedPtr<SGExpressiond> SGExpressiond_ref;
 * typedef SGSharedPtr<SGExpressionb> SGExpressionb_ref;
 * <p>
 * Global function to make an expression out of properties.
 * <p>
 * <clip>
 * <clipMin>0</clipMin>
 * <clipMax>79</clipMax>
 * <abs>
 * <sum>
 * <rad2deg>
 * <property>sim/model/whatever-rad</property>
 * </rad2deg>
 * <property>sim/model/someother-deg</property>
 * <value>-90</value>
 * </sum>
 * </abs>
 * <clip>
 * <p>
 * will evaluate to an expression:
 * <p>
 * SGMisc<T>::clip(abs(deg2rad*sim/model/whatever-rad + sim/model/someother-deg - 90), clipMin, clipMax);
 * <p>
 * Global function to make an expression out of properties.
 * <p>
 * <clip>
 * <clipMin>0</clipMin>
 * <clipMax>79</clipMax>
 * <abs>
 * <sum>
 * <rad2deg>
 * <property>sim/model/whatever-rad</property>
 * </rad2deg>
 * <property>sim/model/someother-deg</property>
 * <value>-90</value>
 * </sum>
 * </abs>
 * <clip>
 * <p>
 * will evaluate to an expression:
 * <p>
 * SGMisc<T>::clip(abs(deg2rad*sim/model/whatever-rad + sim/model/someother-deg - 90), clipMin, clipMax);
 * <p>
 * Global function to make an expression out of properties.
 * <p>
 * <clip>
 * <clipMin>0</clipMin>
 * <clipMax>79</clipMax>
 * <abs>
 * <sum>
 * <rad2deg>
 * <property>sim/model/whatever-rad</property>
 * </rad2deg>
 * <property>sim/model/someother-deg</property>
 * <value>-90</value>
 * </sum>
 * </abs>
 * <clip>
 * <p>
 * will evaluate to an expression:
 * <p>
 * SGMisc<T>::clip(abs(deg2rad*sim/model/whatever-rad + sim/model/someother-deg - 90), clipMin, clipMax);
 * <p>
 * Global function to make an expression out of properties.
 * <p>
 * <clip>
 * <clipMin>0</clipMin>
 * <clipMax>79</clipMax>
 * <abs>
 * <sum>
 * <rad2deg>
 * <property>sim/model/whatever-rad</property>
 * </rad2deg>
 * <property>sim/model/someother-deg</property>
 * <value>-90</value>
 * </sum>
 * </abs>
 * <clip>
 * <p>
 * will evaluate to an expression:
 * <p>
 * SGMisc<T>::clip(abs(deg2rad*sim/model/whatever-rad + sim/model/someother-deg - 90), clipMin, clipMax);
 */


/**
 * Global function to make an expression out of properties.
 * <p>
 * <clip>
 * <clipMin>0</clipMin>
 * <clipMax>79</clipMax>
 * <abs>
 * <sum>
 * <rad2deg>
 * <property>sim/model/whatever-rad</property>
 * </rad2deg>
 * <property>sim/model/someother-deg</property>
 * <value>-90</value>
 * </sum>
 * </abs>
 * <clip>
 * <p>
 * will evaluate to an expression:
 * <p>
 * SGMisc<T>::clip(abs(deg2rad*sim/model/whatever-rad + sim/model/someother-deg - 90), clipMin, clipMax);
 */

//namespace simgear    {        namespace expression        {




/*
class VariableLengthBinding extends Binding {
    List<Value> _bindings;

    /**
     * Liefert die Adresse des ersten Elements und damit die Liste selber.
     * @return
     * /
    /*public          Value getBindings()        {
        if (_bindings.empty())
        return null;
        else
        return &_bindings[0];
        }* /
       /* Value* getBindings()
        {
        if (_bindings.empty())
        return 0;
        else
        return &_bindings[0];
        }* /
}*/

//template<int Size>
/*class FixedLengthBinding extends Binding {
    Value _bindings[    Size];
    /**
     * Liefert die Adresse des ersten Elements und damit die Liste selber.
     * @return
     * /
        /*    public        Value* getBindings()
        {
        return &_bindings[0];
        }
          Value* getBindings()  
        {
        return &_bindings[0];
        }* /
}*/


/**
 * A predicate that wraps, for example the STL template predicate
 * expressions like std::equal_to.
 */
//template<typename OpType, template<typename PredOp> class Pred>
/*class PredicateExpression<OpType> extends GeneralNaryExpression<Boolean, OpType> {
    Pred<OpType> _pred;

    public PredicateExpression() {
    }

    PredicateExpression(SGExpression<OpType> expr0, SGExpression<OpType> expr1) {
        super(expr0, expr1);
    }

    void eval(bool&value, Binding b) {
        int sz = this.getNumOperands();
        if (sz != 2)
            return;
        value = _pred(this.getOperand(0).getValue(b),
                this.getOperand(1).getValue(b));
    }


    static PredicateExpression makePredicate(SGExpression<OpType> op1, SGExpression<OpType> op2) {
        return new PredicateExpression<OpType, Pred>(op1, op2);
    }
}*/

/*
//template<typename OpType>
class EqualToExpression<OpType> extends PredicateExpression<OpType> {//}, std::equal_to>        {

    public EqualToExpression() {
    }

    EqualToExpression(SGExpression<OpType> expr0, SGExpression<OpType> expr1) {
        super(expr0, expr1);
    }
}


//template<typename OpType>
class LessExpression<OpType> extends PredicateExpression<OpType> {//}, std::less>     

    public LessExpression() {
    }

    LessExpression(SGExpression<OpType>/*** /expr0, SGExpression<OpType> /*** /expr1) {
        super(/*:PredicateExpression<OpType, std::less>(* /expr0, expr1);

    }
}

;

//template<typename OpType>
class LessEqualExpression<OpType> extends PredicateExpression<OpType> {//}, std::less_equal>


    public LessEqualExpression() {
    }

    LessEqualExpression(SGExpression<OpType>/*** /expr0, SGExpression<OpType>/*** /expr1) {
        super/*:PredicateExpression<OpType, std::less_equal>* /(expr0, expr1)
        {
        }
    }
}

class NotExpression extends SGUnaryExpression<Boolean> {
    public NotExpression(SGExpression<Boolean> expr/*=0* /) {
        super/*: ::SGUnaryExpression<bool>* /(expr);

    }

       /* void eval(bool&value, Binding b) {
            value = !getOperand().getValue(b);
        }* /
}
*/

class OrExpression extends SGNaryExpression/*<Boolean>*/ {
    //public void eval(bool&value, expression::Binding*b) {
    @Override
    public PrimitiveValue eval(Binding b) {
        boolean value = false;
        for (int i = 0; i < (int) getNumOperands(); ++i) {
            // TODO fix implementation by boolean
            value = value || (getOperand(i).getValue(b) != null && getOperand(i).getValue(b).doubleVal != 0);
            if (value)
                return new PrimitiveValue(1);
        }
        return new PrimitiveValue(0);
    }
}


class AndExpression extends SGNaryExpression/*<Boolean>*/ {
    //public void eval(bool&value, Binding b) {
    @Override
    public PrimitiveValue eval(Binding b) {
        boolean value = true;
        for (int i = 0; i < (int) getNumOperands(); ++i) {
            // TODO fix implementation by boolean
            value = value && getOperand(i).getValue(b) != null && getOperand(i).getValue(b).doubleVal != 0;
            //if (!value)
            //  return;
        }
        return new PrimitiveValue(0);
    }
}

/**
 * Convert an operand from OpType to T.
 */
//template<typename T, typename OpType>
/*class ConvertExpression<T, OpType> extends GeneralNaryExpression<T, OpType> {
    public ConvertExpression() {


        ConvertExpression(SGExpression < OpType > * expr0)
        {
            this.addOperand(expr0);
        }

    void eval(T value, Binding b) {
        //typename ConvertExpression::operand_type result;
        this._expressions.at(0).eval(result, b);
        value = result;
    }
}*/

// #endif // _SG_EXPRESSION_HXX





/*    template<typename T> struct TypeTraits;
        template<> struct TypeTraits<bool> {
static   Type typeTag=BOOL;
        };
        template<> struct TypeTraits<int>{
static   Type typeTag=INT;
        };
        template<> struct TypeTraits<float>{
static   Type typeTag=FLOAT;
        };
        template<> struct TypeTraits<double>{
static   Type typeTag=DOUBLE;
        };*/


class Value {
    int/*Type*/ typeTag;
    //union    {
    boolean boolVal;
    int intVal;
    float floatVal;
    double doubleVal;
    //}

    /*val;

    Value() :

    typeTag(DOUBLE) {
        val.doubleVal = 0.0;
    }

    Value(boolean val_) :

    typeTag(BOOL) {
        val.boolVal = val_;
    }

    Value(int val_) :

    typeTag(INT) {
        val.intVal = val_;
    }

    Value(float val_) :

    typeTag(FLOAT) {
        val.floatVal = val_;
    }

    Value(double val_) :

    typeTag(DOUBLE) {
        val.doubleVal = val_;
    }*/

}


/**
 * Ersatz für die FG Typisierung/Template eines Wertes.
 */
 /*class Wert extends PrimitiveValue {
    boolean boolVal;
    int intVal;
    float floatVal;
    double doubleVal;
    String stringVal;

    public Wert(){
        
    }
    
    public Wert(String str) {
        this.stringVal = str;
    }
    //}

}

class WertInt extends Wert{
    WertInt(int v){
    intVal=v;
    }
}*/

