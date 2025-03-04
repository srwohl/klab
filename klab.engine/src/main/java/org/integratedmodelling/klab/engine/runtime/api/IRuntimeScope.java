package org.integratedmodelling.klab.engine.runtime.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.integratedmodelling.kim.api.IKimExpression;
import org.integratedmodelling.klab.api.actors.IBehavior;
import org.integratedmodelling.klab.api.data.ILocator;
import org.integratedmodelling.klab.api.knowledge.IConcept;
import org.integratedmodelling.klab.api.knowledge.IMetadata;
import org.integratedmodelling.klab.api.knowledge.IObservable;
import org.integratedmodelling.klab.api.observations.IConfiguration;
import org.integratedmodelling.klab.api.observations.IDirectObservation;
import org.integratedmodelling.klab.api.observations.IKnowledgeView;
import org.integratedmodelling.klab.api.observations.IObservation;
import org.integratedmodelling.klab.api.observations.IState;
import org.integratedmodelling.klab.api.observations.scale.IScale;
import org.integratedmodelling.klab.api.provenance.IArtifact;
import org.integratedmodelling.klab.api.resolution.IResolutionScope;
import org.integratedmodelling.klab.api.resolution.IResolutionScope.Mode;
import org.integratedmodelling.klab.api.runtime.IContextualizationScope;
import org.integratedmodelling.klab.api.runtime.IRuntimeProvider;
import org.integratedmodelling.klab.api.runtime.dataflow.IActuator;
import org.integratedmodelling.klab.api.runtime.dataflow.IDataflow;
import org.integratedmodelling.klab.api.runtime.monitoring.IMonitor;
import org.integratedmodelling.klab.components.runtime.observations.Observation;
import org.integratedmodelling.klab.dataflow.Actuator;
import org.integratedmodelling.klab.dataflow.ContextualizationStrategy;
import org.integratedmodelling.klab.dataflow.ObservedConcept;
import org.integratedmodelling.klab.model.Model;
import org.integratedmodelling.klab.owl.ConfigurationDetector;
import org.integratedmodelling.klab.owl.Observable;
import org.integratedmodelling.klab.provenance.Provenance;
import org.integratedmodelling.klab.utils.Pair;

/**
 * This API extends {@link IContextualizationScope} to add setters and other
 * functionalities that are needed at runtime. It is used within common
 * computation code so that any {@link IRuntimeProvider} may implement the
 * computation contexts as needed.
 * 
 * @author Ferd
 */
public interface IRuntimeScope extends IContextualizationScope {

	/**
	 * Return any of the observations created within the context of the root
	 * observation. Must be fast. Relied upon by all methods that retrieve
	 * observations.
	 * 
	 * @param id
	 * @return the requested observation or null.
	 */
	IObservation getObservation(String observationId);

	/**
	 * Called to create the root context for a new dataflow computed within an
	 * existing context. Should inherit root subject, root scope, structure,
	 * provenance etc. from this, but contain an empty artifact catalog.
	 * 
	 * @param scale
	 * @param target
	 * @param scope
	 * @param monitor
	 * @return
	 */
	public IRuntimeScope createContext(IScale scale, IActuator target, IDataflow<?> dataflow, IResolutionScope scope,
			IMonitor monitor);

	/**
	 * Called to create the computation context for any actuator contained in a root
	 * actuator at any level. Create a child context for the passed actuator,
	 * containing a new target observation implementation for the artifact type and
	 * geometry specified in the actuator. The observation is created with the
	 * current target set as its parent.
	 * 
	 * @param scale   scale of the entire context. If the actuator is partial, the
	 *                scale should be revised to the actual sub-scale of
	 *                computation.
	 * @param target  the main target. If the actuator is partial, the target may be
	 *                wrapped to handle the rescaling.
	 * @param scope
	 * @param monitor
	 * @param context the context for the resolution
	 * @return the child context that will resolve the target
	 */
	public IRuntimeScope createChild(IScale scale, IActuator target, IResolutionScope scope, IMonitor monitor);

	/**
	 * Create a child context for the passed observable within the current actuator.
	 * Called when there is a computation involving a different observable than the
	 * actuator's target.
	 * 
	 * @param indirectTarget
	 * @return
	 */
	IRuntimeScope createChild(IObservable indirectTarget);

	/**
	 * Set the passed data object in the symbol table.
	 * 
	 * @param name
	 * @param data
	 */
	void setData(String name, IArtifact data);

	/**
	 * Set POD data or parameters.
	 * 
	 * @param name
	 * @param value
	 */
	void set(String name, Object value);

//	/**
//	 * Each context that handles a temporal scale must expose a configuration
//	 * detector.
//	 * 
//	 * @return the configuration detector for the context
//	 */
//	ConfigurationDetector getConfigurationDetector();

	/**
	 * Produce a deep copy of this context so it can be used for parameters without
	 * affecting the original. The catalog remains unchanged.
	 * 
	 * @return an identical context with a rewriteable object catalog and
	 *         parameters.
	 */
	IRuntimeScope copy();
	//
	// /**
	// * Rename the passed observation data as the passed alias.
	// *
	// * @param name
	// * @param alias
	// */
	// void rename(String name, String alias);

	/**
	 * Set the main target of the computation being carried on by the actuator. Used
	 * by Actuator.compute().
	 * 
	 * @param target
	 */
	void setTarget(IArtifact target);

	/**
	 * The API must be able to reset the geometry for downstream indirectAdapters.
	 * 
	 * @param geometry
	 */
	void setScale(IScale geometry);

	/**
	 * Specialize the provenance so we can use setting methods on it.
	 */
	@Override
	Provenance getProvenance();

	/**
	 * Return the context structure (all father-child relationships) as a graph with
	 * separate logical (observations) and physical (artifact) hierarchies.
	 * 
	 * @return the structure
	 */
	IArtifact.Structure getStructure();

	/**
	 * Return all the children of an artifact in the structural graph that match a
	 * certain class.
	 * 
	 * @param artifact
	 * @param cls
	 * @return the set of all children of class cls
	 */
	<T extends IArtifact> Collection<T> getChildren(IArtifact artifact, Class<T> cls);

	/**
	 * If our catalog contains the artifact we're trying to resolve, return it along
	 * with the name we have for it (which may be local to this scope and not be the
	 * same as the observation's observable's reference name). If the passed
	 * observable matches more than one observation, collect them in an
	 * IObservationGroup (not added to the catalog) before returning them. Returning
	 * more than one observation is possible when the passed observable is abstract
	 * or it's a characterization for an abstract predicate, such as a role or an
	 * identity.
	 * 
	 * @param observable
	 * @return
	 */
	Pair<String, IArtifact> findArtifact(IObservable observable);

	/**
	 * The contextualization strategy is a singleton within the context and is never
	 * null.
	 * 
	 * @return
	 */
	ContextualizationStrategy getContextualizationStrategy();

	// ugly but we don't have a model when we create the first dataflow.
	void setModel(Model model);

	/**
	 * Remove artifact from structure if it's there.
	 * 
	 * @param object
	 */
	void removeArtifact(IArtifact object);

	/**
	 * Create a configuration in the current context, using the passed type and the
	 * trigger observations detected. Called by actuators, never by API users.
	 * 
	 * @param configurationType
	 * @param targets
	 */
	IConfiguration newConfiguration(IConcept configurationType, Collection<IObservation> targets, IMetadata metadata);

	/**
	 * Get a new nonsemantic state for model usage. May be called by contextualizers
	 * when user-relevant intermediate storage is required. If not relevant, raw
	 * storage should be used instead.
	 * 
	 * @param name
	 * @param type
	 * @param scale
	 * @return
	 */
	IState newNonsemanticState(String name, IArtifact.Type type, IScale scale);

	/**
	 * Get the dataflow we're executing.
	 * 
	 * @return
	 */
	IDataflow<?> getDataflow();

	/**
	 * @param directObservation
	 * @return
	 */
	Collection<IArtifact> getChildArtifactsOf(IArtifact directObservation);

	/*
	 * OK, I declare failure for now. No better way to record what has been notified
	 * or not.
	 */
	Set<String> getNotifiedObservations();

	/**
	 * Get the observation group for the main observable in the passed observable.
	 * This will create a main group using the main observable after stripping any
	 * attributes or predicates. If the group was created before, its 'new' flag
	 * (TBD) will have been set to false so that the notifier knows whether to send
	 * the full group data or a change notification. If the observable has
	 * additional predicates, it will then return a view that filters only those.
	 * 
	 * @param observable
	 * @param scale
	 * @return
	 */
	IObservation getObservationGroup(IObservable observable, IScale scale);

	/**
	 * Add the predicate to the passed observation. If the predicate wasn't there
	 * already, resolve it and run the resulting dataflow.
	 * 
	 * @param target
	 * @param predicate a concrete trait or role
	 */
	void newPredicate(IDirectObservation target, IConcept predicate);

	/**
	 * Return a view if needed.
	 * 
	 * @param observable
	 * @param ret
	 * @return
	 */
	IObservation getObservationGroupView(Observable observable, IObservation ret);

	/**
	 * Schedule all the needed actions for this actuator. Called at initialization
	 * after the actuator has computed its initial conditions (first thing if the
	 * actuator observes an occurrent).
	 * 
	 * @param active
	 */
	void scheduleActions(Actuator active);

	/**
	 * Set the scale as specified by the passed locator (which should be a scale and
	 * represent a specific temporal transition) and, according to the target
	 * observations and their view, wrap any context states so that appropriate
	 * temporal mediations are in place when an actuator modifies them.
	 * 
	 * @param transitionScale
	 * @return
	 */
	IRuntimeScope locate(ILocator transitionScale, IMonitor monitor);

	/**
	 * Get all the artifacts known to this context indexed by their local name in
	 * the context of execution. An actuator must have been specified for the
	 * context.
	 * 
	 * @param <T>
	 * @param cls
	 * @return
	 */
	<T extends IArtifact> Map<String, T> getLocalCatalog(Class<T> cls);

	/**
	 * Manually add a state to a target observation, updating structure and
	 * notifying what needs to be notified.
	 * 
	 * @param target     the direct observation that will receive the state.
	 * @param observable for the state. The scale will be the same as the target.
	 * @param data       any kind of content for the state - either a scalar to be
	 *                   redistributed or
	 * @return
	 */
	IState addState(IDirectObservation target, IObservable observable, Object data);

	/**
	 * Resolve the passed observable in the passed context and return the resulting
	 * dataflow. If the observable cannot be resolved, return null without error.
	 * Must cache dataflows by scale and retrieve them quickly as it may be called
	 * many times at each new direct observation.
	 * 
	 * @param observable
	 * @param context
	 * @param task       the task to register the resolution to
	 * @return a dataflow to resolve the observable, or null if there is no coverage
	 */
	<T extends IArtifact> T resolve(IObservable observable, IDirectObservation context, ITaskTree<?> task, Mode mode,
			IDataflow<?> parentDataflow);

	/**
	 * Schedule any actions tagged as scheduled in the behavior of the passed
	 * observation.
	 * 
	 * @param observation
	 * @param behavior
	 */
	void scheduleActions(Observation observation, IBehavior behavior);

	/**
	 * Bindings between observables and behaviors can be made by actors at runtime.
	 * If that happens, the bindings will be stored in the runtime scope. They are
	 * returned as a map (in the default implementation, that will be an
	 * "intelligent" map so that inference is used in attributing the behaviors.
	 * 
	 * @return
	 */
	Map<IConcept, Pair<String, IKimExpression>> getBehaviorBindings();

	/**
	 * Scopes must maintain a synchronized set of IDs for all observations that are
	 * being watched by the view. This is subscribed to through messages sent to the
	 * session that owns the observations.
	 * 
	 * @return
	 */
	public Set<String> getWatchedObservationIds();

	/**
	 * Send any notifications pertaining to this observation to the clients that are
	 * watching. Should call {@link #isNotifiable(IObservation)} to ensure that the
	 * observation is watched.
	 * 
	 * @param observation
	 */
	void updateNotifications(IObservation observation);

	/**
	 * Swap the passed artifact in all the graphs maintained by the context
	 * (structure, provenance). Should be only called with states.
	 * 
	 * @param ret
	 * @param result
	 */
	void swapArtifact(IArtifact ret, IArtifact result);

	/**
	 * Called by actuators after contextualization is complete for any observation.
	 * 
	 * @param object
	 */
	void notifyListeners(IObservation object);

	/**
	 * If true, the root context has seen occurrent observations or occurrent scales
	 * at some point, so the occurrence of anything that is resolved or computed
	 * next will have to be consider. The occurrent status is global across a
	 * context and cannot be revoked after setting.
	 */
	boolean isOccurrent();

	/**
	 * Create and return a shallow copy of the observation catalog optimized for
	 * quick search with ObservedConcept.
	 * 
	 * @return
	 */
	Map<ObservedConcept, IObservation> getCatalog();

	/**
	 * Add a view after it was computed. The result is typically compiled text, such
	 * as HTML or Markdown, which can be sent or inserted in a report, but may also
	 * be some lazy evaluator that will need to be recognizable and handled at the
	 * scope side. The scope's action would be to make it available for insertion in
	 * reports and/or to send to clients for quick display. More complex
	 * functionalities may use the view to handle actions such as exporting.
	 * 
	 * @param view
	 */
	void addView(IKnowledgeView view);

	/**
	 * Get the scope to contextualize a changing resource from that used to
	 * contextualize its change. The result will have the target artifact, semantics
	 * and artifact name set properly according to the context.
	 * 
	 * @return
	 */
	IRuntimeScope targetForChange();

	/**
	 * Recontextualize the scope to a specific target observation. Used to compute
	 * the inferred dependents of a changed observation after a temporal transition.
	 * 
	 * @param target
	 * @return
	 */
	IRuntimeScope targetToObservation(IObservation target);

	/**
	 * Find the name of an artifact in this context. Return null if the artifact is
	 * not in the catalog.
	 * 
	 * @param previous
	 * @return
	 */
	String getArtifactName(IArtifact previous);

	/**
	 * Resolve an abstract identity with one or more concrete ones, so that abstract
	 * dependencies can be expanded properly.
	 * 
	 * @param toResolve
	 * @param traits
	 */
	void setConcreteIdentities(IConcept abstractIdentity, List<IConcept> concreteIdentities);

	/**
	 * Get the concretized identities for the passed predicates. This should be
	 * lenient to a difference in inherency between the passed predicate and the
	 * resolved ones.
	 * 
	 * @param predicate
	 * @return
	 */
	Collection<IConcept> getConcreteIdentities(IConcept predicate);

	/**
	 * Return all the views produced in this scope.
	 * 
	 * @return
	 */
	Collection<IKnowledgeView> getViews();

	/**
	 * Set a local scale in the scope, for actuators that have merged partial
	 * coverages. The coverage may only contain some of the extents - any missing
	 * should be copied from the current scope scale.
	 * 
	 * @param scale
	 * @return
	 */
	IRuntimeScope withCoverage(IScale scale);

	/**
	 * Return all artifacts of the passed class whose observable is affected or
	 * created by the process type indicated. These are either directly affected
	 * (stated in semantics) or describe an observable that is affected.
	 * 
	 * @param <T>
	 * @param processType
	 * @param cls
	 * @return
	 */
	<T extends IArtifact> Collection<T> getAffectedArtifacts(IConcept processType, Class<T> cls);

}
