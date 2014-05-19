package org.lola.resources;

import java.util.ListResourceBundle;

public class I18nLabels_en extends ListResourceBundle {
	public Object[][] getContents() { return contents; }
	static final Object[][] contents = {
		{"invalid_scope", "LDAP scope invalid - %s"},
		{"Dsml", "Creating Dsml Object"},
		{"srchReq", "Search Req - DN : %s - Id : %s - scope : %s"},
		{"LdapServ", "Creating LdapServ Object {0}:{1} - {2}"},
		{"LdapSrch", "LDAP search %s"},
		{"Locale", "Locale : %s"},
		{"LdapSrchResult", "LDAP search result %s"},
		{"DoGet", "DoGet %s"},
		{"DN", "LDAP Distinguished Name %s"},
		{"ErrNotImpl", "Not implemented"},
		{"Attr", "Attribute : %s - val : %s"},
		{"NamingContexts", "LDAP Naming contexts : %s" },
		{"SubTree", "SubTree search %s (%s)" },
		{"ErrCfgHost", "Search hostname in web.xml :  {0}:{1} - {2}" },
		{"ErrUrlNotFound", "Internal Error :  LDAP URL not found in context" },
		{"ErrUrlNotFound", "Internal Error :  LDAP Bind DN not found in context" },
		{"BindOK","Directory bind OK"},
	}; // contents
		
}
