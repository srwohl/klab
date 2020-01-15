package org.integratedmodelling.klab.components.runtime.actors;



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
    		.onSignal(PostStop.class, signal -> onPostStop())
    		.build();
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


