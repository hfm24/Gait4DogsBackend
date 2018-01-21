package gait4dogs.backend.controller;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import gait4dogs.backend.data.Dog;
import gait4dogs.backend.data.DogAnalytics;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.*;


@RestController
public class DogController {

    @Autowired
    MongoDatabase db;

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
        BasicDBObject query = new BasicDBObject();
        query.put("_id", -1);
        Document lastDoc = dogs.find().sort(query).limit(1).first();
        ObjectId id = lastDoc.getObjectId("_id");
        Dog dog = new Dog(id.toString(), name, height, weight, breed, birthDate);
        dogs.insertOne(dog.toDocument());

        return dog;
    }

    @RequestMapping("/dog/get")
    public Dog getDog(@RequestParam(value="name", defaultValue = "Spot") String name) {
        return new Dog("0",name, 5, 80, "German Shepherd", "October 8th, 2017");
    }

    @RequestMapping("/dog/getAll")
    public Dog[] getAll() {
        Dog[] dogs = new Dog[2];
        for (int i = 0; i < dogs.length; i++) {
            dogs[i] = getDog("Dog " + i);
        }
        return dogs;
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