package com.mucommander.ui.action;

import com.mucommander.file.util.FileSet;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.PropertiesDialog;

import java.util.Hashtable;

/**
 * This action pops up the file Properties dialog.
 *
 * @author Maxence Bernard
 */
public class ShowFilePropertiesAction extends SelectedFilesAction {

    public ShowFilePropertiesAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        FileSet files = mainFrame.getActiveTable().getSelectedFiles();
        if(files.size()>0)
            new PropertiesDialog(mainFrame, files).showDialog();
    }
}