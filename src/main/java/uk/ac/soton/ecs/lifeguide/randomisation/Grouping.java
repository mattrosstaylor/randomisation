package uk.ac.soton.ecs.lifeguide.randomisation;

import javax.persistence.*;

@Entity
@Table(name = "groupings")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="type", discriminatorType=DiscriminatorType.STRING)
@DiscriminatorValue("discrete")
public class Grouping {

	@Id @GeneratedValue
	@Column(name="id")
	private int id;

	@ManyToOne
	@JoinColumn(name="attribute_id")
	private Attribute attribute;

	@Column(name="name")
	private String name;

	public Grouping() {
	}

	public Grouping(String name) {
		this.name = name;
	}

	/* getters and setters */

	public int getId() { return id;}
	public void setId(int id) { this.id = id; }

	public Attribute getAttribute() { return attribute; }
	public void setAttribute(Attribute attribute) { this.attribute = attribute; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	/* methods */

	public boolean inGrouping(String s){
		return name.equals(s);
	}

	public String getValidValue() {
		return name;
	}
}