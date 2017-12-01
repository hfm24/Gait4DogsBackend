package gait4dogs.backend.util;

import gait4dogs.backend.data.SessionAnalytics;
import gait4dogs.backend.data.SessionRawData;

public class AnalysisUtil {
    public AnalysisUtil() {

    }

    public SessionAnalytics doSessionAnalysis(SessionRawData rawData, long sessionId) {

        return new SessionAnalytics(sessionId);
    }
}
