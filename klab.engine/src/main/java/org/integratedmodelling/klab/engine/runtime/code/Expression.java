package org.integratedmodelling.klab.engine.runtime.code;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.integratedmodelling.kim.api.IKimConcept.Type;
import org.integratedmodelling.klab.api.data.artifacts.IObjectArtifact;
import org.integratedmodelling.klab.api.data.general.IExpression;
import org.integratedmodelling.klab.api.documentation.IReport;
import org.integratedmodelling.klab.api.knowledge.IConcept;
import org.integratedmodelling.klab.api.knowledge.IMetadata;
import org.integratedmodelling.klab.api.knowledge.IObservable;
import org.integratedmodelling.klab.api.model.IModel;
import org.integratedmodelling.klab.api.model.INamespace;
import org.integratedmodelling.klab.api.observations.IDirectObservation;
import org.integratedmodelling.klab.api.observations.IObservation;
import org.integratedmodelling.klab.api.observations.IRelationship;
import org.integratedmodelling.klab.api.observations.ISubject;
import org.integratedmodelling.klab.api.observations.scale.IScale;
import org.integratedmodelling.klab.api.provenance.IArtifact;
import org.integratedmodelling.klab.api.provenance.IProvenance;
import org.integratedmodelling.klab.api.runtime.IContextualizationScope;
import org.integratedmodelling.klab.api.runtime.IEventBus;
import org.integratedmodelling.klab.api.runtime.IScheduler;
import org.integratedmodelling.klab.api.runtime.dataflow.IDataflow;
import org.integratedmodelling.klab.api.runtime.monitoring.IMonitor;
import org.integratedmodelling.klab.utils.Pair;
import org.integratedmodelling.klab.utils.Parameters;

public abstract class Expression implements IExpression {

  // A dummy context to use when we don't have a context to pass
  public static class Scope extends Parameters<String> implements IContextualizationScope {

    private IMonitor monitor;
    private INamespace namespace;
    
    public Scope(IMonitor monitor) {
      this.monitor = monitor;
    }

    Scope(IMonitor monitor, INamespace namespace) {
      this.monitor = monitor;
      this.namespace = namespace;
    }
    
    @Override
    public INamespace getNamespace() {
      return this.namespace;
    }

    @Override
    public IProvenance getProvenance() {
      return null;
    }

    @Override
    public IEventBus getEventBus() {
      return null;
    }

    @Override
    public Collection<IRelationship> getOutgoingRelationships(IDirectObservation observation) {
      return new ArrayList<>();
    }

    @Override
    public Collection<IRelationship> getIncomingRelationships(IDirectObservation observation) {
      return new ArrayList<>();
    }

    @Override
    public IArtifact getArtifact(String localName) {
      return null;
    }

    @Override
    public IMonitor getMonitor() {
      return monitor;
    }

    @Override
    public IObjectArtifact newObservation(IObservable observable, String name, IScale scale, IMetadata metadata) {
      return null;
    }

    @Override
    public IObjectArtifact newRelationship(IObservable observable, String name, IScale scale,
        IObjectArtifact source, IObjectArtifact target, IMetadata metadata) {
      return null;
    }

    @Override
    public Type getArtifactType() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public <T extends IArtifact> Collection<Pair<String, T>> getArtifacts(Class<T> type) {
      return new ArrayList<>();
    }

    @Override
    public IScale getScale() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public IObservable getSemantics(String identifier) {
      return null;
    }

    @Override
    public IArtifact getTargetArtifact() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public <T extends IArtifact> T getArtifact(String localName, Class<T> cls) {
      // TODO Auto-generated method stub
      return null;
    }

	@Override
	public IObservable getTargetSemantics() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTargetName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ISubject getSourceSubject(IRelationship relationship) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ISubject getTargetSubject(IRelationship relationship) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDirectObservation getContextObservation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDirectObservation getParentOf(IObservation observation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IObservation> getChildrenOf(IObservation observation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IScheduler getScheduler() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IReport getReport() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IModel getModel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IArtifact> getArtifact(IConcept observable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public org.integratedmodelling.klab.api.data.general.IExpression.Context getExpressionContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends IArtifact> T getArtifact(IConcept concept, Class<T> cls) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDataflow<?> getDataflow() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> getSymbolTable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IArtifact> getAdditionalOutputs() {
		// TODO Auto-generated method stub
		return null;
	}
  }
  
  public static IContextualizationScope emptyContext(IMonitor monitor) {
    return new Scope(monitor);
  }
  
  public static IContextualizationScope emptyContext(IMonitor monitor, INamespace namespace) {
    return new Scope(monitor, namespace);
  }    
}
