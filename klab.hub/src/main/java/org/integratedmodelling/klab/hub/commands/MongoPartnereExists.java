package org.integratedmodelling.klab.hub.commands;

import org.integratedmodelling.klab.hub.repository.MongoPartnerRepository;

public class MongoPartnereExists {
    
    private String name;
    private MongoPartnerRepository repository;

    public MongoPartnereExists(String name, MongoPartnerRepository repository) {
        this.name = name;
        this.repository = repository;
    }

    public boolean execute() {
        return repository.existsByNameIgnoreCase(name);
    }
}
