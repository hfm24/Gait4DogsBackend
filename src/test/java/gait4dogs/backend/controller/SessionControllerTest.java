package gait4dogs.backend.controller;

import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.junit.Assert.*;

public class SessionControllerTest {
    @Test
    public void multiplicationOfZeroIntegersShouldReturnZero() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>("Hello World", headers);
        SessionController sessionController = new SessionController();

        //MyClass tester = new MyClass(); // MyClass is tested

        //// assert statements
        //assertEquals(0, tester.multiply(10, 0), "10 x 0 must be 0");
        //assertEquals(0, tester.multiply(0, 10), "0 x 10 must be 0");
        //assertEquals(0, tester.multiply(0, 0), "0 x 0 must be 0");
    }
}