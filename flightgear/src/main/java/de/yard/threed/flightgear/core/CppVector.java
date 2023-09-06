package de.yard.threed.flightgear.core;

import java.util.ArrayList;

/**
 * Nachbildung eines C++ Vector
 * 
 * Created by thomass on 30.05.16.
 */
public class CppVector<T> extends ArrayList<T> {
    public boolean empty() {
        return size()==0;
    }

    public T front() {
        return get(0);
    }


    public T back() {
        return get(size()-1);
    }

    /*muesste iterator liefern. C++woodo public int end(){
        return size();
    }*/

 
}
