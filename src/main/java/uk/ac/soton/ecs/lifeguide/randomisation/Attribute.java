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

	@Column(name="grouping_factor")
	private boolean groupingFactor;

	@OneToMany(mappedBy="attribute", cascade = {CascadeType.ALL})
	private List<Grouping> groupings = new ArrayList<Grouping>();

	@Column(name="number_of_groups")
	private int numberOfGroups;

	@Column(name="attribute_order")
	int attributeOrder;

	private static final double INT_DOUBLE_TOLERANCE = 0.001;

	/* constructors */

	public Attribute() {
			groupings = new ArrayList<Grouping>();
			this.groupingFactor = false;
	}

	public Attribute(String name, int numberOfGroups, double weight, boolean isGroupingFactor) {
		this.name = name;
		this.groupings = new ArrayList<Grouping>();
		this.numberOfGroups = numberOfGroups;
		this.weight = weight;
	}

	public Attribute(String name, List<Grouping> groupings, double weight, boolean isGroupingFactor) {
		this.name = name;
		this.groupings = groupings;
		this.numberOfGroups = groupings.size();
		this.weight = weight;
		this.groupingFactor = isGroupingFactor;

		// mrt - add the back references to the attribute from the grouping
		for (Grouping g: groupings) {
			g.setAttribute(this);
		}
	}

	/* methods */

	/**
	 * Checks whether the attribute is valid for use in a trial.
	 *
	 * @return The validity of the attribute within the context of a trial. To be considered valid, the
	 *         attribute must have a non-empty, non-null name, and at least one group.
	 */
	public boolean isValid() {
		return name != null && !name.equals("") && numberOfGroups > 0;
	} // mrt - this function probably shouldn't exist

	/**
	 * Gets the index of the given attribute response, within the list of possible responses for this attribute.
	 *
	 * @param value The participant's response value for this attribute.
	 * @return If the attribute is a raw value attribute (e.g. the responses are banded into ranges of integers),
	 *         this returns the index of that range group within the list of groups. Otherwise, it is assumed that the
	 *         responses are simply indexed from 0 to the number of groups, so the given participant value is converted to
	 *         an integer and returned.
	 */
	public String getGroupingNameForValue(double value) {
		// mrt - this is completely broken
		if (groupings.size() == 0) {
			int indexVal = (int) value;
			if (Math.abs(indexVal - value) > INT_DOUBLE_TOLERANCE) {
				String warnMsg = "Response for " + name + " is " + value + ", but" + "this attribute is grouped.\nValue rounded to group " + indexVal;
				//logger.warn(warnMsg);
			}
			return Double.toString(value);
		}

		// mrt - this bit is fiiiiiiiiiine
		for (Grouping g : groupings) {
			// Range is: [lower, upper). So equal to lower range is fine, must be less than upper range.
			if (value >= g.getMinimum() && value < g.getMaximum()) {
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

	public boolean isGroupingFactor() { return groupingFactor; }
	public void setGroupingFactor(boolean groupingFactor) { this.groupingFactor = groupingFactor; }

	public List<Grouping> getGroupings() { return groupings; }
	public void setRanges(List<Grouping> groupings) { this.groupings = groupings; }

	public int getNumberOfGroups() { return numberOfGroups; }
	public void setNumberOfGroups(int numberOfGroups) { this.numberOfGroups = numberOfGroups; }

	public int getAttributeOrder() { return attributeOrder; }
	public void setAttributeOrder(int attributeOrder) { this.attributeOrder = attributeOrder; }
}