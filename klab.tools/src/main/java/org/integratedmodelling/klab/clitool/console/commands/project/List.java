package org.integratedmodelling.klab.clitool.console.commands.project;

import org.integratedmodelling.kim.api.IServiceCall;
import org.integratedmodelling.klab.Resources;
import org.integratedmodelling.klab.api.cli.ICommand;
import org.integratedmodelling.klab.api.knowledge.IProject;
import org.integratedmodelling.klab.api.runtime.ISession;

public class List implements ICommand {

	@Override
	public Object execute(IServiceCall call, ISession session) {

		String ret = "";
		for (IProject project : Resources.INSTANCE.getProjects()) {
			ret +=(ret.isEmpty() ? "   " : "\n   ") + project.getName();
			// TODO add info about namespaces, resources etc (linked to -a option)
		}
		return "Local workspace (" + Resources.INSTANCE.getLocalWorkspace().getRoot() + "):\n" + ret;
	}

}
