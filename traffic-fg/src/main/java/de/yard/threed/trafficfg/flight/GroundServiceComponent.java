package de.yard.threed.trafficfg.flight;


import de.yard.threed.core.Util;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.DefaultEcsComponent;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.graph.GraphNode;
import de.yard.threed.traffic.config.VehicleDefinition;

/**
 * Fuer alles Ground(Service)Vehicle, aber mal nicht für Aircraft. Die werden als einfach mal da angesehen. Sind sie in FG ja durch AI.
 * Das schon, aber für Tests und später sehen wir auch mal Aircraft vor.
 * <p>
 * Die Granularitaet der Typisierung ist uneinheitlich. Darum gibt es type und modeltype
 * <p>
 * 13.3.19 MA30: Aircraft kommt hier wieder raus und dann ist das eine ServiceComponent
 * Created by thomass on 13.04.17.
 */
public class GroundServiceComponent extends DefaultEcsComponent {
    private Log logger = Platform.getInstance().getLog(GroundServiceComponent.class);
    public static String TAG = "GroundServiceComponent";
    public static String VEHICLE_FOLLOME = "followme";
    public static String VEHICLE_PUSHBACK = "pushback";
    public static String VEHICLE_STAIRS = "stairs";
    public static String VEHICLE_FUELTRUCK = "fueltruck";
    public static String VEHICLE_CATERING = "catering";

    //14.3.19: Sind das defaultwerte?
    //public static int cateringduration = 10;
    //public static int fuelingduration = ;

    //auf dem return path ist auch idle
    //public static String IDLE = "idle";
    //public static String APPROACHING = "approaching";
    //busy
    //public static String BUSY = "busy";
    public VehicleDefinition config;
    //obs das braucht? public String state = IDLE;
    private int statechangetimestamp = 0;
    private int servicestarttimestamp = 0;
    //SP only temporary. Ist auf dem Weg hin gesetzt, und auf dem Weg zurück schon null.
    public ServicePoint sp;
    public boolean fordoor;
    private int durationinseconds = 5;

    public GroundServiceComponent(VehicleDefinition config) {
        this.config = config;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    public static GroundServiceComponent getGroundServiceComponent(EcsEntity e) {
        GroundServiceComponent vc = (GroundServiceComponent) e.getComponent(GroundServiceComponent.TAG);
        return vc;
    }

    public GraphNode setStateApproaching(ServicePoint sp, String modeltype,int servicedurationinseconds) {
        this.sp = sp;
        this.durationinseconds = servicedurationinseconds;
        statechangetimestamp =  Util.currentTimeSeconds();

        if (modeltype.equals(GroundServiceComponent.VEHICLE_FUELTRUCK)) {
            fordoor = false;
            return sp.wingedge.to;
        }
        if (modeltype.equals(GroundServiceComponent.VEHICLE_CATERING)) {
            fordoor = true;
            return sp.doorEdge.from;
        }
        logger.warn("no preferred destination, using door: " + modeltype);
        fordoor = true;
        return sp.doorEdge.from;
    }

    public void setStateIdle() {
        sp = null;
        statechangetimestamp = Util.currentTimeSeconds();
        servicestarttimestamp = 0;
    }

    public void startService() {
        statechangetimestamp =  Util.currentTimeSeconds();
        servicestarttimestamp =  Util.currentTimeSeconds();
        //TODO duration
    }

    public boolean isApproaching() {
        return sp != null && servicestarttimestamp == 0;
    }

    public boolean serviceCompleted() {
        if (servicestarttimestamp > 0 &&  Util.currentTimeSeconds() > servicestarttimestamp + durationinseconds) {
            return true;
        }
        return false;
    }

    public void reset() {
        servicestarttimestamp = 0;
        sp = null;
    }

    /**
     * 12.9.17: Idle bezieht sich nur auf die Einbeziehung in einen Service, aber nicht darauf ob es sich gerade bewegt.
     * A vehiclke returning from service isType considered to be idle.
     * @return
     */
    public boolean isIdle() {
        return sp == null;
    }
}


