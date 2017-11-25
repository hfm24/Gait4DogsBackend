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
    public Session addSession(HttpEntity<String> httpEntity) throws IOException {
        String json = httpEntity.getBody();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode sessionObj = mapper.readTree(json);
        Long dogId = sessionObj.get("dogId").longValue();
        String notes = sessionObj.get("notes").textValue();
        JsonNode dataObj = sessionObj.get("data");

        JsonNode epocArr = dataObj.get("epoc");
        long[] epoc = new long[epocArr.size()];
        for (int i = 0; i < epocArr.size(); i++) {
            epoc[i] = epocArr.get(i).longValue();
        }

        JsonNode timestampArr = dataObj.get("timestamp");
        String[] timestamp = new String[timestampArr.size()];
        for (int i = 0; i < timestampArr.size(); i++) {
            timestamp[i] = timestampArr.get(i).textValue();
        }

        JsonNode elapsedArr = dataObj.get("elapsed");
        float[] elapsed = new float[elapsedArr.size()];
        for (int i = 0; i < elapsedArr.size(); i++) {
            elapsed[i] = elapsedArr.get(i).floatValue();
        }

        JsonNode xArr = dataObj.get("x");
        float[] x = new float[xArr.size()];
        for (int i = 0; i < xArr.size(); i++) {
            x[i] = xArr.get(i).floatValue();
        }

        JsonNode yArr = dataObj.get("y");
        float[] y = new float[yArr.size()];
        for (int i = 0; i < yArr.size(); i++) {
            y[i] = yArr.get(i).floatValue();
        }

        JsonNode zArr = dataObj.get("z");
        float[] z = new float[zArr.size()];
        for (int i = 0; i < zArr.size(); i++) {
            z[i] = zArr.get(i).floatValue();
        }

        SessionRawData rawData = new SessionRawData(epoc, timestamp, elapsed, x, y, z);
        Session session = new Session(counter.incrementAndGet(), dogId, rawData, notes);

        MongoCollection<Document> sessions = BackendApplication.db.getCollection("Sessions");
        sessions.insertOne(session.toDocument());

        return session;
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
