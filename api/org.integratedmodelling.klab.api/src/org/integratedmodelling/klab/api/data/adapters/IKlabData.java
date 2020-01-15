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
package org.integratedmodelling.klab.api.data.adapters;

import java.util.List;

import org.integratedmodelling.klab.api.data.IGeometry;
import org.integratedmodelling.klab.api.data.ILocator;
import org.integratedmodelling.klab.api.provenance.IArtifact;
import org.integratedmodelling.klab.api.runtime.rest.INotification;

/**
 * Encoded k.LAB data resulting from decoding a resource URN in a specified
 * geometry. The interface supports both direct building within an existing
 * artifact or setting of data into the Protobuf-based encoding for remote
 * consumption.
 * <p>
 * A builder is passed to each {@link IResourceEncoder encoder} by the runtime.
 * The builder is then used to build a {@code IKlabData} object which is in turn
 * used to extract the final artifact.
 * 
 * @author ferdinando.villa
 * @version $Id: $Id
 */
public interface IKlabData {

	/**
	 * A builder is passed to each {@link IResourceEncoder encoder} by the runtime,
	 * set appropriately to ensure that no unnecessary storage is wasted.
	 * <p>
	 * When the builder is used, a {@IComputationContext runtime context} will be
	 * available, and should be used to inquire about the names and types of the
	 * target artifacts expected. * @author ferdinando.villa
	 *
	 */
	interface Builder {

		/**
		 * Get a builder that defines a state. Any further operation will operate on the
		 * object until {@link finishState()} is called.
		 * <p>
		 * If this was called before at the same level, the new artifact will be chained
		 * to the previous when built.
		 * 
		 * @param name
		 *            TODO
		 * @return a builder on which the add() functions can be called.
		 * @throws IllegalArgumentException
		 *             if the state being build has a name not recognized by the context
		 *             associated with this builder.
		 */
		Builder startState(String name);

		/**
		 * Add a value to the state being defined by this builder. The state is added in
		 * the k.LAB natural order for the geometry associated with the builder, i.e.
		 * starting at an offset of 0 and moving up by 1 at every add.
		 * 
		 * @param value
		 * @throws IllegalStateException
		 *             if {@link #startState(String)} has not been called.
		 */
		void add(Object value);
		
		/**
		 * Add a value to the state being defined by this builder at the passed locator.
		 * 
		 * @param value
		 * @param locator
		 * @throws IllegalStateException
		 *             if {@link #startState(String)} has not been called.
		 */
		void add(Object value, ILocator locator);

		/**
		 * Finish building a state artifact and return the original builder on which
		 * {@link #startState(String)} was called.
		 * 
		 * @return the builder on which {@link startState()} was called.
		 * @throws IllegalStateException
		 *             if {@link startState()} was not called before.
		 */
		Builder finishState();

		/**
		 * Get a builder that defines an object. Any further operation should operate on
		 * the object until {@link #finishObject()} is called, returning the builder
		 * that gets this call.
		 * <p>
		 * If this was called before at the same level, the new artifact will be chained
		 * to the previous when built.
		 * 
		 * @param artifactName
		 *            the name of the target artifact (obtained through the runtime
		 *            context)
		 * @param objectName
		 *            the name of the object (which should be unique)
		 * @param scale
		 *            the scale for the new object
		 * @return an object builder
		 * @throws IllegalArgumentException
		 *             if the artifact name is not recognized by the context
		 *             associated with this builder.		 
		 */
		Builder startObject(String artifactName, String objectName, IGeometry scale);

		/**
		 * Finishes the object definition and sets the returned context back to the
		 * original builder.
		 * 
		 * @return the builder on which startObject() was called.
		 * @throws IllegalStateException
		 *             if {@link startState()} was not called before.
		 */
		Builder finishObject();

		/**
		 * Set a property for the metadata associated with the artifact being built.
		 * 
		 * @param property
		 * @param object
		 * @return the builder itself
		 */
		Builder withMetadata(String property, Object object);

		/**
		 * Add a notification to the result. Notifications are global, i.e. they refer
		 * to all artifacts built.
		 * 
		 * @param notification
		 * @return the builder itself
		 */
		Builder addNotification(INotification notification);

		/**
		 * Build the final data object.
		 * 
		 * @return the finished data
		 */
		IKlabData build();
	}

	/**
	 * Return the primary artifact that we are meant to build, building it if
	 * necessary.
	 * 
	 * @return the primary artifact for the builder.
	 */
	IArtifact getArtifact();

	/**
	 * Return any notifications passed through a builder. Notifications are a global
	 * list that refers to all artifacts.
	 * 
	 * @return all notifications
	 */
	List<INotification> getNotifications();

	/**
	 * True if errors happened and results should not be used. Normally linked to
	 * the existence of error-level notifications, but implementations can provide
	 * what they prefer.
	 * 
	 * @return true if errors
	 */
	boolean hasErrors();

}
