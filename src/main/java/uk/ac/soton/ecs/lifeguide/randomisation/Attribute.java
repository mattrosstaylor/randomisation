package uk.ac.soton.ecs.lifeguide.randomisation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * An object which acts as a basic data store for the various attribute within a {@link TrialDefinition}.
 *
 * @author Liam de Valmency (lpdv1g10@ecs.soton.ac.uk)
 * @author Aleksandar Botev (ab9g10@ecs.soton.ac.uk)
 * @author Dionisio Perez-Mavrogenis (dpm3g10@ecs.soton.ac.uk)
 * @author Kim Svensson (ks6g10@ecs.soton.ac.uk)
 * @since 1.7
 */
public class Attribute {

	private static final Logger logger = LoggerFactory.getLogger(Attribute.class);

	private static final float INT_FLOAT_TOLERANCE = 0.001f;

	private String attributeName;
	private int numGroups;
	private List<Group> ranges;
	private float weight;
	private boolean groupingFactor;

	/**
	 * Constructs an empty Attribute object, with no name, no groups and no weight.
	 */
	public Attribute() {
		ranges = new ArrayList<Group>();
		this.groupingFactor = false;
	}

	/**
	 * Constructs an Attribute object with the specified name, number of groups, and weight.
	 *
	 * @param attributeName    The string ID of the attribute.
	 * @param numGroups        The number of groups this represents (e.g. the 'smokes' attribute would have 2 groups, 'yes' and 'no').
	 * @param weight           The weight of this attribute, for use in minimisation to allow the attribute's value to have a greater
	 *                         influence on the balancing function.
	 * @param isGroupingFactor Whether this attribute acts as a stratifying factor in randomisation, or a balancing factor in
	 *                         minimisation·
	 */
	public Attribute(String attributeName, int numGroups, float weight, boolean isGroupingFactor) {
		this.attributeName = attributeName;
		this.ranges = new ArrayList<Group>();
		this.numGroups = numGroups;
		this.weight = weight;
		this.groupingFactor = isGroupingFactor;
	}

	/**
	 * Constructs an Attribute object with the specified name, set of range values, and weight.
	 *
	 * @param attributeName    The string ID of the attribute.
	 * @param ranges           A list of Group objects, each representing a bounded set of values for the attribute responses (e.g.
	 *                         the 'age' attribute might have ranges <20, 20-50 and >50).
	 * @param weight           The weight of this attribute, for use in minimisation to allow the attribute's value to have a greater
	 *                         influence on the balancing function.
	 * @param isGroupingFactor Whether this attribute acts as a stratifying factor in randomisation, or a balancing factor in
	 *                         minimisation·
	 */
	public Attribute(String attributeName, List<Group> ranges, float weight, boolean isGroupingFactor) {
		this.attributeName = attributeName;
		this.ranges = ranges;
		this.numGroups = ranges.size();
		this.weight = weight;
		this.groupingFactor = isGroupingFactor;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public int getGroupCount() {
		return numGroups;
	}

	public List<Group> getRanges() {
		return ranges;
	}

	public float getWeight() {
		return weight;
	}

	public boolean isGroupingFactor() {
		return groupingFactor;
	}

	/**
	 * Gets the index of the given attribute response, within the list of possible responses for this attribute.
	 *
	 * @param value The participant's response value for this attribute.
	 * @return If the attribute is a raw value attribute (e.g. the responses are banded into ranges of integers),
	 *         this returns the index of that range group within the list of groups. Otherwise, it is assumed that the
	 *         responses are simply indexed from 0 to the number of groups, so the given participant value is converted to
	 *         an integer and returned.
	 */
	public int getGroupIndex(float value) {
		if (ranges.size() == 0) {
			int indexVal = (int) value;
			if (Math.abs(indexVal - value) > INT_FLOAT_TOLERANCE) {
				String warnMsg = "Response for " + attributeName + " is " + value + ", but" +
					"this attribute is grouped.\nValue rounded to group " + indexVal;
				logger.warn(warnMsg);
			}
			return (int) value;
		}

		int index = -1;
		for (int i = 0; i < ranges.size(); ++i) {
			Group g = ranges.get(i);

			// Range is: [lower, upper). So equal to lower range is fine, must be less than upper range.
			if (value >= g.getRangeMin() && value < g.getRangeMax()) {
				index = i;
				break;
			}
		}
		return index;
	}

	/**
	 * Checks whether the attribute is valid for use in a trial.
	 *
	 * @return The validity of the attribute within the context of a trial. To be considered valid, the
	 *         attribute must have a non-empty, non-null name, and at least one group.
	 */
	public boolean isValid() {
		return attributeName != null && !attributeName.equals("") && numGroups > 0;
	}

	/**
	 * @return Whether or not the attribute represents a raw-value response (e.g. 'age' response would be the
	 *         number of years, banded into groups), or simply a list of groups (e.g. 'smokes' would have two groups, 'yes'
	 *         and 'no').
	 */
	public boolean isRawValue() {
		return ranges.size() != 0;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public void setRanges(List<Group> ranges) {
		this.ranges = ranges;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

	public void setGroupingFactor(boolean groupingFactor) {
		this.groupingFactor = groupingFactor;
	}

	private String readableBoolean(boolean val) {
		return val ? "yes" : "no";
	}

	// For nicer output when printing/debugging.
	public String toString() {
		String output = attributeName + ": " + numGroups + " groups";
		if (ranges.size() > 0) {
			output += " (";
			for (int i = 0; i < ranges.size(); ++i) {
				output += ranges.get(i);
				if (i != ranges.size() - 1)
					output += ", ";
			}
			output += ")";
		}
		output += ", Weight: " + weight + ", Stratification factor?: " + readableBoolean(groupingFactor);
		return output;
	}
}
