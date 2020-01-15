/*
 * This file is part of k.LAB.
 * 
 * k.LAB is free software: you can redistribute it and/or modify
 * it under the terms of the Affero GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * A copy of the GNU Affero General Public License is distributed in the root
 * directory of the k.LAB distribution (LICENSE.txt). If this cannot be found 
 * see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (C) 2007-2018 integratedmodelling.org and any authors mentioned
 * in author tags. All rights reserved.
 */
package org.integratedmodelling.klab.ogc.vector.files;

import org.integratedmodelling.klab.api.data.IResource;
import org.integratedmodelling.klab.api.data.adapters.IResourcePublisher;
import org.integratedmodelling.klab.api.runtime.monitoring.IMonitor;
import org.integratedmodelling.klab.exceptions.KlabException;

/**
 * The raster publisher will attempt WCS publishing if a WCS server is
 * connected.
 * 
 * @author ferdinando.villa
 *
 */
public class VectorPublisher implements IResourcePublisher {

	@Override
	public IResource publish(IResource localResource, IMonitor monitor) throws KlabException {

		IResource ret = localResource;

		/*
		 * If we have Postgis + Geoserver dedicated to this node instance, publish in
		 * them and turn the resource into a WFS one.
		 */
		
		return ret;
	}

}
