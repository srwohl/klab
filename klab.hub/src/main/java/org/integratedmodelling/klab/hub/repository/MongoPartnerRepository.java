package org.integratedmodelling.klab.hub.repository;

import org.bson.types.ObjectId;
import org.integratedmodelling.klab.hub.api.MongoNode;
import org.integratedmodelling.klab.hub.api.MongoPartner;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MongoPartnerRepository extends MongoRepository<MongoPartner, ObjectId>{
	
	Optional<MongoPartner> findById(String id);
	
	Optional<MongoPartner> findByNameIgnoreCase(String name);
	
	Optional<MongoPartner> findByEmailIgnoreCase(String email);
	
	Optional<MongoPartner> findByNameIgnoreCaseOrEmailIgnoreCase(String username, String email);
	
    Boolean existsByNameIgnoreCase(String node);

    Boolean existsByEmailIgnoreCase(String email);
    
	public List<MongoPartner> findAll();
}
