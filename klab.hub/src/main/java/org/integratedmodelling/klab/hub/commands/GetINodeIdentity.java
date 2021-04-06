package org.integratedmodelling.klab.hub.commands;

import org.integratedmodelling.klab.Authentication;
import org.integratedmodelling.klab.api.auth.INodeIdentity;
import org.integratedmodelling.klab.api.auth.IPartnerIdentity;
import org.integratedmodelling.klab.auth.Hub;
import org.integratedmodelling.klab.auth.Node;
import org.integratedmodelling.klab.auth.Partner;
import org.integratedmodelling.klab.hub.api.MongoNode;
import org.integratedmodelling.klab.rest.IdentityReference;

public class GetINodeIdentity {

	private MongoNode node;
	private Hub hub;
	
	
	public GetINodeIdentity(MongoNode node) {
		super();
		this.node = node;
		this.hub = Authentication.INSTANCE.getAuthenticatedIdentity(Hub.class);
	}

	public INodeIdentity execute() {
	    if(node.getPartner() == null) {
	        
	        INodeIdentity ident = new Node(hub.getName() + "." + node.getName(), hub.getParentIdentity());
	        ident.getUrls().add(node.getUrl());
	        return ident;
	        
	    } else {
	        
	        IdentityReference ref = new IdentityReference(node.getPartner().getName(),
	                node.getPartner().getEmail(),
	                node.getPartner().getLastConnection().toString());
	        
	        Partner partner = new Partner(ref);
	        
	        INodeIdentity ident = new Node(partner.getUsername() + "." + node.getName(), partner);
	        ident.getUrls().add(node.getUrl());
	        return ident;
	        
	    }
	}

}
