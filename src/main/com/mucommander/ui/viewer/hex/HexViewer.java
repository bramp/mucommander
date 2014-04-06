/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2014 Oleg Trifonov
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
package com.mucommander.ui.viewer.hex;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.text.Translator;
import com.mucommander.ui.helper.MenuToolkit;
import com.mucommander.ui.helper.MnemonicHelper;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeManager;
import com.mucommander.ui.viewer.FileViewer;
import com.mucommander.ui.viewer.text.*;
import ru.trolsoft.hexeditor.data.AbstractByteBuffer;
import ru.trolsoft.hexeditor.data.MuCommanderByteBuffer;
import ru.trolsoft.hexeditor.events.OnOffsetChangeListener;
import ru.trolsoft.hexeditor.ui.HexTable;
import ru.trolsoft.hexeditor.ui.ViewerHexTableModel;

import javax.swing.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

/**
 * Hex dump viewer
 */
public class HexViewer extends FileViewer {

    private HexTable hexTable;
    private ViewerHexTableModel model;
    private AbstractByteBuffer byteBuffer;
    private StatusBar statusBar;

    private JMenu menuView;
    private JMenuItem gotoItem;
    private JMenuItem searchItem;

    public HexViewer() {
        super();

        MnemonicHelper menuMnemonicHelper = new MnemonicHelper();
        menuView = MenuToolkit.addMenu(Translator.get("hex_viewer.view"), menuMnemonicHelper, null);

        gotoItem = MenuToolkit.addMenuItem(menuView, Translator.get("hex_viewer.goto"), menuMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_G, getCtrlOrMetaMask()), this);
        searchItem = MenuToolkit.addMenuItem(menuView, Translator.get("hex_viewer.search"), menuMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F, getCtrlOrMetaMask()), this);
    }

    private int getCtrlOrMetaMask() {
        if (OsFamily.getCurrent() != OsFamily.MAC_OS_X) {
            return KeyEvent.CTRL_MASK;
        } else {
            return KeyEvent.META_MASK;
        }
    }

    private OnOffsetChangeListener onOffsetChangeListener = new OnOffsetChangeListener() {
        @Override
        public void onChange(long offset) {
            if (statusBar != null) {
                statusBar.setOffset(offset);
                try {
                    statusBar.setByteValue(byteBuffer.getByte(offset));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void show(AbstractFile file) throws IOException {
        try {
            byteBuffer = new MuCommanderByteBuffer(file);
            model = new ViewerHexTableModel(byteBuffer);
            model.load();
            hexTable = new HexTable(model);
            hexTable.setBackground(ThemeManager.getCurrentColor(Theme.EDITOR_BACKGROUND_COLOR));
            hexTable.setForeground(ThemeManager.getCurrentColor(Theme.EDITOR_FOREGROUND_COLOR));
            //hexTable.setAlternateBackground(ThemeManager.getCurrentColor(Theme.EDITOR_CURRENT_BACKGROUND_COLOR));
            hexTable.setAlternateBackground(new Color(20, 20, 20));
            hexTable.setOffsetColomnColor(new Color(0, 255, 255));
            hexTable.setAsciiColumnColor(new Color(255, 0, 255));
            hexTable.setHighlightSelectionInAsciiDumpColor(new Color(0, 0, 255));
            hexTable.setAlternateRowBackground(true);

            hexTable.setFont(new Font("Monospaced", Font.PLAIN, 14));
            hexTable.getTableHeader().setFont(new Font("Monospaced", Font.PLAIN, 12));

            hexTable.setOnOffsetChangeListener(onOffsetChangeListener);
            onOffsetChangeListener.onChange(0);

            statusBar.setMaxOffset(file.getSize() - 1);

            setComponentToPresent(hexTable);
            getViewport().setBackground(hexTable.getBackground());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected StatusBar getStatusBar() {
        statusBar = new StatusBar();
        return statusBar;
    }

    @Override
    public JMenuBar getMenuBar() {
        JMenuBar menuBar = super.getMenuBar();
        menuBar.add(menuView);
        setMainKeyListener(this, menuBar);
        return menuBar;
    }

    @Override
    protected void saveStateOnClose() {
        try {
            byteBuffer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void restoreStateOnStartup() {

    }


    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == gotoItem && gotoItem.isEnabled()) {
            System.out.println("goto");
        } else if (source == searchItem && searchItem.isEnabled()) {
            FindDialog findDialog = new FindDialog(getFrame()) {
                @Override
                protected void doSearch(String text) {

                }
            };
            findDialog.showDialog();
        } else {
            super.actionPerformed(e);
        }
    }
}