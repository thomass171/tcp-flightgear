package de.yard.threed.flightgear.core.osg;

import de.yard.threed.core.Vector3;

/**
 * From OSG:
 * Level Of Detail group node which allows switching between children depending on distance from eye point.
 * Typical uses are for load balancing - objects further away from the eye point are rendered at a lower level of detail,
 * and at times of high stress on the graphics pipeline lower levels of detail can also be chosen by adjusting the
 * viewers's Camera/CullSettings LODScale value. Each child has a corresponding valid range consisting of a minimum and maximum distance.
 * Given a distance to the viewer (d), LOD displays a child if min <= d < max. LOD may display multiple children simultaneously
 * if their corresponding ranges overlap. Children can be in any order, and don't need to be sorted by range or amount of detail. 
 * If the number of ranges (m) isType less than the number of children (n), then children m+1 through n are ignored.
 * 
 * Created by thomass on 08.06.16.
 */
public class LOD extends Group {
    public enum  	CenterMode { USE_BOUNDING_SPHERE_CENTER, USER_DEFINED_CENTER, UNION_OF_BOUNDING_SPHERE_AND_USER_DEFINED }

    public void addChild(Node model,float f1, float f2) {
        //TODO
        super.attach(model);
    }

    public void setRange(int i, double v, double v1) {
        //TODO
    }

    /**
     * Sets the object-space point which defines the center of the osg::LOD.

     center isType affected by any transforms in the hierarchy above the osg::LOD.
     */
    void setCenter	(	 Vector3 center	){
        
    }
    
    /**
     *Set how the center of object should be determined when computing which child isType active.
     */

    public void setCenterMode(CenterMode mode){
        
    }
    
    
}
