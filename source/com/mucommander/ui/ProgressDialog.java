
package com.mucommander.ui;

import com.mucommander.job.FileJob;
import com.mucommander.job.ExtendedFileJob;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.comp.progress.OverlayProgressBar;
import com.mucommander.ui.comp.button.ButtonChoicePanel;
import com.mucommander.ui.comp.dialog.FocusDialog;
import com.mucommander.ui.comp.dialog.EscapeKeyAdapter;
import com.mucommander.text.SizeFormatter;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * This dialog informs the user of the progress made by a Job.
 */
public class ProgressDialog extends FocusDialog implements Runnable, ActionListener, KeyListener {
    private JLabel infoLabel;
    private JLabel statsLabel;
    private OverlayProgressBar totalProgressBar;
    private OverlayProgressBar fileProgressBar;
    private JButton cancelButton;
    private JButton hideButton;

    private FileJob job;    
    private Thread repaintThread;
    /* True if the current job is a MulitipleFileJob */
    private boolean dualBar;

	// Dialog width is constrained to 320, height is not an issue (always the same)
	private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(320,10000);	
	private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320,0);	

	/** How often should progress information be refreshed (in ms) */
	private final static int REFRESH_RATE = 500;

    private MainFrame mainFrame;

    public ProgressDialog(MainFrame mainFrame, String title) {
        super(mainFrame, title, mainFrame);

        this.mainFrame = mainFrame;

		// Sets maximum and minimum dimensions for this dialog
		setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
		setMinimumSize(MINIMUM_DIALOG_DIMENSION);
    }
    
    
    public void start(FileJob job) {
        this.job = job;
        this.dualBar = job instanceof ExtendedFileJob;
        initUI();
        
        repaintThread = new Thread(this);
        repaintThread.start();

    	showDialog();
	}


    private void initUI() {
        Container contentPane = getContentPane();

        totalProgressBar = new OverlayProgressBar();
		totalProgressBar.setAlignmentX(LEFT_ALIGNMENT);
        infoLabel = new JLabel(job.getStatusString());
		infoLabel.setAlignmentX(LEFT_ALIGNMENT);
		
		JPanel tempPanel = new JPanel();
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.Y_AXIS));
		// 2 progress bars
		if (dualBar) {
        	tempPanel.add(infoLabel);
        	fileProgressBar = new OverlayProgressBar();
			fileProgressBar.setAlignmentX(LEFT_ALIGNMENT);
			tempPanel.add(fileProgressBar);
			tempPanel.add(Box.createRigidArea(new Dimension(0, 15)));
		
			statsLabel = new JLabel("Transfer starting...");
			tempPanel.add(statsLabel);
			tempPanel.add(totalProgressBar);
		}	
		// Single progress bar
		else {
			tempPanel.add(infoLabel);
			tempPanel.add(totalProgressBar);
		}

		tempPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		tempPanel.add(Box.createVerticalGlue());
		contentPane.add(tempPanel, BorderLayout.CENTER);
        
		cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        cancelButton.addKeyListener(this);
		hideButton = new JButton("Hide");
		hideButton.addActionListener(this);
		hideButton.addKeyListener(this);
		// Cancel button receives initial focus
		setInitialFocusComponent(cancelButton);
		// Enter triggers cancel button
		getRootPane().setDefaultButton(cancelButton);
		contentPane.add(new ButtonChoicePanel(new JButton[] {cancelButton, hideButton}, 0, getRootPane()), BorderLayout.SOUTH);
    }

    
    public void run() {
	    // Used for dual bars
		int filePercent;
    	int lastFilePercent;

        int totalPercent;
        int lastTotalPercent;

        String currentInfo;
        String lastInfo;

		long nbBytesTotal;
		long lastBytesTotal = 0;
        
        totalPercent = lastTotalPercent = -1;
        filePercent = lastFilePercent = -1;
        currentInfo = lastInfo = "";

        ExtendedFileJob extendedJob = null;
        if(dualBar)
            extendedJob = (ExtendedFileJob)job;

		long speed;
		long startTime = job.getStartTime();
		long now;
		while(repaintThread!=null && !job.hasFinished()) {
	        if (dualBar) {
				// Updates fileProgressBar if necessary
				filePercent = extendedJob.getFilePercentDone(); 
		        if(lastFilePercent!=filePercent) {
		            // Updates file progress bar
                    fileProgressBar.setValue(filePercent);
                    fileProgressBar.setTextOverlay(filePercent+"%");
		            fileProgressBar.repaint(REFRESH_RATE);

		            lastFilePercent = filePercent;
                }

				// Update stats if necessary
				nbBytesTotal = job.getTotalBytesProcessed();
				if(lastBytesTotal!=nbBytesTotal) {
					now = System.currentTimeMillis();
					speed = (long)(nbBytesTotal/((now-startTime)/(double)1000));
//System.out.println(nbBytesTotal+" "+((now-startTime)/1000));
					statsLabel.setText("Transferred "+SizeFormatter.format(nbBytesTotal, SizeFormatter.DIGITS_MEDIUM|SizeFormatter.UNIT_LONG|SizeFormatter.ROUND_TO_KB)+" at "+SizeFormatter.format(speed, SizeFormatter.DIGITS_MEDIUM|SizeFormatter.UNIT_SHORT|SizeFormatter.ROUND_TO_KB)+"/s");
					statsLabel.repaint(REFRESH_RATE);
	
					lastBytesTotal = nbBytesTotal;
				}
			}
        	
		
            // Updates totalProgressBar if necessary
            totalPercent = job.getTotalPercentDone();
			if(lastTotalPercent!=totalPercent) {
				// Updates total progress bar 
                totalProgressBar.setValue(totalPercent);
				totalProgressBar.setTextOverlay(totalPercent+"% ");
                totalProgressBar.repaint(REFRESH_RATE);
					            
                lastTotalPercent = totalPercent;
            }

            
            // Updates infoLabel if necessary 
            currentInfo = job.getStatusString();
            if(!lastInfo.equals(currentInfo)) {
                infoLabel.setText(currentInfo);
                infoLabel.repaint(REFRESH_RATE);
                lastInfo = currentInfo;
            }

            try { Thread.sleep(REFRESH_RATE); }
            catch(InterruptedException e) {}
        }
	
        dispose();
	}

    public void actionPerformed(ActionEvent e) {
	    Object source = e.getSource();
    	
		if (source==cancelButton) {
			// Cancel button pressed, let's stop deleting
	        job.stop();
	        repaintThread = null;
	        dispose();
		}
		else if(source==hideButton) {
			mainFrame.setState(Frame.ICONIFIED);
		}
    }


    /***********************
     * KeyListener methods *
     ***********************/

     public void keyPressed(KeyEvent e) {
     	int keyCode = e.getKeyCode();
		
     	// Disposes the dialog on escape key
     	if (keyCode==KeyEvent.VK_ESCAPE) {
     		job.stop();
			dispose();
     	}
     }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }
}
