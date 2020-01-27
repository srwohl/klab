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
package org.integratedmodelling.klab.api.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.integratedmodelling.kim.api.IContextualizable;
import org.integratedmodelling.kim.api.IKimStatement.Scope;
import org.integratedmodelling.klab.api.data.IGeometry;
import org.integratedmodelling.klab.api.documentation.IDocumentation;
import org.integratedmodelling.klab.api.knowledge.IMetadata;
import org.integratedmodelling.klab.api.knowledge.IObservable;
import org.integratedmodelling.klab.api.resolution.IComputationProvider;
import org.integratedmodelling.klab.api.resolution.IResolvable;

/**
 * A Model is a statement that produces a computed observation. It has at least
 * one observable. The k.LAB runtime looks for models when resolving a concept
 * that has been requested to be observed within a context (the root context is
 * always acknowledged through an
 * {@link org.integratedmodelling.klab.api.model.IObserver}).
 *
 * Models may be unresolved (extensional, i.e. they leave their observable
 * specified only at a semantic level) or resolved (intensional, i.e. they can
 * build their observations as they are and provide observation semantics for
 * it). Models may need to make additional observations, which are specified as
 * pure semantics through {@link #getDependencies() dependencies}.
 *
 * @author ferdinando.villa
 * @version $Id: $Id
 */
public interface IModel extends IActiveKimObject, INamespaceQualified, IResolvable, IComputationProvider {

	/**
	 * Annotation marking the predictor in learning models.
	 */
	public static final String PREDICTOR_ANNOTATION = "predictor";

	/**
	 * Annotation marking the archetype in learning models. If missing, the
	 * archetype is created from the observable.
	 */
	public static final String ARCHETYPE_ANNOTATION = "archetype";

	/**
	 * Return the semantics of all observables we are observing. The first in the
	 * list is the actual observable and must exist; the others are expected
	 * side-effects of observing the first, which must be connected to semantics
	 * (i.e. they are specified along with observers in the language, or have
	 * correspondent, unambiguous models in the same namespace).
	 *
	 * Secondary observables must be qualities even in agent models.
	 *
	 * @return all the observables in order of declaration.
	 */
	List<IObservable> getObservables();

	/**
	 * If the model is a semantic annotation for a resource, such as a value,
	 * service or URN, return the resources as computables for later resolution.
	 * These include anything described directly (model resource as ...) and any
	 * others given in the 'using' statement. Since 0.10.0 resources may be
	 * multiple, be identified by aliases, and cross-reference each other in
	 * computation.
	 *
	 * The resource also contains any metadata for created objects, such as their
	 * name, no-data values etc. Resources given in the 'using' clause may have
	 * requirements (stated syntactically as alias names); all need to be fully
	 * resolved within the model by dependencies or other resources.
	 *
	 * @return the resources that this model provides semantics for. Possibly empty,
	 *         never null.
	 */
	List<IContextualizable> getResources();

	/**
	 * The asserted semantics for any observation needed in order to produce
	 * observations of the observables. More dependencies may be added at resolution
	 * through context-dependent inference.
	 *
	 * @return all observables required.
	 */
	List<IObservable> getDependencies();

	/**
	 * This will only be called in models that produce objects (isInstantiator() ==
	 * true) and have defined observers for attributes of the objects produced. For
	 * each attribute name returned, the method getAttributeObserver() must return a
	 * valid observer. Some of the attributes may be internally generated: for
	 * example, it is always possible to infer 'presence of' an object from an
	 * observation of the object itself.
	 *
	 * @return the list of dereified attributes with their observers
	 */
	Map<String, IObservable> getAttributeObservables();

	/**
	 * Get the name with which the passed observable is known within this model. The
	 * passed observable's name reported by
	 * {@link org.integratedmodelling.klab.api.knowledge.IObservable#getLocalName()}
	 * may be different as the same observation could come from a different model.
	 *
	 * @param observable a
	 *                   {@link org.integratedmodelling.klab.api.knowledge.IObservable}
	 *                   object.
	 * @return the name with which the passed observable (or one compatible with it)
	 *         is known in this model. If the observable isn't found in the model,
	 *         this method should return the passed observable's local name, not
	 *         null.
	 */
	String getLocalNameFor(IObservable observable);

	/**
	 * Return true if this model can be computed on its own and has associated data.
	 * Normally this amounts to having a data/object source or an instantiator with
	 * getDependencies().size() == 0, but implementations may provide faster ways to
	 * inquire (e.g. without fetching the resource).
	 *
	 * Should really be named isIntensional() but let's stop the good terminology at
	 * reification to keep the API readable by the non-philosopher.
	 *
	 * @return true if model is a leaf in a dependency tree.
	 */
	boolean isResolved();

	/**
	 * True if the model is an instantiator, i.e. produces objects ('model each' in
	 * k.IM). If it is not, it's an 'explanatory' model, which explains/computes an
	 * existing observation by providing a narrative for its observation, rather
	 * than producing new observations.
	 *
	 * @return true if model creates direct observations
	 */
	boolean isInstantiator();

	/**
	 * True if the model is expected to contextually reinterpret its observable
	 * using a role. If the role is abstract, the model will try to establish the
	 * correspondent concrete role from the chain of provenance at runtime.
	 * Observations will be made using an independently resolved, non-roled model
	 * unless the model is already resolved.
	 *
	 * @return true if the model reinterprets the observable through a role.
	 */
	boolean isReinterpreter();

	/**
	 * A learning model must produce a model as its primary artifact.
	 * 
	 * @return
	 */
	boolean isLearning();

	/**
	 * Called by the resolver before a model is used so that it has a chance to
	 * verify runtime availability before being chosen for resolution.
	 *
	 * @return true if everything is OK for computation
	 */
	boolean isAvailable();

	/**
	 * Models may have special documentation templates tied to contextualization
	 * events. If so, they are exposed as metadata, where the key for each template
	 * defines the event tied to the reporting. Each model can only have one
	 * template, but it can also collect templates from other objects it uses, such
	 * as lookup tables or observables.
	 *
	 * @return the declared documentation, or an empty collection if none exists.
	 */
	Collection<IDocumentation> getDocumentation();

	/**
	 * Metadata can be associated to models in k.IM.
	 *
	 * @return metadata (never null, possibly empty).
	 */
	IMetadata getMetadata();

	/**
	 * If not PUBLIC, the model or the namespace containing it are private to its
	 * declared scope, and only models in the same scope can use it for resolution.
	 *
	 * @return true if private
	 */
	Scope getScope();

	/**
	 * If true, the model is semantically and syntactically valid but has been
	 * deactivated (voided) either explicitly by the user or by the validator after
	 * finding unavailable resources, and will not be used, selected or validated
	 * further.
	 * 
	 * @return true if inactive
	 */
	boolean isInactive();

	/**
	 * True if model has concepts associated. Only false for the private
	 * non-semantic models.
	 * 
	 * @return true if semantic
	 */
	boolean isSemantic();

	/**
	 * The geometry implicitly declared for the model, gathered from the resources
	 * and the services used in it. Does not include the explicit contextualization
	 * (over space/time) that comes from the behavior and must be compatible with it
	 * at validation.
	 * 
	 * @return the implicit geometry from the resources, or null.
	 */
	IGeometry getGeometry();

//	/**
//	 * If true, this model uses the 'merging' clause to merge resources or other
//	 * models into a change model for the inherent observable. Such models have the
//	 * same observable as an output instead of a dependency, as normal change models
//	 * do.
//	 * 
//	 * @return
//	 */
//	boolean isResourceMerger();

}
