package org.integratedmodelling.klab.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.integratedmodelling.klab.api.observations.IObservation;
import org.integratedmodelling.klab.api.provenance.IArtifact;

public class CollectionUtils {

	public static <T> List<T> arrayToList(T[] objects) {
		List<T> ret = new ArrayList<>();
		if (objects != null) {
			for (T obj : objects) {
				ret.add(obj);
			}
		}
		return ret;
	}

	@SafeVarargs
	public static <T> List<T> join(Collection<T>... resources) {
		List<T> ret = new ArrayList<>();
		for (Collection<T> list : resources) {
			ret.addAll(list);
		}
		return ret;
	}
	
	@SafeVarargs
	public static <T> List<T> join(Iterable<T>... resources) {
		List<T> ret = new ArrayList<>();
		for (Iterable<T> list : resources) {
			for (T o : list) {
				ret.add(o);
			}
		}
		return ret;
	}
	
	/**
	 * Return a single collection containing all the artifacts in each artifact passed.
	 * 
	 * @param artifacts
	 * @return
	 */
	public static Collection<IArtifact> joinArtifacts(Collection<IArtifact> artifacts) {
		List<IArtifact> ret = new ArrayList<>();
		for (IArtifact artifact : artifacts) {
			for (IArtifact a : artifact) {
				ret.add(a);
			}
		}
		return ret;
	}
	
	public static Collection<IObservation> joinObservations(Collection<IObservation> artifacts) {
		List<IObservation> ret = new ArrayList<>();
		for (IObservation artifact : artifacts) {
			for (IArtifact a : artifact) {
				ret.add((IObservation)a);
			}
		}
		return ret;
	}

    public static Collection<Object> flatCollection(Object... objects) {
        List<Object> ret = new ArrayList<>();
        addToCollection(ret, objects);
        return ret;
    }
    
    private static void addToCollection(List<Object> ret, Object... objects) {
        for (Object o : objects) {
            if (o instanceof Collection) {
                for (Object oo : ((Collection<?>)o)) {
                    addToCollection(ret, oo);
                }
            } else if (o != null && o.getClass().isArray()) {
                for (int i = 0; i < Array.getLength(o); i++) {
                    addToCollection(ret, Array.get(o, i));
                }
            } else {
                ret.add(o);
            }
        }
    }

}
