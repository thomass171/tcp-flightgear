package de.yard.threed.flightgear.core.flightgear.main;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.flightgear.core.StringList;
import de.yard.threed.flightgear.core.simgear.misc.SGPath;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleResourceProvider;
import de.yard.threed.core.StringUtils;

/**
 * Created by thomass on 30.05.16.
 * 22.10.17: Kann jetzt auch ohne Property "/sim/aircraft-dir" arbeiten.
 * 02.10.19: Das ist doch Bundle bezogen. TODO Darum die Constructor mal deprecated oder aendern.
 */
public class AircraftResourceProvider implements /*30.9.19ResourceProvider,*/ BundleResourceProvider {
    private Log logger = Platform.getInstance().getLog(AircraftResourceProvider.class);
    private String aircraftdir = null;

    public AircraftResourceProvider() {
        //TODO super     simgear::ResourceProvider(simgear::ResourceManager::PRIORITY_HIGH)
        int i = 99;
    }

    public AircraftResourceProvider(String aircraftdir) {
        this.aircraftdir = aircraftdir;
    }

    /*30.9.19@Override
    public SGPath resolve(String aResource, SGPath aContext) {

        StringList pieces = StringList.sgPathBranchSplit(aResource);
        if ((pieces.size() < 3) || (!pieces.front().equals("Aircraft"))) {
            //logger.debug("not an aircraft path");
            return new SGPath(); // not an Aircraft path
        }

        // test against the aircraft-dir property
        String aircraftDir = FGProperties.fgGetString("/sim/aircraft-dir");
        StringList aircraftDirPieces = StringList.sgPathBranchSplit(aircraftDir);
        if (!aircraftDirPieces.empty() && (aircraftDirPieces.back().equals(pieces.get(1)))) {
            // current aircraft-dir matches resource aircraft
            SGPath r = new SGPath(aircraftDir);
            for (int i = 2; i < pieces.size(); ++i) {
                r.append(pieces.get(i));
            }

            if (r.exists()) {
                logger.debug("found aircraft path " + r.str());

                return r;
            }
        }

        // try each aircraft dir in turn
        // 27.12.16: Manche haben vorn ein Slash und manche nicht?
        String res = StringUtils.substring(aResource, 9); // resource path with 'Aircraft/' removed
        StringList dirs = FGGlobals.getInstance().get_aircraft_paths();
        //string_list::const_iterator it = dirs.begin();
        //for (; it != dirs.end(); ++it) {
        for (String it : dirs) {
            SGPath p = new SGPath(new SGPath(it), res);
            if (p.exists()) {
                return p;
            }
        } // of aircraft path iteration

        //zu oft logger.debug("found no aircraft path ");

        return new SGPath(); // not found
    }*/

    /**
     * Resolving for bundle.
     *
     * @return
     */
    public BundleResource resolve(String resource/*,Bundle currentbundle*/) {

        StringList pieces = StringList.sgPathBranchSplit(resource);
        if ((pieces.size() < 3) || (!pieces.front().equals("Aircraft"))) {
            //logger.debug("not an aircraft path");
            return null;//new SGPath(); // not an Aircraft path
        }

        // test against the aircraft-dir property
        // doppelt zu CurrentAircraftDirProvider, aber evlt. wegen Prio? Nicht ganz, hier wird ja mit Sonderpfad gesucht.
        String aircraftDir = aircraftdir;
        if (aircraftDir == null) {
            aircraftDir = FGProperties.fgGetString("/sim/aircraft-dir");
        }
        StringList aircraftDirPieces = StringList.sgPathBranchSplit(aircraftDir);
        if (!aircraftDirPieces.empty() && (aircraftDirPieces.back().equals(pieces.get(1)))) {
            // current aircraft-dir matches resource aircraft
            Bundle bundle = BundleRegistry.getBundle(aircraftDir);

            if (bundle==null){
                // this isType an error. bundle isType expected to exist
                logger.error("aircraftDir bundle "+aircraftDir+" does not exist");
                return null;
            }
            SGPath r = new SGPath(pieces.get(2)/*aircraftDir*/);
            for (int i = 3; i < pieces.size(); ++i) {
                r.append(pieces.get(i));
            }

            if (bundle.exists(new BundleResource(r.str()))) {
                //logger.debug("found aircraft path "+r.str());
                return new BundleResource(bundle/*bundle.name*/, r.str());
            }
            //logger.debug(r.str()+" not exists in bundle "+bundle.name);
        }

        // try each aircraft dir in turn
        // 27.12.16: Manche haben vorn ein Slash und manche nicht?
        String res = StringUtils.substring(resource, 9); // resource path with 'Aircraft/' removed
        //28.9.19: Ist das noch zeitgemeass? Erzeugt im FGGlobals.init() evtl. eine self modification. Die AircraftResourceProvider nutzen
        //doch Bundle statt aircraft path. Mal uebergehen.
        logger.warn("Skipping deprecated resolve through aircraft dirs");
        if (false) {
            /*30.9.19 StringList dirs = FGGlobals.getInstance().get_aircraft_paths();
            //string_list::const_iterator it = dirs.begin();
            //for (; it != dirs.end(); ++it) {
            for (String it : dirs) {
                SGPath p = new SGPath(new SGPath(it), res);
                if (p.exists()) {
                    return new BundleResource(p.str());
                }
            } // of aircraft path iteration*/
        }
        return null;// not found
    }

    @Override
    public boolean isAircraftSpecific() {
        return true;
    }

    public void setAircraftDir(String aircraftdir) {
        this.aircraftdir = aircraftdir;
    }
}

