package org.integratedmodelling.klab.rest;

import java.net.URL;

public class ResourceImportRequest {

	private URL importUrl;
	private String targetResourceUrn;
	private String adapter;
	private String projectName;
	private boolean bulkImport;
	private String regex;
	private String workspace;

	public ResourceImportRequest() {
	}

	public ResourceImportRequest(URL url, String project) {
		this.importUrl = url;
		this.projectName = project;
	}
	
	public URL getImportUrl() {
		return importUrl;
	}

	public void setImportUrl(URL importUrl) {
		this.importUrl = importUrl;
	}

	public String getAdapter() {
		return adapter;
	}

	public void setAdapter(String adapter) {
		this.adapter = adapter;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public boolean isBulkImport() {
		return bulkImport;
	}

	public void setBulkImport(boolean bulkImport) {
		this.bulkImport = bulkImport;
	}

    public String getTargetResourceUrn() {
        return targetResourceUrn;
    }

    public void setTargetResourceUrn(String targetResourceUrn) {
        this.targetResourceUrn = targetResourceUrn;
    }

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regexp) {
		this.regex = regexp;
	}

	public void setWorkspace(String workspace) {
		this.workspace = workspace;
	}
	
	public String getWorkspace() {
		return workspace;
	}

}
