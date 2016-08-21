package AstroAI.java;

import java.lang.Iterable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

import AstroAI.java.ActionNeuron;
import AstroAI.java.Connection;
import AstroAI.java.Creature;
import AstroAI.java.Environment;
import AstroAI.java.Genes;
import AstroAI.java.MemoryNeuron;
import AstroAI.java.Neuron;
import AstroAI.java.PredictionNeuron;

public class Brain {

	private Creature myCreature;
	// fraction by which ordinary and sensor neurons approach their
	// ideal output
	private double neuronAdjustment;
	// fraction by which action neurons approach their ideal output
	private double actionNeuronAdjustment;
	private HashMap<String, SensorCell> predictionSensorCells;
	private HashMap<String, SensorCell> actionSensorCells;
	private ArrayList<Neuron> interNeurons;
	private ArrayList<MemoryNeuron> memoryNeurons;
	private ArrayList<PredictionNeuron> predictionNeurons;
	private ArrayList<ActionNeuron> actionNeurons;
	private ArrayList<Neuron> neuronsWithInputs;
	private ArrayList<Neuron> neuronsWithOutputs;
	private ArrayList<Neuron> allNeurons;
	private HashSet<Connection> connections;
	private Random random;
	private int predictionOffset;
	private Brain.DFSClock topoOrder;
	private double perturbationRadius;

	public Brain(
			Creature myCreature,
			Map<String, Feeler> senseList,
			List<String> actionList,
			double na,
			double ana,
			double perturbationRadius) {
		this.myCreature = myCreature;
		this.neuronAdjustment = na;
		this.actionNeuronAdjustment = ana;
		this.perturbationRadius = perturbationRadius;
		this.random = new Random();
		
		// initialize neuron lists
		this.memoryNeurons = new ArrayList<MemoryNeuron>();
		this.predictionNeurons = new ArrayList<PredictionNeuron>();
		this.actionNeurons = new ArrayList<ActionNeuron>();
		
		// initialize sense SensorCells
		this.predictionSensorCells = new HashMap<String, SensorCell>();
		for(String sense : senseList.keySet()) {
			Feeler f = senseList.get(sense);
			// create sensor cell
			SensorCell nsc =
				new SensorCell(this, f);
			this.predictionSensorCells.put(sense, nsc);
			// create neurons
			for(int i = 1; i <= Brain.memoryDepth; ++i) {
				this.memoryNeurons.add(
					new MemoryNeuron(this, nsc, i));
			}
			this.predictionNeurons.add(
				new PredictionNeuron(this, nsc));
		}
		
		// initialize action SensorCells
		this.actionSensorCells = new HashMap<String, SensorCell>();
		for(String action : actionList) {
			// create sensor cell
			SensorCell nsc =
				new SensorCell(this, null);
			this.actionSensorCells.put(action, nsc);
			// create neurons
			for(int i = 1; i <= Brain.memoryDepth; ++i) {
				this.memoryNeurons.add(
					new MemoryNeuron(this, nsc, i));
			}
			this.actionNeurons.add(
				new ActionNeuron(this, nsc));
		}
		
		// Initialize Interneurons
		this.interNeurons =
			new ArrayList<Neuron>(Brain.numInterNeurons);
		for(int i = 0; i < Brain.numInterNeurons; ++i) {
			this.interNeurons.add(new Neuron(this));
		}
		
		// generate list of neurons that can serve as destNeurons in
		// Connections
		this.neuronsWithInputs = new ArrayList<Neuron>(
			this.interNeurons.size() +
			this.predictionNeurons.size() +
			this.actionNeurons.size());
		this.neuronsWithInputs.addAll(this.interNeurons);
		this.neuronsWithInputs.addAll(this.predictionNeurons);
		this.neuronsWithInputs.addAll(this.actionNeurons);
		
		// generate list of neurons that can serve as srcNeurons in
		// Connections
		this.neuronsWithOutputs = new ArrayList<Neuron>(
			this.interNeurons.size() +
			this.actionNeurons.size() +
			this.memoryNeurons.size());
		this.neuronsWithOutputs.addAll(this.interNeurons);
		this.neuronsWithOutputs.addAll(this.actionNeurons);
		this.neuronsWithOutputs.addAll(this.memoryNeurons);
		
		// have a collection of all neurons for DFS purposes
		this.allNeurons = new ArrayList<Neuron>(
			this.neuronsWithOutputs.size() +
			this.predictionNeurons.size());
		this.allNeurons.addAll(this.neuronsWithOutputs);
		this.allNeurons.addAll(this.predictionNeurons);
		
		// generate connections between neurons
		this.connections = new HashSet<Connection>();
		// set DFS states so that tryAddConnection works
		this.DFS();
		// all neurons with outputs should have at least ONE output
		for(Neuron n : this.neuronsWithOutputs) {
			// while this neuron has no output...
			while(!this.tryAddConnection(
				n, this.getRandomDestNeuron()));
		}
		// Now start adding connections randomly
		while(this.connections.size() < Brain.numConnections) {
			this.tryAddConnection(
				this.getRandomSrcNeuron(),
				this.getRandomDestNeuron(),
				GaussGetter.getGauss()
				*Brain.initialConnectionWeightVariance);
		}
	}
	
	// Given a src neuron and a dest neuron, attempts to add a connection
	// to them, assuming the connection is appropriate
	private boolean tryAddConnection(Neuron src, Neuron dest,
					 double initialWgt) {
		
		// check that src really can have outputs, ie, is not a
		// prediction neuron
		if(src instanceof PredictionNeuron &&
		   !(src instanceof ActionNeuron)) {
			return false;
		}
		
		// check that dest really can have inputs, ie, is not a
		// memory neuron
		if(dest instanceof MemoryNeuron) {
			return false;
		}
		
		// check that this is not a back edge
		if(src.getDFSPre() < dest.getDFSPre() &&
		   src.getDFSPost() > dest.getDFSPost()) {
			return false;
		}
		
		// ok, we're all good
		Connection nc = new Connection(src, dest, initialWgt);
		this.connections.add(nc);
		src.addOutputConnection(nc);
		dest.addInputConnection(nc);
		
		// update pre/post number assignments
		this.DFS();
		
		return true;
	}
	
	private boolean tryAddConnection(Neuron src, Neuron dest) {
		return this.tryAddConnection(src, dest, 0.0);
	}
	
	private void removeConnection(Connection c) {
		// Remove connections from neurons first
		c.getSrcNeuron().removeOutputConnection(c);
		c.getDestNeuron().removeInputConnection(c);
		
		// remove connection from our set
		this.connections.remove(c);
		
		// update pre/post number assignments
		this.DFS();
	}
	
	// Update the pre- and post- numbers of all neurons and get topological
	// ordering
	private void DFS() {
		
		// reset neural DFS state
		for(Neuron n : this.allNeurons) {
			n.resetDFSState();
		}
		
		// do DFS
		this.topoOrder = new Brain.DFSClock();
		for(Neuron n : this.allNeurons) {
			n.DFS(this.topoOrder);
		}
	}
	
	private Neuron getRandomSrcNeuron() {
		return this.neuronsWithOutputs.get(this.random.nextInt(
			this.neuronsWithOutputs.size()));
	}
	
	private Neuron getRandomDestNeuron() {
		return this.neuronsWithInputs.get(this.random.nextInt(
			this.neuronsWithInputs.size()));
	}
	
	// Given last inputs, generate actions, predict future outputs based
	// on last state and current actions, and explore alternate actions
	// to increase expected future value
	public void think() {
		// if time, prune connections
		if(this.getEnvironment().getTime()
		   % Brain.connectionHistoryDepth
		   == Brain.connectionHistoryDepth - 1) {
			this.shuffleConnections();
		}
		
		// generate outputs and evaluate value function.
		// This also generates the initial action values.
		double stateValue = this.calcAllNeuralOutputs();
		
		// Explore alternative actions.
		for(int i = 0; i < Brain.numActionUpdates; ++i) {
			this.perturbCurrentActions();
			double tempVal = propagateCurrentActions();
			tempVal += calcFutureNeuralOutputs();
			if(tempVal <= stateValue) {
				this.undoCurrentActionPerturbations();
			} else {
				stateValue = tempVal;
			}
		}
	}
	
	// Finds the least Brain.connectionsToPrune important connections,
	// removes them, and replaces them
	private void shuffleConnections() {
		Connection.ConnectionComparator connComp =
			new Connection.ConnectionComparator();
		PriorityQueue<Connection> connPQ =
			new PriorityQueue<Connection>(
				Brain.connectionsToPrune + 1,
				connComp);
			
		for(Connection conn : this.connections) {
			// check that connection is eligible for removal
			if(Math.abs(conn.getAverageWeight())
			   > Brain.averageWeightImportanceThreshold
			   || conn.getWeightRange()
			   > Brain.weightRangeImportanceThreshold) {
				// connection is too important to remove,
				// or is changing too much to be counted
				// as unimportant
				continue;
			}
			
			// If connection is sufficiently unimportant, put it
			// in the queue
			if(connPQ.size() < Brain.connectionsToPrune
			   || connComp.compare(conn, connPQ.peek()) > 0) {
				connPQ.add(conn);
				if(connPQ.size() > Brain.connectionsToPrune) {
					connPQ.poll();
				}
			}
		}
		
		// remove and replace the connections
		for(Connection c : connPQ) {
			Neuron src = c.getSrcNeuron();
			this.removeConnection(c);
			if(src.getNumOutputs() == 0) {
				// This is a democracy damn it!
				while(!this.tryAddConnection(src,
					this.getRandomDestNeuron())) {
					continue;
				}
			} else {
				while(!this.tryAddConnection(
					this.getRandomSrcNeuron(),
					this.getRandomDestNeuron())) {
					continue;
				}
			}
		}
	}
	
	// Chooses current actions based on neural defaults, and uses these
	// to predict sense and action values at all urrent and future timesteps
	private double calcAllNeuralOutputs() {
		double value = 0.0;
		for(this.predictionOffset = 0;
		    this.predictionOffset <
		    Brain.sensorCellDepth - Brain.memoryDepth;
		    ++this.predictionOffset) {
			value += this.calcNeuralOutputsAtPredictionOffset();
		}
		return value;
	}
	
	// Calculates the neural outputs at whatever temporal offset is
	// specified by the current value of this.predictionOffset, and
	// returns the resulting state value.
	private double calcNeuralOutputsAtPredictionOffset() {
		for(Neuron n : this.topoOrder.getForwardOrder()) {
			n.updateCalculatedOutput();
		}
		return this.calcStateValue();
	}
	
	// Given each sense value at the current prediction offset, computes
	// the state value.
	private double calcStateValue() {
		double retval = 0.0;
		for(SensorCell sc : this.predictionSensorCells.values()) {
			retval +=
				sc.getStateValContributionAtPredictionOffset();
		}
		return retval;
	}
	
	// Perturbs current action values for exploration purposes
	private void perturbCurrentActions() {
		this.predictionOffset = 0;
		for(SensorCell actionCell : this.actionSensorCells.values()) {
			actionCell.perturbSenseAtPredictionOffset(
				GaussGetter.getGauss()
				* this.perturbationRadius);
		}
	}
	
	// Rolls back each sensorCell's current  
	private void undoCurrentActionPerturbations() {
		this.predictionOffset = 0;
		for(SensorCell actionCell : this.actionSensorCells.values()) {
			actionCell.revertSenseAtPredictionOffset();
		}
	}
	
	// Updates neural outputs given current action perturbations. Does
	// NOT update neural outputs of action neurons
	private double propagateCurrentActions() {
		this.predictionOffset = 0;
		for(Neuron n : this.topoOrder.getForwardOrder()) {
			// skip action neurons
			if(n instanceof ActionNeuron) {
				continue;
			}
			n.updateCalculatedOutput();
		}
		return this.calcStateValue();
	}
	
	// Predicts future actions and senses, and returns sum of state values
	// at each future predicted time
	private double calcFutureNeuralOutputs() {
		double value = 0.0;
		for(this.predictionOffset = 1;
		    this.predictionOffset <
		    Brain.sensorCellDepth - Brain.memoryDepth;
		    ++this.predictionOffset) {
			value += this.calcNeuralOutputsAtPredictionOffset();
		}
		return value;
	}
	
	// Given last selected actions and actual inputs values, adjusts neural
	// biases and weights in the direction of the actual values
	public void learn() {
		// re-calculate neural outputs one more time. Don't update the
		// actions though.
		// TODO: is this necessary? might be done at end of this.think()
		this.propagateCurrentActions();
		
		// get inputs
		for(SensorCell sc : this.predictionSensorCells.values()) {
			sc.updateSenseAtPredictionOffset();
		}
		
		// calculate adjustment
		for(Neuron n : this.topoOrder.getBackwardOrder()) {
			n.updateErrorAndIdealOutput();
		}
		
		// update weights and biases
		for(Neuron n : this.topoOrder.getForwardOrder()) {
			n.updateBiasAndWeights();
		}
	}

	public Environment getEnvironment() {
		return this.myCreature.getEnvironment();
	}
	
	// Amount by which ordinary neurons adjust in the direction of their
	// ideal output.
	public double getNeuronAdjustment() {
		return this.neuronAdjustment;
	}

	// Amount by which Action neurons adjust in the direction of their
	// ideal output.
	public double getActionNeuronAdjustment() {
		return this.actionNeuronAdjustment;
	}
	
	public int getCurrentPredictionIndex() {
		return (
			this.getEnvironment().getTime()
			+ this.predictionOffset)
			% Brain.sensorCellDepth;
	}
	
	public static final int sensorCellDepth = 20;
	public static final int memoryDepth = 10;
	public static final int numInterNeurons = 150;
	public static final int connectionHistoryDepth = 1000;
	public static final int numConnections = 2000;
	public static final int numActionUpdates = 10;
	public static final int connectionsToPrune = 50;
	public static final double averageWeightImportanceThreshold = 0.05;
	public static final double weightRangeImportanceThreshold = 0.1;
	public static final double initialConnectionWeightVariance = 0.5;
	
	public static class DFSClock {
		private int time;
		private LinkedList<Neuron> forwardTopoOrder;
		private LinkedList<Neuron> backwardTopoOrder;
		public DFSClock() {
			this.time = 0;
			this.forwardTopoOrder = new LinkedList<Neuron>();
			this.backwardTopoOrder = new LinkedList<Neuron>();
		}
		
		public int getTime() {
			return this.time++;
		}
		
		public void addToTopo(Neuron n) {
			this.forwardTopoOrder.addFirst(n);
			this.backwardTopoOrder.add(n);
		}
		
		public LinkedList<Neuron> getForwardOrder() {
			return this.forwardTopoOrder;
		}
		
		public LinkedList<Neuron> getBackwardOrder() {
			return this.backwardTopoOrder;
		}
	}
}
