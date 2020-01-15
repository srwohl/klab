package org.integratedmodelling.klab.data.encoding;

import java.util.Map;

import org.integratedmodelling.klab.api.data.IGeometry;
import org.integratedmodelling.klab.api.data.ILocator;
import org.integratedmodelling.klab.api.data.adapters.IKlabData;
import org.integratedmodelling.klab.api.data.adapters.IKlabData.Builder;
import org.integratedmodelling.klab.api.runtime.IContextualizationScope;
import org.integratedmodelling.klab.api.runtime.rest.INotification;

/**
 * A builder that encodes the data into a Protobuf object which will be sent
 * over the network for reconstruction by a matching {@link IKlabData} object at
 * the client end. The build() step is normally not called here, but uses the
 * {@link RemoteData} class for completeness and testing.
 * 
 * @author ferdinando.villa
 *
 */
public class DecodingDataBuilder implements IKlabData.Builder {

	IContextualizationScope context;
	private Map<?, ?> data;

	public DecodingDataBuilder(Map<?, ?> data, IContextualizationScope context) {
		this.data = data;
		this.context = context;
	}

	@Override
	public Builder startState(String name) {
		throw new IllegalStateException("modifying methods should not be called on a decoding builder");
	}

	@Override
	public Builder finishState() {
		throw new IllegalStateException("modifying methods should not be called on a decoding builder");
	}

	@Override
	public Builder startObject(String artifactName, String objectName, IGeometry scale) {
		throw new IllegalStateException("modifying methods should not be called on a decoding builder");
	}

	@Override
	public Builder finishObject() {
		throw new IllegalStateException("modifying methods should not be called on a decoding builder");
	}

	@Override
	public Builder withMetadata(String property, Object object) {
		throw new IllegalStateException("modifying methods should not be called on a decoding builder");
	}

	@Override
	public Builder addNotification(INotification notification) {
		throw new IllegalStateException("modifying methods should not be called on a decoding builder");
	}

	@Override
	public IKlabData build() {
		return new LocalData(data, context);
	}

	@Override
	public void add(Object value) {
		throw new IllegalStateException("modifying methods should not be called on a decoding builder");
	}

	@Override
	public void add(Object value, ILocator offset) {
		throw new IllegalStateException("modifying methods should not be called on a decoding builder");
	}
}
