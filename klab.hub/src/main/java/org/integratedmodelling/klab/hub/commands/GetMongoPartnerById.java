package org.integratedmodelling.klab.hub.commands;

import java.util.Optional;

import org.integratedmodelling.klab.hub.api.MongoPartner;
import org.integratedmodelling.klab.hub.repository.MongoPartnerRepository;

public class GetMongoPartnerById {
    private String id;
    private MongoPartnerRepository repository;

    public GetMongoPartnerById(String id, MongoPartnerRepository repository) {
        this.id = id;
        this.repository = repository;
    }

    public Optional<MongoPartner> execute() {
        return repository.findById(id);
    }

}
