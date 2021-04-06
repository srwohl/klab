package org.integratedmodelling.klab.hub.commands;

import org.integratedmodelling.klab.hub.api.MongoPartner;
import org.integratedmodelling.klab.hub.repository.MongoPartnerRepository;

public class CreateMongoPartner {
    private MongoPartner partner;
    private MongoPartnerRepository repository;
    
    public CreateMongoPartner(MongoPartner partner, MongoPartnerRepository repository) {
        super();
        this.partner = partner;
        this.repository = repository;
    }
    
    public MongoPartner execute() {
        return repository.insert(partner);
    }
}
