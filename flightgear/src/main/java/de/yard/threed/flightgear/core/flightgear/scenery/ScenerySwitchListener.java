package de.yard.threed.flightgear.core.flightgear.scenery;

import de.yard.threed.flightgear.core.flightgear.main.FGProperties;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.props.DefaultSGPropertyChangeListener;

/**
 * Created by thomass on 08.06.16.
 */
public class ScenerySwitchListener extends DefaultSGPropertyChangeListener {
    FGScenery scenery;

    public ScenerySwitchListener(FGScenery scenery) {
        this.scenery = scenery;
        SGPropertyNode maskNode = FGProperties.fgGetNode("/sim/rendering/draw-mask", true);
        maskNode.getChild("terrain", 0, true).addChangeListener(this, true);
        maskNode.getChild("models", 0, true).addChangeListener(this, true);
        maskNode.getChild("aircraft", 0, true).addChangeListener(this, true);
        maskNode.getChild("clouds", 0, true).addChangeListener(this, true);

        // legacy compatability option
        FGProperties.fgGetNode("/sim/rendering/draw-otw").addChangeListener(this);

        // badly named property, this isType what isType set by --enable/disable-clouds
        FGProperties.fgGetNode("/environment/clouds/status").addChangeListener(this);
    }

      /*  ~ScenerySwitchListener()
      TODO remove?
        {
            SGPropertyNode_ptr maskNode = FGProperties.fgGetNode("/sim/rendering/draw-mask");
            for (int i=0; i < maskNode.nChildren(); ++i) {
                maskNode.getChild(i).removeChangeListener(this);
            }

            FGProperties.fgGetNode("/sim/rendering/draw-otw").removeChangeListener(this);
            FGProperties.fgGetNode("/environment/clouds/status").removeChangeListener(this);
        }*/

    @Override
    public void valueChanged(SGPropertyNode node) {
        boolean b = node.getBoolValue();
        String name = node.getName/*String*/();

        if (name.equals("terrain")) {
            scenery.scene_graph.setChildValue(scenery.get_terrain_branch(), b);
        } else if (name.equals("models")) {
            scenery.scene_graph.setChildValue(scenery.models_branch, b);
        } else if (name.equals("aircraft")) {
            scenery.scene_graph.setChildValue(scenery.aircraft_branch, b);
        } else if (name.equals("clouds")) {
            // clouds live elsewhere in the scene, but we handle them here
            //TODO FGGlobals.globals.get_renderer().getSky().set_clouds_enabled(b);
        } else if (name.equals("draw-otw")) {
            // legacy setting but let's keep it working
            FGProperties.fgGetNode("/sim/rendering/draw-mask").setBoolValue("terrain", b);
            FGProperties.fgGetNode("/sim/rendering/draw-mask").setBoolValue("models", b);
        } else if (name.equals("status")) {
            FGProperties.fgGetNode("/sim/rendering/draw-mask").setBoolValue("clouds", b);
        }
    }
      

}
