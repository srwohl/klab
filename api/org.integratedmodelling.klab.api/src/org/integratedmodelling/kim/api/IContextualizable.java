package org.integratedmodelling.kim.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.integratedmodelling.klab.api.data.IGeometry;
import org.integratedmodelling.klab.api.knowledge.IObservable;
import org.integratedmodelling.klab.api.provenance.IArtifact;
import org.integratedmodelling.klab.api.resolution.IResolutionScope.Mode;
import org.integratedmodelling.klab.api.runtime.dataflow.IDataflowNode;
import org.integratedmodelling.klab.utils.Pair;

/**
 * A contextualizable is the declaration of a resource that can be compiled into
 * a processing step for a dataflow. In k.IM this can represent:
 * <p>
 * <ul>
 * <li>a literal value;</li>
 * <li>the URN for a data source or computation;</li>
 * <li>a service call explicitly given in k.IM;</li>
 * <li>an executable expression in a supported language;</li>
 * <li>a classification or lookup table;</li>
 * <li>a conversion between a source and a target value semantics (e.g. unit or
 * currency)</li>
 * </ul>
 * <p>
 * Contextualizables have an artifact type and a declared geometry which
 * determines which phases of a dataflow they apply to.
 * <p>
 * It is the runtime's task to turn any computable resource into a uniform k.DL
 * service call. The call produces a IContextualizer that is inserted in a
 * dataflow.
 * 
 * @author Ferd
 *
 */
public interface IContextualizable extends IKimStatement, IDataflowNode {

	public static enum Type {
		CLASSIFICATION, SERVICE, LOOKUP_TABLE, RESOURCE, MERGED_RESOURCES, EXPRESSION, CONVERSION, LITERAL,
		/* conditions are currently underspecified */CONDITION
	}

	/**
	 * The data structure describing interactive parameters. It's a javabean with
	 * only strings for values, so that it can be easily serialized for
	 * communication.
	 * 
	 * @author ferdinando.villa
	 *
	 */
	public static class InteractiveParameter {

		private String id;
		private String functionId;
		private String description;
		private String label;
		private IArtifact.Type type;
		private String initialValue;
		private Set<String> values;
		// validation
		private List<Double> range;
		private int numericPrecision;
		private String regexp;
		private String sectionTitle;
		private String sectionDescription;
		// range, regexp & numeric precision

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public IArtifact.Type getType() {
			return type;
		}

		public void setType(IArtifact.Type type) {
			this.type = type;
		}

		public String getInitialValue() {
			return initialValue;
		}

		public void setInitialValue(String initialValue) {
			this.initialValue = initialValue;
		}

		public Set<String> getValues() {
			return values;
		}

		public void setValues(Set<String> values) {
			this.values = values;
		}

		public List<Double> getRange() {
			return range;
		}

		public void setRange(List<Double> range) {
			this.range = range;
		}

		public int getNumericPrecision() {
			return numericPrecision;
		}

		public void setNumericPrecision(int numericPrecision) {
			this.numericPrecision = numericPrecision;
		}

		public String getRegexp() {
			return regexp;
		}

		public void setRegexp(String regexp) {
			this.regexp = regexp;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getFunctionId() {
			return functionId;
		}

		public void setFunctionId(String functionId) {
			this.functionId = functionId;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		@Override
		public String toString() {
			return "InteractiveParameter [id=" + id + ", functionId=" + functionId + ", description=" + description
					+ ", label=" + label + ", type=" + type + ", initialValue=" + initialValue + ", values=" + values
					+ "]";
		}

		public String getSectionTitle() {
			return sectionTitle;
		}

		public void setSectionTitle(String sectionTitle) {
			this.sectionTitle = sectionTitle;
		}

		public String getSectionDescription() {
			return sectionDescription;
		}

		public void setSectionDescription(String sectionDescription) {
			this.sectionDescription = sectionDescription;
		}

	}

	/**
	 * Contextualizables carry the trigger that they were declared with. Those that
	 * represent "default" computables for a model, such as resources, will report
	 * 
	 * 
	 * @return
	 */
	IKimAction.Trigger getTrigger();

	/**
	 * Return the type of the contained resource.
	 * 
	 * @return
	 */
	Type getType();

	/**
	 * Target ID: if null, the main observable of the model, otherwise another
	 * observable which must be defined. This is a syntactic property and can be
	 * accessed outside of a contextualization scope.
	 * 
	 * @return the target ID.
	 */
	String getTargetId();

	/**
	 * The target observable for this computation, correspondent to the target ID.
	 * Accessible only during contextualization. Null if the target is the main
	 * observable in the correspondent actuator. Otherwise the computation affects
	 * other artifacts, as in the case of internal dependencies due to indirect
	 * observables being used in subsequent computation to produce the main one.
	 * 
	 * @return the target name
	 */
	IObservable getTarget();

	/**
	 * The target artifact ID when this computation is a mediation. In this case the
	 * computation means "send this artifact through this mediator".
	 * <p>
	 * This may be merged with getTarget() at some point as the use cases for it are
	 * similar.
	 * 
	 * @return the mediation target ID.
	 */
	String getMediationTargetId();

	/**
	 * Each computation may use a different language. Null means the default
	 * supported expression language.
	 * 
	 * @return the language or null
	 */
	String getLanguage();

	/**
	 * A literal constant produced in lieu of this computation. Only one among
	 * getLiteral(), getServiceCall(), getUrn(), getClassification(),
	 * getAccordingTo(), getLookupTable() and getExpression() will return a non-null
	 * value.
	 * 
	 * @return any literal
	 */
	Object getLiteral();

	/**
	 * A literal constant produced in lieu of this computation Only one among
	 * getLiteral(), getServiceCall(), getUrn(), getClassification(),
	 * getAccordingTo(), getLookupTable() and getExpression() will return a non-null
	 * value.
	 * 
	 * @return the service call
	 */
	IServiceCall getServiceCall();

	/**
	 * A literal constant produced in lieu of this computation. Only one among
	 * getLiteral(), getServiceCall(), getUrn(), getClassification(),
	 * getAccordingTo(), getLookupTable() and getExpression() will return a non-null
	 * value.
	 * 
	 * @return the expression
	 */
	IKimExpression getExpression();

	/**
	 * A classification of the input. Only one among getLiteral(), getServiceCall(),
	 * getUrn(), getClassification(), getAccordingTo(), getLookupTable() and
	 * getExpression() will return a non-null value.
	 * 
	 * @return the classification
	 */
	IKimClassification getClassification();

	/**
	 * A lookup table translating the inputs. Only one among getLiteral(),
	 * getServiceCall(), getUrn(), getClassification(), getAccordingTo(),
	 * getLookupTable() and getExpression() will return a non-null value.
	 * 
	 * @return the lookup table
	 */
	IKimLookupTable getLookupTable();

	/**
	 * An implicit classification built by matching values of an annotation property
	 * to subclasses of the observable. Only one among getLiteral(),
	 * getServiceCall(), getUrn(), getClassification(), getAccordingTo(),
	 * getLookupTable() and getExpression() will return a non-null value.
	 * 
	 * @return the classifier property
	 */
	String getAccordingTo();

	/**
	 * A URN specifying a remote computation. Only one among getLiteral(),
	 * getServiceCall(), getUrn() and getExpression() will return a non-null value.
	 * 
	 * @return the urn
	 */
	String getUrn();

	/**
	 * Resources such as expressions or URN-specified remote computations may have
	 * requirements that must be satisfied within the model where the computation
	 * appears. These will be made available in appropriate form (scalar or not) by
	 * the runtime environment.
	 * 
	 * @return the requirements as a collection of name and type pairs.
	 */
	Collection<Pair<String, IArtifact.Type>> getInputs();

	/**
	 * Any parameters set for the computation, e.g. in the case of a function call
	 * or a URN with optional values.
	 * 
	 * @return parameter map, never null, possibly empty.
	 */
	Map<String, Object> getParameters();

	/**
	 * In interactive mode, resources may expose parameters for users to check and
	 * modify before execution. Implementation-dependent services will extract
	 * descriptors and set values.
	 * 
	 * @return the list of all parameters that may be changed by users.
	 */
	Collection<String> getInteractiveParameters();

	/**
	 * This computation may be linked to a condition, which is another computation
	 * producing a boolean. This is always empty if this resource is itself a
	 * condition.
	 * 
	 * @return the condition or an empty container.
	 */
	IContextualizable getCondition();

	/**
	 * The computation may consist in a mediation of a quantity represented by the
	 * first element in the returned tuple, which must be converted into a value
	 * represented by the second.
	 * <p>
	 * If the resource is created, the mediators must be guaranteed compatible.
	 * <p>
	 * 
	 * @return a tuple containing the original and target value semantics.
	 */
	Pair<IValueMediator, IValueMediator> getConversion();

	/**
	 * Add the resolution mode from the originating model to disambiguate resources
	 * that can be used in more than one.
	 * 
	 * @return
	 */
	public Mode getComputationMode();

	/**
	 * Only meaningful if this computable is a condition computing a (scalar or
	 * distributed) boolean, this specifies whether this condition was given with
	 * negative ('unless' instead of 'if') semantics.
	 * 
	 * @return true if negated ('unless')
	 */
	boolean isNegated();

	/**
	 * True if this computation is a mediation, expected to output a transformation
	 * of the artifact passed to it, to be used in its place to match a specific
	 * observation semantics.
	 * 
	 * @return true if mediator
	 */
	boolean isMediation();

	/**
	 * This should QUICKLY find out if a resource is available for computation.
	 * 
	 * @return
	 */
	boolean isAvailable();

	/**
	 * This will return the geometry incarnated by the computable. It should
	 * normally return a scalar geometry except for resources and services.
	 * 
	 * @return the geometry
	 */
	IGeometry getGeometry();

	/**
	 * If true, this defines an accessory variable rather than a dependency. The
	 * targetId is the name of the variable.
	 * 
	 * @return
	 */
	boolean isVariable();

//	/**
//	 * List of merged URNs that may represent resources or models. The resulting
//	 * contextualizer must arrange them by temporal context and choose the
//	 * appropriate one when data are requested.
//	 * 
//	 * @param mergedUrns
//	 */
//	List<String> getMergedUrns();

}
