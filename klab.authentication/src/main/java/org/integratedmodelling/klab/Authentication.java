package org.integratedmodelling.klab;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.integratedmodelling.klab.api.auth.ICertificate;
import org.integratedmodelling.klab.api.auth.ICertificate.Type;
import org.integratedmodelling.klab.api.auth.IIdentity;
import org.integratedmodelling.klab.api.auth.INodeIdentity;
import org.integratedmodelling.klab.api.auth.IPartnerIdentity;
import org.integratedmodelling.klab.api.auth.IUserIdentity;
import org.integratedmodelling.klab.api.runtime.ISession;
import org.integratedmodelling.klab.api.services.IAuthenticationService;
import org.integratedmodelling.klab.auth.AnonymousEngineCertificate;
import org.integratedmodelling.klab.auth.Hub;
import org.integratedmodelling.klab.auth.KlabCertificate;
import org.integratedmodelling.klab.auth.KlabUser;
import org.integratedmodelling.klab.auth.NetworkSession;
import org.integratedmodelling.klab.auth.Node;
import org.integratedmodelling.klab.auth.Partner;
import org.integratedmodelling.klab.communication.client.Client;
import org.integratedmodelling.klab.exceptions.KlabAuthorizationException;
import org.integratedmodelling.klab.exceptions.KlabIOException;
import org.integratedmodelling.klab.exceptions.KlabMissingCredentialsException;
import org.integratedmodelling.klab.rest.EngineAuthenticationRequest;
import org.integratedmodelling.klab.rest.EngineAuthenticationResponse;
import org.integratedmodelling.klab.rest.ExternalAuthenticationCredentials;
import org.integratedmodelling.klab.rest.Group;
import org.integratedmodelling.klab.rest.HubReference;
import org.integratedmodelling.klab.rest.IdentityReference;
import org.integratedmodelling.klab.rest.ObservableReference;
import org.integratedmodelling.klab.utils.FileCatalog;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormat;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

public enum Authentication implements IAuthenticationService {

    /**
     * The global instance singleton.
     */
    INSTANCE;

    String LOCAL_HUB_URL = "http://127.0.0.1:8284/hub";

    /**
     * Local catalog of all partner identities registered from the network.
     * 
     */
    Map<String, Partner> partners = Collections.synchronizedMap(new HashMap<>());

    /**
     * All identities available for inspection (sessions, users). The network session is a singleton
     * (or a zeroton) so it's not included as its ID conflicts with the user holding it.
     */
    Map<String, IIdentity> identities = Collections.synchronizedMap(new HashMap<>());

    /**
     * any external credentials taken from the .klab/credentials.json file if any.
     */
    private FileCatalog<ExternalAuthenticationCredentials> externalCredentials;

    /**
     * Status of a user wrt. the network. Starts at UNKNOWN.
     */
    public enum Status {
        /**
         * User is authenticated locally but not online, either for lack of authentication or lack
         * of network connection/
         */
        OFFLINE,
        /**
         * User is authenticated and online with the network.
         */
        ONLINE,
        /**
         * User has not been submitted for authentication yet.
         */
        UNKNOWN
    }

    /**
     * ID for an anonymous user. Unsurprising.
     */
    public static final String ANONYMOUS_USER_ID = "anonymous";

    /**
     * Id for anonymous user who is connecting from the local host. Will have admin privileges.
     */
    public static final String LOCAL_USER_ID = "local";

    private Client client = Client.create();

    private Authentication() {

        /*
         * Create or load the external credentials file
         */
        File file = new File(Configuration.INSTANCE.getDataPath() + File.separator + "credentials.json");
        if (!file.exists()) {
            try {
                FileUtils.writeStringToFile(file, "{}");
            } catch (IOException e) {
                throw new KlabIOException(e);
            }
        }
        externalCredentials = FileCatalog.create(file, ExternalAuthenticationCredentials.class,
                ExternalAuthenticationCredentials.class);

        Services.INSTANCE.registerService(this, IAuthenticationService.class);
    }

    /**
     * If the partner mentioned in the response bean is already known, return it, otherwise create
     * it.
     * 
     * @param partner
     * @return a partner identity for the passed description
     */
    public Partner requirePartner(IdentityReference partner) {
        Partner ret = partners.get(partner.getId());
        if (ret == null) {
            ret = new Partner(partner);
            partners.put(partner.getId(), ret);
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IIdentity> T getIdentity(String id, Class<T> type) {
        IIdentity ret = identities.get(id);
        if (ret != null && type.isAssignableFrom(ret.getClass())) {
            return (T) ret;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IIdentity> T getAuthenticatedIdentity(Class<T> type) {
        for (IIdentity id : identities.values()) {
            if (type.isAssignableFrom(id.getClass())) {
                return (T) id;
            }
        }
        return null;
    }

    /**
     * Register a new session and inject a close listener that will de-register it on expiration.
     * 
     * @param session
     */
    public void registerSession(ISession session) {

        session.addListener(new ISession.Listener(){
            @Override
            public void onClose(ISession session) {
                identities.remove(session.getId());
            }
        });
        identities.put(session.getId(), session);
    }

    /**
     * Register any new identity with this.
     * 
     * @param identity
     */
    public void registerIdentity(IIdentity identity) {
        identities.put(identity.getId(), identity);
    }

    /**
     * Util to extract an identity from the principal of a Spring request. TODO make another to
     * return a specific type or throw an authorization exception
     * 
     * @param principal
     * @return the identity or null
     */
    public IIdentity getIdentity(Principal principal) {
        if (principal instanceof PreAuthenticatedAuthenticationToken
                && ((PreAuthenticatedAuthenticationToken) principal).getPrincipal() instanceof IIdentity) {
            return (IIdentity) ((PreAuthenticatedAuthenticationToken) principal).getPrincipal();
        }
        return null;
    }

    /**
     * If a session with the default flag was started, return it.
     * 
     * @return
     */
    public ISession getDefaultSession() {
        for (IIdentity id : identities.values()) {
            if (id instanceof ISession && ((ISession) id).isDefault()) {
                return (ISession) id;
            }
        }
        return null;
    }

    @Override
    public IIdentity authenticate(ICertificate certificate) throws KlabAuthorizationException {

        IIdentity ret = null;
        EngineAuthenticationResponse authentication = null;

        if (certificate instanceof AnonymousEngineCertificate) {
            // no partner, no node, no token, no nothing. REST calls automatically accept
            // the
            // anonymous user when secured as Roles.PUBLIC.
            Logging.INSTANCE.info("No user certificate: continuing in anonymous offline mode");

            return new KlabUser(Authentication.ANONYMOUS_USER_ID, null);
        }

        if (certificate.getType() == Type.NODE && getAuthenticatedIdentity(INodeIdentity.class) != null) {
            ret = new KlabUser(certificate.getProperty(ICertificate.KEY_NODENAME),
                    getAuthenticatedIdentity(INodeIdentity.class));
            registerIdentity(ret);
            return ret;
        }

        String authenticationServer = null;

        /**
         * TODO try new hub @ https://integratedmodelling.org/hub/api/auth-cert/engine"
         * 
         * if experimental property set in properties
         */
        // try local hub, let fail if not active
        if (client.ping(LOCAL_HUB_URL)) {
            authenticationServer = LOCAL_HUB_URL;
            Logging.INSTANCE.info("local hub is available: trying local authentication");
        } else {
            authenticationServer = certificate.getProperty(KlabCertificate.KEY_PARTNER_HUB);
        }

        if (authenticationServer != null) {

            Logging.INSTANCE.info("authenticating " + certificate.getProperty(KlabCertificate.KEY_USERNAME)
                    + " with hub " + authenticationServer);

            /*
             * Authenticate with server(s). If authentication fails because of a 403, invalidate the
             * certificate. If no server can be reached, certificate is valid but engine is offline.
             */
            EngineAuthenticationRequest request = new EngineAuthenticationRequest(
                    certificate.getProperty(KlabCertificate.KEY_USERNAME),
                    certificate.getProperty(KlabCertificate.KEY_SIGNATURE),
                    certificate.getProperty(KlabCertificate.KEY_CERTIFICATE_TYPE),
                    certificate.getProperty(KlabCertificate.KEY_CERTIFICATE), certificate.getLevel());

            // add email if we have it, so the hub can notify in any case if so configured
            request.setEmail(certificate.getProperty(KlabCertificate.KEY_USERNAME));

            try {
                authentication = client.authenticateEngine(authenticationServer, request);
            } catch (Throwable e) {
                Logging.INSTANCE.error("authentication failed for user "
                        + certificate.getProperty(KlabCertificate.KEY_USERNAME) + ": " + e.getMessage());
                // TODO inspect exception; fatal if 403, warn and proceed offline otherwise
            }
        }

        if (authentication != null) {

            DateTime expiry = null;
            /*
             * check expiration
             */
            try {
                expiry = DateTime.parse(authentication.getUserData().getExpiry());
            } catch (Throwable e) {
                Logging.INSTANCE
                        .error("bad date or wrong date format in certificate. Please use latest version of software.");
                return null;
            }
            if (expiry == null) {
                Logging.INSTANCE
                        .error("certificate has no expiration date. Please obtain a new certificate.");
                return null;
            } else if (expiry.isBeforeNow()) {
                Logging.INSTANCE
                        .error("certificate expired on " + expiry + ". Please obtain a new certificate.");
                return null;
            }
        }

        /*
         * build the identity
         */
        if (certificate.getType() == Type.ENGINE) {

            // if we have connected, insert network session identity
            if (authentication != null) {

                HubReference hubNode = authentication.getHub();
                Hub hub = new Hub(hubNode);
                hub.setOnline(true);
                NetworkSession networkSession = new NetworkSession(authentication.getUserData().getToken(),
                        hub);

                ret = new KlabUser(authentication.getUserData(), networkSession);

                Network.INSTANCE.buildNetwork(authentication);

                Logging.INSTANCE
                        .info("User " + ((IUserIdentity) ret).getUsername() + " logged in through hub "
                                + hubNode.getId() + " owned by " + hubNode.getPartner().getId());

                Logging.INSTANCE.info("The following nodes are available:");
                for (INodeIdentity n : Network.INSTANCE.getNodes()) {
                    Duration uptime = new Duration(n.getUptime());
                    DateTime boottime = DateTime.now(DateTimeZone.UTC).minus(uptime.toPeriod());
                    IPartnerIdentity partner = n.getParentIdentity();
                    Logging.INSTANCE.info("   " + n.getName() + " online since " + boottime);
                    Logging.INSTANCE
                            .info("      " + partner.getName() + " (" + partner.getEmailAddress() + ")");
                    Logging.INSTANCE
                            .info("      " + "online " + PeriodFormat.getDefault().print(uptime.toPeriod()));
                }

            } else {

                // offline node with no partner
                Node node = new Node(certificate.getProperty(KlabCertificate.KEY_NODENAME), null);
                ((Node) node).setOnline(false);
                ret = new KlabUser(certificate.getProperty(KlabCertificate.KEY_USERNAME), node);

                Logging.INSTANCE
                        .info("User " + ((IUserIdentity) ret).getUsername() + " activated in offline mode");
            }

            ((KlabUser) ret).setOnline(authentication != null);

        } else {
            throw new KlabAuthorizationException(
                    "wrong certificate for an engine: cannot create identity of type "
                            + certificate.getType());
        }

        if (ret != null) {
            registerIdentity(ret);
        }

        return ret;
    }

    /**
     * Return a new credential provider that knows the credentials saved into the k.LAB database and
     * will log appropriate messages when credentials aren't found.
     * 
     * @return
     */
    public CredentialsProvider getCredentialProvider() {

        return new CredentialsProvider(){

            @Override
            public void clear() {
            }

            @Override
            public Credentials getCredentials(AuthScope arg0) {

                String auth = arg0.getHost() + (arg0.getPort() == 80 ? "" : (":" + arg0.getPort()));

                ExternalAuthenticationCredentials credentials = externalCredentials.get(auth);

                if (credentials == null) {
                    throw new KlabMissingCredentialsException(auth);
                }

                return new UsernamePasswordCredentials(credentials.getCredentials().get(0),
                        credentials.getCredentials().get(1));
            }

            @Override
            public void setCredentials(AuthScope arg0, org.apache.http.auth.Credentials arg1) {
                // TODO Auto-generated method stub

            }
        };
    }

    public List<ObservableReference> getDefaultObservables(IIdentity identity) {
        List<ObservableReference> ret = new ArrayList<>();
        IUserIdentity user = identity.getParentIdentity(IUserIdentity.class);
        if (user != null) {
            for (Group group : user.getGroups()) {
                ret.addAll(group.getObservables());
            }
        }
        // TODO extract from user's groups, not defaults!
        // if (defaultGroups != null) {
        // for (String groupId : defaultGroups.keySet()) {
        // for (ObservableReference observable : defaultGroups.get(groupId).getObservables()) {
        // ret.add(observable);
        // }
        // }
        // }
        return ret;
    }

    @Override
    public Collection<ISession> getSessions() {
        List<ISession> ret = new ArrayList<>();
        for (IIdentity identity : identities.values()) {
            if (identity instanceof ISession) {
                ret.add((ISession) identity);
            }
        }
        return ret;
    }

    public void addExternalCredentials(String host, ExternalAuthenticationCredentials credentials) {
        externalCredentials.put(host, credentials);
        externalCredentials.write();
    }

}
