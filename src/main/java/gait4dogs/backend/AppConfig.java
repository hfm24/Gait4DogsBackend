package gait4dogs.backend;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import gait4dogs.backend.util.AnalysisUtil;
import gait4dogs.backend.util.DBUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    /*
   * Use the standard Mongo driver API to create a com.mongodb.MongoClient instance.
   */
    public @Bean
    MongoDatabase mongoClient() {
        MongoClientURI uri  = new MongoClientURI("mongodb://dbuser:dbpassword@ds111476.mlab.com:11476/heroku_tq9bs98x");
        MongoClient client = new MongoClient(uri);
        MongoDatabase db = client.getDatabase(uri.getDatabase());
        return db;
    }

    public @Bean
    AnalysisUtil analysisUtil() {
        return new AnalysisUtil();
    }

    public @Bean
    DBUtil dbUtil() {
        return new DBUtil();
    }
}
