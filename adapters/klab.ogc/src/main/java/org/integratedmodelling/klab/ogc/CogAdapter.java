package org.integratedmodelling.klab.ogc;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.integratedmodelling.kim.api.IPrototype;
import org.integratedmodelling.klab.api.data.IResource;
import org.integratedmodelling.klab.api.data.IResourceCalculator;
import org.integratedmodelling.klab.api.data.adapters.IResourceAdapter;
import org.integratedmodelling.klab.api.data.adapters.IResourceEncoder;
import org.integratedmodelling.klab.api.data.adapters.IResourceImporter;
import org.integratedmodelling.klab.api.data.adapters.IResourcePublisher;
import org.integratedmodelling.klab.api.data.adapters.IResourceValidator;
import org.integratedmodelling.klab.raster.cog.CogService;

public class CogAdapter implements IResourceAdapter {
	
	public static final String ID = "cog";
	
	static Map<String, CogService> services = new HashMap<>();
	static Map<String, File> fileCache = new HashMap<>();
	
	public static CogService getService(String cogUrl ) {
		if (services.containsKey(cogUrl)) {
			return services.get(cogUrl);
		}
		CogService ret = new CogService(cogUrl);
		if (ret != null) {
			services.put(cogUrl, ret);
		}
		return ret;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IResourceValidator getValidator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IResourcePublisher getPublisher() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IResourceEncoder getEncoder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IResourceImporter getImporter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IResourceCalculator getCalculator(IResource resource) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IPrototype> getResourceConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

}
