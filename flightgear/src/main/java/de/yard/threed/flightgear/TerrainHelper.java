package de.yard.threed.flightgear;

import de.yard.threed.core.Color;
import de.yard.threed.core.Degree;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.geometry.IndexList;
import de.yard.threed.core.geometry.Primitives;
import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.GenericGeometry;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Mesh;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.apps.ModelSamples;
import de.yard.threed.flightgear.core.simgear.bucket.SGBucket;
import de.yard.threed.flightgear.core.simgear.geodesy.FgMath;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;

import java.util.ArrayList;
import java.util.List;

public class TerrainHelper {
    static Log logger = Platform.getInstance().getLog(TerrainHelper.class);

    /**
     * a green plane as dummy tile.
     */
    public static SceneNode buildDummyTile(SGBucket bucket) {
        SGGeod center = bucket.get_center();
        logger.debug("Building dummy tile at " + bucket.get_center() + "");

        List<Vector2> uvs = new ArrayList<Vector2>();
        List<Vector3> vertices = new ArrayList<Vector3>();
        List<Vector3> normals = new ArrayList<Vector3>();
        IndexList indexes = new IndexList();

        // Should work the same also for western and southern hemisphere.
        vertices.add(v(center, bucket.get_height() / 2, -bucket.get_width() / 2));
        vertices.add(v(center, bucket.get_height() / 2, bucket.get_width() / 2));
        vertices.add(v(center, -bucket.get_height() / 2, bucket.get_width() / 2));
        vertices.add(v(center, -bucket.get_height() / 2, -bucket.get_width() / 2));

        // dummy values for normals and uvs as they are not used by material
        for (int i = 0; i < 4; i++) {
            normals.add(new Vector3(1, 0, 0));
            uvs.add(new Vector2());
        }

        indexes.add(0, 2, 1);
        indexes.add(0, 3, 2);

        Mesh mesh = new Mesh(new SimpleGeometry(vertices, indexes.getIndices(), uvs, normals), Material.buildBasicMaterial(Color.DARKGREEN));
        SceneNode node = new SceneNode(mesh);
        node.setName("dummyTile" + bucket.gen_index());

        //optional marker for debugging
        //SceneNode marker = ModelSamples.buildAxisHelper(4000, 200f);
        //marker.getTransform().setPosition(vertices.get(0));
        //Scene.getCurrent().addToWorld(marker);

        return node;
    }

    /**
     * Use elevation 30 to make sure that also the inner parts of the tile will have an elevation > 0 (required to provide elevation
     * in FGScenery.get_elevation_m())
     */
    private static Vector3 v(SGGeod center, double latOffset, double lonOffset) {
        SGGeod geod = new SGGeod(
                new Degree(center.getLongitudeDeg().getDegree() + lonOffset),
                new Degree(center.getLatitudeDeg().getDegree() + latOffset), 30);
        return geod.toCart();
    }
}
