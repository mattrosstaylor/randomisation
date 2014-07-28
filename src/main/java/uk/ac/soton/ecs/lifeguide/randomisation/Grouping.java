package uk.ac.soton.ecs.lifeguide.randomisation;

import javax.persistence.*;

@Entity
@Table(name = "groupings")
@Embeddable
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
	private float minimum;

	@Column(name="maximum")
	private float maximum;
	
	@Column(name="grouping_order")
	int groupingOrder;
	
	/* constructors */

	public Grouping(String name, float minimum, float maximum) {
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

	public float getMinimum() { return minimum; }
	public void setMinimum(float minimum) { this.minimum = minimum; }

	public float getMaximum() { return maximum; }
	public void setMaximum(float maximum) { this.maximum = maximum; }

	public int getGroupingOrder() { return groupingOrder; }
	public void setGroupingOrder(int groupingOrder) { this.groupingOrder = groupingOrder; }
}