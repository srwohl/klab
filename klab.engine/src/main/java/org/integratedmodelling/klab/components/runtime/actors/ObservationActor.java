package org.integratedmodelling.klab.components.runtime.actors;

import org.integratedmodelling.klab.api.observations.IObservation;
import org.integratedmodelling.klab.components.runtime.actors.SessionActor.RegisterObsMessage;

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
	
    IObservation observation;
	
	
	public static Behavior<Command> create(String obsId) {
		return Behaviors.setup(context -> new ObservationActor(context));
	}


	public ObservationActor(ActorContext<Command> context) {
		super(context);

		context.getLog().info("observation actor {}-{} started");

	}
	
    //------------Messages------------------------------------------------
	
	public interface Command { };
	
    public static class EventMsg implements Command{
        public final String name;

        public EventMsg(String name) {
            this.name = name;
        }
    }

	//----------------------------Actions to take-------------------------------------------

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
                .onMessage(EventMsg.class, this::onEventmsg)
				.onSignal(PostStop.class, signal -> onPostStop())
				.build();
	}

	private ObservationActor onEventmsg(EventMsg Msg) {
		getContext().getLog().info("Observation actor", Msg.name);
		return this;
	
	}

	private Behavior<Command> onPostStop() {
		getContext().getLog().info("Observation actor {}-{} stopped");
		return Behaviors.stopped();
	}
	

	

}


