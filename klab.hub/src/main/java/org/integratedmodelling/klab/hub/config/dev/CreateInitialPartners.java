package org.integratedmodelling.klab.hub.config.dev;

import org.integratedmodelling.klab.hub.api.MongoPartner;
import org.integratedmodelling.klab.hub.commands.CreateMongoPartner;
import org.integratedmodelling.klab.hub.repository.MongoPartnerRepository;
import org.joda.time.DateTime;

public class CreateInitialPartners {
    
    private MongoPartnerRepository repository;

    public CreateInitialPartners(MongoPartnerRepository repository) {
        this.repository = repository;
        
    }

    public void execute() {
        MongoPartner partner = new MongoPartner();
        partner.setName("im");
        partner.setLastConnection();
        partner.setRegistrationDate(DateTime.now());
        partner.setEmail("admin@integratedmodelling.org");
        new CreateMongoPartner(partner, repository).execute();
    }
}
