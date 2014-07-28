package uk.ac.soton.ecs.lifeguide.randomisation;

import javax.persistence.*;
//import org.hibernate.annotations.*;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.MapKey;

import java.util.*;

@Entity
@Table(name = "trials")
public class Trial {

	@Id @GeneratedValue
	@Column(name="id")
	private int id;

	@Column(name="name", unique=true)
	private String name;

	@Column(name="strategy")
	private String strategy;

	@OneToOne
    @JoinColumn(name="default_arm_id")
	private Arm defaultArm;

	@OneToMany(mappedBy="trial", cascade = {CascadeType.ALL})
	@OrderBy("armOrder")
	private List<Arm> arms = new ArrayList<Arm>();

	@OneToMany(mappedBy="trial", cascade = {CascadeType.ALL})
	private List<Attribute> attributes = new ArrayList<Attribute>();

	@CollectionOfElements(targetElement=java.lang.Float.class)
	@JoinTable(name="parameters", joinColumns=@JoinColumn(name="trial_id"))
	@MapKey(columns=@Column(name="name"))
	@Column(name="value") 
	private Map<String, Float> parameters = new HashMap<String, Float>();

	@CollectionOfElements(targetElement=java.lang.Float.class)
	@JoinTable(name="statistics", joinColumns=@JoinColumn(name="trial_id"))
	@MapKey(columns=@Column(name="name"))
	@Column(name="value") 
	private Map<String, Float> statistics = new HashMap<String, Float>();

	@Transient
	private Class<? extends Strategy> strategyClass;

	/* constructor */
    
    public Trial() {}

	public Trial(	String name, Class<? extends Strategy> strategyClass, String strategy,                                                                                                                     
					Map<String, Float> parameters, List<Attribute> attributes,                                                                                                           
					List<Arm> arms, int[] clusterFactors) {                                                                                               
		this.name = name;
		this.strategy = strategy;
		this.strategyClass = strategyClass;
		this.parameters = parameters;
		
		for (Attribute a: attributes) {
			addAttribute(a);
		}
		for (Arm a: arms) {
			addArm(a);
		}

		// mrt - cluster factors do nothing - so who cares?
		//this.clusterFactors = clusterFactors;
	}

	/* functions, son */

	public void addArm(Arm arm) {
		arms.add(arm);
		arm.setArmOrder(arms.indexOf(arm));
		arm.setTrial(this);
	}

	public void addAttribute(Attribute attribute) {
		attributes.add(attribute);
		attribute.setTrial(this);
	}

	public void setDefaultArm(String name) {
		for (Arm arm : arms) {
			if (arm.getName().equals(name)) {
				defaultArm = arm;
			}
		}
	}

	public int getArmCount() {
		return arms.size();
	}

	public int getStratifiedEnumeration(Participant participant) {
		int result = 0;
		int stratifiedCount = getStratifiedCount();
		
		for (Attribute attribute : attributes) {
			if (attribute.isGroupingFactor() == true) {
				stratifiedCount /= attribute.getNumberOfGroups();
				result += attribute.getGroupIndex(participant.getResponse(attribute.getName())) * stratifiedCount;
			}
		}
		return result;
	}

	public int getStratifiedCount() {
		int stratifiedCount = 1;
		for (Attribute attribute : attributes) {
			if (attribute.isGroupingFactor() == true) {
				stratifiedCount *= attribute.getNumberOfGroups();
			}
		}
		return stratifiedCount;
	}

	// mrt - fuck this method
	public int getDefaultArmIndex() {
		return 0;
	}

	public String toString() {
		String output = "Trial: " + name + "\n";
		output += "Allocation strategy: " + strategy;
		
		output += "\nArms: ";
		for (Arm arm : arms) {
			output += "\n" + arm;
		}

		output += "\nParameters: ";
		if (parameters.size() == 0) {
			output += "\nNone";
		}
		else {
			for (String key : parameters.keySet()) {
				output += "\n" + key + " = " + parameters.get(key);
			}
		}
/*        output += "\n\nClustered?: " + readableBoolean(isClustered());
		if (isClustered()) {
			output += "\nCluster factors:";
			List<Attribute> clusterAttributes = getClusterFactors();
			for (Attribute attr : clusterAttributes)
				output += " " + attr.getAttributeName();
		} */ // mrt - cluster factors are never used
		output += "\nAttributes: ";
		if (attributes.size() == 0) {
			output +="\nNone";
		}
		else {
			for (Attribute attr : attributes) {
				output += "\n" + attr;
			}
		}

		if (defaultArm != null) {
			output += "\nDefault Arm: " + defaultArm.getName();
		}
		return output;
	}

	/* getters and setters */

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getStrategy() { return strategy; }
	public void setStrategy(String strategy) { this.strategy = strategy; }
	public Class<? extends Strategy> getStrategyClass() { return strategyClass; }

	public Arm getDefaultArm() { return defaultArm; }
	public void setDefaultArm(Arm defaultArm) { this.defaultArm = defaultArm; }
	
	public List<Arm> getArms() { return arms; }
	public void setArms(List<Arm> arms) { this.arms = arms; }

	public List<Attribute> getAttributes() { return attributes; }
	public void setAttributes(List<Attribute> attributes) { this.attributes = attributes; }

	public Map<String, Float> getParameters() { return parameters; }
	public void setParameters(Map<String, Float> parameters) { this.parameters = parameters; }

	public Map<String, Float> getStatistics() { return statistics; }
	public void setStatistics(Map<String, Float> statistics) { this.statistics = statistics; }
}