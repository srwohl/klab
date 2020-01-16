package org.integratedmodelling.klab.components.runtime.actors;



import org.integratedmodelling.klab.api.observations.IObservation;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class EngineActor extends AbstractBehavior<EngineActor.CommandEngine> {
	
   public interface CommandEngine{}
   
   public static class Start implements CommandEngine, SessionActor.Command{
       public final String name;

       public Start(String name) {
           this.name = name;
       }
   }
   
   public static class ObsMsg implements CommandEngine, SessionActor.Command{
       public IObservation observation;

       public ObsMsg(IObservation observation) {
           this.observation = observation;
       }
   }
   
   // Factory

  public static Behavior<CommandEngine> create() {
    return Behaviors.setup(EngineActor::new);
  }
  
  // Constructor 

  private EngineActor(ActorContext<CommandEngine> context) {
    super(context);
    context.getLog().info("Engine Actor started");
  }

  // No need to handle any messages
  @Override
  public Receive<CommandEngine> createReceive() {
    return newReceiveBuilder()
    		.onMessage(Start.class, this::onStart)
    		.onMessage(ObsMsg.class, this::onObsMsg)
    		.onSignal(PostStop.class, signal -> onPostStop())
    		.build();
  }
  
  private EngineActor onObsMsg(ObsMsg obs) {
	    ActorRef<SessionActor.Command> SessAct = getContext().spawn(SessionActor.create(), "session-actor");

	    System.out.println("session Actor: " + SessAct);
	    SessAct.tell(new SessionActor.RegisterMessage(obs.observation));
	    return this;
	  }
  
  private EngineActor onStart(Start str) {
	    ActorRef<SessionActor.Command> SessAct = getContext().spawn(SessionActor.create(), "session-actor");

	    System.out.println("session Actor: " + SessAct);
	    SessAct.tell(str);
	    return this;
	  }

  private EngineActor onPostStop() {
    getContext().getLog().info("Engine Actor stopped");
    return this;
  }
}


