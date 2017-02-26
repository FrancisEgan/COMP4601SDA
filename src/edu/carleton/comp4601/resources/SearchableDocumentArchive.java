package edu.carleton.comp4601.resources;

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

import edu.carleton.comp4601.crawler.MultiController;
import edu.carleton.comp4601.crawler.MultiCrawler;
import edu.carleton.comp4601.crawler.PageStorage;
import edu.carleton.comp4601.crawler.Vertex;
import edu.carleton.comp4601.dao.Document;
import edu.carleton.comp4601.dao.DocumentCollection;
import edu.carleton.comp4601.dao.SDADocumentAccess;
import edu.uci.ics.crawler4j.crawler.CrawlController;

@Path("/")
public class SearchableDocumentArchive {
	// Allows to insert contextual objects into the class,
	// e.g. ServletContext, Request, Response, UriInfo
	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	
	MultiController webCrawler;
	
	String storageFolder = System.getProperty("user.dir") + "/pagestore/";
	ArrayList<CrawlController> controllers = new ArrayList<CrawlController>();
	
	public void startCrawlers() throws Exception{
		for(int i = 0; i<1; i++){
			CrawlController curr = MultiController.buildController(storageFolder + "crawler" + i, "http://www.cnn.com/");
			controllers.add(curr);
	        // 1 thread per each seed
	        curr.start(MultiCrawler.class, 1);
		}
	}

	public SearchableDocumentArchive() throws Exception {
		SDADocumentAccess.getInstance();
		
        String rootFolder = "/Users/thomasmurphy/Documents/Coding/Eclipse Projects/COMP4601-SDA2/pagestore/";
        String storageFolder = rootFolder + "test";
        
        startCrawlers();
    	System.out.println(PageStorage.getInstance().getGraph().toString());
    	addGraphToMongo();
	}
	
	public void addGraphToMongo(){	
		org.bson.Document dbEntry = new org.bson.Document();
		dbEntry.append("id", "graph");
		dbEntry.append("value", PageStorage.getInstance().getGraph());
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
		
		boolean created = SDADocumentAccess.getInstance().putSDADoc(new Document(p));
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

		DocumentCollection docs = SDADocumentAccess.getInstance().search(_tags);
		String output="<html><body><h1>Results</h1><ol>";
		if (docs.getDocuments().size() != 0) {
			for(Document doc : docs.getDocuments()) {
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
	public DocumentCollection searchXML (@PathParam("tags") String _tags) {
		DocumentCollection docs = SDADocumentAccess.getInstance().search(_tags);
		return docs;
	}
	
	@GET
	@Path("documents")
	@Produces({MediaType.TEXT_HTML})
	public String viewDocsHTML() {
		DocumentCollection docs = new DocumentCollection();
		List<Document> docList = new ArrayList<Document>();
		docList.addAll(SDADocumentAccess.getInstance().getSDAMap().values());
		docs.setDocuments(docList);
		
		String output="<html><body><h1>Documents</h1><ol>";
		if (docs.getDocuments().size() != 0) {
			for(Document doc : SDADocumentAccess.getInstance().getSDAMap().values()) {
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
	public DocumentCollection viewDocsXML() {
		DocumentCollection docs = new DocumentCollection();
		List<Document> docList = new ArrayList<Document>();
		docList.addAll(SDADocumentAccess.getInstance().getSDAMap().values());
		docs.setDocuments(docList);

		return docs;
	}

	@Path("{docId}")
	public SDADocumentEndpoints getDocument(@PathParam("docId") String id) {
		return new SDADocumentEndpoints(uriInfo, request, id);
	}
}
