/*
 * This file is part of k.LAB.
 * 
 * k.LAB is free software: you can redistribute it and/or modify it under the terms of the Affero
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * A copy of the GNU Affero General Public License is distributed in the root directory of the k.LAB
 * distribution (LICENSE.txt). If this cannot be found see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (C) 2007-2018 integratedmodelling.org and any authors mentioned in author tags. All
 * rights reserved.
 */
package org.integratedmodelling.klab;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;

import org.integratedmodelling.klab.api.services.IConfigurationService;
import org.integratedmodelling.klab.exceptions.KlabIOException;
import org.integratedmodelling.klab.utils.MiscUtilities;
import org.integratedmodelling.klab.utils.OS;

// TODO: Auto-generated Javadoc
/**
 * TODO use a declarative approach for all properties, so that there is one
 * place for all default settings and it's possible to override any of them
 * through global JVM settings.
 *
 * @author Ferd
 * @version $Id: $Id
 */
public enum Configuration implements IConfigurationService {

	INSTANCE;

	private OS os;

	private Properties properties;
	private File dataPath;
	private Level loggingLevel = Level.SEVERE;
	private Level notificationLevel = Level.INFO;

	/** The klab relative work path. */
	public String KLAB_RELATIVE_WORK_PATH = ".klab";

	private Configuration() {

		if (System.getProperty(KLAB_DATA_DIRECTORY) != null) {
			this.dataPath = new File(System.getProperty(KLAB_DATA_DIRECTORY));
		} else {
			String home = System.getProperty("user.home");
			if (System.getProperty(KLAB_WORK_DIRECTORY) != null) {
				KLAB_RELATIVE_WORK_PATH = System.getProperty(KLAB_WORK_DIRECTORY);
			}
			this.dataPath = new File(home + File.separator + KLAB_RELATIVE_WORK_PATH);

			/*
			 * make sure it's available for substitution in property files etc.
			 */
			System.setProperty(KLAB_DATA_DIRECTORY, this.dataPath.toString());
		}

		this.dataPath.mkdirs();

		// KLAB.info("k.LAB data directory set to " + dataPath);

		this.properties = new Properties();
		File pFile = new File(dataPath + File.separator + "klab.properties");
		if (!pFile.exists()) {
			try {
				pFile.createNewFile();
			} catch (IOException e) {
				throw new KlabIOException("cannot write to configuration directory");
			}
		}
		try (InputStream input = new FileInputStream(pFile)) {
			this.properties.load(input);
		} catch (Exception e) {
			throw new KlabIOException("cannot read configuration properties");
		}

		Services.INSTANCE.registerService(this, IConfigurationService.class);
	}

	/** {@inheritDoc} */
	@Override
	public Properties getProperties() {
		return this.properties;
	}

	@Override
	public String getProperty(String property, String defaultValue) {
		String ret = System.getProperty(property);
		if (ret == null) {
			ret = getProperties().getProperty(property);
		}
		return ret == null ? defaultValue : ret;
	}

	/**
	 * Non-API Save the properties after making changes from outside configuration.
	 * Should be used only internally, or removed in favor of a painful setting API.
	 */
	public void save() {

		File td = new File(dataPath + File.separator + "klab.properties");

		// String[] doNotPersist = new String[] { Project.ORIGINATING_NODE_PROPERTY };

		Properties p = new Properties();
		p.putAll(getProperties());

		// for (String dn : doNotPersist) {
		// p.remove(dn);
		// }

		try {
			p.store(new FileOutputStream(td), null);
		} catch (Exception e) {
			throw new KlabIOException(e);
		}

	}

	/**
	 * Use reasoner.
	 *
	 * @return a boolean.
	 */
	public boolean useReasoner() {
		return true;
	}

	/**
	 * Applies the standard k.LAB property pattern "klab.{service}.{property}" and
	 * retrieves the correspondent property.
	 * 
	 * @param service
	 * @param property
	 * @return
	 */
	public String getServiceProperty(String service, String property) {
		return getProperty("klab." + service + "." + property, null);
	}

	/** {@inheritDoc} */
	@Override
	public OS getOS() {

		if (this.os == null) {

			String osd = System.getProperty("os.name").toLowerCase();

			// TODO ALL these checks need careful checking
			if (osd.contains("windows")) {
				os = OS.WIN;
			} else if (osd.contains("mac")) {
				os = OS.MACOS;
			} else if (osd.contains("linux") || osd.contains("unix")) {
				os = OS.UNIX;
			}
		}

		return this.os;
	}

	/** {@inheritDoc} */
	@Override
	public File getDataPath(String subspace) {

		String dpath = dataPath.toString();
		File ret = dataPath;

		String[] paths = subspace.split("/");
		for (String path : paths) {
			ret = new File(dpath + File.separator + path);
			ret.mkdirs();
			dpath += File.separator + path;
		}
		return ret;
	}

	public File getDefaultExportDirectory() {
		File ret = new File(getProperties().getProperty(KLAB_EXPORT_PATH, dataPath + File.separator + "export"));
		ret.mkdirs();
		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isOffline() {
		return getProperties().getProperty(KLAB_OFFLINE, "false").equals("true");
	}

	/** {@inheritDoc} */
	@Override
	public boolean isDebugResolutionRanks() {
		return getProperties().getProperty(KLAB_DEBUG_RESOLUTION_RANKS, "false").equals("true");
	}

	/** {@inheritDoc} */
	@Override
	public File getDataPath() {
		return dataPath;
	}

	/** {@inheritDoc} */
	@Override
	public int getDataflowThreadCount() {
		// TODO Auto-generated method stub
		return 10;
	}

	/** {@inheritDoc} */
	@Override
	public int getTaskThreadCount() {
		// TODO Auto-generated method stub
		return 10;
	}

	/** {@inheritDoc} */
	@Override
	public int getScriptThreadCount() {
		// TODO Auto-generated method stub
		return 3;
	}

	public int getResourceThreadCount() {
		// TODO Auto-generated method stub
		return 3;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isRemoteResolutionEnabled() {
		// TODO tie to option + live setting
		return true;
	}

	@Override
	public boolean allowAnonymousUsage() {
		return true;
	}

	@Override
	public Level getLoggingLevel() {
		return loggingLevel;
	}

	@Override
	public Level getNotificationLevel() {
		return notificationLevel;
	}

	@Override
	public double getAcceptedSubsettingError() {
		// TODO Auto-generated method stub
		return 0.15;
	}

	@Override
	public boolean resolveAllInstances() {
		// TODO tie to engine configuration property
		return false;
	}

	@Override
	public int getMaxLiveObservationContextsPerSession() {
		// TODO tie to engine configuration property + live setting
		return 10;
	}

	public boolean useInMemoryDatabase() {
		return getProperties().getProperty(KLAB_USE_IN_MEMORY_DATABASE, "true").equals("true");
	}

	public long getResourceRecheckIntervalMs() {
		// TODO tie to engine configuration property. This is 10 minutes
		return 10 * 60 * 1000;
	}

	public boolean parallelizeContextualization() {
		return System.getProperty("parallel") != null
				|| properties.getProperty(KLAB_PARALLELIZE_CONTEXTUALIZATION, "false").equals("true");
	}

	public boolean useInMemoryStorage() {
		return System.getProperty("mmap") == null
				&& properties.getProperty(KLAB_USE_IN_MEMORY_STORAGE, "true").equals("true");
	}

	public File getExportFile(String export) {
		if (MiscUtilities.isRelativePath(export)) {
			return new File(getDefaultExportDirectory() + File.separator + export);
		}
		return new File(export);
	}

	public boolean forceResourcesOnline() {
		return System.getProperty("forceResourcesOnline") != null;
	}

	public File getTemporaryDataDirectory() {
		return new File(
				getProperties().getProperty(KLAB_TEMPORARY_DATA_DIRECTORY, System.getProperty("java.io.tmpdir")));
	}
}
