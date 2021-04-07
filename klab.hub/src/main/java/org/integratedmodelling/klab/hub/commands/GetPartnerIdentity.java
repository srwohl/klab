package org.integratedmodelling.klab.hub.commands;

import org.integratedmodelling.klab.auth.Partner;
import org.integratedmodelling.klab.hub.api.MongoPartner;
import org.integratedmodelling.klab.rest.IdentityReference;

public class GetPartnerIdentity {

    private MongoPartner partner;

    public GetPartnerIdentity( MongoPartner partner ) {
        this.partner = partner;
    }

    public Partner execute() {
        IdentityReference ref = new IdentityReference(partner.getName(), partner.getEmail(),
                partner.getLastConnection().toString());
        return new Partner(ref);
    }

}
