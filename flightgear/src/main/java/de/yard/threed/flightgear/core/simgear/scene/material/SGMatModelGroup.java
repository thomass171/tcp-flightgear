package de.yard.threed.flightgear.core.simgear.scene.material;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.core.platform.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Aus matmodel.[ch]xx
 * <p/>
 * A collection of related objects with the same visual range.
 * <p/>
 * Grouping objects with the same range together significantly
 * reduces the memory requirements of randomly-placed objects.
 * Each SGMaterial instance keeps a (possibly-empty) list of
 * object groups for placing randomly on the scenery.
 * <p/>
 * Created by thomass on 08.08.16.
 */
public class SGMatModelGroup {
    static Log logger = Platform.getInstance().getLog(SGMatModelGroup.class);

    double _range_m;
    List</*<SGSharedPtr<*/SGMatModel> _objects = new ArrayList<SGMatModel>();

    public SGMatModelGroup(SGPropertyNode node) {
        _range_m = node.getDoubleValue("range-m", 2000);
        // Load the object subnodes
        List<SGPropertyNode/*_ptr*/> object_nodes = ((SGPropertyNode) node).getChildren("object");
        for (int i = 0; i < object_nodes.size(); i++) {
            SGPropertyNode object_node = object_nodes.get(i);
            if (object_node.hasChild("path"))
                _objects.add(new SGMatModel(object_node, _range_m));
            else
                logger.error(/*SG_LOG(SG_INPUT, SG_ALERT,*/ "No path supplied for object");
        }
    }

    /**
     * Get the visual range of the object in meters.
     *
     * @return The visual range.
     */
    double get_range_m() {
        return _range_m;
    }


    /**
     * Get the number of objects in the group.
     *
     * @return The number of objects.
     */
    int get_object_count() {
        return _objects.size();
    }


    /**
     * Get a specific object.
     *
     * @param index The object's index, zero-based.
     * @return The object selected.
     */
    SGMatModel get_object(int index) {
        return _objects.get(index);
    }

}
