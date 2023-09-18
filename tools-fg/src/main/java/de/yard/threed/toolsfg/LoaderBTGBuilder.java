package de.yard.threed.toolsfg;

import de.yard.threed.core.Util;
import de.yard.threed.core.buffer.ByteArrayInputStream;
import de.yard.threed.core.buffer.SimpleByteBuffer;
import de.yard.threed.core.loader.AbstractLoader;
import de.yard.threed.core.loader.AbstractLoaderBuilder;
import de.yard.threed.core.loader.InvalidDataException;
import de.yard.threed.flightgear.LoaderBTG;
import de.yard.threed.flightgear.LoaderOptions;


public class LoaderBTGBuilder implements AbstractLoaderBuilder {

    /**
     * Needs noargconstructor for reflection.
     */
    public LoaderBTGBuilder() {

    }

    @Override
    public boolean supports(String extension) {
        return extension.equalsIgnoreCase("btg");
    }

    @Override
    public AbstractLoader buildAbstractLoader(byte[] data, String filenameForInfo) throws InvalidDataException {

        LoaderOptions loaderoptions = new LoaderOptions(SGMaterialLibWrapper.getInstance().getSGMaterialLib());

        LoaderBTG btg = new LoaderBTG(new ByteArrayInputStream(new SimpleByteBuffer(data)), null, loaderoptions, filenameForInfo);

        return btg;
    }
}
