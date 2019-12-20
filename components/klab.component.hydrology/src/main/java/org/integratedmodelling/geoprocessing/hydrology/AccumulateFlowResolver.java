package org.integratedmodelling.geoprocessing.hydrology;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.integratedmodelling.kim.api.IKimExpression;
import org.integratedmodelling.kim.api.IParameters;
import org.integratedmodelling.klab.Extensions;
import org.integratedmodelling.klab.Units;
import org.integratedmodelling.klab.api.data.general.IExpression;
import org.integratedmodelling.klab.api.data.mediation.IUnit;
import org.integratedmodelling.klab.api.extensions.ILanguageProcessor.Descriptor;
import org.integratedmodelling.klab.api.model.contextualization.IResolver;
import org.integratedmodelling.klab.api.observations.IObservation;
import org.integratedmodelling.klab.api.observations.IState;
import org.integratedmodelling.klab.api.observations.scale.space.IGrid.Cell;
import org.integratedmodelling.klab.api.observations.scale.space.ISpace;
import org.integratedmodelling.klab.api.provenance.IArtifact;
import org.integratedmodelling.klab.api.provenance.IArtifact.Type;
import org.integratedmodelling.klab.api.runtime.IContextualizationScope;
import org.integratedmodelling.klab.components.geospace.Geospace;
import org.integratedmodelling.klab.components.geospace.extents.Grid;
import org.integratedmodelling.klab.components.geospace.extents.Shape;
import org.integratedmodelling.klab.components.geospace.extents.Space;
import org.integratedmodelling.klab.components.geospace.processing.ContributingCell;
import org.integratedmodelling.klab.exceptions.KlabException;
import org.integratedmodelling.klab.exceptions.KlabValidationException;
import org.integratedmodelling.klab.utils.Parameters;

import com.vividsolutions.jts.geom.Point;

/**
 * Generic flow accumulator resolver that will compute an arbitrary quality by
 * following flow directions from the outlets and executing arbitrary
 * expressions.
 * 
 * @author ferdinando.villa
 *
 */
public class AccumulateFlowResolver implements IResolver<IState>, IExpression {

	private Descriptor accumulateDescriptor;
	private Descriptor distributeDescriptor;
	private IContextualizationScope context;

	public AccumulateFlowResolver() {
	}

	public AccumulateFlowResolver(IParameters<String> parameters, IContextualizationScope context) {

		this.context = context;
		if (parameters.containsKey("evaluate")) {
			Object expression = parameters.get("evaluate");
			if (expression instanceof IKimExpression) {
				expression = ((IKimExpression) expression).getCode();
			}
			this.accumulateDescriptor = Extensions.INSTANCE.getLanguageProcessor(Extensions.DEFAULT_EXPRESSION_LANGUAGE)
					.describe(expression.toString(), context.getExpressionContext(), false);
		}
		if (parameters.containsKey("distribute")) {
			Object expression = parameters.get("distribute");
			if (expression instanceof IKimExpression) {
				expression = ((IKimExpression) expression).getCode();
			}
			this.distributeDescriptor = Extensions.INSTANCE.getLanguageProcessor(Extensions.DEFAULT_EXPRESSION_LANGUAGE)
					.describe(expression.toString(), context.getExpressionContext(), false);
		}
		if (this.accumulateDescriptor == null && this.distributeDescriptor == null) {
			throw new IllegalArgumentException("flow accumulation resolver: no expression to evaluate");
		}
	}

	@Override
	public Type getType() {
		return Type.NUMBER;
	}

	@Override
	public IState resolve(IState target, IContextualizationScope context) throws KlabException {

		IState flowdirection = context.getArtifact("flow_directions_d8", IState.class);

		IUnit tUnit = target.getObservable().getUnit();
		Grid grid = Space.extractGrid(target);

		if (grid == null) {
			throw new KlabValidationException("Runoff must be computed on a grid extent");
		}

		if (tUnit != null && tUnit.equals(Units.INSTANCE.SQUARE_METERS)) {
			tUnit = null;
		}

		/*
		 * synchronized in case we parallelize the walker, which we don't do at the
		 * moment (not sure what happens with synchronous writes).
		 */
		Map<String, IState> states = Collections.synchronizedMap(new HashMap<>());
		states.put("self", target);
		IExpression downstreamExpression = null;
		IExpression upstreamExpression = null;

		if (accumulateDescriptor != null) {
			// check inputs and see if the expr is worth anything in this context
			for (String input : accumulateDescriptor.getIdentifiers()) {
				if (accumulateDescriptor.isScalar(input) && context.getArtifact(input, IState.class) != null) {
					IState state = context.getArtifact(input, IState.class);
					states.put(input, state);
				}
			}
			downstreamExpression = accumulateDescriptor.compile();
		}

		if (distributeDescriptor != null) {
			// check inputs and see if the expr is worth anything in this context
			for (String input : distributeDescriptor.getIdentifiers()) {
				if (distributeDescriptor.isScalar(input) && context.getArtifact(input, IState.class) != null) {
					IState state = context.getArtifact(input, IState.class);
					states.put(input, state);
				}
			}
			upstreamExpression = distributeDescriptor.compile();
		}

		for (IArtifact artifact : context.getArtifact("stream_outlet")) {

			ISpace space = ((IObservation) artifact).getSpace();

			if (space == null) {
				continue;
			}

			Point point = ((Shape) space.getShape()).getJTSGeometry().getCentroid();
			long xy = grid.getOffsetFromWorldCoordinates(point.getX(), point.getY());
			Cell start = grid.getCell(xy);
			compute(start, flowdirection, target, states, downstreamExpression, upstreamExpression, true, null);
		}

		return target;
	}

	/*
	 * Computation of runoff accumulates the runoff from upstream cells, ending at
	 * the outlet This function is called with the outlet cell as parameter.
	 */
	private Object compute(Cell cell, IState flowdirection, IState result, Map<String, IState> states,
			IExpression downstreamExpression, IExpression upstreamExpression, boolean isOutlet, Object previousValue) {

		Object ret = previousValue;
		Parameters<String> parameters = Parameters.create();
		parameters.put("current", previousValue);

		/*
		 * collect values of all states @ self
		 */
		for (String state : states.keySet()) {
			parameters.put(state, states.get(state).get(cell));
		}

		/*
		 * build the cell descriptor to give us access to the neighborhood
		 */
		parameters.put("cell",
				new ContributingCell(cell, ((Number) flowdirection.get(cell)).intValue(), flowdirection, states, isOutlet));

		// call upstream before recursion, in upstream order
		if (upstreamExpression != null) {
			result.set(cell, (ret = upstreamExpression.eval(parameters, context)));
		}
		
		List<Cell> upstreamCells = Geospace.getUpstreamCells(cell, flowdirection, null);
		for (Cell upstream : upstreamCells) {
			compute(upstream, flowdirection, result, states, downstreamExpression, upstreamExpression, false, ret);
		}

		/*
		 * call downstream after recursion, i.e. in downstream order
		 */
		if (downstreamExpression != null) {
			result.set(cell, (ret = downstreamExpression.eval(parameters, context)));
		}
		
		return ret;
	}

	@Override
	public Object eval(IParameters<String> parameters, IContextualizationScope context) throws KlabException {
		return new AccumulateFlowResolver(parameters, context);
	}
}
