package de.yard.threed.flightgear.core.osgdb;

import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.Util;

/**
 * Created by thomass on 09.06.16.
 */
public class Registry {
    //MA17static ReadFileCallback callback;
    static Registry instance = null;
    //MA17 List<ReaderWriter> rwlist = new ArrayList<ReaderWriter>();
    //voellig unkjlar, was es damit auf sich hat TODO
    private Options options = new Options();    

    private Registry() {

    }

    public static Registry getInstance() {
        if (instance == null) {
            instance = new Registry();
        }
        return instance;
    }

    /*MA17public static void setReadFileCallback(ReadFileCallback pcallback) {
        callback = pcallback;
    }*/

    public static ReadResult readNode(/*Bundle bundle,*/ BundleResource bpath, String filename, /*22.8.16 SGReaderWriter*/Options options) {
        Util.notyet();
        return null;//callback.readNode(   bpath,filename, options);
    }
  
    /*MA17public void addReaderWriter(ReaderWriter rw) {
        rwlist.add(rw);
    }*/

    /*MA17public  ReaderWriter getReaderWriterForExtension(String ext){
        for (ReaderWriter rw : rwlist){
            if (rw.acceptsExtension(ext))
                return rw;
        }
        return null;
    }*/

    public void clearObjectCache() {
        //TODO
    }

    public Options getOptions() {
        return options;
    }

    /*MA17 public void replaceReaderWriter(String ext, ReaderWriter dummyReaderBTG) {
        for (int i=0;i<rwlist.size();i++){
            if (rwlist.get(i).acceptsExtension(ext)) {
                rwlist.set(i,dummyReaderBTG);
            }
        }
    }*/
}

