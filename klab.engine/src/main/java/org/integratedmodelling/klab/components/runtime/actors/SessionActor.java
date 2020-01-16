package org.integratedmodelling.klab.components.runtime.actors;

import java.util.HashMap;
import java.util.Map;

import org.integratedmodelling.klab.api.observations.IObservation;
import org.integratedmodelling.klab.api.observations.ISubject;
import org.integratedmodelling.klab.components.runtime.actors.EngineActor.Start;
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


	public SessionActor(ActorContext<Command> context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public static Behavior<Command> create() {
		return Behaviors.setup(SessionActor::new);
	}
	
	ActorRef<ContextActor.Command> contextActor;

	private final Map<String, ActorRef<ContextActor.Command>> contextIdToActor = new HashMap<>();

	//---------------------------------- Messages------------------------------------------------

	public interface Command {}  

	public static final class RegisterMessage implements SessionActor.Command, ContextActor.Command {
		public final IObservation observation;


		public RegisterMessage(IObservation observation) {
			this.observation = observation;

		}
	}
	
	public static final class ObsRegistered {
		public final ActorRef<ObservationActor.Command> observ;

		public ObsRegistered(ActorRef<ObservationActor.Command> observ) {
			this.observ = observ;
		}
	}



	//------------------Actions to take-------------------------------------------------------------------

	public Receive<Command> createReceive() {
		return newReceiveBuilder()
				.onMessage(RegisterMessage.class, this::onRegister)
				.onMessage(EngineActor.Start.class, this::onStart)
				.onSignal(PostStop.class, signal -> onPostStop())
				.build();
	}
	
	
	private SessionActor onRegister(RegisterMessage Msg) {
		String obsId = Msg.observation.getId();
		
		if (Msg.observation instanceof ISubject) {
			contextActor = getContext().spawn(ContextActor.create(obsId), "context-" + obsId);
			contextIdToActor.put(obsId, contextActor); 
		}
		
		contextActor.tell(new SessionActor.RegisterMessage(Msg.observation));
		
		return this;

	}


/*
	private SessionActor onRegister(RegisterMessage trackMsg) { 
		String contextId = trackMsg.contextId; 
		ActorRef<ContextActor.Command> ref = contextIdToActor.get(contextId);
		if (ref != null) { ref.tell(trackMsg); }
		else { 
		getContext().getLog().info("Creating context actor for {}",contextId); 
				ActorRef<ContextActor.Command> contextActor = getContext().spawn(ContextActor.create(contextId), "context-" + contextId);
				contextActor.tell(trackMsg); 
				contextIdToActor.put(contextId, contextActor); 
			 }
		return this; 
	}
	
*/

	
	  private SessionActor onStart(Start str) {
		    ActorRef<SessionActor.Command> SessAct = getContext().spawn(SessionActor.create(), "session-actor");

		    System.out.println("session Actor: " + SessAct);
		    SessAct.tell(str);
		    return this;
		  }


	private SessionActor onPostStop() {
		getContext().getLog().info("SessionActor stopped");
		return this;
	}




}


