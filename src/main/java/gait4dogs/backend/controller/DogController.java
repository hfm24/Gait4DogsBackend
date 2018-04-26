package gait4dogs.backend.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import gait4dogs.backend.data.Dog;
import gait4dogs.backend.data.DogAnalytics;
import gait4dogs.backend.util.DBUtil;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.*;


@RestController
public class DogController {

    @Autowired
    MongoDatabase db;
    @Autowired
    DBUtil dbUtil;

    private final AtomicLong counter = new AtomicLong();

    @RequestMapping(value="/dog/add", method= RequestMethod.POST, produces = "application/json", consumes = "application/json")
    @ResponseBody
    public Dog addDog(HttpEntity<String> httpEntity) throws IOException {
        String json = httpEntity.getBody();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode dogObj = mapper.readTree(json);
        JsonNode nameNode = dogObj.get("name");
        String name = nameNode.textValue();
        int height = dogObj.get("height").intValue();
        int weight = dogObj.get("weight").intValue();
        String breed = dogObj.get("breed").textValue();
        String birthDate = dogObj.get("birthDate").textValue();


        MongoCollection<Document> dogs = db.getCollection("Dogs");
        // Get latest id
        Integer id = (Integer)dbUtil.getNextSequence("Dogs");
        Dog dog = new Dog(id.toString(), name, height, weight, breed, birthDate);
        dogs.insertOne(dog.toDocument());

        return dog;
    }

    @RequestMapping("/dog/get")
    public Dog getDog(@RequestParam(value="id", defaultValue = "0") String id) {
        MongoCollection<Document> dogs = db.getCollection("Dogs");
        BasicDBObject query = new BasicDBObject();
        query.put("id", id);
        Document doc = dogs.find(query).first();
        if (doc == null) {
            return null;
        }
        return Dog.toDog(doc);
    }

    @RequestMapping("/dog/delete")
    public int deleteDog(@RequestParam(value="id", defaultValue = "0") String id) {
        MongoCollection<Document> dogs = db.getCollection("Dogs");

        BasicDBObject query = new BasicDBObject();
        query.put("id", id);
        Document doc = dogs.find(query).first();
        if (doc == null) {
            return -1;
        }
        dogs.deleteOne(doc);
        return 1;
    }

    @RequestMapping("/dog/getAll")
    public Dog[] getAll() {
        MongoCollection<Document> dogs = db.getCollection("Dogs");
        List<Dog> dogList = new ArrayList<>();
        MongoCursor<Document> docs = dogs.find().iterator();
        while (docs.hasNext()) {
            dogList.add(Dog.toDog(docs.next()));
        }
        Dog[] dogArray = new Dog[dogList.size()];
        for (int i = 0; i < dogList.size(); i++) {
            dogArray[i] = dogList.get(i);
        }
        return dogArray;
    }

    @RequestMapping("/dogAnalytics/add")
    public DogAnalytics addDogAnalytics(){

        return new DogAnalytics(counter.incrementAndGet());
    }

    @RequestMapping("/dogAnalytics/get")
    public DogAnalytics getDogAnalytics(@RequestParam(value="id", defaultValue = "0") long id){
        return new DogAnalytics(id);
    }


}