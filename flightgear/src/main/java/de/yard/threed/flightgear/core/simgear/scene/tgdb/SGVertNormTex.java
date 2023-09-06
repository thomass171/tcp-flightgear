package de.yard.threed.flightgear.core.simgear.scene.tgdb;

import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;


/**
 * * Aus SGTexturedTriangleBin.hxx
 * <p/>
 * Created by thomass on 04.08.16.
 */
public class SGVertNormTex {
    Vector3 vertex;
    Vector3 normal;
    //TODO optimieren
    Vector2[] texCoord = new Vector2[4];
    int tc_mask;

    public SGVertNormTex() {
        tc_mask = 0;
    }


    public void SetVertex(Vector3 v) {
        vertex = v;
    }

    Vector3 GetVertex() {
        return vertex;
    }

    void SetNormal(Vector3 n) {
        normal = n;
    }

    Vector3 GetNormal() {
        return normal;
    }

    void SetTexCoord(int idx, Vector2 tc) {
        texCoord[idx] = tc;
        tc_mask |= 1 << idx;
    }

    public Vector2 GetTexCoord(int idx) {
        return texCoord[idx];
    }

    /**
     * Stringdarstellung, um zwei Werte vergleichen zu koennen.
     *
     * @return
     */
    public String toKey() {
        String key = vertex.getX() + "" + vertex.getY() + "" + vertex.getZ();
        key += normal.getX() + "" + normal.getY() + "" + normal.getZ();
        for (int i = 0; i < texCoord.length; i++) {
            if (texCoord[i] == null) {
                key += "n";
            } else {
                key += texCoord[i].getX() + "" + texCoord[i].getY();
            }
        }
        return key;
    }
    
     /* wo brauchts denn diesen Operator struct less? Wahrscheinlich wir der durch den toKey ersetzt

    {
        inline bool tc_is_less(const SGVertNormTex & l,
        const SGVertNormTex & r,
        int idx)const
        {
            if (r.tc_mask & 1 << idx) {
                if (l.tc_mask & 1 << idx) {
                    if (l.texCoord[idx] < r.texCoord[idx]) {
                        return true;
                    }
                }
            }

            return false;
        }
        ;

        inline bool operator() (const SGVertNormTex & l,
        const SGVertNormTex & r)const
        {
            if (l.vertex < r.vertex) return true;
            else if (r.vertex < l.vertex) return false;
            else if (l.normal < r.normal) return true;
            else if (r.normal < l.normal) return false;
            else if (tc_is_less(l, r, 0)) return true;
            else if (tc_is_less(r, l, 0)) return false;
            else if (tc_is_less(l, r, 1)) return true;
            else if (tc_is_less(r, l, 1)) return false;
            else if (tc_is_less(l, r, 2)) return true;
            else if (tc_is_less(r, l, 2)) return false;
            else if (tc_is_less(l, r, 3)) return true;
            else return false;
        }
    }*/

}
