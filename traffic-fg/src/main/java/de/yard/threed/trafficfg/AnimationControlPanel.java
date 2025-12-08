package de.yard.threed.trafficfg;

import de.yard.threed.core.Color;
import de.yard.threed.core.DimensionF;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.gui.*;
import de.yard.threed.flightgear.core.flightgear.main.FGGlobals;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;

/**
 * For modifying single properties in the property tree via menu.
 * We also have ... in HangarScene.
 *
 * Extracted from FgGalleryScene.
 */
public class AnimationControlPanel {
    public static Log logger = Platform.getInstance().getLog(AnimationControlPanel.class);

    /**
     * A control panel to be used in a menu or permanently attached to the left controller in VR. Only
     * for controlling FG properties in the global tree. So no callback is needed.
     * <p>
     * Consists of
     * <p>
     * <p>
     * top line: vr y offset spinner
     * medium: spinner for teleport toggle
     */
    public static ControlPanel buildAnimationControlPanel(/*Map<String, ButtonDelegate> buttonDelegates*/) {
        Color backGround = Color.RED;//controlPanelBackground;
        Material mat = Material.buildBasicMaterial(backGround, null);

        double ControlPanelWidth = 0.6;
        double ControlPanelRowHeight = 0.1;
        double ControlPanelMargin = 0.005;

        int rows = 4;
        DimensionF rowsize = new DimensionF(ControlPanelWidth, ControlPanelRowHeight);
        Color textColor = Color.RED;

        ControlPanel cp = new ControlPanel(new DimensionF(ControlPanelWidth, rows * ControlPanelRowHeight), mat, 0.01);

        // for ASI speed needle
        addDoublePropertyRow(cp, 3, "/fdm/jsbsim/velocities/vias-kts", "vias-kts", rows, ControlPanelRowHeight, rowsize, mat, textColor, 5.0, 0);

        SGPropertyNode windFromHeadingDeg = FGGlobals.getInstance().get_props().getNode("/environment/wind-from-heading-deg", false);
        cp.add(new Vector2(0,
                        ControlPanelHelper.calcYoffsetForRow(2, rows, ControlPanelRowHeight)),
                new LabeledSpinnerControlPanel("wnd hdg", rowsize, 0, mat,
                        new NumericSpinnerHandler(15, value -> {
                            if (value != null) {
                                windFromHeadingDeg.setDoubleValue(value);
                            }
                            return windFromHeadingDeg.getDoubleValue();
                        }, 360, new NumericDisplayFormatter(0)), textColor));

        addDoublePropertyRow(cp, 1, "/environment/wind-speed-kt", "wnd spd", rows, ControlPanelRowHeight, rowsize, mat, textColor, 5.0, 0);
        addDoublePropertyRow(cp, 0, "/sim/time/sun-angle-rad", "sun angle", rows, ControlPanelRowHeight, rowsize, mat, textColor, 0.7, 0);
        return cp;
    }

    /**
     * Just increase/decrease a property value step wise.
     */
    private static void addDoublePropertyRow(ControlPanel cp, int row, String inputProperty, String label, int rows, double controlPanelRowHeight, DimensionF rowsize,
                                             Material mat, Color textColor, double step, int precision) {
        SGPropertyNode propertyNode = FGGlobals.getInstance().get_props().getNode(inputProperty, false);
        if (propertyNode == null) {
            logger.error("Property not found: " + inputProperty);
            return;
        }
        cp.add(new Vector2(0,
                        ControlPanelHelper.calcYoffsetForRow(row, rows, controlPanelRowHeight)),
                new LabeledSpinnerControlPanel(label, rowsize, 0, mat,
                        new NumericSpinnerHandler(step, value -> {
                            if (value != null) {
                                propertyNode.setDoubleValue(value);
                            }
                            return propertyNode.getDoubleValue();
                        }, null, new NumericDisplayFormatter(precision)), textColor));
    }
}
