package de.yard.threed.flightgear.core.simgear;

import de.yard.threed.core.Util;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.flightgear.core.PropertyList;
import de.yard.threed.flightgear.core.simgear.props.Props;
import de.yard.threed.flightgear.core.simgear.props.SGPropertyChangeListener;
import de.yard.threed.flightgear.core.simgear.props.SGRaw;
import de.yard.threed.flightgear.core.simgear.props.SGRawValue;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.Color;
import de.yard.threed.core.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A node in a property tree.
 * <p/>
 * Created by thomass on 04.12.15.
 */
public class SGPropertyNode {
    //static wegen ressourcenverbrauch
    static Log logger = Platform.getInstance().getLog(SGPropertyNode.class);

    //28.5.16 String value;
    //17.1.18: children erst bei Bedarf initialisieren
    List<SGPropertyNode> children; 
    public String name;
    private int index;
    /*Type*/ int _type;
    private String str;
    int _attr;
    boolean _tied;
    int _index;
    /// To avoid cyclic reference counting loops this shall not be a reference
    /// counted pointer
    SGPropertyNode _parent;

    ValueUnion _value;
    LocalValUnion _local_val;
    List<SGPropertyChangeListener> _listeners;

    /**
     * Default constructor: always creates a root node.
     */
    public SGPropertyNode() {
        _index = 0;
        _parent = null;
        _type = Props.NONE;
        _tied = false;
        _attr = Props.READ | Props.WRITE;
        _listeners = null;
        //_local_val = new LocalValUnion();
        //_local_val.string_val = null;
        //_value = new ValueUnion();
        //_value.val = null;
    }

    public SGPropertyNode(String name) {
        this();
        this.name = name;
    }

    public SGPropertyNode(String name, String value) {
        this();
        this.name = name;
        this._local_val = new LocalValUnion();
        _local_val.string_val = value;
        _type = Props.STRING;
    }

    public SGPropertyNode(String name, int index) {
        this();
        this.name = name;
        this.index = index;
    }

    public SGPropertyNode addChild(SGPropertyNode node) {
        if (children==null){
            children= new ArrayList<SGPropertyNode>();
        }
        children.add(node);
        node._parent = this;
        return node;
    }


    public void setName(String name) {
        this.name = name;
    }

    public boolean hasChild(String childname) {
        return (getChild(childname, 0) != null);
    }

    /**
     * Get a child node by position (*NOT* index).
     */
    public SGPropertyNode getChild(int position) {
        if (position >= 0 && position < nChildren())
            return children.get(position);
        else
            return null;
    }

    /**
     * Get a const child by name and index.
     */
    public SGPropertyNode getChild(String name, int index) {
        return getChild(name, index, false);
    }

    public SGPropertyNode getChild(String name) {
        return getChild(name, 0, false);
    }

    public SGPropertyNode getChild(String name, int index, boolean create) {
        //SGPropertyNode* node = getExistingChild(name.begin(), name.end(), index);
        int pos = find_child(name/*, name + strlen(name)*/, index, children);
        if (pos >= 0)
            return children.get(pos);
        else {
            if (create) {
                SGPropertyNode node = new SGPropertyNode(name, index/*, this*/);
                node._parent = this;
                if (children==null){
                    children= new ArrayList<SGPropertyNode>();
                }
                children.add(node);
                fireChildAdded(node);
                return node;
            } else {
                return null;
            }
        }
    }

    boolean compare_strings(String s1, String s2) {
        // return !strncmp(s1, s2, SGPropertyNode::MAX_STRING_LEN);
        return s1.equals(s2);
    }

    /**
     * Locate a child node by name and index.
     * <p/>
     * Purpose of "Itr" isType unclear.
     */
    static int find_child(String name/*Itr begin, Itr end*/, int index, List<SGPropertyNode> nodes) {
        if (nodes==null){
            return -1;
        }
        int nNodes = nodes.size();
        //  boost::iterator_range<Itr> name(begin, end);
        for (int i = 0; i < nNodes; i++) {
            SGPropertyNode node = nodes.get(i);

            // searching for a matching index isType a lot less time consuming than
            // comparing two strings so do that getFirst.
            if (node.getIndex() == index && node.getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public String getNameString ()  {
        return name;
    }
    
    /**
     * Alias to another node.
     */
    boolean alias(SGPropertyNode target) {
        if (target != null && (_type != Props.ALIAS) && (!_tied)) {
            clearValue();
            //?? get(target);
            if (_value == null) {
                _value = new ValueUnion();
            }
            _value.alias = target;
            _type = Props.ALIAS;
            return true;
        }

        //#if PROPS_STANDALONE
        //#else
        if (target == null) {
            logger.error("Failed to create alias for " + getPath() + ". " + "The target property does not exist.");
        } else if (_type == Props.ALIAS) {
            if (_value.alias == target)
                return true; // ok, identical alias requested
            logger.error("Failed to create alias at " + target.getPath() + ". " + "Source " + getPath() + " isType already aliasing another property.");
        } else if (_tied) {
            logger.error("Failed to create alias at " + target.getPath() + ". " + "Source " + getPath() + " isType a tied property.");
        }
        //#endif

        return false;
    }

    /**
     * Alias to another node by path.
     */
    public boolean alias(String path) {
        return alias(getNode(path, true));
    }

    public String getDisplayName(boolean simplify) {
        String display_name = name;
        if (_index != 0 || !simplify) {
            //stringstream sstr;
            //sstr << '[' << _index << ']';
            display_name += "[" + _index + "]";
        }
        return display_name;
    }

    public String getPath() {
        return getPath(false);
    }

    public String getPath(boolean simplify) {
        //typedef std::vector<SGConstPropertyNode_ptr> PList;
        //PList pathList;
        String result = getDisplayName(simplify);
        SGPropertyNode node = this;
        while (node._parent != null) {
            node = node._parent;
            result = node.getDisplayName(simplify) + "/" + result;
        }
       /* pathList.push_back(node);
        std::string result;
        for (PList::reverse_iterator itr = pathList.rbegin(),
        rend = pathList.rend();
        itr != rend;
        ++itr) {
        result += '/';
        result += (*itr)->getDisplayName(simplify);*/

        return result;
    }

    /**
     * Test whether another node has a value attached.
     */
    public boolean hasValue(String relative_path) {
        SGPropertyNode node = getNode(relative_path);
        return (node == null) ? false : node.hasValue();
    }

    /**
     * Test whether this node contains a primitive leaf value.
     */
    public boolean hasValue() {
        return (_type != Props.NONE);
    }

    public SGPropertyNode getNode(String relative_path) {
        return getNode(relative_path, false);
    }

    /**
     * Origin and exact purpose of make_iterator_range isType unclear.
     *
     * @param relative_path
     * @param create
     * @return
     */
    public SGPropertyNode getNode(String relative_path, boolean create) {
        return find_node(this, relative_path/*make_iterator_range(relative_path, relative_path
                        + strlen(relative_path))*/, create, -1);
    }

    public SGPropertyNode getNode(String relative_path, int index, boolean create) {
        //using namespace boost;
        return find_node(this, relative_path/*make_iterator_range(relative_path, relative_path
                        + strlen(relative_path))*/, create, index);
    }

    // Internal function for parsing property paths. last_index provides
// and index value for the last node name token, if supplied.
    //template<typename Range>
    static SGPropertyNode find_node(SGPropertyNode current,              /* const Range&*/String path, boolean create, int last_index) {
        //using namespace boost;
        //typedef split_iterator<typename range_result_iterator<Range>::type>            PathSplitIterator;
        PathSplitIterator itr = new PathSplitIterator(path);/*make_split_iterator(path, first_finder("/", is_equal()));*/
        if (StringUtils.startsWith(path,"/"))
            return find_node_aux(current.getRootNode(), itr, create, last_index);
        else
            return find_node_aux(current, itr, create, last_index);
    }


    /**
     * Die "PF Logik" ist undurchsichtig.
     *
     * @param current
     * @param itr
     * @param create
     * @param last_index
     * @return
     */
    //template<typename SplitItr>
    static SGPropertyNode find_node_aux(SGPropertyNode current, /*SplitItr&*/Iterator<String> itr, boolean create, int last_index) {
        //typedef typename SplitItr::value_type Range;
        // Run off the end of the list
        if (current == null) {
            return null;
        }

        // Success! This isType the one we want.
        if (!itr.hasNext()/*eof()*/)
            return current;
        /*Range*/
        String token = /***/itr.next();
        // Empty name at this point isType empty, not root.
        if (StringUtils.empty(token)/*token.trim().length() == 0/*empty()*/)
            return find_node_aux(current, /*++*/itr, create, last_index);
        //Range name = parse_name(current, token);
        String name = token;
        if (name.equals("."))
            return find_node_aux(current, /*++*/itr, create, last_index);
        if (name.equals("..")) {
            SGPropertyNode parent = current.getParent();
            if (parent == null)
                throw new RuntimeException("attempt to move past root with '..'");
            return find_node_aux(parent, /*++*/itr, create, last_index);
        }
        int index = -1;
        if (last_index >= 0) {
            // If we are at the last token and last_index isType valid, use
            // last_index as the index value
            boolean lastTok = true;
            //while (!(++itr).eof()) {
            //    if (!itr->empty()) {
            //Das ist doch nicht richtig? wie unten itr.next();
            while (itr.hasNext()) {
                if (!StringUtils.empty(itr.next())) {
                    lastTok = false;
                    break;
                }
            }
            if (lastTok)
                index = last_index;
        } else {
            //++itr;
            /*Das ist doch nicht richtig? Der wurde doch schon oben hochgesetzt if (itr.hasNext()) {
                itr.next();
            }*/
        }

        if (index < 0) {
            index = 0;
            // if (name.end() != token.end()) {
            /*if (*name.end() == '[') {
                    typename Range::iterator i = name.end() + 1, end = token.end();
                    for (;i != end; ++i) {
                        if (isdigit(*i)) {
                            index = (index * 10) + (*i - '0');
                        } else {
                            break;
                        }
                    }
                    if (i == token.end() || *i != ']')
                    throw std::string("unterminated index (looking for ']')");
                } else {
                    throw std::string("illegal characters in token: ")
                            + std::string(name.begin(), name.end());
                }
            }*/
            int li = StringUtils.indexOf(name, '[');
            if (li != -1) {
                int ri = StringUtils.indexOf(name, ']');
                if (ri == -1) {
                    //TODO verbessern?
                    throw new RuntimeException("unterminated index (looking for ']')");
                }
                index = Util.atoi(StringUtils.substring(name, li + 1, ri));
                name = StringUtils.substring(name, 0, li);
                if (name.equals("viewer")) {
                    index = index;
                }
            }
        }
        return find_node_aux(current.getChild/*Impl*/(name/*.begin(), name.end()*/,
                index, create), itr, create, last_index);
    }

    public SGPropertyNode getRootNode() {
        SGPropertyNode node = this;
        while (node._parent != null) {
            node = node._parent;
        }
        return node;
    }

    public SGPropertyNode getParent() {
        return _parent;
    }

    /**
     * Check a single mode attribute for the property node.
     */
    public boolean getAttribute(int attr) {
        return ((_attr & attr) != 0);
    }

    /**
     * Set a single mode attribute for the property node.
     */
    /*void setAttribute (Attribute attr, bool state) {
        (state ? _attr |= attr : _attr &= ~attr);
    }*/


    /**
     * Get all of the mode attributes for the property node.
     */
    public int getAttributes() {
        return _attr;
    }


    /**
     * Set all of the mode attributes for the property node.
     */
    public void setAttributes(int attr) {
        _attr = attr;
    }

    //////////////////////

    private int getInt() {
        if (_tied)
            return (int) ((SGRawValue<Integer>) _value.val).getValue();
        else
            return _local_val.int_val;
    }

    private long getLong() {
        if (_tied)
            return (long) ((SGRawValue<Long>) _value.val).getValue();
        else
            return _local_val.long_val;
    }

    private String getString() {
        if (_tied)
            return ((SGRawValue<String>) _value.val).getValue();
        else
            return _local_val.string_val;
    }

    public boolean getBool() {
        if (_tied)
            return (boolean)((SGRawValue<Boolean>) _value.val).getValue();
        else
            return _local_val.bool_val;
    }

    private float getFloat() {
        if (_tied)
            return (float) ((SGRawValue<Float>) _value.val).getValue();
        else
            return _local_val.float_val;
    }


    private double getDouble() {
        if (_tied)
            return (double) ((SGRawValue<Double>) _value.val).getValue();
        else
            return _local_val.double_val;
    }

    private Vector3 getVector3() {
        if (_tied)
            return (Vector3) ((SGRawValue<Vector3>) _value.val).getValue();
        else
            return _local_val.vec3_val;
    }

    private Color getColor() {
        if (_tied)
            return (Color) ((SGRawValue<Color>) _value.val).getValue();
        else
            return _local_val.color_val;
    }
    
    ////////////////////

    boolean setString(String val) {
        if (_tied) {
            if (/*static_cast<SGRawValue<const char*>*>*/((SGRawValue<String>) _value.val).setValue(val)) {
                fireValueChanged();
                return true;
            } else {
                return false;
            }
        } else {
            // delete [] _local_val.string_val;
            // _local_val.string_val = copy_string(val);
            if (_local_val == null) {
                _local_val = new LocalValUnion();
            }
            _local_val.string_val = val;
            fireValueChanged();
            return true;
        }
    }

    boolean setInt(int val) {
        if (_tied) {
            if (/*static_cast< SGRawValue <int>*>*/((SGRawValue<Integer>) _value.val).setValue(val)) {
                fireValueChanged();
                return true;
            } else {
                return false;
            }
        } else {
            _local_val.int_val = val;
            fireValueChanged();
            return true;
        }
    }

    boolean setLong(long val) {
        if (_tied) {
            if (/*static_cast< SGRawValue <int>*>*/((SGRawValue<Long>) _value.val).setValue(val)) {
                fireValueChanged();
                return true;
            } else {
                return false;
            }
        } else {
            if (_local_val == null) {
                _local_val = new LocalValUnion();
            }
            _local_val.long_val = val;
            fireValueChanged();
            return true;
        }
    }

    boolean setFloat(float val) {
        if (_tied) {
            if (/*static_cast< SGRawValue <int>*>*/((SGRawValue<Float>) _value.val).setValue(val)) {
                fireValueChanged();
                return true;
            } else {
                return false;
            }
        } else {
            if (_local_val == null) {
                _local_val = new LocalValUnion();
            }
            _local_val.float_val = val;
            fireValueChanged();
            return true;
        }
    }

    boolean setDouble(double val) {
        if (_tied) {
            if (/*static_cast< SGRawValue <int>*>*/((SGRawValue<Double>) _value.val).setValue(val)) {
                fireValueChanged();
                return true;
            } else {
                return false;
            }
        } else {
            if (_local_val == null) {
                _local_val = new LocalValUnion();
            }
            _local_val.double_val = val;
            fireValueChanged();
            return true;
        }
    }

    boolean setBool(boolean val) {
        if (_tied) {
            if (/*static_cast< SGRawValue <int>*>*/((SGRawValue<Boolean>) _value.val).setValue(val)) {
                fireValueChanged();
                return true;
            } else {
                return false;
            }
        } else {
            if (_local_val == null) {
                _local_val = new LocalValUnion();
            }
            _local_val.bool_val = val;
            fireValueChanged();
            return true;
        }
    }

    boolean setColor(Color val) {
        if (_tied) {
            if (/*static_cast< SGRawValue <int>*>*/((SGRawValue<Color>) _value.val).setValue(val)) {
                fireValueChanged();
                return true;
            } else {
                return false;
            }
        } else {
            if (_local_val == null) {
                _local_val = new LocalValUnion();
            }
            _local_val.color_val = val;
            fireValueChanged();
            return true;
        }
    }

    boolean setVector3(Vector3 val) {
        if (_tied) {
            if (/*static_cast< SGRawValue <int>*>*/((SGRawValue<Vector3>) _value.val).setValue(val)) {
                fireValueChanged();
                return true;
            } else {
                return false;
            }
        } else {
            if (_local_val == null) {
                _local_val = new LocalValUnion();
            }
            _local_val.vec3_val = val;
            fireValueChanged();
            return true;
        }
    }

    ///////////

    void clearValue() {
        if (_type == Props.ALIAS) {
            //TODO put(_value.alias);
            _value.alias = null;
        } else if (_type != Props.NONE) {
            switch (_type) {
                case Props.BOOL:
                    _local_val.bool_val = false;//SGRawValue<bool>::DefaultValue();
                    break;
                case Props.INT:
                    _local_val.int_val = 0;//SGRawValue<int>::DefaultValue();
                    break;
                case Props.LONG:
                    _local_val.long_val = 0;//SGRawValue<long>::DefaultValue();
                    break;
                case Props.FLOAT:
                    _local_val.float_val = 0;//SGRawValue<float>::DefaultValue();
                    break;
                case Props.DOUBLE:
                    _local_val.double_val = 0;//SGRawValue<double>::DefaultValue();
                    break;
                case Props.STRING:
                case Props.UNSPECIFIED:
                    if (!_tied) {
                        //delete [] _local_val.string_val;
                    }
                    _local_val.string_val = null;
                    break;
                default: // avoid compiler warning
                    break;
            }
            //delete _value.val;
            if (_value != null) {
                _value.val = null;
            }
        }
        _tied = false;
        _type = Props.NONE;
    }


    /**
     * Get the value as a string.
     */
    String make_string() {
        if (!getAttribute(Props.READ))
            return "";
        switch (_type) {
            case Props.ALIAS:
                return _value.alias.getStringValue();
            case Props.BOOL:
                return getBool() ? "true" : "false";
            case Props.STRING:
            case Props.UNSPECIFIED:
                return getString();
            case Props.NONE:
                return "";
            default:
                break;
        }
        String sstr = "";
        switch (_type) {
            case Props.INT:
                sstr += getInt();
                break;
            case Props.LONG:
                sstr += getLong();
                break;
            case Props.FLOAT:
                sstr += getFloat();
                break;
            case Props.DOUBLE:
                sstr += /*std::setprecision(10) <<*/ getDouble();
                break;
            case Props.EXTENDED: {
                /*Props.Type*/
                int realType = _value.val.getType();
                // Perhaps this should be done for all types?
                if (realType == Props.VEC3D || realType == Props.COLOR/*VEC4D*/) {
                    //TODO    sstr.precision(10);
                }
                Util.notyet();
                //static_cast<SGRawExtended*>(_value.val)->printOn(sstr);
            }
            break;
            default:
                return "";
        }
        //_buffer = sstr.str();
        return sstr;//_buffer.c_str();
    }


    public int getType() {
        if (_type == Props.ALIAS)
            return _value.alias.getType();
        else if (_type == Props.EXTENDED)
            return _value.val.getType();
        else
            return _type;
    }

    public boolean isAlias() {
        return (_type == Props.ALIAS);
    }

    ///////////////
    public boolean getBoolValue() {
        // Shortcut for common case
        if (_attr == (Props.READ | Props.WRITE) && _type == Props.BOOL)
            return getBool();

        //if (getAttribute(TRACE_READ))
        //  trace_read();
        if (!getAttribute(Props.READ))
            return false;//SGRawValue<bool>::DefaultValue();
        switch (_type) {
            case Props.ALIAS:
                return _value.alias.getBoolValue();
            case Props.BOOL:
                return getBool();
            case Props.INT:
                return getInt() == 0 ? false : true;
            case Props.LONG:
                return getLong() == 0L ? false : true;
            case Props.FLOAT:
                return getFloat() == 0.0 ? false : true;
            case Props.DOUBLE:
                return getDouble() == 0.0 ? false : true;
            case Props.STRING:
            case Props.UNSPECIFIED:
                return (compare_strings(getString(), "true") || getDoubleValue() != 0.0);
            case Props.NONE:
            default:
                return false;//SGRawValue<bool>::DefaultValue();
        }
    }

    public int getIntValue() {
        // Shortcut for common case
        if (_attr == (Props.READ | Props.WRITE) && _type == Props.INT)
            return getInt();

        //if (getAttribute(TRACE_READ))
        //    trace_read();
        if (!getAttribute(Props.READ))
            return 0;//SGRawValue<int>::DefaultValue ();
        switch (_type) {
            case Props.ALIAS:
                return _value.alias.getIntValue();
            case Props.BOOL:
                return getBool() ? 1 : 0;
            case Props.INT:
                return getInt();
            case Props.LONG:
                //TODO overflow warning? 
                return (int) getLong();
            case Props.FLOAT:
                return (int) Math.round(getFloat());
            case Props.DOUBLE:
                return (int) Math.round(getDouble());
            case Props.STRING:
            case Props.UNSPECIFIED:
                return Integer.parseInt(getString());
            case Props.NONE:
            default:
                return 0;//SGRawValue<int>::DefaultValue ();

        }
    }

    public long getLongValue() {
        // Shortcut for common case
        if (_attr == (Props.READ | Props.WRITE) && _type == Props.LONG)
            return getLong();

        //if (getAttribute(TRACE_READ))
        //    trace_read();
        if (!getAttribute(Props.READ))
            return 0;//SGRawValue<int>::DefaultValue ();
        switch (_type) {
            case Props.ALIAS:
                return _value.alias.getLongValue();
            case Props.BOOL:
                return getBool() ? 1 : 0;
            case Props.INT:
                return getInt();
            case Props.LONG:
                return getLong();
            case Props.FLOAT:
                return (long) Math.round(getFloat());
            case Props.DOUBLE:
                return (long) Math.round(getDouble());
            case Props.STRING:
            case Props.UNSPECIFIED:
                return Long.parseLong(getString());
            case Props.NONE:
            default:
                return 0;//SGRawValue<int>::DefaultValue ();
        }
    }

    public float getFloatValue() {
        // Shortcut for common case
        if (_attr == (Props.READ | Props.WRITE) && _type == Props.FLOAT)
            return getFloat();

        //if (getAttribute(TRACE_READ))
        //    trace_read();
        if (!getAttribute(Props.READ))
            return 0;//SGRawValue<int>::DefaultValue ();
        switch (_type) {
            case Props.ALIAS:
                return _value.alias.getFloatValue();
            case Props.BOOL:
                return getBool() ? 1 : 0;
            case Props.INT:
                return getInt();
            case Props.LONG:
                return getLong();
            case Props.FLOAT:
                return getFloat();
            case Props.DOUBLE:
                return (float) getDouble();
            case Props.STRING:
            case Props.UNSPECIFIED:
                return Util.atof(getString());
            case Props.NONE:
            default:
                return 0;//SGRawValue<int>::DefaultValue ();
        }
    }

    public double getDoubleValue() {
        // Shortcut for common case
        if (_attr == (Props.READ | Props.WRITE) && _type == Props.DOUBLE)
            return getDouble();

        //if (getAttribute(TRACE_READ))
        //    trace_read();
        if (!getAttribute(Props.READ))
            return 0;//SGRawValue<int>::DefaultValue ();
        switch (_type) {
            case Props.ALIAS:
                return _value.alias.getDoubleValue();
            case Props.BOOL:
                return getBool() ? 1 : 0;
            case Props.INT:
                return getInt();
            case Props.LONG:
                return getLong();
            case Props.FLOAT:
                return getFloat();
            case Props.DOUBLE:
                return getDouble();
            case Props.STRING:
            case Props.UNSPECIFIED:
                return Util.atod(getString());
            case Props.NONE:
            default:
                return 0;//SGRawValue<int>::DefaultValue ();
        }
    }

    public String getStringValue() {
        // Shortcut for common case
        if (_attr == (Props.READ | Props.WRITE) && _type == Props.STRING)
            return getString();

        // if (getAttribute(TRACE_READ))
        //     trace_read();
        if (!getAttribute(Props.READ))
            return "";//SGRawValue<const char *>::DefaultValue();
        return make_string();
    }

    public Vector3 getVector3Value() {
        // Shortcut for common case
        if (_attr == (Props.READ | Props.WRITE) && _type == Props.VEC3D)
            return getVector3();

        // if (getAttribute(TRACE_READ))
        //     trace_read();
        if (!getAttribute(Props.READ))
            return null;//SGRawValue<const char *>::DefaultValue();
        return getVector3();
    }

    public Color getColorValue() {
        // Shortcut for common case
        if (_attr == (Props.READ | Props.WRITE) && _type == Props.COLOR)
            return getColor();

        // if (getAttribute(TRACE_READ))
        //     trace_read();
        if (!getAttribute(Props.READ))
            return null;//SGRawValue<const char *>::DefaultValue();
        return getColor();
    }

    //////////////////////////
    public boolean setBoolValue(boolean value) {
        // Shortcut for common case
        if (_attr == (Props.READ | Props.WRITE) && _type == Props.BOOL)
            return setBool(value);

        boolean result = false;
        //TODO  TEST_WRITE;
        if (_type == Props.NONE || _type == Props.UNSPECIFIED) {
            clearValue();
            _tied = false;
            _type = Props.BOOL;
        }

        switch (_type) {
            case Props.ALIAS:
                result = _value.alias.setBoolValue(value);
                break;
            case Props.BOOL:
                result = setBool(value);
                break;
            case Props.INT:
                result = setInt(value ? 1 : 0);
                break;
            case Props.LONG:
                result = setLong(value ? 1 : 0);
                break;
            case Props.FLOAT:
                result = setFloat(value ? 1 : 0);
                break;
            case Props.DOUBLE:
                result = setDouble(value ? 1 : 0);
                break;
            case Props.STRING:
            case Props.UNSPECIFIED:
                result = setString(value ? "true" : "false");
                break;
            case Props.NONE:
            default:
                break;
        }

        //if (getAttribute(TRACE_WRITE))
        //  trace_write();
        return result;
    }

    public boolean setStringValue(String value) {
        // Shortcut for common case
        if (_attr == (Props.READ | Props.WRITE) && _type == Props.STRING)
            return setString(value);

        boolean result = false;
        //TODO TEST_WRITE;
        if (_type == Props.NONE || _type == Props.UNSPECIFIED) {
            clearValue();
            _type = Props.STRING;
        }

        switch (_type) {
            case Props.ALIAS:
                result = _value.alias.setStringValue(value);
                break;
            case Props.BOOL:
                result = setBool((compare_strings(value, "true") || (Integer.parseInt(value) > 0) ? true : false));
                break;
            case Props.INT:
                result = setInt(Integer.parseInt(value));
                break;
            case Props.LONG:
                result = setLong(Long.parseLong(value/*,0 0*/));
                break;
            case Props.FLOAT:
                result = setFloat(Util.parseFloat(value));
                break;
            case Props.DOUBLE:
                result = setDouble(Util.parseDouble(value/*, 0*/));
                break;
            case Props.STRING:
            case Props.UNSPECIFIED:
                result = setString(value);
                break;
            case Props.EXTENDED: {
                // stringstream sstr (value);
                // static_cast<SGRawExtended*>(_value.val) -> readFrom(sstr);
                Util.notyet();
            }
            break;
            case Props.NONE:
            default:
                break;
        }

        // if (getAttribute(TRACE_WRITE))
        //    trace_write();
        return result;
    }

    public boolean setIntValue(int value) {
        // Shortcut for common case
        if (_attr == (Props.READ | Props.WRITE) && _type == Props.INT)
            return setInt(value);

        boolean result = false;
        //TODO TEST_WRITE;
        if (_type == Props.NONE || _type == Props.UNSPECIFIED) {
            clearValue();
            _type = Props.INT;
            if (_local_val == null) {
                _local_val = new LocalValUnion();
            }
            _local_val.int_val = 0;
        }

        switch (_type) {
            case Props.ALIAS:
                result = _value.alias.setIntValue(value);
                break;
            case Props.BOOL:
                result = setBool(value == 0 ? false : true);
                break;
            case Props.INT:
                result = setInt(value);
                break;
            case Props.LONG:
                result = setLong(value);
                break;
            case Props.FLOAT:
                result = setFloat(value);
                break;
            case Props.DOUBLE:
                result = setDouble(value);
                break;
            case Props.STRING:
            case Props.UNSPECIFIED: {
                //char buf[ 128];
                //sprintf(buf, "%d", value);
                result = setString("" + value);
                break;
            }
            case Props.NONE:
            default:
                break;
        }

        //if (getAttribute(TRACE_WRITE))
        //  trace_write();
        return result;
    }

    public boolean setLongValue(long value) {
        // Shortcut for common case
        if (_attr == (Props.READ | Props.WRITE) && _type == Props.LONG)
            return setLong(value);

        boolean result = false;
        //TODO TEST_WRITE;
        if (_type == Props.NONE || _type == Props.UNSPECIFIED) {
            clearValue();
            _type = Props.LONG;
            if (_local_val == null) {
                _local_val = new LocalValUnion();
            }
            _local_val.long_val = 0;
        }

        switch (_type) {
            case Props.ALIAS:
                result = _value.alias.setLongValue(value);
                break;
            case Props.BOOL:
                result = setBool(value == 0 ? false : true);
                break;
            case Props.INT:
                result = setInt((int) value);
                break;
            case Props.LONG:
                result = setLong(value);
                break;
            case Props.FLOAT:
                result = setFloat(value);
                break;
            case Props.DOUBLE:
                result = setDouble(value);
                break;
            case Props.STRING:
            case Props.UNSPECIFIED: {
                //char buf[ 128];
                //sprintf(buf, "%d", value);
                result = setString("" + value);
                break;
            }
            case Props.NONE:
            default:
                break;
        }

        //if (getAttribute(TRACE_WRITE))
        //  trace_write();
        return result;
    }


    public boolean setFloatValue(float value) {
        // Shortcut for common case
        if (_attr == (Props.READ | Props.WRITE) && _type == Props.FLOAT)
            return setFloat(value);

        boolean result = false;
        //TODO TEST_WRITE;
        if (_type == Props.NONE || _type == Props.UNSPECIFIED) {
            clearValue();
            _type = Props.FLOAT;
            if (_local_val == null) {
                _local_val = new LocalValUnion();
            }
            _local_val.float_val = 0;
        }

        switch (_type) {
            case Props.ALIAS:
                result = _value.alias.setFloatValue(value);
                break;
            case Props.BOOL:
                result = setBool(value == 0 ? false : true);
                break;
            case Props.INT:
                result = setInt((int) Math.round(value));
                break;
            case Props.LONG:
                result = setLong((long) Math.round(value));
                break;
            case Props.FLOAT:
                result = setFloat(value);
                break;
            case Props.DOUBLE:
                result = setDouble(value);
                break;
            case Props.STRING:
            case Props.UNSPECIFIED: {
                //char buf[ 128];
                //sprintf(buf, "%d", value);
                result = setString("" + value);
                break;
            }
            case Props.NONE:
            default:
                break;
        }

        //if (getAttribute(TRACE_WRITE))
        //  trace_write();
        return result;
    }

    public boolean setDoubleValue(double value) {
        // Shortcut for common case
        if (_attr == (Props.READ | Props.WRITE) && _type == Props.DOUBLE)
            return setDouble(value);

        boolean result = false;
        //TODO TEST_WRITE;
        if (_type == Props.NONE || _type == Props.UNSPECIFIED) {
            clearValue();
            _type = Props.DOUBLE;
            if (_local_val == null) {
                _local_val = new LocalValUnion();
            }
            _local_val.double_val = 0;
        }

        switch (_type) {
            case Props.ALIAS:
                result = _value.alias.setDoubleValue(value);
                break;
            case Props.BOOL:
                result = setBool(value == 0 ? false : true);
                break;
            case Props.INT:
                result = setInt((int) Math.round(value));
                break;
            case Props.LONG:
                result = setLong((long) Math.round(value));
                break;
            case Props.FLOAT:
                result = setFloat((float) value);
                break;
            case Props.DOUBLE:
                result = setDouble(value);
                break;
            case Props.STRING:
            case Props.UNSPECIFIED: {
                //char buf[ 128];
                //sprintf(buf, "%d", value);
                result = setString("" + value);
                break;
            }
            case Props.NONE:
            default:
                break;
        }

        //if (getAttribute(TRACE_WRITE))
        //  trace_write();
        return result;
    }

    public boolean setColorValue(Color value) {
        boolean result = false;
        //TEST_WRITE;
        if (_type == Props.NONE) {
            clearValue();
            _type = Props.COLOR;
        }
        int type = _type;
        if (type == Props.EXTENDED) {
            type = _value.val.getType();
        }
        switch (type) {
            case Props.ALIAS:
                Util.notyet();
                //result = _value.alias.setUnspecifiedValue(value);
                break;
            case Props.STRING:
            case Props.UNSPECIFIED:
                Util.notyet();
                break;
            case Props.VEC3D:
                Util.notyet();
                //result = static_cast<SGRawValue<SGVec3d>*>(_value.val)->setValue(parseString<SGVec3d>(value));
                break;
            case Props.COLOR:
                //result = static_cast<SGRawValue<SGVec4d>*>(_value.val)->setValue(parseString<SGVec4d>(value));
                result = setColor(value);
                break;
            case Props.NONE:
            default:
                break;
        }
        return result;
    }

    public boolean setVector3Value(Vector3 value) {
        boolean result = false;
        //TEST_WRITE;
        if (_type == Props.NONE) {
            clearValue();
            _type = Props.VEC3D;
        }
        int type = _type;
        if (type == Props.EXTENDED) {
            type = _value.val.getType();
        }
        switch (type) {
            case Props.ALIAS:
                Util.notyet();
                //result = _value.alias.setUnspecifiedValue(value);
                break;
            case Props.STRING:
            case Props.UNSPECIFIED:
                Util.notyet();
                break;
            case Props.VEC3D:
                result = setVector3(value);
                break;
            case Props.COLOR:
                //result = static_cast<SGRawValue<SGVec4d>*>(_value.val)->setValue(parseString<SGVec4d>(value));
                Util.notyet();
                break;
            case Props.NONE:
            default:
                break;
        }
        return result;
    }

    public boolean setUnspecifiedValue(String value) {
        boolean result = false;
        //TEST_WRITE;
        if (_type == Props.NONE) {
            clearValue();
            _type = Props.UNSPECIFIED;
        }
        int type = _type;
        if (type == Props.EXTENDED) {
            type = _value.val.getType();
        }
        switch (type) {
            case Props.ALIAS:
                result = _value.alias.setUnspecifiedValue(value);
                break;
            case Props.BOOL:
                result = setBool(compare_strings(value, "true") || ((Integer.parseInt(value) != 0) ? true : false));
                break;
            case Props.INT:
                result = setInt(Integer.parseInt(value));
                break;
            case Props.LONG:
                result = setLong(Long.parseLong(value/*, 0, 0*/));
                break;
            case Props.FLOAT:
                result = setFloat(Util.parseFloat(value));
                break;
            case Props.DOUBLE:
                result = setDouble(Util.parseDouble(value/*, 0*/));
                break;
            case Props.STRING:
            case Props.UNSPECIFIED:
                result = setString(value);
                break;
            case Props.VEC3D:
                Util.notyet();
                //result = static_cast<SGRawValue<SGVec3d>*>(_value.val)->setValue(parseString<SGVec3d>(value));
                break;
            case Props.COLOR:
                Util.notyet();
                //result = static_cast<SGRawValue<SGVec4d>*>(_value.val)->setValue(parseString<SGVec4d>(value));
                break;
            case Props.NONE:
            default:
                break;
        }

        //if (getAttribute(TRACE_WRITE))
        //   trace_write();
        return result;
    }

    ///////////////////

    /**
     * Test whether another node isType tied.
     * /
     * bool
     * SGPropertyNode::isTied (const char * relative_path) const
     * {
     * const SGPropertyNode * node = getNode(relative_path);
     * return (node == 0 ? false : node->isTied());
     * }
     * <p/>
     * <p/>
     * /**
     * Tie a node reached by a relative path, creating it if necessary.
     * /
     * bool
     * SGPropertyNode::tie (const char * relative_path,
     * const SGRawValue<bool> &rawValue,
     * bool useDefault)
     * {
     * return getNode(relative_path, true)->tie(rawValue, useDefault);
     * }
     * <p/>
     * <p/>
     * /**
     * Tie a node reached by a relative path, creating it if necessary.
     * /
     * bool
     * SGPropertyNode::tie (const char * relative_path,
     * const SGRawValue<int> &rawValue,
     * bool useDefault)
     * {
     * return getNode(relative_path, true)->tie(rawValue, useDefault);
     * }
     * <p/>
     * <p/>
     * /**
     * Tie a node reached by a relative path, creating it if necessary.
     * /
     * bool
     * SGPropertyNode::tie (const char * relative_path,
     * const SGRawValue<long> &rawValue,
     * bool useDefault)
     * {
     * return getNode(relative_path, true)->tie(rawValue, useDefault);
     * }
     * <p/>
     * <p/>
     * /**
     * Tie a node reached by a relative path, creating it if necessary.
     * /
     * bool
     * SGPropertyNode::tie (const char * relative_path,
     * const SGRawValue<float> &rawValue,
     * bool useDefault)
     * {
     * return getNode(relative_path, true)->tie(rawValue, useDefault);
     * }
     * <p/>
     * <p/>
     * /**
     * Tie a node reached by a relative path, creating it if necessary.
     * /
     * bool
     * SGPropertyNode::tie (const char * relative_path,
     * const SGRawValue<double> &rawValue,
     * bool useDefault)
     * {
     * return getNode(relative_path, true)->tie(rawValue, useDefault);
     * }
     * <p/>
     * <p/>
     * /**
     * Tie a node reached by a relative path, creating it if necessary.
     * /
     * bool
     * SGPropertyNode::tie (const char * relative_path,
     * const SGRawValue<const char *> &rawValue,
     * bool useDefault)
     * {
     * return getNode(relative_path, true)->tie(rawValue, useDefault);
     * }
     * <p/>
     * <p/>
     * /**
     * Attempt to untie another node reached by a relative path.
     * /
     * bool
     * SGPropertyNode::untie (const char * relative_path)
     * {
     * SGPropertyNode * node = getNode(relative_path);
     * return (node == 0 ? false : node->untie());
     * }
     */

    public void addChangeListener(SGPropertyChangeListener listener) {
        addChangeListener(listener, false);
    }

    public void addChangeListener(SGPropertyChangeListener listener, boolean initial) {
        if (_listeners == null)
            _listeners = new ArrayList<SGPropertyChangeListener>();
        _listeners.add(listener);
        listener.register_property(this);
        if (initial)
            listener.valueChanged(this);
    }

   /* void
    SGPropertyNode::removeChangeListener (SGPropertyChangeListener * listener)
    {
        if (_listeners == 0)
            return;
        vector<SGPropertyChangeListener*>::iterator it =
            find(_listeners->begin(), _listeners->end(), listener);
        if (it != _listeners->end()) {
            _listeners->erase(it);
            listener->unregister_property(this);
            if (_listeners->empty()) {
                vector<SGPropertyChangeListener*>* tmp = _listeners;
                _listeners = 0;
                delete tmp;
            }
        }
    }*/


    void fireValueChanged() {
        fireValueChanged(this);
    }

    void fireValueChanged(SGPropertyNode node) {
        if (_listeners != null) {
            for (int i = 0; i < _listeners.size(); i++) {
                _listeners.get(i).valueChanged(node);
            }
        }
        if (_parent != null)
            _parent.fireValueChanged(node);
    }

    void fireChildAdded(SGPropertyNode child) {
        fireChildAdded(this, child);
    }

    void fireChildAdded(SGPropertyNode parent, SGPropertyNode child) {
        if (_listeners != null) {
            for (int i = 0; i < _listeners.size(); i++) {
                (_listeners).get(i).childAdded(parent, child);
            }
        }
        if (_parent != null)
            _parent.fireChildAdded(parent, child);
    }

    ///////////////////

    /**
     * Get a string value for another node.
     */
    public String getStringValue(String relative_path,
                                 String defaultValue) {
        SGPropertyNode node = getNode(relative_path);
        return (node == null) ? defaultValue : node.getStringValue();
    }

    /**
     * Get a string value for another node.
     */
    public String getStringValue(String relative_path) {
        // TODO or null as default?
        return getStringValue(relative_path, "");
    }


    public float getFloatValue(String relative_path, float defaultValue) {
        SGPropertyNode node = getNode(relative_path);
        return (node == null) ? defaultValue : node.getFloatValue();
    }

    public double getDoubleValue(String relative_path, double defaultValue) {
        SGPropertyNode node = getNode(relative_path);
        return (node == null) ? defaultValue : node.getDoubleValue();
    }


    public boolean getBoolValue(String relative_path,
                                boolean defaultValue) {
        SGPropertyNode node = getNode(relative_path);
        return (node == null) ? defaultValue : node.getBoolValue();
    }

    public long getLongValue(String relative_path,
                             long defaultValue) {
        SGPropertyNode node = getNode(relative_path);
        return (node == null) ? defaultValue : node.getLongValue();
    }


    /**
     * Get an int value for another node.
     */
    public int getIntValue(String relative_path, int defaultValue) {
        SGPropertyNode node = getNode(relative_path);
        return (node == null ? defaultValue : node.getIntValue());
    }

    /**
     * Set a string value for another node.
     */
    public boolean setStringValue(String relative_path, String value) {
        return getNode(relative_path, true).setStringValue(value);
    }

    /**
     * Set a boolean value for another node.
     */
    public boolean setBoolValue(String relative_path, boolean value) {
        return getNode(relative_path, true).setBoolValue(value);
    }

    /**
     * Set a int value for another node.
     */
    public boolean setIntValue(String relative_path, int value) {
        return getNode(relative_path, true).setIntValue(value);
    }

    /**
     * Set a long value for another node.
     */
    public boolean setLongValue(String relative_path, long value) {
        return getNode(relative_path, true).setLongValue(value);
    }

    /**
     * Set a float value for another node.
     */
    public boolean setFloatValue(String relative_path, float value) {
        return getNode(relative_path, true).setFloatValue(value);
    }

    /**
     * Set a double value for another node.
     */
    public boolean setDoubleValue(String relative_path, double value) {
        return getNode(relative_path, true).setDoubleValue(value);
    }

    /**
     * Get all children with the same childname (but different indices).
     * Liefert im Zweifel eine leere Liste, aber nie null.
     */
    public PropertyList getChildren(String childname) {
        PropertyList childrenlist = new PropertyList();
        if (children==null){
           return childrenlist;
        }
        int max = children.size();

        for (int i = 0; i < max; i++) {
            if (/*compare_strings*/(children.get(i).getName().equals(childname))) {
                childrenlist.add/*push_back*/(children.get(i));
            }
        }
        //TODO sort(children.begin(), children.end(), CompareIndices());
        return childrenlist;

    }


    public String dump(String lineseparator) {
        return dodump("", lineseparator);
    }

    public String dodump(String level, String lineseparator) {
        String s = level + "." + name;
        s += "(value=" + getStringValue() + ")" + lineseparator;

        if (children!=null) {
            for (SGPropertyNode c : children) {
                s += c.dodump(level + ((StringUtils.length(level) == 0) ? "" : ".") + name, lineseparator) + lineseparator;
            }
        }
        return s;
    }

    /**
     * Parse the name for a path component.
     * <p/>
     * Name: [_a-zA-Z][-._a-zA-Z0-9]*
     */

   /*  Range
    parse_name (SGPropertyNode node, Range path)
    {
        typename Range::iterator i = path.begin();
        typename Range::iterator max = path.end();

        if (*i == '.') {
        i++;
        if (i != path.end() && *i == '.') {
            i++;
        }
        if (i != max && *i != '/')
        throw std::string("illegal character after . or ..");
    } else if (isalpha(*i) || *i == '_') {
        i++;

        // The rules inside a name are a little
        // less restrictive.
        while (i != max) {
            if (isalpha(*i) || isdigit(*i) || *i == '_' ||
            *i == '-' || *i == '.') {
                // name += path[i];
            } else if (*i == '[' || *i == '/') {
                break;
            } else {
                std::string err = "'";
                err.push_back(*i);
                err.append("' found in propertyname after '"+node->getNameString()+"'");
                err.append("\nname may contain only ._- and alphanumeric characters");
                throw err;
            }
            i++;
        }
    }

        else {
        if (path.begin() == i) {
            std::string err = "'";
            err.push_back(*i);
            err.append("' found in propertyname after '"+node->getNameString()+"'");
            err.append("\nname must begin with alpha or '_'");
            throw err;
        }
    }
        return Range(path.begin(), i);
    }*/
    public static boolean setValue(SGPropertyNode node, boolean value) {
        return node.setBoolValue(value);
    }

    public static boolean setValue(SGPropertyNode node, int value) {
        return node.setIntValue(value);
    }

    public static boolean setValue(SGPropertyNode node, long value) {
        return node.setLongValue(value);
    }

    public static boolean setValue(SGPropertyNode node, float value) {
        return node.setFloatValue(value);
    }

    public static boolean setValue(SGPropertyNode node, double value) {
        return node.setDoubleValue(value);
    }

    public static boolean setValue(SGPropertyNode node, String value) {
        return node.setStringValue(value);
    }

    public static boolean setValue(SGPropertyNode node, Color value) {
        return node.setColorValue(value);
    }

    public static boolean setValue(SGPropertyNode node, Vector3 value) {
        return node.setVector3Value(value);
    }


    /**
     * Return the underlying value.
     *
     * @return The actual value for the property.
     * @see #setValue
     */
    //virtual T getValue () const = 0;


    /**
     * FG-DIFF Alternative to C++ type casting
     * TODO what about unconvertable types, eg. Color?
     */
    public void fillSimpleValue(){
        
    }
        
    /**
     * Assign a new underlying value.
     *
     * If the new value cannot be set (because this isType a read-only
     * raw value, or because the new value isType not acceptable for
     * some reason) this method returns false and leaves the original
     * value unchanged.
     *
     * @param value The actual value for the property.
     * @return true if the value was set successfully, false otherwise.
     */
    /*virtual bool setValue (T value) = 0;


    template<typename T>
    inline bool SGPropertyNode::setValue(const T& val,
                                         typename boost::enable_if_c<simgear::props
            ::PropertyTraits<T>::Internal>::type* dummy)
    {
        return ::setValue(this, val);
    }*/

    /**
     * Utility function for creation of a child property node.
     */
    static public SGPropertyNode makeChild(SGPropertyNode parent, String name) {
        return makeChild(parent, name, 0);
    }

    static public SGPropertyNode makeChild(SGPropertyNode parent, String name, int index) {
        return parent.getChild(name, index, true);
    }

    /**
     * Get the number of child nodes.
     */
    public int nChildren() {
        if (children==null){
            return 0;
        }
        return (int) children.size();
    }

    /**
     * Ist eigentlich aus SGSharedPtr und prueft, ob eine Selbstreferenz existiert. Scheint mir 
     * in Java zumindest so nicht erforderlich.
     * @return
     */
    public boolean valid() {
        return true;
    }
}

class PathSplitIterator implements Iterator<String> {
    String[] parts;
    int pos = 0;

    public PathSplitIterator(String path) {
        if (StringUtils.startsWith(path,"/")) {
            parts = StringUtils.split(StringUtils.substring(path, 1), "/");
        } else {
            parts = StringUtils.split(path, "/");
        }
    }

    @Override
    public boolean hasNext() {
        return pos < parts.length;
    }

    @Override
    public String next() {
        return parts[pos++];
    }

    @Override
    public void remove() {

    }
}

class ValueUnion {
    public SGPropertyNode alias;
    //Cannot be SGRawBase because it has no type (C# error). But in fact it isType or should be.
    public SGRaw/*Base*/ val;

}

class LocalValUnion {
    public boolean bool_val;
    public int int_val;
    public long long_val;
    public float float_val;
    public double double_val;
    public String string_val;
    public Color color_val;
    public Vector3 vec3_val;
}
