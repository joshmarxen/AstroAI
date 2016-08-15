package AstroAI.java;

import AstroAI.java.Neuron;

import java.util.ArrayList;

// A one-way connection between two neurons, srcNeuron and destNeuron.
// Contributes to destNeuron's output calculation with weight*srcNeuron's
// output
public class Connection {

	// the two neurons, input and output, associated with this
	// connection
	private Neuron srcNeuron, destNeuron;

	// history of weight values, used to determine whether the
	// connection should be pruned
	private ArrayList<Double> weightHist;
	
	public Connection(Neuron src, Neuron dest) {
		
		// Initialize source and dest neurons
		this.srcNeuron = src;
		this.destNeuron = dest;

		// Initialize weight history
		this.weightHist = new ArrayList<Double>(
			Brain.connectionHistoryDepth + 1
		);

		/* probably not necessary
		int i;
		for(	i = 0; 
			i < Brain.connectionHistoryDepth;
			++i
		) {
			this.weightHist.add(new Double(0.0));
		}
		*/
		
		// set initial weight
		this.weightHist.set(0, GaussGetter.getGauss());
	}

	public Neuron getSrcNeuron() {
		return srcNeuron;
	}

	public Neuron getDestNeuron() {
		return destNeuron;
	}

	private int getCurrentWeightIndex() {
		int historyIndex = this.srcNeuron.getEnvironment().getTime();
		return historyIndex % Brain.connectionHistoryDepth;
	}

	// The current weight is accessed from the weight history at the 
	// appropriate index
	public double getCurrentWeight() {
		return this.weightHist.get(
			this.getCurrentWeightIndex());
	}

	// Called by neurons when calculating their output. Calculates this
	// connection's contribution to the destNeuron's output 
	// calculation in terms of the weight and the srcNeuron's output
	// value.
	public double getUpdateContribution() {
		return this.getCurrentWeight()*
			this.srcNeuron.getLastCalculatedOutput();
	}

	// Called by neurons when calculating their adjustment. Calculates
	// this connection's contribution to the srcNeuron's adjustment
	// calculation by multiplying the weight by the destNeuron's
	// adjustment.
	public double getAdjustmentContribution() {
		return this.getCurrentWeight()*destNeuron.getError();
	}
	
	// Called by destNeuron to adjust the connection's weight. Current
	// weight is moved by the value frac * srcNeuron's last ideal output.
	public void adjustWeightBy(double frac) {
		/*
		this.weightHist.set(
			this.getCurrentWeightIndex()+1,
			this.getCurrentWeight()+frac*this.srcNeuron.getLastIdealOutput()
		);
		*/
		this.weightHist.add(this.getCurrentWeight() +
				    frac*this.srcNeuron.getLastIdealOutput()
				   );
	}

}
