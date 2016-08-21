package AstroAI.java;

import java.util.ArrayList;
import java.util.Comparator;

import AstroAI.java.Brain;
import AstroAI.java.Neuron;

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
	
	public Connection(Neuron src, Neuron dest, double weight) {
		
		// Initialize source and dest neurons
		this.srcNeuron = src;
		this.destNeuron = dest;

		// Initialize weight history
		this.weightHist = new ArrayList<Double>(
			Brain.connectionHistoryDepth + 1
		);
		
		// set initial weight
		this.weightHist.set(0, GaussGetter.getGauss());
	}

	public Neuron getSrcNeuron() {
		return srcNeuron;
	}

	public Neuron getDestNeuron() {
		return destNeuron;
	}

	// The current weight is accessed from the weight history at the 
	// appropriate index
	public double getCurrentWeight() {
		return this.weightHist.get(
			this.weightHist.size()-1).doubleValue();
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
	// weight is moved by the value frac * srcNeuron's last calculated
	// output.
	public void adjustWeightBy(double frac) {
		this.weightHist.add(new Double(
			this.getCurrentWeight()
			+ frac*this.srcNeuron.getLastCalculatedOutput()));
	}
	
	public double getAverageWeight() {
		double sum = 0.0;
		for(Double w : this.weightHist) {
			sum += w.doubleValue();
		}
		return sum/this.weightHist.size();
	}
	
	// Returns the difference between the maximum weight seen in the last
	// weight history period and the minimum weight.
	public double getWeightRange() {
		double max = this.weightHist.get(0).doubleValue();
		double min = max;
		for(Double w : this.weightHist) {
			if(w.doubleValue() > max) {
				max = w.doubleValue();
			} else if (w.doubleValue() < min) {
				min = w.doubleValue();
			}
		}
		return max - min;
	}
	
	public void resetWeightHistory() {
		double lastWeight = this.getCurrentWeight();
		this.weightHist = new ArrayList<Double>(
			Brain.connectionHistoryDepth + 1);
		this.weightHist.set(0, new Double(lastWeight));
	}
	
	public static class ConnectionComparator
		implements Comparator<Connection> {
		
		// Compares two Connections based on their lack of importance
		public int compare(Connection c1, Connection c2) {
			double c1wgt = Math.abs(c1.getAverageWeight());
			double c2wgt = Math.abs(c2.getAverageWeight());
			if (c1wgt < c2wgt) {
				return 1;  // c1 is less important, so greater
					   // in ordering
			} else if (c1wgt > c2wgt) {
				return -1;  // c2 is less important, so greater
					    // in ordering
			} else {
				// order by spread
				c1wgt = c1.getWeightRange();
				c2wgt = c2.getWeightRange();
				if(c1wgt < c2wgt) {
					// spread of c1 is less, therefore c1
					// is less important, therefore is
					// greater
					return 1;
				} else if(c1wgt > c2wgt) {
					return -1;
				}
			}
			return 0;
		}
		
		// not sure how this is supposed to work...
		public boolean equals(Object o) {
			return false; 	
		}
	}

}
