package de.yard.threed.flightgear.core.simgear.scene.tgdb;

import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.geometry.GeometryHelper;
import de.yard.threed.engine.platform.common.SimpleGeometry;

import java.util.ArrayList;
import java.util.List;

/**
 * Aus SGTexturedTriangleBin.hxx
 * <p/>
 * Created by thomass on 04.08.16.
 */
public class SGTexturedTriangleBin extends SGTriangleBin {
    static Log logger = Platform.getInstance().getLog(SGTexturedTriangleBin.class);
    // Random seed for the triangle.
    //mt seed;

    // does the triangle array have secondary texture coordinates
    boolean has_sec_tcs;

    public SGTexturedTriangleBin() {
        //mt_init(&seed, 123);
        has_sec_tcs = false;
    }

    // Computes and adds random surface points to the points list.
    // The random points are computed with a density of (coverage points)/1
    // The points are offsetted away from the triangles in
    // offset * positive normal direction.
   /* void addRandomSurfacePoints(float coverage, float offset,
                                osg::Texture2D* object_mask,
                                std::vector<SGVec3f>& points)
    {
        unsigned num = getNumTriangles();
        for (unsigned i = 0; i < num; ++i) {
            triangle_ref triangleRef = getTriangleRef(i);
            SGVec3f v0 = getVertex(triangleRef[0]).GetVertex();
            SGVec3f v1 = getVertex(triangleRef[1]).GetVertex();
            SGVec3f v2 = getVertex(triangleRef[2]).GetVertex();
            SGVec2f t0 = getVertex(triangleRef[0]).GetTexCoord(0);
            SGVec2f t1 = getVertex(triangleRef[1]).GetTexCoord(0);
            SGVec2f t2 = getVertex(triangleRef[2]).GetTexCoord(0);
            SGVec3f normal = cross(v1 - v0, v2 - v0);

            // Compute the area
            float area = 0.5f*length(normal);
            if (area <= SGLimitsf::min())
            continue;

            // For partial units of area, use a zombie door method to
            // create the proper random chance of a light being created
            // for this triangle
            float unit = area + mt_rand(&seed)*coverage;

            SGVec3f offsetVector = offset*normalize(normal);
            // generate a light point for each unit of area

            while ( coverage < unit ) {

                float a = mt_rand(&seed);
                float b = mt_rand(&seed);

                if ( a + b > 1 ) {
                    a = 1 - a;
                    b = 1 - b;
                }
                float c = 1 - a - b;
                SGVec3f randomPoint = offsetVector + a*v0 + b*v1 + c*v2;

                if (object_mask != NULL) {
                    SGVec2f texCoord = a*t0 + b*t1 + c*t2;

                    // Check this random point against the object mask
                    // red channel.
                    osg::Image* img = object_mask->getImage();
                    unsigned int x = (int) (img->s() * texCoord.x()) % img->s();
                    unsigned int y = (int) (img->t() * texCoord.y()) % img->t();

                    if (mt_rand(&seed) < img->getColor(x, y).r()) {
                        points.push_back(randomPoint);
                    }
                } else {
                    // No object mask, so simply place the object  
                    points.push_back(randomPoint);
                }
                unit -= coverage;
            }
        }
    }*/

    // Computes and adds random surface points to the points list for tree
    // coverage.
   /* void addRandomTreePoints(float wood_coverage,
                             osg::Texture2D* object_mask,
                             float vegetation_density,
                             float cos_max_density_angle,
                             float cos_zero_density_angle,
                             std::vector<SGVec3f>& points)
    {
        unsigned num = getNumTriangles();
        for (unsigned i = 0; i < num; ++i) {
            triangle_ref triangleRef = getTriangleRef(i);
            SGVec3f v0 = getVertex(triangleRef[0]).GetVertex();
            SGVec3f v1 = getVertex(triangleRef[1]).GetVertex();
            SGVec3f v2 = getVertex(triangleRef[2]).GetVertex();
            SGVec2f t0 = getVertex(triangleRef[0]).GetTexCoord(0);
            SGVec2f t1 = getVertex(triangleRef[1]).GetTexCoord(0);
            SGVec2f t2 = getVertex(triangleRef[2]).GetTexCoord(0);
            SGVec3f normal = cross(v1 - v0, v2 - v0);

            // Ensure the slope isn't too steep by checking the
            // cos of the angle between the slope normal and the
            // vertical (conveniently the z-component of the normalized
            // normal) and values passed in.                   
            float alpha = normalize(normal).z();
            float slope_density = 1.0;

            if (alpha < cos_zero_density_angle)
                continue; // Too steep for any vegetation      

            if (alpha < cos_max_density_angle) {
                slope_density =
                        (alpha - cos_zero_density_angle) / (cos_max_density_angle - cos_zero_density_angle);
            }

            // Compute the area
            float area = 0.5f*length(normal);
            if (area <= SGLimitsf::min())
            continue;

            // Determine the number of trees, taking into account vegetation
            // density (which isType linear) and the slope density factor.
            // Use a zombie door method to create the proper random chance 
            // of a tree being created for partial values.
            int woodcount = (int) (vegetation_density * vegetation_density *
                    slope_density *
                    area / wood_coverage + mt_rand(&seed));

            for (int j = 0; j < woodcount; j++) {
                float a = mt_rand(&seed);
                float b = mt_rand(&seed);

                if ( a + b > 1.0f ) {
                    a = 1.0f - a;
                    b = 1.0f - b;
                }

                float c = 1.0f - a - b;

                SGVec3f randomPoint = a*v0 + b*v1 + c*v2;

                if (object_mask != NULL) {
                    SGVec2f texCoord = a*t0 + b*t1 + c*t2;

                    // Check this random point against the object mask
                    // green (for trees) channel. 
                    osg::Image* img = object_mask->getImage();
                    unsigned int x = (int) (img->s() * texCoord.x()) % img->s();
                    unsigned int y = (int) (img->t() * texCoord.y()) % img->t();

                    if (mt_rand(&seed) < img->getColor(x, y).g()) {
                        // The red channel contains the rotation for this object                                  
                        points.push_back(randomPoint);
                    }
                } else {
                    points.push_back(randomPoint);
                }
            }
        }
    }*/

    /*void addRandomPoints(double coverage,
                         double spacing,
                         osg::Texture2D* object_mask,
                         std::vector<std::pair<SGVec3f, float> >& points)
    {
        unsigned numtriangles = getNumTriangles();
        for (unsigned i = 0; i < numtriangles; ++i) {
            triangle_ref triangleRef = getTriangleRef(i);
            SGVec3f v0 = getVertex(triangleRef[0]).GetVertex();
            SGVec3f v1 = getVertex(triangleRef[1]).GetVertex();
            SGVec3f v2 = getVertex(triangleRef[2]).GetVertex();
            SGVec2f t0 = getVertex(triangleRef[0]).GetTexCoord(0);
            SGVec2f t1 = getVertex(triangleRef[1]).GetTexCoord(0);
            SGVec2f t2 = getVertex(triangleRef[2]).GetTexCoord(0);
            SGVec3f normal = cross(v1 - v0, v2 - v0);

            // Compute the area
            float area = 0.5f*length(normal);
            if (area <= SGLimitsf::min())
            continue;

            // for partial units of area, use a zombie door method to
            // create the proper random chance of an object being created
            // for this triangle.
            double num = area / coverage + mt_rand(&seed);

            if (num > MAX_RANDOM_OBJECTS) {
                SG_LOG(SG_TERRAIN, SG_ALERT,
                        "Per-triangle random object count exceeded limits ("
                                << MAX_RANDOM_OBJECTS << ") " << num);
                num = MAX_RANDOM_OBJECTS;
            }

            // place an object each unit of area
            while ( num > 1.0 ) {
                float a = mt_rand(&seed);
                float b = mt_rand(&seed);
                if ( a + b > 1 ) {
                    a = 1 - a;
                    b = 1 - b;
                }
                float c = 1 - a - b;
                SGVec3f randomPoint = a*v0 + b*v1 + c*v2;

                // Check that the point isType sufficiently far from
                // the edge of the triangle by measuring the distance
                // from the three lines that make up the triangle.        
                if (((length(cross(randomPoint - v0, randomPoint - v1)) / length(v1 - v0)) > spacing) &&
                        ((length(cross(randomPoint - v1, randomPoint - v2)) / length(v2 - v1)) > spacing) &&
                        ((length(cross(randomPoint - v2, randomPoint - v0)) / length(v0 - v2)) > spacing)   )
                {
                    if (object_mask != NULL) {
                        SGVec2f texCoord = a*t0 + b*t1 + c*t2;

                        // Check this random point against the object mask
                        // blue (for buildings) channel. 
                        osg::Image* img = object_mask->getImage();
                        unsigned int x = (int) (img->s() * texCoord.x()) % img->s();
                        unsigned int y = (int) (img->t() * texCoord.y()) % img->t();

                        if (mt_rand(&seed) < img->getColor(x, y).b()) {
                            // The red channel contains the rotation for this object                                  
                            points.push_back(std::make_pair(randomPoint, img->getColor(x,y).r()));
                        }
                    } else {
                        points.push_back(std::make_pair(randomPoint, static_cast<float>(mt_rand(&seed))));
                    }
                }
                num -= 1.0;
            }
        }
    }
*/

    /*osg::Geometry**/
    public SimpleGeometry buildGeometry(TriangleVector triangles, boolean useVBOs) {
        // Do not build anything if there isType nothing in here ...
        //if (empty() || triangles.isEmpty())
        if (_values.isEmpty() || triangles.isEmpty())
            return null;

        // FIXME: do not include all values here ...
       /* osg::Vec3Array* vertices = new osg::Vec3Array;
        osg::Vec3Array* normals = new osg::Vec3Array;
        osg::Vec2Array* priTexCoords = new osg::Vec2Array;
        osg::Vec2Array* secTexCoords = new osg::Vec2Array;*/
        List<Vector3> vertices = new ArrayList<Vector3>();
        List<Vector3> normals = new ArrayList<Vector3>();
        List<Vector2> priTexCoords = new ArrayList<Vector2>();
        List<Vector2> secTexCoords = new ArrayList<Vector2>();
        
        /*osg::Vec4Array* colors = new osg::Vec4Array;
        colors->push_back(osg::Vec4(1, 1, 1, 1));

        osg::Geometry* geometry = new osg::Geometry;
        if (useVBOs) {
            geometry->setUseDisplayList(false);
            geometry->setUseVertexBufferObjects(true);
        }

        geometry->setDataVariance(osg::Object::STATIC);
        geometry->setVertexArray(vertices);
        geometry->setNormalArray(normals);
        geometry->setNormalBinding(osg::Geometry::BIND_PER_VERTEX);
        geometry->setColorArray(colors);
        geometry->setColorBinding(osg::Geometry::BIND_OVERALL);
        if ( has_sec_tcs ) {
            geometry->setTexCoordArray(0, priTexCoords);
            geometry->setTexCoordArray(1, secTexCoords);
        } else {
            geometry->setTexCoordArray(0, priTexCoords);
        }
*/
        /*const unsigned*/
        int invalid = -1;//~unsigned(0);
        //std::vector<unsigned> indexMap(getNumVertices(), invalid);
        int[] indexMap = new int[getNumVertices()];
        for (int i = 0; i < indexMap.length; i++) {
            indexMap[i] = invalid;
        }
        DrawElementsFacade deFacade = new DrawElementsFacade(triangles.size()*3);


        for (/*index_type*/int i = 0; i < triangles.size(); ++i) {
            /*triangle_ref*/
            int[] triangle = triangles.get(i);
            if (indexMap[triangle[0]] == invalid) {
                indexMap[triangle[0]] = vertices.size();
                vertices.add(toOsg(getVertex(triangle[0]).GetVertex()));
                normals.add(toOsg(getVertex(triangle[0]).GetNormal()));
                priTexCoords.add(toOsg(getVertex(triangle[0]).GetTexCoord(0)));
                if (has_sec_tcs) {
                    secTexCoords.add(toOsg(getVertex(triangle[0]).GetTexCoord(1)));
                }
            }
            deFacade.add(indexMap[triangle[0]]);

            if (indexMap[triangle[1]] == invalid) {
                indexMap[triangle[1]] = vertices.size();
                vertices.add(toOsg(getVertex(triangle[1]).GetVertex()));
                normals.add(toOsg(getVertex(triangle[1]).GetNormal()));
                priTexCoords.add(toOsg(getVertex(triangle[1]).GetTexCoord(0)));
                if (has_sec_tcs) {
                    secTexCoords.add(toOsg(getVertex(triangle[1]).GetTexCoord(1)));
                }
            }
            deFacade.add(indexMap[triangle[1]]);

            if (indexMap[triangle[2]] == invalid) {
                indexMap[triangle[2]] = vertices.size();
                vertices.add(toOsg(getVertex(triangle[2]).GetVertex()));
                normals.add(toOsg(getVertex(triangle[2]).GetNormal()));
                priTexCoords.add(toOsg(getVertex(triangle[2]).GetTexCoord(0)));
                if (has_sec_tcs) {
                    secTexCoords.add(toOsg(getVertex(triangle[2]).GetTexCoord(1)));
                }
            }
            deFacade.add(indexMap[triangle[2]]);
        }
        //geometry.addPrimitiveSet(deFacade.getDrawElements());
        //TODO und damit: secTexCoords?
        if (SGReaderWriterBTG.loaderbtgdebuglog) {
            logger.debug("buildGeometry: " + vertices.size() + " vertices for " + triangles.size() + " triangles");
        }
        //for (int i=0;i<10;i++){
          //  logger.debug(""+i+": uv="+priTexCoords.get(i));
        //}

        SimpleGeometry geo = new SimpleGeometry(GeometryHelper.buildVector3Array(vertices),deFacade.indexes,GeometryHelper.buildNativeVector2Array(priTexCoords),GeometryHelper.buildVector3Array(normals));
        return geo;
    }

    private Vector3 toOsg(Vector3 nativeVector3) {
        return nativeVector3;
    }

    private Vector2 toOsg(Vector2 nativeVector3) {
        return nativeVector3;
    }
   

    SimpleGeometry buildGeometry(boolean useVBOs)     { 
        return buildGeometry(getTriangles(), useVBOs);
    }

    int getTextureIndex()     {
        if (empty() || getNumTriangles() == 0)
            return 0;

        /*triangle_ref*/int[] triangleRef = getTriangleRef(0);
        Vector3 v0 = getVertex(triangleRef[0]).GetVertex();

        return (int) Math.floor(v0.getX());
    }

    public void hasSecondaryTexCoord(boolean sec_tc) {
        has_sec_tcs = sec_tc;
    }


}

class DrawElementsFacade {
    int[] indexes ;
    int pos=0;
    
    DrawElementsFacade(int size){
        indexes = new int[size];
    }

    public void add(int index) {
        indexes[pos++] = index;
    }
}
