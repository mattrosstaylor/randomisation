package uk.ac.soton.ecs.lifeguide.randomisation.app;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;

import uk.ac.soton.ecs.lifeguide.randomisation.TrialDefinition;
import uk.ac.soton.ecs.lifeguide.randomisation.TrialLoader;
import uk.ac.soton.ecs.lifeguide.randomisation.exception.InvalidTrialException;


public class TrialChangeDistributor implements ActionListener{
	
	private JComboBox parent;
	private List<TrialObserver> observers;
	private LocalDBConnector database;
	private String currentDir;
	
	public TrialChangeDistributor(JComboBox parent, LocalDBConnector database){
		this.parent = parent;
		this.observers = new ArrayList<TrialObserver>();
		this.database = database;
		this.currentDir = System.getProperty("user.dir");
	}
	
	public void addObserver(TrialObserver observer){
		observers.add(observer);
	}
	
	@Override
	public void actionPerformed(ActionEvent event){
		if(event.getID() != ActionEvent.ACTION_PERFORMED)
			return;

		String itemName = (String)parent.getSelectedItem();
		
		if(itemName.equals(TrialGUI.NO_SELECTION_STRING)){
			for(TrialObserver observer: observers)
				observer.notify("", database);
		} else if(itemName.equals(TrialGUI.LOAD_TRIAL_STRING)){
			JFileChooser fileBrowser = new JFileChooser(currentDir);

			int result = fileBrowser.showOpenDialog(parent);
			if(result == JFileChooser.APPROVE_OPTION){
				String filePath = fileBrowser.getSelectedFile().getPath();
				TrialDefinition loadedTrial = null;
				
				try{
					loadedTrial = TrialLoader.loadTrial(filePath);
				}catch(InvalidTrialException e){
					TrialGUI.errorPanel.showError(e.getMessage());
					parent.setSelectedIndex(0);
				}
				
				// Check if the loaded trial is already in the list. If not, add it.
				if(loadedTrial != null){
					int index = -1;
					
					boolean found = false;
					for(int i = 0; i < parent.getItemCount(); ++i){
						if(((String)parent.getItemAt(i)).equals(loadedTrial.getTrialName())){
							index = i;
							found = true;
						}
					}
					
					if(!found){
						database.registerTrial(loadedTrial, filePath);
						currentDir = fileBrowser.getCurrentDirectory().getPath();
						parent.insertItemAt(loadedTrial.getTrialName(), parent.getItemCount() - 1);
						index = parent.getItemCount() - 2;
					}
					
					parent.setSelectedIndex(index);
					distributeEvent();
				}
			}
		}
		
		distributeEvent();
	}
	
	public String getCurrentTrialName(){
		String listItem = (String)parent.getSelectedItem();
		
		if(listItem.equals(TrialGUI.NO_SELECTION_STRING) || listItem.equals(TrialGUI.LOAD_TRIAL_STRING))
			listItem = "";
		
		return listItem;
	}

	public void distributeEvent(){
		String trialName = getCurrentTrialName();

		if(!trialName.equals("") && !database.trialExists(trialName)){
			String errMsg = "That trial could not be found. Its specification file may have been moved, or become invalid.";
			TrialGUI.errorPanel.showError(errMsg);
			parent.removeItem(trialName);
			parent.setSelectedIndex(0);
			database.deleteTrial(trialName);
			trialName = "";
		}
		
		for(TrialObserver observer: observers)
			observer.notify(trialName, database);
	}
	
	public String getCurrentDirectory(){
		return currentDir;
	}
	
	public void setCurrentDirectory(String directory){
		this.currentDir = directory;
	}
	
}
