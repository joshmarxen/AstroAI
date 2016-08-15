package AstroAI.java;

import AstroAI.java.Brain;

public class SensorCell {
    
    private Brain myBrain;
    private double memAndPredictions[];
    
    public SensorCell(Brain b, int depth) {
        this.myBrain = b;
        this.memAndPredictions = new double[depth];
    }
    
    public void setSenseAt(int time, double value) {
        this.memAndPredictions[
            (this.myBrain.getCurrentPredictionIndex() + time) %
            this.memAndPredictions.length] = value;
    }
    
    public double getSenseAt(int time) {
        return this.memAndPredictions[
            (this.myBrain.getCurrentPredictionIndex() + time) %
            this.memAndPredictions.length];
    }
    
    public void setCurrentSense(double value) {
        this.memAndPredictions[
            this.myBrain.getCurrentPredictionIndex()] = value;
    }
    
    public double getCurrentSense() {
        return this.memAndPredictions[
            this.myBrain.getCurrentPredictionIndex()];
    }
    
}