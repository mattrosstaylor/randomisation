package uk.ac.soton.ecs.lifeguide.randomisation.app;

import uk.ac.soton.ecs.lifeguide.randomisation.Participant;
import uk.ac.soton.ecs.lifeguide.randomisation.ParticipantGenerator;
import uk.ac.soton.ecs.lifeguide.randomisation.Strategy;
import uk.ac.soton.ecs.lifeguide.randomisation.exception.AllocationException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class AllocationDialogBox implements ActionListener, TrialObserver {

	private JComponent parent;
	private LocalDBConnector database;
	private String currentTrial;
	private TrialChangeDistributor notifier;

	public AllocationDialogBox(JComponent parent, TrialChangeDistributor notifier) {
		this.parent = parent;
		this.currentTrial = "";
		this.notifier = notifier;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getID() != ActionEvent.ACTION_PERFORMED)
			return;

		int result = -1;
		try {
			String msg = "How many participants would you like to randomly generate and allocate?";
			String input = JOptionPane.showInputDialog(parent, msg);

			// Cancel button was pressed.
			if (input == null || input.equals(""))
				return;

			result = Integer.parseInt(input);
		} catch (NumberFormatException e) {
			String errMsg = "To allocate participants, you must specify a total in numeric format.";
			TrialGUI.errorPanel.showError(errMsg);
			return;
		}

		if (currentTrial == null || currentTrial.equals("") || !database.trialExists(currentTrial)) {
			String errMsg = "You must select a valid trial from the drop-down list above.";
			TrialGUI.errorPanel.showError(errMsg);
			return;
		}

		if (result > 0 && database != null) {
			ParticipantGenerator.setStartID(database.getMaxID(currentTrial) + 1);
			for (int i = 0; i < result; ++i) {
				Participant testParticipant = ParticipantGenerator.generate(database.getTrialDefinition(currentTrial));
				database.addParticipant(database.getTrialDefinition(currentTrial), testParticipant);
				try {
					Strategy.allocate(currentTrial, testParticipant.getId(), database);
				} catch (AllocationException e) {
					TrialGUI.errorPanel.showError(e.getMessage());
					return;
				}
				notifier.distributeEvent();
			}
		}
	}

	@Override
	public void notify(String trialName, LocalDBConnector database) {
		this.currentTrial = trialName;
		this.database = database;
	}

}
