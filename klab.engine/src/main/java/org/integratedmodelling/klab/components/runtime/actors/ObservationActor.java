package org.integratedmodelling.klab.components.runtime.actors;

import org.integratedmodelling.klab.api.observations.IObservation;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.PostStop;

/**
 * Create any observation actor as
 * 
 * @author ferdinando.villa
 *
 */
public class ObservationActor extends AbstractBehavior<ObservationActor.Command> {
	
	private final String contextId;
	private final String obsId;
	
	
	public static Behavior<Command> create(String contextId, String obsId) {
		return Behaviors.setup(context -> new ObservationActor(context, contextId, obsId));
	}


	public ObservationActor(ActorContext<Command> context, String contextId, String obsId) {
		super(context);
		this.contextId = contextId;
		this.obsId = obsId;

		context.getLog().info("observation actor {}-{} started", contextId, obsId);

	}
	
    //------------Messages------------------------------------------------
	
	public interface Command { };

	//----------------------------Actions to take-------------------------------------------

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()

				.onSignal(PostStop.class, signal -> onPostStop())
				.build();
	}



	private Behavior<Command> onPostStop() {
		getContext().getLog().info("Observation actor {}-{} stopped", contextId, obsId);
		return Behaviors.stopped();
	}
	

	

}


