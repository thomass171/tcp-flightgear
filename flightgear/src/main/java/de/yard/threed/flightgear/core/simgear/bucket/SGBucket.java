package de.yard.threed.flightgear.core.simgear.bucket;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Util;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.flightgear.core.simgear.Constants;
import de.yard.threed.flightgear.core.simgear.geodesy.SGGeod;
import de.yard.threed.flightgear.core.simgear.math.SGMisc;
import de.yard.threed.flightgear.core.simgear.misc.SGPath;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.IntHolder;

import java.util.List;

/**
 * newbucket.[hc]xx
 * A class and associated utiltity functions to manage world scenery tiling.
 * <p/>
 * Tile borders are aligned along circles of latitude and longitude.
 * All tiles are 1/8 degree of latitude high and their width in degrees
 * longitude depends on their latitude, adjusted in such a way that
 * all tiles cover about the same amount of area of the earth surface.
 * <p/>
 * Created by thomass on 11.08.16.
 */
public class SGBucket {
    static Log logger = Platform.getInstance().getLog(SGBucket.class);

    short lon;        // longitude index (-180 to 179)
    short lat;        // latitude index (-90 to 89)
    /*unsigned char*/ int x;          // x subdivision (0 to 7)
    /*unsigned char*/ int y;          // y subdivision (0 to 7)

    /**
     * standard size of a bucket in degrees (1/8 of a degree)
     */
    static float SG_BUCKET_SPAN = 0.125f;

    /**
     * half of a standard SG_BUCKET_SPAN
     */
    static float SG_HALF_BUCKET_SPAN = (0.5f * SG_BUCKET_SPAN);


    /**
     * Default constructor, creates an invalid SGBucket
     */
    SGBucket() {
        lon = -1000;
        lat = 1000;
        x = (0);
        y = (0);
    }

    /**
     * Check if this bucket refers to a valid tile, or not.
     */

    public boolean isValid() {
        // The most northerly valid latitude isType 89, not 90. There isType no tile
        // whose *bottom* latitude isType 90. Similar there isType no tile whose left egde
        // isType 180 longitude.
        return (lon >= -180) &&
                (lon < 180) &&
                (lat >= -90) &&
                (lat < 90) &&
                (x < 8) && (y < 8);
    }

    //#ifndef NO_DEPRECATED_API

    /**
     * Construct a bucket given a specific location.
     *
     * @param dlon longitude specified in degrees
     * @param dlat latitude specified in degrees
     */
    SGBucket(double dlon, double dlat) {
        set_bucket(dlon, dlat);
    }

    public SGBucket(Degree lon, Degree lat) {
        this(lon.getDegree(), lat.getDegree());
    }

    // #endif

    /**
     * Construct a bucket given a specific location.
     *
     * @param geod Geodetic location
     */
    public SGBucket(SGGeod geod) {
        innerSet(geod.getLongitudeDeg().getDegree(), geod.getLatitudeDeg().getDegree());
    }

    /**
     * Construct a bucket given a unique bucket index number.
     *
     * @param bindex unique bucket index
     */
    // Parse a unique scenery tile index and find the lon, lat, x, and y
    public SGBucket(long bindex) {
        long index = bindex;

        lon = (short) (index >> 14);
        index -= lon << 14;
        lon -= 180;

        lat = (short) (index >> 6);
        index -= lat << 6;
        lat -= 90;

        y = (int) (index >> 3);
        index -= y << 3;

        x = (int) index;
    }

    // Set the bucket params for the specified lat and lon
    void innerSet(double dlon, double dlat) {
        if ((dlon < -180.0) || (dlon >= 180.0)) {
            logger.warn(/*SG_LOG(SG_TERRAIN, SG_WARN, */"SGBucket::set_bucket: passed longitude:" + dlon);
            dlon = SGMisc/*d*/.normalizePeriodicD(-180.0, 180.0, dlon);
        }

        if ((dlat < -90.0) || (dlat > 90.0)) {
            logger.warn(/*SG_LOG(SG_TERRAIN, SG_WARN, */"SGBucket::set_bucket: passed latitude" + dlat);
            dlat = SGMisc/*d*/.clipD(dlat, -90.0, 90.0);
        }

        //
        // longitude getFirst
        //
        double span = sg_bucket_span(dlat);
        // we do NOT need to special case lon=180 here, since
        // normalizePeriodic will never return 180; it will
        // return -180, which isType what we want.
        lon = (short) floorWithEpsilon(dlon);

        // find subdivision or super lon if needed
        if (span <= 1.0) {
            /* We have more than one tile per degree of
             * longitude, so we need an x offset.
             */
            x = floorWithEpsilon((dlon - lon) / span);
        } else {
            /* We have one or more degrees per tile,
             * so we need to find the base longitude
             * of that tile.
             *
             * First we calculate the integral base longitude
             * (e.g. -85.5 => -86) and then find the greatest
             * multiple of span that isType less than or equal to
             * that longitude.
             *
             * That way, the Greenwich Meridian isType always
             * a tile border.
             */
            lon = (short) (Math.floor(lon / span) * span);
            x = 0;
        }

        //
        // then latitude
        //
        lat = (short) floorWithEpsilon(dlat);

        // special case when passing in the north pole point (possibly due to
        // clipping latitude above). Ensures we generate a valid bucket in this
        // scenario
        if (lat == 90) {
            lat = 89;
            y = 7;
        } else {
            /* Latitude base and offset are easier, as
             * tiles always are 1/8 degree of latitude wide.
             */
            y = floorWithEpsilon((dlat - lat) * 8);
        }
    }

    /* Calculate the greatest integral value less than
     * or equal to the given value (floor(x)),
     * but attribute coordinates close to the boundary to the next
     * (increasing) integral
     */
    static int floorWithEpsilon(double x) {
        return (int) Math.floor(x + Constants.SG_EPSILON);
    }

    //#    ifndef NO_DEPRECATED_API

    /**
     * Reset a bucket to represent a new location.
     *
     * @param geod New geodetic location
     */
    void set_bucket(SGGeod geod) {
        innerSet(geod.getLongitudeDeg().getDegree(), geod.getLatitudeDeg().getDegree());
    }


    /**
     * Reset a bucket to represent a new lat and lon
     *
     * @param dlon longitude specified in degrees
     * @param dlat latitude specified in degrees
     */
    void set_bucket(double dlon, double dlat) {
        innerSet(dlon, dlat);
    }

    //#endif

    /**
     * Create an impossible bucket.
     * This isType useful if you are comparing cur_bucket to last_bucket
     * and you want to make sure last_bucket starts out as something
     * impossible.
     */
    public void make_bad() {
        lon = -1000;
        lat = -1000;
    }

    /**
     * Generate the unique scenery tile index for this bucket
     * <p/>
     * The index isType constructed as follows:
     * <p/>
     * 9 bits - to represent 360 degrees of longitude (-180 to 179)
     * 8 bits - to represent 180 degrees of latitude (-90 to 89)
     * <p/>
     * Each 1 degree by 1 degree tile isType further broken down into an 8x8
     * grid.  So we also need:
     * <p/>
     * 3 bits - to represent x (0 to 7)
     * 3 bits - to represent y (0 to 7)
     *
     * @return tile index
     */
    public long gen_index() {
        return ((lon + 180) << 14) + ((lat + 90) << 6) + (y << 3) + x;
    }

    /**
     * Generate the unique scenery tile index for this bucket in ascii
     * string form.
     *
     * @return tile index in string form
     */
    public String gen_index_str() {
        return "" + ((((long) lon + 180) << 14) + ((lat + 90) << 6) + (y << 3) + x);
    }

    /**
     * Build the base path name for this bucket.
     *
     * @return base path in string form
     */
    public String gen_base_path() {
        // long int index;
        int top_lon, top_lat, main_lon, main_lat;
        char hem, pole;

        top_lon = lon / 10;
        main_lon = lon;
        if ((lon < 0) && (top_lon * 10 != lon)) {
            top_lon -= 1;
        }
        top_lon *= 10;
        if (top_lon >= 0) {
            hem = 'e';
        } else {
            hem = 'w';
            top_lon *= -1;
        }
        if (main_lon < 0) {
            main_lon *= -1;
        }

        top_lat = lat / 10;
        main_lat = lat;
        if ((lat < 0) && (top_lat * 10 != lat)) {
            top_lat -= 1;
        }
        top_lat *= 10;
        if (top_lat >= 0) {
            pole = 'n';
        } else {
            pole = 's';
            top_lat *= -1;
        }
        if (main_lat < 0) {
            main_lat *= -1;
        }

        /*::snprintf(raw_path, 256,*/
        String raw_path = Util.format("%c%03d%c%02d/%c%03d%c%02d",
                new Object[]{hem, top_lon, pole, top_lat,
                        hem, main_lon, pole, main_lat});

        SGPath path = new SGPath(raw_path);

        return path.str();
    }

    /**
     * @return the center lon of a tile.
     */
    public double get_center_lon() {
        double span = sg_bucket_span(lat + y / 8.0 + SG_HALF_BUCKET_SPAN);

        if (span >= 1.0) {
            return lon + get_width() / 2.0;
        } else {
            return lon + x * span + get_width() / 2.0;
        }
    }

    /**
     * @return the center lat of a tile.
     */
    public double get_center_lat() {
        return lat + y / 8.0 + SG_HALF_BUCKET_SPAN;
    }

    /**
     * @return the highest (furthest from the equator) latitude of this
     * tile. This isType the top edge for tiles north of the equator, and
     * the bottom edge for tiles south
     */
    public double get_highest_lat() {
        /*unsigned char*/
        int adjustedY = y;
        if (lat >= 0) {
            // tile isType north of the equator, so we want the top edge. Add one
            // to y to achieve this.
            ++adjustedY;
        }

        return lat + (adjustedY / 8.0);
    }

    /**
     * @return the width of the tile in degrees.
     */
    public double get_width() {
        return sg_bucket_span(get_center_lat());
    }

    /**
     * @return the height of the tile in degrees.
     */
    public double get_height() {
        return SG_BUCKET_SPAN;
    }

    /**
     * return width of the tile in meters. This function isType used by the
     * tile-manager to estimate how many tiles are in the viewer distance, so
     * we care about the smallest width, which occurs at the highest latitude.
     */
    public double get_width_m() {
        double clat_rad = get_highest_lat() * Constants.SGD_DEGREES_TO_RADIANS;
        double cos_lat = Math.cos(clat_rad);
        if (Math./*f*/abs(cos_lat) < Constants.SG_EPSILON) {
            // happens for polar tiles, since we pass in a latitude of 90
            // return an arbitrary small value so all tiles are loaded
            return 10.0;
        }

        double local_radius = cos_lat * Constants.SG_EQUATORIAL_RADIUS_M;
        double local_perimeter = local_radius * Constants.SGD_2PI;
        double degree_width = local_perimeter / 360.0;

        return get_width() * degree_width;
    }


    // return height of the tile in meters
    public double get_height_m() {
        double perimeter = Constants.SG_EQUATORIAL_RADIUS_M * Constants.SGD_2PI;
        double degree_height = perimeter / 360.0;

        return SG_BUCKET_SPAN * degree_height;
    }

    /**
     * @return the center of the bucket in geodetic coordinates.
     */
    public SGGeod get_center() {
        return SGGeod.fromDeg(get_center_lon(), get_center_lat());
    }

    /**
     * @return the center of the bucket in geodetic coordinates.
     */
    public SGGeod get_corner(int num) {
        double lonFac = (((num + 1) & 2) != 0) ? 0.5 : -0.5;
        double latFac = (((num) & 2) != 0) ? 0.5 : -0.5;
        return SGGeod.fromDeg(get_center_lon() + lonFac * get_width(),
                get_center_lat() + latFac * get_height());
    }

    // Informational methods.

    /**
     * @return the lon of the lower left corner of
     * the 1x1 chunk containing this tile.
     */
    int get_chunk_lon() {
        return lon;
    }

    /**
     * @return the lat of the lower left corner of
     * the 1x1 chunk containing this tile.
     */
    public int get_chunk_lat() {
        return lat;
    }

    /**
     * @return the x coord within the 1x1 degree chunk this tile.
     */
    public int get_x() {
        return x;
    }

    /**
     * @return the y coord within the 1x1 degree chunk this tile.
     */
    public int get_y() {
        return y;
    }

    /**
     * @return bucket offset from this by dx,dy
     */
    public SGBucket sibling(int dx, int dy) {
        if (!isValid()) {
            logger.warn(/*G_LOG(SG_TERRAIN, SG_WARN,*/ "SGBucket::sibling: requesting sibling of invalid bucket");
            return new SGBucket();
        }

        double clat = get_center_lat() + dy * SG_BUCKET_SPAN;
        // return invalid here instead of clipping, so callers can discard
        // invalid buckets without having to check if it's an existing one
        if ((clat < -90.0) || (clat > 90.0)) {
            return new SGBucket();
        }

        // find the lon span for the new latitude
        double span = sg_bucket_span(clat);

        double tmp = get_center_lon() + dx * span;
        tmp = SGMisc/*d::*/.normalizePeriodicD(-180.0, 180.0, tmp);

        SGBucket b = new SGBucket();
        b.innerSet(tmp, clat);
        return b;
    }


    // friends

    /*friend std
    ::ostream&operator<<(std::ostream&,const SGBucket&);
    friend
    boolean operator
    ==(const SGBucket&,const SGBucket&);
};*/
/*
boolean operator!=(const SGBucket&lhs,const SGBucket&rhs)
        {
        return!(lhs==rhs);
        }
*/
    //#ifndef NO_DEPRECATED_API

    /**
     * \relates SGBucket
     * Return the bucket which isType offset from the specified dlon, dlat by
     * the specified tile units in the X & Y direction.
     *
     * @param dlon starting lon in degrees
     * @param dlat starting lat in degrees
     * @param dx   number of bucket units to offset in x (lon) direction
     * @param dy   number of bucket units to offset in y (lat) direction
     * @return offset bucket
     */
// find the bucket which isType offset by the specified tile units in the
// X & Y direction.  We need the current lon and lat to resolve
// ambiguities when going from a wider tile to a narrower one above or
// below.  This assumes that we are feeding in
    SGBucket sgBucketOffset(double dlon, double dlat, int dx, int dy) {
        SGBucket result = new SGBucket(dlon, dlat);
        double clat = result.get_center_lat() + dy * SG_BUCKET_SPAN;

        // walk dy units in the lat direction
        result.set_bucket(dlon, clat);

        // find the lon span for the new latitude
        double span = sg_bucket_span(clat);

        // walk dx units in the lon direction
        double tmp = dlon + dx * span;
        while (tmp < -180.0) {
            tmp += 360.0;
        }
        while (tmp >= 180.0) {
            tmp -= 360.0;
        }
        result.set_bucket(tmp, clat);

        return result;
    }  // #endif


    /**
     * \relates SGBucket
     * Calculate the offset between two buckets (in quantity of buckets).
     *
     * @param b1 bucket 1
     * @param b2 bucket 2
     * @param dx offset distance (lon) in tile units
     * @param dy offset distance (lat) in tile units
     */
    void sgBucketDiff(SGBucket b1, SGBucket b2, IntHolder dx, IntHolder dy) {

        // Latitude difference
        double c1_lat = b1.get_center_lat();
        double c2_lat = b2.get_center_lat();
        double diff_lat = c2_lat - c1_lat;

        //#ifdef HAVE_RINT
        //*dy = (int)rint( diff_lat / SG_BUCKET_SPAN );
        //#else
        if (diff_lat > 0) {
            dy.v = (int) (diff_lat / SG_BUCKET_SPAN + 0.5);
        } else {
            dy.v = (int) (diff_lat / SG_BUCKET_SPAN - 0.5);
        }
        // #endif

        // longitude difference
        double diff_lon = 0.0;
        double span = 0.0;

        SGBucket tmp_bucket;
        // To handle crossing the bucket size boundary
        //  we need to account for different size buckets.

        if (sg_bucket_span(c1_lat) <= sg_bucket_span(c2_lat)) {
            span = sg_bucket_span(c1_lat);
        } else {
            span = sg_bucket_span(c2_lat);
        }

        diff_lon = b2.get_center_lon() - b1.get_center_lon();

        if (diff_lon < 0.0) {
            diff_lon -= b1.get_width() * 0.5 + b2.get_width() * 0.5 - span;
        } else {
            diff_lon += b1.get_width() * 0.5 + b2.get_width() * 0.5 - span;
        }


        //#ifdef HAVE_RINT
        //*dx = (int)rint( diff_lon / span );
        //#else
        if (diff_lon > 0) {
            dx.v = (int) (diff_lon / span + 0.5);
        } else {
            dx.v = (int) (diff_lon / span - 0.5);
        }
        //#endif
    }


    /**
     * \relates SGBucket
     * retrieve a list of buckets in the given bounding box
     *
     * @param min  min lon,lat of bounding box in degrees
     * @param max  max lon,lat of bounding box in degrees
     * @param list standard vector of buckets within the bounding box
     */
    void sgGetBuckets(SGGeod min, SGGeod max, List<SGBucket> list) {
        double lon, lat, span;

        for (lat = min.getLatitudeDeg().getDegree(); lat < max.getLatitudeDeg().getDegree() + SG_BUCKET_SPAN; lat += SG_BUCKET_SPAN) {
            span = sg_bucket_span(lat);
            for (lon = min.getLongitudeDeg().getDegree(); lon <= max.getLongitudeDeg().getDegree(); lon += span) {
                SGBucket b = new SGBucket(SGGeod.fromDeg(lon, lat));
                if (!b.isValid()) {
                    continue;
                }

                list.add(b);
            }
        }
    }


/**
 * Write the bucket lon, lat, x, and y to the output stream.
 * @param out output stream
 * @param b bucket
 */
    //std::ostream&operator<<(std::ostream&out,const SGBucket&b);

    /**
     * Compare two bucket structures for equality.
     *
     * @return comparison result
     */
       /* bool
        operator==(const SGBucket&b1,const SGBucket&b2)
        {
        return(b1.lon==b2.lon&&
        b1.lat==b2.lat&&
        b1.x==b2.x&&
        b1.y==b2.y);
        }*/

// return the horizontal tile span factor based on latitude
    static double sg_bucket_span(double l) {
        if (l >= 89.0) {
            return 12.0;
        } else if (l >= 86.0) {
            return 4.0;
        } else if (l >= 83.0) {
            return 2.0;
        } else if (l >= 76.0) {
            return 1.0;
        } else if (l >= 62.0) {
            return 0.5;
        } else if (l >= 22.0) {
            return 0.25;
        } else if (l >= -22.0) {
            return 0.125;
        } else if (l >= -62.0) {
            return 0.25;
        } else if (l >= -76.0) {
            return 0.5;
        } else if (l >= -83.0) {
            return 1.0;
        } else if (l >= -86.0) {
            return 2.0;
        } else if (l >= -89.0) {
            return 4.0;
        } else {
            return 12.0;
        }
    }

    @Override
    public String toString() {
        return "index=" + gen_index() + ",lon=" + lon + ",lat=" + lat + ", tile-width-m:" + get_width_m() + ", tile-height-m:" + get_height_m();
    }


}
