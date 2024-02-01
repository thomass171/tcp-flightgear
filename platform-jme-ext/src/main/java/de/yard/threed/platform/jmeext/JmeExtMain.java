package de.yard.threed.platform.jmeext;

import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformFactory;
import de.yard.threed.core.platform.PlatformInternals;
import de.yard.threed.flightgear.TerraSyncBundleResolver;
import de.yard.threed.javacommon.DefaultResourceReader;
import de.yard.threed.outofbrowser.SimpleBundleResolver;
import de.yard.threed.platform.jme.JmePlatformFactory;
import de.yard.threed.platform.jme.PlatformJme;

import java.util.HashMap;

/**
 * Extension of platform-jme main for easier parameter setting and dev cycles.
 * <p>
 * 20.9.23
 */
public class JmeExtMain extends de.yard.threed.platform.jme.Main {

    public JmeExtMain(String[] args) {
        super(args);
    }

    public static void main(String[] args) {
        new JmeExtMain(args);
    }

    @Override
    public HashMap<String, String> getInitialProperties() {
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("enableUsermode", "false");
        properties.put("visualizeTrack", "true");
        properties.put("enableHud", "true");

        boolean emulateVR = false;
        if (emulateVR) {
            properties.put("emulateVR", "true");
            properties.put("yoffsetVR", "0.3");
        }

        properties.put("logging.level.de.yard.threed","DEBUG");

        //properties.put("argv.initialVehicle", "c172p");
        //Evtl. Bluebird statt c172p wegen sonst verdecktem menu.
        //properties.put("argv.initialVehicle", "bluebird");
        //properties.put("argv.basename","B55-B477");
        //properties.put("argv.basename","B55-B477-small");

        //properties.put("argv.enableAutomove", "true");
        //properties.put("argv.enableFPC", "true");
        //18.11.19: NearView geht in VR eh nicht, darum damit üblicherweise auch sonst nicht arbeiten.
        //properties.put("argv.enableNearView", "true");

        //properties.put("argv.vehiclelist","GenericRoad");

        // traffic-fg
        properties.put("scene", "de.yard.threed.trafficfg.apps.SceneryScene");
        //properties.put("scene", "de.yard.threed.trafficfg.apps.RailingScene");
        boolean demo = false;
        if (demo) {
            // t.b.c.
            properties.put("basename", "trafficfg:flight/Demo.xml");
            properties.put("scene", "de.yard.threed.traffic.apps.BasicTravelScene");
        }

        // traffic-advanced
        properties.put("scene", "de.yard.threed.trafficadvanced.apps.FlatAirportScene");
        //properties.put("scene", "de.yard.threed.trafficadvanced.apps.TravelScene");
        properties.put("scene", "de.yard.threed.trafficadvanced.apps.HangarScene");

        return properties;
    }

    @Override
    protected PlatformFactory getPlatformFactory(Configuration configuration) {
        return new PlatformFactory() {
            @Override
            public PlatformInternals createPlatform(Configuration configuration) {
                PlatformInternals platformInternals = PlatformJme.init(configuration);
                Platform.getInstance().addBundleResolver(new TerraSyncBundleResolver(configuration.getString("HOSTDIRFG") + "/bundles"));
                Platform.getInstance().addBundleResolver(new SimpleBundleResolver(configuration.getString("HOSTDIRFG") + "/bundles", new DefaultResourceReader()));
                return platformInternals;
            }
        };

    }
}
