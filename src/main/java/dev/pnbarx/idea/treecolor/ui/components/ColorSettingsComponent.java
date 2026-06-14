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

import com.intellij.icons.AllIcons;
import com.intellij.util.ui.JBUI;
import dev.pnbarx.idea.treecolor.state.DefaultSettings;
import dev.pnbarx.idea.treecolor.state.ProjectColors;
import com.intellij.ide.ui.LafManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.*;
import dev.pnbarx.idea.treecolor.state.beans.ColorSettings;
import dev.pnbarx.idea.treecolor.utils.ColorVariantUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;

public class ColorSettingsComponent extends JPanel {

    private static final Logger LOG = Logger.getInstance(ColorSettingsComponent.class);
    private static final int SWATCH_SIZE = 54;
    private static final int LINK_BUTTON_SIZE = 18;
    private static final float TARGET_MODE_BORDER_WIDTH = 10.0f;
    private static final Color LIGHT_MODE_BORDER = new Color(0xe0e0e0);
    private static final Color LIGHT_MODE_FOCUSED_BORDER = new Color(0xffffff);
    private static final Color DARK_MODE_BORDER = new Color(0x2b2b2b);
    private static final Color DARK_MODE_FOCUSED_BORDER = new Color(0x111111);

    private final ProjectColors colors;
    private final int colorId;
    private ColorSettings colorSettings;

    private final JPanel headerPanel;
    private final JPanel headerControlsPanel;
    private final JLabel colorNameLabel;
    private final ColorChooserButton lightColorChooserButton;
    private final ColorChooserButton darkColorChooserButton;
    private final JToggleButton linkVariantsButton;
    private final JButton resetColorButton;
    private final JCheckBox enabledCheckbox;

    private enum ThemeVariant {
        LIGHT,
        DARK
    }

    public ColorSettingsComponent(ProjectColors colors, int colorId) {
        super(new BorderLayout(0, 6));

        this.colors = colors;
        this.colorId = colorId;
        this.colorSettings = colors.getColorSettingsById(colorId);

        headerPanel = new JPanel(new BorderLayout(4, 0));
        headerPanel.setBorder(new EmptyBorder(2, 0, 0, 0));
        headerControlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        headerControlsPanel.setOpaque(false);

        colorNameLabel = new JLabel("", SwingConstants.CENTER);
        colorNameLabel.setBorder(new EmptyBorder(0, 0, 0, 0));

        lightColorChooserButton = new ColorChooserButton(ThemeVariant.LIGHT);
        darkColorChooserButton = new ColorChooserButton(ThemeVariant.DARK);
        Dimension swatchDimension = new Dimension(SWATCH_SIZE, SWATCH_SIZE);
        lightColorChooserButton.setMinimumSize(swatchDimension);
        lightColorChooserButton.setPreferredSize(swatchDimension);
        lightColorChooserButton.setMaximumSize(swatchDimension);
        lightColorChooserButton.setToolTipText("Light color");
        darkColorChooserButton.setMinimumSize(swatchDimension);
        darkColorChooserButton.setPreferredSize(swatchDimension);
        darkColorChooserButton.setMaximumSize(swatchDimension);
        darkColorChooserButton.setToolTipText("Dark color");

        linkVariantsButton = new JToggleButton(AllIcons.Ide.Readwrite);
        linkVariantsButton.setSelectedIcon(AllIcons.Ultimate.Lock);
        linkVariantsButton.setDisabledIcon(IconLoader.getDisabledIcon(AllIcons.Ide.Readwrite));
        linkVariantsButton.setDisabledSelectedIcon(IconLoader.getDisabledIcon(AllIcons.Ultimate.Lock));
        linkVariantsButton.setSelected(true);
        linkVariantsButton.setFocusable(false);
        linkVariantsButton.setToolTipText("Link light and dark colors");
        linkVariantsButton.setMargin(new Insets(0, 0, 0, 0));
        linkVariantsButton.setBorderPainted(false);
        linkVariantsButton.setContentAreaFilled(false);
        linkVariantsButton.setOpaque(false);
        linkVariantsButton.setFocusPainted(false);
        linkVariantsButton.setMinimumSize(new Dimension(LINK_BUTTON_SIZE, LINK_BUTTON_SIZE));
        linkVariantsButton.setPreferredSize(new Dimension(LINK_BUTTON_SIZE, LINK_BUTTON_SIZE));
        linkVariantsButton.setMaximumSize(new Dimension(LINK_BUTTON_SIZE, LINK_BUTTON_SIZE));
        linkVariantsButton.addActionListener(this::linkVariantsButtonHandle);

        resetColorButton = new JButton(AllIcons.Actions.Rollback);
        resetColorButton.setDisabledIcon(IconLoader.getDisabledIcon(AllIcons.Actions.Rollback));
        resetColorButton.setToolTipText("Reset this color to factory defaults");
        resetColorButton.setFocusable(false);
        resetColorButton.setBorderPainted(false);
        resetColorButton.setContentAreaFilled(false);
        resetColorButton.setOpaque(false);
        resetColorButton.setFocusPainted(false);
        resetColorButton.setMargin(JBUI.emptyInsets());
        resetColorButton.setMinimumSize(new Dimension(LINK_BUTTON_SIZE, LINK_BUTTON_SIZE));
        resetColorButton.setPreferredSize(new Dimension(LINK_BUTTON_SIZE, LINK_BUTTON_SIZE));
        resetColorButton.setMaximumSize(new Dimension(LINK_BUTTON_SIZE, LINK_BUTTON_SIZE));
        resetColorButton.addActionListener(this::resetColorButtonHandle);

        enabledCheckbox = new JCheckBox("", colorSettings.isEnabled());
        enabledCheckbox.setHorizontalAlignment(JCheckBox.CENTER);
        enabledCheckbox.setVerticalAlignment(JCheckBox.CENTER);
        enabledCheckbox.setFont(UIManager.getFont("Button.font").deriveFont(Font.PLAIN, 10));
        enabledCheckbox.setFocusable(false);
        enabledCheckbox.setToolTipText("Enabled");
        enabledCheckbox.addActionListener(this::enabledCheckboxHandle);
        enabledCheckbox.setBorder(new EmptyBorder(0, 0, 0, 0));
        enabledCheckbox.setMargin(new Insets(0, 0, 0, 0));

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 0, 0));
        buttonPanel.add(lightColorChooserButton);
        buttonPanel.add(darkColorChooserButton);

        headerControlsPanel.add(linkVariantsButton);
        headerControlsPanel.add(resetColorButton);
        headerControlsPanel.add(enabledCheckbox);

        headerPanel.add(colorNameLabel, BorderLayout.CENTER);
        headerPanel.add(headerControlsPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        renderColorSettings();
    }

    public void applyColorSettings() {
        renderColorSettings();
        colors.setColorSettingsById(colorId, colorSettings);
    }

    private void renderColorSettings() {
        boolean isColorEnabled = colorSettings.isEnabled();
        colorNameLabel.setText(colorSettings.getName());
        colorNameLabel.setEnabled(isColorEnabled);
        linkVariantsButton.setEnabled(isColorEnabled);
        resetColorButton.setEnabled(isColorEnabled);
        linkVariantsButton.setSelected(colorSettings.isLinked());
        refreshColorButtons(isColorEnabled);
        enabledCheckbox.setSelected(isColorEnabled);
        LOG.debug("SET LIGHT/DARK BUTTON COLORS TO " + colorSettings.getLightColor() + " / " + colorSettings.getDarkColor());
    }

    private void refreshColorButtons(boolean isColorEnabled) {
        lightColorChooserButton.displayVariantColor(getVariantColor(ThemeVariant.LIGHT), !isColorEnabled);
        darkColorChooserButton.displayVariantColor(getVariantColor(ThemeVariant.DARK), !isColorEnabled);
        updateLinkedModeLockState(isColorEnabled);
        repaint();
    }

    public void reloadColorSettings() {
        colorSettings = colors.getColorSettingsById(colorId);
    }

    private void enabledCheckboxHandle(ActionEvent actionEvent) {
        colorSettings.setEnabled(enabledCheckbox.isSelected());
        applyColorSettings();
    }

    private void resetColorButtonHandle(ActionEvent actionEvent) {
        ColorSettings defaultColorSettings = DefaultSettings.getColorSettingsList().stream()
            .filter(item -> item.getId() == colorId)
            .findFirst()
            .orElse(new ColorSettings(colorId, null, "Color " + colorId, true));

        colorSettings.setLightColor(defaultColorSettings.getLightColor());
        colorSettings.setDarkColor(defaultColorSettings.getDarkColor());
        colorSettings.setName(defaultColorSettings.getName());
        colorSettings.setEnabled(defaultColorSettings.isEnabled());
        colorSettings.setLinked(defaultColorSettings.isLinked());
        applyColorSettings();
    }

    private void linkVariantsButtonHandle(ActionEvent actionEvent) {
        colorSettings.setLinked(linkVariantsButton.isSelected());
        if (linkVariantsButton.isSelected()) {
            ThemeVariant currentThemeVariant = getCurrentThemeVariant();
            ThemeVariant oppositeThemeVariant = getOppositeThemeVariant(currentThemeVariant);
            Color currentThemeColor = getVariantColor(currentThemeVariant);
            if (currentThemeColor != null) {
                setVariantColor(oppositeThemeVariant, oppositeThemeVariant == ThemeVariant.DARK
                    ? ColorVariantUtil.deriveDarkColor(currentThemeColor)
                    : ColorVariantUtil.deriveLightColor(currentThemeColor));
            }
        }
        applyColorSettings();
    }

    private ThemeVariant getCurrentThemeVariant() {
        return LafManager.getInstance().getCurrentUIThemeLookAndFeel().isDark() ? ThemeVariant.DARK : ThemeVariant.LIGHT;
    }

    @NotNull
    private static ThemeVariant getOppositeThemeVariant(@NotNull ThemeVariant themeVariant) {
        return themeVariant == ThemeVariant.DARK ? ThemeVariant.LIGHT : ThemeVariant.DARK;
    }

    @Nullable
    private Color getVariantColor(@NotNull ThemeVariant themeVariant) {
        return themeVariant == ThemeVariant.DARK ? colorSettings.getDarkColor() : colorSettings.getLightColor();
    }

    private void setVariantColor(@NotNull ThemeVariant themeVariant, @Nullable Color color) {
        if (themeVariant == ThemeVariant.DARK) {
            colorSettings.setDarkColor(color);
        } else {
            colorSettings.setLightColor(color);
        }
    }

    private void applyVariantColorChange(@NotNull ThemeVariant editedVariant, @Nullable Color editedColor) {
        if (linkVariantsButton.isSelected() && editedVariant != getCurrentThemeVariant()) {
            return;
        }

        setVariantColor(editedVariant, editedColor);

        if (linkVariantsButton.isSelected() && editedColor != null) {
            ThemeVariant currentThemeVariant = getCurrentThemeVariant();
            if (editedVariant == currentThemeVariant) {
                ThemeVariant oppositeThemeVariant = getOppositeThemeVariant(editedVariant);
                setVariantColor(oppositeThemeVariant, oppositeThemeVariant == ThemeVariant.DARK
                    ? ColorVariantUtil.deriveDarkColor(editedColor)
                    : ColorVariantUtil.deriveLightColor(editedColor));
            }
        }

        colors.setColorSettingsById(colorId, colorSettings);
        refreshColorButtons(colorSettings.isEnabled());
    }

    private void updateLinkedModeLockState(boolean isColorEnabled) {
        if (!isColorEnabled) {
            lightColorChooserButton.setVariantLocked(false);
            darkColorChooserButton.setVariantLocked(false);
            lightColorChooserButton.setEnabled(false);
            darkColorChooserButton.setEnabled(false);
            return;
        }

        lightColorChooserButton.setEnabled(true);
        darkColorChooserButton.setEnabled(true);

        if (!linkVariantsButton.isSelected()) {
            lightColorChooserButton.setVariantLocked(false);
            darkColorChooserButton.setVariantLocked(false);
            return;
        }

        ThemeVariant editableThemeVariant = getCurrentThemeVariant();
        lightColorChooserButton.setVariantLocked(editableThemeVariant != ThemeVariant.LIGHT);
        darkColorChooserButton.setVariantLocked(editableThemeVariant != ThemeVariant.DARK);
    }

    private class ColorChooserButton extends ColoredButton {

        private final ThemeVariant themeVariant;
        private boolean variantLocked = false;

        protected ColorChooserButton(@NotNull ThemeVariant themeVariant) {
            super(themeVariant == ThemeVariant.DARK ? "Dark" : "Light", null);
            this.themeVariant = themeVariant;
        }

        @Override
        protected Color getButtonBorderColor() {
            return themeVariant == ThemeVariant.DARK ? DARK_MODE_BORDER : LIGHT_MODE_BORDER;
        }

        @Override
        protected Color getButtonFocusedBorderColor() {
            if (variantLocked) {
                return getButtonBorderColor();
            }
            return themeVariant == ThemeVariant.DARK ? DARK_MODE_FOCUSED_BORDER : LIGHT_MODE_FOCUSED_BORDER;
        }

        @Override
        protected float getButtonBorderWidth() {
            return TARGET_MODE_BORDER_WIDTH;
        }

        @Override
        protected float getButtonBorderRightWidth() {
            return themeVariant == ThemeVariant.LIGHT ? 0f : super.getButtonBorderRightWidth();
        }

        @Override
        protected float getButtonBorderLeftWidth() {
            return themeVariant == ThemeVariant.DARK ? 0f : super.getButtonBorderLeftWidth();
        }

        @Override
        protected int getButtonArcSize() {
            return 0;
        }

        @Override
        public Color getForeground() {
            return themeVariant == ThemeVariant.DARK ? Color.WHITE : Color.BLACK;
        }

        private void setVariantLocked(boolean isLocked) {
            variantLocked = isLocked;
        }

        private void displayVariantColor(@Nullable Color color, boolean dimmed) {
            setBackground(dimmed ? toDisabledColor(color) : color);
        }

        @Nullable
        private Color toDisabledColor(@Nullable Color color) {
            if (color == null) return null;
            int gray = (int) (color.getRed() * 0.299 + color.getGreen() * 0.587 + color.getBlue() * 0.114);
            int clampedGray = Math.max(0, Math.min(255, (gray + 170) / 2));
            return new Color(clampedGray, clampedGray, clampedGray, color.getAlpha());
        }

        @Override
        protected void onClick(ActionEvent e) {
            if (variantLocked) {
                return;
            }

            List<ColorPickerListener> listeners = Collections.singletonList(new ColorChangeListener(themeVariant));
            Color currentColor = getVariantColor(themeVariant);
            Color newColor = ColorChooserService.getInstance().showDialog(this, "Choose Color", currentColor, true, listeners, true);

            if (newColor != null) {
                applyVariantColorChange(themeVariant, newColor);
                colorSettings.setEnabled(true);
            }
            ColorSettingsComponent.this.applyColorSettings();
        }

        @Override
        protected void onRightClick(MouseEvent e) {
            new ColorNameDialog(ColorSettingsComponent.this).show();
        }
    }


    private class ColorNameDialog extends DialogWrapper {

        private JTextField colorNameInput;

        public ColorNameDialog(Component parent) {
            super(parent, true);
            setTitle("Color Name");
            init();
        }

        @Nullable
        @Override
        protected JComponent createCenterPanel() {
            JPanel dialogPanel = new JPanel(new BorderLayout());
            colorNameInput = new JTextField(colorSettings.getName());
            colorNameInput.setPreferredSize(new Dimension(370, 35));
            dialogPanel.add(colorNameInput);
            return dialogPanel;
        }

        @Override
        public JComponent getPreferredFocusedComponent() {
            return colorNameInput;
        }

        @Override
        protected void doOKAction() {
            colorSettings.setName(colorNameInput.getText().trim());
            ColorSettingsComponent.this.applyColorSettings();
            super.doOKAction();
        }
    }


    private class ColorChangeListener implements ColorPickerListener {

        private final ThemeVariant themeVariant;

        private ColorChangeListener(@NotNull ThemeVariant themeVariant) {
            this.themeVariant = themeVariant;
        }

        @Override
        public void colorChanged(Color color) {
            applyVariantColorChange(themeVariant, color);
        }

        @Override
        public void closed(@Nullable Color color) {
        }
    }

}
