package de.yard.threed.flightgear.core.simgear.scene.util;

import de.yard.threed.flightgear.core.osgdb.FilePathList;
import de.yard.threed.flightgear.core.osgdb.Options;
import de.yard.threed.flightgear.core.osgdb.Registry;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterialLib;
import de.yard.threed.flightgear.core.simgear.scene.model.SGModelData;

/**
 * Created by thomass on 10.12.15.
 */
public class SGReaderWriterOptions extends Options {
    /*TODO
       /* SGSharedPtr<* /SGPropertyNode _propertyNode;
        /*SGSharedPtr<*/ SGMaterialLib _materialLib;
    /*osg::* /Node*(*_load_panel)(SGPropertyNode *);
    /*osg::ref_ptr<* /SGModelData _model_data;*/
    boolean _instantiateEffects;
    private SGPropertyNode propertyNode;
    private SGModelData modelData;

    public SGReaderWriterOptions(SGReaderWriterOptions options) {
        this.propertyNode = options.getPropertyNode();
        this._materialLib = options.getMaterialLib();
        this.modelData = options.getModelData();
        this._instantiateEffects = options.getInstantiateEffects();
        this.databasePathList = new FilePathList();
        for (String s: options.getDatabasePathList()){
            this.databasePathList.add(s);
        }
    }

    public SGReaderWriterOptions(Options options) {
        this.databasePathList = new FilePathList();
        for (String s: options.getDatabasePathList()){
            this.databasePathList.add(s);
        }
    }

    public SGReaderWriterOptions() {

    }

    public static SGReaderWriterOptions copyOrCreate(Options options) {
        if (options == null)
            options = Registry.getInstance().getOptions();
        if (options == null)
            return new SGReaderWriterOptions();
        //if (!dynamic_cast<const SGReaderWriterOptions*>(options))
        if (!(options instanceof SGReaderWriterOptions))
            return new SGReaderWriterOptions(options);
        return new SGReaderWriterOptions(/**static_cast<const */(SGReaderWriterOptions) options);
    }
/*
    const SGSharedPtr<SGPropertyNode>& getPropertyNode() const
    { return _propertyNode; }

    void setPropertyNode(const SGSharedPtr<SGPropertyNode>& propertyNode)
    { _propertyNode = propertyNode; }*/

    public SGMaterialLib getMaterialLib() {
        return _materialLib;
    }

    public void setMaterialLib(SGMaterialLib materialLib) {
        _materialLib = materialLib;
    }

    /*
    typedef osg::Node *(*panel_func)(SGPropertyNode *);

    panel_func getLoadPanel() const
    { return _load_panel; }
    void setLoadPanel(panel_func pf)
    { _load_panel=pf; }

    SGModelData *getModelData() const
    { return _model_data.get(); }
    void setModelData(SGModelData *modelData)
    { _model_data=modelData; }*/

    public boolean getInstantiateEffects() {
        return _instantiateEffects;
    }

    public void setInstantiateEffects(boolean instantiateEffects) {
        _instantiateEffects = instantiateEffects;
    }

    public void setPropertyNode(SGPropertyNode propertyNode) {
        this.propertyNode = propertyNode;
    }

    public void setPluginStringData(String s, String fg_root) {
        //TODO
    }

    public SGPropertyNode getPropertyNode() {
        return propertyNode;
    }

    public SGModelData getModelData() {
        return modelData;
    }

    public void setModelData(SGModelData modelData) {
        this.modelData = modelData;
    }

/*    static SGReaderWriterOptions* fromPath(const std::string& path);

    protected:
    virtual ~SGReaderWriterOptions();
*/

}
