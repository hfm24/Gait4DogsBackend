package gait4dogs.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import gait4dogs.backend.data.*;
import gait4dogs.backend.util.AnalysisUtil;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static com.mongodb.client.model.Filters.eq;

@RestController
public class SessionController {

    @Autowired
    private MongoDatabase db;
    @Autowired
    private AnalysisUtil analysisUtil;

    private final AtomicLong counter = new AtomicLong();
    private final AtomicLong analyticsCounter = new AtomicLong();

    @RequestMapping(value="/session/add", method= RequestMethod.POST, produces = "application/json", consumes = "application/json")
    @ResponseBody
    public Session addSession(HttpEntity<String> httpEntity) throws IOException {
        String json = httpEntity.getBody();
        ObjectMapper mapper = new ObjectMapper();


        JsonNode sessionObj = mapper.readTree(json);
        Long dogId = sessionObj.get("dogId").longValue();
        String notes = sessionObj.get("notes").textValue();
        List<AccelerometerOutput> accelerometerOutputs = new ArrayList<>();
        JsonNode accelOutputs = sessionObj.get("data");
        for (JsonNode dataObj : accelOutputs) {
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
            AccelerometerOutput accelerometerOutput = new AccelerometerOutput(epoc, timestamp, elapsed, x, y, z);
            accelerometerOutputs.add(accelerometerOutput);
        }

        SessionRawData rawData = new SessionRawData(accelerometerOutputs);
        SessionAnalytics sessionAnalytics = analysisUtil.doSessionAnalysis(rawData);
        Session session = new Session(counter.incrementAndGet(), dogId, rawData, sessionAnalytics, notes);

        MongoCollection<Document> sessions = db.getCollection("Sessions");
        sessions.insertOne(session.toDocument());

        return session;
    }

    @RequestMapping(value="/session/get", method= RequestMethod.GET)
    public Session getSession(@RequestParam(value="id", defaultValue = "0") long id){
        MongoCollection<Document> sessions = db.getCollection("Sessions");
        Document doc = sessions.find(eq("id", id)).first();
        if (doc == null) {
            return null;
        }
        return toSession(doc);
    }

    @RequestMapping("/sessionAnalytics/add")
    public SessionAnalytics addSessionAnalytics(){

        return null;
    }

    @RequestMapping("/sessionAnalytics/get")
    public SessionAnalytics getSessionAnalytics(@RequestParam(value="id", defaultValue = "0") long id){
        MongoCollection<Document> sessionAnalytics = db.getCollection("SessionAnalytics");
        Document doc = sessionAnalytics.find(eq("id", id)).first();
        if (doc == null) {
            return null;
        }
        return toSessionAnalytics(doc);
    }

    private Session toSession(Document doc) {
        long id = doc.getLong("id");
        long dogId = doc.getLong("dogId");
        String notes = doc.getString("notes");

        Document data = (Document)doc.get("rawData");
        SessionRawData rawData = toSessionRawData(data);

        Document analytics = (Document)doc.get("analytics");
        SessionAnalytics sessionAnalytics = toSessionAnalytics(analytics);

        return new Session(id, dogId, rawData, sessionAnalytics, notes);
    }

    private AccelerometerOutput toAccelerometerOutput(Document doc) {
        List<Long> epocList = (List<Long>)doc.get("epoc");
        List<String> timeStampList = (List<String>)doc.get("timestamp");
        List<Double> elapsedList = (List<Double>)doc.get("elapsed");
        List<Double> xList = (List<Double>)doc.get("x");
        List<Double> yList = (List<Double>)doc.get("y");
        List<Double> zList = (List<Double>)doc.get("z");

        long[] epoc = new long[epocList.size()];
        String[] timestamp = new String[timeStampList.size()];
        float[] elapsed = new float[elapsedList.size()];
        float[] x = new float[xList.size()];
        float[] y = new float[yList.size()];
        float[] z = new float[zList.size()];

        for (int i = 0; i < epoc.length; i++) {
            epoc[i] = epocList.get(i);
            timestamp[i] = timeStampList.get(i);
            elapsed[i] = elapsedList.get(i).floatValue();
            x[i] = xList.get(i).floatValue();
            y[i] = yList.get(i).floatValue();
            z[i] = zList.get(i).floatValue();
        }
        return new AccelerometerOutput(epoc, timestamp, elapsed, x, y, z);
    }

    private SessionRawData toSessionRawData(Document doc) {
        List<AccelerometerOutput> accelerometerOutputs = new ArrayList<>();
        for (Document accelerometerOutputDoc : (List<Document>)doc) {
            accelerometerOutputs.add(toAccelerometerOutput(accelerometerOutputDoc));
        }
        SessionRawData rawData = new SessionRawData(accelerometerOutputs);
        return rawData;
    }

    private AccelerometerOutputAnalytics toAccelerometerOuptutAnalytics(Document doc) {
        List<Double> minList = (List<Double>)doc.get("minimums");
        List<Double> maxList = (List<Double>)doc.get("maximums");
        List<Double> rangesList = (List<Double>)doc.get("ranges");
        float[] minimums = new float[minList.size()];
        float[] maximums = new float[maxList.size()];
        float[] ranges = new float[rangesList.size()];
        for (int i = 0; i < minimums.length; i++) {
            minimums[i] = minList.get(i).floatValue();
            maximums[i] = maxList.get(i).floatValue();
            ranges[i] = rangesList.get(i).floatValue();
        }
        float minMagnitude = doc.getDouble("minMagnitude").floatValue();
        float maxMagnitude = doc.getDouble("maxMagnitude").floatValue();
        float rangeMagnitude = doc.getDouble("rangeMagnitude").floatValue();
        return new AccelerometerOutputAnalytics(minimums, maximums, ranges, minMagnitude, maxMagnitude, rangeMagnitude);
    }

    private SessionAnalytics toSessionAnalytics(Document doc) {
        List<AccelerometerOutputAnalytics> accelerometerOutputAnalytics = new ArrayList<>();
        for (Document accelOutputAnalyticsDoc : (List<Document>)doc) {
            accelerometerOutputAnalytics.add(toAccelerometerOuptutAnalytics(accelOutputAnalyticsDoc));
        }
        return new SessionAnalytics(accelerometerOutputAnalytics);
    }
}
