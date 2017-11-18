package gait4dogs.backend.controller;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gait4dogs.backend.data.Dog;
import gait4dogs.backend.data.DogAnalytics;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.*;


@RestController
public class DogController {

    private final AtomicLong counter = new AtomicLong();

    @RequestMapping(value="/dog/add", method= RequestMethod.POST, produces = "application/json", consumes = "application/json")
    @ResponseBody
    public Dog addDog(HttpEntity<String> httpEntity) throws IOException {
        String json = httpEntity.getBody();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode dogObj = mapper.readTree(json);
        JsonNode nameNode = dogObj.get("name");
        String name = nameNode.textValue();
        int height = dogObj.get("height").intValue();
        int weight = dogObj.get("weight").intValue();
        String breed = dogObj.get("breed").textValue();
        String birthDate = dogObj.get("birthDate").textValue();
        return new Dog(name, height, weight, breed, birthDate, counter.incrementAndGet());
    }

    @RequestMapping("/dog/get")
    public Dog getDog(@RequestParam(value="name", defaultValue = "Spot") String name) {
        return new Dog("Spot", 5, 80, "German Shepherd", "October 8th, 2017", counter.incrementAndGet());
    }



    @RequestMapping("/dog/getAll")
    public Dog[] getAll() {
        Dog[] dogs = new Dog[2];
        for (int i = 0; i < dogs.length; i++) {
            dogs[i] = getDog("Dog " + i);
        }
        return dogs;
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