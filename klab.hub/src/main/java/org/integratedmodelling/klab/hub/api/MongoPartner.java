package org.integratedmodelling.klab.hub.api;

import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="Partners")
@TypeAlias("MongoPartner")
public class MongoPartner extends IdentityModel {

    @Indexed(unique = true)
    @NotNull(message = "Email field cannot be null or blank")
    private String email;

    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
}
