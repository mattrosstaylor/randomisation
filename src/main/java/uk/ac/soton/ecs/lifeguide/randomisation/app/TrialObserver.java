package uk.ac.soton.ecs.lifeguide.randomisation.app;

public interface TrialObserver{

	public void notify(String trialName, LocalDBConnector database);

}
