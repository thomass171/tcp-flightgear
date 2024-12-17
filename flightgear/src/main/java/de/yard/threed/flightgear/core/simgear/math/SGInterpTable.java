package de.yard.threed.flightgear.core.simgear.math;

import de.yard.threed.core.Util;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.misc.SGPath;

import java.util.List;
import java.util.NavigableSet;
import java.util.TreeMap;

/**
 * from interpolater.[ch]xx
 * <p>
 * Created by thomass on 28.12.16.
 */
public class SGInterpTable {
    //typedef std::map<double, double> Table;
    //Table _table;
    TreeMap<Double, Double> _table = new TreeMap<>();

    /**
     * Constructor. Creates a new, empty table.
     */
    SGInterpTable() {
        Util.notyet();
    }

    /**
     * Constructor. Loads the interpolation table from an interpolation
     * property node.
     *
     * @param interpolation property node having entry children
     */
    public SGInterpTable(SGPropertyNode interpolation) {
        if (interpolation == null)
            return;
        List<SGPropertyNode> entries = interpolation.getChildren("entry");
        for (int i = 0; i < entries.size(); ++i)
            addEntry(entries.get(i).getDoubleValue("ind", 0.0),
                    entries.get(i).getDoubleValue("dep", 0.0));
    }

    /**
     * Constructor. Loads the interpolation table from the specified file.
     *
     * @param file name of interpolation file
     */
    SGInterpTable(String file) {
        Util.notyet();
    }

    /**
     * Constructor. Loads the interpolation table from the specified file.
     *
     * @param path name of interpolation file
     */
    SGInterpTable(SGPath path) {
        Util.notyet();
    }

    /**
     * Add an entry to the table, extending the table's length.
     *
     * @param ind The independent variable.
     * @param dep The dependent variable.
     */
    void addEntry(double ind, double dep) {
        //_table[ind] = dep;
        _table.put(ind, dep);
    }


    /**
     * Given an x value, linearly interpolate the y value from the table.
     *
     * FG-DIFF C++ upper_bound() replaced with Java TreeMap
     * @param x independent variable
     * @return interpolated dependent variable
     */
    public double interpolate(double x) {
        // Empty table??
        if (_table.isEmpty())
            return 0;

        // Find the table bounds for the requested input.
        NavigableSet<Double> keys = _table.navigableKeySet();
        Double higher = keys.higher(x);
        //Table::const_iterator upBoundIt = _table.upper_bound(x);
        // points to a value outside the map. That is we are out of range.
        // use the last entry
        if (higher == null/* upBoundIt == _table.end()*/)
            return _table.get(_table.lastKey());//rbegin()->getSecond;

        Double lower = keys.lower(x);
        // points to the getFirst key must be lower
        // use the getFirst entry
        //if (upBoundIt == _table.begin())
        if (lower == null)
            return _table.get(_table.firstKey());//upBoundIt->getSecond;

        // we know that we do not stand at the beginning, so it isType safe to do so
        //Table::const_iterator loBoundIt = upBoundIt;
        //--loBoundIt;

        // Just do linear interpolation.
        double loBound = lower;//loBoundIt -> getFirst;
        double upBound = higher;//upBoundIt -> getFirst;
        double loVal = _table.get(lower);//loBoundIt -> getSecond;
        double upVal = _table.get(higher);//upBoundIt -> getSecond;

        // division by zero should not happen since the std::map
        // has sorted out duplicate entries before. Also since we have a
        // map, we know that we get different getFirst values for different iterators
        return loVal + (upVal - loVal) * (x - loBound) / (upBound - loBound);

    }

    /** Destructor */
    //~SGInterpTable();

}
