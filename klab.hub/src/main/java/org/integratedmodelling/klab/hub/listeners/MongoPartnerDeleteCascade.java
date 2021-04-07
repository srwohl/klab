package org.integratedmodelling.klab.hub.listeners;

import org.integratedmodelling.klab.Logging;
import org.integratedmodelling.klab.hub.api.MongoPartner;
import org.integratedmodelling.klab.hub.nodes.services.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterDeleteEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeDeleteEvent;
import org.springframework.stereotype.Component;

@Component
public class MongoPartnerDeleteCascade extends AbstractMongoEventListener<MongoPartner> {
    
    @Autowired
    public MongoPartnerDeleteCascade( NodeService service ) {
        super();
        this.service = service;
    }

    private NodeService service;
    
    
    public void onBeforeDelete(BeforeDeleteEvent<MongoPartner> event) {
        String id = event.getDocument().get("_id").toString();
        service.removePartnerFromNodes(id);
        Logging.INSTANCE.info(String.format("Before deleteing MongoPartner: %s, clean up MongoNodes", id));
    }
    
    public void onAfterDelete(AfterDeleteEvent<MongoPartner> event) {
        String id = event.getDocument().get("_id").toString();
        Logging.INSTANCE.info(String.format("Deleted MongoPartner: %s", id));
    }

}
