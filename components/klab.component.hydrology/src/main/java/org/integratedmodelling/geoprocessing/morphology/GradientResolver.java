package org.integratedmodelling.geoprocessing.morphology;

import static org.hortonmachine.gears.libs.modules.HMConstants.floatNovalue;

import java.awt.image.DataBuffer;

import org.hortonmachine.gears.libs.modules.Variables;
import org.hortonmachine.hmachine.modules.geomorphology.gradient.OmsGradient;
import org.integratedmodelling.geoprocessing.TaskMonitor;
import org.integratedmodelling.kim.api.IParameters;
import org.integratedmodelling.klab.api.data.general.IExpression;
import org.integratedmodelling.klab.api.model.contextualization.IResolver;
import org.integratedmodelling.klab.api.observations.IState;
import org.integratedmodelling.klab.api.provenance.IArtifact.Type;
import org.integratedmodelling.klab.api.runtime.IContextualizationScope;
import org.integratedmodelling.klab.components.geospace.Geospace;
import org.integratedmodelling.klab.components.geospace.utils.GeotoolsUtils;
import org.integratedmodelling.klab.exceptions.KlabException;

public class GradientResolver implements IResolver<IState>, IExpression {

	boolean computeAngles = false;
	
	@Override
	public Type getType() {
		return Type.NUMBER;
	}

	@Override
	public IState resolve(IState target, IContextualizationScope context) throws KlabException {

		IState dem = context.getArtifact("elevation", IState.class);

		OmsGradient algorithm = new OmsGradient();
		algorithm.inElev = GeotoolsUtils.INSTANCE.stateToCoverage(dem, context.getScale(), DataBuffer.TYPE_DOUBLE, floatNovalue, false);
		algorithm.pm = new TaskMonitor(context.getMonitor());
		algorithm.doDegrees = computeAngles;
		algorithm.doProcess = true;
		algorithm.pMode = Variables.FINITE_DIFFERENCES;
		algorithm.doReset = false;
		context.getMonitor().info("computing finite differences gradient...");
		try {
			algorithm.process();
		} catch (Exception e) {
			throw new KlabException(e);
		}
		if (!context.getMonitor().isInterrupted()) {
			GeotoolsUtils.INSTANCE.coverageToState(algorithm.outSlope, target, context.getScale(), (a) -> {
				if (a == (double) floatNovalue) {
					return Double.NaN;
				}
				return a;
			});
		}
		return target;
	}

	public double toAngle(double code) {
		if (Double.isNaN(code)) {
			return code;
		}
		return Geospace.getHeading((int)code);
	}
	
	@Override
	public Object eval(IParameters<String> parameters, IContextualizationScope context) throws KlabException {
		GradientResolver ret = new GradientResolver();
		ret.computeAngles = parameters.get("angles", Boolean.FALSE);
		return ret;
	}
}
