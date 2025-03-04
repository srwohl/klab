package org.integratedmodelling.klab.clitool.console.commands.reason;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.integratedmodelling.kim.api.IServiceCall;
import org.integratedmodelling.klab.Observables;
import org.integratedmodelling.klab.Reasoner;
import org.integratedmodelling.klab.api.cli.ICommand;
import org.integratedmodelling.klab.api.knowledge.IConcept;
import org.integratedmodelling.klab.api.knowledge.IObservable;
import org.integratedmodelling.klab.api.runtime.ISession;
import org.integratedmodelling.klab.exceptions.KlabValidationException;
import org.integratedmodelling.klab.owl.Concept;

public class ShowType implements ICommand {

    @Override
    public Object execute(IServiceCall call, ISession session) throws KlabValidationException {
        String declaration = StringUtils.join((List<?>) call.getParameters().get("arguments"), ' ').trim();
        IConcept concept = null;
        if (declaration.startsWith("k:")) {
            concept = Reasoner.INSTANCE.getOntology().getConcept(declaration.substring(2));
        }
        if (concept == null) {
            IObservable observable = Observables.INSTANCE.declare(declaration);
            concept = observable.getType();
        }
        if (concept == null) {
            throw new KlabValidationException("expression '" + declaration + "' does not specify a concept");
        }
        return Arrays.toString(((Concept) concept.getType()).getTypeSet().toArray());
    }

}
