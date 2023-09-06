package de.yard.threed.flightgear.core.simgear.props;

/**
 * Aus props.hxx
 * Hier gibt es auch multi inheritance mit SGRawextended!
 * <p/>
 * Created by thomass on 30.05.16.
 */
public class SGRawBase<T> extends SGRaw {
    T value;

    public T getValue() {
        return value;
    }

    public boolean setValue(T v) {
        value = v;
        return true;//TODO OK?
    }

    
}

