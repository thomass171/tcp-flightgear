package de.yard.threed.flightgear.core.simgear.structure;

/**
 * Created by thomass on 05.09.16.
 */
public class PrimitiveValue {
    public boolean boolVal;
    public int intVal;
    //float floatVal;
    public double doubleVal;
    public String stringVal;

    /*public PrimitiveValue(){

    }*/

    public PrimitiveValue(String str) {
        this.stringVal = str;
    }

    public PrimitiveValue(int v){
        intVal=v;
    }

    public PrimitiveValue(double v){
        doubleVal=v;
    }
}

