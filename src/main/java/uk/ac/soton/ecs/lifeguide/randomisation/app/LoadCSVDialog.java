package uk.ac.soton.ecs.lifeguide.randomisation.app;

import uk.ac.soton.ecs.lifeguide.randomisation.Participant;
import uk.ac.soton.ecs.lifeguide.randomisation.Strategy;
import uk.ac.soton.ecs.lifeguide.randomisation.exception.AllocationException;
import uk.ac.soton.ecs.lifeguide.randomisation.exception.ParticipantLoadException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;


public class LoadCSVDialog implements ActionListener {

    private JComponent parent;
    private LocalDBConnector database;
    private TrialChangeDistributor notifier;

    public LoadCSVDialog(JComponent parent, LocalDBConnector database, TrialChangeDistributor notifier) {
        this.parent = parent;
        this.database = database;
        this.notifier = notifier;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getID() != ActionEvent.ACTION_PERFORMED)
            return;

        JFileChooser fileBrowser = new JFileChooser(notifier.getCurrentDirectory());

        int result = fileBrowser.showOpenDialog(parent);

        if (result == JFileChooser.APPROVE_OPTION) {
            String filePath = fileBrowser.getSelectedFile().getPath();
            notifier.setCurrentDirectory(filePath);

            String trialName = notifier.getCurrentTrialName();
            if (!database.trialExists(trialName)) {
                String errMsg = "You must select a valid trial from the drop-down list above.";
                TrialGUI.errorPanel.showError(errMsg);
                return;
            }

            List<Participant> participants = null;

            try {
                participants = ParticipantLoader.load(filePath);
            } catch (ParticipantLoadException e) {
                TrialGUI.errorPanel.showError(e.getMessage() + " Participants have not been allocated.");
            }

            for (Participant participant : participants) {
                // Register the participant
                database.addParticipant(database.getTrialDefinition(trialName), participant);

                // Allocate them
                try {
                    Strategy.allocate(notifier.getCurrentTrialName(), participant.getId(), database);
                } catch (AllocationException e) {
                    TrialGUI.errorPanel.showError(e.getMessage());
                    return;
                }
            }

            notifier.distributeEvent();
        }
    }

}
