package edu.carleton.comp4601.sda.dao;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;

import edu.carleton.comp4601.sda.model.SDADocument;
import edu.carleton.comp4601.sda.model.SDADocumentCollection;

public class SDADocuments {
	
	private MongoClient	mc;
	private MongoDatabase db;
	private MongoCollection<Document> coll;
	private ConcurrentHashMap<Integer, SDADocument> documents;
	private static SDADocuments instance;
	
	public MongoCollection<Document> getDocuments(){ 
		return coll; 
	}
	
	public ConcurrentHashMap<Integer, SDADocument> getdocuments() {
		return documents;
	}

	public void setdocuments(ConcurrentHashMap<Integer, SDADocument> documents) {
		this.documents = documents;
	}

	public static void setInstance(SDADocuments instance) {
		SDADocuments.instance = instance;
	}
	
	public int size() {
		return documents.size();
	}
	
	public static SDADocuments getInstance() {
		if (instance == null)
			instance = new SDADocuments();
		return instance;
	}
	
	public SDADocuments() {
		documents = new ConcurrentHashMap<Integer, SDADocument>();
		documents.put(1, new SDADocument(1));
		documents.put(2, new SDADocument(2));
		
		try {
			mc = new MongoClient("localhost");
		} catch(Exception e){
			e.printStackTrace();
		}	
		
		db = mc.getDatabase("sda");
		coll = db.getCollection("documents");
		
		// loading data from mongo if it exists
		FindIterable<Document> iterable = coll.find();
		iterable.forEach(new Block<Document>() {
		    @Override
		    public void apply(final Document document) {
		    	HashMap<Object, Object> p = new HashMap<Object, Object>();
		    	
		    	for(String key : document.keySet()){
		    		p.put(key, document.get(key));
		    	}
		    	
		    	SDADocument addition = new SDADocument(p);
		        documents.put(document.getInteger("id"), addition);
		    }
		});
		
		for(Integer id : documents.keySet()){
			if(inMongo(id)) continue; // if the document is in Mongo, don't add it
			
			Document doc = createMongoDoc(new SDADocument(id));
			coll.insertOne(doc);
		}
	}
	
	private Document createMongoDoc(SDADocument addition){
		Document dbEntry = new  Document();
		dbEntry.append("id", addition.getId());
		
		if(addition.getName() != null)	dbEntry.append("name", addition.getName());
		if(addition.getScore() != null)	dbEntry.append("score", addition.getScore());
		if(addition.getText() != null)	dbEntry.append("text", addition.getText());
		if(addition.getLinks() != null)	dbEntry.append("links", addition.getLinks());
		if(addition.getTags() != null)	dbEntry.append("tags", addition.getTags());
		
		return dbEntry;
	}

	public SDADocument find(int id) {
		return documents.get(new Integer(id));
	}
	
	public boolean delete(int id) {
		if (find(id) != null) {
			Integer no = new Integer(id);
			
			BasicDBObject query = new BasicDBObject("id", no);
			DeleteResult result	= coll.deleteOne(query);
			
			if(result.wasAcknowledged()){
				documents.remove(no);
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	public ConcurrentHashMap<Integer, SDADocument> getModel(){
		return this.documents;
	}
	
	private Document getMongoDoc(Integer id){
		MongoCursor<Document> docsCursor = coll.find().iterator();
		Document currDoc;
		
		while(docsCursor.hasNext()){
			currDoc = docsCursor.next();
			
			if(id == (Integer) currDoc.get("id"))	return currDoc;
		}
		
		return null;
	}

	private boolean inMongo(Integer id){
		MongoCursor<Document> docsCursor = coll.find().iterator();
		Document currDoc;
		
		while(docsCursor.hasNext()){
			currDoc = docsCursor.next();
			
			if(id == (Integer) currDoc.get("id"))	return true;
		}
		
		return false;
	}
	
	public SDADocumentCollection search(String _tags) {
		String[] tags = _tags.split(":");
		
		List<SDADocument> list = new ArrayList<SDADocument>();
		for(SDADocument sdaDocument : documents.values()) {
			boolean hastags = true;
			for(String tag : tags) {
				if (!sdaDocument.getTags().contains(tag)) {
					hastags = false;
					break;
				}
			}
			if (hastags)
				list.add(sdaDocument);
		}
		
		SDADocumentCollection sdadc = new SDADocumentCollection();
		sdadc.setDocuments(list);
		return sdadc;
	}
	
	// returns true if a document was created, false if one was updated
	public boolean putSDADocument(SDADocument sdaDoc){
		// updating the collection
		documents.put(sdaDoc.getId(), sdaDoc);
		
		// updating mongo
		if(inMongo(sdaDoc.getId())){
			Document update = createMongoDoc(sdaDoc);
			Document target = coll.find(Filters.eq("id", sdaDoc.getId())).first();
			
			for(String updateKey : update.keySet()){
				target.append(updateKey, update.get(updateKey));
			}
			
			Document updateContainer = new Document();
			updateContainer.append("$set", target);
			
			coll.findOneAndUpdate(Filters.eq("id", sdaDoc.getId()), updateContainer);
			return false;
			
		} else {
			Document acct = createMongoDoc(sdaDoc);
			coll.insertOne(acct);
			return true;
		}
	}
}
