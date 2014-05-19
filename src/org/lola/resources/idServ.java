package org.lola.resources;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.ServerException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Context; 
import javax.ws.rs.core.Response; 
import javax.ws.rs.core.Response.Status;
import javax.naming.InvalidNameException;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.ldap.LdapName;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.QueryParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.DefaultValue;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Path("/idserv")
public class idServ extends Application {
    
    private static Logger logger = null;
    private ResourceBundle labels = null;
    private @Context ServletConfig config;

	  
	  // Logger, I18N, etc.
    private void setLogLabels() {
      if (logger == null) logger = LoggerFactory.getLogger(idServ.class);
  	  if (labels == null) labels = ResourceBundle.getBundle("org.lola.resources.I18nLabels", Locale.getDefault());
    } // setLogLabels
    
    
	  private void setEnv(String Operation, String label) throws InvalidNameException, NamingException {
		  setLogLabels();
		  logger.info(String.format(labels.getString(Operation), label));
		  logger.debug(String.format(labels.getString("Locale"), Locale.getDefault().toString())); 
	  } // setEnv

	  private void setEnv(String Operation, String label, String[] parms) throws InvalidNameException, NamingException {
		  setLogLabels();
		  logger.info(String.format(labels.getString(Operation), label, parms[0]));
		  logger.debug(String.format(labels.getString("Locale"), Locale.getDefault().toString())); 
	  } // setEnv
	  
	  // This method is called if HTML is requested
	  @GET
	  @Produces(MediaType.TEXT_HTML)
	  public String doGetHtml(
			  @DefaultValue("") @QueryParam("dname") String searchedDname
			  ) {
		String  errorString = "", output = ""; 
		searchedDname = "C=FR,dc=thalesgroup,dc=com"; // debug
		String head = "";
		String infoString = "";
		Set<String> searchResult;
		
		try {
			setEnv("DoGet","HTML");
			LdapServ service = new LdapServ(config);
			
			head = "<h1>" + "Hello LDAP - " + service.getHost() + ":" + service.getPort() + "(" + service.getBindDn() + ")</h1>";
			output = "<html> " + "<title>" + "Hello Directory" + "</title>" + "<body>";

			
			searchResult = new HashSet<String>();
			if ((searchedDname == "") || (searchedDname == null)) infoString = "<p> searchedDname:" + "empty" + "</p>";
			else infoString = "<p> searchedDname:" + searchedDname + "</p>";
			
			if ((searchedDname == "") || (searchedDname == null)) searchResult = service.search(); // Read root DSE
			else searchResult = service.search(searchedDname); // non Root DSE
			
		    Iterator<String> it = searchResult.iterator();
			while (it.hasNext()) output += "<p> DN:" + it.next() + "</p>";
				
		} 
		catch (InvalidNameException e) { errorString = "<p>Err: " + e.getClass() + " " + e.getLocalizedMessage() + "</p>"; }
		catch (NamingException e) { errorString = "<p>Err: " + e.getClass() + " " + e.getLocalizedMessage() + "</p>"; }
		catch (MalformedURLException e) { errorString = "<p>Err: " + e.getClass() + " " + e.getLocalizedMessage() + "</p>"; }
		
		
		// Debug
		output += infoString + "<p>" + errorString + "</p>";
        String page = head + output;
		page += "</body>" + "</html> ";
	    return page;
	  }	// doGet
	  
	  // This method is called if XML is requested for Naming contexts
	  @Path("/namingcontexts")
	  @GET
	  @Produces(MediaType.APPLICATION_XML)
	  public Response LookupXML() throws MalformedURLException {
		  try {
			  setEnv("DoGet","Lookup XML");
			  
			  SearchData srchResult = searchNamingContexts();
			  return Response.ok(srchResult.toXML("contexts","namingcontext"),"application/xml").build();
		  }
		  catch (NamingException e) { return intError(e); } // catch
	  } // LookupXML


	  // This method is called if JSON is requested for Naming contexts
	  @Path("/namingcontexts")
	  @GET
	  @Produces(MediaType.APPLICATION_JSON)
	  public Response LookupJson() throws MalformedURLException {	  
		  try {
			  setEnv("DoGet","Lookup JSON");
			  SearchData srchResult = searchNamingContexts();
              return Response.ok(srchResult.toJson("namingcontext"),"application/json").build(); 
		  } // try
		  catch (NamingException e) { return intError(e); } // catch
	  } // LookupJson
	  
	  
	  // Send HTTP 500 return code
	  private Response intError(Exception e) {
		  Status status = Response.Status.INTERNAL_SERVER_ERROR;
		  Response result = Response.status(status).entity(e.getLocalizedMessage()).type("text/plain").build();
		  return result;
	  } // intError
	  
	  // Do the actual search for naming contexts (LDAP stuff)
	  private SearchData searchNamingContexts() throws NamingException, MalformedURLException {  
		  LdapServ service = new LdapServ(config);
		  SearchData namingCtxts = service.search();
          Iterator<String> iterator = namingCtxts.iterator();
          while(iterator.hasNext()) logger.debug(String.format(labels.getString("NamingContexts"),iterator.next())); // log
          return namingCtxts; // HashSet
	  } // searchNamingContexts
	  
	  // Retrieve Tree structure (JSON)
	  @Path("/subtree/{dn}")
	  @GET
	  @Produces(MediaType.APPLICATION_JSON)
	  public Response SubTreeJson(@PathParam("dn") String dn) throws MalformedURLException {
		  SearchData srchResult;
		  try { 
			  setEnv("SubTree",dn, new String[] {"JSON"}); 
			  LdapServ service = new LdapServ(config);
			  srchResult = service.search(dn); // search below dn
			  Response result = Response.ok(srchResult.toJson("rdn"),"application/json").build();
			  return result;
		  } // try 
		  catch (NamingException e) { return intError(e); } // catch
		  
	  } // SubTreeJson
	  
	
	  
} // idServ
