package org.integratedmodelling.klab.engine.rest.controllers.network;

import org.integratedmodelling.klab.api.auth.Roles;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RestController;

/**
 * The controller implementing the {@link org.integratedmodelling.klab.api.API.NETWORK network API}.
 * 
 * @author ferdinando.villa
 *
 */
@RestController
@Secured({ Roles.ENGINE_USER, Roles.ADMIN })
public class NetworkController {

	
	
}
