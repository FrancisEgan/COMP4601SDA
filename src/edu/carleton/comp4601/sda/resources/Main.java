package edu.carleton.comp4601.sda.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;

import edu.carleton.comp4601.sda.dao.SDADocuments;
import edu.carleton.comp4601.sda.model.SDADocument;
import edu.carleton.comp4601.sda.model.SDADocumentCollection;

@Path("sda")
public class Main {
	// Allows to insert contextual objects into the class,
	// e.g. ServletContext, Request, Response, UriInfo
	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	private String name;

	public Main() {
		name = "COMP4601SDAlol";
		SDADocuments.getInstance();
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public void homePage(@Context HttpServletResponse servletResponse) throws IOException{
		servletResponse.sendRedirect("../create_document.html");
	}

	@POST
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response newDocument(@FormParam("name") String name,
								@FormParam("id") String id,
								@FormParam("score") String score,
								@FormParam("text") String text,
								@FormParam("links") String links,
								@FormParam("tags") String tags,
								@Context HttpServletResponse servletResponse) throws IOException {
		
		ArrayList<String> tagList = new ArrayList<String>();
		ArrayList<String> linkList = new ArrayList<String>();
		
		if(links != null)
			for(String link : links.split(",")) linkList.add(link);
		if(tags != null)
			for(String tag : tags.split(",")) tagList.add(tag);
		
		HashMap<Object, Object> p = new HashMap<Object, Object>();
		p.put("id", Integer.parseInt(id));
		p.put("name", name);
		if(score != null) p.put("score", Float.parseFloat(score));
		p.put("text", text);
		p.put("links", linkList);
		p.put("tags", tagList);
		
		boolean created = SDADocuments.getInstance().putSDADocument(new SDADocument(p));
		Response result;
		
		if(created){
			result = Response.created(uriInfo.getAbsolutePath()).build();
		} else {
			result = Response.ok().build();
		}
		
		servletResponse.sendRedirect("../create_document.html");
		return result;
	}
	
	@GET
	@Path("search/{tags}")
	@Produces({MediaType.TEXT_HTML})
	public String searchHTML (@PathParam("tags") String _tags) {
		SDADocumentCollection docs = SDADocuments.getInstance().search(_tags);
		String output="<html><body><h1>Results</h1><ol>";
		if (docs.getDocuments().size() != 0) {
			for(SDADocument doc : docs.getDocuments()) {
				output += "<li>"
						+ doc.getName()
						+ "<a href=\"../"+doc.getId()+"\">Goto</a>"
						+ "</li>";
			}
		} else {
			output += "No documents found.";
		}
		output += "</ol></body></html>";
		return output;
	}
	
	@GET
	@Path("search/{tags}")
	@Produces(MediaType.APPLICATION_XML)
	public SDADocumentCollection searchXML (@PathParam("tags") String _tags) {
		SDADocumentCollection docs = SDADocuments.getInstance().search(_tags);
		return docs;
	}
	
	@GET
	@Path("documents")
	@Produces({MediaType.TEXT_HTML})
	public String viewDocsHTML() {
		SDADocumentCollection docs = new SDADocumentCollection();
		List<SDADocument> docList = new ArrayList<SDADocument>();
		docList.addAll(SDADocuments.getInstance().getModel().values());
		docs.setDocuments(docList);
		
		String output="<html><body><h1>Documents</h1><ol>";
		if (docs.getDocuments().size() != 0) {
			for(SDADocument doc : SDADocuments.getInstance().getModel().values()) {
				output += "<li>"
						+ doc.getName()
						+ "<a href=\"../sda/"+doc.getId()+"\">Goto</a>"
						+ "</li>";
			}
		} else {
			output += "No documents found.";
		}
		output += "</ol></body></html>";
		return output;
	}
	
	@GET
	@Path("documents")
	@Produces(MediaType.APPLICATION_XML)
	public SDADocumentCollection viewDocsXML() {
		SDADocumentCollection docs = new SDADocumentCollection();
		List<SDADocument> docList = new ArrayList<SDADocument>();
		docList.addAll(SDADocuments.getInstance().getModel().values());
		docs.setDocuments(docList);

		return docs;
	}

	@Path("{docId}")
	public SDADocumentActions getDocument(@PathParam("docId") String id) {
		return new SDADocumentActions(uriInfo, request, id);
	}
}
