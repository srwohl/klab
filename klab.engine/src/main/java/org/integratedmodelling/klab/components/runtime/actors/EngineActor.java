package org.integratedmodelling.klab.components.runtime.actors;



import org.integratedmodelling.klab.api.observations.IObservation;
import org.integratedmodelling.klab.components.runtime.observations.Observation;
import org.integratedmodelling.klab.engine.runtime.Session;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class EngineActor extends AbstractBehavior<EngineActor.CommandEngine> {
	
	// Messages
	
   public interface CommandEngine{}
   
   
   public static class ObsMsg implements CommandEngine, SessionActor.Command{
       public Observation observation;

       public ObsMsg(Observation observation) {
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

  @Override
  public Receive<CommandEngine> createReceive() {
    return newReceiveBuilder()
    		.onMessage(ObsMsg.class, this::onObsMsg)
    		.onSignal(PostStop.class, signal -> onPostStop())
    		.build();
  }
  
  private EngineActor onObsMsg(ObsMsg obs) {
	    ActorRef<SessionActor.Command> SessAct = getContext().spawn(SessionActor.create(), "s"+obs.observation.getId());

	    getContext().getLog().info("session Actor: " + SessAct);
	    SessAct.tell(new SessionActor.RegisterObsMessage(obs.observation));
	    return this;
	  }
  

  private EngineActor onPostStop() {
    getContext().getLog().info("Engine Actor stopped");
    return this;
  }
}


