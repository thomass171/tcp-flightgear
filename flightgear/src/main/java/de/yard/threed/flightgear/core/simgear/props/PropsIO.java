package de.yard.threed.flightgear.core.simgear.props;

import de.yard.threed.core.platform.*;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
//import de.yard.threed.flightgear.core.FileSystemResource;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.resource.URL;
import de.yard.threed.flightgear.FgBundleHelper;
import de.yard.threed.flightgear.core.simgear.SGPropertyNode;
import de.yard.threed.flightgear.core.simgear.misc.SgResourceManager;
import de.yard.threed.flightgear.core.simgear.structure.SGException;
import de.yard.threed.flightgear.core.simgear.structure.SGIOException;

import de.yard.threed.core.resource.NativeResource;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.core.Color;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 27.12.16: Fuer das Lesen der includes wird erst mal kein Event verwendet.
 * 27.12.16: Umgestellt von SGPath auf ResourcePath und String auf NativeResource.
 * 10.04.17: Jetzt fuer Bundles. readbyecs koennte damit obselet sein.
 * 24.04.17: Wirft weiterhin SGException ,um Fehlerbehandlung zu erzwingen. Wird wohl nur innerhalb von Loadern verwendet.
 * 08.02.24: Reading properties no longer static, legacy file path source removed, decoupled from bundle.
 * <p>
 * Created by thomass on 04.12.15.
 */
public class PropsIO {
    Log logger = Platform.getInstance().getLog(PropsIO.class);
    // for testing
    public List<String> locations = new ArrayList<String>();

    public PropsIO() {

    }

    public void readProperties(String file, SGPropertyNode startnode) throws SGException {
        readProperties(file, startnode, 0, false);
    }

    /*9.2.24 public void readProperties(String file, SGPropertyNode startnode, int default_mode) throws SGException {
        readProperties(file, startnode, default_mode, false);
    }*/

    public void readProperties(/*Bundle bundle, */BundleResource bpath, SGPropertyNode startnode) throws SGException {
        readProperties(bpath, null, startnode, 0, false, false);
    }

    public void readProperties(BundleResource bpath, SGPropertyNode startnode, int default_mode) throws SGException {
        readProperties(bpath, null, startnode, default_mode, false, false);
    }

    private void readProperties(String file, SGPropertyNode startnode, int default_mode, boolean extended) throws SGException {
        readProperties(null, /*7.7.21FileSystem*/BundleResource.buildFromFullString(file), startnode, default_mode, extended, false);
    }

    public void readProperties(BundleResource bpath, SGPropertyNode startnode, int default_mode, boolean extended) throws SGException {
        readProperties(bpath, null, startnode, default_mode, extended, false);
    }

    public void readProperties(/*Bundle bundle, */BundleResource bpath,/*String*/NativeResource file, SGPropertyNode startnode, int default_mode, boolean extended, boolean readbyecs) throws SGException {
        try {
            if (bpath == null && file == null) {
                throw new SGException("invalid parameter");
            }
            String xmlbuf = null;
            if (bpath != null) {
                if (bpath.bundle == null) {
                    throw new SGException("no bundle in bpath");
                }
                BundleData data = bpath.bundle.getResource(bpath);
                if (data == null) {
                    throw new SGException("no data for bundle resource " + bpath);
                }
               /*17.10.24 don't care  if (!data.isText()) {
                    throw new SGException("no text data in bpath");
                }*/
                xmlbuf = bpath.bundle.getResource(bpath).getContentAsString();
            } else {
                Util.nomore();
                //16.10.18 xmlbuf = StringUtils.buildString(Platform.getInstance().loadResourceSync(/*new FileSystemResource(file))*/file).readFully());
            }
            if (xmlbuf == null) {
                throw new SGException("xml not found:" + ((file != null) ? file.getFullName() : bpath.getFullName()));
            }
            readProperties( xmlbuf,  bpath, file,  startnode,  default_mode,  extended,  readbyecs);
        } catch (java.lang.Exception e) {
            //8.10.18: Warum genau ist hier der catch? Wegen NPE oder aehnlich? Ist irgendwie doch doof, weil es auch einen Stacktrace gibt.
            //Stacktrace mal nicht bei SGException
            if (e instanceof SGException) {
                // 8.10.18: das wurde dann schon gelogged. Darum gar nicht mehr.
                //logger.error("readProperties failed for " + bpath.getFullName() + ": " + e.getMessage());
            } else {
                logger.error("readProperties failed for " + bpath.getFullName() + ": " + e.getMessage(), e);
            }
            throw new SGException("readProperties failed : " + e.getMessage(), e);
        }
    }

    private void readProperties(String xmlbuf, BundleResource bpath,/*String*/NativeResource file, SGPropertyNode startnode, int default_mode, boolean extended, boolean readbyecs) throws SGException {
        try {
            NativeDocument doc = Platform.getInstance().parseXml(xmlbuf);
            if (!doc.getNodeName().equals("PropertyList")) {
                throw new SGException("must start with PropertyList");
            }
            readNode(bpath, null, 0, doc, startnode, (file == null) ? null : file.getPath()/*new SGPath(file)*/, extended, readbyecs, (file == null) ? "" : file.getFullName());
        } catch (java.lang.Exception e) {
            //8.10.18: Warum genau ist hier der catch? Wegen NPE oder aehnlich? Ist irgendwie doch doof, weil es auch einen Stacktrace gibt.
            //Stacktrace mal nicht bei SGException
            if (e instanceof SGException) {
                // 8.10.18: das wurde dann schon gelogged. Darum gar nicht mehr.
                //logger.error("readProperties failed for " + bpath.getFullName() + ": " + e.getMessage());
            } else {
                logger.error("readProperties failed for " + bpath.getFullName() + ": " + e.getMessage(), e);
            }
            throw new SGException("readProperties failed : " + e.getMessage(), e);
        }
    }

    /**
     * siehe Pendant props_io.cxx:PropsVisitor
     *
     * @param node
     * @param destnode
     */
    void readNode(/*Bundle bundle,*/ BundleResource bpath, State parentstate, int level, NativeNode node, SGPropertyNode destnode, /*SGPath*/ResourcePath basepath, boolean extended, boolean readbyecs, String resourcenamefuerlogging) throws SGException {
        State state = startElement(bpath, parentstate, level, destnode, node.getNodeName(), new XMLAttributes(((NativeElement) node).getAttributes()), (NativeElement) node, basepath, extended, readbyecs, resourcenamefuerlogging);

        if (!state.omit) {
            //destnode.setName(node.getNodeName());
            //destnode.setValue(node.getNodeValue());
            String val = node.getNodeValue();
            NativeNodeList childlist = node.getChildNodes();
            // Wenn es 1 "#text" Child gibt, wird es der Value der Node sein.
            if (childlist.getLength() == 1 && childlist.getItem(0).getNodeName().equals("#text")) {
                val = childlist.getItem(0).getNodeValue();
            }
            for (int i = 0; i < childlist.getLength(); i++) {
                // NativeNode nn = node.getChildNodes().getItem(i);
                NativeElement nn = (NativeElement) node.getChildNodes().getItem(i);
                if (nn.getNodeName().equals("#text")) {
                    // Wahrscheinlich nur whitespaces. Pruefen? (TODO)
                } else {
                    if (nn.getNodeName().equals("#comment")) {
                        //ignore
                    } else {
                        //SGPropertyNode pnode = new SGPropertyNode(nn.getNodeName(), nn.getNodeValue());

                        //destnode.addChild(pnode);

                        readNode(bpath, state, level + 1, nn, state.node, basepath, extended, readbyecs, resourcenamefuerlogging);
                        state.haschildren = true;
                    }
                }
            }

            if (val == null) {
                // like FG
                val = "";
            }
            if (level > 0) {
                // Nicht versuchen, die target Node zu beenden, denn die ist ja nur der Einhängpunkt.
                endElement(level, state, val);
            }
        }
    }

    /**
     * aus props_io.cxx:PropsVisitor
     */
    private State startElement(BundleResource bpath, State parentstate, int _level, SGPropertyNode target, String name, XMLAttributes atts, NativeElement element, /*SGPath*/ResourcePath basepath, boolean extended, boolean readbyecs, String resourcenamefuerlogging) throws SGException {
        String attval = null;
        // const sg_location location (getPath(), getLine(), getColumn());
        // 10.4.17: Mit Bundle mal wieder ueber basepath die Location ermitteln 
        //String location = resourcenamefuerlogging;//basepath.str();
        String location;
        if (bpath != null) {
            location = bpath.getFullName();
        } else {
            location = basepath.getPath();
        }
        locations.add(location);

        if (_level == 0) {
            if (!name.equals("PropertyList")) {
                String message = "Root element name isType ";
                message += name;
                message += "; expected PropertyList";
                throw new SGException(message/*, location, "SimGear Property Reader"*/);
            }

            // Check for an include.
            attval = atts.getValue("include");
            if (attval != null) {
                //  try {
                /*SGPath*/
                NativeResource path;
                if (bpath != null) {
                    BundleResource includepath = FgBundleHelper.findPath(attval, bpath);
                    readProperties(includepath, null/*.str()*/, target/*_root*/, 0, extended, readbyecs);
                } else {
                    Util.nomore();
                    /*30.9.19path = SgResourceManager.getInstance().findPath(attval, /*SGPath(_base)* /basepath/*.dir()* /);
                    if (path == null/*.isNull()* /) {
                        String message = "Cannot open include file ";
                        message += attval + " with basepath " + basepath;
                        throw new SGIOException(message/*, location,                                "SimGear Property Reader"* /);
                    }
                    readProperties(null, path/*.str()* /, target/*_root* /, 0, extended, readbyecs);*/
                }

                //} catch (SGIOException e){
                //   setException(e);
                // }
            }

            State state = new State();
            state.node = target;
            state.type = null;
            //state.haschildren = element.getChildNodes().getLength()>0;

            return state;

            //push_state(_root, "", DEFAULT_MODE);
        } else {
            /* State &st = state();
             */
            // Get the index.
            attval = atts.getValue("n");

            int index = 0;
            String strName = name;//C# new String(name);
            State st = parentstate;
            if (attval != null) {
                index = Integer.parseInt(attval);
                //TODO max prüpfuen
                st.setcounters(strName, /*SG_MAX2*/Math.max(st.getcounters(strName), index + 1));
            } else {
                index = st.getcounters(strName);
                st.inccounters(strName);
                //31.5.16 Nicht selber zaehlen
                //09.06.16: Damit wird aber z.B. sim mehrfach angelegt. Das ist Mumpitz. 
                //TODO pruefen, wofuer dieser counter ist. Was soll denn passieren, wenn eine node kein "n=" hat. Immer neu anhaengen? 
                //Fuer model muss das so sein, fuer sim nicht! Evtl. darf ich nicht ueber die children gehen, sondern nur ueber die, die ich in diesem Einlesezyklus angelegt
                //habe. Ja, das ist vielversprechend.
                //index = target.getChildren(strName).size();
            }

            if (name.equals("sim")) {
                index = index;
            }

            // Got the index, so grab the node.
            //logger.debug("locating "+strName+" in "+target.name+" at "+index);
            SGPropertyNode node = /*st.*/target.getChild(strName, index, true);
            if (!node.getAttribute(Props./*SGPropertyNode::*/WRITE)) {
                //TODO SG_LOG(SG_INPUT, SG_ALERT, "Not overwriting write-protected property "                        << node->getPath(true) << "\n at " << location.asString());
                node = null;//&null;
            }

            // TODO use correct default mode (keep for now to match past behavior)
            int mode = /*TODO _default_mode |*/ Props.READ | Props.WRITE;
            /*boolean*/
            int omit = 0;//false;
            String type = "unspecified";

            SGPropertyNode attr_node = null;

            for (int i = 0; i < atts.getLength(); ++i) {
                NativeNode att = atts.getItem(i);
                String att_name = att.getNodeName();
                String val = att.getNodeValue();

                // Get the access-mode attributes,
                // but don't set yet (in case they
                // prevent us from recording the value).
                if (att_name.equals("read"))
                    mode = setFlag(mode, Props.READ, val, location);
                else if (att_name.equals("write"))
                    mode = setFlag(mode, Props.WRITE, val, location);
                else if (att_name.equals("archive"))
                    mode = setFlag(mode, Props.ARCHIVE, val, location);
                else if (att_name.equals("trace-read"))
                    mode = setFlag(mode, Props.TRACE_READ, val, location);
                else if (att_name.equals("trace-write"))
                    mode = setFlag(mode, Props.TRACE_WRITE, val, location);
                else if (att_name.equals("userarchive"))
                    mode = setFlag(mode, Props.USERARCHIVE, val, location);
                else if (att_name.equals("preserve"))
                    mode = setFlag(mode, Props.PRESERVE, val, location);

                    // Check for an alias.
                else if (att_name.equals("alias")) {
                    if (!node.alias(val))
                        logger.error(/* SG_LOG (SG_INPUT,  SG_ALERT,*/ "Failed to set alias to " + val + "\n at " + location);
                }

                // Check for an include.
                else if (att_name.equals("include")) {
                    //try {
                    /*SGPath*/
                    if (bpath != null) {
                        BundleResource includepath = FgBundleHelper.findPath(val, bpath);
                        if (includepath == null) {
                            logger.error("Failed to resolve " + val + " in " + bpath);
                        } else {
                            readProperties(includepath, null/*.str()*/, node/*_root*/, 0, extended, readbyecs);
                        }
                    } else {

                        NativeResource path = SgResourceManager.getInstance().findPath(val, /*SGPath(_base)*/basepath/*.dir()*/);
                        if (path == null/*.isNull()*/) {
                            String message = "Cannot open att include file ";
                            message += val + " with basepath " + basepath;
                            throw new SGIOException(message + " at " + location/*, location, "SimGear Property Reader"*/);
                        }
                        readProperties(null, path/*.str()*/, node, 0, extended, readbyecs);
                    }
                   /* } catch (sg_io_exception&e)
                    {
                        setException(e);
                    }*/
                } else if (att_name.equals("omit-node")) {
                    //TODO remove node in endelement
                    omit = setFlag(omit, 1, val, location);
                } else if (att_name.equals("type")) {
                    type = val;//atts.getValue(i);

                    // if a type isType given and the node isType tied,
                    // don't clear the value because
                    // clearValue() unties the property
                    /*TODO if( !node->isTied() )
                        node->clearValue();*/
                } else if (!att_name.equals("n")) {
                    // Store all additional attributes in a special node named _attr_
                   /*TODO  if( !attr_node )
                        attr_node = node->getChild(ATTR, 0, true);

                    attr_node->setUnspecifiedValue(att_name.c_str(), val.c_str());*/
                }
            }
            State state = new State();
            state.node = node;
            state.type = type;
            //state.haschildren = element.getChildNodes().getLength() > 0;
            state.omit = omit != 0;

            return state;
            //push_state(node, type, mode, omit);

        }
    }

    /**
     * Auch aus PropsVisitor
     */
    private void endElement(int _level, State st, String _data) {
        //State &st = state();
        boolean ret = false;
        //const sg_location location(getPath(), getLine(), getColumn());*/
        boolean _extended = false;//TODO
        // If there are no children and it's
        // not an alias, then it's a leaf value.
        if (!st.hasChildren() && !st.node.isAlias()) {
            if (st.type == null) {
                ret = ret;
            }
            if (st.type.equals("bool")) {
                if (_data.equals("true") || Util.atoi(_data) != 0)
                    ret = st.node.setBoolValue(true);
                else
                    ret = st.node.setBoolValue(false);
            } else if (st.type.equals("int")) {
                ret = st.node.setIntValue(Util.atoi(_data));
            } else if (st.type.equals("long")) {
                ret = st.node.setLongValue(Util.atol(_data/*, 0, 0*/));
            } else if (st.type.equals("float")) {
                ret = st.node.setFloatValue((float) Util.atof(_data));
            } else if (st.type.equals("double")) {
                ret = st.node.setDoubleValue(Util.atof(_data/*, 0*/));
            } else if (st.type.equals("string")) {
                ret = st.node.setStringValue(_data);
            } else if (st.type.equals("vec3d") /*&& _extended*/) {
                ret = st.node.setVector3Value(Util.parseVector3(_data));
            } else if (st.type.equals("vec4d") /*&& _extended*/) {
                ret = st.node.setColorValue(Color.parseString(_data));
            } else if (st.type.equals("unspecified")) {
                if (st.node.name.equals("path")) {
                    ret = ret;
                }
                ret = st.node.setUnspecifiedValue(_data);
            } else if (_level == 1) {
                ret = true;        // empty <PropertyList>
            } else {
                String message = "Unrecognized data type '";
                message += st.type;
                message += '\'';
                // FIXME: add location information
                //TODO error handling
                throw new RuntimeException(new SGIOException(message/*, location, "SimGear Property Reader"*/));
            }
            if (!ret) {
                Util.notyet();
            /*
                SG_LOG                        (                                SG_INPUT,
                                SG_ALERT,                                "readProperties: Failed to set " << st.node->getPath()
                << " to value \"" << _data
                << "\" with type " << st.type
                << "\n at " << location.asString()            );
                */
            }
        }

        // Set the access-mode attributes now,
        // once the value has already been
        // assigned.
       /* st.node->setAttributes(st.mode);

        if (st.omit) {
            State &parent = _state_stack[_state_stack.size() - 2];
            int nChildren = st.node->nChildren();
            for (int i = 0; i < nChildren; i++) {
                SGPropertyNode *src = st.node->getChild(i);
                const char *name = src->getName();
                int index = parent.counters[name];
                parent.counters[name]++;
                SGPropertyNode *dst = parent.node->getChild(name, index, true);
                copyProperties(src, dst);
            }
            parent.node->removeChild(st.node->getName(), st.node->getIndex());
        }*/
        // pop_state();
    }

    /**
     * Set/unset a yes/no flag.
     */
    private int setFlag(int mode, int mask, String val, String/*const sg_location&*/ location) throws SGIOException {
        char flag = StringUtils.charAt(val, 0);
        if (flag == 'y')
            mode |= mask;
        else if (flag == 'n')
            mode &= ~mask;
        else {
            String message = "Unrecognized flag value '";
            message += flag;
            message += '\'';
            // FIXME: add location info
            throw new SGIOException(message/*, location, "SimGear Property Reader"*/);
        }
        return mode;
    }


    /**
     * Copy one property tree to another.
     *
     * @param inprop  The source property tree.
     * @param outprop The destination property tree.
     * @return true if all properties were copied, false if some failed
     * (for example, if the property's value isType tied read-only).
     */

    public static boolean copyProperties(SGPropertyNode inprop, SGPropertyNode outprop) {
        boolean retval = copyPropertyValue(inprop, outprop);
        if (!retval) {
            return false;
        }

        // copy the attributes.
        outprop.setAttributes(inprop.getAttributes());

        // Next, copy the children.
        int nChildren = inprop.nChildren();
        for (int i = 0; i < nChildren; i++) {
            SGPropertyNode in_child = inprop.getChild(i);
            SGPropertyNode out_child = outprop.getChild(in_child.getNameString(),
                    in_child.getIndex(),
                    false);
            if (out_child == null) {
                out_child = outprop.getChild(in_child.getNameString(),
                        in_child.getIndex(),
                        true);
            }

            if (out_child != null && !copyProperties(in_child, out_child))
                retval = false;
        }

        return retval;
    }

    public static boolean copyPropertyValue(SGPropertyNode inprop, SGPropertyNode outprop) {
        boolean retval = true;

        if (!inprop.hasValue()) {
            return true;
        }

        switch (inprop.getType()) {
            case Props.BOOL:
                if (!outprop.setBoolValue(inprop.getBoolValue()))
                    retval = false;
                break;
            case Props.INT:
                if (!outprop.setIntValue(inprop.getIntValue()))
                    retval = false;
                break;
            case Props.LONG:
                if (!outprop.setLongValue(inprop.getLongValue()))
                    retval = false;
                break;
            case Props.FLOAT:
                if (!outprop.setFloatValue(inprop.getFloatValue()))
                    retval = false;
                break;
            case Props.DOUBLE:
                if (!outprop.setDoubleValue(inprop.getDoubleValue()))
                    retval = false;
                break;
            case Props.STRING:
                if (!outprop.setStringValue(inprop.getStringValue()))
                    retval = false;
                break;
            case Props.UNSPECIFIED:
                if (!outprop.setUnspecifiedValue(inprop.getStringValue()))
                    retval = false;
                break;
            case Props.VEC3D:
                if (!outprop.setVector3Value(inprop.getVector3Value/*< Vector3 >*/()))
                    retval = false;
                break;
            case Props.COLOR:
                if (!outprop.setColorValue(inprop.getColorValue/*< Color >*/()))
                    retval = false;
                break;
            default:
                if (inprop.isAlias())
                    break;
                String message = "Unknown internal SGPropertyNode type";
                message += inprop.getType();
                throw new RuntimeException(message + "SimGear Property Reader");
        }

        return retval;
    }

}

class State {
    public SGPropertyNode node;
    public String type;
    public boolean haschildren;
    public HashMap<String, Integer> counters = new HashMap<String, Integer>();
    public boolean omit;

    public boolean hasChildren() {
        return haschildren;
    }

    public int getcounters(String strName) {
        Integer i = counters.get(strName);
        if (i == null) {
            counters.put(strName, 0);
            return 0;
        }
        return (int) i;
    }

    public void setcounters(String strName, int v) {
        counters.put(strName, v);
    }

    public void inccounters(String strName) {
        int i = getcounters(strName);
        setcounters(strName, i + 1);
    }
}

class XMLAttributes {
    NativeAttributeList nalist;

    public XMLAttributes(NativeAttributeList nalist) {
        this.nalist = nalist;
    }


    public String getValue(String attr) {
        NativeNode node = nalist.getNamedItem(attr);
        if (node == null) {
            return null;
        }
        return node.getNodeValue();
    }

    public int getLength() {
        return nalist.getLength();
    }

    public NativeNode getItem(int i) {
        return nalist.getItem(i);
    }
}