package uk.ac.soton.ecs.lifeguide.randomisation;

import javax.persistence.*;

@Entity
@Table(name = "arms", uniqueConstraints=@UniqueConstraint(columnNames={"trial_id", "name"}))
public class Arm {
	@Id @GeneratedValue
	@Column(name="id")
	private int id;

	@ManyToOne
	@JoinColumn(name="trial_id")
	private Trial trial;

	@Column(name="name")
	private String name;

	@Column(name="weight")
	private int weight;

	@Column(name="max_participants")
	private int maxParticipants;

	@Column(name="participant_limit")
	private boolean participantLimit;

	@Column(name="arm_order")
	int armOrder;

	/* constructors */

	public Arm() {
	}

	public Arm(String name, int weight) {
		this(name, weight, Integer.MAX_VALUE);
	}

	public Arm(String name, int weight, int maxParticipants) {
		this.name = name;
		this.weight = weight;
		this.maxParticipants = maxParticipants;
		if (this.maxParticipants < Integer.MAX_VALUE) {
			this.participantLimit = true;
		}
		else {
			this.participantLimit = false;
		}
	}
	
	/* methods */

	public String toString() {
		String output = name + " ";
		output += "(Weight: " + weight;
		if (getParticipantLimit())
			output += ", Limit: " + (maxParticipants == Integer.MAX_VALUE ? "none" : maxParticipants);
		output += ")";
		return output;
	}

	/* getters and setters */

	public int getId() { return id;}
	public void setId(int id) { this.id = id; }

	public Trial getTrial() { return trial; }
	public void setTrial(Trial trial) { this.trial = trial; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public int getWeight() { return weight; }
	public void setWeight(int weight) { this.weight = weight; }

	public int getMaxParticipants() { return maxParticipants; }
	public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }

	public boolean getParticipantLimit() { return participantLimit; }
	public void setParticipantLimit(boolean participantLimit) { this.participantLimit = participantLimit; }

	public int getArmOrder() { return armOrder; }
	public void setArmOrder(int armOrder) { this.armOrder = armOrder; }
}