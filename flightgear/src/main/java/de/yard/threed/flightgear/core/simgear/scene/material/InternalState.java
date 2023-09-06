package de.yard.threed.flightgear.core.simgear.scene.material;

import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.flightgear.core.FlightGear;
import de.yard.threed.core.Pair;
import de.yard.threed.flightgear.core.simgear.scene.util.SGReaderWriterOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * inner class from SGMaterial mat.[hc]xx
 * 
 * Created by thomass on 01.09.16.
 */
public class InternalState {
    /*osg::ref_ptr<simgear::*/ FGEffect effect;
    public List<Pair</* std::vector<std::pair<std::s*/BundleResource, Integer>> texture_paths = new ArrayList<Pair<BundleResource, Integer>>();
    public boolean effect_realized;
    /*osg::ref_ptr<const simgear::*/ SGReaderWriterOptions options;

    InternalState(FGEffect e, boolean l, SGReaderWriterOptions o) {
        this.effect = e;
        effect_realized = l;
        options = o;
    }

    InternalState(FGEffect e, BundleResource texturepath, boolean l, SGReaderWriterOptions o) {
        this.effect = e;
        effect_realized = l;
        options = o;
        texture_paths.add(FlightGear.make_pair(texturepath, 0));
    }

    void add_texture(BundleResource texturepath, int i) {
        texture_paths.add(FlightGear.make_pair(texturepath, i));
    }

}

