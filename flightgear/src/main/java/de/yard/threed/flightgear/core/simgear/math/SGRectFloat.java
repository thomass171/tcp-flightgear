package de.yard.threed.flightgear.core.simgear.math;

import de.yard.threed.core.Vector2;

/**
 * SGRect.hxx
 * <p/>
 * Created by thomass on 08.08.16.
 */
public class SGRectFloat {
    Vector2 _min, _max;
    
    /*public SGRect
    SGRect():
    _min(SGLimits<T>::max(), SGLimits<T>::max()),
    _max(-SGLimits<T>::max(), -SGLimits<T>::max())
    {

    }

    explicit SGRect(const SGVec2<T>& pt):
    _min(pt),
    _max(pt)
    {

    }

    SGRect(T x, T y):
    _min(x, y),
    _max(x, y)
    {

    }

    SGRect(const SGVec2<T>& min, const SGVec2<T>& max):
    _min(min),
    _max(max)
    {

    }*/

    public SGRectFloat(float x, float y, float w, float h) {
        _min = new Vector2(x, y);
        _max = new Vector2(x + w, y + h);

    }
/*
    template<typename S>
    explicit SGRect(const SGRect<S>& rect):
    _min(rect.getMin()),
    _max(rect.getMax())
    {

    }

    void setMin(const SGVec2<T>& min) { _min = min; }
    const SGVec2<T>& getMin() const { return _min; }

    void setMax(const SGVec2<T>& max) { _max = max; }
    const SGVec2<T>& getMax() const { return _max; }

    void set(T x, T y, T w, T h)
    {
        _min.x() = x;
        _min.y() = y;
        _max.x() = x + w;
        _max.y() = y + h;
    }*/

    public double x()  { return _min.getX(); }
    public double y()  { return _min.getY(); }
    public double width()  { return _max.getX() - _min.getX(); }
    public double height()  { return _max.getY() - _min.getY(); }
    /*SGVec2<T> const& pos() const { return _min; }
    SGVec2<T> size() const { return SGVec2<T>(width(), height()); }

    void setX(T x) { T w = width(); _min.x() = x; _max.x() = x + w; }
    void setY(T y) { T h = height(); _min.y() = y; _max.y() = y + h; }
    void setWidth(T w) { _max.x() = _min.x() + w; }
    void setHeight(T h) { _max.y() = _min.y() + h; }
    void setPos(const SGVec2<T>& p) { setX(p.x()); setY(p.y()); }
    void setSize(const SGVec2<T>& s) { setWidth(s.x()); setHeight(s.y()); }

    T l() const { return _min.x(); }
    T r() const { return _max.x(); }
    T t() const { return _min.y(); }
    T b() const { return _max.y(); }

    T& l() { return _min.x(); }
    T& r() { return _max.x(); }
    T& t() { return _min.y(); }
    T& b() { return _max.y(); }

    void setLeft(T l) { _min.x() = l; }
    void setRight(T r) { _max.x() = r; }
    void setTop(T t) { _min.y() = t; }
    void setBottom(T b) { _max.y() = b; }

    /**
     * Expand rectangle to include the given position
     */
   /* void expandBy(T x, T y)
    {
        if( x < _min.x() ) _min.x() = x;
        if( x > _max.x() ) _max.x() = x;

        if( y < _min.y() ) _min.y() = y;
        if( y > _max.y() ) _max.y() = y;
    }*/

    /**
     * Move rect by vector
     */
   /* SGRectFloat& operator+=(const SGVec2<T>& offset)
    {
        _min += offset;
        _max += offset;
        return *this;
    }*/

    /**
     * Move rect by vector in inverse direction
     */
    /*SGRectFloat& operator-=(const SGVec2<T>& offset)
    {
        _min -= offset;
        _max -= offset;
        return *this;
    }

    bool operator==(const SGRectFloat<T>& rhs) const
    {
        return _min == rhs._min
                && _max == rhs._max;
    }

    bool operator!=(const SGRectFloat<T>& rhs) const
    {
        return !(*this == rhs);
    }*/

    public boolean contains(double x, double y)     {
        return _min.getX() <= x && x <= _max.getX()
                && _min.getY() <= y && y <= _max.getY();
    }

    /*bool contains(T x, T y, T margin) const
    {
        return (_min.x() - margin) <= x && x <= (_max.x() + margin)
                && (_min.y() - margin) <= y && y <= (_max.y() + margin);
    }*/

}
/*
template<typename T>
inline SGRect<T> operator+(SGRect<T> rect, const SGVec2<T>& offset)
        {
        return rect += offset;
        }

        template<typename T>
inline SGRect<T> operator+(const SGVec2<T>& offset, SGRect<T> rect)
        {
        return rect += offset;
        }

        template<typename T>
inline SGRect<T> operator-(SGRect<T> rect, const SGVec2<T>& offset)
        {
        return rect -= offset;
        }

        template<typename T>
inline SGRect<T> operator-(const SGVec2<T>& offset, SGRect<T> rect)
        {
        return rect -= offset;
        }

        template<typename char_type, typename traits_type, typename T>
        inline
        std::basic_ostream<char_type, traits_type>&
        operator<<(std::basic_ostream<char_type, traits_type>& s, const SGRect<T>& rect)
        { return s << "min = " << rect.getMin() << ", max = " << rect.getMax(); }

        typedef SGRect<int> SGRecti;
        typedef SGRect<float> SGRectf;
        typedef SGRect<double> SGRectd;

        
*/
  
