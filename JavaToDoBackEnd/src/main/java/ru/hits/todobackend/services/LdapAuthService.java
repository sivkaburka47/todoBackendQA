package ru.hits.todobackend.services;

import javax.naming.*;
import javax.naming.directory.*;
import java.util.Hashtable;

public class LdapAuthService {

    public boolean authenticate(String username, String password) {
        // УЯЗВИМОСТЬ: LDAP Injection через неконтролируемую строку фильтра
        String ldapUrl = "ldap://localhost:389";
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapUrl);
        DirContext ctx = null;

        try {
            ctx = new InitialDirContext(env);

            // ПОДСТАВЛЯЕМ В ИНТЕРПОЛИРОВАННУЮ СТРОКУ ПОЛЬЗОВАТЕЛЯ
            String filter = "(&(uid=" + username + ")(userPassword=" + password + "))";
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);

            NamingEnumeration<SearchResult> results = ctx.search("dc=example,dc=com", filter, controls);
            return results.hasMore();
        } catch (Exception e) {
            return false;
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
