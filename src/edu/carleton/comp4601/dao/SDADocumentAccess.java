package edu.carleton.comp4601.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

public class SDADocumentAccess {
	
	private MongoClient	mc;
	private MongoDatabase db;
	private MongoCollection<Document> coll;
	private static SDADocumentAccess instance;
	
	public static SDADocumentAccess getInstance() {
		if (instance == null)
			instance = new SDADocumentAccess();
		return instance;
	}

	public SDADocumentAccess() {
		
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
		    		if(key.equalsIgnoreCase("score")) p.put(key, Float.parseFloat(document.get(key).toString()));
		    		else p.put(key, document.get(key));
		    	}
		    	
		    	edu.carleton.comp4601.dao.Document addition = new edu.carleton.comp4601.dao.Document(p);
		        getSDAMap().put(document.getInteger("id"), addition);
		    }
		});
		
		for(Integer id : getSDAMap().keySet()){
			if(inMongo(id)) continue; // if the document is in Mongo, don't add it
			
			Document doc = createMongoDoc(new edu.carleton.comp4601.dao.Document(id));
			coll.insertOne(doc);
		}
	}
	
	// very impt
	public ConcurrentHashMap<Integer, edu.carleton.comp4601.dao.Document> getSDAMap(){ 
		return Documents.getInstance().getDocs(); 
	}
	
	public edu.carleton.comp4601.dao.Document getSDADoc(Integer id){
		return getSDAMap().get(id);
	}
	
	// returns true if a document was created, false if one was updated
	public boolean putSDADoc(edu.carleton.comp4601.dao.Document sdaDoc){
		// updating the collection
		getSDAMap().put(sdaDoc.getId(), sdaDoc);
		
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
	
	public boolean deleteSDADoc(int id) {
		if (getSDAMap().get(id) != null) {
			Integer no = new Integer(id);
			
			BasicDBObject query = new BasicDBObject("id", no);
			DeleteResult result	= coll.deleteOne(query);
			
			if(result.wasAcknowledged()){
				getSDAMap().remove(no);
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	private Document createMongoDoc(edu.carleton.comp4601.dao.Document addition){
		Document dbEntry = new  Document();
		dbEntry.append("id", addition.getId());
		
		if(addition.getName() != null)	dbEntry.append("name", addition.getName());
		if(addition.getScore() != null)	dbEntry.append("score", addition.getScore());
		if(addition.getText() != null)	dbEntry.append("text", addition.getText());
		if(addition.getLinks() != null)	dbEntry.append("links", addition.getLinks());
		if(addition.getTags() != null)	dbEntry.append("tags", addition.getTags());
		
		return dbEntry;
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
	
	public DocumentCollection search(String _tags) {
		String[] tags = _tags.split(":");
		
		List<edu.carleton.comp4601.dao.Document> list = new ArrayList<edu.carleton.comp4601.dao.Document>();
		for(edu.carleton.comp4601.dao.Document sdaDocument : getSDAMap().values()) {
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
		
		DocumentCollection sdadc = new DocumentCollection();
		sdadc.setDocuments(list);
		return sdadc;
	}
	
	public ArrayList<edu.carleton.comp4601.dao.Document> search(ArrayList<String> tags){
		return Documents.getInstance().searchForDocs(tags, true);
	}
	
	public ArrayList<edu.carleton.comp4601.dao.Document> search(ArrayList<String> tags, boolean sort){
		return Documents.getInstance().searchForDocs(tags, sort);
	}
}
