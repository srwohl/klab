package org.integratedmodelling.klab.components.geospace.extents.mediators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.integratedmodelling.klab.api.observations.scale.IExtent;
import org.integratedmodelling.klab.api.observations.scale.space.Direction;
import org.integratedmodelling.klab.components.geospace.extents.Grid;
import org.integratedmodelling.klab.components.geospace.extents.Projection;
import org.integratedmodelling.klab.components.geospace.extents.Shape;
import org.integratedmodelling.klab.components.geospace.utils.SpatialDisplay;
import org.integratedmodelling.klab.exceptions.KlabException;
import org.integratedmodelling.klab.utils.Pair;
import org.locationtech.jts.geom.Envelope;


/*
 * The Subgrid extends Grid only to provide compatibility with the constructors in Space. All methods are
 * overridden.
 */
public class Subgrid extends Grid {

	// TODO do this right when we need it again, using a map with a display per original grid indexed by ID
	private static SpatialDisplay display;
	
//	static SpatialDisplay getDisplay(Grid original, IScale scale) {
//		if (display == null) {
//			display = new SpatialDisplay(original.getBoundingBox());
//		}
//		return display;
//	}
	
	/**
	 * Get the percentage of area outside the requested shape that needs to be
	 * covered in order to snap the passed grid to the passed shape so that the
	 * resulting subgrid can be conformant. 
	 * 
	 * @param grid
	 * @param shape
	 * @return
	 */
	public static double getSubsettingError(Grid grid, Shape shape) {

		Envelope genv = new Envelope(grid.getEast(), grid.getWest(), grid.getSouth(), grid.getNorth());
		Envelope senv = shape.getEnvelope().getJTSEnvelope();
		
		if (!genv.covers(senv)) {
			throw new IllegalArgumentException(
					"cannot create subgrid: the passed shape does not cover the original grid");
		}

		double gxmin = grid.snapX(senv.getMinX(), Direction.LEFT);
		double gxmax = grid.snapX(senv.getMaxX(), Direction.RIGHT);
		double gymin = grid.snapY(senv.getMinY(), Direction.BOTTOM);
		double gymax = grid.snapY(senv.getMaxY(), Direction.TOP);

		// net adjustments, for now assume worst-case scenario of grid offset leaving 95% of one cell out 
		double dx = Math.abs(gxmax - gxmin - senv.getWidth())*.95;
		double dy = Math.abs(gymax - gymin - senv.getHeight())*.95;
		
		// relate areal adjustment to total area
		return (dx*dy)/genv.getArea();
	}

	/**
	 * Get a cutout of a grid from a top-level grid and a shape that intersects it.
	 * The resulting grid is made conformant by snapping the shape to the contours
	 * of the original grid.
	 * 
	 * @param grid
	 * @param shape
	 * @return the subgrid
	 * @throws IllegalArgumentException
	 *             if the shape does not cover the original grid shape.
	 */
	public static Subgrid create(Grid grid, Shape shape) {

		Envelope genv = new Envelope(grid.getWest(), grid.getEast(), grid.getSouth(), grid.getNorth());
		Envelope senv = shape.getEnvelope().getJTSEnvelope();

//		getDisplay(grid).add(shape);
//		getDisplay(grid).show();
		
		if (!genv.covers(senv)) {
			throw new IllegalArgumentException(
					"cannot create subgrid: the passed shape does not cover the original grid");
		}

		/*
		 * adjusts envelope boundaries to cover original cells exactly
		 */
		double gxmin = grid.snapX(senv.getMinX(), Direction.LEFT);
		double gxmax = grid.snapX(senv.getMaxX(), Direction.RIGHT);
		double gymin = grid.snapY(senv.getMinY(), Direction.BOTTOM);
		double gymax = grid.snapY(senv.getMaxY(), Direction.TOP);

		double dx = gxmax - gxmin;
		double dy = gymax - gymin;

		long nx = (long) Math.round(dx / grid.getCellWidth());
		long ny = (long) Math.round(dy / grid.getCellHeight());

		long xofs = (long)Math.round((gxmin - grid.getWest()) / grid.getCellWidth());
		long yofs = (long)Math.round((gymin - grid.getSouth()) / grid.getCellHeight());

		return new Subgrid(Grid.create(gxmin, gymin, gxmax, gymax, nx, ny, (Projection) grid.getProjection()), grid, xofs, yofs, shape);
	}

	/**
	 * Return all cells from the ORIGINAL grid that cover the shape, with the
	 * corresponding coverage.
	 * 
	 * @param grid
	 * @param shape
	 * @param useSimpleIntersection
	 *            if true, just check for containment and return full coverage. Much
	 *            faster. Use when no weighted aggregation is necessary.
	 * @return covered cells
	 * @throws KlabException
	 */
	public static Iterable<Pair<Cell, Double>> getCoveredCells(Grid grid, Shape shape, boolean useSimpleIntersection)
			throws KlabException {

		Subgrid subgrid = create(grid, shape);
		if (subgrid == null) {
			return null;
		}

		Collection<Pair<Cell, Double>> ret = new ArrayList<>();
		for (IExtent cell : subgrid) {
			double d = shape.getCoverage((Cell) cell, useSimpleIntersection);
			if (d > 0) {
				Cell orig = subgrid.getOriginalCell((Cell) cell);
				ret.add(new Pair<>(orig, d));
			}
		}

		return ret;
	}

	
	Grid grid;
	Grid ogrid;
	long xofs = 0;
	long yofs = 0;

	Subgrid(Grid grid, Grid originalGrid, long xofs, long yofs, Shape shape) {
		this.grid = grid;
		this.ogrid = originalGrid;
		this.xofs = xofs;
		this.yofs = yofs;
		this.projection = (Projection) originalGrid.getProjection();
		this.grid.createMask(shape);
		this.setSpace(grid.getSpace());
	}
	
	public Subgrid copy() {
		return new Subgrid(this.grid.copy(), this.ogrid, this.xofs, this.yofs, this.getShape().copy());
	}

	@Override
	public String toString() {
		return grid.toString() + "@ (xofs=" + xofs + ", yofs=" + yofs + ")" + ogrid;
	}

	@Override
	public long getYCells() {
		return grid.getYCells();
	}

	@Override
	public long getXCells() {
		return grid.getXCells();
	}

	@Override
	public long getCellCount() {
		return grid.getCellCount();
	}

	@Override
	public long getOffset(long x, long y) {
		return grid.getOffset(x, y);
	}

	@Override
	public boolean isActive(long x, long y) {
		return grid.isActive(x, y);
	}

	@Override
	public long getOffsetFromWorldCoordinates(double lon, double lat) {
		return grid.getOffsetFromWorldCoordinates(lon, lat);
	}

	@Override
	public long[] getXYOffsets(long index) {
		return grid.getXYOffsets(index);
	}

	@Override
	public double[] getCoordinates(long index) {
		return grid.getCoordinates(index);
	}

	@Override
	public double getEast() {
		return grid.getEast();
	}

	@Override
	public double getWest() {
		return grid.getWest();
	}

	@Override
	public double getSouth() {
		return grid.getSouth();
	}

	@Override
	public double getNorth() {
		return grid.getNorth();
	}

	@Override
	public double getCellWidth() {
		return grid.getCellWidth();
	}

	@Override
	public double getCellHeight() {
		return grid.getCellHeight();
	}

	@Override
	public Shape getShape() {
		return grid.getShape();
	}
	
	@Override
	public Shape getBoundingBox() {
		return grid.getBoundingBox();
	}

	@Override
	public double getEWExtent() {
		return grid.getEWExtent();
	}

	@Override
	public double getNSExtent() {
		return grid.getNSExtent();
	}

	@Override
	public Projection getProjection() {
		return grid.getProjection();
	}

	@Override
	public org.integratedmodelling.klab.components.geospace.extents.Envelope getEnvelope() {
		return grid.getEnvelope();
	}

	@Override
	public boolean isCovered(long granule) {
		return grid.isCovered(granule);
	}

	@Override
	public Shape getCentroid() {
		return grid.getCentroid();
	}

	@Override
	public Iterator<Cell> iterator() {
		return grid.iterator();
	}

	public Cell getOriginalCell(Cell cell) {
		return ogrid.getCell(cell.getX() + xofs, ogrid.getYCells() - yofs - (getYCells() - cell.getY() - 1) - 1);
	}

	public long getOriginalOffset(long offset) {
		Cell ocell = getOriginalCell(grid.getCell(offset));
		return ocell.getOffsetInGrid();
	}
	
	public Grid getOriginalGrid() {
		return ogrid;
	}

	@Override
	public double snapX(double xCoordinate, Direction direction) {
		return grid.snapX(xCoordinate, direction);
	}

	@Override
	public double snapY(double yCoordinate, Direction direction) {
		return grid.snapY(yCoordinate, direction);
	}

	@Override
	public double[] getWorldCoordinatesAt(long x, long y) {
		return grid.getWorldCoordinatesAt(x, y);
	}

	@Override
	public long[] getGridCoordinatesAt(double x, double y) {
		return grid.getGridCoordinatesAt(x, y);
	}
}