/*
 * Copyright (c) 2018-2020 Pavel Barykin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.pnbarx.idea.treecolor.ui.components;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.JBColor;
import dev.pnbarx.idea.treecolor.utils.UIUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;


public abstract class ColoredButton extends JButton {

    private static final Logger LOG = Logger.getInstance(ColoredButton.class);

    private static final int hPadding = 15;
    private static final int vPadding = 12;

    private Color backgroundColor;
    private Color foregroundColor;

    public ColoredButton(String text, @Nullable Color backgroundColor) {
        super(text);
        setBackground(backgroundColor);
        setUI(createUI());
        setOpaque(false);
        setRolloverEnabled(true);
        setBorder(BorderFactory.createEmptyBorder(vPadding, hPadding, vPadding, hPadding));
        addActionListener(this::onClick);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    onRightClick(e);
                }
                super.mouseClicked(e);
            }
        });
    }

    protected void onClick(ActionEvent e) {
    }

    protected void onRightClick(MouseEvent e) {
    }

    @Override
    public Color getBackground() {
        return backgroundColor;
    }

    @Override
    public void setBackground(@Nullable Color color) {
        backgroundColor = color != null ? color : UIUtils.getDefaultTreeBackgroundColor();
        foregroundColor = ColorUtil.isDark(backgroundColor) ? Color.WHITE : Color.BLACK;
    }

    @Override
    public Color getForeground() {
        return foregroundColor;
    }

    protected ButtonUI createUI() {
        return new ColoredButtonUI();
    }

    protected Color getButtonBorderColor() {
        return new JBColor(0x999999, 0x777777);
    }

    protected Color getButtonFocusedBorderColor() {
        return new JBColor(0x555555, 0xaaaaaa);
    }

    protected float getButtonBorderWidth() {
        return 3.0f;
    }

    protected float getButtonBorderTopWidth() {
        return getButtonBorderWidth();
    }

    protected float getButtonBorderRightWidth() {
        return getButtonBorderWidth();
    }

    protected float getButtonBorderBottomWidth() {
        return getButtonBorderWidth();
    }

    protected float getButtonBorderLeftWidth() {
        return getButtonBorderWidth();
    }

    protected int getButtonArcSize() {
        return 20;
    }


    protected static class ColoredButtonUI extends BasicButtonUI {

        private static final float fontSize = 12.0f;

        @Override
        protected void installDefaults(final AbstractButton button) {
            super.installDefaults(button);
            button.setFont(UIManager.getFont("Button.font").deriveFont(Font.BOLD, fontSize));
        }

        @Override
        public void paint(@NotNull final Graphics g, final JComponent c) {

            Graphics2D g2d = (Graphics2D) g.create();
            ColoredButton button = (ColoredButton) c;

            try {
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

                int width = button.getWidth();
                int height = button.getHeight();
                int arcSize = getArcSize(button);
                float topBorderWidth = button.getButtonBorderTopWidth();
                float rightBorderWidth = button.getButtonBorderRightWidth();
                float bottomBorderWidth = button.getButtonBorderBottomWidth();
                float leftBorderWidth = button.getButtonBorderLeftWidth();

                if (button.isOpaque()) {
                    g2d.setColor(button.getBackground());
                    g2d.fill(new Rectangle(button.getSize()));
                }

                Shape outerShape = new RoundRectangle2D.Float(0, 0, width, height, arcSize, arcSize);

                ButtonModel model = button.getModel();
                g2d.setColor(getBackgroundColor(button));
                g2d.fill(outerShape);

                g2d.setColor(model.isRollover() ? getFocusedBorderColor(button) : getBorderColor(button));
                if (topBorderWidth > 0f) {
                    g2d.fill(new Rectangle(0, 0, width, Math.round(topBorderWidth)));
                }
                if (rightBorderWidth > 0f) {
                    g2d.fill(new Rectangle(width - Math.round(rightBorderWidth), 0, Math.round(rightBorderWidth), height));
                }
                if (bottomBorderWidth > 0f) {
                    g2d.fill(new Rectangle(0, height - Math.round(bottomBorderWidth), width, Math.round(bottomBorderWidth)));
                }
                if (leftBorderWidth > 0f) {
                    g2d.fill(new Rectangle(0, 0, Math.round(leftBorderWidth), height));
                }

            } finally {
                g2d.dispose();
            }

            super.paint(g, button);
        }

        protected Color getBackgroundColor(@NotNull ColoredButton button) {
            return button.getBackground();
        }

        protected Color getBorderColor(@NotNull ColoredButton button) {
            return button.getButtonBorderColor();
        }

        protected Color getFocusedBorderColor(@NotNull ColoredButton button) {
            return button.getButtonFocusedBorderColor();
        }

        protected int getArcSize(@NotNull ColoredButton button) {
            return button.getButtonArcSize();
        }
    }

}
