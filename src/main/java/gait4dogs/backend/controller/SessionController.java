package gait4dogs.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import gait4dogs.backend.data.*;
import gait4dogs.backend.util.AnalysisUtil;
import gait4dogs.backend.util.DBUtil;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class SessionController {

    @Autowired
    private MongoDatabase db;
    @Autowired
    private DBUtil dbUtil;
    private static final Logger Logger = LoggerFactory.getLogger(SessionController.class);

    @RequestMapping(value="/session/add", method= RequestMethod.POST, produces = "application/json", consumes = "application/json")
    @ResponseBody
    public Session addSession(HttpEntity<String> httpEntity) throws IOException {
        String json = httpEntity.getBody();
        ObjectMapper mapper = new ObjectMapper();


        JsonNode sessionObj = mapper.readTree(json);
        Logger.debug(sessionObj.toString());
        System.out.print(sessionObj.toString());
        String dogId = getJsonNodeText(sessionObj, "dogId");
        String notes = getJsonNodeText(sessionObj, "notes");
        String date = getJsonNodeText(sessionObj, "date");
        String gaitType = getJsonNodeText(sessionObj, "gaitType");
        List<AccelerometerOutput> accelerometerOutputs = new ArrayList<>();
        JsonNode accelOutputs = sessionObj.get("data");
        // Get acceleration data
        for (JsonNode dataObj : accelOutputs) {

            //
            // Accelerometer output
            //
            JsonNode accelObj = dataObj.get("accelerometer");
            JsonNode epocArr = accelObj.get("epoc");
            double[] epoc = new double[epocArr.size()];
            for (int i = 0; i < epocArr.size(); i++) {
                epoc[i] = epocArr.get(i).doubleValue();
            }

            JsonNode timestampArr = accelObj.get("timestamp");
            String[] timestamp = new String[timestampArr.size()];
            for (int i = 0; i < timestampArr.size(); i++) {
                timestamp[i] = timestampArr.get(i).textValue();
            }

            JsonNode elapsedArr = accelObj.get("elapsed");
            double[] elapsed = new double[elapsedArr.size()];
            for (int i = 0; i < elapsedArr.size(); i++) {
                elapsed[i] = elapsedArr.get(i).doubleValue();
            }

            JsonNode xArr = accelObj.get("x");
            double[] x = new double[xArr.size()];
            for (int i = 0; i < xArr.size(); i++) {
                x[i] = xArr.get(i).doubleValue();
            }

            JsonNode yArr = accelObj.get("y");
            double[] y = new double[yArr.size()];
            for (int i = 0; i < yArr.size(); i++) {
                y[i] = yArr.get(i).doubleValue();
            }

            JsonNode zArr = accelObj.get("z");
            double[] z = new double[zArr.size()];
            for (int i = 0; i < zArr.size(); i++) {
                z[i] = zArr.get(i).doubleValue();
            }

            //
            // Gyroscope output
            //

            JsonNode gyroObj = dataObj.get("gyroscope");

            JsonNode gyroEpocArr = gyroObj.get("epoc");
            double[] gyroEpoc = new double[gyroEpocArr.size()];
            for (int i = 0; i < gyroEpocArr.size(); i++) {
                gyroEpoc[i] = gyroEpocArr.get(i).doubleValue();
            }

            JsonNode gyroTimestampArr = gyroObj.get("timestamp");
            String[] gyroTimestamp = new String[gyroTimestampArr.size()];
            for (int i = 0; i < gyroTimestampArr.size(); i++) {
                gyroTimestamp[i] = gyroTimestampArr.get(i).textValue();
            }

            JsonNode gyroElapsedArr = gyroObj.get("elapsed");
            double[] gyroElapsed = new double[gyroElapsedArr.size()];
            for (int i = 0; i < gyroElapsedArr.size(); i++) {
                gyroElapsed[i] = gyroElapsedArr.get(i).doubleValue();
            }

            JsonNode xAxisArr = gyroObj.get("xAxis");
            double[] xAxis = new double[xAxisArr.size()];
            for (int i = 0; i < xAxisArr.size(); i++) {
                xAxis[i] = xAxisArr.get(i).doubleValue();
            }

            JsonNode yAxisArr = gyroObj.get("yAxis");
            double[] yAxis = new double[yAxisArr.size()];
            for (int i = 0; i < yAxisArr.size(); i++) {
                yAxis[i] = yAxisArr.get(i).doubleValue();
            }

            JsonNode zAxisArr = gyroObj.get("zAxis");
            double[] zAxis = new double[zAxisArr.size()];
            for (int i = 0; i < zAxisArr.size(); i++) {
                zAxis[i] = zAxisArr.get(i).doubleValue();
            }

            String label = getJsonNodeText(dataObj, "label");
            AccelerometerOutput accelerometerOutput = new AccelerometerOutput(epoc, timestamp, elapsed, x, y, z,
                    gyroEpoc, gyroTimestamp, gyroElapsed, xAxis, yAxis, zAxis, label);
            accelerometerOutputs.add(accelerometerOutput);
        }

        SessionRawData rawData = new SessionRawData(accelerometerOutputs);
        SessionAnalytics sessionAnalytics = AnalysisUtil.doSessionAnalysis(rawData);
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

    @RequestMapping(value="session/getByDogId", method=RequestMethod.GET)
    public List<String> getSessionsByDogId(@RequestParam(value="dogId", defaultValue = "0") String dogId) {

        List<List<Double>> returnValues = new ArrayList<List<Double>>();
        List<Double> percentDiffs = new ArrayList<Double>();
        // Get list of session ids using dog id
        List<Session> l_Sessions = new ArrayList<>();
        Document currentDoc;
        Session currentSesh;
        String id;
        MongoCollection<Document> sessions = db.getCollection("Sessions");
        List<String> ids = new ArrayList<>();
        BasicDBObject query = new BasicDBObject();
        query.put("dogId", dogId);
        MongoCursor<Document> cursor = sessions.find(query).iterator();

        try {
            while (cursor.hasNext()) {
                currentDoc = cursor.next();
                id = currentDoc.getString("id");
                ids.add(id);
                //currentSesh = Session.toSession(currentDoc);
                //l_Sessions.add(currentSesh);
            }
        } finally {
            cursor.close();
        }

        //percentDiffs = AnalysisUtil.getAggregateDifference(l_Sessions);
        //returnValues.add(ids);
        //returnValues.add(percentDiffs);


        return ids;
    }

    /*public List<List<Double>> getSessionsByDogId(@RequestParam(value="dogId", defaultValue = "0") String dogId) {

        List<List<Double>> returnValues = new ArrayList<List<Double>>();
        List<Double> percentDiffs = new ArrayList<Double>();
        // Get list of session ids using dog id
        List<Session> l_Sessions = new ArrayList<>();
        Document currentDoc;
        Session currentSesh;
        Double id;
        MongoCollection<Document> sessions = db.getCollection("Sessions");
        ArrayList<Double> ids = new ArrayList<>();
        BasicDBObject query = new BasicDBObject();
        query.put("dogId", dogId);
        MongoCursor<Document> cursor = sessions.find(query).iterator();

        try {
            while (cursor.hasNext()) {
                currentDoc = cursor.next();
                id = Double.parseDouble(currentDoc.getString("id"));
                ids.add(id);
                currentSesh = Session.toSession(currentDoc);
                l_Sessions.add(currentSesh);
            }
        } finally {
            cursor.close();
        }

         percentDiffs = AnalysisUtil.getAggregateDifference(l_Sessions);
         returnValues.add(ids);
         returnValues.add(percentDiffs);


        return returnValues;
    }*/

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

    private String getJsonNodeText(JsonNode node, String key) {
        Object valueObj = node.get(key);
        String value = valueObj == null ? null : node.get(key).textValue();
        return value;
    }
}
