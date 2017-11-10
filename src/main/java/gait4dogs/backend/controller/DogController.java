package gait4dogs.backend.controller;

import java.util.concurrent.atomic.AtomicLong;

import gait4dogs.backend.Dog;
import gait4dogs.backend.Greeting;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DogController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/dog/add")
    public Dog addDog() {
        return new Dog("Spot", 5, 80, "German Shepherd", "October 8th, 2017");
    }

    @RequestMapping("/dog/get")
    public Dog getDog(@RequestParam(value="name", defaultValue = "Spot") String name){
        return new Dog("Spot", 5, 80, "German Shepherd", "October 8th, 2017");
    }
}