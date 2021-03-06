package uk.ac.soton.ecs.lifeguide.randomisation;

import uk.ac.soton.ecs.lifeguide.randomisation.exception.*;
import javax.persistence.*;
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
	@OrderBy("variableOrder")
	private List<Variable> variables = new ArrayList<Variable>();

	@OneToMany(mappedBy="trial", cascade = {CascadeType.ALL})
	private List<Participant> participants = new ArrayList<Participant>();

	@CollectionOfElements(targetElement=java.lang.Double.class)
	@JoinTable(name="parameters", joinColumns=@JoinColumn(name="trial_id"))
	@MapKey(columns=@Column(name="name"))
	@Column(name="value") 
	private Map<String, Double> parameters = new HashMap<String, Double>();

	/* constructor */
    
    public Trial() {}

	/* functions, son */

	public void addArm(Arm arm) {
		arms.add(arm);
		arm.setArmOrder(arms.indexOf(arm));
		arm.setTrial(this);
	}

	public void addVariable(Variable variable) {
		variables.add(variable);
		variable.setVariableOrder(variables.indexOf(variable));
		variable.setTrial(this);
	}

	public void addParticipant(Participant p) {
		participants.add(p);
		p.setTrial(this);
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

	public String getStrata(Participant participant) {
		String result = "";
		
		boolean notFirst = false;
		for (Variable variable : getVariablesByType("stratification")) {

			if (notFirst) {
				result += ", ";
			}
			else{
				notFirst = true;
			}
			result += variable.getName();

			String response = participant.getResponse(variable.getName());

			result += " "+variable.getStratumNameForValue(response);
		}
		return result;
	}

	public List<String> getAllStrata() {
		List<String> result = new ArrayList<String>();
		result.add("");

		boolean notFirst = false;
		for (Variable variable : getVariablesByType("stratification")) {
			
			List<String> newResult = new ArrayList<String>();

			for (String strata : result){
				if (notFirst) {
					strata += ", ";
				}
				else{
					notFirst = true;
				}
				strata += variable.getName();

				for (String stratumName : variable.getAllStratumNames()){
					newResult.add(strata + " "+stratumName);
				}
			}

			result = newResult;
		}
		return result;
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

		output += "\nVariables: ";
		if (variables.size() == 0) {
			output +="\nNone";
		}
		else {
			for (Variable v : variables) {
				output += "\n" + v;
			}
		}

		if (defaultArm != null) {
			output += "\nDefault Arm: " + defaultArm.getName();
		}
		return output;
	}

	public Arm allocate(Participant participant, DataManager database) throws InvalidTrialException, uk.ac.soton.ecs.lifeguide.randomisation.exception.PersistenceException {
		return Strategy.create(this, database).allocate(participant);
	}

	public int getTotalWeight() {
		int weight = 0;
		for (Arm a: arms) {
			weight += a.getWeight();
		}
		return weight;
	}

	public List<Variable> getVariablesByType(String type) {
		List<Variable> result = new ArrayList<Variable>();
		for(Variable v: variables) {
			if (v.getType() == null || v.getType().equals(type)) {
				result.add(v);
			}
		}
		return result;
 	}

	/* getters and setters */

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getStrategy() { return strategy; }
	public void setStrategy(String strategy) { this.strategy = strategy; }

	public Arm getDefaultArm() { return defaultArm; }
	public void setDefaultArm(Arm defaultArm) { this.defaultArm = defaultArm; }
	
	public List<Arm> getArms() { return arms; }
	public void setArms(List<Arm> arms) { this.arms = arms; }

	public List<Variable> getVariables() { return variables; }
	public void setVariables(List<Variable> variables) { this.variables = variables; }

	public List<Participant> getParticipants() { return participants; }
	public void setParticipants(List<Participant> participants) { this.participants = participants; }

	public Map<String, Double> getParameters() { return parameters; }
	public void setParameters(Map<String, Double> parameters) { this.parameters = parameters; }
}
