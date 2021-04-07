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

    public GetINodeIdentity( MongoNode node ) {
        super();
        this.node = node;
        this.hub = Authentication.INSTANCE.getAuthenticatedIdentity(Hub.class);
    }

    public INodeIdentity execute() {
        INodeIdentity ident = null;

        if (node.getPartner() == null) {
            ident = new Node(hub.getName() + "." + node.getName(), hub.getParentIdentity());
        } else {
            Partner partner = new GetPartnerIdentity(node.getPartner()).execute();
            ident = new Node(partner.getUsername() + "." + node.getName(), partner);
        }

        ident.getUrls().add(node.getUrl());
        return ident;
    }

}
