package org.integratedmodelling.klab.components.runtime.actors;

import java.util.HashMap;
import java.util.Map;

import org.integratedmodelling.klab.components.runtime.actors.SessionActor.RegisterObsMessage;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.PostStop;

public class ContextActor extends AbstractBehavior<ContextActor.Command>{


	public ContextActor(ActorContext<Command> context) {
		super(context);
		context.getLog().info("Context Actor {} started");

	}

	public static Behavior<Command> create(String contextId) {
		return Behaviors.setup(context -> new ContextActor(context));
	}



	//---------------------Messages--------------------------------------------

	public interface Command {}  
	


	private final Map<String, ActorRef<ObservationActor.Command>> observationIdToActor = new HashMap<>();


	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
				.onMessage(RegisterObsMessage.class, this::handle)
				.onSignal(PostStop.class, signal -> onPostStop())
				.build();
	}
	
	private ContextActor handle(RegisterObsMessage trackMsg) {
		String obsId= trackMsg.observation.getId();
		ActorRef<ObservationActor.Command> ObsActor =
				getContext()
				.spawn(ObservationActor.create(obsId), "observation-" + trackMsg.observation.getId());
		observationIdToActor.put(trackMsg.observation.getId(), ObsActor);
		ObsActor.tell(new ObservationActor.EventMsg("obsId"));
		return this;
	
	}

	
	private ContextActor onPostStop() {
		getContext().getLog().info("Context Actor {} stopped");
		return this;
	}

	
	

}


