package uk.ac.soton.ecs.lifeguide.randomisation.app;

import javax.swing.JComponent;


public class FocusGrabber implements TrialObserver{

	private JComponent component;
	
	public FocusGrabber(JComponent component){
		this.component = component;
	}
	
	@Override
	public void notify(String trialName, LocalDBConnector database){
		component.requestFocusInWindow();
	}
	
}
