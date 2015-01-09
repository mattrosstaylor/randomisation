package uk.ac.soton.ecs.lifeguide.randomisation;

import javax.persistence.*;

@Entity
@DiscriminatorValue("continuous")
public class RangeStratum extends Stratum {

	@Column(name="minimum")
	private double minimum;

	@Column(name="maximum")
	private double maximum;

	public static final double DEFAULT_MAX = Double.MAX_VALUE;
	public static final double DEFAULT_MIN = -Double.MAX_VALUE;
	
	/* constructors */

	public RangeStratum() {
	}

	private static String generateName(double minimum, double maximum){
		boolean setMin = minimum > DEFAULT_MIN,
				setMax = maximum < DEFAULT_MAX;

		if (setMin){
			if (setMax){
				return Double.toString(minimum) + " to " + Double.toString(maximum);
			}
			else {
				return "> " + Double.toString(minimum);
			}
		}
		else if (setMax){
			return "< " + Double.toString(maximum);
		}

		return "any";
	}

	public RangeStratum(double minimum, double maximum) {
		super(RangeStratum.generateName(minimum, maximum));
		this.minimum = minimum;
		this.maximum = maximum;
	}

	/* getters and setters */

	public double getMinimum() { return minimum; }
	public void setMinimum(double minimum) { this.minimum = minimum; }

	public double getMaximum() { return maximum; }
	public void setMaximum(double maximum) { this.maximum = maximum; }

	/* methods */

	public boolean inStratum(String s){
		try {
			double val = Double.parseDouble(s);
			return val < this.maximum && val >= this.minimum;
		}
		catch (NumberFormatException e) {
			return false;
		}
	}
}
