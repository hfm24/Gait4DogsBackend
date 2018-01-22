package gait4dogs.backend.data;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SessionRawData {
    List<AccelerometerOutput> accelerometerOutputs;

    public SessionRawData(List<AccelerometerOutput> accelerometerOutputs) {
        this.accelerometerOutputs = accelerometerOutputs;
    }

    public List<AccelerometerOutput> getAccelerometerOutputs() {
        return accelerometerOutputs;
    }

    public Document toDocument() {
        List<Document> accelerometerOutputDocs = new ArrayList<>();
        for (AccelerometerOutput output : accelerometerOutputs) {
            accelerometerOutputDocs.add(output.toDocument());
        }

        Document doc = new Document("accelerometerOutputs", accelerometerOutputDocs);
        return doc;
    }

}
