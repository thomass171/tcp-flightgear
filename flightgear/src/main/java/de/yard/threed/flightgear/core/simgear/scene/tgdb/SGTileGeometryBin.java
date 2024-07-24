package de.yard.threed.flightgear.core.simgear.scene.tgdb;

import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.core.loader.GeoMat;
import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.loader.PortableModelDefinition;
import de.yard.threed.core.loader.PortableModelList;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.flightgear.core.CppHashMap;
import de.yard.threed.flightgear.core.osg.Group;
import de.yard.threed.flightgear.core.osg.Node;
import de.yard.threed.flightgear.core.SGTexturedTriangleBinFactory;
import de.yard.threed.flightgear.core.simgear.scene.material.EffectGeode;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterial;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterialCache;
import de.yard.threed.flightgear.LoaderBTG;


import de.yard.threed.core.platform.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Aus SGTileGeometryBin.cxx
 * <p/>
 * Fans uns Strips skippen, weil nicht verwendet?.
 * <p/>
 * Created by thomass on 04.08.16.
 */
public class SGTileGeometryBin extends SGTriangleBin {

    static Log logger = Platform.getInstance().getLog(SGTileGeometryBin.class);
    // Enthaelt die Triangles pro Material. public zum Testen
    /*SGMaterialTriangleMap*/public CppHashMap<String, SGTexturedTriangleBin> materialTriangleMap = new CppHashMap<String, SGTexturedTriangleBin>(new SGTexturedTriangleBinFactory());
    public List<String> materialNotFound = new ArrayList<String>();

    public SGTileGeometryBin() {

    }

    private Vector2 getTexCoord(/*const std::vector<SGVec2f>&*/List<Vector2> texCoords,  /*int_list&*/List<Integer> tc, Vector2 tcScale, int i) {
        if (tc.isEmpty())
            return tcScale;
        else if (tc.size() == 1)
            return mult(texCoords.get(tc.get(0)), tcScale);
        else
            return mult(texCoords.get(tc.get(i)), tcScale);
    }

    /**
     * /// multiplication as a multiplicator, that isType assume that the getFirst vector
     * /// represents a 2x2 diagonal matrix with the diagonal elements in the vector.
     * /// Then the result isType the product of that matrix times the getSecond vector.
     * template<typename T>
     * inline
     * SGVec2<T>
     * mult(const SGVec2<T>& v1, const SGVec2<T>& v2)
     * { return SGVec2<T>(v1(0)*v2(0), v1(1)*v2(1)); }
     * TODO explizit testen
     *
     * @return
     */
    private Vector2 mult(Vector2 v1, Vector2 v2) {
        return new Vector2(v1.getX() * v2.getX(), v1.getY() * v2.getY());
    }

    /*SGVec2f*/Vector2 getTexCoordScale(String name, SGMaterialCache matcache) {
        if (matcache == null)
            return new Vector2(1, 1);
        SGMaterial material = matcache.find(name);
        if (material == null)
            return new Vector2(1, 1);

        return material.get_tex_coord_scale();
    }

    private void addTriangleGeometry(SGTexturedTriangleBin triangles,
            /*SGBinObject&*/LoaderBTG obj, int grp,
            /*SGVec2f&*/Vector2 tc0Scale,
            /*SGVec2f&*/Vector2 tc1Scale) {
        /*const std::vector<SGVec3d>& */
        List<Vector3> vertices = obj.get_wgs84_nodes();
        /*const std::vector<SGVec3f>&*/
        List<Vector3> normals = obj.loadedfile.objects.get(0).normals;
        /*const std::vector<SGVec2f>&*/
        List<Vector2> texCoords = obj.get_texcoords();
        /*const int_list&*/
        List<Integer> tris_v = obj.tris_v.get(grp);
        /*const int_list&*/
        List<Integer> tris_n = obj.tris_n.get(grp);
        /*const tci_list&*/
        List<List<Integer>> tris_tc = obj.tris_tcs.get(grp);
        boolean num_norms_is_num_verts = true;

        if (tris_v.size() != tris_n.size()) {
            // If the normal indices do not match, they should be inmplicitly
            // the same than the vertex indices. 
            num_norms_is_num_verts = false;
        }

        if (!tris_tc.get(1).isEmpty()) {
            triangles.hasSecondaryTexCoord(true);
        }

        for (int i = 2; i < tris_v.size(); i += 3) {
            SGVertNormTex v0 = new SGVertNormTex();
            v0.SetVertex(toVec3f(vertices.get(tris_v.get(i - 2))));
            v0.SetNormal(num_norms_is_num_verts ? normals.get(tris_n.get(i - 2)) :
                    normals.get(tris_v.get(i - 2)));
            v0.SetTexCoord(0, getTexCoord(texCoords, tris_tc.get(0), tc0Scale, i - 2));
            if (!tris_tc.get(1).isEmpty()) {
                v0.SetTexCoord(1, getTexCoord(texCoords, tris_tc.get(1), tc1Scale, i - 2));
            }
            SGVertNormTex v1 = new SGVertNormTex();
            v1.SetVertex(toVec3f(vertices.get(tris_v.get(i - 1))));
            v1.SetNormal(num_norms_is_num_verts ? normals.get(tris_n.get(i - 1)) :
                    normals.get(tris_v.get(i - 1)));
            v1.SetTexCoord(0, getTexCoord(texCoords, tris_tc.get(0), tc0Scale, i - 1));
            if (!tris_tc.get(1).isEmpty()) {
                v1.SetTexCoord(1, getTexCoord(texCoords, tris_tc.get(1), tc1Scale, i - 1));
            }
            SGVertNormTex v2 = new SGVertNormTex();
            v2.SetVertex(toVec3f(vertices.get(tris_v.get(i))));
            v2.SetNormal(num_norms_is_num_verts ? normals.get(tris_n.get(i)) :
                    normals.get(tris_v.get(i)));
            v2.SetTexCoord(0, getTexCoord(texCoords, tris_tc.get(0), tc0Scale, i));
            if (!tris_tc.get(1).isEmpty()) {
                v2.SetTexCoord(1, getTexCoord(texCoords, tris_tc.get(1), tc1Scale, i));
            }

            triangles.insert(v0, v1, v2);
        }
        //logger.debug("addTriangleGeometry: triangles.numvertices="+triangles.getNumVertices()+",numtriangles="+triangles.getNumTriangles());

    }

    /**
     * TODO brauchts das?
     *
     * @param nativeVector3
     * @return
     */
    private Vector3 toVec3f(/*7.2.18 Native*/Vector3 nativeVector3) {
        return nativeVector3;
    }

   /* static void
    addStripGeometry(SGTexturedTriangleBin& triangles,
                     const SGBinObject& obj, unsigned grp,
                     const SGVec2f& tc0Scale,
                     const SGVec2f& tc1Scale)
    {
        const std::vector<SGVec3d>& vertices(obj.get_wgs84_nodes());
        const std::vector<SGVec3f>& normals(obj.get_normals());
        const std::vector<SGVec2f>& texCoords(obj.get_texcoords());
        const int_list& strips_v(obj.get_strips_v()[grp]);
        const int_list& strips_n(obj.get_strips_n()[grp]);
        const tci_list& strips_tc(obj.get_strips_tcs()[grp]);
        bool  num_norms_is_num_verts = true;

        if (strips_v.size() != strips_n.size()) {
            // If the normal indices do not match, they should be inmplicitly
            // the same than the vertex indices. 
            num_norms_is_num_verts = false;
        }

        if ( !strips_tc[1].empty() ) {
            triangles.hasSecondaryTexCoord(true);
        }

        for (unsigned i = 2; i < strips_v.size(); ++i) {
            SGVertNormTex v0;
            v0.SetVertex( toVec3f(vertices[strips_v[i-2]]) );
            v0.SetNormal( num_norms_is_num_verts ? normals[strips_n[i-2]] :
                    normals[strips_v[i-2]] );
            v0.SetTexCoord( 0, getTexCoord(texCoords, strips_tc[0], tc0Scale, i-2) );
            if (!strips_tc[1].empty()) {
                v0.SetTexCoord( 1, getTexCoord(texCoords, strips_tc[1], tc1Scale, i-2) );
            }
            SGVertNormTex v1;
            v1.SetVertex( toVec3f(vertices[strips_v[i-1]]) );
            v1.SetNormal( num_norms_is_num_verts ? normals[strips_n[i-1]] :
                    normals[strips_v[i-1]] );
            v1.SetTexCoord( 0, getTexCoord(texCoords, strips_tc[1], tc0Scale, i-1) );
            if (!strips_tc[1].empty()) {
                v1.SetTexCoord( 1, getTexCoord(texCoords, strips_tc[1], tc1Scale, i-1) );
            }
            SGVertNormTex v2;
            v2.SetVertex( toVec3f(vertices[strips_v[i]]) );
            v2.SetNormal( num_norms_is_num_verts ? normals[strips_n[i]] :
                    normals[strips_v[i]] );
            v2.SetTexCoord( 0, getTexCoord(texCoords, strips_tc[0], tc0Scale, i) );
            if (!strips_tc[1].empty()) {
                v2.SetTexCoord( 1, getTexCoord(texCoords, strips_tc[1], tc1Scale, i) );
            }
            if (i%2)
                triangles.insert(v1, v0, v2);
            else
                triangles.insert(v0, v1, v2);
        }
    }*/

    /*static void
    addFanGeometry(SGTexturedTriangleBin& triangles,
                   const SGBinObject& obj, unsigned grp,
                   const SGVec2f& tc0Scale,
                   const SGVec2f& tc1Scale)
    {
        const std::vector<SGVec3d>& vertices(obj.get_wgs84_nodes());
        const std::vector<SGVec3f>& normals(obj.get_normals());
        const std::vector<SGVec2f>& texCoords(obj.get_texcoords());
        const int_list& fans_v(obj.get_fans_v()[grp]);
        const int_list& fans_n(obj.get_fans_n()[grp]);
        const tci_list& fans_tc(obj.get_fans_tcs()[grp]);
        bool  num_norms_is_num_verts = true;

        if (fans_v.size() != fans_n.size()) {
            // If the normal indices do not match, they should be inmplicitly
            // the same than the vertex indices. 
            num_norms_is_num_verts = false;
        }

        if ( !fans_tc[1].empty() ) {
            triangles.hasSecondaryTexCoord(true);
        }

        SGVertNormTex v0;
        v0.SetVertex( toVec3f(vertices[fans_v[0]]) );
        v0.SetNormal( num_norms_is_num_verts ? normals[fans_n[0]] :
                normals[fans_v[0]] );
        v0.SetTexCoord( 0, getTexCoord(texCoords, fans_tc[0], tc0Scale, 0) );
        if (!fans_tc[1].empty()) {
            v0.SetTexCoord( 1, getTexCoord(texCoords, fans_tc[1], tc1Scale, 0) );
        }
        SGVertNormTex v1;
        v1.SetVertex( toVec3f(vertices[fans_v[1]]) );
        v1.SetNormal( num_norms_is_num_verts ? normals[fans_n[1]] :
                normals[fans_v[1]] );
        v1.SetTexCoord( 0, getTexCoord(texCoords, fans_tc[0], tc0Scale, 1) );
        if (!fans_tc[1].empty()) {
            v1.SetTexCoord( 1, getTexCoord(texCoords, fans_tc[1], tc1Scale, 1) );
        }
        for (unsigned i = 2; i < fans_v.size(); ++i) {
            SGVertNormTex v2;
            v2.SetVertex( toVec3f(vertices[fans_v[i]]) );
            v2.SetNormal( num_norms_is_num_verts ? normals[fans_n[i]] :
                    normals[fans_v[i]] );
            v2.SetTexCoord( 0, getTexCoord(texCoords, fans_tc[0], tc0Scale, i) );
            if (!fans_tc[1].empty()) {
                v2.SetTexCoord( 1, getTexCoord(texCoords, fans_tc[1], tc1Scale, i) );
            }
            triangles.insert(v0, v1, v2);
            v1 = v2;
        }
    }*/

    /**
     * Die Geo wird in die materialTriangleMap eingefuegt.
     * <p/>
     * matcache darf null sein.
     *
     * @param obj
     * @param matcache
     * @return
     */
    public boolean insertSurfaceGeometry(/*const SGBinObject&*/ LoaderBTG obj, SGMaterialCache matcache) {
        if (obj.tris_n.size() < obj.tris_v.size() ||
                obj.tris_tcs.size() < obj.tris_v.size()) {
            logger.error(/*SG_LOG(SG_TERRAIN, SG_ALERT,*/                    "Group list sizes for triangles do not match!");
            return false;
        }

        for (int grp = 0; grp < obj.tris_v.size(); ++grp) {
            String materialName = obj.tri_materials.get(grp);
            /*SGVec2f*/
            Vector2 tc0Scale = getTexCoordScale(materialName, matcache);
            /*SGVec2f*/
            Vector2 tc1Scale = new Vector2(1.0f, 1.0f);
            addTriangleGeometry(materialTriangleMap.get(materialName),
                    obj, grp, tc0Scale, tc1Scale);
        }

        /*if (obj.get_strips_n().size() < obj.get_strips_v().size() ||
                obj.get_strips_tcs().size() < obj.get_strips_v().size()) {
            SG_LOG(SG_TERRAIN, SG_ALERT,
                    "Group list sizes for strips do not match!");
            return false;
        }
        for (unsigned grp = 0; grp < obj.get_strips_v().size(); ++grp) {
            std::string materialName = obj.get_strip_materials()[grp];
            SGVec2f tc0Scale = getTexCoordScale(materialName, matcache);
            SGVec2f tc1Scale(1.0, 1.0);
            addStripGeometry(materialTriangleMap[materialName],
                    obj, grp, tc0Scale, tc1Scale);
        }*/

      /*  if (obj.get_fans_n().size() < obj.get_fans_v().size() ||
                obj.get_fans_tcs().size() < obj.get_fans_v().size()) {
            SG_LOG(SG_TERRAIN, SG_ALERT,
                    "Group list sizes for fans do not match!");
            return false;
        }
        for (unsigned grp = 0; grp < obj.get_fans_v().size(); ++grp) {
            std::string materialName = obj.get_fan_materials()[grp];
            SGVec2f tc0Scale = getTexCoordScale(materialName, matcache);
            SGVec2f tc1Scale(1.0, 1.0);
            addFanGeometry(materialTriangleMap[materialName],
                    obj, grp, tc0Scale, tc1Scale );
        }*/
        return true;
    }

    /**
     * Aus der allen Trianglemaps ein Mesh erstellen.
     * 12.12.17: Aufgeteilt um das erstellen der Geo im preprocess verwenden zu koennen.
     */
    public /*Node*/List<GeoMat> getSurfaceGeometryPart1(SGMaterialCache matcache, boolean useVBOs) {
        if (materialTriangleMap.isEmpty())
            return null;

        List<GeoMat> geos = new ArrayList<GeoMat>();
        EffectGeode eg = null;
        /*12.12.17Group group = (materialTriangleMap.size() > 1 ? new Group() : null);
        if (group != null) {
            group.setName("surfaceGeometryGroup");
        }*/

        //osg::Geode* geode = new osg::Geode;
        //SGMaterialTriangleMap::const_iterator i;
        //for (i = materialTriangleMap.begin(); i != materialTriangleMap.end(); ++i) {
        // Is 'ii' the land class name like 'CropGrass'?
        for (String ii : materialTriangleMap.keySet()) {
            SGTexturedTriangleBin i = materialTriangleMap.get(ii);
            /*osg::Geometry**/
            SimpleGeometry geometry = i/*->getSecond*/.buildGeometry(useVBOs);
            SGMaterial mat = null;
            int textureindex = i.getTextureIndex();
            if (matcache != null) {
                mat = matcache.find(ii/*->getFirst*/);
                if (mat == null) {
                    // darf sowas vorkommen?
                    logger.warn("no material found in matcache for " + ii + ". matcache.size=" + matcache.cache.size());
                } else {
                    PortableMaterial pmat = mat.getEffectMaterialByTextureIndex(textureindex);
                    //geos.add(new GeoMat(geometry, (mat != null) ? (mat.get_one_effect(textureindex).getMaterialDefinition()) : null));
                    if (pmat == null) {
                        logger.warn("No material effect available for '" + ii + "' with index " + textureindex);
                        if (!materialNotFound.contains(ii)) {
                            materialNotFound.add(ii);
                        }
                    }
                    GeoMat geoMat = new GeoMat(geometry, pmat);
                    // landclass is used for logging. Not working. Why not?
                    geoMat.landclass = ii;
                    geos.add(geoMat);
                }
            } else {
                //save landclass name as material name
                geos.add(new GeoMat(geometry, ii, textureindex));
            }


            // FG-DIFF
            // Zu FG abweichende implementierung. Wenn es kein Material gibt, wireframe setzen. Fuer Testen vielleicht ganz gut. Ob auf Dauer auch?
            // In FG wird das Material ueber Effect in EffectGeode (teilweise ueber Callback) abgebildet. Effect verbindet direkt zum Shader.
            // Darueber wird zumindest wohl die Textur definiert. Hier jetzt das Mesh direkt erzeugen.
            /*12.12.17: jetzt in Part2 
            eg = new EffectGeode();            
            // FG-DIFF immer derselbe Name ist doch bloed. Es ist auch nicht erkennbar, dass FG da Logik dran hat. 
            //eg.setName("EffectGeode");
            eg.setName("" + ii+"("+i.getNumTriangles()+" tris)");
            if (mat != null) {
                eg.setMaterial(mat);
                eg.setEffect(mat.get_one_effect(i/*->getSecond* /.getTextureIndex()));
            } else {
                eg.setMaterial(null);
            }
            //eg.addDrawable(geometry);
            //eg.runGenerators(geometry);  // Generate extra data needed by effect            
            if (group != null) {
                group.attach(eg);
            }
            eg.buildMesh(geometry);
            */
        }

       /*12.12.17: jetzt in Part2  if (group != null) {
            return group;
        } else {
            return eg;
        }*/
        return geos;
    }


}
