package de.yard.threed.flightgear.core.flightgear.main;

import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.flightgear.core.simgear.misc.ResourceProvider;
import de.yard.threed.flightgear.core.simgear.misc.SGPath;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleResourceProvider;

/**
 * 30.9.19: Der ist doch obsolet? Offenbar.
 *
 * Created by thomass on 30.05.16.
 */
public class CurrentAircraftDirProvider implements ResourceProvider, BundleResourceProvider {
    private CurrentAircraftDirProvider() {
    //TODO super  r(simgear::ResourceManager::PRIORITY_HIGH)
    
    }

    @Override
    public SGPath resolve(String aResource, SGPath aContext) {
        
        String  aircraftDir = FGProperties.fgGetString("/sim/aircraft-dir");
        // Gleicher Nebeneffekt wie FG: Wenn Property nicht gesetzt, loest dieser Provider auch absolute Pfade auf.
        SGPath p = new SGPath(aircraftDir);
        p.append(aResource);
        return p.exists() ? p : new SGPath();
    }

    /**
     * Irgendiwe doppelt, denn im aktuellen Bundle würde ja eh gesucht und damit würder Pfad dann auch gefunden.
     * Und der Aircraftprovider sucht da auch,aber nur mit Sonderpfaden.
     * 
     * @return
     */
    @Override
    public BundleResource resolve(String resource/*, Bundle currrentbundle*/) {
        String  aircraftDir = FGProperties.fgGetString("/sim/aircraft-dir");
        Bundle bundle = BundleRegistry.getBundle(aircraftDir);
        if (bundle != null && bundle.exists(resource)){
            return new BundleResource(bundle,resource);
        }
       return null;
    }

    @Override
    public boolean isAircraftSpecific() {
        return false;
    }
}
