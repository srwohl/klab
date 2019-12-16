package org.integratedmodelling.klab.components.runtime.actors;

import org.integratedmodelling.klab.components.runtime.actors.SessionActor.RegisterMessage;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class EngineActor extends AbstractBehavior<String> {

  public static Behavior<String> create() {
    return Behaviors.setup(EngineActor::new);
  }

  private EngineActor(ActorContext<String> context) {
    super(context);
    context.getLog().info("Engine Actor started");
  }

  // No need to handle any messages
  @Override
  public Receive<String> createReceive() {
    return newReceiveBuilder()
    		.onMessageEquals("start", this::start)
    		.onSignal(PostStop.class, signal -> onPostStop()).build();
  }
  
  private Behavior<String> start() {
	    ActorRef<SessionActor.Command> firstRef = getContext().spawn(SessionActor.create(), "first-actor");

	    System.out.println("First: " + firstRef);
	    firstRef.tell("Start");
	    return Behaviors.same();
	  }

  private EngineActor onPostStop() {
    getContext().getLog().info("Engine Actor stopped");
    return this;
  }
}


