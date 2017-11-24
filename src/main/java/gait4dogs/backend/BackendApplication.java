package gait4dogs.backend;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
public class BackendApplication {

	public static MongoDatabase db;

	public static void main(String[] args) {

		MongoClientURI uri  = new MongoClientURI("mongodb://dbuser:dbpassword@ds111476.mlab.com:11476/heroku_tq9bs98x");
		MongoClient client = new MongoClient(uri);
		db = client.getDatabase(uri.getDatabase());
		//List<Document> seedData = new ArrayList<>();
		//seedData.add(new Document("name","spot"));
		//MongoCollection<Document> dogs = db.getCollection("Dogs");
		//dogs.insertMany(seedData);
		SpringApplication.run(BackendApplication.class, args);
	}
}
