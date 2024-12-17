package de.yard.threed.flightgear.core.simgear.scene.util;

import de.yard.threed.flightgear.core.osg.Node;
import de.yard.threed.flightgear.core.osgdb.Options;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;

/**
 * From SGTransientModelData.hxx
 * <p>
 * Transient data that is used with a given context for any given model.
 * Currently used during the model load to provide a consistent set of data when creating the animations.
 * - provides a map to allow reuse of axis definitions
 * Expected possible future expansion for improvements to model light animations.
 * - typical usage for the SGAnimation::animate() call would be to create one of these like this
 * simgear::SGTransientModelData modelData(group.get(), prop_root, options.get(), path.local8BitStr());
 * This will create the basic transient model data that doesn't change between invocations of animate()
 * then to adapt the model data for an animation element use the LoadAnimationValuesForElement method, e.g.
 * modelData.LoadAnimationValuesForElement(animation_nodes[i], i);
 *
 * FG-DIFF 3.12.24 Switch to this wrapper class as parameter in animation building even though not sure about benefits.
 */
public class SGTransientModelData {
    // typedef std::map<std::string, SGLineSegment<double>> SGAxisDefinitionMap;
    // FG-DIFF more intuitive name instead of just 'node'
    Node xmlNodeOfCurrentModel/*node*/ = null;
    SGPropertyNode configNode = null;
    SGPropertyNode modelRoot = null;
    Options options = null;
    String path;
    int index = 0;
    //SGAxisDefinitionMap axisDefinitions;

    /**
     * fully specified constructor. Probably not relevant as creating an object this specified implies setting up for an individual animation element, which will preclude
     * // the use of the axis definition cache between objects.
     */
    public SGTransientModelData(Node _node, SGPropertyNode _configNode, SGPropertyNode _modelRoot, Options _options, String _path, int _i) {

        this.xmlNodeOfCurrentModel = _node;
        this.configNode = _configNode;
        this.modelRoot = _modelRoot;
        this.options = _options;
        this.path = _path;
        this.index = _i;
    }

    /**
     * usual form of construction - setup the elements that are constant, and specify the config node and index via LoadAnimationValuesForElement
     */

    public SGTransientModelData(Node _node, SGPropertyNode _modelRoot, Options _options, String _path){
        /*
    node(_node),configNode(nullptr),
    modelRoot(_modelRoot),options(_options),
    path(_path),index(0)*/
        this.xmlNodeOfCurrentModel = _node;
        this.configNode = null;
        this.modelRoot = _modelRoot;
        this.options = _options;
        this.path = _path;
        this.index = 0;
    }

    /*
     * Loads the animation values required for a specific element.
     */
    public void LoadAnimationValuesForElement( SGPropertyNode _configNode, int _index) {
        configNode = _configNode;
        index = _index;
    }

    /**
     * FG-DIFF more intuitive name instead of just 'getNode()'
     * @return
     */
    public Node getXmlNode() {
        return xmlNodeOfCurrentModel/*node*/;
    }

    public SGPropertyNode getConfigNode() {
        return configNode;
    }

    public SGPropertyNode getModelRoot() {
        return modelRoot;
    }

    Options getOptions() {
        return options;
    }

    String getPath() {
        return path;
    }

    int getIndex() {
        return index;
    }

    /*
     * Find an already located axis definition object line segment. Returns null if nothing found.
     */
   /* SGLineSegment/*<double> * /  getAxisDefinition(String axis_object_name) {
        SGAxisDefinitionMap::const_iterator axisDefinition = axisDefinitions.find(axis_object_name);

        if (axisDefinition != axisDefinitions.end()) {
            return &(axisDefinition -> second);
        }
        return 0;
    }*/
    /*
     * Add an axis definition line segment. Always returns the line segment that has been added.
     */
       /*  SGLineSegment/*<double> * /    addAxisDefinition(String object_name, SGLineSegment/*<double>* / line) {
        axisDefinitions[object_name] = line;
        return getAxisDefinition(object_name);
    }*/


}
