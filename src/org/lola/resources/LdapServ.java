package org.lola.resources;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.ServerException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.InvalidNameException;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapName;
import javax.servlet.ServletConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapName;

public class LdapServ  {
	private DirContext ctx = null;
	private Logger logger;
	private ResourceBundle labels;
	private Hashtable<String,String> env;
	
	// Constructor
	// -----------
	public LdapServ(ServletConfig config) throws NamingException, MalformedURLException {
		
		logger = LoggerFactory.getLogger(LdapServ.class);
		String srv,strPort;
		labels = ResourceBundle.getBundle("org.lola.resources.I18nLabels", Locale.getDefault());
		
		// Retrieve configuration data
		
		srv = config.getInitParameter("org.lola.resources.server"); // may be absent
		if ((srv == null) || (srv == "")) srv = "localhost";
		  
		strPort = config.getInitParameter("org.lola.resources.port"); // may be absent
		if ((strPort == null) || (strPort == "")) strPort = "389";
		
		env = new Hashtable<String,String>();
		int port;
		try { port = Integer.parseInt(strPort); } catch(NumberFormatException nfe) { port = 389; } // auto-correct wrong entries
		env.put(Context.PROVIDER_URL, "ldap://" + srv + ":" + port);
		env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.REFERRAL, "follow");
		
		// Authentication//
		LdapName dn = new LdapName(config.getInitParameter("org.lola.resources.dname"));
		env.put(Context.SECURITY_AUTHENTICATION,"simple");
		env.put(Context.SECURITY_PRINCIPAL,dn.toString()); // specify the username
		env.put(Context.SECURITY_CREDENTIALS,config.getInitParameter("org.lola.resources.pwd"));   
		
        MessageFormat fmt = new MessageFormat(labels.getString("LdapServ"));
        Object[] args = { srv, strPort, dn.toString() };
        logger.info(fmt.format(args));
        
        ctx = new InitialDirContext(env);
        logger.debug(String.format(labels.getString("BindOK")));
        
	} // LdapServ
	
	public String getBindDn() {
		String result = env.get(Context.SECURITY_PRINCIPAL);
		return result;
	}
	
	public String getHost() { 
		String providerUrl = env.get(Context.PROVIDER_URL);  // should be ldap://hostname:port[/stuff]
		
		String hostPort;
		String hostPortStuff;
		String[] parts;
		if (providerUrl.startsWith("ldap://")) {
			parts = providerUrl.split("ldap://");  // parts[0] : hostname:port[/stuff]
			hostPortStuff = parts[1];
		} else { hostPortStuff = providerUrl; } // no ldap:// prefix
		
		if (hostPortStuff.contains("/")) { // hostname:port/stuff
				parts = hostPortStuff.split("/");
				hostPort = parts[1];
		} else { hostPort = hostPortStuff; } // hostname:port
				
		
		parts = hostPort.split(":");
		return parts[0];
	} // getHost

	public int getPort() { 
		String[] parts;
		String providerUrl = env.get(Context.PROVIDER_URL);  // should be ldap://hostname:port[/stuff]
		String portStuff;
		String strPort;
		
		parts = providerUrl.split(":");
		if (providerUrl.startsWith("ldap://")) { portStuff = parts[2]; } // ldap://host:port[/stuff]
		else { portStuff = parts[1]; } // host:port[/stuff]
		
		if (portStuff.contains("/")) { // port/stuff
			parts = portStuff.split("/");
			strPort = parts[0];
		} else { strPort = portStuff; }
		
		Integer result = Integer.parseInt(strPort);
		return result;
	}
	
	
	// Return the LDAP Bind Distinguished Name
	public LdapName getLdapName() throws NamingException {
			Hashtable<?,?> env = ctx.getEnvironment();	
			if(env.containsKey(Context.SECURITY_PRINCIPAL)) {
				return(new LdapName((String)env.get(Context.SECURITY_PRINCIPAL)));
		    } // if 	
			// We should not get there as we put the Bind DN in the context just before
			throw new InvalidNameException(labels.getString("ErrDnNotFound"));
	}

	
	
	public DirContext getCtx() { return ctx; }
	 
	// Search root (Naming contexts)
	// -----------------------------
	public SearchData search() throws NamingException {
		SearchData result = new SearchData();
		// Do the search
		SearchControls srchCtrl = new SearchControls();
		srchCtrl.setSearchScope(SearchControls.OBJECT_SCOPE);
		srchCtrl.setReturningAttributes(new String[] { "namingContexts" });
		NamingEnumeration<SearchResult> dirNE = null;
		dirNE = ctx.search("", "objectclass=*", srchCtrl);
		
		// inspect search result
		while (dirNE.hasMore()) { // Loop on directory entries
	              SearchResult srchResult = (SearchResult) dirNE.next();
	              logger.debug(String.format(labels.getString("DN"), srchResult.getName())); // print DN of entry
	              Attributes attrs = srchResult.getAttributes();
	              
	              // Loop on attributes
	              NamingEnumeration<? extends Attribute> e = attrs.getAll();
	              while (e.hasMore()) {
	            	    Attribute attr = (Attribute) e.next();
	            	    logger.debug(String.format(labels.getString("Attr"), attr.getID(),attr.get().toString()));
	            	    result.add(attr.get().toString());
	              } // while 
	              
		} // while
		return result;
	} // search naming contexts
	
	// Search subtree
	// --------------
	public SearchData search(String Dname)  throws NamingException {
		SearchData result = new SearchData();
		SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        // ctx.createSubcontext( rootContext );
        logger.debug(String.format(labels.getString("LdapSrch"), Dname));
        NamingEnumeration list = ctx.list(Dname);

        while (list.hasMore()) {
            NameClassPair nc = (NameClassPair)list.next();
            logger.debug(String.format(labels.getString("LdapSrchResult"), nc.getName()));
            result.add(nc.getName());
        }
		return result;
	} // search subtree
}
