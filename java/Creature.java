package AstroAI.java;

import AstroAI.java.Environment;
import AstroAI.java.Genes;

public class Creature {
    
    private Environment myEnvironment;
    private Genes myGenes;
    private Brain myBrain;
    
    /*
    public Creature(Environment env, Genes genes) {
        this.myEnvironment = env;
        this.myGenes = genes;
        this.myBrain = new Brain(
            this,
            genes.getAttribute("neuronAdjustment").doubleValue(),
            genes.getAttribute("actionNeuronAdjustment").doubleValue());
    }
    */
    
    public Environment getEnvironment() {
        return this.myEnvironment;
    }
    
}