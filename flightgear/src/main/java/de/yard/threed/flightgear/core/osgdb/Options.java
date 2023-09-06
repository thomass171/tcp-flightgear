package de.yard.threed.flightgear.core.osgdb;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.flightgear.core.simgear.scene.tgdb.DelayLoadReadFileCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by thomass on 07.12.15.
 */
public class Options {
    protected FilePathList databasePathList = new FilePathList();
    private DelayLoadReadFileCallback readFileCallback;
    //TODO databasepath(31.3.17: z.B. fuer texturepath bei AC Modellen)
    public String databasePath;
    //8.6.17: FG-DIFF. das geht das anders, aber wohl mit gleichem Zweck.
    public Map<String, String> pluginstringdata = new HashMap<String, String>();

    public FilePathList getDatabasePathList() {
        return databasePathList;
    }

    public void setDatabasePathList(FilePathList databasePathList) {
        this.databasePathList = databasePathList;
    }

    public void setReadFileCallback(DelayLoadReadFileCallback readFileCallback) {
        this.readFileCallback = readFileCallback;
    }

    public DelayLoadReadFileCallback getReadFileCallback() {
        return readFileCallback;
    }

    public void setDatabasePath(String databasePath) {
        this.databasePath = databasePath;
    }

    /**
     * FG-DIFF: Wird normal vom Tilemgr gesetzt. Zumindest zwei nehme ich aus dem Env.
     * 8.6.17: jetzt mal echt mit map.
     *
     * @param s
     * @return
     */
    public String getPluginStringData(String s) {
        if (s.equals("SimGear::TERRASYNC_ROOT")) {
            return Platform.getInstance().getConfiguration().getString("FG_SCENERY");
        }
        if (s.equals("SimGear::FG_ROOT")) {
            return Platform.getInstance().getConfiguration().getString("FG_ROOT");
        }
        //TODO
        if (s.equals("SimGear::PREVIEW")) {
            return "";
        }
        String value = pluginstringdata.get(s);
        if (value == null) {
            //NPE vermeiden
            return "";
        }
        return value;
    }
}
