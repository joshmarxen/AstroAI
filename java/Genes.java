package AstroAI.java;

import java.util.HashMap;

public class Genes {

	private HashMap<String, Double> attributeValueMap;

	public Genes() {
		attributeValueMap = new HashMap<String, Double>();
	}

	public Double getAttribute(String attrKey) {
		return this.attributeValueMap.get(attrKey);
	}
}
