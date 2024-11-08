package de.yard.threed.flightgear.core.simgear.scene.material;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.Vector2;
import de.yard.threed.flightgear.core.AreaList;
import de.yard.threed.flightgear.core.ArrayListSGMaterialFactory;
import de.yard.threed.flightgear.core.CppHashMap;
import de.yard.threed.flightgear.core.PropertyList;
import de.yard.threed.flightgear.core.osgdb.Options;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.math.SGRectFloat;
import de.yard.threed.flightgear.core.simgear.props.PropsIO;
import de.yard.threed.flightgear.core.simgear.props.SGCondition;
import de.yard.threed.flightgear.core.simgear.structure.SGException;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.platform.Config;

import java.util.List;

/**
 * Aus matlib.[hc]xx
 * Material management class
 * <p>
 * 12.6.17: Jetzt auch aus Bundle, das schon geladen sein muss.
 * <p/>
 * Created by thomass on 04.08.16.
 */
public class SGMaterialLib {
    public static final String BUNDLENAME = "sgmaterial";
    static Log logger = Platform.getInstance().getLog(SGMaterialLib.class);
    static public boolean materiallibdebuglog = false;

    //class MatLibPrivate    ;   std::    auto_ptr<MatLibPrivate> d;

    // associative array of materials
    /*typedef std
    ::
    vector<SGSharedPtr<SGMaterial>> material_list;
    typedef material_list
    ::
    iterator material_list_iterator;

    typedef std
    ::map<std::string,material_list>material_map;
    typedef material_map
    ::
    iterator material_map_iterator;
    typedef material_map
    ::
    const_iterator const_material_map_iterator;*/

    // Um aus der Vielzahl von Namen für ein Material einfach zum Material zu kommen. Ein Name kann auch mehrere Materialen zugeordnet haben.
    //Wofuer das ist??
    /*material_map*/ public CppHashMap<String, List<SGMaterial>> matlib = new CppHashMap<String, List<SGMaterial>>(new ArrayListSGMaterialFactory());


    // Constructor
    public SGMaterialLib() {
    }

    /**
     * Load a library of material properties.
     * "mpath" eg. points to "Materials/regions/materials.xml".
     */
    public boolean load(/*String fg_root*/ String mpath, SGPropertyNode prop_root) {
        SGPropertyNode materialblocks = new SGPropertyNode();
        if (materiallibdebuglog) {
            logger.debug("load:Reading materials from " + mpath);
        }
        Bundle bundle = BundleRegistry.getBundle(BUNDLENAME);
        if (bundle == null) {
            logger.error("Bundle not loaded:" + BUNDLENAME);
            return false;
        }
        //SG_LOG( SG_INPUT, SG_INFO, "Reading materials from " << mpath );
        try {
            new PropsIO().readProperties(new BundleResource(bundle, mpath), materialblocks);
        } catch (SGException ex) {
            logger.error(/*SG_LOG( SG_INPUT, SG_ALERT*/ "Error reading materials in " + mpath + ": " + ex.getMessage());
            return false;
        }
        Options options = new Options();
        //TODO options.setObjectCacheHint(Options.CACHE_ALL);
        //options.setDatabasePath(fg_root);

        PropertyList blocks = materialblocks.getChildren("region");
        //PropertyList::const_iterator block_iter = blocks.begin();
        //for (; block_iter != blocks.end(); block_iter++) {
        //   SGPropertyNode node = block_iter.get();
        for (SGPropertyNode node : blocks) {

            // Read name node purely for logging purposes
            SGPropertyNode nameNode = node.getChild("name");
            if (nameNode != null) {
                logger.info(/*SG_LOG( SG_TERRAIN, SG_INFO,*/ "Loading region(PropertyList) '" + nameNode.getStringValue() + "' from " + mpath);
            }

            // Read list of areas
            AreaList arealist = new AreaList();

            PropertyList areas = node.getChildren("area");
            //simgear::PropertyList::const_iterator area_iter = areas.begin();
            //for (; area_iter != areas.end(); area_iter++) {
            for (SGPropertyNode area_iter : areas) {
                float x1 = area_iter/*.get()*/.getFloatValue("lon1", -180.0f);
                float x2 = area_iter/*.get()*/.getFloatValue("lon2", 180.0f);
                float y1 = area_iter/*.get()*/.getFloatValue("lat1", -90.0f);
                float y2 = area_iter/*.get()*/.getFloatValue("lat2", 90.0f);
                SGRectFloat rect = new SGRectFloat(Math.min(x1, x2),
                        Math.min(y1, y2),
                        Math.abs(x2 - x1),
                        Math.abs(y2 - y1));
                arealist.add(rect);
                if (materiallibdebuglog) {
                    logger.debug(/*SG_LOG( SG_TERRAIN, SG_INFO,*/ " Area (" + rect.x() + "," + rect.y() + ") width:" + rect.width() + " height:" + rect.height());
                }
            }

            // Read conditions node
            SGPropertyNode conditionNode = node.getChild("condition");
            SGCondition condition = null;
            if (conditionNode != null) {
                // copies conditions from 'conditionNode' into prop_root? Or uses prop_root for check eg. "season"?
                condition = SGCondition.sgReadCondition(prop_root, conditionNode);
            }

            // Now build all the materials for this set of areas and conditions

            PropertyList materials = node.getChildren("material");
            //simgear::PropertyList::const_iterator materials_iter = materials.begin();
            //for (; materials_iter != materials.end(); materials_iter++) {
            //   SGPropertyNode *node = materials_iter.get();

            for (SGPropertyNode node1 : materials) {
                // 7.11.24: prop_root probably is not used in constructor
                SGMaterial m = new SGMaterial(options/*.get()*/, node1, prop_root, arealist, condition);

                List<SGPropertyNode> names = node1.getChildren("name");
                for (int j = 0; j < names.size(); j++) {
                    String name = names.get(j).getStringValue();
                    // cerr << "Material " << name << endl;
                    matlib.get(name).add(m);
                    m.add_name(name);
                    if (m._status.size() == 0) {
                        logger.warn("No effect(texture?) for material " + name);
                    }
                    if (materiallibdebuglog) {
                        logger.debug(/*(SG_TERRAIN,*/ "Built material for name " + name);
                    }
                }
            }
        }
        return true;
    }

    // find a material record by material name
    public SGMaterial find(String material, Vector2 center) {
        SGMaterial result = null;
        /*const_material_map_iterator it = matlib.find(material);
        if (it != end()) {
            // We now have a list of materials that match this
            // name. Find the getFirst one that matches.
            // We start at the end of the list, as the materials
            // list isType ordered with the smallest regions at the end.
            material_list::const_reverse_iterator iter = it -> getSecond.rbegin();
            while (iter != it -> getSecond.rend()) {
                result =*iter;
                if (result -> valid(center)) {
                    return result;
                }
                iter++;
            }
        }*/
        if (material.equals("Grassland")) {
            result = null;
        }
        List<SGMaterial> it = matlib.get(material);
        for (int i = it.size() - 1; i >= 0; i--) {
            result = it.get(i);
            if (result.valid(center)) {
                return result;
            }
        }
        logger.warn("no material " + material + " for center " + center);
        return null;
    }

    /**
     * Helpful for testing
     */
    public List<SGMaterial> get(String material) {
        return matlib.get(material);
    }

    SGMaterial find(String material, SGGeod center) {
        Vector2 c = new Vector2((float) center.getLongitudeDeg().getDegree(), (float) center.getLatitudeDeg().getDegree());
        return find(material, c);
    }

    /**
     * Material lookup involves evaluation of position and SGConditions to
     * determine which possible material (by season, region, etc) isType valid.
     * This involves property tree queries, so repeated calls to find() can cause
     * race conditions when called from the osgDB pager thread. (especially
     * during startup)
     * <p/>
     * To fix this, and also avoid repeated re-evaluation of the material
     * conditions, we provide factory method to generate a material library
     * cache of the valid materials based on the current state and a given position.
     */

    public SGMaterialCache generateMatCache(Vector2 center) {
        SGMaterialCache newCache = new SGMaterialCache();
        /*material_map::const_reverse_iterator it = matlib.rbegin();
        for (; it != matlib.rend(); ++it) {
            newCache -> insert(it -> getFirst, find(it -> getFirst, center));
        }*/
        //TODO reverse from end wie iterator
        for (String name : matlib.keySet()) {
            newCache.insert(name, find(name, center));
        }

        return newCache;
    }

    public SGMaterialCache generateMatCache(SGGeod center) {
        /*SGVec2f*/
        Vector2 c = new Vector2((float) center.getLongitudeDeg().getDegree(), (float) center.getLatitudeDeg().getDegree());
        return generateMatCache(c);
    }

    /*material_map_iterator begin() {
        return matlib.begin();
    }

    const_material_map_iterator begin()

    const

    {
        return matlib.begin();
    }

    material_map_iterator end() {
        return matlib.end();
    }

    const_material_map_iterator end()

    const

    {
        return matlib.end();
    }*/

    /*static SGMaterial findMaterial(/*const osg::* /Geode geode) {
        if (geode == null)
            return null;
        EffectGeode * effectGeode;
        effectGeode = dynamic_cast <const simgear::EffectGeode * > (geode);
        if (!effectGeode)
            return 0;
        const simgear::Effect * effect = effectGeode . getEffect();
        if (!effect)
            return 0;
        const SGMaterialUserData * userData;
        userData = dynamic_cast <const SGMaterialUserData * > (effect . getUserData());
        if (!userData)
            return 0;
        return userData . getMaterial();
    }*/
}
