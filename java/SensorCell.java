package AstroAI.java;

import AstroAI.java.Brain;

public class SensorCell {
    
    private Brain myBrain;
    private double memAndPredictions[];
    private Feeler feeler;
    private double storedSenseVal;
    
    public SensorCell(Brain b, Feeler f) {
        this.myBrain = b;
        this.feeler = f;
        this.memAndPredictions = new double[Brain.sensorCellDepth];
        for(int i = 0; i < Brain.sensorCellDepth; ++i) {
            this.memAndPredictions[i] = 0.0;
        }
        this.storedSenseVal = 0.0;
    }
    
    /*
    public SensorCell(Brain b, double stateValImportance) {
        this.myBrain = b;
        this.stateValImportance = stateValImportance;
        this.memAndPredictions = new double[Brain.sensorCellDepth];
        for(int i = 0; i < Brain.sensorCellDepth; ++i) {
            this.memAndPredictions[i] = 0.0;
        }
    }
    */
    
    public void setSenseAtPredictionOffset(double value) {
        this.memAndPredictions[
            this.myBrain.getCurrentPredictionIndex()] = value;
    }
    
    public double getSenseAtPredictionOffset() {
        return this.memAndPredictions[
            this.myBrain.getCurrentPredictionIndex()];
    }
    
    public double getSenseAtOffsetFromPredictionOffset(int offset) {
        return this.memAndPredictions[
            this.myBrain.getCurrentPredictionIndex() + offset];
    }
    
    // TODO: incorporate past error uncertainty into this calculation
    // eventually
    public double getStateValContributionAtPredictionOffset() {
        try {
            return this.getSenseAtPredictionOffset()
                   * this.feeler.getStateValueImportance();
        } catch(NullPointerException npe) {
            throw new StateValueImportanceQueryException(
                "A sensor cell with a null feeler was queried for its state "
                + "value contribution. This means that this sensor cell is "
                + "an Action's SensorCell. This should not happen.");
        }
    }
    
    public void perturbSenseAtPredictionOffset(double p) {
        // store this in case we need to undo the perturbation
        this.storedSenseVal = this.getSenseAtPredictionOffset();
        this.memAndPredictions[
            this.myBrain.getCurrentPredictionIndex()] =
        Neuron.sigmoid(this.getSenseAtPredictionOffset() + p);
    }
    
    public void revertSenseAtPredictionOffset() {
        this.setSenseAtPredictionOffset(this.storedSenseVal);
    }
    
    public void updateSenseAtPredictionOffset() {
        this.setSenseAtPredictionOffset(
            this.feeler.decodeSense());
    }
    
    public static class StateValueImportanceQueryException
        extends RuntimeException {
        
        public StateValueImportanceQueryException(String msg) {
            super(msg);
        }
    }
}