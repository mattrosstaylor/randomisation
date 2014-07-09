package uk.ac.soton.ecs.lifeguide.randomisation.app;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.event.MouseInputAdapter;


@SuppressWarnings("serial")
public class ErrorPanel extends JPanel{
	
	private JFrame parent;
	private JLabel errorLabel;
	private JTextArea textArea;
	private boolean showing;
	
	public ErrorPanel(JFrame parent){
		super();
		
		this.parent = parent;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		this.showing = false;
		
		JPanel closePanel = new JPanel();
		closePanel.setLayout(new BoxLayout(closePanel, BoxLayout.X_AXIS));
		errorLabel = new JLabel("Error (click to remove):");
		errorLabel.setFont(AppFonts.tooltipFont);
		closePanel.add(errorLabel);
		this.add(closePanel);
		
		this.textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setBackground(new Color(255, 90, 90));
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		textArea.setFont(AppFonts.errorFont);
		this.add(textArea);

		// Add the click-remove listener to both the panel and text area
		ErrorRemoveClickListener removeListener = new ErrorRemoveClickListener();
		this.addMouseListener(removeListener);
		textArea.addMouseListener(removeListener);
		errorLabel.addMouseListener(removeListener);
	}

	public void showError(String errorMsg){
		textArea.setText(errorMsg);

		this.setMaximumSize(new Dimension(Integer.MAX_VALUE, textArea.getHeight() + errorLabel.getHeight()));
		
		if(!showing)
			parent.add(this);
		
		this.showing = true;
		
		parent.invalidate();
		parent.repaint();
	}
	
	public void hideError(){
		this.showing = false;
		
		parent.remove(this);
		parent.invalidate();
	}
	
	private class ErrorRemoveClickListener extends MouseInputAdapter{
		@Override
		public void mouseClicked(MouseEvent event){
			if(event.getButton() == MouseEvent.BUTTON1)
				hideError();
		}
	}

}
