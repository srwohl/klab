package org.integratedmodelling.klab.engine.runtime;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.integratedmodelling.kim.api.IParameters;
import org.integratedmodelling.klab.Resources;
import org.integratedmodelling.klab.api.auth.IIdentity;
import org.integratedmodelling.klab.api.observations.ISubject;
import org.integratedmodelling.klab.api.provenance.IArtifact;
import org.integratedmodelling.klab.api.runtime.monitoring.IMonitor;
import org.integratedmodelling.klab.components.runtime.observations.Observation;
import org.integratedmodelling.klab.engine.Engine;
import org.integratedmodelling.klab.engine.runtime.api.ITaskTree;
import org.integratedmodelling.klab.utils.Pair;
import org.integratedmodelling.klab.utils.Parameters;

/**
 * A ITask that creates a root subject within a Session.
 * 
 * FIXME this should be a secondary task following a context observation; if the URN is observer
 * without a set context, the ROI becomes the scale of the resource. That's it - just copy the logics
 * in regular observations.
 * 
 * @author ferdinando.villa
 *
 */
public class UrnContextualizationTask extends AbstractTask<ISubject> {

    FutureTask<ISubject>  delegate;
    String                taskDescription = "<uninitialized URN preview task " + token + ">";
	IParameters<String> globalState = Parameters.create();

	@Override
	public IParameters<String> getState() {
		return globalState;
	}
	
    public UrnContextualizationTask(UrnContextualizationTask parent, String description) {
        super(parent);
        this.delegate = parent.delegate;
        this.taskDescription = description;
    }

    public UrnContextualizationTask(Session session, String urn) {

        Engine engine = session.getParentIdentity(Engine.class);
        try {

            this.monitor = (session.getMonitor()).get(this);
            this.session = session;
            this.taskDescription = "Previewing resource " + urn + ">";

            session.touch();

            delegate = new FutureTask<ISubject>(new MonitoredCallable<ISubject>(this) {

                @Override
                public ISubject run() throws Exception {

                    ISubject ret = null;

                    try {

                    	notifyStart();
                    	
                        Pair<IArtifact, IArtifact> data = Resources.INSTANCE
                                .resolveResourceToArtifact(urn, monitor);

                        ret = (ISubject) data.getFirst();

//                        /*
//                         * notify context
//                         */
//                        session.getMonitor()
//                                .send(Message.create(session
//                                        .getId(), IMessage.MessageClass.ObservationLifecycle, IMessage.Type.NewObservation, Observations.INSTANCE
//                                                .createArtifactDescriptor(ret, null, ITime.INITIALIZATION, -1, false, true)
//                                                .withTaskId(token)));

                        // TODO must finish this task and start another, otherwise no context gets registered.

//                        /*
//                         * notify result
//                         */
//                        IObservation notifiable = (IObservation) (data.getSecond() instanceof ObservationGroup
//                                && data.getSecond().groupSize() > 0 ? data.getSecond().iterator().next()
//                                        : data.getSecond());
//
//                        session.getMonitor().send(Message.create(session
//                                .getId(), IMessage.MessageClass.ObservationLifecycle, IMessage.Type.NewObservation, Observations.INSTANCE
//                                        .createArtifactDescriptor(notifiable, context, ITime.INITIALIZATION, -1, false, true)
//                                        .withTaskId(token)));

                        /*
                         * Register the observation context with the session. It will be disposed of
                         * and/or persisted by the session itself.
                         */
                        session.registerObservationContext(((Observation) ret).getScope());

                        notifyEnd();

                    } catch (Throwable e) {

                    	throw notifyAbort(e);

                    }
                    return ret;
                }
            });

            engine.getTaskExecutor().execute(delegate);
        } catch (

        Throwable e) {
            monitor.error("error initializing context task: " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return taskDescription;
    }

    @Override
    public String getId() {
        return token;
    }

    @Override
    public boolean is(Type type) {
        return type == Type.TASK;
    }

    @Override
    public <T extends IIdentity> T getParentIdentity(Class<T> type) {
        return IIdentity.findParent(this, type);
    }

    @Override
    public IIdentity getParentIdentity() {
        return parentTask == null ? session : parentTask;
    }

    @Override
    public IMonitor getMonitor() {
        return monitor;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        monitor.interrupt();
        return delegate.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return delegate.isCancelled();
    }

    @Override
    public boolean isDone() {
        return delegate.isDone();
    }

    @Override
    public ISubject get() throws InterruptedException, ExecutionException {
        return delegate.get();
    }

    @Override
    public ISubject get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return delegate.get(timeout, unit);
    }

    @Override
    public ITaskTree<ISubject> createChild(String description) {
        return new UrnContextualizationTask(this, description);
    }

	@Override
	protected String getTaskDescription() {
		return taskDescription;
	}

}
