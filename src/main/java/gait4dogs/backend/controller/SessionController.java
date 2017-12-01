package gait4dogs.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import gait4dogs.backend.data.Session;
import gait4dogs.backend.data.SessionAnalytics;
import gait4dogs.backend.data.SessionRawData;
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


        List<float[]> axisData = new ArrayList<float[]>();
        axisData.add(x);
        axisData.add(y);
        axisData.add(z);

        float[] minimums = new float[3];
        for(int j = 0; j < 3; j++) {
            float curr = 0;
            float near = axisData.get(j)[0];
            for (int i = 0; i < x.length; i++) {
                curr = axisData.get(j)[i] * axisData.get(j)[i];
                if (curr <= (near * near)) {
                    near = axisData.get(j)[i];
                }
            }
            minimums[j] = near;
        }


        SessionRawData rawData = new SessionRawData(epoc, timestamp, elapsed, x, y, z);
        Session session = new Session(counter.incrementAndGet(), dogId, rawData, notes);

        MongoCollection<Document> sessions = db.getCollection("Sessions");
        sessions.insertOne(session.toDocument());

        analysisUtil.doSessionAnalysis(rawData, session.getId());

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

        return new SessionAnalytics(counter.incrementAndGet());
    }

    @RequestMapping("/sessionAnalytics/get")
    public SessionAnalytics getSessionAnalytics(@RequestParam(value="id", defaultValue = "0") long id){
        return new SessionAnalytics(id);
    }

    private Session toSession(Document doc) {
        long id = doc.getLong("id");
        long dogId = doc.getLong("dogId");
        String notes = doc.getString("notes");

        Document data = (Document)doc.get("data");
        List<Long> epocList = (List<Long>)data.get("epoc");
        List<String> timeStampList = (List<String>)data.get("timestamp");
        List<Double> elapsedList = (List<Double>)data.get("elapsed");
        List<Double> xList = (List<Double>)data.get("x");
        List<Double> yList = (List<Double>)data.get("y");
        List<Double> zList = (List<Double>)data.get("z");

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
        SessionRawData rawData = new SessionRawData(epoc, timestamp, elapsed, x, y, z);
        return new Session(id, dogId, rawData, notes);
    }
}
