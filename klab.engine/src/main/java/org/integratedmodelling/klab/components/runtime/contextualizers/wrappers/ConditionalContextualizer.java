package org.integratedmodelling.klab.components.runtime.contextualizers.wrappers;

import org.integratedmodelling.kim.api.IContextualizable;
import org.integratedmodelling.kim.api.IParameters;
import org.integratedmodelling.kim.api.IServiceCall;
import org.integratedmodelling.kim.model.KimServiceCall;
import org.integratedmodelling.klab.Extensions;
import org.integratedmodelling.klab.api.data.general.IExpression;
import org.integratedmodelling.klab.api.runtime.IContextualizationScope;
import org.integratedmodelling.klab.exceptions.KlabException;

public class ConditionalContextualizer implements IExpression {
	
	static final public String FUNCTION_ID = "klab.runtime.conditional";
	
	public ConditionalContextualizer() {
	}

	@Override
	public Object eval(IParameters<String> parameters, IContextualizationScope context) throws KlabException {

		IServiceCall call = parameters.get("call", IServiceCall.class);
		IContextualizable condition = parameters.get("condition", IContextualizable.class);
		boolean isConditionNegated = parameters.get("negated", Boolean.class);
		
		boolean exec = Extensions.INSTANCE.callAsCondition(condition, context);
		if (isConditionNegated) {
			exec = !exec;
		}
		return exec ? Extensions.INSTANCE.callFunction(call, context) : null;
	}

	public static IServiceCall getServiceCall(IContextualizable resource) {
		IServiceCall ret = KimServiceCall.create(FUNCTION_ID);
		ret.getParameters().put("call", resource.getServiceCall());
		ret.getParameters().put("condition", resource.getCondition());
		ret.getParameters().put("negated", resource.isNegated());
		return ret;
	}

}
