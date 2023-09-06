package de.yard.threed.flightgear.core.simgear.scene.material;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.loader.PortableMaterial;
import de.yard.threed.flightgear.core.PropertyList;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.scene.util.SGReaderWriterOptions;


import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Config;
import de.yard.threed.engine.platform.common.EffectShader;

/**
 * Eine abstrakte Abbildung von sowohl eines FG effect (*.eff Datei) und einer JME
 * Materialdefinition (*.j3md).
 * <p/>
 * 2.2.16: So ganz rund ist das nicht, denn Effects koennen sowohl Materialen wie auch Partikel betreffen. Da kann man dann
 * ja nicht einfach irgendwelche Effekte reinstecken.
 * <p/>
 * 20.7.2016: Der Sinn der Klasse ist mittlerweile unklar. Was ist überhaupt ein Effekt? Transparenz z.B. ist doch eine Materialeigenschaft.
 * Und warum sollte man fuer einen Effekt einen eigenen Shader haben, wenn es doch schon fertige gibt; vor allem in Unity.
 * Darum zunächst man transparency in Material verschoben. 
 * 21.7.16: Naja, iregdnwie ist Transparenz aber ja doch auch ein Effekt. Oder er ist ein Efekt, der mit der
 * Materialeigenschaft transparency umgesetzt wird. Das muss sich noch entwickeln.
 * <p/>
 * 10.8.16: Entwickelt sich jetzt zu einem FG Effect. Statt aber hier zu Shadern zu verbinden, wird hier die Verbindung zu einem Platform Material hergestellt. Es
 * gibt die Abbildung: Genau ein Effect entspricht genau einem Platform Material.
 * Wiki.
 * 
 * Vorläufig:Effect=Custom shader aber != materialeigenschaft
 * 27.12.17: Durch preprocess/GLTF ist der Effect jetzt eine Abstraktionebene höher und hat LoadedMaterial statt Material.
 * 16.01.18: Vielleicht kann hier doch einmalig das Material fertig erstellt werden (buidlTechnique()?), dann kann man
 * die PropertyNodes wieder freigegeben (wegen memory). Ist auch im Sinne von Material sharing. Aber ob das wirklich die Intention ist?
 *
 * 9.3.21: MA31 Was once joined with Effect in engine.
 *
 * Created by thomass on 30.10.15.
 */
public class FGEffect /*extends Effect*/ {
    Log logger = Platform.getInstance().getLog(FGEffect.class);
    // 27.12.17: Jetzt eine Abnstraktionsstufe hoeher wegen preProcess 
    PortableMaterial materialdefinition = null;
    //Material material = null;
    public String name;
    public SGPropertyNode root, parametersProp;

    //20.7.16 public boolean transparent = false;
    //11.3.16: Der Shader ist optional. Wenn er nicht angegeben ist, muss die Platform sehen, wie sie den Effekt hinbekommt.
    // Wenn ein Shader angegebn ist, wird er in allen Platformen verwendet.
    public EffectShader shader = null;

    public FGEffect() {

    }

    /**
     * Erstmal zum Einstieg
     */
    private FGEffect(String name) {
        this.name = name;
        shader = new EffectShader();

    }


    public boolean valid() {
        return true;//TODO Util.notyet();
    }

    /**
     * 
     * Walk the techniques property tree, building techniques and passes.
     * <p/>
     * FG-DIFF Implementierung
     * Hier wird mal das Material gebaut. SGMaterial.buildEffectProperties() hat die Materialwerte vorher in die PropertyNode geschrieben.
     * Das ist aber auch reichlich Woodoo. Nicht erkennbar, welche Textur wofuer genutzt wird.
     * Um nicht über die PropNode gehen zu muessen, hier direkt das SGMaterial reingeben. Geht aber nicht so einfgach.
     * 5.10.17: Wird
     * @param options
     */
    public boolean realizeTechniques(SGReaderWriterOptions options/*, SGMaterial mat*/) {
        //material
        if (Config.materiallibdebuglog) {
            logger.debug("Effec:realizeTechniques.");
        }
        /*if (_isRealized)
            return true;
        PropertyList tniqList = root->getChildren("technique");
        for (PropertyList::iterator itr = tniqList.begin(), e = tniqList.end();
        itr != e;
        ++itr)
        buildTechnique(this, *itr, options);
        _isRealized = true;*/

        // der genaue Aufbau ist unklar, auch warum es auf einmal mehrere Texturen gibt. Evtl. normal maps und co
        //logger.debug("effect.root=" + root.dump("\n"));
        PropertyList parameters = root.getChildren("parameters");
        PropertyList texturel = parameters.get(0).getChildren("texture");
        int uniqueid = parameters.get(0).getIntValue("uniqueid",-55);
        int index = 0;
        SGPropertyNode tex = texturel.get(index);
        String image = tex.getStringValue("image");
        //TODO wrap aus tree, bundlename
        //image enthaelt den kompletten absoluten Path. 12.6.17: jetzt nur noch den Pafd relativ im Bundle
        logger.debug("realizeTechniques with texture " + image);
       // if (StringUtils.startsWith(image,F))
        //27.12.17: Nicht mehr Textur laden und Material anlegen, sondern ein LoadedMaterial anlegen.
        //Texture texture = Texture.buildBundleTexture(SGMaterialLib.BUNDLENAME,image, true, true);
       
        /*if (StringUtils.endsWith(image, "drycrop4.png") ||
                StringUtils.endsWith(image, "naturalcrop1.png")) {
            material = Material.buildBasicMaterial(Color.YELLOW);
        } else {*/
            materialdefinition = new PortableMaterial();//Material.buildLambertMaterial(texture);
        //material.setName("SGMaterial id= "+uniqueid);
        materialdefinition.texture=image;
        materialdefinition.wraps=true;
        materialdefinition.wrapt=true;
        //}

        return true;
    }


    /*public Material getMaterialD() {

        return material;
    }*/

    public PortableMaterial getMaterialDefinition() {

        return materialdefinition;
    }
    public void setName(String name) {
        this.name = name;
    }
}

