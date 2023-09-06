package de.yard.threed.flightgear.core.simgear.scene.material;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.flightgear.FlightGearMain;
import de.yard.threed.flightgear.FlightGearSettings;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.props.PropsIO;
import de.yard.threed.flightgear.core.simgear.scene.util.SGReaderWriterOptions;
import de.yard.threed.flightgear.core.simgear.structure.SGException;
import de.yard.threed.core.platform.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * aus makeEffect.cxx
 * <p/>
 * Created by thomass on 10.08.16.
 */
public class MakeEffect {
    static Log logger = Platform.getInstance().getLog(MakeEffect.class);

    //typedef vector<const SGPropertyNode*> RawPropVector;
    //typedef map<const string, ref_ptr<Effect> > EffectMap;
    /*Cpp*/ static HashMap<String, FGEffect> effectMap = new /*Cpp*/HashMap<String, FGEffect>(/*new Effect()*/);
    
    /*namespace
    {
        EffectMap effectMap;
        OpenThreads::ReentrantMutex effectMutex;
    }*/

    /**
     * Merge two property trees, producing a new tree.
     * If the nodes are both leaves, value comes from left leaf.
     * Otherwise, The children are examined. If a left and right child are
     * "identical," they are merged and the result placed in the children
     * of the result. Otherwise the left children are placed after the
     * right children in the result.
     * <p/>
     * Nodes are considered equal if their names and indexes are equal.
     */

   /* struct PropPredicate
    : public unary_function<const SGPropertyNode*, bool>
    {
        PropPredicate(const SGPropertyNode* node_) : node(node_) {}
        bool operator()(const SGPropertyNode* arg) const
        {
            if (strcmp(node.getName(), arg->getName()))
                return false;
            return node->getIndex() == arg->getIndex();
        }
        const SGPropertyNode* node;
    };*/

    /**
     * Nachbau fuer obige Logik
     * @param leftChildren
     * @param node
     * @return
     */
    private static SGPropertyNode findIdentical(RawPropVector leftChildren, SGPropertyNode node) {
        for (SGPropertyNode n : leftChildren){
            if (n.getName().equals(node.getName())&&n.getIndex()==node.getIndex())
                return n;
        }
        return null;
    }
    
    // namespace effect
    // {
    public static void mergePropertyTrees(SGPropertyNode resultNode, SGPropertyNode left, SGPropertyNode right) {
        if (left.nChildren() == 0) {
            PropsIO.copyProperties(left, resultNode);
            return;
        }
        resultNode.setAttributes(right.getAttributes());
        RawPropVector leftChildren = new RawPropVector();
        for (int i = 0; i < left.nChildren(); ++i)
            leftChildren.add(left.getChild(i));
        // Merge identical nodes
        for (int i = 0; i < right.nChildren(); ++i) {
            SGPropertyNode node = right.getChild(i);
            //
            //RawPropVector::iterator litr                    = find_if(leftChildren.begin(), leftChildren.end(),                    PropPredicate(node));
            SGPropertyNode litr = findIdentical(leftChildren,node);
            SGPropertyNode newChild                    = resultNode.getChild(node.getName(), node.getIndex(), true);
            if (litr != null/*leftChildren.end()*/) {
                mergePropertyTrees(newChild, litr, node);
                leftChildren.remove/*erase*/(litr);
            } else {
                PropsIO.copyProperties(node, newChild);
            }
        }
        // Now copy nodes remaining in the left tree
        //for (RawPropVector::iterator itr = leftChildren.begin(),        e = leftChildren.end();        itr != e;        ++itr){
        for (SGPropertyNode itr : leftChildren) {
            SGPropertyNode newChild = resultNode.getChild((itr).getName(), (itr).getIndex(), true);
            PropsIO.copyProperties(itr, newChild);
        }
    }



    static FGEffect makeEffect(String name, boolean realizeTechniques, SGReaderWriterOptions options) {
       /* {
            OpenThreads::ScopedLock < OpenThreads::ReentrantMutex > lockEntity(effectMutex);
            EffectMap::iterator itr = effectMap.find(name);
            if ((itr != effectMap.end()) &&
                    itr.getSecond.valid())
                return itr.getSecond.get();
        }*/
        FGEffect e;
        if ((e = effectMap.get(name)) != null) {
            return e;
        }

        String effectFileName = name;
        effectFileName += ".eff";

        //tsch_log("makeEffect.cxx:makeEffect: effectFileName=%s\n", effectFileName.c_str());

        //28.6.17: Effects kommen aus root bundle
        /*String*/BundleResource absFileName = BundleResource.buildFromFullStringAndBundlename(FlightGearSettings.FGROOTCOREBUNDLE,effectFileName);//SGModelLib.findDataFile(effectFileName, options);
        if (/*StringUtils.empty(*/!absFileName.exists()) {
            logger.error(/*SG_LOG(SG_INPUT, SG_ALERT, */"can't find \"" + effectFileName + "\"");
            return null;
        }
        SGPropertyNode/*_ptr*/ effectProps = new SGPropertyNode();
        try {
            PropsIO.readProperties(absFileName, effectProps/*.ptr()*/, 0, true);
        } catch (SGException e1) {
            logger.error(/*SG_LOG(SG_INPUT, SG_ALERT,*/ "error reading \"" + absFileName + "\": " + e1.getMessage());
            return null;
        }
        /*ref_ptr<*/
        FGEffect result = makeEffect(effectProps/*.ptr()*/, realizeTechniques, options);
        if (result.valid()) {
            /*OpenThreads::ScopedLock < OpenThreads::ReentrantMutex > lockEntity(effectMutex);
            pair<EffectMap::iterator, bool > irslt
                    = effectMap.insert(make_pair(name, result));
            if (!irslt.getSecond) {
                // Another thread beat us to it!. Discard our newly
                // constructed Effect and use the one in the cache.
                result = irslt.getFirst.getSecond;
            }*/
            effectMap.put(name, result);
        }
        return result/*.release()*/;
    }


    /**
     * FG-DIFF zusaetlich SGMaterial als Parameter, um nicht ueber die Propnodes gehen zu müssen. Das ist eh reichlich Woodoo.
     * Geht aber nicht so einfach.
     * 26.1.18: Der baut doch staendig Effects neu. Auch die Parents. Und macht dann den release()??. Alles unklar. Damit erzeugt er bequem 1,4Mio PropertyNodes.
     * Das duerfte auch mit dem mergeNodes zu tun haben.
     * Ich uebergehe mal den Parent. Die Effekte werden ja eh noch nicht so genutzt. Dann ergeben sich nur noch 31000 Nodes.
     * Evtl. muss das ganze umgebaut werden.
     * Wiki.
     * 
     * Returns null if effect cannot be created (already logged).
     * 
     * @param prop
     * @param realizeTechniques
     * @param options
     * @return
     */
    public static FGEffect makeEffect(SGPropertyNode prop, boolean realizeTechniques, SGReaderWriterOptions options/*, SGMaterial mat*/) {
        // Give default names to techniques and passes
        List<SGPropertyNode> techniques = prop.getChildren("technique");
        for (int i = 0; i < (int) techniques.size(); ++i) {
            SGPropertyNode tniqProp = techniques.get(i)/*.ptr()*/;
            if (!tniqProp.hasChild("name"))
                SGPropertyNode.setValue(tniqProp.getChild("name", 0, true), (String)(""+i)   /*boost::lexical_cast < string > (i)*/);
            List<SGPropertyNode> passes = tniqProp.getChildren("pass");
            for (int j = 0; j < (int) passes.size(); ++j) {
                SGPropertyNode passProp = passes.get(j)/*.ptr()*/;
                if (!passProp.hasChild("name"))
                    SGPropertyNode.setValue(passProp.getChild("name", 0, true), (String)(""+j)/* boost::lexical_cast < string > (j)*/);
                List<SGPropertyNode> texUnits
                        = passProp.getChildren("texture-unit");
                for (int k = 0; k < (int) texUnits.size(); ++k) {
                    SGPropertyNode texUnitProp = texUnits.get(k)/*.ptr()*/;
                    if (!texUnitProp.hasChild("name"))
                        SGPropertyNode.setValue(texUnitProp.getChild("name", 0, true), (String)(""+k)/* boost::lexical_cast < string > (k)*/);
                }
            }
        }
        /*ref_ptr<*/
        FGEffect effect = null;
        if (!prop.hasChild("name")) {
            SGPropertyNode.setValue(prop.getChild("name", 0, true), "noname");
        }
        SGPropertyNode nameProp = prop.getChild("name");
        // Merge with the parent effect, if any
        SGPropertyNode inheritProp = prop.getChild("inherits-from");
        FGEffect parent = null;
        //siehe Header
        if (inheritProp != null && false) {
            //auch in FG auskommentiert prop.removeChild("inherits-from");
            parent = makeEffect(inheritProp.getStringValue(), false, options);
            if (parent != null) {
                /*TODO? Effect::Key key;
                key.unmerged = prop;
                if (options) {
                    key.paths = options.getDatabasePathList();
                }
                Effect::Cache * cache = 0;
                Effect::Cache::iterator itr;
                {
                    OpenThreads::ScopedLock < OpenThreads::ReentrantMutex >
                            lockEntity(effectMutex);
                    cache = parent.getCache();
                    itr = cache.find(key);
                    if ((itr != cache.end()) &&
                            itr.getSecond.lockEntity(effect)) {
                        effect.generator = parent.generator;  // Copy the generators
                    }
                }*/
                // die Bedeutung des if in FG ist unklar. Es gibt doch noch keinen Effect. Oder wo kommt der her?
                if (true/*!effect.valid()*/) {
                    effect = new FGEffect();
                    effect.setName(nameProp.getStringValue());
                    effect.root = new SGPropertyNode();
                    mergePropertyTrees(effect.root, prop, parent.root);
                    effect.parametersProp = effect.root.getChild("parameters");
                    // Generator erstmal weglassen
                    /*OpenThreads::ScopedLock < OpenThreads::ReentrantMutex >
                            lockEntity(effectMutex);
                    pair<Effect::Cache::iterator, bool > irslt
                            = cache.insert(make_pair(key, effect));
                    if (!irslt.getSecond) {
                        ref_ptr<Effect> old;
                        if (irslt.getFirst.getSecond.lockEntity(old))
                            effect = old; // Another thread beat us in creating it! Discard our own...
                        else
                            irslt.getFirst.getSecond = effect; // update existing, but empty observer
                    }
                    effect.generator = parent.generator;  // Copy the generators
                    */
                }
            } else {
                logger.error(/*SG_LOG(SG_INPUT, SG_ALERT,*/ "can't find base effect " + inheritProp.getStringValue());
                return null;
            }
        } else {
            effect = new FGEffect();
            effect.setName(nameProp.getStringValue());
            effect.root = prop;
            effect.parametersProp = effect.root.getChild("parameters");
        }
        SGPropertyNode generateProp = prop.getChild("generate");
        if (generateProp != null) {
            /*TODO?effect.generator.clear();

            // Effect needs some generated properties, like tangent vectors
            SGPropertyNode parameter = generateProp.getChild("normal");
            if (parameter != null) effect.setGenerator(Effect::NORMAL, parameter.getIntValue());

            parameter = generateProp.getChild("tangent");
            if (parameter != null) effect.setGenerator(Effect::TANGENT, parameter.getIntValue());

            parameter = generateProp.getChild("binormal");
            if (parameter != null) effect.setGenerator(Effect::BINORMAL, parameter.getIntValue());
            */
        }
        if (realizeTechniques) {
            try {
                //OpenThreads::ScopedLock < OpenThreads::ReentrantMutex >                        lockEntity(effectMutex);
                effect.realizeTechniques(options);
            } catch (/*Builder*/java.lang.Exception e) {
                logger.error(/*SG_LOG(SG_INPUT, SG_ALERT,*/ "Error building technique: " + e.getMessage());
                return null;
            }
        }
        return effect/*.release()*/;
    }

   /* void clearEffectCache()
    {
        OpenThreads::ScopedLock<OpenThreads::ReentrantMutex> lockEntity(effectMutex);
        effectMap.clear();
    }*/


}

class RawPropVector extends ArrayList<SGPropertyNode> {

}