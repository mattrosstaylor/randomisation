package uk.ac.soton.ecs.lifeguide.randomisation;

import javax.persistence.*;

@Entity
@Table(name = "groupings")
public class Grouping {

	@Id @GeneratedValue
	@Column(name="id")
	private int id;

	@ManyToOne
	@JoinColumn(name="attribute_id")
	private Attribute attribute;

	@Column(name="name")
	private String name;

	@Column(name="minimum")
	private double minimum;

	@Column(name="maximum")
	private double maximum;
	
	@Column(name="grouping_order")
	int groupingOrder;
	
	/* constructors */

	public Grouping(String name, double minimum, double maximum) {
		this.name = name;
		this.minimum = minimum;
		this.maximum = maximum;
	}

	/* getters and setters */

	public int getId() { return id;}
	public void setId(int id) { this.id = id; }

	public Attribute getAttribute() { return attribute; }
	public void setAttribute(Attribute attribute) { this.attribute = attribute; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public double getMinimum() { return minimum; }
	public void setMinimum(double minimum) { this.minimum = minimum; }

	public double getMaximum() { return maximum; }
	public void setMaximum(double maximum) { this.maximum = maximum; }

	public int getGroupingOrder() { return groupingOrder; }
	public void setGroupingOrder(int groupingOrder) { this.groupingOrder = groupingOrder; }
}