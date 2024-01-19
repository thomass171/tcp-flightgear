package de.yard.threed.trafficfg;

import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.Payload;


/**
 
 * 
 * Created by thomass on 17.07.17.
 * 14.3.19: Eigentlich die die ganze Klasse doch überflüssig. Event kann direkt verwendet werden.
 */
@Deprecated
public class TrafficEvent extends Event {
    /**
     * 13.3.19: TrafficRequest ist als Universalparameter doch viel zu gross.
     * @param eventtype
     * @param payload
     */
    @Deprecated
    public TrafficEvent(EventType eventtype, TrafficRequest payload) {
        super(eventtype,new Payload(new Object[]{payload}));
    }


}
