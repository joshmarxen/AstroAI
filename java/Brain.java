package AstroAI.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
	private HashMap<String, SensorCell> sensorCells;
	private ArrayList<Neuron> interNeurons;
	private ArrayList<MemoryNeuron> memoryNeurons;
	private ArrayList<PredictionNeuron> predictionNeurons;
	private ArrayList<ActionNeuron> actionNeurons;
	private ArrayList<Neuron> neuronsWithInputs;
	private ArrayList<Neuron> neuronsWithOutputs;
	private ArrayList<Neuron> allNeurons;
	private HashSet<Connection> connections;
	private Random random;

	public Brain(
			Creature myCreature,
			List<String> senseList,
			List<String> actionList,
			double na,
			double ana) {
		this.myCreature = myCreature;
		this.neuronAdjustment = na;
		this.actionNeuronAdjustment = ana;
		this.random = new Random();
		
		// initialize neuron lists
		this.memoryNeurons = new ArrayList<MemoryNeuron>();
		this.predictionNeurons = new ArrayList<PredictionNeuron>();
		this.actionNeurons = new ArrayList<ActionNeuron>();
		
		this.sensorCells = new HashMap<String, SensorCell>();
		
		// initialize sense SensorCells
		for(String sense : senseList) {
			// create sensor cell
			SensorCell nsc =
				new SensorCell(this,
					       Brain.sensorCellDepth);
			this.sensorCells.put(sense, nsc);
			// create neurons
			for(int i = 1; i <= Brain.memoryCellDepth; ++i) {
				this.memoryNeurons.add(
					new MemoryNeuron(this, nsc, i));
			}
			this.predictionNeurons.add(
				new PredictionNeuron(this, nsc));
		}
		
		// initialize action SensorCells
		for(String action : senseList) {
			// create sensor cell
			SensorCell nsc =
				new SensorCell(this,
					       Brain.sensorCellDepth);
			this.sensorCells.put(action, nsc);
			// create neurons
			for(int i = 1; i <= Brain.memoryCellDepth; ++i) {
				this.memoryNeurons.add(
					new MemoryNeuron(this, nsc, i));
			}
			this.predictionNeurons.add(
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
		// all interneurons should have at least ONE output
		for(Neuron interNeuron : this.interNeurons) {
			// while this neuron has no output...
			while(!this.tryAddConnection(
				interNeuron, this.getRandomDestNeuron()));
		}
		// Now start adding connections randomly
		while(this.connections.size() < Brain.numConnections) {
			this.tryAddConnection(
				this.getRandomSrcNeuron(),
				this.getRandomDestNeuron());
		}
	}
	
	private boolean tryAddConnection(Neuron src, Neuron dest) {
		
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
		Connection nc = new Connection(src, dest);
		this.connections.add(nc);
		src.addOutputConnection(nc);
		dest.addInputConnection(nc);
		
		// update pre/post number assignments
		this.DFS();
		
		return true;
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
	
	private void DFS() {
		for(Neuron n : this.allNeurons) {
			n.resetAlgState();
		}
		Brain.DFSClock clock = new Brain.DFSClock();
		for(Neuron n : this.allNeurons) {
			n.DFS(clock);
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

	public Environment getEnvironment() {
		return this.myCreature.getEnvironment();
	}

	public double getNeuronAdjustment() {
		return this.neuronAdjustment;
	}

	public double getActionNeuronAdjustment() {
		return this.actionNeuronAdjustment;
	}
	
	public int getCurrentPredictionIndex() {
		return this.getEnvironment().getTime() % Brain.sensorCellDepth;
	}
	
	public static final int sensorCellDepth = 20;
	public static final int memoryCellDepth = 10;
	public static final int numInterNeurons = 150;
	public static final int connectionHistoryDepth = 1000;
	public static final int numConnections = 2000;
	
	public static class DFSClock {
		private int time;
		public DFSClock() {
			this.time = 0;
		}
		
		public int getTime() {
			return this.time++;
		}
	}

}
