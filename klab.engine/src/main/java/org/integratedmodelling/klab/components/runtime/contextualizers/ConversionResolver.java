package org.integratedmodelling.klab.components.runtime.contextualizers;

import org.integratedmodelling.kim.api.IParameters;
import org.integratedmodelling.kim.api.IServiceCall;
import org.integratedmodelling.kim.api.IValueMediator;
import org.integratedmodelling.kim.model.KimServiceCall;
import org.integratedmodelling.klab.api.data.general.IExpression;
import org.integratedmodelling.klab.api.model.contextualization.IProcessor;
import org.integratedmodelling.klab.api.model.contextualization.IResolver;
import org.integratedmodelling.klab.api.observations.IState;
import org.integratedmodelling.klab.api.provenance.IArtifact;
import org.integratedmodelling.klab.api.runtime.IContextualizationScope;
import org.integratedmodelling.klab.components.runtime.RuntimeScope;
import org.integratedmodelling.klab.data.storage.MediatingState;
import org.integratedmodelling.klab.exceptions.KlabException;
import org.integratedmodelling.klab.exceptions.KlabValidationException;
import org.integratedmodelling.klab.utils.Pair;

public class ConversionResolver implements IResolver<IState>, IProcessor, IExpression {

	static final public String FUNCTION_ID = "klab.runtime.convert";

	IValueMediator from;
	IValueMediator to;

	// don't remove - only used as expression
	public ConversionResolver() {
	}

	public ConversionResolver(IValueMediator from, IValueMediator to) {
		this.from = from;
		this.to = to;
	}

	public static IServiceCall getServiceCall(Pair<IValueMediator, IValueMediator> literal)
			throws KlabValidationException {
		return KimServiceCall.create(FUNCTION_ID, "original", literal.getFirst(), "target", literal.getSecond());
	}

	@Override
	public Object eval(IParameters<String> parameters, IContextualizationScope context) throws KlabException {
		return new ConversionResolver((IValueMediator) parameters.get("original"),
				(IValueMediator) parameters.get("target"));
	}

	@Override
	public IState resolve(IState ret, IContextualizationScope context) throws KlabException {
		return new MediatingState(ret, (RuntimeScope) context, from, to);
	}

	@Override
	public IArtifact.Type getType() {
		return IArtifact.Type.NUMBER;
	}

}
