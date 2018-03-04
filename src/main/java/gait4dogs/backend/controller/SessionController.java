package gait4dogs.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import gait4dogs.backend.data.*;
import gait4dogs.backend.util.AnalysisUtil;
import gait4dogs.backend.util.DBUtil;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class SessionController {

    @Autowired
    private MongoDatabase db;
    @Autowired
    private DBUtil dbUtil;
    @Autowired
    private AnalysisUtil analysisUtil;

    @RequestMapping(value="/session/add", method= RequestMethod.POST, produces = "application/json", consumes = "application/json")
    @ResponseBody
    public Session addSession(HttpEntity<String> httpEntity) throws IOException {
        String json = httpEntity.getBody();
        ObjectMapper mapper = new ObjectMapper();


        JsonNode sessionObj = mapper.readTree(json);
        String dogId = sessionObj.get("dogId").textValue();
        String notes = sessionObj.get("notes").textValue();
        String date = sessionObj.get("date").textValue();
        String gaitType = sessionObj.get("gaitType").textValue();
        List<AccelerometerOutput> accelerometerOutputs = new ArrayList<>();
        JsonNode accelOutputs = sessionObj.get("data");
        // Get acceleration data
        for (JsonNode dataObj : accelOutputs) {
            JsonNode epocArr = dataObj.get("epoc");
            double[] epoc = new double[epocArr.size()];
            for (int i = 0; i < epocArr.size(); i++) {
                epoc[i] = epocArr.get(i).doubleValue();
            }

            JsonNode timestampArr = dataObj.get("timestamp");
            String[] timestamp = new String[timestampArr.size()];
            for (int i = 0; i < timestampArr.size(); i++) {
                timestamp[i] = timestampArr.get(i).textValue();
            }

            JsonNode elapsedArr = dataObj.get("elapsed");
            double[] elapsed = new double[elapsedArr.size()];
            for (int i = 0; i < elapsedArr.size(); i++) {
                elapsed[i] = elapsedArr.get(i).doubleValue();
            }

            JsonNode xArr = dataObj.get("x");
            double[] x = new double[xArr.size()];
            for (int i = 0; i < xArr.size(); i++) {
                x[i] = xArr.get(i).doubleValue();
            }

            JsonNode yArr = dataObj.get("y");
            double[] y = new double[yArr.size()];
            for (int i = 0; i < yArr.size(); i++) {
                y[i] = yArr.get(i).doubleValue();
            }

            JsonNode zArr = dataObj.get("z");
            double[] z = new double[zArr.size()];
            for (int i = 0; i < zArr.size(); i++) {
                z[i] = zArr.get(i).doubleValue();
            }

            JsonNode xAxisArr = dataObj.get("xAxis");
            double[] xAxis = new double[xAxisArr.size()];
            for (int i = 0; i < xAxisArr.size(); i++) {
                xAxis[i] = xAxisArr.get(i).doubleValue();
            }

            JsonNode yAxisArr = dataObj.get("yAxis");
            double[] yAxis = new double[yAxisArr.size()];
            for (int i = 0; i < yAxisArr.size(); i++) {
                yAxis[i] = yAxisArr.get(i).doubleValue();
            }

            JsonNode zAxisArr = dataObj.get("zAxis");
            double[] zAxis = new double[zAxisArr.size()];
            for (int i = 0; i < zAxisArr.size(); i++) {
                zAxis[i] = zAxisArr.get(i).doubleValue();
            }

            String label = dataObj.get("label").textValue();
            AccelerometerOutput accelerometerOutput = new AccelerometerOutput(epoc, timestamp, elapsed, x, y, z, xAxis, yAxis, zAxis, label);
            accelerometerOutputs.add(accelerometerOutput);
        }

        SessionRawData rawData = new SessionRawData(accelerometerOutputs);
        SessionAnalytics sessionAnalytics = analysisUtil.doSessionAnalysis(rawData);
        MongoCollection<Document> sessions = db.getCollection("Sessions");
        // Get latest id
        Integer id = (Integer) dbUtil.getNextSequence("Sessions");
        Session session = new Session(id.toString(), dogId, rawData, sessionAnalytics, notes, date, gaitType);


        sessions.insertOne(session.toDocument());

        return session;
    }

    @RequestMapping(value="/session/get", method= RequestMethod.GET)
    public Session getSession(@RequestParam(value="id", defaultValue = "0") String id){
        MongoCollection<Document> sessions = db.getCollection("Sessions");
        BasicDBObject query = new BasicDBObject();
        query.put("id", id);
        Document doc = sessions.find(query).first();
        if (doc == null) {
            return null;
        }
        return Session.toSession(doc);
    }

    @RequestMapping(value="session/getRawData", method=RequestMethod.GET)
    public SessionRawData getSessionRawData(@RequestParam(value="id", defaultValue = "0") String id) {
        Session session = getSession(id);
        if (session != null) {
            return session.getRawData();
        }

        return null;
    }

    @RequestMapping("/sessionAnalytics/add")
    public SessionAnalytics addSessionAnalytics(){

        return null;
    }

    @RequestMapping("/sessionAnalytics/get")
    public SessionAnalytics getSessionAnalytics(@RequestParam(value="id", defaultValue = "0") String id){
       Session session = getSession(id);
       if (session != null) {
           return session.getSessionAnalytics();
       }
       return null;
    }
}
