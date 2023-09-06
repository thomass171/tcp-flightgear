package de.yard.threed.flightgear.core.simgear.scene.tgdb;

/**
 * Aus SGTriangleBin.hxx
 * 
 * Die Datenstrukturen sind Woodoo. Was ist bloss Sinn der Ableitung der Typisierung?
 * Vereinfacht auf eine einfache Trianglelist.
 * Typisierung wegen C# und Vereinfachung weggelassen.
 * 
 * Created by thomass on 04.08.16.
 */
public class SGTriangleBin extends SGVertexArrayBin{
    boolean BUILD_EDGE_MAP = true;
    //public zum testen
    public TriangleVector _triangleVector;
    //EdgeMap _edgeMap;

    
    /*typedef typename SGVertexArrayBin<T>::value_type value_type;
    typedef typename SGVertexArrayBin<T>::index_type index_type;
    typedef SGVec2<index_type> edge_ref;
    typedef SGVec3<index_type> triangle_ref;
    typedef std::vector<triangle_ref> TriangleVector;
    typedef std::vector<index_type> TriangleList;
    typedef std::map<edge_ref,TriangleList> EdgeMap;*/

    SGTriangleBin(){
        _triangleVector = new TriangleVector();
    }
    
    void insert( /*value_type&*/SGVertNormTex v0,  /*value_type&*/SGVertNormTex v1,  /*value_type&*/SGVertNormTex v2)    {
        /*index_type*/int i0 = /*SGVertexArrayBin<T>::*/insert(v0);
        /*index_type*/int i1 = /*SGVertexArrayBin<T>::*/insert(v1);
        /*index_type*/int i2 = /*SGVertexArrayBin<T>::*/insert(v2);
        /*index_type*/int triangleIndex = _triangleVector.size();
        _triangleVector.add(/*triangle_ref(i0, i1, i2)*/new int[]{i0,i1,i2});
       /* if ( BUILD_EDGE_MAP) {
            _edgeMap[edge_ref(i0, i1)].push_back(triangleIndex);
            _edgeMap[edge_ref(i1, i2)].push_back(triangleIndex);
            _edgeMap[edge_ref(i2, i0)].push_back(triangleIndex);
        }*/
    }

    int getNumTriangles()     {
        return _triangleVector.size(); 
    }
    
   /*  triangle_ref&*/int[] getTriangleRef(/*index_type*/int i){
     return _triangleVector.get(i); 
    }
    
     TriangleVector getTriangles()     {
        return _triangleVector; 
    }

  /*  void getConnectedSets(std::list<TriangleVector>& connectSets) const
    {
        std::vector<bool> processedTriangles(getNumTriangles(), false);
        for (index_type i = 0; i < getNumTriangles(); ++i) {
            if (processedTriangles[i])
                continue;

            TriangleVector currentSet;
            std::vector<edge_ref> edgeStack;

            {
                triangle_ref triangleRef = getTriangleRef(i);
                edgeStack.push_back(edge_ref(triangleRef[0], triangleRef[1]));
                edgeStack.push_back(edge_ref(triangleRef[1], triangleRef[2]));
                edgeStack.push_back(edge_ref(triangleRef[2], triangleRef[0]));
                currentSet.push_back(triangleRef);
                processedTriangles[i] = true;
            }

            while (!edgeStack.empty()) {
                edge_ref edge = edgeStack.back();
                edgeStack.pop_back();

                typename EdgeMap::const_iterator emiList[2] = {
                    _edgeMap.find(edge),
                            _edgeMap.find(edge_ref(edge[1], edge[0]))
                };
                for (unsigned ei = 0; ei < 2; ++ei) {
                    typename EdgeMap::const_iterator emi = emiList[ei];
                    if (emi == _edgeMap.end())
                        continue;

                    for (unsigned ti = 0; ti < emi->getSecond.size(); ++ti) {
                        index_type triangleIndex = emi->getSecond[ti];
                        if (processedTriangles[triangleIndex])
                            continue;

                        triangle_ref triangleRef = getTriangleRef(triangleIndex);
                        edgeStack.push_back(edge_ref(triangleRef[0], triangleRef[1]));
                        edgeStack.push_back(edge_ref(triangleRef[1], triangleRef[2]));
                        edgeStack.push_back(edge_ref(triangleRef[2], triangleRef[0]));
                        currentSet.push_back(triangleRef);
                        processedTriangles[triangleIndex] = true;
                    }
                }
            }

            connectSets.push_back(currentSet);
        }
    }
    */
    }
