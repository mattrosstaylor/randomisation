package uk.ac.soton.ecs.lifeguide.randomisation.app;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFileChooser;

import uk.ac.soton.ecs.lifeguide.randomisation.exception.ParticipantLoadException;


public class SaveCSVDialog implements ActionListener{

	private JComponent parent;
	private LocalDBConnector database;
	private TrialChangeDistributor notifier;
	
	public SaveCSVDialog(JComponent parent, LocalDBConnector database, TrialChangeDistributor notifier){
		this.parent = parent;
		this.database = database;
		this.notifier = notifier;
	}
	
	@Override
	public void actionPerformed(ActionEvent event){
		if(event.getID() != ActionEvent.ACTION_PERFORMED)
			return;

		JFileChooser fileBrowser = new JFileChooser(notifier.getCurrentDirectory());

		int result = fileBrowser.showSaveDialog(parent);
		
		if(result == JFileChooser.APPROVE_OPTION){
			String filePath = fileBrowser.getSelectedFile().getPath();
			notifier.setCurrentDirectory(filePath);
			
			if(!filePath.toLowerCase().endsWith(".csv"))
				filePath += ".csv";
			
			String trialName = notifier.getCurrentTrialName();
			if(!database.trialExists(trialName)){
				String errMsg = "You must select a valid trial from the drop-down list above.";
				TrialGUI.errorPanel.showError(errMsg);
				return;
			}
			
			Map<Integer, Integer> allocs = database.getAllocations().get(trialName);
			if(allocs == null || allocs.size() == 0){
				String errMsg = "No participants are currently signed up to this trial.";
				TrialGUI.errorPanel.showError(errMsg);
				return;
			}
			
			try{
				ParticipantWriter.write(filePath, allocs);
			}catch(ParticipantLoadException e){
				String errorMsg = "Could not export: " + e.getMessage();
				TrialGUI.errorPanel.showError(errorMsg);
			}
			
			notifier.distributeEvent();
		}
	}

}
