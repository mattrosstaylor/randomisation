package uk.ac.soton.ecs.lifeguide.randomisation.app;

import uk.ac.soton.ecs.lifeguide.randomisation.Attribute;
import uk.ac.soton.ecs.lifeguide.randomisation.Participant;
import uk.ac.soton.ecs.lifeguide.randomisation.Strategy;
import uk.ac.soton.ecs.lifeguide.randomisation.TrialDefinition;
import uk.ac.soton.ecs.lifeguide.randomisation.exception.AllocationException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;


@SuppressWarnings("serial")
public class AllocationForm extends JPanel implements TrialObserver {

    private Map<String, JComboBox> formDropDowns;
    private Map<String, JTextField> formTextFields;

    private TrialDefinition currentTrial;
    private LocalDBConnector database;
    private TrialChangeDistributor notifier;

    public AllocationForm(TrialChangeDistributor notifier) {
        super();
        this.notifier = notifier;
        this.setBackground(Color.white);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.formDropDowns = new HashMap<String, JComboBox>();
        this.formTextFields = new HashMap<String, JTextField>();
    }

    @Override
    public void notify(String trialName, LocalDBConnector database) {
        this.removeAll();
        this.database = database;
        formDropDowns.clear();
        formTextFields.clear();

        if (trialName.equals(""))
            return;

        currentTrial = database.getTrialDefinition(trialName);

        for (Attribute attribute : currentTrial.getAttributes()) {
            JPanel labelArea = new JPanel();
            labelArea.setLayout(new BoxLayout(labelArea, BoxLayout.X_AXIS));
            labelArea.setBackground(Color.white);
            labelArea.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
            JLabel label = new JLabel(attribute.getAttributeName());
            labelArea.add(label);
            this.add(labelArea);

            JPanel entryArea = new JPanel();
            entryArea.setLayout(new BoxLayout(entryArea, BoxLayout.X_AXIS));
            entryArea.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 10));
            entryArea.setBackground(Color.white);

            if (attribute.isRawValue()) {
                JTextField textField = new JTextField();
                int fieldWidth = textField.getFont().getSize() * 15;
                int fieldHeight = textField.getPreferredSize().height;
                textField.setMaximumSize(new Dimension(fieldWidth, fieldHeight));
                entryArea.add(textField);
                formTextFields.put(attribute.getAttributeName(), textField);
            } else {
                JComboBox dropDown = new JComboBox();
                dropDown.addItem("");
                for (int i = 0; i < attribute.getGroupCount(); ++i)
                    dropDown.addItem(Integer.toString(i));

                int boxWidth = dropDown.getFont().getSize() * 15;
                int boxHeight = dropDown.getPreferredSize().height;
                dropDown.setMaximumSize(new Dimension(boxWidth, boxHeight));
                entryArea.add(dropDown);
                formDropDowns.put(attribute.getAttributeName(), dropDown);
            }

            this.add(entryArea);
        }

        if (formDropDowns.size() > 0 || formTextFields.size() > 0) {
            JPanel buttonArea = new JPanel();
            buttonArea.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 0));
            buttonArea.setLayout(new BoxLayout(buttonArea, BoxLayout.X_AXIS));
            buttonArea.setBackground(Color.white);

            JButton clearButton = new JButton("Clear");
            clearButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    clearForm();
                }
            });

            JButton submitButton = new JButton("Submit");
            submitButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    submitForm();
                }
            });

            buttonArea.add(clearButton);
            buttonArea.add(submitButton);

            this.add(buttonArea);
        }

        // Not sure if this is necessary. Seems to work without, but that may be platform-dependent?
        this.getParent().invalidate();
        this.getParent().repaint();
    }

    public void clearForm() {
        for (Attribute attribute : currentTrial.getAttributes()) {
            if (attribute.isRawValue()) {
                JTextField textField = formTextFields.get(attribute.getAttributeName());
                textField.setText("");
            } else {
                JComboBox dropDown = formDropDowns.get(attribute.getAttributeName());
                dropDown.setSelectedIndex(0);
            }
        }
    }

    public void submitForm() {
        Participant participantData = new Participant();
        Map<String, Float> responses = new HashMap<String, Float>();

        // Check for empty fields, or non-numeric values.
        for (String attrName : formTextFields.keySet()) {
            String val = formTextFields.get(attrName).getText();
            if (val.equals("")) {
                String errorMsg = "You must enter a value for the " + attrName + " field.";
                TrialGUI.errorPanel.showError(errorMsg);
                return;
            }
            try {
                responses.put(attrName, Float.parseFloat(val));
            } catch (NumberFormatException e) {
                String errorMsg = "You must enter a numeric value for the " + attrName + " field.";
                TrialGUI.errorPanel.showError(errorMsg);
                return;
            }
        }
        for (String attrName : formDropDowns.keySet()) {
            String val = (String) formDropDowns.get(attrName).getSelectedItem();
            if (val.equals("")) {
                String errorMsg = "You must select value for the " + attrName + " field.";
                TrialGUI.errorPanel.showError(errorMsg);
                return;
            }
            try {
                responses.put(attrName, Float.parseFloat(val));
            } catch (NumberFormatException e) {
                String errorMsg = "You must select a numeric value for the " + attrName + " field.";
                TrialGUI.errorPanel.showError(errorMsg);
                return;
            }
        }

        // Submit the data
        participantData.setId(database.getMaxID(currentTrial.getTrialName()) + 1);
        participantData.setResponses(responses);

        database.addParticipant(currentTrial, participantData);
        int group = 0;
        try {
            group = Strategy.allocate(currentTrial.getTrialName(), participantData.getId(), database);
        } catch (AllocationException e) {
            TrialGUI.errorPanel.showError(e.getMessage());
            return;
        }

        String successMsg = "Participant was allocated to treatment group " + group;
        String dialogTitle = "Allocation Successful";
        int msgType = JOptionPane.INFORMATION_MESSAGE;

        JOptionPane.showMessageDialog(this, successMsg, dialogTitle, msgType);
        clearForm();
        notifier.distributeEvent();
    }

}
