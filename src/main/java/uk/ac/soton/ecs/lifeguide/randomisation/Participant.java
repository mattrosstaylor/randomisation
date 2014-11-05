package uk.ac.soton.ecs.lifeguide.randomisation;

import javax.persistence.*;
import java.util.*;
import org.json.*;

@Entity
@Table(name="participants", uniqueConstraints=@UniqueConstraint(columnNames={"trial_id", "identifier"}))
public class Participant {
	
	@Id @GeneratedValue
	@Column(name="id")
	private int id;

	@ManyToOne
	@JoinColumn(name="trial_id")
	private Trial trial;

	@Column(name="identifier")
	private String identifier;

	@ManyToOne
	@JoinColumn(name="allocated_arm_id")
	private Arm allocatedArm;

	@Column
	private String data;

	@Transient
	private JSONObject json = null;

	/* crystal methods */
	public String getResponse(String key) {
		if (json == null) {
			json = new JSONObject(data);
		}
		return json.getString(key);
	}

	/* getters and setters */

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }

	public Trial getTrial() { return trial; }
	public void setTrial(Trial trial) { this.trial = trial; }

	public String getIdentifier() { return identifier; }
	public void setIdentifier(String identifier) { this.identifier = identifier; }

	public Arm getAllocatedArm() { return allocatedArm; }
	public void setAllocatedArm(Arm allocatedArm) { this.allocatedArm = allocatedArm; }

	public String getData() { return data; }
	public void setData(String data) { this.data = data; }
}