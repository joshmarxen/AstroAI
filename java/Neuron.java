package AstroAI.java;

import AstroAI.java.Connection;

import java.lang.Math;
import java.lang.RuntimeException;
import java.util.HashSet;
import java.util.Iterator;

public class Neuron {
	
	// the brain this neuron belongs to
	protected Brain myBrain;
	// the incoming connections to this neuron
	protected HashSet<Connection> inputs;
	// the outgoing connections from this neuron
	protected HashSet<Connection> outputs;
	// all neurons which are reachable from this neuron
	protected HashSet<Neuron> fanout;
	// value added to weighted input sum before applying the sigmoid
	// function
	protected double bias;

	public Neuron(Brain b) {
		this.inputs = new HashSet<Connection>();
		this.outputs = new HashSet<Connection>();
		this.fanout = new HashSet<Neuron>();
		this.fanout.add(this);
		this.bias = GaussGetter.getGauss().doubleValue();
		this.myBrain = b;
		this.resetDFSState();
	}

	// utility variables for tracking outputs derived from NN computations
	// and error calculations
	protected double lastCalculatedOutput;
	protected double lastIdealOutput;
	public double getError() {
		return this.lastIdealOutput - this.lastCalculatedOutput;
	}

	// Updates the neuron's bias and the weights of the incoming
	// connections according to the calculated ideal output value,
	// compared to the value it would have output IF its incoming
	// neurons had also acted as they should ideally have. Moves
	// along the gradient of the output function (with respect to the
	// weights and the internal bias) a fraction of the way towards the
	// ideal output value.
	protected void updateBiasAndWeights() {
		
		// Since input neurons may have updated, recalculate our own
		// output before calculating which direction to move
		this.updateCalculatedOutput();
		
		// Calculate gradient of output wrt weights and bias.
		// Gradient is implicitly available. Lets just calculate
		// the magnitude.
		double grad_mag_squared = 1.0; // from bias
		for(Connection c : inputs) {
			// TODO: consider updating calculated output by the
			// Ideal outputs of input connections, rather than their
			// updated calculated outputs.
			double v = c.getSrcNeuron().getLastCalculatedOutput();
			grad_mag_squared += v*v;
		}
		double grad_mag = Math.sqrt(grad_mag_squared);
		double grad_frac = this.getNeuralAdjustment()/grad_mag;
		if (this.getError() < 0.0) {
			grad_frac = -grad_frac;
		} else if (Math.abs(this.getError())
			   < Neuron.noChangeThreshold) {
			
		}
		
		// Move a little bit along the gradient toward the ideal output
		this.bias += grad_frac;
		for(Connection c : inputs) {
			c.adjustWeightBy(grad_frac);
		}
		
		// now that weights have changed, update our calculated
		// output again so other output neurons can react accordingly
		this.updateCalculatedOutput();
	}

	// gets the fraction by which to approach the ideal output value
	// when learning
	protected double getNeuralAdjustment() {
		return this.myBrain.getNeuronAdjustment();
	}
	
	// calculates the output by applying sig(weighted input sum + bias)
	protected void updateCalculatedOutput() {
		
		this.lastCalculatedOutput = this.bias;
		for(Connection c : this.inputs) {
			this.lastCalculatedOutput += c.getUpdateContribution();
		}
		this.lastCalculatedOutput = 
			Neuron.sigmoid(this.lastCalculatedOutput);
	}

	// According to destNeurons' adjustments, calculate own adjustment.
	// Then, use that to calculate next ideal output.
	protected void updateErrorAndIdealOutput() {
		
		double adjustment = 0.0;
		for(Connection c : this.outputs) {
			adjustment += c.getAdjustmentContribution();
		}

		this.lastIdealOutput = Neuron.sigmoid(
			this.lastCalculatedOutput + adjustment);
	}
	
	public double getLastCalculatedOutput() {
		return this.lastCalculatedOutput;
	}

	public double getLastIdealOutput() {
		return this.lastIdealOutput;
	}
	
	public void addInputConnection(Connection c) {
		// check that c.dest == this neuron
		if(c.getDestNeuron() != this) {
			throw new Neuron.BadConnectionException(
				"Attempted to add input connection to a " +
				"neuron which is not the destination " +
				"neuron of that connection.");
		}
		
		this.inputs.add(c);
	}
	
	public void removeInputConnection(Connection c) {
		this.inputs.remove(c);
	}
	
	public void addOutputConnection(Connection c) {
		// check that c.src == this neuron
		if(c.getSrcNeuron() != this) {
			throw new Neuron.BadConnectionException(
				"Attempted to add output connection to a " +
				"neuron which is not the source neuron of " +
				"that connection.");
		}
		
		this.outputs.add(c);
	}
	
	public void removeOutputConnection(Connection c) {
		this.outputs.remove(c);
	}
	
	public int getNumOutputs() {
		return this.outputs.size();
	}
	
	// Assigns pre and post numbers to itself
	protected int dfsPre;
	protected int dfsPost;
	public void DFS(Brain.DFSClock clock) {
		if(this.dfsPre >= 0) {
			// already visited, do nothing
			return;
		}
		
		this.dfsPre = clock.getTime();
		for(Connection c : this.outputs) {
			c.getDestNeuron().DFS(clock);
		}
		this.dfsPost = clock.getTime();
		clock.addToTopo(this);
	}
	
	public int getDFSPre() {
		return this.dfsPre;
	}
	
	public int getDFSPost() {
		return this.dfsPost;
	}
	
	public void resetDFSState() {
		this.dfsPre = -1;
		this.dfsPost = -1;
	}

	public Environment getEnvironment() {
		return myBrain.getEnvironment();
	}

	// Simple sigmoid function, actually just ensures that output value
	// is between 1.0 and -1.0.
	public static double sigmoid(double t) {
		//return 1.0/(1.0 + Math.exp(-t)) - 0.5;
		if(t > 1.0) {
			return 1.0;
		}
		if(t < -1.0) {
			return -1.0;
		}
		return t;
	}
	
	public static class BadConnectionException extends RuntimeException {
		public BadConnectionException(String msg) {
			super(msg);
		}
	}
	
	protected static final double noChangeThreshold = 1e-9;
}
