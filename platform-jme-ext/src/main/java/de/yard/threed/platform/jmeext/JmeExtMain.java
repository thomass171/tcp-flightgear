package de.yard.threed.platform.jmeext;

import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformFactory;
import de.yard.threed.core.platform.PlatformInternals;
import de.yard.threed.core.resource.BundleResolver;
import de.yard.threed.flightgear.TerraSyncBundleResolver;
import de.yard.threed.javacommon.DefaultResourceReader;
import de.yard.threed.outofbrowser.SimpleBundleResolver;
import de.yard.threed.platform.jme.JmePlatformFactory;
import de.yard.threed.platform.jme.PlatformJme;
import de.yard.threed.trafficfg.SceneSetup;
import de.yard.threed.trafficfg.apps.TravelSceneBluebird;

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
        //13.3.24: No longer exists. Its too unspecific properties.put("enableUsermode", "false");
        properties.put("visualizeTrack", "true");
        properties.put("enableHud", "true");
        properties.put("enableDoormarker", "true");

        boolean emulateVR = false;
        if (emulateVR) {
            properties.put("emulateVR", "true");
            properties.put("yoffsetVR", "0.3");
        }

        properties.put("logging.level.de.yard.threed", "DEBUG");

        // 'initialVehicle' might cause NPE if not found
        //properties.put("initialVehicle", "c172p");
        //properties.put("initialVehicle", "777");
        //properties.put("initialVehicle", "bluebird");
        //properties.put("initialRoute", "wp:50.768,7.1672000->takeoff:50.7692,7.1617000->wp:50.7704,7.1557->wp:50.8176,7.0999->wp:50.8519,7.0921->touchdown:50.8625,7.1317000->wp:50.8662999,7.1443999");


        //properties.put("argv.basename","B55-B477");
        //properties.put("argv.basename","B55-B477-small");

        //properties.put("argv.enableAutomove", "true");
        //properties.put("argv.enableFPC", "true");
        //18.11.19: NearView geht in VR eh nicht, darum damit üblicherweise auch sonst nicht arbeiten.
        //properties.put("argv.enableNearView", "true");
        properties.put("enableNavigator", "true");

        //properties.put("argv.vehiclelist","GenericRoad");

        // traffic-fg
        //properties.put("scene", "de.yard.threed.trafficfg.apps.SceneryScene");
        //properties.put("scene", "de.yard.threed.trafficfg.apps.RailingScene");
        // TravelSceneBluebird needs initialVehicle
        properties.put("scene", "de.yard.threed.trafficfg.apps.TravelSceneBluebird");
        //properties.put("scene", "de.yard.threed.trafficfg.apps.SceneryViewerScene");
        //properties.put("scene", "de.yard.threed.trafficfg.apps.FgModelPreviewScene");
        //properties.put("scene", "de.yard.threed.trafficfg.apps.FgGalleryScene");
        boolean demo = false;
        if (demo) {
            // t.b.c.
            properties.put("basename", "trafficfg:flight/Demo.xml");
            properties.put("scene", "de.yard.threed.traffic.apps.BasicTravelScene");
        }

        // traffic-advanced
        //properties.put("scene", "de.yard.threed.trafficadvanced.apps.FlatAirportScene");
        properties.put("scene", "de.yard.threed.trafficadvanced.apps.TravelScene");
        //properties.put("scene", "de.yard.threed.trafficadvanced.apps.HangarScene");
        properties.put("scene", "de.yard.threed.trafficadvanced.apps.AdvancedSceneryViewerScene");

        boolean blogSample = false;
        if (blogSample) {
            // Greenwich
            properties.put("initialLocation", "51.47752,0,500");
            properties.put("initialHeading", "270");
            // Hamburg (no tile?)
            //properties.put("initialLocation", "53.45,10.08,500");
            //properties.put("initialHeading", "300");
            // Edinburgh
            //properties.put("initialLocation", "55.99,-3.2,1500");
            //properties.put("initialHeading", "270");
            // c172p at EDDK 06
            properties.put("initialVehicle", "c172p");
            properties.put("initialLocation", "geo:50.860122,7.123778");
            properties.put("initialHeading", "64.333166015357");
            properties.put("scene", "de.yard.threed.trafficadvanced.apps.TravelScene");
        }
        //SceneSetup.setupForBluebirdFreeFlight(properties);
        //SceneSetup.setupTravelSceneBluebirdForBluebirdFreeFlightFromEHAM(properties);
        //SceneSetup.setupTravelSceneForC172pFreeFlightFromEHAM(properties);
        //SceneSetup.setupSceneryViewSceneForEHAM(properties);
        return properties;
    }

    @Override
    protected PlatformFactory getPlatformFactory(Configuration configuration) {
        return new PlatformFactory() {
            @Override
            public PlatformInternals createPlatform(Configuration configuration) {
                PlatformInternals platformInternals = PlatformJme.init(configuration);
                Platform.getInstance().addBundleResolver(new TerraSyncBundleResolver(configuration.getString("HOSTDIRFG") + "/bundles"));
                // PlatformJme has built in SimpleBundleResolver for "tcp-22/bundles", so need here for adding any further
                Platform.getInstance().addBundleResolver(new SimpleBundleResolver(configuration.getString("HOSTDIRFG") + "/bundles", new DefaultResourceReader()));
                return platformInternals;
            }
        };

    }
}
