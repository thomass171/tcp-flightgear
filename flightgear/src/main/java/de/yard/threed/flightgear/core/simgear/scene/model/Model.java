package de.yard.threed.flightgear.core.simgear.scene.model;

import de.yard.threed.core.platform.*;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Mesh;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.flightgear.core.PropertyList;
import de.yard.threed.flightgear.core.osg.Node;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.scene.util.SGReaderWriterOptions;

import java.util.List;

/**
 * Aus model.cxx
 * <p/>
 * Created by thomass on 21.07.16.
 */
public class Model {
    static Log logger = Platform.getInstance().getLog(Model.class);

    /**
     * Noch voller TODO (alles auskommentierte)
     *
     * @param modelGroup
     * @param effectProps
     * @param options
     * @return
     */
    /*ref_ptr<*/
    public static Node instantiateEffects(/*osg::*/Node modelGroup, PropertyList effectProps, SGReaderWriterOptions options) {
        SGPropertyNode/*_ptr*/ defaultEffectPropRoot = null;
        /*MakeEffectVisitor visitor(options);
        MakeEffectVisitor::EffectMap& emap = visitor.getEffectMap();*/
        //for (PropertyList::iterator itr = effectProps.begin(), end = effectProps.end(); itr != end; ++itr)
        for (SGPropertyNode/*_ptr*/ configNode : effectProps) {
            //doofes Log durch dump logger.debug("instantiateEffects: effect=" + configNode.getStringValue() + " effect=" + configNode.dump("\n"));

            //SGPropertyNode_ptr configNode = *itr;
            /*std::vector<SGPropertyNode_ptr>*/
            PropertyList objectNames = configNode.getChildren("object-name");
            SGPropertyNode defaultNode = configNode.getChild("default");
            if (defaultNode != null && defaultNode.getBool/*Value/*<bool>*/()) {
                defaultEffectPropRoot = configNode;
            }
            for (SGPropertyNode objNameNode : objectNames) {
                if (SGReaderWriterXML.fgmodelloaddebug) {
                    logger.debug("instantiateEffects: objNameNode=" + objNameNode.getStringValue());
                }

                //emap.insert(make_pair(objNameNode->getStringValue(), configNode));

                // Beginn FG abweichende Implementierung wegen Effects (siehe Wiki)
                SGPropertyNode inherits_from = configNode.getChild("inherits-from");
                if (inherits_from != null && inherits_from.getStringValue().equals("Effects/model-transparent")) {
                    String objname = objNameNode.getStringValue();
                    // 5.10.17: Nicht global suchen, denn durch async ist die Node noch nicht in den tree eingehangen.
                    // je nach Loader (zB. gltf kann es Nodesduibeltten geben. Darum alle druchgehen und die mit Mesh suchen.
                    List<SceneNode> nlist = modelGroup.findNodeByName(objname);
                    if (nlist.size() == 0) {
                        // Kommt wohl schon mal vor. "Lettering_Btns" z.B. wird in boeing.xml auf transparent gesetzt gibt es aber nicht
                        logger.warn("object not found: " + objname);
                    }
                    boolean meshfound = false;
                    for (SceneNode n : nlist) {
                        //logger.debug("setting transparency for node " + n.getName());
                        Mesh mesh = n.getMesh();
                        if (mesh != null) {
                            Material mat = mesh.getMaterial();
                            mat.setTransparency(true);
                            meshfound=true;
                        }
                    }
                    if (!meshfound){
                        logger.warn("no mesh for setting transparency.");// subtree:"+modelGroup.dump("",0));
                    }
                }
                // Ende FG abweichende Implementierung

            }
            //configNode.removeChild("default");
            //configNode.removeChildren("object-name");
        }
        if (!(defaultEffectPropRoot != null))

        {
            //defaultEffectPropRoot = new DefaultEffect::instance () -> getEffect();
        }
        /*visitor.setDefaultEffect(defaultEffectPropRoot.ptr());
        modelGroup -> accept(visitor);
        osg::NodeList & result = visitor.getResults();
        return ref_ptr < Node > (result[0].get());*/
        return modelGroup;
    }
}

