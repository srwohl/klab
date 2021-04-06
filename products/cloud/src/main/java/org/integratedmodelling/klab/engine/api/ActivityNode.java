package org.integratedmodelling.klab.engine.api;

import java.util.ArrayList;

import org.integratedmodelling.klab.rest.SessionActivity;

public class ActivityNode {
    
    SessionActivity nodeRoot;
    ArrayList<ActivityNode> children;
    
    public ActivityNode(SessionActivity nodeRoot) {
        super();
        this.nodeRoot = nodeRoot;
        this.children = new ArrayList<>();
    }
    
    private void addChild(SessionActivity activity) {
        ActivityNode child = new ActivityNode(activity);
        addChild(child);
    }
    
    private void addChild(ActivityNode node) {
        this.children.add(node);
    }
    
    private ArrayList<ActivityNode> getChildren() {
        return children;
    }


    public SessionActivity getNodeRoot() {
        return nodeRoot;
    }
    
    public boolean hasNodeRoot(String id) {
        return nodeRoot.getActivityId().equals(id);
    }
    
    public boolean insertActivityNode(String parentId, SessionActivity current) {
        if(parentId.equals(nodeRoot.getActivityId())) {
            addChild(current);
            return true;
        } else {
            if (getChildren().size() > 0) {
                for(ActivityNode child : getChildren()) {
                    if (child.insertActivityNode(parentId, current)) {
                        return true;
                    };
                }
            }
            return false;
        }
    }
    
    private void updateActivity(SessionActivity update) {
        if(nodeRoot.getActivityId() == update.getActivityId()) {
            nodeRoot = update;
            return;
        } else {
            if (getChildren().size() > 0) {
                for(ActivityNode child : getChildren()) {
                    child.updateActivity(update);
                }
            }
            return;
        }
    }
    
    public ActivityNode findActivityNode(String id, ActivityNode memo) {
        if(memo != null) {
            return memo;
        } else if (nodeRoot.getActivityId() == id) {
            memo = this;
            return memo;
        } else if (getChildren().size() > 0) {
            for(ActivityNode child : getChildren()) {
                memo = child.findActivityNode(id, memo);
                if(memo != null) {
                    return memo;
                }
            }
        } else {
            return memo;
        }
        return memo;
    }

    public static void main(String[] args) throws Exception {
        ArrayList<ActivityNode> list = new ArrayList<ActivityNode>();
        int[] intArray = new int[]{ 1,2,3,4,5}; 
        for (int i : intArray ) {
            SessionActivity nodeRoot = new SessionActivity();
            nodeRoot.setActivityId("NODE_" + i);
            list.add(new ActivityNode(nodeRoot));
        }
        
        list.forEach(action -> {
            SessionActivity nodeRoot = new SessionActivity();
            nodeRoot.setActivityId("NODE_" + 6);    
            action.insertActivityNode("NODE_1", nodeRoot);
        });
        
        list.forEach(action -> {
            SessionActivity nodeRoot = new SessionActivity();
            nodeRoot.setActivityId("NODE_" + 7);    
            action.insertActivityNode("NODE_6", nodeRoot);
        });
        
        ActivityNode test = null;
        for (ActivityNode node : list ) {
            if(test == null) {
                test = node.findActivityNode("NODE_7", test);
            }
        }
        test.getNodeRoot();
    }
}

