package org.integratedmodelling.klab.components.runtime.actors;

import org.integratedmodelling.klab.Authentication;
import org.integratedmodelling.klab.api.engine.IEngine;
import org.integratedmodelling.klab.api.observations.IObservation;
import org.integratedmodelling.klab.components.runtime.actors.EngineActor.CommandEngine;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class MainActor {
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		IObservation observation;
		
		ActorSystem<CommandEngine> rootActorSystem = ActorSystem
				.create(EngineActor.create(),Authentication.INSTANCE.getAuthenticatedIdentity(IEngine.class).getId());
				
		
		rootActorSystem.tell(new EngineActor.Start("start"));
		

	}

}


