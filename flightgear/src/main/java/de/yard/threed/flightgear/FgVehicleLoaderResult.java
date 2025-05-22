package de.yard.threed.flightgear;

import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.VelocityComponent;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.scene.model.SGAnimation;
import de.yard.threed.flightgear.ecs.FgAnimationComponent;
import de.yard.threed.traffic.VehicleLoaderResult;

import java.util.ArrayList;
import java.util.List;

public class FgVehicleLoaderResult implements VehicleLoaderResult {
    //animationlist will be populated later async
    public List<SGAnimation> animationList = new ArrayList<SGAnimation>();

    //or is it rootpropertyNode?
    public SGPropertyNode propertyNodeFromOpt;

    public FgVehicleLoaderResult(List<SGAnimation> animationList, SGPropertyNode propertyNode) {
        this.animationList=animationList;
        this.propertyNodeFromOpt=propertyNode;
    }

    @Override
    public void applyResultsToEntity(EcsEntity vehicleEntity) {
        // die animationlist wird sukzessive gefuellt. 22.10.19: Hier? kommt er doch nur einmal hin??
        if (animationList != null) {
            FgAnimationComponent fgAnimationComponent = new FgAnimationComponent(vehicleEntity.getSceneNode(), animationList, propertyNodeFromOpt);
            //14.5.25: PropertyComponent merged into FgAnimationComponent
            fgAnimationComponent.addPropertySync(
                    FgAnimationComponent.C172P_SPD,
                            parameter ->  VelocityComponent.getVelocityComponent(vehicleEntity).getMovementSpeed());
            vehicleEntity.addComponent(fgAnimationComponent);
        }
    }
}
