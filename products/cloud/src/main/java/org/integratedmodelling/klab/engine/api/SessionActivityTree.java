package org.integratedmodelling.klab.engine.api;

import java.util.ArrayList;
import java.util.List;

import org.integratedmodelling.klab.rest.SessionActivity;
import org.integratedmodelling.klab.rest.SessionReference;

public class SessionActivityTree {
    
    public SessionActivityTree() {
    }
    
    String id;
    SessionReference reference;
    List<ActivityNode> activities = new ArrayList<ActivityNode>();
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public SessionReference getReference() {
        return reference;
    }
    public void setReference(SessionReference reference) {
        this.reference = reference;
    }
    
    public List<ActivityNode> getActivities() {
        return activities;
    }
    public void setActivities(List<ActivityNode> activities) {
        this.activities = activities;
    }
    
    public void updateWithRootActivity(SessionActivity update) {
        activities.add(new ActivityNode(update));
    }
    
    public void updateWithActivity(SessionActivity update) {
        if(activities.size() == 0) {
            activities.add(new ActivityNode(update));
        } else {
            if (update.getParentActivityId() == null) {
                activities.add(new ActivityNode(update));
            } else {
                activities.forEach(act -> {
                    if(act.insertActivityNode(update.getParentActivityId(), update)) {
                        return;
                    }
                });
            }
        }
    }
}
