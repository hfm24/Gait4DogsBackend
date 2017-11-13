package gait4dogs.backend.controller;

import gait4dogs.backend.data.Session;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SessionController {

    @RequestMapping("/session/add")
    public Session addSession() {
        return new Session(0, 5, "data", "this is a test note");
    }

    @RequestMapping("/session/get")
    public Session getSession(@RequestParam(value="id", defaultValue = "0") String id){
        return new Session(0, 5, "data", "This is a test");
    }
}
