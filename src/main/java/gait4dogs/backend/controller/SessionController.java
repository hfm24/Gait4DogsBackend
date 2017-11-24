package gait4dogs.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mongodb.client.MongoCollection;
import gait4dogs.backend.BackendApplication;
import gait4dogs.backend.data.Session;
import gait4dogs.backend.data.SessionAnalytics;
import gait4dogs.backend.data.SessionRawData;
import org.bson.Document;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class SessionController {

    private final AtomicLong counter = new AtomicLong();

    @RequestMapping(value="/session/add", method= RequestMethod.POST, produces = "application/json", consumes = "application/json")
    @ResponseBody
    public Document addSession(HttpEntity<String> httpEntity) throws IOException {
        String json = httpEntity.getBody();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode sessionObj = mapper.readTree(json);
        Long dogId = sessionObj.get("dogId").longValue();
        String notes = sessionObj.get("notes").textValue();
        JsonNode dataObj = sessionObj.get("data");

        JsonNode epocArr = dataObj.get("epoc");
        List<Long> epoc = new ArrayList<>();
        for (int i = 0; i < epocArr.size(); i++) {
            epoc.add(epocArr.get(i).longValue());
        }

        JsonNode timestampArr = dataObj.get("timestamp");
        List<String> timestamp = new ArrayList<>();
        for (int i = 0; i < timestampArr.size(); i++) {
            timestamp.add(timestampArr.get(i).textValue());
        }

        JsonNode elapsedArr = dataObj.get("elapsed");
        List<Float> elapsed = new ArrayList<>();
        for (int i = 0; i < elapsedArr.size(); i++) {
            elapsed.add(elapsedArr.get(i).floatValue());
        }

        JsonNode xArr = dataObj.get("x");
        List<Float> x = new ArrayList<>();
        for (int i = 0; i < xArr.size(); i++) {
            x.add(xArr.get(i).floatValue());
        }

        JsonNode yArr = dataObj.get("y");
        List<Float> y = new ArrayList<>();
        for (int i = 0; i < yArr.size(); i++) {
            y.add(yArr.get(i).floatValue());
        }

        JsonNode zArr = dataObj.get("z");
        List<Float> z = new ArrayList<>();
        for (int i = 0; i < zArr.size(); i++) {
            z.add(zArr.get(i).floatValue());
        }

        Document rawDataDoc = new Document("epoc", epoc)
                .append("timestamp", timestamp)
                .append("elapsed", elapsed)
                .append("x", x)
                .append("y", y)
                .append("z", z);
        Document SessionDoc = new Document("id", counter.incrementAndGet())
                .append("dogId", dogId)
                .append("data", rawDataDoc)
                .append("notes", notes);
        MongoCollection<Document> dogs = BackendApplication.db.getCollection("Sessions");
        dogs.insertOne(SessionDoc);

        return SessionDoc;
    }

    @RequestMapping("/session/get")
    public Session getSession(@RequestParam(value="id", defaultValue = "0") String id){
        return new Session(0, 5, null, "This is a test");
    }

    @RequestMapping("/sessionAnalytics/add")
    public SessionAnalytics addSessionAnalytics(){
        return new SessionAnalytics(counter.incrementAndGet());
    }

    @RequestMapping("/sessionAnalytics/get")
    public SessionAnalytics getSessionAnalytics(@RequestParam(value="id", defaultValue = "0") long id){
        return new SessionAnalytics(id);
    }
}
