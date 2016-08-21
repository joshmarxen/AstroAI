package AstroAI.java;

public class Feeler {

    private double stateValueImportance;

    public Feeler(double importance) {
        this.stateValueImportance = importance;
    }
    
    public double getStateValueImportance() {
        return this.stateValueImportance;
    }

    // TODO: implement this, or make Feeler an interface
    public double decodeSense() {
        return 0.0;
    }
}