package uk.ac.soton.ecs.lifeguide.randomisation.app;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;


public class TrialDeleteButton implements ActionListener{

	private JComboBox trialList;
	private LocalDBConnector database;
	private TrialChangeDistributor notifier;
	
	public TrialDeleteButton(JComboBox trialList, LocalDBConnector database, TrialChangeDistributor notifier){
		this.trialList = trialList;
		this.database = database;
		this.notifier = notifier;
	}
	
	@Override
	public void actionPerformed(ActionEvent event){
		String currentTrial = (String)trialList.getSelectedItem();
		
		if(currentTrial.equals(TrialGUI.NO_SELECTION_STRING) || currentTrial.equals(TrialGUI.LOAD_TRIAL_STRING))
			return;
		
		String confirmMsg = "Are you sure you wish to delete the '" + currentTrial + "' trial?";
		String title = "Confirm Deletion";
		
		int result = JOptionPane.showConfirmDialog(trialList, confirmMsg, title, JOptionPane.YES_NO_OPTION);
		
		if(result == JOptionPane.YES_OPTION){
			trialList.removeItem(currentTrial);
			trialList.setSelectedIndex(0);
			database.deleteTrial(currentTrial);
			notifier.distributeEvent();
		}
	}

}
