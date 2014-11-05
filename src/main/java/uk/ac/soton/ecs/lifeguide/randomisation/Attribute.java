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
	private List<Grouping> groupings = new ArrayList<Grouping>();

	@Column(name="attribute_order")
	int attributeOrder;

	/* constructors */

	public Attribute() {
		groupings = new ArrayList<Grouping>();
	}

	public Attribute(String name, List<Grouping> groupings, double weight) {
		this.name = name;
		this.groupings = groupings;
		this.weight = weight;

		// mrt - add the back references to the attribute from the grouping
		for (Grouping g: groupings) {
			g.setAttribute(this);
		}
	}

	public String getGroupingNameForValue(String value) {
		for (Grouping g : groupings) {
			if (g.inGrouping(value)) {
				return g.getName();
			}
		}
		return null;
	}

	public List<String> getAllGroupingNames() {
		List<String> result = new ArrayList<String>();

		for (Grouping g : groupings) {
			result.add(g.getName());
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

	public List<Grouping> getGroupings() { return groupings; }
	public void setRanges(List<Grouping> groupings) { this.groupings = groupings; }

	public int getAttributeOrder() { return attributeOrder; }
	public void setAttributeOrder(int attributeOrder) { this.attributeOrder = attributeOrder; }
}