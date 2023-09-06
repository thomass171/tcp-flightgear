package de.yard.threed.flightgear.core.simgear.math;

import de.yard.threed.core.Util;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.misc.SGPath;

import java.util.List;

/**
 * from interpolater.[ch]xx
 * 
 * Created by thomass on 28.12.16.
 */
public class SGInterpTable {
    //typedef std::map<double, double> Table;
    //Table _table;
   // Map<Double>
    
    /**
     * Constructor. Creates a new, empty table.
     */
    SGInterpTable(){
        Util.notyet();
    }

    /**
     * Constructor. Loads the interpolation table from an interpolation
     * property node.
     * @param interpolation property node having entry children
     */
    public SGInterpTable(SGPropertyNode interpolation){
        if (interpolation==null)
            return;
        List<SGPropertyNode> entries = interpolation.getChildren("entry");
        for (int i = 0; i < entries.size(); ++i)
            addEntry(entries.get(i).getDoubleValue("ind", 0.0),
                entries.get(i).getDoubleValue("dep", 0.0));
    }

    /**
     * Constructor. Loads the interpolation table from the specified file.
     * @param file name of interpolation file
     */
    SGInterpTable( String file ){
        Util.notyet();
    }

    /**
     * Constructor. Loads the interpolation table from the specified file.
     * @param path name of interpolation file
     */
    SGInterpTable(  SGPath path ){
        Util.notyet();
    }

    /**
     * Add an entry to the table, extending the table's length.
     *
     * @param ind The independent variable.
     * @param dep The dependent variable.
     */
    void addEntry (double ind, double dep){
       //TODO  Util.notyet();
    }


    /**
     * Given an x value, linearly interpolate the y value from the table.
     * @param x independent variable
     * @return interpolated dependent variable
     */
    public double interpolate(double x) {
        //TODO knifflig
        return x;
        // Empty table??
       /* if (_table.empty())
            return 0;

        // Find the table bounds for the requested input.
        Table::const_iterator upBoundIt = _table.upper_bound(x);
        // points to a value outside the map. That isType we are out of range.
        // use the last entry
        if (upBoundIt == _table.end())
            return _table.rbegin()->getSecond;

        // points to the getFirst key must be lower
        // use the getFirst entry
        if (upBoundIt == _table.begin())
            return upBoundIt->getSecond;

        // we know that we do not stand at the beginning, so it isType safe to do so
        Table::const_iterator loBoundIt = upBoundIt;
        --loBoundIt;

        // Just do linear interpolation.
        double loBound = loBoundIt->getFirst;
        double upBound = upBoundIt->getFirst;
        double loVal = loBoundIt->getSecond;
        double upVal = upBoundIt->getSecond;

        // division by zero should not happen since the std::map
        // has sorted out duplicate entries before. Also since we have a
        // map, we know that we get different getFirst values for different iterators
        return loVal + (upVal - loVal)*(x - loBound)/(upBound - loBound);
        */
    }

    /** Destructor */
    //~SGInterpTable();

   
 
}
