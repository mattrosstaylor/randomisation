package uk.ac.soton.ecs.lifeguide.randomisation.app;

import java.util.Map;
import java.util.TreeMap;

import javax.swing.JTextArea;

import uk.ac.soton.ecs.lifeguide.randomisation.TrialDefinition;


public class AllocationTextArea implements TrialObserver{

	private JTextArea textArea;
	
	public AllocationTextArea(JTextArea textArea){
		this.textArea = textArea;
	}
	
	@Override
	public void notify(String trialName, LocalDBConnector database){
		// Empty the text area for non-valid trials.
		if(trialName.equals("")){
			textArea.setText("");
			return;
		}
		
		// Get the allocation map (participant ID -> allocation) for this trial.
		Map<Integer, Integer> allocations = database.getAllocations().get(trialName);

		if(allocations == null){
			textArea.setText("No participants in this trial.\n");
			return;
		}
		
		// For each allocation, keep a running total in a map (treatment group -> total allocated in this group).
		Map<Integer, Integer> allocationCount = new TreeMap<Integer, Integer>();
		for(Integer group: allocations.values()){
			Integer runningTotal = allocationCount.get(group);
			if(runningTotal == null)
				allocationCount.put(group, 1);
			else
				allocationCount.put(group, runningTotal + 1);
		}
		
		// Output the totals for each treatment group.
		String outputString = allocations.size() + " participants in this trial.\n\n";
		for(Integer group: allocationCount.keySet()){
			outputString += "Treatment group " + group + " total: " + allocationCount.get(group) + "\n";
		}
		outputString += "\n";

		// Get the per-treatment allocations for each stratified group (further data breakdown).
		// Map is from stratified group to a map of: treatment group -> allocation total.
		Map<Integer, Map<Integer, Integer>> stratGroupCount = new TreeMap<Integer, Map<Integer, Integer>>();
		for(Integer group: allocations.keySet()){
			int stratGroup = database.getStratifiedGroup(group);
			int allocation = allocations.get(group);
			
			if(stratGroupCount.get(stratGroup) == null)
				stratGroupCount.put(stratGroup, new TreeMap<Integer, Integer>());

			Map<Integer, Integer> treatmentTotals = stratGroupCount.get(stratGroup);
			Integer total = treatmentTotals.get(allocation);
			if(total == null)
				treatmentTotals.put(allocation, 1);
			else
				treatmentTotals.put(allocation, total + 1);
		}
		
		TrialDefinition currentTrial = database.getTrialDefinition(trialName);
		for(Integer stratGroup: stratGroupCount.keySet()){
			outputString += "Participants in " + currentTrial.getStratificationString(stratGroup) + ":\n";

			Map<Integer, Integer> treatmentTotals = stratGroupCount.get(stratGroup);
			for(Integer treatmentID: treatmentTotals.keySet()){
				outputString += "Treatment group " + treatmentID + ": " + treatmentTotals.get(treatmentID) + "\n";
			}
			
			outputString += "\n";
		}
		
		textArea.setText(outputString);
	}

}
