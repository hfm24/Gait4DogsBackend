package gait4dogs.backend.controller;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.client.MongoCollection;
import com.mongodb.DBCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SessionControllerTest {
    @Mock
    private MongoDatabase db;
    @Mock
    private MongoCollection dbCollection;
    @InjectMocks
    private SessionController sessionController;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(db.getCollection("Sessions")).thenReturn(dbCollection);
    }

    @Test
    public void addSessionShouldAddASession() throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = "{\n" +
                "\"dogId\": \"0\",\n" +
                "\"data\": [{\n" +
                "\"accelerometer\": {\n" +
                "\"x\":[0,1,2],\n" +
                "\"y\":[0,1,2],\n" +
                "\"z\":[0,1,2],\n" +
                "\"epoc\": [0,1,2],\n" +
                "\"timestamp\":[\"timestamp_0\", \"timestamp_1\", \"timestamp_2\"],\n" +
                "\"elapsed\":[0,1,2]\n" +
                "},\n" +
                "\"gyroscope\": {\n" +
                "\"xAxis\":[0,1,2],\n" +
                "\"yAxis\":[0,1,2],\n" +
                "\"zAxis\":[0,1,2],\n" +
                "\"epoc\": [0,1,2],\n" +
                "\"timestamp\":[\"timestamp_0\", \"timestamp_1\", \"timestamp_2\"],\n" +
                "\"elapsed\":[0,1,2]\n" +
                "},\n" +
                "\"label\":\"label\"\n" +
                "},\n" +
                "{\n" +
                "\"accelerometer\": {\n" +
                "\"x\":[0,1,2],\n" +
                "\"y\":[0,1,2],\n" +
                "\"z\":[0,1,2],\n" +
                "\"epoc\": [0,1,2],\n" +
                "\"timestamp\":[\"timestamp_0\", \"timestamp_1\", \"timestamp_2\"],\n" +
                "\"elapsed\":[0,1,2]\n" +
                "},\n" +
                "\"gyroscope\": {\n" +
                "\"xAxis\":[0,1,2],\n" +
                "\"yAxis\":[0,1,2],\n" +
                "\"zAxis\":[0,1,2],\n" +
                "\"epoc\": [0,1,2],\n" +
                "\"timestamp\":[\"timestamp_0\", \"timestamp_1\", \"timestamp_2\"],\n" +
                "\"elapsed\":[0,1,2]\n" +
                "},\n" +
                "\"label\":\"label\"\n" +
                "}\n" +
                "],\n" +
                "\"notes\": \"sample_notes\"\n" +
                "}";
        HttpEntity<String> entity = new HttpEntity<String>(body, headers);
        sessionController.addSession(entity);

        //MyClass tester = new MyClass(); // MyClass is tested

        //// assert statements
        //assertEquals(0, tester.multiply(10, 0), "10 x 0 must be 0");
        //assertEquals(0, tester.multiply(0, 10), "0 x 10 must be 0");
        //assertEquals(0, tester.multiply(0, 0), "0 x 0 must be 0");
    }

    @Test
    public void getSessionShouldReturnASession() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>("Hello World", headers);
        sessionController.getSession("0");

        //MyClass tester = new MyClass(); // MyClass is tested

        //// assert statements
        //assertEquals(0, tester.multiply(10, 0), "10 x 0 must be 0");
        //assertEquals(0, tester.multiply(0, 10), "0 x 10 must be 0");
        //assertEquals(0, tester.multiply(0, 0), "0 x 0 must be 0");
    }
}