package org.integratedmodelling.klab.raster.cog.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.geotools.coverage.grid.GridCoverage2D;
import org.integratedmodelling.klab.api.data.IGeometry;
import org.integratedmodelling.klab.api.observations.scale.space.IEnvelope;
import org.integratedmodelling.klab.common.Geometry;
import org.integratedmodelling.klab.components.geospace.extents.Envelope;
import org.integratedmodelling.klab.components.geospace.extents.Projection;
import org.integratedmodelling.klab.rest.SpatialExtent;

import it.geosolutions.jaiext.range.NoDataContainer;

public class CogLayer {
	
	private String name;
	Set<Double> nodata = new HashSet<>();
	private IEnvelope wgs84envelope;
	private int[] gridShape;
	private long timestamp = System.currentTimeMillis();
	
	private Projection originalProjection;
	private IEnvelope originalEnvelope;

	public CogLayer(GridCoverage2D coverage) {
		this.name = coverage.getName().toString(Locale.ENGLISH);
		this.nodata = noDataFromCoverage(coverage);
		this.gridShape = createGridShape(coverage);
		this.originalProjection = Projection.create(coverage.getCoordinateReferenceSystem());
		this.originalEnvelope = envelopeFromCoverage(coverage, originalProjection); 
		this.wgs84envelope = createWgs84Envelope(coverage);
	}

	public IGeometry getGeometry() {
		Geometry ret = Geometry.create("S2");
		
		if (gridShape != null) {
			ret = ret.withSpatialShape((long) gridShape[0], (long) gridShape[1]);
		}
		
		if (originalProjection != null && originalEnvelope != null) {
			if (originalProjection.flipsCoordinates()) {
				ret = ret.withBoundingBox(wgs84envelope.getMinX(), wgs84envelope.getMaxX(), wgs84envelope.getMinY(),
						wgs84envelope.getMaxY()).withProjection(Projection.DEFAULT_PROJECTION_CODE);
			} else {
				ret = ret
						.withBoundingBox(originalEnvelope.getMinX(), originalEnvelope.getMaxX(),
								originalEnvelope.getMinY(), originalEnvelope.getMaxY())
						.withProjection(originalProjection.getSimpleSRS());
			}
		} else if (wgs84envelope != null) {
			ret = ret.withBoundingBox(wgs84envelope.getMinX(), wgs84envelope.getMaxX(), wgs84envelope.getMinY(),
					wgs84envelope.getMaxY()).withProjection(Projection.DEFAULT_PROJECTION_CODE);
		}

		return ret;
	}

	public Set<Double> noDataFromCoverage(GridCoverage2D coverage) {
		final Object property = coverage.getProperty(NoDataContainer.GC_NODATA);
		if (property == null ) {
			return new HashSet<Double>();
		} else {
			Set<Double> targetSet = new HashSet<Double>();
            if (property instanceof NoDataContainer) {
                NoDataContainer container = (NoDataContainer) property;
                double[] vals = container.getAsArray();
                for (double val: vals) {
                	targetSet.add(Double.valueOf(val));
                }
            } else if (property instanceof Double) {
            	targetSet.add((Double) property);
            }
            return targetSet;
		}
	}

	public SpatialExtent getSpatialExtent() {

		if (wgs84envelope == null) {
			return null;
		}
		SpatialExtent ret = new SpatialExtent();
		ret.setWest(wgs84envelope.getMinX());
		ret.setEast(wgs84envelope.getMaxX());
		ret.setSouth(wgs84envelope.getMinY());
		ret.setNorth(wgs84envelope.getMaxY());
		return ret;
	}
	
	private IEnvelope envelopeFromCoverage(GridCoverage2D coverage, Projection projection) {
		double [] upperCorner = coverage.getEnvelope().getUpperCorner().getCoordinate();
		double [] lowerCorner = coverage.getEnvelope().getLowerCorner().getCoordinate();
		return Envelope.create(lowerCorner[0], upperCorner[0], lowerCorner[1],
				upperCorner[1], Projection.getLatLon());
	}
	
	private IEnvelope createWgs84Envelope(GridCoverage2D coverage) {
		return envelopeFromCoverage(coverage, Projection.getLatLon());
	}
	
	private int[] createGridShape(GridCoverage2D coverage) {
		int [] gridHighRange = coverage.getGridGeometry()
				.getGridRange()
				.getHigh()
				.getCoordinateValues();
		
		int [] gridLowRange = coverage.getGridGeometry()
				.getGridRange()
				.getLow()
				.getCoordinateValues();
		
		return new int[] { gridHighRange[0] - gridLowRange[0], gridHighRange[1] - gridLowRange[1] };
	}
	
	
	public String getName() {
		return name;
	}

	public Set<Double> getNodata() {
		return nodata;		
	}
	
	
}
