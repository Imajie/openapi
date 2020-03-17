package org.imajie.api.common.api.impl;

import org.eclipse.jetty.security.AbstractLoginService;
import org.eclipse.jetty.util.security.Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Spliterators;

public class LdapLoginService extends AbstractLoginService {
    private static final Logger log = LoggerFactory.getLogger(LdapLoginService.class);

    private final String baseDn;
    private DirContext dirContext;
    private final Hashtable<String, String> env;
    private final Hashtable<String, String> userEnvBase;

    public LdapLoginService(final String ldapUrl, final String baseDn, final String userPrincipal, final String password) {
        this.baseDn = baseDn;

        final Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.PROVIDER_URL, ldapUrl);
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, userPrincipal);
        env.put(Context.SECURITY_CREDENTIALS, password);

        this.env = env;

        final Hashtable<String, String> userEnvBase = new Hashtable<>();
        userEnvBase.put(Context.PROVIDER_URL, ldapUrl);
        userEnvBase.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        userEnvBase.put(Context.SECURITY_AUTHENTICATION, "simple");

        this.userEnvBase = userEnvBase;
    }

    @Override
    protected void doStart() throws Exception {
        dirContext = new InitialDirContext(env);

        super.doStart();
    }

    @Override
    protected void doStop() throws Exception {
        dirContext.close();
        dirContext = null;

        super.doStop();
    }

    @Override
    protected String[] loadRoleInfo(final UserPrincipal user) {
        final SearchControls searchControls = new SearchControls();
        searchControls.setReturningAttributes(new String[]{"cn"});

        try {
            final NamingEnumeration<SearchResult> search =
                    dirContext.search(
                            "ou=groups," + baseDn,
                            "(member=uid=" + user.getName() + ",ou=users," + baseDn + ")", searchControls);

            List<String> roles = new ArrayList<>();
            while (search.hasMore()) {
                final SearchResult result = search.next();
                final String cn = result.getAttributes().get("cn").get().toString();
                roles.add(cn);
            }

            return roles.toArray(new String[0]);
        } catch (NamingException e) {
            log.error("Failed to lookup user groups: " + user, e);
        }
        return new String[]{""};
    }

    @Override
    protected UserPrincipal loadUserInfo(final String username) {
        return new UserPrincipal(username, new LdapCredential("uid=" + username + ",ou=users," + baseDn, new Hashtable<>(userEnvBase)));
    }

    public static class LdapCredential extends Credential {

        private final Hashtable<String, String> env;

        LdapCredential(final String username, final Hashtable<String, String> env) {
            this.env = env;
            this.env.put(Context.SECURITY_PRINCIPAL, username);
        }

        @Override
        public boolean check(final Object credentials) {
            this.env.put(Context.SECURITY_CREDENTIALS, credentials.toString());

            try {
                new InitialDirContext(this.env).close();
                return true;
            } catch (AuthenticationException e) {
                log.info("Invalid password for user: " + this.env.get(Context.SECURITY_PRINCIPAL));
            } catch (NamingException e) {
                log.error("Failed to query LDAP", e);
            }
            return false;
        }
    }
}
