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
import java.util.concurrent.atomic.AtomicLong;

import static com.mongodb.client.model.Filters.eq;

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
        List<AccelerometerOutput> accelerometerOutputs = new ArrayList<>();
        JsonNode accelOutputs = sessionObj.get("data");
        // Get acceleration data
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

            JsonNode xAxisArr = dataObj.get("xAxis");
            float[] xAxis = new float[xAxisArr.size()];
            for (int i = 0; i < xAxisArr.size(); i++) {
                xAxis[i] = xAxisArr.get(i).floatValue();
            }

            JsonNode yAxisArr = dataObj.get("yAxis");
            float[] yAxis = new float[yAxisArr.size()];
            for (int i = 0; i < yAxisArr.size(); i++) {
                yAxis[i] = yAxisArr.get(i).floatValue();
            }

            JsonNode zAxisArr = dataObj.get("zAxis");
            float[] zAxis = new float[zAxisArr.size()];
            for (int i = 0; i < zAxisArr.size(); i++) {
                zAxis[i] = zAxisArr.get(i).floatValue();
            }


            AccelerometerOutput accelerometerOutput = new AccelerometerOutput(epoc, timestamp, elapsed, x, y, z, xAxis, yAxis, zAxis);
            accelerometerOutputs.add(accelerometerOutput);
        }

        SessionRawData rawData = new SessionRawData(accelerometerOutputs);
        SessionAnalytics sessionAnalytics = analysisUtil.doSessionAnalysis(rawData);
        MongoCollection<Document> sessions = db.getCollection("Sessions");
        // Get latest id
        Integer id = (Integer) dbUtil.getNextSequence("Sessions");
        Session session = new Session(id.toString(), dogId, rawData, sessionAnalytics, notes);


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
        return toSession(doc);
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

    private Session toSession(Document doc) {
        String id = doc.getString("id");
        String dogId = doc.getString("dogId");
        String notes = doc.getString("notes");

        Document data = (Document)doc.get("rawData");
        SessionRawData rawData = toSessionRawData(data);

        Document sesssionAnalytics = (Document)doc.get("sessionAnalytics");
        SessionAnalytics sessionAnalytics = toSessionAnalytics(sesssionAnalytics);

        return new Session(id, dogId, rawData, sessionAnalytics, notes);
    }

    private AccelerometerOutput toAccelerometerOutput(Document doc) {
        List<Long> epocList = (List<Long>)doc.get("epoc");
        List<String> timeStampList = (List<String>)doc.get("timestamp");
        List<Double> elapsedList = (List<Double>)doc.get("elapsed");
        List<Double> xList = (List<Double>)doc.get("x");
        List<Double> yList = (List<Double>)doc.get("y");
        List<Double> zList = (List<Double>)doc.get("z");
        List<Double> xAxisList = (List<Double>)doc.get("xAxis");
        List<Double> yAxisList = (List<Double>)doc.get("yAxis");
        List<Double> zAxisList = (List<Double>)doc.get("zAxis");

        long[] epoc = new long[epocList.size()];
        String[] timestamp = new String[timeStampList.size()];
        float[] elapsed = new float[elapsedList.size()];
        float[] x = new float[xList.size()];
        float[] y = new float[yList.size()];
        float[] z = new float[zList.size()];
        float[] xAxis = new float[xAxisList.size()];
        float[] yAxis = new float[yAxisList.size()];
        float[] zAxis = new float[zAxisList.size()];

        for (int i = 0; i < epoc.length; i++) {
            epoc[i] = epocList.get(i);
            timestamp[i] = timeStampList.get(i);
            elapsed[i] = elapsedList.get(i).floatValue();
            x[i] = xList.get(i).floatValue();
            y[i] = yList.get(i).floatValue();
            z[i] = zList.get(i).floatValue();
            xAxis[i] = xAxisList.get(i).floatValue();
            yAxis[i] = yAxisList.get(i).floatValue();
            zAxis[i] = zAxisList.get(i).floatValue();
        }
        return new AccelerometerOutput(epoc, timestamp, elapsed, x, y, z, xAxis, yAxis, zAxis);
    }

    private SessionRawData toSessionRawData(Document doc) {
        List<AccelerometerOutput> accelerometerOutputs = new ArrayList<>();
        ArrayList<Document> accelerometerOutputDocs = (ArrayList<Document>)doc.get("accelerometerOutputs");
        for (Document accelerometerOutputDoc : accelerometerOutputDocs) {
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
        List<Angle> angles = (List<Angle>)doc.get("angles");
        return new AccelerometerOutputAnalytics(minimums, maximums, ranges, minMagnitude, maxMagnitude, rangeMagnitude, angles);
    }

    private SessionAnalytics toSessionAnalytics(Document doc) {
        List<AccelerometerOutputAnalytics> accelerometerOutputAnalytics = new ArrayList<>();
        ArrayList<Document> analytics = (ArrayList<Document>)doc.get("accelerometerOutputAnalytics");
        for (Document accelOutputAnalyticsDoc : analytics) {
            accelerometerOutputAnalytics.add(toAccelerometerOuptutAnalytics(accelOutputAnalyticsDoc));
        }
        return new SessionAnalytics(accelerometerOutputAnalytics);
    }


}
