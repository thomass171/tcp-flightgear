package de.yard.threed.trafficfg.flight;

import de.yard.threed.core.Color;
import de.yard.threed.core.DimensionF;
import de.yard.threed.core.GeneralHandler;
import de.yard.threed.core.IntHolder;
import de.yard.threed.core.Payload;
import de.yard.threed.core.SpinnerHandler;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.ecs.InputToRequestSystem;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.gui.ButtonDelegate;
import de.yard.threed.engine.gui.ControlPanel;
import de.yard.threed.engine.gui.ControlPanelArea;
import de.yard.threed.engine.gui.ControlPanelHelper;
import de.yard.threed.engine.gui.Icon;
import de.yard.threed.engine.gui.NumericSpinnerHandler;
import de.yard.threed.engine.gui.PanelGrid;
import de.yard.threed.engine.gui.SelectSpinnerHandler;
import de.yard.threed.engine.gui.SpinnerControlPanel;
import de.yard.threed.engine.gui.TextTexture;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.vr.VrOffsetWrapper;
import de.yard.threed.traffic.RequestRegistry;
import de.yard.threed.trafficfg.TravelHelper;

public class FlightVrControlPanel {

    /**
     * A common flight traffic 4x3 control panel permanently attached to the left controller. Consists of
     * <p>
     * 0) finetune spinner - empty
     * 1) aircraft selector
     * 2) service - trip - finetune down
     * teleport not needed because already on VR button. But have it just redundant for now?
     */
    public static ControlPanel buildVrControlPanel(
            boolean withLoad,
            SpinnerHandler tripSpinnerHandler, ButtonDelegate tripStarter,
            SpinnerHandler serviceSpinnerHandler, ButtonDelegate serviceStarter
    ) {

        Log logger = Platform.getInstance().getLog(FlightVrControlPanel.class);
        double ControlPanelWidth = 0.6;
        double ControlPanelRowHeight = 0.1;
        int ControlPanelRows = 5;
        double[] ControlPanelColWidth = new double[]{0.1, 0.1, 0.1, 0.1, 0.1, 0.1};
        double ControlPanelMargin = 0.005;
        Color controlPanelBackground = Color.LIGHTBLUE;

        // keep space for right button by -0.1
        DimensionF spinnerSize = new DimensionF(ControlPanelWidth - 0.1, ControlPanelRowHeight);
        Material mat = Material.buildBasicMaterial(controlPanelBackground, null);

        ControlPanel cp = new ControlPanel(new DimensionF(ControlPanelWidth, ControlPanelRows * ControlPanelRowHeight), mat, 0.01);
        PanelGrid panelGrid = new PanelGrid(ControlPanelWidth, ControlPanelRowHeight, ControlPanelRows, ControlPanelColWidth);

        // line 0: property control for yvroffset
        cp.add(panelGrid.getPosition(2, 4), new SpinnerControlPanel(spinnerSize, ControlPanelMargin, mat,
                new NumericSpinnerHandler(0.1, new VrOffsetWrapper()), Color.BLUE));

        // line 1:
        // Use 'S' instead of ICON_TURNRIGHT to have consistent start trigger
        cp.add(panelGrid.getPosition(2, 3), new SpinnerControlPanel(spinnerSize, ControlPanelMargin, mat, tripSpinnerHandler, Color.BLUE));
        cp.addArea(panelGrid.getPosition(5, 3), new DimensionF(ControlPanelColWidth[5], ControlPanelRowHeight), () -> {
            //logger.debug("starting trip " + tripSelectSpinnerHandler.getValue());
            if (tripStarter != null) {
                tripStarter.buttonpressed();
            }
        }).setIcon(Icon.IconCharacter(18));

        // line 2:
        if (serviceSpinnerHandler != null) {
            cp.add(panelGrid.getPosition(2, 2), new SpinnerControlPanel(spinnerSize, ControlPanelMargin, mat, serviceSpinnerHandler, Color.BLUE));
            cp.addArea(panelGrid.getPosition(5, 2), new DimensionF(ControlPanelColWidth[5], ControlPanelRowHeight), () -> {
                //logger.debug("starting service " + aircraftSelectSpinnerHandler.getValue());
                if (serviceStarter != null) {
                    serviceStarter.buttonpressed();
                }
            }).setIcon(Icon.ICON_TURNRIGHT);
        }

        // line 3
        // text has no margin yet.
        TextTexture textTexture = new TextTexture(Color.LIGHTGRAY);
        ControlPanelArea textArea = cp.addArea(panelGrid.getPosition(2, 1), spinnerSize, null);
        textArea.setTexture(textTexture.getTextureForText("Auto Move", Color.BLUE));
        cp.addArea(panelGrid.getPosition(5, 1), new DimensionF(ControlPanelColWidth[5], ControlPanelRowHeight), () -> {
            SystemManager.putRequest(new Request(UserSystem.USER_REQUEST_AUTOMOVE));
        }).setIcon(Icon.ICON_TURNRIGHT);

        // bottom line: Teleport (Even its redundant to controller button), Load,
        cp.addArea(panelGrid.getPosition(0, 0), new DimensionF(ControlPanelColWidth[0], ControlPanelRowHeight), () -> {
            InputToRequestSystem.sendRequestWithId(new Request(UserSystem.USER_REQUEST_TELEPORT, new Payload(new Object[]{new IntHolder(0)})));
        }).setIcon(Icon.ICON_POSITION);
        if (withLoad) {
            cp.addArea(panelGrid.getPosition(1, 0), new DimensionF(ControlPanelColWidth[1], ControlPanelRowHeight), () -> {
                logger.debug("load clicked");
                SystemManager.putRequest(RequestRegistry.buildLoadVehicle(UserSystem.getInitialUser().getId(), null, null, null));
            }).setIcon(Icon.IconCharacter(11));
        }
        cp.addArea(panelGrid.getPosition(2, 0), new DimensionF(ControlPanelColWidth[2], ControlPanelRowHeight), () -> {
            logger.debug("minus clicked");
        }).setIcon(Icon.ICON_HORIZONTALLINE);
        cp.addArea(panelGrid.getPosition(3, 0), new DimensionF(ControlPanelColWidth[3], ControlPanelRowHeight), () -> {
            logger.debug("plus clicked");
        }).setIcon(Icon.ICON_PLUS);

        return cp;
    }
}
