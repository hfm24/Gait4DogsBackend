package gait4dogs.backend.controller;

import gait4dogs.backend.data.Session;
import gait4dogs.backend.data.SessionAnalytics;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicLong;

@RestController
public class SessionController {

    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/session/add")
    public Session addSession() {
        return new Session(0, 5, "data", "this is a test note");
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
