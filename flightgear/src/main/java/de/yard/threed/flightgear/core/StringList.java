package de.yard.threed.flightgear.core;

import de.yard.threed.core.StringUtils;

/**
 * Nachbildung des typedef string_list.
 * 
 * Created by thomass on 30.05.16.
 */
public class StringList extends CppVector<String> {
    public static StringList sgPathBranchSplit(String path) {
        StringList sl = new StringList();
        // TODO: Platformunabhaebgig 
        String[] parts = StringUtils.splitByWholeSeparator(path, "/");
        for (String s : parts){
            sl.add(s);
        }
        return sl;
    }

    public static StringList sgPathSplit(String searchpath) {
        StringList sl = new StringList();
        // TODO: Platform independent
        String[] parts = StringUtils.splitByWholeSeparator(searchpath, ":");
        for (String s : parts){
            sl.add(s);
        }
        return sl;
    }
    
    /*public void insert(int index, String s){
       gibts nicht in C# super.add(index,s);
    }*/

    public void push_back(String s) {
        add(s);
    }

    public String join() {
        String r = "";
        for (String s : this){
            r += s;
        }
        return r;
    }
}
