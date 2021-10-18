package org.integratedmodelling.klab.raster.cog;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.integratedmodelling.kim.api.IParameters;
import org.integratedmodelling.klab.api.data.ILocator;
import org.integratedmodelling.klab.api.data.IResource;
import org.integratedmodelling.klab.api.data.IResource.Builder;
import org.integratedmodelling.klab.api.data.adapters.IResourceImporter;
import org.integratedmodelling.klab.api.observations.IObservation;
import org.integratedmodelling.klab.api.runtime.monitoring.IMonitor;
import org.integratedmodelling.klab.ogc.CogAdapter;
import org.integratedmodelling.klab.raster.cog.api.CogLayer;
import org.integratedmodelling.klab.utils.Triple;

public class CogImporter implements IResourceImporter {
	
	CogValidator validator = new CogValidator();

	@Override
	public IResourceImporter withOption(String option, Object value) {
		return this;
	}
	
	@Override
	public Collection<Builder> importResources(String importLocation, IParameters<String> userData, IMonitor monitor) {
		List<Builder> ret = new ArrayList<>();
		CogService cog = CogAdapter.getService(importLocation);
		CogLayer layer = cog.getLayer();
		return ret;
	}

	@Override
	public boolean acceptsMultiple() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean importIntoResource(URL importLocation, IResource target, IMonitor monitor) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canHandle(String importLocation, IParameters<String> userData) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean resourceCanHandle(IResource resource, String importLocation) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Triple<String, String, String>> getExportCapabilities(IObservation observation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File exportObservation(File file, IObservation observation, ILocator locator, String format,
			IMonitor monitor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> getExportCapabilities(IResource resource) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean exportResource(File file, IResource resource, String format) {
		// TODO Auto-generated method stub
		return false;
	}

}
