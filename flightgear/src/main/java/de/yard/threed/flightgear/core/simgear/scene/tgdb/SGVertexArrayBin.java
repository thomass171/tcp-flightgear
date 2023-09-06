package de.yard.threed.flightgear.core.simgear.scene.tgdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Aus SGVertexArrayBin.hxx
 * 
 * Das ist ja auch wieder so'ne Konstruktion.
 * Der Sinn ist nicht ganz, daher erstmal als einfache Liste mit Map zur Prüfung auf Duplikate.
 * 
 * Created by thomass on 04.08.16.
 */
public class SGVertexArrayBin {
    //ValueVector _values;
    /*Value*/HashMap<String,Integer> _valueMap = new HashMap<String, Integer>();
    public List<SGVertNormTex> _values = new ArrayList<SGVertNormTex>();
    
    /*typedef T value_type;
    typedef typename value_type::less less;
    typedef std::vector<value_type> ValueVector;
    typedef typename ValueVector::size_type index_type;
    typedef std::map<value_type, index_type, less> ValueMap;
    */

    /*index_type*/int insert( /*value_type&*/SGVertNormTex t)
    {
        /*typename ValueMap::iterator i = _valueMap.find(t);
        if (i != _valueMap.end())
            return i->getSecond;*/
        String key = ((SGVertNormTex)t).toKey();
        Integer i = _valueMap.get(key);
        if (i!=null){
            return (int)i;
        }
        /*index_type*/int index = _values.size();
        _valueMap.put(key,index);
        _values.add(t);
        return index;
    }

     /*value_type&*/SGVertNormTex getVertex(/*index_type*/int index)     { 
        return _values.get(index); 
    }

    /*index_type*/int getNumVertices()     { 
        return _values.size(); 
    }

    boolean empty()     { 
        return _values.isEmpty(); 
    }

}