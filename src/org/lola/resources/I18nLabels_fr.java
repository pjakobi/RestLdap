package org.lola.resources;

import java.util.ListResourceBundle;

public class I18nLabels_fr extends ListResourceBundle {
	public Object[][] getContents() { return contents; }
	static final Object[][] contents = {
		{"invalid_scope", "Scope LDAP invalide : %s"},
		{"Dsml", "Création objet Dsml"},
		{"srchReq", "Search Req - DN : %s - Id : %s - scope : %s"},
		{"LdapServ", "Création objet LdapServ {0}:{1} - {2}"},
		{"LdapSrch", "LDAP search %s"},
		{"LdapSrchResult", "Résultat LDAP search %s"},
		{"Locale", "Locale : %s"},
		{"DoGet", "DoGet %s"},
		{"DN", "LDAP Distinguished Name %s"},
		{"ErrNotImpl", "Pas implémenté"},
		{"Attr", "Attribut : %s - valeur : %s"},
		{"NamingContexts", "Contextes LDAP : %s" },
		{"SubTree", "Exploration ss-arbre %s (%s)" },
		{"ErrCfgHost", "Recherche hostname en conf. web.xml : {0}:{1} - {2}" },
		{"ErrUrlNotFound", "Erreur Interne :  LDAP URL non trouvée dans le contexte" },
		{"ErrDnNotFound", "Erreur Interne :  LDAP Bind DN non trouvée dans le contexte" },
		{"BindOK","Connexion à l'annuaire effectuée"},
	}; // contents
		
}
