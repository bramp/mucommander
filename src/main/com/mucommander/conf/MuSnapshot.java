/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

package com.mucommander.conf;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.List;

import javax.swing.JSplitPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.conf.Configuration;
import com.mucommander.commons.conf.ConfigurationException;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.WindowManager;
import com.mucommander.ui.main.table.Column;
import com.mucommander.ui.main.table.FileTable;
import com.mucommander.ui.main.tabs.FileTableTab;

/**
 * muCommander specific wrapper for the <code>com.mucommander.conf</code> API which is used to save 'dynamic' configurations.
 * 'dynamic' configurations refer to properties that represent the state of the last running instance which is not set from the 
 * preferences dialog. those properties are changed often.
 * 
 * @author Arik Hadas
 */
public class MuSnapshot {
	private static final Logger LOGGER = LoggerFactory.getLogger(MuSnapshot.class);
	
	// - Last screen variables -----------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing last known screen properties. */
    private static final String SCREEN_SECTION                     = "screen";
    /** Last known screen width. */
    public static final String  SCREEN_WIDTH                       = SCREEN_SECTION + "." + "width";
    /** Last known screen height. */
    public static final String  SCREEN_HEIGHT                      = SCREEN_SECTION + "." + "height";
    
	// - Last windows variables ----------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing known information about last muCommander windows. */
    private static final String WINDOWS_SECTION                    = "windows";
    /** Describes the number of windows that were open. */
    public static final String  WINDOWS_COUNT                      = WINDOWS_SECTION + '.' + "count";
    /** Describes the index of the selected window. */
    public static final String  WINDOWS_SELECTION                  = WINDOWS_SECTION + '.' + "selection";
    /** Subsection describing information which is specific to a particular window. */
    private static final String WINDOW                             = "window";
    
    // - Window variables ----------------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing known general information of muCommander window. */
    private static final String WINDOW_PROPERTIES_SECTION          = "window_properties";
    /** Describes the window's horizontal position. */
    private static final String X                                  = "x";
    /** Describes the window's vertical position. */
    private static final String Y                                  = "y";
    /** Describes the window's width. */
    private static final String WIDTH                              = "width";
    /** Describes the window's height. */
    private static final String HEIGHT                             = "height";
    /** Describes the orientation used to split folder panels. */
    private static final String SPLIT_ORIENTATION                  = "split_orientation";
    /** Vertical split pane orientation. */
    public static final String  VERTICAL_SPLIT_ORIENTATION         = "vertical";
    /** Horizontal split pane orientation. */
    public static final String  HORIZONTAL_SPLIT_ORIENTATION       = "horizontal";
    /** Default split pane orientation. */
    public static final String  DEFAULT_SPLIT_ORIENTATION          = VERTICAL_SPLIT_ORIENTATION;
    
    // - Panels variables ----------------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing the dynamic information contained in the folder panels */
    private static final String  PANELS_SECTION                    = "panels";
    /** Identifier of the left panel. */
    private static final String  LEFT                              = "left";
    /** Identifier of the right panel. */
    private static final String  RIGHT                             = "right";
    
    // - Tree variables ------------------------------------------------------
    // -----------------------------------------------------------------------
    /** Subsection describing the tree view CONFIGURATION. */
    private static final String  TREE_SECTION                      = "tree";
    /** Describes whether the tree is visible */
    private static final String  TREE_VISIBLE                      = "visible";
    /** Describes the tree's width */
    private static final String  TREE_WIDTH                        = "width";
    
    // - File Table variables ------------------------------------------------
    // -----------------------------------------------------------------------
    /** Subsection describing the folders view CONFIGURATION. */
    private static final String  FILE_TABLE_SECTION                = "file_table";
    /** Describes an ascending sort order. */
    public static final String  SORT_ORDER_ASCENDING               = "asc";
    /** Describes a descending sort order. */
    public static final String  SORT_ORDER_DESCENDING              = "desc";
    /** Default 'sort order' column for the file table. */
    public static final String  DEFAULT_SORT_ORDER                 = SORT_ORDER_ASCENDING;
    /** Name of the 'show column' variable. */
    private static final String SHOW_COLUMN                        = "show";
    /** Name of the 'column position' variable. */
    private static final String COLUMN_POSITION                    = "position";
    /** Name of the 'column width' variable. */
    private static final String COLUMN_WIDTH                       = "width";
    /** Default 'sort by' column for the file table. */
    public static final String  DEFAULT_SORT_BY                    = "name";
    /** Identifier of the sort section in a file table's CONFIGURATION. */
    private static final String SORT                               = "sort";
    /** Identifier of the sort criteria in a file table's CONFIGURATION. */
    private static final String SORT_BY                            = "by";
    /** Identifier of the sort order in a file table's CONFIGURATION. */
    private static final String SORT_ORDER                         = "order";
    
    // - Tabs variables ------------------------------------------------------
    // -----------------------------------------------------------------------
    /** Subsection describing the tabs CONFIGURATION. */
    private static final String TABS_SECTION                       = "tabs";
    /** Describes the number of tabs presented in the panel */
    private static final String TABS_COUNT                         = "count";
    /** Subsection describing information which is specific to a particular tab */
    private static final String TAB                                = "tab";
    /** Describes the location presented in a tab */
    private static final String TAB_LOCATION                       = "location";
    
    
	/** Cache the screen's size. this value isn't computed during the shutdown process since it cause a deadlock then */
	private Dimension screenSize;
	
	public static String getSelectedWindow() {
		return WINDOWS_SELECTION;
	}
	
	private static String getWindowSection(int index) {
        return WINDOWS_SECTION + "." + WINDOW + "-" + index; 
    }
	
	private static String getWindowPropertiesSection(int window) {
		return getWindowSection(window) + "." + WINDOW_PROPERTIES_SECTION;
	}

	public static String getX(int window) {
    	return getWindowPropertiesSection(window) + "." + X;
    }
    
    public static String getY(int window) {
    	return getWindowPropertiesSection(window) + "." + Y;
    }
    
    public static String getWidth(int window) {
    	return getWindowPropertiesSection(window) + "." + WIDTH;
    }
    
    public static String getHeight(int window) {
    	return getWindowPropertiesSection(window) + "." + HEIGHT;
    }
    
    public static String getSplitOrientation(int window) {
    	return getWindowPropertiesSection(window) + "." + SPLIT_ORIENTATION;
    }

	/**
     * Returns the CONFIGURATION section corresponding to the specified {@link com.mucommander.ui.main.FolderPanel},
     * left or right one in the window at the given index.
     *
     * @param left true for the left FolderPanel, false for the right one
     * @return the CONFIGURATION section corresponding to the specified FolderPanel
     */
	private static String getFolderPanelSection(int window, boolean left) {
        return getWindowSection(window) + "." + PANELS_SECTION + "." + (left?LEFT:RIGHT);
    }
	
	/**
     * Returns the CONFIGURATION section corresponding to the specified {@link com.mucommander.ui.main.FoldersTreePanel},
     * left or right one in the window at the given index.
     *
     * @param left true for the left FoldersTreePanel, false for the right one
     * @return the CONFIGURATION section corresponding to the specified FoldersTreePanel
     */
	private static String getTreeSection(int window, boolean left) {
    	return getFolderPanelSection(window, left) + "." + TREE_SECTION;
    }
    
	/**
     * Returns the variable that controls the visibility of the tree view, in the left or right
     * {@link com.mucommander.ui.main.FolderPanel}.
     *
     * @param left true for the left FolderPanel, false for the right one
     * @return the variable that controls the visibility of the tree view in the specified FolderPanel
     */
    public static String getTreeVisiblityVariable(int window, boolean left) {
    	return getTreeSection(window, left) + "." + TREE_VISIBLE;
    }
    
    /**
     * Returns the variable that holds the width of the tree view, in the left or right
     * {@link com.mucommander.ui.main.FolderPanel}.
     *
     * @param left true for the left FolderPanel, false for the right one
     * @return the variable that holds the width of the tree view in the specified FolderPanel
     */
    public static String getTreeWidthVariable(int window, boolean left) {
    	return getTreeSection(window, left) + "." + TREE_WIDTH;
    }
	
    /**
     * Returns the CONFIGURATION section corresponding to the specified {@link com.mucommander.ui.main.table.FileTable},
     * left or right one.
     *
     * @param left true for the left FileTable, false for the right one
     * @return the CONFIGURATION section corresponding to the specified FileTable
     */
    private static String getFileTableSection(int window, boolean left) {
        return getFolderPanelSection(window, left) + "." + FILE_TABLE_SECTION;
    }
    
    private static String getFileTableSortSection(int window, boolean left) {
    	return getFileTableSection(window, left) + "." + SORT;
    }
    
    /**
     * Returns the variable that controls the sort criteria of the file table, in the left or right
     * {@link com.mucommander.ui.main.FolderPanel}.
     *
     * @param left true for the left FolderPanel, false for the right one
     * @return the variable that controls the sort criteria of the file table in the specified FolderPanel
     */
    public static String getFileTableSortByVariable(int window, boolean left) {
    	return getFileTableSortSection(window, left) + "." + SORT_BY;
    }
    
    /**
     * Returns the variable that controls the sort order (ascending\descending) of the file table, 
     * in the left or right {@link com.mucommander.ui.main.FolderPanel}.
     *
     * @param left true for the left FolderPanel, false for the right one
     * @return the variable that controls the sort order of the file table in the specified FolderPanel
     */
    public static String getFileTableSortOrderVariable(int window, boolean left) {
    	return getFileTableSortSection(window, left) + "." + SORT_ORDER;
    }

    /**
     * Returns the CONFIGURATION section corresponding to the specified column in the left or right
     * {@link com.mucommander.ui.main.table.FileTable}.
     *
     * @param column column, see {@link com.mucommander.ui.main.table.Column} for possible values
     * @param left true for the left FileTable, false for the right one
     * @return the CONFIGURATION section corresponding to the specified FileTable
     */
    private static String getColumnSection(int window, Column column, boolean left) {
        return getFileTableSection(window, left) + "." + column.toString().toLowerCase();
    }

    /**
     * Returns the variable that controls the visibility of the specified column, in the left or right
     * {@link com.mucommander.ui.main.table.FileTable}.
     *
     * @param column column, see {@link com.mucommander.ui.main.table.Column} for possible values
     * @param left true for the left FileTable, false for the right one
     * @return the variable that controls the visibility of the specified column
     */
    public static String getShowColumnVariable(int window, Column column, boolean left) {
        return getColumnSection(window, column, left) + "." + SHOW_COLUMN;
    }

    /**
     * Returns the variable that holds the width of the specified column, in the left or right
     * {@link com.mucommander.ui.main.table.FileTable}.
     *
     * @param column column, see {@link com.mucommander.ui.main.table.Column} for possible values
     * @param left true for the left FileTable, false for the right one
     * @return the variable that holds the width of the specified column
     */
    public static String getColumnWidthVariable(int window, Column column, boolean left) {
        return getColumnSection(window, column, left) + "." + COLUMN_WIDTH;
    }
    
    /**
     * Returns the variable that holds the position of the specified column, in the left or right
     * {@link com.mucommander.ui.main.table.FileTable}.
     *
     * @param column column, see {@link com.mucommander.ui.main.table.Column} for possible values
     * @param left true for the left FileTable, false for the right one
     * @return the variable that holds the position of the specified column
     */
    public static String getColumnPositionVariable(int window, Column column, boolean left) {
        return getColumnSection(window, column, left) + "." + COLUMN_POSITION;
    }
    

    /**
     * Returns the CONFIGURATION section corresponding to the specified {@link com.mucommander.ui.main.tabs.FileTableTabs},
     * left or right one.
     *
     * @param left true for the left FileTableTabs, false for the right one
     * @return the CONFIGURATION section corresponding to the specified FileTableTabs
     */    
    private static String getTabsSection(int window, boolean left) {
    	return getFolderPanelSection(window, left) + "." + TABS_SECTION;
    }
    
    /**
     * Returns the variable that holds the number of presented tabs, in the left or right
     * {@link com.mucommander.ui.main.FolderPanel}.
     *
     * @param left true for the left FolderPanel, false for the right one
     * @return the variable that holds the number of presented tabs in the specified FolderPanel
     */
    public static String getTabsCountVariable(int window, boolean left) {
    	return getTabsSection(window, left) + "." + TABS_COUNT;
    }
    
    /**
     * Returns the CONFIGURATION section corresponding to the specified {@link com.mucommander.ui.main.tabs.FileTableTab},
     * left or right one.
     *
     * @param left true for the left FileTableTab, false for the right one
     * @return the CONFIGURATION section corresponding to the specified FileTableTab
     */    
    private static String getTabSection(int window, boolean left, int index) {
    	return getTabsSection(window, left) + "." + TAB + "-" + index; 
    }
    
    /**
     * Returns the variable that holds the location presented at the tab in the given index,
     * in the left or right {@link com.mucommander.ui.main.FolderPanel}.
     *
     * @param left true for the left FolderPanel, false for the right one
     * @param index the index of tab at the FolderPanel's tabs 
     * @return the variable that holds the location presented at the tab in the given index in the specified FolderPanel
     */
    public static String getTabLocationVariable(int window, boolean left, int index) {
    	return getTabSection(window, left, index) + "." + TAB_LOCATION;
    }
    
    
    private static final String ROOT_ELEMENT = "snapshot";
    
	// - Instance fields -----------------------------------------------------
    // -----------------------------------------------------------------------
    private final Configuration configuration;


    /**
     * Prevents instantiation of this class from outside of this package.
     */
    MuSnapshot() {
    	configuration = new Configuration(MuSnapshotFile.getSnapshotFile(), new VersionedXmlConfigurationReaderFactory(),
    			new VersionedXmlConfigurationWriterFactory(ROOT_ELEMENT));
		
		try {
			screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		}
		catch(HeadlessException e) {
			LOGGER.debug("Could not fetch screen size: " + e.getMessage());
		}
    }
    
    /**
     * TODO: change this method such that it will return a more specific API
     */
    Configuration getConfiguration() {
    	return configuration;
    }
    
 // - Configuration reading / writing -------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Loads the muCommander CONFIGURATION.
     * Here, we don't try to convert preferences that were changed between muCommander versions,
     * as those are 'dynamic' preferences and as such can be ignored when the user installs new version.
     * 
     * @throws IOException            if an I/O error occurs.
     * @throws ConfigurationException if a CONFIGURATION related error occurs.
     */
    void read() throws IOException, ConfigurationException {
        VersionedXmlConfigurationReader reader = new VersionedXmlConfigurationReader();
        configuration.read(reader);
    }

    /**
     * Saves the muCommander CONFIGURATION.
     * @throws IOException            if an I/O error occurs.
     * @throws ConfigurationException if a CONFIGURATION related error occurs.
     */
    void write() throws IOException, ConfigurationException {
    	//Clear the configuration before saving to drop preferences which are unused anymore
    	configuration.clear();
    	
    	// Get opened main frames list
    	List<MainFrame> mainFrames = WindowManager.getMainFrames();    	
    	
    	// Save windows count
    	int nbMainFrames = mainFrames.size();
    	configuration.setVariable(WINDOWS_COUNT, nbMainFrames);
    	
    	// Save the index of the selected window
    	int indexOfSelectedWindow = WindowManager.getCurrentWindowIndex();
    	configuration.setVariable(WINDOWS_SELECTION, indexOfSelectedWindow);
    	
    	// Save attributes for each window
    	for (int i=0; i<nbMainFrames; ++i)
    		setFrameAttributes(mainFrames.get(i), i);
    	
        configuration.write();
    }
    
    private void setFrameAttributes(MainFrame mainFrame, int index) {
    	// Save window position, size and screen resolution
        setWindowAttributes(index, mainFrame);
        
        // Save left panel dynamic properties
        setPanelAttributes(index, true, mainFrame.getLeftPanel());

        // Save right panel dynamic properties
        setPanelAttributes(index, false, mainFrame.getRightPanel());

    }
    
    private void setPanelAttributes(int index, boolean isLeft, FolderPanel panel) {
    	// Save tree folders preferences
        setTreeAttributes(index, isLeft, panel);
    	
        setTableAttributes(index, isLeft, panel.getFileTable());
        
        setTabsAttributes(index, isLeft, panel.getTabs().getClonedTabs());
    }

    private void setTabsAttributes(int index, boolean isLeft, List<FileTableTab> tabs) {
    	// Save tabs count
    	configuration.setVariable(getTabsCountVariable(index, isLeft), tabs.size());
    	// Save tabs locations
    	for(int i=0; i<tabs.size(); i++) {
    		FileTableTab tab = tabs.get(i);
    		configuration.setVariable(getTabLocationVariable(index, isLeft, i), tab.getLocation().getAbsolutePath());
    	}
    }
    
    private void setTableAttributes(int index, boolean isLeft, FileTable table) {
    	// Saves table sort order.
    	configuration.setVariable(MuSnapshot.getFileTableSortByVariable(index, isLeft), table.getSortInfo().getCriterion().toString().toLowerCase());
    	configuration.setVariable(MuSnapshot.getFileTableSortOrderVariable(index, isLeft), table.getSortInfo().getAscendingOrder() ? MuSnapshot.SORT_ORDER_ASCENDING : MuSnapshot.SORT_ORDER_DESCENDING);
    	
    	// Loop on columns
		for(Column c : Column.values()) {
			if(c!=Column.NAME) {       // Skip the special name column (always enabled, width automatically calculated)
				MuConfigurations.getSnapshot().setVariable(
						MuSnapshot.getShowColumnVariable(index, c, isLeft),
						table.isColumnEnabled(c)
						);

				MuConfigurations.getSnapshot().setVariable(
						MuSnapshot.getColumnWidthVariable(index, c, isLeft),
						table.getColumnWidth(c)
						);
			}

			MuConfigurations.getSnapshot().setVariable(
					MuSnapshot.getColumnPositionVariable(index, c, isLeft),
					table.getColumnPosition(c)
					);
		}
    }
    
    private void setTreeAttributes(int index, boolean isLeft, FolderPanel panel) {
    	configuration.setVariable(MuSnapshot.getTreeVisiblityVariable(index, isLeft), panel.isTreeVisible());
        configuration.setVariable(MuSnapshot.getTreeWidthVariable(index, isLeft), panel.getTreeWidth());
    }
    
    private void setWindowAttributes(int index, MainFrame currentMainFrame) {
        Rectangle bounds = currentMainFrame.getBounds();
        configuration.setVariable(getX(index), (int)bounds.getX());
        configuration.setVariable(getY(index), (int)bounds.getY());
        configuration.setVariable(getWidth(index), (int)bounds.getWidth());
        configuration.setVariable(getHeight(index), (int)bounds.getHeight());
        
        if (screenSize != null) {
        	configuration.setVariable(MuSnapshot.SCREEN_WIDTH, screenSize.width);
        	configuration.setVariable(MuSnapshot.SCREEN_HEIGHT, screenSize.height);
        }

        // Save split pane orientation
        // Note: the vertical/horizontal terminology used in muCommander is just the opposite of the one used
        // in JSplitPane which is anti-natural / confusing
    	configuration.setVariable(getSplitOrientation(index), currentMainFrame.getSplitPane().getOrientation()==JSplitPane.HORIZONTAL_SPLIT?MuSnapshot.VERTICAL_SPLIT_ORIENTATION:MuSnapshot.HORIZONTAL_SPLIT_ORIENTATION);
    }
}
