package org.lola.resources;

import java.util.HashSet;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Document;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;


public class SearchData extends HashSet<String> {
	
	static final long serialVersionUID = 1;
	
	public String toJson(String itemLabel) { 
		this.add("dc=Google Engineering,dc=com"); // Debug
		JSONArray array = new JSONArray();
		for (String s : this) {
        	JSONObject namingcontext = new JSONObject();
        	namingcontext.put(itemLabel, s);
        	array.add(namingcontext);
        } // for      
        return array.toString();
    } // toJson
	
	 public String toXML(String objectLabel, String itemLabel) {
		Document document = DocumentHelper.createDocument() ;
		Element root = document.addElement(objectLabel); 
		for (String s : this) {
        	Element current   = root.addElement(itemLabel);
        	current.addText(s);
        } // for
        return document.asXML(); 
	}
} // NamingContext
