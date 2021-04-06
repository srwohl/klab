package org.integratedmodelling.klab.hub.partners.services;

import java.util.Collection;

import javax.ws.rs.BadRequestException;

import org.integratedmodelling.klab.hub.api.MongoPartner;
import org.integratedmodelling.klab.hub.commands.CreateMongoPartner;
import org.integratedmodelling.klab.hub.commands.DeleteMongoPartner;
import org.integratedmodelling.klab.hub.commands.GetMongoPartner;
import org.integratedmodelling.klab.hub.commands.GetMongoPartnerById;
import org.integratedmodelling.klab.hub.commands.GetMongoPartnerByName;
import org.integratedmodelling.klab.hub.commands.MongoNodeExists;
import org.integratedmodelling.klab.hub.commands.MongoPartnereExists;
import org.integratedmodelling.klab.hub.commands.UpdateMongoPartner;
import org.integratedmodelling.klab.hub.repository.MongoPartnerRepository;
import org.integratedmodelling.klab.hub.service.GenericHubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PartnerServiceImpl implements PartnerService {
    
    private MongoPartnerRepository partnerRepository;

    @Autowired
    public PartnerServiceImpl(MongoPartnerRepository partnerRepository) {
        super();
        this.partnerRepository = partnerRepository;
    }

    @Override
    public MongoPartner create(MongoPartner partner) {
        if (!exists(partner.getName())) {
            return new CreateMongoPartner(partner, partnerRepository).execute();
        } else {
            throw new BadRequestException(partner.getName() + " already exists.");
        }
    }

    @Override
    public MongoPartner update(MongoPartner partner) {
        if (exists(partner.getName())) {
            return new UpdateMongoPartner(partner, partnerRepository).execute();
        } else {
            throw new BadRequestException(partner.getName() + " does not exist.");
        }
    }

    @Override
    public void delete(String name) {
        if (exists(name)) {
           new DeleteMongoPartner(name, partnerRepository).execute();
           return;
        } else {
            throw new BadRequestException(name + " does not exist.");
        }
    }

    @Override
    public Collection<MongoPartner> getAll() {
        return new GetMongoPartner(partnerRepository).execute();
    }

    @Override
    public MongoPartner getByName(String name) {
        return new GetMongoPartnerByName(name, partnerRepository).execute()
                .orElseThrow(() -> new BadRequestException("Partner by that name does not exist"));
    }

    @Override
    public MongoPartner getById(String id) {
        return new GetMongoPartnerById(id, partnerRepository).execute()
                .orElseThrow(() -> new BadRequestException("Partner by that id does not exist"));
    }

    @Override
    public boolean exists(String name) {
        return new MongoPartnereExists(name, partnerRepository).execute();
    }

}
