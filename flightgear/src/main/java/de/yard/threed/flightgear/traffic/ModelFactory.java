package de.yard.threed.flightgear.traffic;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.flightgear.ecs.FgAnimationComponent;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.flightgear.core.SGLoaderOptions;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.scene.model.SGAnimation;
import de.yard.threed.flightgear.core.simgear.scene.model.SGReaderWriterXML;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.BuildResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 9.3.21: FG (XML) part separated.
 *
 * Created by thomass on 02.06.16.
 */
public class ModelFactory {
    static Platform pf = Platform.getInstance();
    static Log logger = Platform.getInstance().getLog(ModelFactory.class);

    /**
     * 10.4.17:Variante ueber Bundle. Cache braucht es nicht mehr, weil es preprocessed models gibt.
     * Verscuhen, auf objindex zu verzichten.
     * Liefert ReadResult, um auch Animationen zu liefern zu koennen.
     * Die rootnode wird auch für die Animationen gebraucht.
     * 25.4.17: Die BundleResource sollte/muss auch das Bundle enthalten. Hier das passende Bundle zu resolven dürfte schwierig sein, vor allem bei sowa wie TerrainTiles.
     * Die Loader/reader/Processor entkoppelt, damit hier kein SGPropertyNode und opttexturepath rein muss.
     * 12.6.17: Nicht mehr fuer STG Files. Die sind doch einfach zu speziell. Aber immer noch für btg.
     * Hier werden auch XML model gelesen, dafuer sind die "options". Aber eigenlich ist xml doch genauso speziell wie stg und sollte auch eine eigene MEthode haben.
     * 15.9.17: TODO: Das geh ich jetzt mal an, wegen async gltf. Erstmal Methode nur dupliziert. Dann die eine in den XMlReader analog STG und die zweite ueber die Platform. Darum depreccated.
     * die erste ist jetzt in SGReaderWriterXML.
     * @return
     */
    
    /*21.12.17 kann jetzt weg @Deprecated 
    public static BuildResult buildModelFromBundle(BundleResource modelfile) {
        return ModelLoader.buildModelFromBundle(modelfile,null);
    }*/

    public static SceneNode buildModelFromBundleXml(BundleResource modelfile, SGPropertyNode rootnode, List<SGAnimation> animationList) {
        SGLoaderOptions opt = new SGLoaderOptions();
        opt.setPropertyNode(rootnode);

        BuildResult buildresult = SGReaderWriterXML.buildModelFromBundleXML(modelfile, opt, (bpath, destinationNode, alist) -> {
            if (alist != null) {
                animationList.addAll(alist);//  xmlloaddelegate.modelComplete( animationList);
            }
        });
        SceneNode destinationNode = new SceneNode(buildresult.getNode());
        return destinationNode;
    }

    /**
     * XML Lesen muss eh ueber die Platform gehen. Darum dafuer kein Event schicken. Wohl aber, wenn es gelesen wurde.
     * Wegen moeglicher includes synced und direkt als PropertyList lesen.
     * 29.12.16: Wegen Verschachtelung der XML Properties/Model das Model komplett lesen.
     * Unausgegoren ist die Frage, ob die Node jetzt schon in der Scene ist (Unity like) oder nicht. siehe DVK.
     * Model Load ueber ECS ist eh doof. siehe DVK. Darum jetzt mit CallBack üeber Eventbus.
     * Das mit der proertyNode ist natuerlich auchne Kruecke.
     * 25.4.17: Ob dies mit Bundle noch Bestand hat (und ueberhaupt) ist doch fraglich. Mal deprecated, bis der Zweck klar ist. Das mit dem Event ist ja schon schick.
     * 4.10.19: Methode recycled, um ein XML Model als Entity (async) zu laden. Das Bundle muss aber schon da sein.
     * Ob ich das mit dem Event recycle? Hmmm, mal sehn.
     */
    public static EcsEntity buildModelFromBundleXmlAsEntity(BundleResource modelfile/*FileSystemResource resource, EventNotify callback*/, SGPropertyNode rootnode) {
        List<SGAnimation> animationList = new ArrayList<SGAnimation>();

        SceneNode destinationNode = buildModelFromBundleXml(modelfile,rootnode,animationList);
        // 14.5.25: Assume it is no vehicle so no local property tree
        EcsEntity entity = new EcsEntity(destinationNode, new FgAnimationComponent(destinationNode, animationList, null));
        entity.setName(""/*config.getName()*/);
        //entity.setBasenode(basenode);

        /*ReadResult result = null;//15.9.17 TODO async new SGReaderWriterXML().readNode(null, resource.getFullName(), options);
        //SceneNode node = result.getNode();
        //if (node != null) {
        Event evt = EcsSystemEventFactory.buildModelLoadedEvent(result);
        SystemManager.sendEvent(evt);
        //TODO gehoert zum Dispatcher
        if (callback != null) {
            callback.eventPublished(evt);
        }*/
        //}
        return entity;
    }




}
