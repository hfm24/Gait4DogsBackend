package gait4dogs.backend.controller;

import java.util.concurrent.atomic.AtomicLong;

import gait4dogs.backend.data.Dog;
import gait4dogs.backend.data.DogAnalytics;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DogController {

    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/dog/add")
    public Dog addDog() {
        return new Dog("Spot", 5, 80, "German Shepherd", "October 8th, 2017", counter.incrementAndGet());
    }

    @RequestMapping("/dog/get")
    public Dog getDog(@RequestParam(value="name", defaultValue = "Spot") String name){
        return new Dog("Spot", 5, 80, "German Shepherd", "October 8th, 2017", counter.incrementAndGet());
    }

    @RequestMapping("/dogAnalytics/add")
    public DogAnalytics addDogAnalytics(){
        return new DogAnalytics(counter.incrementAndGet());
    }

    @RequestMapping("/dogAnalytics/get")
    public DogAnalytics getDogAnalytics(@RequestParam(value="id", defaultValue = "0") long id){
        return new DogAnalytics(id);
    }
}