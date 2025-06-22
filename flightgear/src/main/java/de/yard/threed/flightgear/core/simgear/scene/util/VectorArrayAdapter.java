package de.yard.threed.flightgear.core.simgear.scene.util;

/* -*-c++-*-
 *
 * Copyright (C) 2007 Tim Moore
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 *
 * From VectorArrayAdapter.hxx
 */

import de.yard.threed.core.Vector3;
import de.yard.threed.core.Vector3Array;

import java.util.List;

public class VectorArrayAdapter<T> {

    // template<typename Vector>
    //class VectorArrayAdapter {
    //  public:
    List<T> _v;
    /*
     * Adapter that provides 2D array access to the elements of a
     * std::vector in row major order.
     * @param v the vector
     * @param rowStride distance from an element to the corresponding
     * 	element in the next row.
     * @param baseOffset offset of the first element of the array from the
     * 	beginning of the vector.
     * @param rowOffset offset of the first element in a row from the
     * 	actual beginning of the row.
     */
    public VectorArrayAdapter(List<T> list, int rowStride, int baseOffset/* =0*/,
                              int rowOffset /*=0*/) {
_v=list;
        _rowStride = (rowStride);

        _baseOffset = (baseOffset);

        _rowOffset = (rowOffset);
    }

    //public abstract T get(int i, int j) ;
    public T get(int i, int j) {
        //return _v.at(_baseOffset + i * _rowStride + _rowOffset + j);
        return _v.get(_baseOffset + i * _rowStride + _rowOffset + j);
    }

    public void set(int i, int j, T v) {
        //return _v.at(_baseOffset + i * _rowStride + _rowOffset + j);
        int index=_baseOffset + i * _rowStride + _rowOffset + j;
        _v.set(index, v);
    }
//#ifdef SG_CHECK_VECTOR_ACCESS
           /*see subclass  typename Vector::value_type&

            operator() (
            int i, int j)

            {
                return _v.at(_baseOffset + i * _rowStride + _rowOffset + j);
            }
    const
            typename Vector::value_type&

            operator() (
            int i, int j)const

            {
                return _v.at(_baseOffset + i * _rowStride + _rowOffset + j);
            }*/
/*#else
            typename Vector::value_type&

            operator() (
            int i, int j)

            {
                return _v[_baseOffset + i * _rowStride + _rowOffset + j];
            }
    const
            typename Vector::value_type&

            operator() (
            int i, int j)const

            {
                return _v[_baseOffset + i * _rowStride + _rowOffset + j];
            }
#endif*/

    //Vector3Array /*Vector&*/_v;

    int _rowStride;

    int _baseOffset;

    int _rowOffset;
}



