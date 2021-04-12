package org.integratedmodelling.klab.hub.users.services;

import java.util.Set;

import org.integratedmodelling.klab.hub.api.GroupEntry;
import org.integratedmodelling.klab.hub.api.User;
import org.integratedmodelling.klab.hub.exception.UserEmailExistsException;
import org.integratedmodelling.klab.hub.exception.UserExistsException;
import org.springframework.stereotype.Service;

@Service
public interface UserRegistrationService {
	public abstract User registerNewUser(String username, String email)
			throws UserExistsException, UserEmailExistsException;
	public abstract User verifyNewUser(String username);
	public abstract User setPassword(String username, String password, String confirm);
	public abstract User registerUser(User user);
	public abstract User registerInvitedUser(String username, String email, Set<GroupEntry> groups);
}
