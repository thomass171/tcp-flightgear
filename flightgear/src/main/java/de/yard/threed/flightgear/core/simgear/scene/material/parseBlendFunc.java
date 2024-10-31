package de.yard.threed.flightgear.core.simgear.scene.material;

import de.yard.threed.flightgear.core.simgear.SGPropertyNode;

/**
 * From parseBlendFunc.[ch]xx
 */
public class parseBlendFunc {

    /**
     * Parse a blend function from the given property nodes and apply it to the
     * given osg::StateSet.
     *
     * @param ss          StateState which the blend function will be applied to
     * @param src
     * @param dest
     * @param src_rgb
     * @param dest_rgb
     * @param src_alpha
     * @param dest_alpha
     */
    public static boolean parseBlendFunc( /*osg::StateSet*/Pass ss,
                        SGPropertyNode src,
                        SGPropertyNode dest,
                        SGPropertyNode src_rgb,
                        SGPropertyNode dest_rgb,
                        SGPropertyNode src_alpha,
                        SGPropertyNode dest_alpha )
    {
        /*if( !ss )
            return false;

        BlendFunc::BlendFuncMode src_mode = BlendFunc::ONE;
        BlendFunc::BlendFuncMode dest_mode = BlendFunc::ZERO;

        if( src )
            findAttr(blendFuncModes, src, src_mode);
        if( dest )
            findAttr(blendFuncModes, dest, dest_mode);

        if( src && dest
                && !(src_rgb || src_alpha || dest_rgb || dest_alpha)
                && src_mode == BlendFunc::SRC_ALPHA
                && dest_mode == BlendFunc::ONE_MINUS_SRC_ALPHA )
        {
            ss->setAttributeAndModes(
                    StateAttributeFactory::instance()->getStandardBlendFunc()
      );
            return true;
        }

        BlendFunc* blend_func = new BlendFunc;
        if( src )
            blend_func->setSource(src_mode);
        if( dest )
            blend_func->setDestination(dest_mode);

        if( src_rgb )
        {
            BlendFunc::BlendFuncMode sourceRGBMode;
            findAttr(blendFuncModes, src_rgb, sourceRGBMode);
            blend_func->setSourceRGB(sourceRGBMode);
        }
        if( dest_rgb)
        {
            BlendFunc::BlendFuncMode destRGBMode;
            findAttr(blendFuncModes, dest_rgb, destRGBMode);
            blend_func->setDestinationRGB(destRGBMode);
        }
        if( src_alpha )
        {
            BlendFunc::BlendFuncMode sourceAlphaMode;
            findAttr(blendFuncModes, src_alpha, sourceAlphaMode);
            blend_func->setSourceAlpha(sourceAlphaMode);
        }
        if( dest_alpha)
        {
            BlendFunc::BlendFuncMode destAlphaMode;
            findAttr(blendFuncModes, dest_alpha, destAlphaMode);
            blend_func->setDestinationAlpha(destAlphaMode);
        }
        ss->setAttributeAndModes(blend_func);*/
        return true;
    }

}



