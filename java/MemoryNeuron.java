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
	
	// Overrided method does nothing. There are no input connections,
	// and memory neurons should not use their bias for any reason
	@Override
	protected void updateBiasAndWeights() {}
	
	// TODO: make sure this is applicable where its supposed to be
	// ^ WHAT THE HELL DOES THIS MEAN
	@Override
	public double getLastCalculatedOutput() {
		return this.mySensorCell.getSenseAt(-this.timePassed);
	}
	
	@Override
	public double getLastIdealOutput() {
		return this.getLastCalculatedOutput();
	}
}
