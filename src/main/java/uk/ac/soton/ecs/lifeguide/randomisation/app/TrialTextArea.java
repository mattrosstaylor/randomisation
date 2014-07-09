package uk.ac.soton.ecs.lifeguide.randomisation.app;

import javax.swing.JTextArea;


public class TrialTextArea implements TrialObserver{

	private JTextArea textArea;
	
	public TrialTextArea(JTextArea textArea){
		this.textArea = textArea;
	}
	
	@Override
	public void notify(String trialName, LocalDBConnector database){
		if(trialName.equals(""))
			textArea.setText("");
		else
			textArea.setText(database.getTrialDefinition(trialName).toString());
	}

}
