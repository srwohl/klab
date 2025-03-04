package org.integratedmodelling.klab.rest;

import java.util.ArrayList;
import java.util.List;

public class ObservationRequest {

    private String urn;
    private String contextId;
    private String searchContextId;
    private List<String> scenarios = new ArrayList<>();

    public ObservationRequest() {
    }

    public ObservationRequest(String urn, String contextId, String contextSearchId) {
        this.urn = urn;
        this.contextId = contextId;
        this.searchContextId = contextSearchId;
    }

    public String getUrn() {
        return urn;
    }

    public void setUrn(String urn) {
        this.urn = urn;
    }

    public String getSearchContextId() {
        return searchContextId;
    }

    public void setSearchContextId(String searchContextId) {
        this.searchContextId = searchContextId;
    }

    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public List<String> getScenarios() {
        return scenarios;
    }

    public void setScenarios(List<String> scenarios) {
        this.scenarios = scenarios;
    }

	@Override
	public String toString() {
		return "ObservationRequest [urn=" + urn + ", contextId=" + contextId + ", searchContextId=" + searchContextId
				+ ", scenarios=" + scenarios + "]";
	}
    
    

}
