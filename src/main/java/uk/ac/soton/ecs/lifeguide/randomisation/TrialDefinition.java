package uk.ac.soton.ecs.lifeguide.randomisation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An object for specifying the setup of a given randomised control trial, including the
 * {@link Attribute}s, the treatment arms, the allocation method, and any additional parameters.
 *
 * @author Liam de Valmency (lpdv1g10@ecs.soton.ac.uk)
 * @author Aleksandar Botev (ab9g10@ecs.soton.ac.uk)
 * @author Dionisio Perez-Mavrogenis (dpm3g10@ecs.soton.ac.uk)
 * @author Kim Svensson (ks6g10@ecs.soton.ac.uk)
 * @see Participant
 * @since 1.7
 */
public class TrialDefinition {
	private static final Logger logger = LoggerFactory.getLogger(TrialDefinition.class);

	private String trialName;
	private Class<? extends Strategy> strategy;
	private String strategyID;
	private List<Attribute> attributes;
	private List<Treatment> treatments;
	private Map<String, Float> strategyParams;
	private int[] clusterFactors;
	private Treatment defaultTreatment;


	/**
	 * Constructs an empty TrialDefinition object, with an empty list of attributes,
	 * treatments, parameters and clustering factors.
	 */
	public TrialDefinition() {
		attributes = new ArrayList<Attribute>();
		treatments = new ArrayList<Treatment>();
		strategyParams = new HashMap<String, Float>();
		clusterFactors = new int[0];
	}

	/**
	 * Creates a TrialDefinition with the specified parameters.
	 *
	 * @param trialName      The trial's name. Should be lower-case and alphanumeric.
	 * @param strategy       The Strategy class which should be used for allocating participants for this trial.
	 * @param strategyID     The name of the allocation strategy (will usually be identical to the class
	 *                       name for the strategy parameter).
	 * @param strategyParams A map of additional allocation parameters, indexed on the parameter name, mapping
	 *                       to a float value for the parameter.
	 * @param attributes     A list of {@link Attribute}s, which define the data on which the allocation is based.
	 * @param treatments     A list of {@link Treatment}s, which define the treatment arms into which participants
	 *                       may be allocated.
	 * @param clusterFactors A list of integers which acts as an index into the attributes list. Attributes indexed
	 *                       by the values in this array act as factors on which to cluster during the trial.
	 */
	public TrialDefinition(String trialName, Class<? extends Strategy> strategy, String strategyID,
						   Map<String, Float> strategyParams, List<Attribute> attributes,
						   List<Treatment> treatments, int[] clusterFactors) {
		this.trialName = trialName;
		this.strategy = strategy;
		this.attributes = attributes;
		this.treatments = treatments;
		this.strategyID = strategyID;
		this.clusterFactors = clusterFactors;
		this.strategyParams = strategyParams;
	}

	public String getTrialName() {
		return trialName;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

	/**
	 * @return The attribute with the specified name. Returns null if no such attribute exists within
	 *         this trial.
	 */
	public Attribute getAttributeByName(String attr_name) {
		Attribute ret_val = null;
		for (Attribute attr : attributes) {
			if (attr.getAttributeName().equals(attr_name)) {
				ret_val = attr;
				break;
			}
		}
		return ret_val;
	}

	public int getAttributeCount() {
		return attributes.size();
	}

	public Class<? extends Strategy> getStrategy() {
		return strategy;
	}

	public String getStrategyID() {
		return strategyID;
	}

	public int getParamCount() {
		return strategyParams.size();
	}

	public int getTreatmentCount() {
		return treatments.size();
	}

	public Float getStrategyParam(String paramName) {
		return strategyParams.get(paramName);
	}

	public Treatment getDefaultTreatment() {
		return defaultTreatment;
	}

	public int getDefaultTreatmentIndex() {
		if (defaultTreatment == null) {
			return -1;
		}

		int index = -1;
		for (int i = 0; i < treatments.size(); ++i) {
			if (treatments.get(i).equals(defaultTreatment)) {
				index = i;
			}
		}

		return index;
	}

	/**
	 * @return Whether or not this trial has any cluster factors.
	 */
	private boolean isClustered() { // mrt - cluster factors are never used
		return clusterFactors != null && clusterFactors.length > 0;
	}

	private List<Attribute> getClusterFactors() { // mrt - cluster factors are never used
		if (isClustered()) {
			List<Attribute> clusterAttributes = new ArrayList<Attribute>();
			for (Integer i : clusterFactors) {
				clusterAttributes.add(attributes.get(i));
			}
			return clusterAttributes;
		} 
		else {
			logger.warn("Requested cluster factors for " + trialName + ", but no cluster factors exist.");
			return new ArrayList<Attribute>();
		}
	}

	private int[] getClusterIndices() { // mrt - cluster factors are never used
		return clusterFactors;
	}

	public List<Treatment> getTreatments() {
		return treatments;
	}

	public void setTrialName(String trialName) {
		this.trialName = trialName;
	}

	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}

	public void setStrategyID(String id) {
		this.strategyID = id;
	}

	private void setClusterFactors(int[] attributeIndices) { // mrt - cluster factors are never used
		this.clusterFactors = attributeIndices;
	}

	public void setTreatments(List<Treatment> treatments) {
		this.treatments = treatments;
	}

	public Map<String, Float> getStrategyParams() {
		return strategyParams;
	}

	public void setStrategy(Class<? extends Strategy> strategy) {
		this.strategy = strategy;
	}

	public void setStrategyParams(Map<String, Float> params) {
		this.strategyParams = params;
	}

	public void setStrategyParam(String name, Float value) {
		if (this.strategyParams == null)
			this.strategyParams = new HashMap<String, Float>();
		strategyParams.put(name, value);
	}

	public void setDefaultTreatment(Treatment treatment) {
		this.defaultTreatment = treatment;
	}

	/**
	 * Sets the default treatment to use in the event that each of the treatment arms
	 * reaches their participant limit. May have no effect if no treatment with the
	 * given name exists in this trial.
	 *
	 * @param treatmentName The name of the treatment to use as the default treatment
	 *                      (must exist as a treatment in the list of treatments for this trial).
	 */
	public void setDefaultTreatment(String treatmentName) {
		for (Treatment treatment : treatments) {
			if (treatment.getName().equals(treatmentName))
				defaultTreatment = treatment;
		}
	}

	private String readableBoolean(boolean val) {
		return val ? "yes" : "no";
	}

	public String toString() {
		String output = "Trial: " + trialName + "\n";
		output += "Allocation strategy: " + strategyID;
		if (getParamCount() > 0) {
			output += "\n\nParameters:";
			for (String key : strategyParams.keySet()) {
				output += "\n" + key + " = " + strategyParams.get(key);
			}
		}
/*        output += "\n\nClustered?: " + readableBoolean(isClustered());
		if (isClustered()) {
			output += "\nCluster factors:";
			List<Attribute> clusterAttributes = getClusterFactors();
			for (Attribute attr : clusterAttributes)
				output += " " + attr.getAttributeName();
		} */ // mrt - cluster factors are never used
		output += "\n\nAttributes: ";
		for (Attribute attr : attributes) {
			output += "\n" + attr;
		}
		output += "\n\nTreatments: ";
		for (Treatment treatment : treatments) {
			output += "\n" + treatment;
		}
		if (defaultTreatment != null) {
			output += "\nDefault treatment: " + defaultTreatment.getName();
		}
		return output;
	}

	/**
	 * @param participant The participant to find the stratified group index for.
	 * @return The index of the group which the patient falls into when the groups
	 *         are enumerated over the stratification attributes. Enumeration of these
	 *         groups is done in the order of Attributes, then their group values.
	 */
	public int getStratifiedEnumeration(Participant participant) {
		int result = 0;
		int stratifiedCount = 1;
		for (Attribute attribute : attributes)
			if (attribute.isGroupingFactor() == true)
				stratifiedCount *= attribute.getGroupCount();
		for (Attribute attribute : attributes)
			if (attribute.isGroupingFactor() == true) {
				stratifiedCount /= attribute.getGroupCount();
				result += attribute.getGroupIndex(participant.getResponse(attribute.getAttributeName())) * stratifiedCount;
			}
		return result;
	}

	/**
	 * @return The number of separate groups which result when stratifying over the
	 *         specified grouping factors (e.g. stratifying over two attributes, each with three
	 *         groups, results in nine stratified groups).
	 */
	public int getStratifiedCount() {
		int stratifiedCount = 1;
		for (Attribute attribute : attributes) {
			if (attribute.isGroupingFactor() == true) {
				stratifiedCount *= attribute.getGroupCount();
			}
		}
		return stratifiedCount;
	}

	/**
	 * @param group The ID of the stratified group to which a participant belongs (one fetched by
	 *              {@link #getStratifiedEnumeration(Participant)}).
	 * @return A string which contains a human-readable, comma separated list of the group values corresponding
	 *         to a specific stratified group ID.
	 */
	public String getStratificationString(int group) {
		int count = group;
		int total = getStratifiedCount();

		String stratString = "";
		boolean firstStratGroupFound = false;

		for (Attribute attribute : attributes) {
			if (attribute.isGroupingFactor()) {
				total /= attribute.getGroupCount();
				int val = count / total;
				count -= val * total;

				// Prefix each subsequent group with a comma.
				if (firstStratGroupFound) {
					stratString += ", ";
				}
				firstStratGroupFound = true;

				stratString += attribute.getAttributeName() + " " + val;
			}
		}

		return stratString;
	}

	// mrt - have you guys even HEARD of the law of demeter?
	public String getTreatmentName(int index) {
		return treatments.get(index).getName();
	}

}
