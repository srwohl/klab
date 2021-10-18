package org.integratedmodelling.klab.raster.cog;

import java.io.IOException;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataSourceException;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.integratedmodelling.klab.api.data.IGeometry;
import org.integratedmodelling.klab.common.Geometry;
import org.integratedmodelling.klab.components.geospace.extents.Projection;
import org.integratedmodelling.klab.exceptions.KlabIOException;
import org.integratedmodelling.klab.raster.cog.api.CogLayer;

import it.geosolutions.imageio.core.BasicAuthURI;
import it.geosolutions.imageio.plugins.cog.CogImageReadParam;
import it.geosolutions.imageioimpl.plugins.cog.CogImageInputStreamSpi;
import it.geosolutions.imageioimpl.plugins.cog.CogImageReaderSpi;
import it.geosolutions.imageioimpl.plugins.cog.CogSourceSPIProvider;
import it.geosolutions.imageioimpl.plugins.cog.HttpRangeReader;

public class CogService {
	
	BasicAuthURI cogUri;
	GeoTiffReader reader;
	CogLayer layer;
	int[] gridShape;

	public CogService(String cogUrl) {
		this.cogUri = new BasicAuthURI(cogUrl, false);
        HttpRangeReader rangeReader =
                new HttpRangeReader(cogUri.getUri(), CogImageReadParam.DEFAULT_HEADER_LENGTH);
        CogSourceSPIProvider input =
                new CogSourceSPIProvider(
                        cogUri,
                        new CogImageReaderSpi(),
                        new CogImageInputStreamSpi(),
                        rangeReader.getClass().getName());
        try {
			reader = new GeoTiffReader(input);
			GridCoverage2D coverage = reader.read(null);
			new CogLayer(coverage);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new KlabIOException(e);
		}
	}

	public CogLayer getLayer() {
		if(layer != null) {
			return layer;
		} else {
			return null;			
		}
	}
	

}
