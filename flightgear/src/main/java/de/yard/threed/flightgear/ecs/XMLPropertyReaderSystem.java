package de.yard.threed.flightgear.ecs;


import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.DefaultEcsSystem;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;

/**
 * Eine XML PropertyList einlesen. Brauchts aber eigentlich nicht als System, weil XML Lesen immer ueber die Platform gehen muss. includes mache ich deshabl vorerst auch nicht
 * ueber System.
 * 
 * Created by thomass on 27.12.16.
 */
@Deprecated
public class XMLPropertyReaderSystem extends DefaultEcsSystem {
    Log logger = Platform.getInstance().getLog(XMLPropertyReaderSystem.class);

    // 6.2.23 Event von tcp-22 nach hier moved
    public static EventType EVENT_MODELLOAD = EventType.register(-23, "EVENT_MODELLOAD");

    public XMLPropertyReaderSystem(){
        super(new EventType[]{EVENT_MODELLOAD});
    }

    @Override
    public void process(Event evt) {
        SGPropertyNode/*_ptr*/ props = new SGPropertyNode();
        //SystemManager.sendEvent(EcsSystemEvent.buildModelLoadEvent(resource));
    }
    

        public Log getLogger() {
        return logger;
    }
}
