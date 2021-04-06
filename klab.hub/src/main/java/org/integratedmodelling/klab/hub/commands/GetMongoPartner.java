package org.integratedmodelling.klab.hub.commands;

import java.util.List;

import org.integratedmodelling.klab.hub.api.MongoPartner;
import org.integratedmodelling.klab.hub.repository.MongoPartnerRepository;

public class GetMongoPartner {
    
    private MongoPartnerRepository repository;
    
    public GetMongoPartner(MongoPartnerRepository repository) {
        super();
        this.repository = repository;
    }
    
    public List<MongoPartner> execute() {
        return repository.findAll();
    }

}
