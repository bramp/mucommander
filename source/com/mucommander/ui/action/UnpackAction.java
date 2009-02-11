/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2009 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.ui.action;

import com.mucommander.file.filter.AttributeFileFilter;
import com.mucommander.file.filter.OrFileFilter;
import com.mucommander.file.util.FileSet;
import com.mucommander.ui.dialog.file.UnpackDialog;
import com.mucommander.ui.main.MainFrame;

import java.util.Hashtable;

/**
 * This action pops up the 'Unpack files' dialog that allows to unpack the currently marked files.
 *
 * @author Maxence Bernard
 */
public class UnpackAction extends SelectedFilesAction implements InvokesDialog {

    public UnpackAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);

        // Unpack job operates on archives and directories
        OrFileFilter filter = new OrFileFilter();
        filter.addFileFilter(new AttributeFileFilter(AttributeFileFilter.ARCHIVE));
        filter.addFileFilter(new AttributeFileFilter(AttributeFileFilter.DIRECTORY));
        setSelectedFileFilter(filter);
    }

    public void performAction() {
        FileSet files = mainFrame.getActiveTable().getSelectedFiles();
        if(files.size()>0)
            new UnpackDialog(mainFrame, files, false);
    }
}
