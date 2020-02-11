package org.integratedmodelling.klab.provenance;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.integratedmodelling.klab.api.observations.scale.time.ITime;
import org.integratedmodelling.klab.api.provenance.IActivity;
import org.integratedmodelling.klab.api.provenance.IAgent;
import org.integratedmodelling.klab.api.provenance.IProvenance;

/**
 * One of the concrete activities (Description) in the ODO observation ontology:
 * Computation, Detection or Acknowledgement. We only represent the first two,
 * as artifacts that have no activity generating them are by default
 * acknowledged by the identity that owns the graph.
 * <p>
 * The agent for a computation is a dataflow, which follows the Plan specified
 * by a Model. All these are reported in their serialized form.
 * <p>
 * 
 * @author ferdinando.villa
 *
 */
public class Activity implements IActivity {

	private String id;
	private long start = System.currentTimeMillis();
	private long end;
	private Type type = Type.Creation;
	private ITime schedulerTime;

//	public Activity() {
//		// TODO Auto-generated constructor stub
//	}

	// TODO add provenance
	public Activity(String token) {
		this.id = token;
	}

	@Override
	public long getStart() {
		return start;
	}

	@Override
	public long getEnd() {
		return this.end;
	}

	@Override
	public Optional<IActivity> getCause() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IAgent getAgent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getTimestamp() {
		return this.start;
	}

	@Override
	public IProvenance getProvenance() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getId() {
		return id;
	}

	public void finished() {
		this.end = System.currentTimeMillis();
	}

	@Override
	public List<IActivity> getActions() {
		List<IActivity> ret = new ArrayList<>();
		return ret;
	}

	@Override
	public Type getType() {
		return this.type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	@Override
	public ITime getSchedulerTime() {
		return this.schedulerTime;
	}

	public void setSchedulerTime(ITime schedulerTime) {
		this.schedulerTime = schedulerTime;
	}

}
