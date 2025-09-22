package de.yard.threed.flightgear;


import de.yard.threed.core.CharsetException;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.core.loader.BinaryLoader;
import de.yard.threed.core.loader.GeoMat;
import de.yard.threed.core.loader.InvalidDataException;
import de.yard.threed.core.loader.LoadedObject;
import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.loader.PortableModelDefinition;
import de.yard.threed.core.loader.PortableModel;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.flightgear.core.group_tci_list;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterialCache;
import de.yard.threed.flightgear.core.simgear.scene.material.SGMaterialLib;

import de.yard.threed.flightgear.core.simgear.scene.tgdb.SGTileGeometryBin;
import de.yard.threed.flightgear.core.simgear.scene.util.SGReaderWriterOptions;
import de.yard.threed.flightgear.core.tci_list;

import de.yard.threed.core.Color;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;


import de.yard.threed.core.platform.Log;
import de.yard.threed.core.buffer.ByteArrayInputStream;
import de.yard.threed.core.platform.Config;
import de.yard.threed.core.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thschonh on 25.01.2016.
 */
public class LoaderBTG extends BinaryLoader {
    Log logger = Platform.getInstance().getLog(LoaderBTG.class);

    final int SG_BOUNDING_SPHERE = 0;

    final int SG_VERTEX_LIST = 1;
    final int SG_NORMAL_LIST = 2;
    final int SG_TEXCOORD_LIST = 3;
    final int SG_COLOR_LIST = 4;
    final int SG_VA_FLOAT_LIST = 5;
    final int SG_VA_INTEGER_LIST = 6;

    final int SG_POINTS = 9;

    final int SG_TRIANGLE_FACES = 10;
    final int SG_TRIANGLE_STRIPS = 11;
    final int SG_TRIANGLE_FANS = 12;

    final int SG_IDX_VERTICES = 0x01;
    final int SG_IDX_NORMALS = 0x02;
    final int SG_IDX_COLORS = 0x04;
    final int SG_IDX_TEXCOORDS_0 = 0x08;
    final int SG_IDX_TEXCOORDS_1 = 0x10;
    final int SG_IDX_TEXCOORDS_2 = 0x20;
    final int SG_IDX_TEXCOORDS_3 = 0x40;

    // vertex attributes
    final int SG_VA_INTEGER_0 = 0x00000001;
    final int SG_VA_INTEGER_1 = 0x00000002;
    final int SG_VA_INTEGER_2 = 0x00000004;
    final int SG_VA_INTEGER_3 = 0x00000008;

    final int SG_VA_FLOAT_0 = 0x00000100;
    final int SG_VA_FLOAT_1 = 0x00000200;
    final int SG_VA_FLOAT_2 = 0x00000400;
    final int SG_VA_FLOAT_3 = 0x00000800;

    final int SG_MATERIAL = 0;
    final int SG_INDEX_TYPES = 1;
    final int SG_VERT_ATTRIBS = 2;

    public static int sizeof_float = 4;
    int sizeof_unsignedint = 4;
    public static int sizeof_double = 8;
    byte[] buf4 = new byte[4];
    byte[] buf2 = new byte[2];

/*#ifdef HAVE_CONFIG_H
        #  include <simgear_config.h>
        #endif

        #include <simgear/compiler.h>
        #include <simgear/debug/logstream.hxx>

        #include <stdio.h>
        #include <time.h>
        #include <cstring>
#include <cstdlib> // for system()
#include <cassert>

#include <vector>
#include <string>
#include <iostream>
#include <bitset>

#include <simgear/bucket/newbucket.hxx>
        #include <simgear/misc/sg_path.hxx>
        #include <simgear/math/SGGeometry.hxx>
        #include <simgear/structure/exception.hxx>

        #include "lowlevel.hxx"
        #include "sg_binobj.hxx"


        using std::string;
        using std::vector;
        using std::cout;
        using std::endl;

*/

    /*unsigned*/ int /*short*/ version;

    float gbs_radius;

    //List<SGVec3d> wgs84_nodes;   // vertex list
    List</*SGVec4f*/Color> colors;        // color list
    // List<SGVec3f> normals;       // normal list
    public List<Vector2> texcoords;     // texture coordinate list
    List<Float> va_flt;        // vertex attribute list (floats)
    List<Integer> va_int;        // vertex attribute list (ints)

    List<List<Integer>> /*group_list*/ pts_v;                // points vertex index
    List<List<Integer>> /*group_list*/ pts_n;                // points normal index
    List<List<Integer>>  /*group_list*/ pts_c;                // points color index
    group_tci_list pts_tcs;             // points texture coordinates ( up to 4 sets )
    List<List<List<Integer>>>  /*group_vai_list*/ pts_vas;             // points vertex attributes ( up to 8 sets )
    List<String> /*string_list*/ pt_materials;           // points materials

    public List<List<Integer>> /*group_list*/  tris_v;                // triangles vertex index
    public List<List<Integer>> /*group_list*/  tris_n;                // triangles normal index
    List<List<Integer>> /*group_list*/  tris_c;                // triangles color index
    public group_tci_list tris_tcs;            // triangles texture coordinates ( up to 4 sets )
    List<List<List<Integer>>>  /*group_vai_list*/ tris_vas;            // triangles vertex attributes ( up to 8 sets )
    public List<String> /*string_list*/ tri_materials;          // triangles materials

    List<List<Integer>> /*group_list*/  strips_v;                // tristrips vertex index
    List<List<Integer>> /*group_list*/  strips_n;                // tristrips normal index
    List<List<Integer>> /*group_list*/  strips_c;                // tristrips color index
    group_tci_list strips_tcs;          // tristrips texture coordinates ( up to 4 sets )
    List<List<List<Integer>>>  /*group_vai_list*/ strips_vas;          // tristrips vertex attributes ( up to 8 sets )
    List<String> /*string_list*/ strip_materials;        // tristrips materials

    List<List<Integer>> /*group_list*/  fans_v;                // fans vertex index
    List<List<Integer>> /*group_list*/  fans_n;                // fans normal index
    List<List<Integer>> /*group_list*/  fans_c;                // fans color index
    group_tci_list fans_tcs;            // fanss texture coordinates ( up to 4 sets )
    List<List<List<Integer>>>  /*group_vai_list*/ fans_vas;            // fans vertex attributes ( up to 8 sets )
    List<String> /*string_list*/ fan_materials;            // fans materials
    private boolean headerchecked = false;
    ByteArrayInputStream buf;
    // InputStream fp;
    SGReaderWriterOptions options;
    LoaderOptions boptions;
    public Vector3 center;
    String source;

    public SGTileGeometryBin tileGeometryBin;

    public static String BTG_ROOT = "btgroot";

    // 12.11.24: This option might help to avoid BTG converted GLTFs that contain no material and thus reveal
    // configuration problems with SGMaterialLib during conversion.
    public boolean shouldFailOnError = false;
    /**
     * Anscheinend ist das ganze ein einziges Object.
     */
    public LoaderBTG(ByteArrayInputStream buf, SGReaderWriterOptions options, LoaderOptions boptions, String source) throws InvalidDataException {
        this.buf = buf;
        this.options = options;
        this.boptions = boptions;
        this.source = source;
        // 25.9.24: Needs new class extension
        loadedfile = new LoadedBtgFile();
        load();
    }

    /**
     * Weil die Datenstrukturen doch etwas unübersichtlich sind (vor allem tris_tcs), erstmal wie FG in deren Listen einlesen und
     * mit migriertem Algorithmus in Faces ueberführen.
     *
     * @throws InvalidDataException
     */
    @Override
    protected void doload() throws InvalidDataException {
        /*17.6.21 SGVec3d p;*/
        int i, k;
        /*size_t*/
        int j;
        /* unsigned int*/
        int nbytes;
        //sgSimpleBuffer buf;// (32768);  // 32 Kb

        BTGObject currentobject = new BTGObject();
        //25.9.24 loadedfile.objects.add(currentobject);
        loadedfile.object = currentobject;
        // zero out structures
        ((LoadedBtgFile) loadedfile).gbs_center = null;// new SGVec3d(0, 0, 0);
        gbs_radius = (float) 0.0;

        //wgs84_nodes = new ArrayList();
        //normals = new ArrayList();
        //texcoords = new ArrayList<Vector2>();

        // points
        pts_v = new ArrayList<List<Integer>>();
        pts_n = new ArrayList<List<Integer>>();
        pts_c = new ArrayList<List<Integer>>();
        pts_tcs = new group_tci_list();
        pts_vas = new ArrayList<List<List<Integer>>>();
        pt_materials = new ArrayList<String>();

        // triangles
        tris_v = new ArrayList<List<Integer>>();
        tris_n = new ArrayList<List<Integer>>();
        tris_c = new ArrayList<List<Integer>>();
        tris_tcs = new group_tci_list();
        tris_vas = new ArrayList<List<List<Integer>>>();
        tri_materials = new ArrayList<String>();

        strips_v = new ArrayList<List<Integer>>();
        strips_n = new ArrayList<List<Integer>>();
        strips_c = new ArrayList<List<Integer>>();
        strips_tcs = new group_tci_list();
        strips_vas = new ArrayList<List<List<Integer>>>();
        strip_materials = new ArrayList<String>();

        fans_v = new ArrayList<List<Integer>>();
        fans_n = new ArrayList<List<Integer>>();
        fans_c = new ArrayList<List<Integer>>();
        fans_tcs = new group_tci_list();
        fans_vas = new ArrayList<List<List<Integer>>>();
        fan_materials = new ArrayList<String>();


        //TODO sgClearReadError();

        // read headers
        /*unsigned*/
        int header;
        header = buf.readUInt();//sgReadUInt(fp);
        if (((header & 0xFF000000) >> 24) == 'S' &&
                ((header & 0x00FF0000) >> 16) == 'G') {

            // read file version
            version = (header & 0x0000FFFF);
        } else {
            // close the file before we return
            //TODO  gzclose(fp);
            throw new RuntimeException("Bad BTG magic/version"/*, sg_location(file)*/);
        }

        // read creation time
        /* unsigned*/
        int foo_calendar_time;
        foo_calendar_time = buf.readUInt();//sgReadUInt(fp);
/*
        #if 0
        time_t calendar_time = foo_calendar_time;
        // The following code has a global effect on the host application
        // and can screws up the time elsewhere.  It should be avoided
        // unless you need this for debugging in which case you should
        // disable it again once the debugging task isType finished.
        struct tm*local_tm;
        local_tm = localtime( & calendar_time);
        char time_str[ 256];
        strftime(time_str, 256, "%a %b %d %H:%M:%S %Z %Y", local_tm);
        SG_LOG(SG_EVENT, SG_DEBUG, "File created on " << time_str);
        #endif
*/
        // read number of top level objects
        int nobjects;
        if (version >= 10) { // version 10 extends everything to be 32-bit
            nobjects = buf.readInt();//sgReadInt(fp);
        } else if (version >= 7) {
            /*uint16_t*/
            int v;
            v = buf.readUShort();//sgReadUShort(fp);
            nobjects = v;
        } else {
            /*int16_t*/
            int v;
            v = buf.readShort();//sgReadShort(fp);
            nobjects = v;
        }

        //SG_LOG(SG_IO, SG_DEBUG, "SGBinObject::read_bin Total objects to read = " << nobjects);

       /*TODO if (sgReadError()) {
            throw sg_io_exception("Error reading BTG file header", sg_location(file));
        }*/

        // read in objects
        for (i = 0; i < nobjects; ++i) {
            // read object header
            int obj_type;
            /*uint32_t*/
            int nproperties, nelements;
            obj_type = buf.readByte();//sgReadChar(fp);
            if (version >= 10) {
                nproperties = buf.readUInt();//sgReadUInt(fp);
                nelements = buf.readUInt();//sgReadUInt(fp);
            } else if (version >= 7) {
                /*uint16_t*/
                int v;
                v = buf.readUShort();//sgReadUShort(fp);
                nproperties = v;
                v = buf.readUShort();//sgReadUShort(fp);
                nelements = v;
            } else {
                /*int16_t*/
                int v;
                v = buf.readShort();//sgReadShort(fp);
                nproperties = v;
                v = buf.readShort();//sgReadShort(fp);
                nelements = v;
            }
            if (Config.loaderdebuglog) {
                logger.debug("reading object type " + obj_type + " with " + nelements + " elements and " + nproperties + " properties");
            }

         /*   SG_LOG(SG_IO, SG_DEBUG, "SGBinObject::read_bin object " << i <<
                    " = " << (int) obj_type << " props = " << nproperties <<
                    " elements = " << nelements);*/

            if (obj_type == SG_BOUNDING_SPHERE) {
                // read bounding sphere properties
                read_properties(buf, nproperties);

                // read bounding sphere elements
                for (j = 0; j < nelements; ++j) {
                    nbytes = buf.readUInt();//sgReadUInt(fp);
                    ByteArrayInputStream buf1 = buf.readSubbuffer(nbytes);//sgReadBytes(fp, nbytes/*, ptr*/);
                    ((LoadedBtgFile) loadedfile).gbs_center = readVec3d(buf1);
                    gbs_radius = buf1.readFloat();
                }
            } else if (obj_type == SG_VERTEX_LIST) {
                // read vertex list properties
                read_properties(buf, nproperties);

                // read vertex list elements
                for (j = 0; j < nelements; ++j) {
                    nbytes = buf.readUInt();//sgReadUInt(fp);
                    // buf.resize(nbytes);
                    // buf.reset();
                    // char*ptr = buf.get_ptr();
                    //??nbytes += 12;
                    ByteArrayInputStream buf1 = buf.readSubbuffer(nbytes);//sgReadBytes(fp, nbytes);
                    int count = nbytes / (sizeof_float * 3);
                    //wgs84_nodes.reserve(count);
                    currentobject.vertices = new ArrayList</*7.2.18 Native*/Vector3>(count);
                    for (k = 0; k < count; k++) {
                        Vector3 v = readVec3f(buf1);
                        // extend from float to double, hmmm
                        //wgs84_nodes
                        currentobject.vertices.add(((v)));
                        //logger.debug(k+":x="+v.x);
                    }
                    if (Config.loaderdebuglog)
                        logger.debug("vertices found: " + count);
                }
            } else if (obj_type == SG_COLOR_LIST) {
                // read color list properties
                read_properties(buf, nproperties);
                // read color list elements
                for (j = 0; j < nelements; ++j) {
                    nbytes = buf.readUInt();//sgReadUInt(fp);
                    ByteArrayInputStream buf1 = buf.readSubbuffer(nbytes);//sgReadBytes(fp, nbytes);
                    int count = nbytes / (sizeof_float * 4);
                    for (k = 0; k < count; ++k) {
                        colors.add(readVec4f(buf1));
                    }
                    if (Config.loaderdebuglog)
                        logger.debug("colors found: " + count);
                }
            } else if (obj_type == SG_NORMAL_LIST) {
                // read normal list properties
                read_properties(buf, nproperties);

                // read normal list elements
                for (j = 0; j < nelements; ++j) {
                    nbytes = buf.readUInt();//sgReadUInt(fp);
                    ByteArrayInputStream buf1 = buf.readSubbuffer(nbytes);//sgReadBytes(fp, nbytes);
                    int count = nbytes / 3;
                    currentobject.normals = new ArrayList<Vector3>(count);
                    // Scheinen im Normalmap Format vorzuliegen.
                    for (k = 0; k < count; ++k) {
                        /*SGVec3f*/
                        Vector3 normal = new Vector3((float) ((float) buf1.readByte() / 127.5f - 1.0),
                                (float) ((float) buf1.readByte() / 127.5f - 1.0),
                                (float) ((float) buf1.readByte() / 127.5f - 1.0));
                        currentobject.normals.add(normal.normalize());
                        //ptr += 3;
                    }
                    if (Config.loaderdebuglog)
                        logger.debug("normals found: " + count);
                }
            } else if (obj_type == SG_TEXCOORD_LIST) {
                // read texcoord list properties
                read_properties(buf, nproperties);

                // read texcoord list elements
                for (j = 0; j < nelements; ++j) {
                    nbytes = buf.readUInt();//sgReadUInt(fp);
                    ByteArrayInputStream buf1 = buf.readSubbuffer(nbytes);//sgReadBytes(fp, nbytes);
                    int count = nbytes / (sizeof_float * 2);
                    texcoords = new ArrayList<Vector2>(count);
                    for (k = 0; k < count; ++k) {
                        Vector2 uv = readVec2f(buf1);
                        texcoords.add(uv);
                    }
                    if (Config.loaderdebuglog)
                        logger.debug("texcoords found: " + count);
                }
            } else if (obj_type == SG_VA_FLOAT_LIST) {
                // read vertex attribute (float) properties
                read_properties(buf, nproperties);

                // read vertex attribute list elements
                for (j = 0; j < nelements; ++j) {
                    nbytes = buf.readUInt();//sgReadUInt(fp);
                    //buf.resize(nbytes);
                    //buf.reset();
                    //char*ptr = buf.get_ptr();
                    ByteArrayInputStream buf1 = buf.readSubbuffer(nbytes);//sgReadBytes(fp, nbytes);
                    /*int*/
                    long count = nbytes / (sizeof_float);
                    // va_flt.reserve(count);
                    for (k = 0; k < count; ++k) {
                        va_flt.add(buf1.readFloat());
                    }
                }
            } else if (obj_type == SG_VA_INTEGER_LIST) {
                // read vertex attribute (integer) properties
                read_properties(buf, nproperties);

                // read vertex attribute list elements
                for (j = 0; j < nelements; ++j) {
                    nbytes = buf.readUInt();//sgReadUInt(fp);
                    //buf.resize(nbytes);
                    //buf.reset();
                    //char*ptr = buf.get_ptr();
                    ByteArrayInputStream buf1 = buf.readSubbuffer(nbytes);//sgReadBytes(fp, nbytes);
                    /*int*/
                    long count = nbytes / (sizeof_unsignedint);
                    // va_int.reserve(count);
                    for (k = 0; k < count; ++k) {
                        va_int.add(buf1.readInt());
                    }
                }
            } else if (obj_type == SG_POINTS) {
                // read point elements
                read_object(currentobject, buf, SG_POINTS, nproperties, nelements,
                        pts_v, pts_n, pts_c, pts_tcs,
                        pts_vas, pt_materials);
            } else if (obj_type == SG_TRIANGLE_FACES) {
                // read triangle face properties
                read_object(currentobject, buf, SG_TRIANGLE_FACES, nproperties, nelements,
                        tris_v, tris_n, tris_c, tris_tcs,
                        tris_vas, tri_materials);
            } else if (obj_type == SG_TRIANGLE_STRIPS) {
                // read triangle strip properties
                read_object(currentobject, buf, SG_TRIANGLE_STRIPS, nproperties, nelements,
                        strips_v, strips_n, strips_c, strips_tcs,
                        strips_vas, strip_materials);
            } else if (obj_type == SG_TRIANGLE_FANS) {
                // read triangle fan properties
                read_object(currentobject, buf, SG_TRIANGLE_FANS, nproperties, nelements,
                        fans_v, fans_n, fans_c, fans_tcs,
                        fans_vas, fan_materials);
            } else {
                // unknown object type, just skip
                read_properties(buf, nproperties);

                // read elements
                for (j = 0; j < nelements; ++j) {
                    nbytes = buf.readUInt();//sgReadUInt(fp);
                    // cout << "element size = " << nbytes << endl;
                    /*if (nbytes > buf.get_size()) {
                        buf.resize(nbytes);
                    }*/
                    //char*ptr = buf.get_ptr();
                    buf.readSubbuffer(nbytes);//sgReadBytes(fp, nbytes/*, ptr*/);
                }
            }

          /*TODO  if (sgReadError()) {
                throw sg_io_exception("Error while reading object", sg_location(file, i));
            }*/
        }
        // close the file
        //TODO gzclose(fp);
        //TODO return true;
    }

    /**
     * BinaryLoader implementation for BTG. Convert BTG data to PML format. Moved from Obj.cxx/java to here.
     * This includes mapping of the BTG land classes to material known in matlib.
     */
    @Override
    public PortableModel buildPortableModel() {
        SGMaterialLib matlib = null;
        /*osg::ref_ptr<*/
        SGMaterialCache matcache = null;
        boolean useVBOs = false;
        boolean simplifyDistant = false;
        boolean simplifyNear = false;
       /* double ratio       = SG_SIMPLIFIER_RATIO;
        double maxLength   = SG_SIMPLIFIER_MAX_LENGTH;
        double maxError    = SG_SIMPLIFIER_MAX_ERROR;
        double object_range = SG_OBJECT_RANGE;
        double tile_min_expiry = SG_TILE_MIN_EXPIRY;
*/

        if (options != null) {
            matlib = options.getMaterialLib();
            
            /*useVBOs = (options->getPluginStringData("SimGear::USE_VBOS") == "ON");
            SGPropertyNode* propertyNode = options->getPropertyNode().get();

            // We control whether we simplify the nearby terrain and distant terrain separatey.
            // However, we don't allow only simplifying the near terrain!
            simplifyNear = propertyNode->getBoolValue("/sim/rendering/terrain/simplifier/enabled-near", simplifyNear);
            simplifyDistant = simplifyNear || propertyNode->getBoolValue("/sim/rendering/terrain/simplifier/enabled-far", simplifyDistant);
            ratio = propertyNode->getDoubleValue("/sim/rendering/terrain/simplifier/ratio", ratio);
            maxLength = propertyNode->getDoubleValue("/sim/rendering/terrain/simplifier/max-length", maxLength);
            maxError = propertyNode->getDoubleValue("/sim/rendering/terrain/simplifier/max-error", maxError);
            object_range = propertyNode->getDoubleValue("/sim/rendering/static-lod/rough", object_range);
            tile_min_expiry= propertyNode->getDoubleValue("/sim/rendering/plod-minimum-expiry-time-secs", tile_min_expiry);
            */
        }
        // FG-DIFF es gibt ja wegen Bundle zweimal opts.
        if (boptions != null && boptions.materialLib != null) {
            matlib = boptions.materialLib;
        }

        //TODO double
        /*SGVec3d*/
        center = (/*tile.*/((LoadedBtgFile) loadedfile).gbs_center);
        SGGeod geodPos = SGGeod.fromCart(center);
        //SGQuatd hlOr = SGQuatd::fromLonLat(geodPos)*SGQuatd::fromEulerDeg(0, 0, 180);

        if (matlib != null) {
            matcache = matlib.generateMatCache(geodPos);
            if (matcache.cache.size() == 0) {
                // das ist doch vielleicht ein Warning wert, weil es weiter unten dann eine NPE gibt.
                logger.warn("matcache is empty for position " + geodPos + ". matlib.size=" + matlib.getMatlibSize());
            }
        }
        logger.debug("SGLoadBTG gbs_center=" + center);

        // setRotateStatus the tiles so that the bounding boxes get nearly axis aligned.
        // this will help the collision tree's bounding boxes a bit ...
        /*std::vector<SGVec3d> nodes = tile.get_wgs84_nodes();
        for (unsigned i = 0; i < nodes.size(); ++i)
            nodes[i] = hlOr.transform(nodes[i]);
        tile.set_wgs84_nodes(nodes);

        SGQuatf hlOrf(hlOr[0], hlOr[1], hlOr[2], hlOr[3]);
        std::vector<SGVec3f> normals = tile.get_normals();
        for (unsigned i = 0; i < normals.size(); ++i)
            normals[i] = hlOrf.transform(normals[i]);
        tile.set_normals(normals);*/

        // tile surface    
        /*osg::ref_ptr<*/
        tileGeometryBin = new SGTileGeometryBin();

        if (!tileGeometryBin.insertSurfaceGeometry(this, matcache))
            return null;


        List<GeoMat> gml = tileGeometryBin.getSurfaceGeometryPart1(matcache, useVBOs);
        // eine Objectliste oder geoliste? Mal doch lieber geoliste. Scheint irgendwie passender.
        PortableModelDefinition ppo = new PortableModelDefinition();
        //1.8.24 ppo.geolist = new ArrayList<SimpleGeometry>();
        //1.8.24 ppo.geolistmaterial = new ArrayList<String>();
        ppo.translation = center;

        // 31.7.24: Since PortableModel no longer has a top level list, we need a synthetic root node like GLTF
        // uses? BTG content will end in 'kids' (with hierarchy from file). Maybe source is a path. Maybe better last part only which should be the bucket.
        ppo.setName(BTG_ROOT + "-" + StringUtils.substringBeforeLast(StringUtils.substringAfterLast(source, "/"), ".btg"));

        PortableModel ppfile = new PortableModel(ppo, null, (List<GeoMat>) null/*gml*/);
        ppfile.setName(source);

        int errorCnt = 0;
        int index = 0;
        for (GeoMat gm : gml) {
            //ppo.geolist.add(gm.geo);
            String gmMaterial;
            if (matlib == null) {
                //landdclass als material name
                ////1.8.24  ppo.geolistmaterial.add(gm.landclass);
                gmMaterial = gm.landclass;
            } else {
                // Einfach den Index als Material name. Vorsicht: Materials werden teilweise mehrfach verwendet! Darum duplizieren.
                //1.8.24 ppo.geolistmaterial.add("" + index);
                gmMaterial = gm.landclass;//"" + index;
                //LoadedMaterial ppm = new LoadedMaterial(gm.mat);
                //das Material selber hat auch noch keinen Namen. Ohne matlib ist mat aber null
                //5.10.23: Is there a solution if mat is null? Maybe just don't add it. 'gm.landclass' is not set properly.
                //1.8.24: Not sure what effect the simplification of PortableModelDefinition has. Duplicate
                //to parent(!!) still needed?? This might spoil the idea of reusing it in shared models.
                //13.11.24: Now use land class name as material name instead just the index. We assume/hope these will be unique
                if (gm.mat == null) {
                    logger.error("No material for land class '" + gm.landclass + "'. Will lead to hole in tile!");
                    errorCnt++;
                } else {
                    // 22.9.25: BTG materials should always have a texture. So consider it an error when there is no.
                    // Apparently this doesn't happen currently?
                    if (gm.mat.getTexture() == null) {
                        logger.error("No material texture for land class '" + gm.landclass + "'. Will lead to hole in tile!");
                        errorCnt++;
                    }
                    ppfile.addMaterial(gm.mat.duplicate(gm.landclass/*"" + index*/));
                }
            }
            //27.12.17: textureindex gibt es nicht mehr? TODO: Doch, fuer landclasses schon. Setzen bzw. klären
            index++;
            ppo.addChild(new PortableModelDefinition(gm.geo, gmMaterial));
        }
        //PreprocessedLoadedFile ppfile = new PreprocessedLoadedFile(gml);
        if (errorCnt > 0 && shouldFailOnError) {
            throw new RuntimeException("" + errorCnt + " error found (check log output). Aborting");
        }
        return ppfile;
    }

    private Vector3 readVec3f(ByteArrayInputStream buf) {
        float f1 = buf.readFloat();
        float f2 = buf.readFloat();
        float f3 = buf.readFloat();
        return new Vector3(f1, f2, f3);
    }

    private Vector2 readVec2f(ByteArrayInputStream buf) {
        float f1 = buf.readFloat();
        float f2 = buf.readFloat();
        return new Vector2(f1, f2);
    }


    /**
     * @return
     */
    /*SGVec3d*/
    private Vector3 readVec3d(ByteArrayInputStream buf) {
        double f1 = buf.readDouble();
        double f2 = buf.readDouble();
        double f3 = buf.readDouble();
        return new Vector3(f1, f2, f3);
        //return new SGVec3d(f1, f2, f3);
    }

    private Color/*SGVec4f*/ readVec4f(ByteArrayInputStream buf1) {
        float f1 = buf1.readFloat();
        float f2 = buf1.readFloat();
        float f3 = buf1.readFloat();
        float f4 = buf1.readFloat();
        return new Color(f1, f2, f3, f4);
    }

    @Override
    protected Log getLog() {
        return logger;
    }


    /**
     * Indices fuer Faces lesen
     * 05.08.2016:Nicht direkt Faces anlegen. Dafuer sind die Datenstrukturen zu undurchsichtig.
     */
    private void read_object(BTGObject currentobject, /*InputStream fp*/ByteArrayInputStream buf, int obj_type, long nproperties, long nelements,
                             List<List<Integer>> /*group_list&*/vertices,
                             List<List<Integer>> /*group_list&*/normals,
                             List<List<Integer>> /*group_list&*/colors,
                             group_tci_list texCoords,
                             List<List<List<Integer>>> /*group_vai_list&*/vertexAttribs,
                             List<String> /*string_list&*/materials) throws InvalidDataException {
        /* unsigned int*/
        int nbytes;
        /*unsigned*/
        int idx_mask;
        /*unsigned int*/
        int vertex_attrib_mask;
        int j;
        // (32768);  // 32 Kb
        //char material[ 256];
        String material = null;

        // default values
        if (obj_type == SG_POINTS) {
            idx_mask = SG_IDX_VERTICES;
        } else {
            idx_mask = (char) (SG_IDX_VERTICES | SG_IDX_TEXCOORDS_0);
        }
        vertex_attrib_mask = 0;

        for (j = 0; j < nproperties; ++j) {
            int prop_type;
            prop_type = buf.readByte();//sgReadChar(fp);
            nbytes = buf.readUInt();//sgReadUInt(fp);

            //buf.resize(nbytes);
            //char*ptr = buf.get_ptr();

            ByteArrayInputStream buf1 = null;
            switch (prop_type) {
                case SG_MATERIAL:
                    // De3r String wird aus den nbytes Bytes gebaut. Ein 0-Byte ist offenbar nicht gespeichert.
                    buf1 = buf.readSubbuffer(nbytes);//sgReadBytes(fp, nbytes);
                    if (nbytes > 255) {
                        nbytes = 255;
                    }
                    //strncpy(material, ptr, nbytes);
                    //material[nbytes] = '\0';
                    try {
                        material = StringUtils.buildString(buf1.getBuffer());
                    } catch (CharsetException e) {
                        // TODO improved eror handling
                        throw new RuntimeException(e);
                    }
                    if (Config.loaderdebuglog) {
                        logger.debug("material=" + material);
                    }
                    break;

                case SG_INDEX_TYPES:
                    if (nbytes == 1) {
                        idx_mask = buf.readByte();//sgReadChar(fp);
                    } else {
                        buf1 = buf.readSubbuffer(nbytes);//sgReadBytes(fp, nbytes/*, ptr*/);
                    }
                    break;

                case SG_VERT_ATTRIBS:
                    if (nbytes == 4) {
                        vertex_attrib_mask = buf.readUInt();//sgReadUInt(fp);
                    } else {
                        buf = buf.readSubbuffer(nbytes);//sgReadBytes(fp, nbytes/*, ptr*/);
                    }
                    break;

                default:
                    buf = buf.readSubbuffer(nbytes);//sgReadBytes(fp, nbytes/*, ptr*/);
                    //TODO SG_LOG(SG_IO, SG_ALERT, "Found UNKNOWN property type with nbytes == " << nbytes << " mask isType " << (int) idx_mask);
                    break;
            }
        }

       /*TODO if (sgReadError()) {
            throw sg_exception("Error reading object properties");
        }*/

        /*size_t*/
        long indexCount = bitsset(idx_mask);//std::bitset < 32 > ((int) idx_mask).count();
        if (indexCount == 0) {
            //TODO throw sg_exception("object index mask has no bits set");
        }

        //5.8.16 FaceList facelist = currentobject.addFacelist();
        for (j = 0; j < nelements; ++j) {
            nbytes = buf.readUInt();//sgReadUInt(fp);
            /*TODO if (sgReadError()) {
                throw sg_exception("Error reading element size");
            }*/

            //buf.resize(nbytes);
            //char*ptr = buf.get_ptr();
            ByteArrayInputStream buf1 = buf.readSubbuffer(nbytes);//sgReadBytes(fp, nbytes/*, ptr*/);

            /*TODOif (sgReadError()) {
                throw sg_exception("Error reading element bytes");
            }*/

            /*int_list*/
            List<Integer> vs = new ArrayList<Integer>();
            /*int_list*/
            List<Integer> ns = new ArrayList<Integer>();
            /*int_list*/
            List<Integer> cs = new ArrayList<Integer>();
            tci_list tcs = new tci_list();
            tci_list vas = new tci_list();

            if (version >= 10) {
                read_indices/*<uint32_t>*/(buf1/*ptr*/, nbytes, idx_mask, vertex_attrib_mask, vs, ns, cs, tcs, vas, false);
            } else {
                read_indices/*<uint16_t> */(buf1/*ptr*/, nbytes, idx_mask, vertex_attrib_mask, vs, ns, cs, tcs, vas, true);
            }

            // Fix for WS2.0 - ignore zero area triangles
            if (!(vs.size() == 0)/*empty()*/) {
                vertices.add(vs);
                normals.add(ns);
                colors.add(cs);
                texCoords.add(tcs);
                vertexAttribs.add(vas);
                materials.add(material);
            }
            /*6.8.16 if (vs.size() == 3) {
                List<Integer> tcs0 = tcs.get(0);
                Face face = new Face3((int)vs.get(0), (int)vs.get(1), (int)vs.get(2), currentobject.texcoords.get(tcs0.get(0)), currentobject.texcoords.get(tcs0.get(1)), currentobject.texcoords.get(tcs0.get(2)));
                switch (ns.size()) {
                    case 0:
                        // dann eben nicht
                        break;
                    case 1:
                        //fall through
                    case 3:
                        /*13.7.16: Das mit den mehreren Normals pro Face ist doch falsch, oder?
                        face.normals = new ArrayList<Vector3>();
                        for (int i = 0; i < ns.size(); i++) {
                            face.normals.add(currentobject.normals.get(ns.get(i)));
                        }* /
                        break;
                    default:
                        throw new InvalidDataException("unknown normal size " + ns.size());
                }
                facelist.faces.add(face);
            } else {
                throw new InvalidDataException("unknown face size " + vs.size());
            }*/
        } // of element iteration
        //Das Material selber kann er nicht finden, weil es ausserhalb des Models liegt.
        //6.8.16 currentobject.facelistmaterial.add(material);
        if (Config.loaderdebuglog)
            logger.debug("elements of objtype " + obj_type + " found: " + nelements + ". material=" + material + ", vertices.size=" + vertices.size());
    }

    void read_indices(/*char*buffer* /sgSimpleBuffer*/ByteArrayInputStream buffer,
            /*size_t*/long bytes,
                                                      int indexMask,
                                                      int vaMask,
                                                      List<Integer> vertices,
                                                      List<Integer> /*int_list&*/normals,
                                                      List<Integer> /*int_list&*/colors,
                                                      tci_list texCoords,
                                                      tci_list  /*vai_list&*/vas, boolean isshort
    ) {
        int indexSize = ((isshort) ? 2 : 4) * bitsset(indexMask);//std::bitset < 32 > ((int) indexMask).count();
        int vaSize = ((isshort) ? 2 : 4) * bitsset(vaMask);//std::bitset < 32 > ((int) vaMask).count();
        /*int*/
        long count = bytes / (indexSize + vaSize);

        // fix endian-ness of the whole lot, if required
      /*  if (sgIsBigEndian()) {
            int indices = bytes / sizeof(T);
            T * src = reinterpret_cast < T * > (buffer);
            for (int i = 0; i < indices; ++i) {
                sgEndianSwap(src++);
            }
        }

        T * src = reinterpret_cast < T * > (buffer);
        */
        if ((indexMask & SG_IDX_TEXCOORDS_0) > 0) {
            // In der Liste muessen wohl immer genau 4 Liste existieren
            while (texCoords.size() < 4) {
                texCoords.add(new ArrayList<Integer>());
            }
        }

        for (int i = 0; i < count; ++i) {
            if ((indexMask & SG_IDX_VERTICES) > 0)
                vertices.add((isshort) ? buffer.readUShort() : buffer.readUInt());
            if ((indexMask & SG_IDX_NORMALS) > 0)
                normals.add((isshort) ? buffer.readUShort() : buffer.readUInt());
            if ((indexMask & SG_IDX_COLORS) > 0)
                colors.add((isshort) ? buffer.readUShort() : buffer.readUInt());
            if ((indexMask & SG_IDX_TEXCOORDS_0) > 0) {
                texCoords.get(0)/*[0]*/.add((isshort) ? buffer.readUShort() : buffer.readUInt());
            }
            if ((indexMask & SG_IDX_TEXCOORDS_1) > 0)
                texCoords.get(1)/*[1]*/.add((isshort) ? buffer.readUShort() : buffer.readUInt());
            if ((indexMask & SG_IDX_TEXCOORDS_2) > 0)
                texCoords.get(2)/*[2]*/.add((isshort) ? buffer.readUShort() : buffer.readUInt());
            if ((indexMask & SG_IDX_TEXCOORDS_3) > 0)
                texCoords.get(3)/*[3]*/.add((isshort) ? buffer.readUShort() : buffer.readUInt());

            if (vaMask != 0) {
                if ((vaMask & SG_VA_INTEGER_0) > 0)
                    vas.get(0)/*[0]*/.add((isshort) ? buffer.readUShort() : buffer.readUInt());
                if ((vaMask & SG_VA_INTEGER_1) > 0)
                    vas.get(1)/*[0]*/.add((isshort) ? buffer.readUShort() : buffer.readUInt());
                if ((vaMask & SG_VA_INTEGER_2) > 0)
                    vas.get(2)/*[0]*/.add((isshort) ? buffer.readUShort() : buffer.readUInt());
                if ((vaMask & SG_VA_INTEGER_3) > 0)
                    vas.get(3)/*[0]*/.add((isshort) ? buffer.readUShort() : buffer.readUInt());
                if ((vaMask & SG_VA_FLOAT_0) > 0)
                    vas.get(4)/*[0]*/.add((isshort) ? buffer.readUShort() : buffer.readUInt());
                if ((vaMask & SG_VA_FLOAT_1) > 0)
                    vas.get(5)/*[0]*/.add((isshort) ? buffer.readUShort() : buffer.readUInt());
                if ((vaMask & SG_VA_FLOAT_2) > 0)
                    vas.get(6)/*[0]*/.add((isshort) ? buffer.readUShort() : buffer.readUInt());
                if ((vaMask & SG_VA_FLOAT_3) > 0)
                    vas.get(7)/*[0]*/.add((isshort) ? buffer.readUShort() : buffer.readUInt());
            }
        } // of elements in the index

        // WS2.0 fix : toss zero area triangles
        if ((count == 3) && ((indexMask & SG_IDX_VERTICES) > 0)) {
            if (Util.intValue(vertices.get(0)) == Util.intValue(vertices.get(1)) ||
                    Util.intValue(vertices.get(1)) == Util.intValue(vertices.get(2)) ||
                    Util.intValue(vertices.get(2)) == Util.intValue(vertices.get(0))) {
                vertices.clear();
            }
        }
    }


    private int bitsset(int mask) {
        int cnt = 0;
        for (int i = 0; i < 32; i++) {
            if ((mask & (1 << i)) > 0) {
                cnt++;
            }
        }
        return cnt;
    }


/*
    unsigned    int SGBinObject    ::    count_objects(const string_list&materials) {
        unsigned int result = 0;
        unsigned int start = 0, end = 1;
        unsigned int count = materials.size();
        string m;

        while (start < count) {
            m = materials[start];
            for (end = start + 1; (end < count) && (m == materials[end]); ++end) {
            }
            ++result;
            start = end;
        }

        return result;
    }
*/

    /*
        static unsigned    int max_object_size(const string_list&materials) {
            unsigned int max_size = 0;

            for (unsigned int start = 0;
            start<materials.size ();){
                string m = materials[start];
                unsigned int end = start + 1;
                // find range of objects with identical material, calc its size
                for (; (end < materials.size()) && (m == materials[end]); ++end) {
                }

                unsigned int cur_size = end - start;
                max_size = std::max (max_size, cur_size);
                start = end;
            }

            return max_size;
        }
    */
    /*const /*unsigned */ int VERSION_7_MATERIAL_LIMIT = 0x7fff;


    void read_properties(ByteArrayInputStream buf/*fp*/, int nproperties) {
        //sgSimpleBuffer buf;
        /*uint32_t*/
        int nbytes;

        // read properties
        for (int j = 0; j < nproperties; ++j) {
            int prop_type;
            prop_type = buf.readByte();//sgReadChar(fp);
            nbytes = buf.readUInt();//sgReadUInt(fp);
            // cout << "property size = " << nbytes << endl;
            /*if (nbytes > buf.get_size()) {
                buf.resize(nbytes);
            }*/
            //char*ptr = buf.get_ptr();
            buf = buf.readSubbuffer(nbytes);//sgReadBytes(fp, nbytes/*, ptr*/);
        }
    }

    /*
    bool SGBinObject    ::    add_point(const SGBinObjectPoint&pt) {
        // add the point info
        pt_materials.push_back(pt.material);

        pts_v.push_back(pt.v_list);
        pts_n.push_back(pt.n_list);
        pts_c.push_back(pt.c_list);

        return true;
    }
    */

    /*bool SGBinObject    ::    add_triangle(const SGBinObjectTriangle&tri) {
        // add the triangle info and keep lists aligned
        tri_materials.push_back(tri.material);
        tris_v.push_back(tri.v_list);
        tris_n.push_back(tri.n_list);
        tris_c.push_back(tri.c_list);
        tris_tcs.push_back(tri.tc_list);
        tris_vas.push_back(tri.va_list);

        return true;
    }*/

    /*24.4.17 int sgReadUInt(InputStream fd/*, unsigned int *var* /) {
        fd.read(buf4, 4);
        return sgSimpleBuffer.readUInt(buf4, 0);
    }

    int sgReadInt(InputStream fd/*, int *var * /) {
        fd.read(buf4, 4);
        return sgSimpleBuffer.readUInt(buf4, 0);
    }

    int sgReadUShort(InputStream fd/*, unsigned short *var * /) {
        fd.read(buf2, 2);
        return sgSimpleBuffer.readUShort(buf2, 0);
    }

    int sgReadShort(InputStream fd/*, short *var* /) {
        fd.read(buf2, 2);
        return sgSimpleBuffer.readUShort(buf2, 0);
    }

    static int sgReadChar(InputStream fd/*, char *var * /) {
        return fd.read();
    }

    static sgSimpleBuffer sgReadBytes(InputStream fd,/* const unsigned int* /int n/*, void *var * /) {
        if (n < 0) {
            throw new RuntimeException("negative n " + n);
        }
        byte[] buf = new byte[n];
        fd.read(buf, n);
        return new sgSimpleBuffer(buf);
    }*/


    public List</*7.2.18 Native*/Vector3> get_wgs84_nodes() {
        //Das sind  wirklich die gelesenen ohne Manipulation
        return loadedfile.object.getVertices();
    }

    public List<Vector2> get_texcoords() {
        return texcoords;
    }
}


class BTGObject extends LoadedObject {
    //public List<Vector2> texcoords = new ArrayList<Vector2>();
}

