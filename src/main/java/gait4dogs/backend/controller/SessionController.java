package gait4dogs.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gait4dogs.backend.data.Session;
import gait4dogs.backend.data.SessionAnalytics;
import gait4dogs.backend.data.SessionRawData;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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
        JsonNode data = sessionObj.get("data");
        String epocString = data.get("epoc").textValue();

        long[] epoc = mapper.readValue(epocString, long[].class);
        String[] timestamp;
        float[] elapsed;
        float[] x;
        float[] y;
        float[] z;

        return new Session(0, dogId, "data", notes);
    }

    @RequestMapping("/session/get")
    public Session getSession(@RequestParam(value="id", defaultValue = "0") String id){
        return new Session(0, 5, "data", "This is a test");
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
