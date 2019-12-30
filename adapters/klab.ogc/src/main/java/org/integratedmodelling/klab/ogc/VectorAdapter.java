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
package org.integratedmodelling.klab.ogc;

import java.util.Set;

import org.integratedmodelling.kim.api.IPrototype;
import org.integratedmodelling.klab.Dataflows;
import org.integratedmodelling.klab.Version;
import org.integratedmodelling.klab.api.data.IResource;
import org.integratedmodelling.klab.api.data.IResourceCalculator;
import org.integratedmodelling.klab.api.data.adapters.IFileResourceAdapter;
import org.integratedmodelling.klab.api.data.adapters.IResourceEncoder;
import org.integratedmodelling.klab.api.data.adapters.IResourceImporter;
import org.integratedmodelling.klab.api.data.adapters.IResourcePublisher;
import org.integratedmodelling.klab.api.data.adapters.IResourceValidator;
import org.integratedmodelling.klab.api.extensions.ResourceAdapter;
import org.integratedmodelling.klab.kim.Prototype;
import org.integratedmodelling.klab.ogc.vector.files.VectorEncoder;
import org.integratedmodelling.klab.ogc.vector.files.VectorImporter;
import org.integratedmodelling.klab.ogc.vector.files.VectorPublisher;
import org.integratedmodelling.klab.ogc.vector.files.VectorValidator;

import com.google.common.collect.Sets;

/**
 * The Class VectorAdapter.
 */
@ResourceAdapter(type = "vector", version = Version.CURRENT, requires = { "fileUrl" }, optional = { "computeUnion",
		"computeHull", "filter", "nameAttribute", "sanitize" })
public class VectorAdapter implements IFileResourceAdapter {

	/**
	 * All recognized primary file extensions.
	 */
	public static Set<String> fileExtensions = Sets.newHashSet("shp");

	/**
	 * All recognized secondary file extensions
	 */
	public static Set<String> secondaryFileExtensions = Sets.newHashSet("sbx", "prj", "shx", "dbf", "shp.xml", "txt",
			"pdf");

	@Override
	public String getName() {
		return "vector";
	}

	@Override
	public IResourceValidator getValidator() {
		return new VectorValidator();
	}

	@Override
	public IResourcePublisher getPublisher() {
		return new VectorPublisher();
	}

	@Override
	public IResourceEncoder getEncoder() {
		return new VectorEncoder();
	}

	@Override
	public IResourceImporter getImporter() {
		return new VectorImporter();
	}
	
	@Override
	public IPrototype getResourceConfiguration() {
		return new Prototype(
				Dataflows.INSTANCE.declare(getClass().getClassLoader().getResource("ogc/prototypes/vector.kdl"))
						.getActuators().iterator().next(),
				null);
	}

	@Override
	public IResourceCalculator getCalculator(IResource resource) {
		// TODO Auto-generated method stub
		return null;
	}
}
