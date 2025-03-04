package org.integratedmodelling.klab.components.runtime.contextualizers.dereifiers;

import org.integratedmodelling.kim.api.IParameters;
import org.integratedmodelling.kim.api.IServiceCall;
import org.integratedmodelling.kim.model.KimServiceCall;
import org.integratedmodelling.klab.api.data.ILocator;
import org.integratedmodelling.klab.api.data.artifacts.IDataArtifact;
import org.integratedmodelling.klab.api.data.general.IExpression;
import org.integratedmodelling.klab.api.knowledge.IObservable;
import org.integratedmodelling.klab.api.model.contextualization.IResolver;
import org.integratedmodelling.klab.api.observations.IDirectObservation;
import org.integratedmodelling.klab.api.observations.scale.space.ISpace;
import org.integratedmodelling.klab.api.provenance.IArtifact;
import org.integratedmodelling.klab.api.provenance.IArtifact.Type;
import org.integratedmodelling.klab.api.runtime.IContextualizationScope;
import org.integratedmodelling.klab.common.Geometry;
import org.integratedmodelling.klab.components.geospace.extents.Space;
import org.integratedmodelling.klab.components.geospace.processing.Rasterizer;
import org.integratedmodelling.klab.exceptions.KlabException;
import org.integratedmodelling.klab.exceptions.KlabUnsupportedFeatureException;
import org.integratedmodelling.klab.scale.Scale;

public class PresenceResolver implements IResolver<IDataArtifact>, IExpression {

	static final public String FUNCTION_ID = "klab.runtime.dereifiers.presence";

	private String artifactId = null;

	// don't remove - only used as expression
	public PresenceResolver() {
	}

	public PresenceResolver(IParameters<String> parameters, IContextualizationScope context) {
		this.artifactId = parameters.get("artifact", String.class);
	}

	public static IServiceCall getServiceCall(IObservable availableType, IObservable desiredObservation) {
		IServiceCall ret = KimServiceCall.create(FUNCTION_ID);
		ret.getParameters().put("artifact", availableType.getName());
		return ret;
	}

	@Override
	public Object eval(IParameters<String> parameters, IContextualizationScope context) throws KlabException {
		return new PresenceResolver(parameters, context);
	}

	@Override
	public IDataArtifact resolve(IDataArtifact ret, IContextualizationScope context) throws KlabException {

		ISpace space = ((Scale) context.getScale()).getSpace();
		Geometry geometry =  ((Scale) context.getScale()).asGeometry();

		if (!(space instanceof Space) || ((Space) space).getGrid() == null) {
			// TODO only return an appropriate state for existence of artifact in context if
			// artifact is non-empty and either space is there and intersecting, or space
			// isn't there at all.
			throw new KlabUnsupportedFeatureException("cannot yet compute indirect presence over a non-grid extent");
		}
		Rasterizer<Boolean> rasterizer = new Rasterizer<>(((Space) space).getGrid());
		for (IArtifact a : context.getArtifact(this.artifactId)) {
			if (a instanceof IDirectObservation && ((IDirectObservation) a).getSpace() != null) {
				if (context.getMonitor().isInterrupted()) {
					break;
				}
				rasterizer.add(((IDirectObservation) a).getSpace().getShape(), (shape) -> true);
			}
		}
		
		if (context.getMonitor().isInterrupted()) {
			return ret;
		}
		rasterizer.finish((present, xy) -> {
			for (ILocator spl : geometry.at(space, xy[0], xy[1])) {
				ret.set(spl, present == null ? false : present);
			}
		});

		return ret;
	}

	@Override
	public Type getType() {
		return Type.BOOLEAN;
	}

}
