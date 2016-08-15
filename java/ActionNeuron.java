package AstroAI.java;

import AstroAI.java.PredictionNeuron;

public class ActionNeuron extends PredictionNeuron {

	public ActionNeuron(Brain b, SensorCell sc) {
		super(b, sc);
	}

	@Override
	protected double getNeuralAdjustment() {
		return this.myBrain.getActionNeuronAdjustment();
	}
	
	/*
	public void perturbAction(double delta) {
		this.lastIdealOutput += delta;
		this.updateSensorCellWithIdealOutput();
	}
	*/
}
