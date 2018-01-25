package gait4dogs.backend.util;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;

public class DBUtil {
    @Autowired
    private MongoDatabase db;

    public Object getNextSequence(String name) {
        MongoCollection<Document> collection = db.getCollection("sequence");
        BasicDBObject find = new BasicDBObject();
        find.put("_id", name);
        BasicDBObject update = new BasicDBObject();
        update.put("$inc", new BasicDBObject("seq", 1));
        Document doc =  collection.findOneAndUpdate(find, update);
        return doc.get("seq");
    }
}
