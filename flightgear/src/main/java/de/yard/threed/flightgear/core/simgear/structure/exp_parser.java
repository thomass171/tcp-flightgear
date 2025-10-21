package de.yard.threed.flightgear.core.simgear.structure;

import de.yard.threed.flightgear.core.simgear.SGPropertyNode;

import java.util.ArrayList;
import java.util.List;

/**
 * From ExpressionParser.hxx
 */
public
interface exp_parser {
    // FG-DIFF: use SGExpression instead of Expression because they are not easy to interact in lists (addOperands)
    SGExpression parse(SGPropertyNode exp, Parser parser) throws ParseError;
}
