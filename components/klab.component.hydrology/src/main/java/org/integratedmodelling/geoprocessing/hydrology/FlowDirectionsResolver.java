package org.integratedmodelling.geoprocessing.hydrology;

import static org.hortonmachine.gears.libs.modules.HMConstants.floatNovalue;

import java.awt.image.DataBuffer;

import org.hortonmachine.hmachine.modules.geomorphology.flow.OmsFlowDirections;
import org.integratedmodelling.geoprocessing.TaskMonitor;
import org.integratedmodelling.kim.api.IParameters;
import org.integratedmodelling.klab.api.data.ILocator;
import org.integratedmodelling.klab.api.data.general.IExpression;
import org.integratedmodelling.klab.api.model.contextualization.IResolver;
import org.integratedmodelling.klab.api.observations.IState;
import org.integratedmodelling.klab.api.provenance.IArtifact.Type;
import org.integratedmodelling.klab.api.runtime.IContextualizationScope;
import org.integratedmodelling.klab.components.geospace.Geospace;
import org.integratedmodelling.klab.components.geospace.utils.GeotoolsUtils;
import org.integratedmodelling.klab.exceptions.KlabException;
import org.integratedmodelling.klab.utils.NumberUtils;

public class FlowDirectionsResolver implements IResolver<IState>, IExpression {

    boolean computeAngles = false;

    @Override
    public Type getType() {
        return Type.NUMBER;
    }

    @Override
    public IState resolve(IState target, IContextualizationScope context) throws KlabException {

        IState dem = context.getArtifact("hydrologically_corrected_elevation", IState.class);

        OmsFlowDirections algorithm = new OmsFlowDirections();
        algorithm.inPit = GeotoolsUtils.INSTANCE.stateToCoverage(dem, context.getScale(), DataBuffer.TYPE_FLOAT, floatNovalue,
                false);
        algorithm.pm = new TaskMonitor(context.getMonitor());
        algorithm.doProcess = true;
        algorithm.doReset = false;
        context.getMonitor().info("computing flow directions...");
        try {
            algorithm.process();
        } catch (Exception e) {
            throw new KlabException(e);
        }
        if (!context.getMonitor().isInterrupted()) {
            
            for (ILocator locator : context.getScale()) {
                Object val = target.get(locator);
                if (val instanceof Number && !Double.isNaN(((Number)val).doubleValue())) {
                    if (NumberUtils.equal(((Number)val).doubleValue(), 0)) {
                        System.out.println("ZOPPODIO");
                    }
                }
            }
            
            GeotoolsUtils.INSTANCE.coverageToState(algorithm.outFlow, target, context.getScale(), (a) -> {
                if (a == (double) floatNovalue) {
                    return Double.NaN;
                }
                if (computeAngles) {
                    return toAngle(a);
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
        return Geospace.getHeading((int) code);
    }

    @Override
    public Object eval(IParameters<String> parameters, IContextualizationScope context) throws KlabException {
        FlowDirectionsResolver ret = new FlowDirectionsResolver();
        ret.computeAngles = parameters.get("angles", Boolean.FALSE);
        return ret;
    }
}
