package org.integratedmodelling.klab.owl;

import java.util.HashMap;
import java.util.Map;

import org.integratedmodelling.klab.api.knowledge.IConcept;

/**
 * Simple cache for is() operations to minimize the use of the reasoner.
 * 
 * @author Ferd
 *
 */
public class ReasonerCache {

	ThreadLocal<Map<String, Boolean>> cache = new ThreadLocal<>();
	
	public ReasonerCache() {
		cache.set(new HashMap<>());
	}
	
	public boolean is(IConcept a, IConcept b) {
		String desc = a.getDefinition() + "|" + b.getDefinition();
		Boolean ret = cache.get().get(desc);
		if (ret == null) {
			ret = a.is(b);
			cache.get().put(desc, ret);
		}
		return ret;
	}
	
}
