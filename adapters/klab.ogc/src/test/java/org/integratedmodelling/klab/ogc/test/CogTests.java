package org.integratedmodelling.klab.ogc.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.IOException;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.junit.Assert;
import org.junit.Test;

import it.geosolutions.imageio.core.BasicAuthURI;
import it.geosolutions.imageio.plugins.cog.CogImageReadParam;
import it.geosolutions.imageioimpl.plugins.cog.CogImageInputStreamSpi;
import it.geosolutions.imageioimpl.plugins.cog.CogImageReader;
import it.geosolutions.imageioimpl.plugins.cog.CogImageReaderSpi;
import it.geosolutions.imageioimpl.plugins.cog.CogSourceSPIProvider;
import it.geosolutions.imageioimpl.plugins.cog.DefaultCogImageInputStream;
import it.geosolutions.imageioimpl.plugins.cog.HttpRangeReader;

public class CogTests {
	
	private static final String cogUrl = "https://gs-cog.s3.eu-central-1.amazonaws.com/land_topo_cog_jpeg_8192.tif";
	
	 private static final String cogUrl2 = "https://s3-us-west-2.amazonaws.com/landsat-pds/c1/L8/153/075/LC08_L1TP_153075_20190515_20190515_01_RT/LC08_L1TP_153075_20190515_20190515_01_RT_B2.TIF";
	 
	@Test
	public void imageioPresent(){
		
        DefaultCogImageInputStream cogStream = new DefaultCogImageInputStream(cogUrl2);
        CogImageReader reader = new CogImageReader(new CogImageReaderSpi());
        reader.setInput(cogStream);
        reader.getDefaultReadParam();
        
        int x = 0;
        int y = 0;
        int width = 2000;
        int height = 2000;

        CogImageReadParam param = new CogImageReadParam();
        param.setSourceRegion(new Rectangle(x, y, width, height));
        param.setRangeReaderClass(HttpRangeReader.class);
        
        BufferedImage cogImage = null;
		try {
			cogImage = reader.read(0, param);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        Assert.assertEquals(width, cogImage.getWidth());
        Assert.assertEquals(height, cogImage.getHeight());

	}
	
	@Test
	public void readMetaData() throws IOException {
		BasicAuthURI cogUri = new BasicAuthURI(cogUrl2, false);
        HttpRangeReader rangeReader =
                new HttpRangeReader(cogUri.getUri(), CogImageReadParam.DEFAULT_HEADER_LENGTH);
        CogSourceSPIProvider input =
                new CogSourceSPIProvider(
                        cogUri,
                        new CogImageReaderSpi(),
                        new CogImageInputStreamSpi(),
                        rangeReader.getClass().getName());
        GeoTiffReader reader = new GeoTiffReader(input);
        reader.getCoordinateReferenceSystem();
        reader.getGridCoverageNames();
        reader.getFormat();
        reader.getMetadataNames();
        reader.getOriginalEnvelope().getUpperCorner();
        GridCoverage2D coverage = reader.read(null);
        coverage.getProperties();
        coverage.getGridGeometry().getEnvelope().getLowerCorner();
        coverage.getGridGeometry();
        
        assertNotNull(coverage);
        RenderedImage image = coverage.getRenderedImage();
        int numTileX = image.getNumXTiles();
        int numTileY = image.getNumYTiles();

        Raster raster = image.getTile(numTileX / 2, numTileY / 2);
        assertEquals(512, raster.getWidth());
        assertEquals(512, raster.getHeight());
        assertEquals(1, raster.getNumBands());
	}
	
	@Test
	public void cogLayer() throws IOException {
		BasicAuthURI cogUri = new BasicAuthURI(cogUrl2, false);
        HttpRangeReader rangeReader =
                new HttpRangeReader(cogUri.getUri(), CogImageReadParam.DEFAULT_HEADER_LENGTH);
        CogSourceSPIProvider input =
                new CogSourceSPIProvider(
                        cogUri,
                        new CogImageReaderSpi(),
                        new CogImageInputStreamSpi(),
                        rangeReader.getClass().getName());
        GeoTiffReader reader = new GeoTiffReader(input);
        GridCoverage2D coverage = reader.read(null);
	}

}
