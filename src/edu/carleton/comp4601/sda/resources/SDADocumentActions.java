package edu.carleton.comp4601.sda.resources;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;

import edu.carleton.comp4601.sda.dao.SDADocuments;
import edu.carleton.comp4601.sda.model.SDADocument;

public class SDADocumentActions {
	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	String id;

	public SDADocumentActions(UriInfo uriInfo, Request request, String id) {
		this.uriInfo = uriInfo;
		this.request = request;
		this.id = id;
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public String getSDADocumentHTML() {
		SDADocument a = SDADocuments.getInstance().find(new Integer(id));
		if (a == null) {
			return "No document found with that id";
		}
		
		String output = "<html><body>"
					  + "<h1>"+ a.getName() + "</h1>"
					  + "<ul><li>Id: " + a.getId() + "</li>"
					  + "<li>Text: " + a.getText() + "</li>"
					  + "<li>Tags: " + a.getTags() + "</li>"
					  + "<li>Links: " + a.getLinks() + "</li></ul>"
					  + "</body></html>";
		
		return output;
	}
	
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public SDADocument getSDADocumentXML() {
		SDADocument a = SDADocuments.getInstance().find(new Integer(id));
		if (a == null) {
			throw new RuntimeException("No such SDADocument: " + id);
		}
		
		return a;
	}

	@PUT
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_XML)
	public Response updateDoc(JAXBElement<SDADocument> tagsAndLinks) {
		SDADocument target = SDADocuments.getInstance().find(Integer.parseInt(this.id));
		
		if(tagsAndLinks == null) return Response.notAcceptable(null).build();
		if(target == null) return Response.noContent().build();
	
		target.setLinks(tagsAndLinks.getValue().getLinks());
		target.setTags(tagsAndLinks.getValue().getTags());
		
		return putAndGetResponse(target);
	}

	@DELETE
	public Response deleteSDADocument() {
		if (!SDADocuments.getInstance().delete(new Integer(id)))
			return Response.noContent().build();
		
		return Response.ok().build();
	}

	private Response putAndGetResponse(SDADocument SDADocument) {
		Response res;
		if (SDADocuments.getInstance().getModel().containsKey(SDADocument.getId())) {
			//res = Response.noContent().build();
			res = Response.ok().build();
		} else {
			res = Response.created(uriInfo.getAbsolutePath()).build();
		}
		SDADocuments.getInstance().putSDADocument(SDADocument);
		return res;
	}
}
