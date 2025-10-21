package de.yard.threed.flightgear.core.simgear.structure;

/**
 * From SGExpression.hxx
 */
public class ExpParserRegistrar {
    /**
     * Constructor for registering parser functions.
     */
    public ExpParserRegistrar(String token, /*Parser::*/exp_parser parser) {
            ExpressionParser.addExpParser(token, parser);
        }

}
