package de.yard.threed.flightgear.core.simgear.misc;

/**
 * aus sg_dir.cxx
 * Die Klasse i FG heisst Dir?
 * 
 * aber alles zu sehr Platform.
 * 
 * Created by thomass on 31.05.16.
 */
public class SGDir {
    //String dir;
    SGPath path;
    
    public SGDir(/*22.8.16 String dir*/SGPath path) {
    //this.dir = dir;
        this.path=path;
        
    }

    public SGPath file(String filename) {
        return new SGPath(path.realpath()+"/"+filename);
    }
}
