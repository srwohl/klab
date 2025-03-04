package org.integratedmodelling.klab.components.runtime.contextualizers;

import org.integratedmodelling.kim.api.IContextualizable;
import org.integratedmodelling.kim.api.IParameters;
import org.integratedmodelling.kim.api.IServiceCall;
import org.integratedmodelling.kim.model.KimServiceCall;
import org.integratedmodelling.klab.api.data.classification.IClassification;
import org.integratedmodelling.klab.api.data.general.IExpression;
import org.integratedmodelling.klab.api.knowledge.IObservable;
import org.integratedmodelling.klab.api.model.contextualization.IProcessor;
import org.integratedmodelling.klab.api.model.contextualization.IStateResolver;
import org.integratedmodelling.klab.api.provenance.IArtifact;
import org.integratedmodelling.klab.api.provenance.IArtifact.Type;
import org.integratedmodelling.klab.api.runtime.IContextualizationScope;
import org.integratedmodelling.klab.exceptions.KlabException;
import org.integratedmodelling.klab.exceptions.KlabValidationException;

public class ClassifyingStateResolver implements IStateResolver, IProcessor, IExpression {

	static final public String FUNCTION_ID = "klab.runtime.classify";

	private IClassification classification;

	// don't remove - only used as expression
	public ClassifyingStateResolver() {
	}

	public ClassifyingStateResolver(IClassification classification) {
		this.classification = classification;
	}

	public static IServiceCall getServiceCall(IClassification classification, IContextualizable condition, boolean conditionNegated)
			throws KlabValidationException {
		// TODO handle condition
		return KimServiceCall.create(FUNCTION_ID, "classification", classification);
	}

	@Override
	public Object eval(IParameters<String> parameters, IContextualizationScope context) throws KlabException {
		return new ClassifyingStateResolver(parameters.get("classification", IClassification.class));
	}

	@Override
	public Object resolve(IObservable observable, IContextualizationScope context) throws KlabException {
		return classification.classify(context.get("self"), context);
	}

	@Override
	public IArtifact.Type getType() {
		return Type.CONCEPT;
	}
}
