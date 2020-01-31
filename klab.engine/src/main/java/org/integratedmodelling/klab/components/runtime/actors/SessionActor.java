package org.integratedmodelling.klab.components.runtime.actors;

import java.util.HashMap;
import java.util.Map;

import org.integratedmodelling.klab.api.observations.IObservation;
import org.integratedmodelling.klab.api.observations.ISubject;
import org.integratedmodelling.klab.components.runtime.observations.Observation;
import org.integratedmodelling.klab.components.runtime.observations.Subject;
import org.integratedmodelling.klab.engine.runtime.EventBus;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.PostStop;

public class SessionActor extends AbstractBehavior<SessionActor.Command> {

    // Constuctor
	public SessionActor(ActorContext<Command> context) {
		super(context);
		context.getLog().info("Session Actor started");
		// TODO Auto-generated constructor stub
	}
	
	// Factory
	
	public static Behavior<Command> create() {
		return Behaviors.setup(SessionActor::new);
	}
	
	ActorRef<ContextActor.Command> contextActor;

	private final Map<String, ActorRef<ContextActor.Command>> contextIdToActor = new HashMap<>();
	private final Map<String, ActorRef<ObservationActor.Command>> observationIdToActor = new HashMap<>();

	//---------------------------------- Messages------------------------------------------------

	public interface Command {}  

	public static final class RegisterObsMessage implements SessionActor.Command, ContextActor.Command {
		public final Observation observation;


		public RegisterObsMessage(Observation observation) {
			this.observation = observation;

		}
	}
	
	//------------------Actions to take-------------------------------------------------------------------

	public Receive<Command> createReceive() {
		return newReceiveBuilder()
				.onMessage(RegisterObsMessage.class, this::onRegister)
				.onSignal(PostStop.class, signal -> onPostStop())
				.build();
	}
	
	
	private SessionActor onRegister(RegisterObsMessage Msg) {
		String obsId = Msg.observation.getId();
		
		if (Msg.observation.getContext()==null) {
			getContext().getLog().info("Creating context actor for {}",obsId); 
			ActorRef<ContextActor.Command> contextActor= getContext().spawn(ContextActor.create(obsId), "context-" + obsId);
			contextIdToActor.put(obsId, contextActor); 
		}
		else {
			getContext().getLog().info("Creating observation actor for {}",obsId); 
		ActorRef<ObservationActor.Command> ObsActor =
				getContext()
				.spawn(ObservationActor.create(obsId), "observation-" + obsId);
		observationIdToActor.put(obsId, ObsActor);
		
		
		}
		return this;
	}



	private SessionActor onPostStop() {
		getContext().getLog().info("SessionActor stopped");
		return this;
	}




}


