package org.integratedmodelling.klab.components.runtime.actors;

import java.util.HashMap;
import java.util.Map;

import org.integratedmodelling.klab.components.runtime.actors.SessionActor.RegisterMessage;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.PostStop;

public class ContextActor extends AbstractBehavior<ContextActor.Command>{

	final String contextId;

	public ContextActor(ActorContext<Command> context, String contextId) {
		super(context);
		this.contextId = contextId;
		context.getLog().info("Context Actor {} started", contextId);

	}

	public static Behavior<Command> create(String contextId) {
		return Behaviors.setup(context -> new ContextActor(context, contextId));
	}



	//---------------------Messages--------------------------------------------

	public interface Command {}  
	
	private class Terminated implements Command {
		public final ActorRef<ObservationActor.Command> observAct;
		public final String contextId;
		public final String obsId;

		Terminated(ActorRef<ObservationActor.Command> observAct, String contextId, String obsId) {
			this.observAct = observAct;
			this.contextId = contextId;
			this.obsId = obsId;
		}
	}


	private final Map<String, ActorRef<ObservationActor.Command>> observationIdToActor = new HashMap<>();


	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
				.onMessage(RegisterMessage.class, this::handle)
				.onMessage(Terminated.class, this::onTerminated)
				.onSignal(PostStop.class, signal -> onPostStop())
				.build();
	}
	
	private ContextActor handle(RegisterMessage trackMsg) {
		
		ActorRef<ObservationActor.Command> ObsActor =
				getContext()
				.spawn(ObservationActor.create(contextId, trackMsg.observation.getId()), "observation-" + trackMsg.observation.getId());
		observationIdToActor.put(trackMsg.observation.getId(), ObsActor);
		return this;
		
		
	}

	
	
	/*

	private ContextActor handle(SessionActor.RegisterMessage trackMsg) {
		if (this.contextId.equals(trackMsg.contextId)) {
			ActorRef<ObservationActor.Command> ObsActor = observationIdToActor.get(trackMsg.obsId);
			if (ObsActor != null) {
				trackMsg.replyTo.tell(new SessionActor.ObsRegistered(ObsActor));
			} else {
				ObsActor =
						getContext()
						.spawn(ObservationActor.create(contextId, trackMsg.obsId), "observation-" + trackMsg.obsId);
				observationIdToActor.put(trackMsg.obsId, ObsActor);
				trackMsg.replyTo.tell(new SessionActor.ObsRegistered(ObsActor));
			}
		} else {
			getContext()
			.getLog()
			.warn(
					"Ignoring trackobservation request for {}. This actor is responsible for {}.",
					contextId,
					this.contextId);
		}
		return this;
	}
	
	*/
	
	private ContextActor onTerminated(Terminated t) {
		getContext().getLog().info("Observation actor for {} has been terminated", t.obsId);
		observationIdToActor.remove(t.obsId);
		return this;
	}
	
	private ContextActor onPostStop() {
		getContext().getLog().info("Context Actor {} stopped", contextId);
		return this;
	}

	
	

}


