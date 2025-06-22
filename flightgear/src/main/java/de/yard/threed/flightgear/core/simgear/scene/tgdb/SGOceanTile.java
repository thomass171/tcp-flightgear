package de.yard.threed.flightgear.core.simgear.scene.tgdb;

import de.yard.threed.core.Degree;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Pair;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.flightgear.core.simgear.bucket.SGBucket;
import de.yard.threed.flightgear.core.simgear.geodesy.FgMath;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.flightgear.core.simgear.math.SGQuat;
import de.yard.threed.flightgear.core.simgear.misc.TexCoord;
import de.yard.threed.flightgear.core.simgear.scene.material.Effect;
import de.yard.threed.flightgear.core.simgear.scene.material.EffectGeode;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterial;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterialCache;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterialLib;
import de.yard.threed.flightgear.core.simgear.scene.util.VectorArrayAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * -*-c++-*-
 * From SGOceanTile.[ch].xx
 * Copyright (C) 2006-2007 Mathias Froehlich, Tim Moore
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */
/*
// Ocean tile with curvature and apron to hide cracks. The cracks are
// mostly with adjoining coastal tiles that assume a flat ocean
// between corners of a tile; they also hide the micro cracks between
// adjoining ocean tiles. This is probably over-engineered, but it
// serves as a testbed for some things that will come later.

// Helper class for building and accessing the mesh. The layout of the
// points in the mesh is a little wacky. First is the bottom row of
// the points for the apron. Next is the left apron point, the points
// in the mesh, and the right apron point, for each of the rows of the
// mesh; the points for the top apron come last. This order should
// help with things like vertex caching in the OpenGL driver, though
// it may be superfluous for such a small mesh.

Apparently the vertex layout is
--ooooo
o|ooooo|o
o|ooooo|o
o|ooooo|o
o|ooooo|o
o|ooooo|o
--ooooo = 25+2*10=45
*/

public class SGOceanTile {
    static Log log = null;
    public SceneNode node;
    public OceanMesh oceanMesh;
    // for testing only
    public static List<SGBucket> created = new ArrayList<>();

    public SGOceanTile(SceneNode transform, OceanMesh oceanMesh) {
        this.node = transform;
        this.oceanMesh = oceanMesh;
    }

    public static class OceanMesh {
        public Quaternion hlOr;

        //public:
        OceanMesh(int latP, int lonP) {
            latPoints = latP;
            lonPoints = lonP;

            // following from C++ constructor(?)
            geoPoints = (latPoints * lonPoints + 2 * (lonPoints + latPoints));
            geod_nodes = new ArrayList<>(latPoints * lonPoints);
            vl = (new /*osg::*/VecArray(geoPoints));
            nl = new VecArray<>/*new osg::Vec3Array*/(geoPoints);
            tl = new VecArray<>/*new osg::Vec2Array*/(geoPoints);
            vlArray = new VectorArrayAdapter(/* * */vl, lonPoints + 2, lonPoints, 1);
            nlArray = new VectorArrayAdapter( /***/nl, lonPoints + 2, lonPoints, 1);
            tlArray = new VectorArrayAdapter( /***/tl, lonPoints + 2, lonPoints, 1);
            // {
            int numPoints = latPoints * lonPoints;
            geod = new SGGeod[numPoints];
            normals = new Vector3[numPoints];
            rel = new Vector3[numPoints];
        }

   /* ~OceanMesh()
        {
            delete[] geod;
            delete[] normals;
            delete[] rel;
        }*/

        /*const*/ int latPoints, lonPoints;
        /*const*/ int geoPoints;
        SGGeod[] geod;
        public Vector3[] normals;
        public /*SGVec3d**/ Vector3[] rel;

        /*std::
        vector*/ List<SGGeod> geod_nodes;

        /*osg::Vec3Array*/ public VecArray<Vector3> vl;
        /*osg::Vec3Array**/public VecArray<Vector3> nl;
        /*osg::Vec2Array**/public VecArray<Vector2> tl;
        VectorArrayAdapter</*osg::Vec3Array*/Vector3> vlArray;
        VectorArrayAdapter<Vector3> nlArray;
        VectorArrayAdapter<Vector2> tlArray;

        /*void calcMesh(const SGVec3d& cartCenter, const SGQuatd& orient,
                      double clon, double clat,
                      double height, double width, double tex_width);
        void calcApronPt(int latIdx, int lonIdx, int latInner, int lonInner,
                         int destIdx, double tex_width);
        void calcApronPts(double tex_width);*/

        //}

        void /*OceanMesh::*/calcMesh(/*const SGVec3d&*/Vector3 cartCenter, /*const SGQuatd&*/Quaternion orient,
                                                       double clon, double clat,
                                                       double height, double width, double tex_width) {
            // Calculate vertices. By splitting the tile up into 4 quads on a
            // side we avoid curvature-of-the-earth problems; the error should
            // be less than .5 meters.
            double longInc = width * .25;
            double latInc = height * .25;
            double startLat = clat - height * .5;
            double startLon = clon - width * .5;
            for (int j = 0; j < latPoints; j++) {
                double lat = startLat + j * latInc;
                for (int i = 0; i < lonPoints; i++) {
                    int index = (j * lonPoints) + i;
                    geod[index] = SGGeod.fromDeg(startLon + i * longInc, lat);
                    Vector3 cart = /*SGVec3d::fromGeod (*/geod[index].toCart();
                    //rel[index] = orient.transform(cart - cartCenter);
                    // Strange: All 3 returned the same wrong(?) result??
                    //Matrix4 om = MathUtil2.buildRotationMatrix(orient);
                    //rel[index] = (cart.subtract(cartCenter)).rotate(orient);//-8659.0788060017
                    //rel[index] = (om.transform(cart.subtract(cartCenter)));//-8659.07880600178
                    //rel[index] = MathUtil2.multiply(orient,cart.subtract(cartCenter));//-8659.078806001784
                    rel[index] = orient.transform(cart.subtract(cartCenter));//-6949.046476761098
                    getLog().debug("rel[" + index + "]=" + rel[index] + ",cart=" + cart + ",cartCenter=" + cartCenter);
                    //normals[index] = toVec3f(orient.transform(normalize(cart)));
                    normals[index] = orient.transform(cart.normalize());
                }
            }

            // Calculate texture coordinates
            //typedef std::vector<SGGeod> GeodVector;

            VecArray<SGGeod> /*GeodVector*/ geod_nodes = new VecArray<>(latPoints * lonPoints);
            VectorArrayAdapter<SGGeod> geodNodesArray = new VectorArrayAdapter<SGGeod>(geod_nodes, lonPoints, 0, 0);
            /*int_list*/
            VecArray<Integer> rectangle = new VecArray<>(latPoints * lonPoints);
            VectorArrayAdapter<Integer> rectArray = new VectorArrayAdapter<>(rectangle, lonPoints, 0, 0);
            for (int j = 0; j < latPoints; j++) {
                for (int i = 0; i < lonPoints; i++) {
                    int index = (j * lonPoints) + i;
                    geodNodesArray.set(j, i, geod[index]);
                    rectArray.set(j, i, index);
                }
            }

            //typedef std::vector<SGVec2f> Vec2Array;
            List<Vector2> texs = TexCoord.sgCalcTexCoords(clat, geod_nodes, rectangle, 1000.0 / tex_width);
            VectorArrayAdapter<Vector2> texsArray = new VectorArrayAdapter<>(texs, lonPoints, 0, 0);

            // toOsg() is just a type converter
            for (int j = 0; j < latPoints; j++) {
                for (int i = 0; i < lonPoints; ++i) {
                    int index = (j * lonPoints) + i;
                    vlArray.set(j, i, rel[index]);
                    nlArray.set(j, i,/* toOsg(*/normals[index]);
                    tlArray.set(j, i, /*toOsg(*/texsArray.get(j, i));
                }
            }
            // The 20 'outer' elements are still null, only 25 'inner' are filled
        }

        /**
         * Apron points. For each point on the edge we'll go 150
         * metres "down" and 40 metres "out" to create a nice overlap. The
         * texture should be applied according to this dimension. The
         * normals of the apron polygons will be the same as the those of
         * the points on the edge to better disguise the apron.
         */
        void /*OceanMesh::*/calcApronPt(int latIdx, int lonIdx, int latInner, int lonInner,
                                        int destIdx, double tex_width) {
            /*static const*/
            float downDist = 150.0f;
            /*static const*/
            float outDist = 40.0f;
            // Get vector along edge, in the right direction to make a cross
            // product with the normal vector that will point out from the
            // mesh.
            Vector3 edgePt = vlArray.get(latIdx, lonIdx);
            Vector3 edgeVec;
            if (destIdx == 5) {
                int h = 9;
            }
            if (lonIdx == lonInner) {   // bottom or top edge
                if (lonIdx > 0) {
                    edgeVec = vlArray.get(latIdx, lonIdx - 1).subtract(/* -*/ edgePt);
                } else {
                    edgeVec = edgePt.subtract(/* - */vlArray.get(latIdx, lonIdx + 1));
                }
                if (latIdx > latInner) {
                    edgeVec = /*-*/edgeVec.negate();  // Top edge
                }
            } else {                     // right or left edge
                if (latIdx > 0) {
                    edgeVec = edgePt.subtract(/* -*/ vlArray.get(latIdx - 1, lonIdx));
                } else {
                    edgeVec = vlArray.get(latIdx + 1, lonIdx).subtract(/*-*/ edgePt);
                }
                if (lonIdx > lonInner) { // right edge
                    edgeVec = /*-*/edgeVec.negate();
                }
            }

            edgeVec = edgeVec.normalize();
            // Now the outer elements are populated? What is the '^' operator? Assume it's cross product, according to doc it is.
            //Vector3 outVec = nlArray.get(latIdx, lonIdx) ^ edgeVec;
            Vector3 outVec = MathUtil2.getCrossProduct(nlArray.get(latIdx, lonIdx), edgeVec);
            Vector3 t = nlArray.get(latIdx, lonIdx).multiply(downDist);
            vl.set(destIdx, edgePt.subtract(t).

                    add(outVec.multiply(outDist)));
            nl.set(destIdx, nlArray.get(latIdx, lonIdx));
            /*static const*/
            double apronDist = Math.sqrt(downDist * downDist + outDist * outDist);
            double texDelta = apronDist / tex_width;
            if (lonIdx == lonInner) {
                if (latIdx > latInner) {
                    tl.set(destIdx, tlArray.get(latIdx, lonIdx).add(new Vector2(0.0, texDelta)));
                } else {
                    tl.set(destIdx, tlArray.get(latIdx, lonIdx).subtract(new Vector2(0.0, texDelta)));
                }
            } else {
                if (lonIdx > lonInner) {
                    tl.set(destIdx, tlArray.get(latIdx, lonIdx).add(new Vector2(texDelta, 0.0)));
                } else {
                    tl.set(destIdx, tlArray.get(latIdx, lonIdx).subtract(new Vector2(texDelta, 0.0)));
                }
            }
        }

        void /*OceanMesh::*/calcApronPts(double tex_width) {
            for (int i = 0; i < lonPoints; i++) {
                calcApronPt(0, i, 1, i, i, tex_width);
            }
            int topApronOffset = latPoints + (2 + lonPoints) * latPoints;
            for (int i = 0; i < lonPoints; i++) {
                calcApronPt(latPoints - 1, i, latPoints - 2, i,
                        i + topApronOffset, tex_width);
            }
            for (int i = 0; i < latPoints; i++) {
                calcApronPt(i, 0, i, 1, lonPoints + i * (lonPoints + 2), tex_width);
                calcApronPt(i, lonPoints - 1, i, lonPoints - 2,
                        lonPoints + i * (lonPoints + 2) + 1 + lonPoints, tex_width);
            }
        }

        // Enter the vertices of triangles that fill one row of the
        // mesh. The vertices are entered in counter-clockwise order.
        void fillDrawElementsRow(int width, /*short*/
                                 int row0Start, /*short*/
                                 int row1Start,
                /*osg::DrawElementsUShort::vector_type::iterator&*/List<Integer>
                                         elements) {
            /*short*/
            int row0Idx = row0Start;
            /*short*/
            int row1Idx = row1Start;
            for (int i = 0; i < width - 1; i++, row0Idx++, row1Idx++) {
                /* *elements++ = */
                elements.add(row0Idx);
                /* *elements++ = */
                elements.add(row0Idx + 1);
                /* *elements++ = */
                elements.add(row1Idx);
                /* *elements++ = */
                elements.add(row1Idx);
                /* *elements++ = */
                elements.add(row0Idx + 1);
                /* *elements++ = */
                elements.add(row1Idx + 1);
            }
        }

        void fillDrawElementsWithApron(/*short*/                int height, /*short*/                int width,
                /* osg::DrawElementsUShort::vector_type::iterator*/List<Integer>
                                                                        elements) {
            // First apron row
            fillDrawElementsRow(width, 0, width + 1, elements);
            for (/*short*/                    int i = 0; i < height - 1; i++)
                fillDrawElementsRow(width + 2, width + i * (width + 2),
                        width + (i + 1) * (width + 2),
                        elements);
            // Last apron row
            /*short*/
            int topApronBottom = width + (height - 1) * (width + 2) + 1;
            fillDrawElementsRow(width, topApronBottom, topApronBottom + width + 1,
                    elements);
        }
    }

    /**
     * Generate an ocean tile.
     * FG-DIFF Returns not just node but OceanTile for better testing
     */
    public static /*osg::*/SGOceanTile SGOceanTile(/*const*/ SGBucket b, SGMaterialLib matlib, int latPoints, int lonPoints) {
        Effect effect = null;

        double tex_width = 1000.0;

        getLog().debug("Building SGOceanTile for bucket " + b);
        // find Ocean material in the properties list
        SGMaterialCache matcache = matlib.generateMatCache(b.get_center());
        SGMaterial mat = matcache.find("Ocean");
        //delete matcache;

        if (mat != null) {
            // set the texture width and height values for this
            // material
            tex_width = mat.get_xsize();

            // set OSG State
            //effect = mat.get_effect();
            effect = mat.get_effect(0);
        } else {
            getLog().warn("Ack! unknown use material name = Ocean");
        }
        OceanMesh grid = new OceanMesh(latPoints, lonPoints);
        // Calculate center point
        //Vector3 cartCenter = SGVec3d::fromGeod (b.get_center());
        Vector3 cartCenter = b.get_center().toCart();
        //SGGeod geodPos = SGGeod::fromCart (cartCenter);
        SGGeod geodPos = SGGeod.fromCart(cartCenter);
        // SGQuatd has euler angle order z,y,x!!
        //SGQuatd hlOr = SGQuatd::fromLonLat (geodPos) * SGQuatd::fromEulerDeg (0, 0, 180);
        grid.hlOr = FgMath.fromLonLat(geodPos).multiply(Quaternion.buildFromAngles(new Degree(180), new Degree(0), new Degree(0)));

        double clon = b.get_center_lon();
        double clat = b.get_center_lat();
        double height = b.get_height();
        double width = b.get_width();

        getLog().debug("SGOceanTile hlOr=" + grid.hlOr);
        grid.calcMesh(cartCenter, grid.hlOr, clon, clat, height, width, tex_width);
        grid.calcApronPts(tex_width);

        // we don't use a color for now, for what is it intended? Missing texture?
        /*osg::Vec4Array * cl = new osg::Vec4Array;
        cl -> push_back(osg::Vec4 (1, 1, 1, 1));*/

        // TSCH block moved here from below. List instead of array because later elements are added.
        /*osg::DrawElementsUShort **/
        List<Integer> drawElements
                // = new osg::DrawElementsUShort (GL_TRIANGLES,
                = new ArrayList<>();/*int[3 *
                6 * ((latPoints - 1) * (lonPoints + 1)
                + 2 * (latPoints - 1))];*/
        grid.fillDrawElementsWithApron(latPoints, lonPoints, drawElements /*-> begin()*/);
        //geometry -> addPrimitiveSet(drawElements);
        // not nice, but C# safe
        int[] drawElementsArray = new int[drawElements.size()];
        for (int i = 0; i < drawElements.size(); i++) {
            drawElementsArray[i] = drawElements.get(i);
        }

        for (int i = 0; i < grid.vl.size(); i++) {
            getLog().debug("SGOceanTile " + i + ":" + grid.vl.get(i));
        }
        /*osg::Geometry **/
        SimpleGeometry geometry = new /*osg::Geometry*/SimpleGeometry(grid.vl, drawElementsArray, grid.tl, grid.nl);
        //geometry -> setDataVariance(osg::Object::STATIC);
        //geometry -> setVertexArray(grid.vl);
        //geometry -> setNormalArray(grid.nl);
        //geometry -> setNormalBinding(osg::Geometry::BIND_PER_VERTEX);
        //geometry -> setColorArray(cl);
        //geometry -> setColorBinding(osg::Geometry::BIND_OVERALL);
        //geometry -> setTexCoordArray(0, grid.tl);

        // Allocate the indices for triangles in the mesh and the apron
        /* FG-DIFF moved before geo build osg::DrawElementsUShort * drawElements
                = new osg::DrawElementsUShort (GL_TRIANGLES,
                6 * ((latPoints - 1) * (lonPoints + 1)
                        + 2 * (latPoints - 1)));
        fillDrawElementsWithApron(latPoints, lonPoints, drawElements -> begin());
        geometry -> addPrimitiveSet(drawElements);*/

        // FG-DIFF See Obj.java for using EffectGeode. Material is not included in effect. Instead it needs to be set from effect like for
        // real tiles in Obj.java.
        EffectGeode geode = new EffectGeode();
        geode.setName("Ocean tile");
        if (effect != null) {
            geode.setEffect(effect);
            // Setting material probably is more important than setting effect.
            geode.setMaterial(effect.material);
        } else {
            getLog().warn("No effect for ocean tile");
        }
        //geode.addDrawable(geometry);
        //geode.runGenerators(geometry);
        geode.buildMesh(geometry);

        //osg::MatrixTransform * transform = new osg::MatrixTransform;
        SceneNode transform = new SceneNode();
        transform.setName("Ocean");
        // toOsg() is just a type converter
        //transform -> setMatrix(osg::Matrix::rotate (toOsg(hlOr)) * osg::Matrix::translate(toOsg(cartCenter)));
        transform.getTransform().setPosition(cartCenter);
        transform.getTransform().setRotation(grid.hlOr);
        //transform . addChild(geode);
        geode.getTransform().setParent(transform.getTransform());
        //transform -> setNodeMask(~simgear::MODELLIGHT_BIT);

        created.add(b);
        return new SGOceanTile(transform, grid);
    }

    private static Log getLog(){
        if (log == null) {
            log = Platform.getInstance().getLog(SGOceanTile.class);
        }
        return log;
    }
}

