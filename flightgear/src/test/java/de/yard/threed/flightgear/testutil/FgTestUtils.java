package de.yard.threed.flightgear.testutil;


public class FgTestUtils {
    public static String locatedTestFile(String relFilenameToProjectHome) {
        return System.getProperty("user.dir") + "/../" + relFilenameToProjectHome;
    }
}
