package AstroAI.java;

import java.util.Random;

public class GaussGetter {

	public static Random r = new Random(System.nanoTime());
	
	public static Double getGauss() {
		return new Double(r.nextGaussian());
	}
}	
