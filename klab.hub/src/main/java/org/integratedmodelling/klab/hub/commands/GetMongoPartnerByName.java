package org.integratedmodelling.klab.hub.commands;

import java.util.Optional;

import org.integratedmodelling.klab.hub.api.MongoPartner;
import org.integratedmodelling.klab.hub.exception.BadRequestException;
import org.integratedmodelling.klab.hub.repository.MongoPartnerRepository;

public class GetMongoPartnerByName {
    private String name;
    private MongoPartnerRepository repository;
    
    public GetMongoPartnerByName(String name, MongoPartnerRepository repository) {
        super();
        this.name = name;
        this.repository = repository;
    }
    
    public Optional<MongoPartner> execute() {
        return repository.findByNameIgnoreCase(name);
    }

}
