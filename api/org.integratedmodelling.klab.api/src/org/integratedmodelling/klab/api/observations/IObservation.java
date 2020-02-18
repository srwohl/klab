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
package org.integratedmodelling.klab.api.observations;

import org.integratedmodelling.klab.api.actors.IKlabActor;
import org.integratedmodelling.klab.api.auth.IArtifactIdentity;
import org.integratedmodelling.klab.api.data.ILocator;
import org.integratedmodelling.klab.api.knowledge.IObservable;
import org.integratedmodelling.klab.api.observations.scale.IScale;
import org.integratedmodelling.klab.api.observations.scale.space.ISpace;
import org.integratedmodelling.klab.api.provenance.IArtifact;
import org.integratedmodelling.klab.api.runtime.IScheduler;

/**
 * The Interface IObservation, which is the semantic equivalent of an IArtifact
 * and once created in a k.LAB session, can be made reactive by supplementing it
 * with a behavior. Models may bind instantiated observations to actor files
 * that will provide behaviors for their instances (or a subset thereof). Once
 * made reactive, they can interact with each other and the system.
 *
 * @author ferdinando.villa
 * @version $Id: $Id
 */
public interface IObservation extends IArtifactIdentity, IArtifact, IKlabActor {

	/**
	 * Return the observable.
	 *
	 * @return the observation's observable
	 */
	IObservable getObservable();

	/**
	 * Return the scale seen by this object, merging all the extents declared for
	 * the subject in the observation context. This could simply override
	 * {@link org.integratedmodelling.klab.api.provenance.IArtifact#getGeometry()}
	 * as a {@link org.integratedmodelling.klab.api.observations.scale.IScale} is a
	 * {@link org.integratedmodelling.klab.api.data.IGeometry}, and in a standard
	 * implementation should do just that, but a
	 * {@link org.integratedmodelling.klab.api.observations.scale.IScale} is
	 * important enough to deserve its own accessor.
	 *
	 * @return the observation's scale
	 */
	IScale getScale();

	/**
	 * Return a view of this observation restricted to the passed locator, which is
	 * applied to the scale to obtain a new scale, used as a filter to obtain the
	 * view. The result should be able to handle both conformant scaling (e.g. fix
	 * one dimension) and non-conformant (i.e. one state maps to multiple ones with
	 * irregular extent coverage) in both reading and writing.
	 * 
	 * @param locator
	 * @return a rescaled view of this observation
	 * @throws IllegalArgumentException if the locator is unsuitable for the
	 *                                  observation
	 */
	IObservation at(ILocator locator);

	/**
	 * Observation may have been made in the context of another direct observation.
	 * This will always return non-null in indirect observations, and may return
	 * null in direct ones when they represent the "root" context.
	 *
	 * @return the context for the observation, if any.
	 */
	IDirectObservation getContext();

	/**
	 * True if our scale has an observation of space with more than one state value.
	 *
	 * @return true if distributed in space
	 */
	boolean isSpatiallyDistributed();

	/**
	 * True if our scale has an observation of time with more than one state value.
	 *
	 * @return true if distributed in time.
	 */
	boolean isTemporallyDistributed();

	/**
	 * True if our scale has any implementation of time.
	 *
	 * @return if time is known
	 */
	boolean isTemporal();

	/**
	 * True if our scale has any implementation of space.
	 *
	 * @return if space is known
	 */
	boolean isSpatial();

	/**
	 * Return the spatial extent, or null.
	 *
	 * @return the observation of space
	 */
	ISpace getSpace();

	/**
	 * Reinterpret this artifact as a collection of artifacts reflecting the view of
	 * each of the passed observers. The result will behave exactly like the
	 * original artifact but each observer can set itself as the viewpoint,
	 * selecting different content.
	 * 
	 * @param observers
	 * @return
	 */
	ISubjectiveObservation reinterpret(IDirectObservation observer);

	/**
	 * Time of creation. If the context has no time, this is equal to the
	 * {@link IArtifact#getTimestamp()}; otherwise it is the time reported by
	 * {@link IScheduler#getSliceOffsetInBackend()} at the moment of construction.
	 * 
	 * @return the time of creation
	 */
	long getCreationTime();

	/**
	 * Time of exit. If the context has no time or the object is current, this is
	 * -1L; otherwise it is the time reported by
	 * {@link IScheduler#getSliceOffsetInBackend()} at the moment of exit.
	 * 
	 * @return the time of exit
	 */

	long getExitTime();

}
