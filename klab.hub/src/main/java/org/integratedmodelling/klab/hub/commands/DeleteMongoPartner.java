package org.integratedmodelling.klab.hub.commands;

import org.integratedmodelling.klab.hub.repository.MongoPartnerRepository;

public class DeleteMongoPartner {
    private String name;
    private MongoPartnerRepository repository;
    
    public DeleteMongoPartner(String name, MongoPartnerRepository repository) {
        super();
        this.name = name;
        this.repository = repository;
    }
    
    public void execute() {
        repository.findByNameIgnoreCase(name).ifPresent(node -> {
            repository.delete(node);
        });
    }
}
