/*
 * This file is part of k.LAB.
 * 
 * k.LAB is free software: you can redistribute it and/or modify
 * it under the terms of the Affero GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * A copy of the GNU Affero General Public License is distributed in the root
 * directory of the k.LAB distribution (LICENSE.txt). If this cannot be found 
 * see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (C) 2007-2018 integratedmodelling.org and any authors mentioned
 * in author tags. All rights reserved.
 */
package org.integratedmodelling.klab.api.observations.scale;

import java.util.List;

import org.integratedmodelling.klab.api.data.IGeometry;
import org.integratedmodelling.klab.api.data.IGeometry.Dimension.Type;
import org.integratedmodelling.klab.api.data.ILocator;
import org.integratedmodelling.klab.api.observations.scale.space.ISpace;
import org.integratedmodelling.klab.api.observations.scale.time.ITime;
import org.integratedmodelling.klab.api.runtime.monitoring.IMonitor;
import org.integratedmodelling.klab.common.LogicalConnector;

/**
 * The Interface IScale.
 *
 * @author ferdinando.villa
 * @version $Id: $Id
 */
public interface IScale extends ILocator, IGeometry, ITopology<IScale> {

	/**
	 * We deal with space and time in all natural systems, so we expose these to
	 * ease API use.
	 *
	 * @return the space, or null
	 */
	ISpace getSpace();

	/**
	 * We deal with space and time in all natural systems, so we expose these to
	 * ease API use.
	 *
	 * @return the time, or null
	 */
	ITime getTime();

	/**
	 * True if we have time and the time topology determines more than a single
	 * state. It's also in IObservation, but it's convenient to duplicate it here
	 * too.
	 *
	 * @return true if distributed in time
	 */
	boolean isTemporallyDistributed();

	/**
	 * True if we have space and the space topology determines more than a single
	 * state. It's also in IObservation, but it's convenient to duplicate it here
	 * too.
	 *
	 * @return true if distributed in space
	 */
	boolean isSpatiallyDistributed();

	/**
	 * Total number of extents available in this Scale. Note that in principle there
	 * may be more extents than just space and/or time, although this is not
	 * supported at the moment. Read the non-existing documentation.
	 *
	 * @return the number of extents for this topology
	 */
	int getExtentCount();

	/**
	 * Return the list of extents, ordered by contextualization priority (time, if
	 * present, will always be first).
	 *
	 * @return the extents
	 */
	List<IExtent> getExtents();

	/**
	 * Return true only if he scale has > 0 extents and any of them is empty, so
	 * that the coverage of any other scale can only be 0.
	 *
	 * @return true if scale cannot be the context for any observation.
	 */
	boolean isEmpty();

	/**
	 * Merge in another scale to return a new scale that represents the common
	 * traits in both. Used to build the "common" scale of a dataflow before
	 * contextualization, which will then be passed to
	 * {@link #adopt(IScale, IMonitor)} to create the scale of contextualization of
	 * each actuator resulting from a model.
	 * 
	 * @param scale
	 */
	public IScale merge(IScale scale);

	/**
	 * Return a new scale based on this and adopting any constraints set in the
	 * passed scale, which are to be considered "authoritative" and mandatory.
	 * Called at runtime on the result of merge() from all models during
	 * contextualization and before computation of each individual model, to ensure
	 * that any constraints set in the model are represented in the scale it will be
	 * computed in. Model-generated artifacts will have the resulting scale. If
	 * {@link #merge(IScale)} builds the <i>overall</i> scale of
	 * <i>contextualization</i>, this builds the <i>specific</i> scale of
	 * <i>computation</i> for a single model's scope.
	 * 
	 * @param scale
	 * @param monitor
	 * @return
	 */
	public IScale adopt(IScale scale, IMonitor monitor);

	/**
	 * {@inheritDoc}
	 *
	 * Return a new scale merging all extents from the passed parameter. The extents
	 * of the merged in scale are authoritative in terms of extent; granularity is
	 * negotiated as defined by each extent individually.
	 * <p>
	 * Extents in common are merged according to how the merge is implemented; any
	 * extents that are in one scale and not the other are left in the returned
	 * scale as they are.
	 * <p>
	 * Must not modify the original scales.
	 */
	@Override
	IScale merge(ITopologicallyComparable<?> other, LogicalConnector how);

	/**
	 * Mimics
	 * {@link org.integratedmodelling.klab.api.data.IGeometry.Dimension#shape()}
	 * passing the type of the desired dimension.
	 *
	 * @param dimension the dimension we need the shape of
	 * @return the shape of the passed dimension
	 * @throws java.lang.IllegalArgumentException if the dimension is not known in
	 *                                            this scale
	 */
	long[] shape(Type dimension);

	/**
	 * Return the scale at the beginning of time, or the scale itself if there is no
	 * time at all.
	 */
	IScale initialization();
	
	/**
	 * Return a new scale without the passed dimension.
	 * 
	 * @param dimension
	 * @return
	 */
	IScale without(IGeometry.Dimension.Type dimension);

	/**
	 * Return a scale optimized for iterating along the dimensions passed here (use
	 * the same call logics as in {@link IGeometry#at(Object...)}}. At worst the
	 * implementation can return the same scale if the iterated class is compatible,
	 * but ideally it should return a wrapper that makes iteration as fast as
	 * possible. Ensure that the returned iterator is thread-safe (i.e., if objects
	 * are reused ensure they are thread local).
	 * <p>
	 * Dimensions that are not mentioned in the parameters should be removed from
	 * the offsets if the desired locator is an offset and their multiplicity is
	 * one.
	 * <p>
	 * Example: to scan a scale along a spatial grid using simple <x,y> offsets when
	 * it is known that space is gridded and time is not there or is
	 * one-dimensional:
	 * 
	 * <pre>
	 * for (Offset offset : scale.scan(Offset.class, IGrid.class)) {
	 * 	... use spatial-only offset
	 * }
	 * </pre>
	 * 
	 * This will return offsets with only two dimensions (x,y for the grid
	 * coordinates) and ensure that the grid is scanned in the quickest way
	 * possible. An even more API-friendly way (but potentially slower due to
	 * retrieving the cell) would be
	 * 
	 * <pre>
	 * for (Cell cell : scale.scan(Cell.class, IGrid.class)) {
	 * 	... use cell as is
	 * }
	 * </pre>
	 * 
	 * All these are optimized versions of
	 * 
	 * <pre>
	 * for (ILocator locator : scale) {
	 * 	...
	 * }
	 * </pre>
	 * 
	 * which would need a call to locator.as(...) to obtain the desired info and
	 * would therefore iterate more slowly due to the reinterpretation of the
	 * offsets at each call to next().
	 * 
	 * @return the desired iterable
	 * @throw {@link IllegalArgumentException} if the parameters cannot be
	 *        understood or honored.
	 */
	<T extends ILocator> Iterable<T> scan(Class<T> desiredLocatorClass, Object... dimensionIdentifiers);

}
