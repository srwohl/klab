package org.integratedmodelling.klab.clitool.console.commands.context;

import java.util.Map;

import org.integratedmodelling.kim.api.IServiceCall;
import org.integratedmodelling.klab.api.cli.ICommand;
import org.integratedmodelling.klab.api.observations.IObservation;
import org.integratedmodelling.klab.api.observations.ISubject;
import org.integratedmodelling.klab.api.runtime.ISession;
import org.integratedmodelling.klab.components.localstorage.impl.AbstractAdaptiveStorage;
import org.integratedmodelling.klab.components.runtime.observations.Observation;
import org.integratedmodelling.klab.components.runtime.observations.State;
import org.integratedmodelling.klab.dataflow.ObservedConcept;
import org.integratedmodelling.klab.engine.runtime.api.IRuntimeScope;

public class Info implements ICommand {

	@Override
	public Object execute(IServiceCall call, ISession session) {

		String ret = "";
		ISubject ctx = session.getState().getCurrentContext();
		if (ctx != null) {
			Map<ObservedConcept, IObservation> catalog = ((IRuntimeScope)((Observation)ctx).getScope()).getCatalog();
			for (ObservedConcept c : catalog.keySet()) {
				IObservation obs = catalog.get(c);
				ret += c + ":\n";
				ret += dump(obs, 0);
			}
		} else {
			ret += "No current context";
		}
		return ret;
	}

	private String dump(IObservation obs, int indent) {
		String ret = "";
		// TODO remaining cases
		if (obs instanceof State) {
			State state = (State)obs;
			if (state.getStorage() instanceof AbstractAdaptiveStorage) {
				AbstractAdaptiveStorage<?> storage = (AbstractAdaptiveStorage<?>) state.getStorage();
				ret += storage.getInfo(indent + 3);
			}
		}
		return ret;
	}

}
