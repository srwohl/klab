package org.integratedmodelling.klab.hub.config.dev;

import java.util.HashSet;
import java.util.Set;

import org.integratedmodelling.klab.hub.api.MongoGroup;
import org.integratedmodelling.klab.hub.api.MongoNode;
import org.integratedmodelling.klab.hub.api.MongoPartner;
import org.integratedmodelling.klab.hub.commands.CreateMongoNode;
import org.integratedmodelling.klab.hub.repository.MongoGroupRepository;
import org.integratedmodelling.klab.hub.repository.MongoNodeRepository;
import org.integratedmodelling.klab.hub.repository.MongoPartnerRepository;

public class CreateIntialNodes {

	private MongoNodeRepository nodeRepo;
	private MongoGroupRepository groupRepo;
	private MongoPartnerRepository partnerRepo;

	public CreateIntialNodes(MongoNodeRepository nodeRepo, MongoGroupRepository groupRepo, MongoPartnerRepository partnerRepo) {
	    this.nodeRepo = nodeRepo;
		this.groupRepo = groupRepo;
		this.partnerRepo = partnerRepo;
	}

	public void execute() {
	    Set<MongoGroup> hSet = new HashSet<MongoGroup>();
	    hSet.addAll(groupRepo.findAll());
	    
		MongoNode mongoNode = new MongoNode();
		mongoNode.setEmail("support@integratedmodelling.org");
		mongoNode.setGroups(hSet);
		mongoNode.setName("knot");
		mongoNode.setUrl("http://172.17.0.1:8287/node");
		new CreateMongoNode(mongoNode, nodeRepo).execute();
		
        MongoNode anotherNode = new MongoNode();
        anotherNode.setEmail("support@integratedmodelling.org");
        anotherNode.setGroups(hSet);
        anotherNode.setName("outside");
        anotherNode.setUrl("http://172.17.0.1:8287/node");
        if(partnerRepo.findAll().iterator().hasNext()) {
            anotherNode.setPartner(partnerRepo.findAll().iterator().next());    
        }
        new CreateMongoNode(anotherNode, nodeRepo).execute();
		
	}
	
	

}
