package de.yard.threed.flightgear.core;

import de.yard.threed.flightgear.core.simgear.CppFactory;

import java.util.HashMap;

/**
 * C++ might do an auto create on get()!
 * <p>
 * Created by thomass on 05.08.16.
 */
public class CppHashMap<T1, T2> extends HashMap<T1, T2> {
    //T2 creatordummy;
    CppFactory<T2> factory;

    public CppHashMap(/*T2 creatordummy*/CppFactory<T2> factory) {
        this.factory = factory;
    }

    /**
     * C# moechte fuer override ein T1 haben, Java aber Object. Konvertierung hardcoded in Converter.
     *
     * @param key
     * @return
     */
    @Override
    public T2 get(Object key) {
        T2 o = super.get(key);
        if (o != null) {
            return o;
        }
        T2 newentry = factory.createInstance();//createContents();
        put((T1) key, newentry);
        return newentry;
    }

    /**
     * 14.6.25 added for testing(?)
     */
    public T2 find(Object key) {
        return super.get(key);
    }

   /* private T2 createContents()
    {
        try {
            return (T2) creatordummy.getClass().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }*/
}
