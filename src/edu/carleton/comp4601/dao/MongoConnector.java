package edu.carleton.comp4601.dao;

import com.mongodb.*;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.*;
import org.bson.Document;
import org.bson.types.ObjectId;

public class MongoConnector {

	private static MongoConnector instance;
	
	public MongoClient mongoClient;
	public MongoDatabase database = mongoClient.getDatabase("sda");
	
	public static MongoConnector getInstance() {
		if (instance == null)
			instance = new MongoConnector();
		return instance;
	}
	
	public MongoConnector() {
		mongoClient = new MongoClient();
		database = mongoClient.getDatabase("sda");
	}
	
	public MongoCollection<Document> getCollection(String col) {
		return database.getCollection(col);
	}
}
