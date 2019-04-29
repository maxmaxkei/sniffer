package com.wifi;

import java.util.Map;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.ArrayList;
import java.text.SimpleDateFormat;

import org.bson.Document;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.MongoCollection;

public class MongoService {

    private MongoClient mongoClient = new MongoClient("localhost", 27017);
    private MongoDatabase dataBase = mongoClient.getDatabase("macDB");
    private MongoCollection<Document> collection = dataBase.getCollection("macAdressCollection");

    public void extractHistory (Map<String, Device> listOfDevices) {
        MongoCollection<Document> historyCollection = dataBase.getCollection("history");
        try(MongoCursor<Document> historyCursor = historyCollection.find().iterator()) {
            while (historyCursor.hasNext()) {
                Document currentHistoryDeviseDocument = historyCursor.next();
                List list = new ArrayList(currentHistoryDeviseDocument.values());
                listOfDevices.put(
                    (String) list.get(1), new Device((String) list.get(1),
                                                     (Long) list.get(2),
                                                     (Long) list.get(3)));
            }
        }
        historyCollection.drop();
    }

    public void closeConnect () {
        mongoClient.close();
    }

    public void putInToDB(Document device) {
            collection.insertOne(device);
    }

    public void createHistory (Map <String, Device> devices) {
        dataBase.createCollection("history");
        MongoCollection<Document> historyCollection = dataBase.getCollection("history");
        for (Map.Entry<String, Device> device : devices.entrySet()) {
            historyCollection.insertOne(device.getValue().createDocument());
        }
    }

}
