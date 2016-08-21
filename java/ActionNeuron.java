package AstroAI.java;

import AstroAI.java.PredictionNeuron;

public class ActionNeuron extends PredictionNeuron {

	public ActionNeuron(Brain b, SensorCell sc) {
		super(b, sc);
	}

	// Action neurons want to learn at a different rate than prediction-
	// calculation neurons
	@Override
	protected double getNeuralAdjustment() {
		return this.myBrain.getActionNeuronAdjustment();
	}
	
	// Modify this method so that PERTURBED actions can be used as inputs
	@Override
	public double getLastCalculatedOutput() {
		return this.mySensorCell.getSenseAtPredictionOffset();
	}
}
