package de.yard.threed.flightgear.core.simgear.scene.material;

import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.flightgear.core.AreaList;
import de.yard.threed.flightgear.core.CppHashMap;
import de.yard.threed.core.Pair;
import de.yard.threed.flightgear.core.PropertyList;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.MaterialPool;
import de.yard.threed.engine.Texture;
import de.yard.threed.flightgear.core.SGMaterialGlyphFactory;
import de.yard.threed.flightgear.core.osgdb.Options;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.bvh.BVHMaterial;
import de.yard.threed.flightgear.core.simgear.math.SGRectFloat;
import de.yard.threed.flightgear.core.simgear.misc.SGPath;
import de.yard.threed.flightgear.core.simgear.props.PropsIO;
import de.yard.threed.flightgear.core.simgear.props.SGCondition;
import de.yard.threed.flightgear.core.simgear.scene.util.RenderConstants;
import de.yard.threed.flightgear.core.simgear.scene.util.SGReaderWriterOptions;
import de.yard.threed.flightgear.core.simgear.structure.SGException;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.Color;
import de.yard.threed.core.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * In Anlehnung an mat.[hc]xx
 * <p>
 * Corresponds to a material element in one of the sgmaterial XML files. Properties are
 * - an effect(name?). Is this the identifier? Probably not because not unique like 'Effects/urban'.
 * - multiple names
 * - a texture(name)
 * - a.s.o.
 * A SGMaterial/mat seems to be just a material (property tree) definition initially. Shader, textures, etc
 * are created later when needed (by ??()?).
 * <p>
 * <p/>
 * Created by thomass on 23.02.16.
 */
public class SGMaterial extends BVHMaterial {
    static Log logger = Platform.getInstance().getLog(SGMaterial.class);

    ////////////////////////////////////////////////////////////////////
    // Internal state.
    ////////////////////////////////////////////////////////////////////

    // texture status (via Effect?)
    // List increases with every found texture. Thus remains empty if no texture is found.
    /*std::vector<_internal_state>*/ public List<InternalState> _status = new ArrayList<InternalState>();

    // texture size
    double xsize, ysize;

    // wrap texture?
    boolean wrapu, wrapv;

    // use mipmapping?
    boolean mipmap;

    // coverage of night lighting.
    double light_coverage;

    // coverage of buildings
    double building_coverage;

    // building spacing
    double building_spacing;

    // building texture & lightmap
    /*String*/ BundleResource building_texture;
    /*String*/ BundleResource building_lightmap;

    // Ratio of the 3 random building sizes
    double building_small_ratio;
    double building_medium_ratio;
    double building_large_ratio;

    // Proportion of buildings with pitched roofs
    double building_small_pitch;
    double building_medium_pitch;
    double building_large_pitch;

    // Min/Max number of floors for each size
    int building_small_min_floors;
    int building_small_max_floors;
    int building_medium_min_floors;
    int building_medium_max_floors;
    int building_large_min_floors;
    int building_large_max_floors;

    // Minimum width and depth for each size
    double building_small_min_width;
    double building_small_max_width;
    double building_small_min_depth;
    double building_small_max_depth;

    double building_medium_min_width;
    double building_medium_max_width;
    double building_medium_min_depth;
    double building_medium_max_depth;

    double building_large_min_width;
    double building_large_max_width;
    double building_large_min_depth;
    double building_large_max_depth;

    double building_range;

    // Cosine of the angle of maximum and zero density, 
    // used to stop buildings and random objects from being 
    // created on too steep a slope.
    double cos_object_max_density_slope_angle;
    double cos_object_zero_density_slope_angle;

    // coverage of woods
    double wood_coverage;

    // Range at which trees become visible
    double tree_range;

    // Height of the tree
    double tree_height;

    // Width of the tree
    double tree_width;

    // Number of varieties of tree texture
    int tree_varieties;

    // cosine of the tile angle of maximum and zero density,
    // used to stop trees from being created on too steep a slope.
    double cos_tree_max_density_slope_angle;
    double cos_tree_zero_density_slope_angle;

    // material properties
    /*SGVec4f*/ Color ambient, diffuse, specular, emission;
    double shininess;

    // effect for this material
    String effect;

    // the list of names for this material. May be empty.
    List<String> _names = new ArrayList<String>();

    List</*SGSharedPtr<*/SGMatModelGroup> object_groups = new ArrayList<SGMatModelGroup>();

    // taxiway-/runway-sign texture elements
    CppHashMap<String, /*SGSharedPtr<*/SGMaterialGlyph> glyphs = new CppHashMap<String, SGMaterialGlyph>(new SGMaterialGlyphFactory());

    // Tree texture, typically a strip of applicable tree textures
    /*String*/ BundleResource tree_texture;

    // Object mask, a simple RGB texture used as a mask when placing
    // random vegetation, objects and buildings
    List<Texture/*2DRef*/> _masks = new ArrayList<Texture>();

    // Condition, indicating when this material isType active
    /*SGSharedPtr<const*/ SGCondition condition;

    // List of geographical rectangles for this material
    AreaList areas;

    // Parameters from the materials file
    SGPropertyNode parameters;

    // per-material lockEntity for entrypoints called from multiple threads
    //SGMutex _lock;

    // Zum tracking der einzelnen Instanzen
    private static int uniqueidpool = 0;
    public int uniqueid = uniqueidpool++;

    /**
     * fuer CppHashMap dummy
     */
    public SGMaterial() {
    }

    /**
     * Construct a material from a set of properties.
     *
     * @param props A property node containing subnodes with the
     *              state information for the material.  This node isType usually
     *              loaded from the $FG_ROOT/materials.xml file.
     */
    SGMaterial(SGReaderWriterOptions options, SGPropertyNode props, SGPropertyNode prop_root, AreaList a, SGCondition c, boolean forBtgConversion) {
        init();
        areas = a;
        condition = c;
        read_properties(options, props, prop_root);
        buildEffectProperties(options, forBtgConversion);
    }

    SGMaterial(Options options, SGPropertyNode props, SGPropertyNode prop_root, AreaList a, SGCondition c, boolean forBtgConversion) {
        if (SGMaterialLib.materiallibdebuglog) {
            logger.debug("Building SGMaterial with condition " + c);
        }
        SGReaderWriterOptions opt;
        opt = SGReaderWriterOptions.copyOrCreate(options);
        areas = a;
        condition = c;
        init();
        read_properties(opt/*.get()*/, props, prop_root);
        buildEffectProperties(opt/*.get()*/, forBtgConversion);
    }

    /**
     * Das ist nur eine Q&D Kurzform, die es in FG nicht gibt, darum deprecated.
     *
     * @param xmlfilename
     * @param materials
     */
    @Deprecated
    public static void loadMaterial(String xmlfilename, MaterialPool materials) {

        SGPropertyNode/*_ptr*/ props = new SGPropertyNode(null/*TODO*/);

        try {
            new PropsIO().readProperties(xmlfilename, props);
        } catch (SGException t) {
            logger.error("Failed to load xml: " + t.getMessage());

        }
        //props = props.getChildren("PropertyList").get(0);
        for (SGPropertyNode materialnode : props.getChildren("material")) {
            Material material = null;
            PropertyList texturelist = materialnode.getChildren("texture");
            float xsize = materialnode.getFloatValue("xsize", 0.0f);
            float ysize = materialnode.getFloatValue("ysize", 0.0f);
            boolean wrapu = materialnode.getBoolValue("wrapu", true);
            boolean wrapv = materialnode.getBoolValue("wrapv", true);
            boolean mipmap = materialnode.getBoolValue("mipmap", true);
            float light_coverage = materialnode.getFloatValue("light-coverage", 0.0f);

            for (SGPropertyNode texturenode : texturelist) {
                Texture tex = loadTexture(texturenode.getStringValue(), wrapu, wrapv);
                material = Material.buildCustomShaderMaterial(tex);
            }
            if (material != null) {

                for (SGPropertyNode namenode : materialnode.getChildren("name")) {

                    String name = namenode.getStringValue();

                    logger.info("caching material with name " + name);
                    materials.put(name, material);
                }
            }
        }
    }

    public static Texture loadTexture(String name, boolean wrapu, boolean wrapv) {
        //Bundle bundle = BundleRegistry.getBundle(SGMaterialLib);

        //String fgroot = Platform.getInstance().getSystemProperty("FG_ROOT");
        //String basedir = fgroot + "/Textures";
        //return new Texture(new FileSystemResource(basedir + "/" + name), wrapu, wrapv);
        //return Texture.buildBundleTexture("FGTextures",name,wrapu,wrapv);
        return Texture.buildBundleTexture(buildTextureBundleResource(name), wrapu, wrapv);
    }

    /**
     * Never returns null.
     *
     * @param name
     * @return
     */
    public static BundleResource buildTextureBundleResource(String name) {
        Bundle bundle = BundleRegistry.getBundle(SGMaterialLib.BUNDLENAME);
        BundleResource br = new BundleResource(bundle, name);
        return br;
    }

    void read_properties(SGReaderWriterOptions options, SGPropertyNode props, SGPropertyNode prop_root) {
        List<Boolean> dds = new ArrayList<Boolean>();
        /*std::vector<*/
        List<SGPropertyNode> textures = props.getChildren("texture");
        for (int i = 0; i < textures.size(); i++) {
            String tname = textures.get(i).getStringValue();
            if (SGMaterialLib.materiallibdebuglog) {
                logger.debug("SGMaterial.read_properties:props.name=" + props.getName() + ",props.value=" + props.getStringValue());
            }

            if (StringUtils.empty(tname)) {
                logger.warn("empty tname. using unknown.rgb");
                tname = "unknown.rgb";
            }

            //SGPath tpath = new SGPath("Textures");
            //tpath.append(tname);
            //String fullTexPath = SGModelLib.findDataFile(tpath.str(), options);
            BundleResource fullTexPath = buildTextureBundleResource("Textures/" + tname);
            if (!fullTexPath.bundle.exists(fullTexPath)) {
                // material name not yet available for logging. Just a warning, because there might be further textures.
                logger.warn("Cannot find texture '" + tname + "' in Textures folders.");
            } else {

            /*DDS ??if (tpath.lower_extension().equals("dds")) {
                dds.add(true);
            } else {
                dds.add(false);
            }*/

                //if (!StringUtils.empty(fullTexPath)) {
                InternalState st = new InternalState(null, fullTexPath, false, options);
                _status.add(st);
            }
        }

        List<SGPropertyNode> texturesets = props.getChildren("texture-set");
        for (int i = 0; i < texturesets.size(); i++) {
            InternalState st = new InternalState(null, false, options);
            textures = texturesets.get(i).getChildren("texture");
            for (int j = 0; j < textures.size(); j++) {
                String tname = textures.get(j).getStringValue();
                if (StringUtils.empty(tname)) {
                    logger.warn("empty tname. using unknown.rgb");
                    tname = "unknown.rgb";
                }

                //SGPath tpath = new SGPath("Textures");
                //tpath.append(tname);
                //String fullTexPath = SGModelLib.findDataFile(tpath.str(), options);
                //if (StringUtils.empty(fullTexPath)) {
                BundleResource fullTexPath = buildTextureBundleResource("Textures/" + tname);
                if (!fullTexPath.bundle.exists(fullTexPath)) {
                    logger.error(/*SG_LOG(SG_GENERAL, SG_ALERT,*/ "Cannot find texture \"" + tname + "\" in Textures folders.");
                } else {

               /*DDS?? if (j == 0) {
                    if (tpath.lower_extension().equals("dds")) {
                        dds.add(true);
                    } else {
                        dds.add(false);
                    }
                }*/

                    st.add_texture(fullTexPath, textures.get(j).getIndex());
                }
            }

            if (!st.texture_paths.isEmpty()) {
                _status.add(st);
            }
        }

        if (textures.isEmpty() && texturesets.isEmpty()) {
            //SGPath tpath = new SGPath("Textures");
            //tpath.append("Terrain");
            //tpath.append("unknown.rgb");
            BundleResource tpath = buildTextureBundleResource("Textures/Terrain/unknown.rgb");

            InternalState st = new InternalState(null, tpath, true, options);
            _status.add(st);
        }

        List<SGPropertyNode> masks = props.getChildren("object-mask");
        for (int i = 0; i < masks.size(); i++) {
            String omname = masks.get(i).getStringValue();

            if (!StringUtils.empty(omname)) {
                if (SGMaterialLib.materiallibdebuglog) {
                    logger.debug("Building object mask " + omname);
                }
                //12.6.17: Umgestellt auf Bundle
                //SGPath ompath = new SGPath("Textures");
                //ompath.append(omname);
                //String fullMaskPath = SGModelLib.findDataFile(ompath.str(), options);
                //Bundle bundle = BundleRegistry.getBundle(SGMaterialLib.BUNDLENAME);

                BundleResource fullMaskPath = buildTextureBundleResource("Textures/" + omname);

                if (!fullMaskPath.bundle.exists(fullMaskPath)) {
                    logger.error(/*SG_LOG(SG_GENERAL, SG_ALERT,*/ "Cannot find texture \"" + omname + "\" in Textures folders.");
                } else {
                    /* Von FG abweichende Implementierung
                    osg::Image* image = osgDB::readImageFile(fullMaskPath, options);                    
                    if (image && image.valid())
                    {
                        Texture2DRef object_mask = new osg::Texture2D;

                        boolean dds_mask = ompath.lower_extension() .equals( "dds");

                        if (i < dds.size() && dds.get(i) != dds_mask) {
                            // Texture format does not match mask format. This isType relevant for
                            // the object mask, as DDS textures have an origin at the bottom
                            // left rather than top left. Therefore we flip a copy of the image
                            // (otherwise a getSecond reference to the object mask would flip it
                            // back!).                
                            logger.debug(/*SG_LOG(SG_GENERAL, SG_DEBUG,* / "Flipping object mask " + omname);
                            image = (osg::Image* ) image.clone(osg::CopyOp::SHALLOW_COPY);
                            image.flipVertical();
                        }

                        object_mask.setImage(image);

                        // We force the filtering to be nearest, as the red channel (rotation)
                        // in particular, doesn't make sense to be interpolated between pixels.
                        object_mask.setFilter(osg::Texture::MIN_FILTER, osg::Texture::NEAREST);
                        object_mask.setFilter(osg::Texture::MAG_FILTER, osg::Texture::NEAREST);

                        object_mask.setDataVariance(osg::Object::STATIC);
                        object_mask.setWrap(osg::Texture::WRAP_S, osg::Texture::REPEAT);
                        object_mask.setWrap(osg::Texture::WRAP_T, osg::Texture::REPEAT);
                        _masks.push_back(object_mask);
                    }*/
                    //TODO nur provisorisch, NEArest fehlt und flip. 26.4.17 bundle auch provisorisch
                    Texture object_mask;//= new Texture(new FileSystemResource(fullMaskPath), true, true);
                    object_mask = Texture.buildBundleTexture(fullMaskPath, wrapu, wrapv);

                    _masks.add(object_mask);
                }
            }
        }

        xsize = props.getDoubleValue("xsize", 0.0);
        ysize = props.getDoubleValue("ysize", 0.0);
        wrapu = props.getBoolValue("wrapu", true);
        wrapv = props.getBoolValue("wrapv", true);
        mipmap = props.getBoolValue("mipmap", true);
        light_coverage = props.getDoubleValue("light-coverage", 0.0);

        // Building properties
        building_coverage = props.getDoubleValue("building-coverage", 0.0);
        building_spacing = props.getDoubleValue("building-spacing-m", 5.0);

        //28.6.17 Wegen Komponentiserung keine Pfadsuche mehr.

        String bt = props.getStringValue("building-texture", "Textures/buildings.png");
        //building_texture = SGModelLib.findDataFile(bt, options);
        building_texture = buildTextureBundleResource(bt);

        if (/*StringUtils.empty(*/!building_texture.exists()) {
            logger.error(/*SG_LOG(SG_GENERAL, SG_ALERT,*/"Cannot find texture \"" + bt);
        }

        bt = props.getStringValue("building-lightmap", "Textures/buildings-lightmap.png");
        //building_lightmap = SGModelLib.findDataFile(bt, options);
        building_lightmap = buildTextureBundleResource(bt);

        if (/*StringUtils.empty(*/!building_lightmap.exists()) {
            logger.error(/*SG_LOG(SG_GENERAL, SG_ALERT,*/ "Cannot find texture \"" + bt);
        }

        building_small_ratio = props.getDoubleValue("building-small-ratio", 0.8);
        building_medium_ratio = props.getDoubleValue("building-medium-ratio", 0.15);
        building_large_ratio = props.getDoubleValue("building-large-ratio", 0.05);

        building_small_pitch = props.getDoubleValue("building-small-pitch", 0.8);
        building_medium_pitch = props.getDoubleValue("building-medium-pitch", 0.2);
        building_large_pitch = props.getDoubleValue("building-large-pitch", 0.1);

        building_small_min_floors = props.getIntValue("building-small-min-floors", 1);
        building_small_max_floors = props.getIntValue("building-small-max-floors", 3);
        building_medium_min_floors = props.getIntValue("building-medium-min-floors", 3);
        building_medium_max_floors = props.getIntValue("building-medium-max-floors", 8);
        building_large_min_floors = props.getIntValue("building-large-min-floors", 5);
        building_large_max_floors = props.getIntValue("building-large-max-floors", 20);

        building_small_min_width = props.getFloatValue("building-small-min-width-m", 15.0f);
        building_small_max_width = props.getFloatValue("building-small-max-width-m", 60.0f);
        building_small_min_depth = props.getFloatValue("building-small-min-depth-m", 10.0f);
        building_small_max_depth = props.getFloatValue("building-small-max-depth-m", 20.0f);

        building_medium_min_width = props.getFloatValue("building-medium-min-width-m", 25.0f);
        building_medium_max_width = props.getFloatValue("building-medium-max-width-m", 50.0f);
        building_medium_min_depth = props.getFloatValue("building-medium-min-depth-m", 20.0f);
        building_medium_max_depth = props.getFloatValue("building-medium-max-depth-m", 50.0f);

        building_large_min_width = props.getFloatValue("building-large-min-width-m", 50.0f);
        building_large_max_width = props.getFloatValue("building-large-max-width-m", 75.0f);
        building_large_min_depth = props.getFloatValue("building-large-min-depth-m", 50.0f);
        building_large_max_depth = props.getFloatValue("building-large-max-depth-m", 75.0f);

        building_range = props.getDoubleValue("building-range-m", 10000.0);

        cos_object_max_density_slope_angle = Math.cos(props.getFloatValue("object-max-density-angle-deg", 20.0f) * Math.PI / 180.0);
        cos_object_zero_density_slope_angle = Math.cos(props.getFloatValue("object-zero-density-angle-deg", 30.0f) * Math.PI / 180.0);

        // Random vegetation properties
        wood_coverage = props.getDoubleValue("wood-coverage", 0.0);
        tree_height = props.getDoubleValue("tree-height-m", 0.0);
        tree_width = props.getDoubleValue("tree-width-m", 0.0);
        tree_range = props.getDoubleValue("tree-range-m", 0.0);
        tree_varieties = props.getIntValue("tree-varieties", 1);
        cos_tree_max_density_slope_angle = Math.cos(props.getFloatValue("tree-max-density-angle-deg", 30.0f) * Math.PI / 180.0);
        cos_tree_zero_density_slope_angle = Math.cos(props.getFloatValue("tree-zero-density-angle-deg", 45.0f) * Math.PI / 180.0);

        SGPropertyNode treeTexNode = props.getChild("tree-texture");

        if (treeTexNode != null) {
            String treeTexPath = props.getStringValue("tree-texture");

            if (!StringUtils.empty(treeTexPath)) {
                SGPath treePath = new SGPath("Textures");
                treePath.append(treeTexPath);
                //tree_texture = SGModelLib.findDataFile(treePath.str(), options);
                tree_texture = buildTextureBundleResource(treePath.str());

                if (/*StringUtils.empty(*/!tree_texture.exists()) {
                    logger.error(/*SG_LOG(SG_GENERAL, SG_ALERT,*/ "Cannot find tree_texture \"" + tree_texture + "\" in Textures folders.");
                }
            }
        }

        // surface values for use with ground reactions
        _solid = props.getBoolValue("solid", _solid);
        _friction_factor = props.getDoubleValue("friction-factor", _friction_factor);
        _rolling_friction = props.getDoubleValue("rolling-friction", _rolling_friction);
        _bumpiness = props.getDoubleValue("bumpiness", _bumpiness);
        _load_resistance = props.getDoubleValue("load-resistance", _load_resistance);

        // Taken from default values as used in ac3d
        ambient = new Color(props.getFloatValue("ambient/r", 0.2f),
                props.getFloatValue("ambient/g", 0.2f),
                props.getFloatValue("ambient/b", 0.2f),
                props.getFloatValue("ambient/a", 1.0f));

        diffuse = new Color(props.getFloatValue("diffuse/r", 0.8f),
                props.getFloatValue("diffuse/g", 0.8f),
                props.getFloatValue("diffuse/b", 0.8f),
                props.getFloatValue("diffuse/a", 1.0f));

        specular = new Color(props.getFloatValue("specular/r", 0.0f),
                props.getFloatValue("specular/g", 0.0f),
                props.getFloatValue("specular/b", 0.0f),
                props.getFloatValue("specular/a", 1.0f));

        emission = new Color(props.getFloatValue("emissive/r", 0.0f),
                props.getFloatValue("emissive/g", 0.0f),
                props.getFloatValue("emissive/b", 0.0f),
                props.getFloatValue("emissive/a", 1.0f));

        shininess = props.getDoubleValue("shininess", 1.0);

        if (props.hasChild("effect"))
            effect = props.getStringValue("effect");

        List<SGPropertyNode> object_group_nodes =               /* ((SGPropertyNode *)*/ props.getChildren("object-group");
        for (int i = 0; i < object_group_nodes.size(); i++)
            object_groups.add(new SGMatModelGroup(object_group_nodes.get(i)));

        // read glyph table for taxi-/runway-signs
        List<SGPropertyNode> glyph_nodes = props.getChildren("glyph");
        for (int i = 0; i < glyph_nodes.size(); i++) {
            String name = glyph_nodes.get(i).getStringValue("name");
            if (name != null)
                glyphs.put(name, new SGMaterialGlyph(glyph_nodes.get(i)));
        }

        // Read parameters entry, which isType passed into the effect
        if (props.hasChild("parameters")) {
            parameters = props.getChild("parameters");
        } else {
            parameters = new SGPropertyNode();
        }
    }

    /**
     * Evaluate whether this material isType valid given the current global
     * property state and the tile location.
     */
    public boolean valid(Vector2 loc) {
        //zu oft logger.debug(/*SG_LOG( SG_TERRAIN, SG_BULK,*/ "Checking materials for location (" + loc.getX() + "," + loc.getY() + ")");

        // Check location getFirst again the areas the material isType valid for
        //AreaList::const_iterator i = areas->begin();

        if (areas.size() == 0) {//i == areas->end()) {
            // No areas defined, so simply check against condition
            if (condition != null) {
                return condition.test();
            } else {
                return true;
            }
        }

        //; i != areas->end(); i++)
        for (SGRectFloat i : areas) {

            //SG_LOG( SG_TERRAIN, SG_BULK, "Checking area ("                << i->x() << ","                << i->y() << ") width:"                << i->width() << " height:"                << i->height());
            // Areas defined, so check that the tile location falls within it
            // before checking against condition
            if (i.contains(loc.getX(), loc.getY())) {
                if (condition != null) {
                    return condition.test();
                } else {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Get the xsize of the texture, in meters.
     */
    public double get_xsize() {
        return xsize;
    }


    /**
     * Get the ysize of the texture, in meters.
     */
    double get_ysize() {
        return ysize;
    }

    public Vector2 get_tex_coord_scale() {
        float tex_width = (float) get_xsize();
        float tex_height = (float) get_ysize();

        return new Vector2((0 < tex_width) ? 1000.0f / tex_width : 1.0f,
                (0 < tex_height) ? 1000.0f / tex_height : 1.0f);
    }

    void init() {
        _status.clear();
        xsize = 0;
        ysize = 0;
        wrapu = true;
        wrapv = true;

        mipmap = true;
        light_coverage = 0.0;
        building_coverage = 0.0;

        shininess = 1.0;
        /*for (int i = 0; i < 4; i++) {
            ambient[i]  = (i < 3) ? 0.2 : 1.0;
            specular[i] = (i < 3) ? 0.0 : 1.0;
            diffuse[i]  = (i < 3) ? 0.8 : 1.0;
            emission[i] = (i < 3) ? 0.0 : 1.0;
        }*/
        ambient = new Color(0.2f, 0.2f, 0.2f, 1);
        specular = new Color(0.0f, 0.0f, 0.0f, 1);
        diffuse = new Color(0.8f, 0.8f, 0.8f, 1);
        emission = new Color(0.0f, 0.0f, 0.0f, 1);
        // default for ALL materials?
        effect = "Effects/terrain-default";
    }

    /**
     * Applies an effect to a texture via Geode node visitor in FG? No, it is done
     * duringterrain building, so before draw.
     */
    Effect get_effect(int i) {
        // 28.10.24 avoid NPE
        if (_status.get(i).getEffect() == null) {
            return null;
        }
        // 28.10.24: Somehow strange to have a additional effect_realized flag outside of effect. Effect also has one.
        if (!_status.get(i).effect_realized) {
            if (!_status.get(i).getEffect().valid())
                return null;
            _status.get(i).getEffect().realizeTechniques(_status.get(i).options/*.get()*/);
            _status.get(i).effect_realized = true;
        }
        return _status.get(i).getEffect()/*.get()*/;
    }

    /**
     * Get the textured state.
     * texIndex apparently might result from landclass in BTG file.
     * 23.7.24: Probably means 'get one of the textures listed in XML'.
     */
    public Effect get_one_effect(int texIndex) {
        // SGGuard<SGMutex> g(_lock);
        if (_status.isEmpty()) {
            logger.warn("No effect available. Maybe no texture found at all for effect '" + effect + "'");
            return null;
        }

        int i = texIndex % _status.size();
        //FG-DIFF Java kann auch negative Werte liefern.
        i = Math.abs(i);
        if (SGMaterialLib.materiallibdebuglog) {
            logger.debug("texIndex=" + texIndex + ",size=" + _status.size() + ",i=" + i);
        }
        return get_effect(i);
    }

    /**
     * Zum Testen.
     */
    public List<Pair<BundleResource, Integer>> getTexturePaths(int texIndex) {
        // SGGuard<SGMutex> g(_lock);
        if (_status.isEmpty()) {
            logger.warn(/*SG_LOG( SG_GENERAL, SG_WARN,*/ "No effect available.");
            return null;
        }

        int i = texIndex % _status.size();
        return _status.get(i).texture_paths;
    }
    
   /* osg::Texture2D* SGMaterial::get_one_object_mask(int texIndex)
    {
        if (_status.empty()) {
            SG_LOG( SG_GENERAL, SG_WARN, "No mask available.");
            return 0;
        }

        // Note that the object mask isType closely linked to the texture/effect
        // so we index based on the texture index, 
        unsigned int i = texIndex % _status.size();
        if (i < _masks.size()) {
            return _masks[i].get();
        } else {
            return 0;
        }
    }*/

    void buildEffectProperties(SGReaderWriterOptions options, boolean forBtgConversion) {
        if (SGMaterialLib.materiallibdebuglog) {
            logger.debug("buildEffectProperties ");
        }
        //TODO ref_ptr<SGMaterialUserData> user = new SGMaterialUserData(this);
        SGPropertyNode propRoot = new SGPropertyNode();
        SGPropertyNode.makeChild(propRoot, "inherits-from").setStringValue(effect);

        SGPropertyNode paramProp = SGPropertyNode.makeChild(propRoot, "parameters");
        PropsIO.copyProperties(parameters, paramProp);

        SGPropertyNode.makeChild(paramProp, "uniqueid").setIntValue(uniqueid);

        SGPropertyNode materialProp = SGPropertyNode.makeChild(paramProp, "material");
        SGPropertyNode.makeChild(materialProp, "ambient").setColorValue(/*SGVec4d*/(ambient));
        SGPropertyNode.makeChild(materialProp, "diffuse").setColorValue(/*SGVec4d*/(diffuse));
        SGPropertyNode.makeChild(materialProp, "specular").setColorValue(/*SGVec4d*/(specular));
        SGPropertyNode.makeChild(materialProp, "emissive").setColorValue(/*SGVec4d*/(emission));
        SGPropertyNode.makeChild(materialProp, "shininess").setFloatValue((float) shininess);
        if (ambient.getAlpha() < 1 || diffuse.getAlpha() < 1 || specular.getAlpha() < 1 || emission.getAlpha() < 1) {
            SGPropertyNode.makeChild(paramProp, "transparent").setBoolValue(true);
            SGPropertyNode binProp = SGPropertyNode.makeChild(paramProp, "render-bin");
            SGPropertyNode.makeChild(binProp, "bin-number").setIntValue(RenderConstants.TRANSPARENT_BIN);
            SGPropertyNode.makeChild(binProp, "bin-name").setStringValue("DepthSortedBin");
        }
        //BOOST_FOREACH(_internal_state& matState, _status)
        for (InternalState matState : _status) {
            SGPropertyNode effectProp = new SGPropertyNode();
            PropsIO.copyProperties(propRoot, effectProp);
            SGPropertyNode effectParamProp = effectProp.getChild("parameters", 0);
            for (int i = 0; i < matState.texture_paths.size(); i++) {
                SGPropertyNode texProp = SGPropertyNode.makeChild(effectParamProp, "texture", (int) matState.texture_paths.get(i).getSecond());
                //12.6.16: Der Bundlename kommt mal nicht mit in die Property
                SGPropertyNode.makeChild(texProp, "image").setStringValue(matState.texture_paths.get(i).getFirst().getFullName());
                SGPropertyNode.makeChild(texProp, "filter")
                        .setStringValue(mipmap ? "linear-mipmap-linear" : "nearest");
                SGPropertyNode.makeChild(texProp, "wrap-s")
                        .setStringValue(wrapu ? "repeat" : "clamp-to-edge");
                SGPropertyNode.makeChild(texProp, "wrap-t")
                        .setStringValue(wrapv ? "repeat" : "clamp-to-edge");
            }
            SGPropertyNode.makeChild(effectParamProp, "xsize").setDoubleValue(xsize);
            SGPropertyNode.makeChild(effectParamProp, "ysize").setDoubleValue(ysize);
            SGPropertyNode.makeChild(effectParamProp, "scale").setVector3Value(new Vector3((float) xsize, (float) ysize, 0.0f));
            SGPropertyNode.makeChild(effectParamProp, "light-coverage").setDoubleValue(light_coverage);

            // 28.10.24: Build an effect for the texture defined in the material
            matState.setEffect(MakeEffect.makeEffect(effectProp, false, options, "SGMaterial...", forBtgConversion));
            if (matState.getEffect() != null && matState.getEffect().valid()) {
                //TODO matState.effect.setUserData(user.get());
            }
        }
    }

    /**
     * Get the list of names for this material
     */
    List<String> get_names() {
        return _names;
    }

    /**
     * add the given name to the list of names this material isType known
     */
    void add_name(String name) {
        _names.add(name);
    }

    /**
     * Die Namen concatenated lieferen für Anzeigezwecke.
     *
     * @return
     */
    public String getNames() {
        String n = "";
        for (String s : _names)
            n += ((StringUtils.length(n) > 0) ? "," : "") + s;
        return n;
    }

 /*   SGMaterialGlyph* SGMaterial::get_glyph (const std::string& name) const
    {
        map<std::string, SGSharedPtr<SGMaterialGlyph> >::const_iterator it;
        it = glyphs.find(name);
        if (it == glyphs.end())
            return 0;

        return it->getSecond;
    }*/

    /**
     * Only used for BTG conversion, so no need for effects.
     * From Effect.
     */
    /*public PortableMaterial getMaterialByTextureIndex(int textureindex) {
        if (_status.isEmpty()) {
            logger.warn("No texture available. Maybe no texture found at all for effect '" + effect + "'");
            return null;
        }
    }*/
}
