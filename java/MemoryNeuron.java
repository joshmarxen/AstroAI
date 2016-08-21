package AstroAI.java;

import AstroAI.java.Brain;
import AstroAI.java.Environment;
import AstroAI.java.PredictionNeuron;

public class MemoryNeuron extends PredictionNeuron {
	
	// Defines where in the sensor cell, relative to the cell corresponding
	// to the current time, that this neuron should query its 
	private int timePassed;
	
	public MemoryNeuron(Brain b, SensorCell sc, int timePassed) {
		super(b, sc);
		this.timePassed = timePassed;
	}
	
	// Since the memory neuron only conveys the underlying sensor cell
	// value, there is no reason to do expensive calculations here
	@Override
	protected void updateCalculatedOutput() {}
	@Override
	protected void updateErrorAndIdealOutput() {}
	@Override
	protected void updateBiasAndWeights() {}
	
	@Override
	public double getLastCalculatedOutput() {
		return this.mySensorCell.getSenseAtOffsetFromPredictionOffset(
						-this.timePassed);
	}
	
	@Override
	public double getLastIdealOutput() {
		return this.getLastCalculatedOutput();
	}
}
