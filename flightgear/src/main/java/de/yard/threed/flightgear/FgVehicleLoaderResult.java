package de.yard.threed.flightgear;

import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.VelocityComponent;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.scene.model.SGAnimation;
import de.yard.threed.flightgear.ecs.FgAnimationComponent;
import de.yard.threed.traffic.VehicleLoaderResult;

import java.util.ArrayList;
import java.util.List;

/**
 * The result of FgVehicleLoader.loadVehicle()
 */
public class FgVehicleLoaderResult implements VehicleLoaderResult {
    //animationlist will be populated later async
    public List<SGAnimation> animationList = new ArrayList<SGAnimation>();

    //or is it rootpropertyNode?
    public SGPropertyNode propertyNodeFromOpt;

    /**
     * Due to async the animationlist might still be empty here!
     */
    public FgVehicleLoaderResult(List<SGAnimation> animationList, SGPropertyNode propertyNode) {
        this.animationList=animationList;
        this.propertyNodeFromOpt=propertyNode;
    }

    @Override
    public void applyResultsToEntity(EcsEntity vehicleEntity) {
        // animationlist is filled step by step async. So might not be complete yet.
        if (animationList != null) {
            // sync properties. Also happens in ReverseFDM.
            FgAnimationComponent fgAnimationComponent = new FgAnimationComponent(vehicleEntity.getSceneNode(), animationList, propertyNodeFromOpt);
            //14.5.25: PropertyComponent merged into FgAnimationComponent
            fgAnimationComponent.addPropertySync(
                    FgAnimationComponent.C172P_SPD,
                            parameter ->  VelocityComponent.getVelocityComponent(vehicleEntity).getMovementSpeed());
            // 4.12.25 Set more properties based on speed (see also README.md#FDM)
            fgAnimationComponent.addPropertySync(
                    FgAnimationComponent.C172P_SPD,
                    parameter ->  VelocityComponent.getVelocityComponent(vehicleEntity).getMovementSpeed());
            // For simple sound from https://wiki.flightgear.org/Howto:Add_sound_effects_to_aircraft#Sound_configuration_files
            // (generic, but no c172?)
            /*fgAnimationComponent.addPropertySync(
                    FgAnimationComponent.PROP_ENGINE_RUNNING,
                    parameter ->  VelocityComponent.getVelocityComponent(vehicleEntity).getMovementSpeed() > 0?1.0:0.0);*/

            // For spinning propeller. c172p uses engine[0], which appears obvious, but also engine[2].rpm(?) (only that only property is updated in FG)
            // needs a value > 500, so "* 15" appears ok for speed appx 40, maybe 30 is better for now. MAybe need SimpleFDM.
            fgAnimationComponent.addPropertySync(
                    FgAnimationComponent.PROP_ENGINE2,
                    parameter ->  VelocityComponent.getVelocityComponent(vehicleEntity).getMovementSpeed() * 30);

            vehicleEntity.addComponent(fgAnimationComponent);
        }
    }
}
