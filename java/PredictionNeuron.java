package AstroAI.java;

import AstroAI.java.Neuron;
import AstroAI.java.SensorCell;

public class PredictionNeuron extends Neuron {

    // Brains have a sensor cell for each sensory input. It has past
    // sensory inputs as well as space for predictions of future sensory
    // values
    protected SensorCell mySensorCell;
	
    public PredictionNeuron(Brain b, SensorCell sc) {
	super(b);
	//this.bias = 0.0;
	this.mySensorCell = sc;
    }

    private void updateSensorCellWithIdealOutput() {
        this.mySensorCell.setCurrentSense(this.lastIdealOutput);
    }

    // Because this method calls updateCalculatedOutput, we need to ensure
    // that SensorCell isn't overwritten by the wrong (non-ideal) output,
    // ie, something that is calculated rather than derived from actual
    // sensor input.
    @Override
    protected void updateBiasAndWeights() {
        super.updateBiasAndWeights();
        this.updateSensorCellWithIdealOutput();
    }

    // For PredictionNeurons, the ideal output is whatever the current
    // sensory reading is
    @Override
    protected void updateErrorAndIdealOutput() {
        this.lastIdealOutput = this.mySensorCell.getCurrentSense();
    }
    
    // calculates the output by applying sig(weighted input sum + bias)
    @Override
    protected void updateCalculatedOutput() {
        super.updateCalculatedOutput();
        this.mySensorCell.setCurrentSense(
            this.lastCalculatedOutput);
    }
}