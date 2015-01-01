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

package com.mucommander.ui.button;

import com.mucommander.ui.icon.IconManager;

import javax.swing.*;

/**
 * ArrowButton is a button displaying an arrow icon pointing to a specified direction (up/down/left/right).
 * The direction of the arrow can be changed at any time using {@link #setArrowDirection(Direction)}.
 *
 * @author Maxence Bernard
 */
public class ArrowButton extends JButton {

    public enum Direction {
        UP("arrow_up.png"),
        DOWN("arrow_down.png"),
        LEFT("arrow_left.png"),
        RIGHT("arrow_right.png");

        private final String fileName;

        Direction(String fileName) {
            this.fileName = fileName;
        }
    }


    /**
     * Creates a new ArrowButton with no initial arrow icon.
     */
    public ArrowButton() {
        super();
    }

    /**
     * Creates a new ArrowButton showing an arrow icon pointing to the specified direction.
     */
    public ArrowButton(Direction direction) {
        setArrowDirection(direction);
    }

    /**
     * Creates a new ArrowButton using the specified Action and showing an arrow icon pointing to the specified direction.
     */
    public ArrowButton(Action action, Direction direction) {
        super(action);

        setArrowDirection(direction);
    }


    /**
     * Changes the direction of the arrow icon to the specified one.
     *
     * @param direction can have one of the following values: {@link Direction#UP}, {@link Direction#DOWN},
     * {@link Direction#LEFT} or {@link Direction#RIGHT}
     */
    public void setArrowDirection(Direction direction) {
        setIcon(IconManager.getIcon(IconManager.IconSet.COMMON, direction.fileName));
    }
}
