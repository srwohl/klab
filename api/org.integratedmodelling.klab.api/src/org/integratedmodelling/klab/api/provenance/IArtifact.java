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
package org.integratedmodelling.klab.api.provenance;

import java.util.Collection;
import java.util.List;

import org.integratedmodelling.kim.api.IKimModel;
import org.integratedmodelling.klab.api.data.IGeometry;
import org.integratedmodelling.klab.api.data.artifacts.IDataArtifact;
import org.integratedmodelling.klab.api.data.artifacts.IObjectArtifact;
import org.integratedmodelling.klab.api.knowledge.IConcept;
import org.integratedmodelling.klab.api.knowledge.IMetadata;
import org.integratedmodelling.klab.api.knowledge.IObservable;
import org.integratedmodelling.klab.api.model.IAnnotation;
import org.integratedmodelling.klab.api.observations.IDirectObservation;
import org.integratedmodelling.klab.api.observations.IObservation;
import org.integratedmodelling.klab.api.observations.ISubject;
import org.integratedmodelling.klab.utils.Range;

/**
 * An Artifact can be any of the first-class products of a k.LAB task: a
 * non-semantic {@link IDataArtifact} or {@link IObjectArtifact}, an observed
 * (semantic) {@link IObservation} (as produced by most activities in k.LAB) or
 * a {@link IKimModel k.IM model description} when the model has been produced
 * by an observation activity, such as a learning model.
 * <p>
 * By implementing {@link java.lang.Iterable}, we also allow Artifacts to
 * represent groups of artifacts (e.g. all the {@link ISubject subjects}
 * instantiated by resolving a subject {@link IObservable observable}). This
 * enables simpler handling of provenance, as each observation activity returns
 * one artifact, possibly iterable as a group.
 * <p>
 * From an OPM perspective, IArtifact is the equivalent of Entity, including its
 * specialization into Bundle if size() > 1.
 * <p>
 * Each artifact exposes the provenance graph it's part of, allowing all k.LAB
 * tasks to simply return any {@code IArtifact} and provide full information on
 * what happened.
 * <p>
 *
 * @author Ferd
 * @version $Id: $Id
 */
public interface IArtifact extends IProvenance.Node, Iterable<IArtifact> {

	/**
	 * Type contextualized by the actor. Mimics IKdlActuator.Type for now, should be
	 * integrated with it.
	 * 
	 * @author ferdinando.villa
	 *
	 */
	enum Type {
		/**
		 * Contextualizes number states.
		 */
		NUMBER,
		/**
		 * Contextualizes presence/absence states
		 */
		BOOLEAN,
		/**
		 * Contextualizes category states
		 */
		CONCEPT,
		/**
		 * Contextualizes processes, an occurrent so the resolution only happens at
		 * transitions and not at initialization. Contextualizers must have T geometry.
		 */
		PROCESS,
		/**
		 * The other occurrent, this time an instantiator. Instantiators must have T
		 * geometry and only run at transitions.
		 */
		EVENT,
		/**
		 * Instantiates or contextualizes objects, according to arity.
		 */
		OBJECT,
		/**
		 * Produces text values, to be transformed by successive contextualizers.
		 * Illegal in contracts.
		 */
		TEXT,
		/**
		 * Contextualizes any quality. Only legal in contracts.
		 */
		VALUE,
		/**
		 * Produces range values. Only legal in parameters
		 */
		RANGE,
		/**
		 * Produce one of a set of values. Only legal in parameters, values are
		 * specified externally.
		 */
		ENUM,
		/**
		 * Produce extents other than time or space
		 */
		EXTENT,
		/**
		 * Produce temporal extents
		 */
		TEMPORALEXTENT,
		/**
		 * Produce spatial extents
		 */
		SPATIALEXTENT,
		/**
		 * Specify annotation contracts
		 */
		ANNOTATION,
		/**
		 * A list value
		 */
		LIST,
		/**
		 * No value - used only for options in command prototypes
		 */
		VOID,

		/**
		 * Tables are supersets of maps so map literals are valid tables. A table is a
		 * valid literal for an OBJECT input.
		 */
		TABLE;

		/**
		 * Classify a POD type producing the type that represents it.
		 * 
		 * @param o
		 * @return a type for o. If o == null, VALUE is returned.
		 */
		public static Type classify(Object o) {
			if (o instanceof Number) {
				return NUMBER;
			} else if (o instanceof Boolean) {
				return BOOLEAN;
			} else if (o instanceof String) {
				return TEXT;
			} else if (o instanceof Range) {
				return RANGE;
			} else if (o instanceof List) {
				return LIST;
			}
			return VALUE;
		}

		public boolean isState() {
			return this == NUMBER || this == BOOLEAN || this == TEXT || this == CONCEPT || this == VALUE;
		}

		public boolean isNumeric() {
			return this == NUMBER;
		}

		public static boolean isCompatible(Type required, Type supplied) {

			// unknown/universal
			if (supplied == Type.VALUE || required == Type.VALUE) {
				return true;
			}

			if (required == supplied) {
				return true;
			} else if (required == BOOLEAN && supplied == NUMBER) {
				// runtime cast can do this
				return true;
			}

			// TODO probably needs improvement
			return false;
		}

		public boolean isCountable() {
			return this == EVENT || this == OBJECT;
		}

		public boolean isOccurrent() {
			return this == EVENT || this == PROCESS;
		}

	}

	/**
	 * The geometry linked to the observation. Observed artifacts will specialize
	 * this as IScale.
	 *
	 * @return the geometry
	 */
	IGeometry getGeometry();

	/**
	 * Metadata. Never null, possibly empty.
	 *
	 * @return the metadata
	 */
	IMetadata getMetadata();

	/**
	 * <p>
	 * getUrn.
	 * </p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	String getUrn();

	/**
	 * All the annotations proceeding from the k.IM lineage of this artifact (from
	 * the model that produced it, the concepts it incarnates, etc.). Never null,
	 * possibly empty.
	 * <p>
	 * When artifacts are persisted, these may or may not be preserved.
	 * 
	 * @return k.IM annotations in the lineage of this artifact.
	 */
	Collection<IAnnotation> getAnnotations();

	/**
	 * <p>
	 * getConsumer.
	 * </p>
	 *
	 * @return a {@link org.integratedmodelling.klab.api.provenance.IAgent} object.
	 */
	IAgent getConsumer();

	/**
	 * <p>
	 * getOwner.
	 * </p>
	 *
	 * @return a {@link org.integratedmodelling.klab.api.provenance.IAgent} object.
	 */
	IAgent getOwner();

	/**
	 * The activity (process) that generated the artifact.
	 * 
	 * @return
	 */
	IActivity getGenerator();

	/**
	 * Antecedents are the sources of a 'derivedBy' relationship.
	 * 
	 * <p>
	 * getAntecedents.
	 * </p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	Collection<IArtifact> getAntecedents();

	/**
	 * Consequents are the targets of a 'derivedBy' relationship.
	 * 
	 * <p>
	 * getConsequents.
	 * </p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	Collection<IArtifact> getConsequents();

	/**
	 * Trace the nearest artifact of the passed concept (or with the passed
	 * role/trait) up the provenance chain.
	 *
	 * @param concept a {@link org.integratedmodelling.klab.api.knowledge.IConcept}
	 *                object.
	 * @return a {@link org.integratedmodelling.klab.api.provenance.IArtifact}
	 *         object.
	 */
	IArtifact trace(IConcept concept);

	/**
	 * Collect all artifacts of the passed concept (or with the passed role/trait)
	 * up the provenance chain.
	 *
	 * @param concept a {@link org.integratedmodelling.klab.api.knowledge.IConcept}
	 *                object.
	 * @return a {@link java.util.Collection} object.
	 */
	Collection<IArtifact> collect(IConcept concept);

	/**
	 * Trace the nearest artifact with the passed role within the passed observation
	 * up the provenance chain.
	 *
	 * @param role
	 * @param roleContext a
	 *                    {@link org.integratedmodelling.klab.api.observations.IDirectObservation}
	 *                    object.
	 * @return a {@link org.integratedmodelling.klab.api.provenance.IArtifact}
	 *         object.
	 */
	IArtifact trace(IConcept role, IDirectObservation roleContext);

	/**
	 * Some artifact may be connected into a hierarchical structure, which may or
	 * may not be the same as the natural structure of the specific type of
	 * artifact.
	 * 
	 * @return the child artifacts or an empty list
	 */
	Collection<IArtifact> getChildArtifacts();

	/**
	 * Collect all artifacts with the passed role within the passed observation up
	 * the provenance chain.
	 *
	 * @param role
	 * @param roleContext a
	 *                    {@link org.integratedmodelling.klab.api.observations.IDirectObservation}
	 *                    object.
	 * @return a {@link java.util.Collection} object.
	 */
	Collection<IArtifact> collect(IConcept role, IDirectObservation roleContext);

	/**
	 * The size of the group that this artifact is part of. Any artifact is part of
	 * a group including at least itself.
	 *
	 * @return 1 or more
	 */
	int groupSize();

	/**
	 * Any observation that exists has provenance. Call this on the root observation
	 * for the entire graph.
	 *
	 * @return the provenance record leading to this
	 */
	IProvenance getProvenance();

	/**
	 * The type of this artifact. Types are a small set meant to enable more
	 * efficient storage and correct contextualization.
	 * 
	 * @return the type
	 */
	Type getType();

	/**
	 * Call when the artifact can be disposed of. This should schedule the removal
	 * of any storage and free any resources without terminating the object itself,
	 * according to the implementation of the storage provider. Calling release() is
	 * optional and should be done only on temporary artifacts with a well-defined
	 * life span.
	 * 
	 */
	void release();

	/**
	 * We leave specific views of artifacts flexible without specializing the base
	 * class through a simple adaptation mechanism, suitable for PODs or more
	 * complex objects. This method, paired with {@link #as(Class)}, enables
	 * checking for adaptability to specific types.
	 * 
	 * @param cls
	 * @return true if the artifact can be adapted to the passed type.
	 */
	boolean is(Class<?> cls);

	/**
	 * Use after {@link #is(Class)} has returned true to adapt to the corresponding
	 * object of that type.
	 * 
	 * @param cls
	 * @return the specific class requested
	 */
	<T> T as(Class<?> cls);

	/**
	 * True if the artifact is not a true artifact but an archetype for others. In
	 * that case, its specifics will be used to infer general properties that will
	 * be used for other artifacts.
	 * <p>
	 * So far used in learning models that learn contextualized observables, where
	 * no actual state is learned in the context of learning (the actuator is void)
	 * but the semantics of the learned state in each instantiated object is passed
	 * through an archetype.
	 * <p>
	 * 
	 * @return true if archetype. In this case, no use should be made of the
	 *         artifact other than inspection of the relevant specifics.
	 */
	boolean isArchetype();
}
