package de.yard.threed.trafficfg;

import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsService;
import de.yard.threed.traffic.EntityBuilder;
import de.yard.threed.traffic.VehicleComponent;
import de.yard.threed.traffic.config.VehicleDefinition;
import de.yard.threed.trafficfg.flight.GroundServiceComponent;

/**
 * Extracted from VehicleLauncher.buildVehicleOnGraph()
 *
 * 27.12.21
 */
public class VehicleEntityBuilder implements EntityBuilder, EcsService {
    @Override
    public void configure(EcsEntity entity, VehicleDefinition config) {
        if (config.getType().equals(VehicleComponent.VEHICLE_CAR)) {
            // passt z.Z. zwar, aber nicht jedes Car muss unbedingt ein GS vehicle sein.
            GroundServiceComponent gsc = new GroundServiceComponent(config);
            entity.addComponent(gsc);
        }
    }
}
