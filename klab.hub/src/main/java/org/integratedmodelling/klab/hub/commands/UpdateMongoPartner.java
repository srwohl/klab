package org.integratedmodelling.klab.hub.commands;

import org.integratedmodelling.klab.hub.api.MongoPartner;
import org.integratedmodelling.klab.hub.repository.MongoPartnerRepository;

public class UpdateMongoPartner {
    
    private MongoPartner partner;
    private MongoPartnerRepository repository;
    
    public UpdateMongoPartner(MongoPartner partner, MongoPartnerRepository repository) {
        super();
        this.partner = partner;
        this.repository = repository;
    }
    
    public MongoPartner execute() {
        return repository.save(partner);
    }
    
}
