package uk.ac.soton.ecs.lifeguide.randomisation;

import java.util.*;
import javax.persistence.*;

@Entity
@Table(name = "attributes")
public class Attribute {

	@Id @GeneratedValue
	@Column(name="id")
	private int id;

	@ManyToOne
	@JoinColumn(name="trial_id")
	private Trial trial;

	@Column(name="name")
	private String name;

	@Column(name="weight")
	private double weight;

	@OneToMany(mappedBy="attribute", cascade = {CascadeType.ALL})
	private List<Stratum> strata = new ArrayList<Stratum>();

	@Column(name="attribute_order")
	int attributeOrder;

	/* constructors */

	public Attribute() {
		strata = new ArrayList<Stratum>();
	}

	public Attribute(String name, List<Stratum> strata, double weight) {
		this.name = name;
		this.strata = strata;
		this.weight = weight;

		// mrt - add the back references to the attribute from the grouping
		for (Stratum s: strata) {
			s.setAttribute(this);
		}
	}

	public String getStratumNameForValue(String value) {
		for (Stratum s : strata) {
			if (s.inStratum(value)) {
				return s.getName();
			}
		}
		return null;
	}

	public List<String> getAllStratumNames() {
		List<String> result = new ArrayList<String>();

		for (Stratum s : strata) {
			result.add(s.getName());
		}

		return result;
	}

	/* getters and setters */

	public int getId() { return id;}
	public void setId(int id) { this.id = id; }

	public Trial getTrial() { return trial; }
	public void setTrial(Trial trial) { this.trial = trial; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public double getWeight() { return weight; }
	public void setWeight(double weight) { this.weight = weight; }

	public List<Stratum> getStrata() { return strata; }
	public void setStrata(List<Stratum> strata) { this.strata = strata; }

	public int getAttributeOrder() { return attributeOrder; }
	public void setAttributeOrder(int attributeOrder) { this.attributeOrder = attributeOrder; }
}
