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

package com.mucommander.ui.action.impl;

import com.mucommander.ui.action.*;
import com.mucommander.ui.dialog.debug.DebugConsoleDialog;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import java.util.Hashtable;

/**
 * @author Maxence Bernard
 */
public class ShowDebugConsoleAction extends MuAction {

    public ShowDebugConsoleAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        new DebugConsoleDialog(mainFrame).showDialog();
    }


    public static class Factory implements ActionFactory {

		public MuAction createAction(MainFrame mainFrame, Hashtable properties) {
			return new ShowDebugConsoleAction(mainFrame, properties);
		}
    }

    public static class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "ShowDebugConsole";

		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return ActionCategories.MISC; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

		public KeyStroke getDefaultKeyStroke() { return null; }
    }
}
