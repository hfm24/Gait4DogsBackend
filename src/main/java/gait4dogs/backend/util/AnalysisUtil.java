package gait4dogs.backend.util;

import gait4dogs.backend.data.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.abs;

public class AnalysisUtil {

    private final double GYROSCOPE_SENSITIVITY = 100;

    public AnalysisUtil() {

    }

    public SessionAnalytics doSessionAnalysis(SessionRawData rawData) {
        List<AccelerometerOutputAnalytics> accelerometerOutputAnalytics = new ArrayList<>();
        for (AccelerometerOutput accelerometerOutput : rawData.getAccelerometerOutputs()) {
            accelerometerOutputAnalytics.add(doAccelerometerAnalytics(accelerometerOutput));
        }

        if (accelerometerOutputAnalytics.size() == 2) {
            List<List<double[]>> shiftedMagnitudes = getShiftedMagnitudes(accelerometerOutputAnalytics.get(0), accelerometerOutputAnalytics.get(1));
            List<Double> phaseShiftDifs = comparePhaseShift(shiftedMagnitudes.get(0), shiftedMagnitudes.get(1));
            return new SessionAnalytics(accelerometerOutputAnalytics, phaseShiftDifs.get(0), phaseShiftDifs.get(1));
        }
        else {
            return new SessionAnalytics(accelerometerOutputAnalytics);
        }
    }

    public AccelerometerOutputAnalytics doAccelerometerAnalytics(AccelerometerOutput accelerometerOutput) {
        List<double[]> axisData = new ArrayList<>();
        axisData.add(accelerometerOutput.getX());
        axisData.add(accelerometerOutput.getY());
        axisData.add(accelerometerOutput.getZ());

        double[] minimums = MathUtil.getMinimums(axisData);

        double[] maximums = MathUtil.getMaximums(axisData);



        double[] ranges = new double[3];
        ranges[0] = maximums[0] - minimums[0];
        ranges[1] = maximums[1] - minimums[1];
        ranges[2] = maximums[2] - minimums[2];



        //Finding the Magnitude
        /*double[] magnitudes = getMagnitudes(axisData);
        double minMagnitude = magnitudes[0];
        double maxMagnitude = magnitudes[1];
        double rangeMagnitude = magnitudes[2];*/
        int averagePeriod = 3;

        List<Angle> angles = MathUtil.getAngles(accelerometerOutput);

        List<double[]> smoothedAcc = new ArrayList<>(); //MathUtil.averageSmooth(accelerometerOutput.getX(), accelerometerOutput.getY(), accelerometerOutput.getZ(), accelerometerOutput.getEpoc(),averagePeriod);
        smoothedAcc.add(accelerometerOutput.getX());
        smoothedAcc.add(accelerometerOutput.getY());
        smoothedAcc.add(accelerometerOutput.getZ());
        smoothedAcc.add(accelerometerOutput.getEpoc());

        List<Double> footStrikeTimes = getFootStrikeTimes(smoothedAcc);

        String label = accelerometerOutput.getLabel();
        AccelerometerOutputAnalytics AOA = new AccelerometerOutputAnalytics(minimums, maximums, ranges, angles, footStrikeTimes, smoothedAcc, label);
        return AOA;
    }

    public List<Double> getFootStrikeTimes(List<double[]> axisData) {
        // Measuring mean distance between foot strikes
        List<Integer> footStrikes = getFootStrikes(axisData);
        List<Double> footStrikeTimes = new ArrayList<>();
        if (footStrikes.size() == 0) {
            return footStrikeTimes;
        }

        double sum = 0;
        int firstFootStrikeIdx = footStrikes.get(0);
        double lastEpoc = (double)axisData.get(3)[firstFootStrikeIdx];
        footStrikeTimes.add(lastEpoc);
        double currentEpoc;
        double dt;
        List<Double> dts = new ArrayList<>();
        System.out.println(lastEpoc);
        for (int i = 1; i < footStrikes.size(); i++) {
            int elapsedTIdx = footStrikes.get(i);
            currentEpoc = (double)axisData.get(3)[elapsedTIdx];
            footStrikeTimes.add(currentEpoc);
            System.out.println(currentEpoc);
            dt = currentEpoc - lastEpoc;
            dts.add(dt);
            lastEpoc = currentEpoc;
            sum += dt;
        }
        double avgDt = sum / dts.size();

        sum = 0;
        for (int i = 1; i < dts.size(); i++) {
            double sqrMean = (dts.get(i) - avgDt)*(dts.get(i) - avgDt);
            sum += sqrMean;
        }
        double dtVariance = 0;
        if (dts.size() > 1) {
            dtVariance = sum / (dts.size()-1);
        }

        System.out.println("average time between steps: " + avgDt);
        System.out.println("variance of time between steps: " + dtVariance);

        return footStrikeTimes;
    }

    public List<Integer> getFootStrikes(List<double[]> accData) {

        double[] x = accData.get(0);
        double[] y = accData.get(1);
        double[] z = accData.get(2);

        double epsilon = 0.25f;
        List<Integer> peakStridePoints = new ArrayList<>();
        double currentMagnitude;
        double lastMagnitude;
        double nextMagnitude;
        for (int i = 1; i < x.length-1; i++) {
            lastMagnitude = MathUtil.magnitude(x[i-1], y[i-1], z[i-1]);
            currentMagnitude = MathUtil.magnitude(x[i], y[i], z[i]);
            nextMagnitude = MathUtil.magnitude(x[i+1], y[i+1], z[i+1]);
            if (lastMagnitude > currentMagnitude && nextMagnitude-currentMagnitude > epsilon ) {
                peakStridePoints.add(i);
            }
        }
        return peakStridePoints;
    }





    private List<Double> comparePhaseShift(List<double[]> a, List<double[]> b) {
        List<Double> phaseShiftDifs = new ArrayList<>();
        double[] magnitudesControl = a.get(0);
        double[] timesControl = a.get(1);

        double[] magnitudesCompare = b.get(0);
        double[] timesCompare = b.get(0);

        // Get the difference between each datapoint and add all differences to find the area between both curves
        double totalDif = 0;
        for (int i = 0; i < magnitudesControl.length; i++) {
            double dif;
            dif = MathUtil.getMagnitudeDifAtClosestTime(magnitudesControl[i], timesControl[i], magnitudesCompare, timesCompare);
            totalDif += dif;
        }
        double avgDif = totalDif / magnitudesControl.length;

        phaseShiftDifs.add(totalDif);
        phaseShiftDifs.add(avgDif);

        System.out.println("Total difference in phase-shifted curves: " + totalDif);
        System.out.println("Average difference in phase-shifted curves: " + avgDif);
        // If the area is small, good.
        // If the area is large, bad.

        return phaseShiftDifs;
    }

    private List<List<double[]>> getShiftedMagnitudes(AccelerometerOutputAnalytics a, AccelerometerOutputAnalytics b) {
        // If accel. 1 has a higher range, use it as the control. Else use accel. 2
        AccelerometerOutputAnalytics control;
        AccelerometerOutputAnalytics compare;
        double[] aMagnitudes = MathUtil.getMagnitudes(a.getSmoothedAcc());
        double[] bMagnitudes = MathUtil.getMagnitudes(b.getSmoothedAcc());
        double aRange = MathUtil.maximum(aMagnitudes) - MathUtil.minimum(aMagnitudes);
        double bRange = MathUtil.maximum(bMagnitudes) - MathUtil.minimum(bMagnitudes);

        if (aRange > bRange) {
            control = a;
            compare = b;
        } else {
            control = b;
            compare = a;
        }

        // Take 1/2 the period (mean dt between steps) of the control.
        // If the control made the first step, add 1/2 period to each datapoint
        // else subtract 1/2 period from each datapoint
        double[] xA = control.getSmoothedAcc().get(0);
        double[] yA = control.getSmoothedAcc().get(1);
        double[] zA = control.getSmoothedAcc().get(2);
        double[] magnitudesControl = new double[xA.length];
        double[] timesControl = new double[xA.length];
        List<Double> shiftedFootStrikesControl = new ArrayList<>();
        double period = footStrikePeriod(control.getFootStrikeTimes());
        if (control.getFootStrikeTimes().size() == 0 || control.getFootStrikeTimes().get(0) < compare.getFootStrikeTimes().get(0)) {
            for (int i = 0; i < xA.length; i++) {
                double magnitude = Math.sqrt(xA[i]*xA[i] + yA[i]*yA[i] + zA[i]*zA[i]);
                double t = control.getSmoothedAcc().get(3)[i];
                t += period/2;
                magnitudesControl[i] = magnitude;
                timesControl[i] = t;
            }
            for (int i = 0; i < control.getFootStrikeTimes().size(); i++) {
                shiftedFootStrikesControl.add(control.getFootStrikeTimes().get(i) + Math.round(period/2));
            }
        } else {
            for (int i = 0; i < xA.length; i++) {
                double magnitude = Math.sqrt(xA[i]*xA[i] + yA[i]*yA[i] + zA[i]*zA[i]);
                double t = control.getSmoothedAcc().get(3)[i];
                t -= period/2;
                magnitudesControl[i] = magnitude;
                timesControl[i] = t;
            }
            for (int i = 0; i < control.getFootStrikeTimes().size(); i++) {
                shiftedFootStrikesControl.add(control.getFootStrikeTimes().get(i) - Math.round(period/2));
            }
        }

        double[] xB = compare.getSmoothedAcc().get(0);
        double[] yB = compare.getSmoothedAcc().get(1);
        double[] zB = compare.getSmoothedAcc().get(2);
        double[] timesCompare = compare.getSmoothedAcc().get(3);
        double[] magnitudesCompare = new double[xB.length];
        for (int i = 0; i < magnitudesCompare.length; i++) {
            double magnitude = Math.sqrt(xB[i]*xB[i]+yB[i]*yB[i]+zB[i]*zB[i]);
            magnitudesCompare[i] = magnitude;
        }

        List<double[]> shiftedMagnitudesControl = new ArrayList<>();
        List<double[]> shiftedMagnitudesCompare = new ArrayList<>();

        shiftedMagnitudesControl.add(magnitudesControl);
        shiftedMagnitudesControl.add(timesControl);
        shiftedMagnitudesCompare.add(magnitudesCompare);
        shiftedMagnitudesCompare.add(timesCompare);
        control.setShiftedMagnitudes(shiftedMagnitudesControl);
        control.setShiftedFootStrikeTimes(shiftedFootStrikesControl);
        compare.setShiftedMagnitudes(shiftedMagnitudesCompare);
        compare.setShiftedFootStrikeTimes(compare.getFootStrikeTimes());

        List<List<double[]>> shiftedMagnitudes = new ArrayList<>();
        shiftedMagnitudes.add(shiftedMagnitudesControl);
        shiftedMagnitudes.add(shiftedMagnitudesCompare);
        return shiftedMagnitudes;
    }

    private double footStrikePeriod(List<Double> footStrikeTimes) {
        if (footStrikeTimes.size() == 0) {
            return 0;
        }
        double sum = 0;
        double lastEpoc = (double)footStrikeTimes.get(0);
        double currentEpoc;
        double dt;
        List<Double> dts = new ArrayList<>();
        for (int i = 1; i < footStrikeTimes.size(); i++) {
            currentEpoc = footStrikeTimes.get(i);
            dt = currentEpoc - lastEpoc;
            dts.add(dt);
            lastEpoc = currentEpoc;
            sum += dt;
        }
        double avgDt = sum / dts.size();

        return avgDt;
    }

    private List<Double> getAggregateDifference(Session[] sessions){
        List<Double> difs = new ArrayList<>();

        // Get each session
        for (int i=1; i<sessions.length; i++){
            Session currentSession = sessions[i];
            SessionAnalytics currentAnalytics = currentSession.getSessionAnalytics();
            List<AccelerometerOutputAnalytics> acc = currentAnalytics.getAccelerometerOutputAnalytics();
            // Get the smoothed data for each accelerometer in the session
            AccelerometerOutputAnalytics a = acc.get(0);
            AccelerometerOutputAnalytics b = acc.get(1);
            // Get the maximum magnitude for each accelerometer
            double[] mags1 = MathUtil.getMagnitudes(a.getSmoothedAcc());
            double max1 = MathUtil.maximum(mags1);
            double[] mags2 = MathUtil.getMagnitudes(b.getSmoothedAcc());
            double max2 = MathUtil.maximum(mags2);
            double percentDiff;
            // Get a percent difference of the maximums
            if(max1 > max2) {
                percentDiff = (1 - (max2/max1))*100;
            }
            else{
                 percentDiff = (1 - (max1/max2))*100;
            }
            difs.add(percentDiff);
        }
        return difs;
    }

}