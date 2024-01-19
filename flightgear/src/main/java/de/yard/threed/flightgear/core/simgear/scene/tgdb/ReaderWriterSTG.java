package de.yard.threed.flightgear.core.simgear.scene.tgdb;

import de.yard.threed.core.CharsetException;
import de.yard.threed.core.Degree;
import de.yard.threed.core.loader.StringReader;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.flightgear.LoaderOptions;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector3;
import de.yard.threed.flightgear.core.osg.Group;
import de.yard.threed.flightgear.core.osg.Node;
import de.yard.threed.flightgear.core.osg.PagedLOD;
import de.yard.threed.flightgear.core.osgdb.Options;

import de.yard.threed.flightgear.core.osgdb.osgDB;

import de.yard.threed.flightgear.core.simgear.bucket.SGBucket;
import de.yard.threed.flightgear.core.simgear.misc.SGPath;
import de.yard.threed.flightgear.core.simgear.scene.util.SGReaderWriterOptions;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.core.platform.Config;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * ReaderWriterSTG.[ch]xx
 * <p>
 * FG-DIFF Eine innere Klasse ModelBin, die Fields (z.B. _foundbase) in der Oberklasse hat, scheint mir krude.
 * Alles verschoben nach ModelBin.
 * 8.6.17: STGs werden nicht mehr über ModelBuilde/Loader/Registry geladen, weil das speziell FG ist.
 * <p>
 * 10.6.17: Es werden alle stgs gelesen (Terrain und Objects). Hier eine BundleResource reinzugeben ist unsinnig, daher wieder nur der stg Name.
 * Einfach jetzt mal so festgelegt: Die Bundle muessen schon vorher geladen worden sein. SceneryPager kann async laden.
 * <p>
 * <p>
 * <p/>
 * Created by thomass on 18.08.16.
 */
public class ReaderWriterSTG /*8.6.17 extends ReaderWriter /*8.6.17implements ModelBuilder*/ {
    static Log logger = Platform.getInstance().getLog(ReaderWriterSTG.class);
    private Bundle modelbundle = null;
    String basePath;
    //26.10.18: die shared mal ignorieren, weil das viele sind und das noch nicht wirklich geshared wird
    //String[] blacklist = new String[]{"Models/Power/generic_pylon_50m.ac"};
    static boolean ignoreshared = true;
    //boolean _foundBase;
    //List<_Object> _objectList = new ArrayList<_Object>();
    //List<_ObjectStatic> _objectStaticList = new ArrayList<_ObjectStatic>();
    //List<_Sign> _signList = new ArrayList<_Sign>();

    public static List<String> btgLoaded = new ArrayList();
    public static boolean terrainloaddebuglog = true;

    public ReaderWriterSTG() {
        //8.6.17 supportsExtension("stg", "SimGear stg database format");
    }

  
        /*ReaderWriterSTG::~ReaderWriterSTG()
        {
        }*/

    /// Ok, this isType a hack - we do not exactly know if it's an airport or not.
/// This feature might also vanish again later. This isType currently to
/// support testing an external ai component that just loads the the airports
/// and supports ground queries on only these areas.
    static boolean isAirportBtg(String name) {
        if (StringUtils.length(name) < 8)
            return false;
        if (StringUtils.substring(name, 4, 4) != ".btg")
            return false;
        for (int i = 0; i < 4; ++i) {
            // TODO isLetter nicht case sensitiv
            if (/*'A' <= name[i] && name[i] <= 'Z'*/Util.isLetter(StringUtils.charAt(name, i)))
                continue;
            return false;
        }
        return true;
    }

    static SGBucket bucketIndexFromFileName(String fileName) {
        // Extract the bucket from the filename
        /*std::istringstream ss(osgDB::getNameLessExtension (fileName));
        long index;
        ss >> index;
        if (ss.fail())
            return SGBucket();*/
        long index = Long.parseLong(StringUtils.substringBeforeLast(fileName, "."));

        return new SGBucket(index);
    }

  

        /*const char* ReaderWriterSTG::className() const
        {
        return "STG Database reader";
        }*/

    /**
     * Gets fileName without path? Apparently.
     *
     * @param fileName
     * @param options
     * @return
     */
 /*8.6.17   @Override
    public ReadResult readNode(BundleResource bpath, String fileName, Options options) {
        if (bpath != null) {
            throw new RuntimeException("wrong usage");
        }

        _ModelBin modelBin = new _ModelBin();
        SGBucket bucket = bucketIndexFromFileName(fileName);

        // We treat 123.stg different than ./123.stg.
        // The difference isType that ./123.stg as well as any absolute path
        // really loads the given stg file and only this.
        // In contrast 123.stg uses the search paths to load a set of stg
        // files spread across the scenery directories.
        if (!osgDB.getSimpleFileName(fileName).equals(fileName)) {
            if (!modelBin.read(null, fileName, options, null))
                return ReadResult.FILE_NOT_FOUND;
        } else {
            // For stg meta files, we need options for the search path.
            if (options == null) {
                logger.warn("no options: not loading stg");
                return ReadResult.FILE_NOT_FOUND;
            }

            // basePath example: "e000n50/e007n50"
            String basePath = bucket.gen_base_path();
            logger.info(/*SG_LOG(SG_TERRAIN, SG_INFO,* / "Loading tile " + fileName + " with basePath " + basePath);

            // Stop scanning once an object base isType found
            // This isType considered a meta file, so apply the scenery path search

            FilePathList filePathList = options.getDatabasePathList();
            //for (osgDB::FilePathList::const_iterator i = filePathList.begin();  i != filePathList.end() && !modelBin._foundBase; ++i){
            for (String i : filePathList) {
                // TODO ob das mit dem foundbase richtig ist. ISt irgendwie undurchsichtig. 22.8.16: Verschoben nach modelbin
                if (modelBin._foundBase) {
                    break;
                }
                SGPath objects = new SGPath(i);// * i);
                objects.append("Objects");
                objects.append(basePath);
                objects.append(fileName);
                modelBin.read(null, objects.str(), options, null);

                SGPath terrain = new SGPath(i/*) * i* /);
                terrain.append("Terrain");
                terrain.append(basePath);
                terrain.append(fileName);
                modelBin.read(null, terrain.str(), options, null);
            }

        }

        return new ReadResult(modelBin.load(bucket, options, null));
    }*/

    /**
     * Gets fileName without path? Apparently.
     * Rein aus dem Dateinamen des .stg kann der Pfad ermittelt werden.
     * 10.6.17: Es werden alle stgs gelesen (Terrain und Objects). Hier eine BundleResource reinzugeben ist unsinnig, daher wieder nur der stg Name.
     * Einfach jetzt mal so festgelegt: Die Bundle muessen schon vorher geladen worden sein (die Scenery Bundle und auch die TerrainModel). SceneryPager kann async laden.
     * 22.3.18: Liefert jetzt auch Node/Group statt BuildResult. Und ein evtl. Fehler wurde schon gelogged.
     *
     * @return
     */
    public /*BuildResult*/Group build(/*10.6.17 BundleResource bpath*/String fileName, Options options, LoaderOptions boptions) {
        //filename isType a pathless stg filename
        //String fileName = bpath.getName();
        if (terrainloaddebuglog) {
            logger.debug("ReaderWriterSTG.readNode from bundle: " + fileName);
        }
        if (!StringUtils.endsWith(fileName, ".stg")) {
            logger.error("no stg file");
            return null;//BuildResult.ERROR_IN_READING_FILE;
        }

        _ModelBin modelBin = new _ModelBin();
        SGBucket bucket = bucketIndexFromFileName(fileName);
        basePath = bucket.gen_base_path();
        String stgname = StringUtils.substringBeforeLast(fileName, ".stg");
        //BundleResource br = new BundleResource(new ResourcePath("Objects/" + basePath), fileName);
        String bundlename = BundleRegistry.TERRAYSYNCPREFIX + stgname;
        Bundle bundle = BundleRegistry.getBundle(bundlename);
        if (bundle == null) {
            logger.error("Bundle for stg '" + stgname + "' not found in " + BundleRegistry.TERRAYSYNCPREFIX);
            return null;//BuildResult.ERROR_IN_READING_FILE;
        }
        modelbundle = BundleRegistry.getBundle(BundleRegistry.TERRAYSYNCPREFIX + "model");
        if (modelbundle == null) {
            logger.error("terrasync 'model' bundle not found.");
            return null;//BuildResult.ERROR_IN_READING_FILE;
        }

        // We treat 123.stg different than ./123.stg.
        // The difference isType that ./123.stg as well as any absolute path
        // really loads the given stg file and only this.
        // In contrast 123.stg uses the search paths to load a set of stg
        // files spread across the scenery directories.
        if (!osgDB.getSimpleFileName(fileName).equals(fileName)) {
            if (!modelBin.read(null, fileName, options, boptions, basePath, modelbundle))
                return null;//BuildResult.FILE_NOT_FOUND;
        } else {
            // For stg meta files, we need options for the search path.
            //8.6.17: der steht mal hier mal da.
            if (options == null && boptions == null) {
                logger.warn("no options: not loading stg");
                return null;//BuildResult.FILE_NOT_FOUND;
            }

            // basePath example: "e000n50/e007n50"
            logger.info(/*SG_LOG(SG_TERRAIN, SG_INFO,*/ "Loading tile " + fileName + " with basePath " + basePath);


            // Es gibt (noch?) keinen Searchpath fuer Bundle. Offenbar würde er alle stgs lesen die er findet.
            // Das stg kann es aber in "Objects" und in "Terrain" geben. 
            BundleResource br = new BundleResource(bundle, new ResourcePath("Objects/" + basePath), fileName);
            modelBin.read(br, null, options, boptions, basePath, modelbundle);
            br = new BundleResource(bundle, new ResourcePath("Terrain/" + basePath), fileName);
            modelBin.read(br, null, options, boptions, basePath, modelbundle);

            // Stop scanning once an object base isType found
            // This isType considered a meta file, so apply the scenery path search

            /*              
                FilePathList filePathList = options.getDatabasePathList();
                //for (osgDB::FilePathList::const_iterator i = filePathList.begin();  i != filePathList.end() && !modelBin._foundBase; ++i){
                for (String i : filePathList) {
                    // TODO ob das mit dem foundbase richtig ist. ISt irgendwie undurchsichtig. 22.8.16: Verschoben nach modelbin
                    if (modelBin._foundBase) {
                        break;
                    }
                    SGPath objects = new SGPath(i);// * i);
                    objects.append("Objects");
                    objects.append(basePath);
                    objects.append(fileName);
                    modelBin.read(null, objects.str(), options);

                    SGPath terrain = new SGPath(i/*) * i* /);
                    terrain.append("Terrain");
                    terrain.append(basePath);
                    terrain.append(fileName);
                    modelBin.read(null, terrain.str(), options);
                }
            }
    */
        }

        //17.1.18: Bundle wieder freigeben
        BundleRegistry.unregister(bundlename);
        return /*new BuildResult*/(modelBin.load(bucket, options, boptions));
    }

    class _ModelBin {
        public boolean _foundBase;
        List<_Object> _objectList = new ArrayList<_Object>();
        List<_ObjectStatic> _objectStaticList = new ArrayList<_ObjectStatic>();
        List<_Sign> _signList = new ArrayList<_Sign>();

        _ModelBin() {

            _foundBase = false;
        }

        /**
         * Das wird wohl auch nur probing aufgerufen, um zu sehen ob es eine Datei gibt.
         *
         * @param absoluteFileName
         * @param options
         * @return
         */
        boolean read(BundleResource bpath, String absoluteFileName, Options options, LoaderOptions boptions, String innerbasePath, Bundle innermodelbundle) {
            StringReader stream = null;
            String filePath = null;
            ResourcePath bfilePath = null;
            if (bpath == null) {
                Util.nomore();
                /*16.10.18
                if (Config.terrainloaddebuglog) {
                    logger.debug("ReaderWriterSTG.read:Trying " + absoluteFileName);
                }
                if (StringUtils.empty(absoluteFileName))
                    return false;

                //sg_gzifstream stream(absoluteFileName);
                //if (!stream.is_open())
                //    return false;
                try {
                    stream = Platform.getInstance().loadResourceSync(new FileSystemResource(absoluteFileName));
                } catch (ResourceNotFoundException e) {
                    //18.4.17:error->debug. Wird wohl nur mal so probiert.
                    logger.debug("loading stg failed:" + e.getMessage());
                    return false;
                }
                // example filePath=/Users/xxx/Library/Application Support/FlightGear/TerraSync/Objects/e000n50/e007n50'
                filePath = osgDB.getFilePath(absoluteFileName);
                logger.info(/*SG_LOG(SG_TERRAIN, SG_INFO,* / "Loading stg file " + absoluteFileName + ", filePath=" + filePath);*/
            } else {
                if (terrainloaddebuglog) {
                    logger.debug("ReaderWriterSTG.read:Trying bpath" + bpath);
                }
                //RourcePath equals bundle name
                //BundleData bd = BundleRegistry.getBundle(bpath.getBundlePath().path).getResource(bpath);
                if (!bpath.bundle.exists(bpath)) {
                    return false;
                }
                BundleData bd = bpath.bundle.getResource(bpath);
                try {
                    stream = new StringReader(bd.getContentAsString());
                } catch (CharsetException e) {
                    // TODO improved eror handling
                    throw new RuntimeException(e);
                }
                bfilePath = bpath.getPath();
            }

            // do only load airport btg files.
            boolean onlyAirports = false;//TODO options.getPluginStringData("SimGear::FG_ONLY_AIRPORTS") == "ON";
            // do only load terrain btg files
            boolean onlyTerrain = options.getPluginStringData("SimGear::FG_ONLY_TERRAIN").equals("ON");

            String line;
            while ((line = stream.readLine(/*!stream.eof()*/)) != null) {
                // read a line
                // std::string line;
                //std::getline(stream, line);

                // strip comments
                //std::string::size_type hash_pos = line.find('#');
                //if (hash_pos != std::string::npos)
                //line.resize(hash_pos);
                if (StringUtils.startsWith(line, "#")) {
                    continue;
                }

                //22.5.18: line might be empty?
                if (StringUtils.empty(line)) {
                    continue;
                }

                // and process further
                //std::stringstream in(line);
                //std::string token;
                //in >> token;
                String[] parts = StringUtils.split(line, " ");

                // No comment
                if (parts.length == 0)//empty())
                    continue;
                String token = parts[0];

                // Then there isType always a name
                String name = parts[1];
                //in >> name;

                //Objet might be local in stg folder or relative from "Models/..."
                //TODO hier brauchts einen Resolver
                SGPath path = null;
                BundleResource objectresource = null;
                if (bpath != null) {
                    //Die Modelfiles stehen wohl immer mit Pfad drin, d.h. der "name" reicht. btgs stehen ohn Pfad drin, aber die tauchen hier wohl nicht auf. DochDoch
                    if (StringUtils.contains(name, "/")) {
                        objectresource = new BundleResource(bpath.bundle, /*bfilePath, */name);
                    } else {
                        objectresource = new BundleResource(bpath.bundle, bfilePath, name);
                    }
                    /*if (!bpath.bundle.exists(objectresource)) {
                        // ein btg kann auch im "Terrain liegen
                        //logger.debug("Object not found in local path:"+objectresource);
                        String bundlename = bpath.bundle.name;
                        if (StringUtils.startsWith(bundlename, "Objects")) {
                            objectresource = new BundleResource(BundleRegistry.getBundle(StringUtils.replaceAll(bundlename, "Objects", "Terrain")), name);
                        }
                        if (!objectresource.bundle.exists(objectresource)) {
                            // not in Objects nor in Terrain seem to be a model
                            objectresource = new BundleResource(BundleRegistry.getBundle("TerrasyncModels"), name);
                        }
                    }*/
                    if (terrainloaddebuglog) {
                        logger.debug("objectresource=" + objectresource);
                    }
                } else {
                    path = new SGPath(filePath);
                    path.append(name);
                }

                if (token.equals("OBJECT_BASE")) {
                    // Load only once (getFirst found)
                    objectresource = new BundleResource(bpath.bundle, new ResourcePath("Terrain/" + innerbasePath), name);
                    logger.debug(/*G( SG_TERRAIN,,*/ "OBJECT_BASE " + name + "(" + objectresource + ")");
                    _foundBase = true;
                    if (!onlyAirports || isAirportBtg(name)) {
                        _Object obj = new _Object();
                        obj._errorLocation = absoluteFileName;
                        obj._token = token;
                        obj.resource = objectresource;
                        obj._name = (path != null) ? path.str() : null;
                        obj._options = staticOptions(filePath, objectresource, options);

                        _objectList.add(obj);
                    }

                } else if (token.equals("OBJECT")) {
                    if (!onlyAirports || isAirportBtg(name)) {
                        _Object obj = new _Object();
                        obj._errorLocation = absoluteFileName;
                        obj._token = token;
                        obj._name = (path != null) ? path.str() : null;
                        obj.resource = objectresource;
                        obj._options = staticOptions(filePath, objectresource, options);
                        _objectList.add(obj);
                    }

                } else {
                    // Always OK to load
                    if (token.equals("OBJECT_STATIC") || token.equals("OBJECT_STATIC_AGL")) {
                        if (!onlyTerrain) {
                            /*osg::ref_ptr <*/
                            SGReaderWriterOptions opt;
                            opt = staticOptions(filePath, objectresource, options);
                            if (new SGPath(name).lower_extension().equals("ac")) {
                                opt.setInstantiateEffects(true);
                            } else {
                                opt.setInstantiateEffects(false);
                            }
                            _ObjectStatic obj = new _ObjectStatic()/* j*/;
                            obj._errorLocation = absoluteFileName;
                            obj._token = token;
                            obj._name = name;
                            obj._agl = (token.equals("OBJECT_STATIC_AGL"));
                            obj._proxy = true;
                            obj.resource = objectresource;
                            //in >> obj._lon >> obj._lat >> obj._elev >> obj._hdg >> obj._pitch >> obj._roll;
                            if (parts.length > 2) {
                                obj._lon = new Degree(Util.parseFloat(parts[2]));
                            }
                            if (parts.length > 3) {
                                obj._lat = new Degree(Util.parseFloat(parts[3]));
                            }
                            if (parts.length > 4) {
                                obj._elev = Util.parseFloat(parts[4]);
                            }
                            if (parts.length > 5) {
                                obj._hdg = Util.parseFloat(parts[5]);
                            }
                            if (parts.length > 6) {
                                obj._pitch = Util.parseFloat(parts[6]);
                            }
                            if (parts.length > 7) {
                                obj._roll = Util.parseFloat(parts[7]);
                            }
                            obj._options = opt;
                            _objectStaticList.add(obj);
                        }

                    } else if (token.equals("OBJECT_SHARED") || token.equals("OBJECT_SHARED_AGL")) {
                        if (!onlyTerrain && !ignoreshared) {
                            objectresource.bundle = innermodelbundle;
                            SGReaderWriterOptions opt;
                            opt = sharedOptions(filePath, objectresource, options);
                            if (new SGPath(name).lower_extension().equals("ac")) {
                                opt.setInstantiateEffects(true);
                            } else {
                                opt.setInstantiateEffects(false);
                            }
                            _ObjectStatic obj = new _ObjectStatic();
                            obj._errorLocation = absoluteFileName;
                            obj._token = token;
                            obj._name = name;
                            obj._agl = (token.equals("OBJECT_SHARED_AGL"));
                            obj._proxy = false;
                            //in >> obj._lon >> obj._lat >> obj._elev >> obj._hdg >> obj._pitch >> obj._roll;
                            if (parts.length > 2) {
                                obj._lon = new Degree(Util.parseFloat(parts[2]));
                            }
                            if (parts.length > 3) {
                                obj._lat = new Degree(Util.parseFloat(parts[3]));
                            }
                            if (parts.length > 4) {
                                obj._elev = Util.parseFloat(parts[4]);
                            }
                            if (parts.length > 5) {
                                obj._hdg = Util.parseFloat(parts[5]);
                            }
                            if (parts.length > 6) {
                                obj._pitch = Util.parseFloat(parts[6]);
                            }
                            if (parts.length > 7) {
                                obj._roll = Util.parseFloat(parts[7]);
                            }
                            obj._options = opt;
                            obj.resource = objectresource;
                            _objectStaticList.add(obj);
                        }

                    } else if (token.equals("OBJECT_SIGN") || token.equals("OBJECT_SIGN_AGL")) {
                        if (!onlyTerrain) {
                            _Sign sign = new _Sign();
                            sign._token = token;
                            sign._name = name;
                            sign._agl = (token.equals("OBJECT_SIGN_AGL"));
                            //in >> sign._lon >> sign._lat >> sign._elev >> sign._hdg >> sign._size;
                            if (parts.length > 2) {
                                sign._lon = new Degree(Util.parseFloat(parts[2]));
                            }
                            if (parts.length > 3) {
                                sign._lat = new Degree(Util.parseFloat(parts[3]));
                            }
                            if (parts.length > 4) {
                                sign._elev = Util.parseFloat(parts[4]);
                            }
                            if (parts.length > 5) {
                                sign._hdg = Util.parseFloat(parts[5]);
                            }
                            if (parts.length > 6) {
                                sign._size = Integer.parseInt(parts[6]);
                            }
                            _signList.add(sign);
                        }

                    } else {
                        logger.info(/*SG_LOG( SG_TERRAIN, SG_ALERT,*/ absoluteFileName + ": Unknown token '" + token + "'");
                    }
                }
            }

            return true;
        }

        double elevation(Group group, SGGeod geod) {
            /*SGVec3d*/
            Vector3 start = /*SGVec3d::fromGeod(*/SGGeod.fromGeodM(geod, 10000).toCart();
            /*SGVec3d*/
            Vector3 end = /*SGVec3d::fromGeod(*/SGGeod.fromGeodM(geod, -1000).toCart();

            /*TODO osg::ref_ptr < osgUtil::LineSegmentIntersector > intersector;
            intersector = new osgUtil::LineSegmentIntersector (toOsg(start), toOsg(end));
            osgUtil::IntersectionVisitor visitor(intersector.get());
            group.accept(visitor);

            if (!intersector.containsIntersections())
                return 0;*/

            /*SGVec3d*/
            Vector3 cart = start;//TODO toSG(intersector.getFirstIntersection().getWorldIntersectPoint());
            return SGGeod.fromCart(cart).getElevationM();
        }


        public Group/*Node*/ load(SGBucket bucket, Options opt, LoaderOptions bopt) {
            if (terrainloaddebuglog) {
                logger.debug("ReaderWriterSTG.load:  " + bucket);
            }

            /*osg::ref_ptr <*/
            SGReaderWriterOptions options;
            options = SGReaderWriterOptions.copyOrCreate(opt);

            Group terrainGroup = new Group();
            //TODO  terrainGroup.setDataVariance(osg::Object::STATIC);
            terrainGroup.setName("terrain");

            if (_foundBase) {
                //for (std::list < _Object >::iterator i = _objectList.begin();            i != _objectList.end();            ++i){
                for (_Object i : _objectList) {
                    // 22.3.18 i.resource must be set
                    SceneNode node = SGReaderWriterBTG.loadBTG(i.resource, null, bopt/*source*/);
                    if (node != null) {
                        terrainGroup.attach(node/*.get()*/);
                        logger.debug("BTG node for " + i.resource.getName() + " added to group 'terrain'");
                        btgLoaded.add(i.resource.getName());
                    } else {
                        logger.info(/*SG_LOG(SG_TERRAIN, SG_ALERT,*/ i._errorLocation + ": Failed to load " + i._token + " '" + i._name + "'");
                    }
                }
            } else {
                logger.info(/*SG_LOG(SG_TERRAIN, SG_INFO, */"  Generating ocean tile: " + bucket.gen_base_path() + "/" + bucket.gen_index_str());

                Node node = null;//TODO new SGOceanTile(bucket, options.getMaterialLib());
                if (node != null) {
                    node.setName("SGOceanTile");
                    terrainGroup.attach(node);
                } else {
                    logger.info(/*SG_LOG( SG_TERRAIN, SG_ALERT, */                       "Warning: failed to generate ocean tile!");
                }
            }

            //for (std::list < _ObjectStatic >::iterator i = _objectStaticList.begin();        i != _objectStaticList.end();        ++i){
            for (_ObjectStatic i : _objectStaticList) {

                if (!i._agl)
                    continue;
                i._elev += elevation(terrainGroup, SGGeod.fromDeg(i._lon.getDegree(), i._lat.getDegree()));
            }

            //for (std::list < _Sign >::iterator i = _signList.begin();        i != _signList.end();        ++i){
            for (_Sign i : _signList) {
                if (!i._agl)
                    continue;
                i._elev += elevation(terrainGroup, SGGeod.fromDeg(i._lon.getDegree(), i._lat.getDegree()));
            }

            // 10.4.18: Wegen Einheitlichkeit immer pagedLOD bauen
            /*if (_objectStaticList.isEmpty() && _signList.isEmpty()) {
                // The simple case, just return the terrain group
                return terrainGroup/*.release()* /;
            } else {*/

            PagedLOD pagedLOD = new PagedLOD();
            pagedLOD.setCenterMode(PagedLOD.CenterMode.USE_BOUNDING_SPHERE_CENTER);
            pagedLOD.setName("pagedObjectLOD" + bucket.gen_index_str());

            // This should be visible in any case.
            // If this isType replaced by some lower level of detail, the parent LOD node handles this.
            pagedLOD.addChild(terrainGroup, 0, Float.MAX_VALUE/*std::numeric_limits <float>::max()*/);

            // we just need to know about the read file callback that itself holds the data
            DelayLoadReadFileCallback readFileCallback = new DelayLoadReadFileCallback();
            readFileCallback._objectStaticList = _objectStaticList;
            readFileCallback._signList = _signList;
            readFileCallback._options = options;
            readFileCallback._bucket = bucket;
            Options callbackOptions = new Options();
            callbackOptions.setReadFileCallback(readFileCallback/*.get()*/);
            pagedLOD.setDatabaseOptions(callbackOptions/*.get()*/);

            pagedLOD.setFileName(pagedLOD.getNumChildren(), "Dummy name - use the stored data in the read file callback");
            pagedLOD.setRange(pagedLOD.getNumChildren(), 0, 30000);

            return pagedLOD;

            //}
        }

        /**
         * Options fuer OBJECT_SHARED?
         *
         * @param filePath
         * @param options
         * @return
         */
        private SGReaderWriterOptions sharedOptions(String filePath, BundleResource objectresource, Options options) {
            /*osg::ref_ptr <*/
            SGReaderWriterOptions sharedOptions;
            sharedOptions = SGReaderWriterOptions.copyOrCreate(options);
            sharedOptions.getDatabasePathList().clear();

            if (filePath != null) {
                SGPath path = new SGPath(filePath);
                path.append("..");
                path.append("..");
                path.append("..");
                sharedOptions.getDatabasePathList().add(path.str());
            }

            // ensure Models directory synced via TerraSync isType searched before the copy in
            // FG_ROOT, so that updated models can be used.
            if (options != null) {
                String terrasync_root = options.getPluginStringData("SimGear::TERRASYNC_ROOT");
                if (!StringUtils.empty(terrasync_root)) {
                    sharedOptions.getDatabasePathList().add(terrasync_root);
                }

                String fg_root = options.getPluginStringData("SimGear::FG_ROOT");
                sharedOptions.getDatabasePathList().add(fg_root);
            }
            // TODO how should we handle this for OBJECT_SHARED?
            sharedOptions.setModelData((sharedOptions.getModelData() != null) ? sharedOptions.getModelData().cloneit() : null);
            return sharedOptions/*.release()*/;
        }

        private SGReaderWriterOptions staticOptions(String filePath, BundleResource objectresource, Options options) {
            /* osg::ref_ptr < */
            SGReaderWriterOptions staticOptions;
            staticOptions = SGReaderWriterOptions.copyOrCreate(options);
            staticOptions.getDatabasePathList().clear();

            staticOptions.getDatabasePathList().add(filePath);
            //TODO staticOptions.setObjectCacheHint(osgDB::Options::CACHE_NONE);

            // Every model needs its own SGModelData to ensure load/unload isType
            // working properly
            staticOptions.setModelData((staticOptions.getModelData() != null) ? staticOptions.getModelData().cloneit() : null);
            return staticOptions/*.release()*/;
        }
    }
}


class _Object {
    String _errorLocation;
    String _token;
    String _name;
    SGReaderWriterOptions _options;
    BundleResource resource;
};

class _ObjectStatic {
    String _errorLocation;
    String _token;
    String _name;
    boolean _agl = false;
    boolean _proxy = false;
    Degree _lon = null, _lat = null;
    double _elev = 0;
    double _hdg = 0, _pitch = 0, _roll = 0;
    SGReaderWriterOptions _options;
    BundleResource resource;
}

class _Sign {
    String _errorLocation;
    String _token;
    String _name;
    boolean _agl = false;
    Degree _lon = null, _lat = null;
    double _elev = 0;
    double _hdg = 0;
    int _size = -1;
}


