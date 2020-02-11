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
package org.integratedmodelling.klab.api.runtime;

import java.util.Collection;
import java.util.Map;

import org.integratedmodelling.kim.api.IKimConcept;
import org.integratedmodelling.kim.api.IParameters;
import org.integratedmodelling.klab.api.data.artifacts.IObjectArtifact;
import org.integratedmodelling.klab.api.data.general.IExpression;
import org.integratedmodelling.klab.api.documentation.IReport;
import org.integratedmodelling.klab.api.knowledge.IConcept;
import org.integratedmodelling.klab.api.knowledge.IMetadata;
import org.integratedmodelling.klab.api.knowledge.IObservable;
import org.integratedmodelling.klab.api.model.IModel;
import org.integratedmodelling.klab.api.model.INamespace;
import org.integratedmodelling.klab.api.model.contextualization.IInstantiator;
import org.integratedmodelling.klab.api.observations.IDirectObservation;
import org.integratedmodelling.klab.api.observations.IObservation;
import org.integratedmodelling.klab.api.observations.IRelationship;
import org.integratedmodelling.klab.api.observations.IState;
import org.integratedmodelling.klab.api.observations.scale.IScale;
import org.integratedmodelling.klab.api.provenance.IArtifact;
import org.integratedmodelling.klab.api.provenance.IProvenance;
import org.integratedmodelling.klab.api.runtime.dataflow.IDataflow;
import org.integratedmodelling.klab.api.runtime.monitoring.IMonitor;
import org.integratedmodelling.klab.exceptions.KlabException;
import org.integratedmodelling.klab.utils.Pair;

/**
 * The contextualization scope holds all information about the computation being
 * run and the artifacts computed this far. It is passed to dataflows and down
 * to actuators and contextualizers, customized to reflect names, states and
 * locators as each actuator expects them.
 *
 * The {@link org.integratedmodelling.kim.api.IParameters} methods access
 * user-defined parameters, including any passed to the calling functions or
 * URNs and, if appropriate, context-localized POD values for states (e.g. the
 * specific values at the point of computation). The actual input and output
 * artifacts are always available through {@link #getArtifact(String)}.
 *
 * @author Ferd
 */
public interface IContextualizationScope extends IParameters<String> {

	/**
	 * The namespace of reference in this context. Usually that of the running model
	 * or observer.
	 *
	 * @return the namespace of reference. Null in empty contexts or during
	 *         non-semantic computations.
	 */
	INamespace getNamespace();

	/**
	 * <p>
	 * getProvenance.
	 * </p>
	 *
	 * @return the provenance graph. Null in an empty context.
	 */
	IProvenance getProvenance();

	/**
	 * Return all the observable that depend directly on the passed one in this
	 * context.
	 * 
	 * @param observable
	 * @return
	 */
	Collection<IObservable> getDependents(IObservable observable);

	/**
	 * Return all the observables that the passed one depends directly on in this
	 * context.
	 * 
	 * @param observable
	 * @return
	 */
	Collection<IObservable> getPrecursors(IObservable observable);

	/**
	 * <p>
	 * getEventBus.
	 * </p>
	 *
	 * @return the event bus. Null in an empty context.
	 */
	IEventBus getEventBus();

	/**
	 * Return the scheduler for the context, null if non-temporal, create it if not
	 * there if temporal. The scheduler must be a singleton for all the actuators
	 * implied in a contextualization: whichever child creates the scheduler must do
	 * that in the root node.
	 * 
	 * @return a scheduler or null.
	 */
	IScheduler getScheduler();

	/**
	 * There is one report per root context. Actuators will add sections to it as
	 * models are computed, based on the documentation templates associated with
	 * models and their parts. The report can be compiled and rendered at any time.
	 * 
	 * @return
	 */
	IReport getReport();

	/**
	 * During a contextualization there normally is a dataflow being run. This will
	 * only be null only in special situations, e.g. when expressions are passed a
	 * convenience context in order to be evaluated outside of contextualization.
	 * 
	 * @return
	 */
	IDataflow<?> getDataflow();

	/**
	 * Inspect the network graph of the current context, returning all relationships
	 * that have the passed subject as target.
	 *
	 * @param observation a
	 *                    {@link org.integratedmodelling.klab.api.observations.IDirectObservation}
	 *                    object.
	 * @return a {@link java.util.Collection} object.
	 */
	Collection<IRelationship> getOutgoingRelationships(IDirectObservation observation);

	/**
	 * Inspect the network graph of the current context, returning all relationships
	 * that have the passed subject as target.
	 *
	 * @param observation a
	 *                    {@link org.integratedmodelling.klab.api.observations.IDirectObservation}
	 *                    object.
	 * @return a {@link java.util.Collection} object.
	 */
	Collection<IRelationship> getIncomingRelationships(IDirectObservation observation);

	/**
	 * Even computations that have more than one output have a single target
	 * artifact, which corresponds to the primary observable of the model that has
	 * defined the computation. If the computation is an instantiation, the target
	 * artifact is null.
	 *
	 * @return the target artifact, or null in instantiation computations.
	 */
	IArtifact getTargetArtifact();

	/**
	 * Return any additional output artifacts implied by the current scope. If
	 * resources are used, the names of these artifacts must match attribute names
	 * (for object resources) or output names in the resource.
	 * 
	 * @return any output artifacts, or an empty list.
	 */
	Collection<IArtifact> getAdditionalOutputs();

	/**
	 * Get the resolved {@link IArtifact object} corresponding to the passed local
	 * name.
	 *
	 * @param localName a {@link java.lang.String} object.
	 * @return the artifact, null if not found.
	 */
	IArtifact getArtifact(String localName);

	/**
	 * Get the resolved {@link IArtifact object} corresponding to the passed local
	 * name as an object of the passed class. If the artifact is not there or it is
	 * not of a compatible type, return null with no error.
	 *
	 * @param localName a {@link java.lang.String} object.
	 * @param cls       a {@link java.lang.Class} object.
	 * @return the artifact, null if not found or not of passed class.
	 * @param <T> a T object.
	 */
	<T extends IArtifact> T getArtifact(String localName, Class<T> cls);

	/**
	 * Get the resolved artifact that observes the passed concept and disambiguate
	 * any multiple matches by choosing the closest to the model being
	 * contextualized, if any is (i.e. choose the one that is a dependency of the
	 * model returned by {@link #getModel()} if applicable).
	 * 
	 * @param <T>
	 * @param concept
	 * @param cls
	 * @return
	 */
	<T extends IArtifact> T getArtifact(IConcept concept, Class<T> cls);

	/**
	 * Return all known artifacts of the passed class along with their ID in this
	 * context. For example, all data artifacts known at the time of computation can
	 * be retrieved using <code>getData(IDataArtifact.class)</code>.
	 *
	 * @param type a {@link java.lang.Class} object.
	 * @return a collection of pair <name, artifact>, possibly empty, never null.
	 * @param <T> a T object.
	 */
	<T extends IArtifact> Collection<Pair<String, T>> getArtifacts(Class<T> type);

	/**
	 * Get the resolved {@link IArtifact artifacts} corresponding to the observable
	 * concept. Use {@link IParameters IParameters get methods} to retrieve
	 * contextualized values for states or parameters.
	 *
	 * @param localName a {@link java.lang.String} object.
	 * @return the artifact, null if not found.
	 */
	Collection<IArtifact> getArtifact(IConcept observable);

	/**
	 * Return the model being computed, if any.
	 * 
	 * @return a model or null.
	 */
	IModel getModel();

	/**
	 * Return a valid monitor for any communication.
	 *
	 * @return the monitor for this computation. Never null.
	 */
	IMonitor getMonitor();

	/**
	 * Return the concept type describing the artifact being computed.
	 *
	 * @return the type of the observation
	 */
	IKimConcept.Type getArtifactType();

	/**
	 * Return the geometry for the computation (in k.LAB typically a
	 * {@link org.integratedmodelling.klab.api.observations.scale.IScale}). The
	 * scale is for the CURRENT computation, which may be different from the scale
	 * of the full resolution. To retrieve the latter, use
	 * getDataflow().getResolutionScale();
	 *
	 * @return the current geometry. Should never be null.
	 */
	public IScale getScale();

	/**
	 * Get the semantics for the passed identifier, which must be one of those
	 * returned by either {@link #getInputs()} or {@link #getOutputs()}.
	 *
	 * @param identifier the identifier
	 * @return the observable linked to the identifier
	 * @throws java.lang.IllegalArgumentException if the identifier is unknown
	 */
	public IObservable getSemantics(String identifier);

	/**
	 * Create and resolve a new observation of the specified countable observable
	 * and with the specified geometry. Use in {@link IInstantiator instantiators}
	 * to create new objects. Use
	 * {@link #newRelationship(IObservable, IScale, IObjectArtifact, IObjectArtifact)}
	 * to create a relationship.
	 * <p>
	 * While any k.LAB-aware implementation will receive a
	 * {@link org.integratedmodelling.klab.api.observations.scale.IScale} instead of
	 * a {@link org.integratedmodelling.klab.api.data.IGeometry} and return a
	 * {@link org.integratedmodelling.klab.api.observations.ICountableObservation}
	 * rather than just
	 * {@link org.integratedmodelling.klab.api.data.artifacts.IObjectArtifact}, we
	 * keep the basic, non-semantic types in the signature for consistency with
	 * derived APIs of remote services and other non-semantic computations.
	 * <p>
	 * As the runtime provider is responsible for creating the
	 * {@code IComputationContext}, this is where it can control the type and
	 * features of any new object created.
	 * <p>
	 *
	 * @param observable a
	 *                   {@link org.integratedmodelling.klab.api.knowledge.IObservable}
	 *                   object.
	 * @param name       a {@link java.lang.String} object.
	 * @param scale      a
	 *                   {@link org.integratedmodelling.klab.api.observations.scale.IScale}
	 *                   object.
	 * @param metadata   metadata for the new observation, which may be used during
	 *                   resolution if specified by a model with attribute-driven
	 *                   observations. May be null.
	 * @return a new observation for the observable and geometry
	 * @throws org.integratedmodelling.klab.exceptions.KlabException from the
	 *                                                               resolution
	 * @throw IllegalArgumentException if the observable describes a non-countable
	 *        or a relationship.
	 */
	IObjectArtifact newObservation(IObservable observable, String name, IScale scale, IMetadata metadata)
			throws KlabException;

	/**
	 * Create and resolve a new observation of the specified relationship with with
	 * the specified geometry, source and target subjects. Use in
	 * {@link IInstantiator relationship instantiators} to create new objects.
	 * <p>
	 * See {@link #newObservation(IObservable, String, IScale)} for API design
	 * choices.
	 * <p>
	 *
	 * @param observable a
	 *                   {@link org.integratedmodelling.klab.api.knowledge.IObservable}
	 *                   object.
	 * @param name       as direct observations, all relationships have a name
	 * @param scale      a
	 *                   {@link org.integratedmodelling.klab.api.observations.scale.IScale}
	 *                   object.
	 * @param source     a
	 *                   {@link org.integratedmodelling.klab.api.data.artifacts.IObjectArtifact}
	 *                   object.
	 * @param target     a
	 *                   {@link org.integratedmodelling.klab.api.data.artifacts.IObjectArtifact}
	 *                   object.
	 * @param metadata   metadata for the new observation, which may be used during
	 *                   resolution if specified by a model with attribute-driven
	 *                   observations. May be null.
	 * @return a new observation for the observable and geometry
	 * @throw IllegalArgumentException if the observable does not describe a
	 *        relationship.
	 */
	IObjectArtifact newRelationship(IObservable observable, String name, IScale scale, IObjectArtifact source,
			IObjectArtifact target, IMetadata metadata);

	/**
	 * Get the semantics for the primary target of the computation.
	 *
	 * @return observable being described
	 */
	IObservable getTargetSemantics();

	/**
	 * Return the local name of the target artifact, which is also the name of the
	 * actuator being computed.
	 * 
	 * @return the name of the target artifact
	 */
	String getTargetName();

	/**
	 * Return the source of the passed relationship.
	 * 
	 * @param relationship
	 * @return a subject, never null
	 */
	IDirectObservation getSourceSubject(IRelationship relationship);

	/**
	 * Return the target of the passed relationship.
	 * 
	 * @param relationship
	 * @return a subject, never null
	 */
	IDirectObservation getTargetSubject(IRelationship relationship);

	/**
	 * The context observation we are being computed into. Null if the context is
	 * for the computation of the root observation.
	 * 
	 * @return the context observation or null
	 */
	IDirectObservation getContextObservation();

	/**
	 * Return the parent of the passed observation. The runtime context maintains
	 * the structure graph.
	 * 
	 * @param observation
	 * @return the parent, or null if root subject
	 */
	IDirectObservation getParentOf(IObservation observation);

	/**
	 * Return all children of the passed observation. The runtime context maintains
	 * the structure graph.
	 * 
	 * @param observation an observation. {@link IState States} have no children but
	 *                    no error should be raised.
	 * @return the parent, or an empty collection if no children
	 */
	Collection<IObservation> getChildrenOf(IObservation observation);

	/**
	 * A context may have auxiliary named variables created by models, which will be
	 * available to contextualizers. These should be private to the models, so only
	 * visible within their scope.
	 * 
	 * @return the symbol table, never null.
	 */
	Map<String, IVariable> getVariables();

	/**
	 * Create a context to compile an expression.
	 * 
	 * @return
	 */
	IExpression.Context getExpressionContext();

}
