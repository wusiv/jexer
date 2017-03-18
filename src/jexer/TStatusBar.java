/*
 * Jexer - Java Text User Interface
 *
 * The MIT License (MIT)
 *
 * Copyright (C) 2016 Kevin Lamonte
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * @author Kevin Lamonte [kevin.lamonte@gmail.com]
 * @version 1
 */
package jexer;

import java.util.ArrayList;

import jexer.bits.CellAttributes;
import jexer.bits.GraphicsChars;
import jexer.event.TCommandEvent;
import jexer.event.TKeypressEvent;
import jexer.event.TMouseEvent;

/**
 * TStatusBar implements a status line with clickable buttons.
 */
public final class TStatusBar extends TWidget {

    /**
     * A single shortcut key.
     */
    private class TStatusBarKey {

        /**
         * The keypress for this action.
         */
        public TKeypress key;

        /**
         * The command to issue.
         */
        public TCommand cmd;

        /**
         * The label text.
         */
        public String label;

        /**
         * If true, the mouse is on this key.
         */
        public boolean selected;

        /**
         * The left edge coordinate to draw this key with.
         */
        public int x = 0;

        /**
         * The width of this key on the screen.
         *
         * @return the number of columns this takes when drawn
         */
        public int width() {
            return this.label.length() + this.key.toString().length() + 3;
        }

        /**
         * Add a key to this status bar.
         *
         * @param key the key to trigger on
         * @param cmd the command event to issue when key is pressed or this
         * item is clicked
         * @param label the label for this action
         */
        public TStatusBarKey(final TKeypress key, final TCommand cmd,
            final String label) {

            this.key    = key;
            this.cmd    = cmd;
            this.label  = label;
        }

    }

    /**
     * Remember mouse state.
     */
    private TMouseEvent mouse;

    /**
     * The text to display on the right side of the shortcut keys.
     */
    private String text = null;

    /**
     * The shortcut keys.
     */
    private ArrayList<TStatusBarKey> keys = new ArrayList<TStatusBarKey>();

    /**
     * Add a key to this status bar.
     *
     * @param key the key to trigger on
     * @param cmd the command event to issue when key is pressed or this item
     * is clicked
     * @param label the label for this action
     */
    public void addShortcutKeypress(final TKeypress key, final TCommand cmd,
        final String label) {

        TStatusBarKey newKey = new TStatusBarKey(key, cmd, label);
        if (keys.size() > 0) {
            TStatusBarKey oldKey = keys.get(keys.size() - 1);
            newKey.x = oldKey.x + oldKey.width();
        }
        keys.add(newKey);
    }

    /**
     * Set the text to display on the right side of the shortcut keys.
     *
     * @param text the new text
     */
    public void setText(final String text) {
        this.text = text;
    }

    /**
     * Public constructor.
     *
     * @param parent parent widget
     * @param text text for the bar on the bottom row
     */
    public TStatusBar(final TWidget parent, final String text) {

        // Set parent and window
        super(parent, false, 0, 0, text.length(), 1);

        this.text = text;
    }

    /**
     * Public constructor.
     *
     * @param parent parent widget
     */
    public TStatusBar(final TWidget parent) {
        this(parent, "");
    }

    /**
     * Draw the bar.
     */
    @Override
    public void draw() {
        CellAttributes barColor = new CellAttributes();
        barColor.setTo(getTheme().getColor("tstatusbar.text"));
        CellAttributes keyColor = new CellAttributes();
        keyColor.setTo(getTheme().getColor("tstatusbar.button"));
        CellAttributes selectedColor = new CellAttributes();
        selectedColor.setTo(getTheme().getColor("tstatusbar.selected"));

        // Status bar is weird.  Its draw() method is called directly by
        // TApplication after everything is drawn, and after
        // Screen.resetClipping().  So at this point we are drawing in
        // absolute coordinates, not relative to our TWindow.
        int row = getScreen().getHeight() - 1;
        int width = getScreen().getWidth();

        getScreen().hLineXY(0, row, width, ' ', barColor);

        int col = 0;
        for (TStatusBarKey key: keys) {
            String keyStr = key.key.toString();
            if (key.selected) {
                getScreen().putCharXY(col++, row, ' ', selectedColor);
                getScreen().putStringXY(col, row, keyStr, selectedColor);
                col += keyStr.length();
                getScreen().putCharXY(col++, row, ' ', selectedColor);
                getScreen().putStringXY(col, row, key.label, selectedColor);
                col += key.label.length();
                getScreen().putCharXY(col++, row, ' ', selectedColor);
            } else {
                getScreen().putCharXY(col++, row, ' ', barColor);
                getScreen().putStringXY(col, row, keyStr, keyColor);
                col += keyStr.length() + 1;
                getScreen().putStringXY(col, row, key.label, barColor);
                col += key.label.length();
                getScreen().putCharXY(col++, row, ' ', barColor);
            }
        }
        if (text.length() > 0) {
            if (keys.size() > 0) {
                getScreen().putCharXY(col++, row, GraphicsChars.VERTICAL_BAR,
                    barColor);
            }
            getScreen().putCharXY(col++, row, ' ', barColor);
            getScreen().putStringXY(col, row, text, barColor);
        }
    }

    /**
     * Handle keypresses.
     *
     * @param keypress keystroke event
     * @return true if this keypress was consumed
     */
    public boolean statusBarKeypress(final TKeypressEvent keypress) {
        for (TStatusBarKey key: keys) {
            if (keypress.equals(key.key)) {
                getApplication().postMenuEvent(new TCommandEvent(key.cmd));
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the mouse is currently on the button.
     *
     * @param statusBarKey the status bar item
     * @return if true the mouse is currently on the button
     */
    private boolean mouseOnShortcut(final TStatusBarKey statusBarKey) {
        if ((mouse != null)
            && (mouse.getAbsoluteY() == getApplication().getDesktopBottom())
            && (mouse.getAbsoluteX() >= statusBarKey.x)
            && (mouse.getAbsoluteX() < statusBarKey.x + statusBarKey.width())
        ) {
            return true;
        }
        return false;
    }

    /**
     * Handle mouse button presses.
     *
     * @param mouse mouse button event
     * @return true if this mouse event was consumed
     */
    public boolean statusBarMouseDown(final TMouseEvent mouse) {
        this.mouse = mouse;

        for (TStatusBarKey key: keys) {
            if ((mouseOnShortcut(key)) && (mouse.isMouse1())) {
                key.selected = true;
                return true;
            }
        }
        return false;
    }

    /**
     * Handle mouse button releases.
     *
     * @param mouse mouse button release event
     * @return true if this mouse event was consumed
     */
    public boolean statusBarMouseUp(final TMouseEvent mouse) {
        this.mouse = mouse;

        for (TStatusBarKey key: keys) {
            if (key.selected && mouse.isMouse1()) {
                key.selected = false;

                // Dispatch the event
                getApplication().postMenuEvent(new TCommandEvent(key.cmd));
                return true;
            }
        }
        return false;
    }

    /**
     * Handle mouse movements.
     *
     * @param mouse mouse motion event
     */
    public void statusBarMouseMotion(final TMouseEvent mouse) {
        this.mouse = mouse;

        for (TStatusBarKey key: keys) {
            if (!mouseOnShortcut(key)) {
                key.selected = false;
            }
        }
    }

}
